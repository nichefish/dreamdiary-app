# 공통 컴포넌트 마이그레이션 스펙 (Component Spec)

> 저널 전용 컴포넌트(JournalTagCloudHeader, JournalAsideFilterHeader 등)는 ``journal/component-spec.md`` 참조.


## Vue SPA 소스 레이아웃 (feature module)

백엔드 `feature/*` 축과 맞춘 **도메인 우선** 구조. 레거시 `views/` + flat `stores/` hybrid 는 제거됐다.

| 영역 | 경로 | 역할 |
|------|------|------|
| App shell | `app/frontend-vue/src/app/` | `router/`, `layouts/`, `pages/Error*.vue` |
| Shared platform | `app/frontend-vue/src/shared/` | auth·config·theme·menu store, 공통 UI(RichEditor·Tagify), 범용 `utils/` |
| Product features | `app/frontend-vue/src/features/{admin,journal,chat,board,calendar,user,attachable,auth}/` | 화면 + feature store + (admin) types co-location |
| Journal 횡단 store | `features/journal/stores/` | `journal.ts`, `journalModal.ts`, `journalAside.ts` 등 subdomain 공유 상태 |
| Journal domain utils | `features/journal/utils/` | `journalDate.ts`, `journalDayRefresh.ts` 등 |
| UI platform kit (Metronic) | `src/platform/metronic/` (`frontend-vue`·`frontend-react` 공통 SSOT) | UI platform kit 층. import `@metronic` alias. 제품 도메인(`app`/`shared`/`features`)·npm vendor 아님. 상세·편집 정책은 `docs/DEV_NOTES.md` §Metronic platform 경계. Vue·React `src/platform/metronic` (✓). |
| App styles | `src/styles/` | 앱 전역 스타일 |

Import alias: `@/features/...`, `@/shared/...`, `@/app/...`, `@metronic/...`(→ `src/platform/metronic`). 이전 `@/views/`, `@/stores/`, `@/layouts/` 경로는 re-export 없이 제거됐다.
## 공통 컴포넌트 목록

## Vue SPA 공통 구현 (소스 기준)

> Freemarker 매크로를 **별도 Vue 컴포넌트로 추출하지 않은** 항목은 feature 목록·모달 내부에 인라인 구현되어 있다.

| 구분 | Vue 경로 | 레거시 매크로 대응 | 비고 |
|------|----------|-------------------|------|
| 리치 에디터 | `shared/ui/editor/RichEditor.vue` | TinyMCE (`cF.tinymce`) | 저널·게시판 등록 모달. placeholder(`rich-editor.placeholder`), 이미지·접기 섹션 버튼 tooltip, 새 섹션 기본 문구, 이미지 검증·업로드 실패 문구는 에디터 초기화 시점 locale의 클라이언트 카탈로그를 사용한다. 작성 중 내용·커서 보존을 위해 locale 변경만으로 열린 TinyMCE 인스턴스를 재생성하지 않는다. 모달 안 code/link 등 `.tox-tinymce-aux`는 `TINYMCE_AUX_Z`(6190)로 Bootstrap 모달 위에 표시한다. |
| 태그 입력 | `shared/ui/tag/TagifyEditor.vue` + `shared/utils/tagifyHelper.ts` | Tagify (`cF.tagify` init / initWithCtgr / initMeta) | ✓ 레거시 카테고리·메타 2단계 흐름 이식. 카테고리·메타 값 placeholder, 태그 삭제 접근성 레이블, 직접입력 선택지는 현재 locale의 클라이언트 카탈로그를 사용한다. helper는 번역 레이블을 옵션으로 주입받으며 locale 변경 시 Tagify 인스턴스를 재생성하지 않고 기존 태그·draft·포커스를 보존한다. 자동완성 dropdown은 `document.body`에 append 하고 Bootstrap 모달보다 높은 z-index로 표시한다. |
| 댓글 목록/등록 | `features/attachable/CommentListModal.vue` 등 | `list_comment`, `CommentList.modal` | `useAttachableModalStore.openCommentList` |
| 파일 그룹 | `FileGroupListModal.vue`, `FileGroupDetail.vue`, `FileGroupSection.vue` | `list_file_group` | `openFileList`. 다운로드 레이블과 파일 크기 단위는 현재 locale의 클라이언트 카탈로그를 사용하며 다운로드 URL·클릭 흐름은 유지한다. |
| 이력 | `HistoryModal.vue` | — | `openHistory`. 각 이력 카드에 텍스트 복사 버튼(`bi bi-copy`) 구현 완료 |
| 태그 목록/프로필 | `JournalTagListModal.vue` 등 | `list_tag`, `dF.Tag.dtlModal` | `openTagList`, `openTagProfile` |
| 게시판 목록 행 | `BoardPostList.vue` 인라인 | `list_comment`, `list_tag`, `list_dtl_modal` | `useBoardPostStore.fetchList` + 페이지 버튼 |
| 스레드 목록 행 | `JournalThreadList.vue` 인라인 | 동일 | `useJournalThreadStore` |
| 페이지네이션 | 목록 Vue 인라인 + `shared/utils/paginationDataService.ts` | `Pagination` / `_pagination.ftlh` | 서버 JSON script 태그 호환 유틸만 존재, `Pagination.vue` 없음 |
| 페이지 breadcrumb | `app/layouts/default/components/content/PageBreadcrumb.vue` | 레거시 메뉴 경로 표시 | `useMenuStore.menuMetaList` + `toVuePath(menu.url)` 기준 현재 route와 매칭되는 메뉴 트리를 표시. `menuMetaList`가 비어 있으면 로딩 전 fallback으로 `menuList`를 사용한다. 매칭된 메뉴의 `menuDescription`이 있으면 breadcrumb 하단에 설명을 표시하고, 비어 있으면 설명 영역을 렌더링하지 않는다. 홈 레이블은 현재 locale의 클라이언트 카탈로그를 사용하며 `route.meta.breadcrumbs/pageTitleKey`를 표시 원천으로 쓰지 않는다. |
| 브라우저 탭 제목 | `App.vue`, `app/router/index.ts` | 레거시 페이지 제목 | 모든 최종 route는 `meta.pageTitleKey`를 제공하며, `App.vue`가 현재 locale의 클라이언트 카탈로그로 제목을 해석해 `{페이지 제목} - {VITE_APP_NAME}` 형식으로 표시한다. route 또는 locale 변경 시 같은 단일 경로에서 즉시 갱신한다. |
| 사이드바 로고 | `app/layouts/default/components/sidebar/SidebarLogo.vue` | 레거시 앱 로고 | 브랜드명은 locale과 무관하게 `DreamDiary`로 고정 표시한다. locale 변경은 브랜드 표기·홈 링크·DOM·CSS를 변경하지 않는다. |
| Footer | `app/layouts/default/components/footer/Footer.vue` | 레거시 footer | `2024©` tooltip은 프로젝트 기간(`2024.03.20 ~ (진행중)`), 사이트 링크 tooltip은 작업인원(`nysnyari`)을 표시한다. 프로젝트 기간·진행 상태·작업인원·About 레이블은 현재 locale의 클라이언트 카탈로그를 사용한다. tooltip은 `v-tooltip`로 locale 변경 시 재생성하며 링크·DOM·배치는 유지한다. |
| 모달 헤더/버튼 | 각 `modals/*.vue` | `modal_header`, `modal_btn_*` | 공통 추출 **MISSING** |
| AI 채팅 드로어 | `features/chat/AppChat.vue` | Metronic app-chat | 인증 후 전역 플로팅 버튼·드로어. `chat.*` 카탈로그로 셸 UI·RAG 메타 레이블·person-role·response-mode·guard 문구를 표시한다. assistant 메시지에 `metadataJson` RAG 근거가 있으면 접이식 상세(출처 건수·intent·responseMode·personFocus·태그/타임라인·`ragSources` score/snippet)를 렌더한다. 출처는 기본 5건이며 `+N more`로 펼친다. `journalEntryId`가 있는 출처 행은 `journalModal.openEntryView`로 읽기 전용 원문 모달을 연다(`App.vue` 전역 `JournalEntryViewModal`, 팝업 라우트 제외). 모달 footer의 편집은 `openEntryModifyFromView`로 기존 수정 모달로 전환한다. 응답 대기 중에는 서버 `PROGRESS` 단계(`SEARCHING`/`GENERATING`)를 typing 인디케이터 옆에 `chat.waiting.*`로 표시한다. assistant 버블은 `markdownContent`(서버 `renderChatMarkdown`)를 `v-html`로 렌더하고 USER는 평문이다. 메시지가 비어 있으면 `chat.empty.prompt` 아래에 카탈로그 시드 질문(`chat.empty.seed.1`..`4`) 칩을 보이고, 클릭 시 즉시 전송한다. 세션 칩 제목은 더블클릭 인라인 편집으로 `PATCH /chat/sessions/{id}` 저장한다. 본경로 응답 생성 중에는 WS `DELTA`로 임시 assistant 버블(`streamingContent`)을 평문 누적하고, 완성 ASSISTANT 메시지가 도착하면 교체한다. locale 변경은 WebSocket·세션 목록·작성 중 메시지를 초기화하지 않는다. |
| 앱 런타임 상태 | `shared/components/system/AppRuntimeStatus.vue` + `shared/utils/appRuntimeStatus.ts` | — | 라우팅 지연·렌더 예외·전역 런타임 예외를 화면에 표시 |
| 오류 화면 | `app/pages/ErrorPage.vue` | legacy 오류 화면 | 오류 분류·제목·설명·메인 이동 레이블은 현재 locale의 클라이언트 카탈로그를 사용한다. URL query로 전달된 상세 메시지는 서버 원문을 그대로 표시하며 오류 유형·상태 코드·메인 이동 흐름은 유지한다. |
| 앱 헤더 Navbar | `app/layouts/default/components/header/Header.vue`, `Navbar.vue` — 테마·사용자/관리자 모드·국기 버튼(🇰🇷/🇺🇸)·프로필·모바일 헤더/사이드바 메뉴 | — | `useLocaleStore.setLocale()` ko↔en 토글. 테마 전환 tooltip, 사용자/관리자 모드 선택지, 언어 전환 tooltip, 프로필 대체 텍스트, 모바일 헤더·사이드바 메뉴 tooltip은 현재 locale의 클라이언트 카탈로그를 사용하며 locale 변경은 테마·메뉴 모드·라우트·인증 상태를 변경하지 않는다. |
| 사용자 계정 메뉴·사이드바 Footer | `app/layouts/default/components/menus/UserAccountMenu.vue`, `sidebar/SidebarFooter.vue` | — | 사용자 역할·내 설정·로그아웃·로그아웃 확인 문구는 현재 locale의 클라이언트 카탈로그를 사용한다. 사용자 계정 dropdown에는 권한별로 노출되는 메뉴 항목을 두지 않으며(역할 뱃지 아이콘만 isMngr 기준으로 구분), 로그아웃 API, 로그인 화면 이동 흐름은 유지한다. |
| 내 설정 허브 | `features/user/UserMyPage.vue`, `components/UserMy*Tab.vue`, `stores/userMy.ts`, `stores/userPrefixes.ts` | `user_my_page.ftlh` + 비밀번호 변경 모달 | ✓ `/my/profile`, `/my/security`, `/my/prefixes` URL 기반 탭. 공통 정체성 헤더 아래 내 정보·보안·말머리 관리를 분리하며 `/my`는 프로필 탭으로 redirect한다. 마이페이지 내부 섹션 이동은 요약 영역 버튼을 중복 제공하지 않고 이 탭만 사용한다. 내 정보 탭은 조회/편집 상태를 분리하고 닉네임·개인 연락처·생년월일·음력 여부·자기소개만 `PUT /api/user/my`로 저장한다. 생년월일과 양력/음력 토글은 같은 행에 두고 모바일에서 세로 배치한다. 이메일·재직 정보는 조회 전용이며, 저장 성공 후 내 정보와 인증 상태를 다시 조회해 공통 헤더 닉네임까지 서버 확정 상태로 갱신한다. |
| 언어 토글 (로그인) | `features/auth/SignIn.vue` — 로그인 패널 우상단 국기 버튼 | — | `localeStore.setLocale()` + `localeStore.t()` 카탈로그로 화면 텍스트 전환. 언어 선택지 레이블은 `locale.label.ko` / `locale.label.en` |

화면 위치/제목/설명 표시는 breadcrumb가 담당한다. 각 화면 본문 상단에는 breadcrumb와 중복되는 page title 또는 메뉴 설명을 별도로 렌더링하지 않고, 필요한 액션 버튼만 둔다.

`useAttachableModalStore` (`features/attachable/stores/attachableModal.ts`) 주요 API: `openCommentRegist`, `openCommentModify`, `openCommentList`, `openHistory`, `openRelated`, `openTagList`, `openTagProfile`, `openFileList`.
`RelatedContentAddModal.vue`의 제목·필드·옵션·검색 상태·검증·결과 메시지는 현재 locale의 클라이언트 카탈로그를 사용하며, 저장 API가 서버 `message`를 반환하면 그 값을 우선 표시한다. 연결 대상 검색은 선택 유형을 `DIARY|DREAM`으로 변환해 통합 `GET /api/journal/entries`를 호출하고 제목 또는 본문 일치 결과를 최신순 최대 8건 표시한다. 요청 실패는 오류 메시지로 표시해 정상 0건과 구분한다. `openRelated`는 일반 관련글(RELATED) 전용이다.
현재 `RelatedContentAddModal.vue`와 `/api/related` API는 일기·꿈 사이의 직접 관련글 1단계 연결을 구현한다. 엔트리 관련글 행의 직접 관계 해제도 **구현 완료(✓)**다. FLOW 축은 저널 스레드 소속으로 수렴 완료되어 attachable 관련글·종단 보기 경로에 두지 않는다(`docs/migration/journal/interaction-spec.md`, `docs/migration/journal/component-spec.md`).
`JournalTagProfileModal.vue`의 제목·필드·선택지·버튼·확인·결과 메시지는 현재 locale의 클라이언트 카탈로그를 사용하며, 저장·삭제 API가 서버 `message`를 반환하면 그 값을 우선 표시한다. 크기 최대(`forceMax`)는 태그클라우드에서 `ts-9`로 고정하며 엔트리 본문 태그줄에는 적용하지 않는다.
`JournalTagListModal.vue`의 제목·빈 상태·분류·버튼·태그별 일자 목록 툴팁은 현재 locale의 클라이언트 카탈로그를 사용한다.
`CommentRegistModal.vue`의 제목·필드·버튼·검증·확인·결과 메시지는 현재 locale의 클라이언트 카탈로그를 사용하며, 등록·수정 API가 서버 `message`를 반환하면 그 값을 우선 표시한다.

---

### RichEditor 저장 HTML 계약

`RichEditor.vue`는 TinyMCE HTML 원문을 전송하고, 서버 저장 정규화(`MarkdownUtils.normalize`)가 유효한 에디터 HTML 구조와 literal escaped HTML 텍스트를 보존한다. 단일 `<p>` 안에 직접 자식 `<br>`로 문단이 나뉘어 들어온 경우에는 저장 시 별도 `<p>` 문단으로 분리해 레거시 문단 간격을 보존한다.

---

### AppRuntimeStatus

Vue SPA는 빌드가 성공했더라도 라우팅, 동적 import, 렌더링, 전역 Promise 예외가 발생하면 화면에 상태를 표시해야 한다.

- 라우터 이동이 500ms 이상 이어지면 상단 pending 배너에 `runtime.pending.navigation` 카탈로그 문구를 표시한다.
- Vue 렌더 오류, `app.config.errorHandler`, `router.onError`, `window.error`, `unhandledrejection`은 `AppRuntimeStatus` 패널에 오류 출처와 메시지를 표시한다.
- 오류 패널 제목·본문·액션(`runtime.action.reload`, `error.page.action.home`)은 현재 locale 카탈로그를 사용한다. 기본 오류 제목은 `runtime.error.default-title`, 라우터 이동 실패는 `runtime.error.navigation`, 인증 확인 실패는 `auth.verification.failure`.
- `app.mount` 실패 등 Vue mount 이전 부트 오류는 `main.ts`가 `/i18n/{locale}.json`에서 `runtime.boot.failure.title`·`runtime.action.reload`를 조회해 표시한다. catalog fetch 실패 시 `dreamdiary_locale` 기준 최소 ko/en fallback을 사용한다.
- 오류를 정상 화면처럼 숨기지 않는다. 콘솔에도 같은 오류를 남겨 개발자가 원인을 확인할 수 있어야 한다.

적용 파일:
- `app/frontend-vue/src/App.vue`
- `app/frontend-vue/src/main.ts`
- `app/frontend-vue/src/app/router/index.ts`
- `app/frontend-vue/src/shared/components/system/AppRuntimeStatus.vue`
- `app/frontend-vue/src/shared/utils/appRuntimeStatus.ts`

---

### TagifyEditor 스타일 보정

`TagifyEditor.vue`는 Tagify가 생성한 `.tagify` 래퍼와 내부 `.tagify__input`에 좌우 padding을 명시한다. 원본 input의 Bootstrap `form-control` 스타일과 Tagify 기본 스타일이 섞여 입력 커서 영역이 테두리나 태그에 딱 붙어 보이는 문제를 막기 위한 보정이다.

적용 위치: `app/frontend-vue/src/styles/components/tag.scss`

같은 파일은 body에 append 된 `.tagify__dropdown`을 Bootstrap 모달(`body.modal-open .modal`, z-index 6100)보다 위인 z-index 6120으로 둔다.

---



---

### 1. `@component.checkbox` (체크박스)

**Source**: `_page_elements.ftlh` — `<#macro checkbox>`

**렌더 HTML 구조**:
```html
<div class="form-check form-switch form-check-custom form-check-solid">
    <input type="checkbox" name="checkboxNm" id="checkboxNm"
           class="form-check-input cursor-pointer ms-3" value="Y" />
    <label class="form-check-label fw-bold ms-3" for="checkboxNm" id="checkboxNmLabel">
        <!-- 색상·레이블은 JS에서 동적 처리 -->
    </label>
</div>
```

**파라미터**:
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `param` | 객체 | 현재 폼 파라미터 (`param[checkboxNm]`) |
| `checkboxNm` | string | input/label ID 및 name |
| `ynLabel` | string | Y/N 레이블 문자열 (구분자 `//`) |
| `ynColor` | string | Y/N 색상 문자열 (구분자 `//`) |
| `defaultOn` | string | `"defaultOn"` 이면 기본 체크 상태 |

**초기화 동작** (DOMContentLoaded):
```javascript
cF.ui.chckboxLabel(checkboxNm, ynLabel, ynColor);
// param[checkboxNm] === "Y" 이거나 defaultOn === "defaultOn" 이면 클릭 상태로 설정
// checkboxNm에서 ID 추출 후 label 텍스트/색상 적용
```

**CSS 클래스**:
- 컨테이너: `form-check form-switch form-check-custom form-check-solid`
- 입력: `form-check-input cursor-pointer ms-3`
- 레이블: `form-check-label fw-bold ms-3`

**현재 Vue 동등**: 미존재 (MISSING). Vue 컴포넌트로 구현 필요.

---

### 2. `@component.mdfable` (목록 수정 권한 아이콘)

**Source**: `_list_elements.ftlh` — `<#macro mdfable>`

**렌더 HTML 구조**:
```html
<!-- 관리자 수정 가능 (MDFABLE_MNGR) -->
<i class="bi bi-person-lines-fill text-info ms-1 opacity-75"
   data-bs-toggle="tooltip" data-bs-placement="top" title="관리자 수정 가능"></i>

<!-- 모든 사용자 수정 가능 (MDFABLE_USER / MDFABLE_ALL) -->
<i class="bi bi-people-fill ms-1 blink-slow"
   data-bs-toggle="tooltip" data-bs-placement="top" title="모든 사용자가 수정 가능"></i>
```

**파라미터**:
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `post` | 객체 | 게시글 객체 (`post.mdfable` 필드 사용) |

**조건 분기**:
- `post.mdfable == Code.MDFABLE_MNGR && isMngr` → 관리자 아이콘 (고정)
- `post.mdfable == Code.MDFABLE_USER || Code.MDFABLE_ALL` → 전체 수정 아이콘 (blink-slow)
- 그 외 → 렌더 없음

**CSS 클래스**:
- 관리자: `bi bi-person-lines-fill text-info ms-1 opacity-75`
- 전체: `bi bi-people-fill ms-1 blink-slow`

**현재 Vue 동등**: 미존재 (MISSING). 목록 컴포넌트 내 조건부 아이콘 렌더로 구현.

---

### 3. `@component.list_comment` (목록 댓글 수 표시)

**Source**: `_list_elements.ftlh` — `<#macro list_comment>`

**렌더 HTML 구조**:
```html
<!-- post.comment.cnt > 0 일 때만 렌더 -->
<span class="mx-1 text-noti btn-active-warning fs-x-small cursor-pointer opacity-hover"
      onclick="CommentList.modal(${post.id}, '${post.contentType}');"
      data-bs-toggle="tooltip" data-bs-placement="top" title="댓글 모달 호출">
    [N]
</span>
```

**파라미터**:
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `post` | 객체 | `post.comment.cnt` (댓글 수), `post.id`, `post.contentType` |

**조건**: `post.comment` 존재 && `post.comment.cnt > 0` 일 때만 렌더.

**CSS 클래스**: `mx-1 text-noti btn-active-warning fs-x-small cursor-pointer opacity-hover`

**클릭 동작**: `CommentList.modal(id, contentType)` — 댓글 목록 모달 호출

**현재 Vue 동등**: 미존재 (MISSING). 스레드 목록(`JournalThreadListApp`), 게시판 목록은 각 Vue 컴포넌트 내부 처리. 공통 Vue 컴포넌트로 추출 가능.

---

### 4. `@component.list_dtl_modal` (목록 모달 팝업 열기 버튼)

**Source**: `_list_elements.ftlh` — `<#macro list_dtl_modal>`

**렌더 HTML 구조**:
```html
<a class="badge badge-secondary p-2 btn-white bg-hover-white blank blink-slow float-end"
   data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="모달로 보기">
    <i class="bi bi-stickies fs-5 text-noti opacity-hover" onclick="${fullFunc}"></i>
</a>
```

**파라미터**:
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `post` | 객체 | `post.id` — ID 값 |
| `func` | string | 클릭 핸들러 함수 문자열 |

**`__ID__` 치환 로직**:
- `func` 에 `__ID__` 포함 시 → `post.id` 로 치환하여 `fullFunc` 생성
- 포함 안 할 시 → `func(id)` 형태로 생성

**예시**:
```freemarker
<!-- board_post_list.ftlh -->
<@component.list_dtl_modal post
   "window.dispatchEvent(new CustomEvent('board-post:open-detail-modal', { detail: { id: __ID__ } }));" />
<!-- → onclick="window.dispatchEvent(new CustomEvent(..., { detail: { id: 42 } }));" -->
```

**CSS 클래스**: `badge badge-secondary p-2 btn-white bg-hover-white blank blink-slow float-end`
아이콘: `bi bi-stickies fs-5 text-noti opacity-hover`

**현재 Vue 동등**: 미존재 (MISSING). 각 목록 컴포넌트 내부에 인라인으로 구현되어 있음. 공통 Vue 컴포넌트로 추출 가능.

---

### 5. `@component.list_managtr` (목록 등록자/조치자 표시)

**Source**: `_list_elements.ftlh` — `<#macro list_managtr>`

**렌더 HTML 구조**:
```html
<!-- 프로필 이미지 있는 경우 -->
<div class="btn btn-icon btn-active-light-primary position-relative w-15px h-15px w-md-20px h-md-20px me-1">
    <img src="${post.managtrInfo.profileImageUrl}" class="img-thumbnail p-0 w-100" />
</div>

<!-- 항상 표시: 조치자명 + 본인 여부 -->
<span class="cursor-help"
      data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
      title="최초 작성자:&#10;${post.createdByNm}">
    ${post.managt.managtrNm}
    <!-- post.managt.isManagtr === true 이면 -->
    <span class="badge badge-secondary opacity-75 mx-1 fs-9">나</span>
</span>
```

**파라미터**:
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `post` | 객체 | `post.managtrInfo.profileImageUrl`, `post.managt.managtrNm`, `post.managt.isManagtr`, `post.createdByNm` |

**CSS 클래스**:
- 이미지 컨테이너: `btn btn-icon btn-active-light-primary position-relative w-15px h-15px w-md-20px h-md-20px me-1`
- 이미지: `img-thumbnail p-0 w-100`
- 이름 span: `cursor-help`
- 본인 배지: `badge badge-secondary opacity-75 mx-1 fs-9`

**툴팁**: `"최초 작성자:\n${post.createdByNm}"` (`&#10;` = 개행)

**현재 Vue 동등**: 미존재 (MISSING).

---

### 6. `@component.list_managtDt` (목록 등록일시/조치일시 표시)

**Source**: `_list_elements.ftlh` — `<#macro list_managtDt>`

**렌더 HTML 구조**:
```html
<span class="cursor-help"
      data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
      title="최초 작성일:&#10;${post.createdAt}">
    ${post.managt.managtDt}
</span>
```

**파라미터**:
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `post` | 객체 | `post.managt.managtDt` (표시일시), `post.createdAt` (최초 작성일 툴팁) |

**CSS 클래스**: `cursor-help`

**현재 Vue 동등**: 미존재 (MISSING).

---

### 7. `@component.list_file_group` (목록 첨부파일 아이콘 표시)

**Source**: `_list_elements.ftlh` — `<#macro list_file_group>`

**렌더 HTML 구조**:
```html
<!-- 파일 있는 경우 -->
<a class="badge badge-secondary p-2 btn-white bg-hover-white blank blink-slow"
   data-bs-toggle="tooltip" data-bs-placement="top" title="첨부파일">
    <i class="bi bi-file-earmark-arrow-down fs-5 text-info opacity-hover"
       onclick="FileGroupList.modal('${post.fileGroupId}');"></i>
</a>

<!-- 파일 없는 경우 -->
-
```

**파라미터**:
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `post` | 객체 | `post.fileGroupInfo.fileRecordList` (파일 목록), `post.fileGroupId` |

**조건**: `post.fileGroupInfo` 존재 && `fileRecordList` not empty → 파일 아이콘. 아니면 `-`.

**CSS 클래스**: `badge badge-secondary p-2 btn-white bg-hover-white blank blink-slow`
아이콘: `bi bi-file-earmark-arrow-down fs-5 text-info opacity-hover`

**클릭 동작**: `FileGroupList.modal(fileGroupId)` — 파일 목록 모달 호출

**현재 Vue 동등**: 미존재 (MISSING). 각 Vue 컴포넌트 내부에서 조건부 렌더로 처리 가능.

### 7-1. `FileGroupDetail` (읽기 전용 첨부파일 상세)

**Vue 컴포넌트**: `app/frontend-vue/src/features/attachable/FileGroupDetail.vue`

**상태**: 구현 완료 (✓)

**동작 계약**:
- `files`가 존재하고 비어 있지 않을 때만 첨부파일 상세 영역을 렌더링한다.
- 영역 라벨과 다운로드 툴팁은 각각 `attach.label`, `attach.download.tooltip` i18n 키를 사용한다.
- 각 파일은 원본 파일명과 현재 locale의 `attach.file-size.unit` 카탈로그를 사용한 바이트 단위 파일 크기를 표시한다.
- 파일 클릭 시 `/api/file/file-download.do?fileGroupId={fileGroupId}&fileId={fileId}`를 새 창으로 열어 다운로드한다.

---

### 8. `@component.list_tag` (목록 태그 목록 표시)

**Source**: `_list_elements.ftlh` — `<#macro list_tag>`

**렌더 HTML 구조**:
```html
<!-- post.tag.tagStrList 있는 경우 -->
<span class="me-6 fs-7">
    <span class="pe-1 text-muted"><i class="bi bi-tags"></i></span>
    <!-- post.tag.list 순회 -->
    <span class="text-muted pe-1 cursor-pointer"
          onclick="dF.Tag.dtlModal('${tag.tagId}')"
          data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
          title="태그 컨텐츠 목록">
        <span class="fs-7 text-noti">[카테고리]</span>  <!-- ctgr 있을 때만 -->
        #<span class="border-bottom text-primary fw-lighter opacity-hover">태그명</span>
    </span>
</span>
```

**파라미터**:
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `post` | 객체 | `post.tag.tagStrList` (유무 체크), `post.tag.list` (순회), `tag.tagId`, `tag.ctgr`, `tag.name` |

**조건**: `post.tag` 존재 && `post.tag.tagStrList` 존재 → 렌더

**CSS 클래스**:
- 컨테이너: `me-6 fs-7`
- 아이콘: `pe-1 text-muted` + `bi bi-tags`
- 태그 span: `text-muted pe-1 cursor-pointer`
- 카테고리 배지: `fs-7 text-noti`
- 태그명: `border-bottom text-primary fw-lighter opacity-hover`

**클릭 동작**: `dF.Tag.dtlModal(tagId)` — 태그 상세 모달 호출

**현재 Vue 동등**: 미존재 공통 컴포넌트 (MISSING). 스레드 목록은 `JournalThreadListApp` 내부 처리. 별도 `TagList.vue` 컴포넌트로 추출 가능.

---

### 9. `@component.modal_header` (모달 표준 헤더)

**Source**: `_modal_elements.ftlh` — `<#macro modal_header>`

**렌더 HTML 구조**:
```html
<div class="modal-header">
    <h5 id="modal_title" class="modal-title">[제목]</h5>
    <div class="btn btn-sm btn-icon btn-active-light-primary ms-2" aria-label="Close"
         onClick="ModalHistory.pop();"
         data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="modal"
         title="닫기">
        <i class="fas fa-times"></i>
    </div>
</div>
```

**파라미터**:
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `title` | string | 모달 제목 (대괄호 자동 추가: `[title]`) |

**CSS 클래스**:
- 헤더: `modal-header`
- 제목: `modal-title` (id: `modal_title`)
- 닫기 버튼: `btn btn-sm btn-icon btn-active-light-primary ms-2`
- 아이콘: `fas fa-times`

**닫기 동작**: `ModalHistory.pop()` + `data-bs-dismiss="modal"`

**현재 Vue 동등**: 미존재 공통 컴포넌트 (MISSING). Vue 마이그레이션 대상 모달들은 모두 이 헤더 패턴 사용. `ModalHeader.vue` 컴포넌트 추출 권장.

---

### 10. `@component.modal_header_with_back` (이전 모달 돌아가기 포함 헤더)

**Source**: `_modal_elements.ftlh` — `<#macro modal_header_with_back>`

**렌더 HTML 구조**:
```html
<div class="modal-header">
    <h5 id="modal_title" class="modal-title">[제목]</h5>
    <div>
        <!-- 이전 모달로 돌아가기 버튼 -->
        <div class="btn btn-sm btn-icon btn-active-light-primary" aria-label="Close"
             onclick="ModalHistory.pop(); ModalHistory.prev();"
             data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="modal"
             title="이전 모달">
            <i class="bi bi-box-arrow-in-left"></i>
        </div>
        <!-- 닫기 버튼 -->
        <div class="btn btn-sm btn-icon btn-active-light-primary" aria-label="Close"
             onClick="ModalHistory.pop();"
             data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="modal"
             title="닫기">
            <i class="fas fa-times"></i>
        </div>
    </div>
</div>
```

**파라미터**:
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `title` | string | 모달 제목 (`[title]`) |

**적용 모달**: `journal_day_dtl` (저널 일자 상세, size=xxl) — 다른 모달에서 열리는 경우

**현재 Vue 동등**: 미존재 (MISSING). `ModalHeaderWithBack.vue` 컴포넌트로 추출 가능.

---

### 11. `@component.modal_header_dark` (다크 배경 모달 헤더)

**Source**: `_modal_elements.ftlh` — `<#macro modal_header_dark>`

**렌더 HTML 구조**:
```html
<div class="modal-header" style="background-color:#41416e;">
    <h5 id="modal_title" class="modal-title">[제목]</h5>
    <div class="btn btn-sm btn-icon btn-active-light-primary ms-2" aria-label="Close"
         data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="modal"
         title="닫기">
        <i class="fas fa-times"></i>
    </div>
</div>
```

**파라미터**: `title` (string)

**특징**: 헤더 배경색 `#41416e` (다크 퍼플). 닫기 버튼에 `ModalHistory.pop()` 없음 (표준 Bootstrap dismiss만).

**현재 Vue 동등**: 미존재 (MISSING).

---

### 12. `@component.modal_btn_save` (모달 저장 버튼)

**Source**: `_modal_elements.ftlh` — `<#macro modal_btn_save>`

**렌더 HTML 구조**:
```html
<button type="button" class="btn btn-sm btn-primary me-2"
        onclick="${func}"
        data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
        title="저장">
    <i class="bi bi-pencil-square"></i>
    <span class="indicator-label">저장</span>
    <span class="indicator-progress">Please wait...
        <span class="spinner-border spinner-border-sm align-middle ms-2"></span>
    </span>
</button>
```

**파라미터**:
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `func` | string | onclick 핸들러 |

**CSS 클래스**: `btn btn-sm btn-primary me-2`
아이콘: `bi bi-pencil-square`
스피너: `spinner-border spinner-border-sm align-middle ms-2` (로딩 중 표시)

**현재 Vue 동등**: 미존재 공통 컴포넌트. 각 모달 footer에 인라인으로 존재. `ModalBtnSave.vue` 추출 권장.

---

### 13. `@component.modal_btn_modify` (수정 모달 호출 버튼)

**Source**: `_modal_elements.ftlh` — `<#macro modal_btn_modify>`

**렌더 HTML 구조**:
```html
<button type="button" class="btn btn-sm btn-active-primary"
        onclick="${func}"
        data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
        title="수정">
    <i class="bi bi-pencil-square"></i>수정
</button>
```

**CSS 클래스**: `btn btn-sm btn-active-primary`

**현재 Vue 동등**: 미존재 (MISSING).

---

### 14. `@component.modal_btn_delete` (모달 삭제 버튼)

**Source**: `_modal_elements.ftlh` — `<#macro modal_btn_delete>`

**렌더 HTML 구조**:
```html
<button type="button" class="btn btn-sm btn-light btn-active-danger"
        onclick="${func}"
        data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
        title="삭제">
    <i class="bi bi-trash"></i>삭제
</button>
```

**CSS 클래스**: `btn btn-sm btn-light btn-active-danger`
아이콘: `bi bi-trash`

**현재 Vue 동등**: 미존재 (MISSING).

---

### 15. `@component.modal_btn_close` (모달 닫기 버튼 - 기본)

**Source**: `_modal_elements.ftlh` — `<#macro modal_btn_close>`

**렌더 HTML 구조**:
```html
<button type="button" class="btn btn-sm btn-light"
        onClick="ModalHistory.pop();"
        data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="modal"
        title="닫기">
    <i class="bi bi-x"></i>닫기
</button>
```

**CSS 클래스**: `btn btn-sm btn-light`
아이콘: `bi bi-x`

**현재 Vue 동등**: 미존재 (MISSING).

---

### 16. `@component.modal_btn_close_safe` (모달 닫기 버튼 - 안전장치)

**Source**: `_modal_elements.ftlh` — `<#macro modal_btn_close_safe>`

**렌더 HTML 구조**:
```html
<button type="button" class="btn btn-sm btn-light modal-btn-close-safe"
        data-func="ModalHistory.pop(); [func]"
        data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="modal"
        title="닫기(변경사항 확인)">
    <i class="bi bi-x"></i>닫기
</button>
```

**파라미터**:
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `func` | string | 닫기 후 실행할 추가 함수 (비어있으면 `ModalHistory.pop()`만) |

**CSS 클래스**: `btn btn-sm btn-light modal-btn-close-safe`

**동작 메커니즘** (`Layout.modalBtnCloseSafe()`):
1. 클릭 시 `isAllowed` 플래그 확인
2. `data-bs-dismiss="modal"` 동적 추가
3. `data-func` 속성 값 `eval()` 실행 (`ModalHistory.pop()` + 커스텀 콜백)
4. 2초 후 `isAllowed = false`, `data-bs-dismiss` 제거

**사용 예시**:
```freemarker
<!-- journal_day_reg_modal — 저장/닫기 -->
<@component.modal_btn_close_safe
   "this.setAttribute('data-journal-day-action','close-modal');
    dF.JournalDayRuntimeService.handleLegacyActionClick(event);" />
```

**현재 Vue 동등**: ✓ composable — `shared/utils/safeModalClose.ts`의 `useSafeModalClose()`가 레거시 2회 클릭 안전 닫기(2초 armed 상태)를 구현한다. 별도 `ModalBtnCloseSafe.vue` 추출은 없으며, 저널 등록·수정 모달 10곳+에서 헤더 X·푸터 닫기/취소에 적용 (`journal/component-spec.md` 모달 정책).

---

### 17. Pagination (페이지네이션)

**Source**: `_pagination.ftlh`

**렌더 HTML 구조**:
```html
<div id="pagination" class="mt-10">
    <!-- paginationInfo 있는 경우 -->
    <div class="row">
        <div class="col-6">
            <!-- 페이지 크기 선택 -->
            <select name="pageSizeSelect" id="pageSizeSelect"
                    class="form-select form-select-solid"
                    onchange="Pagination.fnPage('${currPageNo}', this.value);"
                    data-bs-toggle="tooltip" title="페이지당 데이터 개수">
                <option value="10" selected>10개씩 조회</option>
                <option value="25">25개씩 조회</option>
                <option value="50">50개씩 조회</option>
            </select>
        </div>
        <div class="col-6 pt-3 px-5">
            <!-- 총 건수 -->
            <span class="float-end">총 N건</span>
        </div>
    </div>
    <div class="row paging">
        <ul class="pagination">
            <!-- 처음 버튼 (isFirstPage면 disabled) -->
            <li class="page-item previous [disabled]">...</li>
            <!-- 이전 생략 (prevEllipsis) -->
            <!-- 페이지 번호 목록: prevPrevPrevPageNo ~ nextNextNextPageNo (최대 7개) -->
            <!-- 현재 페이지 (active) -->
            <li class="page-item active"><a class="page-link">N</a></li>
            <!-- 이후 생략 (nextEllipsis) -->
            <!-- 마지막 버튼 (isLastPage면 disabled) -->
            <li class="page-item next [disabled]">...</li>
        </ul>
    </div>
</div>
```

**서버 파라미터** (`paginationInfo` 객체):
| 필드 | 타입 | 설명 |
|------|------|------|
| `currPageNo` | number | 현재 페이지 번호 |
| `totalCnt` | number | 총 건수 |
| `isFirstPage` | boolean | 첫 페이지 여부 |
| `isLastPage` | boolean | 마지막 페이지 여부 |
| `lastPageNo` | number | 마지막 페이지 번호 |
| `prevEllipsis` | boolean | 앞쪽 생략 여부 |
| `nextEllipsis` | boolean | 뒤쪽 생략 여부 |
| `prevPrevPrevPageNo` | number? | 3칸 이전 페이지 |
| `prevPrevPageNo` | number? | 2칸 이전 페이지 |
| `prevPageNo` | number? | 1칸 이전 페이지 |
| `nextPageNo` | number? | 1칸 다음 페이지 |
| `nextNextPageNo` | number? | 2칸 다음 페이지 |
| `nextNextNextPageNo` | number? | 3칸 다음 페이지 |

**생략(ellipsis) 처리**:
- `prevEllipsis`: 1페이지 링크 + `...` 드롭다운 표시 (직접 페이지 번호 입력 input)
- `nextEllipsis`: `...` 드롭다운 + 마지막 페이지 링크 표시

**JS 모듈** (`Pagination`):
```javascript
Pagination.init()        // DOMContentLoaded 시 실행: 현재 pageSize 선택, .page-ellipsis 숫자만 허용
Pagination.fnPage(pageNo, pageSize?)  // #listForm submit
Pagination.fnRepage(pageNo, prevPageSize, newPageSize)  // 페이지 재계산
```

**페이지 크기 옵션**: 10개(기본), 25개, 50개

**CSS 클래스**:
- 컨테이너: `pagination mt-10`
- 페이지 항목: `page-item [previous/next] [active] [disabled]`
- 페이지 링크: `page-link`
- 드롭다운: `menu menu-sub menu-sub-dropdown menu-rounded fw-bold fs-7 w-100px p-4`
- 직접 입력: `w-70px page-ellipsis`

**현재 Vue 동등**: ⚠ 공통 컴포넌트(`Pagination.vue`) 없음. 게시판(`BoardPostList.vue`)·스레드(`JournalThreadList.vue`) 목록에서 `store.totalPages` + `store.fetchList(p-1)` 방식으로 각 컴포넌트 내부 인라인 구현. 서버사이드 렌더 방식에서 Vue store 기반 클라이언트 페이지네이션으로 전환 완료.

---


---

## 공통 컴포넌트 현황 요약

| 컴포넌트 | 레거시 위치 | Vue 동등 | 우선순위 |
|---------|-----------|---------|---------|
| `checkbox` | `_page_elements.ftlh` | MISSING | 중 |
| `mdfable` | `_list_elements.ftlh` | MISSING | 중 |
| `list_comment` | `_list_elements.ftlh` | MISSING (각 컴포넌트 내 인라인) | 높음 |
| `list_dtl_modal` | `_list_elements.ftlh` | MISSING (각 컴포넌트 내 인라인) | 높음 |
| `list_managtr` | `_list_elements.ftlh` | MISSING | 중 |
| `list_managtDt` | `_list_elements.ftlh` | MISSING | 중 |
| `list_file_group` | `_list_elements.ftlh` | MISSING (각 컴포넌트 내 인라인) | 높음 |
| `list_tag` | `_list_elements.ftlh` | MISSING (각 컴포넌트 내 인라인) | 높음 |
| `modal_header` | `_modal_elements.ftlh` | MISSING | 높음 |
| `modal_header_with_back` | `_modal_elements.ftlh` | MISSING | 중 |
| `modal_header_dark` | `_modal_elements.ftlh` | MISSING | 낮음 |
| `modal_btn_save` | `_modal_elements.ftlh` | MISSING | 높음 |
| `modal_btn_modify` | `_modal_elements.ftlh` | MISSING | 중 |
| `modal_btn_delete` | `_modal_elements.ftlh` | MISSING | 중 |
| `modal_btn_close` | `_modal_elements.ftlh` | MISSING | 높음 |
| `modal_btn_close_safe` | `_modal_elements.ftlh` | ✓ (`useSafeModalClose` composable) | — |
| `Pagination` | `_pagination.ftlh` | ⚠ 인라인 구현 (공통 컴포넌트 없음) | — |

---


## 공통 CSS 클래스 패턴

### Blink 애니메이션

| 클래스 | 적용 대상 |
|--------|---------|
| `blink` | 신규 배지(`bg-noti.blink`), 회사생활 안내 아이콘 |
| `blink-slow` | 모달 열기 버튼, 첨부파일 버튼 (느린 깜빡임) |

### 투명도 hover

| 클래스 | 적용 대상 |
|--------|---------|
| `opacity-hover` | 태그명, 모달 아이콘, 파일 아이콘 — hover 시 불투명 |
| `opacity-75` | 관리자 수정 아이콘 |

### 카테고리 배지

```html
<span class="ctgr-span ctgr-gray">카테고리명</span>  <!-- 게시판 목록 -->
<span class="fs-7 text-noti">[카테고리]</span>        <!-- 태그 카테고리 -->
```

### 신규 배지

```html
<div class="badge border-0 text-white bg-noti blink fs-8 ms-2">N</div>
```

### 숨김 테이블 열 (모바일 반응형)

```html
<th class="hidden-table">...</th>
<td class="hidden-table">...</td>
```

`hidden-table` 클래스: `journal.css` 또는 공통 CSS에서 모바일 뷰에서 `display: none` 처리 (추정).

---

## frontend-vue 통합 검수 항목 (2026-05-17)

이 절은 기존 `templates` + `static/vue` 화면을 `app/frontend-vue`로 흡수할 때 누락되면 안 되는 공통 경계를 정리한다. 단순히 화면 파일이 존재하는 것과 legacy 동작이 보존되는 것은 별개로 본다.

### 레이아웃·메뉴 셸

| 항목 | Vue 위치 | 보존 기준 |
|------|----------|-----------|
| 기본 레이아웃 | `app/frontend-vue/src/app/layouts/default/DefaultLayout.vue` | 패키지명은 `layouts/default`로 둔다. `default-layout`처럼 의미가 중복되는 경로명은 쓰지 않는다. |
| 사이드바 메뉴 | `layouts/default/components/sidebar/SidebarMenu.vue`, `SidebarMenuItem.vue` | 첫 진입 화면에서도 사용자가 이동할 수 있는 메뉴가 보여야 한다. 메뉴 accordion은 항상 펼쳐진 상태로 표시하고, `menuLabel`은 최상위 섹션 라벨에만 사용한다. |
| 동적 메뉴 | `app/frontend-vue/src/shared/menu/stores/menu.ts` | `GET /api/menus?mode=USER|MNGR` 결과는 `sidebarVisibleYn=Y`인 사이드바 메뉴만 렌더링 대상으로 보존한다. 서버는 각 메뉴의 `required_perm_key`를 현재 사용자 유효 permission(롤∪그룹 합집합)으로 필터하며, 메뉴 캐시 키는 locale + permission 지문을 사용한다. `admin_yn`은 USER/MNGR 모드 트리 분리에 유지하고, 세부 노출은 permission 단일 경로로 판정한다. `GET /api/menus?mode=USER|MNGR&includeHidden=true` 결과는 `sidebarVisibleYn=N`인 숨김/시스템 메뉴까지 포함한 breadcrumb·화면 설명 메타 원천으로 보존한다. 하드코딩된 1차원 메뉴는 fallback으로만 허용한다. |
| 사용자/관리자 전환 | `Navbar.vue` + `useMenuStore.setMenuMode()` | 관리자 메뉴 모드 진입 기준은 `menu.view.admin` permission(`canUseMngrMenuMode`, 서버 `AuthUtils.hasPermission`)이다. `isMngr`는 fallback. 모드 전환 시 메뉴 캐시를 무효화하고 다시 조회한다. |
| 경로별 메뉴 모드 복구 | `app/router/index.ts` + `useMenuStore.setMenuMode()` | 세션 만료 후 재로그인 redirect, 직접 URL 진입, 새로고침으로 관리자 경로(`/admin/**`)에 들어오면 관리자 권한 계정에 한해 사이드바 메뉴 모드를 `MNGR`로 복구한다. 사용자 경로(`/journal/**`, `/annual/**`, `/thread/**`, `/schedule`, `/board/**`)에 들어오면 사이드바 메뉴 모드를 `USER`로 복구한다. `/my`는 현재 사용자/관리자 메뉴 모드를 유지하는 계정 화면이며, 모드 전환 트리거로 쓰지 않는다. |
| legacy URL 매핑 | `app/frontend-vue/src/shared/utils/urlMapping.ts` | `.do`/FTL 진입 URL은 Vue route로 흡수한다. 서버 redirect와 클라이언트 라우터가 같은 목적지를 가리켜야 한다. |
| Bootstrap tooltip | `app/frontend-vue/src/main.ts` 전역 `v-tooltip` directive | Vue 렌더링 생명주기에 맞춰 Bootstrap Tooltip 인스턴스를 mount/update/unmount에서 생성·정리한다. Vue 템플릿에서는 `data-bs-toggle="tooltip"`을 직접 반복하지 않고 `v-tooltip` + `title`로 활성화한다. |

### 관리자·계정 화면

| legacy 요구 | Vue route | Vue view/store | 현재 기준 |
|-------------|-----------|----------------|-----------|
| boardGroup 관리자 화면 | `/admin/board-group` | `features/admin/BoardGroupAdminPage.vue`, `features/admin/stores/boardGroup.ts` | 목록/상세/등록·수정/사용여부/삭제 흐름을 legacy와 비교 검수한다. |
| 코드 관리 | `/admin/code` | `features/admin/CodeAdminPage.vue`, `features/admin/stores/codeAdmin.ts` | 코드그룹 + 상세코드 CRUD, 정렬/사용여부 동작을 검수한다. |
| 메뉴 관리 | `/admin/menu` | `features/admin/MenuAdminPage.vue`, `features/admin/MenuAdminTreeNode.vue`, `features/admin/stores/menuAdmin.ts` | tree depth, 사용자/관리자 구분, `submenuExpandType`, `sidebarVisibleYn` 기준 사이드바 표시/숨김 분리, 순서 변경을 legacy 기준으로 검수한다. |
| 사용자 그룹 관리 | `/admin/user-groups` | `features/admin/UserGroupAdminPage.vue`, `features/admin/stores/userGroup.ts` | 그룹 CRUD·멤버십·permission 부여를 검수한다. |
| 계정 관리 | `/admin/users` | `features/admin/UserAdminPage.vue`, `features/admin/stores/userAdmin.ts` | 검색/권한 필터/상세/등록·수정/프로필·고용정보 서브폼을 검수한다. |
| 계정 신청 승인 | `/admin/users?tab=signup` | `UserSignupApprovalList.vue` | 계정 관리 화면의 `계정 신청 승인` 탭. 구 경로 `/user/signup/approval` 은 이 탭으로 리다이렉트된다. |
| 로그 | `/admin/log` | `features/admin/LogAdminPage.vue`, `features/admin/stores/logAdmin.ts` | 운영 로그 목록/검색/상세 모달을 검수한다. |
| `stats_user` | `/admin/log/stats-user` | `LogAdminPage.vue` | 현재 있는 사용자별 통계 + placeholder만 둔다. 없는 기능을 새로 만든 것처럼 표시하지 않는다. |
| 인증 결과 | `/auth/verify-result` | `VerifyResultPage.vue` | legacy `verify_success.ftlh`, `verify_failure.ftlh`를 단일 Vue 결과 화면으로 흡수한다. success/failure는 query/status로 구분한다. |

### 정적 리소스·폰트

| 항목 | 기준 |
|------|------|
| legacy font CSS | `app/frontend-vue/index.html`에서 `/css/font.css`를 로드한다. |
| font 파일 | Spring Boot 정적 리소스의 `/font/**` 경로를 그대로 사용한다. |
| 개발 서버 | `vite.config.ts`에서 `/css`, `/font`를 backend로 proxy한다. |
| 기본 폰트 검수 | legacy `font.css`의 `NotoSans`, `Pretendard`, `Poppins` face가 브라우저 Network/Computed 탭에서 실제 로드되는지 확인한다. |

### attachable 흡수 기준

| attachable 영역 | Vue 기준 |
|-----------------|----------|
| 댓글 등록/수정 | `useAttachableModalStore.openCommentRegist/openCommentModify` + `CommentRegistModal.vue` |
| 댓글 목록 | `openCommentList` + `CommentListModal.vue` |
| 이력 | `openHistory` + `HistoryModal.vue`. 각 이력 카드에 텍스트 복사 버튼 구현 완료 |
| 관련글 추가 | `openRelated` + `RelatedContentAddModal.vue` — RELATED 직접 연결 ✓ |
| FLOW 종단 보기 | 제거 — 저널 스레드 소속으로 수렴 (`docs/migration/journal/component-spec.md`) |
| 태그 목록 | `openTagList` + `JournalTagListModal.vue` |
| 태그 프로필 | `openTagProfile` + `JournalTagProfileModal.vue` |
| 파일 그룹 | `FileGroup` 계열 컴포넌트로 흡수한다. FTLH가 owner인 상태를 최종 상태로 보지 않는다. |

### Prefix 기반축

| 영역 | 구현 상태 | 계약 |
|------|-----------|------|
| Prefix 영속 모델 | ✓ | `prefix_scope.scope_type`은 `PERSONAL/GLOBAL`이며 개인은 `(user_id, content_type)`, 게시판은 `(GLOBAL, boardKey)`가 목록 경계의 SSOT다. `created_by`는 감사 정보로만 쓰며 삭제 대신 `active_yn=N`으로 비활성화한다. |
| 콘텐츠 연결 모델 | ✓ | 개인 저널과 게시글은 `prefix_content(ref_id, ref_content_type, prefix_id)` attachable 연결로 콘텐츠당 활성 Prefix 0..1을 표현한다. 게시글의 `ref_content_type`은 동적 boardKey이며 Prefix 선택은 공통 연결 모델에서 관리한다. |
| Prefix 관리 서비스 | ✓ | 개인·게시판이 참조하는 PrefixScope 공통 경계에서 비활성 포함 이름 중복 금지, `sort_order,id` 정렬, `#RRGGBB` 색상과 Scope 소속을 서버에서 검증한다. 개인 API는 `/api/my/prefixes`, 게시판 관리자 API는 `/api/board/groups/{id}/prefixes`다. |
| 게시판 GLOBAL Scope | ✓ | 게시판 ID로 boardKey를 확정하고 첫 Prefix 등록 시 `GLOBAL + boardKey` Scope를 lazy 생성한다. 게시판별 목록은 독립적이며 다른 게시판에 변경을 전파하지 않는다. |
| 개인 Prefix 관리 UI | ✓ | 내 설정 `/my/prefixes` 초기 화면의 작은 `저널` 도메인 헤더 아래 Scope 존재 여부와 무관한 `일기 챕터 / 노트 챕터 / 일기 / 꿈 / 노트 / 스레드` 고정 6행을 표시한다. 화면 카탈로그는 도메인 그룹별 대상 배열로 구성해 다른 도메인을 같은 형식으로 아래에 추가할 수 있고, 영속 Prefix 구조는 바꾸지 않는다. 행을 누르면 실제 `JOURNAL_CHAPTER_DIARY / JOURNAL_CHAPTER_NOTE / JOURNAL_DIARY / JOURNAL_DREAM / JOURNAL_NOTE / JOURNAL_THREAD`별 관리 모달을 열어 이름·색상·정렬·활성 상태를 관리하며 서버 응답을 store SSOT로 사용한다. 초기 화면은 목록과 말머리 미리보기를 조회하지 않고, 모달 종료 시 목록과 진행 중 조회를 비우며 늦은 이전 응답을 폐기한다. 관리 등록·수정·활성 변경 성공 시 해당 `contentType`의 공통 활성 선택지 캐시만 무효화하고, 다음 소비 화면 진입이 서버 확정 목록을 다시 조회한다. 챕터의 attachable 정체성은 `JOURNAL_CHAPTER`를 유지하되 일기 챕터와 노트 챕터는 서로 다른 개인 Scope를 사용한다. 시스템 요약(`summaryYn=Y`)과 DREAM 챕터는 Prefix를 갖지 않는다. 일기·꿈·노트 엔트리도 각각의 개인 Prefix 목록을 등록·수정·조회·표시에 사용한다. NOTE 엔트리는 공통 `journal_entry`의 실제 연결 타입 `JOURNAL_DIARY`를 유지하되 선택 Scope만 소속 NOTE 챕터에서 `JOURNAL_NOTE`로 확정한다. |
| 게시판 Prefix 관리 UI | ✓ | `BoardPrefixManagementModal.vue`와 `stores/boardPrefixes.ts`가 게시판 관리 행 문맥에서 boardKey별 비활성 포함 Prefix를 관리한다. Scope ID·공유·연결 변경 UI는 노출하지 않는다. |
| 스레드 소비 | ✓ | DTO·저장·목록·후보·상세·내보내기·Vue 편집/필터/표시는 단일 `prefix`/`prefixId` 계약을 사용한다. 쓰기 MapStruct는 읽기용 중첩 `prefix`를 무시하고 서버가 검증한 `prefixId`만 FK에 반영한다. 비활성 과거 Prefix는 표시를 유지하고 신규 선택은 거부한다. |
| 게시판 소비 | ✓ | 게시글 DTO·저장·목록·상세·Vue 편집/필터/표시는 `GLOBAL + boardKey` Scope의 단일 `prefix`/`prefixId` 계약을 사용한다. 선택은 `prefix_content`에 저장하고 `PrefixEmbed`로 조립하며 검색도 같은 연결을 사용한다. 활성 선택지만 신규 선택하고 비활성 과거 Prefix는 표시·동일 선택 유지를 허용한다. |
| 스레드 편집 UX | ✓ | 공용 편집 폼에서 Prefix를 빠르게 추가하고 서버 옵션 재조회 후 현재 선택에 반영한다. 전체 관리는 `/my/prefixes` 탭으로 연결한다. |

### static/vue/global 흡수 기준

`static/vue/global`의 역할은 `app/frontend-vue/src` 아래의 composable/store/service/style 경계로 옮긴다. 최종 기준은 다음과 같다.

- 화면이 `static/vue/global`을 직접 import하지 않는다.
- 공통 상태는 Pinia store 또는 composable로 들어간다.
- 공통 Ajax/URL/i18n/helper는 `src/services`, `src/utils`, `src/core` 중 하나의 명확한 경계로 들어간다.
- legacy global을 임시로 참조해야 하면 migration spec에 남은 의존성을 기록하고 제거 기준을 둔다.
