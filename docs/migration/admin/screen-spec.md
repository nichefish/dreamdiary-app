# 관리자 화면 스펙 (Admin Screen Spec)

> 라우트: `app/frontend-vue/src/app/router/index.ts`
> 전체 라우트 목록: `docs/migration/vue-screen-overview.md`
> 모든 관리자 화면은 `DefaultLayout` 하위, `MNGR` 권한 필요.
> 관리자 화면 본문 상단은 breadcrumb와 중복되는 화면 제목·설명문을 렌더링하지 않는다. 화면 설명은 `menu.menu_description`을 SSOT로 삼아 공통 breadcrumb 하단에 표시하고, 본문 상단에는 필요한 액션 버튼만 표시한다.

## 라우트·화면 매핑

| 화면 | Vue route | Vue view | 구현 |
|------|-----------|----------|------|
| 사이트 관리 | `/admin` | `AdminPage.vue` | ✓ |
| 인증 정책 관리 | `/admin/auth-policy` | `AuthPolicyPage.vue` | ✓ |
| 게시판 그룹 관리 | `/admin/board-group` | `BoardGroupAdminPage.vue` | ✓ |
| 코드 관리 | `/admin/code` | `CodeAdminPage.vue` | ✓ |
| 메뉴 관리 | `/admin/menu` | `MenuAdminPage.vue` | ✓ |
| 계정 관리 | `/admin/users` | `UserAdminPage.vue` | ✓ |
| 로그 관측 | `/admin/log` | `LogAdminPage.vue` | ✓ |
| 사용자별 로그 통계 | `/admin/log/stats-user` | `LogAdminPage.vue` (route로 분기) | ⚠ placeholder |
| 계정 신청 승인 관리 | `/user/signup/approval` | `UserSignupApprovalList.vue` | ✓ |

---

## 사이트 관리 (`admin-page`)

**Vue view**: `app/frontend-vue/src/features/admin/AdminPage.vue`  
**스토어**: `features/admin/stores/adminPage.ts` / **타입**: `features/admin/types/adminPage.types.ts`

**기능**:
- 사이트 관리 본문 상단에는 `일반 관리` / `AI 관리` 탭을 표시한다.
  - 기본 진입(`/admin`)은 `일반 관리` 탭이다.
  - `AI 관리` 탭은 `/admin?tab=ai`, `일반 관리` 탭은 `/admin?tab=general` query로 상태를 유지한다.
- 본문 상단 새로고침 버튼 표시. 화면 설명은 `ADMIN_PAGE` 메뉴의 `menuDescription`으로 breadcrumb 하단에 표시
- `일반 관리` 탭:
  - 운영 도구 모음 (캐시 관리, 공휴일 동기화, Notion 요청)
  - 권한 정보 표시
  - 일반 관리 탭의 새로고침 버튼은 bootstrap 데이터(권한/meta)를 다시 조회한다.
- 캐시 목록 조회 → `GET /api/cache/cache-active-map`
- 캐시 초기화 → `POST /api/cache/cache-evict` / `POST /api/cache-clear`
- `AI 관리` 탭:
  - AI Embedding Backfill → `GET /api/admin/journal-entry-embeddings/stats`, `POST /api/admin/journal-entry-embeddings/sync`, `POST /api/admin/journal-entry-embeddings/requeue-failed`
  - Entity Queue Backfill → `GET /api/admin/journal-entry-entities/stats`, `POST /api/admin/journal-entry-entities/sync`, `POST /api/admin/journal-entry-entities/requeue-failed`
  - `GET /api/admin/ollama/health` 상태를 AI 관리 탭의 Embedding 섹션 상단에 표시한다.
  - AI 관리 탭의 새로고침 버튼은 embedding/entity stats를 다시 조회한다.
  - AI 관리 탭은 AI Embedding Backfill / Entity Queue Backfill 카드를 5:5 컬럼으로 배치한다.
  - 서버 기동 시 `app.journal.embedding.sync-on-startup`(기본 `true`)이면 Admin Sync Entries와 동일한 embedding queue sync job을 자동 enqueue (`DreamdiaryInitializer`)
  - `total` = active journal entry count (Entries baseline)
  - `queueRows` / `unqueuedEntries` = queue table row count / entries not yet queued
  - progress bars use entry coverage (`embedded/total`, `synced/total`)
- Embedding/Entity 백필은 서버 스케줄러·워커에서 비동기 처리한다. Admin 화면을 떠나도 작업은 계속된다.
  - `syncRunning` / pending / processing 중이면 탭과 무관하게 상단 백그라운드 안내 배너 표시
  - "페이지를 떠나도 됨" 안내는 중복 노출하지 않고 상단 백그라운드 안내 배너에만 표시
  - Sync 성공 alert에 백그라운드 처리 문구 포함
  - Entity 섹션에 pending/processing 시 worker 상태 패널 표시 (Embedding과 동일 패턴)
  - `adminPage` store가 backfill 활성 시 5초 폴링을 유지하며, AdminPage unmount 후에도 SPA 내 다른 화면으로 이동해도 stats 갱신이 계속된다

---

## 인증 정책 관리 (`auth-policy`)

**Vue view**: `app/frontend-vue/src/features/admin/AuthPolicyPage.vue`  
**스토어**: `features/admin/stores/authPolicy.ts`

**기능**:
- 본문 상단 새로고침 버튼 표시. 화면 설명은 `AUTH_POLICY` 메뉴의 `menuDescription`으로 breadcrumb 하단에 표시
- 인증 정책 단건 조회/수정 (싱글톤)
- `sessionTimeoutMinutes`: 사용자 체감 로그인 유지 시간(분). `HttpSession#setMaxInactiveInterval`, JWT access token `exp`, JWT 쿠키 max-age, JWT 검증의 `issuedAt + policyTimeout` 기준에 함께 적용
- IP 허용 정책, 허용 IP 목록 CRUD
- `GET /api/auth/policy` → 현재 정책 조회
- `PUT /api/auth/policy` → 정책 수정

---

## 게시판 그룹 관리 (`board-group-admin`)

**Vue view**: `app/frontend-vue/src/features/admin/BoardGroupAdminPage.vue`  
**스토어**: `features/admin/stores/boardGroup.ts`

**기능**:
- 본문 상단 새로고침/등록 버튼 표시. 화면 설명은 `BOARD_ADMIN` 메뉴의 `menuDescription`으로 breadcrumb 하단에 표시
- 게시판 그룹 목록/등록/수정/삭제
- 사용/미사용 토글
- 드래그로 정렬 순서 변경
- API: `GET/POST /api/board/groups`, `POST/DELETE /api/board/groups/{id}`, `POST .../use|unuse`, `PUT .../sort-orders`

---

## 코드 관리 (`code-admin`)

**Vue view**: `app/frontend-vue/src/features/admin/CodeAdminPage.vue`  
**스토어**: `features/admin/stores/codeAdmin.ts`

**기능**:
- 본문 상단 새로고침/분류 코드 등록 버튼 표시. 화면 설명은 `CODE_ADMIN` 메뉴의 `menuDescription`으로 breadcrumb 하단에 표시
- 분류 코드(code_group) 목록/등록/수정/삭제
- 분류 코드별 상세 코드(code_item) 목록/등록/삭제
- 상세 코드 정렬 순서 변경
- API: `GET/POST /api/code/groups`, `GET/PATCH/DELETE /api/code/group/{id}`
- API: `GET/POST /api/code/items`, `GET/DELETE /api/code/item`, `PUT /api/code/items/sort-orders`

---

## 메뉴 관리 (`menu-admin`)

**Vue view**: `app/frontend-vue/src/features/admin/MenuAdminPage.vue`  
**관련 컴포넌트**: `MenuAdminTreeNode.vue`  
**스토어**: `features/admin/stores/menuAdmin.ts`

**기능**:
- 본문 상단 새로고침 버튼 표시. 화면 설명은 `MENU_ADMIN` 메뉴의 `menuDescription`으로 breadcrumb 하단에 표시
- 메뉴 트리 2컬럼 구조 (사용자 메뉴 / 관리자 메뉴)
- 메뉴 등록/수정/삭제
- 하위 메뉴 추가, 사용 여부 토글
- 드래그로 순서 변경
- 신규 메뉴는 기존 트리의 하위 메뉴로만 등록한다. 메인 메뉴 등록 버튼과 메뉴 유형 입력은 제공하지 않으며, 백엔드는 신규 등록을 `SUB`로 고정하고 상위 메뉴를 필수 검증한다. 신규 `MAIN`은 시드/마이그레이션으로만 추가한다.
- 관리자/사용자 메뉴 여부는 최상위 `MAIN` 메뉴의 `adminYn`으로만 판정한다. 개별 메뉴 등록/수정 폼은 `adminYn`을 직접 토글하거나 저장하지 않는다.
- 하위 메뉴를 다른 최상위 메뉴 계열로 이동하면 백엔드는 최상위 메뉴 기준 관리자/사용자 판정 캐시(`isMngrMenu`)를 무효화한다.
- 메뉴 등록/수정 모달은 사용 여부 토글과 상위 메뉴 읽기 전용 필드를 입력 높이 기준으로 세로 정렬한다. `메뉴명`과 필수 `메뉴 라벨`은 기본 정보 row 안에서 병렬 입력으로 배치한다. `URL`은 `submenuExpandType=NO_SUB`일 때만 표시하고 필수로 받으며, 그 외 확장형 메뉴는 저장 시 URL을 빈 값으로 정규화한다. `미열람 카운트`는 스위치와 조건부 입력을 한 줄로 배치하며, 스위치 ON이면 카운트 이름 입력을 필수로 받는다.
- 메뉴 등록/수정 모달은 선택 메뉴의 `menuDescription`을 `메뉴 설명` textarea로 관리한다. 저장된 설명은 sidebar 메뉴 API 응답에 포함되고, 현재 route에 매칭된 메뉴의 설명이 공통 breadcrumb 하단에 표시된다. 설명이 비어 있으면 breadcrumb 하단 설명 영역은 렌더링하지 않는다.
- `protectedYn=Y`는 메뉴 자체의 수정·사용여부 변경·삭제와 자기 자신 드래그 이동을 막는 시스템 보호 의미다. 보호 메뉴는 drag source와 sibling drop target이 되지 않지만, 보호 메뉴 아래에도 하위 메뉴를 추가하거나 다른 하위 메뉴를 이동할 수 있다.
- `managementType=BOARD`는 게시판 관리가 내용을 소유하는 메뉴다. 메뉴 관리에서는 행 컨텍스트 메뉴를 숨기고 행 우측 액션 영역에 넓은 `게시판 관리로 이동` 링크를 표시한다. BOARD 메뉴 자체는 drag source가 될 수 있지만 하위 메뉴 생성 대상이나 sibling drop target은 되지 않는다.
- 메뉴 트리 행의 액션은 `...` 컨텍스트 메뉴로 제공한다. 메뉴 항목은 하위 메뉴 추가, 수정, 사용/미사용 전환, 삭제이며 각 항목은 시스템 보호 상태에 따라 비활성화한다.
- `submenuExpandType=NO_SUB`는 해당 메뉴가 하위 메뉴를 생성하거나 이동받을 수 없는 leaf 메뉴임을 의미한다. `submenuExpandType=BOARD`도 게시판 관리 소유 메뉴이므로 하위 메뉴 추가 액션과 상위 메뉴 후보, 트리 이동 대상 부모에서 제외한다.
- 메뉴 트리의 보호 아이콘은 Bootstrap tooltip을 `v-tooltip` directive로 활성화한다. `protectedYn=Y` 메뉴는 `bi-shield-lock text-warning`와 `시스템 보호 메뉴`로 표시하며 아이콘 hover cursor는 도움말(`help`)이다.
- 하위 메뉴 등록/수정 모달의 상위 메뉴는 호출한 부모 또는 기존 부모를 읽기 전용으로 표시한다. 상위 메뉴 변경은 트리 드래그 앤 드롭과 `PUT /api/menus/tree` 전용이며, 일반 수정 API는 `parentMenuId` 변경 요청을 거부한다.
- API: `GET /api/menus`, `POST/GET/PATCH/DELETE /api/menu(s)/{id}`, `PUT /api/menus/sort-orders`, `PUT /api/menus/tree`

---

## 계정 관리 (`user-admin`)

**Vue view**: `app/frontend-vue/src/features/admin/UserAdminPage.vue`  
**스토어**: `features/admin/stores/userAdmin.ts`

**기능**:
- 본문 상단 새로고침/계정 등록 버튼 표시. 화면 설명은 `USER_ACCOUNT` 메뉴의 `menuDescription`으로 breadcrumb 하단에 표시
- 계정 목록 조회/검색/권한 필터
- 계정 상세/등록/수정 (프로필·고용정보 서브폼 포함)
- 계정 삭제 (본인 계정 삭제 불가)
- 비밀번호 초기화
- 중복 체크 (아이디/이메일)
- 엑셀 다운로드
- API: `GET/POST /api/users`, `GET/POST/DELETE /api/users/{id}`, `POST .../password-reset`, `GET .../xlsx-download`

---

## 로그 관측 / 사용자별 로그 통계 (`log-list`, `log-stats-user`)

**Vue view**: `app/frontend-vue/src/features/admin/LogAdminPage.vue` (route로 뷰 분기)  
**스토어**: `features/admin/stores/logAdmin.ts`

**기능**:
- 본문 상단 목록/통계 전환 버튼 표시. 화면 설명은 메뉴의 `menuDescription`으로 breadcrumb 하단에 표시
- 운영 로그 목록/검색/상세 모달
- `/admin/log` → 전체 로그 관측 뷰 (`isStatsView = false`)
- `/admin/log/stats-user` → 사용자별 통계 뷰 (`isStatsView = true`, ⚠ placeholder)
- API: `GET /api/logs`, `GET /api/logs/{id}`

---

## 계정 신청 승인 관리 (`user-signup-approval`)

**Vue view**: `app/frontend-vue/src/features/user/signup/UserSignupApprovalList.vue`  
**스토어**: `features/user/stores/userSignup.ts`

**기능**:
- breadcrumb와 중복되는 본문 상단 제목은 표시하지 않는다.
- 승인 대기 신청 목록 조회
- 최근 신청 내역 (최근 30건)
- 신청 승인 → `POST /api/user/signup-requests/{id}/approval`
- 신청 반려 → `POST /api/user/signup-requests/{id}/rejection`
