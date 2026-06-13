<template>
  <!--begin::Root-->
  <div class="d-flex flex-column flex-root app-root" id="kt_app_root">
    <!--begin::Page (dreamdiary.jpg 전체 배경)-->
    <div
      class="app-page flex-column flex-column-fluid"
      :style="{
        backgroundImage: `url('${base}dreamdiary/img/dreamdiary.jpg')`,
        backgroundPosition: 'center',
        backgroundRepeat: 'no-repeat',
        backgroundSize: 'cover',
      }"
    >
      <router-view />
    </div>
    <!--end::Page-->
  </div>
  <!--end::Root-->
</template>

<script setup lang="ts">
import { onMounted, onUnmounted } from "vue";
import { THEME_MODE_LS_KEY } from "@/shared/theme/stores/theme";

/** Vite dev: public/ is served at /, production: served under BASE_URL */
const base = import.meta.env.BASE_URL as string;

onMounted(() => {
  // 로그인 화면은 다크모드 설정과 무관하게 항상 라이트 모드로 표시한다.
  document.documentElement.setAttribute("data-bs-theme", "light");
});

onUnmounted(() => {
  // 로그인 화면을 벗어나면 사용자가 설정한 테마를 복원한다.
  const saved = localStorage.getItem(THEME_MODE_LS_KEY) ?? "light";
  document.documentElement.setAttribute("data-bs-theme", saved);
});
</script>
