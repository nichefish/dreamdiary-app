<template>
  <div class="verify-result-page">
    <div class="card post verify-result-card">
      <div class="card-body text-center">
        <div class="verify-result-icon" :class="isSuccess ? 'verify-result-success' : 'verify-result-failure'">
          <i :class="isSuccess ? 'bi bi-check-lg' : 'bi bi-exclamation-triangle'"></i>
        </div>
        <h1 class="verify-result-title">{{ title }}</h1>
        <p class="verify-result-message">{{ message }}</p>
        <p v-if="isSuccess" class="text-muted fs-8 mb-0">잠시 후 로그인 화면으로 이동합니다.</p>
      </div>
      <div class="card-footer verify-result-footer">
        <button type="button" class="btn btn-sm btn-light-primary" @click="goSignIn">
          <i class="bi bi-box-arrow-in-right"></i>
          로그인으로 이동
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted } from "vue";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();
let redirectTimer: number | undefined;

const isSuccess = computed(() => route.query.status === "success");
const title = computed(() => (isSuccess.value ? "인증이 완료되었습니다" : "인증에 실패했습니다"));
const message = computed(() => {
  if (isSuccess.value) return "계정 인증이 정상적으로 처리되었습니다.";
  return String(route.query.message || "인증 링크를 다시 확인해주세요.");
});

function goSignIn() {
  void router.push({ name: "sign-in" });
}

onMounted(() => {
  if (isSuccess.value) {
    redirectTimer = window.setTimeout(goSignIn, 3000);
  }
});

onUnmounted(() => {
  if (redirectTimer) window.clearTimeout(redirectTimer);
});
</script>

<style scoped>
.verify-result-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 2rem 1rem;
}

.verify-result-card {
  width: min(100%, 480px);
}

.verify-result-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  margin-bottom: 1.25rem;
  border-radius: 8px;
  font-size: 1.75rem;
}

.verify-result-success {
  background: var(--bs-success-light);
  color: var(--bs-success);
}

.verify-result-failure {
  background: var(--bs-danger-light);
  color: var(--bs-danger);
}

.verify-result-title {
  margin-bottom: 0.75rem;
  font-size: 1.25rem;
  font-weight: 700;
}

.verify-result-message {
  margin-bottom: 0.5rem;
  color: var(--bs-gray-700);
  word-break: keep-all;
}

.verify-result-footer {
  display: flex;
  justify-content: center;
}
</style>
