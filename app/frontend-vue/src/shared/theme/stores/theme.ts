import { ref } from "vue";
import { defineStore } from "pinia";

export const THEME_MODE_LS_KEY = "kt_theme_mode_value";
export const THEME_MENU_MODE_LS_KEY = "kt_theme_mode_menu";

export const useThemeStore = defineStore("theme", () => {
  const mode = ref<"light" | "dark" | "system">("light");

  function setThemeMode(payload: "light" | "dark" | "system") {
    const currentMode =
      payload === "system"
        ? window.matchMedia("(prefers-color-scheme: dark)").matches
          ? "dark"
          : "light"
        : payload;
    localStorage.setItem(THEME_MODE_LS_KEY, currentMode);
    localStorage.setItem(THEME_MENU_MODE_LS_KEY, currentMode);
    mode.value = currentMode;
    document.documentElement.setAttribute("data-bs-theme", currentMode);
  }

  return {
    mode,
    setThemeMode,
  };
});
