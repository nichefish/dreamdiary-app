# 저널 화면 마이그레이션 스펙 (Journal Screen Spec)

> 공통 컴포넌트/인터랙션 스펙은 ``common/`` 디렉토리 참조.  
> 게시판 화면 스펙: `board/screen-spec.md`  
> 인증/사용자 화면 스펙(내 정보 포함): `auth/screen-spec.md`  
> 사용자 관점 화면 행동 명세(UX flow, 라이프사이클 흐름): `docs/spec/JOURNAL_SCREEN_BEHAVIOR_SPEC.md`



## Vue SPA 구현 현황 (소스 기준)

> 레거시 FTL/브리지 설명은 아래 각 화면 절을 유지한다. **실제 동작·경로는 이 절과 `app/frontend-vue/src` 가 우선**한다.
> 라우트: `app/frontend-vue/src/app/router/index.ts` · URL 매핑: `app/frontend-vue/src/shared/utils/urlMapping.ts`

### 아키텍처 (수렴 후)

| 축 | 레거시 | Vue SPA |
|----|--------|---------|
| 목록 렌더 | FTL `#journal_day_list_div` + `JournalDay*ListApp` 텔레포트/브리지 | `JournalDayCard.vue` + `useJournalStore.dayList` |
| 상태 | `window.JOURNAL`, `JournalDayMonthlyApp.*` pending 큐 | Pinia `features/journal/stores/journal.ts` |
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
| 저널 달력 | `/app/journal/day/cal.do` | `/journal/calendar` | `JournalDayCalendar.vue` | ✓ |
| 저널 메타 | `/app/journal/day/meta.do` | `/journal/meta` | `JournalDayMeta.vue` | ✓ 컨텍스트 메뉴·단일 비교 차트(최대 2시리즈)·연도 전체 |
| 연간 결산 목록 | FTL annual list | `/annual` | `JournalAnnualList.vue` | ✓ |
| 연간 결산 상세 | `/journal/annual/detail?...` | `/annual/:yy` | `JournalAnnualDetail.vue` | ✓ |
| 스레드 목록 | `/journal/thread/list` | `/thread` | `JournalThreadList.vue` | ✓ |
| 스레드 등록 | `/app/journal/thread/regist-form.do` | `/thread/new` | `JournalThreadList.vue` | ✓ |
| 스레드 상세 | `/app/journal/thread/detail.do?id={id}` | `/thread/:id` | `JournalThreadList.vue` | ✓ |
| 스레드 수정 | `/app/journal/thread/modify-form.do?id={id}` | `/thread/:id/edit` | `JournalThreadList.vue` | ✓ |
| 내 정보 | `/app/user/my/page.do` | `/my` | `UserMyPage.vue` | ✓ |
| 일정 | `/app/schedule/calendar.do` | `/schedule` | `ScheduleCalendar.vue` | ✓ |

### 저널 달력 (`JournalDayCalendar.vue`)

- **레거시 원형**: `journal_day_cal.ftlh` + `JournalDayCalApp.ts` (git 이력 `210d8200a^`). 탭 툴바 + 카드(태그 클라우드 헤더 + FullCalendar) + aside(레이아웃 공통) 구성.
- **데이터**: `GET /api/journal/days?viewType=CAL` — `MyJournalDayCalService.getScheduleTotalCalList` = 저널 일자/일기/꿈 이벤트(`JournalDayCalDto`/`JournalEntryCalDto`) + 공휴일·행사(`getHolydayCalList`). 이벤트는 store `calEventList`(dayList 와 별도 상태)로 보관 — aside 의 월 이동·필터·초기화가 부르는 `fetchDays()` 가 CAL 분기로 자연 갱신.
- **달력 옵션**: 레거시 동일 — headerToolbar 는 title 만(자체 prev/next 없음, 월 이동은 aside), `eventOverlap: false`, 음수 margin-top 하네스 보정.
- **i18n**: 로딩 오버레이는 현재 locale의 클라이언트 카탈로그를 사용하고, FullCalendar의 월 제목·요일·기본 문구는 현재 locale에 대응하는 공식 locale 데이터를 사용한다. locale 변경은 현재 월·이벤트·필터·API 조회 조건을 변경하지 않는다.
- **이벤트 렌더**: icon HTML + title. DIARY/DREAM 중요(`imprtcYn=Y`) 시 `text-magenta blink fw-bold`(`.text-magenta` 는 레거시 commons.css 에서 이관). 클릭: JOURNAL_DAY → 일자 상세 모달, DIARY/DREAM → 소속 일자(`journalDayId`) 상세 모달, 일정 이벤트 무동작.
- **툴팁**: DAY=제목, DIARY/DREAM=마크다운 본문. 변경 전(레거시): jQuery tooltip + 페이지 전역 `.tooltip` 광폭 스타일 → 변경 후: bootstrap `Tooltip` + `customClass(journal-cal-tooltip)` 로 이 화면 툴팁에만 광폭 스타일 한정.
- **레거시와의 차이**: 상단 고급 필터 collapse(일정 종류 체크박스, 쿠키 저장)는 이식하지 않음 — 현행 백엔드 `getScheduleTotalCalList` 가 공휴일·행사만 병합해 해당 필터 파라미터를 소비하지 않는다(dead UI). 필요 시 백엔드 계약 복원과 함께 별도 작업.

### 일정 캘린더 (`ScheduleCalendar.vue`)

- **탭·툴바**: `JournalDayViewToolbar` 와 동일 상하감 — 탭(`nav-tabs-line ps-5 mt-5`) 상단, 이동일·검색·필터·등록은 탭 오른쪽(`pe-5 mt-3`). 카드 `margin-top: 0`. 달력 VIEW(FullCalendar) · 목록 VIEW(테이블)
- **aside (년월 내비게이션)**: `ScheduleAside.vue` — 저널 aside 와 동일 폭(280px 고정)·sticky(상단 1rem, 자체 스크롤). 연도 select + 월 prev/next + 월 3열 그리드 + TODAY 버튼만 배치(필터·검색·등록은 툴바 유지). 연/월 선택·TODAY 는 이동일(anchorDate)을 갱신 후 기존 이동일 경로 재사용 — 달력 VIEW `gotoDate`, 목록 VIEW 해당 **월** 재조회(`전체 월` 토글 시 연 전체). 표시/숨김 토글은 `scheduleAside` 스토어(localStorage `schedule_aside_visible`) — 닫기 버튼은 aside 내부, 숨김 시 툴바 끝에 열기 아이콘 버튼(`bi-layout-sidebar-inset-reverse`) 표시. 저널과 달리 Pinpoint·필터 없음
- **저장 모달 날짜 입력**: 시작일·종료일은 레거시(`ScheduleRegModal` — readonly 텍스트 input + `cF.datepicker.singleDatePicker`)와 동일하게 **readonly 텍스트 input + flatpickr**(`bindSingleDatePicker`, 저널 등록 모달과 동일 유틸). input 아무 곳이나 클릭하면 달력이 뜬다. 모달 열 때마다 재초기화(attach), 닫기 버튼에서 destroy. 종료일 칸은 레거시 `#endDtDiv`(display:none 토글)와 동일하게 `v-show` — 공휴일 선택 시 숨기고 `endDt=bgnDt` 로 덮어쓰며 flatpickr 표시값도 `setDate` 동기화. **툴바 이동일 input 도 동일 패턴** — 상시 존재하므로 mount 시 1회 attach·unmount 시 destroy, 달력 이동(datesSet) 등 외부 갱신은 watch 로 flatpickr 표시값 동기화. (변경 전: 네이티브 `<input type="date">` — 마이그레이션 시 이탈분을 레거시로 수렴)
- **휴가 일정 데이터 계약**: 일정 대분류는 `schedule_cd=VCATN`, 세부 휴가 종류는 `schedule.vcatn_cd`와 기존 공통코드 `VCATN_CD`(`ANNUAL`, `AM_HALF`, `PM_HALF`, `PBLEN`, `CTSNN`, `MNSTR`, `UNPAID`)를 사용한다. `HOLYDAY`는 전역 공휴일 전용이며 휴가를 공휴일로 저장하지 않는다. `VCATN` 등록·수정 시 활성 휴가 구분이 필수이고, 다른 일정 유형으로 바꾸면 남아 있던 `vcatn_cd`는 제거한다. 기존 `VCATN` 행은 제목으로 종류를 추정하지 않고 NULL로 보존하며, 수정 시 명시적으로 선택해야 한다.
- **일정 기간 계약**: DB/API의 `bgnDt`·`endDt`는 양 끝을 포함하는 inclusive 날짜다. 종료일 미입력은 시작일과 같은 날로 정규화하고, `HOLYDAY`만 단일 날짜를 강제한다. 종료일이 시작일보다 빠른 요청은 조용히 보정하지 않고 거부한다. FullCalendar all-day 이벤트는 이 값을 `start=bgnDt`, `end=endDt+1일`(exclusive)로 변환하므로 단일·다일 일정 모두 마지막 날까지 표시한다. 변경 전에는 기존 단일 일정을 다일 일정으로 늘릴 수 없었고 FullCalendar `end`에 시작일이 들어가 기간 표시가 잘렸다.
- **목록 API**: `GET /api/schedule/list` — 달력과 동일 `bgnDt`/`endDt`·고급필터·검색어, `ScheduleDto` 페이징
- **목록·달력 가시성 쿼리**: 공개 일정과 현재 사용자가 참가한 개인 일정을 `ScheduleSpec` 단일 쿼리로 조회한다. 개인 일정 체크를 끄거나 파라미터를 생략하면 공개 일정만 조회하며, 켜면 공개 일정 + 현재 사용자 참가 개인 일정을 조회한다. 변경 전에는 공휴일·일반·개인 일정을 세 번 조회해 메모리에서 병합·중복 제거·페이징했고, 개인 일정 전용 파라미터 `prevOnly`와 spec 키 `getPrvtOnly`가 어긋나 가시성 조건이 적용되지 않았다.
- **고급필터 계약**: 「휴가」를 끄면 `schedule_cd=VCATN`을 제외하고, 「내 일정만」을 켜면 현재 사용자가 참가자로 등록된 일정만 남긴다. 개인 일정은 등록·수정 전처리에서 작성자를 참가자에 자동 포함하므로 작성자 자신의 개인 일정도 같은 참가자 조건으로 조회된다.
- **상세·변경 권한**: 공개 일정은 인증 사용자가 조회할 수 있고, 개인 일정은 작성자 또는 참가자만 조회할 수 있다. 수정·삭제는 공개/개인 여부와 무관하게 작성자만 가능하며, 상세 모달의 수정·삭제 버튼도 서버 DTO의 `isCreatedBy`가 참일 때만 활성화한다. 권한 거절은 서버 경고 로그에 작업·일정 ID·현재 사용자·작성자를 남긴다.
- **저널 일자 사용자 휴가 투영·표시**: 저널 목록·상세 응답의 `JournalDayDto`는 전역 `isHolyday`와 별도로 `vacationDayStatus`(`NONE`, `FULL_DAY`, `AM_HALF`, `PM_HALF`, `UNKNOWN`)와 `vacationReasonList`를 가진다. 현재 사용자가 참가한 `VCATN` 일정만 조회 범위와 겹치는 기간을 한 번에 가져와 inclusive 일자별로 펼치며, 오전+오후 반차는 전일로 합친다. 기존 NULL·미등록 휴가 코드는 제목으로 추정하지 않고 `UNKNOWN`으로 남긴다. 사유 목록은 일정 제목을 중복 제거해 사용한다. 월간·주간·일간 카드와 일자 상세·메타 일자 목록은 `FULL_DAY`만 공휴일·주말과 같은 날짜 빨강으로 표시하고, `AM_HALF`·`PM_HALF`는 날짜 색을 바꾸지 않은 채 서로 다른 배지로 표시한다. `UNKNOWN`은 경고 배지로 드러내며 모든 휴가 배지 옆에 일정 제목 사유를 표시한다.
- **목록·상세 일정 구분명**: 엔티티의 transient `scheduleNm`(현재 제목으로 채워지는 값)을 표시명으로 재해석하지 않고 bootstrap `SCHEDULE_CD` 코드 목록을 SSOT로 사용한다. 목록 구분 열과 상세 제목 접두어가 실제 일정 제목을 중복 표시하지 않는다.
- **목록 행 클릭**: 휴가는 `GET /api/schedule/cal-dtl` 상세 모달을 열고 휴가 구분을 표시하며 수정·삭제할 수 있다. 생일 코드는 기존대로 상세 모달을 생략한다. 달력 이벤트 클릭도 같은 계약이다. 변경 전에는 휴가 클릭을 즉시 return하여 등록 후 상세 확인·수정·삭제 진입이 불가능했다.
- **공휴일 표시(`isHolyday`) 캐시 계약**: 저널 일자 목록의 최종 날짜 빨간색은 프론트 `isJournalDayOff()`가 `JournalDayDto.isHolyday` 또는 `vacationDayStatus=FULL_DAY`일 때 적용한다. 이 중 전역 공휴일·주말 축인 `isHolyday`는 `holydayMap` 캐시를 원천으로 `JournalDayHolydayHelper`가 채운다. `holydayMap`은 `@Cacheable`이 아니라 수동 put 캐시라 자동 로딩되지 않으므로, **캐시 미스 시 `JournalDayQueryService.getHolydayMap()`이 `ScheduleService.resyncHolydayMap()`으로 재생성한 뒤 다시 읽는다**. 재생성에 실패하면 공휴일 정보 없이 조회를 계속하되, 별도 사용자 휴가 상태는 유지한다(로그만 남김).
  - **변경 전**: 캐시를 읽기만 하고 미스면 `null` 을 반환했고, 헬퍼는 `null` 이면 조용히 return 해 `isHolyday` 를 설정하지 않았다. 채우는 곳이 기동 시 워밍업(`ScheduleCacheWarmupTask`)과 공휴일 API 동기화뿐인데 ehcache `defaultTemplate` TTL 이 1일이라, **기동 후 하루가 지나면 만료된 채 아무도 다시 채우지 않아 공휴일 빨간색이 사라졌다**(증상이 간헐적으로 보인 이유는 만료 시점에 좌우됐기 때문).
  - 공휴일 일정 등록·수정·삭제 시 `ScheduleService.evictCache()` 가 `holydayMap` 도 비운다(변경 전에는 `isHolyday`/`isHolydayOrWeekend` 만 비워, 공휴일을 고쳐도 목록 색상이 갱신되지 않았다). 비운 뒤 재생성은 위 미스 처리에서 수행한다.
- **공휴일 DB SSOT**: `schedule.schedule_cd` = `HOLYDAY` (`SCHEDULE_CD` 코드·`ScheduleSpec` 조회와 동일). 레거시 `SCHDUL_CD`/`HLDY`/`content_type` `schdul` 은 `data-required-cd-mariadb.sql` 하단 정합 섹션으로 수렴 — `HOLYDAY` 행과 날짜·제목이 완전 중복인 `HLDY` 행은 삭제, 나머지는 rename. 상세 코드 영문(en) 명칭은 같은 파일 하단 `code_item_i18n` INSERT 섹션(재실행 무해)으로 시드한다. **이동일**은 달력/목록 이동용(데이터 필터 아님). 달력 API `bgnDt`/`endDt` = FullCalendar `datesSet` visible 구간. 목록 VIEW는 동일 구간(달력에서 넘어온 경우) 또는 이동일 변경 시 해당 **월** 범위.
- **목록 VIEW 이동일 변경 = 월 범위**: aside 연/월 선택·TODAY 로 이동일이 바뀌면 목록은 그 **달**을 조회한다(`queryRangeForMonth`). 변경 전에는 `queryRangeForYear`(연 전체)로 조회해, aside 월 그리드가 파랗게 표시하는 달과 실제 조회 범위가 어긋났다(월을 골라도 12개월치가 나옴). 연 전체 조회는 아래 `전체 월` 토글로 분리했다.
- **목록 VIEW `전체 월` 토글**: **aside 월 그리드 바로 아래(TODAY 위)** 에 두는 스위치로, 월 선택과 같은 자리에서 `월로 좁혀보기 / 전체 월 보기`를 고르게 한다. 목록 VIEW 에서만 노출한다(`showAllMonths` prop; 달력 VIEW 는 항상 한 달을 그리므로 숨김). 해제(기본)면 월 범위로, 체크면 이동일이 속한 **연도 전체(1/1~12/31, `queryRangeForYear`)** 로 조회한다.
  - 체크 시 aside 월 그리드의 활성 월 강조를 **끈다** — 특정 월로 좁혀 보는 상태가 아니므로 강조가 남으면 표시와 조회 범위가 어긋난다.
  - 반대로 특정 월을 클릭하면 토글을 **자동 해제**한다. 해제하지 않으면 월을 눌러도 연 전체가 유지돼 클릭이 무반응처럼 보인다. 이 토글은 필터 패널 체크박스와 달리 **localStorage 에 저장하지 않아** 화면 진입 시 항상 해제 상태(월 단위)로 시작한다. 토글·페이지 크기 변경 시 페이지를 0으로 되돌리고 재조회하며, 달력 VIEW 조회 범위에는 영향을 주지 않는다.
- **i18n**: 목록·상세 조회와 등록·수정·삭제 결과 메시지는 서버 `message`를 우선 사용하고, 서버 메시지가 없을 때 현재 locale의 클라이언트 카탈로그 메시지를 표시한다.

### 저널 일간 화면 (journal-daily) 스펙

- **용도**: 단일 날짜를 새 창으로 열어 집중 편집
- **레이아웃**: `SystemLayout` > `JournalDayDailyLayout` — 헤더/사이드바 없음, 태그클라우드 없음
- **네비게이션**: 상단 이전/다음 버튼으로 날짜 이동 (`router.replace` + `stdrdDt` query)
- **i18n**: 이전/다음 버튼, 날짜 선택 tooltip, 빈 상태, 목록 조회 실패 fallback은 현재 locale 카탈로그를 사용한다. locale 변경은 선택 날짜·`stdrdDt` query·조회 조건을 변경하지 않는다.
- **모달**: `JournalDayDailyLayout`에 전체 편집 모달 포함 (일자 수정, 챕터, 엔트리, 태그, 메타 등)
- **챕터 모달 i18n**: `JournalChapterRegistModal.vue`의 표시 문구·기준 날짜 요일·확인창·결과 fallback은 현재 locale 카탈로그를 사용하며, 등록·수정·일자 이동 API의 서버 `message`가 있으면 우선 표시
- **진입점**: `JournalDayCard` 컨텍스트 메뉴 "새 창으로 열기 (일자 뷰)", 태그 상세 모달 일자 행 버튼
- **URL**: `/journal/daily?stdrdDt=YYYY-MM-DD`
- **새 창 오픈**: `window.open(url, "_blank", "width=...,height=...")` — features 지정으로 탭 아닌 창 강제


### 저널 메타 VIEW (Journal Meta)

- **Route**: `/journal/meta` (`journal-meta`)
- **Legacy**: `/app/journal/day/meta.do`
- **툴바**: `JournalDayViewToolbar` (주간/월간/달력/메타 탭)

#### Layout

- 카드 헤더: `#메타` 목록 (`GET /api/journal/day/metas` → `store.metaList`)
- 카드 바디: 그래프 영역 `#journal_day_meta_graph_div`

#### 메타 헤더 목록

- 태그 클라우드와 동일하게 `btn btn-link` + 기록 수(`contentSize`)
- 클릭 → `JournalMetaContextMenu` (즉시 그래프에 추가하지 않음)
- 「그래프로 보기」로 포함된 메타: 굵게 + 옆 `bi-x-circle-fill` → `removeMetaFromGraph`

#### 컨텍스트 메뉴 (`JournalMetaContextMenu`)

| 액션 | 동작 |
|------|------|
| 검색 | `openDayFilterModal({ type: "meta", ... })` — `JournalDayMetaModal` |
| 그래프로 보기 | `addMetaToGraph` (최대 2, 중복·꽉 참 시 안내) |
| 메타 설정 | `openMetaProfile` — `JournalMetaProfileModal` |

메타 컨텍스트 메뉴의 액션·표시 상태·제한 경고는 현재 locale의 클라이언트 카탈로그를 사용한다.

#### 그래프 영역

- 선택 0: 안내 문구
- 선택 1+: 연도(「전체」+ 연도 목록 합집합), 임계값, 메타별 통계(합계/평균/최고/최저)
- 차트: 선택 메타 전부를 **한** line chart, X축=일자 합집합, 시리즈=메타별 1선, 범례(2메타 시)
- 단일 메타일 때만 최고/최저 보조선; 비교(2메타) 시 임계선만 공통 적용
- 메타 헤더·메뉴/제거 tooltip·미선택/비교 안내·연도/임계값·통계·빈 상태와 차트 임계/최고/최저 라벨은 현재 locale의 클라이언트 카탈로그를 사용한다. locale 변경은 선택 메타·연도·임계값·그래프 데이터와 통계 계산을 변경하지 않는다.

#### Modals (layout 마운트)

- `JournalDayMetaModal`, `JournalMetaProfileModal`, `JournalMetaContextMenu`
- `JournalDayMetaModal`의 제목·결과 건수·연도/전체 연도·연월 구분선·기준 날짜 요일·필터 추가/제거·일자 새 창 tooltip·빈 상태·닫기와 조회 실패 fallback은 현재 locale의 클라이언트 카탈로그를 사용한다. locale 변경은 선택 메타/태그·AND 필터·연도·일자 목록을 변경하지 않는다. 태그 입력 검색의 placeholder·카테고리 선택·미존재 태그 알림 문구는 엔트리 검색 키(`journal.entry.search.tag.*`, `journal.entry.search.category.*`)를 재사용한다. 태그 입력 검색(자동완성·카테고리 선택·AND 필터 추가)은 interaction-spec 「일자 필터 모달」 항목을 따른다.

### 저널 API (`useJournalStore`)

- `GET /api/journal/days` — `fetchDays` (`viewType`, `yy`, `mnth`, 필터, `weekStartDt`, `stdrdDt`)
- `GET /api/journal/day/metas` — `fetchMetas`
- `GET /api/journal/day/tags`, `GET /api/journal/entry/tags?type=DIARY`, `GET /api/journal/entry/tags?type=DREAM` — `fetchTagCloud`

`sort` / `sortOrder` 는 스토어·UI 모두 구현되어 있으며, 기본값은 `DESC`이고 `localStorage("journal_day_sort")` 로 유지한다.

### 사이드바 메뉴

`SidebarMenu.vue` → `useMenuStore.fetchUserMenu()` + `toVuePath(menu.url)`. 기본 메뉴 accordion은 항상 펼쳐두며, 2depth 내부 key성 `menuLabel`(`JOURNAL_DAY` 등)은 화면에 표시하지 않는다.

---

## 저널 일자 월간 목록 (Journal Day Monthly)

- **Route (레거시)**: `/journal/day/monthly` · **Vue SPA**: `/journal/monthly` (`journal-monthly`)
- `/` 및 `/journal` 기본 진입은 주간 뷰(`/journal/weekly`)로 redirect한다.
- **Legacy file**: `legacy/templates/view/feature/journal/day/journal_day_monthly.ftlh`

### Layout Structure

- 레이아웃: `layout_with_aside.ftlh` 사용 (사이드바 있음)
- 툴바 영역: `_journal_day_page_header.ftlh` include (공통 저널 일자 헤더)
- 메인 영역: `.journal-day-monthly-page` div
  - 상단: `JournalDayViewToolbar.vue` (탭 네비게이션 + 저널 일자 등록 버튼, flex `justify-content-between`) — 레거시 본문 상단 탭 행 + `_journal_day_page_header.ftlh` 의 `header_btn_reg_modal` 동작
  - 카드: `.card.post` (margin-top: 0 !important)
    - 카드 헤더: 태그 헤더 (`_journal_day_tag_header.ftlh`) → Vue: `JournalTagCloudHeader.vue` ✓ 구현 완료
    - 기간별 스레드 요약: 태그클라우드 아래 월간 스레드 집계(상위 10개 + 펼치기, 라벨 `스레드`) ✓ 구현 완료
    - 카드 바디: `JournalDayCard` (`store.dayList`) — 레거시 `#journal_day_list_div` 텔레포트는 SPA 미사용
    - 로딩 렌더: `store.dayList`가 비어 있는 초기 조회에서만 본문 전체 스피너를 표시한다. 이미 목록 DOM이 렌더된 상태의 재조회(등록/수정 후 갱신, 필터 갱신 등)에서는 기존 `JournalDayCard` DOM을 유지해 문서 높이 축소로 인한 스크롤 초기화를 막는다.
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
| 상단 뷰 툴바 | Vue | `JournalDayViewToolbar` | — | 주간/월간/달력/메타 탭 + 우측 검색·등록·aside 열기 액션; 그림자 없이 고정 앱 헤더 아래 sticky floating하며 열린 aside와 상단선 일치; `JournalMonthly` / `JournalWeekly` / `JournalCalendar` / `JournalMeta` 공유 |
| 저널 일자 등록 | `<button>` | `.btn-light-primary.btn-outlined` | `useJournalModalStore.openDayRegist()` | 레거시 `header_btn_reg_modal` → `data-journal-day-action=reg-modal`; 라벨 「저널 일자 등록」, `bi-calendar-plus`; `d-none d-md-flex`. 신규 등록 성공 시 서버가 기본 SUMMARY 챕터와 빈 DIARY 엔트리 구조를 보장한다. |
| 키워드 검색 | Vue | `JournalDayViewToolbar.vue` | `openSearchTab` → `/journal/entry/search` | 레거시 팝업 대체 ✓ — 툴바 일기·꿈 키워드 입력 후 새 탭 검색 |
| 태그 헤더 | include | `_journal_day_tag_header.ftlh` | 태그 목록 | 카드 헤더 내부. 일자/일기/꿈 태그 행의 목록 시작 x좌표는 동일해야 하며, `꿈 태그` 라벨 길이 차이로 들여쓰기 차이가 생기면 안 된다. |
| 목록 컨테이너 | `<div>` | `#journal_day_list_div` | Vue 렌더 | `JournalDayMonthlyListApp` 마운트 대상 |

`JournalDayViewToolbar.vue`의 주간·월간·달력·메타 탭, 일기·꿈 전체검색 placeholder/tooltip, 저널 일자 등록 문구와 월간·주간·일간 목록의 빈 상태는 현재 locale의 클라이언트 카탈로그를 사용한다. locale 변경은 라벨만 바꾸며 탭 route, 로컬 검색어, 새 탭 검색 URL과 등록 모달 호출, 목록 조회 조건은 유지한다.

저널 일자 목록과 엔트리 검색 화면에서 완료된 꿈 엔트리는 일기 완료의 초록과 구분되는 은은한 보라 배경·테두리·좌측선으로 표시한다. 접힌 완료 표시·순번·라이프사이클 메뉴의 완료 라벨도 보라색이며, 중요·참조의 빨강·노랑 상태선은 완료 보라선과 함께 유지한다. 일기·노트·해석 및 챕터 완료 집계는 기존 초록색을 유지한다.

### Action Buttons & Interactions

| Action | Trigger | Legacy handler | Expected behavior |
|--------|---------|---------------|-------------------|
| 주간 뷰로 전환 | 탭 클릭 | `dF.JournalDayViewService.changeView(Url.JOURNAL_DAY_WEEKLY)` | 주간 보기 페이지로 이동 |
| 월간 뷰로 전환 | 탭 클릭 | `dF.JournalDayViewService.changeView(Url.JOURNAL_DAY_MONTHLY)` | 현재 페이지 유지(active) |
| 달력 뷰로 전환 | 탭 클릭 | `dF.JournalDayViewService.changeView(Url.JOURNAL_DAY_CAL)` | 달력 보기 페이지로 이동 |
| 메타 뷰로 전환 | 탭 클릭 | `dF.JournalDayViewService.changeView(Url.JOURNAL_DAY_META_VIEW)` | 메타 보기 페이지로 이동 |
| 저널 일자 등록 | 상단 「저널 일자 등록」 버튼 | `JournalDayRuntimeService` (`data-journal-day-action=reg-modal`) | `JournalDayRegistModal` 신규 등록 오픈 (`openDayRegist()`) |
| 사이드 필터 열기 | aside 숨김 시 상단 툴바 맨 오른쪽 버튼 | — | `asideStore.show()`로 사이드 필터를 표시. 데스크톱에서는 sticky 툴바와 함께 고정 헤더 아래를 따라가며, 모바일에서는 본문 우상단 전용 버튼을 유지 |

**레이아웃 전역 툴바** (`JournalDayViewToolbar.vue`): 고급필터(사이드 패널 토글)·태그 카테고리 동기화·저널 일자 등록·aside 열기 — SPA ✓. 일정 등록·개인 일정은 저널 맥락을 전달하지 않는 중복 진입점이므로 일정 화면에서만 제공한다 (`docs/JOURNAL_SCREEN_BEHAVIOR_SPEC.md` §4.1–4.3).

**인증 만료 후 복귀**: 월간 VIEW 의 현재 기간은 URL query `yy`/`mnth`가 SSOT다. 월 이동, 연도 변경, 월 버튼, TODAY, Pinpoint 되돌리기는 `/journal/monthly?yy=YYYY&mnth=M` 형태로 주소를 갱신하며, 세션 만료로 로그인 화면에 이동할 때 해당 `fullPath`를 `redirect`로 넘긴다. 로그인 성공 후 동일 query로 복귀하면 `JournalDayMonthly`가 query를 store에 복원한 뒤 목록과 태그 클라우드를 조회한다.

### Data Displayed

`JournalDayMonthlyListApp` Vue 앱이 `#journal_day_list_div`에 렌더한다.
- 월 단위 저널 일자 목록 (월별 카드 형태)
- 각 일자 카드에 일기(DIARY)/꿈(DREAM)/노트(NOTE) 엔트리 포함
- 일자 카드의 기준 날짜 요일·날짜 정확도 배지·챕터 필터 안내, 챕터·꿈 등록, 컨텍스트 메뉴, 상태, 메타 tooltip, 숨겨진 카테고리·꿈 숨김 문구와 삭제·꿈 섹션 복사 결과 fallback은 현재 locale의 클라이언트 카탈로그를 사용. 엔트리의 보류 badge와 접힌 상태 문구도 현재 locale을 사용한다. 삭제 API 응답에 `message`가 있으면 서버 메시지를 우선 표시
- 챕터 헤더의 유형·소유권 배지·등록 버튼·액션 툴팁·메뉴·빈 상태 문구는 현재 locale의 클라이언트 카탈로그를 사용
- 챕터 접힘 시 태그 요약과 함께 하위 엔트리의 소속 스레드를 중복 없는 버튼으로 접힘 바깥에 표시한다. 스레드 버튼은 제목을 표시하며 클릭하면 해당 저널 스레드 상세로 이동한다.
- 챕터 소유권 경고·삭제 확인·삭제 결과 fallback·클립보드 복사 결과는 현재 locale 카탈로그를 사용하며, 삭제 API의 서버 `message`가 있으면 우선 표시
- 꿈 가상 섹션 제목은 서버가 요청 locale로 조립하고, 내 꿈 섹션의 등록·복사·TXT 액션 문구는 현재 locale의 클라이언트 카탈로그를 사용
- 해석 아이템의 펼침/접힘·접힌 상태·액션 툴팁·메뉴·라이프사이클·상태 라벨과 상태 변경 실패·복사·삭제 결과 fallback은 현재 locale의 클라이언트 카탈로그를 사용. API 응답에 `message`가 있으면 서버 메시지를 우선 표시
- 엔트리 아이템의 펼침/접힘·꿈 상태 배지·액션 툴팁·메뉴·라이프사이클·상태 라벨과 상태 변경 실패·복사·삭제 결과 fallback은 현재 locale의 클라이언트 카탈로그를 사용. 일반 관련글 행은 관계 유형·대상 유형·대상 제목·사유를 표시하고 제목 클릭으로 원문을 연다. (FLOW 요약 행·「흐름 보기」 종단 모달·FLOW 연결 모달은 스레드 소속으로 수렴하며 모두 제거됐다 — 나-2·다-2.) 「스레드에 추가」 hover 서브메뉴는 280px 폭 안에 새 스레드 생성, 제목 검색, 분류 선택, 현재 엔트리 기준 후보 7개를 순서대로 표시한다. 후보는 제목과 분류명을 표시하고 현재 소속이면 체크한다. 로딩·조회 실패·정상 0건·필터 결과 0건은 서로 다른 상태로 표시한다. API 응답에 `message`가 있으면 서버 메시지를 우선 표시
- 꿈 엔트리(`JOURNAL_DREAM`)의 태그에 사용자별 프로필 본문(`tag.list[].profileContent`)이 있으면 해당 꿈 본문 아래에 표시한다. 일기/노트 태그 프로필 본문은 일자 카드 본문 아래에 표시하지 않는다.
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
| 관련 글 추가 | `RelatedContentAddModal.vue` | 엔트리 ⋯ 메뉴의 「관련 글 추가」 클릭. 선택한 일기/꿈 유형에서 제목·본문 키워드로 최신 8건을 검색하며 실패와 정상 0건을 구분. FLOW 연결 모드는 제거됐다(나-2b) |

`JournalDayRegistModal.vue`의 모달 제목·안전 닫기 안내·날짜·날짜 정확도 선택지·날씨·일기/꿈 완료 여부·태그·메타 안내·저장·닫기 문구는 현재 locale의 클라이언트 카탈로그를 사용한다. locale 변경은 입력값과 저장 payload를 변경하지 않는다.

**일자 축별 완결**: 목록 카드는 우측 액션 칸(등록 버튼 슬롯)에, 상세 모달은 날짜 헤더에 일기/꿈 완결 배지를 표시한다. `diaryResolvedYn`/`dreamResolvedYn` 이 Y 인 축은 등록·수정·삭제·lifecycle·state·댓글·관련 쓰기 UI를 숨기고 서버가 거절한다. 일자 ⋯ Status와 등록 모달에서 토글한다. 완결된 일자는 삭제할 수 없다.

날짜 필수 검증·등록/수정 확인·성공/실패 fallback도 현재 locale의 클라이언트 카탈로그를 사용하며, 저장 API가 `message`를 반환하면 서버 메시지를 우선 표시한다.

`JournalDayDetailModal.vue`의 제목·기준 날짜 요일·날짜 정밀도 배지·빈 상태·조회 실패·닫기 문구는 현재 locale의 클라이언트 카탈로그를 사용한다.

`JournalInterpretationRegistModal.vue`의 제목·기준 날짜 요일·필드·placeholder·안내·저장·닫기·확인·결과 fallback은 현재 locale의 클라이언트 카탈로그를 사용한다. 저장 API가 `message`를 반환하면 서버 메시지를 우선 표시하며, 해석 제목은 locale과 무관하게 선택값으로 유지한다.

`JournalEntryRegistModal.vue`의 일기·꿈·노트·기본 엔트리별 제목, 기준 날짜 요일, 필드 레이블, 글자 수 안내, 카테고리·제목·순서·꿈꾼 placeholder, 저장·닫기·확인·결과 fallback은 현재 locale의 클라이언트 카탈로그를 사용한다. 저장 API 응답에 `message`가 있으면 서버 메시지를 우선 표시하며, locale 변경은 챕터 선택·태그·꿈꾼 값과 저장 payload를 변경하지 않는다.

### Special behaviors

- `Model.locale`을 JS 변수로 노출 (`${.locale?replace('_', '-')?js_string}`)
- Vue 앱 마운트 전 pending 큐 패턴 사용: `JournalDayMonthlyApp`, `JournalDayTagDetailVueApp`, `JournalDayTagPanelVueApp`, `JournalDayTagProfileVueApp` 모두 마운트 전 호출을 큐잉
- TinyMCE 에디터 로드 (일기/꿈/노트 본문 편집용)
- FullCalendar 로드 (달력 뷰 전환 대비)
- Prism.js 코드 하이라이팅 로드
- 태그 헤더: `JournalTagCloudHeader.vue` 컴포넌트 (`v-if="store.showTagCloud"`). `store.tagCloud` 상태를 읽고 parent view 초기화 및 기간/뷰 변경 watch에서 `store.fetchTagCloud()` 호출. 로딩 상태·행 레이블·태그 메뉴 툴팁은 현재 locale의 클라이언트 카탈로그 사용

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
    - 기간별 스레드 요약: 태그클라우드 아래 주간 스레드 집계(최초 등장일순 전체, 라벨 `스레드`) ✓ 구현 완료
    - 카드 바디: `JournalDayCard` (`JournalDayWeekly.vue`, `viewType: WEEKLY`)
    - 로딩 렌더: `store.dayList`가 비어 있는 초기 조회에서만 본문 전체 스피너를 표시한다. 이미 목록 DOM이 렌더된 상태의 재조회(등록/수정 후 갱신, 필터 갱신 등)에서는 기존 `JournalDayCard` DOM을 유지해 문서 높이 축소로 인한 스크롤 초기화를 막는다.
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

**인증 만료 후 복귀**: 주간 VIEW 의 현재 기간은 URL query `weekStartDt`가 SSOT다. 주 이동, 날짜 선택기, TODAY는 `/journal/weekly?weekStartDt=YYYY-MM-DD` 형태로 주소를 갱신하며, 세션 만료로 로그인 화면에 이동할 때 해당 `fullPath`를 `redirect`로 넘긴다. `stdrdDt` query 로 진입한 기존 링크는 해당 날짜가 포함된 주의 `weekStartDt`로 해석해 조회한다.

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
| 결산 등록 | 뷰 툴바(`JournalAnnualViewToolbar`) 우측 액션 버튼 | `dF.JournalAnnual` 서비스 | 결산 등록 모달 오픈 |
| 전체 결산 갱신 | 뷰 툴바 우측 `전체 결산 갱신` 버튼 | `dF.JournalAnnual.makeTotalAnnualAjax()` | `POST /api/journal/annual/make-total` 호출 후 성공 알림, 결산 목록과 전체 꿈 통계 재조회 |
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
- 전체 결산 갱신 후 목록 갱신: 레거시 `blockUIReload()`와 달리 Vue SPA에서는 `JournalAnnualStore.makeTotalAnnual()` 성공 알림 후 `fetchList()`와 `fetchTotal()`을 재호출해 현재 화면의 목록/총 집계를 갱신한다.
- Vue SPA에서 결산 등록·전체 결산 갱신·aside 열기는 저널 일자 툴바의 **액션 행**과 동형인 `JournalAnnualViewToolbar`에 배치한다(`pe-5 mt-3`, 데스크톱 aside 열기는 툴바 우측). 결산에는 뷰 탭이 없으므로 탭용 `mt-5` 빈 여백은 두지 않고, 총 집계 카드는 `margin-top: 0`으로 툴바에 붙인다. aside는 연도·키워드 필터 전용 영역으로 유지한다.
- 결산 카드 컨텍스트 메뉴(`data-kt-menu`)는 비동기 목록 렌더, 수정 저장 후 목록 재조회 완료, 필터 DOM 갱신 후 `reinitMetronicAfterDom()`으로 Metronic 메뉴를 재바인딩한다.
- `preloadJournalDayTagService.js` 별도 적재 (태그 클릭 시 `dF.JournalDayTagService.select` 호출 대비)
- 꿈 아이콘: `bi bi-moon-stars fs-4`
- Vue SPA 목록 필터 우선순위:
  - 연도 필터가 선택되어 있으면 키워드 필터 값과 무관하게 해당 연도 결산만 표시한다.
  - 연도 필터가 비어 있을 때만 키워드 필터가 전체 결산 목록을 축소한다.
  - 따라서 연도 필터와 키워드 필터는 AND 조건이 아니다.
- 키워드 필터 값이 있으면 목록 본문 렌더링 시 일치 텍스트를 `<mark class="journal-annual-list-vue__keyword-mark">`로 하이라이트한다. HTML 문자열 자체를 정규식 치환하지 않고 DOM 텍스트 노드만 감싸서 마크다운 HTML 구조를 보존한다.
- Vue SPA의 결산 목록·필터·등록 모달 표시 문구, 툴팁, 확인창은 `useLocaleStore.t()`를 사용하며 `journal.annual.*`와 공통 i18n 키를 한국어/영어 카탈로그에서 동일하게 제공한다.
- 결산 목록 조회 실패, 전체 결산 갱신·결산/리뷰 등록·수정 결과 fallback과 리뷰 삭제 확인·결과 fallback도 현재 locale의 카탈로그를 사용한다. API 응답에 `message`가 있으면 서버 메시지를 우선 표시하고, 성공 알림 후 기존 목록·총계·상세 재조회 순서를 유지한다.

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
- **엔트리 중요/참조 색상**: 상세 엔트리 행은 저널 일자 엔트리와 동일하게 `data-imprtc`/`data-refrnc` 속성을 렌더해 `journal.scss` 의 일기 `$journal-paired-states`와 꿈 `$journal-dream-paired-states`로 **중요=빨강, 참조=노랑** 좌측선을 표시한다. 꿈 엔트리는 `data-else-dream` 도 함께 내려 타인 꿈 팔레트(`$journal-else-dream-paired-states`)와 구분한다. 변경 전에는 `journal-diary-item`/`journal-dream-item` 클래스만 있고 상태 속성이 없어 CSS 규칙이 걸리지 않았다(스타일은 이미 존재했고 속성만 빠져 있었다). 결산 상세의 `AnnualEntryDto`는 lifecycle을 제공하지 않으므로 이 화면에서는 RESOLVED 보라 팔레트를 판정하지 않는다.
- **중요·참조 모두 해제 = 빈 결과**: 두 토글이 모두 해제되면 상세 엔트리 목록은 빈 결과를 반환한다(`JournalAnnualRestController.isNoStateSelected` 가 조회 없이 빈 목록 응답). 변경 전에는 **전체가 조회됐다** — 공통 `BaseAttachableSpec.resolveStatesPredicate` 가 states 가 비면 상태 조건을 걸지 않고 return 하기 때문이다. 공통 스펙의 의미를 바꾸면 저널 일자·검색 등 다른 화면에 회귀 위험이 있어 결산 상세 경로에서만 처리한다.
- 결산 상세의 SUMMARY·REVIEWS·로딩·태그 메뉴 tooltip·IMPORTANT·REFERENCE와 상세/목록 aside의 FILTER·TAGCLOUD·ENTRY FILTER·SUMMARY FILTER·키워드 레이블은 현재 locale의 클라이언트 카탈로그를 사용한다. 영어는 기존 대문자 표기를 유지하며 locale 변경은 선택 연도·활성 탭·필터값·토글 상태를 변경하지 않는다.
- Vue SPA의 상세 aside는 연도 이동·태그클라우드·엔트리 필터 전용 영역이며 결산 등록 액션을 두지 않는다. 신규 결산 등록은 목록 화면 총 집계 카드 우측 액션 영역에서 수행한다.
- 결산 상세의 SUMMARY 본문과 DIARY/DREAM 엔트리 본문은 저널 일자 엔트리와 같은 `journal-content p-2` 타이포그래피 계약을 따른다. 상세 엔트리 제목도 일자 엔트리와 맞춰 `fs-5 fw-bold` 크기로 표시한다(변경 전 `fs-7`). 본문(`.journal-content`)은 fs 클래스 없이 base 1rem 을 상속하므로, 제목은 본문보다 한 단계 위 크기가 된다. 상세 엔트리 목록은 행 왼쪽 날짜 칼럼을 반복하지 않고, 저널 일자 카드처럼 `journal-day-header` 날짜 헤더 아래에 해당 날짜의 엔트리 본문을 배치한다. DIARY/DREAM 엔트리 태그는 일자 `JournalEntryItem` 과 동일하게 밑줄 primary `#이름`(ctgr·클릭 메뉴 포함, `bi-tag` 없음)으로 표시한다(변경 전: `bi-tag` + 단순 `#name`).
- Vue SPA `JournalAnnualLayout`은 태그 컨텍스트 메뉴와 짝으로 `JournalTagProfileModal`·`JournalDayMetaModal`을 마운트한다. 일자(`JOURNAL_DAY`) 태그 검색은 `openDayFilterModal` → `JournalDayMetaModal`이며, 짝 모달 미마운트 시 무반응처럼 보였다.
- 부트 순서: `journalAnnualCrudService` → `journalAnnualStateService` → `journalAnnualService` → `JournalAnnualDetailCardApp` → `JournalAnnualEntryTagListApp` → `JournalAnnualEntryListApp` → `JournalAnnualDetailPageBoot`
- `preloadJournalDayTagService.js` 적재 (태그 클릭 대비)
- Vue SPA의 결산 상세 날짜 요일·리뷰 등록 모달 표시 문구, 툴팁, 확인창은 `useLocaleStore.t()`를 사용하며 `journal.annual.*`와 공통 i18n 키를 한국어/영어 카탈로그에서 동일하게 제공한다.
- 리뷰 등록·수정 결과와 삭제 확인·결과 fallback은 현재 locale의 카탈로그를 사용하며, API 응답의 `message`를 우선 표시한다.

---

## 저널 스레드 목록 (Journal Thread List)

- **Route (레거시)**: `/journal/thread/list` · **Vue SPA**: `/thread` (`thread-list`)
- **추가 진입 경로**: `/app/journal/thread/regist-form.do` → `/thread/new` (`thread-create`), `/app/journal/thread/detail.do?id={id}` → `/thread/:id` (`thread-detail`), `/app/journal/thread/modify-form.do?id={id}` → `/thread/:id/edit` (`thread-edit`)
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

**현재 Vue SPA 구조**: `JournalThreadLayout`이 `JournalThreadViewToolbar`(등록, `pe-5 mt-3 mb-1`)를 목록 위에 두고, 그 아래 컴팩트한 검색 카드가 있다. ASIDE는 없다. 검색 카드는 분류·제목 검색과 검색·초기화 액션만 두며 `margin-top: 0`으로 툴바에 붙인다. **태그 클라우드 행은 2c-A 에서 제거됐다**(스레드 자체 태그 미소유, 2b). 별도 카드 제목 행은 두지 않으며 기존 목록 카드의 테이블 DOM·클래스와 페이지네이션 구조는 유지한다.

### Key UI Elements

| Element | Type | Legacy class/id | Data source | Notes |
|---------|------|----------------|-------------|-------|
| 태그 필터바 | include | `_tag_list_header.ftlh` | 태그 목록 | 태그 클릭 필터 |
| 뷰 툴바 | `JournalThreadViewToolbar` | Vue 신규 툴바 | Vue Router `thread-create` | 등록 버튼만. 결산·일자 액션 행과 동형(`pe-5 mt-3 mb-1`). ASIDE 없음. 탭용 `mt-5` 빈 여백 없음 |
| Vue 검색 카드 | `.card.mb-4` + `margin-top: 0` | Vue 신규 검색 카드 | `/api/journal/threads/categories` | 분류·제목 검색(태그 클라우드는 2c-A 제거). 등록은 뷰 툴바로 이동. 툴바에 붙는 상단 여백 |
| 테이블 | `<table>` | `.table.align-middle.table-row-dashed.fs-small.gy-5.table-fixed.hoverTable.mb-3` | 서버 모델 | 고정 레이아웃, 행 hover |
| 번호 열 | `<th>` | `.text-center.wb-keepall.w-10.hidden-table` | `post.rnum` | 모바일 숨김 |
| 제목 열 | `<th>` | `.col-lg-9.col-9.text-center.wb-keepall` | `post.title` | `txt.title` i18n |
| 첨부파일 열 | `<th>` | `.col-lg-1.text-center.wb-keepall.hidden-table` | `post.hasFiles` | 모바일 숨김 |
| 관리 열 | `<th>` | `.col-lg-1.text-center.wb-keepall.hidden-table` | Vue Router, thread store | ⋯ 컨텍스트 메뉴에서 수정·삭제 |
| 목록 tbody | `<tbody>` | `#journal_thread_list_div` | `JournalThreadListApp` Vue 텔레포트 | 행 직접 주입 |
| 페이지네이션 | include | `_pagination.ftlh` | `paginationInfo` | 기존 서버사이드 페이지네이션 유지 |

`JournalThreadList.vue`의 분류·제목 placeholder·태그 빈 상태와 조회 실패, 등록 버튼·테이블 헤더·빈 상태·댓글 목록·컨텍스트 메뉴·수정·삭제 문구는 현재 locale의 클라이언트 카탈로그를 사용한다.

`JournalThreadRegistModal.vue`의 등록·수정 제목, 필드, placeholder, 저장·닫기 버튼 및 확인창은 현재 locale의 클라이언트 카탈로그를 사용한다. 목록·수정·상세 조회 실패와 등록·수정 결과 fallback도 현재 locale을 사용하며, API가 `message`를 반환하면 서버 메시지를 우선 표시한다.

스레드 삭제 확인·성공·실패 fallback은 현재 locale의 클라이언트 카탈로그를 사용한다. 삭제 API의 서버 `message`가 있으면 우선 표시하고, 성공 알림 확인 후 첫 페이지 목록을 다시 조회한다.

### Action Buttons & Interactions

| Action | Trigger | Legacy handler | Expected behavior |
|--------|---------|---------------|-------------------|
| 상세 보기 | 제목 행 클릭 | `router.push({ name: "thread-detail", params: { id } })` | 상세 모달 오픈 + URL `/thread/:id` 동기화 |
| 상세 모달 닫기 | 헤더 × 또는 푸터 「닫기」 클릭 | `store.closeDetail()` | 명시적 조작으로만 닫힘. backdrop 클릭과 Escape는 무시하며 URL 이동·조회 실패에 따른 프로그램상 종료는 유지 |
| 등록 모달 열기 | 뷰 툴바(`JournalThreadViewToolbar`) 등록 버튼 클릭 | `router.push({ name: "thread-create" })` | 등록 모달 오픈 + URL `/thread/new` 동기화 |
| 태그 필터 | 검색 카드의 태그 클릭 | `filterTagId` 설정 후 `fetchList(0)` | 단일 태그 선택·재클릭 해제 후 첫 페이지 조회 |
| 분류·제목 검색 | 검색 버튼 또는 Enter | `filterCategory`, `filterKeyword` 설정 후 `fetchList(0)` | `categoryCode`, `searchType=title`, `searchKeyword`로 첫 페이지 조회 |
| 검색 초기화 | 초기화 버튼 클릭 | `resetFilters()` | 태그·분류·제목 조건을 모두 비우고 첫 페이지 조회 |
| 수정 모달 열기 | 수정 버튼 클릭 | `router.push({ name: "thread-edit", params: { id } })` | 수정 모달 오픈 + URL `/thread/:id/edit` 동기화 |
| 댓글 모달 | 댓글 수 클릭 | `CommentList.modal(id, contentType)` | 댓글 목록 모달 |
| 파일 모달 | 첨부파일 아이콘 클릭 | `FileGroupList.modal(fileGroupId)` | 파일 목록 모달 |
| 태그 상세 | 태그 클릭 | `dF.Tag.dtlModal(tagId)` | 태그 상세 모달 |
| 소속 엔트리 액션 | 상세의 `JournalEntryItem` 액션 버튼·⋯ 메뉴 | 저널 일자와 같은 원본 엔트리 액션 | 수정·댓글·해석·이력·관련글·스레드 소속·라이프사이클·상태·삭제를 실행하고 성공 후 열린 스레드 상세·집계 태그·소속 엔트리를 재조회 |

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
| 댓글 목록 | `_comment_list_modal.ftlh` | 목록 댓글 수 클릭·상세 모달 댓글 수 버튼 |
| 댓글 등록 | `_comment_reg_modal.ftlh` | 상세 모달 댓글 등록 버튼 |
| 파일 목록 | `_file_list_modal.ftlh` | 첨부파일 아이콘 클릭 |
| 스레드 상세 | `_journal_thread_detail_modal.ftlh` | 제목 행 클릭 |
| 엔트리 등록·수정 | `JournalEntryRegistModal.vue` (`App.vue` 전역 마운트) | 소속 엔트리 수정 |
| 엔트리 원문 | `JournalEntryViewModal.vue` (`App.vue` 전역 마운트) | 관련글·스레드 칩 대상 열기 |
| 해석 등록·수정 | `JournalInterpretationRegistModal.vue` | 소속 엔트리 해석 등록·수정 |
| 이력 | `HistoryModal.vue` | 소속 엔트리 이력 조회·복원·삭제 |
| 관련글 추가 | `RelatedContentAddModal.vue` | 소속 엔트리 관련글 추가 |
| 태그 프로필 | `JournalTagProfileModal.vue` | 소속 엔트리 태그 설정 |

`JournalThreadDetailModal.vue`의 제목·댓글 섹션(인라인 목록·등록·목록 모달 진입)·닫기 버튼은 현재 locale의 클라이언트 카탈로그를 사용한다. 상세 모달의 댓글 등록은 `CommentRegistModal`을 연다(`JournalThreadLayout`에 마운트).

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

#### 2-b. 주간 네비게이터 (Week section) — **✓ Vue SPA (`JournalAside.vue`)**

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

#### 2-c. Pinpoint 섹션 — **✓ Vue SPA (`JournalAside.vue` + `journalAside` store, localStorage `journal_day_pinpoint`)**

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
- Vue SPA: `journalModalStore.prefetchChapterCategories()` → `GET /api/code/items?groupCode=JOURNAL_CHAPTER_DIARY_CTGR_CD` + `GET /api/code/items?groupCode=JOURNAL_CHAPTER_NOTE_CTGR_CD` 병합

**필터 구조 계약**:

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
        <!-- B-2: CHAPTER CATEGORIES 체크박스 목록 (DIARIES 블록 안에만 있음) -->
        <div class="journal-aside-chapter-categories d-flex flex-column gap-1">
            <label v-for="ctgr in chapterCategoryOptions" :key="ctgr.code"
                   class="form-check form-check-sm form-check-custom form-check-solid cursor-pointer">
                <input class="form-check-input w-16px h-16px"
                       type="checkbox"
                       :checked="isChapterCategorySelected(ctgr.code)"
                       @change="toggleChapterCategory(ctgr.code)">
                <span class="form-check-label text-muted fs-8">[{{ ctgr.codeName }}]</span>
            </label>
        </div>
        <!-- B-3: DIARY LIFECYCLE 선택 -->
        <div class="d-flex flex-column ps-3 gap-1">
            <label for="diaryLifecycleFilter" class="text-muted mb-0">- DIARY LIFECYCLE</label>
            <select id="diaryLifecycleFilter"
                    class="form-select form-select-sm"
                    v-model="store.diaryLifecycleKey"
                    @change="store.fetchDays()">
                <option value="">전체</option>
                <option value="OPEN">진행 중</option>
                <option value="PENDING">보류</option>
                <option value="RESOLVED">완료</option>
            </select>
        </div>
        <!-- B-4: DIARY KEYWORDS 입력 -->
        <div class="d-flex flex-column ps-3 gap-1">
            <label for="diaryFilterKeyword" class="text-muted mb-0">- DIARY KEYWORDS</label>
            <div class="d-flex gap-1">
                <input type="text" name="diaryFilterKeyword" id="diaryFilterKeyword"
                       class="form-control form-control-sm"
                       v-model="store.diaryKeyword"
                       placeholder="일기 키워드" maxlength="200"
                       @keyup.enter="applyFilters">
                <button type="button" class="btn btn-sm btn-outline btn-light-primary px-3">
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
        <!-- C-2: DREAM LIFECYCLE 선택 -->
        <div class="d-flex flex-column ps-3 gap-1">
            <label for="dreamLifecycleFilter" class="text-muted mb-0">- DREAM LIFECYCLE</label>
            <select id="dreamLifecycleFilter"
                    class="form-select form-select-sm"
                    v-model="store.dreamLifecycleKey"
                    @change="store.fetchDays()">
                <option value="">전체</option>
                <option value="OPEN">진행 중</option>
                <option value="PENDING">보류</option>
                <option value="RESOLVED">완료</option>
            </select>
        </div>
        <!-- C-3: DREAM KEYWORDS 입력 -->
        <div class="d-flex flex-column ps-3 gap-1">
            <label for="dreamFilterKeyword" class="text-muted mb-0">- DREAM KEYWORDS</label>
            <div class="d-flex gap-1">
                <input type="text" name="dreamFilterKeyword" id="dreamFilterKeyword"
                       class="form-control form-control-sm"
                       v-model="store.dreamKeyword"
                       placeholder="꿈 키워드" maxlength="200"
                       @keyup.enter="applyFilters">
                <button type="button" class="btn btn-sm btn-outline btn-light-primary px-3">
                    <i class="bi bi-funnel pe-0"></i>
                </button>
            </div>
        </div>
    </div>
    <div class="separator"></div>

    <!-- 블록 D: ENTRY FILTER 레이블 -->
    <div class="text-gray-900 fs-6 fw-bold mt-1">ENTRY FILTER</div>

    <!-- 블록 E: 고급 필터 아코디언 — 이식 대상 아님 (레거시 빈 placeholder; 구현은 JournalEntrySearchPage) -->
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

**부모-자식 배치 계약**:
- `TAGCLOUD`는 엔트리 필터와 독립된 표시 토글로 둔다.
- `DIARIES` 토글은 `CHAPTER CATEGORIES`, `DIARY LIFECYCLE`, `DIARY KEYWORDS`의 부모 항목이다.
- `DREAMS` 토글은 `DREAM LIFECYCLE`, `DREAM KEYWORDS`의 부모 항목이다.
- `DIARIES=false` 또는 `DREAMS=false`일 때 해당 하위 필터 값은 삭제하지 않고 보존하되, 하위 필터 UI는 렌더링하지 않는다.
- `ENTRY FILTER` 레이블은 위 필터 묶음 아래에 표시한다.

**엔트리 필터 Vue 상태 바인딩**:
| Element | 레거시 ID | Vue 상태 |
|---------|----------|---------|
| TAGCLOUD 체크박스 | `#toggleTagCloud` | `store.showTagCloud` — ON 변경 시 `store.fetchTagCloud()` |
| DIARIES 체크박스 | `#toggleDiaries` | `store.showDiaries` — 변경 시 `store.fetchDays()` |
| CHAPTER CATEGORIES 항목 체크박스 | `.journal-aside-chapter-categories input[type=checkbox]` | `store.chapterCtgrCds: string[]` — `store.showDiaries=true` 시에만 표시, 변경 시 `store.fetchDays()` |
| 챕터 옵션 목록 | `window.__journalAsideEntryFiltersBootstrap.chapterCtgrOptions` | `journalModalStore.chapterDiaryCategoryOptions` + `chapterNoteCategoryOptions` — `prefetchChapterCategories()` 로 로드 |
| DREAMS 체크박스 | `#toggleDreams` | `store.showDreams` — 변경 시 `store.fetchDays()` |
| 일기 라이프사이클 | `#diaryLifecycleFilter` | `store.diaryLifecycleKey` — `store.showDiaries=true` 시에만 표시, 변경 시 `store.fetchDays()` |
| 꿈 라이프사이클 | `#dreamLifecycleFilter` | `store.dreamLifecycleKey` — `store.showDreams=true` 시에만 표시, 변경 시 `store.fetchDays()` |
| 일기 키워드 | `#diaryFilterKeyword` | `store.diaryKeyword` — `store.showDiaries=true` 시에만 표시, Enter 시 `store.fetchDays()` |
| 꿈 키워드 | `#dreamFilterKeyword` | `store.dreamKeyword` — `store.showDreams=true` 시에만 표시, Enter 시 `store.fetchDays()` |

`JournalDayLayout.vue`의 필터 패널 열기와 `JournalAside.vue`의 필터 패널 닫기·정렬 변경·날짜 선택·Pinpoint 저장/복귀 tooltip, 월 단위와 요일 축약명은 현재 locale의 클라이언트 카탈로그를 사용한다. locale 변경은 필터 상태·기간·선택 일자·Pinpoint 저장값·route query를 변경하지 않는다.

필터 영역의 고정 영문 제목·토글·섹션명, 로딩·카테고리 조회 실패, 라이프사이클 선택지, 일기·꿈 키워드 placeholder/적용 tooltip, 필터 초기화와 TODO 카드 제목·할일 등록 문구도 현재 locale의 클라이언트 카탈로그를 사용한다. locale 변경은 기존 필터값과 조회·초기화·모달 호출 동작을 변경하지 않는다.

- 챕터 카테고리 필터로 숨겨진 무카테고리 챕터 힌트는 서버가 `common.category.none`을 현재 locale로 조회해 `카테고리 없음`/`No category`로 표시한다.

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
- 모달 제목·대상 년월·필드·placeholder·안내·검증·확인·결과 fallback은 현재 locale의 클라이언트 카탈로그를 사용하고, 저장 API의 서버 `message`가 있으면 우선 표시한다.

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
| 2. Pinpoint | 핀 고정 버튼 | ✓ `pinpoint()` → `asideStore.setPinpoint` |
| 2. Pinpoint | 고정 yy/mnth 표시 | ✓ `pinnedYy` / `pinnedMnth` + 되돌리기 UI |
| 2. Pinpoint | 돌아가기 버튼 | ✓ `turnback()` — 고정된 yy/mnth 로 이동 (`gotoYyMnth`) |
| 3. 엔트리 필터 | TAGCLOUD 토글 | ✓ 구현 |
| 3. 엔트리 필터 | DIARIES 토글 | ✓ 구현 |
| 3. 엔트리 필터 | CHAPTER CATEGORIES (체크박스, 일기·노트 코드 병합) | ✓ |
| 3. 엔트리 필터 | 일기 키워드 | ✓ 구현 (위치 다름) |
| 3. 엔트리 필터 | DREAMS 토글 | ✓ 구현 |
| 3. 엔트리 필터 | 꿈 키워드 | ✓ 구현 (위치 다름) |
| 3. 엔트리 필터 | 고급 필터 아코디언 (블록 D) | — 이식 대상 아님 — 검색 팝업 고급필터 ✓ |
| 4. TODO 카드 | "TODO List" 카드 헤더 | ✓ `JournalAsideTodoCard.vue` (현재 locale 카탈로그 사용) |
| 4. TODO 카드 | TODO 목록 표시 | ✓ `store.fetchTodos()` 월별 목록 표시 |
| 4. TODO 카드 | TODO 삭제 버튼 | ✓ 삭제 API 성공 후 목록 재조회 |

---

## 저널 엔트리 검색 새 창 (Journal Entry Search Popup)

- **Legacy screen**: `legacy/templates/view/feature/journal/entry/journal_entry_search.ftlh`
- **Vue SPA route**: `/journal/entry/search` (`journal-entry-search`)
- **Vue view**: `app/frontend-vue/src/features/journal/entry/JournalEntrySearchPage.vue`
- **Layout**: `SystemLayout.vue` 하위 auth route. 새 창 검색 화면이므로 `DefaultLayout` 메뉴와 `JournalLayout` aside를 렌더링하지 않는다.
- **Open pattern**: 태그 컨텍스트 메뉴의 `검색` 버튼에서 `window.open(...)`. 이미 이 검색 화면 안에 있는 경우에는 같은 창에서 route query를 갱신한다.

### URL Contract

| Query | 의미 |
|-------|------|
| `type=DIARY` | 일기 검색 |
| `type=DREAM` | 꿈 검색 |
| `tagIds=N` | 태그 ID 기반 검색. 검색 팝업의 태그 직접 입력은 기존 태그 자동완성/카테고리 선택으로 특정 태그 ID를 확정한 뒤 이 파라미터에 추가한다. |
| `searchKeywords=...` | 키워드 검색 |

### Data Contract

- 목록 API: `GET /api/journal/entries`
- 파라미터: `type`, `tagIds`, `searchKeywords`
- 고급 필터: 유형 토글, 키워드 입력, 태그 입력. 태그 입력은 현재 `type`의 엔트리 태그 categoryMap과 태그 목록을 사용해 기존 태그만 선택하며, 선택 결과는 `tagIds` query로 보존한다.
- 응답: `AjaxResponse.rsltList` (`JournalEntryDto[]`)
- `type=DREAM` 응답의 태그 항목은 사용자별 꿈 태그 프로필 본문이 있으면 `tag.list[].profileContent`를 포함한다. 검색 결과 행은 `JournalEntryItem`을 통해 꿈 엔트리 본문 아래에 해당 프로필을 표시한다.
- 검색 키워드가 있으면 검색 결과 행의 엔트리 본문에서 해당 키워드와 일치하는 텍스트를 하이라이트한다. 하이라이트는 검색 팝업에서만 활성화하며, 결과 HTML 구조와 복사/TXT 내보내기 포맷을 변경하지 않는다.

### Behavioral Contract

- 태그 클릭 자체는 검색을 실행하지 않고 `JournalTagContextMenu`를 연다.
- 태그 컨텍스트 메뉴의 액션과 태그 프로필 콘텐츠 유형 레이블은 현재 locale의 클라이언트 카탈로그를 사용한다.
- 일기/꿈 태그의 `검색` 액션은 현재 월간/주간 목록 필터를 변경하지 않고 새 창 검색 화면으로 이동한다.
- 검색 팝업 내부에서 일기/꿈 태그의 `검색` 액션을 누르면 새 팝업을 열지 않고 같은 창의 URL query를 바꿔 결과를 재조회한다.
- 검색 팝업 내부의 `태그 설정` 액션은 `JournalTagProfileModal`을 같은 창에서 연다. 저장·삭제 성공 후 `success` 이벤트로 검색 결과(`loadEntries`)를 재조회한다.
- 검색 팝업 내부 검색 결과에 저널 해석이 표시되면, 해석 행의 수정 액션은 `JournalInterpretationRegistModal`을 같은 창에서 열어야 한다.
- 각 검색 결과는 수정/삭제가 가능하다. 수정 저장 성공 시 성공 알림 표시 전에 현재 검색 조건 목록 또는 수정 대상 DOM을 준비하고, 성공 알림 OK 이후 저장한 결과 위치로 스크롤하며, 삭제 성공 시 결과 목록에서 제거한다.
- 검색 팝업 내부에서 엔트리 본문/태그 프로필 다음에 이어지는 저널 해석 행은 같은 결과 묶음으로 읽히도록 공통 저널 행 간격보다 좁게 붙여 표시한다.
- 날짜가 바뀔 때 표시되는 날짜 헤더에는 새 창 버튼이 있어야 한다. 버튼은 해당 날짜의 `/journal/daily?stdrdDt=YYYY-MM-DD` 일자 뷰를 새 창으로 연다.
- 날짜 헤더에는 해당 날짜에 묶인 현재 검색 결과 건수를 함께 표시해 같은 날짜 결과 밀도를 바로 파악할 수 있어야 한다.
- 결과 목록 위에는 현재 결과의 총 건수, 날짜 수, 월 수를 요약해 표시한다.
- 검색 조건은 URL query에 남아야 한다. 검색 결과는 새로고침/공유 가능한 주소 기반 상태여야 한다.
- 키워드/태그 입력은 Enter 로 조건 추가가 가능함을 입력 힌트와 title로 안내한다. 키워드/태그 배지는 제거 tooltip을 제공한다.
- 태그명이 여러 카테고리에 걸쳐 카테고리 선택이 필요한 동안에는 태그 입력과 추가 버튼을 비활성화하고, 카테고리 선택 또는 취소를 안내한다.
- `searchKeywords`와 `tagIds`가 모두 비어 있으면 `type`만으로 목록 API를 호출하지 않고, 검색 전 안내 상태를 표시한다. 검색 전 안내에는 고급 필터를 열고 키워드 입력으로 포커스를 이동하는 조건 추가 CTA가 있어야 한다. 검색 결과가 0건이면 같은 방식으로 조건 수정 CTA를 표시한다. 초기화도 이 검색 전 상태로 돌아간다.
- 검색, 결과 복사, TXT 내보내기는 키워드/태그 입력칸에 남아 있는 값을 먼저 검색 조건으로 확정한 뒤 실행한다. 중복 키워드/태그는 조용히 무시하지 않고 안내 메시지를 표시한다.
- 키워드/태그 입력칸에 아직 확정하지 않은 값이 남아 있으면 고급 필터 영역에 검색 실행 시 해당 값도 조건에 포함된다는 안내를 표시한다.
- 검색 조회 중이거나 복사/TXT 내보내기 실행 중이면 검색·정렬·초기화·결과 복사·TXT 내보내기 버튼을 비활성화한다. 결과 복사는 현재 결과가 있거나 확정 전 입력 조건이 남아 있을 때만 활성화하고, TXT 내보내기는 URL 검색 조건이 있거나 확정 전 입력 조건이 남아 있을 때만 활성화한다.
- 키워드/태그 배지 제거, 정렬 변경, 유형 변경으로 검색 조건이 바뀌면 결과 상태 라벨에 변경 사유를 표시한다. 직전 결과가 있는 상태에서 새 조회가 진행 중이면 목록을 비우지 않고 유지하며, 목록 위에 갱신 중 안내를 표시한다.
- 검색 API 실패는 `0건`으로 표시하지 않는다. 직전 성공 결과와 건수를 보존하고 서버 오류 메시지 또는 검색 실패 안내를 표시한다.
- `결과 복사` 버튼은 현재 검색 결과 전체를 legacy 포맷(`날짜 (요일)`, `#정렬번호`, 본문)으로 클립보드에 복사한다. 본문의 이름·10진수·16진수 HTML 엔티티는 화면 렌더링과 동일하게 문자로 디코딩한다. 확정 전 입력값을 조건에 반영한 뒤 복사한 경우 성공 알림은 조건 반영 후 복사임을 구분해 표시한다.
- 컨트롤 바·고급 필터·유형/키워드/태그 입력·입력 힌트·확정 전 입력 안내·배지 제거 tooltip·카테고리 선택·카테고리 선택 대기 안내·조건 요약·결과 상태 라벨·검색 전 안내와 조건 추가 CTA·빈 결과 조건 수정 CTA·로딩/빈 결과·갱신 중 안내·결과 요약·일자 새 창 tooltip과 결과 건수·연월 구분선·날짜별 결과 건수는 현재 locale의 클라이언트 카탈로그를 사용한다. locale 변경은 URL query·검색 조건·정렬·태그 선택·결과 목록을 변경하지 않는다.
- 검색 결과/엔트리 상세 조회 실패, 태그 선택·검색 조건 검증, 중복 조건 안내, 복사 대상 없음과 복사 성공/실패 알림도 현재 locale의 클라이언트 카탈로그를 사용한다. 클립보드와 TXT 본문은 기존 레거시 출력 포맷을 유지한다.
- 각 검색 결과 행의 복사 버튼은 해당 엔트리 하나만 같은 포맷으로 복사한다.
