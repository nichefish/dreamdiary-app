# Reflection 흡수 (journal_interpretation → journal_entry)

**상태: 설계 확정 · 미구현.**

이 문서는 "저널 해석(interpretation)"을 독립 Entry 종류인 **Reflection**으로 승격시키는 마이그레이션의 target 설계와 계약이다. 구현은 이 문서 확정 이후 별도 SAVEPOINT 로 진행한다. as-built 스펙(`screen-spec.md` / `interaction-spec.md` / `component-spec.md`)은 현재 구현된 현실을 기술하므로 **이 문서로 인해 선반영하지 않는다**. 각 phase 가 실제로 착지할 때 해당 스펙을 MISSING→✓ 로 갱신한다.

설계 SSOT 포인터: `docs/spec/DESIGN_NOTES.md` 의 journal-interpretation / journal-note 절.

---

## 0. 배경 — 왜 흡수인가

현재 코드는 이미 이 방향으로 60~70% 수렴해 있다. 이 마이그레이션은 새 추상을 세우는 게 아니라 **이미 절반 지어진 구조를 수렴 완료**시키는 작업이다(convergence, coexistence 아님).

- Diary/Dream 은 이미 한 엔티티 `JournalEntry` 이며 `contentType`(`JOURNAL_DIARY`/`JOURNAL_DREAM`)으로만 갈린다.
- `JournalInterpretation` 은 이미 별도 엔티티/테이블(`journal_interpretation`)이며, `refId`+`refContentType` 라는 **다형 참조**를 갖는다 = target 개념의 원형.
- 연관글(`relatedContentList`, `attachable.related`)·스레드(`threadList`)는 이미 Entry 에 붙어 있다.
- `ContentType`·`ChapterType` 둘 다 `NOTE` 를 예약해 두었다.

### 핵심 통찰: 위치 타입 vs 본질 타입

현재 코드는 **chapter 타입이 entry 타입을 지시(dictate)한다**. `JournalEntryTypeResolver.resolveByChapterId` 는 entry 타입을 chapter 에서 역산하고, `assertChapterForEntry`(`JournalEntryService`)는 `chapter.chapterType != policy.expectedChapterType` 를 예외로 막는다(단, NOTE chapter 는 DIARY entry 를 예외적으로 받아준다).

즉 **일기·꿈은 "자기가 앉은 chapter 가 지정하는 타입"(positionally typed)** 이다. 일기가 일기인 건 일기 chapter 에 있어서다.

반면 **Reflection 은 이 시스템 최초의 "본질로 타입이 정해지는(intrinsically typed)" Entry** 다. target 을 물든 안 물든, "사유"라는 성질 자체로 Reflection 이다. 그래서 그 chapter 는 타입의 출처가 아니라 순수한 **서사적 배치(narrative placement)** 일 뿐이며, 어느 축의 chapter 에든 놓일 수 있다.

---

## 1. 도메인 규칙 (정본)

### 모델

1. Reflection = Entry 로 흡수. `ContentType.JOURNAL_REFLECTION` 신설 + `JournalEntryTypePolicy.REFLECTION`.
2. `journal_interpretation` 테이블·모듈 → `journal_entry` 로 수렴(흡수 후 제거). `ContentType.JOURNAL_INTERPRETATION` 은 Phase 4 에서 **장기 alias 없이 제거**한다.

### Chapter 축

3. Reflection 은 어느 타입 chapter(DIARY/DREAM/NOTE)든 저자가 **자유 배치**한다. 타입을 chapter 가 지시하지 않는다(본질로 결정).
4. `journalChapterId` 는 **NOT NULL·hard-owned**(chapter→entry 의 cascade/orphanRemoval 유지). 기본값 = 작성 시점 chapter, 무소속 사유 = **orphan-NOTE 버킷**(§4.3).
5. `assertChapterForEntry` 에 REFLECTION-universal 예외 추가. Reflection 생성은 `resolveByRawType`(명시 타입) 경로로 chapter→타입 역산을 우회한다. `supportsChapterChange() = true`.

### 관계

6. **target(interprets, 0..1)** = 기존 `refId`+`refContentType` 재사용, **nullable**. 대상은 Entry 타입 {DIARY, DREAM, REFLECTION}으로 제약한다. → `Reflection ──extends──▶ Reflection`(사유가 사유를 잇는 것)이 자동 성립. target 은 **표시위치만** 결정하며 belongsTo(소속)가 아니다.
7. **target 삭제 시** → target 만 null 처리하여 자동 독립화(Chapter 직속으로 강등). **절대 cascade 삭제하지 않는다.** (Reflection 은 자기 chapter 에만 hard-owned 이고 target 에는 묶이지 않는다. 2009 년 원문을 지웠다고 2026 년 사유를 죽이지 않는다.) 구현 훅: `JournalEntryService.postDelete`(및 soft-delete 경로)에서 `refId = 삭제된 entry ∧ contentType = REFLECTION` 행의 target 을 nullify.
8. **연관글(related, 0..N)** = 기존 `relatedContentList` 재사용. 별도 "reference" 개념을 신설하지 않는다(과도한 세분화 금지). 관계는 딱 둘: target, related.
9. **스레드(thread)** = 기존 `threadList` 재사용.
10. Phase 3 에서 `JournalEntryDto.journalInterpretationList` 를 **`reflectionList` 로 rename** 하고, 의미를 자식 해석 **합성(composition)** → target 역참조 **연관(association)** 으로 전환한다. enricher 를 "`refId = this ∧ contentType = REFLECTION` 로드"로 재작성한다. 과도기 alias·이중 필드 금지. 삭제·소유 정책이 이 전환을 따른다(7번과 연동).

### Day 축

11. Reflection 행의 `journalDayId` 컬럼/비정규화는 **쓰지 않는다(null)**. 일자 완결 축(`diaryResolvedYn`/`dreamResolvedYn`, `JournalDayResolvedGuard`) **밖**에 둔다. day 의 완결 의미는 사유에 무의미하며, target 의 day(다른 지층)를 빌리면 `entry.chapter ≠ entry.day.chapter` 모순이 생기므로 절대 빌리지 않는다. **검색·월 필터의 날짜 축은 버리지 않는다** — `chapter.journalDayId` → `journal_day` 조인으로  deriv 한다(§13). "day 축 밖"은 완결 가드/resolvedYn 의미에만 해당한다.

### 표시

12. Reflection 은 **항상 자기 chapter 의 1급 엔트리**로 표시된다. target 이 있으면 **추가로** 그 target 밑에 "후대의 해석" 교차 뷰(백링크 렌더)로도 나타난다. 이는 서로 다른 지층의 두 뷰이지 중복이 아니다(chapter=작성 지층, target=대상 지층). **숨김 로직 없음.** 저자가 Reflection 을 target 과 동일 chapter 에 수동 배치한 경우에만 그 chapter 뷰에서 dedup 한다. chapter 목록 정렬은 entry `sort_order` 를 쓰고, target 밑 교차 뷰 정렬은 **역참조 쿼리의 별도 order**(`created_at` 등)를 쓰며 chapter `sort_order` 와 혼동하지 않는다(§4.2).

### 검색

13. 특별한 "Reflection 검색"은 없다. **Entry 통합검색 + `contentType` facet(일기/꿈/리플렉션) + Reflection 한정 `refContentType` 서브 facet(독립/일기해석/꿈해석)**. 기존 일기·꿈 검색은 유지한다. Reflection 의 yy/mnth·일자 필터는 entry 의 day 비정규화가 아니라 **소속 chapter → day** 조인으로 처리한다.

### 스키마

14. 단일 테이블 상속: target 컬럼을 `journal_entry` 에 nullable 로 얹는다(기존 `elseDreamYn`/`elseDreamerNm` 와 동일한 트레이드오프). 순수성(제네릭 Node)보다 기존 패턴과의 일관성을 택한다. full schema SSOT(`schema/full/mariadb/schema-journal-mariadb.sql`)도 런타임과 맞춰 `journal_entry`(+ target 컬럼)를 정본으로 두고 `journal_interpretation` DROP 을 반영한다.

---

## 2. 현재 코드 → target 델타 (영향 인벤토리)

| 현재 자산 | 흡수 후 |
|---|---|
| `journal_interpretation` 테이블 | `journal_entry`(contentType=`JOURNAL_REFLECTION`)로 수렴·제거 |
| `journal/interpretation/**` 모듈 (service/mapper/controller/repository/mapstruct/spec) | entry 경로로 흡수 후 제거 |
| `ContentType.JOURNAL_INTERPRETATION` 및 종속 strategy/cache/state policy | Phase 2 에서 DB·사이드 테이블 키를 `JOURNAL_REFLECTION` 으로 전환 후, Phase 4 에서 enum·strategy **제거**(장기 alias 금지) |
| `refId` + `refContentType` | target(nullable, Entry 타입 제약)으로 재해석 |
| `JournalEntryDto.journalInterpretationList` | Phase 3 에서 `reflectionList` 로 rename + target 역참조 enricher 재작성(alias 금지) |
| `JournalEntryService.postDelete`(및 soft-delete) | target 이 삭제 entry 인 Reflection 의 `refId`/`refContentType` **nullify**(규칙 7). cascade 삭제 없음 |
| `JournalInterpretationRegistModal.vue` | Reflection 등록/수정(entry 등록 경로)으로 흡수(Phase 4) |
| `JournalInterpretationItem.vue` | Reflection 아이템 + target 교차 해석 뷰로 흡수(Phase 4) |
| `assertChapterForEntry` / `resolveByChapterId` | REFLECTION-universal 예외 + 명시 타입 생성 경로 |
| `JournalEntryTypePolicy.supportsInterpretation()` | 의미가 "자식 합성 허용" → "Reflection target 가능"으로 바뀜. Phase 3+ 에서 `canBeReflectionTarget()` 등으로 재명명·재정의 검토(REFLECTION 도 target 가능) |
| `JournalDayResolvedGuard` | Reflection(day=null) 완결 축 제외 |
| Entry embedding 큐 | Reflection 도 Entry 와 동일하게 큐 진입(수렴 기본값). Phase 3+ 착지 시 확인 |
| `journal-interpretation-content` (`journal.scss`) | Reflection 본문 클래스로 이관(레거시 스타일·색상 보존) |
| `POST/GET/DELETE /api/journal/interpretation(s)` | entry 등록/수정/조회/삭제 API 로 수렴(Phase 4). Phase 1 동안은 **interpretation API·UI 유지**, Reflection create UI/공개 API **미노출** |

---

## 3. Phase 계획 (SAVEPOINT 단위)

한 커밋으로 불가능하므로 sub-savepoint 로 분할한다. 각 phase 는 그 시점에서 빌드·인코딩 게이트 통과와 호출 그래프 정합을 만족해야 한다(중간 페이지 비가동은 허용).

| Phase | 내용 | 규모 | 회복비용 |
|---|---|---|---|
| **0. 스펙 확정** | 본 문서로 target 설계·백필(§4)·phase 계획 확정 + DESIGN_NOTES 포인터. **코드 0줄.** | 小 | 없음 |
| **1. 스키마 축** | `JOURNAL_REFLECTION` contentType·`JournalEntryTypePolicy.REFLECTION` 배선, Entry 에 target(refId nullable) 수용, `assertChapterForEntry` REFLECTION-universal 예외, 명시 타입 생성 경로. 데이터 이관 없음. **쓰기 UI/공개 API 는 interpretation 유지.** Reflection create 는 내부·테스트만 또는 미노출(dual-path 남용 금지). | 中 | 낮음 |
| **2. 데이터 마이그레이션** | `journal_interpretation` → `journal_entry` 이관 + id 맵·attachable 재키잉·chapter/sortOrder/NOTE 버킷 백필·orphan target nullify(§4). 1회성. | 大 | 높음 |
| **3. 읽기 경로 수렴** | 조회·검색·표시(target 교차 뷰, day=null 완결 제외, chapter→day 검색 조인)·`reflectionList` enricher·**삭제 nullify 훅**을 entry 경로로 통합. interpretation **조회** 경로 제거. | 大 | 中 |
| **4. 쓰기 경로 + 모듈 제거** | 등록/수정 흡수, `interpretation/**` dead 제거, Vue 흡수·제거, `JOURNAL_INTERPRETATION` enum·strategy 제거, CSS 이관, 테이블 DROP. | 中 | 中 |

각 phase 착지 시 갱신할 as-built 스펙: 화면 추가·변경 → `screen-spec.md`, 인터랙션 → `interaction-spec.md`, 저널 전용 컴포넌트 → `component-spec.md`.

---

## 4. 백필 계약

기존 `journal_interpretation` 레코드에는 `journalChapterId` 가 없다. Entry 흡수 후 chapter 는 필수(규칙 4)이므로 각 레코드를 어느 chapter 에 착지시킬지 정한다. 이관은 **별도 AUTO_INCREMENT** 인 두 테이블을 합치므로 id·attachable 재키잉이 필수다.

### 4.1 Chapter 착지

- **`refId` 있음 + live target(soft-delete 아님)** → target 엔트리의 chapter 로 백필. (그 엔트리를 해석하려고 만들어졌으니 그 지층이 정직한 최선이다.)
- **`refId` 없음** → orphan-NOTE 버킷 chapter(§4.3).
- **`refId` 있으나 target 이 없거나 soft-deleted** → 규칙 7 과 동일하게 target(`refId`/`refContentType`) **nullify** 후 orphan-NOTE 버킷 착지.

이 결과로 데이터에는 두 규칙이 공존한다:

- **기존 데이터** = target 지층에 매장(과거의 작성 시점을 알 수 없으므로). orphan 은 NOTE 버킷.
- **신규 데이터** = 작성 시점 chapter(규칙 4).

이 비일관은 **명시 수용**한다. (대안이었던 "기존 전부를 미상 시점 NOTE 하나에 몰기"는 더 정직하나 과거 서사 맥락을 잃으므로 채택하지 않는다.)

### 4.2 Id 맵 · attachable 재키잉 · soft-delete · sortOrder

- interpretation → `journal_entry` INSERT 시 **새 id** 발급(`journal_interpretation.id` 와 `journal_entry.id` 는 별도 시퀀스).
- 이관 중 `(oldInterpretationId → newEntryId)` 맵을 유지한다.
- `(ref_id, ref_content_type)` 로 묶인 attachable·부수 행(comment / tag / state / lifecycle / history 등): `ref_content_type = JOURNAL_INTERPRETATION` → `JOURNAL_REFLECTION`, `ref_id` 를 새 entry id 로 재매핑.
- `file_group_id` 는 entry 행으로 복사한다(파일 그룹 행 자체 id 는 유지 가능).
- **활성(`deleted_at IS NULL`) interpretation 만** 이관한다. soft-deleted interpretation 은 **폐기**(이관하지 않음).
- **sortOrder**: 오늘 의미는 `(refId, refContentType)` 그룹 내 순서. 흡수 후 의미는 **chapter 내 entry 목록** 순서. 이관 시 chapter 별 `MAX(sort_order)+n` 으로 append 한다. 동일(구) target 을 갖던 행끼리는 구 `sort_order`·`created_at` 상대 순서를 보존한다. target 밑 교차 뷰 정렬은 chapter `sort_order` 가 아니라 역참조 쿼리 order 를 쓴다(규칙 12).

### 4.3 orphan-NOTE 버킷

"NOTE 버킷"은 day 에 묶인 일반 NOTE chapter 가 아니다.

- **정체**: `chapter_type = NOTE` 이고 `journal_day_id IS NULL` 인 **orphan-NOTE chapter 1개**(단일 사용자 앱이므로 전역 1개로 충분).
- 이관·신규 무소속 사유 작성 시 해당 chapter 를 보장·재사용하고, 없으면 생성한다.
- day 없는 chapter 는 스키마상 `journal_chapter.journal_day_id` NULL 허용과 맞춘다.

### 4.4 검색 날짜 축 (이관 후)

Reflection 행에 `journal_day_id` 를 채우지 않는다. yy/mnth·일자 네비게이션·검색 필터는 **소속 chapter 의 `journal_day_id` → day** 조인으로 처리한다. orphan-NOTE(`journal_day_id` null) 착지 행은 일자 facet 밖이며, 독립/무소속 facet 으로 조회 가능하다.

---

## 5. 결정 로그 (근거 보존)

향후 재해석을 막기 위해 핵심 판단의 근거를 남긴다.

- **소속(belongsTo) vs 표시위치(attached) 분리**: `chapterId`(소속)와 `refId`(표시)를 다른 축으로 둔다. 원리로 도출한 이 구분에 코드가 독립적으로 같은 골격(`chapterId` + 다형 `refId`)으로 수렴해 있었다 = 모델이 옳다는 강한 신호.
- **Reflection = Entry 서브타입** (별도 aggregate 아님): 별도로 두면 Entry 인프라(chapter·date·thread·연관글·검색·정렬)를 복제하게 된다. 현재 `JournalInterpretation` 이 이미 file/comment/state/history/day/sortOrder 를 복제 중이며, 흡수는 이 복제를 제거하는 수렴이다.
- **어느 chapter 가 Reflection 을 담나 — "any chapter"**: NOTE 전용 축으로 격리하면 사유를 자기가 속한 서사에서 떼어내게 된다. Reflection 은 본질 타입이므로 chapter 는 서사적 배치일 뿐, 어느 축이든 허용한다. NOTE 는 폐기되지 않고 "무소속 사유의 기본 버킷"(orphan-NOTE)으로 남는다.
- **chapter hard-owned 유지(nullable 아님)**: 코드의 cascade/orphanRemoval 불변식을 보존한다. "chapter 를 지운다 = 그 지층을 통째로 폐기한다"로 해석하면 정합적이다. 따라서 chapter 삭제 시 그 지층에 배치된 Reflection 도 cascade 되는 **blast radius 는 의도**다(target 삭제 nullify 와 축이 다르다).
- **target 삭제 nullify 는 신규 계약**: as-built 는 entry 삭제 시 interpretation 을 cascade 하지도 nullify 하지도 않아 orphan pointer 가 남는다. 흡수 후 불변식은 nullify 다.
- **reference 개념 폐기**: 코드에는 연관글(related) 하나뿐이다. 참조/연관을 별도 개념으로 세분화하는 것은 스스로 경계한 과도한 세분화다.
- **Phase 1 쓰기 미노출**: 스키마·policy 만 열고 interpretation 모듈을 잠시 유지하는 것은 수렴 과정의 일시 상태다. Reflection 공개 쓰기 UI/API 를 Phase 1 에 열면 dual-path 가 되므로 금지한다.
- **이관은 활성 행만**: soft-deleted interpretation 을 되살리지 않는다. 복구가 필요하면 이관 전 DB 백업이 SSOT 다.
