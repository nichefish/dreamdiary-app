# 저널 컴포넌트 마이그레이션 스펙 (Journal Component Spec)

> 공통 Freemarker 매크로(checkbox, modal_header 등)는 ``common/component-spec.md`` 참조.

## 저널 전용 컴포넌트 목록


### 18. `JournalTagCloudHeader` (저널 태그 클라우드 헤더)

**Source (Legacy)**: `legacy/templates/view/feature/journal/day/tag/_journal_day_tag_header.ftlh`

**Vue 구현 완료**: `app/frontend-vue/src/features/journal/day/components/JournalTagCloudHeader.vue`

**사용 화면**: `JournalDayMonthly.vue`, `JournalDayWeekly.vue`, `JournalDayCalendar.vue`, `JournalDayDaily.vue` — `.card.post > .card-header` 내부 (`v-if="store.showTagCloud"`). 일간 탭과 팝업은 동일한 `JournalDayDaily.vue`에서 선택 날짜 하루의 태그클라우드를 표시한다.

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
| 태그 클릭 → 일자 필터 모달 | `journalModalStore.openDayFilterModal({ type: "tag", id: tag.id, name: tag.name, ctgr: tag.ctgr })` |

**상태 / API**:
| 항목 | 위치 |
|------|------|
| `store.tagCloud` | `useJournalStore` — `JournalTagCloud { dayTagList, diaryTagList, dreamTagList }` |
| `store.tagCloudLoading` | `useJournalStore` |
| `store.fetchTagCloud()` | 전체 갱신: `GET /api/journal/day/tags`, `GET /api/journal/entry/tags?type=DIARY`, `GET /api/journal/entry/tags?type=DREAM`. 일간은 `stdrdDt`, 주간은 `weekStartDt`, 월간·달력은 `yy`/`mnth`를 기간 조건으로 사용한다. 같은 섹션·기간의 진행 중 조회는 Promise를 공유해 HTTP 1회로 합친다. |
| `store.fetchTagCloud({ sections })` | 부분 갱신: `sections: ["day" \| "diary" \| "dream"]` 에 포함된 태그클라우드 섹션만 조회·반영. 기간이 다른 요청은 각각 실행하고 섹션별 마지막 시작 요청만 반영하며, 실패한 진행 중 요청은 제거해 다음 호출에서 재시도한다. |
| `store.resetTagCloudState()` | 로그아웃·세션 만료 시 목록·오류·로딩·진행 중 요청을 초기화하고 요청 세대를 올린다. 이전 사용자 세대의 늦은 성공·실패 응답과 로딩 완료 처리는 새 세션 상태에 반영하지 않는다. |
| `TagCloudItem` | `{ id: number|string, name: string, ctgr?: string, contentSize: number, tagClass?: string, textClass?: string }` |

**태그 크기 클래스**: `.ts-1` ~ `.ts-9` (`src/styles/components/tag.scss`) — `tagClass` 필드로 전달. 빈도 비율로 base를 구한 뒤 프로필 크기 고정(`cloudSizeLock`)이 MAX면 `ts-9`, MIN이면 `ts-1`로 고정한다(AUTO는 빈도 산출; `TagProfileService.applyVisualSemantic` / `TagCloudSizeSupport`). 색은 `textClass`(시각 의미)로 별도.

**가로 정렬 계약**: 일자/일기/꿈 태그 3행의 태그 목록 시작 x좌표는 동일해야 한다. `꿈 태그` 라벨이 한 글자 짧다는 이유로 태그 목록이 왼쪽으로 들어오면 실패다. Vue 구현은 라벨 컬럼에 `.journal-tag-header__label { width: 6.25rem; justify-content: center; }`를 적용해 3행 모두 같은 라벨 폭을 사용한다.

**초기화 타이밍**:
- 월간/주간/달력/일간 parent view가 `store.viewType`과 기간 상태를 먼저 설정한 뒤 `store.fetchTagCloud()` 호출
- `JournalTagCloudHeader`는 mounted 선조회를 하지 않는다. 자식 mounted가 부모 초기화보다 먼저 실행되어 직전 월간 상태로 조회되는 것을 방지한다.
- `[store.yy, store.mnth, store.weekStartDt, store.dailyStdrdDt, store.viewType]` watch → 기간/뷰 변경 시 재호출
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
- 스레드가 없는 기간에는 요약 행을 표시하지 않는다. 스레드 선택 시 현재 기간 화면을 유지하고 전역 `JournalThreadDetailModal`을 직접 연다.

**상태 / API**:
- `GET /api/journal/threads/period-summary`
- 주간 파라미터: `viewType=WEEKLY&weekStartDt=YYYY-MM-DD`
- 월간 파라미터: `viewType=LIST&yy=YYYY&mnth=M`
- 연간 파라미터: `viewType=ANNUAL&yy=YYYY`
- 응답 항목: `threadId`, `title`, nullable `prefix`(`id`, `name`, `color`, `activeYn`), `entryCount`, `firstEntryDate`
- 기간 집계는 현재 사용자 소유의 활성 스레드·활성 소속·활성 엔트리만 포함한다.

**현재 구현 상태**: ✓ 구현 완료 — 기간 집계 DTO·repository·service와 `GET /api/journal/threads/period-summary`, `useJournalThreadStore`의 기간별 조회 상태·이전 요청 폐기, `JournalPeriodThreadSummary` UI·월간 10개 이후 펼치기를 구현한다. 기간 집계는 `prefix_content`를 LEFT JOIN해 스레드의 nullable Prefix를 함께 반환하고, 공통 요약 버튼은 스레드 목록과 같은 이름·색 배지를 제목 앞에 표시한다. Prefix가 없으면 제목만 표시하며 비활성 과거 Prefix도 연결이 남아 있으면 표시한다. 조회 실패는 빈 기간으로 가장하지 않고 오류 문구를 표시한다. 행 라벨은 기간 문맥이 화면에 있으므로 `스레드`다. 엔트리 소속 추가·해제·새 스레드 생성과 엔트리 삭제 성공 시 `JournalEntryItem`이 `JournalThreadStore.refreshPeriodSummary()`를 호출해 마지막 조회 조건으로 이 요약을 재조회한다(store가 조건 보유, 요약 컴포넌트는 언마운트 시 조건을 비워 비활성 재조회 방지). 연간 결산(`JournalAnnualDetail`)도 같은 컴포넌트를 재사용한다 — day store 대신 옵셔널 `query` prop(`{ viewType: "ANNUAL", yy }`)을 받으면 그걸 쓰고 없으면 기존처럼 day store에서 파생한다. ANNUAL은 `GET /journal/threads/period-summary?viewType=ANNUAL&yy=`(백엔드 `findPeriodSummaryByYear`, 월간과 동일 엔트리 수 내림차순 정렬)로 그 해 전체 스레드를 집계하고 월간처럼 10개 초과 펼치기를 적용한다.

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

**주간 미니 달력 주 범위 하이라이트**: WEEKLY aside 는 `JournalAsideMiniCalendar`(일요일 시작)를 재사용하고 `week-start` prop 으로 선택된 주를 전달한다. `[weekStart … +6일]` 셀이 `is-in-week`(옅은 파란 band)로 칠해지며(월~토 한 줄 + 다음 줄 일요일), 클릭한 날은 `is-selected`(진한 파랑). 날짜 클릭 시 `getWeekStartDateStr`로 그 날이 속한 주로 이동한다.

**표시 문구 i18n**: 월 숫자 뒤 단위, 날짜 선택·Pinpoint 저장·복귀 tooltip과 월~일 요일 축약명은 현재 locale의 클라이언트 카탈로그를 사용한다. locale 변경은 연월·선택 일자·Pinpoint 저장값·route query를 변경하지 않는다.

---

### 21. `JournalAsideEntryFilters` (어사이드 엔트리 필터)

**Source (Legacy)**: `legacy/static/vue/feature/journal/day/JournalDayAsideEntryFiltersApp.ts`

**현재 Vue 동등**: ✓ — `JournalAside.vue` 인라인. 블록 A–C·라이프사이클·어사이드 목록 키워드 구현. 블록 D(고급필터 아코디언)는 **이식 대상 아님** (`vue-screen-overview.md` 필터·검색 정책).

**5개 블록 구조**:

**블록 A — TAGCLOUD 토글**: ✓ 구현
- `store.showTagCloud` — 변경 시 TAGCLOUD ON이면 `store.fetchTagCloud()` 호출

**블록 B — DIARIES + CHAPTER PREFIXES + 일기 키워드**:
```html
<!-- B-1: DIARIES 토글 -->
<input type="checkbox" id="toggleDiaries" :checked="store.showDiaries" @change="toggleDiaries">
<!-- B-2: CHAPTER PREFIXES 체크박스 목록 -->
<div id="chapterCtgrFilterSection" class="d-flex flex-column ps-3 gap-1">
    <label v-for="prefix in chapterPrefixOptions" :key="prefix.id"
           class="form-check form-check-sm form-check-custom form-check-solid cursor-pointer">
        <input class="form-check-input w-16px h-16px"
               type="checkbox"
               :checked="isChapterPrefixSelected(prefix.id)"
               @change="toggleChapterPrefix(prefix.id)">
        <span class="form-check-label text-muted fs-8">[{{ prefix.name }}]</span>
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

**챕터 말머리 데이터**:
- `GET /api/my/prefixes/options?contentType=JOURNAL_CHAPTER_DIARY`와 `GET /api/my/prefixes/options?contentType=JOURNAL_CHAPTER_NOTE`
- `journalModalStore.prefetchChapterPrefixes(chapterType)` — DIARY와 NOTE의 활성 개인 Prefix 목록을 유형별로 분리해 세션 동안 보관한다. 인자 없이 호출하면 두 목록을 함께 선제 조회해 모달 오픈 시 로딩 없이 사용한다.
- 같은 챕터 유형의 동시 호출은 진행 중인 Promise를 공유하고, 서로 다른 유형은 독립된 조회·완료·실패 상태를 유지한다.
- 정상 빈 목록은 적재 완료로 캐시하고 Aside에 `등록된 말머리가 없습니다.`를 표시한다. 조회 실패는 별도 실패 문구를 표시하고 완료로 캐시하지 않아 다음 진입에서 재시도한다.
- 로그아웃·세션 만료 시 `resetCategoryMaps()`가 태그·메타 map과 함께 개인 챕터 Prefix 목록·적재 상태도 비워 다음 사용자의 목록이 섞이지 않게 한다.
- 시스템 요약은 `summaryYn=Y`로 판정하고 고정 `요약` 명칭을 표시하며 Prefix 선택기를 제공하지 않는다. DREAM도 Prefix를 허용하지 않는다. 일자 내 챕터 순서는 정규화·조회 모두 **요약 맨 앞 → 일반 → DREAM 맨 뒤**이다. `sort_order`는 **일반 챕터끼리만 1..N** 순번을 매기고, 요약·DREAM 은 위치가 정렬 버킷(`summaryYn`·`chapterType`)으로 고정되므로 순번 밖(`sort_order=0`)이다. 따라서 첫 일반 챕터가 `#1`이며 요약은 번호를 차지하지 않는다. 순번 부여의 SSOT는 `normalizeSortOrder`(재정렬·이동)와 `applyNewNormalChapterSortOrder`(신규)다. 재배치 `insert()` 도 같은 버킷 규칙으로 요약·DREAM 을 `sort_order=0` 으로 유지하므로, 최종 `normalizeSortOrder` 전에도 순번 밖 불변식이 깨지지 않는다(insert 가 요약·DREAM 에 순번을 쓰면 이후 정규화의 JPA/MyBatis staleness 로 그 값이 남을 수 있어 방지).
- 기존 비활성 Prefix가 선택된 일반 챕터는 수정 화면에서 비활성 옵션을 표시하고 같은 선택 유지 저장만 허용한다.
- DB 계약: 시스템 요약은 `journal_chapter.summary_yn`, 일반 챕터의 사용자 말머리는 `prefix_content`가 각각 담당한다.

**챕터 선택 → store 연동**:
- 체크박스 ON: 해당 ID가 `store.chapterPrefixIds`에 없으면 추가 → `store.fetchDays()`
- 체크박스 OFF: 해당 ID를 `store.chapterPrefixIds`에서 제거 → `store.fetchDays()`
- `DIARIES=false` 상태에서는 챕터 말머리 필터 UI를 렌더링하지 않고 기존 선택값은 보존한다.

**블록 C — DREAMS + 꿈 LIFECYCLE + 꿈 키워드**: 블록 B와 동일 구조, 챕터 말머리 sub-block 없음
- 꿈 LIFECYCLE select: `store.dreamLifecycleKey` — 변경 시 `store.fetchDays()`

**부모 토글과 하위 필터 계약**:
- `TAGCLOUD`는 엔트리 종류와 독립된 표시 토글이므로 DIARIES/DREAMS 하위에 두지 않는다.
- `CHAPTER PREFIXES`, 일기 LIFECYCLE, 일기 키워드는 `DIARIES` 토글 하위에 배치한다.
- 꿈 LIFECYCLE, 꿈 키워드는 `DREAMS` 토글 하위에 배치한다.
- 부모 토글이 OFF이면 해당 하위 필터 UI는 렌더링하지 않는다.
- 부모 토글 OFF는 하위 필터 값을 삭제하지 않는다. 다시 ON으로 돌리면 기존 하위 필터 값이 그대로 적용된다.
- `ENTRY FILTER` 레이블은 TAGCLOUD/DIARIES/DREAMS 필터 묶음 아래에 표시한다.

**라이프사이클 필터**:
- 일기 LIFECYCLE select: `store.diaryLifecycleKey`
- 꿈 LIFECYCLE select: `store.dreamLifecycleKey`
- 옵션: 전체(`""`), 진행 중(`OPEN`), 보류(`PENDING`), 완료(`RESOLVED`)
- `OPEN`은 저장 row와 캐시 맵 항목이 없는 기본 상태이며, 필터는 이 부재 상태를 `OPEN`으로 해석한다.
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
            <a class="btn btn-sm btn-icon btn-primary" title="할일 추가" @click.prevent="openTodoRegist">
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
// modalStore.openTodoRegist({ yy: store.yy, mnth: store.mnth }) 호출
// JournalTodoRegistModal.vue가 처리 — 등록 성공 시 journalStore.fetchTodos() 재호출
```



### 22-1. `JournalDayViewToolbar` (저널 일자 뷰 상단 툴바)

**Vue 구현 완료**: `app/frontend-vue/src/features/journal/day/components/JournalDayViewToolbar.vue`

**레거시 출처**:
- 본문 상단 탭 행: `journal_day_monthly.ftlh` / `journal_day_weekly.ftlh` — `nav-tabs-line` 4탭
- 일자 등록 버튼: `_journal_day_page_header.ftlh` — `@component.header_btn_reg_modal` (`data-journal-day-action=reg-modal` → `JournalDayRuntimeService`)

**사용 화면**: `JournalDayWeekly.vue`, `JournalDayMonthly.vue`, `JournalDayCalendar.vue`, `JournalDayMeta.vue` — 카드(`.card.post`) 위 첫 행

**DOM 구조**:
- sticky flex 행 (`journal-day-view-toolbar`, `justify-content-between`): 좌측 `nav-tabs` (router-link 4개) + 우측 액션 영역 (`d-none d-md-flex pe-5 mt-3`)
- 탭 라벨: `일간 VIEW` / `주간 VIEW` / `월간 VIEW` / `달력 VIEW` / `메타 VIEW`
- 등록 버튼: `btn btn-sm btn-light-primary btn-outlined`, 아이콘 `bi-calendar-plus`, 라벨 「저널 일자 등록」
- aside 숨김 시 액션 영역 맨 오른쪽: 구분자 + `bi-layout-sidebar-inset-reverse` 열기 버튼. 일정 툴바와 같은 순서이며 aside가 보이면 렌더하지 않는다.
- 탭·검색 placeholder/tooltip·등록 버튼 문구는 현재 locale의 클라이언트 카탈로그를 사용한다.

**동작**:
| 액션 | Vue |
|------|-----|
| 저널 일자 등록 클릭 | `useJournalModalStore().openDayRegist()` → `JournalDayRegistModal` (`JournalLayout` 마운트) |
| 탭 전환 | `router-link` — `journal-daily-tab` / `journal-weekly` / `journal-monthly` / `journal-calendar` / `journal-meta` |
| 일기 키워드 검색 | `v-model="localDiaryKw"`, Enter/버튼 클릭 → 일기 전체검색 새 탭 오픈 |
| 꿈 키워드 검색 | `v-model="localDreamKw"`, Enter/버튼 클릭 → 꿈 전체검색 새 탭 오픈 |
| 고급 필터 | `asideStore.toggle()` — 사이드 필터 패널 표시/숨김 |
| 우측 끝 aside 열기 | aside 숨김 시 `asideStore.show()` — 사이드 필터 패널 표시 |
| 일정 등록 / 개인 일정 | 저널 날짜·엔트리 맥락을 전달하지 않는 교차 도메인 액션이므로 저널 툴바에서는 미제공. 일정 화면에서만 제공 |
| 태그 카테고리 동기화 | `syncCategoryMaps()` (`journalCategoryMaps`) — 4종 categoryMap 서버 재조회. 앱 세션의 URL별 최초 조회는 진행 중 Promise를 공유해 동시 호출을 HTTP 1회로 합치며, 초기화·동기화·저장 응답 적용 전에 시작된 구 응답은 URL별 버전 검증으로 폐기한다. |

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

채팅 RAG 출처 딥링크용 읽기 전용 모달이다. `GET /api/journal/entry/{id}`로 조회한 `markdownContent`를 목록과 동일한 `journal-content` HTML로 표시하고, 날짜·제목·태그·꿈꾼(해당 시)만 보여 준다. 저장 폼은 없다. footer 는 왼쪽에 「해당 글로 이동」(`goToEntry` — `router.push({ name: "journal-daily-tab", query: { stdrdDt } })` 로 엔트리 일자의 일간 뷰로 이동 후 모달 닫기; `stdrdDt` 없으면 비활성)을, 오른쪽에 편집·닫기를 둔다(`justify-content-between`). footer 편집은 `openEntryModifyFromView`로 전환하며 `JOURNAL_REFLECTION`이면 `JournalReflectionRegistModal`, 그 외는 `JournalEntryRegistModal`을 연다. 비팝업 라우트에서는 `App.vue`가 전역 마운트한다.

### 22-4. `JournalEntryRegistModal` 등록/수정 폼 여백

**Vue**: `JournalEntryRegistModal.vue` → `journal-entry-regist-modal__body`, `journal-entry-regist-form`

**스타일**: `journal.scss` 에서 데스크톱 좌우 `3rem`, 모바일 좌우 `1.25rem` padding을 적용한다. 제목/본문/태그 입력이 모달 가장자리에 붙어 보이지 않게 하며, TinyMCE iframe 본문은 `RichEditor.vue` 의 `content_style` 로 `12px 16px` 내부 padding을 둔다.

**중복 제출 방지**: 저장 클릭 즉시 `submitting` guard를 세운 뒤 확인창을 띄운다. 확인창이 떠 있는 동안 재클릭되어도 두 번째 `submit()`은 무시되어 같은 꿈/일기 엔트리가 두 번 POST되지 않는다. 꿈 등록 모달 오픈도 `dreamEntryRegistOpening` guard로 `dream-auto` 요청 중복을 막는다.

**본문 문단 저장 기준**: `RichEditor`가 전송한 TinyMCE HTML은 서버 저장 정규화(`MarkdownUtils.normalize`)를 거친다. 단일 `<p>` 안에 직접 자식 `<br>`로 문단이 나뉜 본문은 저장 시 별도 `<p>` 문단으로 분리해 레거시처럼 문단 단위 여백을 유지한다.

**저장 후 위치 복귀**: 저장 성공 시 `JournalEntryRegistModal`은 모달을 닫고 성공 알림을 표시한다. 월간/주간 기본 목록은 성공 알림 OK 이후 현재 route 기준 목록을 갱신하되 강제 스크롤하지 않고, 기존 목록 DOM 유지로 브라우저 스크롤 위치 보존에 맡긴다. 일자 상세 팝업이 열려 있으면 OK 이후 `JournalDayDetailModal` 데이터를 다시 조회하고, 팝업 내부의 `[data-id]` 엔트리 위치로 스크롤한다. 검색 팝업은 `prepare-success` 이벤트에서 결과 DOM을 준비하고, OK 이후 `success` 이벤트 payload를 받아 `#journal-entry-search-{id}`로 스크롤한다. 스레드 상세가 열려 있으면 route와 무관하게 상세 본문·집계 태그·소속 엔트리를 다시 조회하고, 배경이 주간·월간·일간이면 배경 목록도 재조회하되 배경 스크롤은 하지 않는다. 검색 배경은 기존 로컬 결과 교체를 함께 수행한다. 태그클라우드는 엔트리 타입별로 필요한 섹션만 갱신한다: `JOURNAL_DIARY`는 `fetchTagCloud({ sections: ["diary"] })`, `JOURNAL_DREAM`은 `fetchTagCloud({ sections: ["dream"] })`, `JOURNAL_NOTE`는 태그클라우드를 갱신하지 않는다. 신규 엔트리 등록 성공 시 선택된 `journalChapterId`를 목록·상세 갱신 동안 일회성 펼침 대상으로 표시하고, 같은 챕터의 모든 마운트 인스턴스가 로컬 펼침을 적용한다. 완료 자동 접힘과 서버 COLLAPSED보다 이번 화면의 신규 콘텐츠 노출을 우선하지만 서버 상태는 삭제하지 않으며, 엔트리 수정 저장에는 적용하지 않는다.

**챕터 선택 옵션**: 엔트리 신규/수정 모달은 `journalDayId`가 있으면 `GET /api/journal/day/{journalDayId}`를 추가 조회해 해당 일자의 챕터 목록을 `chapterList` 옵션으로 채운다. 엔트리 상세 DTO에는 `chapterList`가 없을 수 있으므로, 수정 모달은 상세 응답의 `entry.chapterList`에 의존하지 않는다. `JOURNAL_DREAM`→`DREAM` 챕터만, `JOURNAL_NOTE`→`NOTE` 챕터만 후보로 쓴다. **NOTE 챕터 엔트리의 `contentType`은 `JOURNAL_DIARY`**이므로, 후보 `chapterType`은 `contentType`이 아니라 `journalChapterId`가 가리키는 챕터(또는 신규 시 caller 가 넘긴 챕터)의 `chapterType`으로 분기한다 — 노트끼리·일기끼리만 이동한다. NOTE 챕터 신규 등록(`JournalChapterItem.openEntryNew`)도 `JOURNAL_DIARY`를 전송한다. 현재 선택값이 없거나 후보에 없으면 첫 번째 후보를 선택한다(이때 `console.warn`).

**엔트리 Prefix 선택**: 등록·수정 모달은 논리 엔트리 유형별 `GET /api/my/prefixes/options?contentType=...` 활성 선택지를 사용해 nullable 단일 `prefixId`를 전송한다. 일기·꿈은 각각 `JOURNAL_DIARY`·`JOURNAL_DREAM`, 노트는 소속 챕터의 `chapterType=NOTE`에서 `JOURNAL_NOTE` Scope를 확정한다. NOTE의 `journal_entry.content_type`과 `prefix_content.ref_content_type`은 기존 영속 계약인 `JOURNAL_DIARY`를 유지하고 Scope 검증 타입만 분리한다. 정상 빈 목록은 캐시하고 조회 실패는 다음 모달 진입에서 재시도하며, 로그아웃 시 캐시 세대를 바꿔 이전 사용자의 늦은 응답을 폐기한다. 비활성 과거 선택은 수정 화면에 비활성 옵션으로 표시해 유지·해제할 수 있지만 신규 선택은 서버가 거부한다.

**등록·수정 정보 조회 실패**: 꿈 등록 정보 또는 엔트리 수정 정보 조회가 실패하면 불완전한 폼을 닫고, 서버 메시지를 우선 표시하며 없으면 현재 locale의 fallback을 사용한다.

**i18n**: 일기·꿈·노트·기본 엔트리별 모달 제목, 기준 날짜 요일, 필드 레이블, 글자 수 안내, 말머리·제목·순서·꿈꾼 placeholder, 저장·닫기·확인·결과 fallback은 현재 locale의 클라이언트 카탈로그를 사용한다. 저장 API 응답에 `message`가 있으면 서버 메시지를 우선 표시한다. locale 변경은 표시 문구만 바꾸며 챕터·말머리 선택, 태그·꿈꾼 값과 저장 payload는 유지한다.

**현재 Vue 동등**: ✓ 구현 완료

---

### 22-5. `JournalTodoRegistModal` 등록/수정 폼

**Vue 구현**: `app/frontend-vue/src/features/journal/todo/modals/JournalTodoRegistModal.vue`

**데이터·동작**: `useJournalModalStore.todoRegistModel`의 대상 년월·카테고리·제목·순서·본문·태그를 편집한다. 신규는 `POST /api/journal/todos`, 수정은 `POST /api/journal/todo/{id}`로 multipart form data를 전송한다. 성공 시 모달을 닫고 성공 알림 확인 후 `refreshJournalDaysForRoute()`로 현재 route 기준 저널 목록을 갱신한다.

**i18n**: 모달 제목·대상 년월·필드·placeholder·안내·저장·닫기·제목 필수 검증·확인·결과 fallback은 현재 locale의 클라이언트 카탈로그를 사용한다. API 응답에 `message`가 있으면 서버 메시지를 우선 표시한다.

**현재 Vue 동등**: ✓ 구현 완료

---

### 22-6. `JournalReflectionRegistModal` 등록/수정 폼

**Vue 구현**: `app/frontend-vue/src/features/journal/reflection/modals/JournalReflectionRegistModal.vue`

**데이터·동작**: `useJournalModalStore` reflection 등록 모델의 제목·본문을 편집한다. 제목은 선택값이다. Reflection 은 대상 필수(About-A)라 모달은 제목·본문만 두고 태그·챕터 선택 UI 는 없다. 대상(`refId`/`refContentType`)은 「해석 등록」 진입 시 payload 로 정해진다. 영속 `content_type`은 `JOURNAL_REFLECTION`이고 `ref_content_type`은 대상 타입이다. 일기 태그클라우드·결산 DIARY 집계·챕터 접힘 태그 요약과 검색 태그·state 스코프는 `JOURNAL_DIARY` 단일 축을 사용한다. Reflection 본문 키워드는 대상 일기를 매칭시킨다(원문·해석 한 몸 EXISTS, `REFLECTION_ONE_TYPE.md` §4). Reflection 은 별도 Aggregate(`journal_reflection`)라 전용 엔드포인트로 쓴다. 신규는 `POST /api/journal/reflections`, 수정은 `POST /api/journal/reflection/{id}`, 삭제는 `DELETE /api/journal/reflection/{id}`, 수정 로드는 `GET /api/journal/reflection/{id}` 로 처리한다(multipart form data). 서버 쓰기는 `JournalReflectionService`가 담당하며 대상 필수(About-A: `refId`/`refContentType`)를 검증한다. 저장 성공 시 응답의 `targetReflectionList`와 `targetLifecycleKey`로 대상 엔트리의 Reflection 목록·라이프사이클을 부분 교체하고, 응답에 갱신 데이터가 없을 때 호스트를 다시 조회한다.

**i18n**: 모달 제목·기준 날짜 요일·필드·placeholder·안내·저장·닫기·확인·결과 fallback은 현재 locale의 클라이언트 카탈로그(`journal.reflection.*`)를 사용한다. API 응답에 `message`가 있으면 서버 메시지를 우선 표시한다.

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

**일간 뷰 새 창 열기**: ✓ 구현 완료 — 컨텍스트 메뉴 "새 창으로 열기 (일자 뷰)" → `window.open(BASE_URL + /journal/daily-popup?stdrdDt=..., "_blank", "width=...,height=...")` 새 창 강제 (features 지정)

**사용자 휴가 표시**: ✓ 구현 완료 — `JournalDayVacationIndicator.vue`가 서버 확정 `vacationDayStatus`와 `vacationReasonList`를 월간·주간·일간 카드, 일자 상세, 메타 일자 목록에 동일하게 표시한다. `FULL_DAY`는 공휴일·주말과 같은 날짜 빨강 + 휴가 배지, `AM_HALF`·`PM_HALF`는 날짜 색 유지 + 구분 배지, `UNKNOWN`은 경고 배지다. 프론트는 제목이나 사유로 휴가 상태를 추정하지 않는다.

---

### 23-1. `JournalChapterItem` (저널 챕터 아이템)

**Vue 구현**: `app/frontend-vue/src/features/journal/chapter/components/JournalChapterItem.vue`

**Reflection 표시 = embed 전용**: `journalEntryList`(journal_entry 행: 일기·꿈·노트)를 그대로 1급 `JournalEntryItem` 행으로 렌더한다. Reflection 은 별도 Aggregate(journal_reflection)이라 이 목록에 들어오지 않고, 대상 엔트리 아래 embed(`JournalReflectionItem`, `entry.reflectionList`)로만 표시된다. 챕터 차원의 1급 Reflection 행·중복 제거(dedup)는 없다.

**scss 클래스 바인딩**:
- root div: `class="journal-chapter-block"` + `:id="'journal-chapter-' + chapter.id"` — 챕터 등록/수정 후 저장 위치 스크롤 앵커. `is-all-pending` / `is-all-resolved`는 하위 엔트리 집계 라이프사이클(`aggregateLifecycleKey`)을 반영한다.
- 외부 div: `class="journal-chapter-item"` + `:data-collapsed` — journal.scss `:has(.collapsed)` 선택자 연동
- 콘텐츠 div: `:class="['journal-chapter-content', { 'collapsed': isCollapsed }]"` — `&.collapsed > * { display: none !important }` CSS 연동
- 접힘 외곽 상태선: `journal.scss` — 전체 RESOLVED/PENDING은 루트 클래스, 중요·참조는 하위 `data-imprtc`/`data-refrnc` `:has` 조합. 펼침 엔트리 `::before` 이중·삼중선과 동일 inset(완료·중요·참조 조합)
- 디버그: `localStorage("debug_collapse")==="true"`이면 루트에 접힘·집계 메타 스트립을 표시하고 `toggleChapter` 콘솔 로그를 남긴다(엔트리·리플렉션과 동일 키).
- 접힘 요약: 챕터가 접히면 태그와 함께 하위 엔트리의 `threadList`를 `threadId`로 중복 제거한 스레드 버튼을 `.journal-chapter-content` 바깥에 표시한다. 스레드 순서는 엔트리·소속 목록의 최초 등장 순서를 보존하고, 버튼 클릭은 기존 엔트리 스레드 칩과 동일하게 현재 저널 화면 위에 전역 상세 모달을 직접 연다.

**챕터 태그 표시 규칙**: 챕터 태그는 하위 엔트리 태그를 집계한 요약이므로 **챕터가 접힌 상태일 때만 표시**. 챕터는 **자체 태그를 소유하지 않는다** — 엔티티 `TagEmbed` 를 제거했고(tag_content 0행, vestigial), 화면 태그는 100% `JournalDayViewHelper.applyChapterTagSummary` 가 소속 `JOURNAL_DIARY` 엔트리 태그를 집계해 `JournalChapterDto.tag` 에 채운 것이다. 저장 경로는 태그를 영구화하지 않는다.
- `v-if="tagList.length > 0 && isCollapsed"` — 접힌 상태에서만 DOM에 마운트
- CSS `d-flex` 와 충돌하므로 CSS 규칙 단독 의존 불가; `v-if` 조건으로 직접 제어

**챕터 헤더 식별**: 헤더는 유형 라벨 + 개인 Prefix 이름에 이어 `chapter.title`이 있으면 `· {제목}`을 헤더 톤(`text-gray-700`, 헤더 `fw-bolder` 상속)으로 인라인 표시한다(접힘·펼침 무관 항상). 같은 Prefix의 챕터가 한 일자에 여러 개일 때 접힘 상태에서도 제목으로 구분하기 위함이며, Prefix가 없으면 유형·제목만 표시한다. 시스템 요약은 Prefix 대신 고정 `요약`을 표시한다. 기존 헤더 행에 붙여 세로 높이를 늘리지 않는다.

**챕터 등록 모달 (`JournalChapterRegistModal.vue`)**: 제목(`title`)은 필수 항목이 아님.
- 레거시에서 제목 없이 등록 가능 — `title` 빈 값 허용.
- `submit()` 에서 title 공백 검증 제거.
- 일반 DIARY/NOTE는 챕터 유형별 개인 Prefix 목록에서 `prefixId` 0..1개를 선택한다. 일기 챕터는 `JOURNAL_CHAPTER_DIARY`, 노트 챕터는 `JOURNAL_CHAPTER_NOTE` 목록을 사용하며, 챕터의 attachable 정체성은 `JOURNAL_CHAPTER`로 유지한다. 유형(`model.chapterType`)에 따라 표시 목록이 달라진다(`modalStore.chapterPrefixOptionsFor(chapterType)`). 사용자가 유형 select를 바꾸면(`@change`) 이전 유형 목록의 선택은 새 유형 목록에 없으므로 `prefixId`를 초기화한다. 모달을 여는 시점(수정 폼)의 기존 선택은 보존한다.
- 시스템 요약은 고정 `요약`을 표시하고 Prefix 선택기를 숨긴다. DREAM도 사용자 Prefix 선택 대상이 아니므로 선택기를 표시하지 않는다.
- 기준 날짜 요일과 제목·필드·선택지·버튼·확인·결과 fallback 문구는 현재 locale의 클라이언트 카탈로그를 사용하고, 등록·수정·일자 이동 API가 서버 `message`를 반환하면 그 값을 우선 표시한다.

**소유권 표시·쓰기 제한** (`isCreatedBy` — 백엔드 `BaseAuditRegDto` 직렬화):
- `isCreatedBy === false`: 헤더에 `타인 작성` 배지, 수정·삭제·엔트리/리플렉션 등록·⋯ 메뉴·서버 접힘 변경 버튼 숨김 (읽기·클라이언트 접힘·복사·TXT는 유지)
- 수정 API 거부 시 메시지 `msg.rslt.not-owner` → 「본인이 작성한 글이 아닙니다.」(HTTP 403)

**헤더 액션 버튼** (우측 `col-3` 영역, `canManageChapter` 일 때만, 레거시 `JournalChapterItem.ts` 동형):
- 엔트리 등록 TEXT 버튼: 타입별 `저널 일기 등록` / `저널 꿈 등록` / `저널 노트 등록` + `bi-book` / `bi-moon-stars` — `openEntryNew()`
- 복사 버튼 (해석 포함/제외 2버튼): `copyChapter(true)`(`bi-copy`) — 날짜·카테고리·제목 + 하위 엔트리 전체를 줄바꿈 연결 텍스트로 클립보드 복사. 각 엔트리 본문 밑에 그 엔트리를 문(target) 리플렉션 본문을 빈 줄로 이어 붙인다(원문·해석은 한 몸, 마커 없음). 하위 엔트리에 리플렉션이 하나라도 있으면(`chapterHasReflections`) `copyChapter(false)`(`bi-clipboard`, 해석 제외) 버튼을 추가로 노출한다
- TXT보내기 버튼 (`fas fa-download`, `btn-outline btn-light-primary`): `exportChapter()` — `GET /api/journal/chapter/{id}/export`
- ⋯ 컨텍스트 메뉴: 수정(`openChapterModify`) / 상태(접힘 서버 토글 `toggleCollapsedState`) / 삭제 — **시스템 요약 챕터(`isSummaryChapter`)에서는 숨긴다**(사용자 편집 대상이 아님). 엔트리 등록·복사·TXT 버튼은 요약에도 유지한다.
- 접힘 화살표 버튼 (`toggle-chapter-btn`): `toggleChapter()` — 클라이언트만 접힘(`localCollapsedOverride`), 서버 POST 없음
- 챕터 유형·소유권 배지·타입별 등록 버튼·액션 툴팁·메뉴·빈 상태 문구는 현재 locale의 클라이언트 카탈로그를 사용한다.
- 소유권 경고·삭제 확인·삭제 결과 fallback·클립보드 복사 결과와 복사 날짜 헤더의 요일은 현재 locale의 클라이언트 카탈로그를 사용하며, 삭제 API가 서버 `message`를 반환하면 그 값을 우선 표시한다.

**시스템 요약 첫 엔트리 강조 (신규)**: 레거시 대응 없음. `chapter.summaryYn === 'Y'` 인 챕터에 한해(프리뷰용 `findSummaryChapter` 의 non-DREAM fallback 은 쓰지 않는다 — 요약 챕터가 없는 날은 강조하지 않는다) 그 챕터의 첫 non-empty 엔트리(`findFirstNonEmptyEntry`, sortOrder→id)에만 `:is-summary="true"` 를 넘긴다. 그 엔트리가 그날 전체 요약임을 나타낸다. 챕터가 접히면 하위 엔트리가 `display:none` 이므로 강조도 함께 사라진다(의도된 동작 — 접힘을 뚫고 표시하지 않는다). 일간·주간·월간이 같은 `JournalDayCard → JournalChapterItem → JournalEntryItem` 체인을 쓰므로 이 판정 한 곳으로 세 뷰가 모두 적용된다.

---

### 23-2. `JournalEntryItem` (저널 엔트리 아이템)

**Vue 구현**: `app/frontend-vue/src/features/journal/entry/components/JournalEntryItem.vue`

**레거시 출처**: `legacy/static/vue/feature/journal/entry/components/JournalEntryItem.ts`

**scss 클래스 바인딩**:
- 외부 div: `itemClass` computed (contentType/isDream 기준) → `journal-dream-item` / `journal-note-item` / `journal-diary-item`
- 콘텐츠 div: `contentClass` computed → `journal-dream-content` / `journal-note-content` / `journal-diary-content`
- data 속성: `:data-imprtc`, `:data-refrnc`, `:data-resolved`, `:data-lifecycle`, `:data-else-dream`, `:data-id` — `journal.scss` CSS 선택자와 연결한다. 타인 꿈(`hasDreamerName` → `data-else-dream="Y"`)은 **회색 이중선**(slate 0/2px)을 기본으로 사용하고, RESOLVED·중요·참조는 4px·6px·8px inset에 완료=보라·중요=빨강·참조=노랑으로 추가한다(`$journal-else-dream-paired-states`).

**lifecycle 상태 표현**: 일반 꿈의 `RESOLVED`는 `$journal-dream-paired-states`의 은은한 보라 배경·테두리·좌측선을 사용하고, 접힌 표시·순번·라이프사이클 메뉴의 선택된 완료 라벨도 보라색으로 맞춘다. 중요·참조가 함께 있으면 완료 보라선과 빨강·노랑 상태선을 조합한다. 일기·노트·해석과 챕터의 완료 집계는 초록색을 사용한다. 일기·꿈·노트의 `PENDING`은 약한 회색 배경·테두리·좌측선과 회색 배지를 사용하고 자동으로 접는다. 해석 라이프사이클 메뉴와 스레드 자체의 목록·상세 배지 및 메뉴, 스레드 후보 목록도 `PENDING`을 같은 회색으로 표시한다. 하위 엔트리가 1개 이상이고 모두 `PENDING`인 챕터도 같은 회색 상태를 표시하고 자동으로 접는다.

**거터(#순번)**: 왼쪽 56px 거터는 이벤트 엔트리(일기·꿈·노트)에 `#{{ entry.sortOrder }}`(콘텐츠 타입별 시퀀스)를 찍는다. `JournalEntryItem`은 Primary 엔트리만 렌더하므로 거터는 항상 #순번이다(Reflection 은 embed 전용이라 이 거터를 쓰지 않는다). #순번 색은 `text-success`(RESOLVED)/`text-muted`. 요약 엔트리(`isSummary`)는 거터 자체를 숨긴다.

**엔트리 제목 표시**: `entry.title` 이 있으면 유형(일기/꿈/노트) 무관하게 꿈 상태 배지 행 아래·마크다운 본문 위에 독립 행으로 표시한다(펼침 `fw-bold fs-5`, 접힘 `fs-7`, 공통 `mb-1`; 접힘 시 제목 뒤에 `(collapsed)` muted 이탤릭). 펼침 본문 `.journal-content` 는 fs 클래스 없이 base 1rem 을 상속하므로 펼침 제목은 본문보다 한 단계 위 크기가 된다. 접힘(`isCollapsed`)과 무관하게 항상 표시하며 본문만 숨긴다. 접힘 시에는 본문 영역이 `d-flex flex-column`, head-main 이 `justify-content-center` 로 전환돼 제목이 태그 위 공간의 세로 중앙에 놓이고, 우측 액션 묶음은 자체 `align-items:flex-start` 로 상단에 유지된다. 제목은 `.journal-content` 밖이라 유형별 본문 색상(꿈 보라 등)을 상속하지 않고 기본 텍스트색을 쓴다. 변경 전: 꿈 엔트리에서만 배지 행에 인라인 `fs-7` 로 표시했고 일기·노트는 제목이 렌더되지 않았다. 결산 상세(`JournalAnnualDetail.vue`)의 엔트리 제목도 같은 `fs-5 fw-bold` 계약을 따른다.

**엔트리 Prefix 표시**: 선택된 `entry.prefix`는 색상 배지로 제목 앞에 표시한다. 제목이 없어도 Prefix 배지는 독립적으로 남고, 접힘 상태에서도 숨기지 않는다. 같은 계약을 엔트리 카드(`JournalEntryItem`), 읽기 전용 원문(`JournalEntryViewModal`), 결산 상세의 DIARY/DREAM 엔트리(`JournalAnnualDetail`)에 적용하며 비활성 과거 Prefix도 서버 DTO의 이름·색을 그대로 표시한다.

**축별 완결 잠금(엔트리 DTO)**: 일자 provide 가 없는 검색·뷰 모달 등에서는 `JournalEntryDto.diaryResolvedYn`/`dreamResolvedYn`(백엔드 day projection)이 SSOT 이다. `JournalEntryItem` 은 `mergeDayResolvedAxis` 로 parent provide 와 병합해 `axisWritable`·관련글 해제·이력 `writeLocked` 를 결정한다. 교차뷰 `JournalReflectionItem` 은 Reflection이 완결축 밖이므로 소유권(`isCreatedBy`)으로 쓰기 가드를 둔다. `HistoryModal` 은 `historyWriteLocked` 시 복원·삭제 UI만 숨기고 복사·상세는 허용한다.

**우측 액션 영역**: `JournalEntryItem`은 Primary 엔트리(일기·꿈·노트)만 렌더한다. 댓글 버튼(단독) + 복사 버튼(`bi-copy`, `copyEntry(true)` 해석 포함; 리플렉션이 있으면 `bi-clipboard` `copyEntry(false)` 해석 제외 버튼을 추가 노출) + 링크 복사 버튼(`bi-link-45deg`, `copyEntryLink`) + ⋯ 컨텍스트 메뉴 (수정/이력/관련글/스레드에 추가/라이프사이클/상태/삭제) — 「FLOW 연결」·「FLOW 보기」는 제거되고 스레드 소속 「스레드에 추가」로 수렴했다(나-2b·나-2c). 수정은 `openEntryModify(id)`를 쓴다. 「해석 등록」은 이 Primary 를 target 으로 한 Reflection 을 다는 진입점이다(`openReflectionRegist`, target=이 엔트리). Reflection 자체의 수정·라이프사이클·상태·삭제 액션은 embed(`JournalReflectionItem`)가 담당한다(§23-3). primary `RESOLVED`→딸린 Reflection `RESOLVED`, `RESOLVED` primary에 Reflection 신규→primary `OPEN`(+접기 해제) 연쇄는 §5.1. 이 액션 묶음은 본문과 같은 flex head-row 오른쪽(min-width 80px; 펼침 시 head-row `align-items:flex-start`로 본문·액션 상단 고정, 접힘 시 head-row `align-items:stretch`+head-main 세로 중앙으로 제목만 중앙·액션은 자체 `align-items:flex-start`로 상단 유지)에 두어, 아래 임베드된 Reflection 의 복사·수정 액션과 같은 오른쪽 열에 정렬한다. **링크 복사(`copyEntryLink`)**는 `{origin}{BASE_URL}/journal/daily?stdrdDt=…&entryId=…` 절대 URL을 클립보드에 담는다 (`joinAppBasePath`로 조립). 외부에서 클릭하면 일간뷰(`journal-daily-tab`)로 진입하고, `entryId` 가 있으면 `JournalDayDaily` 가 목록(`store.dayList`) 로드 완료 후 `#journal-entry-{id}` 로 스크롤한다(1회, `pendingScrollEntryId`)

**스레드·FLOW 액션 경계 (수렴 완료)**: FLOW 를 저널 스레드 소속으로 수렴 완료했다(근거·단계는 `docs/spec/DESIGN_NOTES.md`). **변경 후 현황** — 「흐름 보기」 종단 보기(타임라인 모달 `JournalEntryFlowModal`)와 본문 FLOW 요약 행은 **제거(❌)**됐다. 「흐름 연결」(`openRelatedFlow`, `RelatedContentAddModal` FLOW 고정 모드)도 **제거(❌)**됐다(나-2b). `RelatedContentAddModal` 은 이제 일반 관련 글(RELATED) 전용이다 — 모드 분기·FLOW 안내·FLOW 관계 유형 옵션이 사라졌다. 백엔드 `flowSummary` 계산·`RelationType.FLOW` enum·`related_content` FLOW 행도 모두 **제거(❌)**됐다(다-2). 새 축인 스레드 소속은 `JournalEntryDto.threadList` 로 엔트리에 실리고, 소속 지정 UI 도 **구현 완료(✓)**다(나-2c) — ⋯ 메뉴의 「스레드에 추가」 hover 서브메뉴(소속 토글 + 「새 스레드로 시작」(말머리·제목))와 본문 소속 스레드 칩(클릭 시 현재 저널 화면 위에 상세 모달 열기). 서브메뉴도 전용 후보 API로 **전환 완료(✓)**했다. 280px 서브메뉴 안에서 제목 검색(250ms debounce)·분류 선택·「완료 포함」토글(`includeResolved`)을 제공하고, 후보 행에 `PENDING`/`RESOLVED` 라이프사이클 라벨을 표시한다. 엔트리별 후보 재조회·요청 경합 폐기·소속 변경 후 재조회·오류/정상 빈 결과 분리를 `journalThreadMembership.ts`가 담당한다.

**관련 글 대상 유형 기본값**: 모달을 열 때 대상 콘텐츠 유형은 **출발 엔트리와 같은 유형**을 기본 선택한다(일기에서 열면 일기, 꿈에서 열면 꿈). 변경 전에는 반대 유형(일기 → 꿈)을 기본값으로 잡아, 같은 성격의 기록을 잇는 경우 매번 되돌려야 했다. 자기 자신(같은 유형 + 같은 id) 연결은 저장 시 self 검증이 막는다. 대상 유형 select 는 일기/꿈만 제공하므로 그 밖의 출발 유형(노트 등)은 일기로 떨어뜨린다 — 빈 선택으로 두면 저장 시 검증에 걸려 원인을 알기 어렵다. `RelatedContentAddModal` 은 FLOW 모드가 제거돼(나-2b) 일반 관련 글(RELATED) 전용이며, 초기화 경로(`openRelatedWithMode`)도 RELATED 단일 모드로 남았다.

**관련 글 대상 검색**: `RelatedContentAddModal`은 선택한 대상 유형만 통합 `GET /api/journal/entries?type=DIARY|DREAM`에서 검색한다. 키워드는 제목 또는 본문에 일치하며, 복수 키워드가 전달되면 키워드 사이는 AND다. 요청 실패는 인라인 오류로 표시해 정상 0건과 구분한다.

**관련 관계 행**: 변경 전에는 일반 관련글과 FLOW 직접 간선을 모두 `RelatedContentDto`별 행으로 표시해 하나의 연결 컴포넌트가 여러 FLOW ID 행처럼 보였다. 변경 후 일반 관련글만 `relationType/reason/targetId/targetContentType/targetTitle`별 행을 유지하고, 대상 제목은 `openEntryView(targetId)`로 읽기 전용 원문을 열며 관계 ID로 해제한다. (변경 후) FLOW 본문 요약 행과 「흐름 보기」는 제거됐다. 백엔드 `flowSummary` 필드도 다-2 에서 제거됐다(어느 화면도 표시하지 않았다).

**i18n**: 펼침/접힘, 보류 badge·접힌 상태 문구, 꿈 상태 배지, 액션 툴팁, 일기·꿈 메뉴 헤더, 수정·해석 등록·이력·관련 글 추가·스레드에 추가·후보 검색·분류·빈 상태·오류·관련글 열기/해제, 라이프사이클·상태·삭제 메뉴와 각 옵션 라벨은 현재 locale의 클라이언트 카탈로그를 사용한다. locale 변경은 엔트리 상태·태그·댓글·관련글 데이터와 액션 호출을 변경하지 않는다.

**액션 결과 i18n**: 클립보드 복사 성공·실패와 복사 날짜 헤더의 요일, 라이프사이클·상태 변경 실패, 관련 관계 연결·해제와 삭제 확인·성공·실패 fallback은 현재 locale의 클라이언트 카탈로그를 사용한다. API 응답에 `message`가 있으면 서버 메시지를 우선 표시한다. 관계 해제·변경·삭제 성공 후 스레드 상세가 열려 있으면 상세 본문·집계 태그·소속 엔트리와 주간·월간·일간 배경 목록을 함께 재조회하되 배경 스크롤은 하지 않는다. 상세가 닫힌 화면에서는 기존 목록 재조회와 일자 스크롤을 유지한다.

**엔트리 복사 형식**: 날짜(`stdrdDt (요일)`) + 공통 `htmlToPlainText(content)` (TinyMCE HTML을 브라우저 HTML 파서로 이름·10진수·16진수 엔티티 디코딩 후 평문 변환, 마크다운 재처리 이전 원문 기준). `content` 없을 때 `markdownContent` 폴백. (`#sortOrder` 없음.) 원문·해석은 한 몸 — 해석 포함 복사(`copyEntry(true)`)는 엔트리 본문 뒤에 그 엔트리를 문(target) 리플렉션 본문을 빈 줄로 이어 붙인다(마커 없음). 해석 제외 복사(`copyEntry(false)`)는 리플렉션을 붙이지 않는다(엔트리에 리플렉션이 있을 때만 제외 버튼을 노출). 챕터 복사·검색 전체 복사도 같은 포함/제외 규칙.

**본문 색상·목록 간격 계약**: `journal.scss` 는 `journal-diary-content` / `journal-dream-content` / `journal-reflection-content` 의 `.journal-content`에 본문 색상을 지정하고, `v-html` 내부 `p`와 `li`는 해당 색상을 상속한다. 목록(`<ul>/<ol>/<li>`) 본문이 브라우저 기본 검정색으로 튀거나, 목록 위쪽은 붙고 아래쪽만 기본 margin이 남아 비대칭으로 보이면 실패다.

**시스템 요약 챕터 = '그날 요약' 카드 (신규)**: 배경 tint·좌측 바 같은 '엔트리 상태' 언어는 이미 중요/참조/완결에 쓰이므로, 요약을 그 계열의 level 이 아니라 **별개 오브젝트**로 만들기 위해 시스템 요약 **챕터 전체(헤더+엔트리)**를 카드로 감싼다. `JournalChapterItem` 은 `isSummaryChapter`(=`summaryYn==='Y'`) 일 때 루트 `.journal-chapter-block` 에 `is-summary-chapter` 를 더해 중립 표면(`--bs-gray-100` 을 `--bs-body-bg` 와 섞어 한 단계 옅게)+테두리(1px `--bs-gray-300`)+라운드(0.65rem)+옅은 그림자로 감싸고, `margin-bottom` 으로 다음 챕터와 띄운다. 그 안의 요약 엔트리(`isSummary` prop)는 카드 안에서 크롬만 정리한다: (1) `#순번` 거터 `v-if="!isSummary"` 숨김(개요는 번호 매겨진 목록 항목이 아님), (2) 상태 좌측선(`::before`)을 `content: none !important` 로 제거(base `.journal-diary-item .journal-diary-content::before` 와 특이성 동률이라 강제). 색·형태는 `--bs-*` 토큰이라 다크모드 자동 대응. prop 은 `JournalChapterItem` 만 전달하며 스레드 상세·검색결과·꿈 섹션의 `JournalEntryItem` 은 기본 false 라 무영향. 트레이드오프: 요약 엔트리는 `#순번`·PENDING 배지·엔트리별 접힘 토글 버튼을 표시하지 않는다(챕터 헤더 접힘은 유지).

### 23-2a. `JournalReflectionItem` (저널 리플렉션 슬림 임베드)

**Vue 구현**: `app/frontend-vue/src/features/journal/reflection/components/JournalReflectionItem.vue`

**데이터·표시**: target 엔트리의 `reflectionList`(`JournalEntryDto`) 한 건을 target 엔트리 본문과 태그 사이에 슬림 임베드한다. 헤더·제목·마커 없이 본문·댓글(읽기)만 흐르게 표시해 target 엔트리와 이어지는 글처럼 붙이고, 옅은 1px 좌측선이 "이 엔트리에 매달린 해석"임을 알리는 유일한 신호다. 본문(`.journal-content`) 글자 크기는 일기 `JournalEntryItem`과 같다. 접힘/펼침은 `forceCollapsedSignal` prop(`"expand"|"collapse"|null`, string — Vue 3 Boolean casting 우회)과 자체 `localCollapsedOverride`로 제어한다. 접힘 우선순위: 로컬 토글 > signal > lifecycle(RESOLVED/PENDING) 자동 > 일자 aside 리플렉션 기본 접힘 모드 > 서버 COLLAPSED. 모드 ON이면 로컬·signal·lifecycle 자동 접힘 미해당 시 접힘으로 시작하고, 모드 OFF이면 서버 COLLAPSED만 기본 분기다. `JournalDayLayout` provide 범위에서만 모드가 적용되며 검색·스레드는 OFF 계약이다. 토글 전환 시 `localCollapsedOverride`를 null로 리셋한다. 부모 엔트리를 사용자가 직접 펼치면(`localCollapsedOverride=false`) signal=`"expand"` → lifecycle 무시, 같이 펼쳐진다. signal 변경 시 `localCollapsedOverride`를 null로 초기화. 등록 시 기본 lifecycle은 PENDING이라 접힌 채 시작한다. 신규 등록 성공 직후에는 `reflectionCreatedCollapseId`로 해당 임베드만 일회성 로컬 접힘(`localCollapsedOverride=true`)을 심어, 챕터 일회성 펼침·부모 `forceCollapsedSignal="expand"`보다 우선한다. 수정 저장에는 적용하지 않으며 서버 상태는 바꾸지 않는다. 엔트리 접힘 시 `v-if="!isCollapsed"`로 DOM에서 제거된다. 엔트리를 다시 펼치면 `JournalEntryItem`이 `reinitMetronicAfterDom()`으로 임베드 ⋯(KTMenu)를 재바인딩한다. 신규 등록 부분 갱신(`patchEntryReflections`)은 `fetchDays()` 재바인딩을 타지 않으므로, 새로 렌더된 임베드 ⋯(KTMenu)를 `patchEntryReflections` 안에서 `reinitMetronicAfterDom()`으로 직접 재바인딩한다. RESOLVED 는 옅은 좌측선 색(초록)으로만 신호한다(`.journal-reflection-embed`). 임베드는 flex `[본문 | 우측 액션]`(`align-items:flex-start`)이며, 댓글·복사·⋯ 액션을 target 엔트리 액션과 같은 오른쪽 열(min-width 80px, `.journal-reflection-embed__actions`)에 정렬한다. 리플렉션 자체 접힘 시에는 임베드를 `align-items:stretch`, 본문(`.journal-reflection-content`)을 `d-flex flex-column justify-content-center` 로 전환해 `(collapsed)` 를 임베드 세로 중앙에 두고, 액션은 자체 `align-items:flex-start` 로 상단에 유지한다(엔트리 접힘 제목과 동형).

**액션 UI**: Reflection 은 embed 전용(1급 행 없음)이므로 모든 관리 액션은 임베드가 담당한다. 바깥은 댓글 등록·복사·⋯(Metronic)이고, ⋯ 메뉴는 수정·「전체 (( ))」·이력·관련글·라이프사이클·상태(중요/참조)·삭제를 엔트리 메뉴와 같은 구조로 제공한다. 「전체 (( ))」(`wrapEntireNoti`)는 저장 원문 `content`의 각 `<p>`/`<li>`에 Markdown `((...))`(md-text-noti) 마커를 멱등 적용하고, 변경 시에만 Reflection modify API로 저장한 뒤 `refreshJournalEntryHostForRoute`로 호스트를 갱신한다. 이미 전체가 감싸진 본문은 API를 호출하지 않는다. 인라인 HTML이 있는 블록은 평문으로 평탄화한 뒤 감싼다. Reflection→Reflection 중첩 등록은 제공하지 않는다(`REFLECTION_ONE_TYPE.md` §3.1). 일기·꿈·노트를 target으로 둔 Reflection에는 「스레드에 추가」를 두지 않는다(`isPrimaryContentTargetedReflection`, `REFLECTION_ONE_TYPE.md` §4). 접기(COLLAPSED)는 임베드가 본문을 항상 보이므로 메뉴에 두지 않는다. Reflection은 완결축 밖이므로 쓰기 가드는 소유권(`isCreatedBy !== false`)이다. 삭제 확인·실패 fallback은 `journal.reflection.delete.*`를 쓴다. 액션 성공 후 `refreshJournalEntryHostForRoute`로 호스트를 갱신한다.

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
| 검색 | `journalModalStore.openDayFilterModal({ type: "tag", id: tagId, name, ctgr })` | 새 창 `/vue-app/journal/entry/search?type=DIARY&tagIds=...&tagName=...` | 새 창 `/vue-app/journal/entry/search?type=DREAM&tagIds=...&tagName=...` |
| 태그 설정 | `/api/tags/{tagId}/profile?contentType=JOURNAL_DAY` 조회 후 태그 프로필 모달 | `/api/tags/{tagId}/profile?contentType=JOURNAL_DIARY` 조회 후 태그 프로필 모달 | `/api/tags/{tagId}/profile?contentType=JOURNAL_DREAM` 조회 후 태그 프로필 모달 |

메뉴 액션과 태그 프로필의 콘텐츠 유형 레이블은 현재 locale의 클라이언트 카탈로그를 사용한다.

**검색 팝업 안 단일/다중 분기**: 위 표의 `검색`은 팝업 밖(저널 뷰·결산) 기준이며, **엔트리 검색 팝업 안**(`route.name === "journal-entry-search"`)에서는 태그 컨텍스트 메뉴가 두 검색 액션을 함께 제공한다. `검색`(단일)은 `tagIds`를 클릭한 태그 하나로 **교체**해 새 단일태그 검색을 하고, `검색에 추가`(다중)는 기존 `tagIds`에 **추가**한다(AND, 중복 무시). 두 경우 모두 `type`·`sort`·`searchKeywords` 등 다른 검색 축은 유지한다. `검색에 추가` 버튼은 검색 팝업 안에서만 노출한다(밖에서는 `검색`이 곧 새 팝업이라 무의미). 라벨 i18n 은 `common.search` / `journal.tag.search-add`.

**태그 프로필·일자 필터 모달 마운트 계약**: `JournalTagContextMenu` 의 `프로필`은 `attachableStore.openTagProfile()` 로, `JOURNAL_DAY` `검색`은 `journalModalStore.openDayFilterModal(...)` 로 **상태만** 켠다. 따라서 그 상태를 구독해 실제로 렌더하는 `JournalTagProfileModal`·`JournalDayMetaModal` 이 **같은 화면에 함께 마운트돼 있어야** 화면이 열린다. 컨텍스트 메뉴를 마운트하는 화면은 짝 모달도 반드시 마운트한다 — `JournalDayLayout`·`JournalDayDailyLayout`은 둘 다, `JournalEntrySearchPage`는 프로필만(일자 태그 검색 없음), `JournalAnnualLayout`은 둘 다. 결산에서 프로필·일자 태그 검색이 무반응처럼 보이던 원인은 각각 짝 모달 미마운트였다.

**태그 프로필 cloudSizeLock**: 모달 세그먼트 컨트롤에서 MAX 선택 시 sized 태그클라우드 크기를 `ts-9`, MIN이면 `ts-1`로 고정한다(AUTO는 빈도 산출). 저장·삭제 후 클라우드 재조회로 반영. 엔트리 본문 태그줄에는 적용하지 않는다.


**태그 프로필 저장·삭제 후 갱신** (`JournalTagProfileModal`): 월간/주간/일간 등은 성공 알림 확인 후 `refreshJournalDaysForRoute` + contentType 대응 `fetchTagCloud`(day/diary/dream). 결산 상세(`annual-detail`)는 일자 `fetchTagCloud`가 아니라 `useJournalAnnualStore.fetchTagRows(yy, activeSection)`로 태그클라우드 행을 재조회한다(SSOT: `/api/journal/annual/{yy}/tags`). 검색 팝업은 일자/클라우드 재조회 없이 `@success` → `loadEntries()`. 스레드 상세가 열려 있으면 route와 무관하게 소속 엔트리 태그 집계의 SSOT인 `JournalThreadStore.refreshOpenDetail()`을 먼저 수행하고, 이어서 검색·결산·일자 배경의 기존 갱신 경로도 수행한다.

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
| 그래프로 보기 | `journalStore.addMetaToGraph` (최대 2, 중복 시 비활성, 꽉 차면 alert) |
| 검색 | `journalModalStore.openDayFilterModal({ type: "meta", ... })` → `JournalDayMetaModal` |
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
- `GET /api/journal/entries` 조회 — 응답 `threadList` 포함(일자 목록과 동일 enrich). 결과 카드는 `JournalEntryItem`으로 소속 스레드 칩을 표시한다
- 결과 목록, 태그 목록, 키워드 검색/초기화, 정렬 전환
- 고급 필터 토글 영역에서 유형(일기/꿈) 선택과 키워드 추가
- 결과 전체 복사 버튼 (해석 포함/제외 2버튼) — `copyAll(true)`(`bi-copy`)는 각 결과 엔트리 본문 뒤에 그 엔트리 target 리플렉션 본문을 이어 붙이고, `copyAll(false)`(`bi-clipboard`)는 붙이지 않는다(레거시 `JournalEntrySearch.copy()` 포맷=제외에 해당)
- 개별 결과 복사 버튼 — `JournalEntryItem.copyEntry()` 사용
- TXT export 버튼 — `GET /api/journal/entries/export`
- 결과별 수정/삭제 버튼
- 결과 태그 클릭 컨텍스트 메뉴(`JournalTagContextMenu`)와 태그 프로필 모달(`JournalTagProfileModal`) 마운트
- 검색 팝업 내부 태그 검색은 같은 창 query 갱신으로 반영
- 엔트리 수정 저장 성공 시 `JournalEntryRegistModal` `prepare-success` 이벤트로 현재 검색 목록/수정 대상 DOM을 먼저 준비하고, `success` 이벤트로 저장 위치 스크롤만 수행
- 검색 결과에 포함된 리플렉션의 수정 액션은 같은 창에서 `JournalReflectionRegistModal`을 열어야 한다. 검색 팝업이 이 모달을 직접 마운트하며, 수정 모드는 `GET /api/journal/reflection/{id}` 상세 조회로 제목/본문을 채운 뒤 모달을 열어야 한다. 저장·스레드 소속·상태 등 엔트리 액션 성공 후 `refreshJournalEntryHostForRoute`가 `registerJournalEntrySearchHost`로 등록된 `loadEntries`를 호출해 검색 목록(스레드 칩 포함)을 재조회한다.
- `JournalReflectionRegistModal`의 제목(`title`)은 필수 항목이 아니다. 제목 없이 본문만으로 리플렉션 등록/수정이 가능해야 한다.
- 날짜 헤더는 `stdrdDt (요일)` 옆에 새 창 버튼(`bi-box-arrow-up-right`)을 표시한다. 서버 `isHolyday` 가 true 이면 날짜·요일을 `text-danger` 로 표시하고 `holydayNm` 이 있으면 공휴일명을 붙인다(일자 카드와 동일 축; 프론트 재계산 없음). 클릭 시 월간/주간 일자 카드와 동일하게 `window.open(BASE_URL + /journal/daily-popup?stdrdDt=..., "_blank", "width=...,height=...")`로 일자 뷰를 새 창으로 연다.

**표시 문구 i18n**: 컨트롤 바·고급 필터·유형/키워드/태그 입력·카테고리 선택·로딩/빈 결과·일자 새 창 tooltip과 결과 건수·연월 구분선·요일은 현재 locale의 클라이언트 카탈로그를 사용한다. locale 변경은 URL query·검색 조건·정렬·태그 선택·결과 목록을 변경하지 않는다.

**액션 메시지 i18n**: 검색 결과/엔트리 상세 조회 실패, 태그 선택 검증, 검색 조건 검증, 복사 대상 없음과 복사 성공/실패 알림 및 복사 날짜 헤더의 요일은 현재 locale의 클라이언트 카탈로그를 사용한다. 클립보드와 TXT 본문은 기존 레거시 출력 포맷을 유지한다.

**남은 legacy 동등성 확인 대상**:
- 고급 검색 영역의 태그 직접 입력 UX

---

### 23-5. `JournalThreadDetailModal` / `JournalThreadDetailContent` (저널 스레드 상세)

**Vue 구현**:
- 모달 셸: `app/frontend-vue/src/features/journal/thread/modals/JournalThreadDetailModal.vue`
- 독립 상세 페이지: `app/frontend-vue/src/features/journal/thread/JournalThreadDetailPage.vue`
- 공용 상세 콘텐츠: `app/frontend-vue/src/features/journal/thread/components/JournalThreadDetailContent.vue`

**컴포넌트 책임**: `JournalThreadDetailModal`은 Bootstrap 표시·명시적 닫기·같은 앱의 문맥형 수정 전환처럼 모달 표면에만 속한 계약을 담당한다. `JournalThreadDetailPage`는 독립 route의 카드 셸·수정·목록 복귀를 담당한다. `JournalThreadDetailContent`는 단일 Prefix 배지·제목·소속 기간·작성 정보·본문·소속 엔트리 집계 태그·연관 스레드 목록(행별 뷰 합성 토글·삭제)·일자 그룹 엔트리(빌려온 엔트리 출처 배지)·댓글 렌더링과 해당 댓글 액션을 담당한다. Prefix 배지는 `prefix`의 이름과 검증된 색을 사용하며 비활성 과거 선택도 표시한다. 연관 스레드 행 토글은 `relatedThreadIds`로 엔트리 목록을 재조회하며, 상태는 화면 임시(기본 OFF)이다. 합성으로 빌려온 엔트리(`sourceThreadId`)는 출처 스레드 제목 칩(스레드별 고정 색)으로 base 소속 카드와 구분한다.

**마운트·진입 계약**: 인증된 SPA의 `App.vue`가 문맥형 상세 모달과 등록/수정 모달을 각각 전역 단일 인스턴스로 마운트한다. 저널 엔트리 스레드 칩·접힌 챕터 스레드 요약·기간별 스레드 요약은 `JournalThreadStore.openDetail(threadId)`를 직접 호출해 현재 주간·월간·일간·검색 화면과 스크롤 문맥을 보존한다. 스레드 목록 행과 외부 딥링크는 `thread-detail` route의 `JournalThreadDetailPage`를 렌더한다. 스토어는 두 표면에 같은 `detailModel`·`detailEntries`를 제공하고 `detailSurface=modal|page`로 전역 모달 표시 여부만 구분한다.

**데이터**: `useJournalThreadStore.detailModel`의 카테고리·제목·작성자·작성일·본문·태그·`comment.list`를 읽기 전용으로 표시한다. **태그는 스레드 자체 태그가 아니라 소속 엔트리 태그의 집계다** — 스레드는 자체 태그를 소유하지 않는다(엔티티 `TagEmbed` 제거, 챕터와 동형). 백엔드 `JournalThreadService.viewDetailPage` 의 `applyEntryTagSummary` 가 소속 엔트리 태그를 tagId 로 중복 제거해 `thread.tag.list` 에 채우고(챕터 `applyChapterTagSummary` 와 동형), 상세는 그 결과를 표시한다. 소속 엔트리 목록은 `store.detailEntries`(일자별 그룹 카드, 그룹마다 일자 헤더)로 함께 표시한다. 등록/수정 모달에는 태그 입력이 없다(자체 태그 미소유).

**전체 복사·다운로드 (양 표면 공통)**: 상세 모달 헤더와 독립 페이지 card-toolbar에 `store.detailModel?.id`가 있을 때 「복사」(`bi-copy`)·「다운로드」(`bi-download`) 아이콘 버튼을 둔다(tooltip `common.copy.tooltip`·`common.export-text.tooltip`). 복사는 클라이언트 `journalThreadExport.copyThreadDetail`가 스레드 제목을 머리행으로, 소속 엔트리를 검색 전체 복사(copyAll)와 동일한 `날짜(요일)`·`#순번`·본문 평문 포맷으로 클립보드에 쓰고 `common.copy.success/failure`로 알린다. 다운로드는 `downloadThreadDetail`가 `GET /api/journal/threads/{id}/export`로 이동해 `thread_{id}_@yyyyMMdd.txt` 첨부를 받는다. 서버 텍스트는 `JournalThreadExportService.buildTxt`가 챕터/엔트리 내보내기와 동일한 배너·엔트리 블록 계약(`thread: 제목` + 선택 시 `prefix: 말머리` + 소속 엔트리)으로 생성하며 복사와 배너 유무만 다르다. 소유권은 서버 `getEntriesByThread`가 검증한다. 복사/다운로드 로직은 `features/journal/utils/journalThreadExport.ts` 단일 util을 두 표면이 공유하고, 표시 데이터를 바꾸지 않고 `detailModel`·`detailEntries` SSOT만 읽는다.

**동작**: 댓글 등록 버튼은 `useAttachableModalStore.openCommentRegist(id, contentType)`를 호출한다. 댓글 수 버튼(목록에 댓글이 있을 때)은 `openCommentList`를 호출한다. 댓글 등록 성공 시 `CommentRegistModal`이 열린 상세가 `JOURNAL_THREAD`이면 상세를 재조회하고 목록의 댓글 수를 갱신한다. 문맥형 모달 헤더 「수정」은 원래 저널 화면·스크롤·상세 데이터를 유지한 채 `store.openModifyFromDetail(id)`로 같은 앱의 수정 모달로 전환한다. 독립 상세 헤더 「수정」은 같은 탭의 `thread-edit` route로 이동한다.

**스레드 자체 수정 표면 계약**: 문맥형 상세에서 수정에 진입하면 스토어는 `detailOpen`·상세 데이터를 유지하고 `detailSurface`만 보류한 뒤 `registSurface=modal`을 연다. 수정 취소·저장 종료 시 보류한 상세 ID와 현재 ID가 같을 때만 원래 모달 표면을 복원한다. 저장 성공 시 `refreshJournalEntryHostForRoute`가 상세 본문·제목을 다시 조회하고, 배경이 주간·월간·일간이면 스레드 제목이 실린 배경 목록도 스크롤 없이 함께 갱신한다. 독립 상세는 같은 탭의 `JournalThreadEditPage`로 전환하며 저장·취소 뒤 해당 독립 상세로 복귀한다. 이름 있는 브라우저 팝업과 창 간 메시지 계약은 사용하지 않는다.

**닫기 정책**: 읽는 중인 상세가 backdrop 클릭이나 Escape 입력으로 의도치 않게 닫히지 않도록 루트 DOM에 `data-bs-backdrop="static"`, `data-bs-keyboard="false"`를 선언하고 `Bootstrap Modal`도 `{ backdrop: "static", keyboard: false }`로 생성한다. 헤더 ×와 푸터 「닫기」는 `store.closeDetail()`을 호출하는 명시적 종료 경로로 유지한다. URL 이동·상세 조회 실패에 따른 프로그램상 종료도 유지한다.

**상위 서사·엔트리 소속 계약**: 저널 스레드는 특정 일자 엔트리로 쓰기 어려운 내용을 제목·본문으로 직접 서술하는 독립 상위 서사다. 엔트리를 스레드에 소속시키는 기능은 **✓ 구현 완료** — 백엔드(테이블 `journal_thread_entry` + 소속 등록/해제/조회/후보 API)와 엔트리 ⋯ 메뉴의 소속 지정 UI(「스레드에 추가」 서브메뉴 + 제목 검색·분류 필터 + 「새 스레드로 시작」 + 본문 소속 스레드 칩)와 **스레드 상세의 소속 엔트리 목록**도 완료됐다(✓). 스레드 상세는 소속 엔트리를 저널 일자와 동일한 `JournalEntryItem` 카드로 표시하되, 저널 일자·연간 상세와 동형으로 `stdrdDt` 별로 그룹핑해 그룹마다 `journal-day-header`(날짜 + `(요일)`, `getWeekDayStr`)를 얹는다 — 카드별 날짜 라벨은 일자 헤더로 대체됐다. `GET /api/journal/threads/{id}/entries` 가 소속 메타가 아니라 full `JournalEntryDto` 목록을 반환하며, `JournalThreadEntryService.getEntriesByThread` 가 `JournalEntryService.getListDtoByIds` 로 조회한 뒤 **일자 → 챕터 `sortOrder` → 원본 엔트리 `sortOrder` → ID 오름차순**으로 결정적 정렬한다(공용 헬퍼 `JournalEntryService.sortByChapterAndEntryOrder`; 챕터 sortOrder는 `JournalChapterRepository`로 배치 조회. 연간 결산 엔트리 목록 `getAnnualListDtoByUser`도 같은 헬퍼로 정렬해 저널 일자 화면과 순서를 일치시킨다). 엔트리 `sortOrder`가 챕터별로 1부터라 챕터 순서를 함께 봐야 저널 일자 화면과 같은 그룹 순서가 나온다. 스레드 소속 자체의 nullable `sort_order`는 사용하지 않으며, 챕터·엔트리 순서가 없거나 같으면 ID가 tiebreak다. 프론트 그룹핑은 백엔드 정렬 순서를 first-seen 으로 보존한다. 상세 카드가 수정·댓글·해석·이력·관련글·스레드 소속·라이프사이클·상태·삭제 액션을 제공하는 것은 의도된 계약이다. 스레드에 보이는 항목은 읽기 전용 복제본이 아니라 같은 원본 엔트리이므로 저널 일자와 기능 경계를 달리하지 않는다.

**lifecycle·Reflection 표시·접힘 정책**: 소속 엔트리는 서버에서 target 역참조 Reflection(`enrichMixed` → `reflectionList`)과 lifecycle(`enrichLifecycleMixed`, 교차뷰 Reflection 포함)을 일자 조회와 같은 계약으로 병합해 내려준다. `JournalEntryItem`이 일자와 동일하게 슬림 임베드 교차뷰를 렌더한다. `RESOLVED`는 초록(꿈은 보라), `PENDING`은 회색 상태 표현을 사용한다. 스레드 맥락에서는 `disableLifecycleCollapse`로 lifecycle 자동 접힘을 억제하고, 상태 표시·수동 접기·서버 `COLLAPSED`는 유지한다. state는 병합하지 않는다.

**KTMenu 재초기화**: 소속 엔트리 목록은 `JournalThreadDetailContent`가 `detailEntries`·`detailModel.id` 변화를 watch해 `reinitMetronicAfterDom()`으로 ⋯ 컨텍스트 메뉴·헤더 라이프사이클 드롭다운(Metronic KTMenu)을 재바인딩한다(`{ immediate: true }`, 최초 로드와 `refreshOpenDetail` 재조회 모두 커버). 이 재초기화가 없으면 모달·독립 페이지의 ⋯ 메뉴가 열리지 않는다 — 검색·일자 화면이 목록 렌더 후 재초기화하는 것과 동형 계약이다.

**엔트리 액션 호스트·갱신 계약**: 상세 모달을 연 현재 화면의 레이아웃은 엔트리 카드가 호출하는 `CommentRegistModal`, `CommentListModal`, `JournalReflectionRegistModal`, `HistoryModal`, `RelatedContentAddModal`, `JournalTagProfileModal`, `JournalTagContextMenu`를 마운트한다. `JournalEntryRegistModal`·`JournalEntryViewModal`은 `App.vue`의 비팝업 전역 마운트를 재사용하고 검색·일간 팝업은 자체 마운트를 유지한다. 엔트리·리플렉션의 수정/삭제, 댓글, 이력 복원, 관련글 연결/해제, 태그 프로필, 스레드 소속, 라이프사이클·상태 변경이 성공하면 `refreshJournalEntryHostForRoute`가 route가 아니라 `detailOpen`을 전경 판단 기준으로 사용한다. 열린 상세의 본문·집계 태그·소속 엔트리를 먼저 재조회하고, 배경이 주간·월간·일간이면 `refreshJournalDaysForRoute`도 실행하되 상세 축을 반환해 배경 스크롤을 막는다. 검색 배경은 기존 로컬 결과 갱신 이벤트를 함께 수행한다. 상세 재조회 실패 시 읽던 데이터를 비우지 않고 오류를 기록·표시한다.

**도메인 메타 경계**: 스레드의 자기 설명 SSOT는 제목·본문이다. 시작·최근 시점은 소속 엔트리 일자에서 파생한다. **스레드 라이프사이클(`OPEN`/`PENDING`/`RESOLVED`)은 ✓** — 기본 상태 `OPEN`은 `lifecycle` row 부재로 표현하고, `PENDING`·`RESOLVED`만 `ref_content_type=JOURNAL_THREAD` row로 저장한다. 목록·상세 DTO enrich와 필터는 row 부재를 `OPEN`으로 해석하며, 목록 `lifecycleKey` 필터·후보 `includeResolved`·목록/상세 설정 UI(`PUT /api/lifecycles`)까지 구현한다. 일자 캐시 등록은 하지 않는다. 종결 시점·대표 엔트리(앵커)·별도 핵심 질문 필드는 현재 계약에 포함하지 않는다. 스레드 상세는 날짜순 전체 기록을 읽는 화면이며 nullable 소속 `journal_thread_entry.sort_order`를 표시 순서에 사용하지 않고 별도 소속 역할도 두지 않는다. 같은 일자의 순서는 원본 엔트리 `journal_entry.sort_order`를 따른다.

- 소속은 스레드 1 : 엔트리 N 이 아니라 **N:M** 이다. 한 엔트리가 여러 스레드에 속할 수 있다.
- 소속 등록 API는 소속 행 조회·복원·저장 전에 스레드와 대상 엔트리의 존재·현재 사용자 소유권을 모두 검사한다. 미존재 엔트리는 not found, 타인 소유 엔트리는 access denied로 거부하고 소속 쓰기를 수행하지 않는다.
- 소속 후보 API `GET /api/journal/threads/candidates`는 본인 스레드만 현재 소속 → 최근 활성 소속 추가 시각 → 활성 소속 수 → 스레드 수정·생성 시각 순으로 반환하고 제목 검색·분류 필터·완료 제외(기본)·`includeResolved`·1~20개 조회 상한을 적용한다. 응답에 `lifecycleKey`를 포함한다. 서브메뉴는 현재 엔트리 ID와 검색 조건으로 이 API를 호출하고, 후보 응답의 `member`로 체크·추가/해제를 판정한다.
- 등록·해제 모두 **멱등**이며, 해제는 소프트 삭제다. 해제했던 소속을 다시 등록하면 새 행을 만들지 않고 기존 행을 되살린다.
- API: `GET|POST /api/journal/threads/{id}/entries`, `DELETE /api/journal/threads/{id}/entries/{entryId}`, `GET /api/journal/entries/{entryId}/threads`
- **엔트리 응답에 소속이 실린다**: `JournalEntryDto.threadList` (소속 없으면 빈 목록). 엔트리마다 단건 조회하면 목록 화면에서 N+1 이 나므로 `JournalEntryRelatedEnricher`·`JournalDayQueryService.mergeRelatedContents` 가 엔트리 목록 단위로 **일괄 주입**한다. 조회 대상 사용자는 `username` 파라미터로 받는다 (`getRelatedContentMapByRefs` 와 동일한 계약).
- **변경 전**: 이 계약은 RELATED/FLOW 그래프와 «별도» 축으로 유지할 계획이었다. **변경 후**: FLOW 를 이 소속 구조로 **수렴 완료**했다 (근거·단계는 `docs/spec/DESIGN_NOTES.md` 참조). FLOW 간선은 스레드 소속으로 이관되고 FLOW 경로는 제거됐다(다-2) — 공존 구간은 종료됐다.

---

### 23-6. `JournalThreadPickerModal` (연관 스레드 선택 피커 모달)

**Vue 구현**: `app/frontend-vue/src/features/journal/thread/modals/JournalThreadPickerModal.vue`

**컴포넌트 책임**: 저널 스레드 상세(`JournalThreadDetailContent`)에서 연관 스레드를 추가할 때 대칭 1-hop 연관 대상 스레드를 검색하고 선택하는 전용 모달이다.

**동작 및 게이팅 계약**:
- 스레드 제목/설명 키워드 검색(`GET /api/journal/threads?searchKeyword=...`) 결과를 목록으로 표시한다.
- **자기 자신 및 이미 연관된 스레드 제외/비활성화**: 현재 보고 있는 base 스레드 ID와 이미 `detailRelatedThreads`에 포함된 연관 스레드는 선택 불가능(비활성화 및 뱃지 표시)하게 처리한다.
- 선택 후 추가 버튼 클릭 시 `store.addRelatedThread(baseThreadId, targetThreadId)`를 호출하여 `POST /api/related/JOURNAL_THREAD/{baseId}` 연결을 생성하고 모달을 닫는다.

**i18n**: 모달·독립 페이지 제목, 수정·목록·댓글 섹션·빈 상태·등록·닫기 버튼은 현재 locale의 클라이언트 카탈로그를 사용한다.

**현재 Vue 동등**: ✓ 구현 완료

---

### 23-6. `JournalThreadList` (저널 스레드 목록)

**Vue 구현**: `app/frontend-vue/src/features/journal/thread/JournalThreadList.vue`, `JournalThreadLayout.vue`, `components/JournalThreadViewToolbar.vue`

**데이터·동작**: 등록은 `JournalThreadViewToolbar`(결산·일자 액션 행과 동형, `mt-3 mb-1`; ASIDE 없음; 탭용 `mt-5` 빈 여백 없음)에 두고 `thread-create`로 라우팅한다. 목록 카드 위 컴팩트한 검색 카드에서 말머리·라이프사이클·제목 검색, 전체 초기화를 제공하고 모든 조건 실행은 첫 페이지부터 조회한다. 말머리 선택지는 `GET /api/my/prefixes/options`, 목록 필터는 nullable 단일 `prefixId` 계약을 사용하며 행은 `PrefixDto`의 이름·색 배지를 표시한다. **태그 클라우드는 제거됐다**(2c-A, 스레드 자체 태그 미소유). **목록 행의 소속 엔트리 태그 집계 표시는 ✓** — `applyEntryTagSummaries` 가 소속 집계 쿼리(`findMembershipStatsByThreadIds`·`findMembershipTagsByThreadIds`)로 `thread.tag.list` 를 채우고(엔트리 풀 DTO 로드 없음), 행에 스레드 상세·일자 뷰와 동일한 시각 언어(`[분류]`(`text-noti`) + `#` + 밑줄 `text-primary` 이름, `bi-tag` 아이콘 없음)로 렌더한다. 제목은 `fs-6 fw-semibold` 로 강조한다. **소속 엔트리 수 표시는 ✓** — 같은 enrich 가 `membershipCount`(활성 소속 수)를 채우고, 제목 옆에 기간 요약과 동일 포맷(`{n}건`)으로 표시한다(0건은 숨김). **소속 기간 표시는 ✓** — 같은 enrich 가 소속 엔트리 기준일 min/max 를 `firstEntryDate`/`lastEntryDate` 로 채우고, 제목 옆에 muted `fs-9`로 표시한다(같은 날이면 단일 일자, 범위면 `{0} ~ {1}`, 유효 일자 없으면 숨김). **라이프사이클 표시·필터·설정은 ✓** — 목록 enrich 가 `lifecycle`을 채우고 배지로 표시하되, 행 선두 자리는 Prefix 배지 몫이라 라이프사이클(진행중 등) 배지는 제목·메타(개수·기간·댓글) 뒤=댓글 버튼 뒤·태그 줄 앞(`ms-2`)에 둔다. 검색 카드 `lifecycleKey` 필터와 ⋯ 메뉴 라이프사이클 서브메뉴(`PUT /api/lifecycles`)를 제공한다. **멀티 태그 필터(AND)는 ✓** — 검색 카드에서 엔트리 검색 팝업과 동형으로 태그를 추가·제거하며, `tagIds` 를 목록 API에 보낸다. 의미는 소속 엔트리 태그 합집합이 선택 태그를 모두 포함하는 스레드만 남기는 것이다(카탈로그는 diary+dream 합집합). **변경 후**: 확정 태그 배지는 `#` + `[ctgr]`(`text-noti`) + 이름을 모두 `fs-7`로 표시하고, 인라인 flex로 세로 가운데 정렬한다. 동명 다중 태그 카테고리 선택 UI는 기존과 동일. 검색 카드는 `margin-top: 0`으로 툴바에 붙인다. 목록 API 실패는 `store.error`(`journal.thread.list.load.failure`)를 테이블에 표시하고 정상 빈 목록(`journal.thread.empty`)과 구분한다. 검색 컨트롤은 말머리·라이프사이클·제목·태그·초기화다. `useJournalThreadStore.threadList`를 기존 테이블 DOM·클래스로 표시하고, 행 클릭은 Vue Router를 호출한다. 관리 열의 ⋯ 컨텍스트 메뉴는 수정 route 이동·라이프사이클 설정·삭제 store 액션을 제공하고, 행 상세 이동은 `isMetronicMenuEventTarget` 가드로 막는다(트리거 `@click.stop` 금지 — KTMenu body 위임). **변경 후**: 저널 일자·게시판 목록과 동일하게 Metronic `data-kt-menu`를 쓰고, 비동기 목록 렌더 후 `reinitMetronicAfterDom()`으로 재바인딩한다(Bootstrap `strategy:fixed` 제거). 등록·수정·삭제 성공 후 목록과 태그 클라우드를 함께 갱신한다. 댓글 수 버튼은 `useAttachableModalStore.openCommentList`를 호출한다. (태그 클라우드 제거)

**i18n**: 말머리·라이프사이클 필터·제목 placeholder·태그 빈 상태와 조회 실패, 툴바 등록 버튼·테이블 헤더·빈 상태·댓글 목록·컨텍스트 메뉴·수정·라이프사이클·삭제 문구는 현재 locale의 클라이언트 카탈로그를 사용한다. Prefix 이름은 사용자 입력값이므로 locale에 따라 번역하지 않는다.

**삭제 계약**: 삭제 확인과 실패 fallback은 스레드 전용 현재 locale 메시지를 사용하고, 성공 fallback은 공통 삭제 성공 메시지를 사용한다. 삭제 API가 `message`를 반환하면 서버 메시지를 우선 표시하며, 성공 알림 확인 후 첫 페이지 목록을 다시 조회한다.

**현재 Vue 동등**: ✓ 구현 완료

---

### 23-7. `JournalThreadRegistModal` / `JournalThreadEditPage` / `JournalThreadEditorForm` (저널 스레드 등록/수정)

**Vue 구현**:
- 문맥형 등록/수정 모달: `app/frontend-vue/src/features/journal/thread/modals/JournalThreadRegistModal.vue`
- 독립 수정 페이지: `app/frontend-vue/src/features/journal/thread/JournalThreadEditPage.vue`
- 공용 편집 폼: `app/frontend-vue/src/features/journal/thread/components/JournalThreadEditorForm.vue`

**데이터·동작**: 두 표면은 `JournalThreadEditorForm`과 `useJournalThreadStore.registModel`을 공유해 nullable 단일 `prefixId`·제목·본문을 편집하고 등록/수정 확인 후 `submitRegist()`를 호출한다. `JournalThreadLayout`의 route 동기화가 `ensurePrefixOptions()`를 호출해 `GET /api/my/prefixes/options`로 활성 옵션을 조회하며 store가 검색·등록·수정에 공유한다. 조회 실패는 빈 선택지로 가장하지 않고 오류를 표시한다. 신규 등록은 목록 툴바에서 모달로 열고 문맥형 상세 수정도 같은 모달을 사용한다. 독립 수정은 `JournalThreadEditPage`를 사용하며 기존 미저장 이탈 보호 계약을 유지한다.

**Prefix 빠른 추가·관리 진입**: 빠른 추가는 `/api/my/prefixes`(개인 목록이 `(user, content_type)`로 분리되어 스레드 문맥은 `contentType=JOURNAL_THREAD` 전달)를 사용하고 현재 최대 정렬값 다음으로 생성한다. 성공하면 옵션을 재조회하고 생성 Prefix를 즉시 선택한다. 중복 이름 등 서버 거부는 메시지로 표시하며 전체 관리는 `/my/prefixes` 말머리 관리 탭으로 진입한다.

**i18n**: 모달·페이지 제목, 필드 레이블·제목 placeholder·저장·닫기/취소·확인 문구는 현재 locale의 클라이언트 카탈로그를 사용한다. 저장 결과는 서버 `message`를 우선 표시하고, 서버 메시지가 없을 때 현재 locale의 등록·수정·실패 fallback을 사용한다. 수정·상세 조회 실패 안내도 현재 locale을 사용한다.

**현재 Vue 동등**: ✓ 구현 완료

---

### 24. `JournalLayout` (저널 공통 레이아웃·모달 호스트)

**Vue 구현**: `app/frontend-vue/src/features/journal/day/JournalDayLayout.vue`

**역할**: `<router-view>` + `JournalAside` + 저널·공통 attachable 모달 일괄 마운트

**Aside 스크롤 동작**: `journal.scss`에서 `.journal-layout-vue__aside`에 `position: sticky`와 `overflow-y: auto`를 적용한다. 데스크톱(`min-width: 992px`)에서는 `top: var(--bs-app-header-height, 74px)`와 `max-height: calc(100vh - var(--bs-app-header-height, 74px))`를 사용해 floating 툴바와 같은 상단선에 고정하고, 그 미만 화면에서는 `top: 0; max-height: 100vh`를 사용한다. 본문 스크롤 중 필터 패널이 추가 여백 없이 viewport 안에서 따라오며, 패널 내용이 화면보다 길면 aside 내부만 스크롤된다. 연간 결산 aside(`.journal-annual-layout-vue__aside`)는 기존 `top: 1rem`, `max-height: calc(100vh - 2rem)` 규칙을 유지한다.

**Aside 열기 i18n**: aside가 숨겨졌을 때 표시되는 필터 패널 열기 버튼 tooltip은 현재 locale의 클라이언트 카탈로그를 사용한다. locale 변경은 aside 표시 상태를 변경하지 않는다.

**Aside 열기 버튼 위치**: aside 숨김 시 데스크톱 열기 버튼(`bi-layout-sidebar-inset-reverse`)은 `JournalDayViewToolbar` 액션 행의 맨 오른쪽에서 일정 화면과 같은 구분자+아이콘 슬롯으로 표시되고, sticky 툴바와 함께 스크롤을 따라간다. 기존 본문 컨테이너의 `position-absolute; top: 4rem; right: 0` 버튼은 툴바 액션 영역이 숨겨지는 모바일(`d-md-none`)에서만 유지한다. 변경 전에는 모든 화면 크기에서 본문 우상단에 떠 있어 데스크톱 액션 행과 정렬되지 않았다.

**마운트 모달·메뉴**: `JournalDayRegistModal`, `JournalDayDetailModal`, `JournalChapterRegistModal`, `JournalReflectionRegistModal`, `JournalTodoRegistModal`, `JournalDayMetaModal`, `CommentRegistModal`, `CommentListModal`, `HistoryModal`, `RelatedContentAddModal`, `JournalTagProfileModal`, `JournalMetaProfileModal`, `JournalTagContextMenu`, `JournalMetaContextMenu`. (`JournalEntryRegistModal`·`JournalEntryViewModal`은 채팅 RAG 원문 딥링크를 위해 `App.vue`에서 비팝업 전역 마운트 — 레이아웃 중복 마운트 없음. 출처 클릭은 읽기 전용 뷰, 편집은 뷰 footer에서 수정 모달로 전환.)

`JournalDayMetaModal.vue`의 제목·결과 건수·연도/전체 연도·연월 구분선·기준 날짜 요일·필터 추가/제거·일자 새 창 tooltip·빈 상태·닫기와 조회 실패 fallback은 현재 locale의 클라이언트 카탈로그를 사용한다. 각 일자는 카드형(날짜 → 메타·태그 → 시스템 요약, 구분선)으로 표시한다. 시스템 요약 챕터(`summaryYn=Y`, 없으면 첫 non-DREAM)의 첫 non-empty 엔트리 본문(`summaryEntryHtmlOf`, `journal-content`/`v-html`)은 레거시 `collapse-3`(최대 3줄)·`expand-btn`으로 미리보고 클릭·더보기로 전체를 펼친다. 접힘 중에는 빈 문단을 제거·문단 여백을 줄이고, 펼침 시 원문 HTML·간격을 그대로 둔다. 데이터는 SEARCH 목록의 `journalChapterList`를 파생하며 추가 API는 없다. locale 변경은 선택 메타/태그·AND 필터·연도·일자 목록을 변경하지 않는다. 태그 입력 검색의 placeholder·카테고리 선택·미존재 태그 알림 문구는 엔트리 검색 키(`journal.entry.search.tag.*`, `journal.entry.search.category.*`)를 재사용한다.

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
