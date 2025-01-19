/**
 * ui.ts
 * 공통 - ui/ 관련 함수 모듈
 *
 * @namespace: cF.ui (노출식 모듈 패턴)
 * @author nichefish
 */
// @ts-ignore
if (typeof cF === 'undefined') { var cF = {} as any; }
cF.ui = (function(): Module {
    return {
        /**
         * blockUI wrapped by try-catch
         */
        blockUI: function(): void {
            const blockMessage: string = `<div class="flex-column py-2 bg-dark bg-opacity-25">
                        <span class="spinner-border text-primary" role="status"></span>
                        <span class="text-muted fs-6 fw-semibold mt-5">Loading...</span>
                    </div>`;

            try {
                if (typeof $.blockUI === "function") {
                    $.blockUI({ message: blockMessage });
                } else {
                    console.log("blockUI is not defined.");
                }
            } catch (error) {
                console.log("An error occurred while trying to block the UI.");
            }
        },

        /**
         * unblockUI wrapped by try-catch
         * @param {number} delay - unblockUI를 호출하기 전 대기할 시간(밀리초). (기본 1.5초)
         */
        unblockUI: function(delay: number = 1500): void {
            setTimeout(function(): void {
                try {
                    $.unblockUI();
                } catch(error) {
                    console.log("blockUI is not defined.");
                }
            }, delay);
        },

        /**
         * SweetAlert가 정의되어 있는지 확인합니다.
         * @returns {boolean} - Swal이 정의되어 있으면 true, 그렇지 않으면 false.
         */
        hasSwal: function(): boolean {
            return typeof Swal !== 'undefined';
        },

        /**
         * SweetAlert가 존재하는 경우 Swal을 사용하여 메시지를 표시하고, 그렇지 않으면 alert을 사용합니다.
         * @param {string} msg - 표시할 메시지.
         * @param {Function} [func] - 메시지 처리 후 실행할 콜백 함수 (선택적).
         */
        swalOrAlert: function(msg: string, func: Function): void {
            const hasCallback: boolean = typeof func === 'function';
            if (cF.ui.hasSwal()) {
                // SweetAlert가 존재할 때
                Swal.fire(msg)
                    .then(function(): void {
                        if (hasCallback) func();
                    });
            } else {
                alert(msg);
                if (hasCallback) func();
            }
        },

        /**
         * SweetAlert가 존재하는 경우 Swal을 사용하여 메시지를 표시하고, 그렇지 않으면 confirm을 사용합니다.
         * @param {string} msg - 표시할 메시지.
         * @param {Function} [trueFunc] - 확인 버튼 클릭 시 실행할 함수 (선택적).
         * @param {Function} [falseFunc] - 취소 버튼 클릭 시 실행할 함수 (선택적).
         */
        swalOrConfirm: function(msg: string, trueFunc: Function, falseFunc: Function): void {
            const hasTrueFunc: boolean = typeof trueFunc === 'function';
            const hasFalseFunc: boolean = typeof falseFunc === 'function';
            if (cF.ui.hasSwal()) {
                Swal.fire({
                    text: msg,
                    showCancelButton: true,
                }).then(function(result: SwalResult): void {
                    if (result.value && hasTrueFunc) {
                        trueFunc();
                    } else if (hasFalseFunc) {
                        falseFunc();
                    }
                });
            } else {
                if (confirm(msg) && hasTrueFunc) {
                    trueFunc();
                } else if (hasFalseFunc) {
                    falseFunc();
                }
            }
        },

        /**
         * 요청 중 blockUI를 적용합니다.
         * 서버에서 응답 쿠키를 생성할 때까지 blockUI를 유지합니다.
         * @dependency blockUI (optional)
         */
        blockUIRequest: function(): void {
            cF.ui.blockUI();
            const requestTimer = setInterval(function(): void {
                const token = cF.cookie.get("RESPONSE_SUCCESS");
                if (token === "TRUE") {
                    cF.ui.unblockUI();
                    clearInterval(requestTimer);
                }
            }, 1000);
        },

        /**
         * blockUI를 적용한 페이지 리로드.
         * 서버에서 응답 쿠키를 생성할 때까지 blockUI를 유지합니다.
         * @dependency blockUI (optional)
         */
        blockUIReload: function(): void {
            cF.ui.blockUI();
            cF.ui.closeModal();
            location.reload();
        },

        /**
         * 현재 페이지의 특정 URL 파라미터를 업데이트하고 페이지를 새로고침합니다.
         * @param {string} key - 업데이트할 파라미터의 키.
         * @param {string} value - 지정된 파라미터 키에 설정할 값.
         */
        reloadWithParam: function(key: string, value: string): void {
            // 현재 URL을 가져옵니다.
            const url: URL = new URL(window.location.href);
            // 기존 파라미터를 지우거나 업데이트합니다.
            url.searchParams.set(key, value);
            // 변경된 URL로 리다이렉트합니다.
            window.location.href = url.toString();
        },

        /**
         * blockUI를 적용한 페이지 리플레이스.
         * 서버에서 응답 쿠키를 생성할 때까지 blockUI를 유지합니다.
         * @param {string} url - 리플레이스할 URL.
         * @dependency blockUI (optional)
         */
        blockUIReplace: (function(url: string): void {
            cF.ui.blockUI();
            cF.ui.closeModal();
            location.replace(url);
        }),

        /**
         * 체크박스 클릭 시 라벨 변경 함수
         * @param {string} attrId - 체크박스 요소의 ID.
         * @param {string} ynCn - 예/아니오 문자열. "//"로 예와 아니오를 구분.
         * @param {string} ynColor - 예/아니오 색상. "//"로 예와 아니오 색상을 구분.
         * @param {Function} [yFunc] - 체크박스가 체크되었을 때 호출되는 함수. (선택적)
         * @param {Function} [nFunc] - 체크박스가 체크 해제되었을 때 호출되는 함수. (선택적)
         * @returns {boolean} - 체크박스가 정의되지 않은 경우 false를 반환.
         */
        chckboxLabel: function(attrId: string, ynCn: string, ynColor: string, yFunc: Function, nFunc: Function): void {
            const checkboxElmt: HTMLInputElement = document.getElementById(attrId) as HTMLInputElement;
            if (!checkboxElmt) {
                console.log("체크박스가 정의되지 않았습니다.");
                return;
            }

            const separator: string = "//";
            const [yesStr, noStr] = ynCn.split(separator);
            const [yesColor, noColor] = ynColor.split(separator);
            const labelElmt: HTMLElement = document.getElementById(attrId + "Label") as HTMLElement;
            checkboxElmt.addEventListener("click", function(): void {
                if (checkboxElmt.checked) {
                    labelElmt.textContent = yesStr;
                    labelElmt.style.color = yesColor;
                    if (typeof yFunc === "function") yFunc();
                } else {
                    labelElmt.textContent = noStr;
                    labelElmt.style.color = noColor;
                    if (typeof nFunc === "function") nFunc();
                }
            });
        },

        /**
         * 새 팝업을 엽니다.
         * @param {string} url - 팝업으로 열 URL.
         * @param {string} popupNm - 팝업 창 이름.
         * @param {string} option - 팝업 창 옵션 (예: 'width=600,height=400').
         * @returns {Window|null} - 열린 팝업 창의 Window 객체 또는 null.
         */
        openPopup: function(url: string, popupNm: string, option: string): Window|null {
            const popupWindow: Window = window.open(url, popupNm, option);
            if (!popupWindow) {
                console.error(Message.get("view.error.popup"));     // "팝업 차단 또는 잘못된 URL로 인해 팝업을 열 수 없습니다."
            }
            return popupWindow; // 열린 팝업 창의 Window 객체 반환
        },

        /**
         * button 일시 잠김 (딜레이 적용)
         * @param {HTMLElement|JQuery} elmt - 비활성화할 버튼 요소. jQuery 객체 또는 DOM 요소.
         * @param {number} [sec=2] - 버튼을 비활성화할 시간(초 단위). 기본값은 2초.
         */
        delayBtn: function(elmt: HTMLElement|JQuery, sec: number = 2): void {
            const targetElmt = (elmt instanceof jQuery) ? elmt[0] : elmt;
            if (!targetElmt) return;
            if (targetElmt.classList?.contains("modal-btn-close-safe")) return;     // 안전닫기 버튼 제외

            targetElmt.disabled = true;
            setTimeout(function(): void {
                targetElmt.disabled = false;
            }, sec * 1000);
        },

        /**
         * 전체 (열려 있는) 모달 닫기
         */
        closeModal: function(): void {
            $('.modal-backdrop').remove();
            $(".modal.show").modal("hide");
        },
    }
})();