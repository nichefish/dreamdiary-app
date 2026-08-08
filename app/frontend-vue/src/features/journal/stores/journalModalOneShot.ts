import { ref } from "vue";

/**
 * 저널 모달의 일회성 UI 신호.
 * 저장 직후 챕터 펼침·Reflection 접힘만 전달하며, 영속 COLLAPSED 상태는 바꾸지 않는다.
 */
export function createJournalModalOneShotSignals() {
  /**
   * 신규 엔트리 저장 후 펼칠 챕터 ID.
   * 저장된 COLLAPSED 상태는 변경하지 않고, 목록·상세에 마운트된 챕터가 일회성 로컬 펼침을 적용하는 동안만 유지한다.
   */
  const entryCreatedExpandChapterId = ref<number | string | null>(null);

  /**
   * 신규 Reflection 등록 직후 접힐 Reflection ID.
   * 챕터 일회성 펼침·부모 expand signal 보다 우선해 해당 임베드만 로컬 접힘으로 시작한다. 수정 저장에는 쓰지 않는다.
   */
  const reflectionCreatedCollapseId = ref<number | string | null>(null);

  /** 신규 엔트리가 들어간 챕터를 현재 화면에서 펼침 대상으로 표시한다. */
  function requestEntryCreatedChapterExpand(chapterId: number | string): void {
    entryCreatedExpandChapterId.value = chapterId;
  }

  /** 같은 저장 요청이 표시한 챕터만 해제하여 뒤이은 요청을 지우지 않는다. */
  function clearEntryCreatedChapterExpand(chapterId: number | string): void {
    if (String(entryCreatedExpandChapterId.value) === String(chapterId)) {
      entryCreatedExpandChapterId.value = null;
    }
  }

  /** 신규 Reflection 등록 직후 접힘 대상으로 표시한다. */
  function requestReflectionCreatedCollapse(reflectionId: number | string): void {
    reflectionCreatedCollapseId.value = reflectionId;
  }

  /** 같은 저장 요청이 표시한 Reflection만 해제하여 뒤이은 요청을 지우지 않는다. */
  function clearReflectionCreatedCollapse(reflectionId: number | string): void {
    if (String(reflectionCreatedCollapseId.value) === String(reflectionId)) {
      reflectionCreatedCollapseId.value = null;
    }
  }

  return {
    entryCreatedExpandChapterId,
    reflectionCreatedCollapseId,
    requestEntryCreatedChapterExpand,
    clearEntryCreatedChapterExpand,
    requestReflectionCreatedCollapse,
    clearReflectionCreatedCollapse,
  };
}