<template>
  <!--begin::로그인 패널-->
  <div class="w-lg-500px rounded mt-20 p-10 p-lg-15 mx-auto my-auto">
    <!--begin::도메인 표시-->
    <div class="d-flex justify-content-center fs-4 mb-15 text-secondary fw-bold ls-1">
      <i class="bi bi-cloud-moon fs-2"></i>
      <span class="px-2">{{ domain }}</span>
      <i class="bi bi-stars fs-2"></i>
    </div>
    <!--end::도메인 표시-->

    <!--begin::로그인 폼-->
    <form name="loginForm" id="loginForm" class="w-100" @submit.prevent="handleLogin">
      <!--begin::아이디-->
      <div class="opacity-75">
        <label class="form-label fs-6 text-secondary fw-bolder" for="username">아이디</label>
        <input
          type="text"
          name="username"
          id="username"
          v-model.trim="form.username"
          class="form-control form-control-lg"
          placeholder="아이디를 입력하세요."
          autocomplete="off"
          maxlength="20"
        />
      </div>
      <div class="h-15px mt-1 mb-5">
        <span id="username_validate_span" class="text-danger">{{ fieldErrors.username }}</span>
      </div>
      <!--end::아이디-->

      <!--begin::비밀번호-->
      <div class="opacity-75">
        <label class="form-label text-secondary fw-bolder fs-6 mb-1" for="password">비밀번호</label>
        <input
          type="password"
          name="password"
          id="password"
          v-model="form.password"
          class="form-control form-control-lg"
          :disabled="passwordDisabled"
          placeholder="비밀번호를 입력하세요."
          maxlength="20"
          autocomplete="off"
        />
      </div>
      <div class="h-15px mt-1 mb-3">
        <span id="password_validate_span" class="text-danger">{{ fieldErrors.password }}</span>
      </div>
      <!--end::비밀번호-->

      <!--begin::에러 메시지-->
      <div v-if="sessionExpiredNotice" class="alert alert-warning d-flex align-items-start gap-3 py-3 mb-5">
        <i class="bi bi-exclamation-triangle fs-3"></i>
        <div>
          <div class="fw-bold">로그인이 풀렸습니다.</div>
          <div class="fs-7">{{ sessionExpiredNotice }}</div>
        </div>
      </div>
      <div class="text-left">
        <span id="errorMsgSpan" class="text-danger">
          <template v-for="(line, index) in errorMsgLines" :key="index">
            {{ line }}<br v-if="index < errorMsgLines.length - 1" />
          </template>
        </span>
      </div>
      <!--end::에러 메시지-->

      <!--begin::로그인 상태 유지-->
      <div class="me-2 float-end">
        <label class="form-check form-check-custom form-check-solid" for="rememberMe">
          <span class="form-check-label text-secondary me-2">로그인 상태 유지</span>
          <input
            type="checkbox"
            class="form-check-input cursor-pointer"
            id="rememberMe"
            name="rememberMe"
            v-model="form.rememberMe"
          />
        </label>
      </div>
      <!--end::로그인 상태 유지-->

      <!--begin::버튼 영역-->
      <div class="d-flex flex-column text-center mb-4 mt-12 gap-2">
        <!--begin::로그인 버튼-->
        <button
          type="submit"
          class="btn btn-lg btn-light-primary opacity-75 w-100"
          :disabled="isLoading"
          :aria-busy="isLoading"
        >
          <!-- 변경 전: indicator-label/indicator-progress v-if 전환 — 테마 CSS가 progress 를 숨겨 글자만 사라지고 버튼이 납작해짐 -->
          <span class="d-inline-flex align-items-center justify-content-center gap-2 w-100">
            <span>로그인</span>
            <span
              v-show="isLoading"
              class="spinner-border spinner-border-sm"
              role="status"
              aria-hidden="true"
            ></span>
          </span>
        </button>
        <!--end::로그인 버튼-->

        <!--begin::OAuth2 소셜 로그인-->
        <div class="d-flex gap-2">
          <button
            type="button"
            class="btn btn-lg btn-light-danger opacity-75 w-100"
            @click="openOAuthPopup('/oauth2/authorization/google')"
          >
            <span class="d-flex-center indicator-label gap-2">
              <i class="bi bi-google blink"></i>
              <span>Google 계정</span>
            </span>
          </button>
          <button
            type="button"
            class="btn btn-lg btn-light-success opacity-75 w-100"
            @click="openOAuthPopup('/oauth2/authorization/naver')"
          >
            <span class="d-flex-center indicator-label gap-2">
              <i class="bi bi-naver blink"></i>
              <span>Naver 계정</span>
            </span>
          </button>
        </div>
        <!--end::OAuth2 소셜 로그인-->
      </div>
      <!--end::버튼 영역-->

      <!--begin::회원가입-->
      <div class="d-flex justify-content-end mb-5">
        <button
          type="button"
          class="badge btn btn-sm btn-light-primary badge-outlined btn-outlined fw-light opacity-75 blink"
          @click="goUserSignup"
        >
          <i class="bi bi-person-plus-fill blink"></i>사용자 계정 신청
        </button>
      </div>
      <!--end::회원가입-->

      <!--begin::저작권-->
      <div class="text-end mb-5 text-secondary fs-8">
        2024. nichefish. All rights reserved.
      </div>
      <!--end::저작권-->
    </form>
    <!--end::로그인 폼-->
  </div>
  <!--end::로그인 패널-->

  <!--begin::로그인 비밀번호 변경 모달-->
  <div
    ref="passwordChangeModalEl"
    class="modal fade"
    id="login_pw_chg_modal"
    tabindex="-1"
    role="dialog"
    aria-hidden="true"
    data-bs-keyboard="false"
    data-bs-backdrop="static"
  >
    <div class="modal-dialog modal-dialog-centered modal-md" role="document">
      <div class="modal-content">
        <div class="modal-header bg-dark">
          <h5 class="modal-title text-white">비밀번호 변경</h5>
          <button type="button" class="btn-close btn-close-white" aria-label="닫기" @click="closePasswordChangeModal"></button>
        </div>
        <div class="modal-body">
          <form name="loginPwChgForm" id="loginPwChgForm" class="form" @submit.prevent="submitPasswordChange">
            <input type="hidden" name="username" id="loginUsername" :value="passwordChangeUsername" />
            <div class="row">
              <div class="col-xl-12 text-danger">
                <template v-for="(line, index) in errorMsgLines" :key="index">
                  {{ line }}<br v-if="index < errorMsgLines.length - 1" />
                </template>
              </div>
            </div>
            <div class="row mb-5">
              <div class="col-xl-3">
                <div class="col-form-label text-center fs-6 fw-bold">
                  <label for="currPw">현재 비밀번호</label>
                </div>
              </div>
              <div class="col-xl-9 text-start">
                <input
                  type="password"
                  name="currPw"
                  id="currPw"
                  class="form-control required"
                  maxlength="20"
                  v-model="passwordChangeForm.currPw"
                />
                <div id="currPw_validate_span" class="text-danger">{{ passwordChangeErrors.currPw }}</div>
              </div>
            </div>
            <div class="row">
              <div class="col-xl-3">
                <div class="col-form-label text-center fs-6 fw-bold">
                  <label for="newPw">새 비밀번호</label>
                </div>
              </div>
              <div class="col-xl-9 text-start">
                <input
                  type="password"
                  name="newPw"
                  id="newPw"
                  class="form-control required"
                  maxlength="20"
                  v-model="passwordChangeForm.newPw"
                />
                <div class="fs-8 form-text text-noti">
                  영문, 숫자, 특수문자를 조합해 입력하세요.
                </div>
                <div id="newPw_validate_span" class="text-danger">{{ passwordChangeErrors.newPw }}</div>
              </div>
            </div>
            <div class="row mb-5">
              <div class="col-xl-3">
                <div class="col-form-label text-center fs-6 fw-bold">
                  <label for="newPwCf">새 비밀번호 확인</label>
                </div>
              </div>
              <div class="col-xl-9 text-start">
                <input
                  type="password"
                  name="newPwCf"
                  id="newPwCf"
                  class="form-control required"
                  maxlength="20"
                  v-model="passwordChangeForm.newPwCf"
                />
                <div id="newPwCf_validate_span" class="text-danger">{{ passwordChangeErrors.newPwCf }}</div>
              </div>
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-primary" :disabled="isPasswordChanging" @click="submitPasswordChange">
            <span v-if="isPasswordChanging" class="spinner-border spinner-border-sm me-1"></span>
            저장
          </button>
          <button type="button" class="btn btn-light" @click="closePasswordChangeModal">닫기</button>
        </div>
      </div>
    </div>
  </div>
  <!--end::로그인 비밀번호 변경 모달-->
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { Modal } from "bootstrap";
import axios from "axios";
import { useAuthStore } from "@/shared/auth/stores/auth";
import { swalConfirm } from "@/shared/utils/swal";

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

/** Url.DOMAIN 에 해당하는 값 — 인증 전 화면이므로 API 호출 대신 상수 사용 */
const domain = "dreamdiary.nicheblog.io";

const form = ref({ username: "", password: "", rememberMe: false });
const isLoading = ref(false);
const passwordDisabled = ref(false);
const fieldErrors = ref<{ username?: string; password?: string }>({});
const errorMsgLines = ref<string[]>([]);
const sessionExpiredNotice = ref("");
const passwordChangeModalEl = ref<HTMLElement | null>(null);
let passwordChangeModal: Modal | null = null;
const passwordChangeUsername = ref("");
const passwordChangeToken = ref("");
const isPasswordChanging = ref(false);
const passwordChangeForm = reactive({
  currPw: "",
  newPw: "",
  newPwCf: "",
});
const passwordChangeErrors = reactive({
  currPw: "",
  newPw: "",
  newPwCf: "",
});

/** 서버 오류 메시지의 줄바꿈 마크업을 표시용 배열로 변환한다. */
function splitErrorMsg(errorMsg: string): string[] {
  return errorMsg
    .split(/(?:&lt;br\s*\/?&gt;|<br\s*\/?>)/gi)
    .map((line) => line.trim())
    .filter(Boolean);
}

/** 로그인 오류 메시지를 줄 단위로 표시한다. */
function setLoginErrorLines(messages: string[]): void {
  errorMsgLines.value = messages.flatMap((message) => splitErrorMsg(message));
}

/** OAuth2 소셜 로그인 팝업을 연다. */
function openOAuthPopup(url: string): void {
  const popup = window.open(url, "Authorization", "width=540,height=720,top=0,left=270");
  if (popup) popup.focus();
}

/** 회원가입 페이지로 이동한다. */
function goUserSignup(): void {
  router.push("/user/signup");
}

/** 로그인 비밀번호 변경 폼을 초기화한다. */
function resetPasswordChangeForm(): void {
  passwordChangeForm.currPw = "";
  passwordChangeForm.newPw = "";
  passwordChangeForm.newPwCf = "";
  passwordChangeErrors.currPw = "";
  passwordChangeErrors.newPw = "";
  passwordChangeErrors.newPwCf = "";
}

/** 로그인 실패 후 필요한 경우 비밀번호 변경 모달을 연다. */
function openPasswordChangeModal(username: string): void {
  passwordChangeUsername.value = username;
  form.value.username = username;
  passwordChangeToken.value = authStore.loginAction?.passwordToken ?? "";
  console.info("[SignIn] password change required after login failure.", {
    username,
    isCredentialExpired: authStore.loginAction?.isCredentialExpired === true,
    needsPasswordReset: authStore.loginAction?.needsPasswordReset === true,
  });
  resetPasswordChangeForm();
  passwordChangeModal?.show();
}

/** 로그인 비밀번호 변경 모달을 닫는다. */
function closePasswordChangeModal(): void {
  passwordChangeModal?.hide();
}

/** 로그인 비밀번호 변경 폼을 검증한다. */
function validatePasswordChangeForm(): boolean {
  passwordChangeErrors.currPw = "";
  passwordChangeErrors.newPw = "";
  passwordChangeErrors.newPwCf = "";

  if (!passwordChangeForm.currPw) passwordChangeErrors.currPw = "필수 값을 입력하세요.";
  if (!passwordChangeForm.newPw) passwordChangeErrors.newPw = "필수 값을 입력하세요.";
  if (!passwordChangeForm.newPwCf) passwordChangeErrors.newPwCf = "필수 값을 입력하세요.";
  if (passwordChangeErrors.currPw || passwordChangeErrors.newPw || passwordChangeErrors.newPwCf) return false;

  if (passwordChangeForm.newPw !== passwordChangeForm.newPwCf) {
    passwordChangeErrors.newPwCf = "새 비밀번호 확인 값이 일치하지 않습니다.";
    return false;
  }
  if (passwordChangeForm.newPw.length < 9 || passwordChangeForm.newPw.length > 15) {
    passwordChangeErrors.newPw = "새 비밀번호는 9자 이상 15자 이하로 입력하세요.";
    return false;
  }
  if (!/^(?=.*[a-zA-Z])(?=.*\d)(?=.*[$~@$!%*#?&_!])[a-zA-Z\d$~@$!%*#?&_!]{9,15}$/.test(passwordChangeForm.newPw)) {
    passwordChangeErrors.newPw = "변경할 비밀번호가 형식에 맞지 않습니다.";
    return false;
  }
  return true;
}

/** 로그인 비밀번호 변경을 처리한다. */
async function submitPasswordChange(): Promise<void> {
  if (!validatePasswordChangeForm()) return;

  isPasswordChanging.value = true;
  try {
    const fd = new FormData();
    fd.append("username", passwordChangeUsername.value);
    fd.append("currPw", passwordChangeForm.currPw);
    fd.append("newPw", passwordChangeForm.newPw);
    if (passwordChangeToken.value) fd.append("passwordToken", passwordChangeToken.value);
    const res = await axios.post("/api/auth/login-pw-chg", fd);
    if (!res.data?.rslt) {
      console.warn("[SignIn] password change rejected by server.", { username: passwordChangeUsername.value });
      errorMsgLines.value = [res.data?.message ?? "비밀번호를 변경하지 못했습니다."];
      return;
    }
    closePasswordChangeModal();
    errorMsgLines.value = ["비밀번호가 변경되었습니다. 다시 로그인해주세요."];
    form.value.password = "";
    resetPasswordChangeForm();
  } catch (error) {
    if (axios.isAxiosError<{ message?: string }>(error)) {
      console.warn("[SignIn] password change request failed.", {
        username: passwordChangeUsername.value,
        status: error.response?.status,
      });
      errorMsgLines.value = [error.response?.data?.message ?? "비밀번호를 변경하지 못했습니다."];
    } else {
      console.warn("[SignIn] password change request failed with unknown error.", error);
      errorMsgLines.value = ["비밀번호를 변경하지 못했습니다."];
    }
  } finally {
    isPasswordChanging.value = false;
  }
}

/** 중복 로그인 확인 후 기존 세션을 끊고 로그인을 재시도한다. */
async function confirmDuplicateLoginAndRetry(): Promise<void> {
  const confirmed = await swalConfirm("이미 로그인된 세션이 있습니다. 기존 세션을 끊고 로그인할까요?");
  if (!confirmed) {
    await axios.post("/api/auth/expire-session");
    form.value.password = "";
    console.info("[SignIn] duplicate-login confirmation canceled.", { username: form.value.username });
    return;
  }

  console.info("[SignIn] duplicate-login confirmation accepted.", { username: form.value.username });
  try {
    await authStore.login({ username: form.value.username, password: form.value.password });
    const redirect = typeof route.query.redirect === "string" ? route.query.redirect : "";
    await router.push(redirect || { name: "journal-weekly" });
  } catch {
    const msgs = authStore.errors.length > 0 ? authStore.errors : ["로그인에 실패했습니다."];
    setLoginErrorLines(msgs);
  }
}

/** 마운트 시 쿼리 파라미터에 따라 안내 메시지를 표시한다. */
onMounted(() => {
  if (passwordChangeModalEl.value) passwordChangeModal = new Modal(passwordChangeModalEl.value, { backdrop: "static", keyboard: false });
  if (route.query.dupLoginAt === "Y") {
    errorMsgLines.value = ["중복 로그인으로 인해 로그아웃되었습니다."];
  } else if (route.query.sessionExpired === "Y") {
    sessionExpiredNotice.value = "세션이 만료되었거나 다른 곳에서 로그인되어 현재 로그인이 해제되었습니다. 다시 로그인해주세요.";
  } else if (route.query.oauthError) {
    errorMsgLines.value = [String(route.query.oauthError)];
  }
});

/** 로그인 처리 */
async function handleLogin(): Promise<void> {
  fieldErrors.value = {};
  errorMsgLines.value = [];
  isLoading.value = true;
  try {
    await authStore.login({ username: form.value.username, password: form.value.password });
    const redirect = typeof route.query.redirect === "string" ? route.query.redirect : "";
    await router.push(redirect || { name: "journal-weekly" });
  } catch {
    const msgs = authStore.errors.length > 0 ? authStore.errors : ["로그인에 실패했습니다."];
    setLoginErrorLines(msgs);
    const loginAction = authStore.loginAction;
    if (loginAction?.isDupIdLogin) {
      await confirmDuplicateLoginAndRetry();
    } else if (loginAction?.isCredentialExpired || loginAction?.needsPasswordReset) {
      openPasswordChangeModal(loginAction.username);
    }
  } finally {
    isLoading.value = false;
  }
}
</script>
