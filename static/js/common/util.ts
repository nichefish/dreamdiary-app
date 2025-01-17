/**
 * utils.ts
 * 공통 - 일반 유틸리티 함수 모듈
 *
 * @namespace: cF.util (노출식 모듈 패턴)
 * @author: nichefish
 */
// @ts-ignore
if (typeof cF === 'undefined') { var cF = {} as any; }
cF.util = (function(): Module {
    return {

        /**
         * 주어진 데이터가 비어 있는지 확인합니다.
         * null, undefined, 빈 객체, 빈 배열, 공백 문자열을 체크합니다.
         * @param {any} data - 체크할 데이터.
         * @returns {boolean} - 데이터가 비어 있으면 true, 그렇지 않으면 false.
         */
        isEmpty: function(data: any) : boolean {
            if (data == null) return true;

            const type = typeof(data);
            if (type === 'object') {
                return Object.keys(data).length === 0;
            } else if (type === 'string') {
                // 문자열이 공백만 포함하는지 체크
                return data.trim().length === 0;
            }
            return false;
        },

        /**
         * 주어진 데이터가 비어 있지 않은지 확인합니다.
         * null, undefined, 빈 객체, 빈 배열, 공백 문자열을 체크합니다.
         * @param {any} data - 체크할 데이터.
         * @returns {boolean} - 데이터가 비어 있지 않으면 true, 비어 있으면 false.
         */
        isNotEmpty: function(data: any) : boolean {
            return !cF.util.isEmpty(data);
        },

        /**
         * 선택자에서 유효한 입력 요소를 반환합니다.
         * @param {string|HTMLElement|JQuery} selector - 선택자 문자열 또는 DOM 요소 또는 jQuery 객체.
         * @returns {HTMLElement[]} - 유효한 입력 요소 배열 또는 빈 배열.
         */
        verifySelector: function(selector: string|HTMLElement|JQuery): HTMLElement[] {
            if (selector instanceof jQuery) {
                return (selector as JQuery).toArray();
            } else if (selector instanceof HTMLElement) {
                return [selector];
            } else if (typeof selector === 'string') {
                return Array.from(document.querySelectorAll(selector));
            }
            return [];
        },

        /**
         * 선택자 요소의 존재 여부를 체크합니다.
         * @param {string|HTMLElement|JQuery} selector - 선택자 문자열 또는 DOM 요소 또는 jQuery 객체.
         * @returns {boolean} - 요소가 존재하면 true, 그렇지 않으면 false.
         */
        isPresent: function(selector: string|HTMLElement|JQuery): boolean {
            return this.verifySelector(selector).length > 0;
        },

        /**
         * 입력 요소에 Enter 키 처리를 추가합니다.
         * @param {string} selectorStr - 선택자 문자열.
         * @param {Function} func - Enter 키를 눌렀을 때 호출할 함수.
         */
        enterKey: function(selectorStr: string, func: Function): void {
            if (!selectorStr || typeof func !== 'function') return;

            const inputs: NodeListOf<HTMLInputElement> = document.querySelectorAll(selectorStr);
            if (inputs.length === 0) return;

            inputs.forEach((input: HTMLInputElement): void => {
                input.addEventListener("keyup", function(event: KeyboardEvent): void {
                    if (event.key === "Enter") { // Enter 키 확인
                        event.preventDefault(); // 기본 동작 방지
                        func(); // 전달된 함수 호출
                    }
                });
            });
        },

        /**
         * 행 추가 함수에서 reqstItemIdx를 계산하여 반환합니다.
         * @param {string} arrElmt - 요소를 찾기 위한 선택자.
         * @param {string} selectorStr - 추가적인 선택자 문자열.
         * @param {string} arrElmtId - 요소 ID의 접두사.
         * @returns {number} - 계산된 요청 항목 인덱스.
         */
        getReqstItemIdx: function(arrElmt: string, selectorStr: string, arrElmtId: string): number {
            const reqstDataArr: NodeListOf<HTMLElement> = document.querySelectorAll(arrElmt + "[" + selectorStr + "]"); // 선택자에 해당하는 요소 가져오기
            if (reqstDataArr.length === 0) return 0; // 요소가 없으면 0 반환

            // 각 요소의 인덱스를 계산하여 최대 인덱스를 구함
            return Array.from(reqstDataArr).reduce((reqstItemIdx: number, elmt: HTMLElement): number => {
                const isExcluded: boolean = elmt.id.includes("__");
                if (isExcluded) return reqstItemIdx;

                const currentIdx: number = Number(elmt.id.replace(arrElmtId, ""));
                return Math.max(reqstItemIdx, currentIdx + 1);
            }, 0); // 초기값은 0
        },

        /**
         * 행 추가 함수에서 해당 input의 값(숫자) 총합을 구하여 반환합니다.
         * ".excludeSum" 클래스를 가진 요소는 제외됩니다.
         * @param {string} selectorStr - 선택자 문자열.
         * @returns {number} - 총합 값.
         */
        getReqstItemTotSum: function(selectorStr: string): number {
            const reqstDataArr: NodeListOf<HTMLInputElement> = document.querySelectorAll("input[" + selectorStr + "]");
            if (reqstDataArr.length === 0) return 0;

            return Array.from(reqstDataArr).reduce((total: number, elmt: HTMLInputElement): number => {
                const isExcluded: boolean = elmt.classList.contains("excludeSum") || elmt.id.includes("{");
                const value: string = elmt.value.replace(/,/g, ""); // 쉼표 제거
                const numberValue: number = Number(value); // 숫자로 변환

                return isExcluded ? total : total + (isNaN(numberValue) ? 0 : numberValue); // 제외되지 않은 경우에만 총합에 추가
            }, 0); // 초기값은 0
        },

        /**
         * 파일 다운로드를 수행합니다.
         * AJAX로 파일 존재 여부를 체크한 후, 임시 폼을 생성하여 제출합니다.
         * @param {string|number} atchFileNo - 첨부 파일 번호.
         * @param {string|number} atchFileDtlNo - 첨부 파일 상세 번호.
         * TODO: URL 외부에서 주입하기?
         */
        fileDownload: function(atchFileNo: string|number, atchFileDtlNo: string|number): void {
            const inputs: string = "<input type='hidden' name='atchFileNo' value='" + atchFileNo + "'>" +
                           "<input type='hidden' name='atchFileDtlNo' value='" + atchFileDtlNo + "'>";
            const form: HTMLFormElement = document.createElement("form");
            form.action = "/file/fileDownload.do";
            form.method = "POST"; // POST 방식으로 설정
            form.innerHTML = inputs;
            document.body.appendChild(form);    // 폼 추가
            form.submit();
            document.body.removeChild(form);    // 폼 제거
        },

        /**
         * 파일 다운로드 시 blockUI를 적용합니다.
         * 서버에서 응답 쿠키를 생성할 때까지 blockUI를 유지합니다.
         * @dependency blockUI (optional)
         */
        blockUIFileDownload: (function(): void {
            cF.ui.blockUI();
            const downloadTimer = setInterval(function(): void {
                const token = cF.cookie.get("FILE_CREATE_SUCCESS");
                if (token === "TRUE") {
                    cF.ui.unblockUI();
                    clearInterval(downloadTimer);
                }
            }, 1000);
        }),

        /**
         * Form 데이터 배열을 chunk 크기만큼 나누어 JSON 객체 배열로 변환
         * @param {Array<any>} arr - 시리얼라이즈할 form 데이터 배열. 각 요소는 `{ name, value }` 형태의 객체.
         * @param {number} chunk - 배열을 분할할 크기.
         * @returns {Array} - 각 chunk별로 분할된 객체 배열.
         */
        serializeJsonArray: function(arr: Array<any>, chunk: number): Record<any, any> {
            const processedArr = [];
            for (let i = 0; i < arr.length; i += chunk) {
                const form = arr.slice(i, i + chunk);
                const obj = form.reduce((acc, field) => {
                    if (cF.util.isEmpty(field)) return acc;
                    acc[field.name] = field.value;
                    return acc;
                }, {});
                processedArr.push(obj);
            }
            return processedArr;
        },

        /**
         * 상대경로로부터 절대경로를 반환한다.
         * @param {string} url 상대경로
         * @return {string} 절대경로
         */
        getAbsoluteUrl: function(url: string): string {
            if (url.startsWith('http://') || url.startsWith('https://')) {
                return url;  // 이미 절대 경로라면 그대로 반환
            } else {
                const baseUrl = window.location.origin;  // 현재 웹사이트의 절대 URL
                return baseUrl + url;  // 상대 경로를 절대 경로로 결합
            }
        },

        /**
         * FormData로부터 JSON 객체를 생성한다.
         * @param {string} formSelector
         * @return {Record<string, any>}
         */
        getJsonFormData: function(formSelector: string): Record<string, any> {
            const form: HTMLFormElement = document.querySelector(formSelector) as HTMLFormElement;
            const formData: FormData = new FormData(form);
            return Object.fromEntries(formData);
        }
    }
})();
