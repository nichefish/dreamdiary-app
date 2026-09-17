/**
 * useJournalSearchShortcut.ts
 * 좌측 Shift(ShiftLeft) 더블탭으로 일기 전체검색 팝업을 새 창으로 여는 단축키.
 * 저널 일자 화면(라우트 meta `journalSearchShortcut`) 안에서만 동작하며, 검색 팝업 등 다른 화면에서는 무시한다.
 * IntelliJ "Search Everywhere"(Shift 두 번) 손버릇과 같은 진입점을 제공하며,
 * 팝업 진입 방식은 툴바 전체검색(openSearchTab)·본문 선택 우클릭 검색과 동일한 계약을 따른다.
 */
import { onMounted, onUnmounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "@/shared/auth/stores/auth";
import { assertAuthenticatedBeforePopup } from "@/shared/auth/popupAuth";
import { joinAppBasePath } from "@/shared/utils/appPath";

/** 두 번째 ShiftLeft를 같은 제스처로 인정하는 최대 간격(ms). */
const DOUBLE_TAP_WINDOW_MS = 400;

/**
 * 이벤트 대상이 텍스트 편집 요소인지 판별한다.
 * input/textarea/select 또는 contenteditable 이면 true. 이런 필드에 포커스가 있으면
 * 사용자가 글자를 입력하는 중이므로 Shift(한글 쌍자음 입력 포함)를 단축키로 가로채지 않는다.
 */
function isEditableTarget(target: EventTarget | null): boolean {
  const el = target as HTMLElement | null;
  if (!el || typeof el.tagName !== "string") return false;
  const tag = el.tagName;
  if (tag === "INPUT" || tag === "TEXTAREA" || tag === "SELECT") return true;
  return el.isContentEditable === true;
}

/**
 * 좌측 Shift 더블탭 → 일기 검색 팝업 전역 단축키를 설치한다.
 * App.vue 등 앱 루트 setup에서 한 번 호출한다. 인증 상태에서만 팝업을 연다.
 */
export function useJournalSearchShortcut(): void {
  const authStore = useAuthStore();
  const route = useRoute();
  const router = useRouter();
  let lastShiftAt = 0;

  /**
   * 키다운 감시.
   * 좌측 Shift가 DOUBLE_TAP_WINDOW_MS 안에 연속 두 번 눌리면 일기 검색 팝업을 연다.
   * Shift 외 다른 키가 끼면 카운터를 리셋해 순수 Shift 연타만 제스처로 인정한다.
   * 누른 채 발생하는 반복 keydown(event.repeat)은 무시한다.
   */
  function onKeydown(event: KeyboardEvent): void {
    if (event.repeat) return;
    // 저널 일자 화면(JournalDayLayout 하위 라우트) 안에서만 동작한다. 검색 팝업 등 다른 화면에서는 무시한다.
    if (!route.matched.some((r) => r.meta.journalSearchShortcut === true)) {
      lastShiftAt = 0;
      return;
    }
    // 입력 필드 포커스·IME 조합 중에는 무시한다(한글 쌍자음 입력의 Shift 를 단축키로 가로채지 않도록).
    if (event.isComposing || isEditableTarget(event.target)) {
      lastShiftAt = 0;
      return;
    }
    if (event.code !== "ShiftLeft") {
      lastShiftAt = 0;
      return;
    }
    const now = Date.now();
    if (now - lastShiftAt <= DOUBLE_TAP_WINDOW_MS) {
      lastShiftAt = 0;
      void openDiarySearchPopup();
    } else {
      lastShiftAt = now;
    }
  }

  /** 일기(DIARY) 전체검색 팝업을 새 창으로 연다. 미인증이면 열지 않는다. */
  async function openDiarySearchPopup(): Promise<void> {
    if (!authStore.isAuthenticated) return;
    if (!await assertAuthenticatedBeforePopup(router, route)) return;
    const params = new URLSearchParams({ type: "DIARY" });
    const popup = window.open(
      joinAppBasePath(`/journal/entry/search?${params.toString()}`),
      "journal-entry-search-DIARY",
      "width=1960,height=1440,top=0,left=270",
    );
    if (popup) popup.focus();
  }

  onMounted(() => {
    document.addEventListener("keydown", onKeydown);
  });
  onUnmounted(() => {
    document.removeEventListener("keydown", onKeydown);
  });
}