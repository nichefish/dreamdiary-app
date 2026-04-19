/**
 * journal_note_module.ts
 * 저널 노트 스크립트 모듈 (일기 챕터 DIARY 전용)
 *
 * @author nichefish
 */
if (typeof dF === "undefined") { var dF = {} as any; }
dF.JournalNote = (function(): dfModule {
    return {
        initialized: false,
        initPromise: null,
        viewType: null as "LIST" | "CAL" | "DAILY" | "WEEKLY" | "SEARCH" | null,

        init: async function(viewType: "LIST" | "CAL" | "DAILY" | "WEEKLY" | "SEARCH"): Promise<void> {
            if (this.initPromise) return this.initPromise;
            this.initPromise = (async () => {
                this.viewType = viewType;
                this.initialized = true;
                console.log("'dF.JournalNote' module initialized.");
            })();
            return this.initPromise;
        },

        refresh: function(): void {
            switch (this.viewType) {
                case "LIST":
                    dF.JournalDay.yyMnthListAjax();
                    break;
                case "CAL":
                    if (typeof Page.refreshEventList === "function") {
                        Page.refreshEventList();
                    } else if (typeof dF.JournalDayMeta?.listAjax === "function") {
                        dF.JournalDayMeta.listAjax();
                    }
                    break;
                case "DAILY":
                case "WEEKLY":
                    dF.JournalDay.refresh();
                    break;
                case "SEARCH":
                    location.reload();
                    break;
                default:
                    if (typeof dF.JournalDay?.refresh === "function") dF.JournalDay.refresh();
                    break;
            }
            cF.ui.unblockUI();
        },

        initForm: function(obj: Record<string, any> = {}): void {
            cF.handlebars.modal(obj, "journal_note_reg", ["header"]);
            cF.validate.validateForm("#journalNoteRegForm", dF.JournalNote.regAjax);
            cF.tinymce.init("#tinymce_journalNoteCn");
            cF.tinymce.setContentWhenReady("tinymce_journalNoteCn", obj.content || "");
        },

        /**
         * 노트 등록 모달 (NOTE 챕터만 대상)
         */
        regModal: function({
            journalDayId,
            journalChapterId,
            stdrdDt,
            journalDateWeekDay
        }: {
            journalDayId: string | number;
            journalChapterId: string | number;
            stdrdDt: string;
            journalDateWeekDay: string;
        }): void {
            if (isNaN(Number(journalDayId))) return;
            if (isNaN(Number(journalChapterId))) return;

            const url: string = cF.util.bindUrl(Url.JOURNAL_DAY, { id: journalDayId });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) return;
                const rawList: Record<string, any>[] = res.rsltObj.chapterList ?? [];
                const chapterList: Record<string, any>[] = rawList.filter(
                    (c: Record<string, any>): boolean => c?.chapterType === "NOTE"
                );
                if (!chapterList.length) {
                    Swal.fire({ text: Message.get("msg.journal.note.note-chapter-only") });
                    return;
                }
                let resolvedChapterId: number = Number(journalChapterId);
                const exists: boolean = chapterList.some((c: Record<string, any>): boolean => Number(c.id) === resolvedChapterId);
                if (!exists) {
                    resolvedChapterId = Number(chapterList[0].id);
                }
                const obj: Record<string, any> = {
                    journalDayId: journalDayId,
                    journalChapterId: resolvedChapterId,
                    stdrdDt: stdrdDt,
                    journalDateWeekDay: journalDateWeekDay,
                    chapterList: chapterList
                };
                dF.JournalNote.initForm(obj);
            });
        },

        submit: function(): void {
            tinymce.get("tinymce_journalNoteCn").save();
            $("#journalNoteRegForm").submit();
        },

        regAjax: function(): void {
            const id: string = cF.util.getInputValue("#journalNoteRegForm [name='id']");
            const isMdf: boolean = cF.util.isNotEmpty(id);
            Swal.fire({
                text: Message.get(isMdf ? "view.cnfm.mdf" : "view.cnfm.reg"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = isMdf ? cF.util.bindUrl(Url.JOURNAL_NOTE, { id }) : Url.JOURNAL_NOTES;
                const ajaxData: FormData = new FormData(document.getElementById("journalNoteRegForm") as HTMLFormElement);
                cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message }).then(function(): void {
                        if (!res.rslt) return;
                        dF.JournalNote.refresh();
                    });
                }, "block");
            });
        },

        mdfModal: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            const openModals: NodeList = document.querySelectorAll(".modal.show");
            openModals.forEach((modal: Node): void => {
                $(modal).modal("hide");
            });

            const self = this;
            const func: string = arguments.callee.name;
            const args: any[] = Array.from(arguments);

            const url: string = cF.util.bindUrl(Url.JOURNAL_NOTE, { id });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltObj } = res;
                const dayUrl: string = cF.util.bindUrl(Url.JOURNAL_DAY, { id: rsltObj.journalDayId });
                cF.ajax.get(dayUrl, null, function(dayRes: AjaxResponse): void {
                    if (!dayRes.rslt) return;
                    const rawList: Record<string, any>[] = dayRes.rsltObj.chapterList ?? [];
                    const chapterList: Record<string, any>[] = rawList.filter(
                        (c: Record<string, any>): boolean => c?.chapterType === "NOTE"
                    );
                    const obj: Record<string, any> = { ...rsltObj, chapterList };
                    dF.JournalNote.initForm(obj);
                    ModalHistory.push(self, func, args);
                });
            });
        },

        delAjax: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const delUrl: string = cF.util.bindUrl(Url.JOURNAL_NOTE, { id });
                cF.$ajax.delete(delUrl, null, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;
                            dF.JournalNote.refresh();
                        });
                }, "block");
            });
        },

        toggleStateAjax: function(id: string|number, stateKey: string, { onOffFunc }: { onOffFunc: Function }): void {
            if (isNaN(Number(id))) return;

            const item = document.querySelector(`.journal-note-item[data-id='${id}']`) as HTMLElement;
            const cacheContext = dF.State.resolveJournalCacheContext(item);
            const payload = { id, contentType: "JOURNAL_NOTE", stateKey, cacheContext };
            dF.State.toggleAjax(payload, function(res: AjaxResponse): void {
                if (!item) return;
                const lowerStateKey: string = stateKey.toLowerCase();
                item.dataset[lowerStateKey] = res.rsltSts === "ON" ? "Y" : "N";
                const icon: HTMLElement = item.querySelector(`.icon-${lowerStateKey}`);
                icon?.classList.toggle("d-none", res.rsltSts !== "ON");
                const chk: HTMLInputElement = item.querySelector(`.note-context-${lowerStateKey}-check`);
                if (chk) chk.checked = res.rsltSts === "ON";
                onOffFunc(res, item);
            });
        },

        collapseAjax: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            const onOffFunc: Function = function(res: AjaxResponse, item: HTMLElement): void {
                const content: HTMLDivElement = item.querySelector("div.journal-note-content .journal-content");
                if (!content) return console.warn("content not found.");

                content?.classList.toggle("collapsed", res.rsltSts === "ON");
                item.classList.toggle("is-collapsed", res.rsltSts === "ON");
            };
            this.toggleStateAjax(id, "COLLAPSED", { onOffFunc });
        },

        resolveAjax: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            const onOffFunc: Function = function(res: AjaxResponse, item: HTMLElement): void {
                if (res.rsltSts === "ON") {
                    const content: HTMLDivElement = item.querySelector("div.journal-note-content .journal-content");
                    if (!content) console.warn("content not found.");
                    content?.classList.add("collapsed");
                    item.dataset.collapsed = "Y";
                    item.classList.add("is-collapsed");

                    const collapsedChk: HTMLInputElement = item.querySelector(".note-context-collapsed-check");
                    if (collapsedChk) collapsedChk.checked = true;
                    const icon: HTMLElement = item.querySelector(".icon-collapsed");
                    icon?.classList.toggle("d-none", res.rsltSts !== "ON");
                }
            };
            this.toggleStateAjax(id, "RESOLVED", { onOffFunc });
        },

        imprtcAjax: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            const onOffFunc: Function = function(res: AjaxResponse, item: HTMLElement): void {
                const wrapper: HTMLDivElement = item.querySelector("div.journal-note-content");
                const content: HTMLDivElement = item.querySelector("div.journal-note-content .journal-content");
                if (!content) return console.warn("content not found.");

                wrapper?.classList.remove("bg-secondary");
                content.classList.toggle("imprtc", res.rsltSts === "ON");
            };
            this.toggleStateAjax(id, "IMPRTC", { onOffFunc });
        },

        refrncAjax: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            const onOffFunc: Function = function(res: AjaxResponse, item: HTMLElement): void {
                const wrapper: HTMLDivElement = item.querySelector("div.journal-note-content");
                const content: HTMLDivElement = item.querySelector("div.journal-note-content .journal-content");
                if (!content) return console.warn("content not found.");

                wrapper?.classList.remove("bg-secondary");
                content.classList.toggle("refrnc", res.rsltSts === "ON");
            };
            this.toggleStateAjax(id, "REFRNC", { onOffFunc });
        },

        toggle: function(id: string|number, trigger: HTMLElement): void {
            if (isNaN(Number(id))) return;

            const item: HTMLElement = trigger.closest(`.journal-note-item[data-id='${id}']`);
            if (!item) return console.log("item not found.");

            const content: HTMLElement = item.querySelector(".journal-note-content .journal-content");
            if (!content) return console.log("content not found.");

            const icon: HTMLElement = item.querySelector(".note-toggle-icon");
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

        copy: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            const url: string = cF.util.bindUrl(Url.JOURNAL_NOTE, { id });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const rsltObj: Record<string, any> = res.rsltObj;
                const { stdrdDt, journalDateWeekDay } = rsltObj;
                const titleLine: string = (rsltObj.title != null && String(rsltObj.title).trim() !== "")
                    ? String(rsltObj.title).trim() + "\r\n"
                    : "";
                const date: string = stdrdDt + " (" + journalDateWeekDay + ")" + "\r\n";
                const resultCn: string = rsltObj.content;
                const replacedCn: string = resultCn.replace(/<\s*br\s*\/?>/gi, "\n").replace(/<\s*\/?p[^>]*>/gi, "\n");
                const div: HTMLDivElement = document.createElement("div");
                div.innerHTML = date + titleLine + replacedCn;
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
        }
    };
})();
