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
                await dF.JrnlDreamTag.init();
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
                    dF.JrnlDreamTag.listAjax();     // 태그 refresh
                    break;
                case "CAL":
                    Page.refreshEventList();
                    dF.JrnlDreamTag.listAjax();     // 태그 refresh
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
         * 키워드 검색 팝업 호출
         */
        searchPopup: function(): void {
            const keyword: string = (document.querySelector("#dreamSearchKeyword") as HTMLInputElement)?.value;
            const url: string = `${Url.JRNL_DREAM_SEARCH}?searchKeywords=${keyword}`;
            const popupNm: string = "저널 꿈 검색";
            const options: string = 'width=1960,height=1440,top=0,left=270';
            const popup: Window = cF.ui.openPopup(url, popupNm, options);
            if (popup) popup.focus();
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
            const postNo: string = cF.util.getInputValue("#jrnlDreamRegForm [name='postNo']");
            const isMdf: boolean = cF.util.isNotEmpty(postNo);
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

                            dF.JrnlDream.refresh();
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
         * 상태 토글 (Ajax)
         * @param postNo
         * @param stateCd
         * @param {object} object
         */
        toggleStateAjax: function(postNo: string|number, stateCd: string, { onOffFunc }): void {
            if (isNaN(Number(postNo))) return;

            const item = document.querySelector(`.jrnl-dream-item[data-id='${postNo}']`) as HTMLElement;
            const cacheContext = dF.State.resolveJrnlCacheContext(item);
            const payload = { postNo, contentType: "JRNL_DREAM", stateCd, cacheContext };
            dF.State.toggleAjax(payload, function(res: AjaxResponse): void {
                if (!item) return;
                const lowerStateCd: string = stateCd.toLowerCase();
                item.dataset[lowerStateCd] = res.rsltSts === "ON" ? "Y" : "N";
                const icon: HTMLElement = item.querySelector(`.icon-${lowerStateCd}`);
                icon?.classList.toggle("d-none", res.rsltSts !== "ON");
                const chk: HTMLInputElement = item.querySelector(`.dream-context-${lowerStateCd}-check`);
                if (chk) chk.checked = res.rsltSts === "ON";
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
                const cn: HTMLDivElement = item.querySelector("div.jrnl-dream-cn .cn");
                if (!cn) return console.warn("cn not found.");

                cn?.classList.toggle("collapsed", res.rsltSts === "ON");
                item.classList.toggle("is-collapsed", res.rsltSts === "ON");
            }
            this.toggleStateAjax(postNo, "COLLAPSED", { onOffFunc });
        },

        /**
         * 정리완료 토글. (Ajax)
         * @param {string|number} postNo - 글 번호.
         */
        resolveAjax: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            const onOffFunc = function(res: AjaxResponse, item: HTMLElement): void {
                if (res.rsltSts === "ON") {
                    const cn: HTMLDivElement = item.querySelector("div.jrnl-dream-cn .cn");
                    if (!cn) console.warn("cn not found.");
                    cn?.classList.add("collapsed");
                    item.dataset.collapsed = "Y";
                    item.classList.add("is-collapsed");

                    const collapsedChk: HTMLInputElement = item.querySelector(".dream-context-collapsed-check");
                    if (collapsedChk) collapsedChk.checked = true;
                }
            }
            this.toggleStateAjax(postNo, "RESOLVED", { onOffFunc });
        },

        /**
         * 중요여부 토글. (Ajax)
         * @param {string|number} postNo - 글 번호.
         */
        imprtcAjax: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            const onOffFunc = function(res: AjaxResponse, item: HTMLElement): void {
                const cn: HTMLDivElement = item.querySelector("div.jrnl-dream-cn .cn");
                if (!cn) return console.warn("cn not found.");

                cn.classList.toggle("imprtc", res.rsltSts === "ON");
            }
            this.toggleStateAjax(postNo, "IMPRTC", { onOffFunc });
        },

        /**
         * 참조 여부 토글. (Ajax)
         * @param {string|number} postNo - 글 번호.
         */
        refrncAjax: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            const onOffFunc = function(res: AjaxResponse, item: HTMLElement): void {
                const cn: HTMLDivElement = item.querySelector("div.jrnl-dream-cn .cn");
                if (!cn) return console.warn("cn not found.");

                cn.classList.toggle("refrnc", res.rsltSts === "ON");
            }
            this.toggleStateAjax(postNo, "REFRNC", { onOffFunc });
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

                            dF.JrnlDream.refresh();
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
         * @param {HTMLElement} trigger - 클릭 버튼 객체
         */
        toggle: function(postNo: string|number, trigger: HTMLElement): void {
            if (isNaN(Number(postNo))) return;

            const id: string = String(postNo);
            const item: HTMLElement = trigger.closest(`.jrnl-dream-item[data-id='${id}']`);
            if (!item) return console.log("item not found.");

            const content: HTMLElement = item.querySelector(".jrnl-dream-cn .cn");
            if (!content) return console.log("content not found.");

            const icon: HTMLElement = document.querySelector(`#dream-toggle-icon-${id}`);
            if (!icon) console.log("icon not found.");
            const collapsedIds = new Set(JSON.parse(localStorage.getItem(dF.JrnlDream.STORAGE_KEY) || "[]"));

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
                    item.closest(".jrnl-dream-item")?.classList.add("is-collapsed");
                    icon?.classList.replace("bi-arrows-collapse", "bi-arrows-expand");
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

            const url: string = cF.util.bindUrl(Url.JRNL_DREAM, { postNo });
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
            const profile: any = dF.JrnlDream.PROFILE[profileName];

            if (!profile) throw new Error(`Unknown render profile: ${profileName}`);

            return {
                ...dream,
                view: profile,
                cnClass: [
                    'cn',
                    profile.collapsed && dream.state?.includes('COLLAPSED') ? 'collapsed' : null
                ].filter(Boolean).join(' ')
            };
        }
    }
})();
