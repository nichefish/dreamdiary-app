/**
 * Layout.ts
 * 레이아웃 스크립트 모듈
 *
 * @author nichefish
 */
document.addEventListener("DOMContentLoaded", function () {
    Layout.init();
});
const Layout = (function () {
    return {
        /**
         * Layout 객체 초기화
         */
        init: function () {
            // 공백 자동 제거
            commons.validate.noSpaces(".no-space");
            // 개별 input 유효성검사
            commons.validate.onlyNum(".number");
            Layout.setBtnDelay();
            Layout.modalBtnCloseSafe();
        },
        /**
         * (안전클릭 제외한) 모든 버튼에 딜레이 기능 추가
         */
        setBtnDelay: function () {
            const buttons = document.querySelectorAll("button:not(.modal-btn-close-safe), .btn:not(.modal-btn-close-safe), .badge:not(.modal-btn-close-safe)");
            buttons.forEach(function (button) {
                button.addEventListener("click", function () {
                    commons.util.delayBtn(this);
                });
            });
        },
        /**
         * 모달 닫기 버튼에 안전장치 적용
         */
        modalBtnCloseSafe: function () {
            // 모든 닫기 버튼을 선택
            const closeButtons = document.querySelectorAll('.modal-btn-close-safe');
            // 각 버튼마다 클릭 이벤트 추가
            closeButtons.forEach(function (button) {
                let isAllowed = false;
                button.removeAttribute('data-bs-dismiss');
                button.addEventListener('click', function (event) {
                    if (isAllowed)
                        return;
                    event.preventDefault();
                    isAllowed = true;
                    button.setAttribute('data-bs-dismiss', 'modal');
                    setTimeout(function () {
                        isAllowed = false;
                        button.removeAttribute('data-bs-dismiss');
                    }, 2000);
                });
            });
        },
        /**
         * 창 닫기
         */
        close: function () {
            window.close();
        }
    };
})();
