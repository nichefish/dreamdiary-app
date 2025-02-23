/**
 * $ajax.ts
 * 공통 - ajax (jQuery) 관련 함수 모듈
 *
 * @namespace: cF.$ajax (노출식 모듈 패턴)
 * @author: nichefish
 */
// @ts-ignore
if (typeof cF === 'undefined') { var cF = {} as any; }
cF.$ajax = (function(): Module {
    return {
        /**
         * AJAX 공통 형식. (jQuery.ajax)
         * @param {Object} option - AJAX 요청에 대한 설정 옵션.
         * @param {Function} func - 요청 성공시 호출될 콜백 함수.
         * @param {string} [continueBlock] - 추가적인 블록 UI 동작 여부 (선택적).
         */
        request: function(option: object, func: Function, continueBlock: string): void {
            cF.ui.blockUI();
            $.ajax(
                option
            ).done(function(res): void {
                if (typeof func === 'function') {
                    const isSuccess = func(res);
                    if (!isSuccess) cF.ui.unblockUI();
                }
            // @ts-ignore
            }).fail(function(res: AjaxResponse): void {
                if (cF.util.isNotEmpty(res.message)) cF.ui.swalOrAlert(res.message);
                cF.ui.unblockUI();
            }).always(function(): void {
                if (continueBlock !== 'block') cF.ui.unblockUI();
            });
        },

        /**
         * blockUI를 적용한 AJAX 호출.
         * @param {string} url - 요청할 URL.
         * @param {Object} ajaxData - JSON 형태의 요청 데이터.
         * @param {Function} func - 요청 성공시 호출될 콜백 함수.
         * @param {boolean} [continueBlock] - 추가적인 블록 UI 동작 여부 (선택적).
         */
        get: function(url: string, ajaxData: object, func: Function, continueBlock: string) {
            const option: Record<string, any> = {
                url: url,
                type: 'GET',
                data: ajaxData,
                dataType: 'json',
                async: false
            };
            cF.$ajax.request(option, func, continueBlock);
        },

        /**
         * blockUI를 적용한 AJAX 호출.
         * @param {string} url - 요청할 URL.
         * @param {Object} ajaxData - JSON 형태의 요청 데이터.
         * @param {Function} func - 요청 성공시 호출될 콜백 함수.
         * @param {boolean} [continueBlock] - 추가적인 블록 UI 동작 여부 (선택적).
         */
        post: function(url: string, ajaxData: object, func: Function, continueBlock: string): void {
            const option: Record<string, any> = {
                url: url,
                type: 'POST',
                data: ajaxData,
                dataType: 'json'
            };
            cF.$ajax.request(option, func, continueBlock);
        },

        /**
         * blockUI를 적용한 AJAX 호출 (Multipart).
         * @param {string} url - 요청할 URL.
         * @param {FormData} ajaxData - 파일 데이터가 포함된 FormData 객체.
         * @param {Function} [callback] - 요청 성공시 호출될 콜백 함수.
         * @param {boolean} [continueBlock] - 추가적인 블록 UI 동작 여부 (선택적).
         */
        multipart: async function (url: string, ajaxData: FormData, callback?: (response: any) => any, continueBlock?: string): Promise<void> {
            const option: Record<string, any> = {
                url: url,
                type: 'POST',
                data: ajaxData,
                dataType: 'json',
                cache: false,
                processData: false,
                contentType: false
            };
            cF.$ajax.request(option, callback, continueBlock);
        }
    }
})();
// 인증만료/접근불가로 ajax 실패시 로그인 페이지로 이동 또는 머무르기 (선택)
(function($: JQueryStatic): void {
    $.ajaxSetup({
        error: function(xhr): void {
            const statusCode: number = xhr.status;
            const msg = xhr.responseJSON?.message || Message.get("view.error.access-denied");
            const lgnFormUrl: string =  "/auth/lgnForm.do";

            switch(statusCode) {
                case 401: {
                    cF.ui.swalOrConfirm(msg + "\n" + Message.get("view.auth.redirect-to-lgn-form"), function(): void {
                        window.location.href = lgnFormUrl;
                    }, function(): void {
                        // do nothing... ui에 세션 만료 표시
                        if ($(".session-expired-message").length > 0) return;
                        const $navbar: JQuery<HTMLElement> = $("#kt_app_header_wrapper .app-navbar");
                        const errorMsg: string = Message.get("view.auth.expired");       // "로그인 세션이 만료되었습니다."
                        const sessionExpiredText: JQuery<HTMLElement> = $(`<div class='d-flex align-items-center fs-4 fw-bold text-danger blink me-5'>${errorMsg}</div>`);
                        $navbar.before(sessionExpiredText);
                    });
                    return;
                }
                case 403: {
                    cF.ui.swalOrAlert(Message.get("view.error.forbidden"), function(): void {
                        window.location.href = lgnFormUrl;
                    });
                    return;
                }
                case 400: {
                    const errorLines: string[] = msg.split("\n");
                    let errorField: string, defaultMessage: string;

                    // spring errorMessage 에서 필드와 메세지 추출
                    errorLines.forEach((line: string): void => {
                        const fieldErrorMatch: RegExpMatchArray = line.match(/Field error in object '([^']+)' on field '([^']+)':/);
                        const defaultMessageMatch: RegExpMatchArray = line.match(/\]; default message \[([^\[\]]+)\]$/);
                        if (fieldErrorMatch) errorField = fieldErrorMatch[2];
                        if (defaultMessageMatch) defaultMessage  = defaultMessageMatch[1]; // 0보다 커야 합니다
                        if (errorField && defaultMessage) {
                            const errorMsg: string = errorField + ": " + defaultMessage + ".";
                            const snakeFieldName = cF.util.toSnakeCase(errorField);
                            const elmts = (cF.util.verifySelector("[name=\"" + snakeFieldName + "\"]"));
                            console.log(snakeFieldName);
                            if (elmts.length === 0) {
                                // alert
                                cF.ui.swalOrAlert(errorMsg);
                            } else {
                                // validation 요소 아래 error_valid_span에 내용 표시
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
            }
            // 기본 오류 로그 추가
            console.error("ajax error: ", xhr);
            cF.ui.swalOrAlert(msg);
        }
    });
})(jQuery);