import axios, { type AxiosInstance, type InternalAxiosRequestConfig } from "axios";
import NProgress from "nprogress";
import "nprogress/nprogress.css";
import "@/styles/components/nprogress.scss";

/**
 * Vue SPA 전역 AJAX 로딩바 (nprogress).
 * 레거시 cF.ui.blockUI(전체 화면 오버레이) 대신 상단 progress bar 를 쓴다.
 * axios in-flight 카운터로 병렬 요청에서도 start/done 이 깜빡이지 않게 한다.
 *
 * 제외:
 * - config.skipLoadingBar === true
 * - /api/session/ping (모달 선행 핑 — 바 깜빡임 방지)
 */
declare module "axios" {
  export interface AxiosRequestConfig {
    /** true 이면 전역 nprogress 로딩바에 카운트하지 않는다. */
    skipLoadingBar?: boolean;
  }
}

type TrackedConfig = InternalAxiosRequestConfig & {
  skipLoadingBar?: boolean;
  __loadingBarTracked?: boolean;
};

NProgress.configure({
  showSpinner: false,
  trickleSpeed: 200,
  minimum: 0.08,
});

let pendingCount = 0;

function resolveUrl(config: InternalAxiosRequestConfig): string {
  return String(config.url ?? "");
}

function shouldSkipLoadingBar(config: TrackedConfig): boolean {
  if (config.skipLoadingBar === true) return true;
  return resolveUrl(config).includes("/api/session/ping");
}

function begin(config: TrackedConfig): void {
  if (shouldSkipLoadingBar(config)) return;
  if (pendingCount === 0) {
    NProgress.start();
  }
  pendingCount += 1;
  config.__loadingBarTracked = true;
}

function end(config?: TrackedConfig): void {
  if (!config?.__loadingBarTracked) return;
  pendingCount = Math.max(0, pendingCount - 1);
  if (pendingCount === 0) {
    NProgress.done();
  }
}

/**
 * 전역 axios 인스턴스에 로딩바 request/response 인터셉터를 등록한다.
 * main.ts 에서 앱 부트 시 한 번만 호출한다.
 */
export function installAjaxLoadingBar(instance: AxiosInstance = axios): void {
  instance.interceptors.request.use((config) => {
    begin(config as TrackedConfig);
    return config;
  });

  instance.interceptors.response.use(
    (response) => {
      end(response.config as TrackedConfig);
      return response;
    },
    (error: unknown) => {
      const config = (error as { config?: TrackedConfig })?.config;
      end(config);
      return Promise.reject(error);
    }
  );
}