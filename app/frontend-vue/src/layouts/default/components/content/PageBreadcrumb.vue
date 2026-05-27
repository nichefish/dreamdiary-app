<template>
  <!--begin::PageBreadcrumb-->
  <!--
    레이아웃 공통 breadcrumb 컴포넌트.
    route.meta.breadcrumbs / route.meta.pageTitle 을 읽어
    헤더 흰 띠(#kt_app_header_wrapper) 좌측에 현재 경로를 표시한다.
    헤더는 LayoutService 가 data-kt-app-sidebar-push-header=true 를 body 에 설정하므로
    sidebar 폭만큼 자동 오프셋되어 breadcrumb 이 sidebar 뒤에 가려지지 않는다.
    breadcrumbs 가 없는 경우(로그인·에러 페이지 등)에는 렌더링하지 않는다.
  -->
  <div v-if="breadcrumbs && breadcrumbs.length" class="page-breadcrumb d-flex align-items-center">
    <ul class="breadcrumb breadcrumb-separatorless fw-semibold fs-6 m-0">
      <!--begin::Home-->
      <li class="breadcrumb-item">
        <router-link to="/" class="text-gray-500 text-hover-primary">홈</router-link>
      </li>
      <!--end::Home-->
      <template v-for="(item, i) in breadcrumbs" :key="i">
        <!--begin::Separator-->
        <li class="breadcrumb-item">
          <span class="bullet bg-gray-400 w-5px h-2px"></span>
        </li>
        <!--end::Separator-->
        <!--begin::Item-->
        <li class="breadcrumb-item text-gray-500">{{ item }}</li>
        <!--end::Item-->
      </template>
      <!--begin::Separator-->
      <li v-if="pageTitle" class="breadcrumb-item">
        <span class="bullet bg-gray-400 w-5px h-2px"></span>
      </li>
      <!--end::Separator-->
      <!--begin::Current page — 현재 페이지는 primary 컬러로 강조-->
      <li v-if="pageTitle" class="breadcrumb-item text-primary fw-bold">{{ pageTitle }}</li>
      <!--end::Current page-->
    </ul>
  </div>
  <!--end::PageBreadcrumb-->
</template>

<script lang="ts">
import { computed, defineComponent } from "vue";
import { useRoute } from "vue-router";

/** 레이아웃 공통 breadcrumb — 헤더 흰 띠 좌측에 현재 경로를 표시한다. */
export default defineComponent({
  name: "page-breadcrumb",
  setup() {
    const route = useRoute();

    /** 라우터 meta 의 breadcrumbs 배열 (예: ["관리", "메뉴"]) */
    const breadcrumbs = computed(() => route.meta.breadcrumbs as string[] | undefined);

    /** 라우터 meta 의 pageTitle — breadcrumb 마지막 항목으로 표시 */
    const pageTitle = computed(() => route.meta.pageTitle as string | undefined);

    return {
      breadcrumbs,
      pageTitle,
    };
  },
});
</script>

<style scoped>
.page-breadcrumb {
  /* 헤더 높이(74px) 내에서 세로 중앙 정렬, 내비게이션 아이콘과 시각적 여백 확보 */
  padding: 0 0.25rem;
  min-width: 0;
}
</style>
