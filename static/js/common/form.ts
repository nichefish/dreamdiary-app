/**
 * form.ts
 * 공통 - form 관련 함수 모듈
 *
 * @namespace: cF.form (노출식 모듈 패턴)
 * @author nichefish
 */
// @ts-ignore
if (typeof cF === 'undefined') { var cF = {} as any; }
cF.form = (function(): Module {
    return {
        /**
         * 폼을 초기화합니다.
         * @param {string} formSelectorStr - 초기화할 폼의 선택자.
         */
        reset: function(formSelectorStr: string): void {
            const form: HTMLFormElement|null = document.querySelector(formSelectorStr);
            if (!form) return;

            form?.reset();
        },

        /**
         * 폼을 제출합니다.
         * @param {string} formSelector - 제출할 폼의 선택자.
         * @param {string} actionUrl - 폼 제출 시 사용할 액션 URL.
         * @param {Function} [prefunc] - 폼 제출 전에 실행할 함수 (선택적).
         */
        submit: function(formSelector: string, actionUrl: string, prefunc: Function): void {
            const form: HTMLFormElement|null = document.querySelector(formSelector);
            if (!form) return;

            if (typeof prefunc === 'function') prefunc();
            if (actionUrl) form.action = actionUrl;
            form.submit(); // 폼 제출
        },

        /**
         * blockUI를 적용한 폼 제출.
         * 서버에서 응답 쿠키를 생성할 때까지 blockUI를 유지합니다.
         * @param {string} formSelector - 제출할 폼의 선택자.
         * @param {string} actionUrl - 폼 제출 시 사용할 액션 URL.
         * @param {Function} [prefunc] - 폼 제출 전에 실행할 함수 (선택적).
         * @dependency blockUI (optional)
         */
        blockUISubmit: function(formSelector: string, actionUrl: string, prefunc: Function): void {
            cF.ui.blockUIRequest();
            cF.ui.closeModal();
            cF.form.submit(formSelector, actionUrl, prefunc);
        },
    }
})();