import Swal from "sweetalert2";

/**
 * 확인 다이얼로그. 사용자가 확인하면 true 반환.
 * Vue {@code shared/utils/swal.ts} 의 swalConfirm 과 동일.
 */
export async function swalConfirm(text: string): Promise<boolean> {
  const result = await Swal.fire({ text, showCancelButton: true });
  return result.isConfirmed;
}
