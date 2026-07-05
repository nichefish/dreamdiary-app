<template>
  <div class="error-page-vue d-flex flex-column align-items-center justify-content-center min-vh-100 text-center px-4">
    <span class="ctgr-span ctgr-imprtc mb-4">{{ categoryLabel }}</span>
    <span class="fs-3 fw-semibold text-muted mb-2">{{ statusCode }}</span>
    <h1 class="fs-2x fw-bold mb-3">{{ title }}</h1>
    <p class="text-gray-500 mb-4">{{ description }}</p>
    <p v-if="message" class="text-gray-500 fw-bold min-h-100px mb-8">{{ message }}</p>
    <div class="d-flex justify-content-center gap-2">
      <button class="btn btn-sm btn-outlined btn-light-primary btn-active-primary py-2 px-3" @click="goHome">
        <i class="bi bi-arrow-counterclockwise"></i>{{ t("error.page.action.home") }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

type ErrorType = "general" | "not_found" | "bad_request" | "access_denied";

const route = useRoute();
const router = useRouter();
const { t } = useLocaleStore();

const type = computed<ErrorType>(() => {
  const raw = String(route.query.type ?? route.meta.errorType ?? "general");
  if (raw === "not_found" || raw === "bad_request" || raw === "access_denied") return raw;
  return "general";
});

const message = computed(() => String(route.query.message ?? route.query.errorMsg ?? "").trim());

const statusCode = computed(() => {
  if (type.value === "not_found") return "404";
  if (type.value === "bad_request") return "400";
  if (type.value === "access_denied") return "403";
  return "500";
});

const categoryLabel = computed(() =>
  type.value === "access_denied"
    ? t("error.page.category.access-denied")
    : t("error.page.category.general")
);

const title = computed(() => {
  if (type.value === "not_found") return t("error.page.title.not-found");
  if (type.value === "bad_request") return t("error.page.title.bad-request");
  if (type.value === "access_denied") return t("error.page.title.access-denied");
  return t("error.page.title.server");
});

const description = computed(() => {
  if (type.value === "not_found") return t("error.page.description.not-found");
  if (type.value === "bad_request") return t("error.page.description.bad-request");
  if (type.value === "access_denied") return t("error.page.description.access-denied");
  return t("error.page.description.server");
});

function goHome() {
  void router.push("/");
}
</script>
