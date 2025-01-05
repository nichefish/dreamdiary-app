/**
 * date.ts
 * 공통 - 날짜 관련 함수 모듈
 *
 * @namespace: cF.date (노출식 모듈 패턴)
 * @dependency: commons.js
 */
// @ts-ignore
if (typeof cF === 'undefined') { var cF = {} as any; }
cF.date = (function(): Module {
    return {
        init: function(): void {
            console.log("'cF.date' module initialized.");
        },

        ptnDate: "yyyy-MM-dd",
        ptnDatetime: "yyyy-MM-dd HH:mm:ss",
        ptnPdate: "yyyyMMdd",
        ptnSdate: "yyyy/MM/dd",

        /**
         * 문자열을 받아서 `Date` 객체를 반환합니다.
         * @param {string} dateStr - 날짜를 나타내는 문자열.
         * @returns {Date|null} - 변환된 `Date` 객체 또는 유효하지 않은 경우 `null`.
         */
        strToDate: function(dateStr: string): Date|null {
            // 날짜 형식이 8자리인 경우 `YYYY-MM-DD`로 변환
            const normalizedDateStr: string = cF.date.normalizeDateStr(dateStr);
            // `Date` 객체 생성 및 반환
            try {
                const newDate: Date = new Date(normalizedDateStr);
                // 유효하지 않은 날짜이면 null 반환
                if (isNaN(newDate.getTime())) return null;

                return newDate;
            } catch (error) {
                console.error("Error parsing date:", error);
                return null;
            }
        },

        /**
         * 날짜를 받아서 지정된 패턴에 따른 문자열을 반환합니다.
         * @param {Date} date - 변환할 `Date` 객체.
         * @param {string} [pattern] - 날짜 형식 패턴 (선택적). 기본값은 `cF.date.ptnDate`.
         * @returns {string|null} - 변환된 날짜 문자열 또는 유효하지 않은 경우 `undefined`.
         */
        dateToStr: function(date: Date, pattern: string): string|null {
            if (!(date instanceof Date)) return null;

            pattern = pattern || cF.date.ptnDate;      // 패턴이 없을 경우 기본 패턴 설정

            // 구분자 추출 (첫 번째 구분자)
            const delim: string = pattern.replace(/[a-zA-Z]/g, "").charAt(0);
            // 구분자에 따른 날짜 문자열 구성 및 반환
            const year: number = date.getFullYear();
            const month: string = ('0' + (date.getMonth() + 1)).slice(-2);
            const day: string = ('0' + date.getDate()).slice(-2);
            return year + delim + month + delim + day;
        },

        /**
         * 날짜 또는 문자열을 받아서 `Date` 객체로 변환합니다.
         * @param {Date|string} date - 변환할 날짜 또는 문자열.
         * @returns {Date|null} - 변환된 `Date` 객체 또는 유효하지 않은 경우 `null`.
         */
        asDate: function(date: Date|string): Date|null {
            // 문자열이 'today'인 경우 현재 날짜 반환
            if (date === "today") return new Date();

            // 값이 비어 있는 경우 null 반환
            if (cF.util.isEmpty(date)) return null;

            // `Date` 객체인 경우 그대로 반환
            if (date instanceof Date) return date;

            // 문자열인 경우 `Date`로 변환하여 반환
            if (typeof date === "string") return cF.date.strToDate(date);

            // 유효하지 않은 입력인 경우 null 반환
            return null;
        },

        /**
         * 날짜 또는 문자열을 받아서 지정된 패턴에 따른 날짜 문자열을 반환합니다.
         * @param {Date|string} date - 변환할 날짜 또는 문자열.
         * @param {string} [pattern] - 날짜 형식 패턴 (선택적).
         * @returns {string|null} - 변환된 날짜 문자열 또는 유효하지 않은 경우 `undefined`.
         */
        asStr: function(date: Date|string, pattern: string): string|null {
            pattern = pattern || cF.date.ptnDate;      // 패턴이 없을 경우 기본 패턴 설정

            // 문자열이 'today'인 경우 현재 날짜를 패턴에 맞게 변환
            if (date === "today") return cF.date.dateToStr(new Date(), pattern);

            // 값이 비어 있는 경우 null 반환
            if (cF.util.isEmpty(date)) return null;

            // `Date` 객체로 변환한 후 패턴에 맞게 문자열로 반환
            return cF.date.dateToStr(cF.date.asDate(date), pattern);
        },

        /**
         * 8자리 날짜 문자열을 `YYYY-MM-DD` 형식으로 변환합니다.
         * @param {string} dateStr - 변환할 8자리 날짜 문자열.
         * @param {string} delim - 날짜 구분자 (기본값은 "-").
         * @returns {string} - 변환된 날짜 문자열 (`YYYY-MM-DD` 형식) 또는 원본 문자열.
         */
        normalizeDateStr: function(dateStr: string, delim: string = "-"): string {
            if (dateStr.length === 8) {
                return dateStr.substring(0, 4) + delim +
                    dateStr.substring(4, 6) + delim +
                    dateStr.substring(6, 8);
            }
            return dateStr; // 변환이 필요 없는 경우 원본 반환
        },

        /**
         * 현재 날짜(시간 제외)를 지정된 패턴에 맞춰 문자열로 반환합니다.
         * @param {string} [pattern] - 날짜 형식 패턴 (선택적). 기본값은 `cF.date.ptnDate`.
         * @returns {string} - 변환된 날짜 문자열.
         */
        getCurrDateStr: function(pattern: string): string {
            pattern = pattern || cF.date.ptnDate;      // 패턴이 없을 경우 기본 패턴 설정

            return cF.date.dateToStr(new Date(), pattern);
        },

        /**
         * 어제의 날짜(시간 제외)를 지정된 패턴에 맞춰 문자열로 반환합니다.
         * @param {string} [pattern] - 날짜 형식 패턴 (선택적).
         * @returns {string} - 변환된 날짜 문자열.
         */
        getPrevDateStr: function(pattern: string): string {
            pattern = pattern || cF.date.ptnDate;      // 패턴이 없을 경우 기본 패턴 설정

            return cF.date.getCurrDateAddDayStr(-1, pattern);
        },

        /**
         * 내일 날짜(시간 제외) 문자열 반환 (pattern)
         * @param pattern (optional)
         * @returns {string} - 내일 날짜 문자열.
         */
        getNextDateStr: function(pattern: string): string {
            pattern = pattern || cF.date.ptnDate;      // 패턴이 없을 경우 기본 패턴 설정

            return cF.date.getCurrDateAddDayStr(1, pattern);
        },

        /**
         * 현재 연도를 숫자로 반환합니다.
         * @returns {number} - 현재 연도 (`YYYY`)를 나타내는 숫자.
         */
        getCurrYy: function(): number {
            return new Date().getFullYear();
        },
        
        /**
         * 현재 연도를 문자열로 반환합니다.
         * @returns {string} - 현재 연도 (`YYYY`)를 나타내는 문자열.
         */
        getCurrYyStr: function(): string {
            return cF.date.getCurrYy().toString();
        },

        /**
         * 현재 월을 숫자로 반환합니다.
         * @param {boolean} [dev=false] - `true`이면 0부터 시작하는 월을 반환합니다.
         * @returns {number} - 현재 월 (`MM`)을 나타내는 숫자 (1부터 12까지).
         */
        getCurrMnth: function(dev: boolean = false): number {
            return new Date().getMonth() + (dev ? 0 : 1);
        },

        /**
         * 현재 월을 문자열로 반환합니다.
         * @param {boolean} [dev=false] - `true`이면 0부터 시작하는 월을 반환합니다.
         * @returns {string} - 현재 월 (`MM`)을 나타내는 문자열.
         */
        getCurrMnthStr: function(dev: boolean = false): string {
            return cF.date.getCurrMnth(dev).toString();
        },

        /**
         * 현재 일자를 지정된 자릿수로 문자열로 반환합니다.
         * @param {number} [digits=1] - 반환할 일자의 최소 자릿수 (기본값은 1).
         * @returns {string} - 현재 일자를 나타내는 문자열.
         */
        getCurrDayStr: function(digits: number = 1): string {
            let day: string = new Date().getDate().toString();
            // 숫자를 지정된 자릿수로 맞추기
            if (day.length < digits) {
                day = day.padStart(digits, '0');
            }
            return day;
        },

        /**
         * 현재 날짜(시간 제외)에 일수를 더해서 새로운 `Date` 객체로 반환합니다.
         * @param {number} day - 더할 일수 (양수면 미래, 음수면 과거 날짜).
         * @returns {Date} - 일수가 더해진 새로운 `Date` 객체.
         */
        getCurrDateAddDay: function(day: number): Date {
            const currDate: Date = new Date();
            // 새로운 Date 객체에 일수를 더해 반환
            const newDate: Date = new Date(currDate);
            newDate.setDate(currDate.getDate() + day);

            return newDate;
        },

        /**
         * 현재 날짜에 일수를 더해 지정된 패턴의 문자열로 반환합니다.
         * @param {number} day - 더할 일수 (양수면 미래, 음수면 과거 날짜).
         * @param {string} [pattern] - 반환할 날짜의 형식 (선택적).
         * @returns {string} - 일수가 더해진 날짜를 지정된 패턴으로 변환한 문자열.
         */
        getCurrDateAddDayStr: function(day: number, pattern: string): string {
            pattern = pattern || cF.date.ptnDate;      // 패턴이 없을 경우 기본 패턴 설정

            return cF.date.dateToStr(cF.date.getCurrDateAddDay(day), pattern);
        },

        /**
         * 주어진 날짜에 일수를 더해 새로운 `Date` 객체로 반환합니다.
         * @param {Date|string} paramDate - 기준이 되는 날짜 (Date 객체 또는 날짜 문자열).
         * @param {number} day - 더할 일수 (양수면 미래, 음수면 과거 날짜).
         * @returns {Date|null} - 일수가 더해진 새로운 `Date` 객체 또는 유효하지 않은 경우 `null`.
         */
        getDateAddDay: function(paramDate: Date|string, day: number): Date|null {
            // 유효한 날짜인지 확인
            const baseDate: Date = new Date(paramDate);
            if (isNaN(baseDate.getTime())) return null;
            // 새로운 날짜 객체 생성 후 일수 더하기
            const newDate: Date = new Date(baseDate);
            newDate.setDate(newDate.getDate() + day);
            return newDate;
        },

        /**
         * 주어진 날짜의 끝 시간을 설정하여 새로운 `Date` 객체로 반환합니다.
         * @param {Date|string} paramDate - 기준이 되는 날짜 (Date 객체 또는 날짜 문자열).
         * @returns {Date|null} - 하루의 끝 시간을 나타내는 `Date` 객체 또는 유효하지 않은 경우 `null`.
         */
        getEndOfDay: function(paramDate: Date|string): Date|null {
            // 유효한 날짜인지 확인
            const date: Date = new Date(paramDate);
            if (isNaN(date.getTime())) return null;
            // 하루의 끝 시간 설정
            date.setHours(23, 59, 59);
            return date;
        },

        /**
         * 주어진 날짜에 일수를 더해 지정된 패턴의 문자열로 반환합니다.
         * @param {Date|string} date - 기준이 되는 날짜 (Date 객체 또는 날짜 문자열).
         * @param {number} day - 더할 일수 (양수면 미래, 음수면 과거 날짜).
         * @param {string} [pattern] - 반환할 날짜의 형식 (선택적).
         * @returns {string|null} - 일수가 더해진 날짜를 지정된 패턴으로 변환한 문자열 또는 유효하지 않은 경우 `null`.
         */
        getDateAddDayStr: function(date: Date|string, day: number, pattern: string): string|null {
            // 유효하지 않은 날짜인 경우 null 반환
            const newDate: Date = cF.date.getDateAddDay(date, day);
            if (newDate === null) return null;

            pattern = pattern || cF.date.ptnDate;      // 패턴이 없을 경우 기본 패턴 설정

            return cF.date.dateToStr(newDate, pattern);
        },

        /**
         * 날짜 문자열을 받아서 일수를 더해 새로운 `Date` 객체로 반환합니다.
         * @param {string} datestr - 기준이 되는 날짜 문자열.
         * @param {number} day - 더할 일수 (양수면 미래, 음수면 과거 날짜).
         * @returns {Date|null} - 일수가 더해진 새로운 `Date` 객체 또는 유효하지 않은 경우 `null`.
         */
        getDateStrAddDay: function(datestr: string, day: number): Date|null {
            // 유효한 날짜인지 확인
            const date: Date = cF.date.asDate(datestr);
            if (date === null) return null;

            // 원본 날짜를 변경하지 않도록 새로운 날짜 객체 생성 후 일수 더하기
            const newDate: Date = new Date(date);
            newDate.setDate(newDate.getDate() + day);
            return newDate;
        },

        /**
         * 날짜 문자열에 일수를 더해 지정된 패턴의 문자열로 반환합니다.
         * @param {string} datestr - 기준이 되는 날짜 문자열.
         * @param {number} day - 더할 일수 (양수면 미래, 음수면 과거 날짜).
         * @param {string} [pattern] - 반환할 날짜의 형식 (선택적).
         * @returns {string|null} - 일수가 더해진 날짜를 지정된 패턴으로 변환한 문자열 또는 유효하지 않은 경우 `null`.
         */
        getDateStrAddDayStr: function(datestr: string, day: number, pattern: string): string|null {
            // 유효한 날짜인지 확인
            const newDate: Date = cF.date.getDateStrAddDay(datestr, day);
            if (newDate === null) return null;

            pattern = pattern || cF.date.ptnDate;      // 패턴이 없을 경우 기본 패턴 설정

            return cF.date.dateToStr(newDate, pattern);
        },

        /**
         * 현재 시간을 `HH:mm:ss` 형식의 문자열로 반환합니다.
         * @returns {string} - 현재 시간을 나타내는 문자열.
         */
        getCurrTimeStr: function(): string {
            const currDate: Date = new Date();
            return cF.date.getTimeStr(currDate);
        },

        /**
         * 현재 날짜와 시간을 `yyyy-MM-dd HH:mm:ss` 형식의 문자열로 반환합니다.
         * @param {string} [pattern] - 날짜-시간 형식 (선택적). 기본값은 `cF.date.ptnDatetime`.
         * @returns {string} - 현재 날짜와 시간을 나타내는 문자열.
         */
        getCurrDatetimeStr: function(pattern: string): string {
            pattern = pattern || cF.date.ptnDate;      // 패턴이 없을 경우 기본 패턴 설정

            return cF.date.getDatetimeStr(new Date(), pattern);
        },

        /**
         * 날짜 문자열을 받아서 지정된 패턴으로 변환된 문자열을 반환합니다.
         * @param {string} dateStr - 변환할 날짜 문자열 (형식: YYYYMMDD 또는 다른 형식).
         * @param {string} [pattern] - 반환할 날짜 형식 (선택적). 기본값은 `cF.date.ptnDate`.
         * @returns {string|null} - 변환된 날짜 문자열 또는 유효하지 않은 경우 `null`.
         */
        getDateStrStr: function(dateStr: string, pattern: string): string|null {
            // 날짜 형식이 8자리인 경우 `YYYY-MM-DD`로 변환
            const date: Date = cF.date.strToDate(dateStr);
            if (date === null) return null;

            pattern = pattern || cF.date.ptnDate;      // 패턴이 없을 경우 기본 패턴 설정

            // 지정된 패턴으로 날짜 문자열 반환
            return cF.date.dateToStr(date, pattern);
        },

        /**
         * 날짜 객체를 받아서 시간 문자열로 반환합니다 (형식: HH:mm:ss).
         * @param {Date} date - 변환할 `Date` 객체.
         * @returns {string|null} - 변환된 시간 문자열 또는 유효하지 않은 경우 `null`.
         */
        getTimeStr: function(date: Date): string|null {
            if (!(date instanceof Date)) return null;
            const hours: string = ('0' + date.getHours()).slice(-2);
            const minutes: string = ('0' + date.getMinutes()).slice(-2);
            const seconds: string = ('0' + date.getSeconds()).slice(-2);

            return hours + ":" + minutes + ":" + seconds;
        },

        /**
         * 날짜를 받아서 날짜시간 문자열을 반환합니다 (형식: yyyy-MM-dd HH:mm:ss).
         * @param {Date} date - 변환할 `Date` 객체.
         * @param {string} [pattern] - 날짜 형식 패턴 (선택적). 기본값은 `cF.date.ptnDate`.
         * @returns {string|null} - 변환된 날짜시간 문자열 또는 유효하지 않은 경우 `null`.
         */
        getDatetimeStr: function(date: Date, pattern: string): string|null {
            if (!(date instanceof Date)) return null;

            pattern = pattern || cF.date.ptnDate;      // 패턴이 없을 경우 기본 패턴 설정

            return cF.date.dateToStr(date, pattern) + " " + cF.date.getTimeStr(date);
        },

        /**
         * 시작-끝 문자열을 받아서 현재 시간이 포함되어 있는지 여부를 판단합니다.
         * @param {Date|string} startDt - 시작 날짜 (문자열 또는 Date 객체).
         * @param {Date|string} endDt - 종료 날짜 (문자열 또는 Date 객체).
         * @returns {boolean} - 현재 날짜가 시작 날짜와 종료 날짜 사이에 있으면 `true`, 그렇지 않으면 `false`.
         */
        chkIfCurrDateWithin: function(startDt: Date|string, endDt: Date|string): boolean {
            // 현재 날짜를 문자열 형식으로 가져옴
            const currentDateStr: string = this.getCurrDateStr();
            // 현재 날짜가 시작과 끝 날짜 사이에 있는지 체크
            return cF.date.getDateWithinChk(currentDateStr, startDt, endDt);
        },

        /**
         * 주어진 날짜가 시작 및 종료 날짜 사이에 있는지 여부를 판단합니다.
         * @param {Date|string} dt - 확인할 날짜 (문자열 또는 Date 객체).
         * @param {Date|string} startDt - 시작 날짜 (문자열 또는 Date 객체).
         * @param {Date|string} endDt - 종료 날짜 (문자열 또는 Date 객체).
         * @returns {boolean} - 주어진 날짜가 시작 날짜와 종료 날짜 사이에 있으면 `true`, 그렇지 않으면 `false`.
         */
        getDateWithinChk: function(dt: Date|string, startDt: Date|string, endDt: Date|string): boolean {
            // 입력 값들을 Date 객체로 변환
            const dateToCheck: Date = cF.date.asDate(dt);
            const startDate: Date = cF.date.asDate(startDt);
            const endDate: Date = cF.date.asDate(endDt);

            // 시작 날짜가 비어있을 경우 종료 날짜만 체크
            if (cF.util.isEmpty(startDt)) {
                return dateToCheck <= endDate;
            }
            // 종료 날짜가 비어있을 경우 시작 날짜만 체크
            if (cF.util.isEmpty(endDt)) {
                return dateToCheck >= startDate;
            }
            // 시작 날짜와 종료 날짜 사이에 있는지 체크
            return dateToCheck >= startDate && dateToCheck <= endDate;
        },

        /**
         * 주어진 날짜에서 요일을 한글 또는 영문으로 반환합니다.
         * @param {Date|string} date - 변환할 `Date` 객체 또는 날짜 문자열.
         * @param {string} locale - 반환할 요일의 언어 코드 ("KO" 또는 "EN").
         * @returns {string|null} - 요일 문자열 (한글 또는 영문) 또는 유효하지 않은 경우 `null`.
         */
        getDayweekStr: function(date: Date|string, locale: string): string|null {
            // 날짜를 `Date` 객체로 변환
            const asDate: Date = cF.date.asDate(date);
            if (asDate === null) return null; // 유효하지 않은 날짜 처리

            const dw: number = asDate.getDay(); // 요일 인덱스
            const dwStrArr = [["일", "SUN"], ["월", "MON"], ["화", "TUE"], ["수", "WED"], ["목", "THU"], ["금", "FRI"], ["토", "SAT"]];

            let localeIdx: number;
            switch (locale) {
                case "KO":
                    localeIdx = 0;
                    break;
                case "EN":
                    localeIdx = 1;
                    break;
                default:
                    localeIdx = 0; // 기본값으로 한글 설정
            }
            return dwStrArr[dw][localeIdx];
        },

        /**
         * 특정 날짜가 포함되는 주의 특정 요일 날짜를 반환합니다.
         * @param {Date|string} date - 기준이 되는 날짜 (Date 객체 또는 날짜 문자열).
         * @param {number} paramWeekday - 반환할 요일 (0: 일요일, 1: 월요일, ..., 6: 토요일).
         * @returns {Date|null} - 지정된 요일의 날짜를 나타내는 `Date` 객체 또는 유효하지 않은 경우 `null`.
         */
        getWeekdayDate: function(date: Date|string, paramWeekday: number): Date|null {
            // 입력된 날짜를 `Date` 객체로 변환
            const asDate: Date = cF.date.asDate(date);
            if (asDate === null) return null;

            // 요일 인덱스를 받아서 빼고 해당 요일로 추가
            const currentDay: number = asDate.getDay(); // 현재 요일 인덱스
            const dayDiff: number = asDate.getDate() - currentDay + (currentDay === 0 ? -7 : 0) + paramWeekday;

            // 새로운 날짜 객체 반환
            return new Date(asDate.setDate(dayDiff));
        },

        /**
         * 오늘 날짜가 포함되는 주의 특정 요일 날짜 문자열을 반환합니다.
         * @param {Date|string} date - 기준이 되는 날짜 (Date 객체 또는 날짜 문자열).
         * @param {0|1|2|3|4|5|6} paramWeekday - 반환할 요일 (0: 일요일, 1: 월요일, ..., 6: 토요일).
         * @param {string} [pattern] - 반환할 날짜 형식 (선택적). 기본값은 `cF.date.ptnDate`.
         * @returns {string|null} - 지정된 요일의 날짜를 나타내는 문자열 또는 유효하지 않은 경우 `null`.
         */
        getWeekdayDateStr: function(date: Date|string, paramWeekday: 0|1|2|3|4|5|6, pattern: string): string|null {
            pattern = pattern || cF.date.ptnDate;      // 패턴이 없을 경우 기본 패턴 설정

            // 특정 요일의 날짜 객체를 가져오기
            const weekdayDate: Date = cF.date.getWeekdayDate(date, paramWeekday);
            if (weekdayDate === null) return null; // 유효하지 않은 경우 null 반환

            // 날짜 객체를 지정된 패턴으로 변환하여 문자열 반환
            return cF.date.dateToStr(weekdayDate, pattern);
        },

        /**
         * 시작일과 종료일을 비교하여 시작일이 종료일보다 앞서는지 여부를 반환합니다.
         * @param {string|HTMLElement|JQuery} beginSelector - 시작일 입력 요소의 선택자 문자열.
         * @param {string|HTMLElement|JQuery} endSelector - 종료일 입력 요소의 선택자 문자열.
         * @returns {boolean} - 시작일이 종료일보다 같거나 이전이면 `true`, 그렇지 않으면 `false`, 유효하지 않은 경우 `null`.
         */
        isBefore: function(beginSelector: string|HTMLElement|JQuery, endSelector: string|HTMLElement|JQuery): boolean {
            // 시작일과 종료일의 값 가져오기
            const beginElmt: HTMLInputElement = cF.util.verifySelector(beginSelector);
            const begin: string = beginElmt?.value;
            const endElmt: HTMLInputElement = cF.util.verifySelector(endSelector);
            const end: string = endElmt?.value;

            // 날짜로 변환
            const beginCompareDate: Date = cF.date.asDate(begin);
            const endCompareDate: Date = cF.date.asDate(end);

            // 유효한 날짜인지 확인
            if (beginCompareDate === null || endCompareDate === null) return false;

            // 시작일이 종료일보다 앞서는지 비교
            return beginCompareDate.getTime() <= endCompareDate.getTime();
        },

        /**
         * 기준일을 바탕으로 좌우로 이동한 날짜를 계산합니다.
         * @param {'day'|'week'|'month'|'year'} searchType - 이동할 단위 ("day", "week", "month", "year").
         * @param {Date|string} stdrdDate - 기준일 (형식: "yyyy-MM-dd").
         * @param {'prev'|'next'} type - 이동 방향 ("prev" 또는 "next").
         * @returns {Date|null} - 이동한 날짜를 나타내는 `Date` 객체 또는 유효하지 않은 경우 `null`.
         */
        navigateDate: function(searchType: 'day'|'week'|'month'|'year', stdrdDate: Date|string, type: 'prev'|'next'): Date|null {
            // 기준일을 Date 객체로 변환
            const date: Date = cF.date.asDate(stdrdDate);
            if (date === null) return null;

            switch(searchType) {
                case "day":
                    // 어제, 내일
                    return cF.date.getDateStrAddDay(stdrdDate, (type === "prev") ? -1 : 1);
                case "week":
                    // 이전주, 다음주
                    return cF.date.getDateStrAddDay(stdrdDate, (type === "prev") ? -7 : 7);
                case "month":
                    // 이전월, 다음월
                    if (type === "prev") {
                        date.setMonth(date.getMonth() - 1);
                        date.setDate(1); // 첫 날로 설정
                    } else {
                        date.setMonth(date.getMonth() + 1);
                        date.setDate(1); // 첫 날로 설정
                    }
                    return date;
                case "year":
                    // 작년, 내년
                    const newYear: number = date.getFullYear() + (type === "prev" ? -1 : 1);
                    return new Date(newYear, 0, 1); // 해당 연도의 1월 1일
                default:
                    return null; // 유효하지 않은 경우
            }
        },

        /**
         * 기준일을 바탕으로 좌우로 이동한 날짜를 계산합니다.
         * @param {'day'|'week'|'month'|'year'} searchType - 이동할 단위 ("day", "week", "month", "year").
         * @param {Date|string} stdrdDate - 기준일 (형식: "yyyy-MM-dd").
         * @param {'prev'|'next'} type - 이동 방향 ("prev" 또는 "next").
         * @param {string} pattern - 날짜 패턴
         * @returns {string|undefined} - 이동한 날짜를 나타내는 `Date` 객체 또는 유효하지 않은 경우 undefined.
         */
        navigateDateStr: function(searchType: 'day'|'week'|'month'|'year', stdrdDate: Date|string, type: 'prev'|'next', pattern: string): string|null {
            // 기준일을 바탕으로 좌우 이동한 날짜 계산
            const newDate: Date = cF.date.navigateDate(searchType, stdrdDate, type);
            if (newDate === null) return null;

            pattern = pattern || cF.date.ptnDate;      // 패턴이 없을 경우 기본 패턴 설정

            return cF.date.dateToStr(newDate, pattern);
        }
    }
})();
