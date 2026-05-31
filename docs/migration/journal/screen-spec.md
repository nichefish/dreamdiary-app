# 저널 화면 마이그레이션 스펙 (Journal Screen Spec)

> 공통 컴포넌트/인터랙션 스펙은 ``common/`` 디렉토리 참조.  
> 게시판 화면 스펙: `board/screen-spec.md`  
> 인증/사용자 화면 스펙(내 정보 포함): `auth/screen-spec.md`  
> 사용자 관점 화면 행동 명세(UX flow, 라이프사이클 흐름): `docs/spec/JOURNAL_SCREEN_BEHAVIOR_SPEC.md`



## Vue SPA 구현 현황 (소스 기준)

> 레거시 FTL/브리지 설명은 아래 각 화면 절을 유지한다. **실제 동작·경로는 이 절과 `app/frontend-vue/src` 가 우선**한다.
> 라우트: `app/frontend-vue/src/router/index.ts` · URL 매핑: `app/frontend-vue/src/utils/urlMapping.ts`

### 아키텍처 (수렴 후)

| 축 | 레거시 | Vue SPA |
|----|--------|---------|
| 목록 렌더 | FTL `#journal_day_list_div` + `JournalDay*ListApp` 텔레포트/브리지 | `JournalDayCard.vue` + `useJournalStore.dayList` |
| 상태 | `window.JOURNAL`, `JournalDayMonthlyApp.*` pending 큐 | Pinia `stores/journal.ts` |
| 탭 전환 | `dF.JournalDayViewService.changeView(url)` | `router-link` (`journal-monthly` 등) |
| 모달 | Bootstrap + 레거시 서비스 | `JournalDayLayout.vue` + `useJournalModalStore` |
| 공통 첨부 | `CommentList.modal`, `FileGroupList.modal` | `useAttachableModalStore` |

### 라우트·화면 매핑

| 화면 | 레거시 URL (대표) | Vue route | Vue view | 구현 |
|------|-------------------|-----------|----------|------|
| 저널 기본 진입 | — | `/journal` → `/journal/weekly` | `JournalDayWeekly.vue` | ✓ |
| 저널 월간 | `/app/journal/day/monthly.do` | `/journal/monthly` | `JournalDayMonthly.vue` | ✓ |
| 저널 주간 | `/app/journal/day/weekly.do` | `/journal/weekly` | `JournalDayWeekly.vue` | ✓ |
| 저널 일간 | `/app/journal/day/daily.do` | `/journal/daily` | `JournalDayDaily.vue` | ✓ 새 창 전용, SystemLayout, 사이드바/헤더 없음, 이전/다음 네비 |
| 저널 달력 | `/app/journal/day/cal.do` | `/journal/calendar` | `JournalDayCalendar.vue` | △ 탭/플레이스홀더 |
| 저널 메타 | `/app/journal/day/meta.do` | `/journal/meta` | `JournalDayMeta.vue` | ⚠ 그래프 TODO |
| 연간 결산 목록 | FTL annual list | `/annual` | `JournalAnnualList.vue` | ✓ |
| 연간 결산 상세 | `/journal/annual/detail?...` | `/annual/:yy` | `JournalAnnualDetail.vue` | ✓ |
| 스레드 목록 | `/journal/thread/list` | `/thread` | `JournalThreadList.vue` | ✓ |
| 내 정보 | `/app/user/my/page.do` | `/my` | `UserMyPage.vue` | ✓ |
| 일정 | `/app/schedule/cal.do` | `/schedule` | `ScheduleCalendar.vue` | ✓ |
| 대시보드 | — | `/dashboard` | `Dashboard.vue` | ⚠ placeholder |

### 저널 일간 화면 (journal-daily) 스펙

- **용도**: 단일 날짜를 새 창으로 열어 집중 편집
- **레이아웃**: `SystemLayout` > `JournalDayDailyLayout` — 헤더/사이드바 없음, 태그클라우드 없음
- **네비게이션**: 상단 이전/다음 버튼으로 날짜 이동 (`router.replace` + `stdrdDt` query)
- **모달**: `JournalDayDailyLayout`에 전체 편집 모달 포함 (일자 수정, 챕터, 엔트리, 태그, 메타 등)
- **진입점**: `JournalDayCard` 컨텍스트 메뉴 "새 창으로 열기 (일자 뷰)", 태그 상세 모달 일자 행 버튼
- **URL**: `/journal/daily?stdrdDt=YYYY-MM-DD`
- **새 창 오픈**: `window.open(url, "_blank", "width=...,height=...")` — features 지정으로 탭 아닌 창 강제

### 저널 API (`useJournalStore`)

- `GET /api/journal/days` — `fetchDays` (`viewType`, `yy`, `mnth`, 필터, `weekStartDt`, `stdrdDt`)
- `GET /api/journal/day/metas` — `fetchMetas`
- `GET /api/journal/day/tag/cloud` — `fetchTagCloud`

`sort` / `sortOrder` 는 스토어·UI 모두 구현되어 있으며, 기본값은 `DESC`이고 `localStorage("journal_day_sort")` 로 유지한다.

### 사이드바 메뉴

`SidebarMenu.vue` → `useMenuStore.fetchUserMenu()` + `toVuePath(menu.url)`. 기본 메뉴 accordion은 항상 펼쳐두며, 2depth 내부 key성 `menuLabel`(`JOURNAL_DAY` 등)은 화면에 표시하지 않는다.

---

## 저널 일자 월간 목록 (Journal Day Monthly)

- **Route (레거시)**: `/journal/day/monthly` · **Vue SPA**: `/journal/monthly` (`journal-monthly`)
- `/journal` 기본 진입은 주간 뷰(`/journal/weekly`)로 redirect한다.
- **Legacy file**: `legacy/templates/view/feature/journal/day/journal_day_monthly.ftlh`

### Layout Structure

- 레이아웃: `layout_with_aside.ftlh` 사용 (사이드바 있음)
- 툴바 영역: `_journal_day_page_header.ftlh` include (공통 저널 일자 헤더)
- 메인 영역: `.journal-day-monthly-page` div
  - 상단: `JournalDayViewToolbar.vue` (탭 네비게이션 + 저널 일자 등록 버튼, flex `justify-content-between`) — 레거시 본문 상단 탭 행 + `_journal_day_page_header.ftlh` 의 `header_btn_reg_modal` 동작
  - 카드: `.card.post` (margin-top: 0 !important)
    - 카드 헤더: 태그 헤더 (`_journal_day_tag_header.ftlh`) → Vue: `JournalTagCloudHeader.vue` ✓ 구현 완료
    - 카드 바디: `JournalDayCard` (`store.dayList`) — 레거시 `#journal_day_list_div` 텔레포트는 SPA 미사용
  - 사이드 패널: `_journal_day_aside_base.ftlh` include
- 숨겨진 마운트 루트: `<div id="journal_day_app" class="d-none" data-view-type="monthly">`

### Key UI Elements

| Element | Type | Legacy class/id | Data source | Notes |
|---------|------|----------------|-------------|-------|
| 뷰 전환 탭 | `<ul class="nav nav-tabs nav-tabs-line">` | `.nav-item`, `.nav-link` | 없음(정적) | 주간/월간/달력/메타 4개 탭 |
| 주간 탭 | `<a>` | `.nav-link.px-6.cursor-pointer` | `Url.JOURNAL_DAY_WEEKLY` | `bi-calendar-week` 아이콘 |
| 월간 탭 (현재) | `<a>` | `.nav-link.px-6.cursor-pointer.active` | `Url.JOURNAL_DAY_MONTHLY` | `bi-people` 아이콘, active 상태 |
| 달력 탭 | `<a>` | `.nav-link.px-6.cursor-pointer` | `Url.JOURNAL_DAY_CAL` | `bi-sort-down-alt` 아이콘 |
| 메타 탭 | `<a>` | `.nav-link.px-6.cursor-pointer` | `Url.JOURNAL_DAY_META_VIEW` | `bi-bar-chart-line` 아이콘 (Vue) |
| 탭 라벨 | text | — | — | `주간 VIEW` / `월간 VIEW` / `달력 VIEW` / `메타 VIEW` |
| 상단 뷰 툴바 | Vue | `JournalDayViewToolbar` | — | 주간/월간/달력/메타 탭 + 우측 등록 버튼; `JournalMonthly` / `JournalWeekly` / `JournalCalendar` / `JournalMeta` 공유 |
| 저널 일자 등록 | `<button>` | `.btn-light-primary.btn-outlined` | `useJournalModalStore.openDayRegist()` | 레거시 `header_btn_reg_modal` → `data-journal-day-action=reg-modal`; 라벨 「저널 일자 등록」, `bi-calendar-plus`; `d-none d-md-flex` |
| 키워드 검색 | include | `_journal_day_keyword_search.ftlh` | 검색 파라미터 | 팝업 형태 — SPA ❌ MISSING |
| 태그 헤더 | include | `_journal_day_tag_header.ftlh` | 태그 목록 | 카드 헤더 내부 |
| 목록 컨테이너 | `<div>` | `#journal_day_list_div` | Vue 렌더 | `JournalDayMonthlyListApp` 마운트 대상 |

### Action Buttons & Interactions

| Action | Trigger | Legacy handler | Expected behavior |
|--------|---------|---------------|-------------------|
| 주간 뷰로 전환 | 탭 클릭 | `dF.JournalDayViewService.changeView(Url.JOURNAL_DAY_WEEKLY)` | 주간 보기 페이지로 이동 |
| 월간 뷰로 전환 | 탭 클릭 | `dF.JournalDayViewService.changeView(Url.JOURNAL_DAY_MONTHLY)` | 현재 페이지 유지(active) |
| 달력 뷰로 전환 | 탭 클릭 | `dF.JournalDayViewService.changeView(Url.JOURNAL_DAY_CAL)` | 달력 보기 페이지로 이동 |
| 메타 뷰로 전환 | 탭 클릭 | `dF.JournalDayViewService.changeView(Url.JOURNAL_DAY_META_VIEW)` | 메타 보기 페이지로 이동 |
| 저널 일자 등록 | 상단 「저널 일자 등록」 버튼 | `JournalDayRuntimeService` (`data-journal-day-action=reg-modal`) | `JournalDayRegistModal` 신규 등록 오픈 (`openDayRegist()`) |

**레이아웃 전역 툴바** (`_journal_day_page_header.ftlh` 나머지): 고급필터·일정 등록·개인 일정·태그 카테고리 동기화 — SPA ❌ MISSING (`docs/JOURNAL_SCREEN_BEHAVIOR_SPEC.md` §4.1–4.3).

### Data Displayed

`JournalDayMonthlyListApp` Vue 앱이 `#journal_day_list_div`에 렌더한다.
- 월 단위 저널 일자 목록 (월별 카드 형태)
- 각 일자 카드에 일기(DIARY)/꿈(DREAM)/노트(NOTE) 엔트리 포함
- 정렬, 필터, 검색 파라미터 반영

**window 브리지 API** (Vue 마운트 전 큐잉):
- `JournalDayMonthlyApp.render(model)` — 목록 렌더
- `JournalDayMonthlyApp.clear()` — 목록 초기화
- `JournalDayMonthlyApp.sort(sort)` — 정렬 변경
- `JournalDayMonthlyApp.loadMonthly()` — 월간 로드
- `JournalDayMonthlyApp.loadWeekly(stdrdDt, targetDt)` — 주간 로드
- `JournalDayMonthlyApp.loadDaily(stdrdDt)` — 일간 로드
- `JournalDayMonthlyApp.applySearchParamsAndReload(patch, scope)` — 검색 파라미터 적용 후 리로드
- `JournalDayMonthlyApp.refresh()` — 갱신

**Vue SPA (Pinia)**: 위 `window` 브리지 API 는 레거시 전용. SPA에서는 `useJournalStore.fetchDays` / `dayList` / `JournalDayCard` 로 대체됨.


### Modals opened from this page

| Modal | ID | 열리는 조건 |
|-------|----|-----------|
| 저널 일자 등록/수정 | `journal_day_reg` | 일자 등록/수정 버튼 클릭 |
| 저널 일자 상세 | `journal_day_dtl` | 일자 행 클릭 |
| 일기 등록/수정 | `journal_diary_reg` | 일기 등록/수정 버튼 클릭 |
| 꿈 등록/수정 | `journal_dream_reg` | 꿈 등록/수정 버튼 클릭 |
| 노트 등록/수정 | `journal_entry_reg` (NOTE) | 노트 등록/수정 버튼 클릭 |
| 챕터 등록 | `_journal_chapter_regist_modal.ftlh` | 챕터 등록 버튼 클릭 |
| 꿈 해석 등록 | `_journal_interpretation_regist_modal.ftlh` | 해석 등록 버튼 클릭 |
| 저널 일자 태그 상세 | `_journal_day_tag_dtl_modal.ftlh` | 태그 클릭 |
| 저널 일자 메타 | `_journal_day_meta_modal.ftlh` | 메타 버튼 클릭 |
| 태그 목록 | `_journal_tag_list_modal.ftlh` | 태그 관련 팝업 |
| 태그 프로필 | `_tag_profile_modal.ftlh` | 태그 프로필 클릭 |
| 이력 | `_history_modal.ftlh` | 이력 버튼 클릭 |
| 댓글 등록 | `_comment_reg_modal.ftlh` | 댓글 등록 버튼 클릭 |

### Special behaviors

- `Model.locale`을 JS 변수로 노출 (`${.locale?replace('_', '-')?js_string}`)
- Vue 앱 마운트 전 pending 큐 패턴 사용: `JournalDayMonthlyApp`, `JournalDayTagDetailVueApp`, `JournalDayTagPanelVueApp`, `JournalDayTagProfileVueApp` 모두 마운트 전 호출을 큐잉
- TinyMCE 에디터 로드 (일기/꿈/노트 본문 편집용)
- FullCalendar 로드 (달력 뷰 전환 대비)
- Prism.js 코드 하이라이팅 로드
- 태그 헤더: `JournalTagCloudHeader.vue` 컴포넌트 (`v-if="store.showTagCloud"`). `store.tagCloud` 상태를 읽고 `onMounted`/`watch([yy, mnth])`에서 `store.fetchTagCloud()` 호출

---

## 저널 일자 주간 목록 (Journal Day Weekly)

- **Route (레거시)**: `/journal/day/weekly` · **Vue SPA**: `/journal/weekly` (`journal-weekly`)
- **Legacy file**: `legacy/templates/view/feature/journal/day/journal_day_weekly.ftlh`

### Layout Structure

- 레이아웃: `layout_with_aside.ftlh` (사이드바 있음)
- 툴바: `_journal_day_page_header.ftlh` (공통 저널 일자 헤더)
- 메인 영역: `.journal-day-weekly-page` div
  - 상단: `JournalDayViewToolbar.vue` (탭 + 저널 일자 등록 버튼) — 월간 목록과 동일 컴포넌트
  - 카드: `.card.post` (margin-top: 0 !important)
    - 카드 헤더: 태그 헤더 (`_journal_day_tag_header.ftlh`) → Vue: `JournalTagCloudHeader.vue` ✓ 구현 완료
    - 카드 바디: `JournalDayCard` (`JournalDayWeekly.vue`, `viewType: WEEKLY`)
  - 사이드 패널: `_journal_day_aside_base.ftlh`
- 숨겨진 마운트 루트: `<div id="journal_day_app" class="d-none" data-view-type="weekly">`

### Key UI Elements

| Element | Type | Legacy class/id | Data source | Notes |
|---------|------|----------------|-------------|-------|
| 주간 탭 (현재) | `<a>` | `.nav-link.active` | `Url.JOURNAL_DAY_WEEKLY` | `bi-calendar-week` 아이콘, active |
| 월간 탭 | `<a>` | `.nav-link` | `Url.JOURNAL_DAY_MONTHLY` | `bi-view-stacked` 아이콘 |
| 달력 탭 | `<a>` | `.nav-link` | `Url.JOURNAL_DAY_CAL` | `bi-calendar3` 아이콘 |
| 메타 탭 | `<a>` | `.nav-link` | `Url.JOURNAL_DAY_META_VIEW` | `bi-bar-chart-line` 아이콘 |
| 상단 뷰 툴바 | Vue | `JournalDayViewToolbar` | — | 월간 목록과 동일 (탭 + 저널 일자 등록) |
| 저널 일자 등록 | `<button>` | `.btn-light-primary.btn-outlined` | `openDayRegist()` | 월간 목록 표와 동일 |
| 목록 컨테이너 | `<div>` | `#journal_day_list_div` | Vue 렌더 | `JournalDayWeeklyListApp` 마운트 대상 |

### Action Buttons & Interactions

월간 목록과 동일 (탭 전환 + 저널 일자 등록). 주간 탭이 active 상태.

| Action | Trigger | Legacy handler | Expected behavior |
|--------|---------|---------------|-------------------|
| 저널 일자 등록 | 상단 「저널 일자 등록」 버튼 | `JournalDayRuntimeService` `reg-modal` | `JournalDayRegistModal` 신규 등록 (`openDayRegist()`) |

### Data Displayed

- `window.JOURNAL.stdrdDt`: 서버에서 현재 기준일(`${stdrdDt?js_string}`)을 JS에 노출
- 주별 저널 일자 목록 (7일 단위)
- `JournalDayWeeklyApp` 브리지 API: Monthly와 동일한 구조 (`render`, `clear`, `sort`, `loadMonthly`, `loadWeekly`, `loadDaily`, `applySearchParamsAndReload`, `refresh`)

### Modals opened from this page

월간 목록과 동일한 모달 세트 포함.

### Special behaviors

- 월간과 구조 동일. 차이점: `data-view-type="weekly"`, `JournalDayWeeklyListApp` 사용, `window.JOURNAL.stdrdDt` 노출

---

## 저널 결산 목록 (Journal Annual List)

- **Route (레거시)**: `/journal/annual/list` · **Vue SPA**: `/annual` (`annual-list`)
- **Legacy file**: `legacy/templates/view/feature/journal/annual/journal_annual_list.ftlh`

### Layout Structure

- 레이아웃: `layout_with_aside.ftlh` (사이드바 있음)
- 툴바: `_journal_annual_list_header.ftlh`
- 메인 영역:
  - 요약 카드: `.card.post` — 전체 꿈 통계 표시
  - 목록: `#journal_annual_list_div` — `JournalAnnualListApp` Vue 마운트 대상
  - 사이드 패널: `_journal_annual_aside_base.ftlh`
- 히든 폼: `#procForm` (GET, `id` hidden)

### Key UI Elements

| Element | Type | Legacy class/id | Data source | Notes |
|---------|------|----------------|-------------|-------|
| 요약 카드 | `.card.post > .card-body` | `.d-flex-between.fs-5` | `totalAnnual` 서버 모델 | 전체 꿈 통계 표시 |
| 꿈 일수 | `<span>` | `.text-info.fw-bold.mx-1` | `totalAnnual.dreamDayCnt` | "(N일" 형식 |
| 꿈 건수 | `<span>` | `.text-info.fw-bold.mx-1` | `totalAnnual.dreamCnt` | "/ N건)" 형식 |
| 결산 카드 목록 | `<div>` | `#journal_annual_list_div` | Vue 렌더 | `JournalAnnualListItem` 컴포넌트 반복 |
| 연도 필터 | `<select>` | annual aside | `store.filterYy` | 선택 시 해당 연도 결산만 표시 |
| 목록 키워드 필터 | `<input>` | annual aside | `store.listKeyword` | 연도 필터 미선택 시 전체 목록을 축소 |

### Action Buttons & Interactions

| Action | Trigger | Legacy handler | Expected behavior |
|--------|---------|---------------|-------------------|
| 결산 등록 | 툴바 버튼 | `dF.JournalAnnual` 서비스 | 결산 등록 모달 오픈 |
| 결산 카드 클릭 | 목록 카드 클릭 | `JournalAnnualListItem` Vue 내부 | 결산 상세 페이지 이동 |
| 태그 클릭 | 태그 배지 클릭 | `dF.JournalDayTagService.select(...)` | 태그 상세 모달 |
| 연도 필터 변경 | annual aside select 변경 | Vue store | 연도 필터가 키워드 필터보다 우선하며 AND 조건으로 결합하지 않음 |
| 키워드 필터 입력 | annual aside input 입력/Enter/버튼 | Vue store | 전체 목록 상태에서 제목·본문·태그 기준으로 목록 축소 |

### Data Displayed

- `totalAnnual.dreamDayCnt`: 전체 꿈 일수
- `totalAnnual.dreamCnt`: 전체 꿈 건수
- 결산 목록: `JournalAnnualListApp`이 AJAX로 로드하여 `JournalAnnualListItem` 컴포넌트로 렌더

### Modals opened from this page

| Modal | 파일 | 열리는 조건 |
|-------|------|-----------|
| 결산 등록 | `_journal_annual_regist_modal.ftlh` | 결산 등록 버튼 클릭 |
| 태그 목록 | `_journal_tag_list_modal.ftlh` | 태그 관련 팝업 |

### Special behaviors

- 결산 등록 후 목록 갱신: `JournalAnnualListApp.init() + listAjax()` (레거시 IIFE 동등)
- `preloadJournalDayTagService.js` 별도 적재 (태그 클릭 시 `dF.JournalDayTagService.select` 호출 대비)
- 꿈 아이콘: `bi bi-moon-stars fs-4`
- Vue SPA 목록 필터 우선순위:
  - 연도 필터가 선택되어 있으면 키워드 필터 값과 무관하게 해당 연도 결산만 표시한다.
  - 연도 필터가 비어 있을 때만 키워드 필터가 전체 결산 목록을 축소한다.
  - 따라서 연도 필터와 키워드 필터는 AND 조건이 아니다.
- 키워드 필터 값이 있으면 목록 본문 렌더링 시 일치 텍스트를 `<mark class="journal-annual-list-vue__keyword-mark">`로 하이라이트한다. HTML 문자열 자체를 정규식 치환하지 않고 DOM 텍스트 노드만 감싸서 마크다운 HTML 구조를 보존한다.

---

## 저널 결산 상세 (Journal Annual Detail)

- **Route (레거시)**: `/journal/annual/detail?id=N&yy=YYYY` · **Vue SPA**: `/annual/:yy` (`annual-detail`)
- **Legacy file**: `legacy/templates/view/feature/journal/annual/journal_annual_detail.ftlh`

### Layout Structure

- 레이아웃: `layout_default.ftlh` (사이드바 없음)
- 툴바: 서버에서 `toolbar` 변수로 전달
- 메인 영역:
  - 결산 상세 카드: `#journal_annual_detail_div.card.post` — `JournalAnnualDetailCardApp` 텔레포트 대상
  - 탭 네비게이션: DIARY(일기 요약) / DREAM(꿈 요약) 2개 탭
  - 태그 헤더: `_journal_annual_detail_tag_header.ftlh` include + `<hr>`
  - 중요도/참조 토글 체크박스 영역
  - 태그 분류 div 3개: `.journal_annual_day_tag_div`, `.journal_annual_diary_tag_div`, `.journal_annual_dream_tag_div`
  - 엔트리 목록: `.card-post.p-10`
    - 중요 일기: `#journal_annual_diary_list_div`
    - 중요 꿈: `#journal_annual_imprtc_dream_list_div`
- 히든 폼: `#procForm` (GET, `id`, `yy`, `mnth` hidden; mnth 기본값 "99")

### Key UI Elements

| Element | Type | Legacy class/id | Data source | Notes |
|---------|------|----------------|-------------|-------|
| 결산 상세 카드 | `<div>` | `#journal_annual_detail_div.card.post` | `JournalAnnualDetailCardApp` | Vue 텔레포트 |
| DIARY 탭 | `<a>` | `.nav-link.${(section=='DIARY')?then('active','')}` | `section` 서버 변수 | `bi-book` 아이콘 |
| DREAM 탭 | `<a>` | `.nav-link.${(section=='DREAM')?then('active','')}` | `section` 서버 변수 | `bi-moon-stars` 아이콘 |
| IMPORTANT 토글 | `<input type="checkbox" id="toggleImprtc">` | `#toggleImprtc` | 없음(기본 checked) | 중요 엔트리 표시/숨김 |
| REFERENCE 토글 | `<input type="checkbox" id="toggleRefrnc">` | `#toggleRefrnc` | 없음(기본 checked) | 참조 엔트리 표시/숨김 |
| 일자 태그 div | `<div>` | `.journal_annual_day_tag_div` | `JournalAnnualEntryTagListApp` | 일자 태그 헤더 |
| 일기 태그 div | `<div>` | `.journal_annual_diary_tag_div` | `JournalAnnualEntryTagListApp` | 일기 태그 헤더 |
| 꿈 태그 div | `<div>` | `.journal_annual_dream_tag_div` | `JournalAnnualEntryTagListApp` | 꿈 태그 헤더 |
| 중요 일기 목록 | `<div>` | `#journal_annual_diary_list_div` | `JournalAnnualEntryListApp` | Vue 렌더 |
| 중요 꿈 목록 | `<div>` | `#journal_annual_imprtc_dream_list_div` | `JournalAnnualEntryListApp` | Vue 렌더 |

### Action Buttons & Interactions

| Action | Trigger | Legacy handler | Expected behavior |
|--------|---------|---------------|-------------------|
| DIARY 탭 클릭 | 탭 클릭 | `dF.JournalAnnual.detailViewWithSection('DIARY')` | 일기 요약 섹션 활성화 |
| DREAM 탭 클릭 | 탭 클릭 | `dF.JournalAnnual.detailViewWithSection('DREAM')` | 꿈 요약 섹션 활성화 |
| IMPORTANT 토글 | 체크박스 change | `dF.JournalAnnual.toggleParam()` | 중요 엔트리 표시/숨김 |
| REFERENCE 토글 | 체크박스 change | `dF.JournalAnnual.toggleParam()` | 참조 엔트리 표시/숨김 |

### Data Displayed

- 결산 카드: 연도, 요약 정보 (JournalAnnualDetailCardApp)
- 태그 헤더 3행: 일자 태그 / 일기 태그 / 꿈 태그 (JournalAnnualEntryTagListApp)
- 중요 일기 목록: `JournalAnnualEntryListApp` (DIARY 섹션)
- 중요 꿈 목록: `JournalAnnualEntryListApp` (DREAM 섹션)

### Modals opened from this page

| Modal | 파일 | 열리는 조건 |
|-------|------|-----------|
| 꿈 등록/수정 | `_journal_entry_reg_modal.ftlh` (DREAM) | 꿈 수정/복사 버튼 |
| 결산 리뷰 등록 | `_journal_annual_review_regist_modal.ftlh` | 리뷰 등록 버튼 |
| 태그 목록 | `_journal_tag_list_modal.ftlh` | 태그 팝업 |
| 댓글 등록 | `_comment_reg_modal.ftlh` | 댓글 등록 버튼 |

### Special behaviors

- 탭 active 상태는 서버 변수 `section` 에 따라 초기 설정 (`DIARY` 또는 `DREAM`)
- 토글 체크박스 2개: 기본 모두 checked 상태
- IMPORTANT/REFERENCE 라벨: 영문 대문자 표기
- 부트 순서: `journalAnnualCrudService` → `journalAnnualStateService` → `journalAnnualService` → `JournalAnnualDetailCardApp` → `JournalAnnualEntryTagListApp` → `JournalAnnualEntryListApp` → `JournalAnnualDetailPageBoot`
- `preloadJournalDayTagService.js` 적재 (태그 클릭 대비)

---

## 저널 스레드 목록 (Journal Thread List)

- **Route (레거시)**: `/journal/thread/list` · **Vue SPA**: `/thread` (`thread-list`)
- **Legacy file**: `legacy/templates/view/feature/journal/thread/journal_thread_list.ftlh`

### Layout Structure

- 레이아웃: `layout_default.ftlh` (사이드바 없음)
- 툴바: `_journal_thread_list_header.ftlh`
- 메인 영역:
  - 태그 클릭 필터바: `_tag_list_header.ftlh` include
  - 카드: `.card.post`
    - 카드 바디: 테이블 (`#journal_thread_list_div` tbody에 Vue `JournalThreadListApp` 텔레포트)
    - 카드 푸터: `_pagination.ftlh`
- 히든 폼: `#procForm` (GET, `id` hidden)
- 마운트 루트: `<div id="journal_thread_list_app" class="d-none">`

### Key UI Elements

| Element | Type | Legacy class/id | Data source | Notes |
|---------|------|----------------|-------------|-------|
| 태그 필터바 | include | `_tag_list_header.ftlh` | 태그 목록 | 태그 클릭 필터 |
| 테이블 | `<table>` | `.table.align-middle.table-row-dashed.fs-small.gy-5.table-fixed.hoverTable.mb-3` | 서버 모델 | 고정 레이아웃, 행 hover |
| 번호 열 | `<th>` | `.text-center.wb-keepall.w-10.hidden-table` | `post.rnum` | 모바일 숨김 |
| 제목 열 | `<th>` | `.col-lg-9.col-9.text-center.wb-keepall` | `post.title` | `txt.title` i18n |
| 첨부파일 열 | `<th>` | `.col-lg-1.text-center.wb-keepall.hidden-table` | `post.hasFiles` | 모바일 숨김 |
| 목록 tbody | `<tbody>` | `#journal_thread_list_div` | `JournalThreadListApp` Vue 텔레포트 | 행 직접 주입 |
| 페이지네이션 | include | `_pagination.ftlh` | `paginationInfo` | 기존 서버사이드 페이지네이션 유지 |

### Action Buttons & Interactions

| Action | Trigger | Legacy handler | Expected behavior |
|--------|---------|---------------|-------------------|
| 상세 페이지 이동 | 제목 링크 클릭 | `JournalThreadListApp` CustomEvent | 상세 페이지 이동 |
| 모달 상세 보기 | 모달 아이콘 클릭 | `JournalThreadListApp` CustomEvent | 상세 모달 오픈 |
| 댓글 모달 | 댓글 수 클릭 | `CommentList.modal(id, contentType)` | 댓글 목록 모달 |
| 파일 모달 | 첨부파일 아이콘 클릭 | `FileGroupList.modal(fileGroupId)` | 파일 목록 모달 |
| 태그 상세 | 태그 클릭 | `dF.Tag.dtlModal(tagId)` | 태그 상세 모달 |

### Data Displayed

서버에서 `#journal_thread_list_data` (JSON script tag)로 데이터 전달:

| 필드 | 타입 | 설명 |
|------|------|------|
| `rnum` | number | 행 번호 |
| `id` | number | 스레드 ID |
| `contentType` | string | 컨텐츠 타입 |
| `categoryName` | string | 카테고리명 |
| `title` | string | 제목 |
| `isNew` | boolean | 신규 여부 (N 배지 표시) |
| `commentCnt` | number | 댓글 수 |
| `fileGroupId` | string | 파일 그룹 ID |
| `hasFiles` | boolean | 첨부파일 유무 |
| `hasTagsLayout` | boolean | 태그 레이아웃 표시 여부 |
| `tags[].tagId` | string | 태그 ID |
| `tags[].ctgr` | string | 태그 카테고리 |
| `tags[].name` | string | 태그명 |

라벨 데이터: `#journal_thread_label_data` (JSON script tag) — `pageDetail`, `comment`, `atchFile`, `tagContentList`, `modalView`, `emptyList`

### Modals opened from this page

| Modal | 파일 | 열리는 조건 |
|-------|------|-----------|
| 댓글 목록 | `_comment_list_modal.ftlh` | 댓글 수 클릭 |
| 파일 목록 | `_file_list_modal.ftlh` | 첨부파일 아이콘 클릭 |
| 스레드 상세 | `_journal_thread_detail_modal.ftlh` | 모달 아이콘 클릭 |

### Special behaviors

- `isNew` true이면 `.badge.border-0.text-white.bg-noti.blink.fs-8.ms-2` 에 "N" 표시 (blink 애니메이션)
- 태그 있으면 `<span class="me-6.fs-7">` 내 태그 배지 목록 표시
- 카테고리명 있으면 `<span class="ctgr-span ctgr-gray">` 형태로 제목 앞에 표시

---

## 저널 일자 사이드바 전체 구조 (Journal Day Aside — Full Spec)

> **마이그레이션 가장 큰 누락 영역.** 아래 4개 섹션이 모두 구현되어야 한다.
> 레거시에서 이미 Vue 미니앱으로 전환된 코드를 Vue SPA Pinia store 구조로 재이식하는 작업이다.

### 최상위 aside 컨테이너 구조

레거시 FTL (`_journal_day_aside_base.ftlh`) 기준 전체 DOM 골격:

```html
<!-- Metronic aside drawer 컨테이너 — 모바일 lg 미만에서 drawer로 동작 -->
<div id="kt_app_aside" class="app-aside flex-column mt-7"
     data-kt-drawer="true"
     data-kt-drawer-name="app-aside"
     data-kt-drawer-activate="{default: true, lg: false}"
     data-kt-drawer-overlay="true"
     data-kt-drawer-width="auto"
     data-kt-drawer-direction="end"
     data-kt-drawer-toggle="#kt_app_aside_toggle">

    <!-- scroll wrapper -->
    <div id="kt_app_aside_wrapper" class="hover-scroll-y ps-5 pe-2 mx-5 my-5"
         data-kt-scroll="true" data-kt-scroll-activate="true" data-kt-scroll-height="auto"
         data-kt-scroll-dependencies="#kt_app_header" data-kt-scroll-wrappers="#kt_app_aside"
         data-kt-scroll-offset="5px" data-kt-scroll-save-state="true">

        <!-- 필터 카드 (섹션 1+2+3 통합) -->
        <div class="card card-reset card-p-0">
            <!-- 섹션 1: filter header 마운트 포인트 -->
            <div id="journal_day_aside_filter_header_mount" style="display:contents"></div>
            <div id="journal_aside" class="card-body">
                <input type="hidden" name="sort" id="sort">
                <div class="d-flex flex-column gap-4">
                    <!-- 섹션 2: 년월·주간·Pinpoint 마운트 포인트 -->
                    <div id="journal_day_aside_yy_mnth_mount" style="display:contents"></div>
                    <div class="separator"></div>
                    <!-- 섹션 3: 엔트리 필터 마운트 포인트 (챕터 옵션은 FTL bootstrap) -->
                    <div id="journal_day_aside_entry_filters_mount" style="display:contents"></div>
                </div>
            </div>
        </div>

        <div class="separator my-8"></div>

        <!-- 섹션 4: TODO 카드 마운트 포인트 -->
        <div id="journal_day_aside_todo_card_mount" style="display:contents"></div>
    </div>
</div>

<!-- 주간 네비게이터 앱 마운트 루트 (hidden) -->
<div id="journal_day_aside_week_nav_app" class="d-none"></div>
```

**Vue SPA 매핑**: `JournalDayLayout.vue` 우측 `JournalAside.vue` (`useJournalAsideStore` 표시 제어). Metronic `#kt_app_aside` drawer 는 SPA 저널에서 미사용(레거시 FTL 전용).

---

### 섹션 1: 필터 카드 헤더 (JournalDayAsideFilterHeaderApp)

**레거시 소스**: `legacy/static/vue/feature/journal/day/JournalDayAsideFilterHeaderApp.ts`

**정확한 HTML 구조**:
```html
<div id="journal_aside_header" class="card-header min-h-auto mb-5">
    <h3 class="card-title text-gray-900 fw-bold fs-3">
        <i class="bi bi-filter fs-2 me-1"></i> FILTER
    </h3>
    <div class="card-toolbar">
        <a href="javascript:void(0);"
           class="btn btn-sm btn-icon btn-color-gray-500 btn-light"
           data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
           title="정렬 변경"
           @click.prevent="sortAside">
            <i class="bi bi-sort-numeric-up-alt fs-2 pe-0" id="sortIcon"></i>
        </a>
    </div>
</div>
```

**Vue 구현 요구사항**:
| Element | 동작 | Vue 상태 |
|---------|------|---------|
| `#sortIcon` | 클릭 → 정렬 ASC/DESC 토글 | `store.sortOrder: 'ASC'|'DESC'` → 토글 후 `store.fetchDays()` |
| 아이콘 `bi-sort-numeric-up-alt` | 정렬 방향에 따라 아이콘 변경 | `sortOrder === 'ASC'` → `bi-sort-numeric-up-alt` / `DESC` → `bi-sort-numeric-down-alt` |

**현재 Vue 동등**: `JournalAside.vue` `#journal_aside_header` — FILTER 제목 + `#sortIcon` 정렬 토글 (`store.toggleSort`, `sortOrder` ASC/DESC).

---

### 섹션 2: 년월 · 주간 · Pinpoint (JournalDayAsideYyMnthApp)

**레거시 소스**: `legacy/static/vue/feature/journal/day/JournalDayAsideYyMnthApp.ts`

**정확한 HTML 구조 (3개 sub-block)**:

#### 2-a. 년·월 셀렉트 + 이전/다음월 화살표 + TODAY 버튼

```html
<div class="journal-day-aside-yy-mnth-vue-root">
    <div class="d-flex-between gap-4">
        <!-- 연도 컬럼 -->
        <div class="col">
            <span class="text-gray-900 fs-h6 fw-bold d-inline-block ms-6 mb-2">년</span>
            <div class="d-flex">
                <div class="d-flex align-items-center me-2">
                    <i id="left" class="bi bi-caret-left fs-2 cursor-pointer"
                       data-bs-toggle="tooltip" data-bs-placement="left"
                       title="이전 월"
                       @click="leftMonth"></i>
                </div>
                <!-- 연도 목록: window.__journalAsideYyMnthBootstrap.yyOptions 에서 읽음 -->
                <!-- Vue SPA: store.yy (현재 선택 연도) 바인딩 -->
                <select name="yy" id="yy" class="form-select" @change="onYyChange">
                    <option value="">----</option>
                    <option v-for="opt in yyOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
                </select>
            </div>
        </div>
        <!-- 월 컬럼 -->
        <div class="col">
            <span class="text-gray-900 fs-h6 fw-bold d-inline-block ms-2 mb-2">월</span>
            <button type="button"
                    class="btn btn-sm btn-outline btn-light-info blink-slow ms-6 ps-2 pe-1 py-0"
                    title="오늘 월로 이동" @click="todayMonth">
                TODAY <i class="bi bi-box-arrow-in-up-right"></i>
            </button>
            <div class="d-flex">
                <!-- 1~12 옵션 목록 -->
                <select name="mnth" id="mnth" class="form-select" @change="onMnthChange">
                    <option value="">--</option>
                    <option v-for="m in months" :key="m" :value="m">{{ m }}</option>
                </select>
                <div class="d-flex align-items-center ms-2">
                    <i id="right" class="bi bi-caret-right fs-2 cursor-pointer"
                       title="다음 월" @click="rightMonth"></i>
                </div>
            </div>
        </div>
    </div>
```

**현재 Vue 구현 차이**: `JournalAside.vue`는 `<select>` 대신 그리드+화살표 방식. `id="yy"`, `id="mnth"` 가 없어 레거시 JS 호환 불가. 연도는 최소 2010년부터 현재 연도까지 포함해야 함.

#### 2-b. 주간 네비게이터 (Week section) — **MISSING in Vue SPA**

```html
    <!-- Week section -->
    <div class="mt-0">
        <div class="d-flex-between align-items-center pe-3 mb-2">
            <div class="d-flex align-items-center flex-wrap gap-2">
                <span class="text-gray-900 fs-h6 fw-bold d-inline-block ms-6">Week</span>
                <!-- 현재 주 범위 표시: "MM-DD ~ MM-DD" -->
                <div class="btn btn-sm btn-light-primary py-1 px-3 pe-none fs-8">
                    <span id="journalAsideWeekRange" class="fw-semibold">----</span>
                </div>
            </div>
            <button type="button"
                    class="btn btn-sm btn-outline btn-light-info blink-slow ms-6 ps-2 pe-1 py-0"
                    title="오늘 주로 이동" @click="todayWeek">
                TODAY <i class="bi bi-box-arrow-in-up-right"></i>
            </button>
        </div>
        <div class="journal-aside-week-nav d-flex align-items-center gap-2">
            <div class="d-flex align-items-center me-2 cursor-pointer"
                 title="이전 주" @click="leftWeek">
                <i class="bi bi-caret-left fs-2"></i>
            </div>
            <!-- JournalDayAsideWeekNavigatorApp 가 이 div 내부에 요일 셀을 렌더 -->
            <!-- Vue SPA: 직접 v-for로 요일 셀 렌더 -->
            <div id="journalAsideWeekDays" class="journal-aside-week-days flex-grow-1"
                 aria-label="Weekly navigation">
                <!-- WeekDayItem { label: "월~일", dateStr: "YYYY-MM-DD", hasDay: boolean, isActive: boolean } -->
            </div>
            <div class="d-flex align-items-center me-2 cursor-pointer"
                 title="다음 주" @click="rightWeek">
                <i class="bi bi-caret-right fs-2"></i>
            </div>
        </div>
    </div>
```

**`WeekDayItem` 데이터 모델**:
```typescript
type WeekDayItem = {
    label: string;      // "월", "화", ..., "일"
    dateStr: string;    // "YYYY-MM-DD"
    hasDay: boolean;    // 해당 날짜에 저널 데이터 있는지
    isActive: boolean;  // 현재 선택 날짜인지
};
```

**주간 범위 라벨**: `weekStartDt ~ weekEndDt` (월요일 ~ 일요일, `MM-DD ~ MM-DD` 형식)

#### 2-c. Pinpoint 섹션 — **MISSING in Vue SPA**

```html
    <!-- Pinpoint section -->
    <div class="mt-0">
        <div class="text-gray-900 fs-h6 fw-bold d-inline-block ms-6 mb-2">Pinpoint</div>
        <div class="d-flex align-items-center justify-content-center px-8 mb-4 justify-content-between gap-1">
            <!-- 현재 yy/mnth 고정 버튼 -->
            <button type="button"
                    class="btn btn-sm btn-outline btn-light-primary px-2 pt-1"
                    title="현재 월 고정" @click="pinpoint">
                <i class="bi bi-bookmarks pe-0"></i>
            </button>
            <span class="mx-2">|</span>
            <!-- 고정된 yy/mnth 표시 -->
            <span id="journal_aside_pinText" class="px-1">
                <span id="pinnedYy" class="fs-6 text-muted text-underline-dotted">----</span>
                <span> / </span>
                <span id="pinnedMnth" class="fs-6 text-muted text-underline-dotted">--</span>
                <i class="bi bi-pin-map fs-7"></i>
            </span>
            <span class="mx-2">|</span>
            <!-- 고정 월로 돌아가기 버튼 -->
            <button type="button"
                    class="btn btn-sm btn-outline btn-light-primary px-2 pt-1"
                    title="고정된 월로 이동" @click="turnback">
                <i class="bi bi-reply-all pe-0"></i>
            </button>
        </div>
    </div>
</div> <!-- end journal-day-aside-yy-mnth-vue-root -->
```

**Pinpoint 상태**:
```typescript
// store 또는 로컬 ref로 관리
const pinnedYy = ref<number | null>(null);
const pinnedMnth = ref<number | null>(null);
// pinpoint(): pinnedYy = store.yy; pinnedMnth = store.mnth
// turnback(): store.gotoYyMnth(pinnedYy, pinnedMnth)
```

---

### 섹션 3: 엔트리 필터 (JournalDayAsideEntryFiltersApp)

**레거시 소스**: `legacy/static/vue/feature/journal/day/JournalDayAsideEntryFiltersApp.ts`

**챕터 카테고리 옵션 데이터 출처**:
- 레거시: FTL이 `window.__journalAsideEntryFiltersBootstrap.chapterCtgrOptions` 에 서버 코드 목록을 주입
- Vue SPA: `journalModalStore.fetchChapterCategories()` → `GET /api/code/items?groupCode=JOURNAL_CHAPTER_CTGR_CD`

**정확한 HTML 구조 (5개 블록)**:

```html
<div class="journal-day-aside-entry-filters-vue-root">

    <!-- 블록 A: TAGCLOUD 토글 -->
    <div class="d-flex flex-column gap-2 px-2">
        <div class="d-flex align-items-center justify-content-between"
             data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
             title="태그 클라우드를 표시/숨깁니다.">
            <label for="toggleTagCloud" class="text-muted cursor-help mb-0">TAGCLOUD</label>
            <input type="checkbox" id="toggleTagCloud"
                   class="form-check-input cursor-pointer m-0"
                   :checked="store.showTagCloud"
                   @change="toggleTagCloud">
        </div>
    </div>
    <div class="separator"></div>

    <!-- 블록 B: DIARIES 필터 블록 -->
    <div class="d-flex flex-column gap-2 mb-2 px-2">
        <!-- B-1: DIARIES 토글 -->
        <div class="d-flex align-items-center justify-content-between"
             data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
             title="일기를 표시/숨깁니다.">
            <label for="toggleDiaries" class="text-muted cursor-help mb-0">DIARIES</label>
            <input type="checkbox" id="toggleDiaries"
                   class="form-check-input cursor-pointer m-0"
                   :checked="store.showDiaries"
                   @change="toggleDiaries">
        </div>
        <!-- B-2: CHAPTER CATEGORIES 멀티셀렉트 (DIARIES 블록 안에만 있음) — MISSING -->
        <div id="chapterCtgrFilterSection" class="d-flex flex-column ps-3 gap-1">
            <div class="d-flex align-items-center justify-content-between">
                <label for="chapterCtgrFilter" class="text-muted mb-0">- CHAPTER CATEGORIES</label>
                <!-- 챕터 필터 활성화 여부 토글 (checked = 필터 적용 중) -->
                <input type="checkbox" id="toggleChapterCtgr"
                       class="form-check-input cursor-pointer m-0"
                       :checked="chapterCtgrEnabled"
                       @change="toggleChapterCtgr">
            </div>
            <!-- multiple size="4" 멀티셀렉트 — Ctrl+클릭으로 복수 선택 -->
            <select id="chapterCtgrFilter"
                    class="form-select form-select-sm w-100"
                    multiple size="4"
                    data-bs-toggle="tooltip" data-bs-placement="top"
                    title="Ctrl+클릭으로 여러 항목 선택"
                    @change="onChapterCtgrChange">
                <option value="__ALL__">{{ 전체 }}</option>
                <option v-for="ct in chapterCtgrOptions" :key="ct.code" :value="ct.code">
                    [{{ ct.codeName }}]
                </option>
            </select>
        </div>
        <!-- B-3: DIARY KEYWORDS 입력 -->
        <div class="d-flex flex-column ps-3 gap-1">
            <label for="diaryFilterKeyword" class="text-muted mb-0">- DIARY KEYWORDS</label>
            <div class="d-flex gap-1">
                <input type="text" name="diaryFilterKeyword" id="diaryFilterKeyword"
                       class="form-control form-control-sm"
                       v-model="store.diaryKeyword"
                       placeholder="일기 키워드" maxlength="200"
                       @keyup.enter="applyFilters">
                <button type="button" class="btn btn-sm btn-outline btn-light-primary px-3" disabled>
                    <i class="bi bi-funnel pe-0"></i>
                </button>
            </div>
        </div>
    </div>
    <div class="separator"></div>

    <!-- 블록 C: DREAMS 필터 블록 -->
    <div class="d-flex flex-column gap-2 mb-2 px-2">
        <!-- C-1: DREAMS 토글 -->
        <div class="d-flex align-items-center justify-content-between"
             data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
             title="꿈을 표시/숨깁니다.">
            <label for="toggleDreams" class="text-muted cursor-help mb-0">DREAMS</label>
            <input type="checkbox" id="toggleDreams"
                   class="form-check-input cursor-pointer m-0"
                   :checked="store.showDreams"
                   @change="toggleDreams">
        </div>
        <!-- C-2: DREAM KEYWORDS 입력 -->
        <div class="d-flex flex-column ps-3 gap-1">
            <label for="dreamFilterKeyword" class="text-muted mb-0">- DREAM KEYWORDS</label>
            <div class="d-flex gap-1">
                <input type="text" name="dreamFilterKeyword" id="dreamFilterKeyword"
                       class="form-control form-control-sm"
                       v-model="store.dreamKeyword"
                       placeholder="꿈 키워드" maxlength="200"
                       @keyup.enter="applyFilters">
                <button type="button" class="btn btn-sm btn-outline btn-light-primary px-3" disabled>
                    <i class="bi bi-funnel pe-0"></i>
                </button>
            </div>
        </div>
    </div>
    <div class="separator"></div>

    <!-- 블록 D: 고급 필터 아코디언 (Bootstrap accordion) — MISSING -->
    <div class="accordion accordion-flush" id="journal_day_filter_accordion">
        <div class="accordion-item">
            <h2 class="accordion-header" id="journal_day_filter_heading_advanced">
                <button class="accordion-button collapsed fw-semibold" type="button"
                        data-bs-toggle="collapse"
                        data-bs-target="#journal_day_filter_advanced"
                        aria-expanded="false"
                        aria-controls="journal_day_filter_advanced">
                    고급 필터
                </button>
            </h2>
            <div id="journal_day_filter_advanced"
                 class="accordion-collapse collapse"
                 aria-labelledby="journal_day_filter_heading_advanced"
                 data-bs-parent="#journal_day_filter_accordion">
                <div class="accordion-body pt-4">
                    <div class="text-muted fs-7">
                        추가 필터 옵션 (미구현 placeholder)
                    </div>
                    <div class="mt-4" data-filter-slot="advanced"></div>
                </div>
            </div>
        </div>
    </div>

</div> <!-- end journal-day-aside-entry-filters-vue-root -->
```

**엔트리 필터 Vue 상태 바인딩**:
| Element | 레거시 ID | Vue 상태 |
|---------|----------|---------|
| TAGCLOUD 체크박스 | `#toggleTagCloud` | `store.showTagCloud` — 변경 시 `store.fetchTagCloud()` |
| DIARIES 체크박스 | `#toggleDiaries` | `store.showDiaries` — 변경 시 `store.fetchDays()` |
| CHAPTER CATEGORIES 체크박스 | `#toggleChapterCtgr` | `chapterCtgrEnabled: ref(true)` — false 시 `store.chapterCtgrCds = []` |
| CHAPTER CATEGORIES 멀티셀렉트 | `#chapterCtgrFilter` | `store.chapterCtgrCds: string[]` — 변경 시 `store.fetchDays()` |
| 챕터 옵션 목록 | `window.__journalAsideEntryFiltersBootstrap.chapterCtgrOptions` | `journalModalStore.chapterCategoryOptions` — `fetchChapterCategories()` 로 로드 |
| `__ALL__` 선택 시 | 전체 선택 처리 | `store.chapterCtgrCds = []` (필터 없음으로 처리) |
| DREAMS 체크박스 | `#toggleDreams` | `store.showDreams` — 변경 시 `store.fetchDays()` |
| 일기 키워드 | `#diaryFilterKeyword` | `store.diaryKeyword` — Enter 시 `store.fetchDays()` |
| 꿈 키워드 | `#dreamFilterKeyword` | `store.dreamKeyword` — Enter 시 `store.fetchDays()` |

---

### 섹션 4: TODO 카드 (JournalDayAsideTodoCardApp)

**레거시 소스**: `legacy/static/vue/feature/journal/day/JournalDayAsideTodoCardApp.ts`

**API**:
- 목록 조회: `GET /api/journal/todo/list?yy=&mnth=` → `rsltList: TodoRow[]`
- 삭제: `DELETE /api/journal/todo/{id}`
- 등록: 별도 모달 (`#journal_todo_regist`) Bootstrap modal

**TodoRow 데이터 모델**:
```typescript
type TodoRow = {
    id: string | number;
    title: string;       // 할일 제목 (텍스트만, HTML 없음)
};
```

**정확한 HTML 구조**:

```html
<div class="card card-reset card-p-0">
    <!-- 카드 헤더: "TODO List" + 추가 버튼 -->
    <div id="journal_todo_aside_header" class="card-header min-h-auto mb-5">
        <h3 class="card-title text-gray-900 fw-bold fs-3">
            <i class="bi bi-list-task fs-2 me-1"></i> TODO List
        </h3>
        <div class="card-toolbar">
            <a href="javascript:void(0);"
               class="btn btn-sm btn-icon btn-primary"
               data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
               title="할일 추가"
               @click.prevent="openTodoReg">
                <i class="bi bi-plus fs-2 pe-0" id="journalTodoAsideRegistIcon"></i>
            </a>
        </div>
    </div>
    <!-- 카드 바디: todo 목록 -->
    <div id="journal_todo_list_div">
        <!-- 목록이 있을 때 -->
        <template v-if="todos.length > 0">
            <div v-for="item in todos" :key="'todo-' + item.id"
                 class="row d-flex-align-center justify-content-between">
                <!-- 제목 (overflow truncate, tooltip) -->
                <div class="col text-truncate cursor-pointer"
                     data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                     :title="item.title">
                    {{ item.title }}
                </div>
                <!-- 삭제 버튼 -->
                <div class="col-3 d-flex justify-content-end">
                    <button type="button"
                            class="btn btn-sm btn-light-danger btn-outlined py-2 px-3 cursor-pointer"
                            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                            title="삭제"
                            @click.prevent="deleteTodo(item.id)">
                        <i class="bi bi-trash p-0"></i>
                    </button>
                </div>
            </div>
        </template>
        <!-- 목록 없을 때 -->
        <div v-else class="journal-day d-flex-center">
            할일이 없습니다.
        </div>
    </div>
</div>
```

**TODO 카드 초기화 타이밍**:
- `onMounted`에서 `fetchTodos({ yy: store.yy, mnth: store.mnth })` 호출
- `watch([store.yy, store.mnth])` → 연/월 변경 시 재조회
- 등록 모달: `modalStore.openTodoReg({ yy: store.yy, mnth: store.mnth })` (기존 로직)
- 삭제: `DELETE /api/journal/todo/{id}` 성공 후 목록 재조회

**TODO 등록 모달** (`#journal_todo_regist`):
- Bootstrap modal: `modal-xl`, id=`journal_todo_regist`
- 폼: `#journalTodoRegistForm` (multipart/form-data)
- 본문 textarea: `#tinymce_journalTodoCn` (TinyMCE 에디터)
- Vue SPA: `journalModalStore.openTodoReg()` → 모달 open, `JournalTodoRegistModal.vue` 처리

---

### 섹션별 미구현 현황 (Vue SPA 기준)

| 섹션 | 기능 | 현재 상태 |
|------|------|---------|
| 1. 필터 헤더 | "FILTER" 제목 | ✅ `JournalAside.vue` `#journal_aside_header` |
| 1. 필터 헤더 | 정렬 토글 버튼 (`#sortIcon`) | ✅ `store.toggleSort` + 아이콘 토글 |
| 2. 년월 | 연도 `<select>` | ⚠ `JournalAside.vue` 연도 select + 2010~현재; 레거시 `id="yy"` 없음 |
| 2. 년월 | 월 선택 | ⚠ 3열 월 버튼 그리드 + `navigateMonth`; 레거시 `<select id="mnth">` 아님 |
| — | 태그 목록 버튼 | ✓ `attachableStore.openTagList` |
| — | 어사이드 닫기 | ✓ `useJournalAsideStore.hide()` |
| 2. 년월 | 이전/다음 월 화살표 | ✓ 구현 |
| 2. 년월 | TODAY 버튼 | ✓ 구현 |
| 2. 주간 | Week 범위 표시 | ✓ `weekRangeLabel` computed (`JournalAside.vue`) |
| 2. 주간 | 주간 요일 셀 내비게이터 | ✓ `weekDays` computed + `journal-aside-week-days` |
| 2. 주간 | 이전/다음 주 화살표 | ✓ `store.navigateWeek(-1/1)` |
| 2. 주간 | 주간 TODAY 버튼 | ✓ 공통 TODAY 버튼 (`store.gotoToday()`) |
| 2. Pinpoint | 핀 고정 버튼 | ❌ MISSING |
| 2. Pinpoint | 고정 yy/mnth 표시 | ❌ MISSING |
| 2. Pinpoint | 돌아가기 버튼 | ❌ MISSING |
| 3. 엔트리 필터 | TAGCLOUD 토글 | ✓ 구현 |
| 3. 엔트리 필터 | DIARIES 토글 | ✓ 구현 |
| 3. 엔트리 필터 | CHAPTER CATEGORIES (체크박스, 일기·노트 코드 병합) | ✓ |
| 3. 엔트리 필터 | 일기 키워드 | ✓ 구현 (위치 다름) |
| 3. 엔트리 필터 | DREAMS 토글 | ✓ 구현 |
| 3. 엔트리 필터 | 꿈 키워드 | ✓ 구현 (위치 다름) |
| 3. 엔트리 필터 | 고급 필터 아코디언 | ❌ MISSING |
| 4. TODO 카드 | "TODO List" 카드 헤더 | ❌ MISSING (등록 버튼만 있고 카드/헤더 없음) |
| 4. TODO 카드 | TODO 목록 표시 | ❌ MISSING |
| 4. TODO 카드 | TODO 삭제 버튼 | ❌ MISSING |

---

## 저널 엔트리 검색 새 창 (Journal Entry Search Popup)

- **Legacy screen**: `legacy/templates/view/feature/journal/entry/journal_entry_search.ftlh`
- **Vue SPA route**: `/journal/entry/search` (`journal-entry-search`)
- **Vue view**: `app/frontend-vue/src/views/journal/entry/JournalEntrySearchPage.vue`
- **Layout**: `SystemLayout.vue` 하위 auth route. 새 창 검색 화면이므로 `DefaultLayout` 메뉴와 `JournalLayout` aside를 렌더링하지 않는다.
- **Open pattern**: 태그 컨텍스트 메뉴의 `검색` 버튼에서 `window.open(...)`. 이미 이 검색 화면 안에 있는 경우에는 같은 창에서 route query를 갱신한다.

### URL Contract

| Query | 의미 |
|-------|------|
| `type=DIARY` | 일기 검색 |
| `type=DREAM` | 꿈 검색 |
| `tagIds=N` | 태그 ID 기반 검색 |
| `tagName=...` | 화면 표시용 태그명 |
| `searchKeywords=...` | 키워드 검색 |

### Data Contract

- 목록 API: `GET /api/journal/entries`
- 파라미터: `type`, `tagIds`, `searchKeywords`
- 응답: `AjaxResponse.rsltList` (`JournalEntryDto[]`)

### Behavioral Contract

- 태그 클릭 자체는 검색을 실행하지 않고 `JournalTagContextMenu`를 연다.
- 일기/꿈 태그의 `검색` 액션은 현재 월간/주간 목록 필터를 변경하지 않고 새 창 검색 화면으로 이동한다.
- 검색 팝업 내부에서 일기/꿈 태그의 `검색` 액션을 누르면 새 팝업을 열지 않고 같은 창의 URL query를 바꿔 결과를 재조회한다.
- 검색 팝업 내부의 `태그 설정` 액션은 `JournalTagProfileModal`을 같은 창에서 연다.
- 각 검색 결과는 수정/삭제가 가능하다. 수정 저장 성공 시 현재 검색 조건으로 목록을 다시 조회하고 저장한 결과 위치로 스크롤하며, 삭제 성공 시 결과 목록에서 제거한다.
- 검색 조건은 URL query에 남아야 한다. 검색 결과는 새로고침/공유 가능한 주소 기반 상태여야 한다.
- `결과 복사` 버튼은 현재 검색 결과 전체를 legacy 포맷(`날짜 (요일)`, `#정렬번호`, 본문)으로 클립보드에 복사한다.
- 각 검색 결과 행의 복사 버튼은 해당 엔트리 하나만 같은 포맷으로 복사한다.
