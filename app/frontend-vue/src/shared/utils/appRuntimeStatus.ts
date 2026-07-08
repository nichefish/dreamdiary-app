import { reactive } from "vue";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

/** {@link resolveErrorMessage} 가 i18n 키를 반환했음을 표시하는 sentinel. */
export const RUNTIME_RENDER_FAILURE_KEY = "runtime.error.render-failure";

export interface RuntimeErrorState {
  titleKey: string;
  message: string;
  detail?: string;
  source?: string;
}

interface RuntimeStatusState {
  pending: boolean;
  pendingLabelKey: string;
  error: RuntimeErrorState | null;
}

export const appRuntimeStatus = reactive<RuntimeStatusState>({
  pending: false,
  pendingLabelKey: "runtime.pending.default",
  error: null,
});

/** 라우팅·초기화 지연 시 상단 pending 배너를 표시한다. labelKey 는 locale 카탈로그 키. */
export function markRuntimePending(labelKey = "runtime.pending.default") {
  appRuntimeStatus.pending = true;
  appRuntimeStatus.pendingLabelKey = labelKey;
}

export function clearRuntimePending() {
  appRuntimeStatus.pending = false;
  appRuntimeStatus.pendingLabelKey = "runtime.pending.default";
}

/** 전역 런타임 오류를 AppRuntimeStatus 패널에 표시한다. titleKey 는 locale 카탈로그 키. */
export function reportRuntimeError(
  error: unknown,
  source: string,
  titleKey = "runtime.error.default-title"
) {
  appRuntimeStatus.pending = false;
  appRuntimeStatus.error = {
    titleKey,
    message: resolveErrorMessage(error),
    detail: resolveErrorDetail(error),
    source,
  };
  // 콘솔도 남긴다. 화면은 사용자가, 콘솔은 개발자가 본다.
  console.error(`[${source}]`, error);
}

export function clearRuntimeError() {
  appRuntimeStatus.error = null;
}

function resolveErrorMessage(error: unknown): string {
  if (error instanceof Error && error.message) return error.message;
  if (typeof error === "string" && error.trim()) return error;
  const candidate = error as { message?: unknown; reason?: unknown };
  if (typeof candidate?.message === "string" && candidate.message.trim()) return candidate.message;
  if (typeof candidate?.reason === "string" && candidate.reason.trim()) return candidate.reason;
  return RUNTIME_RENDER_FAILURE_KEY;
}

function resolveErrorDetail(error: unknown): string | undefined {
  if (error instanceof Error && error.stack) return error.stack;
  const candidate = error as { stack?: unknown; reason?: unknown };
  if (typeof candidate?.stack === "string") return candidate.stack;
  if (candidate?.reason instanceof Error) return candidate.reason.stack ?? candidate.reason.message;
  return undefined;
}

/** AppRuntimeStatus 에서 message 필드를 표시용 문구로 변환한다. */
export function resolveRuntimeErrorMessage(message: string): string {
  if (message === RUNTIME_RENDER_FAILURE_KEY) {
    return useLocaleStore().t(RUNTIME_RENDER_FAILURE_KEY);
  }
  return message;
}
