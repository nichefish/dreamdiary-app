/**
 * overlayZIndex.ts
 * 모달·SweetAlert·채팅 드로어·TinyMCE 보조 UI 등 전역 오버레이 z-index SSOT.
 * <p>
 * AppChat 드로어(6002)보다 모달(6100+)이 위에 와야 하고,
 * 모달 안 TinyMCE code/link 등 다이얼로그는 모달·Tagify(6120)보다 위에,
 * SweetAlert 확인창은 그 모든 것보다 항상 위에 와야 한다.
 * App.vue CSS의 숫자와 이 상수를 함께 갱신한다.
 */

/** AppChat drawer (vendors-override/bootstrap.scss) 위. App.vue `body.modal-open .modal` 기준. */
export const MODAL_BASE_Z = 6100;

/** 중첩 모달 한 단계당 증가폭. */
export const MODAL_STEP = 2;

/**
 * SweetAlert2 컨테이너 z-index.
 * 변경 전: 6110 — 중첩 모달 스택(6100 + n*2)과 여유가 10뿐이라
 * 스레드 상세 위 자식 모달에서 확인창이 모달에 가려질 수 있었다.
 * 변경 후: 6200 으로 올려 스택과 확실히 분리한다.
 */
export const SWAL_Z = 6200;

/** 모달 스택 상한. TinyMCE·SweetAlert보다 항상 아래. */
export const MODAL_MAX_Z = SWAL_Z - 20;

/**
 * TinyMCE 보조 UI(`.tox-tinymce-aux`: code/source·link·table 다이얼로그·메뉴) z-index.
 * body 에 append 되며 기본값이 ~1300 이라, 모달을 6100으로 올린 뒤 모달에 가려졌다.
 * Tagify dropdown(6120)·모달 스택 상한(6180)보다 위, SweetAlert(6200)보다 아래.
 */
export const TINYMCE_AUX_Z = SWAL_Z - 10;
