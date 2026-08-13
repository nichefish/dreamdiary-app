# 저널 어사이드 인터랙션 스펙 (Journal Aside Interaction Spec)

> 공통 인터랙션 패턴(AJAX, 모달, Tagify, TinyMCE 등)은 ``common/interaction-spec.md`` 참조.



## Vue SPA 구현 상태 (소스 기준)

| 인터랙션 | `features/journal/stores/journal.ts` / UI | 상태 |
|----------|---------------------------|------|
| 정렬 토글 | `sortOrder` + `toggleSort` + localStorage 유지 + 프론트 역순 | ✓ |
| 태그 클릭 컨텍스트 메뉴 | `tagContextMenu.ts` + `JournalTagContextMenu.vue` — 메뉴 액션과 태그 프로필 콘텐츠 유형 레이블은 현재 locale 카탈로그 사용 | ✓ |
| 일자 카드 ⋯ 컨텍스트 메뉴 | `JournalDayCard.vue` — Metronic dropdown | ✓ |
| 메타 버튼 드롭다운 | `JournalDayCard.vue` — `bi-bar-chart` 버튼 클릭 시 Bootstrap `dropup` 메뉴; 해당 일자 메타 항목 1개씩 나열; 항목 클릭 → `JournalDayMetaModal` 오픈; `width: max-content`로 내용 폭에 맞게 auto-size | ✓코드 |
| 일자 필터 모달 (메타+태그 다중 AND) | `JournalDayMetaModal.vue` — 메타 또는 태그를 시드로 열림(`openDayFilterModal`); 상단 칩에 선택 메타(파랑)·태그(초록) 혼합 표시; 최초 시드 칩도 × 클릭으로 자유 제거(제한 없음)되며 같은 seed 의 payload 재조회로 다시 주입하지 않는다; 모든 필터 제거 시 빈 결과 반환(payload.list 전체 노출 방지); AND 필터(모든 선택 메타+태그 보유 날짜만); 행에서 비선택 메타 뱃지 클릭 → 메타 필터 추가, 비선택 태그 클릭 → 태그 필터 추가, 선택된 태그 클릭 → 태그 필터 제거; 각 행의 선택 메타 값은 `selectedMetas` 배열 순서(선택 순)대로 표시하여 행마다 순서 일관성 유지; 연도 변경 시 필터 유지(재조회만), 신규 오픈 시 시드 1개로 초기화; 각 일자는 카드형(날짜 → 메타·태그 → SUMMARY)으로 표시하고, SUMMARY 첫 non-empty 엔트리 본문(`summaryEntryHtmlOf`, SEARCH `journalChapterList` 파생)을 레거시 `collapse-3`/`expand-btn`으로 최대 3줄 미리보기한 뒤 클릭·더보기로 전체를 펼친다(접힘 중 빈 문단·여백 축소, 펼침 시 원문 유지); `JournalDayTagDetailModal` 제거하여 단일 모달로 수렴; 태그 입력 검색 — 컨트롤 행의 태그 입력(모달 내 datalist 미표시 대응: 인라인 typeahead 미리보기; 모달 오픈·포커스 시 `journalModalStore.dayTagCategoryMap`(SSOT)과 `/api/journal/day/tags` 를 병합해 최초 1회 로드)으로 기존 태그만 AND 필터에 추가(엔트리 검색과 동일 `findKnownTagName`·categoryMap 매칭); 카탈로그에 없는 이름은 Swal 대신 인라인 안내(모달 유지), 동명 태그(다중 카테고리)는 카테고리 선택 버튼으로 분기; 모달 닫힘 시 입력·힌트·카테고리 선택 상태 초기화(카탈로그 캐시는 유지) | ✓코드 |
| 엔트리 ⋯ 컨텍스트 메뉴 | `JournalEntryItem.vue` — lifecycle/status/수정/이력/관련글/스레드에 추가/삭제 | ✓ |
| FLOW 연결 (수렴 완료) | FLOW 를 스레드 소속으로 수렴 완료했다(`docs/spec/DESIGN_NOTES.md`). 「흐름 보기」·본문 요약 행·「흐름 연결」 UI 는 모두 제거됐다(나-2a·나-2b). 백엔드 `flowSummary` 와 `related_content` FLOW 행도 다-2 에서 제거됐다. | ✓ |
| 엔트리 클라이언트 접힘 토글 | `JournalEntryItem.vue` — `localCollapsedOverride` ref | ✓ |
| 챕터 복사 버튼 | `JournalChapterItem.vue` — `copyChapter()`, 날짜(요일)·카테고리·엔트리 전체 텍스트 클립보드 복사 | ✓ |
| 챕터 접힘 스레드 요약 | `JournalChapterItem.vue` — 접힌 상태에서 하위 엔트리 `threadList`를 `threadId`로 중복 제거해 태그와 함께 접힘 바깥에 스레드 버튼 표시; 클릭 시 현재 화면 위에 전역 스레드 상세 모달 열기 | ✓ |
| 기간별 스레드 요약 | 월간·주간·연간 결산 태그클라우드 아래에 필터와 무관한 기간 스레드를 표시하고 현재 화면 위에 스레드 상세 모달 열기; nullable Prefix는 스레드 목록과 같은 이름·색 배지로 제목 앞에 표시하며 비활성 과거 선택도 유지; 월간·연간 10개 이후 펼치기. 라벨은 `스레드` | ✓ |
| 꿈 복사 버튼 | `JournalDayCard.vue` — `copyDreams()`, 날짜(요일) 헤더 + 꿈 엔트리 전체 클립보드 복사 | ✓ |
| 엔트리 복사 버튼 | `JournalEntryItem.vue` — `copyEntry()`, 날짜(요일)·본문 텍스트 클립보드 복사 (레거시 동일 포맷) | ✓ |
| 헤더 검색 드롭다운 | `Search.vue` — 일기/꿈 유형 선택 + debounce 검색 + 결과 링크 (`journal-entry-search`) | ✓ |
| 메타 VIEW · 메타 컨텍스트 메뉴 | `metaContextMenu.ts` + `JournalMetaContextMenu.vue` — 헤더 `#메타` 클릭 시 팝업(태그 메뉴와 동일 UI); 현재 locale 메뉴로 「그래프로 보기」→ `addMetaToGraph`(최대 2·이미 있으면 비활성, 제한 경고도 현재 locale), 「검색」→ `openDayFilterModal`(`JournalDayMetaModal`), 「메타 설정」→ `openMetaProfile`(`JournalMetaProfileModal`, `GET /api/journal/day/metas/{id}`) | ✓코드 |
| 메타 VIEW 비교 그래프 | `JournalDayMeta.vue` — `selectedMetas` 최대 2; 헤더에서 그래프에 포함된 메타는 굵게 표시·옆 × 제거; 연도 「전체」(yy 미전송)·임계값·메타별 통계; **한 ApexCharts**에 시리즈 최대 2개(일자 합집합 X축, 범례, 단위 다르면 Y축·툴팁에서 메타별 단위) | ✓코드 |
| Pinpoint | `JournalAside.vue` — `pinnedYy/pinnedMnth` ref + pinpoint/turnback 함수 | ✓ |
| 챕터 말머리 필터 | `JournalAside.vue` — `JOURNAL_CHAPTER_DIARY`(일기 챕터) 개인 Prefix 체크박스, `store.chapterPrefixIds` → `fetchDays` | ✓ |
| 일기/꿈 라이프사이클 필터 | `JournalAside.vue` — `store.diaryLifecycleKey` / `store.dreamLifecycleKey` → `fetchDays` 후 일기/꿈 각각 후처리 필터 | ✓ |
| 주간 네비게이터 | `JournalAside.vue` — 요일 버튼 7개, 이전/다음 주 화살표, 주간 범위 라벨 | ✓ |
| 연/월 select | 연도 select + 월 그리드 (`navigateMonth`, `gotoYyMnth`) | ✓ |
| 일간 날짜 네비게이션 | 탭(`journal-daily-tab`)은 aside의 `JournalAsideMiniCalendar.vue`만 사용하며 중복 본문 네비게이션 행을 표시하지 않는다. 팝업(`journal-daily`)은 aside가 없으므로 본문 이전/날짜/다음 행을 유지한다. 두 경로 모두 날짜 선택 시 `router.replace({ query: { stdrdDt } })`로 이동한다. 미니 달력은 토/일·공휴일을 빨간색으로 표시하고 공휴일은 `GET /api/schedule/holidays`로 조회한다. | ✓ |
| 툴바 키워드 전체검색 | `JournalDayViewToolbar` 로컬 ref → `openSearchTab()` → 새 탭 `/vue-app/journal/entry/search` | ✓ |
| 툴바 floating·aside 열기 | `JournalDayViewToolbar` 전체와 열린 저널 일자 aside의 상단선이 고정 앱 헤더 아래에서 sticky로 일치하고, 툴바는 별도 그림자 없이 하단 경계만 사용. aside 숨김 시 우측 끝 버튼이 `asideStore.show()` 호출. 모바일은 본문 우상단 전용 버튼 유지 | ✓ |
| 어사이드 목록 키워드 필터 | `JournalAside.vue` `diaryKeyword` / `dreamKeyword` → `fetchDays()` (목록 축소) | ✓ — 툴바 `openSearchTab` 전체검색과 분리 (`vue-screen-overview.md` 필터·검색 정책) |
| 등록/수정 후 확인·스크롤 | 일자/챕터/엔트리 등 submit 성공 → 성공 알림 OK 이후 저장 위치 scrollIntoView(일간 일자 저장은 sticky 툴바·날짜 네비 유지를 위해 스크롤 생략·`stdrdDt` query 동기화). 엔트리는 성공 알림 전 목록/상세 DOM을 먼저 준비하고, OK 이후에는 스크롤만 수행한다. 월간/주간 화면은 재조회 중 기존 목록 DOM을 유지한다. 챕터는 저장된 챕터 DOM(`#journal-chapter-{id}`)을 우선 탐색하고 없으면 일자 카드로 fallback. 신규 엔트리 등록 후에는 완료 자동 접힘·서버 COLLAPSED 여부와 무관하게 대상 챕터를 현재 화면에서 일회성으로 펼치며, 신규 Reflection 등록은 같은 저장 응답의 Reflection ID에 `requestReflectionCreatedCollapse`로 일회성 로컬 접힘을 심어 챕터 펼침·부모 expand signal보다 우선한다, 수정 저장에는 적용하지 않고 서버 접힘 상태도 변경하지 않는다. | ✓ |
| 상태/라이프사이클 변경 후 갱신 | 상태 토글·라이프사이클 설정 서버 반영 후 `refreshJournalEntryHostForRoute`가 `detailOpen`을 전경 판단 기준으로 사용한다. 스레드 상세가 열려 있으면 상세·집계 태그·소속 엔트리를 다시 조회하고, 배경이 주간/월간/일간(탭·팝업 모두)이면 `refreshJournalDaysForRoute`도 실행하되 배경 스크롤은 하지 않는다. 배경이 검색 팝업(`journal-entry-search`)이면 등록된 `loadEntries`로 로컬 결과(스레드 칩 포함)를 함께 재조회한다. 상세가 닫힌 주간/월간/일간 route는 기존 목록 갱신 → `#journal-day-{stdrdDt}` scrollIntoView를 유지한다. **일간(`journal-daily`/`journal-daily-tab`)** 은 `route.query.stdrdDt`(없으면 항목 `stdrdDt`)로 `viewType=DAILY`·`yy`/`mnth` 파생 조회 — 무파라미터 `fetchDays()`는 스토어 기본 월(오늘)로 재조회되어 날짜가 어긋남 | ✓ |
| 챕터 일자 변경 | `JournalChapterRegistModal.vue` — 수정 모드+비DREAM 한정, 날짜 picker + 챕터 일자 변경 버튼, 현재 locale 확인창 사용, `POST /api/journal/chapter/{id}/move` 호출. 응답 `message`를 우선 표시하고 없으면 현재 locale fallback을 사용한 뒤 `fetchDays` + 신 일자 scrollIntoView | ✓ |
| 챕터 소유권 표시 | `JournalChapterItem.vue` — API `isCreatedBy`; 타인 작성 시 배지·쓰기 버튼 숨김; 클라이언트 차단 경고는 현재 locale 카탈로그 사용, 수정/삭제/이동 API 거부 시 서버 `msg.rslt.not-owner` (403) alert | ✓ |
| 챕터 resolved (파생) | 챕터 자체 resolved 상태 없음. Vue `allEntriesResolved` → 루트 `.is-all-resolved`(PENDING의 `.is-all-pending`과 동형). 접힘·펼침 초록 inset/배경은 이 클래스 기준으로 표시한다. 중요·참조 상태선은 하위 DOM `:has`로 조합한다. 접힘 바: 완료 1px 초록·중요 2px 빨강·참조 4px 노랑(엔트리 `$journal-paired-states` 와 동일). 단독 우선 중요>참조>완료; 중요+완료·중요+참조·삼중 조합 다중선. DB 마이그레이션: `lifecycle` 테이블 `ref_content_type='JOURNAL_CHAPTER'` RESOLVED 레코드 소프트 삭제 | ✓ |
| Reflection 전체 (( )) | `JournalReflectionItem` ⋯ 메뉴 「전체 (( ))」(`wrapEntireNoti`). 저장 원문 `content`의 각 `<p>`/`<li>`에 Markdown `((...))`를 멱등 적용하고, 변경 시에만 `POST /api/journal/reflection/{id}`(multipart)로 저장한 뒤 `refreshJournalEntryHostForRoute`로 갱신한다. 이미 적용된 본문·빈 본문은 API 없이 안내만 표시한다 | ✓ |
| TAGCLOUD/DIARIES/DREAMS | `showTagCloud` 등 + 토글 핸들러. 일간에서는 URL `stdrdDt` 하루를 태그 기간 축으로 사용하며 날짜 이동 시 일자·일기·꿈 태그클라우드를 함께 갱신한다. | ✓ |

**일자 필터 모달 i18n**: 제목·결과 건수·연도/전체 연도·연월 구분선·필터 추가/제거·일자 새 창 tooltip·빈 상태·닫기와 조회 실패 fallback은 현재 locale의 클라이언트 카탈로그를 사용한다. locale 변경은 선택 메타/태그·AND 필터·모든 필터 제거 시 빈 결과·연도 변경 시 필터 유지 계약을 변경하지 않는다. 태그 입력 검색의 placeholder·카테고리 선택·미존재 태그 알림 문구는 엔트리 검색 키(`journal.entry.search.tag.*`, `journal.entry.search.category.*`)를 재사용한다.

**FLOW 종단 추적 계약 (수렴 완료 — 종단 보기 ❌ 제거)**: FLOW 를 스레드 소속으로 수렴 완료했다(`docs/spec/DESIGN_NOTES.md`). 「흐름 보기」 종단 보기(타임라인 모달)와 본문 FLOW 요약 행은 나-2a 에서 제거됐다. 「흐름 연결」(`openRelatedFlow`) UI 도 나-2b 에서 제거됐고, `RelatedContentAddModal` 은 일반 관련 글 전용이 됐다. 백엔드 `flowSummary` 배치 계산·`GET /api/related/{contentType}/{id}/flow`·`RelationType.FLOW` enum·`related_content` FLOW 행도 모두 제거됐다(다-2). FLOW 축은 수렴 완료. 저널 스레드 연결은 독립 상위 서사에 엔트리를 소속시키는 새 축이며, FLOW 간선 29건은 V0.24.8 에서 스레드 4건·소속 33건으로 이미 이관됐다.

**스레드 소속 등록 서버 경계**: `POST /api/journal/threads/{id}/entries`는 소속 행을 조회·복원·저장하기 전에 대상 스레드와 엔트리가 모두 존재하고 현재 사용자 소유인지 검증한다. 엔트리가 없으면 not found, 타인 소유이면 access denied로 응답하며, 두 실패 모두 기존 소속 조회·복원·INSERT를 수행하지 않는다. 변경 전에는 스레드 소유권만 검사해 직접 API 요청으로 타인 엔트리 소속 행이 만들어질 수 있었고, 변경 후에는 서버 쓰기 경계에서 이를 차단한다.

**스레드 소속 후보 계약**: `GET /api/journal/threads/candidates?entryId=...&keyword=...&prefixId=...&includeResolved=...&limit=...`는 현재 사용자 소유 스레드만 경량 후보로 반환한다. nullable 단일 `prefixId`는 같은 Prefix를 직접 참조하는 스레드만 남기며, 선택 가능한 Prefix는 로그인 사용자의 `(PERSONAL, user_id, JOURNAL_THREAD)` Scope에 속해야 한다. 정렬은 현재 엔트리 소속 여부 → 가장 최근 활성 소속 `created_at` → 활성 소속 수 → 스레드 수정·생성 시각 → ID 역순이며, 소프트 삭제된 소속은 집계하지 않는다. 제목 검색과 말머리 필터는 정렬 전에 후보 집합을 좁히고, `limit`는 서버에서 1~20으로 보정한다. 기본은 완료(`RESOLVED`) 스레드를 제외하고, `includeResolved=true`일 때만 포함한다. 라이프사이클 행이 없으면 `OPEN`으로 본다. 응답 각 후보에 nullable 단일 `PrefixDto prefix`와 `lifecycleKey`를 실어 보내며 Prefix 이름·색으로 말머리를 표시한다. 별도 사용 시각 컬럼이 없으므로 「최근 사용」은 최근 활성 소속 추가 시각으로 정의한다. 「스레드에 추가」 서브메뉴는 진입한 엔트리 ID로 매번 후보를 조회하며 다른 엔트리로 전환하면 이전 필터와 후보·「완료 포함」토글을 비운다. 제목 입력은 250ms debounce, 말머리 변경·「완료 포함」토글은 즉시 재조회한다. 요청 순번으로 늦게 끝난 이전 엔트리 응답을 폐기하고, 같은 엔트리의 재조회 실패는 직전 성공 후보를 보존하면서 오류를 별도 표시한다. 「새 스레드로 시작」은 말머리(nullable `prefixId`)와 제목을 받아 `POST /api/journal/threads`로 생성한 뒤 현재 엔트리를 소속시킨다. 서브메뉴 말머리 필터가 있으면 생성 폼 기본값으로 쓴다. 소속 추가·해제·새 스레드 생성 성공 후 현재 메뉴가 같은 엔트리를 가리킬 때 후보를 다시 조회해 체크와 랭킹을 갱신한다. 같은 성공 후 `JournalThreadStore.refreshPeriodSummary()`로 태그클라우드 아래 기간별 스레드 요약도 마지막 조회 조건으로 재조회해 새로 소속된 스레드와 건수 변화를 반영한다. 요약이 마운트된 월간·주간에서만 실제 재조회가 일어나며(store가 마지막 조회 조건 보유), 요약 화면 이탈 시 조건을 비워 비활성 재조회를 막는다.

---

## 어사이드 인터랙션 패턴

---

### 정렬 토글 (Sort Toggle)

**트리거**: 필터 카드 헤더 정렬 버튼 (`#sortIcon`) 클릭

**레거시 구현**: `JournalDayAsideFilterHeaderApp.ts` → `bridge.sortAside()` 호출

**Vue SPA 구현**:
```typescript
// journal.ts store에 추가
const sortOrder = ref<'ASC' | 'DESC'>('DESC');
function toggleSort() {
    sortOrder.value = sortOrder.value === 'DESC' ? 'ASC' : 'DESC';
    void fetchDays();  // 정렬 적용 후 목록 재조회
}
```

**아이콘 변경**: 정렬 방향 반영
- `DESC` (최신순): `bi-sort-numeric-down-alt`
- `ASC` (오래된순): `bi-sort-numeric-up-alt`

**fetchDays 파라미터**: `sort` 쿼리에 `sortOrder` 반영 (`JournalDaySearchParam.sort` 구현 완료)

**기본값**: `DESC` (최신순). localStorage(`journal_day_sort`) 에서 복원.

**프론트 역순 처리**: 백엔드는 항상 ASC 반환. `sortOrder !== "ASC"` 이면 `[...rslt].reverse()` 적용.

**localStorage 유지**: `toggleSort()` 내에서 `localStorage.setItem("journal_day_sort", sortOrder.value)`.

---

### 핀포인트 (Pinpoint)

**목적**: 현재 조회 중인 연/월을 임시 저장하고, 나중에 그 시점으로 돌아올 수 있게 함.

**트리거 A — 핀 고정** (`<i class="bi bi-bookmarks">`):
1. `asideStore.setPinpoint(store.yy, store.mnth)` — `localStorage` 동기 저장
2. UI 갱신: 고정 연도·월 표시 (`asideStore.pinnedYy` / `pinnedMnth`)

**트리거 B — 돌아가기** (`<i class="bi bi-reply-all">`):
1. `if (asideStore.pinnedYy && asideStore.pinnedMnth)`
2. `store.gotoYyMnth(...)` 호출
3. 목록 재조회 (`store.fetchDays()` 내부 호출됨)

**상태 저장 위치**: `useJournalAsideStore` + 브라우저 `localStorage` 키 `journal_day_pinpoint` (`{ yy, mnth }` JSON). 서버·계정 설정에 저장하지 않음. 새로고침·재방문 시 복원.

**초기 표시**: `pinnedYy === null` → `<span id="pinnedYy">----</span>`, `<span id="pinnedMnth">--</span>`

---

### 챕터 말머리 필터 (Chapter Prefix Filter)

**트리거**: CHAPTER PREFIXES 항목 체크박스 변경

**변경 전**: 레거시·초기 Vue는 공통 코드의 챕터 카테고리 문자열을 `chapterCtgrCds`로 전송했다.

**Vue SPA 구현**:
```typescript
// JournalAside.vue 또는 JournalAsideEntryFilters 컴포넌트 내
function toggleChapterPrefix(prefixId: number) {
    if (store.chapterPrefixIds.includes(prefixId)) {
        store.chapterPrefixIds = store.chapterPrefixIds.filter((item) => item !== prefixId);
    } else {
        store.chapterPrefixIds = [...store.chapterPrefixIds, prefixId];
    }
    void store.fetchDays();
}
```

**DIARIES 부모 토글과의 관계**:
- 챕터 말머리 필터는 `DIARIES` 하위 필터다.
- `store.showDiaries=false` 동안 챕터 말머리 필터 UI는 렌더링하지 않는다.
- `DIARIES` OFF는 `store.chapterPrefixIds` 값을 삭제하지 않는다. 다시 ON으로 돌리면 기존 선택값이 그대로 적용된다.

**챕터 옵션 로드 타이밍**: `onMounted` 시 `journalModalStore.prefetchChapterPrefixes("DIARY")`가 콘텐츠 타입별 공통 Prefix 옵션 store를 통해 `GET /api/my/prefixes/options?contentType=JOURNAL_CHAPTER_DIARY`를 호출한다(일기 필터는 일기 챕터 목록을 사용). 챕터 말머리는 유형별로 분리되며(일기 `JOURNAL_CHAPTER_DIARY`, 노트 `JOURNAL_CHAPTER_NOTE`), 동일 시점의 호출자는 진행 중인 Promise를 함께 기다린다. 내 설정에서 일기 챕터 말머리를 변경하면 `JOURNAL_CHAPTER_DIARY` 캐시만 무효화하고 다음 프리페치가 서버 확정 목록을 다시 조회한다.

**store 반영**: `store.chapterPrefixIds: number[]` — `fetchDays`에서 선택 ID를 콤마 구분 `chapterPrefixIds` query param으로 전송한다. 서버는 시스템 요약과 Prefix 미선택 챕터를 항상 유지하고, 제외된 Prefix 이름·색상을 `hiddenChapterPrefixList`로 반환한다.

---

### 일기/꿈 라이프사이클 필터

**목표**: 우측 ENTRY FILTER에서 일기와 꿈을 각각 라이프사이클 상태로 거른다.

**Vue 구현**:
- `store.diaryLifecycleKey`: 일기 전용 lifecycle 필터
- `store.dreamLifecycleKey`: 꿈 전용 lifecycle 필터
- 옵션: 전체(`""`), 진행 중(`OPEN`), 보류(`PENDING`), 완료(`RESOLVED`)
- select 변경 즉시 `store.fetchDays()` 호출
- 필터 초기화 시 두 값 모두 `""` 로 되돌림
- 일기 LIFECYCLE은 `DIARIES` 하위 필터이며, `store.showDiaries=false` 동안 렌더링하지 않는다.
- 꿈 LIFECYCLE은 `DREAMS` 하위 필터이며, `store.showDreams=false` 동안 렌더링하지 않는다.
- 부모 토글 OFF는 하위 필터 값을 삭제하지 않는다. 다시 ON으로 돌리면 기존 값이 그대로 적용된다.

**서버 반영**: `JournalDaySearchParam.diaryLifecycleKey` / `dreamLifecycleKey` 를 `JournalDayFilterHelper.filterInMemory()`에서 적용한다. 일기/꿈 키워드와는 AND 조건이며, 저장 row와 캐시 맵 항목이 없는 엔트리는 DTO에서 `OPEN`으로 정규화해 필터링한다.

---

### 주간 네비게이터 (Week Navigator)

**목적**: 사이드바에서 현재 주의 요일을 셀로 표시하고, 날짜 클릭으로 해당 날짜 기준 목록 조회.

**`WeekDayItem` 데이터 모델**:
```typescript
interface WeekDayItem {
    label: string;      // '월', '화', '수', '목', '금', '토', '일'
    dateStr: string;    // 'YYYY-MM-DD'
    hasDay: boolean;    // true면 해당 날짜에 저널 데이터 있음 → 강조 표시
    isActive: boolean;  // true면 현재 선택 날짜 → active 상태
}
```

**주간 범위 라벨 계산**:
```typescript
// weekStartDt: 해당 주 월요일 (YYYY-MM-DD)
// weekEndDt: weekStartDt + 6일
const weekRangeLabel = computed(() => {
    if (!store.weekStartDt) return '----';
    const end = addDays(store.weekStartDt, 6);
    return `${store.weekStartDt.slice(5)} ~ ${end.slice(5)}`;  // MM-DD ~ MM-DD
});
```

**이전 주 이동** (`leftWeek`):
- `store.weekStartDt` 기준 -7일 날짜 계산
- `store.fetchDays({ viewType: 'WEEKLY', weekStartDt: prevWeekStart })`

**다음 주 이동** (`rightWeek`):
- `store.weekStartDt` 기준 +7일
- `store.fetchDays({ viewType: 'WEEKLY', weekStartDt: nextWeekStart })`

**요일 셀 클릭** (`clickDay(dateStr)`):
- `store.fetchDays({ stdrdDt: dateStr })` — 해당 날짜 기준 조회

**오늘 주 이동** (`todayWeek`):
- 오늘 날짜 기준 해당 주 월요일 계산 → `store.fetchDays({ weekStartDt: ... })`

**주간 범위 레이블 클릭 → 날짜 선택기** (`openWeekPicker` / `onWeekPickerChange`):
- 사이드바의 주간 범위 레이블(`MM-DD ~ MM-DD`)을 클릭하면 브라우저 네이티브 date picker 팝업.
- 팝업 표시 기준일은 현재 `store.weekStartDt`(해당 주 월요일) — `:value` 바인딩 및 `openWeekPicker` 직전 `input.value` 동기화.
- 날짜 선택 시 `getWeekStartDateStr(selectedDate)`로 해당 주 월요일 계산.
- `store.weekStartDt`, `store.yy`, `store.mnth` 갱신 후 `await store.fetchDays()` 호출.
- 목록 렌더 완료 후 선택한 날짜(`val`)에 해당하는 `#journal-day-{val}` 카드로 `scrollIntoView({ behavior: "smooth", block: "start" })`.
- 구현: 숨긴 `<input type="date">` (opacity:0, pointer-events:none) + `showPicker()` 호출.

**요일 버튼 클릭 → 해당 일자 카드 스크롤** (`selectWeekDay`):
- 현재 주 내 요일 버튼 클릭 시 `selectedDt`(활성 표시) 갱신.
- `await nextTick()` 후 `#journal-day-{dateStr}` 카드로 `scrollIntoView({ behavior: "smooth", block: "start" })`.
- `hasDay`가 false인 버튼(데이터 없는 날짜)은 비활성화(disabled).

---

### 연/월 SELECT 기반 내비게이션

레거시에서는 연도·월을 `<select id="yy">` / `<select id="mnth">` 로 선택했다.
Vue SPA의 현재 구현(그리드+화살표)과 달리 select 방식이었음.

**레거시 연도 목록**: 현재 연도 → 2010년까지 역순, 2010년은 `"~2010"` 레이블

**연도 변경 시 동작** (`onYyChange`):
1. 선택된 연도로 `store.yy = newYy`
2. 월 선택 초기화 (`store.mnth`를 유지하거나 초기화 — 레거시는 월 select 초기화)
3. `store.fetchDays()` 호출

**월 변경 시 동작** (`onMnthChange`):
1. `store.gotoYyMnth(store.yy, newMnth)`

> **구현 노트**: Vue SPA는 현재 그리드+화살표 방식이 select보다 UX상 낫다.
> 다만 `id="yy"`, `id="mnth"` 가 없어 레거시 jQuery 코드와 호환이 안 된다.
> 완전 수렴 후 레거시 jQuery는 제거 대상이므로 id 호환보다 Vue 방식 유지가 맞다.

**DAILY viewType 미니 달력**:
- `store.viewType === 'DAILY'` 일 때 월 그리드(1~12월) 대신 `JournalAsideMiniCalendar` 컴포넌트를 렌더한다.
- 요일 헤더(일~토) + 해당 월 날짜 셀을 7열 CSS grid로 표시한다.
- 선택된 날짜(`route.query.stdrdDt`)는 `is-selected`(파란 배경), 오늘은 `is-today`(파란 테두리)로 구분한다.
- 토요일·일요일은 `is-weekend`(빨간색 텍스트)로 표시한다. 요일 헤더의 일/토도 동일.
- 공휴일은 `is-holiday`(빨간색 텍스트)로 표시한다. `GET /api/schedule/holidays?yy=&mnth=` 로 해당 월의 공휴일 날짜 목록을 조회하며, `store.yy`/`store.mnth` 변경 시 재조회한다. 서버는 캐시된 전체 공휴일 엔티티에서 year/month 필터링하여 반환한다.
- 선택된 날짜가 주말·공휴일이어도 `is-selected` 흰색 텍스트가 우선한다.
- 날짜 클릭 → `router.replace({ query: { stdrdDt: 'YYYY-MM-DD' } })` → `JournalDayDaily`의 `stdrdDt` watch가 재조회.
- 월 이동 chevron은 aside `store.yy`/`store.mnth`만 변경하고, `fetchDays`를 호출하지 않는다(달력 표시 월만 전환).
- TODAY 버튼은 오늘 날짜로 `store.yy`/`mnth` + `router.replace({ query: { stdrdDt } })`를 함께 수행한다.

---

### 키워드 검색 / 필터 (Keyword Search & Filter)

**툴바 전체검색 (`JournalDayViewToolbar.vue`)**
- 로컬 `ref`(`localDiaryKw` / `localDreamKw`) 사용 — `store.diaryKeyword/dreamKeyword`(필터 상태)와 완전 분리
- 검색 버튼 클릭 / Enter → `assertAuthenticatedBeforePopup(router, route)` 로 현재 세션을 먼저 확인한다. 세션이 풀렸으면 새 창을 열지 않고 현재 화면에서 로그인 복귀 안내를 표시한다.
- 인증 확인 성공 시 `openSearchTab(type, keyword)` → `window.open(/vue-app/journal/entry/search?type=...&searchKeywords=..., journal-entry-search-{type}, "width=1960,height=1440,top=0,left=270")` 새 창 (태그 컨텍스트 메뉴와 동일 방식, 같은 타입 재검색 시 창 재사용)
- BASE_URL: `import.meta.env.BASE_URL` (vite config `base: "/vue-app/"`)

**어사이드 현재결과 필터 (`JournalAside.vue`)**
- `v-model="store.diaryKeyword"` / `v-model="store.dreamKeyword"` — store 상태 직결
- funnel 버튼(`@click`) / Enter(`@keyup.enter`) 모두 `store.fetchDays()` 호출
- 현재 목록을 재조회하는 필터이며, 툴바 검색과 동작이 다름

**중요 보존 규칙**:
- 툴바 인풋은 store 상태를 오염시키지 않는다 (`name` 속성 없음, 로컬 ref만 사용)
- 어사이드 인풋은 store 상태를 직접 바인딩하며 필터로만 작동한다

### 등록/수정 후 일자/챕터 위치 스크롤

**트리거**: `JournalDayRegistModal` submit 성공 (등록 및 수정 공통)

**저장 메시지 i18n**: 날짜 필수 검증·등록/수정 확인·성공/실패 fallback은 현재 locale의 클라이언트 카탈로그를 사용한다. 저장 API 응답에 `message`가 있으면 서버 메시지를 우선 표시한다.

**동작**:
1. `model.journalDate`를 `savedDate`로 캡처
2. 모달 닫기
3. 성공 알림 표시
4. 사용자가 OK를 누른 뒤 `refreshCurrentDayView(savedDate)` 호출
5. **일간(`journal-daily` / `journal-daily-tab`)**: sticky 뷰 툴바 아래로 날짜 네비(이전/날짜/다음)가 스크롤되어 사라지지 않도록 `scrollIntoView`를 호출하지 않는다. `savedDate`가 `route.query.stdrdDt`와 다르면 `router.replace({ query: { stdrdDt: savedDate } })`로 URL을 맞추고 `JournalDayDaily`의 `stdrdDt` watch가 재조회한다. 같으면 `refreshJournalDaysForRoute`만 호출한다.
6. **주간/월간 등**: `fetchDays()` 완료(`.then`) → `nextTick` → `document.getElementById('journal-day-{savedDate}')` → `scrollIntoView({ behavior: "smooth", block: "start" })`

**카드 id SSOT**: `JournalDayCard.vue` `:id="'journal-day-' + day.stdrdDt"`

**챕터 id SSOT**: `JournalChapterItem.vue` `:id="'journal-chapter-' + chapter.id"`

**챕터 등록/수정 후 동작**:
1. 저장 성공 응답에서 챕터 ID를 확인한다. 수정이면 기존 `model.id`를 fallback으로 사용한다.
2. 모달을 닫고 성공 알림을 표시한다.
3. 사용자가 OK를 누른 뒤 현재 route 기준으로 월간(`LIST`) 또는 주간(`WEEKLY`) 목록을 다시 조회한다.
4. 목록 렌더 완료 후 `#journal-chapter-{id}`로 스크롤한다.
5. 신규 등록 등 챕터 ID를 확인할 수 없거나 retry 이후에도 챕터 DOM을 찾지 못하면 `#journal-day-{stdrdDt}`로 스크롤한다.

**챕터 삭제/상태 변경 후 동작**:
- 삭제는 삭제 전에 `stdrdDt`를 캡처하고 현재 locale 확인창을 표시한다. 삭제 응답 `message`를 우선 표시하고 없으면 현재 locale fallback을 사용하며, 성공 알림 OK 이후 현재 route 기준 목록 재조회 + `#journal-day-{stdrdDt}` 스크롤.
- 상태 변경은 현재 route 기준 목록 재조회 + `#journal-day-{stdrdDt}` 스크롤.

---

### 저널 일자 중복 등록 차단
**트리거**: `JournalDayRegistModal` 신규 등록 submit

**동작**:
1. 서버는 로그인 사용자 기준으로 같은 `journalDate`의 활성 저널 일자가 있는지 확인한다.
2. 중복이면 기존 데이터를 수정으로 전환하지 않고 `rslt=false`, `msg.journal.day.duplicate` 메시지로 응답한다.
3. 모달은 저장 성공 처리를 진행하지 않고 서버 메시지를 표시한다.
4. 신규 등록 성공 시 서버는 `JournalDayBootstrapService`로 시스템 요약 챕터(`summaryYn=Y`)와 빈 DIARY 엔트리 구조를 보장한다. 이미 DIARY 챕터가 있으면 추가 생성하지 않는다.

---

### 첫 일반 챕터 시스템 요약 역할 자동 부여
**트리거**: 챕터 신규 등록 (`JournalChapterService.preRegist` — 수동 등록 및 `JournalDayBootstrapService` 자동 등록 공통)

**동작**:
1. 새 일반 챕터의 `sortOrder` 는 같은 일자의 기존 일반 챕터만 대상으로 마지막 순번+1을 계산한다. 시스템 요약·DREAM은 순번 밖(`sortOrder=0`)이며 일반 챕터만 1..N 순번을 사용한다.
2. 시스템 요약 역할 판정은 **"기존 non-DREAM 챕터가 없는가"** 기준이다. DREAM 은 항상 마지막에 배치되는 개념 챕터이므로 판정에서 제외한다.
3. 첫 non-DREAM 챕터는 제출된 `prefixId`·`summaryYn`과 무관하게 `summaryYn=Y`가 되고 Prefix 선택은 제거된다.
4. 이후 일반 챕터는 `summaryYn=N`이며 사용자가 `summaryYn=Y`를 직접 제출할 수 없다. 시스템 요약은 사용자가 고르는 말머리 옵션이 아니다.
5. 결과적으로 꿈 챕터가 먼저 있는 날에 첫 일기·노트 챕터를 등록해도 시스템 요약 역할이 정상 부여된다.
6. 시스템 요약 자동 등록 뒤 빈 DIARY 생성 여부도 `summaryYn=Y`로 판정한다. DREAM이 먼저 존재해도 첫 non-DREAM 챕터는 `summaryYn=Y`·`sortOrder=0`으로 등록되어 빈 DIARY가 누락되지 않는다.

시스템 요약 의미의 SSOT는 `summaryYn`이며 정렬 순번과 사용자 Prefix 선택에서 분리된다. 같은 일자 `normalizeSortOrder`·조회 Spec은 **시스템 요약 맨 앞 → 일반 → DREAM 맨 뒤**다. 일반 챕터 수정 화면의 `#`(=`sortOrder`)는 정규화 후 요약 다음 구간에 반영된다 — 요약이 있는 날 `#1`로 저장하면 요약 바로 다음(전체 순번 2)이 된다. 일반 챕터의 사용자 말머리는 챕터 유형별 PERSONAL Scope와 `prefix_content` 연결을 사용한다.

---

### 등록/수정 후 엔트리 위치 스크롤

**트리거**: `JournalEntryRegistModal` submit 성공 (등록 및 수정 공통)

**동작**:
1. 저장 성공 응답에서 엔트리 ID를 확인한다. 수정이면 기존 `model.id`를 fallback으로 사용한다.
2. 모달을 닫은 뒤 성공 알림을 표시한다.
3. 성공 알림 OK 이후 월간/주간 화면에서는 현재 route 기준으로 `fetchDays()`를 호출해 목록 DOM을 갱신한다.
4. 스레드 상세가 열려 있으면 route와 무관하게 열린 스레드의 상세 본문·집계 태그·소속 엔트리를 다시 조회하고 모달 내부 읽기 맥락을 유지한다. 배경이 주간·월간·일간이면 목록도 재조회하되 배경 스크롤은 하지 않는다.
5. 검색 팝업(`JournalEntrySearchPage`)에서는 `prepare-success` 이벤트에서 현재 검색 조건으로 결과 목록을 재조회(`loadEntries()`)해 준비한다. 서버 검색 쿼리가 소속(결과 포함 여부)의 단일 진실 원천이므로, 수정으로 검색 조건 태그가 빠진 엔트리는 결과에서 제거되고 조건을 새로 충족한 엔트리는 편입된다.
6. 월간/주간 기본 목록은 재조회 후 강제 스크롤하지 않고 현재 스크롤 위치를 유지한다. 검색 팝업은 `prepare-success`에서 결과를 재조회한 뒤, 성공 알림 OK 이후 저장 위치 스크롤만 수행한다.
7. 월간/주간 기본 목록은 기존 DOM을 유지해 브라우저 스크롤 위치 보존에 맡긴다.
8. 일자 상세 팝업은 팝업 내부의 동일 엔트리 위치로 스크롤한다.
9. 검색 팝업은 `success` 이벤트 payload를 받아 `#journal-entry-search-{id}`로 스크롤한다.

**월간/주간 재조회 렌더 계약**: 기존 `dayList`가 있는 상태에서 `fetchDays()`가 다시 실행될 때는 본문을 전체 로딩 스피너로 교체하지 않는다. 기존 목록 DOM을 유지해야 문서 높이가 순간적으로 줄어들지 않고, 저장 후 스크롤 위치가 브라우저에 의해 `0`으로 클램프되지 않는다.

**엔트리 저장 메시지 i18n**: 등록·수정 확인과 성공·실패 fallback은 현재 locale의 클라이언트 카탈로그를 사용하고, 저장 API 응답에 `message`가 있으면 서버 메시지를 우선 표시한다. 검색 팝업의 `prepare-success` 작업은 성공 알림 전에 완료하고, 알림 확인 후 기존 `success` 이벤트 또는 상세·목록 재조회와 위치 복귀를 실행한다.

**엔트리 Prefix 선택·저장**: 등록·수정 모달은 선택한 챕터의 논리 유형에 따라 `JOURNAL_DIARY / JOURNAL_DREAM / JOURNAL_NOTE` 개인 Prefix 활성 목록을 조회하고 nullable 단일 `prefixId`를 저장 payload에 포함한다. 서버는 저장된 엔트리와 소속 챕터를 다시 조회해 소유권과 Scope를 확정한 뒤 `PrefixContentService.applySelection`으로 연결을 교체한다. NOTE 엔트리 연결은 `ref_content_type=JOURNAL_DIARY`를 사용하고, 허용 목록 검증은 NOTE 챕터에서 파생한 `JOURNAL_NOTE` Scope를 사용한다. 엔트리 삭제 시 같은 attachable 연결을 제거한다. 조회 DTO는 `prefix`, `prefixId`, 논리 `prefixContentType`을 함께 내려 수정 모달이 실제 저장 타입을 Scope로 오인하지 않게 한다.

**엔트리 Prefix 선택지 캐시**: 프론트의 콘텐츠 타입별 공통 Prefix 옵션 store는 DIARY/DREAM/NOTE 선택지를 서로 분리해 한 번 로드한 정상 빈 목록도 캐시하고, 실패한 목록은 다음 진입에서 재시도한다. 내 설정의 등록·수정·활성 변경 성공 시 해당 `contentType`만 무효화하고 다음 엔트리 모달 진입에서 서버 확정 목록을 다시 조회한다. 로그아웃·사용자 전환 시 세대를 증가시켜 진행 중이던 이전 사용자 응답을 폐기한다. 등록·수정 폼은 정상 빈 목록에서 말머리 필드를 숨기고 제목 입력을 확장한다. 조회 DTO의 비활성 과거 선택은 disabled 옵션으로 표시해 그대로 유지하거나 해제할 수 있고, 이 경우 활성 선택지가 없어도 필드를 표시한다. 신규 비활성 선택은 서버가 거부한다.

**태그클라우드 갱신 범위 계약**: 저장 후 태그클라우드는 변경된 데이터 범위만 다시 조회한다. 저널 일자 저장은 `day`, `JOURNAL_DIARY` 엔트리 저장은 `diary`, `JOURNAL_DREAM` 엔트리 저장은 `dream` 섹션만 갱신한다. `JOURNAL_NOTE` 엔트리 저장과 저널 챕터 저장은 일자/일기/꿈 태그클라우드의 직접 변경이 아니므로 태그클라우드를 재조회하지 않는다.

**엔트리 id SSOT**: `JournalEntryItem.vue` `:id="'journal-entry-' + entry.id"`, 검색 팝업은 `JournalEntrySearchPage.vue` 결과 article `:id="'journal-entry-search-' + entry.id"`.

---

### 태그 컨텍스트 메뉴와 엔트리 태그 검색 새 창

**목표**: 태그 클릭 시 바로 검색을 실행하지 않고, legacy처럼 컨텍스트 메뉴를 먼저 표시한다.

**적용 대상**:
- 저널 월간/주간 태그클라우드 헤더: `JournalTagCloudHeader.vue`
- 일자 카드의 일자 태그: `JournalDayCard.vue`
- 일기/꿈 엔트리 태그: `JournalEntryItem.vue`
- 결산 상세 태그클라우드·DIARY/DREAM 엔트리 태그: `JournalAnnualDetail.vue` (`JournalAnnualLayout`에 메뉴·짝 모달 마운트)

**컨텍스트 메뉴**: `JournalTagContextMenu.vue`
- 버튼: `검색`, `태그 설정`
- 위치: 클릭 좌표 기준, viewport 안쪽으로 clamp
- 닫기: 외부 클릭, ESC, scroll/resize

**검색 액션**:
- `JOURNAL_DAY`: `journalModalStore.openDayFilterModal({ type: 'tag', id: tagId, name, ctgr })` 으로 일자 필터 모달(메타+태그 통합) 오픈. 모달이 실제로 뜨려면 현재 화면에 `JournalDayMetaModal`이 마운트돼 있어야 한다(결산: `JournalAnnualLayout`).
- `JOURNAL_DIARY`: 새 창 `/vue-app/journal/entry/search?type=DIARY&tagIds={tagId}`
- `JOURNAL_DREAM`: 새 창 `/vue-app/journal/entry/search?type=DREAM&tagIds={tagId}`
- 단, 현재 route가 `journal-entry-search`인 검색 팝업 내부에서는 새 창을 다시 열지 않고 같은 창의 query를 `router.replace(...)`로 갱신한다. 검색 페이지는 `route.fullPath` watch로 즉시 재조회한다.
- 검색 팝업 외부에서 새 창을 열기 전에는 `assertAuthenticatedBeforePopup(router, route)` 로 현재 세션을 확인한다. 세션이 풀렸으면 새 창을 열지 않고 현재 화면에서 로그인 복귀 안내를 표시한다.

**태그 설정 액션**:
- `GET /api/tags/{tagId}/profile?contentType=...` 로 기존 프로필 조회
- `attachableStore.openTagProfile(...)` 로 태그 프로필 모달 오픈
- 모달의 `forceMax`는 저장 후 sized 태그클라우드 크기를 `ts-9`로 고정한다. 엔트리 본문 태그줄에는 적용하지 않는다.
- 저장된 `JOURNAL_DREAM` 태그 프로필 본문은 목록/검색/상세의 꿈 엔트리 본문 아래에 표시된다. 일기(`JOURNAL_DIARY`) 태그 프로필은 설정 모달과 태그 색상 의미에만 사용하고, 엔트리 본문 아래에는 표시하지 않는다.
- 저장·삭제 성공 알림 확인 후 화면 갱신: 월간/주간/일간 등에서는 `refreshJournalDaysForRoute` + contentType 대응 `fetchTagCloud`(`JOURNAL_DAY`→day, `JOURNAL_DIARY`→diary, `JOURNAL_DREAM`→dream). 결산 상세(`annual-detail`)에서는 `fetchTagRows(yy, activeSection)`로 결산 태그클라우드를 재조회한다. 검색 팝업(`journal-entry-search`)에서는 일자/클라우드 재조회 없이 `registerJournalEntrySearchHost`로 등록된 `loadEntries()`를 `refreshJournalEntryHostForRoute`가 호출한다(태그 프로필 `success` 경로의 `loadEntries`도 유지). 스레드 상세가 열려 있으면 route와 무관하게 열린 상세와 소속 엔트리를 먼저 재조회하고, 검색·결산·일자 배경도 각 기존 경로로 갱신한다.

**중요 보존 규칙**:
- 일기/꿈 태그 검색은 현재 목록의 `diaryKeyword` / `dreamKeyword` 필터로 대체하지 않는다.
- 검색은 URL 기반 새 창으로 열어야 하며, 검색 조건은 주소로 재현 가능해야 한다.
- 새 창 검색은 앱 내 탐색 화면이 아니므로 기본 메뉴와 저널 aside를 붙이지 않는다. route는 `SystemLayout.vue` 하위 auth route로 둔다.
- 검색 팝업 내부 태그 클릭도 동일 컨텍스트 메뉴(`JournalTagContextMenu`)를 사용하며, `태그 설정`은 팝업 안에서 `JournalTagProfileModal`을 열 수 있어야 한다.

---

### 일자 카드 ⋯ 컨텍스트 메뉴 (JournalDayCard Context Menu)

**구현 파일**: `app/frontend-vue/src/features/journal/day/components/JournalDayCard.vue`

**레거시 출처**: `legacy/static/vue/feature/journal/day/components/JournalDayContextMenu.ts`

**메뉴 구조**:
- 주간 뷰로 이동 → `router.push({ name: "journal-weekly", query: { stdrdDt: day.stdrdDt } })` (월간·캘린더·메타 등 전용; route `journal-weekly` 에서는 `v-if` 로 메뉴 미표시)
- 새 창으로 열기 (일자 뷰) → `window.open(BASE_URL + /journal/daily-popup?stdrdDt=..., "_blank", "width=...,height=...")` — features 지정으로 탭 아닌 새 창 강제
- JournalDayDaily.vue 로드·route.query.stdrdDt watch: buildDailyFetchParams(stdrdDt) → fetchDays({ viewType: DAILY, stdrdDt, yy, mnth }) (@/utils/journalDayRefresh.ts)
- 일간 팝업(`journal-daily`)과 일간 탭(`journal-daily-tab`)에서 저장·삭제·상태 변경·이력 복원 후 목록 갱신: refreshJournalDaysForRoute(store, route, fallbackStdrdDt?) — 주간/월간과 동일하게 route name 분기, 일간은 URL·fallback 기준일 유지
- 날짜 이동(이전/다음)은 로컬 Date 생성자로 파싱 — `new Date(stdrdDt)`는 UTC로 파싱되어 Korea(UTC+9) 환경에서 날짜가 1일 밀리는 버그 발생
- 중앙 날짜 표시: `<input type=date>` 클릭 시 브라우저 달력 피커 오픈, 선택 날짜로 `router.replace`하여 이동
- 이전/다음 버튼·날짜 선택 tooltip·빈 상태와 목록 조회 실패 fallback은 현재 locale 카탈로그를 사용한다. locale 변경은 `stdrdDt` query와 일간 조회 조건을 변경하지 않는다.
- 팝업 너비 1600px / 높이 1080px (`window.screen.availWidth/Height` 상한); left/top 제거하여 OS 기본 배치 사용
- 수정 → `openReg()` (등록 모달 재활용, id 포함)
- 상태 서브메뉴: 중요(IMPRTC, 표시 전용), 일기 완결(`diaryResolvedYn`, `POST /api/journal/day/{id}/resolved`), 꿈 완결(`dreamResolvedYn`, 동일 API), 접힘(COLLAPSED, `POST /api/states` 토글)
  - 삭제 → `DELETE /api/journal/day/{id}`. `diaryResolvedYn` 또는 `dreamResolvedYn` 이 Y 이면 클라이언트·서버 모두 거절(`journal.day.resolved-delete-locked`). 성공 시 OK 이후 `refreshJournalDaysForRoute` (일간 포함 route 분기)

### 저널 일자 축별 완결·쓰기 잠금 (`diaryResolvedYn` / `dreamResolvedYn`)

**의미**: `journal_day` 의 수동 플래그 두 개. 엔트리 `RESOLVED`·챕터 완료 집계와 무관하다. 일기 축(DIARY/NOTE 챕터·엔트리·해석·댓글·관련·lifecycle·state)과 꿈 축(꿈 등록·엔트리·해석·댓글·관련·lifecycle·state)은 직교한다.

**표시**: 일자 카드에서는 우측 액션 칸(`col-3`, `align-items-center`)에 등록 버튼과 같은 슬롯으로 `일기 완결`/`꿈 완결` 배지를 둔다(쓰기 가능 시 버튼, 완결 시 배지; 일기=`badge-light-success`, 꿈=`badge-light-info`). 상세 모달은 날짜 헤더 인라인. 등록 모달 스위치와 ⋯ Status 토글로 설정·해제.

**쓰기 잠금(축별)**: 해당 축 Y 이면 구조·본문·해석·댓글·관련·lifecycle·state 쓰기 UI 숨김 + `JournalDayResolvedGuard` 서버 거절. 허용: 읽기·복사·TXT·클라이언트 접힘·일자 접힘(COLLAPSED)·일자 수정 모달(날씨·태그·메타·완결 플래그)·태그 프로필·이력 조회(복원·삭제는 잠금). 일자 삭제는 한쪽이라도 Y 이면 불가.

**provide 없는 화면(검색·뷰 모달 등)**: `JournalDayCard` 의 `provideJournalDayResolved` 가 없을 때는 엔트리 DTO 투영 필드 `diaryResolvedYn`/`dreamResolvedYn` 이 SSOT 이다. `JournalEntryItem` 은 `mergeDayResolvedAxis(parent, entry)` 로 병합해 하위 해석에 provide 하고, `JournalEntryViewModal` 은 동일 플래그로 수정 버튼을 숨긴다.

**API**: 저장은 일자 등록/수정 FormData; 빠른 토글은 `POST /api/journal/day/{id}/resolved?diaryResolvedYn=|dreamResolvedYn=`.

**드롭다운**: Metronic `data-kt-menu-trigger="click"`, `data-kt-menu-placement="bottom-end"`

**액션 메시지 i18n**: 일자 삭제 확인·성공·실패와 꿈 섹션 클립보드 복사 성공·실패 fallback은 현재 locale의 클라이언트 카탈로그를 사용한다. 삭제 API 응답에 `message`가 있으면 서버 메시지를 우선 표시하며, 성공 알림 확인 후 `refreshJournalDaysForRoute()`로 현재 route 기준 목록을 갱신하고 해당 일자 카드로 스크롤한다.

---

### 엔트리 ⋯ 컨텍스트 메뉴 (JournalEntryItem Context Menu)

**구현 파일**: `app/frontend-vue/src/features/journal/entry/components/JournalEntryItem.vue`

**레거시 출처**: `legacy/static/vue/feature/journal/entry/components/JournalEntryContextMenu.ts`

**우측 액션 영역 구조**:
- 댓글 등록 버튼 (⋯ 밖, 단독 버튼) → `attachableStore.openCommentRegist(id, contentType)`
- 복사 버튼 (`bi-copy`, ⋯ 밖, 단독 버튼) → `copyEntry()` — 날짜(요일)·`htmlToPlainText(content)` 클립보드 복사. `content` = TinyMCE HTML 원문; 브라우저 HTML 파서로 이름·10진수·16진수 엔티티를 화면과 동일하게 디코딩하고 HTML을 제거한 평문. (레거시 `copy()` 동일 포맷)
- ⋯ 드롭다운:
  - 헤더: contentLabel (일기/꿈)
  - 수정 → `contentType === JOURNAL_REFLECTION` 이면 `openReflectionRegist({ id })`, 그 외 `openEntryModify(id)`
  - 이력 → `attachableStore.openHistory(contentType, id)`
  - 관련 글 추가 (지정 꿈꾼 이름 없을 때만 — `hasDreamerName(entry)` false) → `attachableStore.openRelated(contentType, id)`
  - 구분선
  - 라이프사이클 서브메뉴 (OPEN/PENDING/RESOLVED radio) → `PUT /api/lifecycles { id, contentType, lifecycleKey, cacheContext }`
  - 상태 서브메뉴 toggle → `POST /api/states { id, contentType, stateKey, cacheContext }`
    - IMPRTC(중요), REFRNC(참조) — 공통
    - NHTMR(악몽), HALLUC(환각/현시) — 꿈 전용 (`isDream`)
    - COLLAPSED(접기) — 공통
  - 구분선
  - 삭제 → `DELETE /api/journal/entry/{id}` → 성공 알림 OK 이후 현재 route 기준 목록 재조회 + `#journal-day-{stdrdDt}` 스크롤

**상태/라이프사이클 변경 후**: `fetchDays().then(() => nextTick(() => scrollIntoView(#journal-day-{stdrdDt})))` — 목록 갱신 + 해당 일자로 스크롤. 라이프사이클의 `OPEN`은 기본 상태라 `lifecycle` row와 보조 캐시 맵 항목을 제거하고, `PENDING`·`RESOLVED`만 명시적으로 저장한다. 조회 DTO와 필터는 row/맵 항목 부재를 `OPEN`으로 해석한다. 일기·꿈·Reflection의 월간/주간 보조 맵은 Ehcache namespace에 등록되며, 일기·꿈 lifecycle 변경은 실제 연간 엔트리 목록 캐시 `journalEntryYyAnnualStatedListByUser`를 무효화한다.

**상태·라이프사이클 소유권 계약**: `POST /api/states`와 `PUT /api/lifecycles`는 저장 전에 요청 `contentType + id`의 원본 저널 콘텐츠가 존재하고 현재 로그인 사용자가 작성자인지 검증한다. 일자·챕터는 각 원본 행, 일기·노트·꿈은 원본 엔트리, 해석은 해석 원본, 스레드는 스레드 원본의 `created_by`를 사용한다. 요청 콘텐츠 타입과 실제 엔트리의 챕터 유형도 일치해야 한다. 원본이 없거나 다른 사용자 소유이면 대상 존재 여부를 구분해 노출하지 않고 403 권한 오류로 거부하며 state/lifecycle 행과 캐시를 변경하지 않는다. `MNGR` 역할도 다른 사용자의 개인 저널을 대신 변경하지 않는다.

**관련 관계 표시·해제**: 일반 관련글은 엔트리 아래에서 `RelatedContentDto.relationType/reason/targetId/targetContentType/targetTitle`별 행을 표시하고 제목 클릭으로 원문을 열며 ×로 해제한다. (변경 후) FLOW 본문 요약 행·「흐름 보기」·전체 흐름 모달·백엔드 `flowSummary` 는 모두 제거됐다(나-2·다-2). 흐름은 스레드 소속으로 수렴.

**스레드 소속 후보 메뉴**: 「스레드에 추가」 hover 서브메뉴는 일반 스레드 목록 상태와 분리된 `journalThreadMembership` store를 사용한다. 일기·꿈·노트를 target으로 둔 `JOURNAL_REFLECTION`에는 서브메뉴를 렌더하지 않으며, API도 동일 조건을 거절한다. 후보의 `member`가 소속 토글 판정의 SSOT이며, 엔트리 본문의 `threadList`는 소속 칩 표시를 담당한다. 제목 검색·분류 입력은 메뉴를 닫지 않고 후보 API만 갱신한다. 말머리 선택지는 콘텐츠 타입별 개인 목록 계약에 따라 `GET /api/my/prefixes/options?contentType=JOURNAL_THREAD`로 조회한다. 후보 조회 실패와 분류 선택지 조회 실패를 정상 빈 결과로 가장하지 않으며, 분류 조회 실패는 다음 메뉴 진입에서 재시도한다.

**관련 글 대상 검색**: 대상 유형 select의 `JOURNAL_DIARY|JOURNAL_DREAM`을 `DIARY|DREAM`으로 변환해 `GET /api/journal/entries?type=...&searchKeywords=...&pageSize=8&sort=DESC`를 호출한다. 키워드별 제목 OR 본문, 복수 키워드 사이는 AND다. API 실패는 별도 인라인 오류를 표시하고 정상 0건 문구를 함께 표시하지 않는다.

**액션 메시지 i18n**: 클립보드 복사 성공·실패, 라이프사이클·상태 변경 실패, 관련글 열기·연결·해제, 삭제 확인·성공·실패 fallback은 현재 locale의 클라이언트 카탈로그를 사용한다. API 응답에 `message`가 있으면 서버 메시지를 우선 표시하며, 상태 변경·관계 해제·삭제 성공 후 기존 현재 route 재조회와 `#journal-day-{stdrdDt}` 스크롤 순서를 유지한다. 검색 팝업에서는 재조회 중인 `journalStore.loading` 완료를 감지해 검색 결과를 다시 조회한다.

**lifecycle 자동 접힘**: `isCollapsed` computed에서 `localCollapsedOverride`가 없고 `lifecycleKey`가 `PENDING` 또는 `RESOLVED`이면 `true`를 반환한다. 상태 서버 저장 없이 클라이언트에서 자동 접힘 처리한다.

**꿈 RESOLVED 표시**: `JOURNAL_DREAM`의 완료 상태는 일기·노트·해석의 초록과 구분해 보라색을 사용한다. 펼친 행은 은은한 보라 배경·테두리·좌측선, 자동 접힌 행은 보라 접힘 표시와 순번, 라이프사이클 메뉴는 보라 완료 라벨을 사용한다. 중요·참조를 함께 지정하면 완료 보라선과 중요 빨강·참조 노랑선을 모두 유지한다. 이 표시 차이는 lifecycle 저장 payload와 RESOLVED 자동 접힘 우선순위를 변경하지 않는다.

**PENDING 표시·접힘**: 일기·꿈·노트의 보류 상태는 약한 회색 배경·테두리·좌측선과 회색 배지로 표시하고 자동으로 접는다. 중요·참조 상태가 함께 있으면 빨강·노랑 상태 표현이 우선한다. 하위 엔트리가 1개 이상이고 모두 `PENDING`인 챕터는 같은 회색 상태를 표시하고 자동으로 접는다. 스레드 상세는 lifecycle 자동 접힘을 억제해 `PENDING`과 `RESOLVED` 본문을 펼친 상태로 표시한다.

**검색 팝업의 엔트리 액션**: `JournalEntrySearchPage.vue`는 `JournalEntryRegistModal`을 직접 마운트하고, 모달의 `prepare-success` 이벤트에서 현재 검색 목록 또는 수정 대상 article DOM을 성공 알림 전에 준비한다. 모달의 `success` 이벤트는 성공 알림 OK 이후 저장한 엔트리 article 스크롤만 담당한다. 삭제는 `DELETE /api/journal/entry/{id}` 후 검색 목록에서 해당 항목을 제거한다. 검색 결과 내부의 리플렉션 수정 액션은 `JournalReflectionRegistModal`을 같은 페이지에 직접 마운트해 열며, 수정 모드는 `GET /api/journal/reflection/{id}` 상세 조회가 성공한 뒤 제목/본문을 채운 폼을 표시한다. 리플렉션 제목은 선택값이므로 제목이 비어 있어도 저장 확인 후 등록/수정을 진행한다. 저장 후 `refreshJournalEntryHostForRoute` / 검색 호스트 재조회로 목록을 갱신한다.

**Reflection 등록**: Reflection 은 대상 필수(About-A)라 대상 엔트리(`JournalEntryItem`) ⋯ 메뉴의 「해석 등록」에서만 등록한다 → `openReflectionRegist({ refId, refContentType, journalDayId, journalChapterId, stdrdDt })`. 챕터 헤더의 독립(대상 없는) 등록 진입점은 없다(Standalone 폐기). 저장은 전용 API `POST /api/journal/reflections` 로 `refId`/`refContentType` 을 싣고, 성공 응답의 `rsltMap.targetReflectionList`/`rsltMap.targetLifecycleKey`로 dayList 내 target entry의 reflectionList·lifecycle을 in-place 교체한다(fetchDays 전체 재호출 없음). enrichment 응답이 없으면 fallback으로 호스트 재조회(`refreshJournalEntryHostForRoute`)를 수행한다.

**Reflection 태그**: Reflection 은 태그를 두지 않는다(모달에 태그 UI 없음, 서버 쓰기 DTO 에 tag 필드 없음). 저장 `content_type`은 `JOURNAL_REFLECTION`이고 `ref_content_type`은 대상 타입이다. 결산·엔트리 태그클라우드·챕터 접힘 요약의 DIARY 집계는 `JOURNAL_DIARY` 단일 축을 사용한다. 원문 뷰(`JournalEntryViewModal`)의 「수정」은 `JournalReflectionRegistModal`로 연다(태그 UI 없음). 일기용 `JournalEntryRegistModal`로 보내지 않는다. 백엔드 `JournalCacheEvictWorker`는 Reflection 저장 후 대상 일자·챕터·라이프사이클 캐시를 무효화하며 태그 캐시는 유지한다. 엔트리 삭제 후처리의 관련글 정리(`RelatedContentService.deleteAllByRef(key, createdBy)`)는 관련글 지원 타입(일기·꿈)만 수행하고, Reflection 등 미지원 타입은 no-op 한다. Reflection 은 스레드 소속 대상이 아니다. 라이프사이클·상태: Reflection은 OPEN/PENDING/RESOLVED와 COLLAPSED/IMPRTC/REFRNC를 허용한다. Reflection은 대상 엔트리 아래 임베드로만 표시되며 접힘/펼침 토글과 일자 aside 기본 접힘 모드를 따른다. primary(일기·꿈·노트) `RESOLVED` 시 딸린 Reflection도 `RESOLVED`로 맞추고, `RESOLVED` primary에 Reflection 신규 등록 시 primary를 `OPEN`(+`COLLAPSED` 해제)으로 재개한다.

**임베드 Reflection 액션**: `JournalReflectionItem` 우측은 댓글·복사·⋯. ⋯에서 수정·이력·관련글·라이프사이클·중요/참조·삭제. Reflection→Reflection 중첩 등록 메뉴는 숨기고, 신규 등록은 primary 엔트리의 「해석 등록」 경로를 사용한다. Reflection에는 「스레드에 추가」를 두지 않는다. 접기는 미제공. 대상 엔트리 접힘 시 임베드는 `v-if`로 언마운트되고, 재펼침 시 `JournalEntryItem`이 `reinitMetronicAfterDom()`으로 ⋯ KTMenu를 재바인딩한다. 삭제는 `DELETE /api/journal/reflection/{id}` + `journal.reflection.delete.confirm`.

**Reflection 등록 기본 라이프사이클**: 등록 시 서버(`postRegist`)가 즉시 lifecycle을 `PENDING`으로 설정한다. 프론트에서 PENDING은 자동 접힘이므로 리플렉션은 기본 접힌 상태로 시작한다. 사용자가 수동으로 OPEN 또는 RESOLVED로 변경할 수 있다.

**Reflection 접힘 전파**: 부모 엔트리(`JournalEntryItem`)가 펼쳐지면 하위 리플렉션도 같이 펼쳐진다. `JournalEntryItem`은 `reflectionForceSignal` computed(`"expand"|"collapse"|null`)를 리플렉션에 `forceCollapsedSignal` prop으로 전달한다. 사용자가 엔트리를 직접 펼침(`localCollapsedOverride=false`) → `"expand"`, 직접 접음(`true`) → `"collapse"`, 아무도 안 건드림(`null`) → `null`(리플렉션 자체 lifecycle·모드 기본 분기 따름). `JournalReflectionItem`의 prop은 `boolean`이 아니라 `string|null`(`"expand"|"collapse"|null`)이다 — Vue 3 Boolean casting이 absent prop을 `false`로 만드는 문제를 우회하기 위함. `forceCollapsedSignal` 변경 시 `localCollapsedOverride`를 null로 초기화해 이전 자체 토글 상태를 리셋한다. 접힘 우선순위: 로컬 토글 > signal > lifecycle(RESOLVED/PENDING) 자동 > **일자 aside 리플렉션 기본 접힘 모드** > 서버 COLLAPSED. 모드 ON이면 로컬·signal·lifecycle 자동 접힘에 해당하지 않을 때 접힘으로 시작한다. 모드 OFF이면 서버 `COLLAPSED`만 기본 분기로 쓴다(기존 계약). `JournalDayLayout`이 `provideJournalReflectionDefaultCollapsed`로 모드를 하위에 전달하며, 검색·스레드 등 provide 없는 표면은 항상 OFF 계약이다. 토글 전환 시 마운트된 리플렉션의 `localCollapsedOverride`를 null로 리셋한다. 토글 상태는 `journal` store `reflectionDefaultCollapsed` + localStorage `journal_reflection_default_collapsed`이며 조회 필터·필터 초기화 대상이 아니다.

**대상 삭제 Block(Reference→Block)**: 일기·꿈·노트를 가리키는 Reflection 이 있으면 그 대상 엔트리 삭제(`DELETE /api/journal/entry/{id}`)는 `journal.entry.delete.blocked-by-reflection` 으로 거부된다. Reflection 을 가리키는 하위 Reflection 이 있으면 부모 Reflection 삭제는 `journal.reflection.delete.blocked-by-child` 으로 거부된다. 챕터 내 엔트리에 참조 Reflection 이 있으면 챕터 삭제(`DELETE /api/journal/chapter/{id}`)는 `journal.chapter.delete.blocked-by-reflection` 으로 거부된다(Hibernate cascade 가 엔트리 서비스 `preDelete` 를 우회하므로 챕터 `preDelete` 에서 막는다). nullify·orphan 화는 하지 않으며, 사용자는 참조 Reflection 을 먼저 삭제한다. 명시 cascade(대상+Reflection 동시 삭제)는 미구현이다. 정본 `docs/migration/journal/reflection-domain-model.md` §5.

---

### 엔트리 클라이언트 접힘 토글 (Entry Local Collapse Toggle)

**구현 파일**: `app/frontend-vue/src/features/journal/entry/components/JournalEntryItem.vue`

**레거시 출처**: `JournalEntryItem.ts` 왼쪽 열 토글 버튼 + `journalEntryStateService.toggle()` (localStorage 기반, 서버 상태 무변경)

**동작**: 서버의 COLLAPSED 상태와 별개로 클라이언트에서 임시 펼치기/접기.

**구현**:
```typescript
// props.forceCollapsed: 챕터 토글이 전파하는 강제 접힘 여부 (null=챕터 미개입)
const localCollapsedOverride = ref<boolean | null>(null);
const isCollapsed = computed(() => {
  if (localCollapsedOverride.value !== null) return localCollapsedOverride.value;
  if (props.forceCollapsed !== null && props.forceCollapsed !== undefined) return props.forceCollapsed;
  if (isResolved.value) return true;  // RESOLVED 자동 접힘
  return hasState("COLLAPSED");
});
function toggleEntry(): void {
  localCollapsedOverride.value = !isCollapsed.value;
}
```

**접힘 우선순위**: 엔트리 자체 토글 > 챕터 강제(`forceCollapsed`) > PENDING/RESOLVED 자동 접힘 > 서버 COLLAPSED 상태

**토글 버튼 위치**: 왼쪽 열 (`#sortOrder` 아래) — `bi-arrows-expand` (접힘) / `bi-arrows-collapse` (펼침)

---

### 챕터 토글 → 하위 엔트리 전파 접힘/펼침 (Chapter Collapse Propagation)

**구현 파일**: `JournalChapterItem.vue` → `JournalEntryItem.vue`

**동작**: 챕터 우측 접힘 토글 버튼 클릭 시, 챕터 `localCollapsedOverride`가 하위 엔트리 전체에 `forceCollapsed` prop으로 전달된다.
- 챕터 펼침(`false`) → 엔트리 전체 펼침 (RESOLVED 자동 접힘도 override)
- 챕터 접힘(`true`) → 엔트리 전체 접힘
- 챕터 override 없음(`null`) → 엔트리 각자의 접힘 로직으로 복귀
- 접힌 상태에서는 숨겨진 엔트리 내부의 스레드 소속이 사라지지 않도록 `chapter.journalEntryList[].threadList`를 최초 등장 순서로 집계하고, 같은 `threadId`는 하나의 스레드 버튼으로 표시한다. 버튼은 접힘 태그와 같은 `.journal-chapter-content` 바깥 요약 영역에 있으며 기존 엔트리 스레드 칩과 같이 현재 저널 화면 위에 전역 상세 모달을 직접 연다.

**주요 동기**: RESOLVED 챕터는 모든 엔트리가 RESOLVED → 챕터·엔트리 모두 자동 접힘. 챕터를 펼치면 엔트리도 함께 펼쳐져야 하나, 엔트리는 각자 `isResolved → true`를 유지하므로 챕터 펼침만으로는 엔트리가 열리지 않던 문제를 해결.

**스마트 토글 (첫 클릭 보정)**: 챕터는 `allEntriesResolved`(전부 RESOLVED)일 때만 자동 접히므로, **일부만** RESOLVED면 챕터는 펼쳐진 채 일부 엔트리만 접힌 상태가 된다. 이때 `localCollapsedOverride`가 아직 `null`이라 "챕터가 펼쳐져 있다"는 신호가 엔트리에 전달되지 않는다. 변경 전에는 이 상태에서 첫 클릭이 챕터를 접어버려 엔트리를 펼치려면 2클릭이 필요했다.

변경 후 `toggleChapter()`는 다음 조건을 모두 만족하면 접기 대신 `override=false`로 **하위 엔트리를 전체 펼친다**(1클릭):
- `localCollapsedOverride === null` (사용자가 아직 토글하지 않음)
- `!isCollapsed` (챕터가 펼쳐져 있음)
- `hasDataCollapsedEntry` (데이터 규칙으로 접힌 엔트리가 있음)

`hasDataCollapsedEntry`는 엔트리의 `lifecycleKey`가 `PENDING`/`RESOLVED`이거나 서버 `COLLAPSED` 상태일 때 참이다. 엔트리 개별 로컬 토글은 각 엔트리가 소유해 챕터에서 볼 수 없으므로 감지하지 못하는 근사치다.

이후 클릭부터는 `override`가 non-null 이라 기존 토글(`!isCollapsed`)이 동작한다. **트레이드오프**: 이 상태에서 챕터를 접으려면 전체 펼침 후 한 번 더 눌러야 한다(접기 2클릭).

**구현**:
```vue
<!-- JournalChapterItem.vue -->
<JournalEntryItem :force-collapsed="localCollapsedOverride" ... />
```
```js
function toggleChapter(): void {
  if (localCollapsedOverride.value === null && !isCollapsed.value && hasDataCollapsedEntry.value) {
    localCollapsedOverride.value = false;
    return;
  }
  localCollapsedOverride.value = !isCollapsed.value;
}
```

---

### 챕터 복사 버튼 (Chapter Copy)

**구현 파일**: `app/frontend-vue/src/features/journal/chapter/components/JournalChapterItem.vue`

**트리거**: 챕터 헤더 우측 복사 버튼 (`bi-copy`) 클릭

**복사 포맷**:
```
날짜 (요일) / 요약 또는 Prefix명 / 챕터 제목   ← 있는 것만 " / " 구분
#정렬번호
엔트리 본문 (HTML 태그 제거)
                                         ← 빈 줄 구분 (엔트리 반복)
```

요일은 `getWeekDayStr(stdrdDt, t)` 로 계산하고 현재 locale의 공용 요일 카탈로그를 사용한다 (`journalDate.ts`). 한국어는 기존 한자 표기를 유지한다.

클립보드 쓰기 성공·실패 알림은 현재 locale의 클라이언트 카탈로그를 사용한다.

**구현**:
```typescript
async function copyChapter(): Promise<void> {
  const lines: string[] = [];
  const headerParts: string[] = [];
  if (props.chapter.stdrdDt) {
    const weekDay = getWeekDayStr(props.chapter.stdrdDt);
    headerParts.push(weekDay ? `${props.chapter.stdrdDt} (${weekDay})` : props.chapter.stdrdDt);
  }
  if (isSummaryChapter.value) headerParts.push(t("journal.chapter.summary"));
  else if (props.chapter.prefix?.name) headerParts.push(props.chapter.prefix.name);
  if (props.chapter.title) headerParts.push(props.chapter.title);
  if (headerParts.length > 0) lines.push(headerParts.join(" / "));
  for (const entry of entryList.value) {
    const sortNum = entry.sortOrder != null ? "#" + String(entry.sortOrder) : "";
    /* content = TinyMCE HTML 원문; markdownContent = MarkdownUtils 처리 후 HTML */
    const raw = htmlToPlainText(entry.content ?? entry.markdownContent ?? "");
    if (sortNum) lines.push(sortNum);
    if (raw) lines.push(raw);
    lines.push("");
  }
  await navigator.clipboard.writeText(lines.join("\n").trim());
}
```

---

### 엔트리 복사 버튼 (Entry Copy)

**구현 파일**: `app/frontend-vue/src/features/journal/entry/components/JournalEntryItem.vue`

**트리거**: 우측 액션 영역 복사 버튼 (`bi-copy`) 클릭 (댓글 버튼과 ⋯ 사이)

**복사 포맷**:
```
날짜 (요일)
본문 평문 (HTML 태그 제거)
```

**구현**: 공통 `htmlToPlainText(content ?? markdownContent)`. `content` = TinyMCE HTML 원문 (마크다운 재처리 이전). 브라우저 HTML 파서로 이름·10진수·16진수 엔티티를 화면과 동일하게 디코딩하고 HTML 제거 후 평문으로 복사 → 텍스트에디터에 그대로 재붙여넣기 가능. `#sortOrder` 없음 — 레거시 `copy()` 동일.

---

### 헤더 검색 드롭다운 (Header Search Dropdown)

**구현 파일**: `app/frontend-vue/src/app/layouts/default/components/search/Search.vue`

**참고**: 이 파일은 `.gitignore` 경로(`/app/frontend-vue/src/app/layouts/default/components/search/`)에 포함되어 git 추적 대상이 아님.

**UI 구조**:
- 일기/꿈 유형 버튼 (`btn-primary` / `btn-info`)
- 검색어 input (debounce 400ms 후 API 호출)
- 결과 목록: 날짜 배지 + 80자 content snippet + 검색 페이지 링크 버튼
- "전체 결과 보기" RouterLink → `journal-entry-search` route

**검색 API**: `GET /api/journal/entries?type=DIARY|DREAM&sort=asc|desc&searchKeywords=...&tagIds=...`
- 응답: `AjaxResponse.rsltList`
- `type=DIARY`: 결과 행 = 일기(Primary)만. Reflection 은 별도 Aggregate(journal_reflection)이고 대상 필수(About-A)라 검색 결과 행이 되지 않는다. 대상 일기를 가리키는 Reflection 본문에 키워드가 있으면 대상 일기가 매칭된다(원문·해석 한 몸, `JournalEntrySpec#targetReflectionKeywordSubquery` 가 journal_reflection 을 EXISTS 로 조회). 태그·state 검색과 태그 클릭 팝업은 `JOURNAL_DIARY` 단일 축을 사용한다. 태그클라우드·결산·챕터 요약도 동일한 DIARY 단일 태그 축을 사용한다.

**결과 클릭**: `RouterLink :to="{ name: 'journal-entry-search', query: { type, searchKeywords } }"`
- 검색 팝업에서 직접 수정/삭제하지 않고 검색 페이지(`JournalEntrySearchPage`)로 이동
- `JournalEntryRegistModal`은 `JournalLayout` 하위에만 마운트되므로 헤더 드롭다운에서는 모달 직접 열기 불가

---

### 검색 팝업 전체 기능 (`JournalEntrySearchPage.vue`)

레거시 `journal_entry_search_module.ts` 의 멀티키워드·멀티태그 AND 검색을 Vue SPA 로 재현.

**컨트롤 바 (1행)**: 고급 필터 토글 | 초기화 | 정렬 토글(asc/desc) | 검색 || 전체 복사 | TXT 내보내기 | 키워드 배지들 | 태그 배지들 | 조건 요약(유형/정렬/키워드 수/태그 수) | 결과 상태 라벨 | 결과 건수

**고급 필터 아코디언**: 유형 토글(일기/꿈) + 키워드 입력 + 추가 버튼 + 태그 입력 + 추가 버튼. 키워드/태그 입력은 Enter 로 조건 추가가 가능함을 현재 locale 힌트와 title로 안내한다. `v-show` 로 토글.

**멀티키워드 AND 검색**: 키워드는 `searchKeywords[]` URL 파라미터 배열로 관리. 배지 X 클릭 → 해당 키워드 제거 후 URL replace → 자동 재조회. 키워드 배지는 제거 tooltip을 제공한다.

**키워드 하이라이트**: 검색 팝업은 URL `searchKeywords[]`를 `JournalEntryItem.highlightKeywords`로 전달해 엔트리 본문 `markdownContent`의 일치 텍스트를 표시한다. 하이라이트는 검색 화면 전용 표시 보조이며 검색 조건, 복사/TXT 내보내기 본문, 월간/주간/챕터 화면 렌더를 변경하지 않는다.

**멀티태그 AND 검색**: 태그는 `tagIds[]` URL 파라미터 배열로 관리. 배지 X 클릭 → 해당 태그 제거. 태그 배지는 제거 tooltip을 제공한다. 팝업 고급 필터에서 태그를 직접 입력할 때는 현재 `type`의 엔트리 태그 categoryMap과 태그 목록을 조회해 자동완성 후보를 제공하고, 태그명+카테고리로 특정 태그 ID를 확정한 뒤 `tagIds[]`에 추가한다. 같은 이름에 여러 카테고리가 있으면 카테고리 선택 버튼을 먼저 표시하고, 선택 또는 취소 전까지 태그 입력과 추가 버튼을 잠가 카테고리 선택 대기 상태를 명확히 한다.

**검색 전/빈 결과/실패 상태**: `searchKeywords[]`와 `tagIds[]`가 모두 비어 있으면 `type`만으로 `GET /api/journal/entries`를 호출하지 않고 검색 전 안내를 표시한다. 검색 전 안내에는 고급 필터를 열고 키워드 입력으로 포커스를 이동하는 조건 추가 CTA를 제공한다. 검색 결과가 0건이면 고급 필터를 열고 키워드 입력으로 포커스를 이동하는 조건 수정 CTA를 제공한다. 초기화는 조건·결과·오류 상태를 비우고 검색 전 상태로 돌아간다. 검색 실패 시 기존 결과 배열과 결과 건수를 비우지 않고 inline 오류 안내를 표시해 실제 0건과 조회 실패를 구분한다.

**실행 전 입력 확정**: 검색, 전체 복사, TXT 내보내기는 키워드/태그 입력칸에 남아 있는 값을 먼저 URL 검색 조건으로 확정한 뒤 실행한다. 태그명이 여러 카테고리에 걸쳐 있으면 카테고리 선택이 완료될 때까지 검색·복사·내보내기 실행을 보류한다. 이미 추가된 키워드/태그를 다시 입력하면 조건을 조용히 무시하지 않고 locale 메시지로 중복 상태를 안내한다.

**route 동기화 입력 초기화**: route query가 로컬 검색 조건으로 동기화될 때 확정 전 키워드/태그 입력과 태그 카테고리 선택 대기 상태를 비운다. URL 검색 조건(`searchKeywords[]`, `tagIds[]`)과 표시용 태그명 캐시는 유지한다.

**액션 잠금**: 검색 조회 중이거나 복사/TXT 내보내기 실행 중이면 검색·정렬·초기화·복사·TXT 내보내기 버튼을 비활성화해 중복 실행을 막는다. 복사 버튼은 현재 결과가 있거나 입력칸에 확정 전 조건이 남아 있을 때만 활성화한다. TXT 내보내기 버튼은 URL 검색 조건이 있거나 입력칸에 확정 전 조건이 남아 있을 때만 활성화한다.

**조건 변경 피드백**: 키워드/태그 배지 제거, 정렬 변경, 유형 변경은 URL query를 갱신하면서 결과 상태 라벨에 변경 사유를 표시한다. 새 조회가 진행 중이고 직전 결과가 남아 있으면 결과 목록을 비우지 않고 유지하며, 목록 위와 결과 상태 라벨에 갱신 중임을 표시한다. 조회 성공 후에는 변경 사유 라벨을 지우고 현재 조건 기준 라벨로 돌아간다.

**수정 후 소속 재판정**: 검색 팝업 내부에서 엔트리를 저장(등록·수정)하면 부분 교체가 아니라 현재 검색 조건으로 결과를 재조회(`loadEntries()`)한다. 소속(결과 포함 여부)의 단일 진실 원천은 서버 검색 쿼리이며, 수정으로 검색 필터 태그가 빠진 엔트리는 결과 목록에서 제거되고 조건을 새로 충족하게 된 엔트리는 편입된다. 저장 위치 스크롤은 재조회 이후 `success` 이벤트에서 수행한다.

**결과 읽기 보조**: 날짜 헤더는 해당 날짜에 묶인 현재 검색 결과 건수를 locale 라벨로 함께 표시한다. 건수는 현재 `entries` 배열 기준으로 계산하며, 검색 조건·정렬·locale 변경 외의 별도 상태를 만들지 않는다.

**결과 요약 바**: 결과 목록 위에는 현재 `entries` 배열 기준 총 건수, 고유 기준일 수, 고유 연월 수를 locale 라벨로 표시한다. 검색 전/빈 결과 상태에서는 별도 요약 바를 표시하지 않는다.

**확정 전 입력 안내**: 키워드/태그 입력칸에 값이 남아 있으면 `hasPendingSearchInputs` 기준으로 고급 필터 영역에 검색 실행 시 현재 입력값도 조건에 포함된다는 locale 안내를 표시한다. 안내는 URL query, 검색 실행 순서, 입력값 확정 규칙을 변경하지 않는다.

**태그 컨텍스트 메뉴에서 태그 추가** (`JournalTagContextMenu.vue`):
- 팝업 외부: 새 창 열기 (`tagIds` 파라미터)
- 팝업 내부 (`route.name === "journal-entry-search"`): 기존 `tagIds` 배열에 APPEND (중복 무시) → `router.replace()`

**TXT 내보내기**: `GET /api/journal/entries/export?type=...&sort=...&tagIds=...&searchKeywords=...`

**전체 복사 포맷**: 레거시 `JournalEntrySearch.copy()` 동일 — 날짜가 바뀔 때만 `날짜(요일)` 헤더, `#순번\n본문`, 엔트리 간 빈 줄, `\r\n` 줄바꿈. 요일은 현재 locale의 공용 요일 카탈로그를 사용한다. 확정 전 입력값을 URL 검색 조건으로 반영하고 재조회한 뒤 복사한 경우 성공 알림은 조건 반영 후 복사임을 구분한다.

**액션 메시지 i18n**: 검색 결과 조회 실패, 태그 선택·검색 조건 검증, 검색 전 안내와 조건 추가 CTA, 빈 결과 조건 수정 CTA, 조건 요약과 결과 상태 라벨, 결과 요약, 날짜별 결과 건수, 입력 힌트, 확정 전 입력 안내, 배지 제거 tooltip, 카테고리 선택 대기 안내, 중복 조건 안내, 복사 대상 없음과 복사 성공/실패 알림은 현재 locale의 클라이언트 카탈로그를 사용한다. locale 변경은 URL query·검색·복사 실행과 클립보드/TXT 본문 포맷을 변경하지 않는다.

---

### 연간 결산 CRUD 메시지와 재조회

`journalAnnual.ts`의 결산 목록 조회 실패, 전체 결산 갱신·결산/리뷰 등록·수정 결과 fallback과 리뷰 삭제 확인·결과 fallback은 현재 locale의 카탈로그를 사용한다. API 응답에 `message`가 있으면 서버 메시지를 우선 표시한다. 성공 알림 후 전체 결산 갱신은 목록·총계를, 결산 저장은 목록을, 리뷰 저장·삭제는 해당 연도 상세를 기존 순서대로 재조회한다.

---

### 일정 휴가 등록·수정과 기간 검증

- 일정 저장 모달에서 대분류 `VCATN`을 선택하면 `VCATN_CD` 기반 「휴가 구분」 select를 표시하고 필수로 검증한다. `VCATN`이 아닌 유형으로 바꾸면 폼과 서버 저장값의 휴가 구분을 모두 비운다.
- 시작일·종료일은 inclusive 날짜다. 종료일 미입력은 시작일로 정규화하고, 종료일이 시작일보다 빠르면 확인창과 API 호출 전에 경고한다. 서버도 같은 계약을 다시 검증하여 직접 요청 우회를 막는다.
- `HOLYDAY` 선택 시 종료일 입력을 숨기고 시작일과 같은 날로 저장한다. 휴가는 `HOLYDAY`로 변환하지 않으며 다일 범위를 허용한다.
- 휴가 목록 행·달력 이벤트 클릭은 상세 모달을 열며, 상세에는 휴가 구분 코드명을 표시한다. 상세에서 기존 수정·삭제 흐름으로 진입할 수 있다.
- 기존 `VCATN` 데이터의 `vcatn_cd`가 NULL이면 상세에는 `-`를 표시하고, 수정 저장 전 사용자가 휴가 구분을 선택해야 한다. 제목 기반 자동 추정이나 클라이언트 fallback은 두지 않는다.
- 달력·목록 조회는 공개 일정과 현재 사용자 참가 개인 일정을 단일 쿼리로 처리한다. 개인 일정 필터를 끄거나 생략하면 공개 일정만, 켜면 공개 일정 + 참가 개인 일정을 반환한다. 「내 일정만」은 현재 사용자 참가 조건을 추가하고 「휴가」 해제는 `VCATN`을 제외한다.
- 공개 일정은 인증 사용자가 상세를 볼 수 있고 개인 일정은 작성자 또는 참가자만 볼 수 있다. 참가자는 조회만 가능하며 수정·삭제는 작성자만 가능하다. 상세 모달의 수정·삭제도 `isCreatedBy`가 거짓이면 비활성화하고, 직접 API 요청은 서버가 같은 권한으로 거절한다.
- 저널 일자 조회는 현재 사용자 참가 `VCATN` 일정을 별도 휴가 축으로 투영한다. `isHolyday`는 전역 공휴일·주말 의미를 유지하고, 휴가는 `vacationDayStatus`와 `vacationReasonList`로 전달한다. `FULL_DAY`는 날짜를 빨갛게 표시하고, `AM_HALF`·`PM_HALF`는 날짜 색을 유지한 채 각각 오전·오후 반차 배지로 구분한다. 같은 날짜의 오전+오후 반차는 서버가 `FULL_DAY`로 병합한다. NULL·정책 미등록 코드는 `UNKNOWN` 경고 배지로 표시하며 전일로 추정하지 않는다. 월간·주간·일간 카드, 일자 상세, 메타 일자 목록은 동일 표시 컴포넌트로 일정 제목 사유를 함께 노출한다.

## 검색 팝업 전용 동작

### AI 챗 숨김 (`App.vue`)

팝업 라우트(`journal-entry-search`, `journal-daily`)에서는 `AppChat`을 렌더하지 않는다.

```typescript
// App.vue
const isPopup = computed(() => ["journal-entry-search", "journal-daily"].includes(String(route.name)));
// template: <AppChat v-if="authStore.isAuthenticated && !isPopup" />
```

### AI 챗 세션 제목 편집 (`AppChat.vue`, `chat.ts`)

세션 칩 제목을 더블클릭하면 인라인 input으로 전환된다. Enter/블러 시 `PATCH /chat/sessions/{id}` `{ title }`로 저장하고, Esc는 취소한다. 비어 있는 제목은 저장하지 않고 `chat.session.rename.empty`를 표시한다. 수동 제목은 서버 `DEFAULT_TITLE`(`새 대화`) 자동 축약 대상에서 빼지며, 클라이언트 `bumpActiveSession`도 `새 대화`/`New chat`일 때만 자동 제목을 동기화한다.

### AI 챗 빈 세션 시드 질문 (`AppChat.vue`)

메시지가 없을 때 empty state에 `chat.empty.prompt`와 함께 `chat.empty.seed.1`..`4` 칩을 표시한다. 칩 클릭은 composer에 넣지 않고 `chat.sendMessage(seedText)`로 즉시 전송한다. 응답 대기 중(`isWaitingResponse`) 시드는 비활성화된다. 시드 문구는 카탈로그 고정값이며 개인화/개인 기록 기반 추천이 아니다.

### AI 챗 assistant 마크다운 렌더 (`AppChat.vue`)

assistant 메시지는 서버가 저장한 평문 `content`(마크다운 기호 유지)를 `MarkdownUtils.renderChatMarkdown`으로 HTML화한 `markdownContent`를 버블에 `v-html`로 표시한다. USER·기타 역할은 평문 `content`만 표시한다. `markdownContent`가 없거나 `-`인 구 메시지는 content를 escape한 뒤 `<p>`/`<br>`로 폴백한다.

### AI 챗 응답 스트리밍 (`AppChat.vue`, `chat.ts`)

`ChatOrchestrator` 본경로(LLM) 생성은 `OllamaClient.chatStream`으로 NDJSON을 읽으며 같은 `/topic/chat/session/{id}`에 `rsltObj.type=DELTA` (`delta` 청크)를 브로드캐스트한다. 클라이언트는 `streamingContent`에 누적해 임시 assistant 버블(평문)로 보이고, 메시지 목록에는 넣지 않는다. 완성 ASSISTANT 메시지·취소·세션 전환 시 `streamingContent`를 비운다. hybrid/retry·language-guard 재시도는 비스트림이며 DELTA를 보내지 않는다. 스트림 중 버블은 마크다운 HTML이 아니라 평문이며, 완성 메시지만 `markdownContent` `v-html`을 쓰는다.

### AI 챗 응답 대기 단계 (`AppChat.vue`, `chat.ts`)

`ChatOrchestrator.processChat`은 USER 메시지 broadcast 이후 같은 `/topic/chat/session/{id}`로 `rsltObj.type=PROGRESS` 이벤트를 보낸다 (`phase=SEARCHING` → RAG, `phase=GENERATING` → LLM/하이브리드). 클라이언트는 메시지 목록에 넣지 않고 `responsePhase`만 갱신하며, typing 인디케이터 옆에 `chat.waiting.searching` / `chat.waiting.generating`을 표시한다. ASSISTANT 메시지 수신·취소·세션 전환·연결 오류 시 phase를 비운다.

### AI 챗 RAG 근거 → 원문 열기 (`AppChat.vue`)

assistant 메시지 `metadataJson.ragSources` 행을 클릭하면 `useJournalModalStore().openEntryView(journalEntryId)`로 읽기 전용 엔트리 모달(`JournalEntryViewModal`)을 연다. 본문은 목록과 동일하게 `markdownContent` HTML(`journal-content`)을 표시한다. footer **편집**은 `openEntryModifyFromView`로 전환하되, `JOURNAL_REFLECTION`이면 `JournalReflectionRegistModal`, 그 외는 `JournalEntryRegistModal`을 연다.

- 모달 마운트: 비팝업 인증 라우트에서는 `App.vue`가 `JournalEntryViewModal`과 `JournalEntryRegistModal`을 전역 마운트한다 (`JournalDayLayout`에서는 등록/수정 모달 중복 마운트하지 않음).
- 팝업 라우트(`journal-entry-search`, `journal-daily`)는 각자 레이아웃/페이지에 등록/수정 모달을 유지하고, `AppChat` 자체가 숨겨지므로 채팅 출처 딥링크 경로가 없다.
- 채팅 드로어 z-index(6002)보다 모달이 위에 오도록 `App.vue`가 `body.modal-open`일 때 `.modal` / `.modal-backdrop` z-index를 6100 / 6090으로 올린다. 모달 내부 저장·삭제 확인 SweetAlert2는 `shared/utils/overlayZIndex.ts`의 `SWAL_Z`(6200)가 SSOT다 — `App.vue`가 `.swal2-container`에 `6200 !important`를 주고, `swalFire` `didOpen`이 컨테이너 inline z-index도 같은 값으로 강제한다. 모든 모달을 6100으로 평탄화하면 중첩 모달(예: 스레드 상세에서 연 수정·댓글·이력 모달)이 DOM 순서에 따라 부모 뒤로 깔린다. 이를 막기 위해 `shared/utils/modalStack.ts`의 `installModalStacking()`이 `show.bs.modal`에서 이미 열린 모달 수에 따라 z-index를 `6100 + n*2`로 올리고(해당 backdrop은 `z-1`) 자식 모달을 부모 위로 스태킹하되, `MODAL_MAX_Z`(`SWAL_Z - 20`)로 캡해 확인창이 모달에 가려지지 않게 한다.
- 출처 목록은 기본 5건 미리보기이며, 숨은 건수가 있으면 `chat.rag.source.more`로 전체 펼친다.

### 팝업 직접 진입 세션 만료 처리 (`router/index.ts`, `sessionExpired.ts`)

팝업 전용 보호 라우트(`journal-entry-search`, `journal-daily`)는 인증이 없을 때 로그인 화면을 팝업 내부에 렌더하지 않는다. 라우터 가드는 `confirmSessionExpired(to.name)`을 호출해 레거시처럼 창 닫기 확인 alert를 표시하고, 확인 시 `window.close()`를 호출한 뒤 현재 route 이동은 `next(false)`로 중단한다.

### 401 세션 만료 처리 (`main.ts`)

팝업 라우트에서 401 응답 시 로그인 화면 이동 대신 창 닫기 확인 다이얼로그를 표시한다. 일반 보호 라우트에서 라우터 가드가 미인증을 감지한 경우도 같은 `confirmSessionExpired` alert를 거친 뒤, 확인 시에만 `/sign-in?sessionExpired=Y&redirect=...`로 이동한다.

저널 등록·수정·삭제·상태 변경 요청이 401 이외의 HTTP 오류로 실패하면 `swalRequestError()`가 오류를 기록하고 응답 JSON의 `message`를 우선 표시한다. 서버 메시지가 없을 때만 `요청 처리 중 오류가 발생했습니다.`를 표시하며, 401의 `AuthExpiredError`는 전역 안내와 중복되지 않도록 별도 alert를 띄우지 않는다.

검색·목록·메타 조회 실패는 실제 빈 결과와 구분한다. 직전 성공 데이터와 결과 건수를 보존하면서 오류를 기록·표시하고, 일자/엔트리/스레드의 상세·수정용 조회가 실패하면 불완전한 모델로 모달을 열지 않는다. 일자 수정·상세, 꿈 등록, 엔트리 수정 정보 조회 실패는 서버 메시지를 우선 표시하고 없으면 현재 locale의 fallback을 사용한다.

저널 스레드 등록·수정 모달의 확인창과 저장 결과 fallback은 현재 locale의 클라이언트 카탈로그를 사용한다. 등록·수정 성공 또는 실패 응답에 서버 `message`가 있으면 서버 메시지를 우선 표시하며, 수정·상세 조회 실패 시 모달을 닫고 현재 locale의 조회 실패 안내를 표시한다. 삭제 확인·성공·실패 fallback도 현재 locale을 사용하고 서버 `message`를 우선 표시하며, 성공 알림 확인 후 첫 페이지 목록을 다시 조회한다.

**저널 스레드 상세 닫기 정책**: 문맥형 `JournalThreadDetailModal`은 `backdrop: "static"`, `keyboard: false`로 생성해 모달 바깥 클릭과 Escape 입력으로 닫히지 않는다. 헤더 ×와 푸터 「닫기」는 `store.closeDetail()`을 호출하는 명시적 종료 경로다. 독립 `JournalThreadDetailPage`는 모달 닫기 계약을 쓰지 않고 목록 이동 버튼으로 `/thread`에 복귀한다.

**저널 스레드 상세 진입 정책**: 인증된 SPA는 문맥형 `JournalThreadDetailModal`과 `JournalThreadRegistModal`을 `App.vue`에 각각 단일 마운트한다. 저널 엔트리 스레드 칩·접힌 챕터 스레드 요약·기간별 스레드 요약은 route 이동 없이 `JournalThreadStore.openDetail(threadId)`를 호출해 현재 주간·월간·일간·검색 화면과 스크롤 문맥을 보존한다. 스레드 목록 행과 외부 딥링크는 `/thread/:id`의 `JournalThreadDetailPage`로 이동해 스레드 자체를 주 문맥으로 표시한다. 두 표면은 `detailModel`·`detailEntries` SSOT를 공유하고 `detailSurface=modal|page`로 렌더 표면만 구분한다. 상세 전환 중 늦게 도착한 이전 상세·엔트리 응답은 요청 토큰과 현재 ID를 검사해 폐기한다.

**저널 스레드 상세 정렬 정책**: 소속 엔트리는 일자 오름차순으로 표시하고 같은 일자에서는 챕터 `sortOrder` → 원본 엔트리 `sortOrder` 순으로 정렬해 저널 일자 화면과 동일한 챕터별 그룹 순서를 유지한다. 엔트리 `sortOrder`는 챕터별로 1부터 매겨지므로 챕터 순서를 함께 보지 않으면 같은 일자의 여러 챕터 #1들이 앞으로 몰린다. 챕터·엔트리 순서가 없거나 같을 때만 엔트리 ID 오름차순을 tiebreak로 사용한다. 스레드 소속의 nullable `journal_thread_entry.sort_order`는 별도 수동 순서가 아니므로 표시 정렬에 사용하지 않는다.

**저널 스레드 자체 수정 정책**: 문맥형 상세 모달의 헤더 「수정」은 route·저널 배경·스크롤과 상세 데이터를 유지한 채 `detailSurface`만 보류하고 같은 앱의 수정 모달(`registSurface=modal`)로 전환한다. 취소는 보류한 ID와 현재 상세 ID가 같을 때만 원래 상세 모달을 복원한다. 저장 성공도 상세 모달을 복원한 뒤 `refreshJournalEntryHostForRoute`로 상세와 주간·월간·일간 배경을 갱신하며 배경 스크롤은 하지 않는다. 목록·외부 딥링크의 독립 상세에서 「수정」을 누르면 같은 탭의 `/thread/{id}/edit` 독립 편집 페이지(`registSurface=page`)로 이동하고 저장·취소 뒤 `/thread/{id}` 독립 상세로 복귀한다. 두 표면은 같은 `registModel`과 `JournalThreadEditorForm`을 사용한다. 변경 전의 이름 있는 브라우저 팝업·`window.opener.postMessage`·팝업 query 계약은 제거했다.

**저널 스레드 Prefix 선택 정책**: Prefix는 목록 검색과 등록·수정이 함께 쓰는 단일 말머리 데이터다. `JournalThreadLayout`이 `ensurePrefixOptions()`로 콘텐츠 타입별 공통 Prefix 옵션 store를 통해 `GET /api/my/prefixes/options`의 활성 Prefix를 조회하고 검색 카드·공용 편집 폼·스레드 소속 후보 메뉴가 같은 `JOURNAL_THREAD` 목록을 공유한다. 이 API와 저장 검증은 로그인 사용자의 `(PERSONAL, user_id, JOURNAL_THREAD)` Scope를 기준으로 하며 `created_by`를 소유권으로 재해석하지 않는다. 동시 조회는 한 요청으로 합치고 실패는 다음 route 동기화에서 재시도한다. 내 설정의 등록·수정·활성 변경 성공 시 `JOURNAL_THREAD` 캐시를 즉시 무효화하고, 다음 스레드 화면·후보 메뉴 진입이 서버 확정 목록을 다시 조회한다(변경 전: 관리 store만 재조회해 소비 store의 세션 캐시가 이전 값을 유지). 목록 select는 단일 `prefixId` 필터를 즉시 적용한다. 편집은 nullable `registModel.prefixId`를 전송하며 빠른 추가는 `/api/my/prefixes`를 재사용해 생성 후 옵션을 강제 재조회하고 새 Prefix를 즉시 선택한다. 전체 관리는 `/my/prefixes` 탭으로 이동한다. 조회 DTO의 `prefix`는 이름·색·활성 상태를 포함해 비활성 과거 선택도 목록·상세에 남기며 신규 비활성 선택은 서버가 거부한다.

**저널 스레드 독립 수정 이탈 정책**: 스토어는 수정 모델을 연 시점의 제목·본문·분류를 snapshot으로 보존하고 현재 값과 비교해 `registDirty`를 계산한다. `/thread/{id}/edit`에서 브라우저 뒤로가기·사이드 메뉴처럼 편집 버튼을 우회해 이동할 때 dirty이면 현재 locale의 미저장 변경 폐기 확인창을 표시한다. 취소하면 route와 폼을 유지하고, 확인하면 편집 상태를 닫은 뒤 원래 이동을 계속한다. 저장 성공과 페이지의 안전 취소 버튼은 편집 상태를 먼저 닫으므로 이 확인을 중복 표시하지 않는다. 수정 조회 응답이 비었거나 요청 ID와 다른 ID를 반환하면 불완전한 폼을 열지 않고 조회 실패로 닫아 목록으로 복귀한다.

**저널 스레드 상세 엔트리 액션 계약**: 소속 엔트리는 스레드용 읽기 전용 복제본이 아니라 원본 `JournalEntryDto`이므로 저널 일자와 같은 수정·댓글·해석·이력·관련글·스레드 소속·라이프사이클·상태·삭제 액션을 유지한다. 현재 화면 레이아웃이 해당 액션의 자식 모달을 마운트하고, 전역 마운트된 엔트리 수정·원문 모달은 중복 마운트하지 않는다. 액션 성공 후에는 모달·페이지 공용 `detailOpen`을 전경 판단 기준으로 활성 스레드 상세의 본문·집계 태그·소속 엔트리를 함께 재조회한다. `detailSurface=modal`이고 배경이 주간·월간·일간이면 배경 목록도 재조회하지만 상세 축을 반환해 배경 스크롤은 하지 않고, 검색 팝업 배경이면 등록된 `loadEntries`로 로컬 결과(스레드 칩 포함)를 함께 재조회한다. 독립 페이지는 같은 상세 SSOT만 갱신한다. 현재 스레드 소속 해제·엔트리 삭제는 재조회 결과에서 카드를 제거하고, 수정·관계·라이프사이클·상태·태그 변경은 같은 카드의 최신 DTO로 교체한다. 재조회 실패는 기존 상세를 보존한 채 오류 로그와 안내로 드러낸다.

**저널 스레드 상세 복사·다운로드**: 상세 모달·독립 페이지 모두 「복사」·「다운로드」를 제공한다. 복사는 클라이언트에서 스레드 제목 + 소속 엔트리를 검색 전체 복사와 같은 평문 포맷(`날짜(요일)`·`#순번`·본문)으로 클립보드에 쓴다. 다운로드는 서버 `GET /api/journal/threads/{id}/export`가 `=== dreamdiary export ===` 배너 + `thread: 제목` + 선택 시 `prefix: 말머리` + 소속 엔트리 텍스트를 `thread_{id}_@yyyyMMdd.txt` 첨부로 내려준다(챕터/엔트리 내보내기와 동일 계약, 소유권은 `getEntriesByThread`가 검증). 두 액션은 표시 데이터를 바꾸지 않고 `detailModel`·`detailEntries` SSOT만 읽으며, 로직은 `journalThreadExport.ts` util을 공유한다.

**저널 스레드 연관 뷰 합성 및 스레드 피커 정책**: 서로 관련된 스레드를 1-hop 대칭 연관으로 묶고(`POST/DELETE /api/related/JOURNAL_THREAD/{id}`), 스레드 상세의 연관 스레드 행별 합성 토글(`relatedThreadIds`)로 선택한 연관 스레드의 엔트리만 같은 시간축에 겹쳐 본다(행 기본 OFF, 임시 화면 옵션). 연관 스레드 추가는 `JournalThreadPickerModal`을 통해 대상 스레드를 키워드로 검색·선택하며, 자기 자신 및 이미 연관된 스레드는 목록에서 선택 불가(비활성화) 처리한다. 빌려온 연관 엔트리(`sourceThreadId` 존재)는 출처 스레드 제목 칩으로 구분하고 설계 §2-6에 따라 「스레드에서 빼기」 멤버십 제거 메뉴를 숨긴다(`filteredThreadOptions` / `toggleThread` 가드).

**저널 스레드 상세 lifecycle 표시·접힘 정책**: 소속 엔트리는 `JournalThreadEntryService.getEntriesByThread`가 `JournalEntryStateEnricher.enrichLifecycleMixed`로 lifecycle을 병합해 내려준다. 완료(`RESOLVED`) 엔트리의 `#순번`은 저널 일자와 같은 초록(꿈은 보라), 보류(`PENDING`) 엔트리는 같은 회색 상태 표현을 사용한다. 스레드 상세는 `JournalEntryItem`에 `disableLifecycleCollapse`를 넘겨 lifecycle 자동 접힘을 억제하고 본문을 펼친 상태로 표시한다. 수동 접기와 서버 `COLLAPSED`는 유지하며 state는 병합하지 않는다.

**저널 스레드 목록 검색 카드**: 화면 진입 시 목록과 함께 콘텐츠 타입별 공통 캐시의 `GET /api/my/prefixes/options` 말머리 선택지를 조회한다. 내 설정 변경으로 `JOURNAL_THREAD`가 무효화된 상태면 이전 선택지를 재사용하지 않고 최신 목록을 받는다. 말머리·제목 검색과 초기화는 모두 `fetchList(0)`으로 첫 페이지부터 조회한다. 제목은 `searchType=title`·`searchKeyword`, 말머리는 단일 `prefixId`를 전송한다. 등록·수정·삭제 성공 후 목록을 갱신하고, 보조 데이터 조회 실패는 빈 상태로 가장하지 않고 오류 로그와 현재 locale 안내를 표시한다. 목록 API(`GET /api/journal/threads`) 실패는 `journal.thread.list.load.failure`를 테이블에 표시하며 `journal.thread.empty`(정상 0건)와 구분한다. 등록은 `JournalThreadViewToolbar`에 두며 ASIDE는 없다. 목록 행에는 Prefix 배지 하나, 소속 엔트리 태그 합집합(`thread.tag.list`), 활성 소속 수와 기간을 표시한다. 이 값들은 `JournalThreadService.getPageDto` enrich 가 소속 집계 쿼리로 채우며 엔트리 풀 DTO 로드는 하지 않는다. 멀티 태그 필터는 기존 `tagIds` AND 계약을 유지한다.

**저널 스레드 목록 행 액션**: 관리 열은 수정·삭제 개별 버튼 대신 ⋯ 컨텍스트 메뉴를 제공한다. 수정은 기존 수정 route로 이동하고 삭제는 기존 확인·삭제 store 액션을 유지한다. **변경 후**: 저널 일자·게시판 목록과 동일하게 Metronic `data-kt-menu`(+목록 렌더 후 `reinitMetronicAfterDom`)를 쓴다. KTMenu 는 `document.body` 위임 클릭으로 열리므로 트리거에 `@click.stop`을 두면 메뉴가 열리지 않는다 — 행 상세 이동은 `isMetronicMenuEventTarget` 가드로 막는다. Bootstrap `strategy:fixed` 땜빵은 제거했다.

저널 할일 등록·수정 모달의 제목 필수 검증·확인·결과 fallback은 현재 locale의 클라이언트 카탈로그를 사용한다. 저장 API의 서버 `message`가 있으면 우선 표시하고, 성공 시 모달을 닫은 뒤 성공 알림 확인 후 `refreshJournalDaysForRoute()`로 현재 route의 저널 목록을 갱신한다.

저널 해석 등록·수정 모달의 확인·결과 fallback은 현재 locale의 클라이언트 카탈로그를 사용한다. 해석 제목은 선택값으로 유지하고, 저장 API의 서버 `message`가 있으면 우선 표시하며, 성공 시 모달을 닫은 뒤 성공 알림 확인 후 `refreshJournalEntryHostForRoute()`로 현재 표시 호스트를 갱신한다. 스레드 상세에서는 열린 상세·집계 태그·소속 엔트리를 재조회하고, 그 밖의 route에서는 기존 저널 목록 갱신을 유지한다.

저널 해석 아이템의 라이프사이클·상태 변경 실패, 클립보드 복사 성공·실패, 삭제 확인·성공·실패 fallback은 현재 locale의 클라이언트 카탈로그를 사용한다. API 응답의 `message`를 우선 표시한다. 변경·삭제 성공 후 스레드 상세에서는 열린 상세·집계 태그·소속 엔트리를 재조회하고 스크롤하지 않으며, 그 밖의 route에서는 현재 일자를 재조회한 뒤 기존 스크롤 동작을 유지한다.

```typescript
const confirmed = await confirmSessionExpired(route.name);
if (confirmed && !isAuthPopupRoute(route.name)) {
  await router.push(buildSessionExpiredSignInRoute(route.fullPath));
}
```


---

### 저널 설정 (Journal Setting)

**API**: `GET /api/journal/settings` — 전역 저널 설정 조회, `PUT /api/journal/settings` — 전역 저널 설정 갱신 (MNGR 권한).

**설정 항목**:
- `embeddingEnabled` (Boolean) — AI 임베딩 활성화 여부. ON이면 엔트리 등록/수정 시 embedding queue + entity queue에 적재, OFF면 건너뜀.

**동작**: `JournalEntryService.postRegist`/`postModify`에서 `JournalSettingService.isEmbeddingEnabled()` 확인 후 queue 적재를 조건부 실행. Reflection은 임베딩 대상이 아니라 적용되지 않음. 설정 변경은 즉시 반영(재시작 불필요). 기본값 ON.
