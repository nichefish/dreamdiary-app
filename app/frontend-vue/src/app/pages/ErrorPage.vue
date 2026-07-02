<template>
  <div class="error-page-vue d-flex flex-column align-items-center justify-content-center min-vh-100 text-center px-4">
    <span class="ctgr-span ctgr-imprtc mb-4">{{ categoryLabel }}</span>
    <span class="fs-3 fw-semibold text-muted mb-2">{{ statusCode }}</span>
    <h1 class="fs-2x fw-bold mb-3">{{ title }}</h1>
    <p class="text-gray-500 mb-4">{{ description }}</p>
    <p v-if="message" class="text-gray-500 fw-bold min-h-100px mb-8">{{ message }}</p>
    <div class="d-flex justify-content-center gap-2">
      <button class="btn btn-sm btn-outlined btn-light-primary btn-active-primary py-2 px-3" @click="goHome">
        <i class="bi bi-arrow-counterclockwise"></i>메인으로
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";

type ErrorType = "general" | "not_found" | "bad_request" | "access_denied";

const route = useRoute();
const router = useRouter();

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

const categoryLabel = computed(() => type.value === "access_denied" ? "Access Denied" : "Error Page");

const title = computed(() => {
  if (type.value === "not_found") return "페이지를 찾을 수 없습니다";
  if (type.value === "bad_request") return "잘못된 요청입니다";
  if (type.value === "access_denied") return "접근 권한이 없습니다";
  return "서버 오류가 발생했습니다";
});

const description = computed(() => {
  if (type.value === "not_found") return "요청하신 주소가 존재하지 않거나 이동되었습니다.";
  if (type.value === "bad_request") return "요청 내용을 확인한 뒤 다시 시도해 주세요.";
  if (type.value === "access_denied") return "현재 계정으로는 이 화면에 접근할 수 없습니다.";
  return "서버 처리 중 문제가 생겼습니다. 잠시 후 다시 시도해 주세요.";
});

function goHome() {
  void router.push("/");
}
</script>
