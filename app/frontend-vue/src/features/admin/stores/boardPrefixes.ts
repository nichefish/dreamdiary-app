import { ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";
import { assertAuthenticatedBeforeModal } from "@/shared/auth/sessionPing";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

export interface BoardPrefix {
  id?: number;
  name: string;
  color?: string | null;
  sortOrder: number;
  activeYn?: "Y" | "N";
}

export interface BoardPrefixTarget {
  id: number;
  boardKey: string;
  boardName: string;
}

interface BoardPrefixManagement {
  boardId: number;
  boardKey: string;
  boardName: string;
  prefixes: BoardPrefix[];
}

/**
 * 게시판 관리 화면의 Prefix 전용 상태.
 * 게시판 ID를 관리 문맥으로 고정하고 서버가 반환한 boardKey별 GLOBAL Prefix 목록을 화면 SSOT로 사용한다.
 */
export const useBoardPrefixesStore = defineStore("boardPrefixes", () => {
  const { t } = useLocaleStore();
  const modalOpen = ref(false);
  const boardId = ref<number | null>(null);
  const boardKey = ref("");
  const boardName = ref("");
  const prefixes = ref<BoardPrefix[]>([]);
  const loading = ref(false);
  const saving = ref(false);
  const error = ref("");

  async function open(target: BoardPrefixTarget) {
    if (!await assertAuthenticatedBeforeModal()) return;
    boardId.value = target.id;
    boardKey.value = target.boardKey;
    boardName.value = target.boardName;
    modalOpen.value = true;
    await fetchManagement();
  }

  function close() {
    modalOpen.value = false;
    boardId.value = null;
    boardKey.value = "";
    boardName.value = "";
    prefixes.value = [];
    error.value = "";
  }

  async function fetchManagement() {
    const targetBoardId = boardId.value;
    if (targetBoardId == null) {
      console.error("[BoardPrefixes] 관리 대상 게시판 ID 누락");
      throw new Error(t("board.group.prefix.load.failure"));
    }
    loading.value = true;
    error.value = "";
    try {
      const response = await axios.get(`/api/board/groups/${targetBoardId}/prefixes`);
      if (!response.data?.rslt) {
        throw new Error(response.data?.message ?? t("board.group.prefix.load.failure"));
      }
      if (boardId.value !== targetBoardId) return;
      const management = (response.data?.rsltObj ?? {}) as Partial<BoardPrefixManagement>;
      boardKey.value = management.boardKey ?? boardKey.value;
      boardName.value = management.boardName ?? boardName.value;
      prefixes.value = Array.isArray(management.prefixes) ? management.prefixes : [];
    } catch (cause) {
      console.error("[BoardPrefixes] 게시판 말머리 관리 조회 실패", {
        boardId: targetBoardId,
        cause,
      });
      error.value = cause instanceof Error ? cause.message : t("board.group.prefix.load.failure");
      throw cause;
    } finally {
      loading.value = false;
    }
  }

  async function savePrefix(payload: BoardPrefix) {
    const targetBoardId = boardId.value;
    if (targetBoardId == null) {
      console.error("[BoardPrefixes] 저장 대상 게시판 ID 누락", { prefixId: payload.id });
      throw new Error(t("board.group.prefix.save.failure"));
    }
    saving.value = true;
    try {
      const response = payload.id
        ? await axios.put(`/api/board/groups/${targetBoardId}/prefixes/${payload.id}`, payload)
        : await axios.post(`/api/board/groups/${targetBoardId}/prefixes`, payload);
      if (!response.data?.rslt) {
        throw new Error(response.data?.message ?? t("board.group.prefix.save.failure"));
      }
      await fetchManagement();
    } catch (cause) {
      console.error("[BoardPrefixes] 게시판 말머리 저장 실패", {
        boardId: targetBoardId,
        prefixId: payload.id,
        cause,
      });
      throw cause;
    } finally {
      saving.value = false;
    }
  }

  async function setPrefixActive(prefixId: number, active: boolean) {
    const targetBoardId = boardId.value;
    if (targetBoardId == null) {
      console.error("[BoardPrefixes] 활성 변경 대상 게시판 ID 누락", { prefixId, active });
      throw new Error(t("board.group.prefix.active.failure"));
    }
    saving.value = true;
    try {
      const response = await axios.patch(
        `/api/board/groups/${targetBoardId}/prefixes/${prefixId}/active`,
        null,
        { params: { active } },
      );
      if (!response.data?.rslt) {
        throw new Error(response.data?.message ?? t("board.group.prefix.active.failure"));
      }
      await fetchManagement();
    } catch (cause) {
      console.error("[BoardPrefixes] 게시판 말머리 활성 상태 변경 실패", {
        boardId: targetBoardId,
        prefixId,
        active,
        cause,
      });
      throw cause;
    } finally {
      saving.value = false;
    }
  }

  return {
    modalOpen,
    boardId,
    boardKey,
    boardName,
    prefixes,
    loading,
    saving,
    error,
    open,
    close,
    fetchManagement,
    savePrefix,
    setPrefixActive,
  };
});
