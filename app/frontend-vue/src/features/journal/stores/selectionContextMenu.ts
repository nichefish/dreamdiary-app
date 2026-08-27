/**
 * selectionContextMenu.ts
 * 엔트리·리플렉션 본문 선택(드래그) 후 우클릭 시 뜨는 저널 맥락 컨텍스트 메뉴 상태.
 * metaContextMenu.ts 와 동일한 고정 위치 팝업 패턴을 따른다.
 */
import { defineStore } from "pinia";
import { ref } from "vue";

/**
 * 선택 컨텍스트 메뉴 payload.
 * text 는 드래그로 선택된 평문, type 은 선택 영역에서 파생한 검색 도메인(DIARY|DREAM)이다.
 */
export interface SelectionContextMenuPayload {
  text: string;
  type: "DIARY" | "DREAM";
}

export const useSelectionContextMenuStore = defineStore("selectionContextMenu", () => {
  const visible = ref(false);
  const x = ref(0);
  const y = ref(0);
  const payload = ref<SelectionContextMenuPayload>({ text: "", type: "DIARY" });

  /**
   * 컨텍스트 메뉴를 열고 클릭 좌표 근처에 위치시킨다.
   * 메뉴가 뷰포트를 벗어나지 않도록 좌/상단을 클램프한다.
   */
  function open(event: MouseEvent, selection: SelectionContextMenuPayload): void {
    payload.value = selection;
    const menuWidth = 176;
    const menuHeight = 92;
    let left = event.clientX;
    let top = event.clientY + 8;
    left = Math.min(Math.max(left, 8), Math.max(window.innerWidth - menuWidth - 8, 8));
    top = Math.min(Math.max(top, 8), Math.max(window.innerHeight - menuHeight - 8, 8));
    x.value = left;
    y.value = top;
    visible.value = true;
  }

  function close(): void {
    visible.value = false;
  }

  return { visible, x, y, payload, open, close };
});