/**
 * form.ts
 * 공통 - form 관련 함수 모듈
 *
 * @namespace: cF.form (노출식 모듈 패턴)
 * @author nichefish
 */
// @ts-ignore
if (typeof cF === 'undefined') { var cF = {} as any; }
cF.cookie = (function(): Module {
    return {
        /**
         * 쿠키를 추가합니다.
         * @param {string} name - 쿠키의 이름.
         * @param {string} value - 쿠키의 값.
         * @param {Object} [options] - 쿠키 설정 옵션.
         * @param {number} [options.maxAge] - 쿠키의 최대 수명 (초 단위).
         * @param {Date} [options.expires] - 쿠키 만료 날짜.
         */
        set: function(name: string, value: string, options: any): void {
            let cookieStr: string = encodeURIComponent(name) + '=' + encodeURIComponent(value) + ';path=/'; // 쿠키 이름과 값을 인코딩하여 설정
            if (options) {
                if (options.maxAge !== undefined) {
                    cookieStr = cookieStr + ";max-age=" + options.maxAge;
                }
                if (options.expires) {
                    cookieStr += ";expires=" + new Date(options.expires).toUTCString(); // 문자열 형식을 Date 객체로 변환하여 UTC 문자열로 설정
                }
            }
            document.cookie = cookieStr;
        },

        /**
         * 지정된 이름의 쿠키를 조회합니다.
         * @param {string} name - 조회할 쿠키의 이름.
         * @returns {string} - 쿠키 값 또는 쿠키가 없을 경우 "".
         */
        get: function(name: string): string {
            if (!document.cookie) return "";
            const array: string[] = document.cookie.split(encodeURIComponent(name) + '=');
            if (array.length < 2) return "";
            const arraySub: string[] = array[1].split(';');
            return decodeURIComponent(arraySub[0]); // 쿠키 값을 디코딩하여 반환
        },

        /**
         * 지정된 쿠키를 만료 처리합니다.
         * @param {string} name - 만료할 쿠키의 이름.
         */
        expire: function(name: string): void {
            document.cookie = encodeURIComponent(name) + "=deleted; expires=" + new Date(0).toUTCString();
        },
    }
})();