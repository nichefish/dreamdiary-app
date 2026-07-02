import { ref } from "vue";
import { defineStore } from "pinia";

const STORAGE_KEY_VISIBLE = "journal_aside_visible";
/** 저널 Pinpoint 고정 연·월 — 브라우저 localStorage 전용(서버·Pinia 영속 아님) */
const STORAGE_KEY_PINPOINT = "journal_day_pinpoint";

function readStoredVisible() {
  if (typeof window === "undefined") return true;
  return window.localStorage.getItem(STORAGE_KEY_VISIBLE) !== "false";
}

type PinpointSnapshot = { yy: number; mnth: number };

/**
 * localStorage 에서 Pinpoint 연·월을 읽는다.
 *
 * @returns 유효한 고정값 또는 null
 */
function readStoredPinpoint(): PinpointSnapshot | null {
  if (typeof window === "undefined") return null;
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY_PINPOINT);
    if (!raw) return null;
    const parsed = JSON.parse(raw) as { yy?: unknown; mnth?: unknown };
    const yy = Number(parsed.yy);
    const mnth = Number(parsed.mnth);
    if (!Number.isFinite(yy) || !Number.isFinite(mnth) || mnth < 1 || mnth > 12) {
      return null;
    }
    return { yy, mnth };
  } catch {
    return null;
  }
}

/**
 * Pinpoint 연·월을 localStorage 에 저장하거나 제거한다.
 *
 * @param snapshot 저장할 연·월. null 이면 키 삭제
 */
function writeStoredPinpoint(snapshot: PinpointSnapshot | null): void {
  if (typeof window === "undefined") return;
  if (snapshot == null) {
    window.localStorage.removeItem(STORAGE_KEY_PINPOINT);
    return;
  }
  window.localStorage.setItem(STORAGE_KEY_PINPOINT, JSON.stringify(snapshot));
}

export const useJournalAsideStore = defineStore("journalAside", () => {
  const visible = ref<boolean>(readStoredVisible());

  const initialPinpoint = readStoredPinpoint();
  /** Pinpoint — 고정된 년/월 (null: 미고정) */
  const pinnedYy = ref<number | null>(initialPinpoint?.yy ?? null);
  const pinnedMnth = ref<number | null>(initialPinpoint?.mnth ?? null);

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

  /**
   * 현재 조회 중인 연·월을 Pinpoint 로 고정하고 localStorage 에 반영한다.
   *
   * @param yy 연도
   * @param mnth 월 (1-based)
   */
  function setPinpoint(yy: number, mnth: number) {
    pinnedYy.value = yy;
    pinnedMnth.value = mnth;
    writeStoredPinpoint({ yy, mnth });
  }

  return {
    visible,
    pinnedYy,
    pinnedMnth,
    setVisible,
    show,
    hide,
    toggle,
    setPinpoint,
  };
});
