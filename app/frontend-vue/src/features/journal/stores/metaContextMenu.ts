/**
 * metaContextMenu.ts
 * 메타 클릭 컨텍스트 메뉴 상태 관리.
 * 레거시 태그 컨텍스트 메뉴(tagContextMenu.ts)와 동일한 고정 위치 팝업 패턴.
 */
import { defineStore } from "pinia";
import { ref } from "vue";

export interface MetaContextMenuPayload {
  metaId: number | string;
  name: string;
  ctgr: string;
  unit?: string;
  contentSize?: number;
}

export const useMetaContextMenuStore = defineStore("metaContextMenu", () => {
  const visible = ref(false);
  const x = ref(0);
  const y = ref(0);
  const payload = ref<MetaContextMenuPayload>({ metaId: "", name: "", ctgr: "", unit: "" });

  /**
   * 컨텍스트 메뉴를 열고 클릭 좌표 근처에 위치시킨다.
   */
  function open(event: MouseEvent, meta: MetaContextMenuPayload): void {
    payload.value = meta;
    const menuWidth = 176;
    const menuHeight = 140;
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
