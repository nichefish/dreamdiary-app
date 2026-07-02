import { ref } from "vue";
import { defineStore } from "pinia";

/** 일정 달력 aside 표시 여부 — 브라우저 localStorage 전용(서버·Pinia 영속 아님) */
const STORAGE_KEY_VISIBLE = "schedule_aside_visible";

function readStoredVisible() {
  if (typeof window === "undefined") return true;
  return window.localStorage.getItem(STORAGE_KEY_VISIBLE) !== "false";
}

/**
 * 일정 달력 aside 상태 스토어.
 * 저널 aside(`journalAside`)와 동일한 표시/숨김 토글 계약을 따른다.
 */
export const useScheduleAsideStore = defineStore("scheduleAside", () => {
  const visible = ref<boolean>(readStoredVisible());

  function setVisible(nextVisible: boolean) {
    visible.value = nextVisible;
    if (typeof window !== "undefined") {
      window.localStorage.setItem(STORAGE_KEY_VISIBLE, String(nextVisible));
    }
  }

  function show() {
    setVisible(true);
  }

  function hide() {
    setVisible(false);
  }

  function toggle() {
    setVisible(!visible.value);
  }

  return {
    visible,
    setVisible,
    show,
    hide,
    toggle,
  };
});
