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

        savedYy: null,
        savedMnth: null,

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
            cF.ui.chckboxLabel("imprtcYn", "중요//해당없음", "red//gray");
            /* tagify */
            // dF.JrnlEntry.tagify = cF.tagify.initWithCtgr("#jrnlEntryRegForm #tagListStr", dF.JrnlEntryTag.ctgrMap);
        },

        /**
         * 목록 조회 (Ajax)
         */
        keywordListAjax: function(): void {
            const keyword: string = (document.querySelector("#jrnl_aside #entryKeyword") as HTMLInputElement)?.value;
            if (cF.util.isEmpty(keyword)) return;

            // 검색 시 기존 년월 저장
            dF.JrnlEntry.savedYy = $("#jrnl_aside #yy").val() as string;
            dF.JrnlEntry.savedMnth = $("#jrnl_aside #mnth").val() as string;

            const url: string =Url.JRNL_ENTRY_LIST_AJAX;
            const ajaxData: Record<string, any> = { "entryKeyword": keyword };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                $("#jrnl_aside #yy").val("");
                $("#jrnl_aside #mnth").val("");
                // 목록 영역 클리어
                $("#jrnl_aside #dreamKeyword").val("");
                $("#jrnl_day_list_div").empty();
                $("#jrnl_dream_list_div").empty();
                // 태그 헤더 클리어
                $("#jrnl_day_tag_list_div").empty();
                $("#jrnl_entry_tag_list_div").empty();
                $("#jrnl_dream_tag_list_div").empty();
                cF.ui.closeModal();
                cF.handlebars.template(res.rsltList, "jrnl_entry_list");
                dF.JrnlEntry.inKeywordSearchMode = true;
                // 버튼 추가
                $("#jrnl_aside #jrnl_entry_reset_btn").remove();
                const resetBtn = $(`<button type="button" id="jrnl_entry_reset_btn" class="btn btn-sm btn-outline btn-light-danger px-4" 
                                          onclick="dF.JrnlEntry.resetKeyword();" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                                          aria-label="항목 키워드 검색을 리셋합니다." 
                                          data-bs-original-title="항목 키워드 검색을 리셋합니다." data-kt-initialized="1">
                                     <i class="bi bi-x pe-0"></i>
                                  </button>`);
                $("#jrnl_aside #jrnl_entry_search_btn").after(resetBtn);
                resetBtn.tooltip();
            }, "block");
        },

        /**
         * 키워드 검색 종료
         */
        resetKeyword: function(): void {
            $("#jrnl_aside #jrnl_entry_reset_btn").remove();
            $("#jrnl_aside #yy").val(dF.JrnlEntry.savedYy);
            $("#jrnl_aside #mnth").val(dF.JrnlEntry.savedMnth);
            dF.JrnlDayAside.mnth();
        },

        /**
         * 등록 모달 호출
         * @param {string|number} jrnlDayNo - 저널 일자 번호.
         * @param {string} stdrdDt - 기준 날짜.
         * @param {string} jrnlDtWeekDay - 기준 날짜 요일.
         */
        regModal: function(jrnlDayNo: string|number, stdrdDt: string, jrnlDtWeekDay: string): void {
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
            const isReg: boolean = postNoElmt?.value === "";
            Swal.fire({
                text: Message.get(isReg ? "view.cnfm.reg" : "view.cnfm.mdf"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = isReg ? Url.JRNL_ENTRY_REG_AJAX : Url.JRNL_ENTRY_MDF_AJAX;
                const ajaxData: FormData = new FormData(document.getElementById("jrnlEntryRegForm") as HTMLFormElement);
                cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            const isCalendar: boolean = Page?.calendar !== undefined;
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

            const url: string = Url.JRNL_ENTRY_DTL_AJAX;
            const ajaxData: Record<string, any> = { "postNo" : postNo };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
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

            const url: string = Url.JRNL_ENTRY_DTL_AJAX;
            const ajaxData: Record<string, any> = { "postNo" : postNo };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
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

                const url: string = Url.JRNL_ENTRY_DEL_AJAX;
                const ajaxData: Record<string, any> = { "postNo": postNo };
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            const isCalendar: boolean = Page?.calendar !== undefined;
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

            const id = String(postNo);
            const item = document.querySelector(`.jrnl-entry-item[data-id='${id}']`);
            if (!item) return console.log("item not found.");

            const content = item.querySelector(".jrnl-entry-cn .cn") as HTMLElement;
            if (!content) return console.log("content not found.");

            const icon = item.querySelector(`#toggle-icon-${id}`) as HTMLElement;
            const collapsedIds = new Set(JSON.parse(localStorage.getItem(dF.JrnlEntry.STORAGE_KEY) || "[]"));

            const isCollapsed = content.classList.contains("collapsed");
            if (isCollapsed) {
                content.classList.remove("collapsed");
                icon?.classList.replace("bi-chevron-down", "bi-chevron-up");
                collapsedIds.delete(id);
            } else {
                content.classList.add("collapsed");
                icon?.classList.replace("bi-chevron-up", "bi-chevron-down");
                collapsedIds.add(id);
            }

            localStorage.setItem(dF.JrnlEntry.STORAGE_KEY, JSON.stringify(Array.from(collapsedIds)));
        },

        /**
         * 접힌 항목 초기화
         */
        initCollapseState: function(): void {
            const collapsedIds = new Set(JSON.parse(localStorage.getItem(dF.JrnlEntry.STORAGE_KEY) || "[]"));
            document.querySelectorAll(".jrnl-entry-item .cn").forEach(item => {
                const el = item as HTMLElement;
                const id = el.dataset.id;
                const content = el.querySelector(".jrnl-entry-cn");
                const icon = el.querySelector(`#toggle-icon-${id}`);
                if (id && collapsedIds.has(id)) {
                    content?.classList.add("collapsed");
                    icon?.classList.replace("bi-chevron-up", "bi-chevron-down");
                }
            });
        }
    }
})();