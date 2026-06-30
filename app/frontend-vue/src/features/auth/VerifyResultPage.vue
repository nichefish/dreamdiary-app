<template>
  <div class="verify-result-page">
    <div class="card post verify-result-card">
      <div class="card-body text-center">
        <div class="verify-result-icon" :class="isSuccess ? 'verify-result-success' : 'verify-result-failure'">
          <i :class="isSuccess ? 'bi bi-check-lg' : 'bi bi-exclamation-triangle'"></i>
        </div>
        <h1 class="verify-result-title">{{ title }}</h1>
        <p class="verify-result-message">{{ message }}</p>
        <p v-if="isSuccess" class="text-muted fs-8 mb-0">{{ t('auth.verify.redirect-notice') }}</p>
      </div>
      <div class="card-footer verify-result-footer">
        <button type="button" class="btn btn-sm btn-light-primary" @click="goSignIn">
          <i class="bi bi-box-arrow-in-right"></i>
          {{ t('auth.verify.go-to-login') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

const route = useRoute();
const router = useRouter();
const { t } = useLocaleStore();
let redirectTimer: number | undefined;

const isSuccess = computed(() => route.query.status === "success");
const title = computed(() => (isSuccess.value ? t("auth.verify.success.title") : t("auth.verify.failure.title")));
const message = computed(() => {
  if (isSuccess.value) return t("auth.verify.success.message");
  return String(route.query.message || t("auth.verify.link-invalid"));
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
