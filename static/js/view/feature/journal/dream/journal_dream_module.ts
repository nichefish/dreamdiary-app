/**
 * journal_dream_module.ts
 * 저널 꿈 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.JournalDream = (function(): dfModule {
    return {
        STORAGE_KEY: "collapsedJournalDreamIds",
        PROFILE: {
            LIST: {
                collapsed: true,
            },
            TAG: {
                collapsed: false,
            },
            SEARCH: {
                collapsed: false,
            },
            ANNUAL: {
                collapsed: false,
            }
        },

        profile: null,
        initialized: false,
        initPromise: null,
        inKeywordSearchMode: false,
        tagify: null,

        /**
         * initializes module.
         * @param {"LIST"|"CAL"|"DAILY"|"WEEKLY"|"SEARCH"} viewType
         * @return Promise<void>
         */
        init: async function(viewType: "LIST"|"CAL"|"DAILY"|"WEEKLY"|"SEARCH"): Promise<void> {
            if (this.initPromise) return this.initPromise;

            /* initialize modules. */
            this.initPromise = (async () => {
                await dF.JournalDreamTag.init();
                this.viewType = viewType;
                this.initialized = true;
                console.log("'dF.JournalDiary' module initialized.");
            })();

            return this.initPromise;
        },

        /**
         * refresh
         */
        refresh: function(): void {
            switch (this.viewType) {
                case "LIST":
                    dF.JournalDay.yyMnthListAjax();
                    dF.JournalDreamTag.listAjax();     // 태그 refresh
                    break;
                case "CAL":
                    Page.refreshEventList();
                    dF.JournalDreamTag.listAjax();     // 태그 refresh
                    break;
                case "DAILY":
                case "WEEKLY":
                    dF.JournalDay.refresh();
                    break;
                case "SEARCH":
                    location.reload();
                    break;
            }
            cF.ui.unblockUI();
        },

        /**
         * form init
         * @param {Record<string, any>} obj - 폼에 바인딩할 데이터
         */
        initForm: function(obj: Record<string, any> = {}): void {
            /* show modal */
            cF.handlebars.modal(obj, "journal_dream_reg", ["header"]);

            /* jquery validation */
            cF.validate.validateForm("#journalDreamRegForm", dF.JournalDream.regAjax, {
                rules: {
                    elseDreamerNm: {
                        required: function() {
                            return $("#journalDreamRegForm #elseDreamYn").prop(":checked", false);
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
            cF.ui.chckboxLabel("#journalDreamRegForm #resolvedYn", "정리완료//정리중", "green//gray");
            cF.ui.chckboxLabel("#journalDreamRegForm #imprtcYn", "중요//해당없음", "red//gray");
            cF.ui.chckboxLabel("#journalDreamRegForm #elseDreamYn", "해당//미해당", "blue//gray", function(): void {
                $("#elseDreamerNmDiv").removeClass("d-none");
            }, function(): void {
                $("#elseDreamerNmDiv").addClass("d-none");
            });
            /* tinymce editor reset */
            cF.tinymce.init('#tinymce_journalDreamCn');
            cF.tinymce.setContentWhenReady("tinymce_journalDreamCn", obj.content || "");
            /* tagify */
            dF.JournalDream.tagify = cF.tagify.initWithCtgr("#journalDreamRegForm #tagListStr", dF.JournalDreamTag.ctgrMap);
        },

        resolveDreamChapterList: function(day: Record<string, any> = {}): Record<string, any>[] {
            const chapterList: Record<string, any>[] = Array.isArray(day?.journalChapterList) ? day.journalChapterList : [];
            return chapterList.filter((chapter: Record<string, any>): boolean => chapter?.chapterType === "DREAM");
        },

        createDreamChapterAndOpenModal: function(
            journalDayId: string | number,
            stdrdDt: string,
            journalDateWeekDay: string,
            onReady?: () => void
        ): void {
            const ajaxData: FormData = new FormData();
            ajaxData.append("journalDayId", String(journalDayId));

            cF.$ajax.multipart(Url.JOURNAL_CHAPTER_DREAM_AUTO, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }

                dF.JournalDream.openRegModalWithDayContext(
                    journalDayId,
                    res?.rsltObj?.id,
                    stdrdDt,
                    journalDateWeekDay,
                    onReady
                );
            }, "block");
        },

        openRegModalWithDayContext: function(
            journalDayId: string | number,
            journalChapterId: string | number | undefined,
            stdrdDt: string,
            journalDateWeekDay: string,
            onReady?: () => void
        ): void {
            const url: string = cF.util.bindUrl(Url.JOURNAL_DAY, { id: journalDayId });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) return;

                const chapterList: Record<string, any>[] = dF.JournalDream.resolveDreamChapterList(res.rsltObj);
                if (chapterList.length === 0) {
                    dF.JournalDream.createDreamChapterAndOpenModal(journalDayId, stdrdDt, journalDateWeekDay, onReady);
                    return;
                }

                const resolvedChapterId: number = chapterList.some((chapter: Record<string, any>): boolean => {
                    return Number(chapter?.id) === Number(journalChapterId);
                })
                    ? Number(journalChapterId)
                    : Number(chapterList[0]?.id);

                const obj: Record<string, any> = {
                    journalDayId,
                    journalChapterId: resolvedChapterId,
                    stdrdDt,
                    journalDateWeekDay,
                    chapterList
                };
                dF.JournalDream.initForm(obj);
                onReady?.();
            });
        },

        /**
         * 키워드 검색 팝업 호출
         */
        searchPopup: function(): void {
            const keyword: string = (document.querySelector("#dreamSearchKeyword") as HTMLInputElement)?.value;
            const url: string = `${Url.JOURNAL_DREAM_SEARCH}?searchKeywords=${keyword}`;
            const popupNm: string = "저널 꿈 검색";
            const options: string = 'width=1960,height=1440,top=0,left=270';
            const popup: Window = cF.ui.openPopup(url, popupNm, options);
            if (popup) popup.focus();
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
            journalChapterId,
            stdrdDt,
            journalDateWeekDay
        }: {
            journalDayId: string | number;
            journalChapterId?: string | number;
            stdrdDt: string;
            journalDateWeekDay: string;
        }): void {
            if (isNaN(Number(journalDayId))) return;
            dF.JournalDream.openRegModalWithDayContext(journalDayId, journalChapterId, stdrdDt, journalDateWeekDay);
        },

        /**
         * form submit
         */
        submit: function(): void {
            tinymce.get("tinymce_journalDreamCn").save();
            $("#journalDreamRegForm").submit();
        },

        /**
         * 등록 (Ajax)
         */
        regAjax: function(): void {
            const id: string = cF.util.getInputValue("#journalDreamRegForm [name='id']");
            const isMdf: boolean = cF.util.isNotEmpty(id);
            Swal.fire({
                text: Message.get(isMdf ? "view.cnfm.mdf" : "view.cnfm.reg"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = isMdf ? cF.util.bindUrl(Url.JOURNAL_DREAM, { id }) : Url.JOURNAL_DREAMS;
                const ajaxData: FormData = new FormData(document.getElementById("journalDreamRegForm") as HTMLFormElement);
                cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            dF.JournalDream.refresh();
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

            const url: string = cF.util.bindUrl(Url.JOURNAL_DREAM, { id });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const rsltObj: Record<string, any> = res.rsltObj;
                /* show modal */
                cF.handlebars.modal(rsltObj, "journal_dream_dtl");

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

            const url: string = cF.util.bindUrl(Url.JOURNAL_DREAM, { id });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltObj } = res;
                dF.JournalDream.openRegModalWithDayContext(
                    rsltObj.journalDayId,
                    rsltObj.journalChapterId,
                    rsltObj.stdrdDt,
                    rsltObj.journalDateWeekDay,
                    function(): void {
                        ModalHistory.push(self, func, args);
                    }
                );
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

            const item = document.querySelector(`.journal-dream-item[data-id='${id}']`) as HTMLElement;
            const cacheContext = dF.State.resolveJournalCacheContext(item);
            const payload = { id, contentType: "JOURNAL_DREAM", stateKey, cacheContext };
            dF.State.toggleAjax(payload, function(res: AjaxResponse): void {
                if (!item) return;
                const lowerStateKey: string = stateKey.toLowerCase();
                item.dataset[lowerStateKey] = res.rsltSts === "ON" ? "Y" : "N";
                const icon: HTMLElement = item.querySelector(`.icon-${lowerStateKey}`);
                icon?.classList.toggle("d-none", res.rsltSts !== "ON");
                const chk: HTMLInputElement = item.querySelector(`.dream-context-${lowerStateKey}-check`);
                if (chk) chk.checked = res.rsltSts === "ON";
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
                const content: HTMLDivElement = item.querySelector("div.journal-dream-content .journal-content");
                if (!content) return console.warn("content not found.");

                content?.classList.toggle("collapsed", res.rsltSts === "ON");
                item.classList.toggle("is-collapsed", res.rsltSts === "ON");
            }
            this.toggleStateAjax(id, "COLLAPSED", { onOffFunc });
        },

        /**
         * 정리완료 토글. (Ajax)
         * @param {string|number} id - 글 번호.
         */
        resolveAjax: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            const onOffFunc = function(res: AjaxResponse, item: HTMLElement): void {
                if (res.rsltSts === "ON") {
                    const content: HTMLDivElement = item.querySelector("div.journal-dream-content .journal-content");
                    if (!content) console.warn("content not found.");
                    content?.classList.add("collapsed");
                    item.dataset.collapsed = "Y";
                    item.classList.add("is-collapsed");

                    const collapsedChk: HTMLInputElement = item.querySelector(".dream-context-collapsed-check");
                    if (collapsedChk) collapsedChk.checked = true;
                }
            }
            this.toggleStateAjax(id, "RESOLVED", { onOffFunc });
        },

        /**
         * 중요여부 토글. (Ajax)
         * @param {string|number} id - 글 번호.
         */
        imprtcAjax: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            const onOffFunc = function(res: AjaxResponse, item: HTMLElement): void {
                const content: HTMLDivElement = item.querySelector("div.journal-dream-content .journal-content");
                if (!content) return console.warn("content not found.");

                content.classList.toggle("imprtc", res.rsltSts === "ON");
            }
            this.toggleStateAjax(id, "IMPRTC", { onOffFunc });
        },

        /**
         * 참조 여부 토글. (Ajax)
         * @param {string|number} id - 글 번호.
         */
        refrncAjax: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            const onOffFunc = function(res: AjaxResponse, item: HTMLElement): void {
                const content: HTMLDivElement = item.querySelector("div.journal-dream-content .journal-content");
                if (!content) return console.warn("content not found.");

                content.classList.toggle("refrnc", res.rsltSts === "ON");
            }
            this.toggleStateAjax(id, "REFRNC", { onOffFunc });
        },

        /**
         * 악몽(NHTMR) 상태 토글
         */
        nhtmrAjax: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            const onOffFunc = function(res: AjaxResponse, item: HTMLElement): void {
                item.querySelector(".dream-nhtmr-badge")?.classList.toggle("d-none", res.rsltSts !== "ON");
            };
            this.toggleStateAjax(id, "NHTMR", { onOffFunc });
        },

        /**
         * 입면 환각(HALLUC) 상태 토글
         */
        hallucAjax: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            const onOffFunc = function(res: AjaxResponse, item: HTMLElement): void {
                item.querySelector(".dream-halluc-badge")?.classList.toggle("d-none", res.rsltSts !== "ON");
            };
            this.toggleStateAjax(id, "HALLUC", { onOffFunc });
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

                const url: string = cF.util.bindUrl(Url.JOURNAL_DREAM, { id });
                cF.$ajax.delete(url, null, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            dF.JournalDream.refresh();
                        });
                }, "block");
            });
        },

        /**
         * @param {string|number} id - 글 번호.
         * @param {'Y'|'N'} collapsedYn - 글접기 여부.
         */
        collapse: function(id: string|number, collapsedYn: 'Y'|'N'): void {
            if (isNaN(Number(id))) return;

            const url: string = Url.JOURNAL_DREAM_SET_COLLAPSE_AJAX;
            const ajaxData: Record<string, any> = { id, collapsedYn };
            cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) return;

                // 찾아서 해당 그것만 collapse 추가 제거.
                const item: HTMLElement = document.querySelector(`.journal-dream-content[data-id='${id}']`);
                if (!item) return console.log("item not found.");

                const content: HTMLElement = item.querySelector(".journal-content");
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
         * @param {string|number} id - 글 번호.
         * @param {HTMLElement} trigger - 클릭 버튼 객체
         */
        toggle: function(id: string|number, trigger: HTMLElement): void {
            if (isNaN(Number(id))) return;

            const item: HTMLElement = trigger.closest(`.journal-dream-item[data-id='${id}']`);
            if (!item) return console.log("item not found.");

            const content: HTMLElement = item.querySelector(".journal-dream-content .journal-content");
            if (!content) return console.log("content not found.");

            const icon: HTMLElement = document.querySelector(`#dream-toggle-icon-${id}`);
            if (!icon) console.log("icon not found.");
            const collapsedIds = new Set(JSON.parse(localStorage.getItem(dF.JournalDream.STORAGE_KEY) || "[]"));

            const isCollapsed: boolean = content.classList.contains("collapsed");
            if (isCollapsed) {
                content.classList.remove("collapsed");
                item.classList.remove("is-collapsed");
                icon?.classList.replace("bi-arrows-expand", "bi-arrows-collapse");
                collapsedIds.delete(id);
            } else {
                content.classList.add("collapsed");
                item.classList.add("is-collapsed");
                icon?.classList.replace("bi-arrows-collapse", "bi-arrows-expand");
                collapsedIds.add(id);
            }

            localStorage.setItem(dF.JournalDream.STORAGE_KEY, JSON.stringify(Array.from(collapsedIds)));
        },

        /**
         * 접힌 꿈 초기화
         */
        initCollapseState: function(): void {
            const collapsedIds = new Set(JSON.parse(localStorage.getItem(dF.JournalDream.STORAGE_KEY) || "[]"));
            document.querySelectorAll(".journal-dream-item .journal-dream-content").forEach((item: HTMLElement): void => {
                const id: string = item.dataset.id;
                const content: HTMLElement = item.querySelector(".journal-content");
                const icon: HTMLElement = document.querySelector(`#dream-toggle-icon-${id}`);
                if (!icon) console.log("icon not found.");
                if (id && collapsedIds.has(id)) {
                    content?.classList.add("collapsed");
                    item.closest(".journal-dream-item")?.classList.add("is-collapsed");
                    icon?.classList.replace("bi-arrows-collapse", "bi-arrows-expand");
                }
            });
        },

        /**
         * copy
         * @param {string|number} id - 글 번호.
         * @deprecated
         */
        copy: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            const url: string = cF.util.bindUrl(Url.JOURNAL_DREAM, { id });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const rsltObj: Record<string, any> = res.rsltObj;
                const { stdrdDt, journalDateWeekDay } = rsltObj;
                const date: string = stdrdDt + " (" + journalDateWeekDay + ")" + "\r\n";
                const resultCn: string = rsltObj.content;
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
         * View Model 구성
         * @param {Object} dream
         * @param {String} profileName
         */
        buildViewModel: function(dream, profileName) {
            const profile: any = dF.JournalDream.PROFILE[profileName];

            if (!profile) throw new Error(`Unknown render profile: ${profileName}`);

            const hasState = (targetState: string): boolean =>
                Array.isArray(dream.state?.list) && dream.state.list.some((state: any): boolean => state?.stateKey === targetState);

            return {
                ...dream,
                view: profile,
                contentClass: [
                    'journal-content',
                    profile.collapsed && hasState('COLLAPSED') ? 'collapsed collapse-4' : null,
                    hasState('IMPRTC') ? 'imprtc' : null,
                    hasState('REFRNC') ? 'refrnc' : null
                ].filter(Boolean).join(' ')
            };
        }
    }
})();
