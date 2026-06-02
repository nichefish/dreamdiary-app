# 저널 어사이드 인터랙션 스펙 (Journal Aside Interaction Spec)

> 공통 인터랙션 패턴(AJAX, 모달, Tagify, TinyMCE 등)은 ``common/interaction-spec.md`` 참조.



## Vue SPA 구현 상태 (소스 기준)

| 인터랙션 | `stores/journal.ts` / UI | 상태 |
|----------|---------------------------|------|
| 정렬 토글 | `sortOrder` + `toggleSort` + localStorage 유지 + 프론트 역순 | ✓ |
| 태그 클릭 컨텍스트 메뉴 | `tagContextMenu.ts` + `JournalTagContextMenu.vue` | ✓ |
| 일자 카드 ⋯ 컨텍스트 메뉴 | `JournalDayCard.vue` — Metronic dropdown | ✓ |
| 메타 버튼 드롭다운 | `JournalDayCard.vue` — `bi-bar-chart` 버튼 클릭 시 Bootstrap `dropup` 메뉴; 해당 일자 메타 항목 1개씩 나열; 항목 클릭 → `JournalDayMetaModal` 오픈; `width: max-content`로 내용 폭에 맞게 auto-size | ✓코드 |
| 일자 필터 모달 (메타+태그 다중 AND) | `JournalDayMetaModal.vue` — 메타 또는 태그를 시드로 열림(`openDayFilterModal`); 상단 칩에 선택 메타(파랑)·태그(초록) 혼합 표시; 칩 × 클릭 시 자유 제거(제한 없음); 모든 필터 제거 시 빈 결과 반환(payload.list 전체 노출 방지); AND 필터(모든 선택 메타+태그 보유 날짜만); 행에서 비선택 메타 뱃지 클릭 → 메타 필터 추가, 비선택 태그 클릭 → 태그 필터 추가, 선택된 태그 클릭 → 태그 필터 제거; 각 행의 선택 메타 값은 `selectedMetas` 배열 순서(선택 순)대로 표시하여 행마다 순서 일관성 유지; 연도 변경 시 필터 유지(재조회만), 신규 오픈 시 시드 1개로 초기화; `JournalDayTagDetailModal` 제거하여 단일 모달로 수렴 | ✓코드 |
| 엔트리 ⋯ 컨텍스트 메뉴 | `JournalEntryItem.vue` — lifecycle/status/수정/이력/관련글/삭제 | ✓ |
| 엔트리 클라이언트 접힘 토글 | `JournalEntryItem.vue` — `localCollapsedOverride` ref | ✓ |
| 챕터 복사 버튼 | `JournalChapterItem.vue` — `copyChapter()`, 날짜(요일)·카테고리·엔트리 전체 텍스트 클립보드 복사 | ✓ |
| 꿈 복사 버튼 | `JournalDayCard.vue` — `copyDreams()`, 날짜(요일) 헤더 + 꿈 엔트리 전체 클립보드 복사 | ✓ |
| 엔트리 복사 버튼 | `JournalEntryItem.vue` — `copyEntry()`, 날짜(요일)·본문 텍스트 클립보드 복사 (레거시 동일 포맷) | ✓ |
| 헤더 검색 드롭다운 | `Search.vue` — 일기/꿈 유형 선택 + debounce 검색 + 결과 링크 (`journal-entry-search`) | ✓ |
| 메타 VIEW · 메타 컨텍스트 메뉴 | `metaContextMenu.ts` + `JournalMetaContextMenu.vue` — 헤더 `#메타` 클릭 시 팝업(태그 메뉴와 동일 UI); 「검색」→ `openDayFilterModal`(`JournalDayMetaModal`), 「그래프로 보기」→ `addMetaToGraph`(최대 2·이미 있으면 비활성), 「메타 설정」→ `openMetaProfile`(`JournalMetaProfileModal`, `GET /api/journal/day/metas/{id}`) | ✓코드 |
| 메타 VIEW 비교 그래프 | `JournalDayMeta.vue` — `selectedMetas` 최대 2; 헤더에서 그래프에 포함된 메타는 굵게 표시·옆 × 제거; 연도 「전체」(yy 미전송)·임계값·메타별 통계; **한 ApexCharts**에 시리즈 최대 2개(일자 합집합 X축, 범례, 단위 다르면 Y축·툴팁에서 메타별 단위) | ✓코드 |
| Pinpoint | `JournalAside.vue` — `pinnedYy/pinnedMnth` ref + pinpoint/turnback 함수 | ✓ |
| 챕터 카테고리 필터 | `JournalAside.vue` — `JOURNAL_CHAPTER_DIARY_CTGR_CD`·`NOTE` 병합 체크박스, `store.chapterCtgrCds` → `fetchDays` | ✓ |
| 주간 네비게이터 | `JournalAside.vue` — 요일 버튼 7개, 이전/다음 주 화살표, 주간 범위 라벨 | ✓ |
| 연/월 select | 연도 select + 월 그리드 (`navigateMonth`, `gotoYyMnth`) | ⚠ |
| 툴바 키워드 전체검색 | `JournalDayViewToolbar` 로컬 ref → `openSearchTab()` → 새 탭 `/vue-app/journal/entry/search` | ✓ |
| 어사이드 키워드 필터 | `JournalDayViewToolbar.vue`에만 있음; `JournalAside.vue` 어사이드에는 키워드 입력 없음 | ⚠ 툴바만 |
| 등록/수정 후 확인·스크롤 | 일자/챕터/엔트리 등 submit 성공 → 성공 알림 OK 이후 목록/상세 갱신 → 저장 위치 scrollIntoView. 챕터는 저장된 챕터 DOM(`#journal-chapter-{id}`)을 우선 탐색하고 없으면 일자 카드로 fallback | ✓ |
| 상태/라이프사이클 변경 후 스크롤 | 상태 토글·라이프사이클 설정 서버 반영 후 현재 route 기준 목록 재조회 → `#journal-day-{stdrdDt}` scrollIntoView | ✓ |
| 챕터 일자 변경 | `JournalChapterRegistModal.vue` — 수정 모드+비DREAM 한정, 날짜 picker + 챕터 일자 변경 버튼, `POST /api/journal/chapter/{id}/move` 호출 후 `fetchDays` + 신 일자 scrollIntoView | ✓ |
| 챕터 소유권 표시 | `JournalChapterItem.vue` — API `isCreatedBy`; 타인 작성 시 배지·쓰기 버튼 숨김; 수정/삭제/이동 거부 시 `msg.rslt.not-owner` (403) alert | ✓ |
| 챕터 resolved (파생) | 챕터 자체 resolved 상태 없음. CSS `:has` + `:not(:has([data-resolved=\"N\"]))` 로 하위 엔트리 전체 resolved 여부를 집계해 접힘 하이라이트 표시. DB 마이그레이션: `lifecycle` 테이블 `ref_content_type='JOURNAL_CHAPTER'` RESOLVED 레코드 소프트 삭제 | ✓ |
| TAGCLOUD/DIARIES/DREAMS | `showTagCloud` 등 + 토글 핸들러 | ✓ |

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
1. `pinnedYy.value = store.yy`
2. `pinnedMnth.value = store.mnth`
3. UI 갱신: `#pinnedYy` → 저장된 연도, `#pinnedMnth` → 저장된 월 표시

**트리거 B — 돌아가기** (`<i class="bi bi-reply-all">`):
1. `if (pinnedYy.value && pinnedMnth.value)`
2. `store.gotoYyMnth(pinnedYy.value, pinnedMnth.value)` 호출
3. 목록 재조회 (`store.fetchDays()` 내부 호출됨)

**상태 저장 위치**: `JournalAside.vue` 로컬 ref (Pinia store 불필요 — 사이드바 범위 상태)

**초기 표시**: `pinnedYy === null` → `<span id="pinnedYy">----</span>`, `<span id="pinnedMnth">--</span>`

---

### 챕터 카테고리 필터 (Chapter Category Filter)

**트리거**: CHAPTER CATEGORIES `<select multiple>` 항목 변경

**레거시**: `data-journal-day-action="chapter-ctgr-select"` → `bridge.applySearchParamsAndReload({ chapterCtgrCds: [...] })`

**Vue SPA 구현**:
```typescript
// JournalAside.vue 또는 JournalAsideEntryFilters 컴포넌트 내
function onChapterCtgrChange(event: Event) {
    const select = event.target as HTMLSelectElement;
    const selected = Array.from(select.selectedOptions).map(opt => opt.value);
    if (selected.includes('__ALL__') || selected.length === 0) {
        store.chapterCtgrCds = [];  // 필터 없음 = 전체 표시
    } else {
        store.chapterCtgrCds = selected;
    }
    void store.fetchDays();
}
```

**챕터 필터 활성화 토글** (`#toggleChapterCtgr`):
- `checked=false` → `store.chapterCtgrCds = []` + `store.fetchDays()` (전체 표시)
- `checked=true` → 현재 멀티셀렉트 값 적용

**챕터 옵션 로드 타이밍**: `onMounted` 시 `journalModalStore.prefetchChapterCategories()` 호출 후 일기·노트 코드 병합 (`JOURNAL_CHAPTER_CTGR_CD` 는 DB 마이그레이션으로 제거됨)

**`__ALL__` 처리**: 첫 번째 `<option value="__ALL__">전체</option>` 선택 시 모든 챕터 표시

**store 반영**: `store.chapterCtgrCds: string[]` — `fetchDays` 에서 `chapterCtgrCds.length > 0` 이면 query param 포함

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

---

### 키워드 검색 / 필터 (Keyword Search & Filter)

**툴바 전체검색 (`JournalDayViewToolbar.vue`)**
- 로컬 `ref`(`localDiaryKw` / `localDreamKw`) 사용 — `store.diaryKeyword/dreamKeyword`(필터 상태)와 완전 분리
- 검색 버튼 클릭 / Enter → `openSearchTab(type, keyword)` → `window.open(/vue-app/journal/entry/search?type=...&searchKeywords=..., journal-entry-search-{type}, "width=1960,height=1440,top=0,left=270")` 새 창 (태그 컨텍스트 메뉴와 동일 방식, 같은 타입 재검색 시 창 재사용)
- BASE_URL: `import.meta.env.BASE_URL` (vite config `base: "/vue-app/"`)

**어사이드 현재결과 필터 (`JournalAside.vue`)**
- `v-model="store.diaryKeyword"` / `v-model="store.dreamKeyword"` — store 상태 직결
- funnel 버튼(`@click`) / Enter(`@keyup.enter`) 모두 `store.fetchDays()` 호출
- 현재 목록을 재조회하는 필터이며, 툴바 검색과 동작이 다름

**중요 보존 규칙**:
- 툴바 인풋은 store 상태를 오염시키지 않는다 (`name` 속성 없음, 로컬 ref만 사용)
- 어사이드 인풋은 store 상태를 직접 바인딩하며 필터로만 작동한다

---

### 등록/수정 후 일자/챕터 위치 스크롤

**트리거**: `JournalDayRegistModal` submit 성공 (등록 및 수정 공통)

**동작**:
1. `model.journalDate`를 `savedDate`로 캡처
2. 모달 닫기
3. 성공 알림 표시
4. 사용자가 OK를 누른 뒤 `refreshCurrentDayView(savedDate)` 호출
5. `fetchDays()` 완료(`.then`) → `nextTick` → `document.getElementById('journal-day-{savedDate}')` → `scrollIntoView({ behavior: "smooth", block: "start" })`

**카드 id SSOT**: `JournalDayCard.vue` `:id="'journal-day-' + day.stdrdDt"`

**챕터 id SSOT**: `JournalChapterItem.vue` `:id="'journal-chapter-' + chapter.id"`

**챕터 등록/수정 후 동작**:
1. 저장 성공 응답에서 챕터 ID를 확인한다. 수정이면 기존 `model.id`를 fallback으로 사용한다.
2. 모달을 닫고 성공 알림을 표시한다.
3. 사용자가 OK를 누른 뒤 현재 route 기준으로 월간(`LIST`) 또는 주간(`WEEKLY`) 목록을 다시 조회한다.
4. 목록 렌더 완료 후 `#journal-chapter-{id}`로 스크롤한다.
5. 신규 등록 등 챕터 ID를 확인할 수 없거나 retry 이후에도 챕터 DOM을 찾지 못하면 `#journal-day-{stdrdDt}`로 스크롤한다.

**챕터 삭제/상태 변경 후 동작**:
- 삭제는 삭제 전에 `stdrdDt`를 캡처하고, 성공 알림 OK 이후 현재 route 기준 목록 재조회 + `#journal-day-{stdrdDt}` 스크롤.
- 상태 변경은 현재 route 기준 목록 재조회 + `#journal-day-{stdrdDt}` 스크롤.

---

### 저널 일자 중복 등록 차단
**트리거**: `JournalDayRegistModal` 신규 등록 submit

**동작**:
1. 서버는 로그인 사용자 기준으로 같은 `journalDate`의 활성 저널 일자가 있는지 확인한다.
2. 중복이면 기존 데이터를 수정으로 전환하지 않고 `rslt=false`, `msg.journal.day.duplicate` 메시지로 응답한다.
3. 모달은 저장 성공 처리를 진행하지 않고 서버 메시지를 표시한다.

---

### 등록/수정 후 엔트리 위치 스크롤

**트리거**: `JournalEntryRegistModal` submit 성공 (등록 및 수정 공통)

**동작**:
1. 저장 성공 응답에서 엔트리 ID를 확인한다. 수정이면 기존 `model.id`를 fallback으로 사용한다.
2. 모달을 닫고 성공 알림을 표시한다.
3. 사용자가 OK를 누른 뒤 월간/주간 화면에서는 현재 route 기준으로 `fetchDays()`를 다시 호출한다.
4. 목록 갱신 후 `#journal-entry-{id}`로 스크롤한다. 신규 등록 등 엔트리 ID를 확인할 수 없으면 `#journal-day-{stdrdDt}`로 스크롤한다.
5. 일자 상세 팝업(`JournalDayDtlModal`)이 열려 있으면 OK 이후 상세 데이터를 다시 조회하고, 팝업 내부의 동일 엔트리 위치로 스크롤한다.
6. 검색 팝업(`JournalEntrySearchPage`)에서는 OK 이후 `success` 이벤트를 받아 현재 검색 조건으로 `loadEntries()` 후 `#journal-entry-search-{id}`로 스크롤한다.
7. 월간/주간 화면에서는 엔트리 ID가 있으면 엔트리 DOM을 retry로 우선 탐색하고, retry 이후에도 찾지 못할 때만 해당 일자 카드로 fallback한다.

**엔트리 id SSOT**: `JournalEntryItem.vue` `:id="'journal-entry-' + entry.id"`, 검색 팝업은 `JournalEntrySearchPage.vue` 결과 article `:id="'journal-entry-search-' + entry.id"`.

---

### 태그 컨텍스트 메뉴와 엔트리 태그 검색 새 창

**목표**: 태그 클릭 시 바로 검색을 실행하지 않고, legacy처럼 컨텍스트 메뉴를 먼저 표시한다.

**적용 대상**:
- 저널 월간/주간 태그클라우드 헤더: `JournalTagCloudHeader.vue`
- 일자 카드의 일자 태그: `JournalDayCard.vue`
- 일기/꿈 엔트리 태그: `JournalEntryItem.vue`

**컨텍스트 메뉴**: `JournalTagContextMenu.vue`
- 버튼: `검색`, `태그 설정`
- 위치: 클릭 좌표 기준, viewport 안쪽으로 clamp
- 닫기: 외부 클릭, ESC, scroll/resize

**검색 액션**:
- `JOURNAL_DAY`: `journalModalStore.openDayFilterModal({ type: 'tag', id: tagId, name, ctgr })` 으로 일자 필터 모달(메타+태그 통합) 오픈
- `JOURNAL_DIARY`: 새 창 `/vue-app/journal/entry/search?type=DIARY&tagIds={tagId}&tagName={name}`
- `JOURNAL_DREAM`: 새 창 `/vue-app/journal/entry/search?type=DREAM&tagIds={tagId}&tagName={name}`
- 단, 현재 route가 `journal-entry-search`인 검색 팝업 내부에서는 새 창을 다시 열지 않고 같은 창의 query를 `router.replace(...)`로 갱신한다. 검색 페이지는 `route.fullPath` watch로 즉시 재조회한다.

**태그 설정 액션**:
- `GET /api/tags/{tagId}/profile?contentType=...` 로 기존 프로필 조회
- `attachableStore.openTagProfile(...)` 로 태그 프로필 모달 오픈
- 저장된 `JOURNAL_DREAM` 태그 프로필 본문은 목록/검색/상세의 꿈 엔트리 본문 아래에 표시된다. 일기(`JOURNAL_DIARY`) 태그 프로필은 설정 모달과 태그 색상 의미에만 사용하고, 엔트리 본문 아래에는 표시하지 않는다.

**중요 보존 규칙**:
- 일기/꿈 태그 검색은 현재 목록의 `diaryKeyword` / `dreamKeyword` 필터로 대체하지 않는다.
- 검색은 URL 기반 새 창으로 열어야 하며, 검색 조건은 주소로 재현 가능해야 한다.
- 새 창 검색은 앱 내 탐색 화면이 아니므로 기본 메뉴와 저널 aside를 붙이지 않는다. route는 `SystemLayout.vue` 하위 auth route로 둔다.
- 검색 팝업 내부 태그 클릭도 동일 컨텍스트 메뉴(`JournalTagContextMenu`)를 사용하며, `태그 설정`은 팝업 안에서 `JournalTagProfileModal`을 열 수 있어야 한다.

---

### 일자 카드 ⋯ 컨텍스트 메뉴 (JournalDayCard Context Menu)

**구현 파일**: `app/frontend-vue/src/views/journal/day/components/JournalDayCard.vue`

**레거시 출처**: `legacy/static/vue/feature/journal/day/components/JournalDayContextMenu.ts`

**메뉴 구조**:
- 주간 뷰로 이동 → `router.push({ name: "journal-weekly", query: { stdrdDt: day.stdrdDt } })` (월간 뷰 전용 용도)
- 새 창으로 열기 (일자 뷰) → `window.open(BASE_URL + /journal/daily?stdrdDt=..., "_blank", "width=...,height=...")` — features 지정으로 탭 아닌 새 창 강제
- `JournalDayDaily.vue` 로드 시 `stdrdDt`에서 `yy`·`mnth`를 파생해 `fetchDays`에 명시 전달 (스토어 기본값 현재 날짜가 백엔드 필터와 불일치하는 문제 방지)
- 날짜 이동(이전/다음)은 로컬 Date 생성자로 파싱 — `new Date(stdrdDt)`는 UTC로 파싱되어 Korea(UTC+9) 환경에서 날짜가 1일 밀리는 버그 발생
- 중앙 날짜 표시: `<input type=date>` 클릭 시 브라우저 달력 피커 오픈, 선택 날짜로 `router.replace`하여 이동
- 팝업 너비 1600px / 높이 1080px (`window.screen.availWidth/Height` 상한); left/top 제거하여 OS 기본 배치 사용
- 수정 → `openReg()` (등록 모달 재활용, id 포함)
- 상태 서브메뉴: 중요(IMPRTC, 표시 전용), 접힘(COLLAPSED, `POST /api/states` 토글)
  - 삭제 → `DELETE /api/journal/day/{id}` → 성공 알림 OK 이후 `journalStore.fetchDays()`

**드롭다운**: Metronic `data-kt-menu-trigger="click"`, `data-kt-menu-placement="bottom-end"`

---

### 엔트리 ⋯ 컨텍스트 메뉴 (JournalEntryItem Context Menu)

**구현 파일**: `app/frontend-vue/src/views/journal/entry/components/JournalEntryItem.vue`

**레거시 출처**: `legacy/static/vue/feature/journal/entry/components/JournalEntryContextMenu.ts`

**우측 액션 영역 구조**:
- 댓글 등록 버튼 (⋯ 밖, 단독 버튼) → `attachableStore.openCommentRegist(id, contentType)`
- 복사 버튼 (`bi-copy`, ⋯ 밖, 단독 버튼) → `copyEntry()` — 날짜(요일)·`htmlToPlainText(content)` 클립보드 복사. `content` = TinyMCE HTML 원문; HTML 제거 후 평문. (레거시 `copy()` 동일 포맷)
- ⋯ 드롭다운:
  - 헤더: contentLabel (일기/꿈)
  - 수정 → `modalStore.openEntryModify(id)`
  - 이력 → `attachableStore.openHistory(contentType, id)`
  - 관련 글 추가 (`elseDreamYn !== "Y"` 조건) → `attachableStore.openRelated(contentType, id)`
  - 구분선
  - 라이프사이클 서브메뉴 (OPEN/PENDING/RESOLVED radio) → `PUT /api/lifecycles { id, contentType, lifecycleKey }`
  - 상태 서브메뉴 toggle → `POST /api/states { id, contentType, stateKey }`
    - IMPRTC(중요), REFRNC(참조) — 공통
    - NHTMR(악몽), HALLUC(환각/현시) — 꿈 전용 (`isDream`)
    - COLLAPSED(접기) — 공통
  - 구분선
  - 삭제 → `DELETE /api/journal/entry/{id}` → 성공 알림 OK 이후 현재 route 기준 목록 재조회 + `#journal-day-{stdrdDt}` 스크롤

**상태/라이프사이클 변경 후**: `fetchDays().then(() => nextTick(() => scrollIntoView(#journal-day-{stdrdDt})))` — 목록 갱신 + 해당 일자로 스크롤.

**RESOLVED 자동 접힘**: `isCollapsed` computed에서 `localCollapsedOverride` 없고 `lifecycleKey === "RESOLVED"`이면 `true` 반환. 상태 서버 저장 없이 클라이언트에서 자동 접힘 처리.

**검색 팝업의 엔트리 액션**: `JournalEntrySearchPage.vue`는 `JournalEntryRegistModal`을 직접 마운트하고, 모달의 `success` 이벤트를 받아 현재 검색 목록을 다시 조회한 뒤 저장한 엔트리 article로 스크롤한다. 삭제는 `DELETE /api/journal/entry/{id}` 후 검색 목록에서 해당 항목을 제거한다. 검색 결과 내부의 저널 해석 수정 액션은 `JournalInterpretationRegistModal`을 같은 페이지에 직접 마운트해 열며, 수정 모드는 `GET /api/journal/interpretation/{id}` 상세 조회가 성공한 뒤 제목/본문/순번을 채운 폼을 표시한다. 해석 제목은 선택값이므로 제목이 비어 있어도 저장 확인 후 등록/수정을 진행한다. 해석 저장 후 모달 내부의 `journalStore.fetchDays()` 완료를 감지해 검색 목록을 재조회한다.

---

### 엔트리 클라이언트 접힘 토글 (Entry Local Collapse Toggle)

**구현 파일**: `app/frontend-vue/src/views/journal/entry/components/JournalEntryItem.vue`

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

**접힘 우선순위**: 엔트리 자체 토글 > 챕터 강제(`forceCollapsed`) > RESOLVED 자동 접힘 > 서버 COLLAPSED 상태

**토글 버튼 위치**: 왼쪽 열 (`#sortOrder` 아래) — `bi-arrows-expand` (접힘) / `bi-arrows-collapse` (펼침)

---

### 챕터 토글 → 하위 엔트리 전파 접힘/펼침 (Chapter Collapse Propagation)

**구현 파일**: `JournalChapterItem.vue` → `JournalEntryItem.vue`

**동작**: 챕터 우측 접힘 토글 버튼 클릭 시, 챕터 `localCollapsedOverride`가 하위 엔트리 전체에 `forceCollapsed` prop으로 전달된다.
- 챕터 펼침(`false`) → 엔트리 전체 펼침 (RESOLVED 자동 접힘도 override)
- 챕터 접힘(`true`) → 엔트리 전체 접힘
- 챕터 override 없음(`null`) → 엔트리 각자의 접힘 로직으로 복귀

**주요 동기**: RESOLVED 챕터는 모든 엔트리가 RESOLVED → 챕터·엔트리 모두 자동 접힘. 챕터를 펼치면 엔트리도 함께 펼쳐져야 하나, 엔트리는 각자 `isResolved → true`를 유지하므로 챕터 펼침만으로는 엔트리가 열리지 않던 문제를 해결.

**구현**:
```vue
<!-- JournalChapterItem.vue -->
<JournalEntryItem :force-collapsed="localCollapsedOverride" ... />
```

---

### 챕터 복사 버튼 (Chapter Copy)

**구현 파일**: `app/frontend-vue/src/views/journal/chapter/components/JournalChapterItem.vue`

**트리거**: 챕터 헤더 우측 복사 버튼 (`bi-copy`) 클릭

**복사 포맷**:
```
날짜 (요일) / 카테고리명 / 챕터 제목   ← 있는 것만 " / " 구분
#정렬번호
엔트리 본문 (HTML 태그 제거)
                                         ← 빈 줄 구분 (엔트리 반복)
```

요일은 `getWeekDayStr(stdrdDt)` 로 계산 (`journalDate.ts`).

**구현**:
```typescript
async function copyChapter(): Promise<void> {
  const lines: string[] = [];
  const headerParts: string[] = [];
  if (props.chapter.stdrdDt) {
    const weekDay = getWeekDayStr(props.chapter.stdrdDt);
    headerParts.push(weekDay ? `${props.chapter.stdrdDt} (${weekDay})` : props.chapter.stdrdDt);
  }
  if (props.chapter.categoryName) headerParts.push(props.chapter.categoryName);
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

**구현 파일**: `app/frontend-vue/src/views/journal/entry/components/JournalEntryItem.vue`

**트리거**: 우측 액션 영역 복사 버튼 (`bi-copy`) 클릭 (댓글 버튼과 ⋯ 사이)

**복사 포맷**:
```
날짜 (요일)
본문 평문 (HTML 태그 제거)
```

**구현**: `htmlToPlainText(content ?? markdownContent)`. `content` = TinyMCE HTML 원문 (마크다운 재처리 이전). HTML 제거 후 평문으로 복사 → 텍스트에디터에 그대로 재붙여넣기 가능. `#sortOrder` 없음 — 레거시 `copy()` 동일.

---

### 헤더 검색 드롭다운 (Header Search Dropdown)

**구현 파일**: `app/frontend-vue/src/layouts/default/components/search/Search.vue`

**참고**: 이 파일은 `.gitignore` 경로(`/app/frontend-vue/src/layouts/default/components/search/`)에 포함되어 git 추적 대상이 아님.

**UI 구조**:
- 일기/꿈 유형 버튼 (`btn-primary` / `btn-info`)
- 검색어 input (debounce 400ms 후 API 호출)
- 결과 목록: 날짜 배지 + 80자 content snippet + 검색 페이지 링크 버튼
- "전체 결과 보기" RouterLink → `journal-entry-search` route

**검색 API**: `GET /api/journal/entries?type=DIARY|DREAM&sort=asc|desc&searchKeywords=...&tagIds=...`
- 응답: `AjaxResponse.rsltList`

**결과 클릭**: `RouterLink :to="{ name: 'journal-entry-search', query: { type, searchKeywords } }"`
- 검색 팝업에서 직접 수정/삭제하지 않고 검색 페이지(`JournalEntrySearchPage`)로 이동
- `JournalEntryRegistModal`은 `JournalLayout` 하위에만 마운트되므로 헤더 드롭다운에서는 모달 직접 열기 불가

---

### 검색 팝업 전체 기능 (`JournalEntrySearchPage.vue`)

레거시 `journal_entry_search_module.ts` 의 멀티키워드·멀티태그 AND 검색을 Vue SPA 로 재현.

**컨트롤 바 (1행)**: 고급 필터 토글 | 초기화 | 정렬 토글(asc/desc) | 검색 || 전체 복사 | TXT 내보내기 | 키워드 배지들 | 태그 배지들 | 결과 건수

**고급 필터 아코디언**: 유형 토글(일기/꿈) + 키워드 입력 + 추가 버튼. `v-show` 로 토글.

**멀티키워드 AND 검색**: 키워드는 `searchKeywords[]` URL 파라미터 배열로 관리. 배지 X 클릭 → 해당 키워드 제거 후 URL replace → 자동 재조회.

**멀티태그 AND 검색**: 태그는 `tagIds[]` + `tagNames[]` URL 파라미터 배열로 관리. 배지 X 클릭 → 해당 태그 제거. `tagNames[]` 은 기존 단일 `tagName` 파라미터와 하위호환.

**태그 컨텍스트 메뉴에서 태그 추가** (`JournalTagContextMenu.vue`):
- 팝업 외부: 새 창 열기 (`tagIds`, `tagNames` 파라미터)
- 팝업 내부 (`route.name === "journal-entry-search"`): 기존 `tagIds`/`tagNames` 배열에 APPEND (중복 무시) → `router.replace()`

**TXT 내보내기**: `GET /api/journal/entries/export?type=...&sort=...&tagIds=...&searchKeywords=...`

**전체 복사 포맷**: 레거시 `JournalEntrySearch.copy()` 동일 — 날짜가 바뀔 때만 `날짜(요일)` 헤더, `#순번\n본문`, 엔트리 간 빈 줄, `\r\n` 줄바꿈.

---

## 검색 팝업 전용 동작

### AI 챗 숨김 (`App.vue`)

팝업 라우트(`journal-entry-search`)에서는 `AppChat`을 렌더하지 않는다.

```typescript
// App.vue
const isPopup = computed(() => route.name === "journal-entry-search");
// template: <AppChat v-if="authStore.isAuthenticated && !isPopup" />
```

### 401 세션 만료 처리 (`main.ts`)

팝업 라우트에서 401 응답 시 로그인 화면 이동 대신 창 닫기 확인 다이얼로그를 표시한다.

```typescript
const isPopup = router.currentRoute.value.name === "journal-entry-search";
if (isPopup) {
  // "세션이 만료되었습니다. 창을 닫겠습니까?" → window.close()
} else {
  // 기존: "로그인 화면으로 이동하시겠습니까?" → router.push({ name: "sign-in" })
}
```
