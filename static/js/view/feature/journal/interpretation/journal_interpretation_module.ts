/**
 * journal_interpretation_module.ts
 * 저널 해석 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.JournalInterpretation = (function(): dfModule {
    return {
        STORAGE_KEY: "collapsedJournalInterpretationIds",

        initialized: false,
        inKeywordSearchMode: false,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.JournalInterpretation.initialized) return;

            dF.Lifecycle?.init?.();
            dF.JournalInterpretation.initialized = true;
            console.log("'dF.JournalInterpretation' module initialized.");
        },

        /**
         * form init
         * @param {Record<string, any>} obj - 폼에 바인딩할 데이터
         */
        initForm: function(obj: Record<string, any> = {}): void {
            /* show modal */
            cF.handlebars.modal(obj, "journal_interpretation_reg", ["header"]);

            /* jquery vali  dation */
            cF.validate.validateForm("#journalInterpretationRegForm", dF.JournalInterpretation.regAjax);
            // checkbox init
            cF.ui.chckboxLabel("#journalInterpretationRegForm #imprtcYn", "중요//해당없음", "red//gray");
            /* tinymce editor reset */
            cF.tinymce.init('#tinymce_journalInterpretationCn');
            cF.tinymce.setContentWhenReady("tinymce_journalInterpretationCn", obj.content || "");
        },

        /**
         * 등록 모달 호출
         * @param {Object} param - 파라미터 객체
         * @param {string|number} param.journalDayId - 저널 일자 번호.
         * @param {string|number} param.refId - 참조 컨텐츠 번호.
         * @param {string} param.refContentType - 참조 컨텐츠 타입.
         * @param {string} param.stdrdDt - 기준 날짜.
         * @param {string} param.journalDateWeekDay - 기준 날짜 요일.
         */
        regModal: function({
            journalDayId,
            refId,
            refContentType,
            stdrdDt,
            journalDateWeekDay
        }: {
            journalDayId: string | number;
            refId: string | number;
            refContentType: string;
            stdrdDt: string;
            journalDateWeekDay: string;
        }): void {
            if (isNaN(Number(journalDayId))) return;
            if (isNaN(Number(refId))) return;
            if (cF.util.isEmpty(refContentType)) return;

            const obj: Record<string, any> = {
                journalDayId,
                refId,
                refContentType,
                stdrdDt,
                journalDateWeekDay
            };
            /* initialize form. */
            dF.JournalInterpretation.initForm(obj);
        },

        /**
         * form submit
         */
        submit: function(): void {
            tinymce.get("tinymce_journalInterpretationCn").save();
            $("#journalInterpretationRegForm").submit();
        },

        /**
         * 등록 (Ajax)
         */
        regAjax: function(): void {
            const id: string = cF.util.getInputValue("#journalInterpretationRegForm [name='id']");
            const isMdf: boolean = cF.util.isNotEmpty(id);
            Swal.fire({
                text: Message.get(isMdf ? "view.cnfm.mdf" : "view.cnfm.reg"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = isMdf ? cF.util.bindUrl(Url.JOURNAL_INTERPRETATION, { id }) : Url.JOURNAL_INTERPRETATIONS;
                const ajaxData: FormData = new FormData(document.getElementById("journalInterpretationRegForm") as HTMLFormElement);
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

            const url: string = cF.util.bindUrl(Url.JOURNAL_INTERPRETATION, { id });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const rsltObj: Record<string, any> = res.rsltObj;
                /* show modal */
                cF.handlebars.modal(rsltObj, "journal_interpretation_dtl");

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

            const url: string = cF.util.bindUrl(Url.JOURNAL_INTERPRETATION, { id });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltObj } = res;
                /* initialize form. */
                dF.JournalInterpretation.initForm(rsltObj);

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

                const url: string = cF.util.bindUrl(Url.JOURNAL_INTERPRETATION, { id });
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
         * 컨텍스트 메뉴 스위치 값에 따라 해석 라이프사이클을 RESOLVED 또는 OPEN으로 설정한다.
         *
         * 완료 진행 상태는 라이프사이클 endpoint가 소유한다. 이 메소드는 보이는 항목을 즉시 갱신하고,
         * 완료 상태일 때 글접기 표시를 적용한다.
         *
         * @param id interpretation ID
         * @param trigger 원하는 완료 여부를 나타내는 checkbox
         */
        resolveAjax: function(id: string|number, trigger?: HTMLInputElement): void {
            if (isNaN(Number(id))) return;

            const lifecycleKey: string = trigger?.checked ? "RESOLVED" : "OPEN";
            this.setLifecycleAjax(id, lifecycleKey);
        },

        /**
         * 해석 라이프사이클을 명시적으로 설정한다.
         *
         * @param id 해석 ID
         * @param lifecycleKey 설정할 라이프사이클 키
         */
        setLifecycleAjax: function(id: string|number, lifecycleKey: string): void {
            if (isNaN(Number(id))) return;

            const content = document.querySelector(`.journal-interpretation-content[data-id='${id}']`) as HTMLElement;
            const item = content?.closest(".journal-interpretation-item") as HTMLElement;
            const cacheContext = dF.Lifecycle.resolveJournalCacheContext(content);
            const payload = { id, contentType: "JOURNAL_INTERPRETATION", lifecycleKey, cacheContext };
            dF.Lifecycle.setAjax(payload, function(_res: AjaxResponse): void {
                if (!content) return;

                content.dataset.lifecycle = lifecycleKey;
                content.dataset.resolved = lifecycleKey === "RESOLVED" ? "Y" : "N";

                const idx: HTMLElement = item?.querySelector(".col-1 span") as HTMLElement;
                idx?.classList.toggle("text-success", lifecycleKey === "RESOLVED");

                const resolvedChk: HTMLInputElement = item?.querySelector(".interpretation-context-resolved-check") as HTMLInputElement;
                if (resolvedChk) resolvedChk.checked = lifecycleKey === "RESOLVED";

                const lifecycleChecks: NodeListOf<HTMLInputElement> = item?.querySelectorAll(".interpretation-context-lifecycle-check") as NodeListOf<HTMLInputElement>;
                lifecycleChecks?.forEach(function(chk: HTMLInputElement): void {
                    chk.checked = chk.value === lifecycleKey;
                    const label: HTMLElement = chk.closest("label")?.querySelector(".form-check-label") as HTMLElement;
                    label?.classList.toggle("text-primary", chk.value === "PENDING" && chk.checked);
                    label?.classList.toggle("text-success", chk.value === "RESOLVED" && chk.checked);
                    label?.classList.toggle("text-muted", !chk.checked);
                });

                const resolvedLabel: HTMLElement = resolvedChk?.closest("label")?.querySelector(".form-check-label") as HTMLElement;
                resolvedLabel?.classList.toggle("text-success", lifecycleKey === "RESOLVED");
                resolvedLabel?.classList.toggle("text-muted", lifecycleKey !== "RESOLVED");

                if (lifecycleKey !== "RESOLVED") return;
                const journalContent: HTMLElement = content.querySelector(".journal-content") as HTMLElement;
                journalContent?.classList.add("collapsed");
            });
        },

        /**
         * @param {string|number} id - 글 번호.
         * @param {'Y'|'N'} collapsedYn - 글접기 여부.
         */
        collapse: function(id: string|number, collapsedYn: 'Y'|'N'): void {
            if (isNaN(Number(id))) return;

            const url: string = Url.JOURNAL_INTERPRETATION_SET_COLLAPSE_AJAX;
            const ajaxData: Record<string, any> = { id, collapsedYn };
            cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) return;

                // 찾아서 해당 그것만 collapse 추가 제거.
                const item: HTMLElement = document.querySelector(`.journal-interpretation-content[data-id='${id}']`);
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

            const item: HTMLElement = trigger.closest(`.journal-interpretation-item[data-id='${id}']`);
            if (!item) return console.log("item not found.");

            const content: HTMLElement = item.querySelector(".journal-interpretation-content .journal-content");
            if (!content) return console.log("content not found.");

            const icon: HTMLElement = document.querySelector(`#interpretation-toggle-icon-${id}`);
            if (!icon) console.log("icon not found.");
            const collapsedIds = new Set(JSON.parse(localStorage.getItem(dF.JournalInterpretation.STORAGE_KEY) || "[]"));

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

            localStorage.setItem(dF.JournalInterpretation.STORAGE_KEY, JSON.stringify(Array.from(collapsedIds)));
        },
        
        /**
         * 접힌 엔트리 초기화
         */
        initCollapseState: function(): void {
            const collapsedIds = new Set(JSON.parse(localStorage.getItem(dF.JournalInterpretation.STORAGE_KEY) || "[]"));
            document.querySelectorAll(".journal-interpretation-item .journal-interpretation-content").forEach((item: HTMLElement): void => {
                const id: string = item.dataset.id;
                const content: HTMLElement = item.querySelector(".journal-content");
                const icon: HTMLElement = document.querySelector(`#interpretation-toggle-icon-${id}`);
                if (!icon) console.log("icon not found.");
                if (id && collapsedIds.has(id)) {
                    content?.classList.add("collapsed");
                    icon?.classList.replace("bi-chevron-up", "bi-chevron-down");
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

            const url: string = cF.util.bindUrl(Url.JOURNAL_INTERPRETATION, { id });
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
                const replacedCn: string = resultCn
                    .replace(/<\s*hr\b[^>]*\/?>/gi, "\n---\n")
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
    }
})();
