/**
 * jrnl_intrpt_module.ts
 * 저널 항목 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.JrnlIntrpt = (function(): dfModule {
    return {
        STORAGE_KEY: "collapsedJrnlIntrptIds",

        initialized: false,
        inKeywordSearchMode: false,
        tagify: null,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.JrnlIntrpt.initialized) return;

            /* initialize submodules. */
            dF.JrnlIntrptTag.init();

            dF.JrnlIntrpt.initialized = true;
            console.log("'dF.JrnlIntrpt' module initialized.");
        },

        /**
         * form init
         * @param {Record<string, any>} obj - 폼에 바인딩할 데이터
         */
        initForm: function(obj: Record<string, any> = {}): void {
            /* show modal */
            cF.handlebars.modal(obj, "jrnl_intrpt_reg", ["header"]);

            /* jquery vali  dation */
            cF.validate.validateForm("#jrnlIntrptRegForm", dF.JrnlIntrpt.regAjax);
            // checkbox init
            cF.ui.chckboxLabel("#jrnlIntrptRegForm #resolvedYn", "정리완료//정리중", "green//gray");
            cF.ui.chckboxLabel("#jrnlIntrptRegForm #imprtcYn", "중요//해당없음", "red//gray");
            /* tinymce editor reset */
            cF.tinymce.init('#tinymce_jrnlIntrptCn');
            cF.tinymce.setContentWhenReady("tinymce_jrnlIntrptCn", obj.cn || "");
            /* tagify */
            dF.JrnlIntrpt.tagify = cF.tagify.initWithCtgr("#jrnlIntrptRegForm #tagListStr", dF.JrnlIntrptTag.ctgrMap);
        },

        /**
         * 등록 모달 호출
         * @param {Object} param - 파라미터 객체
         * @param {string|number} param.jrnlDayNo - 저널 일자 번호.
         * @param {string|number} param.jrnlDreamNo - 저널 꿈 번호.
         * @param {string} param.stdrdDt - 기준 날짜.
         * @param {string} param.jrnlDtWeekDay - 기준 날짜 요일.
         */
        regModal: function({ jrnlDayNo, jrnlDreamNo, stdrdDt, jrnlDtWeekDay }: { jrnlDayNo: string | number; jrnlDreamNo: string | number; stdrdDt: string; jrnlDtWeekDay: string; }): void {
            if (isNaN(Number(jrnlDayNo))) return;

            const obj: Record<string, any> = { jrnlDayNo: jrnlDayNo, jrnlDreamNo: jrnlDreamNo, stdrdDt: stdrdDt, jrnlDtWeekDay: jrnlDtWeekDay };
            /* initialize form. */
            dF.JrnlIntrpt.initForm(obj);
        },

        /**
         * form submit
         */
        submit: function(): void {
            tinymce.get("tinymce_jrnlIntrptCn").save();
            $("#jrnlIntrptRegForm").submit();
        },

        /**
         * 등록 (Ajax)
         */
        regAjax: function(): void {
            const postNo: string = cF.util.getInputValue("#jrnlIntrptRegForm [name='postNo']");
            const isMdf: boolean = cF.util.isNotEmpty(postNo);
            Swal.fire({
                text: Message.get(isMdf ? "view.cnfm.mdf" : "view.cnfm.reg"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = isMdf ? cF.util.bindUrl(Url.JRNL_INTRPT, { postNo }) : Url.JRNL_INTRPTS;
                const ajaxData: FormData = new FormData(document.getElementById("jrnlIntrptRegForm") as HTMLFormElement);
                cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            dF.JrnlDay.refresh();
                            dF.JrnlIntrptTag.listAjax();     // 태그 refresh
                        });
                }, "block");
            });
        },

        /**
         * 상세 모달 호출
         * @param {string|number} postNo - 글 번호.
         */
        dtlModal: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            // 기존에 열린 모달이 있으면 닫기
            const openModals: NodeList = document.querySelectorAll('.modal.show'); // 열린 모달을 찾기
            openModals.forEach((modal: Node): void => {
                $(modal).modal('hide');  // 각각의 모달을 닫기
            });

            const self = this;
            const func: string = arguments.callee.name; // 현재 실행 중인 함수 참조
            const args: any[] = Array.from(arguments); // 함수 인자 배열로 받기

            const url: string = cF.util.bindUrl(Url.JRNL_INTRPT, { postNo });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const rsltObj: Record<string, any> = res.rsltObj;
                /* show modal */
                cF.handlebars.modal(rsltObj, "jrnl_intrpt_dtl");

                /* modal history push */
                ModalHistory.push(self, func, args);
            });
        },

        /**
         * 수정 모달 호출
         * @param {string|number} postNo - 글 번호.
         */
        mdfModal: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            // 기존에 열린 모달이 있으면 닫기
            const openModals: NodeList = document.querySelectorAll('.modal.show'); // 열린 모달을 찾기
            openModals.forEach((modal: Node): void => {
                $(modal).modal('hide');  // 각각의 모달을 닫기
            });

            const self = this;
            const func: string = arguments.callee.name; // 현재 실행 중인 함수 참조
            const args: any[] = Array.from(arguments); // 함수 인자 배열로 받기

            const url: string = cF.util.bindUrl(Url.JRNL_INTRPT, { postNo });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltObj } = res;
                /* initialize form. */
                dF.JrnlIntrpt.initForm(rsltObj);

                /* modal history push */
                ModalHistory.push(self, func, args);
            });
        },

        /**
         * 삭제 (Ajax)
         * @param {string|number} postNo - 글 번호.
         */
        delAjax: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = cF.util.bindUrl(Url.JRNL_INTRPT, { postNo });
                cF.$ajax.delete(url, null, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            dF.JrnlDay.refresh();
                            dF.JrnlIntrptTag.listAjax();     // 태그 refresh
                        });
                }, "block");
            });
        },

        /**
         * @param {string|number} postNo - 글 번호.
         * @param {'Y'|'N'} collapsedYn - 글접기 여부.
         */
        collapse: function(postNo: string|number, collapsedYn: 'Y'|'N'): void {
            if (isNaN(Number(postNo))) return;

            const url: string = Url.JRNL_INTRPT_SET_COLLAPSE_AJAX;
            const ajaxData: Record<string, any> = { postNo, collapsedYn };
            cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) return;

                // 찾아서 해당 그것만 collapse 추가 제거.
                const item: HTMLElement = document.querySelector(`.jrnl-intrpt-cn[data-id='${postNo}']`);
                if (!item) return console.log("item not found.");

                const content: HTMLElement = item.querySelector(".cn");
                if (!content) return console.log("content not found.");

                if (collapsedYn === "Y") {
                    content.classList.add("collapsed");
                } else {
                    content.classList.remove("collapsed");
                }
            }, "block");
        },

        /**
         * toggle
         * @param {string|number} postNo - 글 번호.
         * @param {HTMLElement} trigger - 클릭 버튼 객체
         */
        toggle: function(postNo: string|number, trigger: HTMLElement): void {
            if (isNaN(Number(postNo))) return;

            const id: string = String(postNo);
            const item: HTMLElement = trigger.closest(`.jrnl-intrpt-item[data-id='${id}']`);
            if (!item) return console.log("item not found.");

            const content: HTMLElement = item.querySelector(".jrnl-intrpt-cn .cn");
            if (!content) return console.log("content not found.");

            const icon: HTMLElement = document.querySelector(`#intrpt-toggle-icon-${id}`);
            if (!icon) console.log("icon not found.");
            const collapsedIds = new Set(JSON.parse(localStorage.getItem(dF.JrnlIntrpt.STORAGE_KEY) || "[]"));

            const isCollapsed: boolean = content.classList.contains("collapsed");
            if (isCollapsed) {
                content.classList.remove("collapsed");
                icon?.classList.replace("bi-chevron-down", "bi-chevron-up");
                collapsedIds.delete(id);
            } else {
                content.classList.add("collapsed");
                icon?.classList.replace("bi-chevron-up", "bi-chevron-down");
                collapsedIds.add(id);
            }

            localStorage.setItem(dF.JrnlIntrpt.STORAGE_KEY, JSON.stringify(Array.from(collapsedIds)));
        },
        
        /**
         * 접힌 엔트리 초기화
         */
        initCollapseState: function(): void {
            const collapsedIds = new Set(JSON.parse(localStorage.getItem(dF.JrnlIntrpt.STORAGE_KEY) || "[]"));
            document.querySelectorAll(".jrnl-intrpt-item .jrnl-intrpt-cn").forEach((item: HTMLElement): void => {
                const id: string = item.dataset.id;
                const content: HTMLElement = item.querySelector(".cn");
                const icon: HTMLElement = document.querySelector(`#intrpt-toggle-icon-${id}`);
                if (!icon) console.log("icon not found.");
                if (id && collapsedIds.has(id)) {
                    content?.classList.add("collapsed");
                    icon?.classList.replace("bi-chevron-up", "bi-chevron-down");
                }
            });
        },

        /**
         * copy
         * @param {string|number} postNo - 글 번호.
         * @deprecated
         */
        copy: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            const url: string = cF.util.bindUrl(Url.JRNL_INTRPT, { postNo });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const rsltObj: Record<string, any> = res.rsltObj;
                const resultCn: string = rsltObj.cn;
                // 문단/줄바꿈을 먼저 텍스트로 치환
                const replacedCn = resultCn.replace(/<\s*br\s*\/?>/gi, "\n").replace(/<\s*\/?p\s*>/gi, "\n");
                const div: HTMLDivElement = document.createElement("div");
                div.innerHTML = replacedCn;
                const textToCopy: string = (div.textContent ?? "").replace(/\n+/g, "\n").trim();

                if (navigator.clipboard && window.isSecureContext) {
                    navigator.clipboard.writeText(textToCopy)
                        .then((): void => {
                            Swal.fire({ icon: "success", text: "클립보드에 복사되었습니다." });
                        })
                        .catch((): void => {
                            cF.util.legacyCopy(textToCopy);
                        });
                } else {
                    cF.util.legacyCopy(textToCopy);
                }
            });
        },
    }
})();