# 인증/사용자 화면 스펙 (Auth & User Screen Spec)

> 라우트: `app/frontend-vue/src/app/router/index.ts`
> 전체 라우트 목록: `docs/migration/vue-screen-overview.md`

## 라우트·화면 매핑

| 화면 | Vue route | Vue view | 레이아웃 | 구현 |
|------|-----------|----------|---------|------|
| 로그인 | `/sign-in` | `SignIn.vue` | AuthLayout | ✓ |
| 계정 신청 | `/user/signup` | `UserSignupPage.vue` | AuthLayout | ✓ |
| 계정 인증 결과 | `/auth/verify-result` | `VerifyResultPage.vue` | AuthLayout | ✓ |
| 내 정보 | `/my` | `UserMyPage.vue` | DefaultLayout | ✓ |

> 계정 신청 승인 관리(`/user/signup/approval`)는 관리자 기능으로 `admin/screen-spec.md` 참조.

---

## 로그인 (`sign-in`)

**Vue view**: `app/frontend-vue/src/features/auth/SignIn.vue`  
**스토어**: `shared/auth/stores/auth.ts`

**기능**:
- ID/PW 폼 로그인 → `POST /api/auth/login`
- Google OAuth2 소셜 로그인 → `/oauth2/authorization/google` (팝업)
- Naver OAuth2 소셜 로그인 → `/oauth2/authorization/naver` (팝업)
- 로그인 실패 메시지 표시
- 로그인 실패 횟수 및 계정 잠금 정책은 `POST /api/auth/login` JSON 로그인에서도 적용
- 로그인 실패 응답이 `isDupIdLogin`이면 중복 로그인 확인 다이얼로그 표시 후 확인 시 재로그인, 취소 시 `/api/auth/expire-session` 호출
- 로그인 실패 응답이 `isCredentialExpired` 또는 `needsPasswordReset`이면 로그인 비밀번호 변경 모달 오픈
- 로그인 비밀번호 변경 모달 저장 → `POST /api/auth/login-pw-chg`
- 로그인 성공 → `/journal/weekly` 리다이렉트

---

## 계정 신청 (`user-signup`)

**Vue view**: `app/frontend-vue/src/features/auth/UserSignupPage.vue`

**기능**:
- 신규 계정 신청 폼 (이름, 이메일, 비밀번호 등)
- 신청 완료 → `POST /api/user/signup-requests`
- 신청 완료 후 안내 메시지 표시

---

## 계정 인증 결과 (`auth-verify-result`)

**Vue view**: `app/frontend-vue/src/features/auth/VerifyResultPage.vue`

**기능**:
- 이메일 인증 토큰 결과 표시 (레거시 `verify_success.ftlh` + `verify_failure.ftlh` 통합)
- `?status=success` → "인증이 완료되었습니다"
- `?status=failure&message=...` → "인증에 실패했습니다" + 메시지

---

## 내 정보 (`user-my`)

- **Vue SPA**: `/my`
- **Legacy file**: `legacy/templates/view/feature/user/my/user_my_page.ftlh`
- **스토어**: `features/user/stores/userMy.ts`
- **본문 상단**: breadcrumb와 중복되는 `내 정보` 제목은 렌더링하지 않고 안내문과 새로고침 버튼만 표시

### Layout Structure

- 레이아웃: `layout_default.ftlh` (사이드바 없음)
- 툴바: 안내문과 새로고침 버튼
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
| 저장 | 폼 데이터 저장 (닉네임, 이메일, 전화번호 등) |
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
| 비밀번호 변경 | `_user_my_pw_chg_modal.ftlh` | 비밀번호 변경 버튼 클릭 |

### Special behaviors

- `vacation.visible`: `authInfo.hasEcnyDt` 기반 — 입사일 없는 사용자에게는 연차 정보 미표시
- 연차 툴팁: 기본연차(신입 여부 포함) + 근속추가연차 + 프로젝트추가연차 조합 문자열 (`\n` 개행)
- 프로필 이미지 업로드: `onchange="return false;"` — Vue에서 직접 파일 접근 후 처리
- `UserMyPageApp` Vue가 `user_my_module.js` + `user_my_pw_chg_module.js` + `user_my_page.js` 3개 레거시 모듈 완전 대체
