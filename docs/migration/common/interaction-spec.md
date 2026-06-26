# 인터랙션 패턴 마이그레이션 스펙 (Interaction Spec)

> 저널 어사이드 전용 인터랙션은 ``journal/interaction-spec.md`` 참조.



## Vue SPA 인터랙션 (소스 기준)

### HTTP 클라이언트

- 저널·게시판·attachable 스토어는 **axios** (`axios.get/post` 등) 사용.
- 레거시 `cF.ajax` / 전역 `fetch` 오버라이드(blockUI)는 SPA 빌드에서 **별도 이식 여부는 코드 확인 필요** — 스토어 레벨에서는 axios 인터셉터에 의존하지 않고 각 `catch`에서 메시지 처리.

### 목록 갱신 (저널)

- 레거시 `JournalDayMonthlyApp.refresh()` 등 **미사용**.
- CRUD·필터 후: `useJournalStore.fetchDays()` (필요 시 `fetchTagCloud()`).

### 목록 갱신 (게시판·스레드)

- `useBoardPostStore.fetchList(page)`, `useJournalThreadStore.fetchList(page)` — Vue 템플릿 내 페이지 버튼.
- `journal-thread` 상세/등록/수정 진입은 목록 내부 local state만으로 열지 않고 Vue Router 경로(`/thread`, `/thread/new`, `/thread/:id`, `/thread/:id/edit`)를 단일 진입 경로로 사용한다. 라우트 변경 시 `JournalThreadLayout`이 모달 상태를 동기화하고, 모달이 모두 닫히면 `thread-list`로 복귀한다.
- 레거시 `#listForm` + `Pagination.fnPage` 서버 리로드는 SPA 목록에서 **대체**.

### CRUD 성공 알림 후 갱신

- Vue SPA의 등록·수정·삭제 성공 흐름은 성공 메시지를 먼저 표시하고, 사용자가 OK를 누른 뒤 목록·상세·달력 갱신 또는 라우터 이동을 수행한다.
- 적용 화면: 게시판 게시물, 일정 달력, 관리자 게시판 그룹, 코드 관리, 메뉴 관리, 계정 관리, 계정 신청.
- 토글·정렬 저장처럼 등록·수정·삭제가 아닌 보조 동작은 각 화면의 기존 처리 순서를 유지한다.

### 모달

- Pinia 스토어의 `visible` / `open*` 함수 + Bootstrap 5 모달 컴포넌트.
- 게시판 상세: 레거시 `CustomEvent('board-post:open-detail-modal')` 대신 `useBoardPostStore.openDetail(id)`.

### 라우팅·메뉴

- `router/index.ts` + `beforeEach` 인증 (`useAuthStore.verifyAuth`).
- 사이드바: `toVuePath(menu.url)` (`utils/urlMapping.ts`).

---

## AJAX 패턴

### 기반 모듈

`cF.ajax` (`legacy/static/js/common/ajax.ts`) — 노출식 모듈 패턴(IIFE)

### fetch 전역 오버라이드

모든 `fetch` 호출은 전역 래퍼로 가로채진다:

```
window.fetch = async (url, options) => {
    // 1. X-Requested-With: XMLHttpRequest 헤더 자동 추가
    // 2. Content-Type: application/json 자동 추가 (FormData 제외)
    // 3. cF.ui.blockUI() — 요청 전 UI 차단
    // 4. 원본 fetch 수행
    // 5. !response.ok → handleError(response) 분기
    // 6. finally: cF.ui.unblockUI() — UI 차단 해제
};
```

### 에러 핸들링

`handleError(response)` 에서 HTTP 상태코드별 분기:

| 상태코드 | 처리 방식 |
|---------|----------|
| 401 Unauthorized | 팝업 창이면 `closePopupAndRedirectOpener(loginFormUrl)`. 일반 창이면 SweetAlert 확인 다이얼로그 → 로그인 페이지 이동 또는 머무르기. 머무르기 선택 시 navbar에 `session-expired-message` div 삽입 (`.blink.text-danger` blink 애니메이션) |
| 403 Forbidden | SweetAlert alert → 로그인 페이지 이동 |
| 400 Bad Request | 응답 body를 파싱하여 필드 에러 추출. 대상 필드의 `#${fieldId}_validate_span`에 에러 메시지 표시 (`.text-danger`). 필드 없으면 SweetAlert |
| 5xx | SweetAlert alert (`view.error.request` 메시지) |
| 기타 | SweetAlert alert (body에서 추출한 메시지 또는 `view.error.access-denied`) |

### AJAX 메소드

```javascript
// GET 요청 (query string 자동 변환)
cF.ajax.get(url, ajaxData, callback, continueBlock?)

// POST 요청 (JSON body)
cF.ajax.post(url, ajaxData, callback, continueBlock?)

// Multipart POST (파일 업로드)
cF.ajax.multipart(url, formData, callback, continueBlock?)

// 직접 fetch 옵션 지정
cF.ajax.request(url, options, callback, continueBlock?)
```

- `continueBlock = 'block'`: 콜백 완료 후에도 blockUI 유지
- `continueBlock = 'none'` (기본): 콜백 완료 후 unblockUI

### Vue 전환 후 패턴

레거시 `cF.ajax.*` 대신 Vue 서비스 모듈(`journalEntryCrudService`, `journalAnnualCrudService` 등)이 직접 `fetch` API 사용. 전역 fetch 오버라이드는 그대로 유효하여 blockUI/에러 처리가 자동 적용된다.

### Vue SPA 에러 핸들링 (전역 Axios 인터셉터)

`main.ts`에 등록된 전역 `axios.interceptors.response` 에서 HTTP 상태코드별 처리:

| 상태코드 | 처리 방식 |
|---------|----------|
| 401 Unauthorized | `shared/auth/sessionExpired.ts`의 `confirmSessionExpired()` 다이얼로그 사용. 일반 라우트는 확인 시 `/sign-in?sessionExpired=Y&redirect=현재 fullPath` 이동, 취소 시 현재 화면 유지. 팝업 라우트(`journal-entry-search`, `journal-daily`)는 로그인 화면 이동 대신 창 닫기 확인을 표시하고 확인 시 `window.close()` 호출. 동시에 여러 요청이 401 로 실패해도 대화상자는 1회만 표시(`authExpiredDialogShowing` 플래그). `/api/auth/` 경로는 제외(auth 스토어에서 직접 처리). |
| 403 Forbidden | 권한 오류로 유지한다. 전역 401 인터셉터의 세션 만료 다이얼로그나 로그인 화면 이동으로 분류하지 않고, 호출부의 서버 메시지 alert 또는 `/403` access denied 화면으로 처리한다. |
| 그 외 | Vue 저널의 변경·주요 조회 요청은 `swalRequestError()`가 구조화된 Axios 응답의 `message`를 우선 표시하고, 메시지가 없을 때만 작업별 또는 공통 요청 실패 문구를 표시한다. 그 밖의 컴포넌트/스토어는 각 catch 블록에서 개별 처리한다. |

- 인터셉터에서 401 처리 후 `AuthExpiredError` sentinel(`utils/authError.ts`)을 throw 해 각 catch 블록의 일반 오류 alert 가 중복으로 뜨지 않도록 억제한다.
- 라우터 가드(`router/index.ts`)가 `verifyAuth()` 이후 미인증 상태를 감지한 경우에도 같은 `confirmSessionExpired()`를 거친다. 일반 보호 라우트는 확인 시 `buildSessionExpiredSignInRoute(to.fullPath)`로 이동하고, 팝업 보호 라우트는 alert 후 `next(false)`로 로그인 화면 렌더를 막는다.
- 사용자 체감 로그인 유지 시간은 `auth_policy.session_timeout_minutes` 단일 정책으로 관리한다. 서버는 이 값을 Spring Session max inactive interval, JWT access token `exp`, JWT 쿠키 max-age에 적용하고, JWT 검증 시에도 `issuedAt + policyTimeout`을 넘으면 만료로 처리한다. 정책값이 없거나 조회 실패 시 기존 `server.servlet.session.timeout` 설정을 fallback으로 사용한다.
- Vue 저널의 submit/delete/state catch는 `swalRequestError(e)`를 호출한다. 이 공통 함수가 `AuthExpiredError`를 즉시 무시하고, 그 외 오류는 콘솔에 기록한 뒤 서버 `message` 또는 공통 실패 문구를 표시한다.
- Vue 저널의 검색·목록 조회가 실패해도 직전 성공 데이터를 빈 목록이나 `0건`으로 덮지 않는다. 상세·수정용 조회 실패는 오류를 표시하고 해당 모달을 열지 않는다.
- 저장·삭제·복원처럼 결과값으로 후속 알림을 분기하는 store action은 `AuthExpiredError`를 `{ rslt: false }` 또는 `false`로 변환하지 않고 재throw한다. 호출부는 인증 만료일 때 전역 401 안내만 남기고, 실제 처리 실패일 때만 실패 알림을 표시한다.
- 인증이 필요한 Vue SPA 모달은 `shared/auth/sessionPing.ts`의 `assertAuthenticatedBeforeModal()`을 먼저 호출한 뒤 모달 open 플래그 또는 Bootstrap `show()`를 실행한다. 이 핑은 `/api/session/ping`을 호출하며, 로그인 세션이 풀려 있으면 전역 401 인터셉터가 즉시 세션 만료 안내를 표시하고 모달은 열지 않는다. 로그인 화면의 비밀번호 변경 모달처럼 비로그인 상태에서 열려야 하는 auth 모달은 선행 핑 대상에서 제외한다.
- 취소 시 navbar 세션 만료 메시지 표시(legacy `.blink.text-danger`)는 Vue SPA 에서 미구현.

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

채팅 메시지 이력은 세션 소유권을 먼저 검증하는 `GET /chat/sessions/{sessionId}/messages` 단일 경로로 조회한다. 소유권 범위를 확정하지 않는 레거시 `GET /chat/messages`는 제공하지 않는다. WebSocket 취소 요청도 대상 세션의 소유권을 검증한 뒤에만 취소 플래그를 변경한다.

---

## 폼 제출 패턴

### 기반 모듈

`cF.form` (`legacy/static/js/common/form.ts`) — 노출식 모듈 패턴(IIFE)

### 일반 폼 제출

```javascript
// 직접 폼 제출 (prefunc 선택적)
cF.form.submit(formSelector, actionUrl, prefunc?)

// jQuery submit (jquery-validation 통과용)
cF.form.$submit(formSelector, actionUrl, prefunc?)
```

### blockUI 적용 폼 제출

```javascript
// blockUI + closeModal + submit
cF.form.blockUISubmit(formSelector, actionUrl, prefunc?)
// 내부: cF.ui.blockUIRequest() → cF.ui.closeModal() → cF.form.submit()

// blockUI + closeModal + jQuery submit
cF.form.$blockUISubmit(formSelector, actionUrl, prefunc?)
```

### 페이지네이션 폼 제출

`_pagination.ftlh`의 `Pagination.fnPage(pageNo, pageSize?)`:

```javascript
// 내부적으로 #listForm의 pageNo, pageSize hidden 필드 업데이트 후 제출
cF.form.blockUISubmit("#listForm", listUrl)
```

페이지 크기 변경 시 현재 페이지 자동 재계산:
```javascript
Pagination.fnRepage(pageNo, prevPageSize, newPageSize)
// = Math.floor((pageNo-1)*prevPageSize / newPageSize) + 1
```

### 저널 엔트리 등록 폼

각 엔트리 타입별 폼 ID:
- DIARY: `#journalDiaryRegistForm` → `enctype="multipart/form-data"`, `method="post"`
- DREAM: `#journalDreamRegistForm` → 동일
- NOTE: `#journalEntryRegistForm` → 동일

`<input type="hidden" name="type" value="${entryRegType}">` — 타입 구분 hidden 필드 포함

제출 핸들러: `JournalEntryRegVueApp.submit('JOURNAL_DIARY'|'JOURNAL_DREAM'|'JOURNAL_NOTE')`

### 저널 일자 등록 폼

`#journalDayRegistForm` → `enctype="multipart/form-data"`
저장 버튼: `dF.JournalDayRuntimeService.handleLegacyActionClick(event)` (이벤트 위임)
닫기 버튼: `data-journal-day-action` 속성으로 액션 전달

### 유효성 검사 패턴

- `required` 클래스: 필수 필드 표시 (TinyMCE textarea 포함)
- 에러 표시: `#${fieldId}_validate_span` (`.text-danger`)에 메시지 직접 삽입
- 공백 자동 제거: `.no-space` 클래스 — `Layout.init()` 시 `cF.validate.noSpaces(".no-space")` 적용
- 숫자만 허용: `.number` 클래스 — `cF.validate.onlyNum(".number")` 적용
- 페이지네이션 숫자 입력: `.page-ellipsis` — `cF.validate.onlyNum(".page-ellipsis")` 적용

---

## 모달 열기/닫기 패턴

### Bootstrap 5 기반 모달

모든 모달은 Bootstrap 5 `modal` 플러그인 기반. Freemarker macro `@modal.layout id="XXX" size="xl|xxl"` 로 껍데기 생성.

### 모달 오픈 방법

1. **직접 jQuery 호출** (구형 패턴):
   ```javascript
   $('#modal_id').modal('show');
   ```

2. **CustomEvent 패턴** (Vue 전환 후):
   ```javascript
   window.dispatchEvent(new CustomEvent('board-post:open-detail-modal', { detail: { id: N } }));
   ```

3. **Vue 브리지 호출**:
   ```javascript
   window.JournalDayRegVueApp.pendingPayload = payload;
   $('#journal_day_reg').modal('show');
   ```

4. **서비스 함수 호출**:
   ```javascript
   dF.JournalDayRuntimeService.handleLegacyActionClick(event);
   CommentList.modal(id, contentType);
   FileGroupList.modal(fileGroupId);
   dF.Tag.dtlModal(tagId);
   ```

### ModalHistory 패턴

모달 스택 관리. 닫기 버튼에서 `ModalHistory.pop()` 호출:

```javascript
// 표준 닫기 버튼 (modal_header macro)
onClick="ModalHistory.pop();"

// 이전 모달로 돌아가기 (modal_header_with_back macro)
onclick="ModalHistory.pop(); ModalHistory.prev();"
```

### 안전 닫기 패턴 (modal_btn_close_safe)

`Layout.modalBtnCloseSafe()` 가 모든 `.modal-btn-close-safe` 버튼에 적용:

```javascript
// 클릭 시:
// 1. isAllowed 플래그 체크 (중복 클릭 방지)
// 2. data-bs-dismiss="modal" 속성 동적 추가
// 3. data-func 속성의 함수 eval() 실행 (ModalHistory.pop() + 커스텀 콜백)
// 4. 2초 후 isAllowed = false, data-bs-dismiss 제거 (안전장치 복구)
```

닫기 버튼에 저장되지 않은 변경 감지 로직을 `data-func`로 주입 가능.

### 모달 크기

| 크기 코드 | Bootstrap 클래스 | 용도 |
|----------|----------------|------|
| `xl` | `modal-xl` | 등록/수정 모달 (기본) |
| `xxl` | `modal-xxl` | 상세 모달 (넓은 뷰) |

### 모달 헤더 종류 (macro)

| Macro | 특징 |
|-------|------|
| `@component.modal_header title` | 표준 헤더. 닫기 버튼 1개 (`ModalHistory.pop()`) |
| `@component.modal_header_with_back title` | 이전 모달 돌아가기 + 닫기 버튼 2개 |
| `@component.modal_header_dark title` | 다크 헤더 (`background-color: #41416e`) |

---

## 공통 확인 다이얼로그

### 로그아웃 확인

`Layout.logout()`:
```javascript
Swal.fire({
    text: Message.get("view.cnfm.logout"),
    showCancelButton: true,
}).then(result => {
    if (!result.value) return;
    location.replace(Url.API_AUTH_LGOUT);
});
```

### 401 세션 만료 처리

`handleError()` 내부:
```javascript
cF.ui.swalOrConfirm(
    msg + "\n" + Message.get("view.auth.redirect-to-login-form"),
    /* 확인 콜백 */ () => { window.location.href = loginFormUrl; },
    /* 취소 콜백 */ () => { /* navbar에 만료 메시지 표시 */ }
);
```

### CRUD 삭제 확인

각 서비스 모듈 내부에서 `cF.ui.swalOrConfirm()` 또는 `Swal.fire({ showCancelButton: true })` 형태로 구현. 레거시 코드에서 삭제 전 확인 다이얼로그는 서비스별로 개별 구현.

### 모달 닫기 버튼 확인

`modal_btn_close_safe` 패턴: 변경 사항 있을 때 닫기 전 확인 로직을 `data-func` 속성에 주입. Vue 전환 후 `dF.JournalDayRuntimeService.handleLegacyActionClick(event)` 형태로 처리.

---

## 태그 입력 패턴 (Tagify)

### 기반 모듈

`cF.tagify` (`legacy/static/js/common/helper/tagify.ts`)

### 초기화 방법

```javascript
// 기본 태그 입력
const tagify = cF.tagify.init(selector, additionalOptions?)

// 카테고리 있는 태그 입력
const tagify = cF.tagify.initWithCtgr(selector, ctgrMap, additionalOptions?)

// 메타(카테고리 + 값) 있는 태그 입력
const tagify = cF.tagify.initMeta(selector, ctgrMap, additionalOptions?)
```

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

**변경 전 (legacy/초기 SPA):** `TagifyEditor.vue`가 `onMounted` 시 HTTP로 ctgrMap을 직접 조회 → 모달 열릴 때마다 추가 round-trip 발생.

**변경 후 (현행):** `journalModal` Pinia 스토어가 앱 세션 SSOT로 4종 categoryMap(`dayTag`/`dayMeta`/`entryDiary`/`entryDream`)을 유지한다. `App.vue` 로그인·마운트 시 `preloadCategoryMaps()`로 1회 HTTP 적재. 모달 오픈은 `ensureCategoryMap`으로 **미적재 시에만** 조회하며, 이미 있으면 ref 그대로 사용(모달 오픈 갱신 아님). 태그 저장 성공 시 `applyCategoryMapsFromTagSave`가 Tagify JSON을 `mergeTagifyListIntoCategoryMap`으로 **병합** — 무효화·추가 GET 없음.

```
앱 부트 (인증됨)
  └─ preloadCategoryMaps() → ensureCategoryMap × 4 → 스토어 ref 적재

모달 오픈
  └─ openDayRegist / openEntryRegist / …
       └─ ensureCategoryMap(url) — loaded 플래그 있으면 HTTP 생략
            └─ <TagifyEditor :category-map="modalStore.dayTagCategoryMap | entryCategoryMap(computed)" />

태그·메타 포함 저장 성공
  └─ save API 응답 rsltMap: dayTagCategoryMap / dayMetaCategoryMap / entryTagCategoryMap (서버 evict 후 DB 기준 전역 map, 추가 GET 없음)
  └─ applyCategoryMapsFromSaveResponse(rsltMap) — 앱 세션 ref 교체(삭제 반영)
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

- `_journal_entry_reg_modal.ftlh`에서 DIARY 타입 (`entryRegShowTagify = true`) 과 DREAM 타입에서 Tagify 초기화
- `entryRegShowTagify = false`: NOTE 타입 (태그 없음)

---

## 리치 에디터 패턴 (TinyMCE)

### 기반 모듈

`cF.tinymce` (`legacy/static/js/common/helper/tinymce.ts`)

### 초기화

```javascript
cF.tinymce.init(selectorStr, imgFunc?)
```

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

1. **`custom_image`** (이미지 아이콘): `imgFunc()` 호출
   - 기본 `imgFunc`: `fileGroup0` input 클릭 → change 이벤트 → `/file/fileUploadAjax.do` 업로드 → `tinymce.execCommand('mceInsertContent', true, imgTag)` 삽입
   - 이미지 태그: `<img src='URL' data-mce-src='URL' data-originalFileName='원본파일명'>`
   - 파일 타입 검증: `cF.validate.fileSizeChck`, `cF.validate.fileImgExtnChck`

2. **`moreless`** (글접기/펼치기 아이콘): `cF.tinymce.morelessFunc()` 호출
   - 삽입 구조:
     ```html
     <div class="tinymce-section" id="tinymce_section_N">
         <span id="tinymce_toggle_N" class="tinymce-collapse-toggle">Toggle Section N</span>
         <div id="tinymce_section_content_N" class="tinymce-collapsed">Section content goes here.</div>
     </div>
     ```
   - `tinymce_toggle_N` 클릭 → `#tinymce_section_content_N`에 `.collapsed` 클래스 토글

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

### 콘텐츠 설정 (비동기)

```javascript
cF.tinymce.setContentWhenReady(editorNm, content, attempt?)
// 최대 20회(50ms 간격) 재시도하여 editor 초기화 완료 후 content 설정
// resetContent() → undoManager.clear() → setDirty(false) → save() 순서로 초기화
```

### 에디터 삭제

```javascript
cF.tinymce.destroy(selector)
// tinymce.remove(editorElement)
```

### 적용 화면

- `journal_day_monthly.ftlh`, `journal_day_weekly.ftlh`: 일기/꿈/노트 등록 모달에서 사용
- `journal_annual_list.ftlh`, `journal_annual_detail.ftlh`: 결산 등록 모달에서 사용
- 각 엔트리 등록 모달의 textarea ID:
  - DIARY: `tinymce_journalDiaryCn`
  - DREAM: `tinymce_journalDreamCn`
  - NOTE: `tinymce_journalEntryCn`

---

## 목록 새로고침 패턴

### 브리지 API 패턴 (Vue 전환 후)

Vue 앱이 마운트되기 전에도 호출이 가능하도록 `window` 브리지 객체에 큐잉:

```javascript
// 마운트 전 상태 (셸)
window.JournalDayMonthlyApp = {
    mounted: false,
    pendingLoad: null,
    refresh: function() { this.pendingLoad = { type: 'refresh' }; },
    render: function(model) { this.pendingModel = model; },
    ...
};

// Vue 마운트 완료 후 실제 구현으로 교체
// pendingLoad / pendingModel을 확인하여 큐잉된 명령 즉시 실행
```

### 저널 일자 목록 갱신

```javascript
// 월간 새로고침
JournalDayMonthlyApp.refresh()
JournalDayMonthlyApp.loadMonthly()
JournalDayMonthlyApp.applySearchParamsAndReload(patch, scope?)

// 주간 새로고침
JournalDayWeeklyApp.refresh()
JournalDayWeeklyApp.loadWeekly(stdrdDt, targetDt)
```

### 결산 목록 갱신

`JournalAnnualListApp` 부트 시 `dF.JournalAnnual.init() + listAjax()` 수행 (레거시 IIFE 동등).

### 페이지네이션 기반 목록 (스레드, 게시판)

서버사이드 페이지네이션. 갱신은 `Pagination.fnPage(pageNo, pageSize?)` → `cF.form.blockUISubmit("#listForm", listUrl)` → 전체 페이지 재렌더.

### CRUD 완료 후 목록 처리

Vue 서비스 모듈에서 CRUD 완료 콜백:
1. 성공 응답 확인 (`rslt === true` 또는 `res.rslt`)
2. `cF.ui.swalOrAlert(res.message)` — 성공/실패 메시지 표시
3. 모달 닫기 (`cF.ui.closeModal()` 또는 `ModalHistory.pop()`)
4. 목록 App의 refresh/reload 메소드 호출

### 태그 헤더 갱신

`JournalDayEntryTagListVueApp.setList(type, tagList)` 브리지:
- Vue 마운트 전 호출은 `pendingByType` 큐잉
- `journalEntryTagService.renderList` → `JournalDayEntryTagListVueApp.setList` 브리지 경유

---

## 미리보기 패턴 (저널 엔트리 등록 모달)

저널 엔트리 등록 모달의 미리보기 버튼:

```html
<button type="button" class="btn btn-sm btn-light-primary me-2"
        onclick="${entryRegPreviewHandler}"
        data-bs-toggle="tooltip" title="미리보기">
    <i class="bi bi-eye"></i>미리보기
</button>
```

핸들러:
```javascript
// DIARY
window.JournalEntryRegVueApp && JournalEntryRegVueApp.preview('JOURNAL_DIARY')
// DREAM
window.JournalEntryRegVueApp && JournalEntryRegVueApp.preview('JOURNAL_DREAM')
// NOTE
window.JournalEntryRegVueApp && JournalEntryRegVueApp.preview('JOURNAL_NOTE')
```

---

## 뷰 전환 패턴 (저널 일자 탭)

`dF.JournalDayViewService.changeView(url)`:
- 탭 클릭 → URL 전환 (페이지 이동)
- 탭 4종: 주간(`JOURNAL_DAY_WEEKLY`), 월간(`JOURNAL_DAY_MONTHLY`), 달력(`JOURNAL_DAY_CAL`), 메타(`JOURNAL_DAY_META_VIEW`)

현재 활성 탭은 해당 페이지 ftlh에서 `.active` 클래스로 하드코딩.

---

## 레이아웃 사이드바/어사이드 패턴

`Layout.ts`:

### 사이드바 상태 저장

- `localStorage` 키: `layout_sidebar_desktop_state`
- 값: `"minimized"` | `"expanded"`
- 태블릿 뷰 (768px~1199.98px): 기본 minimized
- 터치 기기 데스크톱 뷰: 기본 minimized

### 어사이드(우측 패널) 상태

- `localStorage` 키: `layout_aside_state:${location.pathname}` (경로별 독립 저장)
- 데스크톱: `data-kt-app-aside-collapse` 속성으로 열림/닫힘 제어
- 모바일/태블릿: `data-app-hide-aside` 속성으로 표시/숨김 제어
- 토글 버튼: `#kt_app_engage_primary_btn` — 클릭 시 아이콘/텍스트 전환
  - 열림: `<i class="bi bi-x-lg me-1"></i>Filter`
  - 닫힘: `<i class="bi bi-layout-sidebar-inset-reverse me-1"></i>Filter`

### 버튼 딜레이

`Layout.setBtnDelay()`: 모든 `button`, `.btn`, `.badge` (`.modal-btn-close-safe` 제외)에 클릭 딜레이 적용 (`cF.ui.delayBtn()`).

### Alive Check

`Layout.aliveCheck(60)`: 60초 주기로 alive check URL 호출 (현재 fetch 주석 처리됨).

---

## frontend-vue 공통 runtime 계약 (2026-05-17)

### 메뉴 조회와 전환

- 기본 레이아웃은 첫 렌더 시 `useMenuStore.fetchUserMenu()`로 서버 메뉴를 조회한다.
- 조회 URL은 `/api/menus?mode=USER|MNGR`이며, 응답의 `subMenuList` depth를 유지한다.
- 서버 메뉴 조회 실패 시에만 fallback 메뉴를 사용한다. fallback은 운영 메뉴 관리 기능의 대체물이 아니다.
- 사용자/관리자 전환은 `useMenuStore.setMenuMode()`로 처리하고, 전환 시 menu cache를 비운 뒤 다시 조회한다.
- 로그아웃 또는 인증 검증 실패로 `useAuthStore.purgeAuth()`가 실행되면 `useMenuStore.resetMenu()`로 사이드바 메뉴와 저장된 모드를 `USER` 기본 상태로 초기화한다. 다시 로그인한 뒤 기본 진입 화면(`/journal/weekly`)과 사이드바 모드가 어긋나지 않아야 한다.

### 정적 리소스와 폰트

- Vue 앱은 `index.html`에서 `/css/font.css`를 직접 로드한다.
- `/css/font.css` 내부의 `/font/**` 참조는 Spring Boot static resource가 제공하는 파일을 바라본다.
- Vite 개발 서버는 `/css`, `/font` proxy를 backend로 넘긴다.
- 빌드 산출물 안에 font 파일을 복제하지 않는다. 런타임 static 경로가 단일 출처다.

### 인증 결과 화면

- legacy `verify_success.ftlh`, `verify_failure.ftlh`는 Vue route `/auth/verify-result`로 통합한다.
- 성공/실패 분기는 query/status 또는 서버 redirect 파라미터로 표현한다.
- 별도 FTLH 화면을 다시 만들지 않는다.
---

## 테마 (다크모드 / 라이트모드)

- useThemeStore.setThemeMode(mode) → document.documentElement.setAttribute("data-bs-theme", ...) + localStorage 저장 (kt_theme_mode_value).
- **로그인 화면(AuthLayout.vue)은 테마 설정과 무관하게 항상 라이트 모드**: onMounted에서 data-bs-theme=light 강제 적용, onUnmounted에서 localStorage 저장값으로 복원.
