# 저널 컴포넌트 마이그레이션 스펙 (Journal Component Spec)

> 공통 Freemarker 매크로(checkbox, modal_header 등)는 ``common/component-spec.md`` 참조.

## 저널 전용 컴포넌트 목록


### 18. `JournalTagCloudHeader` (저널 태그 클라우드 헤더)

**Source (Legacy)**: `legacy/templates/view/feature/journal/day/tag/_journal_day_tag_header.ftlh`

**Vue 구현 완료**: `app/frontend-vue/src/features/journal/day/components/JournalTagCloudHeader.vue`

**사용 화면**: `JournalDayMonthly.vue`, `JournalDayWeekly.vue` — `.card.post > .card-header` 내부 (`v-if="store.showTagCloud"`). `JournalDayDaily.vue` 에는 미포함 (일간 뷰는 태그클라우드 없음)

**레거시 HTML 구조**:
```html
<div id="journal_tag_header" class="mb-6 ms-4 w-100">
  <!-- 일자 태그 행 -->
  <div id="journal_day_tag_header" class="row align-items-center mb-4 ms-4 min-h-42px">
    <div class="journal-tag-header__label col-auto d-none d-md-flex ms-4 me-6 text-center fs-6"><b>일자 태그 :</b></div>
    <div class="col flex-grow-1" id="journal_day_tag_list_div"><!-- Handlebars 태그 렌더 --></div>
    <div class="col-auto d-none d-md-flex ms-4 pe-0 border-2 border-gray-300 border-end h-75 w-10px">&nbsp;</div>
    <div class="col-auto d-none d-md-flex ms-4 me-20 text-center fs-6 gap-3">
      <button class="btn btn-sm btn-outline btn-light-primary px-4"
              onclick="dF.Tag.hideSingleTag('#journal_day_tag_list_div');">
        <i class="bi bi-tag pe-0"></i>
      </button>
      <button class="btn btn-sm btn-outline btn-light-primary px-4"
              onclick="dF.JournalTag.listAllAjax();">
        <i class="bi bi-tag pe-0"></i>
      </button>
    </div>
  </div>
  <div class="separator"></div>
  <!-- 일기 태그 행: 동일 구조, id=journal_diary_tag_header, mb-4 -->
  <div class="separator"></div>
  <!-- 꿈 태그 행: 동일 구조, id=journal_dream_tag_header, mb-6 (마지막이므로) -->
</div>
```

**Vue 데이터 바인딩**:
| 레거시 | Vue |
|--------|-----|
| Handlebars 태그 목록 렌더 | `store.tagCloud.{dayTagList,diaryTagList,dreamTagList}` |
| `dF.Tag.hideSingleTag(selector)` | 각 행별 로컬 ref (`hideDaySingles`, `hideDiarySingles`, `hideDreamSingles`) |
| `dF.JournalTag.listAllAjax()` | `attachableStore.openTagList({ yy, mnth, weekStartDt })` |
| 태그 클릭 → 일자 목록 모달 | `journalModalStore.openTagDetail(tag.id, tag.name)` |

**상태 / API**:
| 항목 | 위치 |
|------|------|
| `store.tagCloud` | `useJournalStore` — `JournalTagCloud { dayTagList, diaryTagList, dreamTagList }` |
| `store.tagCloudLoading` | `useJournalStore` |
| `store.fetchTagCloud()` | 전체 갱신: `GET /api/journal/day/tags`, `GET /api/journal/entry/tags?type=DIARY`, `GET /api/journal/entry/tags?type=DREAM` |
| `store.fetchTagCloud({ sections })` | 부분 갱신: `sections: ["day" \| "diary" \| "dream"]` 에 포함된 태그클라우드 섹션만 조회·반영 |
| `TagCloudItem` | `{ id: number|string, name: string, ctgr?: string, contentSize: number, tagClass?: string, textClass?: string }` |

**태그 크기 클래스**: `.ts-1` ~ `.ts-9` (`src/styles/components/tag.scss`) — `tagClass` 필드로 전달. 빈도 비율로 base를 구한 뒤 프로필 `forceMax`이면 `ts-9`로 고정한다(`TagProfileService.applyVisualSemantic` / `TagCloudSizeSupport`). 색은 `textClass`(시각 의미)로 별도.
orceMax이면 	s-9로 고정한다(TagProfileService.applyVisualSemantic / TagCloudSizeSupport). 색은 	extClass(시각 의미)로 별도.

**가로 정렬 계약**: 일자/일기/꿈 태그 3행의 태그 목록 시작 x좌표는 동일해야 한다. `꿈 태그` 라벨이 한 글자 짧다는 이유로 태그 목록이 왼쪽으로 들어오면 실패다. Vue 구현은 라벨 컬럼에 `.journal-tag-header__label { width: 6.25rem; justify-content: center; }`를 적용해 3행 모두 같은 라벨 폭을 사용한다.

**초기화 타이밍**:
- 월간/주간 parent view가 `store.viewType`과 기간 상태를 먼저 설정한 뒤 `store.fetchTagCloud()` 호출
- `JournalTagCloudHeader`는 mounted 선조회를 하지 않는다. 자식 mounted가 부모 초기화보다 먼저 실행되어 직전 월간 상태로 조회되는 것을 방지한다.
- `[store.yy, store.mnth, store.weekStartDt, store.viewType]` watch → 기간/뷰 변경 시 재호출
- store는 태그클라우드 요청 순서를 섹션별로 추적해, 이전 기간/섹션 요청이 늦게 완료되어도 최신 요청 결과만 반영한다. 부분 갱신 응답은 해당 섹션만 교체하고 다른 섹션의 현재 값을 덮어쓰지 않는다.

**저장 후 부분 갱신 계약**:
- 저널 일자 저장: 일자 태그가 변경될 수 있으므로 `fetchTagCloud({ sections: ["day"] })` 만 호출한다.
- `JOURNAL_DIARY` 엔트리 저장: 일기 태그가 변경될 수 있으므로 `fetchTagCloud({ sections: ["diary"] })` 만 호출한다.
- `JOURNAL_DREAM` 엔트리 저장: 꿈 태그가 변경될 수 있으므로 `fetchTagCloud({ sections: ["dream"] })` 만 호출한다.
- `JOURNAL_NOTE` 엔트리 저장과 저널 챕터 저장은 태그클라우드 직접 변경이 아니므로 태그클라우드를 재조회하지 않는다.

로딩 상태·일자/일기/꿈 태그 행 레이블·태그 메뉴 툴팁은 현재 locale의 클라이언트 카탈로그를 사용한다.

**현재 Vue 동등**: ✓ 구현 완료 (`JournalTagCloudHeader.vue`)

---

### 18-1. `JournalPeriodThreadSummary` (기간별 스레드 요약)

**신규 기능**: 레거시 대응 없음. 월간·주간 태그클라우드 아래에서 현재 조회 기간에 엔트리가 등장한 스레드를 요약한다.

**표시 계약**:
- 주간은 해당 `weekStartDt`의 스레드를 기간 내 최초 엔트리 일자순으로 모두 표시한다.
- 월간은 해당 `yy`/`mnth`의 스레드를 기간 내 엔트리 수 내림차순, 최초 엔트리 일자순으로 표시하며 처음 10개 이후는 펼치기로 노출한다.
- 집계는 일기/꿈 표시·키워드·챕터 필터와 무관한 기간 전체를 대상으로 한다. 현재 필터가 적용된 `store.dayList`를 클라이언트에서 재집계하지 않는다.
- 같은 스레드의 엔트리 수는 전체 소속 수가 아니라 조회 기간 안의 소속 엔트리 수다.
- 스레드가 없는 기간에는 요약 행을 표시하지 않는다. 스레드 선택 시 기존 `thread-detail` route를 연다.

**상태 / API**:
- `GET /api/journal/threads/period-summary`
- 주간 파라미터: `viewType=WEEKLY&weekStartDt=YYYY-MM-DD`
- 월간 파라미터: `viewType=LIST&yy=YYYY&mnth=M`
- 응답 항목: `threadId`, `title`, `entryCount`, `firstEntryDate`
- 기간 집계는 현재 사용자 소유의 활성 스레드·활성 소속·활성 엔트리만 포함한다.

**현재 구현 상태**: ✓ 구현 완료 — 기간 집계 DTO·repository·service와 `GET /api/journal/threads/period-summary`, `useJournalThreadStore`의 기간별 조회 상태·이전 요청 폐기, `JournalPeriodThreadSummary` UI·월간 10개 이후 펼치기를 구현한다. 조회 실패는 빈 기간으로 가장하지 않고 오류 문구를 표시한다. 행 라벨은 기간 문맥이 화면에 있으므로 `스레드`다.

---

### 19. `JournalAsideFilterHeader` (어사이드 필터 카드 헤더)

**Source (Legacy)**: `legacy/static/vue/feature/journal/day/JournalDayAsideFilterHeaderApp.ts`

0**현재 Vue 동등**: ✓ 구현 완료 — `JournalAside.vue` 상단 `#journal_aside_header` + `#sortIcon` + `store.toggleSort()`

**Vue DOM 구조**:
```html
<div id="journal_aside_header" class="card-header min-h-auto mb-5">
    <h3 class="card-title text-gray-900 fw-bold fs-3">
        <i class="bi bi-filter fs-2 me-1"></i> FILTER
    </h3>
    <div class="card-toolbar">
        <a class="btn btn-sm btn-icon btn-color-gray-500 btn-light"
           title="정렬 변경" @click.prevent="toggleSort">
            <i class="bi bi-sort-numeric-up-alt fs-2 pe-0" id="sortIcon"></i>
        </a>
    </div>
</div>
```

**상태 / 액션**:
```typescript
const sortOrder = ref<'ASC' | 'DESC'>('DESC');
function toggleSort() {
    sortOrder.value = sortOrder.value === 'ASC' ? 'DESC' : 'ASC';
    localStorage.setItem('journal_day_sort', sortOrder.value);
    fetchDays();
}
```

**정렬 아이콘**: `DESC` → `bi-sort-numeric-down-alt`, `ASC` → `bi-sort-numeric-up-alt`

**표시 문구 i18n**: 필터 패널 닫기와 정렬 변경 tooltip은 현재 locale의 클라이언트 카탈로그를 사용한다. locale 변경은 패널 표시 상태와 정렬값을 변경하지 않는다.

---

### 20. `JournalAsideYyMnthSection` (어사이드 연월·주간·핀포인트)

**Source (Legacy)**: `legacy/static/vue/feature/journal/day/JournalDayAsideYyMnthApp.ts`

**현재 Vue 동등**: ✓ 구현완료 — 연도 select + 월 그리드/TODAY + 주간 네비게이터 + Pinpoint (`JournalAside.vue`)

**3개 sub-block 요약**:

**Sub-block 1 — 연도 select + 월 그리드 + 이전/다음 화살표**:
- 연도: `<select class="form-select form-select-sm">` — `:value="store.yy"` / `onYyChange` — 2010년~현재 역순 목록
- 월: 1~12 버튼 그리드 — `store.gotoYyMnth(store.yy, m)` 호출. 레거시 `<select id="mnth">` 구조는 쓰지 않는다.
- 이전 월 화살표: `<i class="bi bi-caret-left fs-2">` — `store.navigateMonth(-1)` 호출
- 다음 월 화살표: `<i class="bi bi-caret-right fs-2">` — `store.navigateMonth(1)` 호출
- TODAY 버튼: `btn btn-sm btn-outline btn-light-info blink-slow` — `store.gotoToday()` 호출

**Sub-block 2 — Week 네비게이터** ✓ 구현:
```typescript
// store 또는 로컬 상태로 관리
const weekDays = ref<WeekDayItem[]>([]);
const weekRangeLabel = ref<string>('----');
// WeekDayItem: { label: '월'~'일', dateStr: 'YYYY-MM-DD', hasDay: boolean, isActive: boolean }
// 주범위 라벨: `${weekStartDt.slice(5)} ~ ${weekEndDt.slice(5)}` (MM-DD ~ MM-DD)
```

요일 셀 클릭 시 `store.gotoDay(dateStr)` 또는 `store.fetchDays({ stdrdDt: dateStr })` 호출.

**Sub-block 3 — Pinpoint** ✓ 구현:
```typescript
// useJournalAsideStore — pinnedYy/pinnedMnth + setPinpoint(yy, mnth)
// localStorage journal_day_pinpoint: { yy, mnth } (서버 미저장)
// pinpoint(): asideStore.setPinpoint(store.yy, store.mnth)
// turnback(): store.gotoYyMnth(asideStore.pinnedYy, asideStore.pinnedMnth)
```

HTML 요소:
- 핀 버튼: `<i class="bi bi-bookmarks">` — pinpoint() 호출
- 고정 표시: pinnedYy/pinnedMnth 반응형 표시 — null이면 `----`, `--`
- 돌아가기 버튼: `<i class="bi bi-reply-all">` — turnback() 호출; pinnedYy null이면 disabled

**요일 버튼 색상 수정**: `isActive`는 `dateStr === selectedDt` (선택된 날짜만 파란색). `hasDay=false`이면 `:disabled` → CSS가 회색 처리. 기존 `is-active: day.hasDay` 버그 수정됨.

**표시 문구 i18n**: 월 숫자 뒤 단위, 날짜 선택·Pinpoint 저장·복귀 tooltip과 월~일 요일 축약명은 현재 locale의 클라이언트 카탈로그를 사용한다. locale 변경은 연월·선택 일자·Pinpoint 저장값·route query를 변경하지 않는다.

---

### 21. `JournalAsideEntryFilters` (어사이드 엔트리 필터)

**Source (Legacy)**: `legacy/static/vue/feature/journal/day/JournalDayAsideEntryFiltersApp.ts`

**현재 Vue 동등**: ✓ — `JournalAside.vue` 인라인. 블록 A–C·라이프사이클·어사이드 목록 키워드 구현. 블록 D(고급필터 아코디언)는 **이식 대상 아님** (`vue-screen-overview.md` 필터·검색 정책).

**5개 블록 구조**:

**블록 A — TAGCLOUD 토글**: ✓ 구현
- `store.showTagCloud` — 변경 시 TAGCLOUD ON이면 `store.fetchTagCloud()` 호출

**블록 B — DIARIES + CHAPTER CATEGORIES + 일기 키워드**:
```html
<!-- B-1: DIARIES 토글 -->
<input type="checkbox" id="toggleDiaries" :checked="store.showDiaries" @change="toggleDiaries">
<!-- B-2: CHAPTER CATEGORIES 체크박스 목록 -->
<div id="chapterCtgrFilterSection" class="d-flex flex-column ps-3 gap-1">
    <label v-for="ctgr in chapterCategoryOptions" :key="ctgr.code"
           class="form-check form-check-sm form-check-custom form-check-solid cursor-pointer">
        <input class="form-check-input w-16px h-16px"
               type="checkbox"
               :checked="isChapterCategorySelected(ctgr.code)"
               @change="toggleChapterCategory(ctgr.code)">
        <span class="form-check-label text-muted fs-8">[{{ ctgr.codeName }}]</span>
    </label>
</div>
<!-- B-3: 일기 라이프사이클 선택 -->
<select id="diaryLifecycleFilter" class="form-select form-select-sm" v-model="store.diaryLifecycleKey" @change="store.fetchDays()">
    <option value="">전체</option>
    <option value="OPEN">진행 중</option>
    <option value="PENDING">보류</option>
    <option value="RESOLVED">완료</option>
</select>
<!-- B-4: 일기 키워드 입력 -->
<input id="diaryFilterKeyword" class="form-control form-control-sm" v-model="store.diaryKeyword" @keyup.enter="applyFilters">
```

**챕터 카테고리 데이터**:
- 일기 전용: `GET /api/code/items?groupCode=JOURNAL_CHAPTER_DIARY_CTGR_CD`
- 노트 전용: `GET /api/code/items?groupCode=JOURNAL_CHAPTER_NOTE_CTGR_CD`
- `journalModalStore.prefetchChapterCategories()` — 두 그룹 병렬 조회, 세션 캐시. `JournalMonthly` / `JournalWeekly` / `JournalDaily` onMounted에서 선제 호출해 모달 오픈 시 로딩 없이 사용
- 동시 호출 시 새 요청을 버리지 않고 진행 중인 Promise를 반환한다. Aside와 월간/주간 화면이 같은 시점에 호출해도 호출자는 동일한 조회 완료를 기다린 뒤 옵션을 병합한다.
- `chapterType === "NOTE"` 이면 `chapterNoteCategoryOptions`, 그 외엔 `chapterDiaryCategoryOptions` 사용 (`JournalChapterRegistModal` computed `currentCategoryOptions`)
- `chapterType` 변경 시 `categoryCode` 자동 초기화 (watch)
- DB 마이그레이션: `JOURNAL_CHAPTER_CTGR_CD` → `JOURNAL_CHAPTER_DIARY_CTGR_CD`로 복사 후 기존 그룹 삭제 (`data-required-cd-mariadb.sql`)

**챕터 선택 → store 연동**:
- 체크박스 ON: 해당 코드가 `store.chapterCtgrCds` 에 없으면 추가 → `store.fetchDays()`
- 체크박스 OFF: 해당 코드를 `store.chapterCtgrCds` 에서 제거 → `store.fetchDays()`
- `DIARIES=false` 상태에서는 챕터 카테고리 필터 UI를 렌더링하지 않고 기존 선택값은 보존한다.

**블록 C — DREAMS + 꿈 LIFECYCLE + 꿈 키워드**: 블록 B와 동일 구조, 챕터 카테고리 sub-block 없음
- 꿈 LIFECYCLE select: `store.dreamLifecycleKey` — 변경 시 `store.fetchDays()`

**부모 토글과 하위 필터 계약**:
- `TAGCLOUD`는 엔트리 종류와 독립된 표시 토글이므로 DIARIES/DREAMS 하위에 두지 않는다.
- `CHAPTER CATEGORIES`, 일기 LIFECYCLE, 일기 키워드는 `DIARIES` 토글 하위에 배치한다.
- 꿈 LIFECYCLE, 꿈 키워드는 `DREAMS` 토글 하위에 배치한다.
- 부모 토글이 OFF이면 해당 하위 필터 UI는 렌더링하지 않는다.
- 부모 토글 OFF는 하위 필터 값을 삭제하지 않는다. 다시 ON으로 돌리면 기존 하위 필터 값이 그대로 적용된다.
- `ENTRY FILTER` 레이블은 TAGCLOUD/DIARIES/DREAMS 필터 묶음 아래에 표시한다.

**라이프사이클 필터**:
- 일기 LIFECYCLE select: `store.diaryLifecycleKey`
- 꿈 LIFECYCLE select: `store.dreamLifecycleKey`
- 옵션: 전체(`""`), 진행 중(`OPEN`), 보류(`PENDING`), 완료(`RESOLVED`)
- `OPEN`은 라이프사이클 값이 없거나 `OPEN`인 엔트리를 포함한다.
- 일기/꿈 각각 독립적으로 적용하며, 기존 키워드 필터와 AND 조건으로 동작한다.

**표시 문구 i18n**: 필터 영역의 고정 영문 제목·토글·섹션명, 로딩·카테고리 조회 실패, 라이프사이클 선택지, 일기·꿈 키워드 placeholder/적용 tooltip, 필터 초기화 문구는 현재 locale의 클라이언트 카탈로그를 사용한다. locale 변경은 토글·카테고리·라이프사이클·키워드 필터값과 조회 호출을 변경하지 않는다.

**블록 D — 고급 필터 아코디언 (이식 대상 아님)**:
```html
<div class="accordion accordion-flush" id="journal_day_filter_accordion">
    <div class="accordion-item">
        <h2 class="accordion-header">
            <button class="accordion-button collapsed fw-semibold" type="button"
                    data-bs-toggle="collapse" data-bs-target="#journal_day_filter_advanced">
                고급 필터
            </button>
        </h2>
        <div id="journal_day_filter_advanced" class="accordion-collapse collapse">
            <div class="accordion-body pt-4">
                <!-- 추가 필터 옵션 placeholder -->
            </div>
        </div>
    </div>
</div>
```

레거시 aside accordion 본문은 placeholder뿐이며 Vue 에서 이식하지 않는다. 멀티 키워드·태그 AND 고급 필터는 `JournalEntrySearchPage.vue` 에만 구현한다.

---

### 22. `JournalAsideTodoCard` (어사이드 투두 카드)

**Source (Legacy)**: `legacy/static/vue/feature/journal/day/JournalDayAsideTodoCardApp.ts`

**현재 Vue 동등**: ✓ `JournalAsideTodoCard.vue` — 카드 헤더·월별 목록 조회·삭제·등록 모달 호출 구현

현재 구현된 카드 제목과 할일 등록 버튼 문구는 현재 locale의 클라이언트 카탈로그를 사용하며, locale 변경은 목록·모달에 전달하는 대상 년월을 변경하지 않는다.

**API**:
| 작업 | 엔드포인트 | 응답 |
|------|-----------|------|
| 목록 조회 | `GET /api/journal/todo/list?yy=&mnth=` | `{ rsltList: TodoRow[] }` |
| 삭제 | `DELETE /api/journal/todo/{id}` | `{ rslt: boolean }` |

**TodoRow 모델**:
```typescript
interface TodoRow {
    id: string | number;
    title: string;
}
```

**카드 구조**:
```html
<div class="card card-reset card-p-0">
    <!-- 헤더: 제목 + 추가 버튼 -->
    <div id="journal_todo_aside_header" class="card-header min-h-auto mb-5">
        <h3 class="card-title text-gray-900 fw-bold fs-3">
            <i class="bi bi-list-task fs-2 me-1"></i> TODO List
        </h3>
        <div class="card-toolbar">
            <a class="btn btn-sm btn-icon btn-primary" title="할일 추가" @click.prevent="openTodoReg">
                <i class="bi bi-plus fs-2 pe-0" id="journalTodoAsideRegistIcon"></i>
            </a>
        </div>
    </div>
    <!-- 목록 -->
    <div id="journal_todo_list_div">
        <template v-if="todos.length > 0">
            <div v-for="item in todos" :key="'todo-' + item.id"
                 class="row d-flex-align-center justify-content-between">
                <div class="col text-truncate" data-bs-toggle="tooltip" :title="item.title">
                    {{ item.title }}
                </div>
                <div class="col-3 d-flex justify-content-end">
                    <button class="btn btn-sm btn-light-danger btn-outlined py-2 px-3"
                            title="삭제" @click.prevent="deleteTodo(item.id)">
                        <i class="bi bi-trash p-0"></i>
                    </button>
                </div>
            </div>
        </template>
        <div v-else class="journal-day d-flex-center">할일이 없습니다.</div>
    </div>
</div>
```

**초기화 및 갱신 타이밍**:
- `onMounted` → `fetchTodos({ yy: store.yy, mnth: store.mnth })`
- `watch([() => store.yy, () => store.mnth])` → 연/월 변경 시 재조회
- TODO 등록/삭제 성공 후 `fetchTodos()` 재호출

**TODO 등록 모달 연결**:
```typescript
// modalStore.openTodoReg({ yy: store.yy, mnth: store.mnth }) 호출
// JournalTodoRegistModal.vue가 처리 — 등록 성공 시 todos 목록 갱신 이벤트 emit 필요
```



### 22-1. `JournalDayViewToolbar` (저널 일자 뷰 상단 툴바)

**Vue 구현 완료**: `app/frontend-vue/src/features/journal/day/components/JournalDayViewToolbar.vue`

**레거시 출처**:
- 본문 상단 탭 행: `journal_day_monthly.ftlh` / `journal_day_weekly.ftlh` — `nav-tabs-line` 4탭
- 일자 등록 버튼: `_journal_day_page_header.ftlh` — `@component.header_btn_reg_modal` (`data-journal-day-action=reg-modal` → `JournalDayRuntimeService`)

**사용 화면**: `JournalDayWeekly.vue`, `JournalDayMonthly.vue`, `JournalDayCalendar.vue`, `JournalDayMeta.vue` — 카드(`.card.post`) 위 첫 행

**DOM 구조**:
- sticky flex 행 (`journal-day-view-toolbar`, `justify-content-between`): 좌측 `nav-tabs` (router-link 4개) + 우측 액션 영역 (`d-none d-md-flex pe-5 mt-3`)
- 탭 라벨: `주간 VIEW` / `월간 VIEW` / `달력 VIEW` / `메타 VIEW`
- 등록 버튼: `btn btn-sm btn-light-primary btn-outlined`, 아이콘 `bi-calendar-plus`, 라벨 「저널 일자 등록」
- aside 숨김 시 액션 영역 맨 오른쪽: 구분자 + `bi-layout-sidebar-inset-reverse` 열기 버튼. 일정 툴바와 같은 순서이며 aside가 보이면 렌더하지 않는다.
- 탭·검색 placeholder/tooltip·등록 버튼 문구는 현재 locale의 클라이언트 카탈로그를 사용한다.

**동작**:
| 액션 | Vue |
|------|-----|
| 저널 일자 등록 클릭 | `useJournalModalStore().openDayRegist()` → `JournalDayRegistModal` (`JournalLayout` 마운트) |
| 탭 전환 | `router-link` — `journal-weekly` / `journal-monthly` / `journal-calendar` / `journal-meta` |
| 일기 키워드 검색 | `v-model="localDiaryKw"`, Enter/버튼 클릭 → 일기 전체검색 새 탭 오픈 |
| 꿈 키워드 검색 | `v-model="localDreamKw"`, Enter/버튼 클릭 → 꿈 전체검색 새 탭 오픈 |
| 고급 필터 | `asideStore.toggle()` — 사이드 필터 패널 표시/숨김 |
| 우측 끝 aside 열기 | aside 숨김 시 `asideStore.show()` — 사이드 필터 패널 표시 |
| 일정 등록 / 개인 일정 | 저널 날짜·엔트리 맥락을 전달하지 않는 교차 도메인 액션이므로 저널 툴바에서는 미제공. 일정 화면에서만 제공 |
| 태그 카테고리 동기화 | `syncCategoryMaps()` — 4종 categoryMap 서버 재조회 |

**추가 구현 (2026-05-19, 현재 계약)**: 우측 영역에 일기·꿈 키워드 검색 input + 돋보기 버튼 추가. 툴바는 `localDiaryKw` / `localDreamKw` 로컬 ref를 사용해 새 탭 전체검색을 실행하며, `JournalAside`의 `store.diaryKeyword` / `store.dreamKeyword` 현재 목록 필터와 상태를 공유하지 않는다.

**행동 spec 교차 참조**: `docs/JOURNAL_SCREEN_BEHAVIOR_SPEC.md` §4.4 (상단 등록 버튼)

**레이아웃 전역 툴바 (2026-07 현재 계약)**: `JournalDayViewToolbar.vue` 우측 — 고급필터(사이드 패널 토글), 태그 카테고리 동기화(`syncCategoryMaps`), 저널 일자 등록, aside 열기. P2.3에서 추가됐던 일정 등록·개인 일정(`/schedule?regist=`)은 저널 맥락을 전달하지 않는 중복 진입점이라 제거했으며 일정 화면에서만 제공한다 — `JOURNAL_SCREEN_BEHAVIOR_SPEC.md` §4.1–4.3

**Floating 툴바 (2026-07 현재 계약)**: 공통 툴바는 `position: sticky`로 원래 문서 높이를 유지하면서 스크롤 중 viewport 상단에 머문다. 데스크톱(`min-width: 992px`)은 고정 앱 헤더 높이 `var(--bs-app-header-height, 74px)` 아래에 붙고, 그 미만 화면은 `top: 0`을 사용한다. 불투명 흰 배경·하단 경계로 뒤의 저널 본문과 구분하며 별도 그림자는 사용하지 않는다. `z-index: 90`으로 앱 헤더(`100`) 아래에 놓인다.

**현재 Vue 동등**: ✓ 구현 완료

---

### 22-2. `JournalDayRegistModal` 날짜 선택 (datepicker)

**Vue**: `JournalDayRegistModal.vue` — `#journalDate` 입력

**레거시**: `attachRegFormControls()` → `cF.datepicker.singleDatePicker("#journalDate", "yyyy-MM-DD", obj.journalDate)` (daterangepicker)

**Vue 동등**: `flatpickrSingleDate.ts` — `bindSingleDatePicker()` (flatpickr, `Y-m-d`, locale `ko`). 모달 `shown` 시 부착·`hidden` 시 `destroy`. 캘린더 아이콘 클릭 → `open()`. 모달 내부 입력도 calendar DOM은 기본 body append를 사용한다. `.modal-body`에 append하면 입력칸 좌표와 calendar 좌표 기준이 달라져 위치가 틀어질 수 있다.

**신규 등록 기본값**: `openDayRegist()` 에서 `journalDate` 비어 있으면 오늘(`formatLocalDateStr`) — 레거시 daterangepicker `startDate: moment()` 와 동일.

**저장 후 갱신**: `JournalDayRegistModal.vue` 는 저장 성공 시 모달을 닫고 성공 알림을 표시한 뒤, 사용자가 OK를 누르면 현재 route 기준으로 목록을 갱신한다. `/journal/weekly` 에서는 `setViewType("WEEKLY")` 후 `fetchDays({ viewType: "WEEKLY" })`, `/journal/monthly` 에서는 `setViewType("LIST")` 후 `fetchDays({ viewType: "LIST" })` 를 호출해 주간 등록 완료 후 월간 목록으로 돌아가지 않게 한다. 일자 태그 변경이 일자 태그클라우드에 반영되도록 OK 이후 `fetchTagCloud({ sections: ["day"] })`도 함께 호출한다 (2026-05-19 수정, 2026-06-02 OK 이후 갱신, 2026-06-03 부분 갱신).

**수정 정보 조회 실패**: 수정용 일자 정보 조회가 실패하면 불완전한 폼을 열지 않고, 서버 메시지를 우선 표시하며 없으면 현재 locale의 fallback을 사용한다.

**중복 등록 정책**: 신규 등록 시 같은 사용자에게 같은 `journalDate`의 활성 저널 일자가 이미 있으면 등록을 실패 처리한다. 과거처럼 기존 일자를 수정으로 전환하지 않는다 (2026-05-20 수정).

**닫기 정책**: 등록/수정 중 백드롭(모달 바깥)을 클릭하거나 Escape를 눌러도 모달을 닫지 않는다. 사용자가 입력 중인 날짜/태그/메타 값이 의도치 않은 닫힘으로 클리어되지 않도록 `Bootstrap Modal`을 `backdrop: "static", keyboard: false`로 생성한다 (2026-05-21 수정).

**표시 문구 i18n**: 모달 제목·안전 닫기 안내·날짜·날짜 정확도 선택지·날씨·일기/꿈 완료 여부·태그·메타 안내·저장·닫기 문구는 현재 locale의 클라이언트 카탈로그를 사용한다. locale 변경은 입력값과 저장 payload를 변경하지 않는다.

**축별 완결 (`diaryResolvedYn` / `dreamResolvedYn`)**: 모달에 일기·꿈 완료 스위치를 두고 FormData로 저장한다. 변경 전 `diaryResolvedYn` 은 UI만 있고 백엔드에 저장되지 않았다. 변경 후 두 플래그가 `journal_day` 에 저장되며, Y 인 축의 쓰기(챕터/엔트리/해석/댓글/관련/lifecycle/state)를 잠근다. 날씨·태그·메타·완결 해제와 읽기·복사는 허용한다. NOTE는 일기 축에 포함한다.

**저장 메시지 i18n**: 날짜 필수 검증·등록/수정 확인·성공/실패 fallback은 현재 locale의 클라이언트 카탈로그를 사용한다. 저장 API 응답에 `message`가 있으면 서버 메시지를 우선 표시하며, 성공 알림 확인 후 기존 현재 route 재조회·태그클라우드 갱신·일자 스크롤 순서를 유지한다.

**현재 Vue 동등**: ✓ 구현 완료

---

### 22-3. `JournalDayRegistModal` 태그/메타 Tagify

**Vue**: `JournalDayRegistModal.vue` → 공통 `TagifyEditor.vue`

**초기값 로딩**: 수정 모달에서 서버 상세의 `tag.tagListStrWithCtgr` / `meta.metaListStr` 를 `Tagify.loadOriginalValues()` 로 반영한다. 이때 기존 값 로딩은 사용자 신규 태그 추가가 아니므로 카테고리 입력 프롬프트를 열지 않는다.

**저장 직전 처리**: 메타 입력 중인 draft는 `commitPendingDraft()` 로 확정 후 `tag.tagListStr`, `meta.metaListStr` 를 multipart form data에 포함한다. 태그 draft는 카테고리 선택/입력 단계가 모호할 수 있으므로 자동 확정하지 않는다.

**메타 단위 안내**: `JournalDayRegistModal` 의 메타 라벨 옆 도움말 아이콘은 값 뒤에 단위를 붙이는 입력법(예: 30분, 2회, 5점, 72kg)을 안내한다. `TagifyEditor` 의 `metaMode` 메타 값 입력 placeholder는 대표 예시를 표시한다.

**draft 정리**: 수정 모달 초기값 재로딩 또는 모달 닫기 시 남아 있는 Tagify draft 입력 UI는 취소한다.

**현재 Vue 동등**: ✓ 구현 완료

---

### 22-3-1. 작성 폼 모달 닫기 정책

**대상**: 저널 일자/챕터/엔트리/해석/할일/댓글/관련 컨텐츠, 저널 연간/연간 리뷰, 저널 스레드 등록·수정 모달.

**정책**: 작성 중 내용 유실을 막기 위해 등록/수정 성격의 모달은 백드롭 클릭 및 Escape 키로 닫히지 않는다. 명시적인 닫기 버튼도 레거시 `modal_btn_close_safe`와 동일하게 1회 클릭 시 확인 상태로 전환하고, 2초 안에 한 번 더 클릭해야 닫는다. 저장 성공 흐름은 즉시 닫는다.

**구현**: 각 Vue 모달의 루트에 `data-bs-backdrop="static"` / `data-bs-keyboard="false"`를 선언하고, `new Modal(...)` 생성 옵션에도 `{ backdrop: "static", keyboard: false }`를 지정한다. 닫기 버튼은 공통 `useSafeModalClose()`를 사용해 헤더 X 버튼과 푸터 닫기/취소 버튼 모두 동일한 2회 클릭 정책을 적용한다.

---

### 22-4b. `JournalEntryViewModal` 읽기 전용 원문

**Vue**: `JournalEntryViewModal.vue` → `journal_entry_view_modal`

채팅 RAG 출처 딥링크용 읽기 전용 모달이다. `GET /api/journal/entry/{id}`로 조회한 `markdownContent`를 목록과 동일한 `journal-content` HTML로 표시하고, 날짜·제목·태그·꿈꾼(해당 시)만 보여 준다. 저장 폼은 없다. footer 편집은 `openEntryModifyFromView`로 `JournalEntryRegistModal`을 연다. 비팝업 라우트에서는 `App.vue`가 전역 마운트한다.

### 22-4. `JournalEntryRegistModal` 등록/수정 폼 여백

**Vue**: `JournalEntryRegistModal.vue` → `journal-entry-regist-modal__body`, `journal-entry-regist-form`

**스타일**: `journal.scss` 에서 데스크톱 좌우 `3rem`, 모바일 좌우 `1.25rem` padding을 적용한다. 제목/본문/태그 입력이 모달 가장자리에 붙어 보이지 않게 하며, TinyMCE iframe 본문은 `RichEditor.vue` 의 `content_style` 로 `12px 16px` 내부 padding을 둔다.

**중복 제출 방지**: 저장 클릭 즉시 `submitting` guard를 세운 뒤 확인창을 띄운다. 확인창이 떠 있는 동안 재클릭되어도 두 번째 `submit()`은 무시되어 같은 꿈/일기 엔트리가 두 번 POST되지 않는다. 꿈 등록 모달 오픈도 `dreamEntryRegistOpening` guard로 `dream-auto` 요청 중복을 막는다.

**본문 문단 저장 기준**: `RichEditor`가 전송한 TinyMCE HTML은 서버 저장 정규화(`MarkdownUtils.normalize`)를 거친다. 단일 `<p>` 안에 직접 자식 `<br>`로 문단이 나뉜 본문은 저장 시 별도 `<p>` 문단으로 분리해 레거시처럼 문단 단위 여백을 유지한다.

**저장 후 위치 복귀**: 저장 성공 시 `JournalEntryRegistModal`은 모달을 닫고 성공 알림을 표시한다. 월간/주간 기본 목록은 성공 알림 OK 이후 현재 route 기준 목록을 갱신하되 강제 스크롤하지 않고, 기존 목록 DOM 유지로 브라우저 스크롤 위치 보존에 맡긴다. 일자 상세 팝업이 열려 있으면 OK 이후 `JournalDayDetailModal` 데이터를 다시 조회하고, 팝업 내부의 `[data-id]` 엔트리 위치로 스크롤한다. 검색 팝업은 `prepare-success` 이벤트에서 결과 DOM을 준비하고, OK 이후 `success` 이벤트 payload를 받아 `#journal-entry-search-{id}`로 스크롤한다. 스레드 상세에서는 일자 목록 대신 열린 스레드의 상세 본문·집계 태그·소속 엔트리를 다시 조회하며 모달 내부 맥락을 유지한다. 태그클라우드는 엔트리 타입별로 필요한 섹션만 갱신한다: `JOURNAL_DIARY`는 `fetchTagCloud({ sections: ["diary"] })`, `JOURNAL_DREAM`은 `fetchTagCloud({ sections: ["dream"] })`, `JOURNAL_NOTE`는 태그클라우드를 갱신하지 않는다.

**챕터 선택 옵션**: 엔트리 신규/수정 모달은 `journalDayId`가 있으면 `GET /api/journal/day/{journalDayId}`를 추가 조회해 해당 일자의 챕터 목록을 `chapterList` 옵션으로 채운다. 엔트리 상세 DTO에는 `chapterList`가 없을 수 있으므로, 수정 모달은 상세 응답의 `entry.chapterList`에 의존하지 않는다. `JOURNAL_DREAM`→`DREAM` 챕터만, `JOURNAL_NOTE`→`NOTE` 챕터만 후보로 쓴다. **NOTE 챕터 엔트리의 `contentType`은 `JOURNAL_DIARY`**이므로, 후보 `chapterType`은 `contentType`이 아니라 `journalChapterId`가 가리키는 챕터(또는 신규 시 caller 가 넘긴 챕터)의 `chapterType`으로 분기한다 — 노트끼리·일기끼리만 이동한다. NOTE 챕터 신규 등록(`JournalChapterItem.openEntryNew`)도 `JOURNAL_DIARY`를 전송한다. 현재 선택값이 없거나 후보에 없으면 첫 번째 후보를 선택한다(이때 `console.warn`).

**등록·수정 정보 조회 실패**: 꿈 등록 정보 또는 엔트리 수정 정보 조회가 실패하면 불완전한 폼을 닫고, 서버 메시지를 우선 표시하며 없으면 현재 locale의 fallback을 사용한다.

**i18n**: 일기·꿈·노트·기본 엔트리별 모달 제목, 기준 날짜 요일, 필드 레이블, 글자 수 안내, 카테고리·제목·순서·꿈꾼 placeholder, 저장·닫기·확인·결과 fallback은 현재 locale의 클라이언트 카탈로그를 사용한다. 저장 API 응답에 `message`가 있으면 서버 메시지를 우선 표시한다. locale 변경은 표시 문구만 바꾸며 챕터 선택·태그·꿈꾼 값과 저장 payload는 유지한다.

**현재 Vue 동등**: ✓ 구현 완료

---

### 22-5. `JournalTodoRegistModal` 등록/수정 폼

**Vue 구현**: `app/frontend-vue/src/features/journal/todo/modals/JournalTodoRegistModal.vue`

**데이터·동작**: `useJournalModalStore.todoRegistModel`의 대상 년월·카테고리·제목·순서·본문·태그를 편집한다. 신규는 `POST /api/journal/todos`, 수정은 `POST /api/journal/todo/{id}`로 multipart form data를 전송한다. 성공 시 모달을 닫고 성공 알림 확인 후 `refreshJournalDaysForRoute()`로 현재 route 기준 저널 목록을 갱신한다.

**i18n**: 모달 제목·대상 년월·필드·placeholder·안내·저장·닫기·제목 필수 검증·확인·결과 fallback은 현재 locale의 클라이언트 카탈로그를 사용한다. API 응답에 `message`가 있으면 서버 메시지를 우선 표시한다.

**현재 Vue 동등**: ✓ 구현 완료

---

### 22-6. `JournalInterpretationRegistModal` 등록/수정 폼

**Vue 구현**: `app/frontend-vue/src/features/journal/interpretation/modals/JournalInterpretationRegistModal.vue`

**데이터·동작**: `useJournalModalStore.interpretationRegistModel`의 참조 대상·날짜·카테고리·제목·순서·본문을 편집한다. 제목은 선택값이다. 신규는 `POST /api/journal/interpretations`, 수정은 `POST /api/journal/interpretation/{id}`로 multipart form data를 전송한다. 성공 시 모달을 닫고 성공 알림 확인 후 `refreshJournalEntryHostForRoute()`로 현재 표시 호스트를 갱신한다. 스레드 상세이면 원본 엔트리를 포함한 열린 상세를 재조회하고, 다른 저널 화면이면 기존 route 기준 저널 목록을 갱신한다.

**i18n**: 모달 제목·기준 날짜 요일·필드·placeholder·안내·저장·닫기·확인·결과 fallback은 현재 locale의 클라이언트 카탈로그를 사용한다. API 응답에 `message`가 있으면 서버 메시지를 우선 표시한다.

**현재 Vue 동등**: ✓ 구현 완료

---

### 23. `JournalDayCard` (저널 일자 카드 — 목록 단위)

**Vue 구현**: `app/frontend-vue/src/features/journal/day/components/JournalDayCard.vue`

**사용 화면**: `JournalDayMonthly.vue`, `JournalDayWeekly.vue`, `JournalDayDaily.vue` — `v-for="day in store.dayList"`

**하위 컴포넌트**: `JournalChapterItem.vue`, `JournalEntryItem.vue`

**데이터**: `JournalDayDto` (`features/journal/stores/journal.ts`) — `journalChapterList`, `journalDreamSectionList`, `tag`, `meta` 등

**꿈 렌더링 분리 (Phase 1 가상 섹션)**: 백엔드 `JournalEntryViewProjectionHelper.applyDayEntryProjections()` 가 DREAM 챕터 꿈을 `journalDreamSectionList`(`JournalDreamSectionDto`: `sectionKey`, `title`, `dreamerName`, `entries`) 로 내려준다. 내 꿈=`own`/「꿈」, 지정 꿈꾼=`dreamer:{이름}`/「{이름} 꿈」(동일 철자=한 섹션). 분류·묶음 SSOT는 `JournalDreamSectionHelper`·`JournalDreamerFieldHelper` 이다. `title`은 서버가 요청 locale의 `common.dream`·`journal.dream.section.named` 메시지로 조립한다. Vue는 `journalDreamSectionList` 를 `JournalDreamVirtualSection.vue` 로만 렌더(프론트 재묶음 없음). 지정 꿈꾼 섹션에는 저널 꿈 등록 버튼 없음(내 꿈 `own` 만). 엔트리 본문에 꿈꾼 이름 배지 없음(섹션 헤더로 구분). 등록·복사·TXT 액션 문구는 현재 locale의 클라이언트 카탈로그를 사용한다. `JournalEntryRegistModal.vue` 에 비필수 `elseDreamerNm` 입력.

**모달 연동**: `useJournalModalStore` — 일자/챕터/엔트리 등록·상세·수정

**현재 Vue 동등**: ✓ 구현 완료 (레거시 `JournalDayMonthlyListApp` / 텔레포트 대체)

**컨텍스트 메뉴**: ✓ 구현 완료 — 레거시 `JournalDayContextMenu.ts` → `JournalDayCard.vue` 내 Metronic ⋯ dropdown 흡수. 「주간 뷰로 이동」은 route `journal-weekly` 에서 미표시(월간·캘린더·메타 등 전용).

**i18n**: 기준 날짜 요일·날짜 정확도 배지·일기/꿈 완결 배지·챕터 필터 안내, 챕터·꿈 등록, 메뉴·저널 일자 헤더, 주간 뷰 이동·일자 뷰 새 창, 수정·상태·중요·일기/꿈 완결·접힘·삭제, 메타 tooltip, 숨겨진 카테고리·꿈 숨김 문구는 현재 locale의 클라이언트 카탈로그를 사용한다. locale 변경은 route·팝업·상태·필터 데이터와 액션 호출을 변경하지 않는다.

**액션 결과 i18n**: 일자 삭제 확인·성공·실패와 꿈 섹션 클립보드 복사 성공·실패 fallback 및 복사 날짜 헤더의 요일은 현재 locale의 클라이언트 카탈로그를 사용한다. 삭제 API 응답에 `message`가 있으면 서버 메시지를 우선 표시하고, 성공 알림 확인 후 현재 route 기준 목록 재조회와 일자 스크롤을 유지한다.

**일간 뷰 새 창 열기**: ✓ 구현 완료 — 컨텍스트 메뉴 "새 창으로 열기 (일자 뷰)" → `window.open(BASE_URL + /journal/daily?stdrdDt=..., "_blank", "width=...,height=...")` 새 창 강제 (features 지정)

**사용자 휴가 표시**: ✓ 구현 완료 — `JournalDayVacationIndicator.vue`가 서버 확정 `vacationDayStatus`와 `vacationReasonList`를 월간·주간·일간 카드, 일자 상세, 메타 일자 목록에 동일하게 표시한다. `FULL_DAY`는 공휴일·주말과 같은 날짜 빨강 + 휴가 배지, `AM_HALF`·`PM_HALF`는 날짜 색 유지 + 구분 배지, `UNKNOWN`은 경고 배지다. 프론트는 제목이나 사유로 휴가 상태를 추정하지 않는다.

---

### 23-1. `JournalChapterItem` (저널 챕터 아이템)

**Vue 구현**: `app/frontend-vue/src/features/journal/chapter/components/JournalChapterItem.vue`

**scss 클래스 바인딩**:
- root div: `class="journal-chapter-block"` + `:id="'journal-chapter-' + chapter.id"` — 챕터 등록/수정 후 저장 위치 스크롤 앵커
- 외부 div: `class="journal-chapter-item"` + `:data-collapsed` — journal.scss `:has(.collapsed)` 선택자 연동
- 콘텐츠 div: `:class="['journal-chapter-content', { 'collapsed': isCollapsed }]"` — `&.collapsed > * { display: none !important }` CSS 연동
- 접힘 외곽 상태선: `journal.scss` — 하위 `data-imprtc`/`data-refrnc`/전체 `data-resolved` 집계. 펼침 엔트리 `::before` 이중·삼중선과 동일 inset(완료·중요·참조 조합)
- 접힘 요약: 챕터가 접히면 태그와 함께 하위 엔트리의 `threadList`를 `threadId`로 중복 제거한 스레드 버튼을 `.journal-chapter-content` 바깥에 표시한다. 스레드 순서는 엔트리·소속 목록의 최초 등장 순서를 보존하고, 버튼 클릭은 기존 엔트리 스레드 칩과 동일한 `thread-detail` route로 이동한다.

**챕터 태그 표시 규칙**: 챕터 태그는 하위 엔트리 태그를 집계한 요약이므로 **챕터가 접힌 상태일 때만 표시**. 챕터는 **자체 태그를 소유하지 않는다** — 엔티티 `TagEmbed` 를 제거했고(tag_content 0행, vestigial), 화면 태그는 100% `JournalDayViewHelper.applyChapterTagSummary` 가 소속 diary 엔트리 태그를 집계해 `JournalChapterDto.tag` 에 채운 것이다. 저장 경로는 태그를 영구화하지 않는다.
- `v-if="tagList.length > 0 && isCollapsed"` — 접힌 상태에서만 DOM에 마운트
- CSS `d-flex` 와 충돌하므로 CSS 규칙 단독 의존 불가; `v-if` 조건으로 직접 제어

**챕터 등록 모달 (`JournalChapterRegistModal.vue`)**: 제목(`title`)은 필수 항목이 아님.
- 레거시에서 제목 없이 등록 가능 — `title` 빈 값 허용.
- `submit()` 에서 title 공백 검증 제거.
- 기준 날짜 요일과 제목·필드·선택지·버튼·확인·결과 fallback 문구는 현재 locale의 클라이언트 카탈로그를 사용하고, 등록·수정·일자 이동 API가 서버 `message`를 반환하면 그 값을 우선 표시한다.

**소유권 표시·쓰기 제한** (`isCreatedBy` — 백엔드 `BaseAuditRegDto` 직렬화):
- `isCreatedBy === false`: 헤더에 `타인 작성` 배지, 수정·삭제·엔트리 등록·⋯ 메뉴·서버 접힘 변경 버튼 숨김 (읽기·클라이언트 접힘·복사·TXT는 유지)
- 수정 API 거부 시 메시지 `msg.rslt.not-owner` → 「본인이 작성한 글이 아닙니다.」(HTTP 403)

**헤더 액션 버튼** (우측 `col-3` 영역, `canManageChapter` 일 때만, 레거시 `JournalChapterItem.ts` 동형):
- 엔트리 등록 TEXT 버튼: 타입별 `저널 일기 등록` / `저널 꿈 등록` / `저널 노트 등록` + `bi-book` / `bi-moon-stars` — `openEntryNew()`
- 복사 버튼 (`bi-copy`): `copyChapter()` — 날짜·카테고리·제목 + 하위 엔트리 전체를 줄바꿈 연결 텍스트로 클립보드 복사
- TXT보내기 버튼 (`fas fa-download`, `btn-outline btn-light-primary`): `exportChapter()` — `GET /api/journal/chapter/{id}/export`
- ⋯ 컨텍스트 메뉴: 수정(`openChapterModify`) / 상태(접힘 서버 토글 `toggleCollapsedState`) / 삭제
- 접힘 화살표 버튼 (`toggle-chapter-btn`): `toggleChapter()` — 클라이언트만 접힘(`localCollapsedOverride`), 서버 POST 없음
- 챕터 유형·소유권 배지·타입별 등록 버튼·액션 툴팁·메뉴·빈 상태 문구는 현재 locale의 클라이언트 카탈로그를 사용한다.
- 소유권 경고·삭제 확인·삭제 결과 fallback·클립보드 복사 결과와 복사 날짜 헤더의 요일은 현재 locale의 클라이언트 카탈로그를 사용하며, 삭제 API가 서버 `message`를 반환하면 그 값을 우선 표시한다.

---

### 23-2. `JournalEntryItem` (저널 엔트리 아이템)

**Vue 구현**: `app/frontend-vue/src/features/journal/entry/components/JournalEntryItem.vue`

**레거시 출처**: `legacy/static/vue/feature/journal/entry/components/JournalEntryItem.ts`

**scss 클래스 바인딩**:
- 외부 div: `itemClass` computed (contentType/isDream 기준) → `journal-dream-item` / `journal-note-item` / `journal-diary-item`
- 콘텐츠 div: `contentClass` computed → `journal-dream-content` / `journal-note-content` / `journal-diary-content`
- data 속성: `:data-imprtc`, `:data-refrnc`, `:data-resolved`, `:data-else-dream`, `:data-id` — journal.scss CSS 선택자 연동. 타인 꿈(`hasDreamerName` → `data-else-dream="Y"`)은 일반 꿈의 RESOLVED 보라 1줄 대신 **회색 이중선**(slate 0/2px) 기본; RESOLVED·중요·참조는 그 뒤 4px·6px·8px inset 에 완료=보라·중요=빨강·참조=노랑으로 추가(`$journal-else-dream-paired-states`)

**꿈 RESOLVED 팔레트**: 변경 전에는 일기와 꿈이 `$journal-paired-states`의 초록 완료색을 공유했다. 변경 후 일반 꿈은 `$journal-dream-paired-states`의 은은한 보라 배경·테두리·좌측선을 사용하고, 접힌 표시·순번·라이프사이클 메뉴의 선택된 완료 라벨도 보라색으로 맞춘다. 중요·참조가 함께 있으면 완료 보라선을 유지하면서 기존 빨강·노랑 상태선을 조합한다. 일기·노트·해석과 챕터의 완료 집계는 기존 초록 계약을 유지한다.

**엔트리 제목 표시**: `entry.title` 이 있으면 유형(일기/꿈/노트) 무관하게 꿈 상태 배지 행 아래·마크다운 본문 위에 독립 행으로 `fw-bold fs-5 mb-1` 표시한다. 본문 `.journal-content` 는 fs 클래스 없이 base 1rem 을 상속하므로 제목은 본문보다 한 단계 위 크기가 된다. 접힘(`isCollapsed`)과 무관하게 항상 표시하며 본문만 숨긴다. 제목은 `.journal-content` 밖이라 유형별 본문 색상(꿈 보라 등)을 상속하지 않고 기본 텍스트색을 쓴다. 변경 전: 꿈 엔트리에서만 배지 행에 인라인 `fs-7` 로 표시했고 일기·노트는 제목이 렌더되지 않았다. 결산 상세(`JournalAnnualDetail.vue`)의 엔트리 제목도 같은 `fs-5 fw-bold` 계약을 따른다.

**축별 완결 잠금(엔트리 DTO)**: 일자 provide 가 없는 검색·뷰 모달 등에서는 `JournalEntryDto.diaryResolvedYn`/`dreamResolvedYn`(백엔드 day projection)이 SSOT 이다. `JournalEntryItem` 은 `mergeDayResolvedAxis` 로 parent provide 와 병합해 `axisWritable`·관련글 해제·이력 `writeLocked` 를 결정하고 하위 `JournalInterpretationItem` 에 provide 한다. `HistoryModal` 은 `historyWriteLocked` 시 복원·삭제 UI만 숨기고 복사·상세는 허용한다.

**우측 액션 영역**: 댓글 버튼(단독) + 복사 버튼(`bi-copy`, `copyEntry()`) + ⋯ 컨텍스트 메뉴 (수정/이력/관련글/스레드에 추가/라이프사이클/상태/삭제) — 「FLOW 연결」·「FLOW 보기」는 제거되고 스레드 소속 「스레드에 추가」로 수렴했다(나-2b·나-2c)

**스레드·FLOW 액션 경계 (수렴 완료)**: FLOW 를 저널 스레드 소속으로 수렴 완료했다(근거·단계는 `docs/spec/DESIGN_NOTES.md`). **변경 후 현황** — 「흐름 보기」 종단 보기(타임라인 모달 `JournalEntryFlowModal`)와 본문 FLOW 요약 행은 **제거(❌)**됐다. 「흐름 연결」(`openRelatedFlow`, `RelatedContentAddModal` FLOW 고정 모드)도 **제거(❌)**됐다(나-2b). `RelatedContentAddModal` 은 이제 일반 관련 글(RELATED) 전용이다 — 모드 분기·FLOW 안내·FLOW 관계 유형 옵션이 사라졌다. 백엔드 `flowSummary` 계산·`RelationType.FLOW` enum·`related_content` FLOW 행도 모두 **제거(❌)**됐다(다-2). 새 축인 스레드 소속은 `JournalEntryDto.threadList` 로 엔트리에 실리고, 소속 지정 UI 도 **구현 완료(✓)**다(나-2c) — ⋯ 메뉴의 「스레드에 추가」 hover 서브메뉴(소속 토글 + 「새 스레드로 시작」)와 본문 소속 스레드 칩(클릭 시 스레드 상세 이동). 서브메뉴도 전용 후보 API로 **전환 완료(✓)**했다. 280px 서브메뉴 안에서 제목 검색(250ms debounce)·분류 선택을 제공하고, 엔트리별 후보 재조회·요청 경합 폐기·소속 변경 후 재조회·오류/정상 빈 결과 분리를 `journalThreadMembership.ts`가 담당한다.

**관련 글 대상 유형 기본값**: 모달을 열 때 대상 콘텐츠 유형은 **출발 엔트리와 같은 유형**을 기본 선택한다(일기에서 열면 일기, 꿈에서 열면 꿈). 변경 전에는 반대 유형(일기 → 꿈)을 기본값으로 잡아, 같은 성격의 기록을 잇는 경우 매번 되돌려야 했다. 자기 자신(같은 유형 + 같은 id) 연결은 저장 시 self 검증이 막는다. 대상 유형 select 는 일기/꿈만 제공하므로 그 밖의 출발 유형(노트 등)은 일기로 떨어뜨린다 — 빈 선택으로 두면 저장 시 검증에 걸려 원인을 알기 어렵다. `RelatedContentAddModal` 은 FLOW 모드가 제거돼(나-2b) 일반 관련 글(RELATED) 전용이며, 초기화 경로(`openRelatedWithMode`)도 RELATED 단일 모드로 남았다.

**관련 글 대상 검색**: `RelatedContentAddModal`은 선택한 대상 유형만 통합 `GET /api/journal/entries?type=DIARY|DREAM`에서 검색한다. 키워드는 제목 또는 본문에 일치하며, 복수 키워드가 전달되면 키워드 사이는 AND다. 요청 실패는 인라인 오류로 표시해 정상 0건과 구분한다.

**관련 관계 행**: 변경 전에는 일반 관련글과 FLOW 직접 간선을 모두 `RelatedContentDto`별 행으로 표시해 하나의 연결 컴포넌트가 여러 FLOW ID 행처럼 보였다. 변경 후 일반 관련글만 `relationType/reason/targetId/targetContentType/targetTitle`별 행을 유지하고, 대상 제목은 `openEntryView(targetId)`로 읽기 전용 원문을 열며 관계 ID로 해제한다. (변경 후) FLOW 본문 요약 행과 「흐름 보기」는 제거됐다. 백엔드 `flowSummary` 필드도 다-2 에서 제거됐다(어느 화면도 표시하지 않았다).

**i18n**: 펼침/접힘, 보류 badge·접힌 상태 문구, 꿈 상태 배지, 액션 툴팁, 일기·꿈 메뉴 헤더, 수정·해석 등록·이력·관련 글 추가·스레드에 추가·후보 검색·분류·빈 상태·오류·관련글 열기/해제, 라이프사이클·상태·삭제 메뉴와 각 옵션 라벨은 현재 locale의 클라이언트 카탈로그를 사용한다. locale 변경은 엔트리 상태·태그·댓글·관련글 데이터와 액션 호출을 변경하지 않는다.

**액션 결과 i18n**: 클립보드 복사 성공·실패와 복사 날짜 헤더의 요일, 라이프사이클·상태 변경 실패, 관련 관계 연결·해제와 삭제 확인·성공·실패 fallback은 현재 locale의 클라이언트 카탈로그를 사용한다. API 응답에 `message`가 있으면 서버 메시지를 우선 표시한다. 관계 해제·변경·삭제 성공 후 스레드 상세이면 상세 본문·집계 태그·소속 엔트리를 재조회하고, 그 밖의 route에서는 기존 목록 재조회와 일자 스크롤을 유지한다.

**엔트리 복사 형식**: 날짜(`stdrdDt (요일)`) + 공통 `htmlToPlainText(content)` (TinyMCE HTML을 브라우저 HTML 파서로 이름·10진수·16진수 엔티티 디코딩 후 평문 변환, 마크다운 재처리 이전 원문 기준). `content` 없을 때 `markdownContent` 폴백. (`#sortOrder` 없음 — 레거시 `copy()` 동일)

**본문 색상·목록 간격 계약**: `journal.scss` 는 `journal-diary-content` / `journal-dream-content` / `journal-interpretation-content` 의 `.journal-content`에 본문 색상을 지정하고, `v-html` 내부 `p`와 `li`는 해당 색상을 상속한다. 목록(`<ul>/<ol>/<li>`) 본문이 브라우저 기본 검정색으로 튀거나, 목록 위쪽은 붙고 아래쪽만 기본 margin이 남아 비대칭으로 보이면 실패다.

### 23-2a. `JournalInterpretationItem` (저널 해석 아이템)

**Vue 구현**: `app/frontend-vue/src/features/journal/interpretation/components/JournalInterpretationItem.vue`

**데이터·표시**: `InterpretationItem`의 순번·제목·본문·댓글·라이프사이클·상태를 표시한다. RESOLVED 또는 COLLAPSED 상태는 접힌 렌더를 사용하며, `localCollapsedOverride`는 서버 상태를 변경하지 않고 현재 화면에서만 펼침/접힘을 전환한다.

**액션 UI**: 댓글 등록·복사 버튼과 수정·이력·라이프사이클·상태·삭제 컨텍스트 메뉴를 제공한다. 펼침/접힘·접힌 상태·툴팁·메뉴·라이프사이클 및 상태 라벨은 현재 locale의 클라이언트 카탈로그를 사용한다.

**액션 결과 i18n**: 라이프사이클·상태 변경 실패, 클립보드 복사 성공·실패와 복사 날짜 헤더의 요일, 삭제 확인·성공·실패 fallback은 현재 locale의 클라이언트 카탈로그를 사용한다. API 응답에 `message`가 있으면 서버 메시지를 우선 표시한다. 변경·삭제 성공 후 스레드 상세이면 원본 엔트리를 포함한 열린 상세를 재조회하고, 그 밖의 route에서는 `refreshJournalDaysForRoute()`로 저널 목록을 갱신한다.

**현재 Vue 동등**: ✓ 구현 완료

---

### 23-3. `JournalTagContextMenu` (태그 클릭 컨텍스트 메뉴)

**Vue 구현**: `app/frontend-vue/src/features/journal/shared/components/JournalTagContextMenu.vue`

**Pinia 스토어**: `app/frontend-vue/src/features/journal/stores/tagContextMenu.ts`

**레거시 출처**: `legacy/static/vue/feature/journal/day/services/journalDayTagContextMenuShell.ts`

**동작**: 일자/일기/꿈 태그 클릭 시 즉시 검색하지 않고, legacy와 같이 2단계 액션 메뉴를 연다. payload는 `{ tagId, name, ctgr, contentType }`이며 viewport clamp 후 fixed 위치에 표시한다.

**메뉴 액션**:
| 액션 | `JOURNAL_DAY` | `JOURNAL_DIARY` | `JOURNAL_DREAM` |
|------|---------------|-----------------|-----------------|
| 검색 | `journalModalStore.openTagDetail(tagId, name)` | 새 창 `/vue-app/journal/entry/search?type=DIARY&tagIds=...&tagName=...` | 새 창 `/vue-app/journal/entry/search?type=DREAM&tagIds=...&tagName=...` |
| 태그 설정 | `/api/tags/{tagId}/profile?contentType=JOURNAL_DAY` 조회 후 태그 프로필 모달 | `/api/tags/{tagId}/profile?contentType=JOURNAL_DIARY` 조회 후 태그 프로필 모달 | `/api/tags/{tagId}/profile?contentType=JOURNAL_DREAM` 조회 후 태그 프로필 모달 |

메뉴 액션과 태그 프로필의 콘텐츠 유형 레이블은 현재 locale의 클라이언트 카탈로그를 사용한다.

**태그 프로필·일자 필터 모달 마운트 계약**: `JournalTagContextMenu` 의 `프로필`은 `attachableStore.openTagProfile()` 로, `JOURNAL_DAY` `검색`은 `journalModalStore.openDayFilterModal(...)` 로 **상태만** 켠다. 따라서 그 상태를 구독해 실제로 렌더하는 `JournalTagProfileModal`·`JournalDayMetaModal` 이 **같은 화면에 함께 마운트돼 있어야** 화면이 열린다. 컨텍스트 메뉴를 마운트하는 화면은 짝 모달도 반드시 마운트한다 — `JournalDayLayout`·`JournalDayDailyLayout`은 둘 다, `JournalEntrySearchPage`는 프로필만(일자 태그 검색 없음), `JournalAnnualLayout`은 둘 다. 결산에서 프로필·일자 태그 검색이 무반응처럼 보이던 원인은 각각 짝 모달 미마운트였다.

**태그 프로필 forceMax**: 모달 체크 시 sized 태그클라우드 크기를 `ts-9`로 고정한다. 저장·삭제 후 클라우드 재조회로 반영. 엔트리 본문 태그줄에는 적용하지 않는다.


**태그 프로필 저장·삭제 후 갱신** (`JournalTagProfileModal`): 월간/주간/일간 등은 성공 알림 확인 후 `refreshJournalDaysForRoute` + contentType 대응 `fetchTagCloud`(day/diary/dream). 결산 상세(`annual-detail`)는 일자 `fetchTagCloud`가 아니라 `useJournalAnnualStore.fetchTagRows(yy, activeSection)`로 태그클라우드 행을 재조회한다(SSOT: `/api/journal/annual/{yy}/tags`). 검색 팝업은 일자/클라우드 재조회 없이 `@success` → `loadEntries()`. 스레드 상세는 소속 엔트리 태그 집계가 SSOT이므로 `JournalThreadStore.refreshOpenDetail()`로 상세와 엔트리를 함께 재조회한다.

**검색 팝업 내부 동작**: 현재 route가 `journal-entry-search`이면 `검색` 액션은 새 창을 열지 않고 같은 창에서 `router.replace({ name: "journal-entry-search", query })`를 호출한다. `JournalEntrySearchPage`가 route 변경을 watch해 목록을 즉시 갱신한다.

**보존 기준**:
- 태그 클릭 자체는 검색 실행이 아니다.
- 검색은 별도 메뉴 액션이다.
- 일기/꿈 태그 검색은 현재 목록 필터가 아니라 새 창 검색 화면이다.
- 닫기는 외부 클릭, ESC, 스크롤/리사이즈에서 동작한다.

**마운트 위치**: `JournalDayLayout.vue`·`JournalDayDailyLayout.vue`·`JournalEntrySearchPage.vue`·`JournalAnnualLayout.vue` — `<JournalTagContextMenu />` (Teleport to body). 결산은 일자 태그 검색을 위해 `<JournalDayMetaModal />`도 함께 마운트한다.

---

### 23-3a. `JournalMetaContextMenu` (메타 클릭 컨텍스트 메뉴)

**Vue 구현**: `app/frontend-vue/src/features/journal/shared/components/JournalMetaContextMenu.vue`

**Pinia 스토어**: `app/frontend-vue/src/features/journal/stores/metaContextMenu.ts`

**동작**: 메타 VIEW 헤더 `#메타` 클릭 시 태그와 동일한 fixed 팝업. payload `{ metaId, name, ctgr, unit? }`.

**메뉴 액션** (메타 VIEW):

| 액션 | 동작 |
|------|------|
| 검색 | `journalModalStore.openDayFilterModal({ type: "meta", ... })` → `JournalDayMetaModal` |
| 그래프로 보기 | `journalStore.addMetaToGraph` (최대 2, 중복 시 비활성, 꽉 차면 alert) |
| 메타 설정 | `journalModalStore.openMetaProfile` → `JournalMetaProfileModal` |

메뉴 액션·그래프 표시 상태·최대 2개 제한 경고는 현재 locale의 클라이언트 카탈로그를 사용한다.

**마운트 위치**: `JournalDayLayout.vue` — `<JournalMetaContextMenu />` (Teleport to body)

**현재 Vue 동등**: ✓ 구현 완료

---

### 23-3b. `JournalMetaProfileModal` (메타 설정 모달)

**Vue 구현**: `app/frontend-vue/src/features/journal/shared/modals/JournalMetaProfileModal.vue`

**데이터**: `GET /api/journal/day/metas/{id}` → 로그인 사용자 기준 `contentSize`(JOURNAL_DAY 기록 수)를 포함한 `journalModalStore.metaProfileModel`. 컨텍스트 메뉴 seed(이름·카테고리·단위·기록 수)와 API 응답을 병합한다.

**동작**: 조회 전용 모달이다(`spec/JOURNAL_SCREEN_BEHAVIOR_SPEC.md` §17.2). 태그 프로필처럼 색·메모 편집은 `meta_profile` 엔티티/API가 없으며 본 모달 범위 밖이다.

**i18n**: 모달 제목·배지·필드 레이블·조회 실패 문구·닫기 버튼은 현재 locale의 클라이언트 카탈로그를 사용한다.

**현재 Vue 동등**: ✓ 구현 완료 (조회 전용)

---

### 23-3c. `JournalAnnualDetail`·Annual Aside (연간 결산 상세·필터)

**Vue 구현**: `app/frontend-vue/src/features/journal/annual/JournalAnnualList.vue`, `JournalAnnualDetail.vue`, `JournalAnnualLayout.vue`, `components/JournalAnnualViewToolbar.vue`, `components/JournalAnnualDetailAside.vue`, `components/JournalAnnualListAside.vue`, `stores/journalAnnualAside.ts`

**마운트 모달·메뉴** (`JournalAnnualLayout`): `JournalAnnualRegistModal`, `JournalAnnualReviewRegistModal`, `JournalDayMetaModal`(일자 태그 검색), `JournalTagProfileModal`, `JournalTagContextMenu`.

**데이터·동작**: 전체 결산 갱신·결산 등록·aside 열기는 `JournalAnnualViewToolbar`(저널 일자 액션 행과 동형, `mt-3`; 탭용 `mt-5` 빈 여백 없음)에 둔다. 총 집계 카드는 통계만 표시하며 `margin-top: 0`으로 툴바에 붙인다. 결산 상세·리뷰·태그클라우드·중요/참조 토글과 상세/목록 필터를 `useJournalAnnualStore` 상태 및 기존 액션에 연결한다. aside 표시는 `useJournalAnnualAsideStore`가 레이아웃·툴바가 공유한다. 목록 aside와 상세 aside는 필터 전용 영역으로 유지하며, 상세 aside·상세 화면 툴바에는 결산 등록 액션을 두지 않는다(목록 route에서만 툴바에 노출). 결산 목록 카드의 Metronic 컨텍스트 메뉴는 비동기 목록 렌더, 수정 저장 후 목록 재조회 완료, 필터 DOM 갱신 후 `reinitMetronicAfterDom()`으로 재바인딩한다. locale 변경은 선택 연도·활성 탭·태그클라우드·필터값·토글 상태·API 호출을 변경하지 않는다.

**본문 타이포그래피**: 결산 상세 SUMMARY 본문과 DIARY/DREAM 엔트리 본문은 저널 일자 엔트리와 같은 `journal-content p-2` 계약을 따른다. 결산 상세 엔트리 제목은 일자 엔트리 제목과 맞춰 `fs-5` 크기로 표시한다. 결산 상세 DIARY/DREAM 엔트리 목록은 행 왼쪽 날짜 칼럼을 반복하지 않고, 날짜별 `journal-day-header` 아래에 `journal-diary-item`/`journal-dream-item` 본문 행을 배치해 저널 일자 화면의 시각 흐름과 맞춘다. DIARY/DREAM 엔트리 태그는 `JournalEntryItem` 과 동일하게 밑줄 primary `#이름`(ctgr 포함, `bi-tag` 없음)으로 표시하고 클릭 시 태그 컨텍스트 메뉴를 연다(변경 전: `bi-tag` + 단순 `#name`).

**i18n**: 상세의 SUMMARY·REVIEWS·로딩·태그 메뉴 tooltip·IMPORTANT·REFERENCE와 두 aside의 FILTER·TAGCLOUD·ENTRY FILTER·SUMMARY FILTER·키워드 레이블은 현재 locale의 클라이언트 카탈로그를 사용한다. 영어는 기존 대문자 표기를 유지한다.

**현재 Vue 동등**: ✓ 구현 완료

---

### 23-4. `JournalEntrySearchPage` (저널 엔트리 검색 새 창)

**Vue 구현**: `app/frontend-vue/src/features/journal/entry/JournalEntrySearchPage.vue`

**Route**: `/journal/entry/search` (`/vue-app/journal/entry/search`로 접근)

**Layout**: `SystemLayout.vue` 하위 auth route. 새 창 검색 화면이므로 `DefaultLayout` 메뉴와 `JournalLayout` aside를 붙이지 않는다.

**Source (Legacy)**:
- `legacy/templates/view/feature/journal/entry/journal_entry_search.ftlh`
- `legacy/static/vue/feature/journal/entry/JournalEntrySearchApp.ts`

**현재 Vue 범위**:
- `type`, `tagIds`, `tagName`, `searchKeywords` query 파싱
- `GET /api/journal/entries` 조회
- 결과 목록, 태그 목록, 키워드 검색/초기화, 정렬 전환
- 고급 필터 토글 영역에서 유형(일기/꿈) 선택과 키워드 추가
- 결과 전체 복사 버튼 — 레거시 `JournalEntrySearch.copy()` 포맷으로 클립보드 복사
- 개별 결과 복사 버튼 — `JournalEntryItem.copyEntry()` 사용
- TXT export 버튼 — `GET /api/journal/entries/export`
- 결과별 수정/삭제 버튼
- 결과 태그 클릭 컨텍스트 메뉴(`JournalTagContextMenu`)와 태그 프로필 모달(`JournalTagProfileModal`) 마운트
- 검색 팝업 내부 태그 검색은 같은 창 query 갱신으로 반영
- 엔트리 수정 저장 성공 시 `JournalEntryRegistModal` `prepare-success` 이벤트로 현재 검색 목록/수정 대상 DOM을 먼저 준비하고, `success` 이벤트로 저장 위치 스크롤만 수행
- 검색 결과에 포함된 저널 해석의 수정 액션은 같은 창에서 `JournalInterpretationRegistModal`을 열어야 한다. 검색 팝업이 이 모달을 직접 마운트하며, 수정 모드는 `GET /api/journal/interpretation/{id}` 상세 조회로 제목/본문/순번을 채운 뒤 모달을 열어야 한다. 저장 성공 후 모달 내부의 `journalStore.fetchDays()` 완료 신호를 `JournalEntrySearchPage`가 감지해 현재 검색 목록을 재조회한다.
- `JournalInterpretationRegistModal`의 제목(`title`)은 필수 항목이 아니다. 제목 없이 본문만으로 해석 등록/수정이 가능해야 한다.
- 날짜 헤더는 `stdrdDt (요일)` 옆에 새 창 버튼(`bi-box-arrow-up-right`)을 표시한다. 클릭 시 월간/주간 일자 카드와 동일하게 `window.open(BASE_URL + /journal/daily?stdrdDt=..., "_blank", "width=...,height=...")`로 일자 뷰를 새 창으로 연다.

**표시 문구 i18n**: 컨트롤 바·고급 필터·유형/키워드/태그 입력·카테고리 선택·로딩/빈 결과·일자 새 창 tooltip과 결과 건수·연월 구분선·요일은 현재 locale의 클라이언트 카탈로그를 사용한다. locale 변경은 URL query·검색 조건·정렬·태그 선택·결과 목록을 변경하지 않는다.

**액션 메시지 i18n**: 검색 결과/엔트리 상세 조회 실패, 태그 선택 검증, 검색 조건 검증, 복사 대상 없음과 복사 성공/실패 알림 및 복사 날짜 헤더의 요일은 현재 locale의 클라이언트 카탈로그를 사용한다. 클립보드와 TXT 본문은 기존 레거시 출력 포맷을 유지한다.

**남은 legacy 동등성 확인 대상**:
- 고급 검색 영역의 태그 직접 입력 UX

---

### 23-5. `JournalThreadDetailModal` (저널 스레드 상세 모달)

**Vue 구현**: `app/frontend-vue/src/features/journal/thread/modals/JournalThreadDetailModal.vue`

**데이터**: `useJournalThreadStore.detailModel`의 카테고리·제목·작성자·작성일·본문·태그·`comment.list`를 읽기 전용으로 표시한다. **태그는 스레드 자체 태그가 아니라 소속 엔트리 태그의 집계다** — 스레드는 자체 태그를 소유하지 않는다(엔티티 `TagEmbed` 제거, 챕터와 동형). 백엔드 `JournalThreadService.viewDetailPage` 의 `applyEntryTagSummary` 가 소속 엔트리 태그를 tagId 로 중복 제거해 `thread.tag.list` 에 채우고(챕터 `applyChapterTagSummary` 와 동형), 상세는 그 결과를 표시한다. 소속 엔트리 목록은 `store.detailEntries`(일자별 그룹 카드, 그룹마다 일자 헤더)로 함께 표시한다. 등록/수정 모달에는 태그 입력이 없다(자체 태그 미소유).

**동작**: 댓글 등록 버튼은 `useAttachableModalStore.openCommentRegist(id, contentType)`를 호출한다. 댓글 수 버튼(목록에 댓글이 있을 때)은 `openCommentList`를 호출한다. 댓글 등록 성공 시 `CommentRegistModal`이 열린 상세가 `JOURNAL_THREAD`이면 상세를 재조회하고 목록의 댓글 수를 갱신한다.

**닫기 정책**: 읽는 중인 상세가 backdrop 클릭이나 Escape 입력으로 의도치 않게 닫히지 않도록 루트 DOM에 `data-bs-backdrop="static"`, `data-bs-keyboard="false"`를 선언하고 `Bootstrap Modal`도 `{ backdrop: "static", keyboard: false }`로 생성한다. 헤더 ×와 푸터 「닫기」는 `store.closeDetail()`을 호출하는 명시적 종료 경로로 유지한다. URL 이동·상세 조회 실패에 따른 프로그램상 종료도 유지한다.

**상위 서사·엔트리 소속 계약**: 저널 스레드는 특정 일자 엔트리로 쓰기 어려운 내용을 제목·본문으로 직접 서술하는 독립 상위 서사다. 엔트리를 스레드에 소속시키는 기능은 **✓ 구현 완료** — 백엔드(테이블 `journal_thread_entry` + 소속 등록/해제/조회/후보 API)와 엔트리 ⋯ 메뉴의 소속 지정 UI(「스레드에 추가」 서브메뉴 + 제목 검색·분류 필터 + 「새 스레드로 시작」 + 본문 소속 스레드 칩)와 **스레드 상세의 소속 엔트리 목록**도 완료됐다(✓). 스레드 상세는 소속 엔트리를 저널 일자와 동일한 `JournalEntryItem` 카드로 표시하되, 저널 일자·연간 상세와 동형으로 `stdrdDt` 별로 그룹핑해 그룹마다 `journal-day-header`(날짜 + `(요일)`, `getWeekDayStr`)를 얹는다 — 카드별 날짜 라벨은 일자 헤더로 대체됐다. `GET /api/journal/threads/{id}/entries` 가 소속 메타가 아니라 full `JournalEntryDto` 목록을 반환하며, `JournalThreadEntryService.getEntriesByThread` 가 `JournalEntryService.getListDtoByIds` 로 조회한 뒤 **일자 오름차순(동일 일자는 엔트리 ID 오름차순 tiebreak)** 으로 결정적 정렬한다(`sort_order` 미사용 구간의 IN 순서 비결정성 제거). 프론트 그룹핑은 백엔드 정렬 순서를 first-seen 으로 보존한다. 상세 카드가 수정·댓글·해석·이력·관련글·스레드 소속·라이프사이클·상태·삭제 액션을 제공하는 것은 의도된 계약이다. 스레드에 보이는 항목은 읽기 전용 복제본이 아니라 같은 원본 엔트리이므로 저널 일자와 기능 경계를 달리하지 않는다.

**엔트리 액션 호스트·갱신 계약**: `JournalThreadLayout`은 엔트리 카드가 호출하는 `CommentRegistModal`, `CommentListModal`, `JournalInterpretationRegistModal`, `HistoryModal`, `RelatedContentAddModal`, `JournalTagProfileModal`, `JournalTagContextMenu`를 마운트한다. `JournalEntryRegistModal`·`JournalEntryViewModal`은 `App.vue`의 비팝업 전역 마운트를 재사용해 중복 마운트하지 않는다. 엔트리·해석의 수정/삭제, 댓글, 이력 복원, 관련글 연결/해제, 태그 프로필, 스레드 소속, 라이프사이클·상태 변경이 성공하면 `refreshJournalEntryHostForRoute`가 현재 route를 판별한다. `thread-detail`에서는 `JournalThreadStore.refreshOpenDetail()`이 상세 본문·집계 태그와 소속 엔트리를 함께 재조회하고, 다른 저널 화면에서만 기존 `refreshJournalDaysForRoute`를 사용한다. 상세 재조회 실패 시 읽던 데이터를 비우지 않고 오류를 기록·표시한다.

**도메인 메타 경계**: 스레드의 자기 설명 SSOT는 제목·본문이다. 시작·최근 시점은 소속 엔트리 일자에서 파생하고, 스레드 라이프사이클·진행/종결 상태·종결 시점·대표 엔트리(앵커)·별도 핵심 질문 필드는 현재 계약에 포함하지 않는다. 스레드 상세는 날짜순 전체 기록을 읽는 화면이며 nullable `sort_order`를 표시 순서에 사용하지 않고 별도 소속 역할도 두지 않는다. 이는 미구현 항목이 아니라 현재 채택하지 않은 설계다.

- 소속은 스레드 1 : 엔트리 N 이 아니라 **N:M** 이다. 한 엔트리가 여러 스레드에 속할 수 있다.
- 소속 등록 API는 소속 행 조회·복원·저장 전에 스레드와 대상 엔트리의 존재·현재 사용자 소유권을 모두 검사한다. 미존재 엔트리는 not found, 타인 소유 엔트리는 access denied로 거부하고 소속 쓰기를 수행하지 않는다.
- 소속 후보 API `GET /api/journal/threads/candidates`는 본인 스레드만 현재 소속 → 최근 활성 소속 추가 시각 → 활성 소속 수 → 스레드 수정·생성 시각 순으로 반환하고 제목 검색·분류 필터·1~20개 조회 상한을 적용한다. 서브메뉴는 현재 엔트리 ID와 검색 조건으로 이 API를 호출하고, 후보 응답의 `member`로 체크·추가/해제를 판정한다.
- 등록·해제 모두 **멱등**이며, 해제는 소프트 삭제다. 해제했던 소속을 다시 등록하면 새 행을 만들지 않고 기존 행을 되살린다.
- API: `GET|POST /api/journal/threads/{id}/entries`, `DELETE /api/journal/threads/{id}/entries/{entryId}`, `GET /api/journal/entries/{entryId}/threads`
- **엔트리 응답에 소속이 실린다**: `JournalEntryDto.threadList` (소속 없으면 빈 목록). 엔트리마다 단건 조회하면 목록 화면에서 N+1 이 나므로 `JournalEntryRelatedEnricher`·`JournalDayQueryService.mergeRelatedContents` 가 엔트리 목록 단위로 **일괄 주입**한다. 조회 대상 사용자는 `username` 파라미터로 받는다 (`getRelatedContentMapByRefs` 와 동일한 계약).
- **변경 전**: 이 계약은 RELATED/FLOW 그래프와 «별도» 축으로 유지할 계획이었다. **변경 후**: FLOW 를 이 소속 구조로 **수렴 완료**했다 (근거·단계는 `docs/spec/DESIGN_NOTES.md` 참조). FLOW 간선은 스레드 소속으로 이관되고 FLOW 경로는 제거됐다(다-2) — 공존 구간은 종료됐다.

**i18n**: 모달 제목·댓글 섹션 제목·빈 상태·등록 툴팁·닫기 버튼은 현재 locale의 클라이언트 카탈로그를 사용한다.

**현재 Vue 동등**: ✓ 구현 완료

---

### 23-6. `JournalThreadList` (저널 스레드 목록)

**Vue 구현**: `app/frontend-vue/src/features/journal/thread/JournalThreadList.vue`, `JournalThreadLayout.vue`, `components/JournalThreadViewToolbar.vue`

**데이터·동작**: 등록은 `JournalThreadViewToolbar`(결산·일자 액션 행과 동형, `mt-3 mb-1`; ASIDE 없음; 탭용 `mt-5` 빈 여백 없음)에 두고 `thread-create`로 라우팅한다. 목록 카드 위 컴팩트한 검색 카드에서 분류·제목 검색, 전체 초기화를 제공하고 모든 조건 실행은 첫 페이지부터 조회한다. **태그 클라우드·태그 필터는 제거됐다** — 스레드가 자체 태그를 소유하지 않게 되면서(2b) 자체 태그 기반 클라우드/필터가 무의미해졌다. 소속 엔트리 태그 집계 기반으로 되살리는 것은 별도 후속 기능이다(백로그). 검색 카드는 `margin-top: 0`으로 툴바에 붙인다. 검색 컨트롤(분류·제목·초기화)만 두고 별도 제목 행은 두지 않는다. `useJournalThreadStore.threadList`를 기존 테이블 DOM·클래스로 표시하고, 행 클릭은 Vue Router를 호출한다. 관리 열의 ⋯ 컨텍스트 메뉴는 수정 route 이동과 삭제 store 액션을 제공하고, 메뉴 클릭은 행 상세 이동으로 전파하지 않는다. 비동기 목록 조회가 끝나면 Metronic 메뉴를 재초기화한다. 등록·수정·삭제 성공 후 목록과 태그 클라우드를 함께 갱신한다. 댓글 수 버튼은 `useAttachableModalStore.openCommentList`를 호출한다. 분류 선택지는 사용자 권한 전용 `GET /api/journal/threads/categories`를 사용한다. (태그 클라우드 제거)

**i18n**: 분류·제목 placeholder·태그 빈 상태와 조회 실패, 툴바 등록 버튼·테이블 헤더·빈 상태·댓글 목록·컨텍스트 메뉴·수정·삭제 문구는 현재 locale의 클라이언트 카탈로그를 사용한다. 분류 API도 현재 요청 locale의 코드명을 반환한다.

**삭제 계약**: 삭제 확인과 실패 fallback은 스레드 전용 현재 locale 메시지를 사용하고, 성공 fallback은 공통 삭제 성공 메시지를 사용한다. 삭제 API가 `message`를 반환하면 서버 메시지를 우선 표시하며, 성공 알림 확인 후 첫 페이지 목록을 다시 조회한다.

**현재 Vue 동등**: ✓ 구현 완료

---

### 23-7. `JournalThreadRegistModal` (저널 스레드 등록/수정 모달)

**Vue 구현**: `app/frontend-vue/src/features/journal/thread/modals/JournalThreadRegistModal.vue`

**데이터·동작**: `useJournalThreadStore.registModel`의 제목·본문·태그를 편집하고, 등록/수정 확인 후 `submitRegist()`를 호출한다. 안전 닫기는 1회 클릭 시 확인 상태로 전환하고 2초 안에 다시 클릭하면 닫는다.

**i18n**: 모달 제목·필드 레이블·제목 placeholder·저장·닫기·확인 문구는 현재 locale의 클라이언트 카탈로그를 사용한다. 저장 결과는 서버 `message`를 우선 표시하고, 서버 메시지가 없을 때 현재 locale의 등록·수정·실패 fallback을 사용한다. 수정·상세 조회 실패 안내도 현재 locale을 사용한다.

**현재 Vue 동등**: ✓ 구현 완료

---

### 24. `JournalLayout` (저널 공통 레이아웃·모달 호스트)

**Vue 구현**: `app/frontend-vue/src/features/journal/day/JournalDayLayout.vue`

**역할**: `<router-view>` + `JournalAside` + 저널·공통 attachable 모달 일괄 마운트

**Aside 스크롤 동작**: `journal.scss`에서 `.journal-layout-vue__aside`에 `position: sticky`와 `overflow-y: auto`를 적용한다. 데스크톱(`min-width: 992px`)에서는 `top: var(--bs-app-header-height, 74px)`와 `max-height: calc(100vh - var(--bs-app-header-height, 74px))`를 사용해 floating 툴바와 같은 상단선에 고정하고, 그 미만 화면에서는 `top: 0; max-height: 100vh`를 사용한다. 본문 스크롤 중 필터 패널이 추가 여백 없이 viewport 안에서 따라오며, 패널 내용이 화면보다 길면 aside 내부만 스크롤된다. 연간 결산 aside(`.journal-annual-layout-vue__aside`)는 기존 `top: 1rem`, `max-height: calc(100vh - 2rem)` 규칙을 유지한다.

**Aside 열기 i18n**: aside가 숨겨졌을 때 표시되는 필터 패널 열기 버튼 tooltip은 현재 locale의 클라이언트 카탈로그를 사용한다. locale 변경은 aside 표시 상태를 변경하지 않는다.

**Aside 열기 버튼 위치**: aside 숨김 시 데스크톱 열기 버튼(`bi-layout-sidebar-inset-reverse`)은 `JournalDayViewToolbar` 액션 행의 맨 오른쪽에서 일정 화면과 같은 구분자+아이콘 슬롯으로 표시되고, sticky 툴바와 함께 스크롤을 따라간다. 기존 본문 컨테이너의 `position-absolute; top: 4rem; right: 0` 버튼은 툴바 액션 영역이 숨겨지는 모바일(`d-md-none`)에서만 유지한다. 변경 전에는 모든 화면 크기에서 본문 우상단에 떠 있어 데스크톱 액션 행과 정렬되지 않았다.

**마운트 모달·메뉴**: `JournalDayRegistModal`, `JournalDayDetailModal`, `JournalChapterRegistModal`, `JournalInterpretationRegistModal`, `JournalTodoRegistModal`, `JournalDayMetaModal`, `CommentRegistModal`, `CommentListModal`, `HistoryModal`, `RelatedContentAddModal`, `JournalTagProfileModal`, `JournalMetaProfileModal`, `JournalTagContextMenu`, `JournalMetaContextMenu`. (`JournalEntryRegistModal`·`JournalEntryViewModal`은 채팅 RAG 원문 딥링크를 위해 `App.vue`에서 비팝업 전역 마운트 — 레이아웃 중복 마운트 없음. 출처 클릭은 읽기 전용 뷰, 편집은 뷰 footer에서 수정 모달로 전환.)

`JournalDayMetaModal.vue`의 제목·결과 건수·연도/전체 연도·연월 구분선·기준 날짜 요일·필터 추가/제거·일자 새 창 tooltip·빈 상태·닫기와 조회 실패 fallback은 현재 locale의 클라이언트 카탈로그를 사용한다. locale 변경은 선택 메타/태그·AND 필터·연도·일자 목록을 변경하지 않는다. 태그 입력 검색의 placeholder·카테고리 선택·미존재 태그 알림 문구는 엔트리 검색 키(`journal.entry.search.tag.*`, `journal.entry.search.category.*`)를 재사용한다.

`JournalDayDetailModal.vue`의 제목·기준 날짜 요일·날짜 정밀도 배지·빈 상태·조회 실패·닫기 문구는 현재 locale의 클라이언트 카탈로그를 사용한다.

**현재 Vue 동등**: ✓ 구현 완료

---

## 저널 컴포넌트 현황 요약


| 컴포넌트 | 레거시 위치 | Vue 동등 | 우선순위 |
|---------|-----------|---------|---------|
| `JournalDayViewToolbar` | `_journal_day_page_header.ftlh` (`header_btn_reg_modal`) + 탭 행 | ✓ `JournalDayViewToolbar.vue` | 완료 |
| `JournalTagCloudHeader` | `_journal_day_tag_header.ftlh` | ✓ `JournalTagCloudHeader.vue` | 완료 |
| `JournalAsideFilterHeader` | `JournalDayAsideFilterHeaderApp.ts` | ✓ `JournalAside.vue` (`#journal_aside_header` + 정렬 토글) | 완료 |
| `JournalAsideYyMnthSection` | `JournalDayAsideYyMnthApp.ts` | ✓ `JournalAside.vue` (연/월/TODAY/Week/Pinpoint 모두 구현) | 완료 |
| `JournalAsideEntryFilters` | `JournalDayAsideEntryFiltersApp.ts` | ✓ (블록 D 제외 — 이식 대상 아님) | — |
| `JournalAsideTodoCard` | `JournalDayAsideTodoCardApp.ts` | ✓ `JournalAsideTodoCard.vue` (카드 헤더·목록 API·삭제·등록 모달 호출) | 완료 |
