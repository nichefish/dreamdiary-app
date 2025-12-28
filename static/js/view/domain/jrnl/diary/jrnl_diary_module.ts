/**
 * jrnl_diary_module.ts
 * 저널 일기 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.JrnlDiary = (function(): dfModule {
    return {
        STORAGE_KEY: "collapsedJrnlDiaryIds",

        initialized: false,
        inKeywordSearchMode: false,
        tagify: null,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.JrnlDiary.initialized) return;

            /* initialize submodules. */
            dF.JrnlDiaryTag.init();

            dF.JrnlDiary.initialized = true;
            console.log("'dF.JrnlDiary' module initialized.");
        },

        /**
         * form init
         * @param {Record<string, any>} obj - 폼에 바인딩할 데이터
         */
        initForm: function(obj: Record<string, any> = {}): void {
            /* show modal */
            cF.handlebars.modal(obj, "jrnl_diary_reg", ["header"]);

            /* jquery validation */
            cF.validate.validateForm("#jrnlDiaryRegForm", dF.JrnlDiary.regAjax);
            /* tinymce editor reset */
            cF.tinymce.init('#tinymce_jrnlDiaryCn');
            cF.tinymce.setContentWhenReady("tinymce_jrnlDiaryCn", obj.cn || "");
            /* tagify */
            dF.JrnlDiary.tagify = cF.tagify.initWithCtgr("#jrnlDiaryRegForm #tagListStr", dF.JrnlDiaryTag.ctgrMap);
        },

        /**
         * 목록 조회 (Ajax)
         */
        keywordListAjax: function(): void {
            const keyword: string = (document.querySelector("#jrnl_aside #diaryKeyword") as HTMLInputElement)?.value;
            if (cF.util.isEmpty(keyword)) return;

            const url: string = Url.JRNL_DIARIES;
            const ajaxData: Record<string, any> = { "diaryKeyword": keyword };
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
                $("#jrnl_diary_tag_list_div").empty();
                $("#jrnl_dream_tag_list_div").empty();
                cF.ui.closeModal();
                cF.handlebars.template(res.rsltList, "jrnl_diary_list");
                dF.JrnlDiary.inKeywordSearchMode = true;
                // 버튼 추가
                $("#jrnl_aside #jrnl_diary_reset_btn").remove();
                const resetBtn = $(`<button type="button" id="jrnl_diary_reset_btn" class="btn btn-sm btn-outline btn-light-danger px-4" 
                                          onclick="dF.JrnlDiary.resetKeyword();" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                                          aria-label="일기 키워드 검색을 리셋합니다." 
                                          data-bs-original-title="일기 키워드 검색을 리셋합니다." data-kt-initialized="1">
                                     <i class="bi bi-x pe-0"></i>
                                  </button>`);
                $("#jrnl_aside #jrnl_diary_search_btn").after(resetBtn);
                resetBtn.tooltip();
            }, "block");
        },

        /**
         * 키워드 검색 종료
         */
        resetKeyword: function(): void {
            $("#jrnl_aside #jrnl_diary_reset_btn").remove();
            dF.JrnlDayAside.mnth();
        },

        /**
         * 등록 모달 호출
         * @param {Object} param - 파라미터 객체
         * @param {string|number} param.jrnlDayNo - 저널 일자 번호.
         * @param {string|number} param.jrnlEntryNo - 저널 항목 번호.
         * @param {string} param.stdrdDt - 기준 날짜.
         * @param {string} param.jrnlDtWeekDay - 기준 날짜 요일.
         */
        regModal: function({ jrnlDayNo, jrnlEntryNo, stdrdDt, jrnlDtWeekDay }: { jrnlDayNo: string | number; jrnlEntryNo: string | number; stdrdDt: string; jrnlDtWeekDay: string; }): void {
            if (isNaN(Number(jrnlEntryNo))) return;

            const url: string = cF.util.bindUrl(Url.JRNL_DAY, { postNo: jrnlDayNo });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) return;
                const entryList = res.rsltObj.entryList;
                const obj: Record<string, any> = { jrnlDayNo: jrnlDayNo, jrnlEntryNo: jrnlEntryNo, stdrdDt: stdrdDt, jrnlDtWeekDay: jrnlDtWeekDay, entryList: entryList };
                /* initialize form. */
                dF.JrnlDiary.initForm(obj);
            });
        },

        /**
         * form submit
         */
        submit: function(): void {
            tinymce.get("tinymce_jrnlDiaryCn").save();
            $("#jrnlDiaryRegForm").submit();
        },

        /**
         * 등록 (Ajax)
         */
        regAjax: function(): void {
            const postNo: string = cF.util.getInputValue("#jrnlDiaryRegForm [name='postNo']");
            const isMdf: boolean = cF.util.isNotEmpty(postNo);
            Swal.fire({
                text: Message.get(isMdf ? "view.cnfm.mdf" : "view.cnfm.reg"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = isMdf ? cF.util.bindUrl(Url.JRNL_DIARY, { postNo }) : Url.JRNL_DIARIES;
                const ajaxData: FormData = new FormData(document.getElementById("jrnlDiaryRegForm") as HTMLFormElement);
                cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                    dF.JrnlDay.refresh();
                            dF.JrnlDiaryTag.listAjax();     // 태그 refresh
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

            const url: string = cF.util.bindUrl(Url.JRNL_DIARY, { postNo });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const rsltObj: Record<string, any> = res.rsltObj;
                /* show modal */
                cF.handlebars.modal(rsltObj, "jrnl_diary_dtl");

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

            const url: string = cF.util.bindUrl(Url.JRNL_DIARY, { postNo });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltObj } = res;
                const url: string = cF.util.bindUrl(Url.JRNL_DAY, { postNo: rsltObj.jrnlDayNo });
                cF.ajax.get(url, null, function(res: AjaxResponse): void {
                    if (!res.rslt) return;
                    const entryList = res.rsltObj.entryList;
                    const obj: Record<string, any> = { ...rsltObj, entryList: entryList };
                    /* initialize form. */
                    dF.JrnlDiary.initForm(obj);

                    /* modal history push */
                    ModalHistory.push(self, func, args);
                });
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

                const url: string = cF.util.bindUrl(Url.JRNL_DIARY, { postNo });
                cF.$ajax.delete(url, null, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            dF.JrnlDay.refresh();
                            dF.JrnlDiaryTag.listAjax();     // 태그 refresh
                        });
                }, "block");
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

            const url: string = cF.util.bindUrl(Url.JRNL_DIARY, { postNo });
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

            const item: HTMLElement = document.querySelector(`.jrnl-diary-item[data-id='${postNo}']`);
            if (!item) return;

            const current: string = (item.dataset.resolved || "N").toUpperCase();
            const next: "Y"|"N" = current === "Y" ? "N" : "Y";
            const nextBoolean: boolean = next === "Y"

            const payload: Record<string, any> = { resolved: nextBoolean, collapsed: nextBoolean };
            dF.JrnlDiary.patchAjax(postNo, payload, function() {
                item.dataset.resolved = next;
                item.dataset.collapsed = next;

                const content: HTMLElement = item.querySelector(".cn");
                if (content) {
                    content.classList.toggle("collapsed", next === "Y");
                }
                const chk: HTMLInputElement = item.querySelector(".diary-context-collapse-check");
                if (chk) chk.checked = (next === "Y");
            });
        },

        /**
         * 글 접기/펼치기 토글. (Ajax)
         * @param {string|number} postNo - 글 번호.
         */
        collapseAjax: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            const item: HTMLElement = document.querySelector(`.jrnl-diary-item[data-id='${postNo}']`);
            if (!item) return;

            const current: string = (item.dataset.collapsed || "N").toUpperCase();
            const next: "Y"|"N" = current === "Y" ? "N" : "Y";
            const nextBoolean: boolean = next === "Y"

            const payload: Record<string, any> = { collapsed: nextBoolean };
            dF.JrnlDiary.patchAjax(postNo, payload, function(): void {
                item.dataset.collapsed = next;

                const content: HTMLElement = item.querySelector(".cn");
                if (content) {
                    content.classList.toggle("collapsed", next === "Y");
                }
                const chk: HTMLInputElement = item.querySelector(".diary-context-collapse-check");
                if (chk) chk.checked = (next === "Y");
            });
        },

        /**
         * 중요여부 토글. (Ajax)
         * @param {string|number} postNo - 글 번호.
         */
        imprtcAjax: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            const item: HTMLElement = document.querySelector(`.jrnl-diary-item[data-id='${postNo}']`);
            if (!item) return;

            const current: string = (item.dataset.imprtc || "N").toUpperCase();
            const next: "Y"|"N" = current === "Y" ? "N" : "Y";
            const nextBoolean: boolean = next === "Y"

            const payload: Record<string, any> = { imprtc: nextBoolean };
            dF.JrnlDiary.patchAjax(postNo, payload, function(): void {
                item.dataset.imprtc = next;

                const cn: HTMLDivElement = item.querySelector("div.jrnl-diary-cn");
                if (!cn) return console.warn("cn not found.");
                const titleWrap: HTMLElement = cn.querySelector("div.title-wrap");
                if (!titleWrap) return console.warn("titleWrap not found.");
                const existing: HTMLDivElement = titleWrap.querySelector(".ctgr-imprtc");
                if (nextBoolean) {
                    if (existing) return;

                    const imprtcWrap: HTMLDivElement = document.createElement("div");
                    imprtcWrap.className = "ctgr-span ctgr-imprtc w-60px d-flex-center";
                    imprtcWrap.innerText = "!중요";
                    // 첫 번째 요소로 삽입
                    titleWrap.prepend(imprtcWrap);
                    cn.classList.add("bg-secondary");
                } else {
                    if (existing) existing.remove();
                    cn.classList.remove("bg-secondary");
                }
            });
        },

        /**
         * toggle
         * @param {string|number} postNo - 글 번호.
         * @param {HTMLElement} trigger - 클릭 버튼 객체
         */
        toggle: function(postNo: string|number, trigger: HTMLElement): void {
            if (isNaN(Number(postNo))) return;

            const id: string = String(postNo);
            const item: HTMLElement = trigger.closest(`.jrnl-diary-item[data-id='${id}']`);
            if (!item) return console.log("item not found.");

            const content: HTMLElement = item.querySelector(".jrnl-diary-cn .cn");
            if (!content) return console.log("content not found.");

            const icon: HTMLElement = item.querySelector('.diary-toggle-icon');
            if (!icon) console.log("icon not found.");

            const isCollapsed: boolean = content.classList.contains("collapsed");
            if (isCollapsed) {
                content.classList.remove("collapsed");
                icon?.classList.replace("bi-chevron-down", "bi-chevron-up");
            } else {
                content.classList.add("collapsed");
                icon?.classList.replace("bi-chevron-up", "bi-chevron-down");
            }
        },

        /**
         * copy
         * @param {string|number} postNo - 글 번호.
         */
        copy: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            const url: string = cF.util.bindUrl(Url.JRNL_DIARY, { postNo });
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