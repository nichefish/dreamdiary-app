# 관리자 화면 스펙 (Admin Screen Spec)

> 라우트: `app/frontend-vue/src/app/router/index.ts`
> 전체 라우트 목록: `docs/migration/vue-screen-overview.md`
> 모든 관리자 화면은 `DefaultLayout` 하위, `MNGR` 권한 필요.
> 관리자 화면 본문 상단은 breadcrumb와 중복되는 화면 제목·설명문을 렌더링하지 않는다. 화면 설명은 `menu.menu_description`을 SSOT로 삼아 공통 breadcrumb 하단에 표시하고, 본문 상단에는 필요한 액션 버튼만 표시한다.
> 관리자 화면의 API 결과 메시지는 서버 `message`를 우선 표시하고, 서버 메시지가 없을 때는 현재 locale의 클라이언트 카탈로그 메시지를 fallback으로 표시한다. 메뉴 확장 방식 선택지도 현재 locale 변경에 반응해 갱신한다.

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
| 사용자별 로그 통계 | `/admin/log/stats-user` | `LogAdminPage.vue` (route로 분기) | ✓ |
| 계정 신청 승인 관리 | `/user/signup/approval` | `UserSignupApprovalList.vue` | ✓ |

---

## 사이트 관리 (`admin-page`)

**Vue view**: `app/frontend-vue/src/features/admin/AdminPage.vue`  
**스토어**: `features/admin/stores/adminPage.ts` / **타입**: `features/admin/types/adminPage.types.ts`

**기능**:
- 사이트 관리 본문 상단에는 `일반 관리` / `AI 관리` 탭을 표시한다.
  - 탭 마크업·여백은 저널 일자 뷰 툴바(`JournalDayViewToolbar`)와 동일 골격이다: `admin-view-toolbar` 래퍼 + `nav nav-tabs nav-tabs-line ps-5 mt-5 mb-0`(헤더와의 상단 간격 `mt-5`).
  - 기본 진입(`/admin`)은 `일반 관리` 탭이다.
  - `AI 관리` 탭은 `/admin?tab=ai`, `일반 관리` 탭은 `/admin?tab=general` query로 상태를 유지한다.
- 화면 설명은 `ADMIN_PAGE` 메뉴의 `menuDescription`으로 breadcrumb 하단에 표시한다. 본문 상단 전용 새로고침 버튼은 두지 않는다(브라우저 새로고침 또는 진입·탭 전환 시 자동 조회).
- `일반 관리` 탭:
  - 운영 도구 모음 (캐시 관리, 공휴일 동기화, Notion 요청)
  - 권한 정보 표시
  - 탭 진입·전환 시 bootstrap 데이터(권한/meta)를 조회한다.
- 캐시 목록 조회 → `GET /api/cache/cache-active-map`
- 캐시 초기화 → `POST /api/cache/cache-evict` / `POST /api/cache-clear`
- `AI 관리` 탭:
  - AI Embedding Backfill → `GET /api/admin/journal-entry-embeddings/stats`, `POST /api/admin/journal-entry-embeddings/sync`, `POST /api/admin/journal-entry-embeddings/requeue-failed`
  - Chat RAG settings card → `GET`/`PATCH /admin/chat/settings` (`rag_enabled`, `rag_top_k`, `rag_min_score`, `rag_summary_top_k`, `rag_synthesis_top_k`, `rag_stance_top_k`, `rag_synthesis_min_score`); under Ollama health on AI tab.
  - Entity Queue Backfill → `GET /api/admin/journal-entry-entities/stats`, `POST /api/admin/journal-entry-entities/sync`, `POST /api/admin/journal-entry-entities/requeue-failed`
  - `GET /api/admin/ollama/health` 상태를 AI 관리 탭의 Embedding 섹션 상단에 표시한다.
  - 탭 진입·전환 시 embedding/entity stats·Ollama health·RAG settings를 조회한다. Embedding/Entity 카드 안의 Refresh는 해당 섹션만 재조회한다.
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
- 화면 설명은 `AUTH_POLICY` 메뉴의 `menuDescription`으로 breadcrumb 하단에 표시한다. 본문 상단 전용 새로고침 버튼은 두지 않는다.
- 인증 정책 단건 조회/수정 (싱글톤)
- `sessionTimeoutMinutes`: 사용자 체감 로그인 유지 시간(분). `HttpSession#setMaxInactiveInterval`, JWT access token `exp`, JWT 쿠키 max-age, JWT 검증의 `issuedAt + policyTimeout` 기준에 함께 적용
- `passwordHistoryCount`: 최근 비밀번호 재사용 제한 개수. 기본값은 `2`이며, `0`이면 현재 비밀번호/이전 비밀번호 이력 재사용 검사를 수행하지 않는다.
- IP 허용 정책, 허용 IP 목록 CRUD
- `GET /api/auth/policy` → 현재 정책 조회
- `PUT /api/auth/policy` → 정책 수정

---

## 게시판 그룹 관리 (`board-group-admin`)

**Vue view**: `app/frontend-vue/src/features/admin/BoardGroupAdminPage.vue`  
**스토어**: `features/admin/stores/boardGroup.ts`

**기능**:
- 등록은 저널 스레드·게시판·코드/계정 관리와 동형인 뷰 툴바(`board-group-view-toolbar`, `pe-5 mt-3 mb-1`)에 둔다. ASIDE·탭용 `mt-5` 빈 여백은 없다. 목록 카드는 `margin-top: 0`으로 툴바에 붙인다. 화면 설명은 `BOARD_ADMIN` 메뉴의 `menuDescription`으로 breadcrumb 하단에 표시한다. 본문 상단 전용 새로고침 버튼은 두지 않는다.
- 목록 관리 열은 메뉴 관리와 동일하게 Bootstrap `⋯` 드롭다운(`data-bs-toggle="dropdown"`, `strategy:fixed`)으로 수정·삭제를 제공한다(변경 전: 아이콘 버튼 → Metronic `data-kt-menu`는 `.table-responsive` overflow에 잘려 미표시). Metronic 재바인딩은 필요 없다.
- 게시판 그룹 목록/등록/수정/삭제
- 사용/미사용 토글
- 드래그로 정렬 순서 변경
- API: `GET/POST /api/board/groups`, `POST/DELETE /api/board/groups/{id}`, `POST .../use|unuse`, `PUT .../sort-orders`

---

## 코드 관리 (`code-admin`)

**Vue view**: `app/frontend-vue/src/features/admin/CodeAdminPage.vue`  
**스토어**: `features/admin/stores/codeAdmin.ts`

**기능**:
- 분류 코드 등록은 저널 스레드·게시판과 동형인 뷰 툴바(`code-admin-view-toolbar`, `pe-5 mt-3 mb-1`)에 둔다. ASIDE·탭용 `mt-5` 빈 여백은 없다. 목록 카드는 `margin-top: 0`으로 툴바에 붙인다. 화면 설명은 `CODE_ADMIN` 메뉴의 `menuDescription`으로 breadcrumb 하단에 표시한다. 본문 상단 전용 새로고침 버튼은 두지 않는다.
- 분류 목록·상세 아이템 관리 열은 메뉴 관리와 동일하게 Bootstrap `⋯` 드롭다운(`data-bs-toggle="dropdown"`, `strategy:fixed`)으로 수정·삭제를 제공한다(변경 전: 아이콘 버튼 → Metronic `data-kt-menu`는 `.table-responsive` overflow에 잘려 미표시). 메뉴 클릭은 행의 상세/아이템 수정 이동으로 전파하지 않으며 Metronic 재바인딩은 필요 없다.
- 분류 코드(code_group) 목록/등록/수정/삭제
- 분류 코드별 상세 코드(code_item) 목록/등록/삭제
- 상세 코드 정렬 순서 변경
- 상세 코드 등록/수정 시 다국어 번역명을 `로케일 select + 번역명` 행으로 관리한다. `+` 버튼으로 행을 추가하고 `-` 버튼으로 제거하며, 저장 시 남아 있는 행 전체가 해당 코드의 번역을 교체한다(제거한 행의 번역은 삭제된다). 레이블·placeholder·안내는 현재 locale의 클라이언트 카탈로그를 사용하고, 번역이 없는 locale 은 한국어 `codeName` 을 fallback 으로 사용한다.
  - **변경 전**: 영문 전용 단일 입력(`codeNameEn`)이었고, 서버는 `locale='en'` 한 건만 저장했다. `saveI18n()`이 기존 번역을 전부 지운 뒤 en 만 다시 써서, en 이외 locale 행은 저장할 때마다 유실됐다. **변경 후**: DTO 계약이 `i18nNames: Map<locale, 번역명>`으로 일반화되어 어떤 locale 도 보존된다. 읽기 모델 `CodeLookupItem.i18nNames` 와 같은 모양이다.
  - 로케일 선택지는 `SUPPORTED_LOCALES`(`shared/i18n/stores/locale.ts`)에서 기준 로케일 `ko` 를 제외한 값이다. 지원 로케일을 추가하면 코드 수정 없이 선택지에 반영된다. 현재는 `ko`,`en` 만 지원하므로 선택 가능한 값은 `en` 뿐이다.
  - `ko` 는 `code_item.code_name` 이 단일 원천이므로 `code_item_i18n` 과 번역명 목록에서 제외한다. 한국어는 `코드명` 필드가 담당한다. 서버는 `ko` 가 들어와도 저장하지 않고 경고 로그를 남긴다.
  - 같은 locale 행은 중복될 수 없다(`code_item_i18n` 복합 PK). 이미 사용 중인 locale 은 다른 행의 select 선택지에서 제외하고, 남은 로케일이 없으면 `+` 버튼을 비활성화한다.
  - 폼 전송은 기존 `FormData`(@ModelAttribute 바인딩)를 유지하며 `i18nNames[<locale>]=<번역명>` 형식을 사용한다.
- 다국어 번역은 `code_item_i18n` 테이블(code_item_id, locale, code_name 복합PK)에 저장. 현재 `en` 언어만 지원.
- API: `GET/POST /api/code/groups`, `GET/PATCH/DELETE /api/code/group/{id}`
- API: `GET/POST /api/code/items`, `GET/DELETE /api/code/item`, `PUT /api/code/items/sort-orders`

---

## 메뉴 관리 (`menu-admin`)

**Vue view**: `app/frontend-vue/src/features/admin/MenuAdminPage.vue`  
**관련 컴포넌트**: `MenuAdminTreeNode.vue`  
**스토어**: `features/admin/stores/menuAdmin.ts`

**기능**:
- 화면 설명은 `MENU_ADMIN` 메뉴의 `menuDescription`으로 breadcrumb 하단에 표시한다. 본문 상단 전용 새로고침 버튼은 두지 않는다. 본문 보드(`menu-admin-board`)는 저널 일자 뷰 툴바·사이트 관리 탭과 동일하게 상단 `mt-5` 여백을 둔다.
- 메뉴 트리 2컬럼 구조 (사용자 메뉴 / 관리자 메뉴)를 유지하고, `sidebarVisibleYn=N` 메뉴는 하단 `숨김·시스템 메뉴` 섹션으로 분리 표시한다.
- 메뉴 등록/수정/삭제
- 하위 메뉴 추가, 사용 여부 토글
- 드래그로 순서 변경
- 신규 메뉴는 기존 트리의 하위 메뉴로만 등록한다. 메인 메뉴 등록 버튼과 메뉴 유형 입력은 제공하지 않으며, 백엔드는 신규 등록을 `SUB`로 고정하고 상위 메뉴를 필수 검증한다. 신규 `MAIN`은 시드/마이그레이션으로만 추가한다.
- 관리자/사용자 메뉴 여부는 최상위 `MAIN` 메뉴의 `adminYn`으로만 판정한다. 개별 메뉴 등록/수정 폼은 `adminYn`을 직접 토글하거나 저장하지 않는다.
- 하위 메뉴를 다른 최상위 메뉴 계열로 이동하면 백엔드는 최상위 메뉴 기준 관리자/사용자 판정 캐시(`isMngrMenu`)를 무효화한다.
- 메뉴 등록/수정 모달은 사용 여부와 사이드바 표시 여부 토글, 상위 메뉴 읽기 전용 필드를 입력 높이 기준으로 세로 정렬한다. `sidebarVisibleYn=N`은 메뉴 테이블과 breadcrumb/권한/화면 메타 원천에는 남기되 사이드바 트리 렌더링에서 제외한다. `메뉴명`과 필수 `메뉴 라벨`은 기본 정보 row 안에서 병렬 입력으로 배치한다. `URL`은 `submenuExpandType=NO_SUB`일 때만 표시하고 필수로 받으며, 그 외 확장형 메뉴는 저장 시 URL을 빈 값으로 정규화한다. `미열람 카운트`는 스위치와 조건부 입력을 한 줄로 배치하며, 스위치 ON이면 카운트 이름 입력을 필수로 받는다.
- 메뉴 등록/수정 모달은 선택 메뉴의 `menuDescription`을 `메뉴 설명` textarea로 관리한다. 저장된 설명은 sidebar 메뉴 API 응답에 포함되고, 현재 route에 매칭된 메뉴의 설명이 공통 breadcrumb 하단에 표시된다. 설명이 비어 있으면 breadcrumb 하단 설명 영역은 렌더링하지 않는다.
- `protectedYn=Y`는 메뉴 자체의 수정·사용여부 변경·삭제와 자기 자신 드래그 이동을 막는 시스템 보호 의미다. 보호 메뉴는 drag source와 sibling drop target이 되지 않지만, 보호 메뉴 아래에도 하위 메뉴를 추가하거나 다른 하위 메뉴를 이동할 수 있다.
- **BOARD 확장 메뉴의 사이드바 하위 항목**: `submenuExpandType=BOARD` 메뉴(`일반게시판`)는 하위 메뉴를 menu 테이블에 두지 않는다. 게시판은 `board` 테이블이 소유하므로, 서버가 사이드바 조회 시 사용중(`useYn=Y`) 게시판을 `sortOrder` 순으로 `subMenuList` 에 주입한다(`MenuService.attachBoardSubMenus`). 각 게시판은 `/app/board/post/list.do?contentType=<boardKey>` URL 을 가진 `NO_SUB` 메뉴로 변환하며, 프론트 `urlMapping` 이 이를 `/board/<boardKey>` 라우트로 흡수한다. 변경 전에는 주입이 없어 BOARD 메뉴가 자식도 URL 도 없는 빈 메뉴였고, 게시판을 등록해도 사용자 화면에 나타나지 않았다(프론트는 `subMenuList` 가 있어야 아코디언으로 펼친다). BOARD 메뉴 자체는 링크 없이 아코디언 역할만 한다.
  - 캐시: 사이드바 메뉴 캐시(`userMenuList`/`userMenuMetaList`/`mngrMenuList`/`mngrMenuMetaList`)에 주입 결과가 함께 담긴다. 게시판 등록·수정·삭제·사용여부·정렬 변경 시 `BoardService` 후처리에서 이 캐시들도 무효화해야 새 게시판이 반영된다.
- `managementType=BOARD`는 게시판 관리가 내용을 소유하는 메뉴다. `MenuDto.getManagementType()`이 `submenuExpandType=BOARD` 여부로 파생하는 값이며 DB 컬럼이 아니다. 메뉴 관리에서는 행 우측 액션 영역에 넓은 `게시판 관리로 이동` 링크를 표시하고, **`...` 컨텍스트 메뉴도 함께 표시**한다(변경 전: 컨텍스트 메뉴를 숨기고 링크만 표시). BOARD 메뉴도 수정·사용여부 전환·삭제가 가능하며, 하위 메뉴 추가만 노출하지 않는다(백엔드 `MenuService`가 BOARD 부모 아래 하위 메뉴 등록을 거부). BOARD 메뉴 자체는 drag source가 될 수 있지만 하위 메뉴 생성 대상이나 sibling drop target은 되지 않는다.
- 메뉴 트리 행의 액션은 `...` 컨텍스트 메뉴로 제공한다. 메뉴 항목은 하위 메뉴 추가, 수정, 사용/미사용 전환, 삭제이며 각 항목은 시스템 보호 상태에 따라 비활성화한다.
- 메뉴 등록·수정 폼의 URL 레이블은 현재 locale의 `common.technical.url` 카탈로그를 사용하며 URL 입력·검증 계약은 유지한다.
- **메뉴명·설명 다국어**: 사이드바 메뉴명(`menuName`)과 설명(`menuDescription`)은 `menu_i18n (menu_id, locale)` 테이블에 locale 별 번역을 둔다. 한국어(ko)는 `menu.menu_name`/`menu.menu_description` 이 단일 원천이라 `menu_i18n` 에 저장하지 않는다. 조회 시 요청 locale(`Accept-Language` → `AcceptHeaderLocaleResolver`)의 번역이 있으면 그 값으로, 없으면 기본값으로 fallback 한다.
  - 사이드바 조회(`getUserMenuList`/`getUserMenuMetaList`/`getMngrMenuList`/`getMngrMenuMetaList`)는 트리를 재귀 지역화하며, `@Cacheable` 캐시 key 에 locale(`LocaleContextHolder.getLocale().getLanguage()`)을 포함해 언어별로 분리 캐시한다(변경 전: locale 무관 단일 엔트리라 먼저 접속한 언어가 전체에 노출되던 결함). `ko` 는 번역 조회 없이 원본을 반환한다.
  - 메뉴 관리 트리 조회(`menu-main-list` → `getMainMenuList`)도 같은 방식으로 지역화한다. 편집 폼은 이 목록이 아니라 상세 조회(`GET /menu/{id}`, 지역화하지 않음)를 원천으로 쓰므로, 트리에 번역이 보여도 수정 대상(ko 원본)과 어긋나지 않는다. 이 조회는 캐시가 없어 locale 별 캐시 key 가 필요 없다. 하위 메뉴 추가 시 `상위 메뉴` 읽기 전용 필드에는 트리의 번역된 이름이 표시되지만 저장 payload 에는 포함되지 않는다(표시 전용). `MenuAdminPage` 는 locale 변경을 watch 해 `fetchTree()` 로 트리를 재조회한다.
  - 관리 상세 조회(`GET /menu/{id}`)는 `MenuDto.i18nList`(locale 별 `menuName`/`menuDescription` 레코드 목록)를 함께 내려준다. 등록/수정은 폼에서 `i18nList[n].locale`/`menuName`/`menuDescription` 로 전송하며, 서버는 기존 번역을 전부 지우고 전달 목록으로 교체한다(번역명이 빈 행·`ko` 행은 제외). 저장 시 사이드바 캐시 4종을 무효화한다.
  - 메뉴 등록/수정 모달은 `메뉴 설명` 아래에 다국어 번역 영역을 둔다. 각 행은 `로케일 select + 번역된 메뉴명 + 번역된 설명`이며 `+`로 추가, `-`로 제거한다. 로케일 선택지는 `MENU_I18N_LOCALE_OPTIONS`(= `SUPPORTED_LOCALES` − `ko`)이고, 이미 다른 행이 쓰는 로케일은 select에서 제외하며 남은 로케일이 없으면 `+`를 비활성화한다(같은 locale 중복 불가 — 복합 PK). 레이블·placeholder·안내는 현재 locale의 클라이언트 카탈로그를 사용한다.
  - 클라이언트 `localeStore.setLocale`은 catalog 재로드 후 `menuStore.refreshMenu()`로 사이드바를 재조회해 새 언어의 메뉴명/설명을 반영한다(모듈 순환 회피 위해 지연 import). 재조회 실패는 콘솔 경고로 남기고 기존 메뉴를 유지한다.
  - i18n 계약을 `code_item` 처럼 `Map<locale, 번역명>` 이 아니라 `List<MenuI18nDto>` 레코드로 둔 이유: 메뉴는 번역 필드가 `menuName`+`menuDescription` 둘이라, 맵 두 개로 쪼개면 두 맵의 locale 키 집합이 어긋난 고아 행이 생길 수 있어서다.
  - locale 전환 시 사이드바 반영: 클라이언트 `setLocale` 이 메뉴 재조회를 트리거해야 새 언어가 사이드바에 나타난다(서버 캐시는 locale 별로 분리되어 있으므로 재조회만으로 반영됨).
- `submenuExpandType=NO_SUB`는 해당 메뉴가 하위 메뉴를 생성하거나 이동받을 수 없는 leaf 메뉴임을 의미한다. `submenuExpandType=BOARD`도 게시판 관리 소유 메뉴이므로 하위 메뉴 추가 액션과 상위 메뉴 후보, 트리 이동 대상 부모에서 제외한다.
- 메뉴 트리의 보호/숨김 아이콘은 Bootstrap tooltip을 `v-tooltip` directive로 활성화한다. `protectedYn=Y` 메뉴는 `bi-shield-lock text-warning`와 `시스템 보호 메뉴`로 표시하며, `sidebarVisibleYn=N` 메뉴는 `bi-eye-slash text-muted`와 `사이드바 숨김 메뉴`로 표시한다. 아이콘 hover cursor는 도움말(`help`)이다.
- 하위 메뉴 등록/수정 모달의 상위 메뉴는 호출한 부모 또는 기존 부모를 읽기 전용으로 표시한다. 상위 메뉴 변경은 트리 드래그 앤 드롭과 `PUT /api/menus/tree` 전용이며, 일반 수정 API는 `parentMenuId` 변경 요청을 거부한다.
- API: `GET /api/menus`, `POST/GET/PATCH/DELETE /api/menu(s)/{id}`, `PUT /api/menus/sort-orders`, `PUT /api/menus/tree`

---

## 계정 관리 (`user-admin`)

**Vue view**: `app/frontend-vue/src/features/admin/UserAdminPage.vue`  
**스토어**: `features/admin/stores/userAdmin.ts`

**기능**:
- 계정 등록은 저널 스레드·게시판·코드 관리와 동형인 뷰 툴바(`user-admin-view-toolbar`, `pe-5 mt-3 mb-1`)에 둔다. ASIDE·탭용 `mt-5` 빈 여백은 없다. 목록 카드는 `margin-top: 0`으로 툴바에 붙인다. 화면 설명은 `USER_ACCOUNT` 메뉴의 `menuDescription`으로 breadcrumb 하단에 표시한다. 본문 상단 전용 새로고침 버튼은 두지 않는다.
- 계정 목록 조회/검색/권한 필터
- 계정 상세/등록/수정 (프로필·고용정보 서브폼 포함)
- 계정 삭제 (본인 계정 삭제 불가)
- 비밀번호 초기화. 초기화 전 비밀번호 해시는 `user_password_history`에 기록되어 `auth_policy.password_history_count` 재사용 제한에 포함된다.
- 중복 체크 (아이디/이메일)
- 고용정보 이메일 ID·도메인 입력을 유지하며 도메인 placeholder는 현재 locale의 `user.form.email-domain-placeholder` 카탈로그를 사용한다.
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
- `/admin/log/stats-user` → 사용자별 통계 뷰 (`isStatsView = true`) — 로그인 사용자별 + 비로그인 구분별 활동 건수 목록(로그 수 내림차순·순번 부여). 기간 미지정 시 **오늘 통계**(레거시 `log_stats_user_list` 기본 노출 동일). 통계 뷰 진입 시 조회
- 로그 목록·검색·상세의 URL·URI·Trace·IP·Referer와 응답시간 `ms` 단위는 현재 locale의 공통 기술 카탈로그를 사용하며 기술 표기 자체는 한·영에서 동일하게 유지한다.
- API: `GET /api/logs`, `GET /api/logs/{id}`, `GET /api/logs/stats-user` (`LogStatsUserQueryService` — 레거시 서비스를 현행 flat 패키지로 복원, 응답 `rsltObj = { userList, anonymousList }`)

---

## 계정 신청 승인 관리 (`user-signup-approval`)

**Vue view**: `app/frontend-vue/src/features/user/signup/UserSignupApprovalList.vue`  
**스토어**: `features/user/stores/userSignup.ts`

**기능**:
- breadcrumb와 중복되는 본문 상단 제목은 표시하지 않는다.
- 승인 대기 신청 목록 조회
- 최근 신청 내역 (최근 30건)
- 승인 대기·최근 신청 표의 ID·이메일 열 레이블은 현재 locale의 클라이언트 카탈로그를 사용한다.
- 신청 승인 → `POST /api/user/signup-requests/{id}/approval`
- 신청 반려 → `POST /api/user/signup-requests/{id}/rejection`
