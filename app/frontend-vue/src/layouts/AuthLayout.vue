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
/** Vite dev: public/ is served at /, production: served under BASE_URL */
const base = import.meta.env.BASE_URL as string;
</script>
