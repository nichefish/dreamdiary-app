import { ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";
import { usePersonalPrefixOptionsStore } from "@/features/attachable/stores/personalPrefixOptions";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

export interface UserPrefix {
  id?: number;
  name: string;
  color?: string | null;
  sortOrder: number;
  activeYn?: "Y" | "N";
}

/**
 * 내 설정의 사용자 소유 말머리 관리 상태.
 * 평면 Prefix 목록의 서버 응답을 화면 SSOT로 사용한다.
 */
export const useUserPrefixesStore = defineStore("userPrefixes", () => {
  const { t } = useLocaleStore();
  const personalPrefixOptionsStore = usePersonalPrefixOptionsStore();
  const prefixes = ref<UserPrefix[]>([]);
  const loading = ref(false);
  const saving = ref(false);
  let fetchSequence = 0;

  /**
   * 현재 표시 목록을 비우고 진행 중인 이전 대상 조회를 무효화한다.
   * 대상 상세에서 상위 목록으로 돌아갈 때 다른 content_type의 항목이 남지 않게 한다.
   */
  function clearPrefixes() {
    fetchSequence += 1;
    prefixes.value = [];
    loading.value = false;
  }

  async function fetchPrefixes(contentType: string) {
    const requestSequence = ++fetchSequence;
    loading.value = true;
    try {
      const response = await axios.get("/api/my/prefixes", { params: { contentType } });
      if (requestSequence !== fetchSequence) {
        console.debug("[userPrefixes] stale list response ignored", {
          contentType,
          requestSequence,
          activeRequestSequence: fetchSequence,
        });
        return;
      }
      if (!response.data?.rslt) {
        throw new Error(response.data?.message ?? t("common.result.failure"));
      }
      prefixes.value = response.data?.rsltList ?? [];
    } catch (error) {
      if (requestSequence !== fetchSequence) {
        console.debug("[userPrefixes] stale list failure ignored", {
          contentType,
          requestSequence,
          activeRequestSequence: fetchSequence,
        });
        return;
      }
      throw error;
    } finally {
      if (requestSequence === fetchSequence) loading.value = false;
    }
  }

  async function savePrefix(contentType: string, payload: UserPrefix) {
    saving.value = true;
    try {
      if (payload.id) {
        await axios.put(`/api/my/prefixes/${payload.id}`, payload, { params: { contentType } });
      } else {
        await axios.post("/api/my/prefixes", payload, { params: { contentType } });
      }
      personalPrefixOptionsStore.invalidate(contentType);
      await fetchPrefixes(contentType);
    } finally {
      saving.value = false;
    }
  }

  async function setPrefixActive(contentType: string, prefixId: number, active: boolean) {
    await axios.patch(`/api/my/prefixes/${prefixId}/active`, null, { params: { contentType, active } });
    personalPrefixOptionsStore.invalidate(contentType);
    await fetchPrefixes(contentType);
  }

  return { prefixes, loading, saving, clearPrefixes, fetchPrefixes, savePrefix, setPrefixActive };
});
