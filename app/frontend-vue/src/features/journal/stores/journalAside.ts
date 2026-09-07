import { computed, ref } from "vue";
import { defineStore } from "pinia";

const STORAGE_KEY_VISIBLE = "journal_aside_visible";
/** 저널 Pinpoint 고정 기간 — 브라우저 localStorage 전용(서버·Pinia 영속 아님) */
const STORAGE_KEY_PINPOINT = "journal_day_pinpoint";

function readStoredVisible() {
  if (typeof window === "undefined") return true;
  return window.localStorage.getItem(STORAGE_KEY_VISIBLE) !== "false";
}

/** Pinpoint 고정 스냅샷 — viewType 별 복원 기준(일간 stdrdDt·주간 weekStartDt·월간 yy/mnth)을 함께 보관한다. */
type PinpointSnapshot = {
  viewType: string;
  yy: number;
  mnth: number;
  weekStartDt?: string;
  stdrdDt?: string;
};

/**
 * localStorage 에서 Pinpoint 스냅샷을 읽는다.
 *
 * <p>이전 스키마({yy,mnth})는 viewType 없이 월간 고정으로 호환한다.</p>
 *
 * @returns 유효한 고정값 또는 null
 */
function readStoredPinpoint(): PinpointSnapshot | null {
  if (typeof window === "undefined") return null;
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY_PINPOINT);
    if (!raw) return null;
    const parsed = JSON.parse(raw) as {
      viewType?: unknown;
      yy?: unknown;
      mnth?: unknown;
      weekStartDt?: unknown;
      stdrdDt?: unknown;
    };
    const yy = Number(parsed.yy);
    const mnth = Number(parsed.mnth);
    if (!Number.isFinite(yy) || !Number.isFinite(mnth) || mnth < 1 || mnth > 12) {
      return null;
    }
    return {
      viewType: typeof parsed.viewType === "string" ? parsed.viewType : "",
      yy,
      mnth,
      weekStartDt: typeof parsed.weekStartDt === "string" ? parsed.weekStartDt : undefined,
      stdrdDt: typeof parsed.stdrdDt === "string" ? parsed.stdrdDt : undefined,
    };
  } catch {
    return null;
  }
}

/**
 * Pinpoint 스냅샷을 localStorage 에 저장하거나 제거한다.
 *
 * @param snapshot 저장할 스냅샷. null 이면 키 삭제
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
  /** Pinpoint — 고정된 뷰 유형 (null: 미고정) */
  const pinnedViewType = ref<string | null>(initialPinpoint?.viewType ?? null);
  /** Pinpoint — 고정된 년/월 (null: 미고정) */
  const pinnedYy = ref<number | null>(initialPinpoint?.yy ?? null);
  const pinnedMnth = ref<number | null>(initialPinpoint?.mnth ?? null);
  /** Pinpoint — 주간 뷰 복원 기준 시작일 */
  const pinnedWeekStartDt = ref<string | null>(initialPinpoint?.weekStartDt ?? null);
  /** Pinpoint — 일간 뷰 복원 기준일 */
  const pinnedStdrdDt = ref<string | null>(initialPinpoint?.stdrdDt ?? null);

  /** 어사이드 표시용 라벨 — 일간=기준일, 주간=주 시작일, 그 외=연/월. */
  const pinnedLabel = computed<string>(() => {
    if (pinnedYy.value == null || pinnedMnth.value == null) return "";
    if (pinnedViewType.value === "DAILY" && pinnedStdrdDt.value) return pinnedStdrdDt.value;
    if (pinnedViewType.value === "WEEKLY" && pinnedWeekStartDt.value) return pinnedWeekStartDt.value;
    return `${pinnedYy.value} / ${pinnedMnth.value}`;
  });

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
   * 현재 조회 중인 기간을 Pinpoint 로 고정하고 localStorage 에 반영한다.
   *
   * <p>viewType 에 따라 복원 기준(일간 stdrdDt·주간 weekStartDt·월간 yy/mnth)을 함께 저장한다.</p>
   *
   * @param snapshot 고정할 스냅샷
   */
  function setPinpoint(snapshot: PinpointSnapshot) {
    pinnedViewType.value = snapshot.viewType;
    pinnedYy.value = snapshot.yy;
    pinnedMnth.value = snapshot.mnth;
    pinnedWeekStartDt.value = snapshot.weekStartDt ?? null;
    pinnedStdrdDt.value = snapshot.stdrdDt ?? null;
    writeStoredPinpoint(snapshot);
  }

  return {
    visible,
    pinnedViewType,
    pinnedYy,
    pinnedMnth,
    pinnedWeekStartDt,
    pinnedStdrdDt,
    pinnedLabel,
    setVisible,
    show,
    hide,
    toggle,
    setPinpoint,
  };
});
