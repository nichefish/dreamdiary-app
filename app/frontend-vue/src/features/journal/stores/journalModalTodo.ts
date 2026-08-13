import { ref } from "vue";
import { assertAuthenticatedBeforeModal } from "@/shared/auth/sessionPing";
import type { JournalTodoRegistModel } from "@/features/journal/stores/journalModal.types";

/**
 * 저널 할일 등록/수정 모달 surface.
 */
export function createJournalModalTodo() {
  /** 할일 등록/수정 모달 오픈 여부 */
  const todoRegistOpen = ref(false);
  /** 할일 등록/수정 폼 모델 */
  const todoRegistModel = ref<JournalTodoRegistModel | null>(null);

  /**
   * 할일 등록/수정 모달을 연다.
   * @param payload - 수정 시 기존 데이터, 신규 시 yy/mnth 등 초기값
   */
  async function openTodoRegist(payload?: JournalTodoRegistModel) {
    if (!await assertAuthenticatedBeforeModal()) return;
    todoRegistModel.value = {
      categoryCode: "",
      title: "",
      content: "",
      tag: { tagListStrWithCtgr: "" },
      ...payload,
    };
    todoRegistOpen.value = true;
  }

  /** 할일 등록/수정 모달을 닫는다. */
  function closeTodoRegist() {
    todoRegistOpen.value = false;
  }

  return {
    todoRegistOpen,
    todoRegistModel,
    openTodoRegist,
    closeTodoRegist,
  };
}
