/**
 * ajax.ts
 * @namespace: cF.ajax
 * @author: nichefish
 * 공통 - ajax 관련 함수 모듈
 * (노출식 모듈 패턴 적용 :: cF.util.enterKey("#userId") 이런식으로 사용)
 */
if (typeof cF === 'undefined') {
    let cF = {};
}
// 인증만료/접근불가로 ajax 실패시 로그인 페이지로 이동 또는 머무르기 (선택)
(function ($) {
    $.ajaxSetup({
        error: function (xhr) {
            const statusCode = xhr.status;
            const msg = xhr.responseJSON ? xhr.responseJSON.message : "접근이 거부되었습니다. (ACCESS DENIED)";
            const lgnFormUrl = "/auth/lgnForm.do";
            if (statusCode === 401) {
                cF.util.swalOrConfirm(msg + "\n로그인 화면으로 돌아갑니다.", function () {
                    window.location.href = lgnFormUrl;
                }, function () {
                    // do nothing... and mark as session expired
                    if ($(".session-expired-message").length > 0)
                        return;
                    // 세션 만료 표시
                    const $navbar = $("#kt_app_header_wrapper .app-navbar");
                    const sessionExpiredText = $("<div class='d-flex align-items-center fs-4 fw-bold text-danger blink me-5'>로그인 세션이 만료되었습니다.</div>");
                    $navbar.before(sessionExpiredText);
                });
                return;
            }
            else if (statusCode === 403) {
                cF.util.swalOrAlert("접근이 거부되었습니다. (FORBIDDEN)", function () {
                    window.location.href = lgnFormUrl;
                });
                return;
            }
            else if (statusCode === 400) {
                const errorLines = msg.split("\n");
                let errorField, defaultMessage;
                errorLines.forEach((line) => {
                    const fieldErrorMatch = line.match(/Field error in object '([^']+)' on field '([^']+)':/);
                    console.log(fieldErrorMatch);
                    const defaultMessageMatch = line.match(/\]; default message \[([^\[\]]+)\]$/);
                    console.log(defaultMessageMatch);
                    if (fieldErrorMatch)
                        errorField = fieldErrorMatch[2];
                    if (defaultMessageMatch)
                        defaultMessage = defaultMessageMatch[1]; // 0보다 커야 합니다
                    if (errorField && defaultMessage) {
                        const errorMsg = errorField + ": " + defaultMessage + ".";
                        // 필드네임을 스네이크 캐이스로 변환
                        const snakeFieldName = cF.util.toSnakeCase(errorField);
                        const elmts = (cF.util.verifySelector("[name=\"" + snakeFieldName + "\"]"));
                        console.log(snakeFieldName);
                        if (elmts.length === 0) {
                            cF.util.swalOrAlert(errorMsg);
                        }
                        else {
                            const elmt = elmts[0];
                            const errorSpan = document.querySelector("#" + elmt.id + "_validate_span");
                            errorSpan.classList.add("text-danger");
                            errorSpan.appendChild(document.createTextNode(errorMsg));
                            elmt.focus();
                        }
                        console.error("ajax error: ", xhr);
                    }
                });
                console.error("ajax error: ", xhr);
                return;
            }
            // 기본 오류 로그 추가
            console.error("ajax error: ", xhr);
            cF.util.swalOrAlert(msg);
        }
    });
})(jQuery);
cF.ajax = (function () {
    return {
        /**
         * AJAX 공통 형식.
         * @param {Object} option - AJAX 요청에 대한 설정 옵션.
         * @param {Function} func - 요청 성공시 호출될 콜백 함수.
         * @param {string} [continueBlock] - 추가적인 블록 UI 동작 여부 (선택적).
         */
        request: function (option, func, continueBlock) {
            cF.util.blockUI();
            $.ajax(option).done(function (res) {
                if (typeof func === 'function') {
                    const isSuccess = func(res);
                    if (!isSuccess)
                        cF.util.unblockUI();
                }
            }).fail(function (res) {
                if (cF.util.isNotEmpty(res.message))
                    cF.util.swalOrAlert(res.message);
                cF.util.unblockUI();
            }).always(function () {
                if (continueBlock !== 'block')
                    cF.util.unblockUI();
            });
        },
        /**
         * blockUI를 적용한 AJAX 호출.
         * @param {string} url - 요청할 URL.
         * @param {Object} ajaxData - JSON 형태의 요청 데이터.
         * @param {Function} func - 요청 성공시 호출될 콜백 함수.
         * @param {boolean} [continueBlock] - 추가적인 블록 UI 동작 여부 (선택적).
         */
        get: function (url, ajaxData, func, continueBlock) {
            const option = {
                url: url,
                type: 'GET',
                data: ajaxData,
                dataType: 'json',
                async: false
            };
            cF.ajax.request(option, func, continueBlock);
        },
        /**
         * blockUI를 적용한 AJAX 호출.
         * @param {string} url - 요청할 URL.
         * @param {Object} ajaxData - JSON 형태의 요청 데이터.
         * @param {Function} func - 요청 성공시 호출될 콜백 함수.
         * @param {boolean} [continueBlock] - 추가적인 블록 UI 동작 여부 (선택적).
         */
        post: function (url, ajaxData, func, continueBlock) {
            const option = {
                url: url,
                type: 'POST',
                data: ajaxData,
                dataType: 'json',
                async: false
            };
            cF.ajax.request(option, func, continueBlock);
        },
        /**
         * blockUI를 적용한 AJAX 호출 (Multipart).
         * @param {string} url - 요청할 URL.
         * @param {FormData} ajaxData - 파일 데이터가 포함된 FormData 객체.
         * @param {Function} func - 요청 성공시 호출될 콜백 함수.
         * @param {boolean} [continueBlock] - 추가적인 블록 UI 동작 여부 (선택적).
         */
        multipartRequest: function (url, ajaxData, func, continueBlock) {
            const option = {
                url: url,
                type: 'POST',
                data: ajaxData,
                dataType: 'json',
                cache: false,
                async: false,
                processData: false,
                contentType: false
            };
            cF.ajax.request(option, func, continueBlock);
        },
        /**
         * blockUI를 적용한 AJAX 호출 (비동기).
         * @param {string} url - 요청할 URL.
         * @param {string} method - HTTP 메서드 (GET, POST 등).
         * @param {Object} ajaxData - JSON 형태의 요청 데이터.
         * @param {Function} func - 요청 성공시 호출될 콜백 함수.
         * @param {boolean} [continueBlock] - 추가적인 블록 UI 동작 여부 (선택적).
         */
        asyncRequest: function (url, method, ajaxData, func, continueBlock) {
            const option = {
                url: url,
                type: method.toUpperCase(),
                data: ajaxData,
                dataType: 'json',
                async: true
            };
            cF.ajax.request(option, func, continueBlock);
        },
        /**
         * blockUI를 적용한 AJAX 호출 (JSON 요청 본문).
         * @param {string} url - 요청할 URL.
         * @param {string} method - HTTP 메서드 (GET, POST 등).
         * @param {Object} ajaxData - JSON 형태의 요청 데이터.
         * @param {Function} func - 요청 성공시 호출될 콜백 함수.
         * @param {boolean} [continueBlock] - 추가적인 블록 UI 동작 여부 (선택적).
         */
        jsonRequest: function (url, method, ajaxData, func, continueBlock) {
            const option = {
                url: url,
                type: method.toUpperCase(),
                data: ajaxData,
                dataType: 'json',
                async: false,
                contentType: 'application/json',
                traditional: true
            };
            cF.ajax.request(option, func, continueBlock);
        },
    };
})();
