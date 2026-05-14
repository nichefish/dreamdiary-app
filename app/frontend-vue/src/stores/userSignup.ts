/**
 * 계정 신청 Pinia 스토어
 * - 승인관리 목록 조회/승인/반려
 * - 신청 폼 제출
 *
 * @author nichefish
 */
import { ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";

/** 계정 신청 행 DTO */
export interface SignupRequestRow {
  id: number;
  username: string;
  nickname: string;
  email: string;
  status: string;
  createdAt: string;
}

export const useUserSignupStore = defineStore("userSignup", () => {
  /** 승인 대기 목록 */
  const pendingList = ref<SignupRequestRow[]>([]);
  /** 최근 신청 목록 */
  const recentList = ref<SignupRequestRow[]>([]);
  const loading = ref(false);
  /** 신청 폼 제출 진행 상태 */
  const submitting = ref(false);

  /**
   * 승인관리 목록 조회 — GET /api/user/signup-requests
   */
  async function fetchApprovalList(): Promise<void> {
    loading.value = true;
    try {
      const res = await axios.get("/api/user/signup-requests");
      const obj = res.data?.rsltObj as
        | { pendingList?: SignupRequestRow[]; recentList?: SignupRequestRow[] }
        | null;
      pendingList.value = obj?.pendingList ?? [];
      recentList.value = obj?.recentList ?? [];
    } catch (e) {
      console.error("[userSignup] fetchApprovalList 실패", e);
    } finally {
      loading.value = false;
    }
  }

  /**
   * 계정 신청 승인 — POST /api/user/signup-requests/{id}/approval
   *
   * @param id 신청 식별자
   */
  async function approve(id: number): Promise<boolean> {
    const confirmed = window.confirm("승인하시겠습니까?");
    if (!confirmed) return false;
    try {
      const res = await axios.post(`/api/user/signup-requests/${id}/approval`);
      if (res.data?.rslt) {
        pendingList.value = pendingList.value.filter((r) => r.id !== id);
        const target = recentList.value.find((r) => r.id === id);
        if (target) target.status = "APPROVED";
        return true;
      }
      alert(res.data?.message || "처리에 실패했습니다.");
      return false;
    } catch (e) {
      console.error("[userSignup] approve 실패", e);
      return false;
    }
  }

  /**
   * 계정 신청 반려 — POST /api/user/signup-requests/{id}/rejection
   *
   * @param id 신청 식별자
   */
  async function reject(id: number): Promise<boolean> {
    const confirmed = window.confirm("반려하시겠습니까?");
    if (!confirmed) return false;
    try {
      const res = await axios.post(`/api/user/signup-requests/${id}/rejection`);
      if (res.data?.rslt) {
        pendingList.value = pendingList.value.filter((r) => r.id !== id);
        const target = recentList.value.find((r) => r.id === id);
        if (target) target.status = "REJECTED";
        return true;
      }
      alert(res.data?.message || "처리에 실패했습니다.");
      return false;
    } catch (e) {
      console.error("[userSignup] reject 실패", e);
      return false;
    }
  }

  /**
   * 계정 신청 폼 제출 — POST /api/user/signup-requests (multipart/form-data)
   *
   * @param formData 신청 폼 데이터
   */
  async function submitSignup(
    formData: FormData,
  ): Promise<{ ok: boolean; message: string }> {
    submitting.value = true;
    try {
      const res = await axios.post("/api/user/signup-requests", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      return { ok: !!res.data?.rslt, message: res.data?.message ?? "" };
    } catch (e: unknown) {
      const msg =
        (e as { response?: { data?: { message?: string } } })?.response?.data?.message ??
        "처리 중 오류가 발생했습니다.";
      return { ok: false, message: msg };
    } finally {
      submitting.value = false;
    }
  }

  return {
    pendingList,
    recentList,
    loading,
    submitting,
    fetchApprovalList,
    approve,
    reject,
    submitSignup,
  };
});
