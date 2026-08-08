# Vue SPA 전체 화면 현황 (Vue Screen Overview)

> 저널 화면 상세: `journal/screen-spec.md`  
> 게시판 화면 상세: `board/screen-spec.md`  
> 관리자 화면 상세: `admin/screen-spec.md`  
> 인증/사용자 화면 상세: `auth/screen-spec.md`  
> 공통 컴포넌트: `common/component-spec.md` · 인터랙션: `common/interaction-spec.md`

## 전체 라우트 매핑

| Vue route | route name | Vue view | Layout | 구현 |
|-----------|-----------|----------|--------|------|
| `/` | — | redirect → `/journal/weekly` | DefaultLayout | — |
| `/journal` | — | redirect → `/journal/weekly` | JournalLayout | — |
| `/journal/weekly` | `journal-weekly` | `JournalDayWeekly.vue` | Journal | ✓ |
| `/journal/monthly` | `journal-monthly` | `JournalDayMonthly.vue` | Journal | ✓ |
| `/journal/calendar` | `journal-calendar` | `JournalDayCalendar.vue` | Journal | ✓ FullCalendar 일자/일기/꿈+공휴일 |
| `/journal/meta` | `journal-meta` | `JournalDayMeta.vue` | Journal | ✓ 메타 컨텍스트 메뉴·단일 차트 2시리즈 비교 |
| `/journal/daily` | `journal-daily` | `JournalDayDaily.vue` | JournalDayDailyLayout | ✓ 새 창 전용 |
| `/annual` | `annual-list` | `JournalAnnualList.vue` | AnnualLayout | ✓ |
| `/annual/:yy` | `annual-detail` | `JournalAnnualDetail.vue` | AnnualLayout | ✓ |
| `/thread` | `thread-list` | `JournalThreadList.vue` | ThreadLayout | ✓ |
| `/thread/new` · `/thread/:id` · `/thread/:id/edit` | `thread-*` | `JournalThreadList.vue` | ThreadLayout | ✓ |
| `/schedule` | `schedule-calendar` | `ScheduleCalendar.vue` | Default | ✓ |
| `/board/:boardKey` | `board-post-list` | `BoardPostList.vue` | BoardPostLayout | ✓ |
| `/my` | — | redirect → `/my/profile` | Default | — |
| `/my/profile` · `/my/security` · `/my/prefixes` | `user-my-profile` · `user-my-security` · `user-my-prefixes` | `UserMyPage.vue` + `UserMy{Profile,Security,Prefixes}Tab.vue` | Default | ✓ |
| `/admin` | `admin-page` | `AdminPage.vue` | Default | ✓ |
| `/admin/auth-policy` | `auth-policy` | `AuthPolicyPage.vue` | Default | ✓ |
| `/admin/board-group` | `board-group-admin` | `BoardGroupAdminPage.vue` | Default | ✓ |
| `/admin/code` | `code-admin` | `CodeAdminPage.vue` | Default | ✓ |
| `/admin/menu` | `menu-admin` | `MenuAdminPage.vue` | Default | ✓ |
| `/admin/user-groups` | `user-group-admin` | `UserGroupAdminPage.vue` | Default | ✓ |
| `/admin/users` | `user-admin` | `UserAdminPage.vue` | Default | ✓ |
| `/admin/log` | `log-list` | `LogAdminPage.vue` | Default | ✓ |
| `/admin/log/stats-user` | `log-stats-user` | `LogAdminPage.vue` | Default | ✓ 사용자별/비로그인 통계 |
| `/admin/users?tab=signup` | `user-admin` (탭) | `UserSignupApprovalList.vue` | Default | ✓ |
| `/sign-in` | `sign-in` | `SignIn.vue` | Auth | ✓ |
| `/user/signup` | `user-signup` | `UserSignupPage.vue` | Auth | ✓ |
| `/auth/verify-result` | `auth-verify-result` | `VerifyResultPage.vue` | Auth | ✓ |
| `/journal/entry/search` | `journal-entry-search` | `JournalEntrySearchPage.vue` | System | ✓ |
| `/error`, `/400`, `/403`, `/404`, `/500` | — | `ErrorPage.vue` | System | ✓ |

---

## 레이아웃 구조

```
DefaultLayout
├── Navbar (검색 드롭다운 포함)
├── SidebarMenu (동적 서버 메뉴)
└── <router-view>
    ├── JournalLayout          ← journal/* 전용 wrapper (aside 포함)
    │   ├── JournalWeekly / JournalMonthly / JournalCalendar / JournalMeta
    │   └── JournalAside (필터 패널)
    ├── JournalAnnualLayout    ← annual/* 전용 wrapper
    ├── JournalThreadLayout    ← thread/* 전용 wrapper
    ├── BoardPostLayout        ← board/* 전용 wrapper
    ├── ScheduleCalendar
    ├── UserMyPage
    ├── Admin* 페이지들
    └── UserSignupApprovalList

AuthLayout
├── SignIn
├── UserSignupPage
└── VerifyResultPage

SystemLayout                   ← 헤더/사이드바 없는 새 창/팝업 전용
├── JournalDayDailyLayout         ← /journal/daily 전용 (모달 포함, aside 없음)
│   └── JournalDayDaily.vue
├── JournalEntrySearchPage     ← 새 창 검색 팝업
└── ErrorPage (400/403/404/500/error)
```

---

## JournalAside 구현 현황

| 기능 | 구현 상태 |
|------|---------|
| FILTER 헤더 + 정렬 토글 (`ASC`/`DESC`) | ✓ |
| 연도 `<select>` (현재 연도 → 2010 역순) | ✓ |
| 월 그리드 + 이전/다음 화살표 | ✓ |
| 주간 요일 버튼 7개 (`hasDay`/`isActive`) | ✓ |
| 이전/다음 주 화살표 | ✓ |
| TODAY 버튼 | ✓ |
| Pinpoint (핀 고정 / 표시 / 돌아가기) | ✓ |
| DIARIES 토글 | ✓ |
| DREAMS 토글 | ✓ |
| TAGCLOUD 토글 | ✓ |
| 어사이드 닫기 버튼 | ✓ |
| CHAPTER CATEGORIES 체크박스 (일기·노트 코드 그룹 병합) | ✓ |
| DIARY/DREAM LIFECYCLE select 필터 | ✓ |
| 일기/꿈 키워드 필터 input (어사이드) | ✓ |
| 필터 초기화 버튼 | ✓ |
| TODO List 카드 (등록 + 목록·삭제, `JournalAsideTodoCard`) | ✓ |
| 고급 필터 아코디언 (블록 D) | — 이식 대상 아님 — 검색 팝업 `JournalEntrySearchPage` 참고 |

---


### 저널 필터·검색 정책 (Vue SPA)

> 상세: `journal/screen-spec.md` · `journal/interaction-spec.md` · `journal/component-spec.md`

| 구분 | Vue 위치 | 레거시 | 상태 |
|------|----------|--------|------|
| 툴바 전체검색 | `JournalDayViewToolbar.vue` → `openSearchTab()` | `_journal_day_keyword_search.ftlh` 팝업 | ✓ **대체 완료** — 새 탭 `/journal/entry/search` |
| 어사이드 목록 키워드 | `JournalAside.vue` `diaryKeyword` / `dreamKeyword` | aside 인라인 필터 | ✓ 현재 월·주간 **목록 축소** (`fetchDays`) — 툴바 검색과 **상태·동작 분리** |
| 검색 팝업 고급필터 | `JournalEntrySearchPage.vue` 아코디언 | 검색 화면 고급 필터 | ✓ |
| 어사이드 고급필터 아코디언 | — (블록 D) | aside accordion **빈 placeholder** | — **이식 대상 아님** — 실제 컨트롤은 검색 팝업에만 존재 |
| 레이아웃 전역 툴바 | `JournalDayViewToolbar.vue` | `_journal_day_page_header.ftlh` 나머지 | ✓ 고급필터·태그 카테고리 동기화·저널 일자 등록·aside 열기. 일정 등록·개인 일정은 일정 화면에서만 제공 |

## 미구현/부분구현 요약

| 화면/기능 | 상태 | 비고 |
|----------|------|------|
| JournalCalendar | ✓ | 저널 일자/일기/꿈 + 공휴일 이벤트, 상세 모달 연동 (고급 필터 collapse 는 백엔드 계약 부재로 미이식 — `journal/screen-spec.md` 참고) |
| JournalMeta 그래프 | ✓ | 컨텍스트 메뉴(검색/그래프로 보기/메타 설정), 최대 2메타·헤더 × 제거, 연도 전체, 한 차트 2선 |

---

## Pinia 스토어 목록

| 스토어 | 담당 화면/기능 |
|-------|-------------|
| `auth.ts` | 인증 상태, 로그인/로그아웃, `verifyAuth` |
| `menu.ts` | 사이드바 동적 메뉴 (`USER`/`MNGR` 모드) |
| `journal.ts` | 저널 일자 목록, 태그클라우드, 메타, 필터 상태 |
| `journalModal.ts` | 저널 모달 열기/닫기 (일자/챕터/엔트리/투두 등). categoryMap 은 `journalCategoryMaps`에 위임 |
| `journalModal.types.ts` | 저널 모달 폼·페이로드 타입 (`journalModal` 에서 re-export) |
| `journalModalOneShot.ts` | 저장 직후 일회성 챕터 펼침·Reflection 접힘 신호 |
| `journalModalDay.ts` | 일자 등록·상세·필터·메타 프로필 모달 surface |
| `journalModalEntry.ts` | 엔트리 등록·수정·읽기 전용 모달 surface |
| `journalModalChapter.ts` | 챕터 등록/수정 모달 surface |
| `journalModalReflection.ts` | Reflection 등록/수정 모달 surface |
| `journalModalTodo.ts` | 할일 등록/수정 모달 surface |
| `journalCategoryMaps.ts` | 앱 세션 태그·메타 categoryMap SSOT (preload/ensure/sync/apply) |
| `journalAside.ts` | 어사이드 표시 제어 (`visible`, `show/hide`) |
| `journalAnnual.ts` | 연간 결산 목록/상세 |
| `journalAnnualAside.ts` | 결산 목록/상세 공통 FILTER 어사이드 표시 제어 (`visible`, `show/hide`) |
| `journalThread.ts` | 스레드 목록/상세 |
| `journalThreadMembership.ts` | 저널 엔트리↔스레드 소속 지정 액션 (FLOW 대체 축) |
| `tagContextMenu.ts` | 태그 클릭 컨텍스트 메뉴 위치·상태 |
| `metaContextMenu.ts` | 메타 클릭 컨텍스트 메뉴 위치·상태 |
| `attachableModal.ts` | 댓글/이력/파일/태그 공통 모달 |
| `boardPost.ts` | 게시판 목록/상세 |
| `schedule.ts` | 일정 캘린더 |
| `scheduleAside.ts` | 일정 aside 표시 제어 (`visible`, `show/hide`) |
| `userMy.ts` | 내 정보 |
| `userPrefixes.ts` | 내 설정 사용자 소유 말머리 관리 |
| `personalPrefixOptions.ts` | 활성 개인 Prefix 선택지 콘텐츠 타입별 세션 캐시 |
| `userSignup.ts` | 계정 신청 승인 |
| `adminPage.ts` | 사이트 관리 |
| `authPolicy.ts` | 인증 정책 |
| `boardGroup.ts` | 게시판 그룹 관리 |
| `boardPrefixes.ts` | 게시판 그룹 관리 — boardKey별 GLOBAL Scope 말머리 목록 |
| `codeAdmin.ts` | 코드 관리 |
| `menuAdmin.ts` | 메뉴 관리 |
| `userAdmin.ts` | 계정 관리 |
| `userGroup.ts` | 사용자 그룹·permission 관리 |
| `logAdmin.ts` | 로그 관리 |
| `chat.ts` | AI 채팅 (라우트 없음, `AppChat.vue`) |
| `locale.ts` | i18n locale·메시지 카탈로그 (`t()`) |
| `body.ts`, `config.ts`, `theme.ts` | 레이아웃 설정 |
