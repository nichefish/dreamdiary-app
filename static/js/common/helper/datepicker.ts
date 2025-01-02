/**
 * datepicker.ts
 * @namespace: cF.datepicker
 * @author: nichefish
 * @dependency: daterangepicker.js, commons-validate.js
 * 공통 - datepicker(라이브러리) 관련 함수 모듈
 * (노출식 모듈 패턴 적용 :: util.datepicker.singleDatePicker() 요런식으로 사용)
 */
if (typeof cF === 'undefined') { let cF = {}; }
cF.datepicker = (function() {

    /** 기본 날짜 옵션 */
    const baseOptions = {
        singleDatePicker: true,
        startDate: moment().startOf("Day"),
        showDropdowns: true,
        autoUpdateInput: false,
        locale: {
            format: cF.date.ptnDate,
                daysOfWeek: ["일", "월", "화", "수", "목", "금", "토"],
                monthNames: ["1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월"]
        }
    };

    /**
     * 시간 포함 날짜 옵션
     */
    const timeOptions = {
        ...baseOptions,
        timePicker: true,
        timePicker24Hour: true,
        timePickerSeconds: true,
        locale: {
            ...baseOptions.locale,
            format: cF.date.ptnDatetime
        }
    };

    return {
        /**
         * 기본 공통 로직 : jQuery 요소에 datepicker를 초기화하고 날짜 변경 시 콜백을 실행합니다.
         * @param {string} selector - 초기화할 요소의 선택자 문자열.
         * @param {object} options - `daterangepicker`에 적용할 옵션.
         * @param {function} func - 날짜 선택 시 실행할 콜백 함수 (선택적).
         */
        datepicker: function(selector, options: object, func: Function) {
            if (cF.util.isEmpty(selector)) return;

            const $elmt = $(selector);
            const id = $elmt.attr("id");

            // 초기 값 설정
            $elmt.val(options.startDate);

            // datepicker 초기화
            $elmt.daterangepicker(options, function(start) {
                // 날짜 포맷 설정
                $elmt.val(start.format(options.locale.format));
                // 에러 메세지 제거
                const $errorSpan = $("#"+id+"_validate_span");
                if ($errorSpan.length) $errorSpan.empty();
                // 콜백 함수 실행
                if (typeof func === 'function') func(start);
            });
            // 날짜 유효성 검증
            cF.validate.onlyDt($elmt);
        },

        /**
         * dateRangePicker init : 단일 날짜 선택기를 초기화합니다.
         * @param {string} selector - 초기화할 요소의 선택자 문자열.
         * @param {string} format - 날짜 형식 (선택적).
         * @param {string|Date} initDt - 초기 날짜 값 (선택적, 'today'가 아닌 경우 설정).
         * @param {function} func - 날짜 선택 시 실행할 콜백 함수 (선택적).
         * @param {object} additionalOptions - 추가로 적용할 옵션 (선택적).
         * @returns {object} - 초기화된 datepicker 인스턴스.
         */
        singleDatePicker: function(selector: string, format: string, initDt, func: Function, additionalOptions = {}) {
            if (cF.util.isEmpty(selector)) return;

            const mergedOptions = {
                ...baseOptions,
                ...additionalOptions
            };
            // 날짜 형식 설정
            if (cF.util.isNotEmpty(format)) mergedOptions.locale.format = format;
            // 초기 날짜 설정 ('today'가 아닌 경우에만 설정)
            if (initDt !== 'today') mergedOptions.startDate = cF.util.isEmpty(initDt) ? undefined : initDt;
            // datepicker 초기화
            return this.datepicker(selector, mergedOptions, func);
        },

        /**
         * dateRangePicker init : 단일 날짜-시간 선택기를 초기화합니다.
         * @param {string} selector - 초기화할 요소의 선택자 문자열.
         * @param {string} format - 날짜-시간 형식 (선택적).
         * @param {string|Date} initDt - 초기 날짜 값 (선택적, 'today'가 아닌 경우 설정).
         * @param {function} func - 날짜 선택 시 실행할 콜백 함수 (선택적).
         * @param {object} additionalOptions - 추가로 적용할 옵션 (선택적).
         * @returns {object} - 초기화된 datepicker 인스턴스.
         */
        singleDatetimePicker: function(selector: string, format: string, initDt, func: Function, additionalOptions = {}) {
            if (cF.util.isEmpty(selector)) return;

            const mergedOptions = {
                ...timeOptions,
                ...additionalOptions
            };
            // 날짜 형식 설정
            if (cF.util.isNotEmpty(format)) mergedOptions.locale.format = format;
            // 초기 날짜 설정 ('today'가 아닌 경우에만 설정)
            if (initDt !== 'today') mergedOptions.startDate = cF.util.isEmpty(initDt) ? undefined : initDt;
            // datepicker 초기화
            return this.datepicker(selector, mergedOptions, func);
        },
    }
})();
