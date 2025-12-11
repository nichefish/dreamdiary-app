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
            cF.handlebars.modal(obj, "jrnl_entry_reg", ["header"]);

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
            const postNoElmt: HTMLInputElement = document.querySelector("#jrnlEntryRegForm [name='postNo']") as HTMLInputElement;
            const postNo: string = postNoElmt?.value;
            const isMdf: boolean = !!postNo;
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

                            const isCalendar: boolean = Page?.calendar != null;
                            if (isCalendar) {
                                Page.refreshEventList();
                                dF.JrnlEntryTag.listAjax();     // 태그 refresh
                            } else {
                                if (dF.JrnlEntry.inKeywordSearchMode) {
                                    dF.JrnlEntry.keywordListAjax();
                                } else {
                                    dF.JrnlDay.yyMnthListAjax();
                                    dF.JrnlEntryTag.listAjax();     // 태그 refresh
                                }
                            }
                            // TODO: 결산 페이지에서 처리시도 처리해 줘야 한다.
                            cF.ui.unblockUI();

                            /* modal history pop */
                            ModalHistory.reset();
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

                            const isCalendar: boolean = Page?.calendar != null;
                            if (isCalendar) {
                                Page.refreshEventList();
                                dF.JrnlEntryTag.listAjax();     // 태그 refresh
                            } else {
                                if (dF.JrnlEntry.inKeywordSearchMode) {
                                    dF.JrnlEntry.keywordListAjax();
                                } else {
                                    dF.JrnlDay.yyMnthListAjax();
                                    dF.JrnlEntryTag.listAjax();     // 태그 refresh
                                }
                            }

                            /* modal history pop */
                            ModalHistory.reset();
                        });
                }, "block");
            });
        },

        /**
         * toggle
         * @param {string|number} postNo - 글 번호.
         */
        toggle: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            const id: string = String(postNo);
            const entry: HTMLElement = document.querySelector(`.jrnl-entry[data-id='${id}']`);
            const diaries: NodeListOf<HTMLElement> = entry.querySelectorAll(".jrnl-diary-cn");
            if (!diaries.length) return;

            // collapsed 상태 판정 → diary 중 하나라도 펴져 있으면 전체 접기
            const shouldCollapse = Array.from(diaries).some(item => {
                const cn = item.querySelector(".cn");
                return cn && !cn.classList.contains("collapsed");
            });

            const entryIcon: HTMLElement = document.querySelector(`#entry-toggle-icon-${id}`);
            if (!entryIcon) console.log("entryIcon not found.");
            const collapsedIds = new Set(JSON.parse(localStorage.getItem(dF.JrnlDiary.STORAGE_KEY) || "[]"));

            diaries.forEach((item: HTMLElement): void => {
                const id: string = item.dataset.id;
                const content: HTMLElement = item.querySelector(".cn");
                const icon: HTMLElement = item.querySelector('#diary-toggle-icon');
                if (!icon) console.log("icon not found.");

                if (!id || !content) return;

                if (shouldCollapse) {
                    // 전체 접기
                    content.classList.add("collapsed");
                    icon?.classList.replace("bi-chevron-up", "bi-chevron-down");
                    entryIcon?.classList.replace("bi-arrows-collapse", "bi-arrows-expand");
                    collapsedIds.add(id);
                } else {
                    // 전체 펼치기
                    content.classList.remove("collapsed");
                    icon?.classList.replace("bi-chevron-down", "bi-chevron-up");
                    entryIcon?.classList.replace("bi-arrows-expand", "bi-arrows-collapse");
                    collapsedIds.delete(id);
                }
            });

            localStorage.setItem(dF.JrnlDiary.STORAGE_KEY, JSON.stringify(Array.from(collapsedIds)));
        },
        
        /**
         * 접힌 엔트리 초기화
         */
        initCollapseState: function(): void {
            const entries: NodeListOf<HTMLElement> = document.querySelectorAll('.jrnl-entry');
            if (!entries.length) return;

            entries.forEach((entry: HTMLElement): void => {
                const entryId = entry.dataset.id;
                if (!entryId) return;

                const entryIcon: HTMLElement = document.querySelector(`#entry-toggle-icon-${entryId}`);
                if (!entryIcon) return;

                const diaries: NodeListOf<HTMLElement> = entry.querySelectorAll(".jrnl-diary-cn");
                if (!diaries.length) return;

                // diary 중 하나라도 "펼쳐진" 상태가 있는지 확인
                const anyExpanded: boolean = Array.from(diaries).some((diary: HTMLElement): boolean => {
                    const content: HTMLElement = diary.querySelector(".cn");
                    return content && !content.classList.contains("collapsed");
                });

                if (anyExpanded) {
                    // diary 중 하나라도 펼쳐져 있으면 → entry 아이콘 = 'collapse'
                    entryIcon.classList.replace("bi-arrows-expand", "bi-arrows-collapse");
                } else {
                    // diary가 모두 collapsed → entry 아이콘 = 'expand'
                    entryIcon.classList.replace("bi-arrows-collapse", "bi-arrows-expand");
                }
            });
        }
    }
})();