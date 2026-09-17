# 인터랙션 패턴 마이그레이션 스펙 (Interaction Spec)

> 저널 어사이드 전용 인터랙션은 ``journal/interaction-spec.md`` 참조.



## Vue SPA 인터랙션 (소스 기준)

### HTTP 클라이언트

- 저널·게시판·attachable 스토어는 **axios** (`axios.get/post` 등) 사용.
- Vue SPA는 전역 `blockUI`(진행 차단)를 사용하지 않는다.
- 전역 AJAX 진행 표시는 **nprogress** 상단 로딩바로 대체한다. `shared/http/ajaxLoadingBar.ts`의 `installAjaxLoadingBar(axios)`가 in-flight 카운터로 request/response 인터셉터를 등록하며, `main.ts`에서 부트 시 한 번 호출한다.
- 제외: `config.skipLoadingBar === true`, `/api/session/ping`(모달 선행 핑). 영역별 스피너·`로딩...` 텍스트(태그클라우드 등)는 기존대로 유지하고 전역 바와 병행한다.
- 스토어 레벨 오류 메시지는 axios 로딩 인터셉터에 의존하지 않고 각 `catch`에서 처리한다(401은 전역 응답 인터셉터).

### 목록 갱신 (저널)

- CRUD·필터 후: `useJournalStore.fetchDays()` (필요 시 `fetchTagCloud()`).

### 목록 갱신 (게시판·스레드)

- `useBoardPostStore.fetchList(page)`, `useJournalThreadStore.fetchList(page)` — Vue 템플릿 내 페이지 버튼.
- `journal-thread` 상세/등록/수정 진입은 목록 내부 local state만으로 열지 않고 Vue Router 경로(`/thread`, `/thread/new`, `/thread/:id`, `/thread/:id/edit`)를 단일 진입 경로로 사용한다. 라우트 변경 시 `JournalThreadLayout`이 모달 상태를 동기화하고, 모달이 모두 닫히면 `thread-list`로 복귀한다.
- SPA 목록은 클라이언트 페이지네이션을 사용한다.

### CRUD 성공 알림 후 갱신

- Vue SPA의 등록·수정·삭제 성공 흐름은 성공 메시지를 먼저 표시하고, 사용자가 OK를 누른 뒤 목록·상세·달력 갱신 또는 라우터 이동을 수행한다.
- 적용 화면: 게시판 게시물, 일정 달력, 관리자 게시판 그룹, 코드 관리, 메뉴 관리, 계정 관리, 계정 신청.
- 토글·정렬 저장처럼 등록·수정·삭제가 아닌 보조 동작은 각 화면의 기존 처리 순서를 유지한다.

### 모달

- Pinia 스토어의 `visible` / `open*` 함수 + Bootstrap 5 모달 컴포넌트.
- 게시판 상세: `useBoardPostStore.openDetail(id)`.

### 언어(Locale) 전환

- `shared/i18n/stores/locale.ts` (`useLocaleStore`) — Pinia store, `localStorage("dreamdiary_locale")` 에 `ko`/`en` 저장.
- 전환 시 `axios.defaults.headers.common["Accept-Language"]` 갱신 → 이후 모든 axios 요청에 반영.
- 서버는 `AcceptHeaderLocaleResolver`(Spring MVC) 로 `Accept-Language` 헤더를 읽어 `LocaleContextHolder` locale 설정 → `MessageUtils.getMessage()` 응답 메시지 다국어 반환.
- 앱 초기 로드 시 `applyLocaleHeader()` (`ApiService.ts`) 가 localStorage 값을 읽어 axios 헤더를 초기화한다.
- 라우터 `beforeEach`는 인증 상태 확인과 화면 마운트보다 먼저 `localeStore.ensureCatalog()`를 호출한다. 같은 locale의 catalog가 이미 준비됐으면 재요청하지 않으며, 직접 URL 진입·새로고침에서도 번역 키 대신 현재 locale 메시지를 표시한다.
- `i18nCatalogService.t(catalog, key)`는 catalog에 키가 있으면 그 값을 쓴다. **빈 문자열도 유효한 번역**이다(예: en `date.suffix.after-month-number` → 월 접미사 없음 → `7`). 키가 없을 때만 key 문자열을 그대로 반환한다. `value || key`로 빈 값을 키로 되돌리면 안 된다.
- 로그인 화면(`SignIn.vue`): 국기 버튼(🇰🇷/🇺🇸) — `localeStore.setLocale()` 호출, 화면 텍스트 `localeStore.t()` 카탈로그로 전환.
- 로그인 폼(`SignIn.vue`): 아이디 또는 비밀번호가 비어 있으면 `POST /api/auth/login`을 호출하지 않고 필드 검증 메시지(`auth.login.required`)만 표시한다. 빈 값 제출은 로그인 실패 횟수에 포함되지 않는다.
- 앱 헤더 Navbar: 국기 버튼 클릭 → ko↔en 토글. 테마 전환·사용자/관리자 모드·언어 전환·프로필·모바일 헤더 메뉴의 사용자 노출 레이블은 현재 locale의 클라이언트 카탈로그를 사용하며, locale 변경은 기존 테마·메뉴 모드·라우트·인증 상태를 보존한다.
- 브라우저 탭 제목은 최종 route의 `meta.pageTitleKey`를 현재 locale의 클라이언트 카탈로그로 해석한다. route 또는 locale 변경 시 `App.vue`의 단일 반응형 경로가 제목을 즉시 갱신하며, locale 변경은 현재 route와 인증 상태를 변경하지 않는다.

### 라우팅·메뉴

- `router/index.ts` + `beforeEach` 인증 (`useAuthStore.verifyAuth`). 정상 서버 인증 결과는 메모리에서 15초간 재사용하며, 신선도 만료·새로고침·강제 검증 시 `/api/auth/get-auth-account`를 다시 호출한다. 동시 검증은 진행 중 Promise를 공유한다.
- 사이드바: `toVuePath(menu.url)` (`utils/urlMapping.ts`).
- 메뉴 DB와 외부 링크의 `/app/**`는 프론트엔드 종류와 독립적인 제품 화면 URL이다. `.do` 경로는 서버 MVC 화면 계약이며, 신규 제품 화면 URL은 `.do` 없이 정의할 수 있다. `toVuePath`는 제품 화면 URL을 현재 Vue 내부 route로 연결한다. 데이터 조회·저장 엔드포인트는 `/api/**`에서 분리한다.

---

## AJAX 패턴

### Vue SPA 에러 핸들링 (전역 Axios 인터셉터)

`main.ts`에 등록된 전역 `axios.interceptors.response` 에서 HTTP 상태코드별 처리:

| 상태코드 | 처리 방식 |
|---------|----------|
| 401 Unauthorized | 응답 문구나 locale과 무관하게 HTTP 상태만으로 미인증을 판정하고 `shared/auth/sessionExpired.ts`의 `confirmSessionExpired()` 다이얼로그를 사용한다. 일반 라우트는 확인 시 `/sign-in?sessionExpired=Y&redirect=현재 fullPath` 이동, 취소 시 현재 화면 유지. 팝업 라우트(`journal-entry-search`, `journal-daily`)는 로그인 화면 이동 대신 창 닫기 확인을 표시하고 확인 시 `window.close()` 호출. 동시에 여러 요청이 401 로 실패해도 대화상자는 1회만 표시(`authExpiredDialogShowing` 플래그). `/api/auth/` 경로는 제외(auth 스토어에서 직접 처리). |
| 403 Forbidden | 권한 오류로 유지한다. 전역 401 인터셉터의 세션 만료 다이얼로그나 로그인 화면 이동으로 분류하지 않고, 호출부의 서버 메시지 alert 또는 `/403` access denied 화면으로 처리한다. |
| 그 외 | Vue 저널의 변경·주요 조회 요청은 `swalRequestError()`가 구조화된 Axios 응답의 `message`를 우선 표시하고, 메시지가 없을 때만 작업별 또는 공통 요청 실패 문구를 표시한다. 그 밖의 컴포넌트/스토어는 각 catch 블록에서 개별 처리한다. |

- 인터셉터에서 401 처리 후 `AuthExpiredError` sentinel(`utils/authError.ts`)을 throw 해 각 catch 블록의 일반 오류 alert 가 중복으로 뜨지 않도록 억제한다.
- 라우터 가드(`router/index.ts`)가 `verifyAuth()` 이후 미인증 상태를 감지한 경우에도 같은 `confirmSessionExpired()`를 거친다. 일반 보호 라우트는 확인 시 `buildSessionExpiredSignInRoute(to.fullPath)`로 이동하고, 팝업 보호 라우트는 alert 후 `next(false)`로 로그인 화면 렌더를 막는다.
- `auth.purgeAuth()`는 로그아웃·세션 만료 시 categoryMap·개인 Prefix·메뉴와 함께 저널 태그 클라우드의 목록·오류·로딩·진행 중 요청 세대를 초기화한다. 이전 사용자 세대의 늦은 태그 클라우드 응답은 다음 사용자 상태에 반영하지 않는다.
- 인증 해제(`isAuthenticated` true→false) 시 `App.vue`가 `v-if`로 마운트하는 전역 저널 모달(`JournalEntryRegistModal`·`JournalEntryViewModal`·`JournalThreadDetailModal`·`JournalThreadRegistModal`)이 즉시 unmount된다. 이 모달들은 Bootstrap `Modal({ backdrop: "static" })`을 사용하므로 body 직하에 append된 `.modal-backdrop`과 `body.modal-open`(overflow·padding-right 잠금)은 element 제거만으로는 걷히지 않는다. `App.vue`의 `isAuthenticated` watch가 nextTick에서 `body > .modal-backdrop`과 body 잠금을 걷어, 세션 만료로 로그인 화면에 복귀했을 때 정적 오버레이가 화면 전체를 막지 않게 한다. admin 페이지가 Vue로 직접 렌더하는 backdrop은 `#app` 하위라 이 정리 대상에서 제외한다.
- `confirmSessionExpired()`의 일반 화면·팝업 제목, 설명, 확인·취소 버튼은 현재 locale의 클라이언트 카탈로그를 사용한다. locale은 안내 문구만 변경하며 HTTP 상태 판정, 중복 다이얼로그 방지, 팝업 닫기, 로그인 이동·취소 분기를 변경하지 않는다.
- 사용자 체감 로그인 유지 시간은 `auth_policy.session_timeout_minutes` 단일 정책으로 관리한다. 서버는 이 값을 Spring Session max inactive interval, JWT access token `exp`, JWT 쿠키 max-age에 적용하고, JWT 검증 시에도 `issuedAt + policyTimeout`을 넘으면 만료로 처리한다. 정책값이 없거나 조회 실패 시 기존 `server.servlet.session.timeout` 설정을 fallback으로 사용한다.
- Vue 저널의 submit/delete/state catch는 `swalRequestError(e)`를 호출한다. 이 공통 함수가 `AuthExpiredError`를 즉시 무시하고, 그 외 오류는 콘솔에 기록한 뒤 서버 `message` 또는 현재 locale의 `common.error.processing` 공통 실패 문구를 `swalFire({ icon: "error", text })`로 표시한다.
- Ajax `rslt` 분기 후속 안내는 `shared/utils/swal.ts`의 `swalAjaxResult({ rslt, message, successFallback?, failureFallback? })`를 사용한다. 내부에서 `swalFire({ text, icon })`로 success/error 아이콘을 주입한다. 옵션 직접 주입이 필요하면 `swalFire(options)`를 쓴다. 검증·중립 안내는 아이콘 없는 `swalAlert(text)`를 유지한다.
- Vue 저널의 검색·목록 조회가 실패해도 직전 성공 데이터를 빈 목록이나 `0건`으로 덮지 않는다. 상세·수정용 조회 실패는 오류를 표시하고 해당 모달을 열지 않는다. 동일 계약은 aside TODO·태그 클라우드 섹션·스레드 상세 소속/연관/피커·결산 상세 엔트리·계정 신청 승인 목록에도 적용한다(실패 UI ≠ 정상 빈 결과).
- 저장·삭제·복원처럼 결과값으로 후속 알림을 분기하는 store action은 `AuthExpiredError`를 `{ rslt: false }` 또는 `false`로 변환하지 않고 재throw한다. 호출부는 인증 만료일 때 전역 401 안내만 남기고, 실제 처리 실패일 때만 실패 알림을 표시한다.
- 인증이 필요한 Vue SPA 모달은 `shared/auth/sessionPing.ts`의 `assertAuthenticatedBeforeModal()`을 먼저 호출한 뒤 모달 open 플래그 또는 Bootstrap `show()`를 실행한다. 이 핑은 `/api/session/ping`을 호출하며, 로그인 세션이 풀려 있으면 전역 401 인터셉터가 즉시 세션 만료 안내를 표시하고 모달은 열지 않는다. 로그인 화면의 비밀번호 변경 모달처럼 비로그인 상태에서 열려야 하는 auth 모달은 선행 핑 대상에서 제외한다.
- 취소 시 navbar 세션 만료 메시지 표시는 Vue SPA 에서 미구현.

### 백엔드 전역 예외 응답

`BaseExceptionHandler`는 Ajax 요청의 HTTP 상태와 `AjaxResponse.status`를 동일하게 유지한다.

| 예외 유형 | HTTP/본문 status | 사용자 메시지 계약 |
|---|---:|---|
| `BindException`, `BusinessException`, `IllegalArgumentException` | 400 | 첫 검증 메시지 또는 메시지 번들로 해석한 요청 오류 |
| `NotAuthorizedException`, `AccessDeniedException` | 403 | 권한 오류 메시지 |
| `EntityNotFoundException`, `NoHandlerFoundException` | 404 | 대상 데이터/경로 없음 메시지 |
| `DuplicateException`, `DataIntegrityViolationException` | 409 | 중복 또는 데이터 무결성 충돌 메시지 |
| 그 밖의 `Exception` | 500 | 상세 예외는 서버 로그에만 기록하고 응답에는 `msg.rslt.exception` 공통 메시지만 노출 |

메시지가 영문 내부 조건문인 `BusinessException`은 예외 클래스 번들의 공통 요청 실패 문구로 치환하며, 메시지 키나 명시적 사용자 메시지는 그대로 해석한다.

Spring Security 필터·인증 진입점의 Ajax 오류도 `SecurityErrorResponseWriter`를 통해 같은 `AjaxResponse` JSON 계약을 사용한다. 401은 `msg.auth.login-required`, 403은 권한 오류, 필터 내부의 예상 밖 오류는 `msg.rslt.exception`을 사용하며 HTTP 상태와 본문 `status`를 일치시킨다. `AjaxSessionTimeoutFilter`가 비Ajax 요청에서 포착한 인증·권한 예외는 빈 응답으로 삼키지 않고 다시 throw하여 정상 보안 오류 흐름으로 전달한다.

API 인증 경계는 `PublicApiRequestMatcher`를 SSOT로 사용하며 Spring Security와 JWT 필터가 같은 경로·HTTP 메서드 목록을 공유한다. 로그인·토큰 갱신·로그아웃 JSON·세션 만료·로그인 비밀번호 변경·인증 메일 검증·아이디/이메일 중복 확인·신규 계정 신청 POST만 공개하고, 그 외 `/api/**`는 인증을 요구한다. `/api/user/signup-requests`는 POST만 공개하며 목록 GET과 승인/거절은 보호한다. `/api/auth/get-auth-account`와 `/api/session/ping`도 보호 API로서 미인증 요청에 공통 JSON 401을 반환한다.

채팅 경로는 `WebSocketHandshakeRequestMatcher`가 식별하는 `GET /chat` 핸드셰이크만 Spring Security URL 인가에서 공개한다. 핸드셰이크 인증은 `WebSocketAuthInterceptor`가 JWT 또는 기존 Principal로 수행하며, `/chat/settings`, `/chat/sessions`를 포함한 등록된 `/chat/**` REST 요청은 인증을 요구하고 미인증 요청에 공통 JSON 401을 반환한다.

세션 종료 WebSocket 알림은 전역 토픽을 사용하지 않는다. `SessionDestroyListener`는 종료된 세션의 사용자명으로 `/user/queue/session-invalid`에만 발송하며, 웹·모바일 클라이언트도 사용자 전용 큐만 구독한다. 다른 사용자의 세션 종료 이벤트를 현재 사용자의 세션 만료로 오인해서는 안 된다.

채팅 메시지 이력은 세션 소유권을 먼저 검증하는 `GET /chat/sessions/{sessionId}/messages` 단일 경로로 조회한다. 소유권 범위를 확정하지 않는 `GET /chat/messages`는 제공하지 않는다. WebSocket 취소 요청도 대상 세션의 소유권을 검증한 뒤에만 취소 플래그를 변경한다.

---

## 폼 제출 패턴

Vue SPA는 각 feature 스토어 action(필요 시 axios multipart)으로 폼을 제출하고, 등록·수정 모달이 성공 알림 후 목록·상세를 갱신한다(위 「CRUD 성공 알림 후 갱신」). 필드 검증과 오류 표시는 각 모달·컴포넌트에서 처리한다.

---

## 모달 열기/닫기 패턴

Vue SPA 모달은 Pinia 스토어의 `visible`/`open*` + Bootstrap 5 모달 컴포넌트로 연다(위 「모달」). 2회 클릭 안전 닫기는 `useSafeModalClose()` composable로 구현한다(`common/component-spec.md` §16).

### 모달 크기

| 크기 코드 | Bootstrap 클래스 | 용도 |
|----------|----------------|------|
| `xl` | `modal-xl` | 등록/수정 모달 (기본) |
| `xxl` | `modal-xxl` | 상세 모달 (넓은 뷰) |

---

## 공통 확인 다이얼로그

### 로그아웃 확인

Vue SPA의 `UserAccountMenu.vue`와 `SidebarFooter.vue`는 현재 locale의 `account.logout.confirm` 문구로 확인한 뒤 `useAuthStore.logout()`과 로그인 화면 이동을 수행한다. locale 변경은 로그아웃 API·메뉴 초기화·이동 흐름을 변경하지 않는다.

401 세션 만료 확인·이동은 위 「Vue SPA 에러 핸들링」의 `confirmSessionExpired()` 단일 경로를 따른다.

### CRUD 삭제 확인

삭제 확인은 `shared/utils/swal.ts`의 `swalConfirm()`(SweetAlert2)으로 표시한다.

Vue SPA에서 Bootstrap 모달이 열린 상태의 SweetAlert2 확인 다이얼로그는 활성 모달 위에 표시한다. z-index SSOT는 `shared/utils/overlayZIndex.ts`의 `SWAL_Z`(6200)이며, `App.vue` CSS(`!important`)와 `swalFire` `didOpen` inline 강제·모달 스택 `MODAL_MAX_Z` 캡이 함께 확인창이 모달 뒤로 가려지지 않게 한다. 같은 모달 안의 TinyMCE code/link 등 보조 UI(`.tox-tinymce-aux`)는 `TINYMCE_AUX_Z`(6190)로 올려 모달(6100+)·Tagify(6120)에 가려지지 않게 하고, SweetAlert보다는 아래에 둔다. 또한 `.tox-tinymce-aux`로의 `focusin`을 `installModalStacking`이 capture 단계에서 가로채 Bootstrap 모달 FocusTrap의 포커스 회수를 면제하므로, 모달 안 에디터에서 find/replace·link 등 다이얼로그 입력창에 타이핑할 수 있다.

### 모달 닫기 버튼 확인

`modal_btn_close_safe` 패턴: Vue SPA 는 `useSafeModalClose()` composable로 2회 클릭 armed 닫기를 구현한다 — `common/component-spec.md` §16 ✓.

---

## 태그 입력 패턴 (Tagify)

### 초기화 방법

`TagifyEditor.vue`가 Tagify를 초기화한다. `ctgrMap`이 없으면 단순 태그, 있으면 카테고리(2단계), `metaMode`면 카테고리+값(3단계) 입력 모드다.

### 기본 옵션

```javascript
{
    whitelist: [],
    maxTags: 21,
    keepInvalidTags: false,
    skipInvalid: true,
    duplicates: false,
    editTags: { clicks: 2, keepInvalid: false },
    transformTag(tagData) { tagData.value = tagData.value.replace(/\s+/g, '_'); }, // 공백→언더바
    templates: { tag: tagTemplate }  // 커스텀 태그 렌더 (카테고리 배지 포함)
}
```

### 태그 템플릿 구조

```html
<!-- tagTemplate (카테고리 있는 일반 태그) -->
<tag value="TagName" data-ctgr="카테고리">
    <x class="tagify__tag__removeBtn"></x>
    <div>
        <span class="tagify__tag-category text-noti me-1">[카테고리]</span>  <!-- ctgr 있을 때만 -->
        <span class="tagify__tag-text">TagName</span>
    </div>
</tag>

<!-- metaTemplate (카테고리 + 값 있는 메타 태그) -->
<tag value="TagName" data-ctgr="카테고리" data-value="메타값">
    <x class="tagify__tag__removeBtn"></x>
    <div>
        <span class="tagify__tag-category text-noti me-1">[카테고리]</span>
        <span class="tagify__tag-text">TagName</span>
        <span class="tagify__tag-meta mx-1"> - 레이블</span>  <!-- 콜론 앞 부분 -->
        <span class="text-dialog">: 값</span>                 <!-- 콜론 뒤 부분 -->
    </div>
</tag>
```

### 카테고리 입력 흐름 (`initWithCtgr`)

자동완성은 `categoryMap`의 태그명 prefix를 영문 대소문자 구분 없이 비교한다. 입력 `f`·`F`는 모두 저장된 `Flyway`를 후보로 표시하며, 선택·저장에는 카탈로그의 원래 태그명 표기를 사용한다. 부분 문자열 검색으로 확장하지 않고 prefix 검색 범위를 유지한다.

```
1. 사용자가 태그 입력 → Tagify `add` 이벤트 발생
2. 임시 태그로 처리 (committing = false)
3. ctgrMap에 해당 태그 없으면 → 즉시 제거 (무효 태그)
4. ctgrMap에 있으면 → 기존 카테고리 옵션을 먼저 렌더하고 `직접입력` 옵션은 마지막에 둔 selectbox 표시 + **selectbox에 자동 포커스**
5a. selectbox에서 `Tab`/`Shift+Tab` → 다음/이전 카테고리 후보로 이동
5b. selectbox에서 `Enter` → 현재 선택된 카테고리로 commitTag(value, ctgr, null)
5c. "직접입력" 선택 후 `Enter` → ctgr 입력 필드 표시 + input에 자동 포커스 → Tab/Enter로 확정 → commitTag
6. ctgrMap에 없는 태그 → ctgr 입력 필드 직접 표시 + input에 자동 포커스
7. Escape → 입력 취소, draft 초기화
```

Vue `TagifyEditor.vue`의 일반/메타 카테고리 placeholder, 메타 값 예시, 태그 삭제 접근성 레이블, `직접입력` 선택지는 현재 locale의 클라이언트 카탈로그를 사용한다. `tagifyHelper.ts`는 해당 레이블을 호출자로부터 주입받으며 번역 카탈로그를 직접 참조하지 않는다. locale 변경 시 이미 렌더된 삭제 버튼과 열린 `직접입력` 선택지의 레이블만 갱신하고 Tagify 인스턴스를 재생성하지 않아 기존 태그·draft·포커스를 보존한다.

관련 DOM 요소 (스코프 내 선택):
- `#tag_ctgr_select_div`: 카테고리 선택 selectbox 컨테이너
- `#tag_ctgr_select`: 카테고리 selectbox
- `#tag_display_div`: 태그 표시 컨테이너
- `#tag_display`: 태그 이름 표시 input
- `#tag_ctgr_div`: 카테고리 직접 입력 컨테이너
- `#tag_ctgr`: 카테고리 직접 입력 input

### 메타 입력 흐름 (`initMeta`)

카테고리 입력 흐름 후 추가로:
- `#meta_value_div`: 메타 값 입력 컨테이너
- `#meta_value`: 메타 값 input

`commitTag(tagify, value, ctgr, meta)` — 최종 태그 추가:
```javascript
tagify.committing = true;
tagify.addTags([{ value, data: { ctgr, value: meta } }]);
// 모든 보조 입력 컨테이너 숨기고 draft 초기화
// Tagify 기본 입력창으로 포커스 복귀 (setTimeout(..., 0))
```

### 키보드 단축키

| 키 | 동작 |
|----|------|
| Escape | 입력 취소, draft 초기화, 기본 입력창으로 포커스 |
| Tab / Shift+Tab | 카테고리 select에서는 다음/이전 후보 이동. 카테고리 직접 입력·메타 값 입력에서는 현재 단계 확정 후 다음 단계로 이동 |
| Enter | 현재 단계 확정 |

**DRAFT 진입 시 자동 포커스**: 태그 추가로 DRAFT 상태에 진입하면 첫 입력 대상에 자동 포커스된다.
- ctgrMap에 카테고리 목록이 있으면 → `select` 포커스
- ctgrMap에 없거나 "직접입력" 선택 시 → `ctgr input` 포커스
- 구현: `showAndFocus(container, el)` — `display:block` + `setTimeout(..., 0)` 포커스 후 다음 animation frame에서 `document.activeElement`를 확인한다. Tagify가 Enter/add 처리 직후 내부 입력창으로 포커스를 되가져가면 표시된 카테고리 컨트롤에 한 번 더 포커스를 적용한다.
- 포커스 대상이 없거나 재시도 후에도 적용되지 않으면 `console.warn("[tagifyHelper] category focus ...")` 로 대상 컨트롤과 현재 activeElement를 남긴다.

### ctgrMap 로딩 아키텍처


`journalCategoryMaps` Pinia 스토어가 앱 세션 SSOT로 4종 categoryMap(`dayTag`/`dayMeta`/`entryDiary`/`entryDream`)을 유지한다. `journalModal` 모달 openers는 이 스토어에 위임하고, 템플릿은 `modalStore.dayTagCategoryMap` 등 기존 facade를 그대로 쓴다. `App.vue`·로그인 시 `preloadCategoryMaps()`로 1회 HTTP 적재. 모달 오픈은 `ensure`로 **미적재 시에만** 조회하며, 이미 있으면 ref 그대로 사용(모달 오픈 갱신 아님). 태그·메타 포함 저장 성공 시 `applyCategoryMapsFromSaveResponse`가 서버 `rsltMap`으로 세션 ref를 **교체** — 무효화·추가 GET 없음.

```
앱 부트 (인증됨)
  └─ preloadCategoryMaps() → journalCategoryMaps.ensure × 4 → 스토어 ref 적재

모달 오픈
  └─ openDayRegist / openEntryRegist / … (journalModal)
       └─ categoryMaps.ensure(url) — loaded 플래그 있으면 HTTP 생략
            └─ <TagifyEditor :category-map="modalStore.dayTagCategoryMap | entryCategoryMap(computed)" />

태그·메타 포함 저장 성공
  └─ save API 응답 rsltMap: dayTagCategoryMap / dayMetaCategoryMap / entryTagCategoryMap (서버 evict 후 DB 기준 전역 map, 추가 GET 없음)
  └─ applyCategoryMapsFromSaveResponse(rsltMap) — journalCategoryMaps 세션 ref 교체(삭제 반영)
       └─ TagifyEditor categoryMap watch → initTagify()
```

categoryMap URL 매핑:
- 일자 태그: `/api/journal/day/tag/categories`
- 일자 메타: `/api/journal/day/meta/categories`
- 엔트리 DIARY 태그: `/api/journal/entry/tag/categories?type=DIARY`
- 엔트리 DREAM 태그: `/api/journal/entry/tag/categories?type=DREAM`

`TagifyEditor` Props:
- `ctgrMap?: Record<string, string[]> | null` — null이면 ctgr 없는 단순 태그 모드
- `metaMode?: boolean` — true이면 카테고리 + 값 2단계 입력 모드

### 적용 대상 화면

- 엔트리 등록 모달의 DIARY·DREAM 타입에서 Tagify를 사용하고, NOTE 타입은 태그가 없다.

---

## 리치 에디터 패턴 (TinyMCE)

### 초기화

`RichEditor.vue`가 TinyMCE 런타임·플러그인·스킨을 단일 공유 Promise로 로드해 초기화한다(`common/component-spec.md` §RichEditor).

### 기본 설정

```javascript
{
    editor_encoding: "raw",
    height: 540,
    menubar: false,
    branding: false,
    statusbar: false,
    default_link_target: "_blank",
    convert_urls: false,
    plugins: 'help quickbars searchreplace link autolink pageembed table lists advlist checklist emoticons hr visualchars visualblocks pagebreak code codesample',
    toolbar1: 'undo redo | searchreplace | styles styleselect fontselect fontsizeselect | bold italic underline strikethrough | forecolor backcolor | align | code codesample | help',
    toolbar2: 'emoticons custom_image link pageembed | hr | numlist bullist checklist moreless | visualchars visualblocks pagebreak | table tabledelete | tableprops tablerowprops tablecellprops | tableinsertrowbefore tableinsertrowafter tabledeleterow | tableinsertcolbefore tableinsertcolafter tabledeletecol',
    contextmenu: 'link custom_image lists table'
}
```

### 커스텀 버튼

1. **`custom_image`** (이미지 아이콘): 파일 선택 → 이미지 업로드 → `mceInsertContent`로 삽입.
   - 이미지 태그: `<img src='URL' data-mce-src='URL' data-originalFileName='원본파일명'>`
   - 업로드 전 파일 크기·확장자를 검증한다.

2. **`moreless`** (글접기/펼치기 아이콘): 접기 섹션을 삽입한다.
   - 삽입 구조:
     ```html
     <div class="tinymce-section" id="tinymce_section_N">
         <span id="tinymce_toggle_N" class="tinymce-collapse-toggle">Toggle Section N</span>
         <div id="tinymce_section_content_N" class="tinymce-collapsed">Section content goes here.</div>
     </div>
     ```
   - `tinymce_toggle_N` 클릭 → `#tinymce_section_content_N`에 `.collapsed` 클래스 토글

Vue `RichEditor.vue`는 `custom_image`·`moreless` tooltip, 새 섹션의 토글·기본 내용, 이미지 크기 제한·업로드 실패 문구를 에디터 초기화 시점 locale의 클라이언트 카탈로그에서 읽는다. 위 삽입 구조와 ID·class·onclick 토글 계약은 유지하며, 영어 카탈로그에서는 예시 문구를 그대로 사용한다. 서버가 이미지 업로드 실패 `message`를 반환하면 카탈로그 기본 문구보다 우선 표시한다. 작성 중 내용과 커서 상태를 보존하기 위해 locale 변경만으로 열린 TinyMCE 인스턴스를 재생성하지 않는다.

### SaveContent 이벤트 처리

```javascript
editor.on('SaveContent', function(e) {
    e.content = e.content.replace(/&#39/g, '&apos').replace(/&amp;/g, '&');
});
```

자동 이스케이핑 처리 (따옴표, 앰퍼샌드).

### PostRender 이벤트 처리

```javascript
editor.on('PostRender', function() {
    // .tox-tinymce-aux 를 editor 컨테이너의 parentNode로 이동
    // (드롭다운 UI 오버레이 위치 고정)
});
```

### 콘텐츠 설정

`RichEditor.vue`는 에디터 초기화 완료 후 초기 content를 설정하고 편집 상태(undo/dirty)를 정리한다.

### 적용 화면

- 저널 일기/꿈/노트 등록 모달과 결산 등록 모달에서 `RichEditor`를 사용한다. 인스턴스 정리(destroy)는 `RichEditor.vue`가 unmount 시 수행한다.

---

## 목록 새로고침 패턴

목록 갱신은 위 「목록 갱신 (저널)」·「목록 갱신 (게시판·스레드)」·「CRUD 성공 알림 후 갱신」의 스토어 action으로 수행한다: 저널은 `useJournalStore.fetchDays()`(+필요 시 `fetchTagCloud()`), 게시판·스레드는 `fetchList(page)`, 결산은 `JournalAnnualStore.fetchList()`. CRUD 성공은 `swalAjaxResult({ rslt, message, ... })`로 알린 뒤 갱신한다.

---

## 미리보기 패턴 (저널 엔트리·리플렉션 등록 모달)

`JournalEntryRegistModal` / `JournalReflectionRegistModal` 푸터는 저장 왼쪽에 미리보기 버튼을 둔다.

```html
<button type="button" class="btn btn-sm btn-light-primary"
        :title="t('journal.entry.preview.tooltip')"
        @click="preview">
    <i class="bi bi-eye"></i>{{ t('common.preview') }}
</button>
```

클릭 즉시 이름 있는 새 창(`/journal/entry/preview-pop`, `SystemLayout`)을 연 뒤 `POST /api/journal/entries/preview`로 미저장 HTML을 `MarkdownUtils.markdown()`한 `markdownContent`를 받아 localStorage로 전달한다. 팝업은 목록과 같은 `journal-content`와 유형별 item/content 클래스로 렌더한다. 팝업 차단 시 `common.error.popup`을 표시하고 API를 호출하지 않는다.

---

## 뷰 전환 패턴 (저널 일자 탭)

주간/월간/달력/메타 탭 전환은 `router-link`(`journal-weekly`/`journal-monthly`/`journal-calendar`/`journal-meta`)로 수행하며, 활성 탭은 현재 route로 판정한다(`JournalDayViewToolbar.vue`).

---

## 레이아웃 사이드바/어사이드 패턴

사이드바 접힘/펼침과 우측 aside 표시 상태는 클라이언트에 유지하며(aside는 경로별 독립), 저널 aside 토글은 `useJournalAsideStore`가 제어한다. 레이아웃·aside 세부 구조는 `common/component-spec.md`와 `journal/screen-spec.md` aside 섹션을 참조한다.

---

## frontend-vue 공통 runtime 계약 (2026-05-17)

### 메뉴 조회와 전환

- 기본 레이아웃은 첫 렌더 시 `useMenuStore.fetchUserMenu()`로 서버 메뉴를 조회한다.
- 조회 URL은 `/api/menus?mode=USER|MNGR`이며, 응답의 `subMenuList` depth를 유지한다.
- 서버 메뉴 조회 실패 시에만 fallback 메뉴를 사용한다. fallback은 운영 메뉴 관리 기능의 대체물이 아니다.
- 사용자/관리자 전환은 `useMenuStore.setMenuMode()`로 처리하고, 전환 시 menu cache를 비운 뒤 다시 조회한다.
- 로그아웃 또는 인증 검증 실패로 `useAuthStore.purgeAuth()`가 실행되면 `useMenuStore.resetMenu()`로 사이드바 메뉴와 저장된 모드를 `USER` 기본 상태로 초기화한다. 다시 로그인한 뒤 기본 진입 화면(`/journal/daily`)과 사이드바 모드가 어긋나지 않아야 한다.

### 정적 리소스와 폰트

- Vue 앱은 `index.html`에서 `/css/font.css`를 직접 로드한다.
- `/css/font.css` 내부의 `/font/**` 참조는 Spring Boot static resource가 제공하는 파일을 바라본다.
- Vite 개발 서버는 `/css`, `/font` proxy를 backend로 넘긴다.
- 빌드 산출물 안에 font 파일을 복제하지 않는다. 런타임 static 경로가 단일 출처다.

### 인증 결과 화면

- 인증 결과(`verify_success`/`verify_failure`)는 Vue route `/auth/verify-result`로 통합한다.
- 성공/실패 분기는 query/status 또는 서버 redirect 파라미터로 표현한다.
- 별도 FTLH 화면을 다시 만들지 않는다.
---

## 테마 (다크모드 / 라이트모드)

- `useThemeStore.setThemeMode(mode)` → `document.documentElement.setAttribute("data-bs-theme", ...)` + localStorage 저장 (`kt_theme_mode_value`, `kt_theme_mode_menu`) + Pinia `mode` 갱신.
- **부트 hydrate**: store 생성 시 `mode` 초기값은 `index.html`이 세팅한 `data-bs-theme`를 우선하고, 없으면 `kt_theme_mode_value`를 해석한다. 새로고침 후에도 Navbar 아이콘/툴팁이 실제 테마와 일치해야 한다.
- **로그인 화면(`AuthLayout.vue`)은 테마 설정과 무관하게 항상 라이트 모드로 표시**: onMounted에서 DOM만 `data-bs-theme=light` 강제(Pinia `mode`는 사용자 설정 유지). onUnmounted에서는 `applyStoredThemeMode()`로 localStorage 값을 DOM + Pinia에 함께 복원한다.
- 커스텀 SCSS(저널 툴바·챗 등)의 라이트 하드코딩 대응은 별도 작업 범위다. 이 절은 테마 상태 정합만 다룬다.

---

## Prefix API 계약

### Prefix 관리

- `GET /api/my/prefixes?contentType=`: 로그인 사용자의 `(PERSONAL, user_id, content_type)` 개인 Scope에 속한 Prefix를 비활성 포함 `sort_order,id` 순으로 조회한다. Scope가 없으면 빈 목록을 반환한다.
- `GET /api/my/prefixes/options?contentType=`: 같은 개인 Scope의 활성 Prefix만 편집·검색 선택지로 반환하며 Scope가 없으면 빈 목록을 반환한다.
- `POST /api/my/prefixes?contentType=`, `PUT /api/my/prefixes/{prefixId}?contentType=`: 해당 개인 Scope에 Prefix를 생성하고 같은 Scope 소속 Prefix만 수정한다. 최초 등록은 `(user_id, content_type)` Scope를 lazy 생성한다.
- `PATCH /api/my/prefixes/{prefixId}/active?contentType=&active=`: 기존 `prefix_content` 연결을 보존하고 Prefix 활성 상태만 변경한다.
- Prefix 소유권 SSOT는 `created_by`가 아니라 `prefix_scope.(scope_type, user_id, content_type)`와 `prefix.scope_id`의 일치다. `created_by`·`updated_by`는 감사 정보로만 사용한다.
- 사용자 등록 트랜잭션에서 개인 PrefixScope를 사전 생성하지 않는다. 각 content_type의 첫 Prefix 등록 시 Scope를 만들며, 조회 시 Scope 누락은 아직 목록이 없는 정상 상태로 취급한다.

### 스레드 선택

- 스레드 등록·수정 multipart payload는 nullable `prefixId`, 조회 DTO는 `prefix` 표시 정보와 `prefixId`를 사용한다.
- 서버는 Prefix가 로그인 사용자의 개인 Scope에 속하고 스레드가 로그인 사용자 소유인지 확인하며 비활성 Prefix의 신규 선택을 거부한다.
- 목록·후보 검색은 단일 `prefixId`를 사용한다.

### 게시판 선택

- `GET /api/board/{boardKey}/prefixes`는 서버가 게시판 존재를 확인한 뒤 `GLOBAL + boardKey` Scope의 활성 Prefix만 정렬 반환한다. Scope가 없으면 아직 Prefix가 없는 정상 상태로 보고 빈 목록을 반환하며, 공통 코드 관리 API나 `category_group_code`를 조회하지 않는다.
- 게시글 등록·수정 multipart payload는 nullable `prefixId`, 조회 DTO는 `prefix` 표시 정보와 `prefixId`를 사용한다. 목록 검색도 단일 `prefixId`를 사용한다.
- 서버는 Prefix가 대상 게시판의 `GLOBAL + boardKey` Scope에 속하는지 검증하고 비활성 Prefix의 신규 선택을 거부한다. 기존 글의 동일한 비활성 선택은 다른 필드 수정에서 유지할 수 있으며 목록·상세에도 표시한다.
- 게시글 선택은 `prefix_content(ref_id, ref_content_type=boardKey, prefix_id)`에 저장하고 `PrefixEmbed`로 조회한다. 목록 검색은 같은 동적 boardKey 연결을 EXISTS로 필터한다.

### 게시판 Prefix 관리

- 관리자 전용 `GET /api/board/groups/{id}/prefixes`는 게시판 ID로 boardKey를 확정하고 해당 GLOBAL Scope의 비활성 포함 Prefix를 반환한다. Scope 식별자와 게시판 간 공유 메타데이터는 클라이언트 관리 폼에 노출하지 않는다.
- 관리자 전용 `POST /api/board/groups/{id}/prefixes`, `PUT /api/board/groups/{id}/prefixes/{prefixId}`, `PATCH /api/board/groups/{id}/prefixes/{prefixId}/active?active=`는 게시판 ID로 `GLOBAL + boardKey` Scope를 확정한 뒤 공통 Prefix 이름·색상·정렬·중복 불변식을 적용한다. 최초 등록은 Scope를 lazy 생성한다.
- URL의 게시판 Scope와 요청 Prefix의 Scope가 다르면 수정·활성 상태 변경을 거부하고 구조화 로그를 남긴다. 게시판별 목록은 독립적이므로 한 게시판의 변경이 다른 게시판에 전파되지 않는다.
- 게시판 관리 행의 ⋯ 메뉴에서 전용 모달을 열며, 로그인 preflight 뒤 게시판 ID 기준 관리 정보를 조회한다. 저장·활성 상태 변경 성공 후 같은 API를 다시 조회해 서버 응답을 화면 SSOT로 삼는다.
- 신규 게시판 등록은 Prefix 목록을 만들지 않으며 관리 모달에서 첫 Prefix를 추가할 때 독립 GLOBAL Scope가 생긴다.
- `boardKey`는 생성 후 변경 불가다. 수정 화면 read-only와 별개로 서버가 변경을 거부하고, 신규 키가 고정 시스템 ContentType과 충돌해도 등록을 거부한다.

### 내 설정 Prefix 관리 UI

- `/my`는 `/my/profile`로 redirect하고 공통 정체성 헤더 아래 `/my/profile`, `/my/security`, `/my/journal`, `/my/prefixes` 하위 route 탭을 제공한다.
- 탭 이동은 사용자/관리자 메뉴 모드를 변경하지 않으며 브라우저 URL·새로고침·앞뒤 이동으로 선택 탭을 복원한다.
- `PUT /api/user/my`는 요청의 사용자 식별자를 받지 않고 로그인 사용자 본인을 수정한다. 허용 필드는 `nickname`, `phoneNumber`, `brthdy`, `lunarYn`, `proflCn`이며 이메일·역할·허용 IP·재직 정보는 이 계약에서 제외한다.
- 닉네임은 필수·20자 이하, 연락처는 선택·20자 이하, 생년월일은 오늘 이전 날짜, 음력 여부는 `Y|N`, 자기소개는 선택·4000자 이하로 검증한다. 기존 `user_profile` 행이 없으면 저장 트랜잭션에서 생성한다.
- 내 정보 편집의 오른쪽 주 액션 자리는 조회 상태의 「편집」에서 편집 상태의 「저장」으로 전환하고, 「취소」는 저장 왼쪽의 보조 액션으로 표시한다. 저장은 「내 정보를 저장하시겠습니까?」 확인을 거쳐야 하며 확인을 취소하면 API를 호출하지 않고 편집 상태를 유지한다. 편집 취소는 저장하지 않고 서버에서 조회한 값으로 복원한다.
- 내 정보 저장 성공 후 `GET /api/user/my`와 인증 정보 조회를 다시 수행해 닉네임·프로필 표시를 서버 확정 상태로 동기화한다.
- `/my/journal`은 `GET /api/journal/settings/me`로 서버 확정 `defaultEntryView`를 표시하고, 사용자가 선택한 `DAILY | WEEKLY | MONTHLY`를 `PUT /api/journal/settings/me`로 저장한다. 선택값과 서버 확정값이 같으면 저장 버튼을 비활성화하며, 성공 응답의 값을 다시 화면 상태로 사용한다. 조회·저장 실패와 지원하지 않는 응답값은 오류로 표시하고 임의 기본값으로 보정하지 않는다.
- `/my/prefixes` 진입 시 Scope 존재 여부와 무관하게 작은 `저널` 도메인 헤더 아래 `일기 챕터 / 노트 챕터 / 일기 / 꿈 / 노트 / 스레드` 고정 대상 6행을 표시한다. 각 행은 `JOURNAL_CHAPTER_DIARY`, `JOURNAL_CHAPTER_NOTE`, `JOURNAL_DIARY`, `JOURNAL_DREAM`, `JOURNAL_NOTE`, `JOURNAL_THREAD` 실제 `contentType`에 대응하며 사용자가 대상을 추가·삭제하지 않는다. 이후 다른 도메인은 같은 화면 카탈로그의 별도 그룹으로 추가한다.
- 초기 대상 목록에서는 Prefix API를 호출하거나 각 행에 말머리 미리보기를 표시하지 않는다. 행을 누르면 해당 목록만 서버에서 조회해 관리 모달을 열고, Prefix가 없으면 빈 상태와 추가 액션을 표시한다. 최초 등록 시 서버가 해당 `(user, content_type)` Scope를 lazy 생성한다.
- 관리 모달을 닫으면 표시 목록을 비우고 진행 중인 조회를 무효화해 다른 content_type 항목을 임시로 재사용하지 않는다. 늦게 끝난 이전 응답은 요청 순번으로 폐기한다.
- 말머리 편집 중 관리 모달을 닫으면 미저장 변경 폐기 확인을 먼저 표시한다. 확인하면 폼·오류를 초기화하고 닫으며, 취소하면 현재 모달과 편집 상태를 유지한다.
- 저장·활성 상태 변경 성공 후 현재 대상의 서버 목록을 다시 조회한다. 색상은 `#RRGGBB`, 정렬은 0 이상의 정수로 전송한다.
