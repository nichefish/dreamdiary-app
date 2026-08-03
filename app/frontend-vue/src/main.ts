import { createApp } from "vue";
import { createPinia } from "pinia";
import { Tooltip } from "bootstrap";
import axios from "axios";
import App from "./App.vue";

/*
TIP: To get started with clean router change path to @/router/clean.ts.
 */
import router from "./app/router";
import { AuthExpiredError } from "@/shared/utils/authError";
import { handleSessionExpired } from "@/shared/auth/sessionExpired";
import {
  clearRuntimePending,
  reportRuntimeError,
} from "@/shared/utils/appRuntimeStatus";
import { installAjaxLoadingBar } from "@/shared/http/ajaxLoadingBar";

/**
 * 전역 Axios 인터셉터: 401(세션 만료/비로그인) 응답 시 로그인 만료를 명확히 안내한다.
 * /api/auth/ 경로(로그인 자체)는 auth 스토어에서 이미 처리하므로 제외.
 * 동시에 여러 요청이 401 로 실패해도 대화상자는 한 번만 뜬다.
 * 각 catch 블록에서 isAuthExpiredError() 로 판별해 일반 오류 alert 를 억제한다.
 */
function isAuthApiUrl(url: string): boolean {
  return url.includes("/api/auth/");
}

/** 레거시 blockUI 대체: axios in-flight 동안 상단 nprogress 로딩바. */
installAjaxLoadingBar(axios);

axios.interceptors.response.use(
  (response) => response,
  async (error: unknown) => {
    const status = (error as { response?: { status?: number }; config?: { url?: string } })?.response?.status;
    const url = (error as { response?: { status?: number }; config?: { url?: string } })?.config?.url ?? "";
    if (status === 401 && !isAuthApiUrl(url)) {
      await handleSessionExpired(router);
      return Promise.reject(new AuthExpiredError());
    }
    return Promise.reject(error);
  }
);
import ElementPlus from "element-plus";
import i18n from "@metronic/core/plugins/i18n";

//imports for app initialization
import ApiService, { applyLocaleHeader } from "@metronic/core/services/ApiService";
import LayoutService from "@metronic/core/services/LayoutService";
import { initApexCharts } from "@metronic/core/plugins/apexcharts";
import { initInlineSvg } from "@metronic/core/plugins/inline-svg";
import { initVeeValidate } from "@metronic/core/plugins/vee-validate";
import {
  initKtIcon,
  initializeComponents,
  reinitializeComponents,
} from "@metronic/core/plugins/keenthemes";
import { useConfigStore } from "@/shared/config/stores/config";

import "@metronic/core/plugins/prismjs";

const app = createApp(App);
const pinia = createPinia();

app.config.errorHandler = (error) => {
  reportRuntimeError(error, "vue-error-handler");
};

app.use(pinia);
app.use(router);
app.use(ElementPlus);

ApiService.init(app);
applyLocaleHeader();
initApexCharts(app);
initKtIcon(app);
initInlineSvg(app);
initVeeValidate();

app.use(i18n);

type TooltipElement = HTMLElement & { _dreamdiaryTooltip?: Tooltip };

function refreshTooltip(el: TooltipElement) {
  el._dreamdiaryTooltip?.dispose();
  el._dreamdiaryTooltip = undefined;
  if (!el.getAttribute("title")) return;
  el._dreamdiaryTooltip = new Tooltip(el);
}

app.directive("tooltip", {
  mounted(el: TooltipElement) {
    refreshTooltip(el);
  },
  updated(el: TooltipElement) {
    refreshTooltip(el);
  },
  unmounted(el: TooltipElement) {
    el._dreamdiaryTooltip?.dispose();
    el._dreamdiaryTooltip = undefined;
  },
});

const configStore = useConfigStore();
configStore.overrideLayoutConfig();
LayoutService.init();
initializeComponents();

router.afterEach(() => {
  clearRuntimePending();
  setTimeout(() => {
    reinitializeComponents();
  }, 0);
});

router.onError((error) => {
  reportRuntimeError(error, "router");
});

window.addEventListener("error", (event) => {
  reportRuntimeError(event.error ?? event.message, "window-error");
});

window.addEventListener("unhandledrejection", (event) => {
  reportRuntimeError(event.reason, "unhandled-rejection");
});

try {
  app.mount("#app");
} catch (error) {
  reportRuntimeError(error, "app-mount");
  void renderBootFailure(error);
}

const LS_LOCALE_KEY = "dreamdiary_locale";

function readStoredLocale(): "ko" | "en" {
  const stored = window.localStorage.getItem(LS_LOCALE_KEY);
  return stored === "en" ? "en" : "ko";
}

/** Vue mount 이전 부트 실패 화면용 카탈로그 조회. fetch 실패 시 ko/en 최소 fallback. */
async function loadBootFailureLabels(): Promise<{ title: string; reload: string }> {
  const locale = readStoredLocale();
  const fallback =
    locale === "en"
      ? { title: "Failed to start the application", reload: "Reload" }
      : { title: "앱을 시작하지 못했습니다", reload: "새로고침" };
  try {
    const response = await fetch(`/i18n/${locale}.json`, {
      headers: { Accept: "application/json" },
    });
    if (!response.ok) return fallback;
    const catalog = (await response.json()) as Record<string, string>;
    return {
      title: catalog["runtime.boot.failure.title"] || fallback.title,
      reload: catalog["runtime.action.reload"] || fallback.reload,
    };
  } catch {
    return fallback;
  }
}

async function renderBootFailure(error: unknown) {
  document.body.classList.remove("page-loading");
  document.getElementById("splash-screen")?.remove();
  const root = document.getElementById("app");
  if (!root) return;
  const message = error instanceof Error ? error.message : String(error);
  const labels = await loadBootFailureLabels();
  root.innerHTML = `
    <div style="min-height:100vh;display:flex;align-items:center;justify-content:center;background:#f8fafc;padding:24px;">
      <div style="max-width:720px;width:100%;background:#fff;border:1px solid #e4e6ef;border-radius:8px;padding:28px;box-shadow:0 12px 32px rgba(15,23,42,.16);">
        <div style="color:#7e8299;font-size:12px;font-weight:700;text-transform:uppercase;margin-bottom:8px;">app-mount</div>
        <h1 style="color:#181c32;font-size:22px;font-weight:700;margin:0 0 10px;">${escapeHtml(labels.title)}</h1>
        <p style="color:#5e6278;font-size:14px;white-space:pre-wrap;margin:0 0 18px;">${escapeHtml(message)}</p>
        <button type="button" onclick="window.location.reload()" style="border:1px solid #e4e6ef;border-radius:6px;background:#f5f8fa;color:#181c32;font-weight:600;padding:8px 12px;cursor:pointer;">${escapeHtml(labels.reload)}</button>
      </div>
    </div>
  `;
}

function escapeHtml(value: string) {
  return value
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}
