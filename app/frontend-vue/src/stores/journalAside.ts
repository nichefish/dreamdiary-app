import { ref } from "vue";
import { defineStore } from "pinia";

const STORAGE_KEY = "journal_aside_visible";

function readStoredVisible() {
  if (typeof window === "undefined") return true;
  return window.localStorage.getItem(STORAGE_KEY) !== "false";
}

export const useJournalAsideStore = defineStore("journalAside", () => {
  const visible = ref<boolean>(readStoredVisible());

  function setVisible(nextVisible: boolean) {
    visible.value = nextVisible;
    if (typeof window !== "undefined") {
      window.localStorage.setItem(STORAGE_KEY, String(nextVisible));
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
