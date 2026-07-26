/**
 * journalAnnualAside.ts
 * 결산 목록/상세 공통 FILTER aside 표시 상태.
 * 저널 일자 useJournalAsideStore 와 같이 툴바·레이아웃이 공유한다.
 */
import { ref } from "vue";
import { defineStore } from "pinia";

const STORAGE_KEY_VISIBLE = "journal_annual_aside_visible";

function readStoredVisible(): boolean {
  if (typeof window === "undefined") return true;
  return window.localStorage.getItem(STORAGE_KEY_VISIBLE) !== "false";
}

export const useJournalAnnualAsideStore = defineStore("journalAnnualAside", () => {
  const visible = ref<boolean>(readStoredVisible());

  function setVisible(nextVisible: boolean): void {
    visible.value = nextVisible;
    if (typeof window !== "undefined") {
      window.localStorage.setItem(STORAGE_KEY_VISIBLE, String(nextVisible));
    }
  }

  function show(): void {
    setVisible(true);
  }

  function hide(): void {
    setVisible(false);
  }

  function toggle(): void {
    setVisible(!visible.value);
  }

  return { visible, setVisible, show, hide, toggle };
});
