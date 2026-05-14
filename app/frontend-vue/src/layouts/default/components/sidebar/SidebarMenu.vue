<template>
  <!--begin::sidebar menu-->
  <div class="app-sidebar-menu overflow-hidden flex-column-fluid">
    <!--begin::Menu wrapper-->
    <div
      id="kt_app_sidebar_menu_wrapper"
      class="app-sidebar-wrapper hover-scroll-overlay-y my-5"
      data-kt-scroll="true"
      data-kt-scroll-activate="true"
      data-kt-scroll-height="auto"
      data-kt-scroll-dependencies="#kt_app_sidebar_logo, #kt_app_sidebar_footer"
      data-kt-scroll-wrappers="#kt_app_sidebar_menu"
      data-kt-scroll-offset="5px"
      data-kt-scroll-save-state="true"
    >
      <!--begin::Menu-->
      <div
        id="kt_app_sidebar_menu"
        class="menu menu-column menu-rounded menu-sub-indention px-3"
        data-kt-menu="true"
      >
        <!--begin: 메뉴 섹션 반복 (MAIN 메뉴 기준) -->
        <template v-for="(mainMenu, i) in menuStore.menuList" :key="i">
          <!--begin: 섹션 헤딩 -->
          <div v-if="mainMenu.menuName" class="menu-item pt-5">
            <div class="menu-content">
              <span class="menu-heading fw-bold text-uppercase fs-7">
                {{ mainMenu.menuName }}
              </span>
            </div>
          </div>
          <!--end: 섹션 헤딩 -->

          <!--begin: 서브메뉴 반복 -->
          <template v-for="(subMenu, j) in mainMenu.subMenuList || []" :key="j">
            <!--begin: 단순 링크 (NO_SUB) -->
            <template v-if="subMenu.submenuExpandType === 'NO_SUB'">
              <div class="menu-item">
                <router-link
                  v-if="subMenu.url"
                  class="menu-link"
                  active-class="active"
                  :to="toVuePath(subMenu.url)"
                >
                  <span v-if="subMenu.icon" class="menu-icon">
                    <i :class="subMenu.icon" class="bi fs-3"></i>
                  </span>
                  <span class="menu-title">{{ subMenu.menuName }}</span>
                </router-link>
              </div>
            </template>
            <!--end: 단순 링크 -->

            <!--begin: 아코디언 (LIST / COLLAPSE) -->
            <template v-else-if="subMenu.submenuExpandType === 'LIST' || subMenu.submenuExpandType === 'COLLAPSE'">
              <div
                :class="{ show: hasActiveChildren(subMenu.url) }"
                class="menu-item menu-accordion"
                data-kt-menu-sub="accordion"
                data-kt-menu-trigger="click"
              >
                <span class="menu-link">
                  <span v-if="subMenu.icon" class="menu-icon">
                    <i :class="subMenu.icon" class="bi fs-3"></i>
                  </span>
                  <span class="menu-title">{{ subMenu.menuName }}</span>
                  <span class="menu-arrow"></span>
                </span>
                <div
                  :class="{ show: hasActiveChildren(subMenu.url) }"
                  class="menu-sub menu-sub-accordion"
                >
                  <template v-for="(item2, k) in subMenu.subMenuList || []" :key="k">
                    <div class="menu-item">
                      <router-link
                        v-if="item2.url"
                        class="menu-link"
                        active-class="active"
                        :to="toVuePath(item2.url)"
                      >
                        <span class="menu-bullet">
                          <span class="bullet bullet-dot"></span>
                        </span>
                        <span class="menu-title">{{ item2.menuName }}</span>
                      </router-link>
                    </div>
                  </template>
                </div>
              </div>
            </template>
            <!--end: 아코디언 -->
          </template>
          <!--end: 서브메뉴 반복 -->
        </template>
        <!--end: 메뉴 섹션 반복 -->
      </div>
      <!--end::Menu-->
    </div>
    <!--end::Menu wrapper-->
  </div>
  <!--end::sidebar menu-->
</template>

<script lang="ts">
import { defineComponent, nextTick, onMounted } from "vue";
import { useRoute } from "vue-router";
import { reinitializeComponents } from "@metronic/core/plugins/keenthemes";
import { useMenuStore } from "@/stores/menu";
import { toVuePath } from "@/utils/urlMapping";

export default defineComponent({
  name: "sidebar-menu",
  components: {},
  setup() {
    const route = useRoute();
    const menuStore = useMenuStore();

    onMounted(async () => {
      await menuStore.fetchUserMenu();
      await nextTick();
      reinitializeComponents();
    });

    /** 현재 경로가 주어진 경로 하위인지 확인 */
    const hasActiveChildren = (match: string) => {
      return match ? route.path.indexOf(match) !== -1 : false;
    };

    return {
      hasActiveChildren,
      menuStore,
      toVuePath,
    };
  },
});
</script>
