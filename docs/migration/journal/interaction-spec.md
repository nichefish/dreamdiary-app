# 저널 어사이드 인터랙션 스펙 (Journal Aside Interaction Spec)

> 공통 인터랙션 패턴(AJAX, 모달, Tagify, TinyMCE 등)은 ``common/interaction-spec.md`` 참조.



## Vue SPA 구현 상태 (소스 기준)

| 인터랙션 | `features/journal/stores/journal.ts` / UI | 상태 |
|----------|---------------------------|------|
| 정렬 토글 | `sortOrder` + `toggleSort` + localStorage 유지 + 프론트 역순 | ✓ |
| 태그 클릭 컨텍스트 메뉴 | `tagContextMenu.ts` + `JournalTagContextMenu.vue` — 메뉴 액션과 태그 프로필 콘텐츠 유형 레이블은 현재 locale 카탈로그 사용 | ✓ |
| 일자 카드 ⋯ 컨텍스트 메뉴 | `JournalDayCard.vue` — Metronic dropdown | ✓ |
| 메타 버튼 드롭다운 | `JournalDayCard.vue` — `bi-bar-chart` 버튼 클릭 시 Bootstrap `dropup` 메뉴; 해당 일자 메타 항목 1개씩 나열; 항목 클릭 → `JournalDayMetaModal` 오픈; `width: max-content`로 내용 폭에 맞게 auto-size | ✓코드 |
| 일자 필터 모달 (메타+태그 다중 AND) | `JournalDayMetaModal.vue` — 메타 또는 태그를 시드로 열림(`openDayFilterModal`); 상단 칩에 선택 메타(파랑)·태그(초록) 혼합 표시; 최초 시드 칩도 × 클릭으로 자유 제거(제한 없음)되며 같은 seed 의 payload 재조회로 다시 주입하지 않는다; 모든 필터 제거 시 빈 결과 반환(payload.list 전체 노출 방지); AND 필터(모든 선택 메타+태그 보유 날짜만); 행에서 비선택 메타 뱃지 클릭 → 메타 필터 추가, 비선택 태그 클릭 → 태그 필터 추가, 선택된 태그 클릭 → 태그 필터 제거; 각 행의 선택 메타 값은 `selectedMetas` 배열 순서(선택 순)대로 표시하여 행마다 순서 일관성 유지; 연도 변경 시 필터 유지(재조회만), 신규 오픈 시 시드 1개로 초기화; `JournalDayTagDetailModal` 제거하여 단일 모달로 수렴; 태그 입력 검색 — 컨트롤 행의 태그 입력(모달 내 datalist 미표시 대응: 인라인 typeahead 미리보기; 모달 오픈·포커스 시 `journalModalStore.dayTagCategoryMap`(SSOT)과 `/api/journal/day/tags` 를 병합해 최초 1회 로드)으로 기존 태그만 AND 필터에 추가(엔트리 검색과 동일 `findKnownTagName`·categoryMap 매칭); 카탈로그에 없는 이름은 Swal 대신 인라인 안내(모달 유지), 동명 태그(다중 카테고리)는 카테고리 선택 버튼으로 분기; 모달 닫힘 시 입력·힌트·카테고리 선택 상태 초기화(카탈로그 캐시는 유지) | ✓코드 |
| 엔트리 ⋯ 컨텍스트 메뉴 | `JournalEntryItem.vue` — lifecycle/status/수정/이력/관련글/삭제 | ✓ |
| 엔트리 클라이언트 접힘 토글 | `JournalEntryItem.vue` — `localCollapsedOverride` ref | ✓ |
| 챕터 복사 버튼 | `JournalChapterItem.vue` — `copyChapter()`, 날짜(요일)·카테고리·엔트리 전체 텍스트 클립보드 복사 | ✓ |
| 꿈 복사 버튼 | `JournalDayCard.vue` — `copyDreams()`, 날짜(요일) 헤더 + 꿈 엔트리 전체 클립보드 복사 | ✓ |
| 엔트리 복사 버튼 | `JournalEntryItem.vue` — `copyEntry()`, 날짜(요일)·본문 텍스트 클립보드 복사 (레거시 동일 포맷) | ✓ |
| 헤더 검색 드롭다운 | `Search.vue` — 일기/꿈 유형 선택 + debounce 검색 + 결과 링크 (`journal-entry-search`) | ✓ |
| 메타 VIEW · 메타 컨텍스트 메뉴 | `metaContextMenu.ts` + `JournalMetaContextMenu.vue` — 헤더 `#메타` 클릭 시 팝업(태그 메뉴와 동일 UI); 현재 locale 메뉴로 「검색」→ `openDayFilterModal`(`JournalDayMetaModal`), 「그래프로 보기」→ `addMetaToGraph`(최대 2·이미 있으면 비활성, 제한 경고도 현재 locale), 「메타 설정」→ `openMetaProfile`(`JournalMetaProfileModal`, `GET /api/journal/day/metas/{id}`) | ✓코드 |
| 메타 VIEW 비교 그래프 | `JournalDayMeta.vue` — `selectedMetas` 최대 2; 헤더에서 그래프에 포함된 메타는 굵게 표시·옆 × 제거; 연도 「전체」(yy 미전송)·임계값·메타별 통계; **한 ApexCharts**에 시리즈 최대 2개(일자 합집합 X축, 범례, 단위 다르면 Y축·툴팁에서 메타별 단위) | ✓코드 |
| Pinpoint | `JournalAside.vue` — `pinnedYy/pinnedMnth` ref + pinpoint/turnback 함수 | ✓ |
| 챕터 카테고리 필터 | `JournalAside.vue` — `JOURNAL_CHAPTER_DIARY_CTGR_CD`·`NOTE` 병합 체크박스, `store.chapterCtgrCds` → `fetchDays` | ✓ |
| 일기/꿈 라이프사이클 필터 | `JournalAside.vue` — `store.diaryLifecycleKey` / `store.dreamLifecycleKey` → `fetchDays` 후 일기/꿈 각각 후처리 필터 | ✓ |
| 주간 네비게이터 | `JournalAside.vue` — 요일 버튼 7개, 이전/다음 주 화살표, 주간 범위 라벨 | ✓ |
| 연/월 select | 연도 select + 월 그리드 (`navigateMonth`, `gotoYyMnth`) | ⚠ |
| 툴바 키워드 전체검색 | `JournalDayViewToolbar` 로컬 ref → `openSearchTab()` → 새 탭 `/vue-app/journal/entry/search` | ✓ |
| 툴바 floating·aside 열기 | `JournalDayViewToolbar` 전체와 열린 저널 일자 aside의 상단선이 고정 앱 헤더 아래에서 sticky로 일치하고, 툴바는 별도 그림자 없이 하단 경계만 사용. aside 숨김 시 우측 끝 버튼이 `asideStore.show()` 호출. 모바일은 본문 우상단 전용 버튼 유지 | ✓ |
| 어사이드 목록 키워드 필터 | `JournalAside.vue` `diaryKeyword` / `dreamKeyword` → `fetchDays()` (목록 축소) | ✓ — 툴바 `openSearchTab` 전체검색과 분리 (`vue-screen-overview.md` 필터·검색 정책) |
| 등록/수정 후 확인·스크롤 | 일자/챕터/엔트리 등 submit 성공 → 성공 알림 OK 이후 저장 위치 scrollIntoView. 엔트리는 성공 알림 전 목록/상세 DOM을 먼저 준비하고, OK 이후에는 스크롤만 수행한다. 월간/주간 화면은 재조회 중 기존 목록 DOM을 유지한다. 챕터는 저장된 챕터 DOM(`#journal-chapter-{id}`)을 우선 탐색하고 없으면 일자 카드로 fallback | ✓ |
| 상태/라이프사이클 변경 후 스크롤 | 상태 토글·라이프사이클 설정 서버 반영 후 `refreshJournalDaysForRoute`(주간/월간/일간 route 분기) → `#journal-day-{stdrdDt}` scrollIntoView. **일간(`journal-daily`)** 은 `route.query.stdrdDt`(없으면 항목 `stdrdDt`)로 `viewType=DAILY`·`yy`/`mnth` 파생 조회 — 무파라미터 `fetchDays()`는 스토어 기본 월(오늘)로 재조회되어 날짜가 어긋남 | ✓ |
| 챕터 일자 변경 | `JournalChapterRegistModal.vue` — 수정 모드+비DREAM 한정, 날짜 picker + 챕터 일자 변경 버튼, 현재 locale 확인창 사용, `POST /api/journal/chapter/{id}/move` 호출. 응답 `message`를 우선 표시하고 없으면 현재 locale fallback을 사용한 뒤 `fetchDays` + 신 일자 scrollIntoView | ✓ |
| 챕터 소유권 표시 | `JournalChapterItem.vue` — API `isCreatedBy`; 타인 작성 시 배지·쓰기 버튼 숨김; 클라이언트 차단 경고는 현재 locale 카탈로그 사용, 수정/삭제/이동 API 거부 시 서버 `msg.rslt.not-owner` (403) alert | ✓ |
| 챕터 resolved (파생) | 챕터 자체 resolved 상태 없음. CSS `:has` + `:not(:has([data-resolved=\"N\"]))` 로 하위 엔트리 전체 resolved 여부를 집계해 접힘 외곽 inset 표시. 접힘 바: 완료 1px 초록·중요 2px 빨강·참조 4px 노랑(엔트리 `$journal-paired-states` 와 동일). 단독 우선 중요>참조>완료; 중요+완료·중요+참조·삼중 조합 다중선. DB 마이그레이션: `lifecycle` 테이블 `ref_content_type='JOURNAL_CHAPTER'` RESOLVED 레코드 소프트 삭제 | ✓ |
| TAGCLOUD/DIARIES/DREAMS | `showTagCloud` 등 + 토글 핸들러 | ✓ |

**일자 필터 모달 i18n**: 제목·결과 건수·연도/전체 연도·연월 구분선·필터 추가/제거·일자 새 창 tooltip·빈 상태·닫기와 조회 실패 fallback은 현재 locale의 클라이언트 카탈로그를 사용한다. locale 변경은 선택 메타/태그·AND 필터·모든 필터 제거 시 빈 결과·연도 변경 시 필터 유지 계약을 변경하지 않는다. 태그 입력 검색의 placeholder·카테고리 선택·미존재 태그 알림 문구는 엔트리 검색 키(`journal.entry.search.tag.*`, `journal.entry.search.category.*`)를 재사용한다.

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

### 챕터 카테고리 필터 (Chapter Category Filter)

**트리거**: CHAPTER CATEGORIES 항목 체크박스 변경

**레거시**: `data-journal-day-action="chapter-ctgr-select"` → `bridge.applySearchParamsAndReload({ chapterCtgrCds: [...] })`

**Vue SPA 구현**:
```typescript
// JournalAside.vue 또는 JournalAsideEntryFilters 컴포넌트 내
function toggleChapterCategory(code: string) {
    if (store.chapterCtgrCds.includes(code)) {
        store.chapterCtgrCds = store.chapterCtgrCds.filter((item) => item !== code);
    } else {
        store.chapterCtgrCds = [...store.chapterCtgrCds, code];
    }
    void store.fetchDays();
}
```

**DIARIES 부모 토글과의 관계**:
- 챕터 카테고리 필터는 `DIARIES` 하위 필터다.
- `store.showDiaries=false` 동안 챕터 카테고리 필터 UI는 렌더링하지 않는다.
- `DIARIES` OFF는 `store.chapterCtgrCds` 값을 삭제하지 않는다. 다시 ON으로 돌리면 기존 선택값이 그대로 적용된다.

**챕터 옵션 로드 타이밍**: `onMounted` 시 `journalModalStore.prefetchChapterCategories()` 호출 후 일기·노트 코드 병합 (`JOURNAL_CHAPTER_CTGR_CD` 는 DB 마이그레이션으로 제거됨). 동일 시점에 여러 컴포넌트가 호출하면 진행 중인 Promise를 공유해, 뒤따른 호출자도 실제 조회 완료 후 옵션을 병합한다.

**store 반영**: `store.chapterCtgrCds: string[]` — `fetchDays` 에서 `chapterCtgrCds.length > 0` 이면 query param 포함

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

**서버 반영**: `JournalDaySearchParam.diaryLifecycleKey` / `dreamLifecycleKey` 를 `JournalDayFilterHelper.filterInMemory()`에서 적용한다. 일기/꿈 키워드와는 AND 조건이며, `OPEN`은 값이 없거나 `OPEN`인 엔트리를 포함한다.

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
- 삭제는 삭제 전에 `stdrdDt`를 캡처하고 현재 locale 확인창을 표시한다. 삭제 응답 `message`를 우선 표시하고 없으면 현재 locale fallback을 사용하며, 성공 알림 OK 이후 현재 route 기준 목록 재조회 + `#journal-day-{stdrdDt}` 스크롤.
- 상태 변경은 현재 route 기준 목록 재조회 + `#journal-day-{stdrdDt}` 스크롤.

---

### 저널 일자 중복 등록 차단
**트리거**: `JournalDayRegistModal` 신규 등록 submit

**동작**:
1. 서버는 로그인 사용자 기준으로 같은 `journalDate`의 활성 저널 일자가 있는지 확인한다.
2. 중복이면 기존 데이터를 수정으로 전환하지 않고 `rslt=false`, `msg.journal.day.duplicate` 메시지로 응답한다.
3. 모달은 저장 성공 처리를 진행하지 않고 서버 메시지를 표시한다.
4. 신규 등록 성공 시 서버는 `JournalDayBootstrapService`로 기본 SUMMARY 챕터와 빈 DIARY 엔트리 구조를 보장한다. 이미 DIARY 챕터가 있으면 추가 생성하지 않는다.

---

### 첫 일반 챕터 SUMMARY 자동 부여
**트리거**: 챕터 신규 등록 (`JournalChapterService.preRegist` — 수동 등록 및 `JournalDayBootstrapService` 자동 등록 공통)

**동작**:
1. 새 챕터의 `sortOrder` 는 같은 일자의 마지막 순번+1로 계산한다. 이 계산에는 DREAM 챕터도 포함된다.
2. SUMMARY 기본 카테고리 부여 판정은 **"기존 non-DREAM 챕터가 없는가"** 기준이다. DREAM 은 항상 마지막에 배치되는 개념 챕터이므로 판정에서 제외한다.
3. 기존 non-DREAM 챕터가 없고 `categoryCode` 가 비어 있으면 첫 일반 챕터로 보고 `SUMMARY` 를 부여한다. 이미 카테고리가 있으면 덮지 않는다.
4. 결과적으로 꿈 챕터가 먼저 있는 날에 첫 일기·노트 챕터를 등록해도 SUMMARY 가 정상 부여된다.

**변경 전/후**: 변경 전에는 `sortOrder == 1` 을 기준으로 판정해, 꿈 챕터가 먼저 있으면 첫 일반 챕터의 `sortOrder` 가 2 이상이 되어 SUMMARY 가 누락됐다. 기존 데이터는 `V0.24.2__journal-chapter-summary-backfill-mariadb.sql` 로 각 일자의 가장 앞선 non-DREAM 챕터(빈 카테고리)에 SUMMARY 를 백필한다.

---

### 등록/수정 후 엔트리 위치 스크롤

**트리거**: `JournalEntryRegistModal` submit 성공 (등록 및 수정 공통)

**동작**:
1. 저장 성공 응답에서 엔트리 ID를 확인한다. 수정이면 기존 `model.id`를 fallback으로 사용한다.
2. 모달을 닫은 뒤 성공 알림을 표시한다.
3. 성공 알림 OK 이후 월간/주간 화면에서는 현재 route 기준으로 `fetchDays()`를 호출해 목록 DOM을 갱신한다.
4. 검색 팝업(`JournalEntrySearchPage`)에서는 `prepare-success` 이벤트에서 현재 검색 조건 목록 또는 수정 대상 항목 DOM을 먼저 준비한다.
5. 월간/주간 기본 목록은 재조회 후 강제 스크롤하지 않고 현재 스크롤 위치를 유지한다. 검색 팝업은 성공 알림 OK 이후 추가 재조회 없이 저장 위치 스크롤만 수행한다.
6. 월간/주간 기본 목록은 기존 DOM을 유지해 브라우저 스크롤 위치 보존에 맡긴다.
7. 일자 상세 팝업은 팝업 내부의 동일 엔트리 위치로 스크롤한다.
8. 검색 팝업은 `success` 이벤트 payload를 받아 `#journal-entry-search-{id}`로 스크롤한다.

**월간/주간 재조회 렌더 계약**: 기존 `dayList`가 있는 상태에서 `fetchDays()`가 다시 실행될 때는 본문을 전체 로딩 스피너로 교체하지 않는다. 기존 목록 DOM을 유지해야 문서 높이가 순간적으로 줄어들지 않고, 저장 후 스크롤 위치가 브라우저에 의해 `0`으로 클램프되지 않는다.

**엔트리 저장 메시지 i18n**: 등록·수정 확인과 성공·실패 fallback은 현재 locale의 클라이언트 카탈로그를 사용하고, 저장 API 응답에 `message`가 있으면 서버 메시지를 우선 표시한다. 검색 팝업의 `prepare-success` 작업은 성공 알림 전에 완료하고, 알림 확인 후 기존 `success` 이벤트 또는 상세·목록 재조회와 위치 복귀를 실행한다.

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
- 저장된 `JOURNAL_DREAM` 태그 프로필 본문은 목록/검색/상세의 꿈 엔트리 본문 아래에 표시된다. 일기(`JOURNAL_DIARY`) 태그 프로필은 설정 모달과 태그 색상 의미에만 사용하고, 엔트리 본문 아래에는 표시하지 않는다.
- 저장·삭제 성공 알림 확인 후 화면 갱신: 월간/주간/일간 등에서는 `refreshJournalDaysForRoute` + contentType 대응 `fetchTagCloud`(`JOURNAL_DAY`→day, `JOURNAL_DIARY`→diary, `JOURNAL_DREAM`→dream). 결산 상세(`annual-detail`)에서는 `fetchTagRows(yy, activeSection)`로 결산 태그클라우드를 재조회한다. 검색 팝업(`journal-entry-search`)에서는 일자/클라우드 재조회 없이 `success` → `loadEntries()`만 수행한다.

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
- 새 창으로 열기 (일자 뷰) → `window.open(BASE_URL + /journal/daily?stdrdDt=..., "_blank", "width=...,height=...")` — features 지정으로 탭 아닌 새 창 강제
- JournalDayDaily.vue 로드·route.query.stdrdDt watch: buildDailyFetchParams(stdrdDt) → fetchDays({ viewType: DAILY, stdrdDt, yy, mnth }) (@/utils/journalDayRefresh.ts)
- 일간 팝업(journal-daily)에서 저장·삭제·상태 변경·이력 복원 후 목록 갱신: refreshJournalDaysForRoute(store, route, fallbackStdrdDt?) — 주간/월간과 동일하게 route name 분기, 일간은 URL·fallback 기준일 유지
- 날짜 이동(이전/다음)은 로컬 Date 생성자로 파싱 — `new Date(stdrdDt)`는 UTC로 파싱되어 Korea(UTC+9) 환경에서 날짜가 1일 밀리는 버그 발생
- 중앙 날짜 표시: `<input type=date>` 클릭 시 브라우저 달력 피커 오픈, 선택 날짜로 `router.replace`하여 이동
- 이전/다음 버튼·날짜 선택 tooltip·빈 상태와 목록 조회 실패 fallback은 현재 locale 카탈로그를 사용한다. locale 변경은 `stdrdDt` query와 일간 조회 조건을 변경하지 않는다.
- 팝업 너비 1600px / 높이 1080px (`window.screen.availWidth/Height` 상한); left/top 제거하여 OS 기본 배치 사용
- 수정 → `openReg()` (등록 모달 재활용, id 포함)
- 상태 서브메뉴: 중요(IMPRTC, 표시 전용), 접힘(COLLAPSED, `POST /api/states` 토글)
  - 삭제 → `DELETE /api/journal/day/{id}` → 성공 알림 OK 이후 `refreshJournalDaysForRoute` (일간 포함 route 분기)

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
  - 수정 → `modalStore.openEntryModify(id)`
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

**상태/라이프사이클 변경 후**: `fetchDays().then(() => nextTick(() => scrollIntoView(#journal-day-{stdrdDt})))` — 목록 갱신 + 해당 일자로 스크롤.

**액션 메시지 i18n**: 클립보드 복사 성공·실패, 라이프사이클·상태 변경 실패, 삭제 확인·성공·실패 fallback은 현재 locale의 클라이언트 카탈로그를 사용한다. API 응답에 `message`가 있으면 서버 메시지를 우선 표시하며, 상태 변경·삭제 성공 후 기존 현재 route 재조회와 `#journal-day-{stdrdDt}` 스크롤 순서를 유지한다. 검색 팝업에서는 재조회 중인 `journalStore.loading` 완료를 감지해 검색 결과를 다시 조회한다.

**RESOLVED 자동 접힘**: `isCollapsed` computed에서 `localCollapsedOverride` 없고 `lifecycleKey === "RESOLVED"`이면 `true` 반환. 상태 서버 저장 없이 클라이언트에서 자동 접힘 처리.

**꿈 RESOLVED 표시**: `JOURNAL_DREAM`의 완료 상태는 일기·노트·해석의 초록과 구분해 보라색을 사용한다. 펼친 행은 은은한 보라 배경·테두리·좌측선, 자동 접힌 행은 보라 접힘 표시와 순번, 라이프사이클 메뉴는 보라 완료 라벨을 사용한다. 중요·참조를 함께 지정하면 완료 보라선과 중요 빨강·참조 노랑선을 모두 유지한다. 이 표시 차이는 lifecycle 저장 payload와 RESOLVED 자동 접힘 우선순위를 변경하지 않는다.

**검색 팝업의 엔트리 액션**: `JournalEntrySearchPage.vue`는 `JournalEntryRegistModal`을 직접 마운트하고, 모달의 `prepare-success` 이벤트에서 현재 검색 목록 또는 수정 대상 article DOM을 성공 알림 전에 준비한다. 모달의 `success` 이벤트는 성공 알림 OK 이후 저장한 엔트리 article 스크롤만 담당한다. 삭제는 `DELETE /api/journal/entry/{id}` 후 검색 목록에서 해당 항목을 제거한다. 검색 결과 내부의 저널 해석 수정 액션은 `JournalInterpretationRegistModal`을 같은 페이지에 직접 마운트해 열며, 수정 모드는 `GET /api/journal/interpretation/{id}` 상세 조회가 성공한 뒤 제목/본문/순번을 채운 폼을 표시한다. 해석 제목은 선택값이므로 제목이 비어 있어도 저장 확인 후 등록/수정을 진행한다. 해석 저장 후 모달 내부의 `journalStore.fetchDays()` 완료를 감지해 검색 목록을 재조회한다.

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

**스마트 토글 (첫 클릭 보정)**: 챕터는 `allEntriesResolved`(전부 RESOLVED)일 때만 자동 접히므로, **일부만** RESOLVED면 챕터는 펼쳐진 채 일부 엔트리만 접힌 상태가 된다. 이때 `localCollapsedOverride`가 아직 `null`이라 "챕터가 펼쳐져 있다"는 신호가 엔트리에 전달되지 않는다. 변경 전에는 이 상태에서 첫 클릭이 챕터를 접어버려 엔트리를 펼치려면 2클릭이 필요했다.

변경 후 `toggleChapter()`는 다음 조건을 모두 만족하면 접기 대신 `override=false`로 **하위 엔트리를 전체 펼친다**(1클릭):
- `localCollapsedOverride === null` (사용자가 아직 토글하지 않음)
- `!isCollapsed` (챕터가 펼쳐져 있음)
- `hasDataCollapsedEntry` (데이터 규칙으로 접힌 엔트리가 있음)

`hasDataCollapsedEntry`는 엔트리의 `lifecycleKey==='RESOLVED'` 또는 서버 `COLLAPSED` 상태로만 판정한다. 엔트리 개별 로컬 토글은 각 엔트리가 소유해 챕터에서 볼 수 없으므로 감지하지 못하는 근사치다.

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
날짜 (요일) / 카테고리명 / 챕터 제목   ← 있는 것만 " / " 구분
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

**결과 읽기 보조**: 날짜 헤더는 해당 날짜에 묶인 현재 검색 결과 건수를 locale 라벨로 함께 표시한다. 건수는 현재 `entries` 배열 기준으로 계산하며, 검색 조건·정렬·locale 변경 외의 별도 상태를 만들지 않는다.

**결과 요약 바**: 결과 목록 위에는 현재 `entries` 배열 기준 총 건수, 고유 기준일 수, 고유 연월 수를 locale 라벨로 표시한다. 검색 전/빈 결과 상태에서는 별도 요약 바를 표시하지 않는다.

**확정 전 입력 안내**: 키워드/태그 입력칸에 값이 남아 있으면 `hasPendingSearchInputs` 기준으로 고급 필터 영역에 검색 실행 시 현재 입력값도 조건에 포함된다는 locale 안내를 표시한다. 안내는 URL query, 검색 실행 순서, 입력값 확정 규칙을 변경하지 않는다.

**태그 컨텍스트 메뉴에서 태그 추가** (`JournalTagContextMenu.vue`):
- 팝업 외부: 새 창 열기 (`tagIds` 파라미터)
- 팝업 내부 (`route.name === "journal-entry-search"`): 기존 `tagIds` 배열에 APPEND (중복 무시) → `router.replace()`

**TXT 내보내기**: `GET /api/journal/entries/export?type=...&sort=...&tagIds=...&searchKeywords=...`

**전체 복사 포맷**: 레거시 `JournalEntrySearch.copy()` 동일 — 날짜가 바뀔 때만 `날짜(요일)` 헤더, `#순번\n본문`, 엔트리 간 빈 줄, `\r\n` 줄바꿈. 요일은 현재 locale의 공용 요일 카탈로그를 사용한다. 확정 전 입력값을 URL 검색 조건으로 반영하고 재조회한 뒤 복사한 경우 성공 알림은 조건 반영 후 복사임을 구분한다.

**액션 메시지 i18n**: 검색 결과/엔트리 상세 조회 실패, 태그 선택·검색 조건 검증, 검색 전 안내와 조건 추가 CTA, 빈 결과 조건 수정 CTA, 조건 요약과 결과 상태 라벨, 결과 요약, 날짜별 결과 건수, 입력 힌트, 확정 전 입력 안내, 배지 제거 tooltip, 카테고리 선택 대기 안내, 중복 조건 안내, 복사 대상 없음과 복사 성공/실패 알림은 현재 locale의 클라이언트 카탈로그를 사용한다. locale 변경은 URL query·검색·복사 실행과 클립보드/TXT 본문 포맷을 변경하지 않는다.

---

### 연간 결산 CRUD 메시지와 재조회

`journalAnnual.ts`의 결산 목록 조회 실패, 전체 결산 갱신·결산/리뷰 등록·수정 결과 fallback과 리뷰 삭제 확인·결과 fallback은 현재 locale의 카탈로그를 사용한다. API 응답에 `message`가 있으면 서버 메시지를 우선 표시한다. 성공 알림 후 전체 결산 갱신은 목록·총계를, 결산 저장은 목록을, 리뷰 저장·삭제는 해당 연도 상세를 기존 순서대로 재조회한다.

---

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

`ChatAIService` 본경로(LLM) 생성은 `OllamaClient.chatStream`으로 NDJSON을 읽으며 같은 `/topic/chat/session/{id}`에 `rsltObj.type=DELTA` (`delta` 청크)를 브로드캐스트한다. 클라이언트는 `streamingContent`에 누적해 임시 assistant 버블(평문)로 보이고, 메시지 목록에는 넣지 않는다. 완성 ASSISTANT 메시지·취소·세션 전환 시 `streamingContent`를 비운다. hybrid/retry·language-guard 재시도는 비스트림이며 DELTA를 보내지 않는다. 스트림 중 버블은 마크다운 HTML이 아니라 평문이며, 완성 메시지만 `markdownContent` `v-html`을 쓰는다.

### AI 챗 응답 대기 단계 (`AppChat.vue`, `chat.ts`)

`ChatAIService.processChat`은 USER 메시지 broadcast 이후 같은 `/topic/chat/session/{id}`로 `rsltObj.type=PROGRESS` 이벤트를 보낸다 (`phase=SEARCHING` → RAG, `phase=GENERATING` → LLM/하이브리드). 클라이언트는 메시지 목록에 넣지 않고 `responsePhase`만 갱신하며, typing 인디케이터 옆에 `chat.waiting.searching` / `chat.waiting.generating`을 표시한다. ASSISTANT 메시지 수신·취소·세션 전환·연결 오류 시 phase를 비운다.

### AI 챗 RAG 근거 → 원문 열기 (`AppChat.vue`)

assistant 메시지 `metadataJson.ragSources` 행을 클릭하면 `useJournalModalStore().openEntryView(journalEntryId)`로 읽기 전용 엔트리 모달(`JournalEntryViewModal`)을 연다. 본문은 목록과 동일하게 `markdownContent` HTML(`journal-content`)을 표시한다. footer **편집**은 `openEntryModifyFromView`로 기존 `JournalEntryRegistModal`로 전환한다.

- 모달 마운트: 비팝업 인증 라우트에서는 `App.vue`가 `JournalEntryViewModal`과 `JournalEntryRegistModal`을 전역 마운트한다 (`JournalDayLayout`에서는 등록/수정 모달 중복 마운트하지 않음).
- 팝업 라우트(`journal-entry-search`, `journal-daily`)는 각자 레이아웃/페이지에 등록/수정 모달을 유지하고, `AppChat` 자체가 숨겨지므로 채팅 출처 딥링크 경로가 없다.
- 채팅 드로어 z-index(6002)보다 모달이 위에 오도록 `App.vue`가 `body.modal-open`일 때 `.modal` / `.modal-backdrop` z-index를 6100 / 6090으로 올린다. 모달 내부 저장·삭제 확인 SweetAlert2 컨테이너는 활성 모달보다 위에 보여야 하므로 같은 조건에서 `.swal2-container` z-index를 6110으로 올린다.
- 출처 목록은 기본 5건 미리보기이며, 숨은 건수가 있으면 `chat.rag.source.more`로 전체 펼친다.

### 팝업 직접 진입 세션 만료 처리 (`router/index.ts`, `sessionExpired.ts`)

팝업 전용 보호 라우트(`journal-entry-search`, `journal-daily`)는 인증이 없을 때 로그인 화면을 팝업 내부에 렌더하지 않는다. 라우터 가드는 `confirmSessionExpired(to.name)`을 호출해 레거시처럼 창 닫기 확인 alert를 표시하고, 확인 시 `window.close()`를 호출한 뒤 현재 route 이동은 `next(false)`로 중단한다.

### 401 세션 만료 처리 (`main.ts`)

팝업 라우트에서 401 응답 시 로그인 화면 이동 대신 창 닫기 확인 다이얼로그를 표시한다. 일반 보호 라우트에서 라우터 가드가 미인증을 감지한 경우도 같은 `confirmSessionExpired` alert를 거친 뒤, 확인 시에만 `/sign-in?sessionExpired=Y&redirect=...`로 이동한다.

저널 등록·수정·삭제·상태 변경 요청이 401 이외의 HTTP 오류로 실패하면 `swalRequestError()`가 오류를 기록하고 응답 JSON의 `message`를 우선 표시한다. 서버 메시지가 없을 때만 `요청 처리 중 오류가 발생했습니다.`를 표시하며, 401의 `AuthExpiredError`는 전역 안내와 중복되지 않도록 별도 alert를 띄우지 않는다.

검색·목록·메타 조회 실패는 실제 빈 결과와 구분한다. 직전 성공 데이터와 결과 건수를 보존하면서 오류를 기록·표시하고, 일자/엔트리/스레드의 상세·수정용 조회가 실패하면 불완전한 모델로 모달을 열지 않는다. 일자 수정·상세, 꿈 등록, 엔트리 수정 정보 조회 실패는 서버 메시지를 우선 표시하고 없으면 현재 locale의 fallback을 사용한다.

저널 스레드 등록·수정 모달의 확인창과 저장 결과 fallback은 현재 locale의 클라이언트 카탈로그를 사용한다. 등록·수정 성공 또는 실패 응답에 서버 `message`가 있으면 서버 메시지를 우선 표시하며, 수정·상세 조회 실패 시 모달을 닫고 현재 locale의 조회 실패 안내를 표시한다. 삭제 확인·성공·실패 fallback도 현재 locale을 사용하고 서버 `message`를 우선 표시하며, 성공 알림 확인 후 첫 페이지 목록을 다시 조회한다.

**저널 스레드 상세 닫기 정책**: `JournalThreadDetailModal`은 `backdrop: "static"`, `keyboard: false`로 생성해 모달 바깥 클릭과 Escape 입력으로 닫히지 않는다. 헤더 ×와 푸터 「닫기」는 `store.closeDetail()`을 호출하는 명시적 종료 경로이며, URL 이동·상세 조회 실패에 따른 프로그램상 종료는 유지한다.

**저널 스레드 목록 검색 카드**: 화면 진입 시 목록과 함께 `GET /api/tags?contentType=JOURNAL_THREAD` 태그 클라우드 및 `GET /api/journal/threads/categories` 분류 선택지를 조회한다. 태그는 단일 선택이며 같은 태그를 다시 클릭하면 해제한다. 태그 선택·해제, 분류·제목 검색, 초기화는 모두 `fetchList(0)`으로 첫 페이지부터 조회한다. 제목 키워드는 서버의 `searchType`·`searchKeyword` 쌍 계약에 맞춰 `searchType=title`을 함께 전송하고, 태그는 `tags[]`, 분류는 `categoryCode`로 전송한다. 등록·수정·삭제 성공 후 목록과 태그 클라우드를 함께 갱신해 태그 빈도와 선택 가능 항목을 최신 상태로 맞춘다. 태그·분류 보조 데이터 조회 실패는 빈 상태로 가장하지 않고 오류 로그와 현재 locale 안내를 표시한다. 등록은 `JournalThreadViewToolbar`에 두고 `thread-create` 라우팅을 유지한다(검색 카드에는 두지 않음). ASIDE는 없다.

**저널 스레드 목록 행 액션**: 관리 열은 수정·삭제 개별 버튼 대신 ⋯ 컨텍스트 메뉴를 제공한다. 메뉴 트리거와 항목 클릭은 행의 상세 이동 이벤트로 전파하지 않으며, 수정은 기존 수정 route로 이동하고 삭제는 기존 확인·삭제 store 액션을 유지한다. 비동기 목록 조회가 끝날 때 Metronic 메뉴를 재초기화한다.

저널 할일 등록·수정 모달의 제목 필수 검증·확인·결과 fallback은 현재 locale의 클라이언트 카탈로그를 사용한다. 저장 API의 서버 `message`가 있으면 우선 표시하고, 성공 시 모달을 닫은 뒤 성공 알림 확인 후 `refreshJournalDaysForRoute()`로 현재 route의 저널 목록을 갱신한다.

저널 해석 등록·수정 모달의 확인·결과 fallback은 현재 locale의 클라이언트 카탈로그를 사용한다. 해석 제목은 선택값으로 유지하고, 저장 API의 서버 `message`가 있으면 우선 표시하며, 성공 시 모달을 닫은 뒤 성공 알림 확인 후 `refreshJournalDaysForRoute()`로 현재 route의 저널 목록을 갱신한다.

저널 해석 아이템의 라이프사이클·상태 변경 실패, 클립보드 복사 성공·실패, 삭제 확인·성공·실패 fallback은 현재 locale의 클라이언트 카탈로그를 사용한다. API 응답의 `message`를 우선 표시하고, 상태 변경 성공은 현재 일자를 재조회한 뒤 스크롤하며, 삭제 성공은 성공 알림 확인 후 현재 route 기준 목록을 갱신한다.

```typescript
const confirmed = await confirmSessionExpired(route.name);
if (confirmed && !isAuthPopupRoute(route.name)) {
  await router.push(buildSessionExpiredSignInRoute(route.fullPath));
}
```
