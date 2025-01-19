/**
 * format.ts
 * 포맷팅 관련 함수 모듈
 *
 * @namespace: cF.format (노출식 모듈 패턴)
 * @author nichefish
 */
// @ts-ignore
if (typeof cF === 'undefined') { var cF = {} as any; }
cF.format = (function(): Module {
    return {
        /**
         * 자간 처리 (글자가 영역 전체에 고르게 퍼지도록 처리)
         * @param {string} str - 자간을 적용할 문자열.
         * @returns {string} - 각 글자를 `<span>` 태그로 감싼 HTML 문자열. 별도의 CSS 처리가 필요함.
         */
        letterSpacing: function(str: string): string {
            if (cF.util.isEmpty(str)) return "";

            // 각 글자를 span 태그로 감싸고 문자열로 결합
            return Array.from(str)
                .map((spell: string) => "<span>" + spell + "</span>")
                .join("");
        },

        /**
         * 숫자(정수)에 천 단위로 콤마(,)를 추가.
         * 숫자가 넘어온 경우 포맷팅된 문자열 반환. selector로 넘어온 경우에는 이벤트 리스너를 추가함.
         * @param {number|string} value (숫자 또는 selectorStr)
         * @param {number} unit (나눔단위 ex.천단위)
         * @returns {string|void} - 콤마가 추가된 숫자 문자열.
         */
        thousandSeparator: function(value: number|string, unit: number = 1): string|void {
            if (cF.util.isEmpty(value)) return "";

            // 숫자값이 넘어오면 걍 콤마 붙인 결과값을(string) 넘겨버린다.
            const numValue: number = (typeof value === 'string') ? parseFloat(value.replace(/,/g, "")) : value;
            if (!isNaN(numValue)) {
                const divided: number = numValue / unit;
                return divided.toLocaleString();
            }

            // 나머지 경우에는 selector로 간주, 입력 필드에 이벤트 리스너 추가
            const inputs: NodeListOf<HTMLInputElement> = document.querySelectorAll(value as string);
            if (inputs.length === 0) return;

            inputs.forEach((elmt: HTMLInputElement): void => {
                elmt.value = cF.format.thousandSeparator(elmt.value, unit);
                elmt.addEventListener("keyup", function(): void {
                    let localeStr = cF.format.thousandSeparator(elmt.value, unit);

                    // maxlength를 초과하는 경우 처리
                    const maxlength: string|null = elmt.getAttribute("maxlength");
                    if (cF.util.isEmpty(maxlength)) localeStr = localeStr.substring(0, maxlength);

                    elmt.value = localeStr;
                });
            });
        },

        /**
         * 숫자에 콤마(,) 빼기
         * @param {number|string} value (숫자 또는 selectorStr)
         * @param {number} unit (나눔단위 ex.천단위)
         */
        removeComma: function (value: number|string, unit: number = 1): string|void {
            if (cF.util.isEmpty(value)) return "";

            // 숫자값이 넘어오면 걍 콤마 붙인 결과값을(string) 넘겨버린다.
            const numValue: number = (typeof value === 'string') ? parseFloat(value.replace(/,/g, "")) : value;
            if (!isNaN(numValue)) {
                const divided: number = numValue / unit;
                return divided.toLocaleString();
            }

            // 나머지 경우에는 selector로 간주, 콤마 제거 처리 및 keyup시 콤마 제거 처리한다.
            const inputs: NodeListOf<HTMLInputElement> = document.querySelectorAll(value as string);
            if (inputs.length === 0) return;

            inputs.forEach((elmt: HTMLInputElement): void => {
                elmt.value = cF.format.removeComma(elmt.value, unit);
                elmt.addEventListener("keyup", function (): void {
                    elmt.value = cF.format.removeComma(elmt.value, unit);
                });
            });
        },

        /**
         * 숫자에 소숫점 처리
         * @param {number|string} value (숫자 또는 selectorStr)
         * @param {number} fixed (자릿수)
         * @param {string|void} unit
         */
        addDot: function(value: number|string, fixed: number = 0, unit: number = 0): string|void {
            if (cF.util.isEmpty(value)) return "";

            // 숫자값이 넘어오면 걍 콤마 빼서 넘겨버린다.
            const numValue: number = (typeof value === 'string') ? parseFloat(value.replace(/,/g, "")) : value;
            if (!isNaN(numValue)) {
                return numValue.toLocaleString(undefined, {
                    minimumFractionDigits: fixed,
                    maximumFractionDigits: fixed
                });
            }
            // 나머지 경우에는 selector로 간주, 콤마 제거 처리 및 keyup시 콤마 제거 처리한다.
            const inputs: NodeListOf<HTMLInputElement> = document.querySelectorAll(value as string);
            if (inputs.length === 0) return;

            inputs.forEach((elmt: HTMLInputElement): void => {
                elmt.value = cF.format.addDot(elmt.value, fixed, unit);
                elmt.addEventListener("keyup", function(): void {
                    elmt.value = cF.format.addDot(elmt.value, fixed, unit);
                });
            });
        },

        /**
         * 주어진 선택자 문자열에 해당하는 input 요소의 값을 숫자로 변환하여 반환합니다.
         * @param {string} selector - 변환할 input 요소의 선택자 문자열.
         * @returns {number|null} - 변환된 숫자 값 또는 유효하지 않은 경우 `null`.
         */
        toNumber: function(selector: string|HTMLElement|JQuery): number|null {
            const inputs: HTMLElement[] = cF.util.verifySelector(selector);
            if (inputs.length === 0) return null;

            // input 값이 없거나 빈 문자열이면 null 반환
            const input: HTMLInputElement = inputs[0] as HTMLInputElement;
            const inputValue: string = input.value.trim();
            if (inputValue === "") return null;

            // 쉼표를 제거하고 숫자로 변환
            const numValue: number = Number(inputValue.replace(/,/g, ""));
            return !isNaN(numValue) ? numValue : null;
        },

        /**
         * CamelCase를 SnakeCase로 변환
         * @param str
         * @return {string}
         */
        toSnakeCase: function(str: string): string {
            if (cF.util.isEmpty(str)) return "";

            return str
                .replace(/([a-z])([A-Z])/g, '$1_$2')
                .toLowerCase(); // 모두 소문자로 변환
        },

        /**
         * HTML 엔티티를 다시 원래 문자로 변환
         * @param {string} escaped
         * @return {string}
         */
        unescapeHtml: function(escaped: string): string {
            const doc: Document = new DOMParser().parseFromString(escaped, 'text/html');
            return doc.body.textContent || "";
        },

        /**
         * 문자열의 첫 글자를 대문자로 변환합니다.
         * @param {string} str - 변환할 문자열.
         * @returns {string} - 첫 글자가 대문자로 변환된 문자열.
         */
        upperFirst: function(str: string): string {
            if (cF.util.isEmpty(str)) return str;

            // 첫 글자 대문자로 변환 후 나머지 문자열 결합
            return str.charAt(0).toUpperCase() + str.slice(1);
        },
    }
})();