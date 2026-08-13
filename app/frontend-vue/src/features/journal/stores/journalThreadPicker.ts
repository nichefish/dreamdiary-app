import { ref } from "vue";
import axios from "axios";
import { assertAuthenticatedBeforeModal } from "@/shared/auth/sessionPing";
import type { JournalThreadDto } from "@/features/journal/stores/journalThread.types";

export interface JournalThreadPickerDeps {
  t: (key: string) => string;
}

export function createJournalThreadPicker(deps: JournalThreadPickerDeps) {
  const { t } = deps;

  // ---- 스레드 피커 모달 상태 ----
  const pickerOpen = ref(false);
  const pickerLoading = ref(false);
  const pickerSearched = ref(false);
  const pickerSearchResults = ref<JournalThreadDto[]>([]);
  /** 피커 검색 실패 메시지 */
  const pickerSearchError = ref<string | null>(null);

  /** 스레드 피커 모달 열기. 인증 확인 후에만 연다. */
  async function openPicker(): Promise<void> {
    if (!await assertAuthenticatedBeforeModal()) return;
    pickerOpen.value = true;
    pickerSearched.value = false;
    pickerSearchResults.value = [];
    pickerSearchError.value = null;
  }

  /** 스레드 피커 모달 닫기 */
  function closePicker(): void {
    pickerOpen.value = false;
    pickerSearchResults.value = [];
  }

  /**
   * 피커 모달용 스레드 목록 검색.
   * GET /api/journal/threads — 키워드 검색.
   *
   * @param keyword - 검색어
   */
  async function searchThreadsForPicker(keyword: string): Promise<void> {
    pickerLoading.value = true;
    pickerSearched.value = true;
    pickerSearchError.value = null;
    try {
      const params = new URLSearchParams();
      params.set("page", "0");
      params.set("size", "50");
      const trimmed = keyword.trim();
      if (trimmed) {
        params.set("searchType", "title");
        params.set("searchKeyword", trimmed);
      }
      const res = await axios.get("/api/journal/threads", { params });
      const pageResult = res.data?.rsltObj;
      pickerSearchResults.value = (pageResult?.content ?? []) as JournalThreadDto[];
    } catch (e: unknown) {
      console.error("[journalThread] searchThreadsForPicker failed", { keyword }, e);
      pickerSearchError.value = t("journal.thread.related.picker.load.failure");
    } finally {
      pickerLoading.value = false;
    }
  }

  return {
    pickerOpen,
    pickerLoading,
    pickerSearched,
    pickerSearchResults,
    pickerSearchError,
    openPicker,
    closePicker,
    searchThreadsForPicker,
  };
}
