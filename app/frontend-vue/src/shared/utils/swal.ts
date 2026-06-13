/**
 * swal.ts
 * SweetAlert2 공통 래퍼. 레거시 cF.ui.swalOrConfirm / Swal.fire 패턴을 Vue SPA 에서 통일 적용.
 */
import axios from "axios";
import Swal from "sweetalert2/dist/sweetalert2.js";
import { isAuthExpiredError } from "@/shared/utils/authError";

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

/**
 * 확인 다이얼로그. 사용자가 확인하면 true 반환.
 * 레거시 cF.ui.swalOrConfirm 의 confirm 분기와 동일.
 */
export async function swalConfirm(text: string): Promise<boolean> {
  const result = await Swal.fire({ text, showCancelButton: true });
  return result.isConfirmed;
}

/**
 * 알림 다이얼로그.
 * 레거시 Swal.fire({ text }) 단순 호출과 동일.
 */
export async function swalAlert(text: string): Promise<void> {
  if (text === "AUTH_EXPIRED") return;
  await Swal.fire({ text });
}

/**
 * 요청 실패 알림. 인증 만료는 전역 인터셉터에서 이미 안내하므로 일반 오류로 다시 띄우지 않는다.
 */
export async function swalRequestError(error: unknown, text = "요청 처리 중 오류가 발생했습니다."): Promise<void> {
  if (isAuthExpiredError(error)) return;
  await swalAlert(getAjaxResponseMessage(error) ?? text);
}
