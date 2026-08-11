# DEV NOTES


## Agent / CI 빌드 검증

Agent shell·일부 CI에는 PATH `npm`이 없다. **Vue 빌드는 Gradle Node로 돌린다** (`build.gradle` → `com.github.node-gradle.node`, `nodeProjectDir = app/frontend-vue`).

Java 빌드 계약 (2026-07-19): Gradle wrapper를 실행한 IDE·셸 JBR과 무관하게 Java compiler toolchain과 Gradle Daemon JVM은 17을 사용한다. `build.gradle`의 `java.toolchain`과 `gradle/gradle-daemon-jvm.properties`가 저장소 SSOT이며, 빌드 머신에는 JDK 17이 설치되어 있어야 한다. Lombok 1.18.42는 IntelliJ IDEA 2026.1.1의 JBR 25와 호환하고, `lombok.config`는 버전 갱신 전의 JaCoCo·Jackson annotation 생성 계약을 보존한다.

| 목적 | 명령 |
|------|------|
| Vue 프로덕션 빌드 | `./gradlew buildFrontend` |
| npm install | `./gradlew npmInstall` |
| 기타 package.json 스크립트 | `./gradlew npm_run_<script>` (`type-check` → `npm_run_type_check`) |

gradlew 폴백 (2026-07-10): 일부 agent shell 에서는 `./gradlew` 가 데몬 loopback 연결 실패(`Unable to establish loopback connection`)로 아예 동작하지 않는다 — IntelliJ·일반 터미널에서는 정상이므로 셸 환경 한정 문제다. 이때는 Gradle 이 받아둔 `.gradle/nodejs/node-v<버전>-win-x64` 를 PATH 에 추가하고 `app/frontend-vue` 에서 `npm run build` / `npm run test` 를 직접 실행한다 — `buildFrontend` 와 동일 Node 버전·동일 스크립트라 게이트 등가. Java 작업은 폴백 대상이 아니며, JDK 17을 설치해 Gradle Daemon JVM 기준을 충족해야 한다.


백엔드 테스트·배포 게이트 (`gradle/testconfig.gradle`, `Dockerfile`, 2026-07-05):
- `ignoreFailures` 제거 — `./gradlew test` / `check` / `build` 는 테스트 실패 시 중단된다. (`bootJar` 는 `test` 비의존.)
- JaCoCo 하한: LINE·INSTRUCTION 9% (측정 baseline ~10.5%, 회귀 차단용).
- Docker 런타임: `eclipse-temurin:17-jre-jammy` — build.gradle Java 17 과 일치.

인코딩 게이트: `python scripts/check_encoding.py` (`npm run check:encoding`과 동일).

에이전트용 상세: `.cursor/rules/agent-build-toolchain.mdc`

---

## 공통 인코딩 게이트

- `npm run check:encoding` → `scripts/check_encoding.py`. 실패 시 **해당 변경 묶음 전체 폐기·되돌림**(부분 통과 없음). `scripts/`에는 검증 외 자동 수정 도구를 두지 않는다(`scripts/README.md`).

---

## 문서 기록 원칙

- **남길 것**: 아키텍처·SSOT·마이그레이션 방향, 제거·합의된 단일 경로, 되돌리기 어려운 계약, 운영·보안상 필수 결정.
- **남기지 말 것**: 자잘한 리네임·한 줄 수정·매 커밋 단위 “변경 로그”용 `.md` 양산.
- **같은 주제·같은 마이그레이션**은 이 파일(또는 `DESIGN_NOTES.md`) 안에서 정리한다. Phase별로 파일을 새로 만들지 말고, 필요하면 **한 문서에 `### Phase N` 섹션**만 둔다.
- UTF-8 깨짐이 보이면 **일괄 스크립트·전역 치환으로 복구하지 말고** 해당 범위에서 작업을 중단한다. (연쇄 손상 방지.)

---

## 설정 네임스페이스

- `spring.*` 아래에는 Spring Boot/Spring Security가 직접 해석하는 공식 설정만 둔다. 앱 코드가 임의로 읽는 커스텀 설정은 `spring.*`에 새로 추가하지 않는다.
- DreamDiary 앱 런타임 설정은 `app.*`로 수렴한다. 인증 커스텀 설정은 `app.auth.*`를 사용한다.
- 외부 연동 커스텀 설정은 `app.integration.*`를 사용한다.
- 커스텀 `app.*` 설정을 Java 코드에서 읽을 때는 새 `@Value`를 늘리지 않고, 책임 경계의 `*Properties` 클래스에 `@ConfigurationProperties`로 바인딩한다.
- Spring Boot/외부 라이브러리 공식 namespace(`spring.*`, `server.*`, `springdoc.*`)는 공식 설정 계약을 유지한다. 앱 코드가 소수 값을 직접 참조해야 하는 경우에도 별도 `app.*` 래핑을 기본값으로 만들지 않는다.
- JWT 직접 발급 설정은 `app.auth.jwt.*`, refresh token 유지 시간은 `app.auth.refresh-token.*`에 둔다. 사용자 체감 세션 유지 시간처럼 재기동 없이 바뀌어야 하는 정책은 yml/env가 아니라 `auth_policy`를 SSOT로 둔다.

## Spring Cache 저장소 선택

- 일반 `@Cacheable` 메소드는 Ehcache 메모리 저장소를 사용한다. 캐시 namespace는 `config/ehcache/ehcache.xml`에 등록되어 있어야 하며, 누락 시 no-op 캐시로 감추지 않고 오류로 처리한다.
- Redis 공유 캐시는 메소드 또는 대상 클래스에 `@CacheableConfig(cacheTarget = SHARED|MEMORY_AND_SHARED)`가 명시된 경우에만 사용한다. `MEMORY_AND_SHARED`는 Redis 장애 시 사용 가능한 Ehcache를 유지하며, `SHARED` 단독 설정은 Redis 장애 시 명시적으로 실패한다.
- Redis 기반 refresh token·인증 코드 저장은 Spring Cache resolver와 별도 계약이다. 일반 `@Cacheable` 조회는 Redis 연결 확인을 수행하지 않는다.

---

## 패키지 구조

- "패키지는 파일 묶음이 아니라 책임 경계다."
- `feature`는 사용자 시나리오와 비즈니스 규칙에만 집중한다.
- "`global` 상수는 `auth`/`feature`/`infrastructure`를 import하면 안 된다."
- "`feature`-`infrastructure` 계층간 협력은 구현체가 아니라 명시적 계약(포트/서비스)으로 연결한다."

---

## 날짜·시간 처리 — java.time 수렴 (Date → LocalDateTime)

- **현황 (2026-07-03 진단)**: 백엔드 날짜 축은 `global/util/date`(DateUtils·DateParser·DatePtn·ChineseCalModule·DayOfWeek, 733줄). `DateUtils` 호출 106파일, `java.util.Date` 사용 79파일, **엔티티 날짜 필드 33건 전부 `Date`**(java.time 필드 0건). 전면 전환은 대수술이라 3단계로 분할한다.
- **계약**: `DatePtn` 은 SimpleDateFormat 인스턴스를 **스레드 간 공유하지 않는다** — `format(Date)`/`parse(String)` 호출마다 새 인스턴스를 만든다. 파싱은 기존 SimpleDateFormat 의 **lenient 계약**(예: 13월 허용)과 `'S'` 밀리초 표기를 유지한다. `DateTimeFormatter` 전환은 이 해석 계약 변경을 수반하므로 Phase 1 에서 하지 않았다.
- 유틸의 외부 라이브러리 상속 패턴(FileUtils→commons-io `479e64175`, DateUtils→commons-lang3)은 전부 제거됨 — 새 유틸에서 재도입하지 말 것.

### Phase 1 — 내부 현대화 (완료, `332781c31`)

- `DatePtn`: enum 상수 필드로 공유하던 SimpleDateFormat 16개 제거 → `format`/`parse` 메서드 대체. **동시 요청 시 parse/format 오염 가능하던 스레드 버그 해소.**
- `DateUtils`: commons-lang3 DateUtils 상속 제거. 실사용 상속 메서드는 동등 시그니처 로컬 정의 — `isSameDay(Date,Date)` 는 Mapstruct 표현식(`ScheduleCalMapstruct`)에서 쓰므로 **unchecked 유지 필수**, `addDays(Date,int)` 동일 계약(null 시 NPE). `parseDateStrictly` 는 `DateParser` 에서 lang3 직접 호출. 미사용 공유 포맷터 `DF_DATE` 제거.
- 공개 시그니처 무변경 — 호출부 106파일 영향 없음.

### Phase 2 — 엔티티 전환 (진행 중, 전 필드 `LocalDateTime` 통일)

- 원칙: DTO 는 이미 String 날짜(API 계약 불변). `DateUtils` 의 Object-파라미터 API(asDate/asStr/asLocalDateTime)가 전환 브리지.

#### Phase 2-A — 감사 필드 축 (구현 완료, 로컬 빌드 게이트 대기)

- 베이스 3필드 전환: `BaseCrudEntity.deletedAt`·`BaseAuditRegEntity.createdAt`·`BaseAuditEntity.updatedAt` + 자체 선언分 `LogEntity.createdAt`·`LogUrlNmEntity.deletedAt`. `@Temporal` 제거 (Spring auditing/@UpdateTimestamp 는 LocalDateTime 네이티브 지원).
- Spec 8종: `Expression<LocalDateTime>` + `asLocalDateTime` 비교로 전환. stale-requeue 체인(embedding·entitycatalog repo→service→worker)·LogStats 조회 체인 전환.
- **임시 브리지 (2-C 에서 acntStus 전환 시 제거)**: `AuthInfoMapstruct` lastLoginAt/passwordChangedAt 표현식의 `DateUtils.asDate(createdAt)`, `UserService` 휴면 판정의 동일 브리지.
- 겸사 수정: `RelatedContentRepository.softDeleteAllByRef` 의 `SET deletedAt = 'Y'`(날짜 필드에 문자열 대입하던 기존 결함) → `CURRENT_TIMESTAMP`. `ReleaseHistoryDto.createdAt` 은 `@JsonFormat` 으로 직렬화 포맷 계약 유지.

#### Phase 2-B — journal·schedule 도메인 필드 (구현 완료, 로컬 빌드 게이트 대기)

- **DB date 컬럼 필드는 `LocalDate` 채택** (전부 LocalDateTime 통일 합의를 수정 — `journal_day.journal_date`·`week_start_date`·`journal_entry_embedding.journal_date` 가 date 컬럼으로 확인됨): JournalDay(Smp) journalDate/weekStartDt, EmbeddingEntity.journalDate.
- datetime 컬럼은 `LocalDateTime`: ScheduleEntity.bgnDt/endDt, EmbeddingEntity.embeddedAt, SyncJob started/finished/heartbeatAt, EntityJob.processedAt.
- toEntity mapstruct 는 asDate → asLocalDate/asLocalDateTime, 조회 API(JsonFormat: EmbeddingStatsDto·SyncJobStatusDto) 직렬화 포맷 계약 유지. `isSameDay(LocalDateTime, LocalDateTime)` unchecked 오버로드 추가 (Mapstruct allDay 표현식용).
- DTO·SearchParam 의 날짜는 String 그대로 — 화면·API 계약 무변경.

#### Phase 2-C — 잔여 도메인 필드 (구현 완료, 로컬 빌드 게이트 대기)

- auth 축: UserStateEntity 7필드·AuthInfo 4필드 → LocalDateTime. 토큰 만료(plusSeconds)·잠금(plusMinutes)·실패 윈도(Duration.between)·비번 만료(plusDays)·리셋 토큰 창(plusMinutes) 로직을 java.time 연산으로 재작성 (의미 동일). **2-A 임시 브리지(AuthInfoMapstruct·UserService asDate) 제거 완료.**
- 나머지: signup approved/rejectedAt·emplym retireDt·pwHistory changedAt·chat lastMessageAt·viewer lastVisitedAt·release started/deployedAt(@JsonFormat)·Managt/History embeds·popup 2필드 → LocalDateTime. **date 컬럼**인 emplym.ecnyDt·profile.brthdy → LocalDate.
- 2-B 전환 필드에 남아 있던 `@Temporal` 잔여 6건 제거 (java.time 필드에 스펙 위반). **엔티티 `java.util.Date` 필드 0건 달성, `@Temporal` 전면 소멸.**
- attachable "새 글" 판정(managtDt 7일)·비번 만료 판정 등은 isAfter/isBefore 로 전환, mapstruct toEntity 는 asLocalDate(Time) 로 정렬.

### Phase 3 — 유틸 축소 (1차 완료: dead API 제거)

- 호출처 0 인 dead API 제거: DateUtils 19종(월 인덱스·어제/내일·연/분 가산·getDateDiff·요일 한글·@Deprecated 위임·isDateStr 등, 420→300줄) + DateParser.sDateParseStr 오버로드 2종 + DayOfWeek.asKorean. 잔여 참조 0건 검증.
- 유지 계약: 문자열↔날짜 변환(asDate/asStr/asLocalDate(Time)) + 레거시 호환 연산. **신규 코드는 java.time 직접 사용** (클래스 Javadoc 에 명시).
- 후속(비긴급): 호출부 java.time 점진 전환 → Object-파라미터 API 축소. lenient 파싱 의존처를 정리한 뒤에만 `DateTimeFormatter` 로 전환한다.

---

## 저널 도메인 — 데이터·프론트 수렴

마이그레이션 철학·폴백 금지·UI 동결 등은 `CLAUDE.md` 를 본다. 여기서는 **구조 결정·경계**만 요약한다.

### 공통 원칙

- 목표는 **convergence**. 공통 축(부트스트랩·브리지·상태·렌더)을 세운 뒤 레거시 이중 경로를 제거하고 **단일 진입**만 남긴다. 브리지 실패 시 조용한 HBS 폴백으로 이어가지 않고 **로그 후 중단**이 기본이다.
- 데이터와 코드가 어긋나면 클라이언트 **땜빵** 없이 DB·시드·서버 단일 진실 원천을 맞춘다.

### 레거시 복원 모드 — UI 동일성 SSOT

- 저널 Vue 마이그레이션에서 화면 UI의 SSOT는 legacy templates/static의 partial, FTL, CSS, 실제 DOM이다. `app/frontend-vue` 구현은 이를 재해석하지 않고 먼저 동일하게 복원한다.
- **legacy 원본 참조 경로 (2026-07-02 `legacy/` 폴더 삭제 이후)**: ① 템플릿·정적 소스 원본은 git 이력 — 각 모듈의 Vue 전환 커밋 직전 트리(예: 일정은 `4932678fd^`)에서 원래 경로(`app/backend/src/main/resources/templates/…`, `static/…`)로 조회. ② 삭제 직전 `legacy/` 폴더 스냅샷 전체(2,560개 파일, ftlh/hbs + static 이미지·GeoLite2 mmdb 포함)는 리포 밖 `../legacy_backup_20260702.zip` 아카이브. (`legacy/`는 gitignore 대상이었으므로 폴더 자체는 git 이력에 없다.)
- **FreeMarker MVC 렌더 경로 제거 (2026-07-02)**: 화면 뷰 전면 Vue 이관 완료에 따라 `FreemarkerInterceptor`·`FreemarkerModelContributor`(port/adapter)·MVC `FreeMarkerConfigurer` 커스터마이즈·`spring.freemarker` 설정·`spring-boot-starter-freemarker` 를 제거. FreeMarker 는 **이메일 템플릿 렌더**(`freemarkerEmailConfig` + `templates/email/*.ftlh`, `spring-context-support`)로만 잔존한다.
- 작업 순서: ① legacy partial/FTL 확인 ② legacy CSS 확인 ③ legacy 렌더 DOM·클래스 확인 ④ 현재 Vue 비교 ⑤ 차이 목록 작성 ⑥ 차이 전부 수정 ⑦ 타입체크와 필요한 spec 갱신.
- 사용자가 짚은 한 픽셀·문구·간격은 국소 요청이 아니라 해당 컴포넌트의 legacy 동등성 검수 신호로 취급한다. 그 지점만 고치고 끝내지 않는다.
- 의미를 이해하지 못한 UI는 추정·개선하지 않는다. 우선 legacy와 동일하게 옮긴 뒤, 개선은 별도 명시 요청이 있을 때만 진행한다.
- "현재 화면을 보고 적당히 비슷하게 맞춘 뒤 사용자가 발견한 차이만 수정"하는 방식은 실패다. 완료 보고 전 관련 범위의 legacy ↔ Vue DOM·클래스·스타일·동작 차이를 선제적으로 확인한다.
- 프레임워크 관용구로 마크업을 정리하지 않는다. legacy가 라벨과 코드를 별도 span/class/style로 나눴다면 Vue도 같은 DOM 경계와 class/style 경계를 유지한다. 클래스 통합, `fs-*` 통합, wrapper 합치기, gap 유틸 대체는 시각 차이를 만드는 재설계로 본다.
- 자동 비주얼 diff가 없는 범위에서는 완료 보고에 최소 검증 근거를 포함한다: 비교한 legacy 파일, 비교한 Vue 파일, 보존한 DOM/class/style 차이, 남은 미검증 범위. Playwright/스크린샷 diff 도입 전까지 이 항목은 수동 게이트다.

### 저장소: 저널 엔트리 하드컷

- Primary 영속화는 **`journal_entry`**. 다형은 `content_type`(예: `JOURNAL_DIARY`/`JOURNAL_DREAM`)으로 구분. NOTE도 동일 테이블에 두며 영속 `content_type`은 `JOURNAL_DIARY` 계약을 유지한다.
- Reflection(Commentary) 영속화는 **`journal_reflection`**(About-A `ref_id`/`ref_content_type` 필수).
- 선언 스키마 SSOT는 `app/backend/src/main/resources/schema/full/mariadb/schema-*.sql`이다(1.0 전 Flyway 증분 없음). `schema-journal-mariadb.sql`은 `journal_entry`·`journal_reflection` CREATE로 entity와 맞춘다.

### 결산(Annual) — Vue·ESM (A-6 정리)

- **`dF.JournalAnnual` 표면**: `static/vue/feature/journal/annual/services/journalAnnualService.ts`(ES module) 단일. CRUD/Ajax 는 `journalAnnualCrudService`, 상태·렌더는 `journalAnnualStateService`(태그 헤더 3행·엔트리 리스트는 모두 Vue 브리지로 수렴 — Handlebars 직호출 0건).
- **목록 (`journal_annual_list`)**: `JournalAnnualListApp` 가 `#journal_annual_list_div` 소유. `journalAnnualCrudService.listAjax` → `window.JournalAnnualListVueApp.setList` — Handlebars 목록 카드 경로 제거.
- **Aside (`_journal_annual_aside_base`)**: `journalAnnualAsideService` + `JournalAnnualAsidePanelApp`; 년/월 변경은 `dF.JournalAnnualAside.yyMnth` 단일. SSR 부트스트랩은 `window.__journalAnnualAsideBootstrap`.
- **등록/리뷰 모달**: `JournalAnnualRegModalApp` / `JournalAnnualReviewRegModalApp`; FTL 가드 안에서 서비스 번들 순서 고정.
- **상세 (`journal_annual_dtl`)**: 페이지 부트는 `JournalAnnualDtlPageBoot` ES module 단일 수렴(A-7-α). 상단 카드·리뷰 목록은 `JournalAnnualDtlCardApp` Vue(A-7-β, `journalAnnualCrudService.dtlAjax` → `JournalAnnualDtlVueApp.setModel`). 태그 헤더(DAY/DIARY/DREAM 3행)는 `JournalAnnualEntryTagListApp` Vue(A-7-δ, `journalAnnualStateService.renderTagList` → `JournalAnnualEntryTagListVueApp.applyTagRow`). 엔트리 리스트(DIARY/DREAM)는 `JournalAnnualEntryListApp` + `JournalAnnualEntryItem` Vue(A-7-γ, `journalAnnualStateService.renderEntryList` → `JournalAnnualEntryListVueApp.setList`) — 행 컴포넌트는 `JournalEntryContent` / `JournalEntryContextMenu` / `JournalDayContextMenu` Vue 컴포넌트를 1:1 재사용하고, 좌열 일자 셀(stdrdDt + holyday/weather 표시)과 우열 댓글 등록·복사 버튼은 행 내부 인라인이다.
- **가시 dead**: 목록 헤더 검색이 호출하는 `dF.JournalAnnual.search()` 미정의 — 폴백 별칭 없이 유지(룰).
- **dead partial 정리(A-8)**: A-7 시리즈 직후, 호출 그래프상 사용처 0건으로 확정된 7개 HBS partial 을 일괄 제거했다. 대상: `_journal_day_stdrd_dt_partial`, `_journal_day_context_btn_partial`, `_journal_day_meta_btn_partial`, `_journal_entry_content_partial`(자기 자신 `_by_type` 래퍼 포함), `_journal_entry_context_btn_partial`(`_by_type` 래퍼 포함), `_journal_entry_copy_btn_partial`(`_by_type` 래퍼 포함), `_comment_reg_btn_partial`. FTL include 제거 페이지: `journal_annual_dtl`, `journal_day_cal`, `journal_day_meta`, `journal_day_weekly`, `journal_day_daily`, `journal_day_monthly`, `journal_entry_search`. 검증 기준은 ① Handlebars `{{> ... }}` 호출 0건(자체 `_by_type` 래퍼도 호출자 0건), ② JS/TS 문자열 참조 0건. 살아있는 인접 partial(`tag_list_partial`, `tag_list_sized_partial`, `comment_list_partial`)은 본 정리 범위 밖(A-9 에서 별도 처리).
- **일기/꿈 엔트리 태그 헤더 Vue 흡수(A-9)**: 일자(monthly/weekly/daily/cal/meta) + 엔트리 검색 페이지의 `#journal_diary_tag_list_div` / `#journal_dream_tag_list_div` 컨테이너가 placeholder 안내 박스(하드컷)로 비워져 있던 것을 Vue 로 수렴해 마무리했다. 신규 ESM `static/vue/feature/journal/day/JournalDayEntryTagListApp.ts` 가 두 컨테이너 위에 Vue 앱을 마운트하고 `window.JournalDayEntryTagListVueApp.setList(kind, list, { module })` 브리지를 노출(적재 경합은 `pendingByType` 큐잉으로 흡수). `journalEntryTagService.renderList` 는 `cF.handlebars.compile(..., "journal_entry_tag_list")` 컴파일 코드를 제거하고 본 브리지를 호출 — 서비스의 마지막 Handlebars 직호출이 사라졌다. `journalDayUiBridgeService.syncTagCloud` 는 `paintJournalEntryTagCloudHardCutNotice` 안내 박스 코드를 제거하고 `dF.JournalEntryTag.get(ct).listAjax()` 진입(=service.renderList → Vue 브리지 단일 경로). FTL 6개(`journal_day_monthly/weekly/daily/cal/meta`, `journal_entry_search`)에 본 ESM `<script type="module">` 적재를 추가하고 `_journal_entry_tag_list_template.hbs` / `_tag_list_partial.hbs` / `_tag_list_sized_partial.hbs` include 를 제거. 호출 그래프 검증(`{{>}}` 호출 0건, JS/TS 문자열 참조 0건) 후 3개 partial 파일을 삭제했다(결산 `journal_annual_dtl.ftlh` 의 `_tag_list_partial.hbs` include 도 동시 정리 — A-5-α 이후 이미 dead). 마크업/onclick 시그니처는 `_tag_list_sized_partial.hbs`(`<span class="py-2 me-3 cursor-pointer opacity-hover" onclick="{module}.select({id},'{tagNm}','{ctgr}')">…`) 와 1:1 동등.
- **dead partial 추가 정리(A-10)**: A-9 직후, "저널 Vue 통합 평가" 에서 식별된 6개 dead 의심 hbs partial 을 호출 그래프 1:1 검증 후 삭제. 대상: `_journal_entry_reg_btn_partial`(자기 자신 `_by_type` 래퍼 포함), `_journal_entry_toggle_btn_partial`, `_journal_entry_states_partial`, `_journal_tag_group_list_template`, `_comment_list_partial`, `_meta_list_partial`. 검증 기준은 A-8 과 동일 — ① Handlebars `{{> ... }}` 호출 0건, ② JS/TS 문자열로 id 참조 0건. 흡수처(이미 Vue 가 동일 마크업/onclick 동작을 보유): 등록 버튼/토글 버튼/상태 뱃지는 `JournalDayCard` / `JournalEntryItem` / `JournalChapterItem` / `JournalEntrySearchItem` / `JournalInterpretationItem`, 메타 sized 행은 `JournalDayMetaHeaderList`, `_comment_list_partial` 은 id `comment_list_partial` 자체 호출자 0건(별개의 id `comment_list` 모달은 `_comment_list_modal.ftlh` 가 보유, 본 정리 범위 밖). `_journal_tag_group_list_template` 은 `<#include>` / `{{>}}` / JS 참조 모두 0건이라 기존 시점부터 단순 dead. FTL include 제거 페이지: `journal_annual_dtl`, `journal_day_monthly/weekly/daily/cal/meta`, `journal_entry_search`, `journal_sbjct_reg_form`, 그리고 attachable 공용 `_comment_list_partial` 의 dead 정리 일관성을 위해 `board/notice/notice_regist_form` 의 dead include 도 동시 제거(저널 phase 범위 밖이지만 partial 삭제로 컴파일 깨짐 방지).
- **Vue 글로벌 `Message` 결의 통일(D)**: A-9 hotfix 가 드러낸 ESM 스코프 식별자 결의 race(`Cannot read properties of undefined (reading 'get')` first-render 시 가능)를 사전 차단하기 위해 공용 헬퍼 `static/vue/common/messageHelper.ts` 의 `resolveMessage(key, fallback?)` 를 도입했다. 결의 규칙: `window.Message` → `globalThis.Message` 우선 결의, `Message.get` 이 함수가 아니거나 throw 하면 `fallback ?? key` 반환(렌더 안전성 우선, 절대 throw 안 함). Vue 측 `Message.get(...)` 직호출 ~52건을 본 헬퍼 호출로 일괄 치환 — (1) template/computed 매 렌더 평가 4건(`JournalDayTagPanelApp` — `data()` 마운트 시점 1회 결의 후 캐시), (2) module top-level eval 7건(`journalEntryService` 의 `configs.contentLabel/emptyLabel` 4건 + `journalAnnualStateService` 의 `tagListConfigsCache` 3건), (3) `data()` 내 `t()` 헬퍼 9 + ModalBody computed 8건(annual 컴포넌트 일대), (4) `typeof Message !== "undefined"` 가드 6건(Aside 4 + `JournalDayList` + `JournalDayEntryTagListApp` 의 A-9 인라인 결의 함수도 헬퍼 위임으로 통일), (5) 이벤트 핸들러 안 `Swal.fire` 류 28+건(services 일대). API 사용은 두 가지: 단발 호출 `resolveMessage("...")` 또는 `data() { return { ... fooLabel: resolveMessage("...") } }` (마운트 시점 1회 결의 후 캐시). 호출 시그니처 보존 — 결의 결과는 변경 전 동작과 동일하며, 미정의 환경에서만 폴백이 다를 수 있다(이전: 미정의 시 ReferenceError 또는 `""`/`key`. 이후: 명시적 폴백 → `key` 반환이 기본).
- **Metronic Vue 데모 전환 — defer anchor**: 베이스 전환(`metronic_vue_v8.2.1_demo1/`) 은 잠재 종착지로 인지하되 즉시 진입은 보류한다(룰: convergence 우선, 동축 아닌 변경 묶음 금지, dual-path/coexistence 금지). 데모 스택은 Vue 3 + Vite + vue-router(`createWebHistory`, `base=/metronic8/vue/demo1/`) + Pinia + axios + vue-i18n + Element Plus, 단일 SPA `#app`, `assets/ts` 의 `MenuComponent.bootstrap()` 를 route 변경 시 `reinitializeComponents()` 로 재호출, JWT(`Authorization: Token ...`, `localStorage.id_token`) 가정. 현 시스템(Spring + FreeMarker per-page SSR + `_head.ftlh` 글로벌 적재 + 페이지별 `<script type="module" src="/vue/...">` ESM 진입 + 전역 `dF.*`/`cF.*` + 서버 messageMap 인라인 + tsc-only sass + form login + JWT filter + oauth2 + session IF_REQUIRED + remember-me) 과의 구조적 갭은 5개 축에서 동시 발생: 레이아웃(SPA 트리 vs FTL 셸), 라우팅(vue-router vs FTL/MVC), 인증(JWT/Token vs 세션+폼), i18n(vue-i18n 클라 카탈로그 vs 서버 인라인 messageMap), 빌드(Vite vs tsc-only). 부분 통합은 dual-path 가 강제되어 룰 위반. 진입 조건은 `시기` 가 아니라 `기능 상태` 4개(EC-1~EC-4) 가 동시 충족되는 시점으로 정의: EC-1 콘텐츠 영역 활성 `.hbs` partial 0(잔존은 호출 그래프상 명시 dead 만 — A-8/A-10 검증 기준: `{{>}}` 호출 0 + JS/TS 문자열 참조 0), EC-2 HBS/FTL 인라인 `onclick="dF.*"` / `onclick="cF.*"` 0건(`templates/` + `.hbs` grep 0), EC-3 페이지 = 단일 Vue root(현재처럼 한 페이지에 여러 ESM 진입이 흩어져 있지 않고 단일 셸 컴포넌트로 합쳐진 상태 — router-view 가 받을 prerequisite), EC-4 `dF.*` 가 Vue 서비스 단일 경로(`static/vue/feature/**/services/`) 로만 등록(잔존 `static/js/view/feature/**/*_module.js` 의 dF 부착 0). 부가 게이트(EC 와 별축): 그 SAVEPOINT 에서 `npm run build:ts` + `npm run check:encoding` 그린. EC 와 무관하게 **셸 phase 까지 살아있어도 무방한 자산** 3개(셸 phase 본 작업 범위): `cF.util/ajax/handlebars/format/validate` 공용 util(SPA 안에서도 노출 가능), `KTMenu.createInstances()` + Metronic v8.2.5 HTML 번들(`plugins.bundle.js` / `scripts.bundle.js`) (데모의 `MenuComponent` 모델로 일괄 이전이 셸 phase 본 작업), `_head.ftlh` 의 `window.Message` 서버 messageMap 인라인(셸 phase 안에서 i18n 재설계 결정). 진입 phase 진입 시 결정해야 할 sub-questions(키워드 anchor): ① 셸 흡수 범위(`layout_default` / `layout_with_aside` / `layout_without_sidebar` 모두 vs 일부), ② 라우팅(`createWebHistory` vs hash, Spring 정적 폴백 정책), ③ 인증(현 form+session 유지 vs JWT-localStorage 재설계 + CSRF 정책 — 현 `csrf().disable()` 정합 재검토), ④ i18n(서버 messageMap 인라인 유지 = JSON 카탈로그 export vs vue-i18n 단일 진실원천 이전), ⑤ 빌드(tsc-only 폐기 → Vite 도입; 산출물 위치·`/vue/...?releaseDate` 경로·`scripts/check_encoding.py` 룰 재정의 — SFC 안 한글 주석까지 검사 확장 필요), ⑥ 글로벌 `cF.*` / `dF.*` 처리(Pinia 흡수 vs 호환 shim 한시 — 룰의 dual-path 금지 와 충돌 안 하게 phase 안에서 종료 시점 명시), ⑦ Metronic KT* 처리(흩어진 `KTMenu.createInstances()` 호출을 데모 `MenuComponent` / `reinitializeComponents()` 모델로 일괄 이전). 시기 표현(예: "B/C phase 완료") 은 본문 anchor 에 직접 쓰지 않고 EC 충족이 보통 그 시점에 도달한다는 부가 사실로만 둔다. 향후 사용자가 "metronic vue 가자" 라고 명시하면 본 anchor 의 EC-1~EC-4 + sub-questions 7개를 곧장 plan 첫 단계로 꺼낸다.

### 저널 일자(journal_day) — 목록·부트스트랩

- monthly/weekly/daily 목록은 `journalDayListAppMount` 및 `JournalDay*ListApp` 진입으로 `#journal_day_list_div` 를 소유한다. **단일 부트 축**으로 수렴.
- `dF.JournalDay.refresh`: Vue 마운트된 monthly/weekly 에서는 `JournalDayVueApp.refresh()` 로 reload 단일화.

### 어사이드·주간·필터·검색 파라미터

- 검색 파라미터·필터 상태 **단일 원천**으로 수렴. DOM 정렬·HBS 주입 폴백은 제거 방향.

### 런타임·`dF.JournalDay`

- 레거시 상태 저장소 역할은 전용 모듈로 분리하고, 파사드 최소 API·`dF.JournalDay` 참조 축소를 지속한다.

### 인라인 `onclick`·레거시 호출

- 이벤트 브리지·데이터 속성으로 옮기고 구역별 직접 호출을 정리해 **단일 액션 진입**만 남긴다.

### 태그 UI·모달

- Vue 분할·HBS 폴백 제거 방향. **TEXT_CLASS_CD / EMOTION** 등은 DB 시드가 단일 진실(UI 임시 옵션 금지).

### Vue 컴포넌트 디렉터리

- `journal/day/components` 에 섞였던 entry/chapter/interpretation 조립 컴포넌트는 `feature/journal/entry|chapter|interpretation/components` 로 직접 이동했다. **UI/DOM/클래스 불변.**

### frontend SPA 패키지 구조 기준 (Vue·React)

- flat `src/views/`, `src/stores/`, `src/layouts/`, `src/router/` hybrid 는 제거됐다. import 축은 `@/app/`, `@/shared/`, `@/features/` 이다.
- `src/app/` — `router/`, `layouts/`, `pages/Error*.vue` 등 앱 shell.
- `src/shared/` — auth·config·theme·menu store, `ui/editor|tag`, 범용 `utils/`, `components/system/`.
- `src/features/{admin,journal,chat,board,calendar,user,attachable,auth}/` — 화면 + feature store(+ types) co-location. 백엔드 `feature/*` 축과 맞춘다.
- feature store는 Pinia 상태·API 조립까지만 담당한다. store가 Vue 컴포넌트나 DOM을 import하면 구조 위반이다.
- `src/styles/` 는 앱 전역 스타일 경계다.
- `src/platform/metronic/` 는 UI 플랫폼 킷(Metronic) 경계다. npm vendor·제품 도메인이 아닌 **UI platform 층**으로 분류한다. `frontend-vue`·`frontend-react` 공통 SSOT.
- `src` top-level 책임: `app/`(shell), `shared/`(횡단 플랫폼), `features/`(제품), `platform/`(UI 킷), `styles/`(앱 스타일).
- `metronic_vue_v8.2.1_demo1/` 은 Metronic 원본 참조·업그레이드 diff용 외부 경계(앱 import 축 아님).

### journal Vue 패키지 기준

- `features/journal/**` 는 Java `feature/journal/**` 패키지 경계를 따른다. `day`, `entry`, `chapter`, `interpretation`, `todo`, `annual`, `thread`, `shared` 를 기준으로 둔다.
- 횡단 store(`journal.ts`, `journalModal.ts`, `journalAside.ts` 등)는 `features/journal/stores/` 에 둔다.
- `daily`, `weekly`, `monthly`, `calendar`, `meta` 는 독립 feature가 아니라 `journal.day` 의 view mode/presentation 이다. 화면 파일은 `features/journal/day/` 아래에 둔다.
- `features/journal/day/components` 는 day aggregate 표시용 컴포넌트만 둔다. `JournalEntryItem`, `JournalChapterItem`, `JournalInterpretationItem` 처럼 다른 journal feature의 항목 컴포넌트는 각 feature 하위에 둔다.
- 여러 journal feature가 함께 쓰는 context menu, tag profile, comment/related modal 은 `features/journal/shared/**` 에 둔다.
- modal 위치도 대상 도메인을 따른다. 예: day 등록/상세/meta/tag 상세는 `day/modals`, entry 등록은 `entry/modals`, chapter 등록은 `chapter/modals`, todo 등록은 `todo/modals`.

### Metronic platform 경계 (UI 킷 층)

분류: Metronic은 **외부 npm vendor**도 **DreamDiary 제품 도메인**(`app`/`shared`/`features`)도 아니다. 앱 부트스트랩·전역 SCSS·layout 런타임에 깊게 붙은 **UI platform kit** 으로 본다.

| 층 | 경로 | 역할 |
|---|---|---|
| 제품 | `app/`, `shared/`, `features/` | DreamDiary 라우트·상태·도메인 UI |
| 통합 | `app/layouts/**` | Metronic DOM/class를 DreamDiary 방식으로 소유·감쌈 |
| UI 킷 | `platform/metronic/**` | Metronic 원본(runtime, assets, demo 잔재) |
| import 축 | `@metronic` alias | 물리 경로 `src/platform/metronic` — 앱 코드에 물리 경로 하드코딩 금지 |

`frontend-vue`·`frontend-react` 공통:
- **물리 경로 SSOT**: `src/platform/metronic/`
- **alias SSOT**: `@metronic` → 위 경로 (`vite.config`·`tsconfig` paths)
- **내부 subtree**: Vue(`core/` 등)와 React(`layout/`, `partials/` 등)는 킷·스타터가 달라 **1:1 동일할 필요 없음**. 맞출 것은 층·alias·편집 정책.

물리 경로 이전 현황:
- Vue: `src/vendor/metronic` → `src/platform/metronic` (✓ 완료)
- React: `src/_metronic` → `src/platform/metronic` (✓ 완료)

- **Metronic asset**: CSS/SCSS, 폰트, 이미지, 아이콘, 데모 미디어 등 정적 자산. 예: `src/platform/metronic/assets/**`, `public/media/**`.
- **Metronic runtime/core**: 앱이 직접 import하는 helper/plugin/service. 예: `@metronic/core/services/ApiService`, `@metronic/core/plugins/keenthemes`, `@metronic/core/helpers/assets`. public repo에서는 Metronic 원본 재배포 이슈로 git 미추적; **로컬 킷 복원 절차**로 채운다.
- **Metronic demo source**: 원본 스타터 샘플 화면·컴포넌트. 예: `platform/metronic/components/**`, `views/crafted/**`, `LayoutBuilder.vue`, 데모용 drawer/search/toolbar/modal. 제품에서 import하지 않으면 제거.
- **앱 소유 컴포넌트**: Metronic class/icon/asset을 쓰더라도 DreamDiary 라우트·레이아웃·기능에서 import하는 Vue/React 컴포넌트는 `src/app/**`, `src/shared/**`, `src/features/**` 소유.
- 예외 판단: `app/layouts/default/components/modals/Modals.vue` 처럼 Metronic demo modal을 조립하는 파일은 demo source. 제품에서 필요해지면 `shared` 또는 `app` 으로 승격 후 import 경로를 앱 소유 컴포넌트에 맞춘 뒤 커밋. 쓰지 않으면 제거.
- **편집 원칙**: `platform/metronic` 자체를 앱 루트로 만들지 않는다. 킷 내부 링크 일괄 수정은 피하고 `app/layouts` adapter에서 감쌈. 장기적으로 demo source(`components/**` 등)는 삭제 대상.
- **추적 원칙**: 추적되는 앱 코드가 import하는 파일은 ignored 로 두지 않는다. ignored 킷 파일을 import해야 하면 앱 소유 코드로 승격하거나 명시적 **킷 복원 절차**를 문서화한다.
- public repo 원칙: Metronic 원본 소스·SCSS·폰트·이미지·데모 미디어는 git에 올리지 않는다. 필요한 경우 라이선스를 보유한 개발자가 로컬 **킷 복원 절차**로 채운다.

### 검증

- `./gradlew buildFrontend` (또는 PATH `npm` 있을 때 `npm run build`). 브라우저 회귀: monthly/weekly·필터·주간 네비·태그·모달 등.

---

## 관리·공통 플랫폼 — Vue·URL·i18n

### UI 규약

- 화면 변경은 **명시 요청** 없으면 하지 않는다. 마이그레이션은 동작·마크업·클래스·플로우 보존.

### Vue 정적 리소스

- `static/vue/feature` 중심으로 경로 통일(예: admin/chat → `feature/admin`, `feature/chat`). 템플릿 스크립트 URL `/vue/feature/...` 정합.
- Vue 프로덕션 빌드의 `/vue-app/assets/**`는 내용 해시 파일명을 사용하며 `public, max-age=31536000, immutable` 브라우저 캐시를 적용한다. SPA 진입점과 fallback `index.html`은 이 장기 캐시 경계에 포함하지 않는다.
- 서버는 1KB 이상의 JS·CSS·JSON·HTML·XML·SVG·일반 텍스트 응답을 압축한다. 이미지·폰트처럼 자체 압축된 바이너리는 서버 압축 대상에 포함하지 않는다.

### i18n 카탈로그

- `GET /i18n/{locale}.json` 단일 축, 비인증 접근 등 프록시 규칙 정합.

### RESTful·URL

- 태그·사용자·게시판·권한 등 API 스타일은 모듈마다 동일 원칙(리소스 중심, `.do` 정리 등). 세부 문자열은 코드·컨트롤러를 본다.

### Ajax 오류 처리

- 공통 실패 핸들러에서 XHR 메시지 노출 방식 정리.

### 페이지네이션·관리 UX

- 어드민·코드 등 페이지 번호 파서·FTL 기본값 **선행 순위** 규칙 정합.

### 게시판·코드·메뉴·공지·권한·사용자·로그

- 보드/post Vue, 코드 관리, 메뉴 관리, 공지·권한·사용자, 운영 로그 등은 **저널과 동형**으로 브리지 축소·컴포넌트화·단일 경로 수렴을 적용했다. DB 시드 순서 이슈는 **DB가 단일 진실**.

### 예약(enum)·사이트

- 구조적 예약 키는 enum 등으로 명시. 메뉴 라벨·캐시 무효화 정책은 운영 기대에 맞게 정리.

### 릴리즈 이력(`release_info`)

- 배포 단위 변경을 구조적으로 남긴다. 예: `SERVER_START`(매 기동), `DEPLOY`(릴리즈 식별 키 변경 시만). 초기화기에서 기록 실패가 부팅 전체를 막지 않게 예외 처리. 조회 API 예: `GET /cmm/get-release-history.do?size=20`.

### 메뉴 컬럼 하드컷 런북

- `menu` 테이블 컬럼명 하드컷·코드그룹 제거 등 **운영 DB 절차**는 별도 긴 런북이었으나, 본 저장소에서는 **실제 MariaDB 마이그레이션 스크립트·커밋 메시지**를 기준으로 검증한다(경로 고정 파일 없음).

### 검증

- `npm run build:ts` 및 화면별 회귀.

---

## 저널 일자(journal_day) 마이그레이션 — 문서 인덱스

- **도메인 의도**: `DESIGN_NOTES.md`
- **철학·규약**: `CLAUDE.md`
- **기술 롤업**: 아래 절.

---

## 저널 일자(journal_day) 마이그레이션 — 기술 변경 롤업

**규약·철학**은 `CLAUDE.md` 를 본다(중복 서술 생략).

### 1. 2026-05-07 — 모달·메타 페이지 Vue 이전

| 기록 | 한 줄 요약 |
|------|------------|
| 상세 모달 | HBS 제거 → `JournalDayDetailModalApp`, teleport, `JournalDayDetailVueApp` 큐 패턴 |
| 등록/수정 모달 | HBS 제거 → `JournalDayRegModalApp`, 플러그인·폼 서비스 브리지 |
| 메타 조회 모달 | HBS 제거 → `JournalDayMetaModalApp` / `JournalDayMetaVueApp` |
| 메타 페이지 설정 스트립 | `JournalDayMetaPageApp`, FTL/HBS 정리 |
| 검색 파라미터 SSOT | `getSharedSearchParams()` 등 접근 경로 단일화 |

### 2. 2026-05-08 — 검색·필터·폼·CRUD (Phase 1~6)

| Phase | 한 줄 요약 |
|-------|------------|
| 1 | `JournalDayListApp` 검색 파라미터 Vue reactive SSOT |
| 2 | CRUD·컨텍스트 메뉴 → Vue 소유, 레거시 `CrudService` 직접 호출 제거 |
| 3 | 등록 폼 TinyMCE/Tagify/datepicker·`regAjax` Vue 소유 |
| 4 | 이중 쓰기 제거, 레거시 get/patch → Vue SSOT 프록시 |
| 5 | 필터 핸들러 5종 Vue·`JournalDayVueApp` 노출 |
| 6 | 외부 호출자 `JournalDayVueApp` 직접 호출, 레거시 SearchStateService 축소 |

### 3. 2026-05-09 — Phase 9 ~ 17

| 구간 | 한 줄 요약 |
|------|------------|
| 9 | `journalDayUiBridgeService` 에서 `dF` 제거, URL·검색 SSOT 직결 |
| 10 | Vue에서 `JournalDayPageStateService` / `ViewService` 제거 |
| 11 | 레거시 데드코드·`runtime_service.refresh` 단순화 |
| 12 | aside/tag `getViewType()` 제거 → Vue SSOT |
| 13 | `JournalDayPageStateService` 완전 제거 |
| 14 | `JournalDayViewService` 일부 삭제·`.js` 동기화 |
| 15 | `JournalDayViewService` 삭제, `navigateToWeekDay` 브리지 |
| 16 | `refresh()` 외부 호출 제거·런타임 정리 |
| 17 | `JournalDayBootstrapService` 삭제 → `bootstrapDfJournalDayShell` |

### 4. 2026-05-09 — 소규모 슬라이스

- 태그 Vue 브리지 실패 시 침묵 제거 → DOM/`Swal`/`console`.
- 태그/메타 URL `yy` 폴백 제거: Vue SSOT만.
- 주간 네비 DOM 폴백 제거: 브리지 실패 시 경고·`console.error`.
- ListApp ↔ Aside 주간 네비: `syncAsideWeekNavigator` 직접.
- Aside `init` 순서: `initJournalDayAsideShell` 후 `JournalDayAside.init`.
- 엔트리 태그: `listEntryTagAjax` 제거 등 하드컷.
- `journalDayTagService` 브리지: `dF.JournalDayTag` 단일 위임.
- 목록 뷰 bootstrap: `journalDayListAppMount` 동기 구간 단일 호출.
- 룰: 비정상 감추 금지·메인 중간 깨짐 허용·새 폴백 금지 → `CLAUDE.md` 반영.

### 5. 목록 진입 3분할 + `journalDayListAppMount`

| 진입 모듈 | 역할 |
|-----------|------|
| `JournalDayMonthlyListApp.ts` | `mountJournalDayListApp("MONTHLY")` 만 호출 |
| `JournalDayWeeklyListApp.ts` | `await mountJournalDayListApp("WEEKLY")` 후 주간 전용 셸 |
| `JournalDayDailyListApp.ts` | `await mountJournalDayListApp("DAILY")` 후 일간 전용 셸 |
| `journalDayListAppMount.ts` | Vue·브리지·데이터 로드 SSOT(단일 구현) |

레거시 `journal_day_weekly.ts` / `journal_day_daily.ts` 의 `Page` 및 동명 `.js` 는 제거했다.

### 6. 일간 리소스 이름 (`journal_day_daily`)

| 항목 | 변경 |
|------|------|
| FTL | `journal_day_view.ftlh` → `journal_day_daily.ftlh` |
| 일간 진입 | `JournalDayDailyListApp.ts` — 레거시 `journal_day_daily.ts` / `Page` 제거 후 통합 |
| Spring 뷰 | `JournalDayPageController` → `…/journal_day_daily` |
| P3~P4 | 탭 `changeView` → `registerJournalDayViewService.js` + `journalDayUiBridgeService`. 런타임은 `journal_day_runtime_service.js` side-effect import. FTL에서 classic 서비스 스크립트 줄 제거. |

### 7. 운영 런북(메뉴 하드컷)

- 메뉴 DB 하드컷 런북은 **저널 일자 마이그레이션과 별개** 운영 절차로, 본 롤업에 포함하지 않음. 스크립트는 저장소 `schema`/마이그레이션을 본다.

---

## 2026-05-17 frontend-vue 마이그레이션 지시 누락 정리

이번 대화에서 나온 요구는 "화면을 Vue 파일로 만든다"가 아니라 **legacy templates/static 동작을 `app/frontend-vue`의 단일 앱 경계로 흡수한다**는 의미다. 중간 단계가 깨지는 것은 허용하지만, 완료라고 말하려면 아래 항목의 legacy 동등성을 검수해야 한다.

| 범위 | 결정/기록 |
|------|-----------|
| Metronic platform 경계 | `platform/metronic` 을 UI platform kit 층으로 둔다(제품 도메인·npm vendor 아님). 킷 자체를 앱 루트로 만들지 않고 `app/layouts` adapter에서 감싼다. import는 `@metronic` alias. 장기적으로 demo source(`platform/metronic/components/**` 등)는 삭제 대상. 물리 이동: Vue `vendor/metronic` → `platform/metronic` (✓). React `_metronic` → `platform/metronic` (✓). |
| 기본 레이아웃명 | `layout/default-layout`은 의미 중복이므로 `layouts/default`로 둔다. |
| 첫 화면 메뉴 | 대시보드가 placeholder여도 기본 메뉴/사이드바가 있어야 한다. 이동 수단 없는 빈 화면은 마이그레이션 완료 상태가 아니다. |
| 사용자/관리자 메뉴 | 메뉴는 1차원 하드코딩이 아니라 `GET /api/menus?mode=USER|MNGR` + `subMenuList` depth 기반이어야 한다. fallback 메뉴는 서버 실패 시 보조 수단으로만 둔다. |
| 관리자 화면 | boardGroup, code, menu, users, logs, stats_user, auth-policy는 `/admin/**` Vue route로 들어온다. 각 화면은 legacy 기능 단위로 재검수한다. |
| 인증 결과 | `verify_success.ftlh`, `verify_failure.ftlh`는 `/auth/verify-result` 단일 Vue 화면으로 흡수한다. |
| attachable | 댓글/이력/관련글/태그/파일 등은 하나씩 Vue modal/store로 흡수하고 FTLH owner를 제거한다. |
| `static/vue/global` | 공통 전역은 `src/stores`, `src/utils`, `src/services`, `src/styles`로 재배치한다. 임시 참조가 남으면 migration spec에 제거 기준을 남긴다. |
| 폰트 | legacy `font.css`를 `/css/font.css`로 로드하고 `/font/**` 파일은 Spring Boot static 경로를 사용한다. Vite dev server는 `/css`, `/font` proxy를 둔다. |
| 저널 태그클라우드 | day/diary/dream tag row를 Vue에서 표시한다. "1개짜리 태그 숨김"과 "해당년도 태그 다 보기" 기능은 제거 요구에 따라 복원하지 않는다. |
| 태그 클릭 | 태그 클릭은 즉시 검색이 아니다. `JournalTagContextMenu`를 열고, 검색 액션에서 일자 태그는 상세 모달, 일기/꿈 태그는 새 창 검색으로 간다. |
| 엔트리 검색 새 창 | `/vue-app/journal/entry/search`는 legacy `journal_entry_search.ftlh`의 replacement다. 새 창이므로 메뉴/aside 없는 `SystemLayout` auth route로 둔다. 현재 구현은 목록/태그/키워드 조회와 결과 전체/개별 복사를 포함하며, export/sort/고급 다중 입력은 남은 검수 대상이다. |

문서 위치:
- 공통 통합 기준: `docs/migration/common/component-spec.md`
- 저널 태그/검색 흐름: `docs/migration/journal/component-spec.md`, `interaction-spec.md`, `screen-spec.md`
- 제품 동작 기준: `docs/JOURNAL_SCREEN_BEHAVIOR_SPEC.md`
