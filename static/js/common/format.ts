/**
 * format.ts
 *
 * @author nichefish
 */
if (typeof cF === 'undefined') { var cF = {} as any; }
cF.format = (function(): Module {
    return {
        init: function(): void {
            console.log("'cF.format' module initialized.");
        },

        /**
         * 자간 처리 (글자가 영역 전체에 고르게 퍼지도록 처리)
         * @param {string} str - 자간을 적용할 문자열.
         * @returns {string} - 각 글자를 `<span>` 태그로 감싼 HTML 문자열. 별도의 CSS 처리가 필요함.
         */
        letterSpacing: function(str: string): string {
            if (cF.util.isEmpty(str)) return "";

            // 각 글자를 span 태그로 감싸고 문자열로 결합
            return Array.from(str)
                .map(spell => "<span>" + spell + "</span>")
                .join("");
        },

        /**
         * 숫자(정수)에 천 단위로 콤마(,)를 추가.
         * 숫자가 넘어온 경우 포맷팅된 문자열 반환. selector로 넘어온 경우에는 이벤트 리스너를 추가함.
         * @param value (숫자 또는 selectorStr)
         * @param unit (나눔단위 ex.1,400만)
         * @returns {string|void} - 콤마가 추가된 숫자 문자열.
         */
        thousandSeparator: function(value: number|string, unit = 1): string|void {
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

            inputs.forEach(elmt => {
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
         * @param value (숫자 또는 selectorStr)
         * @param unit (나눔단위 ex.천단위)
         */
        removeComma: function (value: number|string, unit = 1): string|void {
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

            inputs.forEach(elmt => {
                elmt.value = cF.format.removeComma(elmt.value, unit);
                elmt.addEventListener("keyup", function (): void {
                    elmt.value = cF.format.removeComma(elmt.value, unit);
                });
            });
        },

        /**
         * 숫자에 소숫점 처리
         * @param value (숫자 또는 selectorStr)
         * @param fixed (자릿수)
         * @param unit
         */
        addDot: function(value, fixed = 0, unit = 0) {
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

            inputs.forEach(elmt => {
                elmt.value = cF.format.addDot(elmt.value, fixed, unit);
                elmt.addEventListener("keyup", function(): void {
                    elmt.value = cF.format.addDot(elmt.value, fixed, unit);
                });
            });
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
        }
    }
})();