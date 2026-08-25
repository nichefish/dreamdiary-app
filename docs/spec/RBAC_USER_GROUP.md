# 사용자 그룹 / 권한 (RBAC) 설계

> 상태: 1차(메뉴) + 2차(관리 API permission 게이트) 탑재 (`feat(auth): add user group RBAC axis…`, branch `dev_0.25.0`)
> 화면 계약: `docs/migration/admin/screen-spec.md` (사용자 그룹 관리)
> 메뉴 노출 계약: `docs/migration/common/component-spec.md` (동적 메뉴 · `required_perm_key`)

## 1. 목적

DreamDiary에 당장 필요한 세분 권한이 아니라, **표준에 가까운 Role ≠ Group ≠ Permission 축**을 한 번 제대로 깔고, **메뉴 노출**을 첫 소비처로 증명한다.

- 제품 특수용 땜빵이 아니라 재사용 가능한 RBAC 골격
- 기존 `USER` / `MNGR` / `DEV` 시스템 롤과 **직교**
- 컨텐츠 소유권(`isCreatedBy`)과는 별축 (1차 비범위)

## 2. 레이어 모델

```text
User ──┬── System Role (USER / MNGR / DEV) ──► role_permission ──┐
       │                                                         ├──► 유효 Permission (합집합)
       └── User Group (M:N) ─────────────────► group_permission ─┘
                                                                    │
                                                                    ▼
                                                         menu.required_perm_key
                                                         (사이드바/메타 필터)
```

| 개념 | 의미 | 저장 |
|------|------|------|
| **System Role** | 전역 시스템 신분 (“관리자인가”) | `role`, `user_role` |
| **User Group** | 소속·업무 묶음 + 속성 | `user_group`, `user_group_member` |
| **Permission** | 원자 권한 키 | `permission` |
| **Grant** | 롤/그룹 → 권한 부여 | `role_permission`, `group_permission` |

### 롤 vs 그룹 (고정)

- `MNGR` / `DEV` = 시스템 관리자 신분. 사이드바 USER↔MNGR 모드 스위치의 `isMngr` 근거.
- Group = 어느 묶음에 속하고, 그 묶음에 어떤 **세부 permission**이 달렸는가.
- 롤을 그룹으로 흡수하지 않는다.

### 판정 규칙 (고정)

1. 유효 권한 = **롤에서 온 permission ∪ 소속 그룹에서 온 permission** (합집합).
2. 한 유저·다그룹 = M:N. `is_primary_yn`은 표시용이며 판정에는 합집합만 사용.
3. 비활성 그룹(`use_yn≠Y`)은 권한 전개에서 제외.
4. Spring `GrantedAuthority`에는 기존 `ROLE_*`와 함께 **permission key 그대로** 올린다 (예: `menu.admin.board`).
5. 헬퍼: `AuthInfo.hasPermission` / `AuthUtils.hasPermission`.

## 3. 스키마 · 시드

### 테이블

| 테이블 | 역할 |
|--------|------|
| `permission` | 권한 카탈로그 (`perm_key` UNIQUE) |
| `role_permission` | 시스템 롤 ↔ 권한 |
| `user_group` | 그룹 (`group_key` UNIQUE, 설명·정렬·use_yn) |
| `user_group_member` | 유저 ↔ 그룹 (물리 삭제 — UNIQUE와 소프트삭제 충돌 방지) |
| `group_permission` | 그룹 ↔ 권한 (물리 삭제) |
| `menu.required_perm_key` | 메뉴 노드 노출에 필요한 permission (NULL이면 추가 검사 없음) |

SSOT DDL: `schema/full/mariadb/schema-user-mariadb.sql`, `schema-cmm-mariadb.sql`  
필수 데이터: `data-required-rbac-mariadb.sql` (+ 메뉴 시드의 `USER_GROUP` 행)

### Permission 카탈로그 (1차)

| perm_key | 용도 |
|----------|------|
| `menu.view.user` | 사용자 사이드바/메타 |
| `menu.view.admin` | 관리자 트리 루트·폴더 |
| `menu.admin.user_account` | 계정 관리 |
| `menu.admin.auth_policy` | 인증 정책 |
| `menu.admin.user_group` | 사용자 그룹 관리 |
| `menu.admin.menu` | 메뉴 관리 |
| `menu.admin.code` | 코드 관리 |
| `menu.admin.page` | 사이트 관리 |
| `menu.admin.board` | 게시판 그룹 관리 |
| `menu.admin.log` | 로그 목록 |
| `menu.admin.log_stats` | 사용자별 로그 통계 |

### 롤 → permission (현행 동작 동치)

| 롤 | 부여 |
|----|------|
| `USER` | `menu.view.user` |
| `MNGR` | `menu.%` 전체 |
| `DEV` | `menu.%` 전체 (런타임 `ROLE_*`는 기존처럼 MNGR로 접히더라도 permission 축은 별도 유지) |

샘플 그룹(가상 픽스처): `CONTENT_EDITORS` → `menu.view.admin`, `menu.admin.board`, `menu.admin.code`.

## 4. 런타임 경로

### 로그인 · 인증 객체

1. `AuthService.loadUserByUsername` / `loadUserByEmail`
2. 롤 보강 후 `PermissionResolveService.resolvePermKeys(user)`
3. `AuthInfo.permissions` 세팅
4. `AuthInfo.getAuthorities()` = `ROLE_*` + permission keys
5. SPA 전달: `AuthUserDto.permissions` → `useAuthStore` / `packages/shared-types`

주요 코드:

- `auth/permission/service/PermissionResolveService.java`
- `auth/security/model/AuthInfo.java`
- `auth/security/util/AuthUtils.java` (`hasPermission`, `getPermissionCacheKey`)

### 메뉴 노출 (1차 소비처 · 단일 경로)

1. `GET /api/menus?mode=USER|MNGR` — 모드로 `admin_yn` 트리를 고른다 (UX 유지).
2. 로드된 트리에 `filterByRequiredPermission` 적용: 노드의 `required_perm_key`가 있으면 현재 사용자 permission 보유 시에만 남김.
3. URL 없고 자식도 없는 폴더 노드는 제거 (`BOARD` 확장 제외).
4. EhCache 키: `locale + '::' + AuthUtils.getPermissionCacheKey()` (권한 집합 지문).

`admin_yn`은 **모드별 트리 분리**에 남기고, **세부 노출 판정은 permission만** 사용한다. “실패 시 롤만 fallback” / feature-flag 이중 메뉴 경로는 두지 않는다.

### 관리 UI

| 항목 | 경로 |
|------|------|
| Vue | `/admin/user-groups` → `UserGroupAdminPage.vue` |
| API | `GET/POST /api/user/groups`, `GET/PUT/DELETE /api/user/groups/{id}` |
| 카탈로그 | `GET /api/permissions` |
| 게이트 | `@PreAuthorize("hasAuthority('menu.admin.*')")` (메뉴 `required_perm_key`와 동일 키). `GET /api/menus` 사이드바는 `@Secured(ROLE_USER, ROLE_MNGR)` 유지 |
| 레거시 진입 | `/app/user/group/page.do` → Vue redirect, `urlMapping` |

그룹 저장 시 멤버십·permission을 통째로 교체하고, 메뉴 캐시(`userMenuList` / `mngrMenuList` / meta)를 비운다.

## 5. 2차: 관리 API permission 게이트

관리 페이지/관리 REST의 `@Secured(ROLE_MNGR)` 일변도를
`@PreAuthorize("hasAuthority('menu.admin.*')")` 로 바꾸었다.
메뉴 `required_perm_key`와 **같은 키**를 쓰므로, 그룹에 부여된 permission이 메뉴·API에 동시에 반영된다.

| 영역 | permission |
|------|------------|
| 계정 / 가입 승인 | `menu.admin.user_account` |
| 인증 정책 | `menu.admin.auth_policy` |
| 사용자 그룹 | `menu.admin.user_group` |
| 메뉴 관리 CRUD | `menu.admin.menu` |
| 코드 | `menu.admin.code` |
| 사이트 관리 / 캐시 / AI 백필 | `menu.admin.page` |
| 게시판 그룹 | `menu.admin.board` |
| 로그 목록 | `menu.admin.log` |
| 로그 통계 | `menu.admin.log_stats` |

사이드바 `mode=MNGR` 허가: 서버 `AuthUtils.hasPermission(menu.view.admin)`,
SPA `canUseMngrMenuMode()` (같은 키, `isMngr` fallback).

상수: `auth.permission.PermissionKey`.

## 6. 의도적 비범위 (이후)

- 저널/게시판/일정 등 **일반 기능** API의 `@Secured(ROLE_USER, ROLE_MNGR)` 전면 치환
- 그룹 소유 컨텐츠 · 공유 저널
- ABAC, 조직 트리, 위임/승인 워크플로
- SPA 라우터에 화면별 permission 가드 전면화 (서버가 SSOT)

## 7. 수렴 방향

1. 새 세분화는 **롤 키를 늘리지 말고** permission + (필요 시) group grant로만 추가한다.
2. ~~관리 API permission 게이트~~ — 2차 완료. MNGR은 시드상 전 permission을 가지므로 기존 동작과 동치.
3. `admin_yn`은 모드별 트리 UX에 사용하고 세부 노출 판정은 permission으로 수행한다.
4. `auth_policy` 테이블/화면은 **로그인·세션·비밀번호 정책**이며 RBAC과 이름만 비슷한 별 도메인이다.

## 8. 운영 · 적용

- DB 구조 기준선: full schema의 `schema-user-mariadb.sql`, `schema-cmm-mariadb.sql`.
- 필수 데이터 기준선: `data-required-rbac-mariadb.sql`과 `data-required-menu-mariadb.sql`.
- 개발 DB는 위 기준선의 DDL과 필수 데이터를 수동으로 동기화한다.
- 메뉴 캐시는 그룹/권한 변경 시 서비스가 비우지만, 운영 중 이상 시 `userMenuList` / `mngrMenuList` / meta 캐시를 함께 확인한다.

## 9. 관련 파일 맵

| 영역 | 경로 |
|------|------|
| Permission 도메인 | `app/backend/.../auth/permission/` |
| User Group 도메인 | `app/backend/.../feature/user/group/` |
| 메뉴 필터 | `feature/admin/menu/service/MenuService.java` |
| Vue 관리 | `app/frontend-vue/src/features/admin/UserGroupAdminPage.vue`, `stores/userGroup.ts` |
| 시드 | `data-required-rbac-mariadb.sql`, `data-required-menu-mariadb.sql` (`USER_GROUP`) |
