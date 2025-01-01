/**
 * commons-helpers.js
 * @namespace: commons.date
 * @author: nichefish
 * @since: 2022-06-27
 * @dependency: commons.js
 * 공통 - Handlebars(템플릿) 헬퍼 관련 함수 모듈
 * (노출식 모듈 패턴 적용 :: commons.date.getCurrDateStr(commons.date.ptnDate) 이런식으로 사용)
 */
(function (Handlebars) {
    if (!Handlebars) {
        return;
    }
    /**
     * exists : 값 존재 여부 체크.
     * @param {any} value - 체크할 값.
     * @returns {boolean} - `value`가 비어 있지 않으면 `true`, 비어 있으면 `false`.
     */
    Handlebars.registerHelper("exists", function (value) {
        return !commons.util.isEmpty(value);
    });
    /**
     * notExists : 값 미존재 여부 체크.
     * @param {any} value - 체크할 값.
     * @returns {boolean} - `value`가 비어 있으면 `true`, 비어 있지 않으면 `false`.
     */
    Handlebars.registerHelper("notExists", function (value) {
        return commons.util.isEmpty(value);
    });
    /**
     * ifEmpty : 값 미존재시 기본값 반환.
     * @param {any} value - 체크할 값.
     * @param {any} alt - `value`가 비어 있을 때 반환할 기본값.
     * @returns {any} - `value`가 비어 있지 않으면 `value`, 비어 있으면 `alt`.
     */
    Handlebars.registerHelper("ifEmpty", function (value, alt) {
        return commons.util.isEmpty(value) ? alt : value;
    });
    /**
     * selectedIf : 조건에 따라 <select> 선택.
     * @param {boolean} value - 선택 여부를 결정할 값.
     * @returns {string} - "selected" 또는 빈 문자열.
     */
    Handlebars.registerHelper("selectedIf", function (value) {
        return value ? "selected" : "";
    });
    /**
     * checkedYn : 값이 "Y"일 경우 체크박스 체크.
     * @param {string} value - 체크 여부를 결정할 값.
     * @returns {string} - 값이 "Y"이면 "checked", 그렇지 않으면 빈 문자열.
     */
    Handlebars.registerHelper("checkedYn", function (value) {
        return "Y" === value ? "checked" : "";
    });
    /**
     * checkedIf : 값이 true일 경우 체크박스 체크.
     * @param {any} value - 체크 여부를 결정할 값.
     * @returns {string} - 값이 true면 "checked", 그렇지 않으면 빈 문자열.
     */
    Handlebars.registerHelper("checkedIf", function (value) {
        return value ? "checked" : "";
    });
    /**
     * numberFormat : 숫자(정수)에 천 단위로 콤마(,)를 추가.
     * @param {number|string} value - 포맷팅할 숫자 값.
     * @returns {string} - 천 단위로 콤마가 추가된 문자열. 값이 없으면 `undefined`.
     */
    Handlebars.registerHelper("numberFormat", function (value) {
        if (commons.util.isEmpty(value))
            return;
        return commons.util.thousandSeparator(value);
    });
    /**
     * ifYn : 값이 "Y"인 경우에 특정 템플릿 내용을 렌더링합니다.
     * @param {string} value - 조건부로 체크할 값.
     * @param {object} options - Handlebars 옵션 객체로, `fn`과 `inverse`를 포함.
     * @returns {string} - `value`가 "Y"일 경우 `options.fn(this)`, 그렇지 않으면 `options.inverse(this)`.
     */
    Handlebars.registerHelper("ifYn", function (value, options) {
        const trimmedValue = value && value.trim();
        return trimmedValue === "Y" ? options.fn(this) : options.inverse(this);
        ;
    });
    /**
     * trueFalse : 주어진 `value`를 "Y" 값과 비교하여 `true` 또는 `false`를 반환합니다.
     * @param {string} value - 체크할 값.
     * @param {string} ynValues - "Y"와 "N" 값을 구분하는 문자열.
     * @param {object} options - Handlebars 옵션 객체.
     * @returns {boolean} - `value`가 `ynValues`의 "Y" 값과 일치하면 `true`, 그렇지 않으면 `false`.
     */
    const truefalseFunc = function (value, ynValues, options) {
        // 기본값 null일 때 true로 간주하는 옵션 : 기본값 옵션이 true이고 값이 비어있으면 true 반환
        const defaultTrue = options.hash["default"] || false;
        if (defaultTrue && commons.util.isEmpty(value))
            return true;
        const separator = "//";
        const [yValue] = ynValues.split(separator);
        return (value === yValue);
    };
    Handlebars.registerHelper("trueFalse", truefalseFunc);
    /**
     * equals : 두 값을 비교하여 일치 여부를 반환합니다.
     * @param {any} value - 비교할 첫 번째 값.
     * @param {any} compareValue - 비교할 두 번째 값.
     * @param {object} options - Handlebars 옵션 객체.
     * @returns {boolean} - 두 값이 일치하면 `true`, 그렇지 않으면 `false`.
     */
    Handlebars.registerHelper("equals", function (value, compareValue, options) {
        // 기본값 null일 때 true로 간주하는 옵션 : 기본값 옵션이 true이고 값이 비어있으면 true 반환
        const defaultTrue = options.hash["default"] || false;
        if (defaultTrue && commons.util.isEmpty(value))
            return true;
        // 비교결과 반환 :: 일부러 느슨한 비교
        return (value == compareValue);
    });
    /**
     * notEquals : 두 값을 비교하여 불일치 여부를 반환합니다.
     * @param {any} value - 비교할 첫 번째 값.
     * @param {any} compareValue - 비교할 두 번째 값.
     * @param {object} options - Handlebars 옵션 객체.
     * @returns {boolean} - 두 값이 일치하면 `true`, 그렇지 않으면 `false`.
     */
    Handlebars.registerHelper("notEquals", function (value, compareValue, options) {
        return !equalsFunc(value, compareValue, options);
    });
    /**
     * checkedLabel : 주어진 `value`가 `true`이면 "Y" 레이블을, 그렇지 않으면 "N" 레이블을 반환합니다.
     * @param {any} value - 체크할 값.
     * @param {string} ynLabels - "Y"와 "N" 레이블을 구분하는 문자열 (구분자는 "//").
     * @returns {string} - `value`가 `true`이면 "Y" 레이블, 그렇지 않으면 "N" 레이블을 반환.
     */
    Handlebars.registerHelper("checkedLabel", function (value, ynLabels) {
        // `ynLabels`를 구분자로 나누어 "Y"와 "N" 레이블 추출
        const separator = "//";
        const [yLabel, nLabel] = ynLabels.split(separator);
        return value ? yLabel : nLabel;
    });
    /**
     * checkedStyle : `value`에 따라 스타일 속성을 반환하는 헬퍼.
     * @param {any} value - 체크할 값.
     * @param {string} type - 적용할 스타일의 타입 (예: "color", "background-color").
     * @param {string} ynColors - "Y"와 "N"의 색상을 구분하는 문자열 (구분자는 "//").
     * @returns {string} - `style` 속성을 반환 (예: `style="color:red;"`).
     */
    const checkedStyleFunc = function (value, type, ynColors) {
        // `ynColors`를 구분자로 나누어 "Y"와 "N"의 색상 추출
        const separator = "//";
        const [yColor, nColor] = ynColors.split(separator);
        return "style=" + type + ":" + (value ? yColor : nColor) + ";";
    };
    Handlebars.registerHelper("checkedStyle", checkedStyleFunc);
    /**
     * checkedYnStyle : `value`가 `"Y"`인 경우에 스타일 속성을 반환하는 헬퍼.
     * @param {string} value - 체크할 값. "Y"와 비교됩니다.
     * @param {string} type - 적용할 스타일의 타입 (예: "color", "background-color").
     * @param {string} ynColors - "Y"와 "N"의 색상을 구분하는 문자열 (구분자는 "//").
     * @returns {string} - `style` 속성을 반환 (예: `style="color:red;"`).
     */
    Handlebars.registerHelper("checkedYnStyle", function (value, type, ynColors) {
        // `value`가 "Y"인 경우에만 true로 간주하여 스타일 반환
        return checkedStyleFunc(value === "Y", type, ynColors);
    });
    /**
     * concat : 두 값을 문자열로 결합하는 헬퍼.
     * @param {any} value1 - 첫 번째 값.
     * @param {any} value2 - 두 번째 값.
     * @returns {string} - `value1`과 `value2`를 결합한 문자열.
     */
    Handlebars.registerHelper("concat", function (value1, value2) {
        return String(value1) + String(value2);
    });
    /**
     * dateformat : 주어진 날짜 값을 지정된 패턴으로 포맷팅하는 헬퍼.
     * @param {Date|string|number} value - 포맷팅할 날짜 값 (Date 객체, 문자열, 또는 타임스탬프).
     * @param {string} pattern - 날짜를 포맷팅할 패턴.
     * @returns {string} - 포맷팅된 날짜 문자열. 값이 없으면 빈 문자열을 반환.
     */
    Handlebars.registerHelper("dateformat", function (value, pattern) {
        if (!value)
            return "";
        try {
            return commons.date.asStr(value, pattern);
        }
        catch (e) {
            console.error("Date formatting error:", e);
            return "";
        }
    });
    /**
     * add : 두 수를 더하는 헬퍼.
     * @param {number|string} a - 첫 번째 값.
     * @param {number|string} b - 두 번째 값.
     * @returns {number} - `a`와 `b`의 합.
     */
    Handlebars.registerHelper('add', function (a, b) {
        return a + b;
    });
    /**
     * sub : 두 수를 빼는 헬퍼.
     * @param {number|string} a - 첫 번째 값.
     * @param {number|string} b - 두 번째 값.
     * @returns {number} - `a`와 `b`의 차.
     */
    Handlebars.registerHelper('sub', function (a, b) {
        return a - b;
    });
    /**
     * lt : `a`가 `b`보다 작은지 비교하는 헬퍼.
     * @param {number|string} a - 비교할 첫 번째 값.
     * @param {number|string} b - 비교할 두 번째 값.
     * @returns {boolean} - `a`가 `b`보다 작으면 `true`, 그렇지 않으면 `false`.
     */
    Handlebars.registerHelper('lt', function (a, b) {
        return a < b;
    });
    /**
     * le : `a`가 `b`보다 작거나 같은지 비교하는 헬퍼.
     * @param {number|string} a - 비교할 첫 번째 값.
     * @param {number|string} b - 비교할 두 번째 값.
     * @returns {boolean} - `a`가 `b`보다 작거나 같으면 `true`, 그렇지 않으면 `false`.
     */
    Handlebars.registerHelper('le', function (a, b) {
        return a <= b;
    });
    /**
     * gt : `a`가 `b`보다 큰지 비교하는 헬퍼.
     * @param {number|string} a - 비교할 첫 번째 값.
     * @param {number|string} b - 비교할 두 번째 값.
     * @returns {boolean} - `a`가 `b`보다 크면 `true`, 그렇지 않으면 `false`.
     */
    Handlebars.registerHelper('gt', function (a, b) {
        return a > b;
    });
    /**
     * ge : `a`가 `b`보다 크거나 같은지 비교하는 헬퍼.
     * @param {number|string} a - 비교할 첫 번째 값.
     * @param {number|string} b - 비교할 두 번째 값.
     * @returns {boolean} - `a`가 `b`보다 크거나 같으면 `true`, 그렇지 않으면 `false`.
     */
    Handlebars.registerHelper('ge', function (a, b) {
        return a >= b;
    });
    /**
     * Handlebars 헬퍼 함수 'stringify'를 등록합니다.
     * 주어진 값이 객체일 경우 JSON 문자열로 변환하고, 그렇지 않으면 문자열로 변환합니다.
     *
     * @param {any} value - 변환할 값.
     * @returns {string} - 객체일 경우 JSON 문자열, 그렇지 않으면 문자열. 값이 없으면 'null' 문자열.
     */
    Handlebars.registerHelper('stringify', function (value) {
        if (value && typeof value === 'object') {
            return JSON.stringify(value); // JSON 형식으로 출력
        }
        return value ? value.toString() : 'null';
    });
})(Handlebars);
