/**
 * Layout.ts
 * 레이아웃 스크립트 모듈
 *
 * @author nichefish
 */
document.addEventListener("DOMContentLoaded", function(): void {
    Layout.init();
});
const Layout: Module = (function(): Module {
    return {
        /**
         * Layout 객체 초기화
         */
        init: function(): void {
            // 공백 자동 제거
            cF.validate.noSpaces(".no-space");
            // 개별 input 유효성검사
            cF.validate.onlyNum(".number");

            Layout.setBtnDelay();
            Layout.modalBtnCloseSafe();
        },

        /**
         * (안전클릭 제외한) 모든 버튼에 딜레이 기능 추가
         */
        setBtnDelay: function(): void {
            const buttons = document.querySelectorAll("button:not(.modal-btn-close-safe), .btn:not(.modal-btn-close-safe), .badge:not(.modal-btn-close-safe)");
            buttons.forEach(function(button) {
                button.addEventListener("click", function(): void {
                    cF.util.delayBtn(this);
                });
            });
        },

        /**
         * 모달 닫기 버튼에 안전장치 적용
         */
        modalBtnCloseSafe: function(): void {
            // 모든 닫기 버튼을 선택
            const closeButtons = document.querySelectorAll('.modal-btn-close-safe');
            // 각 버튼마다 클릭 이벤트 추가
            closeButtons.forEach(function(button): void {
                let isAllowed: boolean = false;
                button.removeAttribute('data-bs-dismiss');
                button.addEventListener('click', function(event: Event): void {
                    event.preventDefault();
                    if (isAllowed) return;

                    isAllowed = true;
                    button.setAttribute('data-bs-dismiss', 'modal');
                    setTimeout(function(): void {
                        isAllowed = false;
                        button.removeAttribute('data-bs-dismiss');
                    }, 2000);
                });
            });
        },

        /**
         * 창 닫기
         */
        close: function(): void {
            window.close();
        },

        /**
         * 내 정보 상세 페이지 이동
         */
        myInfoDtl: function(): void {
            cF.util.blockUIReplace(Url.USER_MY_DTL);
        },

        /**
         * 로그아웃 처리
         */
        lgout: function(): void {
            Swal.fire({
                text: Message.get("view.cnfm.lgout"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                location.replace(Url.AUTH_LGOUT);
            });
        },
    }
})();