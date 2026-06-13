import { onBeforeUnmount, ref } from "vue";

const DEFAULT_SAFE_CLOSE_MS = 2000;

export function useSafeModalClose(onClose: () => void, resetMs = DEFAULT_SAFE_CLOSE_MS) {
  const closeArmed = ref(false);
  let closeTimer: ReturnType<typeof setTimeout> | null = null;

  function clearCloseTimer(): void {
    if (closeTimer) {
      clearTimeout(closeTimer);
      closeTimer = null;
    }
  }

  function resetSafeClose(): void {
    clearCloseTimer();
    closeArmed.value = false;
  }

  function requestSafeClose(): void {
    if (!closeArmed.value) {
      closeArmed.value = true;
      clearCloseTimer();
      closeTimer = setTimeout(resetSafeClose, resetMs);
      return;
    }

    resetSafeClose();
    onClose();
  }

  onBeforeUnmount(resetSafeClose);

  return {
    closeArmed,
    requestSafeClose,
    resetSafeClose,
  };
}
