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
        >
          <span v-if="!isLoading" class="indicator-label">로그인</span>
          <span v-else class="indicator-progress">
            잠시만 기다려주세요...
            <span class="spinner-border spinner-border-sm align-middle ms-2"></span>
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
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useAuthStore } from "@/stores/auth";

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

/** OAuth2 소셜 로그인 팝업을 연다. */
function openOAuthPopup(url: string): void {
  const popup = window.open(url, "Authorization", "width=540,height=720,top=0,left=270");
  if (popup) popup.focus();
}

/** 회원가입 페이지로 이동한다. */
function goUserSignup(): void {
  router.push("/user/signup");
}

/** 마운트 시 쿼리 파라미터에 따라 안내 메시지를 표시한다. */
onMounted(() => {
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
    await router.push(redirect || { name: "dashboard" });
  } catch {
    const msgs = authStore.errors.length > 0 ? authStore.errors : ["로그인에 실패했습니다."];
    errorMsgLines.value = msgs;
  } finally {
    isLoading.value = false;
  }
}
</script>
