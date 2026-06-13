import { reactive } from "vue";

export interface RuntimeErrorState {
  title: string;
  message: string;
  detail?: string;
  source?: string;
}

interface RuntimeStatusState {
  pending: boolean;
  pendingLabel: string;
  error: RuntimeErrorState | null;
}

export const appRuntimeStatus = reactive<RuntimeStatusState>({
  pending: false,
  pendingLabel: "",
  error: null,
});

export function markRuntimePending(label = "화면을 준비하고 있습니다.") {
  appRuntimeStatus.pending = true;
  appRuntimeStatus.pendingLabel = label;
}

export function clearRuntimePending() {
  appRuntimeStatus.pending = false;
  appRuntimeStatus.pendingLabel = "";
}

export function reportRuntimeError(error: unknown, source: string, title = "앱 화면 오류가 발생했습니다") {
  appRuntimeStatus.pending = false;
  appRuntimeStatus.error = {
    title,
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
  return "화면을 그리는 중 예외가 발생했습니다.";
}

function resolveErrorDetail(error: unknown): string | undefined {
  if (error instanceof Error && error.stack) return error.stack;
  const candidate = error as { stack?: unknown; reason?: unknown };
  if (typeof candidate?.stack === "string") return candidate.stack;
  if (candidate?.reason instanceof Error) return candidate.reason.stack ?? candidate.reason.message;
  return undefined;
}
