/**
 * swal.ts
 * SweetAlert2 공통 래퍼. 레거시 cF.ui.swalOrConfirm / Swal.fire 패턴을 Vue SPA 에서 통일 적용.
 */
import axios from "axios";
import Swal from "sweetalert2/dist/sweetalert2.js";
import type { SweetAlertOptions, SweetAlertResult } from "sweetalert2";
import { isAuthExpiredError } from "@/shared/utils/authError";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

/** Ajax 오류 응답 본문의 message 필드 추출 */
export function getAjaxResponseMessage(error: unknown): string | undefined {
  if (!axios.isAxiosError(error)) return undefined;
  const data = error.response?.data;
  if (data && typeof data === "object" && "message" in data) {
    const message = (data as { message?: unknown }).message;
    if (typeof message === "string" && message.length > 0) return message;
  }
  return undefined;
}

/** AUTH_EXPIRED 는 전역 인터셉터에서 이미 안내하므로 재표시하지 않는다. */
function isSuppressedText(text: string | undefined): boolean {
  return text === "AUTH_EXPIRED";
}

/**
 * SweetAlert2 단일 진입점. 레거시 Swal.fire({ ... }) 와 동일하게 옵션·콜백을 주입한다.
 */
export async function swalFire(options: SweetAlertOptions): Promise<SweetAlertResult> {
  if (isSuppressedText(options.text)) {
    return { isConfirmed: false, isDenied: false, isDismissed: true };
  }
  return Swal.fire(options);
}

/** Ajax {@code rslt} 응답 후속 알림. message 가 있으면 우선하고, 없으면 fallback 을 쓴다. */
export type SwalAjaxResultOptions = {
  rslt: boolean;
  message?: string | null;
  successFallback?: string;
  failureFallback?: string;
};

/**
 * Ajax {@code rslt} 기반 결과 알림.
 * {@code rslt === true} → success 아이콘, false → error 아이콘.
 */
export async function swalAjaxResult(options: SwalAjaxResultOptions): Promise<void> {
  const { rslt, message, successFallback, failureFallback } = options;
  const resolvedMessage =
    message && message.length > 0 ? message : (rslt ? successFallback : failureFallback) ?? "";
  if (!resolvedMessage || isSuppressedText(resolvedMessage)) return;
  await swalFire({ text: resolvedMessage, icon: rslt ? "success" : "error" });
}

/**
 * 확인 다이얼로그. 사용자가 확인하면 true 반환.
 * 레거시 cF.ui.swalOrConfirm 의 confirm 분기와 동일.
 */
export async function swalConfirm(text: string): Promise<boolean> {
  const result = await swalFire({ text, showCancelButton: true });
  return result.isConfirmed;
}

/**
 * 중립 알림 다이얼로그(아이콘 없음).
 * 검증 안내 등 성공/실패가 아닌 단순 안내에 사용한다.
 */
export async function swalAlert(text: string): Promise<void> {
  if (isSuppressedText(text)) return;
  await swalFire({ text });
}

/**
 * 요청 실패 알림. 인증 만료는 전역 인터셉터에서 이미 안내하므로 일반 오류로 다시 띄우지 않는다.
 */
export async function swalRequestError(error: unknown, text?: string): Promise<void> {
  if (isAuthExpiredError(error)) return;
  console.error("[swalRequestError] API request failed", error);
  const fallbackText = text ?? useLocaleStore().t("common.error.processing");
  await swalFire({ icon: "error", text: getAjaxResponseMessage(error) ?? fallbackText });
}
