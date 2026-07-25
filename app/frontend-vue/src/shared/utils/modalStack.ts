/**
 * modalStack.ts
 * 중첩 Bootstrap 모달의 z-index 스태킹 보정.
 * <p>
 * App.vue 전역 스타일이 `body.modal-open .modal`을 z-index 6100(채팅 드로어 6002 위)으로 평탄화하는데,
 * 두 모달이 같은 6100이면 DOM 순서로 스태킹이 결정돼 나중 마운트 모달(예: 스레드 상세)이 위에 그려지고
 * 그 안에서 연 자식 모달(수정·댓글·이력·관련글)이 뒤에 깔린다. 이 핸들러는 모달이 열릴 때마다 이미 열린
 * 모달 수에 따라 z-index를 올려(6100 + n*STEP) 자식이 항상 부모 위로 오게 한다.
 * <p>
 * STEP은 작게(2) 두어 스택 모달을 SweetAlert(6110)보다 낮게 유지한다 — 자식 모달 위에서 뜨는 확인창이
 * 최상단을 지키게 하기 위함이다(중첩 깊이는 실사용 2~3단).
 */

/** App.vue `body.modal-open .modal` 기준 z-index. */
const MODAL_BASE_Z = 6100;
/** 스택 한 단계당 증가폭. SweetAlert(6110)보다 낮게 유지하기 위해 작게 둔다. */
const MODAL_STEP = 2;

let installed = false;

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
    const z = MODAL_BASE_Z + openCount * MODAL_STEP;
    target.style.zIndex = String(z);
    // backdrop은 show 중 append되므로 다음 프레임에 최신 backdrop을 모달 바로 아래로 보정한다.
    requestAnimationFrame(() => {
      const backdrops = document.querySelectorAll<HTMLElement>(".modal-backdrop");
      const latest = backdrops[backdrops.length - 1];
      if (latest) latest.style.zIndex = String(z - 1);
    });
  });
}
