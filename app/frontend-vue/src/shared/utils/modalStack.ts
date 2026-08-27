/**
 * modalStack.ts
 * 중첩 Bootstrap 모달의 z-index 스태킹 보정.
 * <p>
 * App.vue 전역 스타일이 `body.modal-open .modal`을 z-index 6100(채팅 드로어 6002 위)으로 평탄화하는데,
 * 두 모달이 같은 6100이면 DOM 순서로 스태킹이 결정돼 나중 마운트 모달(예: 스레드 상세)이 위에 그려지고
 * 그 안에서 연 자식 모달(수정·댓글·이력·관련글)이 뒤에 깔린다. 이 핸들러는 모달이 열릴 때마다 이미 열린
 * 모달 수에 따라 z-index를 올려(6100 + n*STEP) 자식이 항상 부모 위로 오게 한다.
 * <p>
 * 스택 상한은 {@link MODAL_MAX_Z}(= SweetAlert {@link SWAL_Z} - 20)로 캡해, 깊은 중첩에서도
 * 확인창이 모달에 가려지지 않게 한다.
 * <p>
 * 또한 TinyMCE 다이얼로그(`.tox-tinymce-aux`, find/replace·code 등)로의 `focusin`을 capture 단계에서
 * 가로채 Bootstrap 모달 FocusTrap의 포커스 회수를 면제한다 — 모달 안 에디터에서 다이얼로그 입력이 막히던 문제.
 */
import { MODAL_BASE_Z, MODAL_MAX_Z, MODAL_STEP } from "@/shared/utils/overlayZIndex";

let installed = false;

/**
 * 이미 열린 모달 수에 대한 스택 z-index를 계산한다. (단위 테스트·문서용)
 *
 * @param openCount 이 모달이 열리기 직전 `.modal.show` 개수
 */
export function resolveStackedModalZ(openCount: number): number {
  const raw = MODAL_BASE_Z + Math.max(0, openCount) * MODAL_STEP;
  return Math.min(raw, MODAL_MAX_Z);
}

/**
 * 문서 레벨 `show.bs.modal` 리스너를 1회 설치한다. 앱 진입 시 App.vue에서 호출한다.
 * inline z-index는 평탄 CSS(`body.modal-open .modal`)보다 특이도가 높아 스택 모달에서 우선한다.
 */
export function installModalStacking(): void {
  if (installed || typeof document === "undefined") return;
  installed = true;

  document.addEventListener("show.bs.modal", (e: Event) => {
    const target = e.target;
    if (!(target instanceof HTMLElement)) return;
    // 이 모달이 열리기 직전 이미 열려 있는 모달 수 (show.bs.modal 시점엔 .show 미부여)
    const openCount = document.querySelectorAll(".modal.show").length;
    const z = resolveStackedModalZ(openCount);
    target.style.zIndex = String(z);
    // backdrop은 show 중 append되므로 다음 프레임에 최신 backdrop을 모달 바로 아래로 보정한다.
    requestAnimationFrame(() => {
      const backdrops = document.querySelectorAll<HTMLElement>(".modal-backdrop");
      const latest = backdrops[backdrops.length - 1];
      if (latest) latest.style.zIndex = String(z - 1);
    });
  });

  /*
   * TinyMCE 다이얼로그(find/replace·code·link 등)는 `.tox-tinymce-aux`로 body 직하에 렌더돼
   * Bootstrap 모달 FocusTrap이 포커스를 모달로 회수해 입력이 막힌다. tox-aux 안으로의 포커스
   * 이동은 capture 단계에서 stopImmediatePropagation 하여 FocusTrap 핸들러를 건너뛴다.
   */
  document.addEventListener("focusin", (e: Event) => {
    const target = e.target;
    if (target instanceof HTMLElement && target.closest(".tox-tinymce-aux")) {
      e.stopImmediatePropagation();
    }
  }, true);
}
