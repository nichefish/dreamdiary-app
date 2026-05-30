/**
 * swal.ts
 * SweetAlert2 공통 래퍼. 레거시 cF.ui.swalOrConfirm / Swal.fire 패턴을 Vue SPA 에서 통일 적용.
 */
import Swal from "sweetalert2/dist/sweetalert2.js";

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
  await Swal.fire({ text });
}
