/**
 * commons.ts
 * @namespace: cF.util
 * @author: nichefish
 * @dependency: jquery.blockUI.js, jquery.forms.js
 * 공통 - 일반 함수 모듈
 * (노출식 모듈 패턴 적용 :: cF.util.enterKey("#userId") 이런식으로 사용)
 */
if (typeof cF === 'undefined') {
    let cF = {};
}
cF.util = (function () {
    return {
        /**
         * blockUI wrapped by try-catch
         */
        blockUI: function () {
            const blockMessage = `<div class="flex-column py-2 bg-dark bg-opacity-25">
                        <span class="spinner-border text-primary" role="status"></span>
                        <span class="text-muted fs-6 fw-semibold mt-5">Loading...</span>
                    </div>`;
            try {
                if (typeof $.blockUI === "function") {
                    $.blockUI({ message: blockMessage });
                }
                else {
                    console.log("blockUI is not defined.");
                }
            }
            catch (error) {
                console.log("An error occurred while trying to block the UI.");
            }
        },
        /**
         * unblockUI wrapped by try-catch
         * @param {number} delay - unblockUI를 호출하기 전 대기할 시간(밀리초). (기본 1.5초)
         */
        unblockUI: function (delay = 1500) {
            setTimeout(function () {
                try {
                    $.unblockUI();
                }
                catch (error) {
                    console.log("blockUI is not defined.");
                }
            }, delay);
        },
        /**
         * SweetAlert가 정의되어 있는지 확인합니다.
         * @returns {boolean} - Swal이 정의되어 있으면 true, 그렇지 않으면 false.
         */
        hasSwal: function () {
            return typeof Swal !== 'undefined';
        },
        /**
         * SweetAlert가 존재하는 경우 Swal을 사용하여 메시지를 표시하고, 그렇지 않으면 alert을 사용합니다.
         * @param {string} msg - 표시할 메시지.
         * @param {Function} [func] - 메시지 처리 후 실행할 콜백 함수 (선택적).
         */
        swalOrAlert: function (msg, func) {
            const hasCallback = typeof func === 'function';
            if (cF.util.hasSwal()) {
                // SweetAlert가 존재할 때
                Swal.fire(msg)
                    .then(function () {
                    if (hasCallback)
                        func();
                });
            }
            else {
                alert(msg);
                if (hasCallback)
                    func();
            }
        },
        /**
         * SweetAlert가 존재하는 경우 Swal을 사용하여 메시지를 표시하고, 그렇지 않으면 confirm을 사용합니다.
         * @param {string} msg - 표시할 메시지.
         * @param {Function} [trueFunc] - 확인 버튼 클릭 시 실행할 함수 (선택적).
         * @param {Function} [falseFunc] - 취소 버튼 클릭 시 실행할 함수 (선택적).
         */
        swalOrConfirm: function (msg, trueFunc, falseFunc) {
            const hasTrueFunc = typeof trueFunc === 'function';
            const hasFalseFunc = typeof falseFunc === 'function';
            if (cF.util.hasSwal()) {
                Swal.fire({
                    text: msg,
                    showCancelButton: true,
                }).then(function (result) {
                    if (result.value && hasTrueFunc) {
                        trueFunc();
                    }
                    else if (hasFalseFunc) {
                        falseFunc();
                    }
                });
            }
            else {
                if (confirm(msg) && hasTrueFunc) {
                    trueFunc();
                }
                else if (hasFalseFunc) {
                    falseFunc();
                }
            }
        },
        /**
         * 주어진 데이터가 비어 있는지 확인합니다.
         * null, undefined, 빈 객체, 빈 배열, 공백 문자열을 체크합니다.
         * @param {any} data - 체크할 데이터.
         * @returns {boolean} - 데이터가 비어 있으면 true, 그렇지 않으면 false.
         */
        isEmpty: function (data) {
            if (data == null)
                return true;
            const type = typeof (data);
            if (type === 'object') {
                return Object.keys(data).length === 0;
            }
            else if (type === 'string') {
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
        isNotEmpty: function (data) {
            return !cF.util.isEmpty(data);
        },
        /**
         * 선택자에서 유효한 입력 요소를 반환합니다.
         * @param {string|HTMLElement|jQuery} selector - 선택자 문자열 또는 DOM 요소 또는 jQuery 객체.
         * @returns {HTMLElement[]} - 유효한 입력 요소 배열 또는 빈 배열.
         */
        verifySelector: function (selector) {
            if (selector instanceof jQuery) {
                return selector.toArray();
            }
            else if (selector instanceof HTMLElement) {
                return [selector];
            }
            else if (typeof selector === 'string') {
                return Array.from(document.querySelectorAll(selector));
            }
            return [];
        },
        /**
         * 선택자 요소의 존재 여부를 체크합니다.
         * @param {string|jQuery|HTMLElement} selectorStr - 체크할 요소의 선택자 문자열, jQuery 객체, 또는 DOM 요소.
         * @returns {boolean} - 요소가 존재하면 true, 그렇지 않으면 false.
         */
        isPresent: function (selector) {
            return this.verifySelector(selector).length > 0;
        },
        /**
         * 새 팝업을 엽니다.
         * @param {string} url - 팝업으로 열 URL.
         * @param {string} popupNm - 팝업 창 이름.
         * @param {string} option - 팝업 창 옵션 (예: 'width=600,height=400').
         * @returns {Window|null} - 열린 팝업 창의 Window 객체 또는 null.
         */
        openPopup: function (url, popupNm, option) {
            const popupWindow = window.open(url, popupNm, option);
            if (!popupWindow) {
                console.error("팝업 차단기 또는 잘못된 URL로 인해 팝업을 열 수 없습니다.");
            }
            return popupWindow; // 열린 팝업 창의 Window 객체 반환
        },
        /**
         * 입력 요소에 Enter 키 처리를 추가합니다.
         * @param {string} selectorStr - 선택자 문자열.
         * @param {Function} func - Enter 키를 눌렀을 때 호출할 함수.
         */
        enterKey: function (selectorStr, func) {
            if (!selectorStr || typeof func !== 'function')
                return;
            const inputs = document.querySelectorAll(selectorStr);
            if (inputs.length === 0)
                return;
            inputs.forEach(input => {
                input.addEventListener("keyup", function (event) {
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
        getReqstItemIdx: function (arrElmt, selectorStr, arrElmtId) {
            const reqstDataArr = document.querySelectorAll(arrElmt + "[" + selectorStr + "]"); // 선택자에 해당하는 요소 가져오기
            if (reqstDataArr.length === 0)
                return 0; // 요소가 없으면 0 반환
            // 각 요소의 인덱스를 계산하여 최대 인덱스를 구함
            return Array.from(reqstDataArr).reduce((reqstItemIdx, elmt) => {
                const isExcluded = elmt.id.includes("__");
                if (isExcluded)
                    return reqstItemIdx;
                const currentIdx = Number(elmt.id.replace(arrElmtId, ""));
                return Math.max(reqstItemIdx, currentIdx + 1);
            }, 0); // 초기값은 0
        },
        /**
         * 행 추가 함수에서 해당 input의 값(숫자) 총합을 구하여 반환합니다.
         * ".excludeSum" 클래스를 가진 요소는 제외됩니다.
         * @param {string} selectorStr - 선택자 문자열.
         * @returns {number} - 총합 값.
         */
        getReqstItemTotSum: function (selectorStr) {
            const reqstDataArr = document.querySelectorAll("input[" + selectorStr + "]");
            if (reqstDataArr.length === 0)
                return 0;
            return Array.from(reqstDataArr).reduce((total, elmt) => {
                const isExcluded = elmt.classList.contains("excludeSum") || elmt.id.includes("{");
                const value = elmt.value.replace(/,/g, ""); // 쉼표 제거
                const numberValue = Number(value); // 숫자로 변환
                return isExcluded ? total : total + (isNaN(numberValue) ? 0 : numberValue); // 제외되지 않은 경우에만 총합에 추가
            }, 0); // 초기값은 0
        },
        /**
         * 주어진 선택자 문자열에 해당하는 input 요소의 값을 숫자로 변환하여 반환합니다.
         * @param {string} selector - 변환할 input 요소의 선택자 문자열.
         * @returns {number|null} - 변환된 숫자 값 또는 유효하지 않은 경우 `null`.
         */
        toNumber: function (selector) {
            const inputElement = cF.util.verifySelector(selector);
            if (inputElement.length === 0)
                return null;
            const input = document.querySelector(selector);
            // input 요소의 값이 없거나 undefined인 경우 null 반환
            const inputValue = input === null || input === void 0 ? void 0 : input.value;
            if (inputValue === undefined || inputValue === "")
                return null;
            // 쉼표를 제거하고 숫자로 변환
            const numValue = Number(inputValue.replace(/,/g, ""));
            return !isNaN(numValue) ? numValue : null;
        },
        /**
         * 파일 다운로드를 수행합니다.
         * AJAX로 파일 존재 여부를 체크한 후, 임시 폼을 생성하여 제출합니다.
         * @param {string} atchFileNo - 첨부 파일 번호.
         * @param {string} atchFileDtlNo - 첨부 파일 상세 번호.
         * TODO: URL 외부에서 주입하기?
         */
        fileDownload: function (atchFileNo, atchFileDtlNo) {
            const inputs = "<input type='hidden' name='atchFileNo' value='" + atchFileNo + "'>" +
                "<input type='hidden' name='atchFileDtlNo' value='" + atchFileDtlNo + "'>";
            const form = document.createElement("form");
            form.action = "/file/fileDownload.do";
            form.method = "POST"; // POST 방식으로 설정
            form.innerHTML = inputs;
            document.body.appendChild(form); // 폼 추가
            form.submit();
            document.body.removeChild(form); // 폼 제거
        },
        /**
         * 쿠키를 추가합니다.
         * @param {string} name - 쿠키의 이름.
         * @param {string} value - 쿠키의 값.
         * @param {Object} [options] - 쿠키 설정 옵션.
         * @param {number} [options.maxAge] - 쿠키의 최대 수명 (초 단위).
         * @param {Date} [options.expires] - 쿠키 만료 날짜.
         */
        setCookie: function (name, value, options) {
            let cookieStr = encodeURIComponent(name) + '=' + encodeURIComponent(value) + ';path=/'; // 쿠키 이름과 값을 인코딩하여 설정
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
         * @returns {string|undefined} - 쿠키 값 또는 쿠키가 없을 경우 undefined.
         */
        getCookie: function (name) {
            if (!document.cookie)
                return;
            const array = document.cookie.split(encodeURIComponent(name) + '=');
            if (array.length < 2)
                return;
            const arraySub = array[1].split(';');
            return decodeURIComponent(arraySub[0]); // 쿠키 값을 디코딩하여 반환
        },
        /**
         * 지정된 쿠키를 만료 처리합니다.
         * @param {string} name - 만료할 쿠키의 이름.
         */
        expireCookie: function (name) {
            document.cookie = encodeURIComponent(name) + "=deleted; expires=" + new Date(0).toUTCString();
        },
        /**
         * 파일 다운로드 시 blockUI를 적용합니다.
         * 서버에서 응답 쿠키를 생성할 때까지 blockUI를 유지합니다.
         * @dependency blockUI (optional)
         */
        blockUIFileDownload: (function () {
            cF.util.blockUI();
            const downloadTimer = setInterval(function () {
                const token = cF.util.getCookie("FILE_CREATE_SUCCESS");
                if (token === "TRUE") {
                    cF.util.unblockUI();
                    clearInterval(downloadTimer);
                }
            }, 1000);
        }),
        /**
         * 요청 중 blockUI를 적용합니다.
         * 서버에서 응답 쿠키를 생성할 때까지 blockUI를 유지합니다.
         * @dependency blockUI (optional)
         */
        blockUIRequest: function () {
            cF.util.blockUI();
            const requestTimer = setInterval(function () {
                const token = cF.util.getCookie("RESPONSE_SUCCESS");
                if (token === "TRUE") {
                    cF.util.unblockUI();
                    clearInterval(requestTimer);
                }
            }, 1000);
        },
        /**
         * blockUI를 적용한 페이지 리로드.
         * 서버에서 응답 쿠키를 생성할 때까지 blockUI를 유지합니다.
         * @dependency blockUI (optional)
         */
        blockUIReload: function () {
            cF.util.blockUI();
            cF.util.closeModal();
            location.reload();
        },
        /**
         * blockUI를 적용한 페이지 리플레이스.
         * 서버에서 응답 쿠키를 생성할 때까지 blockUI를 유지합니다.
         * @param {string} url - 리플레이스할 URL.
         * @dependency blockUI (optional)
         */
        blockUIReplace: (function (url) {
            cF.util.blockUI();
            cF.util.closeModal();
            location.replace(url);
        }),
        /**
         * blockUI를 적용한 폼 제출.
         * 서버에서 응답 쿠키를 생성할 때까지 blockUI를 유지합니다.
         * @param {string} formSelector - 제출할 폼의 선택자.
         * @param {string} actionUrl - 폼 제출 시 사용할 액션 URL.
         * @param {Function} [prefunc] - 폼 제출 전에 실행할 함수 (선택적).
         * @dependency blockUI (optional)
         */
        blockUISubmit: function (formSelector, actionUrl, prefunc) {
            cF.util.blockUIRequest();
            cF.util.closeModal();
            cF.util.submit(formSelector, actionUrl, prefunc);
        },
        /**
         * 폼을 초기화합니다.
         * @param {string} formSelector - 초기화할 폼의 선택자.
         */
        resetForm: function (formSelector) {
            const form = document.querySelector(formSelector);
            form === null || form === void 0 ? void 0 : form.reset();
        },
        /**
         * 폼을 제출합니다.
         * @param {string} formSelector - 제출할 폼의 선택자.
         * @param {string} actionUrl - 폼 제출 시 사용할 액션 URL.
         * @param {Function} [prefunc] - 폼 제출 전에 실행할 함수 (선택적).
         */
        submit: function (formSelector, actionUrl, prefunc) {
            const form = document.querySelector(formSelector);
            if (!form)
                return false;
            if (typeof prefunc === 'function')
                prefunc();
            if (actionUrl)
                form.action = actionUrl;
            form.submit(); // 폼 제출
        },
        /**
         * 문자열의 첫 글자를 대문자로 변환합니다.
         * @param {string} str - 변환할 문자열.
         * @returns {string} - 첫 글자가 대문자로 변환된 문자열.
         */
        upperFirst: function (str) {
            if (cF.util.isEmpty(str))
                return str;
            // 첫 글자 대문자로 변환 후 나머지 문자열 결합
            return str.charAt(0).toUpperCase() + str.slice(1);
        },
        /**
         * 모든 table 헤더에 클릭 이벤트를 설정하여 해당 열을 정렬합니다.
         */
        initSortTable: function () {
            if (typeof Page === 'undefined') {
                var Page = {};
            }
            const tables = document.getElementsByTagName("table");
            // 각 테이블에 대해 헤더 클릭 이벤트 설정
            Array.from(tables).forEach((table, i) => {
                const headers = table.getElementsByTagName("th");
                // 각 헤더에 대해 클릭 이벤트 설정
                Array.from(headers).forEach((header, j) => {
                    header.onclick = () => {
                        cF.util.sortTable(table, j, Page.tableSortMode);
                        Page.tableSortMode = (Page.tableSortMode === "REVERSE") ? "FORWARD" : "REVERSE";
                    };
                });
            });
        },
        /**
         * 특정 테이블 헤더에 해당하는 열을 정렬합니다.
         * @param {string} tableId - 정렬할 테이블의 ID.
         * @param {number} colIdx - 정렬할 열의 인덱스.
         * @param {string} sortMode - 정렬 방식 ("FORWARD" 또는 "REVERSE").
         */
        sortTableByIdx: function (tableId, colIdx, sortMode) {
            const table = document.getElementById(tableId);
            cF.util.sortTable(table, colIdx, sortMode);
        },
        /**
         * 테이블(텍스트, 숫자) 정렬 함수
         * @param {HTMLTableElement} table - 정렬할 테이블 요소.
         * @param {number} n - 정렬할 열의 인덱스.
         * @param {string} sortMode - 정렬 방식 ("FORWARD" 또는 "REVERSE").
         */
        sortTable: function (table, n, sortMode) {
            if (!table || !table.tBodies)
                return;
            const tbody = table.tBodies[0];
            const rows = Array.from(tbody.getElementsByTagName("tr")); // HTMLCollection을 배열로 변환
            if (rows.length < 2)
                return;
            // 셀에서 값을 추출하여 정리하는 중첩 함수
            const getCellValue = (row, index) => {
                const cell = row.getElementsByTagName("td")[index];
                const value = cell.textContent || cell.innerText;
                return isNaN(Number(value.replace(/,/g, ""))) ? value : Number(value.replace(/,/g, ""));
            };
            // 두 값을 비교하는 중첩 함수
            const compareValues = (value1, value2) => {
                if (value1 < value2)
                    return -1;
                if (value1 > value2)
                    return 1;
                return 0;
            };
            // 행 정렬
            rows.sort((row1, row2) => {
                const value1 = getCellValue(row1, n);
                const value2 = getCellValue(row2, n);
                if (sortMode === "FORWARD") {
                    return compareValues(value1, value2);
                }
                else if (sortMode === "REVERSE") {
                    return compareValues(value2, value1);
                }
                return 0;
            });
            // 정렬된 행을 tbody에 다시 추가
            rows.forEach(row => tbody.appendChild(row));
        },
        /**
         * 테이블(추가된 input값: 텍스트, 숫자) 정렬 함수
         * @param {HTMLTableElement} table - 정렬할 테이블 요소.
         * @param {number} n - 정렬할 열의 인덱스.
         * @param {string} sortMode - 정렬 방식 ("FORWARD" 또는 "REVERSE").
         */
        sortReqstTable: function (table, n, sortMode) {
            if (!table || !table.tBodies)
                return;
            const tbody = table.tBodies[0];
            const rows = Array.from(tbody.getElementsByTagName("tr"));
            if (rows.length < 2)
                return;
            // 셀에서 값을 추출하여 정리하는 함수
            const getCellValue = (row, index) => {
                const cell = row.getElementsByTagName("td")[index];
                const inputValue = cell.getElementsByTagName("input")[0].value;
                return isNaN(Number(inputValue.replace(/,/g, ""))) ? inputValue : Number(inputValue.replace(/,/g, ""));
            };
            // 두 값을 비교하는 함수
            const compareValues = (value1, value2) => {
                if (value1 < value2)
                    return -1;
                if (value1 > value2)
                    return 1;
                return 0;
            };
            // 정렬
            rows.sort((row1, row2) => {
                const value1 = getCellValue(row1, n);
                const value2 = getCellValue(row2, n);
                if (sortMode === "FORWARD") {
                    return compareValues(value1, value2);
                }
                else if (sortMode === "REVERSE") {
                    return compareValues(value2, value1);
                }
                return 0;
            });
            // 정렬된 행을 tbody에 다시 추가
            rows.forEach(row => tbody.appendChild(row));
        },
        /**
         * 체크박스 클릭 시 라벨 변경 함수
         * @param {string} attrId - 체크박스 요소의 ID.
         * @param {string} ynCn - 예/아니오 문자열. "//"로 예와 아니오를 구분.
         * @param {string} ynColor - 예/아니오 색상. "//"로 예와 아니오 색상을 구분.
         * @param {Function} [yFunc] - 체크박스가 체크되었을 때 호출되는 함수. (선택적)
         * @param {Function} [nFunc] - 체크박스가 체크 해제되었을 때 호출되는 함수. (선택적)
         * @returns {boolean} - 체크박스가 정의되지 않은 경우 false를 반환.
         */
        chckboxLabel: function (attrId, ynCn, ynColor, yFunc, nFunc) {
            const checkboxElmt = document.getElementById(attrId);
            if (!checkboxElmt) {
                console.log("체크박스가 정의되지 않았습니다.");
                return false;
            }
            const separator = "//";
            const [yesStr, noStr] = ynCn.split(separator);
            const [yesColor, noColor] = ynColor.split(separator);
            const labelElmt = document.getElementById(attrId + "Label");
            checkboxElmt.addEventListener("click", function () {
                if (checkboxElmt.checked) {
                    labelElmt.textContent = yesStr;
                    labelElmt.style.color = yesColor;
                    if (typeof yFunc === "function")
                        yFunc();
                }
                else {
                    labelElmt.textContent = noStr;
                    labelElmt.style.color = noColor;
                    if (typeof nFunc === "function")
                        nFunc();
                }
            });
        },
        /**
         * 숫자(정수)에 천 단위로 콤마(,)를 추가.
         * @param value (숫자 또는 selectorStr)
         * @param unit (나눔단위 ex.1,400만)
         */
        thousandSeparator: function (value, unit = 1) {
            if (value === "")
                return "";
            // 숫자값이 넘어오면 걍 콤마 붙인 결과값을(string) 넘겨버린다.
            const numValue = value.replace(/,/g, "");
            if (!isNaN(numValue)) {
                const divided = parseInt(numValue, 10) / unit;
                return Number(divided).toLocaleString();
            }
            // 나머지 경우에는 selector로 간주, 입력 필드에 이벤트 리스너 추가
            const inputs = document.querySelectorAll(value);
            if (inputs.length === 0)
                return;
            inputs.forEach(elmt => {
                elmt.value = cF.util.thousandSeparator(elmt.value, unit);
                elmt.addEventListener("keyup", function () {
                    let localeStr = cF.util.thousandSeparator(elmt.value, unit);
                    // maxlength를 초과하는 경우 처리
                    const maxlength = elmt.getAttribute("maxlength");
                    if (maxlength !== null)
                        localeStr = localeStr.substring(0, maxlength);
                    elmt.value = localeStr;
                });
            });
        },
        /**
         * 숫자에 콤마(,) 빼기
         * @param: value (숫자 또는 selectorStr)
         * @param: unit (나눔단위 ex.천단위)
         */
        removeComma: function (value, unit = 1) {
            if (value === "")
                return "";
            // 숫자값이 넘어오면 걍 콤마 빼서 넘겨버린다.
            const numValue = value.replace(/,/g, "");
            if (!isNaN(numValue))
                return Number(numValue / unit);
            // 나머지 경우에는 selector로 간주, 콤마 제거 처리 및 keyup시 콤마 제거 처리한다.
            const inputs = cF.util.verifySelector(value);
            if (inputs.length === 0)
                return;
            inputs.forEach(elmt => {
                elmt.value = cF.util.removeComma(elmt.value, unit);
                elmt.addEventListener("keyup", function () {
                    elmt.value = cF.util.removeComma(elmt.value, unit);
                });
            });
        },
        /**
         * 숫자에 소숫점 처리
         * @param: selectorStr (숫자 또는 selectorStr)
         * @param: fixed (자릿수)
         */
        addDot: function (value, fixed = 0, unit = 0) {
            if (value === "")
                return "";
            // 숫자값이 넘어오면 걍 콤마 빼서 넘겨버린다.
            let numValue = value.replace(/,/g, "");
            if (!isNaN(numValue)) {
                return Number(value).toLocaleString(undefined, {
                    minimumFractionDigits: fixed,
                    maximumFractionDigits: fixed
                });
            }
            // 나머지 경우에는 selector로 간주, 콤마 제거 처리 및 keyup시 콤마 제거 처리한다.
            const inputs = document.querySelectorAll(value);
            if (inputs.length === 0)
                return;
            inputs.forEach(elmt => {
                elmt.value = cF.util.addDot(elmt.value, fixed, unit);
                elmt.addEventListener("keyup", function () {
                    elmt.value = cF.util.addDot(elmt.value, fixed, unit);
                });
            });
        },
        /**
         * 자간 처리 (글자가 영역 전체에 고르게 퍼지도록 처리)
         * @param {string} str - 자간을 적용할 문자열.
         * @returns {string} - 각 글자를 `<span>` 태그로 감싼 HTML 문자열. 별도의 CSS 처리가 필요함.
         */
        letterSpacing: function (str) {
            if (cF.util.isEmpty(str))
                return;
            // 각 글자를 span 태그로 감싸고 문자열로 결합
            return Array.from(str)
                .map(spell => "<span>" + spell + "</span>")
                .join("");
        },
        /**
         * Form 데이터 배열을 chunk 크기만큼 나누어 JSON 객체 배열로 변환
         * @param {Array} arr - 시리얼라이즈할 form 데이터 배열. 각 요소는 `{ name, value }` 형태의 객체.
         * @param {number} chunk - 배열을 분할할 크기.
         * @returns {Array} - 각 chunk별로 분할된 객체 배열.
         */
        serializeJsonArray: function (arr, chunk) {
            const processedArr = [];
            for (let i = 0; i < arr.length; i += chunk) {
                const form = arr.slice(i, i + chunk);
                const obj = form.reduce((acc, field) => {
                    if (cF.util.isEmpty(field))
                        return acc;
                    acc[field.name] = field.value;
                    return acc;
                }, {});
                processedArr.push(obj);
            }
            return processedArr;
        },
        /**
         * button 일시 잠김 (딜레이 적용)
         * @param {HTMLElement|jQuery} elmt - 비활성화할 버튼 요소. jQuery 객체 또는 DOM 요소.
         * @param {number} [sec=2] - 버튼을 비활성화할 시간(초 단위). 기본값은 2초.
         */
        delayBtn: function (elmt, sec = 2) {
            var _a;
            if (elmt instanceof jQuery)
                elmt = elmt[0];
            if (!elmt)
                return;
            if ((_a = elmt.classList) === null || _a === void 0 ? void 0 : _a.contains("modal-btn-close-safe"))
                return; // 안전닫기 버튼 제외
            elmt.disabled = true;
            setTimeout(function () {
                elmt.disabled = false;
            }, sec * 1000);
        },
        /**
         * 전체 (열려 있는) 모달 닫기
         */
        closeModal: function () {
            $(".modal.show").modal("hide");
        },
        /**
         * 페이지 상단으로 이동
         */
        toPageTop: function () {
            window.scrollTo({
                top: 0,
                behavior: 'smooth'
            });
        },
        /**
         * 현재 페이지의 특정 URL 파라미터를 업데이트하고 페이지를 새로고침합니다.
         * @param {string} key - 업데이트할 파라미터의 키.
         * @param {string} value - 지정된 파라미터 키에 설정할 값.
         */
        reloadWithParam: function (key, value) {
            // 현재 URL을 가져옵니다.
            const url = new URL(window.location.href);
            // 기존 파라미터를 지우거나 업데이트합니다.
            url.searchParams.set(key, value);
            // 변경된 URL로 리다이렉트합니다.
            window.location.href = url.toString();
        },
        toSnakeCase: function (str) {
            return str
                .replace(/([a-z])([A-Z])/g, '$1_$2') // camelCase에서 _를 추가
                .toLowerCase(); // 모두 소문자로 변환
        }
    };
})();
