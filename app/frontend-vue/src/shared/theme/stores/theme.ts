import { ref } from "vue";
import { defineStore } from "pinia";

export const THEME_MODE_LS_KEY = "kt_theme_mode_value";
export const THEME_MENU_MODE_LS_KEY = "kt_theme_mode_menu";

export type ThemeMode = "light" | "dark" | "system";
export type AppliedThemeMode = "light" | "dark";

/**
 * 저장값·요청값을 실제 적용 모드(light|dark)로 해석한다.
 * system 은 prefers-color-scheme 으로 해석한다.
 */
export function resolveAppliedThemeMode(payload: string | null | undefined): AppliedThemeMode {
  if (payload === "dark" || payload === "light") return payload;
  if (payload === "system") {
    return window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
  }
  return "light";
}

/**
 * 부트 시 Pinia mode 초기값.
 * index.html 이 이미 세팅한 data-bs-theme 를 우선하고, 없으면 localStorage 를 본다.
 * (이전: mode 가 항상 "light" 로 시작해 새로고침 후 Navbar 아이콘/툴팁이 DOM 과 어긋남)
 */
function readInitialThemeMode(): AppliedThemeMode {
  const attr = document.documentElement.getAttribute("data-bs-theme");
  if (attr === "dark" || attr === "light") return attr;
  return resolveAppliedThemeMode(localStorage.getItem(THEME_MODE_LS_KEY));
}

export const useThemeStore = defineStore("theme", () => {
  const mode = ref<ThemeMode>(readInitialThemeMode());

  /**
   * 테마 모드를 적용한다.
   * DOM(data-bs-theme) + localStorage + Pinia mode 를 함께 갱신한다.
   */
  function setThemeMode(payload: ThemeMode): void {
    const currentMode = resolveAppliedThemeMode(payload);
    localStorage.setItem(THEME_MODE_LS_KEY, currentMode);
    localStorage.setItem(THEME_MENU_MODE_LS_KEY, currentMode);
    mode.value = currentMode;
    document.documentElement.setAttribute("data-bs-theme", currentMode);
  }

  /**
   * localStorage 저장값을 DOM·Pinia 에 다시 적용한다.
   * AuthLayout 이탈 복원 등 DOM 만 건드린 뒤 store 를 맞출 때 사용한다.
   */
  function applyStoredThemeMode(): void {
    setThemeMode(resolveAppliedThemeMode(localStorage.getItem(THEME_MODE_LS_KEY)));
  }

  return {
    mode,
    setThemeMode,
    applyStoredThemeMode,
  };
});
