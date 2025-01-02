/**
 * validate.ts
 * @namespace: cF.validate
 * @author: nichefish
 * @dependency: jquery.validate.js
 * 공통 - 유효성 검사 관련 함수 모듈
 * (노출식 모듈 패턴 적용 :: cF.validate.noSpaces("#id") 이런식으로 사용)
 */
if (typeof cF === 'undefined') { let cF = {}; }
// 글로벌 정규 표현식 검증 메소드 추가
$(function() {
    $.validator.addMethod("regex", function(value, element, regex) {
        const regExp = new RegExp(regex);
        return this.optional(element) || regExp.test(value);
    });
});
cF.validate = (function() {

    return {
        // "global flag /g works inconsistent if called multiple times..."

        /** jQuery valication 기본 옵션 */
        baseOptions: {
            errorPlacement : function($error, $element) {
                // 공통 함수로 분리
                cF.validate.errorSpan($error, $element);
            },
            success: function(label) {
                // 유효성 검사를 통과한 경우 에러 메시지 제거
                label.remove();
            },
            rules: {
                ".required": {
                    required: true
                }
            },
            ignore: [], // hidden 필드도 검증하기 위함
        },

        basicExtnFilter: "exe|class|jsp|asp|php|sh|bat|war|jar|java|xml|js|css|html|sql",
        validImgExtn: "jpg|jpeg|png",

        /**
         * 입력 요소에서 공백을 자동으로 제거합니다. (onkeyup 이벤트)
         * @param {string|HTMLElement|jQuery} selector - 공백을 제거할 입력 요소의 선택자 또는 DOM 또는 jQuery객체.
         */
        noSpaces: function (selector) {
            const inputs = cF.util.verifySelector(selector);
            if (inputs.length === 0) return;

            inputs.forEach(input => {
                input.addEventListener("keyup", function() {
                    this.value = this.value.replace(/\s+/g, ''); // 모든 공백 제거
                });
            });
        },

        /**
         * 정규식에 해당하는 경우 공백 및 문자를 처리합니다. (onkeyup)
         * @param {string|HTMLElement|jQuery} selector - 처리할 입력 요소의 선택자 또는 DOM 요소 또는 jQuery 객체.
         * @param {RegExp} regex - 제거할 문자에 대한 정규 표현식.
         */
        replaceBlankIfMatches: function (selector, regex) {
            const inputs = cF.util.verifySelector(selector);
            if (inputs.length === 0) return;

            inputs.forEach(input => {
                cF.validate.noSpaces(input); // 공백 처리
                input.addEventListener("keyup", function() {
                    this.value = this.value.replace(regex, ""); // 정규식에 따라 문자 제거
                });
            });
        },

        /**
         * 숫자 제외 공백처리 (onkeyup)
         * @param {string|HTMLElement|jQuery} selector - 숫자만 허용할 입력 요소의 선택자, DOM 요소, 또는 jQuery 객체.
         */
        onlyNum: function (selector) {
            return cF.validate.replaceBlankIfMatches(selector, cF.regex.nonNum);
        },

        /**
         * 날짜 제외 공백처리 (onkeyup)
         * @param {string|HTMLElement|jQuery} selector - 날짜만 허용할 입력 요소의 선택자, DOM 요소, 또는 jQuery 객체.
         */
        onlyDt: function (selector) {
            return cF.validate.replaceBlankIfMatches(selector, cF.regex.nonDt);
        },

        /**
         * 숫자와 점을 제외한 공백처리 (onkeyup)
         * @param {string|HTMLElement|jQuery} selector - 숫자와 점만 허용할 입력 요소의 선택자, DOM 요소, 또는 jQuery 객체.
         */
        onlyNumAndDot: function (selector) {
            return cF.validate.replaceBlankIfMatches(selector, cF.regex.nonNumAndDot);
        },

        /**
         * 숫자와 쉼표를 제외한 공백처리 (onkeyup)
         * @param {string|HTMLElement|jQuery} selector - 숫자와 쉼표만 허용할 입력 요소의 선택자, DOM 요소, 또는 jQuery 객체.
         */
        onlyNumAndComma: function (selector) {
            return cF.validate.replaceBlankIfMatches(selector, cF.regex.nonNumAndComma);
        },

        /**
         * 숫자와 영문자를 제외한 공백처리 (onkeyup)
         * @param {string|HTMLElement|jQuery} selector - 숫자와 영문자가 아닌 문자를 허용할 입력 요소의 선택자, DOM 요소, 또는 jQuery 객체.
         */
        onlyEngNum: function (selector) {
            return cF.validate.replaceBlankIfMatches(selector, cF.regex.nonEngNum);
        },

        /**
         * IPv4 CIDR 형식을 검사하고, 입력 필드에서 유효하지 않은 경우 공백으로 설정합니다.
         * @param {string|HTMLElement|jQuery} selector - CIDR 형식을 검사할 입력 요소의 선택자, DOM 요소, 또는 jQuery 객체.
         */
        checkIpv4Cidr: function (selector) {
            const inputs = cF.util.verifySelector(selector);
            if (inputs.length === 0) return;

            // 숫자, 점, 슬래시를 제외한 문자 제거
            cF.validate.replaceBlankIfMatches(selector, cF.regex.nonNumDotAndSlash);

            inputs.forEach(input => {
                input.addEventListener("blur", function() {
                    const errorSpan = document.getElementById(input.id + "_validate_span");
                    const isValidIp = cF.regex.ipv4.test(input.value);
                    const isValidCidr = cF.regex.ipv4Cidr.test(input.value);
                    const isValid = isValidIp || isValidCidr;

                    input.value = isValid ? input.value : "";
                    if (errorSpan) {
                        errorSpan.textContent = isValid ? "" : "유효하지 않은 IP 주소입니다.";
                        errorSpan.classList.toggle("text-danger", !isValid);
                    }
                });
            });
        },

        /**
         * 입력 요소에서 자동으로 소문자로 변환합니다. (onkeyup 이벤트)
         * @param {string|HTMLElement|jQuery} selector - 소문자로 변환할 입력 요소의 선택자, DOM 요소, 또는 jQuery 객체.
         */
        toLowerCase: function(selector) {
            const inputs = cF.util.verifySelector(selector);
            if (inputs.length === 0) return;

            inputs.forEach(input => {
                input.addEventListener("keyup", function() {
                    this.value = this.value.toLowerCase();
                });
            });
        },

        /**
         * 입력 요소에서 자동으로 대문자로 변환합니다. (onkeyup 이벤트)
         * @param {string|HTMLElement|jQuery} selector - 대문자로 변환할 입력 요소의 선택자, DOM 요소, 또는 jQuery 객체.
         */
        toUpperCase: function(selector) {
            const inputs = cF.util.verifySelector(selector);
            if (inputs.length === 0) return;

            inputs.forEach(input => {
                input.addEventListener("keyup", function() {
                    this.value = this.value.toUpperCase();
                });
            });
        },

        /**
         * 핸드폰 번호 형식 유효성 검사를 수행하고, 유효하지 않으면 입력값을 초기화합니다. (onkeyup 이벤트)
         * @param {string|HTMLElement|jQuery} selector - 핸드폰 번호를 입력할 요소의 선택자, DOM 요소 또는 jQuery 객체.
         */
        cttpc: function(selector) {
            const inputs = cF.util.verifySelector(selector);
            if (inputs.length === 0) return;

            inputs.forEach(function(input) {
                input.addEventListener("keyup", function() {
                    // 모든 공백 제거
                    const value = this.value.trim();
                    this.value = value;

                    // 0으로 시작하지 않으면 무효 처리
                    const startWithZero = /(^0)([0-9]|-)*$/g;
                    if (!startWithZero.test(value)) {
                        this.value = "";
                        return;
                    }

                    // 02 번호와 그 외의 번호 구분
                    // 02는 12자/나머지는 13자보다 길어지면 끝자리 자름, 이후 xxx-xxx(x)-xxxx 형식에 맞게 변환
                    const seoulLocal = /(^02)([0-9]|-)*$/g; // 시작번호 02 vs. 나머지
                    const isSeoulLocal = seoulLocal.test(value);
                    const formattedValue = isSeoulLocal ?
                        value.substr(0, 12).replace(/[^0-9]/g, "").replace(/(^02)([0-9]+)?([0-9]{4})$/, "$1-$2-$3").replace("--", "-") :
                        value.substr(0, 13).replace(/[^0-9]/g, "").replace(/(^[0-9]{3})([0-9]+)?([0-9]{4})$/, "$1-$2-$3").replace("--", "-");

                    this.value = formattedValue.replace("--", "-"); // 포맷팅 적용
                });
            });
        },

        /**
         * jQuery Validation을 사용하여 폼 검증을 수행합니다.
         * @dependency: jquery-validation
         * @param {string} formSelector - 검증할 폼의 선택자.
         * @param {Function} [func] - 제출 시 호출할 함수 (선택적).
         * @param {object} additionalOptions - 추가로 적용할 옵션 (선택적).
         */
        validateForm: function (formSelector, func, additionalOptions = {}) {
            if (!cF.util.isPresent(formSelector)) return;
            // jQuery Validation을 초기화
            const mergedOptions = {
                ...cF.validate.baseOptions,
                ...additionalOptions
            };
            // 제출 핸들러가 있는 경우 추가 설정
            if (typeof func === 'function') mergedOptions.submitHandler = func;
            $(formSelector).validate(mergedOptions); // 검증 초기화
        },

        /**
         * jquery-validation에서 공통으로 사용하는 에러 처리 함수 분리
         * @dependency: jQueryValidation
         * @param: error, element
         */
        errorSpan: function ($error, $element) {
            const errorSpan = document.getElementById($element[0].id + "_validate_span");
            if (!errorSpan) return;
            errorSpan.innerHTML = $error.html();
            errorSpan.classList.add("text-danger");
            errorSpan.focus();
        },

        /**
         * 모든 필수 입력 요소의 에러 메시지를 지웁니다.
         * @param {string} selector - 필수 입력 요소의 선택자.
         */
        clearErrorSpan: function(selector) {
            const requiredElements = document.querySelectorAll(selector || ".required");
            requiredElements.forEach(function(elmt) {
                const errorSpan = document.getElementById(elmt.id + "_validate_span");
                if (errorSpan) errorSpan.innerHTML = '';
            });
        },

        /**
         * 파일 업로드 시 확장자를 체크합니다.
         * @param {HTMLInputElement} selector - 파일 입력 요소.
         * @param {string} validExtn - 허용되는 확장자 목록 (파이프(|)로 구분).
         * @returns {boolean} - 유효한 확장자일 경우 true, 아니면 false.
         */
        fileExtnChck: function(selector, validExtn = "") {
            const inputElements = cF.util.verifySelector(selector);
            if (inputElements.length === 0 || !inputElements[0].files || !inputElements[0].files.length) return false;

            const input = inputElements[0];
            const file = input.files[0];
            const filename = file.name;
            let extn = filename.split('.').pop();
            // 확장가가 없는 경우 처리
            if (extn === filename) extn = "";

            // 기본 확장자 필터
            const basicExtnFiltered = cF.validate.basicExtnFilter.split('|').includes(extn.toLowerCase());
            if (basicExtnFiltered) {
                cF.util.swalOrAlert("파일첨부가 불가능한 파일 형식입니다.");
                input.value = "";
                return false;
            }
            // 'validExtn' 인자가 넘어온 경우:: 해당 확장자만 업로드 가능 (기본 필터는 그대로 막힘)
            const validExtnMatches = validExtn.split('|').includes(extn.toLowerCase());
            const isExtnNotValid = validExtn && validExtn !== "" && !validExtnMatches;
            if (isExtnNotValid) {
                const message = validExtn.replace(/\|/g, ", ");
                cF.util.swalOrAlert(message + " 파일만 첨부 가능합니다.");
                input.value = "";
                return false;
            }
            return true;
        },

        /**
         * 이미지 파일 확장자 체크
         * @param {string|HTMLElement|jQuery} selector - 파일 입력 요소의 선택자, DOM 요소 또는 jQuery 객체.
         * @returns {boolean} - 유효한 이미지 확장자일 경우 true, 아니면 false.
         */
        fileImgExtnChck: function(selector) {
            return cF.validate.fileExtnChck(selector, cF.validate.validImgExtn);
        },

        /**
         * 파일 업로드시 용량 체크 (현재 50MB)
         */
        fileSizeChck: function(selector) {
            const inputElements = cF.util.verifySelector(selector);
            if (inputElements.length === 0 || !inputElements[0].files || !inputElements[0].files.length) return false;

            const input = inputElements[0];
            const fileSizeLimitMb = 50;       // 50MB
            const fileSizeLimit = 1024 * 1024 * fileSizeLimitMb;
            const file = input.files[0];

            if (file.size > fileSizeLimit) {
                cF.util.swalOrAlert(fileSizeLimitMb + "MB 이하의 파일만 첨부가능합니다.");
                input.value = "";
                return false;
            }
            return true;
        }
    }
})();
