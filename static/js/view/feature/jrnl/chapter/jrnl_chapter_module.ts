/**
 * jrnl_chapter_module.ts
 * 저널 챕터 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.JrnlChapter = (function(): dfModule {
    return {
        STORAGE_KEY: "collapsedJrnlChapterIds",

        initialized: false,
        inKeywordSearchMode: false,
        tagify: null,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.JrnlChapter.initialized) return;

            /* initialize submodules. */
            dF.JrnlChapterTag.init();

            dF.JrnlChapter.initialized = true;
            console.log("'dF.JrnlChapter' module initialized.");
        },

        /**
         * form init
         * @param {Record<string, any>} obj - 폼에 바인딩할 데이터
         */
        initForm: function(obj: Record<string, any> = {}): void {
            /* show modal */
            cF.handlebars.modal(obj, "jrnl_chapter_reg");

            /* jquery validation */
            cF.validate.validateForm("#jrnlChapterRegForm", dF.JrnlChapter.regAjax);
        },

        /**
         * 등록 모달 호출
         * @param {Object} param - 파라미터 객체
         * @param {string|number} param.jrnlDayId - 저널 일자 번호.
         * @param {string} param.stdrdDt - 기준 날짜.
         * @param {string} param.jrnlDtWeekDay - 기준 날짜 요일.
         */
        regModal: function({ jrnlDayId, stdrdDt, jrnlDtWeekDay }: { jrnlDayId: string | number; stdrdDt: string; jrnlDtWeekDay: string; }): void {
            if (isNaN(Number(jrnlDayId))) return;

            const obj: Record<string, any> = { jrnlDayId: jrnlDayId, stdrdDt: stdrdDt, jrnlDtWeekDay: jrnlDtWeekDay };
            /* initialize form. */
            dF.JrnlChapter.initForm(obj);
        },

        /**
         * form submit
         */
        submit: function(): void {
            $("#jrnlChapterRegForm").submit();
        },

        /**
         * 등록 (Ajax)
         */
        regAjax: function(): void {
            const id: string = cF.util.getInputValue("#jrnlChapterRegForm [name='id']");
            const isMdf: boolean = cF.util.isNotEmpty(id);
            Swal.fire({
                text: Message.get(isMdf ? "view.cnfm.mdf" : "view.cnfm.reg"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = isMdf ? cF.util.bindUrl(Url.JRNL_CHAPTER, { id }) : Url.JRNL_CHAPTERS;
                const ajaxData: FormData = new FormData(document.getElementById("jrnlChapterRegForm") as HTMLFormElement);
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
         * @param {string|number} id - 글 번호.
         */
        dtlModal: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            // 기존에 열린 모달이 있으면 닫기
            const openModals: NodeList = document.querySelectorAll('.modal.show'); // 열린 모달을 찾기
            openModals.forEach((modal: Node): void => {
                $(modal).modal('hide');  // 각각의 모달을 닫기
            });

            const self = this;
            const func: string = arguments.callee.name; // 현재 실행 중인 함수 참조
            const args: any[] = Array.from(arguments); // 함수 인자 배열로 받기

            const url: string = cF.util.bindUrl(Url.JRNL_CHAPTER, { id });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const rsltObj: Record<string, any> = res.rsltObj;
                /* show modal */
                cF.handlebars.modal(rsltObj, "jrnl_chapter_dtl");

                /* modal history push */
                ModalHistory.push(self, func, args);
            });
        },

        /**
         * 수정 모달 호출
         * @param {string|number} id - 글 번호.
         */
        mdfModal: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            // 기존에 열린 모달이 있으면 닫기
            const openModals: NodeList = document.querySelectorAll('.modal.show'); // 열린 모달을 찾기
            openModals.forEach((modal: Node): void => {
                $(modal).modal('hide');  // 각각의 모달을 닫기
            });

            const self = this;
            const func: string = arguments.callee.name; // 현재 실행 중인 함수 참조
            const args: any[] = Array.from(arguments); // 함수 인자 배열로 받기

            const url: string = cF.util.bindUrl(Url.JRNL_CHAPTER, { id });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltObj } = res;
                /* initialize form. */
                dF.JrnlChapter.initForm(rsltObj);

                /* modal history push */
                ModalHistory.push(self, func, args);
            });
        },

        /**
         * 삭제 (Ajax)
         * @param {string|number} id - 글 번호.
         */
        delAjax: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = cF.util.bindUrl(Url.JRNL_CHAPTER, { id });
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
         * @param id
         * @param stateCd
         * @param {object} object
         */
        toggleStateAjax: function(id: string|number, stateCd: string, { onOffFunc }): void {
            if (isNaN(Number(id))) return;

            const item = document.querySelector(`.jrnl-chapter-item[data-id='${id}']`) as HTMLElement;
            const cacheContext = dF.State.resolveJrnlCacheContext(item);
            const payload = { id: id, contentType: "JRNL_CHAPTER", stateCd, cacheContext };
            dF.State.toggleAjax(payload, function(res: AjaxResponse): void {
                if (!item) return;
                const lowerStateCd: string = stateCd.toLowerCase();
                const icon: HTMLElement = item.querySelector(`.icon-${lowerStateCd}`);
                if (!icon) {
                    console.warn("icon not found.");
                } else {
                    icon?.classList.toggle("d-none", res.rsltSts !== "ON");
                }
                const chk: HTMLInputElement = item.querySelector(`.chapter-context-${lowerStateCd}-check`);
                if (!chk) {
                    console.warn("chk not found.");
                } else {
                    chk.checked = res.rsltSts === "ON";
                }
                const tagDiv: HTMLInputElement = item.querySelector(".jrnl-chapter-tags");
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
         * @param {string|number} id - 글 번호.
         */
        collapseAjax: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            const onOffFunc = function(res: AjaxResponse, item: HTMLElement): void {
                const cn: HTMLDivElement = item.querySelector("div.jrnl-chapter-cn");
                if (!cn) return console.warn("cn not found.");

                cn?.classList.toggle("collapsed", res.rsltSts === "ON");
            }
            this.toggleStateAjax(id, "COLLAPSED", { onOffFunc });
        },

        /**
         * toggle
         * @param {string|number} id - 글 번호.
         */
        toggle: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            const item = document.querySelector(`.jrnl-chapter-item[data-id='${id}']`) as HTMLElement;
            if (!item) return console.warn("item not found.");

            const cn: HTMLDivElement = item.querySelector("div.jrnl-chapter-cn");
            if (!cn) return console.warn("cn not found.");

            // collapsed 상태 판정 → diary 중 하나라도 펴져 있으면 전체 접기
            const shouldCollapse: boolean = cn && !cn.classList.contains("collapsed");
            const diaries: NodeListOf<HTMLElement> = item.querySelectorAll(".jrnl-diary-cn");
            const tagDiv = item.querySelector(".jrnl-chapter-tags");
            const icon: HTMLElement = document.querySelector(`#chapter-toggle-icon-${id}`);
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
         * @param {string|number} id - 글 번호.
         * @deprecated
         */
        copy: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            const url: string = cF.util.bindUrl(Url.JRNL_CHAPTER, { id });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const rsltObj: Record<string, any> = res.rsltObj;
                const jrnlDiaryList: object[] = rsltObj.jrnlDiaryList;
                const { stdrdDt, jrnlDtWeekDay } = rsltObj;
                const date: string = stdrdDt + " (" + jrnlDtWeekDay + ")" + "\r\n";
                const resultCn: string = jrnlDiaryList?.map((item: any): any => "#" + (item?.idx ?? "") + (item?.cn ?? "")).join("\r\n");

                // 문단/줄바꿈을 먼저 텍스트로 치환
                const replacedCn: string = resultCn.replace(/<\s*br\s*\/?>/gi, "\n").replace(/<\s*\/?p[^>]*>/gi, "\n");
                const div: HTMLDivElement = document.createElement("div");
                div.innerHTML = date + replacedCn;
                const textToCopy: string = (div.innerText ?? "")
                    .replace(/\n+/g, "\n")
                    .replace(/\n/g, "\r\n")
                    .trim();

                if (navigator.clipboard && window.isSecureContext) {
                    navigator.clipboard.writeText(textToCopy)
                        .then((): void => {
                            Swal.fire({ icon: "success", text: "클립보드에 복사되었습니다.", timer: 1500, showConfirmButton: false  });
                        })
                        .catch((): void => {
                            cF.util.legacyCopy(textToCopy);
                        });
                } else {
                    cF.util.legacyCopy(textToCopy);
                }
            });
        },

        /**
         * 검색 결과 txt 다운로드
         */
        exportTxt: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            window.location.href = cF.util.bindUrl(Url.JRNL_CHAPTER_EXPORT, { id });
        }
    }
})();
