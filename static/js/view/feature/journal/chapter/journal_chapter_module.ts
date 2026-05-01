/**
 * journal_chapter_module.ts
 * 저널 챕터 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.JournalChapter = (function(): dfModule {
    return {
        STORAGE_KEY: "collapsedJournalChapterIds",

        initialized: false,
        inKeywordSearchMode: false,
        tagify: null,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.JournalChapter.initialized) return;

            /* initialize submodules. */
            dF.JournalChapterTag.init();

            dF.JournalChapter.initialized = true;
            console.log("'dF.JournalChapter' module initialized.");
        },

        /**
         * form init
         * @param {Record<string, any>} obj - 폼에 바인딩할 데이터
         */
        initForm: function(obj: Record<string, any> = {}): void {
            /* show modal */
            cF.handlebars.modal(obj, "journal_chapter_reg");

            /* jquery validation */
            cF.validate.validateForm("#journalChapterRegForm", dF.JournalChapter.regAjax);
        },

        /**
         * 등록 모달 호출
         * @param {Object} param - 파라미터 객체
         * @param {string|number} param.journalDayId - 저널 일자 번호.
         * @param {string} param.stdrdDt - 기준 날짜.
         * @param {string} param.journalDateWeekDay - 기준 날짜 요일.
         */
        regModal: function({
            journalDayId,
            stdrdDt,
            journalDateWeekDay,
            chapterType
        }: {
            journalDayId: string | number;
            stdrdDt: string;
            journalDateWeekDay: string;
            chapterType?: string;
        }): void {
            if (isNaN(Number(journalDayId))) return;

            const obj: Record<string, any> = {
                journalDayId: journalDayId,
                stdrdDt: stdrdDt,
                journalDateWeekDay: journalDateWeekDay,
                chapterType: chapterType ?? "DIARY"
            };
            /* initialize form. */
            dF.JournalChapter.initForm(obj);
        },

        /**
         * form submit
         */
        submit: function(): void {
            $("#journalChapterRegForm").submit();
        },

        /**
         * 등록 (Ajax)
         */
        regAjax: function(): void {
            const id: string = cF.util.getInputValue("#journalChapterRegForm [name='id']");
            const isMdf: boolean = cF.util.isNotEmpty(id);
            Swal.fire({
                text: Message.get(isMdf ? "view.cnfm.mdf" : "view.cnfm.reg"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = isMdf ? cF.util.bindUrl(Url.JOURNAL_CHAPTER, { id }) : Url.JOURNAL_CHAPTERS;
                const ajaxData: FormData = new FormData(document.getElementById("journalChapterRegForm") as HTMLFormElement);
                cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            dF.JournalDay.refresh();
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

            const url: string = cF.util.bindUrl(Url.JOURNAL_CHAPTER, { id });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const rsltObj: Record<string, any> = res.rsltObj;
                /* show modal */
                cF.handlebars.modal(rsltObj, "journal_chapter_dtl");

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

            const url: string = cF.util.bindUrl(Url.JOURNAL_CHAPTER, { id });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltObj } = res;
                /* initialize form. */
                dF.JournalChapter.initForm(rsltObj);

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

                const url: string = cF.util.bindUrl(Url.JOURNAL_CHAPTER, { id });
                cF.$ajax.delete(url, null, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            dF.JournalDay.refresh();
                        });
                }, "block");
            });
        },

        /**
         * 상태 토글 (Ajax)
         * @param id
         * @param stateKey
         * @param {object} object
         */
        toggleStateAjax: function(id: string|number, stateKey: string, { onOffFunc }): void {
            if (isNaN(Number(id))) return;

            const item = document.querySelector(`.journal-chapter-item[data-id='${id}']`) as HTMLElement;
            const cacheContext = dF.State.resolveJournalCacheContext(item);
            const payload = { id: id, contentType: "JOURNAL_CHAPTER", stateKey, cacheContext };
            dF.State.toggleAjax(payload, function(res: AjaxResponse): void {
                if (!item) return;
                const lowerStateKey: string = stateKey.toLowerCase();
                const icon: HTMLElement = item.querySelector(`.icon-${lowerStateKey}`);
                if (!icon) {
                    console.warn("icon not found.");
                } else {
                    icon?.classList.toggle("d-none", res.rsltSts !== "ON");
                }
                const chk: HTMLInputElement = item.querySelector(`.chapter-context-${lowerStateKey}-check`);
                if (!chk) {
                    console.warn("chk not found.");
                } else {
                    chk.checked = res.rsltSts === "ON";
                }
                if (stateKey === "COLLAPSED") {
                    item.dataset.collapsed = res.rsltSts === "ON" ? "Y" : "N";
                }
                const tagDiv: HTMLInputElement = item.querySelector(".journal-chapter-tags");
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
                const content: HTMLDivElement = item.querySelector("div.journal-chapter-content");
                if (!content) return console.warn("content not found.");

                content?.classList.toggle("collapsed", res.rsltSts === "ON");
            }
            this.toggleStateAjax(id, "COLLAPSED", { onOffFunc });
        },

        /**
         * toggle
         * @param {string|number} id - 글 번호.
         */
        toggle: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            const item = document.querySelector(`.journal-chapter-item[data-id='${id}']`) as HTMLElement;
            if (!item) return console.warn("item not found.");

            const content: HTMLDivElement = item.querySelector("div.journal-chapter-content");
            if (!content) return console.warn("content not found.");

            // collapsed 상태 판정 → diary 중 하나라도 펴져 있으면 전체 접기
            const shouldCollapse: boolean = content && !content.classList.contains("collapsed");
            const diaries: NodeListOf<HTMLElement> = item.querySelectorAll(".journal-diary-content");
            const tagDiv = item.querySelector(".journal-chapter-tags");
            const icon: HTMLElement | null = document.querySelector(`#chapter-toggle-icon-${id}`);
            if (!icon) console.warn("chapter toggle icon not found:", id);
            if (shouldCollapse) {
                // 전체 접기
                content.classList.add("collapsed");
                item.dataset.collapsed = "Y";
                icon?.classList.add("bi-arrows-expand");
                icon?.classList.remove("bi-arrows-collapse");
                tagDiv?.classList.remove("d-none");
                diaries.forEach((diary: HTMLElement): void => {
                    const inner: HTMLElement | null = diary.querySelector(".journal-content");
                    inner?.classList.add("collapsed");
                });
            } else {
                // 전체 펼치기
                content.classList.remove("collapsed");
                item.dataset.collapsed = "N";
                icon?.classList.add("bi-arrows-collapse");
                icon?.classList.remove("bi-arrows-expand");
                tagDiv?.classList.add("d-none");
                diaries.forEach((diary: HTMLElement): void => {
                    const inner: HTMLElement | null = diary.querySelector(".journal-content");
                    inner?.classList.remove("collapsed");
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

            const url: string = cF.util.bindUrl(Url.JOURNAL_CHAPTER, { id });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const rsltObj: Record<string, any> = res.rsltObj;
                const journalEntryList: any[] = Array.isArray(rsltObj?.journalEntryList)
                    ? rsltObj.journalEntryList
                    : (Array.isArray(rsltObj?.journalDiaryList) ? rsltObj.journalDiaryList : []);
                if (!Array.isArray(rsltObj?.journalEntryList) && !Array.isArray(rsltObj?.journalDiaryList)) {
                    console.warn("journalEntryList is missing or invalid. fallback to empty list.", rsltObj?.journalEntryList);
                }
                const { stdrdDt, journalDateWeekDay } = rsltObj;
                const date: string = stdrdDt + " (" + journalDateWeekDay + ")" + "\r\n";
                const resultCn: string = journalEntryList.map((item: any): string => "#" + (item?.sortOrder ?? "") + (item?.content ?? "")).join("\r\n");

                // 문단/줄바꿈을 먼저 텍스트로 치환
                const replacedCn: string = resultCn
                    .replace(/<\s*hr\b[^>]*\/?>/gi, "\n------\n")
                    .replace(/<\s*br\s*\/?>/gi, "\n")
                    .replace(/<\s*\/?p[^>]*>/gi, "\n");
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

            window.location.href = cF.util.bindUrl(Url.JOURNAL_CHAPTER_EXPORT, { id });
        }
    }
})();
