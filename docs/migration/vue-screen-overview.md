# Vue SPA 전체 화면 현황 (Vue Screen Overview)

> 저널 화면 상세 스펙은 `journal/screen-spec.md` 참조.  
> 공통 컴포넌트 스펙은 `common/component-spec.md`, 인터랙션 스펙은 `common/interaction-spec.md` 참조.

## 전체 라우트 매핑

| Vue route | route name | Vue view | Layout | 구현 |
|-----------|-----------|----------|--------|------|
| `/` | — | redirect → `/dashboard` | DefaultLayout | — |
| `/dashboard` | `dashboard` | `Dashboard.vue` | Default | ❌ placeholder |
| `/journal` | — | redirect → `/journal/weekly` | JournalLayout | — |
| `/journal/weekly` | `journal-weekly` | `JournalWeekly.vue` | Journal | ✓ |
| `/journal/monthly` | `journal-monthly` | `JournalMonthly.vue` | Journal | ✓ |
| `/journal/calendar` | `journal-calendar` | `JournalCalendar.vue` | Journal | ❌ placeholder |
| `/journal/meta` | `journal-meta` | `JournalMeta.vue` | Journal | ⚠ 그래프 미구현 |
| `/annual` | `annual-list` | `JournalAnnualList.vue` | AnnualLayout | ✓ |
| `/annual/:yy` | `annual-detail` | `JournalAnnualDetail.vue` | AnnualLayout | ✓ |
| `/thread` | `thread-list` | `JournalThreadList.vue` | ThreadLayout | ✓ |
| `/schedule` | `schedule-calendar` | `ScheduleCalendar.vue` | Default | ✓ |
| `/board/:boardKey` | `board-post-list` | `BoardPostList.vue` | BoardPostLayout | ✓ |
| `/my` | `user-my` | `UserMyPage.vue` | Default | ✓ |
| `/admin` | `admin-page` | `AdminPage.vue` | Default | ✓ |
| `/admin/auth-policy` | `auth-policy` | `AuthPolicyPage.vue` | Default | ✓ |
| `/admin/board-group` | `board-group-admin` | `BoardGroupAdminPage.vue` | Default | ✓ |
| `/admin/code` | `code-admin` | `CodeAdminPage.vue` | Default | ✓ |
| `/admin/menu` | `menu-admin` | `MenuAdminPage.vue` | Default | ✓ |
| `/admin/users` | `user-admin` | `UserAdminPage.vue` | Default | ✓ |
| `/admin/log` | `log-list` | `LogAdminPage.vue` | Default | ✓ |
| `/admin/log/stats-user` | `log-stats-user` | `LogAdminPage.vue` | Default | ⚠ 동일 컴포넌트, stats placeholder |
| `/user/signup/approval` | `user-signup-approval` | `UserSignupApprovalList.vue` | Default | ✓ |
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
    ├── Dashboard.vue
    ├── JournalLayout          ← journal/* 전용 wrapper
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

SystemLayout
├── JournalEntrySearchPage    ← 새 창 팝업용
└── ErrorPage (400/403/404/500/error)
```

---

## DefaultLayout 화면 상세

### Dashboard (`/dashboard`)

| 항목 | 내용 |
|------|------|
| 구현 상태 | ❌ placeholder (`대시보드 준비 중입니다.` 텍스트만) |
| 스토어 | 없음 |
| TODO | 대시보드 위젯 구현 |

---

### JournalLayout (`/journal/*`)

> 상세 스펙: `journal/screen-spec.md`

#### JournalAside (사이드 필터 패널) 현재 구현 현황

| 기능 | 구현 상태 |
|------|---------|
| FILTER 헤더 + 정렬 토글 (`ASC`/`DESC`) | ✓ |
| 연도 `<select>` (현재 연도 → 2010 역순) | ✓ |
| 월간 뷰: 3열 월 그리드 + 이전/다음 화살표 | ✓ (레거시 `<select>` 방식 아닌 그리드 방식) |
| 주간 뷰: 요일 버튼 7개 (`hasDay`/`isActive` 상태) | ✓ |
| 이전/다음 주 화살표 | ✓ |
| TODAY 버튼 | ✓ |
| Pinpoint (핀 고정 + 고정 yy/mnth 표시 + 돌아가기) | ✓ |
| DIARIES 토글 | ✓ |
| DREAMS 토글 | ✓ |
| TAGCLOUD 토글 | ✓ |
| 할일 등록 버튼 (`JournalTodoRegistModal` 연결) | ✓ |
| 태그 목록 버튼 (`JournalTagListModal` 연결) | ✓ |
| 어사이드 닫기 버튼 | ✓ |
| CHAPTER CATEGORIES 멀티셀렉트 (`chapterCtgrCds`) | ❌ MISSING |
| 일기/꿈 키워드 필터 input (어사이드 위치) | ❌ MISSING (툴바에만 있음) |
| 고급 필터 아코디언 | ❌ MISSING |
| TODO 목록 표시 + 삭제 (API: `GET/DELETE /api/journal/todo`) | ❌ MISSING |

> **interaction-spec.md §Pinpoint** 항목(`❌`)은 현재 구현 완료됨. 해당 스펙 항목 수정 필요.

#### 라우트별 뷰 현황

| 뷰 | 구현 상태 | 비고 |
|----|---------|------|
| `JournalWeekly.vue` | ✓ | 주간 일자 카드 목록 |
| `JournalMonthly.vue` | ✓ | 월간 일자 카드 목록 |
| `JournalCalendar.vue` | ❌ | `JournalDayViewToolbar` + placeholder 텍스트만 |
| `JournalMeta.vue` | ⚠ | 메타 헤더 목록 표시, 메타 클릭 → 엔트리 목록 조회; 그래프 TODO |

---

### JournalAnnualLayout (`/annual/*`)

> 상세 스펙: `journal/screen-spec.md`

| 뷰 | 구현 상태 |
|----|---------|
| `JournalAnnualList.vue` | ✓ |
| `JournalAnnualDetail.vue` | ✓ |

---

### JournalThreadLayout (`/thread`)

> 상세 스펙: `journal/screen-spec.md`

| 뷰 | 구현 상태 |
|----|---------|
| `JournalThreadList.vue` | ✓ |

---

### BoardPostLayout (`/board/:boardKey`)

> 상세 스펙: `journal/screen-spec.md`

| 뷰 | 구현 상태 |
|----|---------|
| `BoardPostList.vue` | ✓ |

---

### ScheduleCalendar (`/schedule`)

| 항목 | 내용 |
|------|------|
| 스토어 | `stores/schedule.ts` |
| 주요 기능 | FullCalendar 기반 달력, 조회일 date input, 키워드 검색, 필터 패널 collapse, 일정 등록 버튼 |
| 구현 상태 | ✓ (기능 구현됨) |
| 모달 | 일정 등록/수정 모달 포함 |

---

### UserMyPage (`/my`)

> 상세 스펙: `journal/screen-spec.md` §내 정보 페이지

| 항목 | 내용 |
|------|------|
| 스토어 | `stores/userMy.ts` |
| 주요 기능 | 프로필 정보 조회/수정, 프로필 이미지 업로드, 비밀번호 변경 모달, 연차 정보(입사일 있는 경우) |
| 구현 상태 | ✓ |

---

### 관리자 화면 (`/admin/*`)

#### AdminPage (`/admin`)

| 항목 | 내용 |
|------|------|
| 스토어 | `stores/adminPage.ts` |
| 주요 기능 | 캐시 조회/삭제, 외부 동기화(노션/기타), 권한 관리, 임베딩 큐 |
| 구현 상태 | ✓ |

#### AuthPolicyPage (`/admin/auth-policy`)

| 항목 | 내용 |
|------|------|
| 스토어 | `stores/authPolicy.ts` |
| 주요 기능 | IP 허용 정책, 허용 IP 목록 CRUD |
| 구현 상태 | ✓ |

#### BoardGroupAdminPage (`/admin/board-group`)

| 항목 | 내용 |
|------|------|
| 스토어 | `stores/boardGroup.ts` |
| 주요 기능 | 게시판 그룹 목록/상세/등록·수정/사용여부/삭제 |
| 구현 상태 | ✓ |

#### CodeAdminPage (`/admin/code`)

| 항목 | 내용 |
|------|------|
| 스토어 | `stores/codeAdmin.ts` |
| 주요 기능 | 코드 그룹 + 상세코드 CRUD, 정렬/사용여부 |
| 구현 상태 | ✓ |

#### MenuAdminPage (`/admin/menu`)

| 항목 | 내용 |
|------|------|
| 스토어 | `stores/menuAdmin.ts` |
| 관련 컴포넌트 | `MenuAdminTreeNode.vue` |
| 주요 기능 | 메뉴 트리 depth 관리, 사용자/관리자 구분, submenuExpandType, 순서 변경 |
| 구현 상태 | ✓ |

#### UserAdminPage (`/admin/users`)

| 항목 | 내용 |
|------|------|
| 스토어 | `stores/userAdmin.ts` |
| 주요 기능 | 계정 검색/권한 필터/상세/등록·수정/프로필·고용정보 서브폼 |
| 구현 상태 | ✓ |

#### LogAdminPage (`/admin/log`, `/admin/log/stats-user`)

| 항목 | 내용 |
|------|------|
| 스토어 | `stores/logAdmin.ts` |
| 주요 기능 | 운영 로그 목록/검색/상세 모달, `/admin/log/stats-user`는 사용자별 통계 placeholder |
| 구현 상태 | ✓ (stats placeholder 포함) |

---

### UserSignupApprovalList (`/user/signup/approval`)

| 항목 | 내용 |
|------|------|
| 스토어 | `stores/userSignup.ts` |
| 주요 기능 | 관리자 메뉴에서 계정 신청 목록 조회 및 승인/반려 |
| 구현 상태 | ✓ |

---

## AuthLayout 화면 상세

### SignIn (`/sign-in`)

| 항목 | 내용 |
|------|------|
| 스토어 | `stores/auth.ts` |
| 주요 기능 | 아이디/비밀번호 로그인, 로그인 실패 메시지 |
| 구현 상태 | ✓ |

### UserSignupPage (`/user/signup`)

| 항목 | 내용 |
|------|------|
| 주요 기능 | 계정 신청 폼 (이름, 이메일, 비밀번호 등) |
| 구현 상태 | ✓ |

### VerifyResultPage (`/auth/verify-result`)

| 항목 | 내용 |
|------|------|
| 주요 기능 | 이메일 인증 성공/실패 결과 표시 (레거시 `verify_success.ftlh` + `verify_failure.ftlh` 통합) |
| 구현 상태 | ✓ |

---

## SystemLayout 화면 상세

### JournalEntrySearchPage (`/journal/entry/search`)

> 상세 스펙: `journal/component-spec.md` §23-4

| 항목 | 내용 |
|------|------|
| 스토어 | `stores/journal.ts` (entries 조회) |
| 주요 기능 | 일기/꿈 태그/키워드 검색, 결과 수정/삭제, 태그 컨텍스트 메뉴, 태그 프로필 모달 |
| Open pattern | `window.open(...)` 새 창 (태그 컨텍스트 메뉴 검색 액션, 툴바 전체검색) |
| 구현 상태 | ✓ (결과 복사, TXT export, 고급 검색 UX 미구현 — `component-spec.md` 참조) |

### ErrorPage (`/error`, `/400`, `/403`, `/404`, `/500`)

| 항목 | 내용 |
|------|------|
| 주요 기능 | `errorType` meta 기반 오류 메시지 표시 |
| 구현 상태 | ✓ |

---

## 공통 attachable 모달

> 상세 스펙: `common/component-spec.md` §attachable 흡수 기준

| 모달 컴포넌트 | 스토어 액션 | 구현 상태 |
|-------------|-----------|---------|
| `CommentListModal.vue` | `attachableStore.openCommentList` | ✓ |
| `CommentRegistModal.vue` (= `views/journal/modals/CommentRegistModal.vue`) | `openCommentReg`, `openCommentMdf` | ✓ |
| `HistoryModal.vue` | `openHistory` (이력 카드 복사 버튼 포함) | ✓ |
| `RelatedContentAddModal.vue` | `openRelated` | ✓ |
| `FileGroupListModal.vue` | `openFileList` | ✓ |
| `FileGroupSection.vue` | 파일 업로드 섹션 | ✓ |
| `JournalTagListModal.vue` | `openTagList` | ✓ |
| `JournalTagProfileModal.vue` | `openTagProfile` | ✓ |

---

## 미구현/부분구현 요약

| 화면/기능 | 구현 상태 | 비고 |
|----------|---------|------|
| Dashboard | ❌ placeholder | 위젯 없음 |
| JournalCalendar | ❌ placeholder | `JournalDayViewToolbar` + 안내 텍스트만 |
| JournalMeta 그래프 | ❌ | 헤더·목록 조회는 구현, 시각화 없음 |
| JournalAside — CHAPTER CATEGORIES 멀티셀렉트 | ❌ | store(`chapterCtgrCds`) 존재, UI 없음 |
| JournalAside — 키워드 필터 input (어사이드) | ❌ | `JournalDayViewToolbar`에만 있음 |
| JournalAside — 고급 필터 아코디언 | ❌ | |
| JournalAside — TODO 목록 표시·삭제 | ❌ | 등록 버튼만 있음 |
| JournalEntrySearchPage — 결과 복사, TXT export | ❌ | `component-spec.md` §23-4 참조 |
| 로그 사용자별 통계 (`/admin/log/stats-user`) | ⚠ | placeholder 포함 |

---

## Pinia 스토어 목록

| 스토어 | 담당 화면/기능 |
|-------|-------------|
| `auth.ts` | 인증 상태, 로그인/로그아웃, `verifyAuth` |
| `menu.ts` | 사이드바 동적 메뉴 (`USER`/`MNGR` 모드) |
| `journal.ts` | 저널 일자 목록, 태그클라우드, 메타, 필터 상태 |
| `journalModal.ts` | 저널 모달 열기/닫기 (일자/챕터/엔트리/투두 등) |
| `journalAside.ts` | 어사이드 표시 제어 (`visible`, `show/hide`) |
| `journalAnnual.ts` | 연간 결산 목록/상세 |
| `journalThread.ts` | 스레드 목록/상세 |
| `tagContextMenu.ts` | 태그 클릭 컨텍스트 메뉴 위치·상태 |
| `attachableModal.ts` | 댓글/이력/파일/태그 공통 모달 |
| `boardPost.ts` | 게시판 목록/상세 |
| `schedule.ts` | 일정 캘린더 |
| `userMy.ts` | 내 정보 |
| `userSignup.ts` | 계정 신청 승인 |
| `adminPage.ts` | 사이트 관리 |
| `authPolicy.ts` | 인증 정책 |
| `boardGroup.ts` | 게시판 그룹 관리 |
| `codeAdmin.ts` | 코드 관리 |
| `menuAdmin.ts` | 메뉴 관리 |
| `userAdmin.ts` | 계정 관리 |
| `logAdmin.ts` | 로그 관리 |
| `chat.ts` | 채팅 (라우트 없음, `AppChat.vue` 존재) |
| `body.ts`, `config.ts`, `theme.ts` | 레이아웃 설정 |
