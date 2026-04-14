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
            SUMRY: {
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
                await dF.JrnlDiaryTag.init();
                this.viewType = viewType;
                this.initialized = true;
                console.log("'dF.JrnlDiary' module initialized.");
            })();

            return this.initPromise;
        },

        /**
         * refresh
         */
        refresh: function(): void {
            switch (this.viewType) {
                case "LIST":
                    dF.JrnlDay.yyMnthListAjax();
                    dF.JrnlDiaryTag.listAjax();     // 태그 refresh
                    break;
                case "CAL":
                    Page.refreshEventList();
                    dF.JrnlDiaryTag.listAjax();     // 태그 refresh
                    break;
                case "DAILY":
                case "WEEKLY":
                    dF.JrnlDay.refresh();
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
         * 키워드 검색 (Ajax)
         */
        searchPopup: function(): void {
            const keyword: string = (document.querySelector("#diarySearchKeyword") as HTMLInputElement)?.value;
            const url: string = `${Url.JRNL_DIARY_SEARCH}?searchKeywords=${keyword}`;
            const popupNm: string = "저널 일기 검색";
            const options: string = 'width=1960,height=1440,top=0,left=270';
            const popup: Window = cF.ui.openPopup(url, popupNm, options);
            if (popup) popup.focus();
        },

        /**
         * 등록 모달 호출
         * @param {Object} param - 파라미터 객체
         * @param {string|number} param.jrnlDayId - 저널 일자 번호.
         * @param {string|number} param.jrnlChapterId - 저널 챕터 번호.
         * @param {string} param.stdrdDt - 기준 날짜.
         * @param {string} param.jrnlDtWeekDay - 기준 날짜 요일.
         */
        regModal: function({ jrnlDayId, jrnlChapterId, stdrdDt, jrnlDtWeekDay }: { jrnlDayId: string | number; jrnlChapterId: string | number; stdrdDt: string; jrnlDtWeekDay: string; }): void {
            if (isNaN(Number(jrnlDayId))) return;
            if (isNaN(Number(jrnlChapterId))) return;

            const url: string = cF.util.bindUrl(Url.JRNL_DAY, { id: jrnlDayId });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) return;
                const chapterList = res.rsltObj.chapterList;
                const obj: Record<string, any> = { jrnlDayId: jrnlDayId, jrnlChapterId: jrnlChapterId, stdrdDt: stdrdDt, jrnlDtWeekDay: jrnlDtWeekDay, chapterList: chapterList };
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
            const id: string = cF.util.getInputValue("#jrnlDiaryRegForm [name='id']");
            const isMdf: boolean = cF.util.isNotEmpty(id);
            Swal.fire({
                text: Message.get(isMdf ? "view.cnfm.mdf" : "view.cnfm.reg"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = isMdf ? cF.util.bindUrl(Url.JRNL_DIARY, { id }) : Url.JRNL_DIARIES;
                const ajaxData: FormData = new FormData(document.getElementById("jrnlDiaryRegForm") as HTMLFormElement);
                cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            dF.JrnlDiary.refresh();
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

            const url: string = cF.util.bindUrl(Url.JRNL_DIARY, { id });
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

            const url: string = cF.util.bindUrl(Url.JRNL_DIARY, { id });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltObj } = res;
                const url: string = cF.util.bindUrl(Url.JRNL_DAY, { id: rsltObj.jrnlDayId });
                cF.ajax.get(url, null, function(res: AjaxResponse): void {
                    if (!res.rslt) return;
                    const chapterList = res.rsltObj.chapterList;
                    const obj: Record<string, any> = { ...rsltObj, chapterList: chapterList };
                    /* initialize form. */
                    dF.JrnlDiary.initForm(obj);

                    /* modal history push */
                    ModalHistory.push(self, func, args);
                });
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

                const url: string = cF.util.bindUrl(Url.JRNL_DIARY, { id });
                cF.$ajax.delete(url, null, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            dF.JrnlDiary.refresh();
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

            const item = document.querySelector(`.jrnl-diary-item[data-id='${id}']`) as HTMLElement;
            const cacheContext = dF.State.resolveJrnlCacheContext(item);
            const payload = { id, contentType: "JRNL_DIARY", stateCd, cacheContext };
            dF.State.toggleAjax(payload, function(res: AjaxResponse): void {
                if (!item) return;
                const lowerStateCd: string = stateCd.toLowerCase();
                item.dataset[lowerStateCd] = res.rsltSts === "ON" ? "Y" : "N";
                const icon: HTMLElement = item.querySelector(`.icon-${lowerStateCd}`);
                icon?.classList.toggle("d-none", res.rsltSts !== "ON");
                const chk: HTMLInputElement = item.querySelector(`.diary-context-${lowerStateCd}-check`);
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

            const onOffFunc: Function = function(res: AjaxResponse, item: HTMLElement): void {
                const cn: HTMLDivElement = item.querySelector("div.jrnl-diary-cn .cn");
                if (!cn) return console.warn("cn not found.");

                cn?.classList.toggle("collapsed", res.rsltSts === "ON");
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

            const onOffFunc: Function = function(res: AjaxResponse, item: HTMLElement): void {
                if (res.rsltSts === "ON") {
                    const cn: HTMLDivElement = item.querySelector("div.jrnl-diary-cn .cn");
                    if (!cn) console.warn("cn not found.");
                    cn?.classList.add("collapsed");
                    item.dataset.collapsed = "Y";
                    item.classList.add("is-collapsed");

                    const collapsedChk: HTMLInputElement = item.querySelector(".diary-context-collapsed-check");
                    if (collapsedChk) collapsedChk.checked = true;
                    const icon: HTMLElement = item.querySelector(".icon-collapsed");
                    icon?.classList.toggle("d-none", res.rsltSts !== "ON");
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

            const onOffFunc: Function = function(res: AjaxResponse, item: HTMLElement): void {
                const wrapper: HTMLDivElement = item.querySelector("div.jrnl-diary-cn");
                const cn: HTMLDivElement = item.querySelector("div.jrnl-diary-cn .cn");
                if (!cn) return console.warn("cn not found.");

                wrapper?.classList.remove("bg-secondary");
                cn.classList.toggle("imprtc", res.rsltSts === "ON");
            }
            this.toggleStateAjax(id, "IMPRTC", { onOffFunc });
        },

        /**
         * 참조 여부 토글. (Ajax)
         * @param {string|number} id - 글 번호.
         */
        refrncAjax: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            const onOffFunc: Function = function(res: AjaxResponse, item: HTMLElement): void {
                const wrapper: HTMLDivElement = item.querySelector("div.jrnl-diary-cn");
                const cn: HTMLDivElement = item.querySelector("div.jrnl-diary-cn .cn");
                if (!cn) return console.warn("cn not found.");

                wrapper?.classList.remove("bg-secondary");
                cn.classList.toggle("refrnc", res.rsltSts === "ON");
            }
            this.toggleStateAjax(id, "REFRNC", { onOffFunc });
        },

        /**
         * toggle
         * @param {string|number} id - 글 번호.
         * @param {HTMLElement} trigger - 클릭 버튼 객체
         */
        toggle: function(id: string|number, trigger: HTMLElement): void {
            if (isNaN(Number(id))) return;

            const item: HTMLElement = trigger.closest(`.jrnl-diary-item[data-id='${id}']`);
            if (!item) return console.log("item not found.");

            const content: HTMLElement = item.querySelector(".jrnl-diary-cn .cn");
            if (!content) return console.log("content not found.");

            const icon: HTMLElement = item.querySelector('.diary-toggle-icon');
            if (!icon) console.log("icon not found.");

            const isCollapsed: boolean = content.classList.contains("collapsed");
            if (isCollapsed) {
                content.classList.remove("collapsed");
                item.classList.remove("is-collapsed");
                icon?.classList.replace("bi-arrows-expand", "bi-arrows-collapse");
            } else {
                content.classList.add("collapsed");
                item.classList.add("is-collapsed");
                icon?.classList.replace("bi-arrows-collapse", "bi-arrows-expand");
            }
        },

        /**
         * copy
         * @param {string|number} id - 글 번호.
         */
        copy: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            const url: string = cF.util.bindUrl(Url.JRNL_DIARY, { id });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const rsltObj: Record<string, any> = res.rsltObj;
                const { stdrdDt, jrnlDtWeekDay } = rsltObj;
                const date: string = stdrdDt + " (" + jrnlDtWeekDay + ")" + "\r\n";
                const resultCn: string = rsltObj.cn;
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
                            Swal.fire({ icon: "success", text: "클립보드에 복사되었습니다.", timer: 1500, showConfirmButton: false });
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
         * @param {Object} diary
         * @param {String} profileName
         */
        buildViewModel: function(diary, profileName) {
            const profile: any = dF.JrnlDiary.PROFILE[profileName];

            if (!profile) throw new Error(`Unknown render profile: ${profileName}`);

            const hasState = (targetState: string): boolean =>
                Array.isArray(diary.state?.list) && diary.state.list.some((state: any): boolean => state?.stateCd === targetState);

            return {
                ...diary,
                view: profile,
                cnClass: [
                    'cn',
                    profile.collapsed && hasState('COLLAPSED') ? 'collapsed' : null,
                    hasState('IMPRTC') ? 'imprtc' : null,
                    hasState('REFRNC') ? 'refrnc' : null
                ].filter(Boolean).join(' ')
            };
        }
    }
})();
