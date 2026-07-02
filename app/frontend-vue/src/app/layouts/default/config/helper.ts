import { computed } from "vue";
import { useConfigStore } from "@/shared/config/stores/config";

/** 레이아웃 config */
export const config = computed(() => useConfigStore().config);

/** 테마 모드 (Metronic ThemeModeComponent 의존성 제거 — 항상 light) */
export const themeMode = computed(() => "light");

export const themeConfigValue = computed<"system" | "dark" | "light">(() => "light");

/** 사이드바 표시 여부 */
export const displaySidebar = computed(() =>
  useConfigStore().getLayoutConfig("sidebar.display")
);

/** 사이드바 토글 표시 여부 */
export const sidebarToggleDisplay = computed(() =>
  useConfigStore().getLayoutConfig("sidebar.default.minimize.desktop.enabled")
);

/** 푸터 fluid 여부 */
export const footerWidthFluid = computed(() =>
  useConfigStore().getLayoutConfig("footer.container") === "fluid"
);

/** 푸터 표시 여부 */
export const footerDisplay = computed(() =>
  useConfigStore().getLayoutConfig("footer.display")
);

/** 헤더 fluid 여부 */
export const headerWidthFluid = computed(() =>
  useConfigStore().getLayoutConfig("header.default.container") === "fluid"
);

/** 헤더 표시 여부 */
export const headerDisplay = computed(() =>
  useConfigStore().getLayoutConfig("header.display")
);

export const headerLeft = computed(() =>
  useConfigStore().getLayoutConfig("header.left")
);

export const headerDesktopFixed = computed(() =>
  useConfigStore().getLayoutConfig("header.default.fixed.desktop")
);

export const headerMobileFixed = computed(() =>
  useConfigStore().getLayoutConfig("header.default.fixed.mobile")
);

export const sidebarDisplay = computed(() =>
  useConfigStore().getLayoutConfig("sidebar.display")
);

/** 레이아웃 타입 */
export const layout = computed(() =>
  useConfigStore().getLayoutConfig("general.layout")
);

export const toolbarWidthFluid = computed(() =>
  useConfigStore().getLayoutConfig("toolbar.container") === "fluid"
);

export const toolbarDisplay = computed(() =>
  useConfigStore().getLayoutConfig("toolbar.display")
);

/** 콘텐츠 영역 fluid 여부 */
export const contentWidthFluid = computed(() =>
  useConfigStore().getLayoutConfig("content.container") === "fluid"
);

export const sidebarEnabled = computed(() =>
  !!useConfigStore().getLayoutConfig("aside.display")
);

export const sidebarTheme = computed(() =>
  useConfigStore().getLayoutConfig("aside.theme")
);

export const subheaderDisplay = computed(() =>
  useConfigStore().getLayoutConfig("toolbar.display")
);

/** 사이드바 메뉴 아이콘 타입 */
export const sidebarMenuIcons = computed(() =>
  useConfigStore().getLayoutConfig("sidebar.default.menu.iconType")
);

export const themeLightLogo = computed(() =>
  useConfigStore().getLayoutConfig("main.logo.light")
);

export const themeDarkLogo = computed(() =>
  useConfigStore().getLayoutConfig("main.logo.dark")
);

export const headerMenuIcons = computed(() =>
  useConfigStore().getLayoutConfig("header.default.menu.iconType")
);

export const headerMenuDisplay = computed(() =>
  useConfigStore().getLayoutConfig("header.default.menu.display")
);

export const pageTitleDisplay = computed(() =>
  useConfigStore().getLayoutConfig("pageTitle.display")
);

export const pageTitleBreadcrumbDisplay = computed(() =>
  useConfigStore().getLayoutConfig("pageTitle.breadcrumb")
);

export const pageTitleDirection = computed(() =>
  useConfigStore().getLayoutConfig("pageTitle.direction")
);

export const scrolltopDispaly = computed(() =>
  useConfigStore().getLayoutConfig("scrolltop.display")
);

export const illustrationsSet = computed(() =>
  useConfigStore().getLayoutConfig("illustrations.set")
);
