/**
 * modal_history.ts
 *
 * @author nichefish
 */
const ModalHistory: Page = (function(): Page {
    return {
        stack: [],

        /**
         * 모달 히스토리 초기화
         */
        init: function(): void {
            ModalHistory.reset();
        },

        /**
         * 현재 모달 정보를 stack에 추가합니다.
         * @param {Module} module
         * @param {string} methodNm
         * @param {Array} args
         */
        push: function(module: Module, methodNm: string, args: any[]): void {
            const currentModal = {
                module: module,       // 호출한 모듈 객체
                methodNm: methodNm,     // 호출한 메소드명
                args: args                  // 메소드에 전달된 인자들
            };

            ModalHistory.stack.push(currentModal);  // modalHistory에 모달 정보 추가
        },

        /**
         * 가장 최근에 열린 모달을 stack에서 제거합니다.
         *
         * @returns {Object} 스택에서 제거된 모달 정보
         */
        pop: function(): any {
            return ModalHistory.stack.pop();
        },

        /**
         * 현재 열려 있는 가장 최근 모달을 확인합니다.
         *
         * @returns {Object} 가장 최근에 열린 모달 정보
         */
        peek: function(): any {
            const maxIdx: number = ModalHistory.stack.length - 1;
            return ModalHistory.stack[maxIdx];
        },

        /**
         * 현재 모달을 닫고 이전 모달로 돌아갑니다.
         */
        previous: function (): void {
            // 현재 모달을 먼저 pop()
            const poppedModal = this.pop();
            if (!poppedModal) {
                console.log("No modal to pop.");
                return;
            }

            // 스택에 이전 모달이 있는지 확인
            const prevModal = this.peek();
            if (!prevModal) {
                console.log("No previous modal to return to.");
                return;
            }

            console.log("Returning to previous modal:", prevModal.methodNm);

            // 이전 모달을 여는 로직
            if (prevModal.module && typeof prevModal.module[prevModal.methodNm] === 'function') {
                prevModal.module[prevModal.methodNm](...prevModal.args);
            } else {
                console.log("Failed to return to previous modal: Method not found.");
            }
        },

        /**
         * 모달 히스토리 스택을 초기화합니다.
         */
        reset: function(): void {
            ModalHistory.stack = [];
            console.log("Modal history reset."); // 히스토리 초기화 메시지
        }
    }
})();