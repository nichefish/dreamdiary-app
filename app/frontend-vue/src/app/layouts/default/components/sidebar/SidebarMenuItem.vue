<template>
  <!--begin::MAIN 루트: 컨테이너 노드는 자식만 렌더 (레거시 사이드바와 동일)-->
  <template v-if="passThroughMain">
    <SidebarMenuItem
      v-for="child in children"
      :key="child.id ?? `${itemId}-${child.menuName}`"
      :menu="child"
      :depth="depth"
    />
  </template>
  <template v-else>
  <template v-if="showMenuLabel">
    <div class="menu-item">
      <div class="menu-content mt-5 pb-2">
        <span class="menu-heading fw-bold text-uppercase fs-8 ls-1">
          {{ menu.menuLabel }}
        </span>
      </div>
    </div>
  </template>

  <div
    v-if="isAccordion"
    class="menu-item menu-accordion"
    :class="{ here: active, show: true }"
    :id="itemId"
  >
    <span class="menu-link mb-1" :class="{ active }">
      <span v-if="menu.icon" class="menu-icon">
        <span v-if="iconHtml" v-html="menu.icon"></span>
        <i v-else :class="iconClass" class="fs-3"></i>
      </span>
      <span v-else-if="depth > 0" class="menu-bullet">
        <span class="bullet bullet-dot"></span>
      </span>
      <span class="menu-title">{{ menu.menuName }}</span>
      <span v-if="unreadCount > 0" class="badge badge-success bg-noti ms-auto" :class="{ blink: unreadCount > 5 }">
        {{ unreadCount }}
      </span>
      <span v-if="!alwaysExpanded" class="menu-arrow"></span>
    </span>

    <div class="menu-sub menu-sub-accordion menu-active-bg show">
      <SidebarMenuItem
        v-for="child in children"
        :key="child.id ?? `${itemId}-${child.menuName}`"
        :menu="child"
        :depth="depth + 1"
      />
    </div>
  </div>

  <div v-else class="menu-item" :id="itemId">
    <router-link
      v-if="routePath"
      class="menu-link mb-1"
      exact-active-class="active"
      :class="{ active }"
      :to="routePath"
    >
      <span v-if="menu.icon" class="menu-icon">
        <span v-if="iconHtml" v-html="menu.icon"></span>
        <i v-else :class="iconClass" class="fs-3"></i>
      </span>
      <span v-else-if="depth > 0" class="menu-bullet">
        <span class="bullet bullet-dot"></span>
      </span>
      <span class="menu-title">{{ menu.menuName }}</span>
      <span v-if="unreadCount > 0" class="badge badge-success bg-noti ms-auto" :class="{ blink: unreadCount > 5 }">
        {{ unreadCount }}
      </span>
    </router-link>
    <span v-else class="menu-link mb-1" :class="{ active }">
      <span v-if="menu.icon" class="menu-icon">
        <span v-if="iconHtml" v-html="menu.icon"></span>
        <i v-else :class="iconClass" class="fs-3"></i>
      </span>
      <span v-else-if="depth > 0" class="menu-bullet">
        <span class="bullet bullet-dot"></span>
      </span>
      <span class="menu-title">{{ menu.menuName }}</span>
    </span>
  </div>
  </template>
</template>

<script lang="ts">
import { computed, defineComponent, type PropType } from "vue";
import { useRoute } from "vue-router";
import type { MenuDto } from "@/shared/menu/stores/menu";
import { toVuePath } from "@/shared/utils/urlMapping";

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

function hasActiveDescendant(menu: MenuDto, currentPath: string, currentFullPath: string): boolean {
  const current = menu.url ? isSameRoute(toVuePath(menu.url), currentPath, currentFullPath) : false;
  const children = menu.subMenuList ?? [];
  return current || children.some((child) => hasActiveDescendant(child, currentPath, currentFullPath));
}

export default defineComponent({
  name: "SidebarMenuItem",
  props: {
    menu: {
      type: Object as PropType<MenuDto>,
      required: true,
    },
    depth: {
      type: Number,
      default: 0,
    },
  },
  setup(props) {
    const route = useRoute();
    const children = computed(() => props.menu.subMenuList ?? []);
    const hasChildren = computed(() => children.value.length > 0);
    const expandType = computed(() => props.menu.submenuExpandType ?? "NO_SUB");
    const routePath = computed(() => (props.menu.url ? toVuePath(props.menu.url) : ""));
    const itemId = computed(() => (props.menu.id != null ? String(props.menu.id) : `menu-${props.depth}-${props.menu.menuName}`));
    const iconHtml = computed(() => props.menu.icon?.trim().startsWith("<"));
    const iconClass = computed(() => {
      const icon = props.menu.icon?.trim();
      if (!icon) return "";
      return icon.includes(" ") || icon.startsWith("bi-") ? ["bi", icon] : ["bi", icon];
    });
    const unreadCount = computed(() => {
      const value = Number(props.menu.unreadCntNm ?? 0);
      return Number.isFinite(value) ? value : 0;
    });
    const isAccordion = computed(() =>
      hasChildren.value && ["LIST", "COLLAPSE", "EXTEND", "BOARD"].includes(expandType.value)
    );
    const active = computed(() => {
      if (isAccordion.value) {
        return hasActiveDescendant(props.menu, route.path, route.fullPath);
      }
      return routePath.value ? isSameRoute(routePath.value, route.path, route.fullPath) : false;
    });
    const alwaysExpanded = computed(() => true);
    const showMenuLabel = computed(() => props.depth === 0 && !!props.menu.menuLabel);

    const passThroughMain = computed(
      () => props.menu.menuType === "MAIN" && hasChildren.value
    );

    return {
      active,
      children,
      iconClass,
      iconHtml,
      isAccordion,
      itemId,
      alwaysExpanded,
      passThroughMain,
      routePath,
      showMenuLabel,
      unreadCount,
    };
  },
});
</script>
