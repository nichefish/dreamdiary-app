/**
 * tagContextMenu.ts
 * 태그 클릭 컨텍스트 메뉴 상태 관리.
 * 레거시 journalDayTagContextMenuShell.ts 의 전역 DOM 방식을 Pinia store 로 대체.
 */
import { defineStore } from "pinia";
import { ref } from "vue";

export interface TagContextMenuPayload {
  tagId: number | string;
  name: string;
  ctgr: string;
  contentType: string;
}

export const useTagContextMenuStore = defineStore("tagContextMenu", () => {
  const visible = ref(false);
  const x = ref(0);
  const y = ref(0);
  const payload = ref<TagContextMenuPayload>({ tagId: "", name: "", ctgr: "", contentType: "" });

  /**
   * 컨텍스트 메뉴를 열고 클릭 좌표 근처에 위치시킨다.
   * 레거시 getContextMenuPosition 과 동일하게 viewport 경계를 넘지 않도록 clamping.
   */
  function open(event: MouseEvent, tag: TagContextMenuPayload): void {
    payload.value = tag;
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
