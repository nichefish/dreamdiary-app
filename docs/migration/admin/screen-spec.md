# 관리자 화면 스펙 (Admin Screen Spec)

> 라우트: `app/frontend-vue/src/router/index.ts`
> 전체 라우트 목록: `docs/migration/vue-screen-overview.md`
> 모든 관리자 화면은 `DefaultLayout` 하위, `MNGR` 권한 필요.

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

**Vue view**: `app/frontend-vue/src/views/admin/AdminPage.vue`  
**스토어**: `stores/adminPage.ts`

**기능**:
- 운영 도구 모음 (캐시 관리, 임베딩 백필 등)
- 캐시 목록 조회 → `GET /api/cache/cache-active-map`
- 캐시 초기화 → `POST /api/cache/cache-evict` / `POST /api/cache-clear`
- AI Embedding Backfill → `GET /api/admin/journal-entry-embeddings/stats`, `POST /api/admin/journal-entry-embeddings/sync`
- 권한 정보 표시

---

## 인증 정책 관리 (`auth-policy`)

**Vue view**: `app/frontend-vue/src/views/admin/AuthPolicyPage.vue`  
**스토어**: `stores/authPolicy.ts`

**기능**:
- 인증 정책 단건 조회/수정 (싱글톤)
- IP 허용 정책, 허용 IP 목록 CRUD
- `GET /api/auth/policy` → 현재 정책 조회
- `PUT /api/auth/policy` → 정책 수정

---

## 게시판 그룹 관리 (`board-group-admin`)

**Vue view**: `app/frontend-vue/src/views/admin/BoardGroupAdminPage.vue`  
**스토어**: `stores/boardGroup.ts`

**기능**:
- 게시판 그룹 목록/등록/수정/삭제
- 사용/미사용 토글
- 드래그로 정렬 순서 변경
- API: `GET/POST /api/board/groups`, `POST/DELETE /api/board/groups/{id}`, `POST .../use|unuse`, `PUT .../sort-orders`

---

## 코드 관리 (`code-admin`)

**Vue view**: `app/frontend-vue/src/views/admin/CodeAdminPage.vue`  
**스토어**: `stores/codeAdmin.ts`

**기능**:
- 분류 코드(code_group) 목록/등록/수정/삭제
- 분류 코드별 상세 코드(code_item) 목록/등록/삭제
- 상세 코드 정렬 순서 변경
- API: `GET/POST /api/code/groups`, `GET/PATCH/DELETE /api/code/group/{id}`
- API: `GET/POST /api/code/items`, `GET/DELETE /api/code/item`, `PUT /api/code/items/sort-orders`

---

## 메뉴 관리 (`menu-admin`)

**Vue view**: `app/frontend-vue/src/views/admin/MenuAdminPage.vue`  
**관련 컴포넌트**: `MenuAdminTreeNode.vue`  
**스토어**: `stores/menuAdmin.ts`

**기능**:
- 메뉴 트리 2컬럼 구조 (사용자 메뉴 / 관리자 메뉴)
- 메뉴 등록/수정/삭제
- 하위 메뉴 추가, 사용 여부 토글
- 드래그로 순서 변경
- API: `GET /api/menus`, `POST/GET/PATCH/DELETE /api/menu(s)/{id}`, `PUT /api/menus/sort-orders`, `PUT /api/menus/tree`

---

## 계정 관리 (`user-admin`)

**Vue view**: `app/frontend-vue/src/views/admin/UserAdminPage.vue`  
**스토어**: `stores/userAdmin.ts`

**기능**:
- 계정 목록 조회/검색/권한 필터
- 계정 상세/등록/수정 (프로필·고용정보 서브폼 포함)
- 계정 삭제 (본인 계정 삭제 불가)
- 비밀번호 초기화
- 중복 체크 (아이디/이메일)
- 엑셀 다운로드
- API: `GET/POST /api/users`, `GET/POST/DELETE /api/users/{id}`, `POST .../password-reset`, `GET .../xlsx-download`

---

## 로그 관측 / 사용자별 로그 통계 (`log-list`, `log-stats-user`)

**Vue view**: `app/frontend-vue/src/views/admin/LogAdminPage.vue` (route로 뷰 분기)  
**스토어**: `stores/logAdmin.ts`

**기능**:
- 운영 로그 목록/검색/상세 모달
- `/admin/log` → 전체 로그 관측 뷰 (`isStatsView = false`)
- `/admin/log/stats-user` → 사용자별 통계 뷰 (`isStatsView = true`, ⚠ placeholder)
- API: `GET /api/logs`, `GET /api/logs/{id}`

---

## 계정 신청 승인 관리 (`user-signup-approval`)

**Vue view**: `app/frontend-vue/src/views/user/signup/UserSignupApprovalList.vue`  
**스토어**: `stores/userSignup.ts`

**기능**:
- 승인 대기 신청 목록 조회
- 최근 신청 내역 (최근 30건)
- 신청 승인 → `POST /api/user/signup-requests/{id}/approval`
- 신청 반려 → `POST /api/user/signup-requests/{id}/rejection`
