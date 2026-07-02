<template>
  <!--begin::PageBreadcrumb-->
  <!--
    레이아웃 공통 breadcrumb 컴포넌트.
      메뉴 store 의 menuMetaList 를 읽어
    헤더 흰 띠(#kt_app_header_wrapper) 좌측에 현재 경로를 표시한다.
    헤더는 LayoutService 가 data-kt-app-sidebar-push-header=true 를 body 에 설정하므로
    sidebar 폭만큼 자동 오프셋되어 breadcrumb 이 sidebar 뒤에 가려지지 않는다.
    현재 경로와 매칭되는 메뉴가 없는 경우(로그인·에러 페이지 등)에는 렌더링하지 않는다.
  -->
  <div v-if="breadcrumbItems.length" class="page-breadcrumb d-flex flex-column justify-content-center">
    <ul class="breadcrumb breadcrumb-separatorless fw-semibold fs-6 m-0">
      <!--begin::Home-->
      <li class="breadcrumb-item">
        <router-link to="/" class="text-gray-500 text-hover-primary">홈</router-link>
      </li>
      <!--end::Home-->
      <template v-for="(item, i) in breadcrumbItems" :key="`${item.name}-${i}`">
        <!--begin::Separator-->
        <li class="breadcrumb-item">
          <span class="bullet bg-gray-400 w-5px h-2px"></span>
        </li>
        <!--end::Separator-->
        <!--begin::Item-->
        <li
          class="breadcrumb-item"
          :class="i === breadcrumbItems.length - 1 ? 'text-primary fw-bold' : 'text-gray-500'"
        >
          {{ item.name }}
        </li>
        <!--end::Item-->
      </template>
    </ul>
    <div v-if="menuDescription" class="page-breadcrumb-description">
      {{ menuDescription }}
    </div>
  </div>
  <!--end::PageBreadcrumb-->
</template>

<script lang="ts">
import { computed, defineComponent } from "vue";
import { useRoute } from "vue-router";
import { useMenuStore, type MenuDto } from "@/shared/menu/stores/menu";
import { toVuePath } from "@/shared/utils/urlMapping";

interface BreadcrumbItem {
  name: string;
}

interface BreadcrumbMatch {
  items: BreadcrumbItem[];
  description: string;
}

function normalizePath(path: string): string {
  const parsedUrl = new URL(path, window.location.origin);
  const normalizedPath = parsedUrl.pathname.replace(/\/+$/, "") || "/";
  return `${normalizedPath}${parsedUrl.search}`;
}

function isSameRoute(target: string, currentPath: string, currentFullPath: string): boolean {
  const normalizedTarget = normalizePath(target);
  return normalizedTarget.includes("?")
    ? normalizePath(currentFullPath) === normalizedTarget
    : normalizePath(currentPath) === normalizedTarget;
}

function findMenuBreadcrumb(
  menuList: MenuDto[],
  currentPath: string,
  currentFullPath: string,
  parentItems: BreadcrumbItem[] = []
): BreadcrumbMatch | null {
  for (const menu of menuList) {
    const isPassThroughMain = menu.menuType === "MAIN" && (menu.subMenuList?.length ?? 0) > 0;
    const currentItems = isPassThroughMain
      ? parentItems
      : [...parentItems, { name: menu.menuName }];

    if (menu.url && isSameRoute(toVuePath(menu.url), currentPath, currentFullPath)) {
      return {
        items: currentItems,
        description: menu.menuDescription?.trim() ?? "",
      };
    }

    const childMatch = findMenuBreadcrumb(menu.subMenuList ?? [], currentPath, currentFullPath, currentItems);
    if (childMatch) return childMatch;
  }

  return null;
}

/** 레이아웃 공통 breadcrumb — 헤더 흰 띠 좌측에 현재 경로를 표시한다. */
export default defineComponent({
  name: "page-breadcrumb",
  setup() {
    const route = useRoute();
    const menuStore = useMenuStore();

    /** 메뉴 트리의 현재 route 매칭 결과를 breadcrumb 와 설명으로 표시한다. */
    const breadcrumbMatch = computed(() => {
      const menuMetaList = menuStore.menuMetaList.length ? menuStore.menuMetaList : menuStore.menuList;
      return findMenuBreadcrumb(menuMetaList, route.path, route.fullPath);
    });
    const breadcrumbItems = computed(() => breadcrumbMatch.value?.items ?? []);
    const menuDescription = computed(() => breadcrumbMatch.value?.description ?? "");

    return {
      breadcrumbItems,
      menuDescription,
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

.page-breadcrumb-description {
  display: -webkit-box;
  max-width: min(720px, 70vw);
  max-height: 2.4em;
  margin-top: 0.25rem;
  color: var(--bs-gray-500);
  font-size: 0.78rem;
  font-weight: 600;
  line-height: 1.2;
  overflow: hidden;
  overflow-wrap: anywhere;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}
</style>
