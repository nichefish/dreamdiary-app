# 인증/사용자 화면 스펙 (Auth & User Screen Spec)

> Vue 라우트: `app/frontend-vue/src/app/router/index.ts` / React 라우트: `app/frontend-react/src/router.tsx`
> 전체 라우트 목록: `docs/migration/vue-screen-overview.md`, `docs/migration/react-screen-overview.md`
> 인증/사용자 Vue store의 API 결과 메시지는 서버 `message`를 우선 사용하고, 서버 메시지가 없을 때는 현재 locale의 클라이언트 카탈로그 메시지를 fallback으로 사용한다.

## 라우트·화면 매핑

| 화면 | Vue route | Vue view | React view | 레이아웃 | Vue | React |
|------|-----------|----------|------------|---------|-----|-------|
| 로그인 | `/sign-in` | `SignIn.vue` | `SignInPage.tsx` | AuthLayout | ✓ | ✓ |
| 계정 신청 | `/user/signup` | `UserSignupPage.vue` | — | AuthLayout | ✓ | ❌ |
| 계정 인증 결과 | `/auth/verify-result` | `VerifyResultPage.vue` | — | AuthLayout | ✓ | ❌ |
| 내 설정 | `/my/profile`, `/my/security`, `/my/journal`, `/my/prefixes` | `UserMyPage.vue` + `UserMy*Tab.vue` | — | DefaultLayout | ✓ | ❌ |

> 계정 신청 승인 관리는 계정 관리 화면의 탭(`/admin/users?tab=signup`)으로 흡수됐다. 관리자 기능이므로 `admin/screen-spec.md` 참조.

---

## 로그인 (`sign-in`)

**Vue view**: `app/frontend-vue/src/features/auth/SignIn.vue`  
**Vue 스토어**: `shared/auth/stores/auth.ts`

**React view**: `app/frontend-react/src/features/auth/SignInPage.tsx`  
**React 스토어**: `shared/auth/authStore.ts` (zustand)  
**React 레이아웃**: `app/layouts/AuthLayout.tsx`  
**Spring 서빙**: `/react-app/**` → `static/react-app/` SPA fallback (`WebMvcContextConfig`)

**기능**:
- ID/PW 폼 로그인 → `POST /api/auth/login`
- POST /api/auth/login internal server errors return HTTP 500 with a login-scoped error message and must not be classified as login-required/session-expired.
- Vue auth verification treats only HTTP 401 from `/api/auth/get-auth-account` as unauthenticated/session-expired; HTTP 403, HTTP 500, and network failures surface as auth verification/runtime errors and must not purge auth state as a login-required diagnosis. 정상 인증 결과는 새로고침 시 초기화되는 메모리에 15초간 신선한 상태로 보존하고, 같은 구간의 라우트 이동은 서버 조회를 생략한다. 로그인·프로필 변경·팝업 사전 확인은 `force` 검증을 사용한다.
- Google OAuth2 소셜 로그인 → `/oauth2/authorization/google` (팝업)
- Naver OAuth2 소셜 로그인 → `/oauth2/authorization/naver` (팝업)
- 로그인 실패 메시지 표시
- 로그인 실패 횟수 및 계정 잠금 정책은 `POST /api/auth/login` JSON 로그인에서도 적용
- 로그인 실패 응답이 `isDupIdLogin`이면 중복 로그인 확인 다이얼로그 표시 후 확인 시 재로그인, 취소 시 `/api/auth/expire-session` 호출
- 로그인 실패 응답이 `isCredentialExpired` 또는 `needsPasswordReset`이면 로그인 비밀번호 변경 모달 오픈
- 로그인 비밀번호 변경 모달 저장 → `POST /api/auth/login-pw-chg`
- 로그인 비밀번호 변경은 `auth_policy.password_history_count` 기준으로 현재 비밀번호와 최근 이력 재사용을 막는다. 정책값 기본은 `2`이며, `0`이면 재사용 검사를 수행하지 않는다.
- 별도 `redirect`가 없는 로그인 성공 → `/journal/daily` 일간 뷰로 이동

---

## 계정 신청 (`user-signup`)

**Vue view**: `app/frontend-vue/src/features/auth/UserSignupPage.vue`

**기능**:
- 신규 계정 신청 폼 (이름, 이메일, 비밀번호 등)
- 신청 완료 → `POST /api/user/signup-requests`
- 신청 완료 후 안내 메시지 표시
- **i18n**: 화면 내 모든 UI 텍스트는 `useLocaleStore.t()` 카탈로그 키로 표시 (`user.signup.*`, `user.form.*`, `user.profile.*`, `user.emplym.*`). 이메일 필드 레이블도 `user.form.email`을 사용하며 관련 messages_ko/en.properties 키를 동일하게 제공한다.

---

## 계정 인증 결과 (`auth-verify-result`)

**Vue view**: `app/frontend-vue/src/features/auth/VerifyResultPage.vue`

**기능**:
- 이메일 인증 토큰 결과 표시 (레거시 `verify_success.ftlh` + `verify_failure.ftlh` 통합)
- `?status=success` → "인증이 완료되었습니다"
- `?status=failure&message=...` → "인증에 실패했습니다" + 메시지
- **i18n**: 화면 내 모든 UI 텍스트는 `useLocaleStore.t()` 카탈로그 키로 표시 (`auth.verify.*`). 관련 messages_ko/en.properties 키 일괄 정의 완료.

---

## 내 설정 (`user-my`)

- **Vue SPA**: `/my/profile`, `/my/security`, `/my/journal`, `/my/prefixes` (`/my`는 `/my/profile`로 redirect)
- **Legacy file**: `legacy/templates/view/feature/user/my/user_my_page.ftlh`
- **스토어**: `features/user/stores/userMy.ts`, `features/user/stores/userCategories.ts`
- **본문 상단**: breadcrumb와 중복되는 제목·설명문 및 별도 새로고침 버튼은 렌더링하지 않는다. 프로필 이미지·닉네임·계정·역할 공통 정체성 헤더와 URL 기반 탭을 표시한다.
- **메뉴 모드**: `/my` 진입은 사용자/관리자 메뉴 모드를 전환하지 않는다. 관리자 모드에서 프로필 메뉴로 들어오면 관리자 사이드바를 유지하고, 사용자 모드에서 들어오면 사용자 사이드바를 유지한다.

### URL 기반 탭

| 탭 | Route | 컴포넌트 | 동작 |
|---|---|---|---|
| 내 정보 | `/my/profile` | `UserMyProfileTab.vue` | 개인 연락처·프로필은 조회·수정하고 재직 정보는 조회 전용으로 표시한다. |
| 보안 | `/my/security` | `UserMySecurityTab.vue` | 허용 IP를 표시하고 비밀번호 변경 모달을 연다. |
| 저널 설정 | `/my/journal` | `UserMyJournalTab.vue` | 저널 일자 공통 진입점에서 사용할 일간·주간·월간 기본 보기를 조회·저장한다. |
| 말머리 관리 | `/my/prefixes` | `UserMyPrefixesTab.vue` | 초기 화면의 작은 `저널` 도메인 헤더 아래 `일기 챕터 / 노트 챕터 / 일기 / 꿈 / 노트 / 스레드` 고정 대상 6행을 표시하고, 행을 누르면 해당 개인 Prefix 목록의 생성·수정·정렬·색상·활성 상태를 관리하는 모달을 연다. |

- 탭은 로컬 상태가 아니라 Vue Router 하위 route로 유지하므로 새로고침·직접 URL 진입·브라우저 앞뒤 이동 시 같은 탭을 복원한다.
- 공통 헤더의 보안 버튼은 `/my/security`로 이동하며 비밀번호 모달을 직접 열지 않는다.
- 프로필 이미지 변경·삭제는 탭과 무관하게 공통 헤더에서 처리하고 사용자 정보와 인증 상태를 함께 갱신한다.
- 내 정보 수정은 로그인 사용자 본인의 `nickname`, `phoneNumber`, `profile.brthdy`, `profile.lunarYn`, `profile.proflCn`만 허용한다. `username`, 역할·허용 IP, 재직 정보는 요청 계약에 포함하지 않는다.
- 생년월일과 달력 구분은 한 행에 표시한다. 조회 상태는 날짜 뒤에 `(양력|음력)`을 붙이고, 편집 상태는 날짜 입력 오른쪽의 토글을 사용해 OFF `양력`·ON `음력`을 명시한다. 모바일에서는 날짜 입력과 토글을 세로로 배치한다.
- 계정 이메일은 OAuth2 사용자 식별에도 사용되므로 일반 프로필 저장에서 변경하지 않는다. 이메일 변경은 현재 비밀번호 확인·중복 검사·새 주소 검증을 포함하는 별도 보안 기능이 확정될 때 추가한다.
- 저장 성공 후 내 정보와 인증 상태를 다시 조회해 공통 정체성 헤더·계정 메뉴의 닉네임을 서버 확정 상태로 갱신한다.
- 저널 설정은 `GET /api/journal/settings/me`의 서버 확정값을 표시하고 `PUT /api/journal/settings/me`로 선택값만 저장한다. 저장 버튼은 선택값이 서버 확정값과 다를 때 활성화한다.
- 말머리는 콘텐츠당 0개 또는 1개다. 관리 화면은 `(user, content_type)`별 개인 Scope를 `JOURNAL_CHAPTER_DIARY`, `JOURNAL_CHAPTER_NOTE`, `JOURNAL_DIARY`, `JOURNAL_DREAM`, `JOURNAL_NOTE`, `JOURNAL_THREAD`로 분리한다. 챕터 말머리는 일기 챕터·노트 챕터로 나뉘며, 챕터의 attachable 정체성(`prefix_content.ref_content_type`)은 `JOURNAL_CHAPTER`로 유지한다.
- 대상 6행은 Scope 존재 여부와 무관하게 `저널` 도메인 그룹 안에 `일기 챕터 / 노트 챕터 / 일기 / 꿈 / 노트 / 스레드` 순으로 항상 표시한다. 이후 다른 도메인은 같은 화면 카탈로그의 별도 그룹으로 아래에 추가한다. 초기 화면에서는 각 목록을 미리 조회하거나 말머리 미리보기를 표시하지 않고, 행을 눌러 관리 모달을 열 때 해당 `contentType` 목록만 조회한다. Prefix가 하나도 없는 모달은 빈 상태와 추가 버튼을 표시하며 최초 등록 시 서버가 Scope를 lazy 생성한다.
- 이번 화면 SAVEPOINT는 여섯 목록 관리까지만 포함하고 실제 챕터·일기·꿈·노트 등록·수정 소비 연결은 후속 범위다.
- Prefix는 해당 Scope의 평면 목록이며 이름·색상·정렬·활성 상태를 관리한다. 비활성화는 기존 콘텐츠 선택을 보존한다.

### Layout Structure

- 레이아웃: `layout_default.ftlh` (사이드바 없음)
- 툴바: 별도 툴바 없음. `/my` 화면 진입 시 사용자 정보를 조회하고, 프로필 이미지 변경/삭제 후에는 내부적으로 사용자 정보와 인증 상태를 갱신한다.
- 메인 영역:
  - Vue 마운트 루트: `#user_my_app`
  - 컨텐츠 div: `#user_my_page_div` (Vue `UserMyPageApp` 텔레포트 대상)
  - 히든 폼: `#procForm` (GET, `id`, `userProfileId` hidden)
  - 프로필 이미지 폼 템플릿: `#proflImageTemplate` (hidden, 파일 업로드용)
- 모달: 비밀번호 변경 모달 (`_user_my_pw_chg_modal.ftlh`)

### Key UI Elements

| Element | Type | Legacy class/id | Data source | Notes |
|---------|------|----------------|-------------|-------|
| Vue 마운트 루트 | `<div>` | `#user_my_app` | `UserMyPageApp` | Vue 앱 마운트 |
| 컨텐츠 영역 | `<div>` | `#user_my_page_div` | Vue 텔레포트 | 내 정보 화면 렌더 |
| 프로필 이미지 폼 | `<form id="profllImgForm">` | `#proflImageTemplate` (hidden) | `input[type=file]` | `.png`, `.jpg`, `.jpeg` 허용 |
| 파일 인풋 | `<input type="file" id="fileGroup0">` | `#fileGroup0` | 프로필 이미지 | accept: `.png, .jpg, .jpeg` |

### Action Buttons & Interactions

Vue `UserMyPageApp` 내부에서 처리. 레거시 모듈(`user_my_module.js`, `user_my_pw_chg_module.js`, `user_my_page.js`)을 대체.

| Action | 기대 동작 |
|--------|----------|
| 수정 | 내 정보 탭의 개인 연락처·프로필 표시를 편집 폼으로 전환한다. 재직 정보는 조회 전용으로 유지한다. |
| 저장 | 「내 정보를 저장하시겠습니까?」 확인 후 로그인 사용자 본인의 닉네임·개인 연락처·생년월일·음력 여부·자기소개를 저장한 뒤 서버 상태를 다시 조회한다. 확인을 취소하면 API를 호출하지 않고 편집 상태를 유지한다. |
| 취소 | 저장하지 않고 조회 상태로 돌아가며 서버에서 조회한 값을 복원한다. |
| 말머리 관리 모달 | `저널` 그룹의 `일기 챕터 / 노트 챕터 / 일기 / 꿈 / 노트 / 스레드` 행을 누르면 해당 `(user, content_type)` 목록만 조회해 관리 모달을 연다. 모달을 닫을 때 편집 중이면 미저장 변경 폐기 확인을 표시하고, 취소하면 현재 모달과 편집 상태를 유지한다. |
| 프로필 이미지 업로드 | 파일 선택 후 `/file/fileUploadAjax.do` 업로드 |
| 프로필 이미지 삭제 | 이미지 제거 |
| 비밀번호 변경 | 비밀번호 변경 모달 오픈 |

### Data Displayed

`#user_my_page_data` (JSON script tag)로 모든 데이터 전달:

**user 객체**:
- `id`, `username`, `nickname`, `email`, `phoneNumber`, `profileImageUrl`
- `userRoles[].roleKey`, `userRoles[].roleName`
- `isAllowedIpY`: IP 허용 여부
- `allowedIpList[].allowedIp`: 허용 IP 목록
- `userInfo.userProfileId`, `userInfo.cmpyNm`, `userInfo.teamNm`, `userInfo.emplymNm`
- `userInfo.rankNm`, `userInfo.rankCd`
- `userInfo.apntcYn`: 수습 여부 (`Y`/`N`)
- `userInfo.ecnyDt`: 입사일
- `userInfo.retireYn`, `userInfo.retireDt`
- `userInfo.brthdy`: 생년월일
- `userInfo.acntBank`, `userInfo.acntNo`: 계좌 정보
- `userInfo.itemList[].itemNm`, `.itemCn`, `.itemDc`: 추가 항목 목록

**vacation 객체** (입사일 있는 경우만 표시):
- `visible`: `authInfo.hasEcnyDt`
- `statsYy`, `bgnDt`, `endDt`: 연차 기준 연도/기간
- `total`, `used`, `remains`: 총/사용/잔여 연차
- `tooltip`: 연차 상세 내용 (기본연차, 신입연차, 근속연차, 프로젝트연차 조합)

**labels 객체**: 모든 UI 레이블 i18n 텍스트 포함

**errorMsg**: 서버 에러 메시지 (있을 경우 표시)

### Modals opened from this page

| Modal | 파일 | 열리는 조건 |
|-------|------|-----------|
| 비밀번호 변경 | `UserMySecurityTab.vue` | 보안 탭의 비밀번호 변경 버튼 클릭 |

### Special behaviors

- `vacation.visible`: `authInfo.hasEcnyDt` 기반 — 입사일 없는 사용자에게는 연차 정보 미표시
- 연차 툴팁: 기본연차(신입 여부 포함) + 근속추가연차 + 프로젝트추가연차 조합 문자열 (`\n` 개행)
- 프로필 이미지 업로드: `onchange="return false;"` — Vue에서 직접 파일 접근 후 처리
- `UserMyPageApp` Vue가 `user_my_module.js` + `user_my_pw_chg_module.js` + `user_my_page.js` 3개 레거시 모듈 완전 대체
