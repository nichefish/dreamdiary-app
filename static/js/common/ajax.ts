/**
 * ajax.ts
 * 공통 - ajax 관련 함수 모듈
 *
 * @namespace: cF.ajax (노출식 모듈 패턴)
 * @author: nichefish
 */
// @ts-ignore
if (typeof cF === 'undefined') { var cF = {} as any; }
cF.ajax = (function(): Module {
    return {
        /**
         * initializes module.
         */
        init: function(): void {
            console.log("'cF.ajax' module initialized.");
        },

        /**
         * blockUI를 적용한 AJAX 요청 (공통).
         * @param {string} url - 요청할 URL.
         * @param {RequestInit} options - fetch 요청에 대한 설정 옵션.
         * @param {Function} [callback] - 요청 성공 시 호출될 콜백 함수.
         * @param {boolean} [continueBlock] - 추가적인 블록 UI 동작 여부 (선택적).
         */
        request: async function(url: string, options: RequestInit, callback?: (response: any) => any, continueBlock: string = 'none'): Promise<void> {
            cF.ui.blockUI();  // UI 차단

            // fetch 요청
            const response: Response = await fetch(url, options);

            // 응답이 성공적이지 않으면 에러 처리
            if (!response.ok) throw new Error(`HTTP Error: ${response.status}`);

            // 응답 데이터 파싱
            const res: JSON = await response.json();

            // 성공 콜백 함수 호출
            if (typeof callback === 'function') callback(res);

            if (continueBlock !== 'block') cF.ui.unblockUI();  // UI 차단 해제
        },

        /**
         * blockUI를 적용한 AJAX 호출.
         * @param {string} url - 요청할 URL.
         * @param {Record<string, any>} ajaxData - JSON 형태의 요청 데이터.
         * @param {Function} [callback] - 요청 성공시 호출될 콜백 함수.
         * @param {boolean} [continueBlock] - 추가적인 블록 UI 동작 여부 (선택적).
         */
        get: async function (url: string, ajaxData: Record<string, any>, callback?: (response: any) => any, continueBlock?: string): Promise<void> {
            // URL에 query string을 추가 (ajaxData는 쿼리 파라미터로 변환)
            const absoluteUrl: string = cF.util.getAbsoluteUrl(url);  // 상대 경로를 절대 경로로 변환
            const urlWithParams: URL = new URL(absoluteUrl);
            ajaxData = ajaxData ?? {};
            Object.keys(ajaxData).forEach(
                (key: string) => urlWithParams.searchParams.append(key, String(ajaxData[key]))
            );

            const options: RequestInit = {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'  // AJAX 요청을 명시적으로 지정
                },
                body: null, // GET 요청이므로 body는 필요 없음
            };

            await cF.ajax.request(urlWithParams.toString(), options, callback, continueBlock);
        },

        /**
         * blockUI를 적용한 AJAX 호출.
         * @param {string} url - 요청할 URL.
         * @param {Record<string, any>} ajaxData - JSON 형태의 요청 데이터.
         * @param {Function} [callback] - 요청 성공시 호출될 콜백 함수.
         * @param {boolean} [continueBlock] - 추가적인 블록 UI 동작 여부 (선택적).
         */
        post: async function (url: string, ajaxData: Record<string, any>, callback?: (response: any) => any, continueBlock?: string): Promise<void> {
            const options: RequestInit = {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(ajaxData), // JSON 형태로 데이터를 POST
            };

            await cF.ajax.request(url, options, callback, continueBlock);
        },

        /**
         * blockUI를 적용한 AJAX 호출 (Multipart).
         * @param {string} url - 요청할 URL.
         * @param {FormData} ajaxData - 파일 데이터가 포함된 FormData 객체.
         * @param {Function} [callback] - 요청 성공시 호출될 콜백 함수.
         * @param {boolean} [continueBlock] - 추가적인 블록 UI 동작 여부 (선택적).
         */
        multipart: async function (url: string, ajaxData: FormData, callback?: (response: any) => any, continueBlock?: string): Promise<void> {
            const options: RequestInit = {
                method: 'POST',
                body: ajaxData,  // FormData 객체를 body에 전달
            };

            await cF.ajax.request(url, options, callback, continueBlock);
        },
    }
})();
// 인증만료/접근불가로 ajax 실패시 로그인 페이지로 이동 또는 머무르기 (선택)
// 기존 fetch를 가로채서 override
(function (): void {
    const originalFetch = window.fetch;

    window.fetch = async function (url: RequestInfo, options: RequestInit = {}): Promise<Response> {
        const defaultOptions: RequestInit = {
            headers: {
                'Content-Type': 'application/json',
            },
            ...options,
        };

        try {
            // UI 차단
            cF.ui.blockUI();

            // 기존 fetch 수행
            const response: Response = await originalFetch(url, defaultOptions);

            // 응답이 성공적이지 않으면 에러 처리
            if (!response.ok) await handleError(response);

            return response;
        } catch (error: any) {
            console.error('Ajax request failed:', error);
            const msg: string = error.message || Message.get("view.error.request");         // "요청에 실패했습니다."
            cF.ui.swalOrAlert(msg);
            throw error;
        } finally {
            // UI 차단 해제
            cF.ui.unblockUI();
        }
    };

    const handleError = async (response: Response): Promise<void> => {
        const statusCode: number = response.status;
        const msg: string = await response.json().catch(() => Message.get("view.error.access-denied"));      // "접근이 거부되었습니다. (ACCESS DENIED)"
        const lgnFormUrl: string = "/auth/lgnForm.do";

        switch(statusCode) {
            case 401: {
                cF.ui.swalOrConfirm(msg + "\n" + Message.get("view.auth.redirect-to-lgn-form"), function(): void {
                    window.location.href = lgnFormUrl;
                }, function(): void {
                    if (!document.querySelector(".session-expired-message")) {
                        // do nothing... ui에 세션 만료 표시
                        const navbar: HTMLElement = document.querySelector("#kt_app_header_wrapper .app-navbar");
                        const sessionExpiredText: HTMLDivElement = document.createElement("div");
                        sessionExpiredText.className = "d-flex align-items-center fs-4 fw-bold text-danger blink me-5";
                        sessionExpiredText.textContent = Message.get("view.auth.expired");     // "로그인 세션이 만료되었습니다."
                        navbar?.insertAdjacentElement('beforebegin', sessionExpiredText);
                    }
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
                if (errorLines.length === 0) return;

                // spring errorMessage 에서 필드와 메세지 추출
                errorLines.forEach((line: string): void => {
                    const fieldErrorMatch: RegExpMatchArray = line.match(/Field error in object '([^']+)' on field '([^']+)':/);
                    const defaultMessageMatch: RegExpMatchArray = line.match(/]; default message \[([^\[\]]+)]$/);
                    if (fieldErrorMatch && defaultMessageMatch) {
                        const errorFieldNm: string = fieldErrorMatch[2];
                        const defaultMessage: string = defaultMessageMatch[1];
                        const errorMsg: string = `${errorFieldNm}: ${defaultMessage}.`;
                        const errorFieldSnakeName: string = cF.format.toSnakeCase(errorFieldNm);
                        const elmt: HTMLInputElement|null = document.querySelector(`[name='${errorFieldSnakeName}']`);
                        if (elmt) {
                            const errorSpan: HTMLElement = document.querySelector(`#${elmt.id}_validate_span`);
                            if (errorSpan) {
                                errorSpan.classList.add("text-danger");
                                errorSpan.textContent = errorMsg;
                            }
                            elmt.focus();
                        } else {
                            cF.ui.swalOrAlert(errorMsg);
                        }
                    }
                });
                return;
            }
        }
        // 기본 오류 로그 추가
        console.error("ajax error: ", response);
        cF.ui.swalOrAlert(msg);
    };
})();