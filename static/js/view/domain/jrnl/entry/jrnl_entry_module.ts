/**
 * jrnl_entry_module.ts
 * 저널 항목 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.JrnlEntry = (function(): dfModule {
    return {
        STORAGE_KEY: "collapsedJrnlEntryIds",

        initialized: false,
        inKeywordSearchMode: false,
        tagify: null,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.JrnlEntry.initialized) return;

            /* initialize submodules. */
            dF.JrnlEntryTag.init();

            dF.JrnlEntry.initialized = true;
            console.log("'dF.JrnlEntry' module initialized.");
        },

        /**
         * form init
         * @param {Record<string, any>} obj - 폼에 바인딩할 데이터
         */
        initForm: function(obj: Record<string, any> = {}): void {
            /* show modal */
            cF.handlebars.modal(obj, "jrnl_entry_reg");

            /* jquery validation */
            cF.validate.validateForm("#jrnlEntryRegForm", dF.JrnlEntry.regAjax);
            // checkbox init
            cF.ui.chckboxLabel("#jrnlEntryRegForm #imprtcYn", "중요//해당없음", "red//gray");
            /* tagify */
            // dF.JrnlEntry.tagify = cF.tagify.initWithCtgr("#jrnlEntryRegForm #tagListStr", dF.JrnlEntryTag.ctgrMap);
        },

        /**
         * 등록 모달 호출
         * @param {Object} param - 파라미터 객체
         * @param {string|number} param.jrnlDayNo - 저널 일자 번호.
         * @param {string} param.stdrdDt - 기준 날짜.
         * @param {string} param.jrnlDtWeekDay - 기준 날짜 요일.
         */
        regModal: function({ jrnlDayNo, stdrdDt, jrnlDtWeekDay }: { jrnlDayNo: string | number; stdrdDt: string; jrnlDtWeekDay: string; }): void {
            if (isNaN(Number(jrnlDayNo))) return;

            const obj: Record<string, any> = { jrnlDayNo: jrnlDayNo, stdrdDt: stdrdDt, jrnlDtWeekDay: jrnlDtWeekDay };
            /* initialize form. */
            dF.JrnlEntry.initForm(obj);
        },

        /**
         * form submit
         */
        submit: function(): void {
            $("#jrnlEntryRegForm").submit();
        },

        /**
         * 등록 (Ajax)
         */
        regAjax: function(): void {
            const postNo: string = cF.util.getInputValue("#jrnlEntryRegForm [name='postNo']");
            const isMdf: boolean = cF.util.isNotEmpty(postNo);
            Swal.fire({
                text: Message.get(isMdf ? "view.cnfm.mdf" : "view.cnfm.reg"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = isMdf ? cF.util.bindUrl(Url.JRNL_ENTRY, { postNo }) : Url.JRNL_ENTRIES;
                const ajaxData: FormData = new FormData(document.getElementById("jrnlEntryRegForm") as HTMLFormElement);
                cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            dF.JrnlDay.refresh();
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

            const url: string = cF.util.bindUrl(Url.JRNL_ENTRY, { postNo });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const rsltObj: Record<string, any> = res.rsltObj;
                /* show modal */
                cF.handlebars.modal(rsltObj, "jrnl_entry_dtl");

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

            const url: string = cF.util.bindUrl(Url.JRNL_ENTRY, { postNo });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltObj } = res;
                /* initialize form. */
                dF.JrnlEntry.initForm(rsltObj);

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

                const url: string = cF.util.bindUrl(Url.JRNL_ENTRY, { postNo });
                cF.$ajax.delete(url, null, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            dF.JrnlDay.refresh();
                        });
                }, "block");
            });
        },

        /**
         * 상태 토글 (Ajax)
         * @param postNo
         * @param stateCd
         * @param {object} object
         */
        toggleStateAjax: function(postNo: string|number, stateCd: string, { onOffFunc }): void {
            if (isNaN(Number(postNo))) return;

            const cacheContext = { yy: cF.util.getUrlParam("yy"), mnth: cF.util.getUrlParam("mnth") };
            const payload = { postNo, contentType: "JRNL_ENTRY", stateCd, cacheContext };
            dF.State.toggleAjax(payload, function(res: AjaxResponse): void {
                const item = document.querySelector(`.jrnl-entry-item[data-id='${postNo}']`) as HTMLElement;
                if (!item) return;
                const lowerStateCd: string = stateCd.toLowerCase();
                const icon: HTMLElement = item.querySelector(`.icon-${lowerStateCd}`);
                if (!icon) {
                    console.warn("icon not found.");
                } else {
                    icon?.classList.toggle("d-none", res.rsltSts !== "ON");
                }
                const chk: HTMLInputElement = item.querySelector(`.entry-context-${lowerStateCd}-check`);
                if (!chk) {
                    console.warn("chk not found.");
                } else {
                    chk.checked = res.rsltSts === "ON";
                }
                const tagDiv: HTMLInputElement = item.querySelector(".jrnl-entry-tags");
                if (!tagDiv) {
                    console.warn("tagDiv not found.");
                } else {
                    tagDiv.classList.toggle("d-none", res.rsltSts !== "ON");
                }
                onOffFunc(res, item);
            });
        },

        /**
         * 글 접기/펼치기 토글. (Ajax)
         * @param {string|number} postNo - 글 번호.
         */
        collapseAjax: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            const onOffFunc = function(res: AjaxResponse, item: HTMLElement): void {
                const cn: HTMLDivElement = item.querySelector("div.jrnl-entry-cn");
                if (!cn) return console.warn("cn not found.");

                cn?.classList.toggle("collapsed", res.rsltSts === "ON");
            }
            this.toggleStateAjax(postNo, "COLLAPSED", { onOffFunc });
        },

        /**
         * toggle
         * @param {string|number} postNo - 글 번호.
         */
        toggle: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            const item = document.querySelector(`.jrnl-entry-item[data-id='${postNo}']`) as HTMLElement;
            if (!item) return console.warn("item not found.");

            const cn: HTMLDivElement = item.querySelector("div.jrnl-entry-cn");
            if (!cn) return console.warn("cn not found.");

            // collapsed 상태 판정 → diary 중 하나라도 펴져 있으면 전체 접기
            const shouldCollapse: boolean = cn && !cn.classList.contains("collapsed");
            const diaries: NodeListOf<HTMLElement> = item.querySelectorAll(".jrnl-diary-cn");
            const tagDiv = item.querySelector(".jrnl-entry-tags");
            const icon: HTMLElement = document.querySelector(`#entry-toggle-icon-${postNo}`);
            if (!icon) console.log("icon not found.");
            if (shouldCollapse) {
                // 전체 접기
                cn.classList.add("collapsed");
                icon.classList.add("bi-arrows-expand");
                icon.classList.remove("bi-arrows-collapse");
                tagDiv.classList.remove("d-none");
                diaries.forEach((diary: HTMLElement): void => {
                    const content: HTMLElement = diary.querySelector(".cn");
                    content.classList.add("collapsed");
                });
            } else {
                // 전체 펼치기
                cn.classList.remove("collapsed");
                icon.classList.add("bi-arrows-collapse");
                icon.classList.remove("bi-arrows-expand");
                tagDiv.classList.add("d-none");
                diaries.forEach((diary: HTMLElement): void => {
                    const content: HTMLElement = diary.querySelector(".cn");
                    content.classList.remove("collapsed");
                });
            }
        },

        /**
         * copy
         * @param {string|number} postNo - 글 번호.
         * @deprecated
         */
        copy: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            const url: string = cF.util.bindUrl(Url.JRNL_ENTRY, { postNo });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const rsltObj: Record<string, any> = res.rsltObj;
                const jrnlDiaryList: object[] = rsltObj.jrnlDiaryList;
                const resultCn: string = jrnlDiaryList?.map((item: any): any => "#" + (item?.idx ?? "") + (item?.cn ?? "")).join("\r\n");

                // 문단/줄바꿈을 먼저 텍스트로 치환
                const replacedCn: string = resultCn.replace(/<\s*br\s*\/?>/gi, "\n").replace(/<\s*\/?p[^>]*>/gi, "\n");
                const div: HTMLDivElement = document.createElement("div");
                div.innerHTML = replacedCn;
                const textToCopy: string = (div.innerText ?? "")
                    .replace(/\n+/g, "\n")
                    .replace(/\n/g, "\r\n")
                    .trim();

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