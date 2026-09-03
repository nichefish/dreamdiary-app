import { nextTick, ref } from "vue";
import type { useChatStore, ChatSession } from "@/features/chat/stores/chat";
import type { TranslateFn } from "@/features/chat/chatMessageFormat";

type ChatStore = ReturnType<typeof useChatStore>;

/**
 * useSessionRename
 *
 * 세션 칩 제목의 인라인 편집 상태와 커밋/취소 로직을 관리한다.
 * blur 와 Esc 취소가 경합하지 않도록 커밋 중 여부를 내부 flag 로 잠근다.
 *
 * @param chat 세션 rename 을 수행하는 채팅 스토어
 * @param t 오류 메시지 표시용 로케일 번역 함수
 */
export function useSessionRename(chat: ChatStore, t: TranslateFn) {
  /** 인라인 편집 중인 세션 ID */
  const renamingSessionId = ref<number | null>(null);
  const renameDraft = ref("");
  const isRenamingSaving = ref(false);
  const renameInputEls = new Map<number, HTMLInputElement>();
  /** blur와 Esc 취소가 경합하지 않도록 커밋 중인지 표시 */
  let renameCommitLocked = false;

  function setRenameInputRef(sessionId: number, el: unknown): void {
    if (el instanceof HTMLInputElement) {
      renameInputEls.set(sessionId, el);
    } else {
      renameInputEls.delete(sessionId);
    }
  }

  /**
   * 세션 칩 제목 인라인 편집을 시작한다.
   */
  function startRename(session: ChatSession): void {
    if (!session.id || isRenamingSaving.value) return;
    renamingSessionId.value = session.id;
    renameDraft.value = session.title || "";
    renameCommitLocked = false;
    nextTick(() => {
      const input = renameInputEls.get(session.id);
      if (!input) return;
      input.focus();
      input.select();
    });
  }

  function cancelRename(): void {
    renameCommitLocked = true;
    renamingSessionId.value = null;
    renameDraft.value = "";
    isRenamingSaving.value = false;
  }

  /**
   * 인라인 제목 편집을 저장한다. 비어 있으면 취소한다.
   */
  async function commitRename(session: ChatSession): Promise<void> {
    if (renameCommitLocked) return;
    if (renamingSessionId.value !== session.id) return;

    const nextTitle = renameDraft.value.trim();
    const currentTitle = (session.title || "").trim();
    if (!nextTitle) {
      chat.lastError = t("chat.session.rename.empty");
      cancelRename();
      return;
    }
    if (nextTitle === currentTitle) {
      cancelRename();
      return;
    }

    renameCommitLocked = true;
    isRenamingSaving.value = true;
    try {
      const updated = await chat.renameSession(session.id, nextTitle);
      if (!updated) {
        chat.lastError = t("chat.session.rename.failure");
      }
    } catch (e) {
      console.error("[AppChat] renameSession failed", { sessionId: session.id }, e);
      chat.lastError = t("chat.session.rename.failure");
    } finally {
      isRenamingSaving.value = false;
      renamingSessionId.value = null;
      renameDraft.value = "";
    }
  }

  return {
    renamingSessionId,
    renameDraft,
    isRenamingSaving,
    setRenameInputRef,
    startRename,
    cancelRename,
    commitRename,
  };
}
