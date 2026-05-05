# DEV NOTES

## 공통 인코딩 게이트

- `npm run check:encoding` → `scripts/check_encoding.py`. 실패 시 **해당 변경 묶음 전체 폐기·되돌림**(부분 통과 없음). `scripts/`에는 검증 외 자동 수정 도구를 두지 않는다(`scripts/README.md`).

---

## 문서 기록 원칙

- **남길 것**: 아키텍처·SSOT·마이그레이션 방향, 제거·합의된 단일 경로, 되돌리기 어려운 계약, 운영·보안상 필수 결정.
- **남기지 말 것**: 자잘한 리네임·한 줄 수정·매 커밋 단위 “변경 로그”용 `.md` 양산.
- **같은 주제·같은 마이그레이션**은 이 파일(또는 `DESIGN_NOTES.md`) 안에서 정리한다. Phase별로 파일을 새로 만들지 말고, 필요하면 **한 문서에 `### Phase N` 섹션**만 둔다.
- UTF-8 깨짐이 보이면 **일괄 스크립트·전역 치환으로 복구하지 말고** 해당 범위에서 작업을 중단한다. (연쇄 손상 방지.)

---

## 패키지 구조

- "패키지는 파일 묶음이 아니라 책임 경계다."
- `feature`는 사용자 시나리오와 비즈니스 규칙에만 집중한다.
- "`global` 상수는 `auth`/`feature`/`infrastructure`를 import하면 안 된다."
- "`feature`-`infrastructure` 계층간 협력은 구현체가 아니라 명시적 계약(포트/서비스)으로 연결한다."

---

## 저널 도메인 — 데이터·프론트 수렴

마이그레이션 철학·폴백 금지·UI 동결 등은 `CLAUDE.md` 를 본다. 여기서는 **구조 결정·경계**만 요약한다.

### 공통 원칙

- 목표는 **convergence**. 공통 축(부트스트랩·브리지·상태·렌더)을 세운 뒤 레거시 이중 경로를 제거하고 **단일 진입**만 남긴다. 브리지 실패 시 조용한 HBS 폴백으로 이어가지 않고 **로그 후 중단**이 기본이다.
- 데이터와 코드가 어긋나면 클라이언트 **땜빵** 없이 DB·시드·서버 단일 진실 원천을 맞춘다.

### 저장소: 저널 엔트리 하드컷

- 영속화는 **`journal_entry` 단일 테이블**로 수렴. 다형은 `content_type`(예: `JOURNAL_DIARY` 등)으로 구분.
- 스키마·수리 스크립트는 `src/main/resources/schema/` 등 저장소 내 마이그레이션 SQL 을 본다.

### 결산(Annual) — Vue·ESM

- 결산 상세는 저널 entry 공통 줄을 Vue로 당기는 시험 축으로 썼다.
- 페이지 엔트리에서 **ESM `import()`** 로 레거시 로드, **`import.meta.url`** 쿼리로 캐시버스트. FTL은 Vue 부트스트랩 번들 **필수** 포함.
- 상세·필터·섹션 탭·entry/tag 갱신과 `dF.JournalAnnual` Ajax 는 브리지로 잇는다. **DIARY** 섹션은 Vue 마운트·탭 전환 시 `unmount` 로 잔류 방지.

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

### 검증

- `npm run build:ts` 등. 브라우저 회귀: monthly/weekly·필터·주간 네비·태그·모달 등.

---

## 관리·공통 플랫폼 — Vue·URL·i18n

### UI 규약

- 화면 변경은 **명시 요청** 없으면 하지 않는다. 마이그레이션은 동작·마크업·클래스·플로우 보존.

### Vue 정적 리소스

- `static/vue/feature` 중심으로 경로 통일(예: admin/chat → `feature/admin`, `feature/chat`). 템플릿 스크립트 URL `/vue/feature/...` 정합.

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
