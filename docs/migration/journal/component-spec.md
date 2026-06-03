# 저널 컴포넌트 마이그레이션 스펙 (Journal Component Spec)

> 공통 Freemarker 매크로(checkbox, modal_header 등)는 ``common/component-spec.md`` 참조.

## 저널 전용 컴포넌트 목록


### 18. `JournalTagCloudHeader` (저널 태그 클라우드 헤더)

**Source (Legacy)**: `legacy/templates/view/feature/journal/day/tag/_journal_day_tag_header.ftlh`

**Vue 구현 완료**: `app/frontend-vue/src/views/journal/day/components/JournalTagCloudHeader.vue`

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
| `TagCloudItem` | `{ id: number|string, name: string, ctgr?: string, contentSize: number, textClass?: string }` |

**태그 크기 클래스**: `.ts-1` ~ `.ts-9` (`src/styles/components/tag.scss`) — `textClass` 필드로 전달

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

**현재 Vue 동등**: ✓ 구현 완료 (`JournalTagCloudHeader.vue`)

---

### 19. `JournalAsideFilterHeader` (어사이드 필터 카드 헤더)

**Source (Legacy)**: `legacy/static/vue/feature/journal/day/JournalDayAsideFilterHeaderApp.ts`

**현재 Vue 동등**: ❌ MISSING — `JournalAside.vue` 상단 필터 헤더 없음

**구현할 DOM 구조**:
```html
<div id="journal_aside_header" class="card-header min-h-auto mb-5">
    <h3 class="card-title text-gray-900 fw-bold fs-3">
        <i class="bi bi-filter fs-2 me-1"></i> FILTER
    </h3>
    <div class="card-toolbar">
        <a class="btn btn-sm btn-icon btn-color-gray-500 btn-light"
           data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
           title="정렬 변경" @click.prevent="toggleSort">
            <i class="bi bi-sort-numeric-up-alt fs-2 pe-0" id="sortIcon"></i>
        </a>
    </div>
</div>
```

**상태 / 액션**:
```typescript
// journal.ts store에 추가 필요
const sortOrder = ref<'ASC' | 'DESC'>('DESC');
function toggleSort() {
    sortOrder.value = sortOrder.value === 'DESC' ? 'ASC' : 'DESC';
    fetchDays();
}
```

**정렬 아이콘**: `DESC` → `bi-sort-numeric-down-alt`, `ASC` → `bi-sort-numeric-up-alt`

---

### 20. `JournalAsideYyMnthSection` (어사이드 연월·주간·핀포인트)

**Source (Legacy)**: `legacy/static/vue/feature/journal/day/JournalDayAsideYyMnthApp.ts`

**현재 Vue 동등**: ✓ 구현완료 — 년/월 select + TODAY + 월 그리드 + 주간 네비게이터 + Pinpoint (`JournalAside.vue`)

**3개 sub-block 요약**:

**Sub-block 1 — 년·월 select + 이전/다음 화살표**:
- 연도: `<select name="yy" id="yy">` — `v-model="store.yy"` (integer) — 2010년~현재 역순 목록
- 월: `<select name="mnth" id="mnth">` — `v-model="store.mnth"` — 1~12 정수 목록
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

---

### 21. `JournalAsideEntryFilters` (어사이드 엔트리 필터)

**Source (Legacy)**: `legacy/static/vue/feature/journal/day/JournalDayAsideEntryFiltersApp.ts`

**현재 Vue 동등**: ⚠ 부분구현 — CHAPTER CATEGORIES 체크박스·고급필터 아코디언은 레거시 멀티셀렉트·토글과 DOM 상이

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

**블록 D — 고급 필터 아코디언 (MISSING)**:
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

---

### 22. `JournalAsideTodoCard` (어사이드 투두 카드)

**Source (Legacy)**: `legacy/static/vue/feature/journal/day/JournalDayAsideTodoCardApp.ts`

**현재 Vue 동등**: ❌ MISSING — `openTodoReg` 버튼만 있고 TODO 카드 전체가 없음

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

**Vue 구현 완료**: `app/frontend-vue/src/views/journal/day/components/JournalDayViewToolbar.vue`

**레거시 출처**:
- 본문 상단 탭 행: `journal_day_monthly.ftlh` / `journal_day_weekly.ftlh` — `nav-tabs-line` 4탭
- 일자 등록 버튼: `_journal_day_page_header.ftlh` — `@component.header_btn_reg_modal` (`data-journal-day-action=reg-modal` → `JournalDayRuntimeService`)

**사용 화면**: `JournalDayWeekly.vue`, `JournalDayMonthly.vue`, `JournalDayCalendar.vue`, `JournalDayMeta.vue` — 카드(`.card.post`) 위 첫 행

**DOM 구조**:
- flex 행 (`justify-content-between`): 좌측 `nav-tabs` (router-link 4개) + 우측 등록 버튼 (`d-none d-md-flex pe-5 mt-5`)
- 탭 라벨: `주간 VIEW` / `월간 VIEW` / `달력 VIEW` / `메타 VIEW`
- 등록 버튼: `btn btn-sm btn-light-primary btn-outlined`, 아이콘 `bi-calendar-plus`, 라벨 「저널 일자 등록」

**동작**:
| 액션 | Vue |
|------|-----|
| 저널 일자 등록 클릭 | `useJournalModalStore().openDayRegist()` → `JournalDayRegistModal` (`JournalLayout` 마운트) |
| 탭 전환 | `router-link` — `journal-weekly` / `journal-monthly` / `journal-calendar` / `journal-meta` |
| 일기 키워드 검색 | `v-model="store.diaryKeyword"`, Enter/버튼 클릭 → `store.fetchDays()` |
| 꿈 키워드 검색 | `v-model="store.dreamKeyword"`, Enter/버튼 클릭 → `store.fetchDays()` |

**추가 구현 (2026-05-19)**: 우측 영역에 일기·꿈 키워드 검색 input + 돋보기 버튼 추가 — `store.diaryKeyword` / `store.dreamKeyword` 바인딩. `JournalAside`의 키워드 필터와 동일 store 값을 공유하므로 양쪽 동기화.

**행동 spec 교차 참조**: `docs/JOURNAL_SCREEN_BEHAVIOR_SPEC.md` §4.4 (상단 등록 버튼)

**미이관 (레이아웃 전역 `_journal_day_page_header.ftlh` 나머지)**: 고급필터, 일정 등록, 개인 일정, 태그 카테고리 동기화 — `screen-spec.md` 월간/주간 Action 표 참고

**현재 Vue 동등**: ✓ 구현 완료

---

### 22-2. `JournalDayRegistModal` 날짜 선택 (datepicker)

**Vue**: `JournalDayRegistModal.vue` — `#journalDate` 입력

**레거시**: `attachRegFormControls()` → `cF.datepicker.singleDatePicker("#journalDate", "yyyy-MM-DD", obj.journalDate)` (daterangepicker)

**Vue 동등**: `flatpickrSingleDate.ts` — `bindSingleDatePicker()` (flatpickr, `Y-m-d`, locale `ko`). 모달 `shown` 시 부착·`hidden` 시 `destroy`. 캘린더 아이콘 클릭 → `open()`. 모달 내부 입력도 calendar DOM은 기본 body append를 사용한다. `.modal-body`에 append하면 입력칸 좌표와 calendar 좌표 기준이 달라져 위치가 틀어질 수 있다.

**신규 등록 기본값**: `openDayRegist()` 에서 `journalDate` 비어 있으면 오늘(`formatLocalDateStr`) — 레거시 daterangepicker `startDate: moment()` 와 동일.

**저장 후 갱신**: `JournalDayRegistModal.vue` 는 저장 성공 시 모달을 닫고 성공 알림을 표시한 뒤, 사용자가 OK를 누르면 현재 route 기준으로 목록을 갱신한다. `/journal/weekly` 에서는 `setViewType("WEEKLY")` 후 `fetchDays({ viewType: "WEEKLY" })`, `/journal/monthly` 에서는 `setViewType("LIST")` 후 `fetchDays({ viewType: "LIST" })` 를 호출해 주간 등록 완료 후 월간 목록으로 돌아가지 않게 한다. 일자 태그 변경이 일자 태그클라우드에 반영되도록 OK 이후 `fetchTagCloud({ sections: ["day"] })`도 함께 호출한다 (2026-05-19 수정, 2026-06-02 OK 이후 갱신, 2026-06-03 부분 갱신).

**중복 등록 정책**: 신규 등록 시 같은 사용자에게 같은 `journalDate`의 활성 저널 일자가 이미 있으면 등록을 실패 처리한다. 과거처럼 기존 일자를 수정으로 전환하지 않는다 (2026-05-20 수정).

**닫기 정책**: 등록/수정 중 백드롭(모달 바깥)을 클릭하거나 Escape를 눌러도 모달을 닫지 않는다. 사용자가 입력 중인 날짜/태그/메타 값이 의도치 않은 닫힘으로 클리어되지 않도록 `Bootstrap Modal`을 `backdrop: "static", keyboard: false`로 생성한다 (2026-05-21 수정).

**현재 Vue 동등**: ✓ 구현 완료

---

### 22-3. `JournalDayRegistModal` 태그/메타 Tagify

**Vue**: `JournalDayRegistModal.vue` → 공통 `TagifyEditor.vue`

**초기값 로딩**: 수정 모달에서 서버 상세의 `tag.tagListStrWithCtgr` / `meta.metaListStr` 를 `Tagify.loadOriginalValues()` 로 반영한다. 이때 기존 값 로딩은 사용자 신규 태그 추가가 아니므로 카테고리 입력 프롬프트를 열지 않는다.

**저장 직전 처리**: 메타 입력 중인 draft는 `commitPendingDraft()` 로 확정 후 `tag.tagListStr`, `meta.metaListStr` 를 multipart form data에 포함한다. 태그 draft는 카테고리 선택/입력 단계가 모호할 수 있으므로 자동 확정하지 않는다.

**draft 정리**: 수정 모달 초기값 재로딩 또는 모달 닫기 시 남아 있는 Tagify draft 입력 UI는 취소한다.

**현재 Vue 동등**: ✓ 구현 완료

---

### 22-3-1. 작성 폼 모달 닫기 정책

**대상**: 저널 일자/챕터/엔트리/해석/할일/댓글/관련 컨텐츠, 저널 연간/연간 리뷰, 저널 스레드 등록·수정 모달.

**정책**: 작성 중 내용 유실을 막기 위해 등록/수정 성격의 모달은 백드롭 클릭 및 Escape 키로 닫히지 않는다. 명시적인 닫기 버튼도 레거시 `modal_btn_close_safe`와 동일하게 1회 클릭 시 확인 상태로 전환하고, 2초 안에 한 번 더 클릭해야 닫는다. 저장 성공 흐름은 즉시 닫는다.

**구현**: 각 Vue 모달의 루트에 `data-bs-backdrop="static"` / `data-bs-keyboard="false"`를 선언하고, `new Modal(...)` 생성 옵션에도 `{ backdrop: "static", keyboard: false }`를 지정한다. 닫기 버튼은 공통 `useSafeModalClose()`를 사용해 헤더 X 버튼과 푸터 닫기/취소 버튼 모두 동일한 2회 클릭 정책을 적용한다.

---

### 22-4. `JournalEntryRegistModal` 등록/수정 폼 여백

**Vue**: `JournalEntryRegistModal.vue` → `journal-entry-regist-modal__body`, `journal-entry-regist-form`

**스타일**: `journal.scss` 에서 데스크톱 좌우 `3rem`, 모바일 좌우 `1.25rem` padding을 적용한다. 제목/본문/태그 입력이 모달 가장자리에 붙어 보이지 않게 하며, TinyMCE iframe 본문은 `RichEditor.vue` 의 `content_style` 로 `12px 16px` 내부 padding을 둔다.

**중복 제출 방지**: 저장 클릭 즉시 `submitting` guard를 세운 뒤 확인창을 띄운다. 확인창이 떠 있는 동안 재클릭되어도 두 번째 `submit()`은 무시되어 같은 꿈/일기 엔트리가 두 번 POST되지 않는다. 꿈 등록 모달 오픈도 `dreamEntryRegistOpening` guard로 `dream-auto` 요청 중복을 막는다.

**본문 문단 저장 기준**: `RichEditor`가 전송한 TinyMCE HTML은 서버 저장 정규화(`MarkdownUtils.normalize`)를 거친다. 단일 `<p>` 안에 직접 자식 `<br>`로 문단이 나뉜 본문은 저장 시 별도 `<p>` 문단으로 분리해 레거시처럼 문단 단위 여백을 유지한다.

**저장 후 위치 복귀**: 저장 성공 시 `JournalEntryRegistModal`은 모달을 닫고 성공 알림을 표시한다. 월간/주간 기본 목록은 성공 알림 OK 이후 현재 route 기준 목록을 갱신하되 강제 스크롤하지 않고, 기존 목록 DOM 유지로 브라우저 스크롤 위치 보존에 맡긴다. 일자 상세 팝업이 열려 있으면 OK 이후 `JournalDayDtlModal` 데이터를 다시 조회하고, 팝업 내부의 `[data-id]` 엔트리 위치로 스크롤한다. 검색 팝업은 `prepare-success` 이벤트에서 결과 DOM을 준비하고, OK 이후 `success` 이벤트 payload를 받아 `#journal-entry-search-{id}`로 스크롤한다. 태그클라우드는 엔트리 타입별로 필요한 섹션만 갱신한다: `JOURNAL_DIARY`는 `fetchTagCloud({ sections: ["diary"] })`, `JOURNAL_DREAM`은 `fetchTagCloud({ sections: ["dream"] })`, `JOURNAL_NOTE`는 태그클라우드를 갱신하지 않는다.

**챕터 선택 옵션**: 엔트리 신규/수정 모달은 `journalDayId`가 있으면 `GET /api/journal/day/{journalDayId}`를 추가 조회해 해당 일자의 챕터 목록을 `chapterList` 옵션으로 채운다. 엔트리 상세 DTO에는 `chapterList`가 없을 수 있으므로, 수정 모달은 상세 응답의 `entry.chapterList`에 의존하지 않는다. `JOURNAL_DREAM`→`DREAM` 챕터만, `JOURNAL_NOTE`→`NOTE` 챕터만 후보로 쓴다. **NOTE 챕터 엔트리의 `contentType`은 `JOURNAL_DIARY`**이므로, 후보 `chapterType`은 `contentType`이 아니라 `journalChapterId`가 가리키는 챕터(또는 신규 시 caller 가 넘긴 챕터)의 `chapterType`으로 분기한다 — 노트끼리·일기끼리만 이동한다. NOTE 챕터 신규 등록(`JournalChapterItem.openEntryNew`)도 `JOURNAL_DIARY`를 전송한다. 현재 선택값이 없거나 후보에 없으면 첫 번째 후보를 선택한다(이때 `console.warn`).

**현재 Vue 동등**: ✓ 구현 완료

---

### 23. `JournalDayCard` (저널 일자 카드 — 목록 단위)

**Vue 구현**: `app/frontend-vue/src/views/journal/day/components/JournalDayCard.vue`

**사용 화면**: `JournalDayMonthly.vue`, `JournalDayWeekly.vue`, `JournalDayDaily.vue` — `v-for="day in store.dayList"`

**하위 컴포넌트**: `JournalChapterItem.vue`, `JournalEntryItem.vue`

**데이터**: `JournalDayDto` (`stores/journal.ts`) — `journalChapterList`, `journalDreamList`, `tag`, `meta` 등

**꿈 렌더링 분리 (Phase 1 가상 섹션)**: 백엔드 `JournalEntryViewProjectionHelper.applyDayEntryProjections()` 는 DREAM 챕터 안 꿈을 `journalDreamList`(꿈꾼 이름 없음) / `journalElseDreamList`(이름 있음) 로 투영한다. 분류는 `else_dream_yn` 이 아니라 `elseDreamerNm` 트림 후 비어 있지 않은지이다. 저장 시 `JournalDreamerFieldHelper` 가 이름을 정규화하고 `else_dream_yn` 을 파생한다. `JournalDayCard.vue`·`JournalDayDetailModal.vue` 는 `buildDreamVirtualSections()` 로 「꿈」·「{이름} 의 꿈」(동일 철자=한 블록) 가상 섹션을 `JournalDreamVirtualSection.vue` 로 렌더한다. `JournalEntryRegistModal.vue` 에 비필수 `elseDreamerNm` 입력이 있고, 섹션별 등록은 해당 이름을 초기값으로 넣는다.

**모달 연동**: `useJournalModalStore` — 일자/챕터/엔트리 등록·상세·수정

**현재 Vue 동등**: ✓ 구현 완료 (레거시 `JournalDayMonthlyListApp` / 텔레포트 대체)

**컨텍스트 메뉴**: ✓ 구현 완료 — 레거시 `JournalDayContextMenu.ts` → `JournalDayCard.vue` 내 Metronic ⋯ dropdown 흡수. 「주간 뷰로 이동」은 route `journal-weekly` 에서 미표시(월간·캘린더·메타 등 전용).

**일간 뷰 새 창 열기**: ✓ 구현 완료 — 컨텍스트 메뉴 "새 창으로 열기 (일자 뷰)" → `window.open(BASE_URL + /journal/daily?stdrdDt=..., "_blank", "width=...,height=...")` 새 창 강제 (features 지정)

---

### 23-1. `JournalChapterItem` (저널 챕터 아이템)

**Vue 구현**: `app/frontend-vue/src/views/journal/chapter/components/JournalChapterItem.vue`

**scss 클래스 바인딩**:
- root div: `class="journal-chapter-block"` + `:id="'journal-chapter-' + chapter.id"` — 챕터 등록/수정 후 저장 위치 스크롤 앵커
- 외부 div: `class="journal-chapter-item"` + `:data-collapsed` — journal.scss `:has(.collapsed)` 선택자 연동
- 콘텐츠 div: `:class="['journal-chapter-content', { 'collapsed': isCollapsed }]"` — `&.collapsed > * { display: none !important }` CSS 연동
- 접힘 외곽 상태선: `journal.scss` — 하위 `data-imprtc`/`data-refrnc`/전체 `data-resolved` 집계. 펼침 엔트리 `::before` 이중·삼중선과 동일 inset(완료·중요·참조 조합)

**챕터 태그 표시 규칙**: 챕터 태그는 하위 엔트리 태그를 집계한 요약이므로 **챕터가 접힌 상태일 때만 표시**.
- `v-if="tagList.length > 0 && isCollapsed"` — 접힌 상태에서만 DOM에 마운트
- CSS `d-flex` 와 충돌하므로 CSS 규칙 단독 의존 불가; `v-if` 조건으로 직접 제어

**챕터 등록 모달 (`JournalChapterRegistModal.vue`)**: 제목(`title`)은 필수 항목이 아님.
- 레거시에서 제목 없이 등록 가능 — `title` 빈 값 허용.
- `submit()` 에서 title 공백 검증 제거.

**소유권 표시·쓰기 제한** (`isCreatedBy` — 백엔드 `BaseAuditRegDto` 직렬화):
- `isCreatedBy === false`: 헤더에 `타인 작성` 배지, 수정·삭제·엔트리 등록·⋯ 메뉴·서버 접힘 변경 버튼 숨김 (읽기·클라이언트 접힘·복사·TXT는 유지)
- 수정 API 거부 시 메시지 `msg.rslt.not-owner` → 「본인이 작성한 글이 아닙니다.」(HTTP 403)

**헤더 액션 버튼** (우측 `col-3` 영역, `canManageChapter` 일 때만, 레거시 `JournalChapterItem.ts` 동형):
- 엔트리 등록 TEXT 버튼: 타입별 `저널 일기 등록` / `저널 꿈 등록` / `저널 노트 등록` + `bi-book` / `bi-moon-stars` — `openEntryNew()`
- 복사 버튼 (`bi-copy`): `copyChapter()` — 날짜·카테고리·제목 + 하위 엔트리 전체를 줄바꿈 연결 텍스트로 클립보드 복사
- TXT보내기 버튼 (`fas fa-download`, `btn-outline btn-light-primary`): `exportChapter()` — `GET /api/journal/chapter/{id}/export`
- ⋯ 컨텍스트 메뉴: 수정(`openChapterModify`) / 상태(접힘 서버 토글 `toggleCollapsedState`) / 삭제
- 접힘 화살표 버튼 (`toggle-chapter-btn`): `toggleChapter()` — 클라이언트만 접힘(`localCollapsedOverride`), 서버 POST 없음

---

### 23-2. `JournalEntryItem` (저널 엔트리 아이템)

**Vue 구현**: `app/frontend-vue/src/views/journal/entry/components/JournalEntryItem.vue`

**레거시 출처**: `legacy/static/vue/feature/journal/entry/components/JournalEntryItem.ts`

**scss 클래스 바인딩**:
- 외부 div: `itemClass` computed (contentType/isDream 기준) → `journal-dream-item` / `journal-note-item` / `journal-diary-item`
- 콘텐츠 div: `contentClass` computed → `journal-dream-content` / `journal-note-content` / `journal-diary-content`
- data 속성: `:data-imprtc`, `:data-refrnc`, `:data-resolved`, `:data-id` — journal.scss CSS 선택자 연동

**우측 액션 영역**: 댓글 버튼(단독) + 복사 버튼(`bi-copy`, `copyEntry()`) + ⋯ 컨텍스트 메뉴 (수정/이력/관련글/라이프사이클/상태/삭제)

**엔트리 복사 형식**: 날짜(`stdrdDt (요일)`) + `htmlToPlainText(content)` (TinyMCE HTML → 평문, 마크다운 재처리 이전 원문 기준). `content` 없을 때 `markdownContent` 폴백. (`#sortOrder` 없음 — 레거시 `copy()` 동일)

**클라이언트 접힘 토글**: 왼쪽 열 `#sortOrder` 아래 `bi-arrows-expand`/`bi-arrows-collapse` 버튼 — 서버 상태 무변경, `localCollapsedOverride ref`로 임시 제어

**태그 클릭**: `@click.stop="openTagContextMenu($event, tag)"` → `tagContextMenuStore.open(event, payload)`

**꿈 태그 프로필 표시**: 펼쳐진 `JOURNAL_DREAM` 엔트리에서만 `tag.list[].profileContent` 값이 있는 태그 프로필을 엔트리 태그 줄 아래에 표시한다. 접힌 엔트리와 일기/노트 엔트리는 같은 필드가 응답에 있더라도 렌더하지 않는다. 태그 줄은 기존 검색/설정용 태그 역할 그대로 유지하고, 프로필은 그 아래에서 `#태그명 | 프로필 내용` 구조로 별도 표시한다. 프로필 태그명 칸은 고정폭(`4.5rem`)으로 두어 여러 프로필 행의 본문 시작선이 맞아야 한다. 태그 프로필 본문은 `.journal-dream-tag-profile__content { white-space: pre-line; }` 로 줄바꿈을 보존한다.

**데이터 보강**: 백엔드 `TagProfileService.applyProfileContent(...)`가 사용자별 `tag_profile.content`를 `TagContentDto.profileContent`에 병합한다. 월간/주간/일간 일자 응답은 `JournalDayQueryService`, 검색/연간/상세 엔트리 응답은 `JournalEntryMyViewService`에서 `JOURNAL_DREAM`에 한정해 병합한다.

**스크롤 앵커**: root 요소는 `:id="'journal-entry-' + entry.id"`와 `:data-id="entry.id"`를 가진다. 메인 월간/주간 목록은 id를, 일자 상세 팝업 내부 스크롤은 modal 내부 `[data-id]`를 사용한다. 엔트리 수정은 현재 목록 route를 유지한 채 `JournalEntryRegistModal`을 직접 열며, 수정 모달 open/close는 URL을 변경하지 않는다.

**⋯ 메뉴 API**:
| 액션 | 엔드포인트 |
|------|-----------|
| 라이프사이클 설정 | `PUT /api/lifecycles { id, contentType, lifecycleKey }` |
| 상태 토글 | `POST /api/states { id, contentType, stateKey }` |
| 삭제 | `DELETE /api/journal/entry/{id}` |

---

### 23-3. `JournalTagContextMenu` (태그 클릭 컨텍스트 메뉴)

**Vue 구현**: `app/frontend-vue/src/views/journal/shared/components/JournalTagContextMenu.vue`

**Pinia 스토어**: `app/frontend-vue/src/stores/tagContextMenu.ts`

**레거시 출처**: `legacy/static/vue/feature/journal/day/services/journalDayTagContextMenuShell.ts`

**동작**: 일자/일기/꿈 태그 클릭 시 즉시 검색하지 않고, legacy와 같이 2단계 액션 메뉴를 연다. payload는 `{ tagId, name, ctgr, contentType }`이며 viewport clamp 후 fixed 위치에 표시한다.

**메뉴 액션**:
| 액션 | `JOURNAL_DAY` | `JOURNAL_DIARY` | `JOURNAL_DREAM` |
|------|---------------|-----------------|-----------------|
| 검색 | `journalModalStore.openTagDetail(tagId, name)` | 새 창 `/vue-app/journal/entry/search?type=DIARY&tagIds=...&tagName=...` | 새 창 `/vue-app/journal/entry/search?type=DREAM&tagIds=...&tagName=...` |
| 태그 설정 | `/api/tags/{tagId}/profile?contentType=JOURNAL_DAY` 조회 후 태그 프로필 모달 | `/api/tags/{tagId}/profile?contentType=JOURNAL_DIARY` 조회 후 태그 프로필 모달 | `/api/tags/{tagId}/profile?contentType=JOURNAL_DREAM` 조회 후 태그 프로필 모달 |

**검색 팝업 내부 동작**: 현재 route가 `journal-entry-search`이면 `검색` 액션은 새 창을 열지 않고 같은 창에서 `router.replace({ name: "journal-entry-search", query })`를 호출한다. `JournalEntrySearchPage`가 route 변경을 watch해 목록을 즉시 갱신한다.

**보존 기준**:
- 태그 클릭 자체는 검색 실행이 아니다.
- 검색은 별도 메뉴 액션이다.
- 일기/꿈 태그 검색은 현재 목록 필터가 아니라 새 창 검색 화면이다.
- 닫기는 외부 클릭, ESC, 스크롤/리사이즈에서 동작한다.

**마운트 위치**: `JournalDayLayout.vue` 및 `JournalEntrySearchPage.vue` — `<JournalTagContextMenu />` (Teleport to body)

---

### 23-3a. `JournalMetaContextMenu` (메타 클릭 컨텍스트 메뉴)

**Vue 구현**: `app/frontend-vue/src/views/journal/shared/components/JournalMetaContextMenu.vue`

**Pinia 스토어**: `app/frontend-vue/src/stores/metaContextMenu.ts`

**동작**: 메타 VIEW 헤더 `#메타` 클릭 시 태그와 동일한 fixed 팝업. payload `{ metaId, name, ctgr, unit? }`.

**메뉴 액션** (메타 VIEW):

| 액션 | 동작 |
|------|------|
| 검색 | `journalModalStore.openDayFilterModal({ type: "meta", ... })` → `JournalDayMetaModal` |
| 그래프로 보기 | `journalStore.addMetaToGraph` (최대 2, 중복 시 비활성, 꽉 차면 alert) |
| 메타 설정 | `journalModalStore.openMetaProfile` → `JournalMetaProfileModal` |

**마운트 위치**: `JournalDayLayout.vue` — `<JournalMetaContextMenu />` (Teleport to body)

**현재 Vue 동등**: ✓ 구현 완료

---

### 23-3b. `JournalMetaProfileModal` (메타 설정 모달)

**Vue 구현**: `app/frontend-vue/src/views/journal/shared/modals/JournalMetaProfileModal.vue`

**데이터**: `GET /api/journal/day/metas/{id}` → `journalModalStore.metaProfileModel` (이름·카테고리·단위·기록 수 등 조회·표시)

**현재 Vue 동등**: ⚠ 조회·표시만 (태그 프로필 수준의 색·메모 편집 API 없음)

---

### 23-4. `JournalEntrySearchPage` (저널 엔트리 검색 새 창)

**Vue 구현**: `app/frontend-vue/src/views/journal/entry/JournalEntrySearchPage.vue`

**Route**: `/journal/entry/search` (`/vue-app/journal/entry/search`로 접근)

**Layout**: `SystemLayout.vue` 하위 auth route. 새 창 검색 화면이므로 `DefaultLayout` 메뉴와 `JournalLayout` aside를 붙이지 않는다.

**Source (Legacy)**:
- `legacy/templates/view/feature/journal/entry/journal_entry_search.ftlh`
- `legacy/static/vue/feature/journal/entry/JournalEntrySearchApp.ts`

**현재 Vue 범위**:
- `type`, `tagIds`, `tagName`, `searchKeywords` query 파싱
- `GET /api/journal/entries` 조회
- 결과 목록, 태그 목록, 키워드 검색/초기화
- 결과별 수정/삭제 버튼
- 결과 태그 클릭 컨텍스트 메뉴(`JournalTagContextMenu`)와 태그 프로필 모달(`JournalTagProfileModal`) 마운트
- 검색 팝업 내부 태그 검색은 같은 창 query 갱신으로 반영
- 엔트리 수정 저장 성공 시 `JournalEntryRegistModal` `prepare-success` 이벤트로 현재 검색 목록/수정 대상 DOM을 먼저 준비하고, `success` 이벤트로 저장 위치 스크롤만 수행
- 검색 결과에 포함된 저널 해석의 수정 액션은 같은 창에서 `JournalInterpretationRegistModal`을 열어야 한다. 검색 팝업이 이 모달을 직접 마운트하며, 수정 모드는 `GET /api/journal/interpretation/{id}` 상세 조회로 제목/본문/순번을 채운 뒤 모달을 열어야 한다. 저장 성공 후 모달 내부의 `journalStore.fetchDays()` 완료 신호를 `JournalEntrySearchPage`가 감지해 현재 검색 목록을 재조회한다.
- `JournalInterpretationRegistModal`의 제목(`title`)은 필수 항목이 아니다. 제목 없이 본문만으로 해석 등록/수정이 가능해야 한다.
- 날짜 헤더는 `stdrdDt (요일)` 옆에 새 창 버튼(`bi-box-arrow-up-right`)을 표시한다. 클릭 시 월간/주간 일자 카드와 동일하게 `window.open(BASE_URL + /journal/daily?stdrdDt=..., "_blank", "width=...,height=...")`로 일자 뷰를 새 창으로 연다.

**남은 legacy 동등성 확인 대상**:
- 결과 전체 복사 버튼
- 개별 결과 복사 버튼
- TXT export 버튼
- 정렬 전환
- 고급 검색 영역의 키워드/태그 다중 입력 UX

---

### 24. `JournalLayout` (저널 공통 레이아웃·모달 호스트)

**Vue 구현**: `app/frontend-vue/src/views/journal/day/JournalDayLayout.vue`

**역할**: `<router-view>` + `JournalAside` + 저널·공통 attachable 모달 일괄 마운트

**Aside 스크롤 동작**: `journal.scss`에서 `.journal-layout-vue__aside`에 `position: sticky`, `top: 1rem`, `max-height: calc(100vh - 2rem)`, `overflow-y: auto`를 적용한다. 본문 스크롤 중 필터 패널이 viewport 안에서 따라오며, 패널 내용이 화면보다 길면 aside 내부만 스크롤된다. 연간 결산 aside(`.journal-annual-layout-vue__aside`)도 같은 규칙을 공유한다.

**마운트 모달·메뉴**: `JournalDayRegistModal`, `JournalDayDtlModal`, `JournalChapterRegistModal`, `JournalInterpretationRegistModal`, `JournalEntryRegistModal`, `JournalDayTagDtlModal`, `JournalTodoRegistModal`, `JournalDayMetaModal`, `CommentRegistModal`, `CommentListModal`, `HistoryModal`, `RelatedContentAddModal`, `JournalTagListModal`, `JournalTagProfileModal`, `JournalMetaProfileModal`, `JournalTagContextMenu`, `JournalMetaContextMenu`

**현재 Vue 동등**: ✓ 구현 완료

---

## 저널 컴포넌트 현황 요약


| 컴포넌트 | 레거시 위치 | Vue 동등 | 우선순위 |
|---------|-----------|---------|---------|
| `JournalDayViewToolbar` | `_journal_day_page_header.ftlh` (`header_btn_reg_modal`) + 탭 행 | ✓ `JournalDayViewToolbar.vue` | 완료 |
| `JournalTagCloudHeader` | `_journal_day_tag_header.ftlh` | ✓ `JournalTagCloudHeader.vue` | 완료 |
| `JournalAsideFilterHeader` | `JournalDayAsideFilterHeaderApp.ts` | ❌ MISSING (`JournalAside.vue`에 FILTER 헤더·정렬 없음) | 높음 |
| `JournalAsideYyMnthSection` | `JournalDayAsideYyMnthApp.ts` | ✓ `JournalAside.vue` (연/월/TODAY/Week/Pinpoint 모두 구현) | 완료 |
| `JournalAsideEntryFilters` | `JournalDayAsideEntryFiltersApp.ts` | ⚠ 부분 (토글·키워드; CHAPTER·고급필터 없음) | 높음 |
| `JournalAsideTodoCard` | `JournalDayAsideTodoCardApp.ts` | ❌ MISSING (할일 등록 버튼만; 목록 API 미연동) | 높음 |
