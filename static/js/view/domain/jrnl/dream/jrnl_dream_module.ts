/**
 * jrnl_dream_module.ts
 * 저널 꿈 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.JrnlDream = (function(): dfModule {
    return {
        STORAGE_KEY: "collapsedJrnlDreamIds",

        initialized: false,
        inKeywordSearchMode: false,
        tagify: null,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.JrnlDream.initialized) return;

            /* initialize submodules. */
            dF.JrnlDreamTag.init();

            dF.JrnlDream.initialized = true;
            console.log("'dF.JrnlDream' module initialized.");
        },

        /**
         * form init
         * @param {Record<string, any>} obj - 폼에 바인딩할 데이터
         */
        initForm: function(obj: Record<string, any> = {}): void {
            /* show modal */
            cF.handlebars.modal(obj, "jrnl_dream_reg", ["header"]);

            /* jquery validation */
            cF.validate.validateForm("#jrnlDreamRegForm", dF.JrnlDream.regAjax, {
                rules: {
                    elseDreamerNm: {
                        required: function() {
                            return $("#jrnlDreamRegForm #elseDreamYn").prop(":checked", false);
                        }
                    },
                },
                ignore: undefined
            });
            // 체크박스 상태 변경시 필드 재검증
            $("#elseDreamYn").change(function(): void {
                $("#elseDreamerNm").valid();
            });
            // checkbox init
            cF.ui.chckboxLabel("#jrnlDreamRegForm #resolvedYn", "정리완료//정리중", "green//gray");
            cF.ui.chckboxLabel("#jrnlDreamRegForm #imprtcYn", "중요//해당없음", "red//gray");
            cF.ui.chckboxLabel("#jrnlDreamRegForm #nhtmrYn", "악몽//해당없음", "red//gray");
            cF.ui.chckboxLabel("#jrnlDreamRegForm #hallucYn", "입면환각//해당없음", "blue//gray");
            cF.ui.chckboxLabel("#jrnlDreamRegForm #elseDreamYn", "해당//미해당", "blue//gray", function(): void {
                $("#elseDreamerNmDiv").removeClass("d-none");
            }, function(): void {
                $("#elseDreamerNmDiv").addClass("d-none");
            });
            /* tinymce editor reset */
            cF.tinymce.init('#tinymce_jrnlDreamCn');
            cF.tinymce.setContentWhenReady("tinymce_jrnlDreamCn", obj.cn || "");
            /* tagify */
            dF.JrnlDream.tagify = cF.tagify.initWithCtgr("#jrnlDreamRegForm #tagListStr", dF.JrnlDreamTag.ctgrMap);
        },

        /**
         * 목록 조회 (Ajax)
         */
        keywordListAjax: function(): void {
            const keyword: string = (document.querySelector("#jrnl_aside #dreamKeyword") as HTMLInputElement)?.value;
            if (cF.util.isEmpty(keyword)) return;

            const url: string = Url.JRNL_DREAMS;
            const ajaxData: Record<string, any> = { "dreamKeyword": keyword };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                $("#jrnl_aside #yy").val("");
                $("#jrnl_aside #mnth").val("");
                // 목록 영역 클리어
                $("#jrnl_aside #diaryKeyword").val("");
                $("#jrnl_day_list_div").empty();
                $("#jrnl_diary_list_div").empty();
                // 태그 헤더 클리어
                $("#jrnl_day_tag_list_div").empty();
                $("#jrnl_diary_tag_list_div").empty();
                $("#jrnl_dream_tag_list_div").empty();
                cF.ui.closeModal();
                cF.handlebars.template(res.rsltList, "jrnl_dream_list");
                dF.JrnlDream.inKeywordSearchMode = true;
                // 버튼 추가
                $("#jrnl_aside #jrnl_dream_reset_btn").remove();
                const resetBtn = $(`<button type="button" id="jrnl_dream_reset_btn" class="btn btn-sm btn-outline btn-light-danger px-4" 
                                          onclick="dF.JrnlDream.resetKeyword();" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                                          aria-label="꿈 키워드 검색을 리셋합니다." 
                                          data-bs-original-title="꿈 키워드 검색을 리셋합니다." data-kt-initialized="1">
                                     <i class="bi bi-x pe-0"></i>
                                  </button>`);
                $("#jrnl_aside #jrnl_dream_search_btn").after(resetBtn);
                resetBtn.tooltip();
            }, "block");
        },

        /**
         * 키워드 검색 종료
         */
        resetKeyword: function(): void {
            $("#jrnl_aside #jrnl_dream_reset_btn").remove();
            dF.JrnlDayAside.mnth();
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
            dF.JrnlDream.initForm(obj);
        },

        /**
         * form submit
         */
        submit: function(): void {
            tinymce.get("tinymce_jrnlDreamCn").save();
            $("#jrnlDreamRegForm").submit();
        },

        /**
         * 등록 (Ajax)
         */
        regAjax: function(): void {
            const postNoElmt: HTMLInputElement = document.querySelector("#jrnlDreamRegForm [name='postNo']");
            const postNo: string = postNoElmt?.value;
            const isMdf: boolean = !!postNo;
            Swal.fire({
                text: Message.get(isMdf ? "view.cnfm.mdf" : "view.cnfm.reg"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = isMdf ? cF.util.bindUrl(Url.JRNL_DREAM, { postNo }) : Url.JRNL_DREAMS;
                const ajaxData: FormData = new FormData(document.getElementById("jrnlDreamRegForm") as HTMLFormElement);
                cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            const isCalendar: boolean = Page?.calendar != null;
                            if (isCalendar) {
                                Page.refreshEventList();
                                dF.JrnlDreamTag.listAjax();     // 태그 refresh
                            } else {
                                if (dF.JrnlDream.inKeywordSearchMode) {
                                    dF.JrnlDream.keywordListAjax();
                                } else {
                                    dF.JrnlDay.yyMnthListAjax();
                                    dF.JrnlDreamTag.listAjax();         // 태그 refresh
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

            const url: string = cF.util.bindUrl(Url.JRNL_DREAM, { postNo });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const rsltObj: Record<string, any> = res.rsltObj;
                /* show modal */
                cF.handlebars.modal(rsltObj, "jrnl_dream_dtl");

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

            const url: string = cF.util.bindUrl(Url.JRNL_DREAM, { postNo });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltObj } = res;
                /* initialize form. */
                dF.JrnlDream.initForm(rsltObj);

                /* modal history push */
                ModalHistory.push(self, func, args);
            });
        },

        /**
         * 상태 변경 처리. (Ajax)
         * @param {string|number} postNo - 글 번호.
         * @param {object} payload
         * @param {Function} [callback]
         */
        patchAjax: function(postNo: string|number, payload: object, callback: Function): void {
            if (isNaN(Number(postNo))) return;

            const url: string = cF.util.bindUrl(Url.JRNL_DREAM, { postNo });
            cF.$ajax.patch(url, payload, function(res: AjaxResponse): void {
                if (!res.rslt) return;

                if (!callback || typeof callback != "function") return;

                callback(res);
            }, "block");
        },

        /**
         * 정리완료 처리. (Ajax)
         * @param {string|number} postNo - 글 번호.
         */
        resolveAjax: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            const item: HTMLElement = document.querySelector(`.jrnl-dream-item[data-id='${postNo}']`);
            if (!item) return;

            const current: string = (item.dataset.resolved || "N").toUpperCase();
            const next = current === "Y" ? "N" : "Y";
            const nextBoolean = current !== "Y"

            const payload: Record<string, any> = { resolved: nextBoolean, collapsed: nextBoolean };
            dF.JrnlDream.patchAjax(postNo, payload, function() {
                item.dataset.resolved = next;
                item.dataset.collapsed = next;

                const content: HTMLElement = item.querySelector(".cn");
                if (content) {
                    content.classList.toggle("collapsed", next === "Y");
                }
                const chk: HTMLInputElement = item.querySelector(".dream-context-collapse-check");
                if (chk) chk.checked = (next === "Y");
            });
        },

        /**
         * 글 접기/펼치기 토글. (Ajax)
         * @param {string|number} postNo - 글 번호.
         */
        collapseAjax: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            const item: HTMLElement = document.querySelector(`.jrnl-dream-item[data-id='${postNo}']`);
            if (!item) return;

            const current: string = (item.dataset.collapsed || "N").toUpperCase();
            const next = current === "Y" ? "N" : "Y";
            const nextBoolean = current !== "Y"

            const payload: Record<string, any> = { collapsed: nextBoolean };
            dF.JrnlDream.patchAjax(postNo, payload, function() {
                item.dataset.collapsed = next;

                const content: HTMLElement = item.querySelector(".cn");
                if (content) {
                    content.classList.toggle("collapsed", next === "Y");
                }
                const chk: HTMLInputElement = item.querySelector(".dream-context-collapse-check");
                if (chk) chk.checked = (next === "Y");
            });
        },

        /**
         * 중요여부 토글. (Ajax)
         * @param {string|number} postNo - 글 번호.
         */
        imprtcAjax: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            const item: HTMLElement = document.querySelector(`.jrnl-dream-item[data-id='${postNo}']`);
            if (!item) return;

            const current: string = (item.dataset.imprtc || "N").toUpperCase();
            const next = current === "Y" ? "N" : "Y";
            const nextBoolean = current !== "Y"

            const payload: Record<string, any> = { imprtc: nextBoolean };
            dF.JrnlDream.patchAjax(postNo, payload, function() {
                item.dataset.imprtc = next;

                const content: HTMLElement = item.querySelector(".cn");
                if (content) {
                    content.classList.toggle("collapsed", next === "Y");
                }
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

                const url: string = cF.util.bindUrl(Url.JRNL_DREAM, { postNo });
                cF.$ajax.delete(url, null, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            const isCalendar: boolean = Page?.calendar != null;
                            if (isCalendar) {
                                Page.refreshEventList();
                                dF.JrnlDreamTag.listAjax();     // 태그 refresh
                            } else {
                                if (dF.JrnlDream.inKeywordSearchMode) {
                                    dF.JrnlDream.keywordListAjax();
                                } else {
                                    dF.JrnlDay.yyMnthListAjax();
                                    dF.JrnlDreamTag.listAjax();     // 태그 refresh
                                }
                            }

                            /* modal history pop */
                            ModalHistory.reset();
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

            const url: string = Url.JRNL_DREAM_SET_COLLAPSE_AJAX;
            const ajaxData: Record<string, any> = { postNo, collapsedYn };
            cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) return;

                // 찾아서 해당 그것만 collapse 추가 제거.
                const item: HTMLElement = document.querySelector(`.jrnl-dream-cn[data-id='${postNo}']`);
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
         */
        toggle: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            const id: string = String(postNo);
            const item: HTMLElement = document.querySelector(`.jrnl-dream-cn[data-id='${id}']`);
            if (!item) return console.log("item not found.");

            const content: HTMLElement = item.querySelector(".jrnl-dream-cn .cn");
            if (!content) return console.log("content not found.");

            const icon: HTMLElement = document.querySelector(`#dream-toggle-icon-${id}`);
            if (!icon) console.log("icon not found.");
            const collapsedIds = new Set(JSON.parse(localStorage.getItem(dF.JrnlDream.STORAGE_KEY) || "[]"));

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

            localStorage.setItem(dF.JrnlDream.STORAGE_KEY, JSON.stringify(Array.from(collapsedIds)));
        },

        /**
         * 접힌 꿈 초기화
         */
        initCollapseState: function(): void {
            const collapsedIds = new Set(JSON.parse(localStorage.getItem(dF.JrnlDream.STORAGE_KEY) || "[]"));
            document.querySelectorAll(".jrnl-dream-item .jrnl-dream-cn").forEach((item: HTMLElement): void => {
                const id: string = item.dataset.id;
                const content: HTMLElement = item.querySelector(".cn");
                const icon: HTMLElement = document.querySelector(`#dream-toggle-icon-${id}`);
                if (!icon) console.log("icon not found.");
                if (id && collapsedIds.has(id)) {
                    content?.classList.add("collapsed");
                    icon?.classList.replace("bi-chevron-up", "bi-chevron-down");
                }
            });
        }
    }
})();