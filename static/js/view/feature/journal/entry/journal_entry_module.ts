/**
 * journal_entry_module.ts
 * 일기, 꿈, 노트 공통 저널 entry 모듈.
 */
if (typeof dF === "undefined") { var dF = {} as any; }

dF.JournalEntry = (function(): dfModule {
    const profile = {
        LIST: { collapsed: true },
        TAG: { collapsed: false },
        SEARCH: { collapsed: false },
        ANNUAL: { collapsed: false },
    };

    const hasState = function(entry: Record<string, any>, targetState: string): boolean {
        return Array.isArray(entry.state?.list)
            && entry.state.list.some((state: any): boolean => state?.stateKey === targetState);
    };

    const closeOpenModals = function(): void {
        document.querySelectorAll(".modal.show").forEach((modal: Node): void => {
            $(modal).modal("hide");
        });
    };

    const resolveSearchUrl = function(config: Record<string, any>): string {
        if (cF.util.isNotEmpty(config?.searchUrl) && !String(config.searchUrl).includes("undefined")) {
            return String(config.searchUrl);
        }
        const typeSegment: string = String(config?.entryType ?? "DIARY").toLowerCase();
        return cF.util.bindUrl(Url.JOURNAL_EMTRY_SEARCH, { type: typeSegment });
    };

    /**
     * 타입별 행동 플러그인.
     * create() 내부에서 contentType 분기 대신 이 플러그인을 호출한다.
     */
    interface JournalEntryPlugin {
        /** 폼 검증 및 타입 전용 UI 초기화. */
        setupFormValidation(config: Record<string, any>, module: dfModule): void;
        /** 접힘 상태에 사용할 CSS 클래스. */
        collapseClass: string;
        /** toggle 시 localStorage에 접힘 ID를 저장할지 여부. */
        persistToggleToStorage: boolean;
        /** copy 시 제목 라인 추출 (없으면 빈 문자열 반환). */
        extractTitleLine(rsltObj: Record<string, any>): string;
    }

    const diaryPlugin: JournalEntryPlugin = {
        setupFormValidation(config: Record<string, any>, module: dfModule): void {
            cF.validate.validateForm(config.formSelector, module.submitHandler);
        },
        collapseClass: "collapsed",
        persistToggleToStorage: false,
        extractTitleLine(_rsltObj: Record<string, any>): string { return ""; },
    };

    const dreamPlugin: JournalEntryPlugin = {
        setupFormValidation(config: Record<string, any>, module: dfModule): void {
            cF.validate.validateForm(config.formSelector, module.submitHandler, {
                rules: {
                    elseDreamerNm: {
                        required(): boolean {
                            return $(`${config.formSelector} #elseDreamYn`).is(":checked");
                        },
                    },
                },
                ignore: undefined,
            });
            $("#elseDreamYn").change(function(): void {
                $("#elseDreamerNm").valid();
            });
            cF.ui.chckboxLabel(`${config.formSelector} #imprtcYn`, "중요//해당없음", "red//gray");
            cF.ui.chckboxLabel(`${config.formSelector} #elseDreamYn`, "해당//미해당", "blue//gray", function(): void {
                $("#elseDreamerNmDiv").removeClass("d-none");
            }, function(): void {
                $("#elseDreamerNmDiv").addClass("d-none");
            });
        },
        collapseClass: "collapsed collapse-4",
        persistToggleToStorage: true,
        extractTitleLine(_rsltObj: Record<string, any>): string { return ""; },
    };


    const configs: Record<string, Record<string, any>> = {
        JOURNAL_DIARY: {
            moduleName: "JournalEntry[JOURNAL_DIARY]",
            contentType: "JOURNAL_DIARY",
            entryType: "DIARY",
            moduleExpr: "dF.JournalEntry.get('JOURNAL_DIARY')",
            tagModuleExpr: "dF.JournalEntryTag.get('JOURNAL_DIARY')",
            contentLabel: Message.get("txt.journal.diary"),
            emptyLabel: Message.get("txt.journal.diary"),
            chapterType: null,
            listUrl: Url.JOURNAL_ENTRIES,
            itemUrl: Url.JOURNAL_ENTRY,
            searchUrl: Url.JOURNAL_DIARY_SEARCH,
            searchInputSelector: "#diarySearchKeyword",
            exportUrl: Url.JOURNAL_ENTRIES_EXPORT,
            tagCtgrMapUrl: Url.JOURNAL_ENTRY_TAG_CTGR_MAP,
            tagsUrl: Url.JOURNAL_ENTRY_TAGS,
            tagListTargetId: "journal_diary_tag_list_div",
            popupName: "diary search",
            modalKey: "journal_diary_reg",
            detailModalKey: "journal_diary_dtl",
            formSelector: "#journalDiaryRegForm",
            tinymceId: "tinymce_journalDiaryCn",
            tagInputSelector: "#journalDiaryRegForm #tagListStr",
            itemClass: "journal-diary-item",
            contentClass: "journal-diary-content",
            cssPrefix: "diary",
            toggleIconSelector: ".diary-toggle-icon",
            storageKey: "collapsedJournalDiaryIds",
            rightBorderClass: "ms-4",
            useTag: true,
            plugin: diaryPlugin,
        },
        JOURNAL_DREAM: {
            moduleName: "JournalEntry[JOURNAL_DREAM]",
            contentType: "JOURNAL_DREAM",
            entryType: "DREAM",
            moduleExpr: "dF.JournalEntry.get('JOURNAL_DREAM')",
            tagModuleExpr: "dF.JournalEntryTag.get('JOURNAL_DREAM')",
            contentLabel: Message.get("txt.journal.dream"),
            emptyLabel: Message.get("txt.journal.dream"),
            chapterType: "DREAM",
            listUrl: Url.JOURNAL_ENTRIES,
            itemUrl: Url.JOURNAL_ENTRY,
            searchUrl: Url.JOURNAL_DREAM_SEARCH,
            searchInputSelector: "#dreamSearchKeyword",
            exportUrl: Url.JOURNAL_ENTRIES_EXPORT,
            tagCtgrMapUrl: Url.JOURNAL_ENTRY_TAG_CTGR_MAP,
            tagsUrl: Url.JOURNAL_ENTRY_TAGS,
            tagListTargetId: "journal_dream_tag_list_div",
            popupName: "dream search",
            modalKey: "journal_dream_reg",
            detailModalKey: "journal_dream_dtl",
            formSelector: "#journalDreamRegForm",
            tinymceId: "tinymce_journalDreamCn",
            tagInputSelector: "#journalDreamRegForm #tagListStr",
            itemClass: "journal-dream-item",
            contentClass: "journal-dream-content",
            cssPrefix: "dream",
            toggleIconSelector: "#dream-toggle-icon-",
            iconIdPrefix: "dream-toggle-icon-",
            storageKey: "collapsedJournalDreamIds",
            useTag: true,
            autoCreateChapterUrl: Url.JOURNAL_CHAPTER_DREAM_AUTO,
            hideSortWhenElseDream: true,
            hasDreamStates: true,
            highlightImportant: true,
            plugin: dreamPlugin,
        },
    };

    const create = function(config: Record<string, any>): dfModule {
        const module: dfModule = {
            STORAGE_KEY: config.storageKey,
            PROFILE: profile,
            profile: null,
            initialized: false,
            initPromise: null,
            inKeywordSearchMode: false,
            tagify: null,
            submitMode: "",

            init: async function(viewType: "LIST"|"CAL"|"DAILY"|"WEEKLY"|"SEARCH"): Promise<void> {
                if (this.initPromise) return this.initPromise;

                this.initPromise = (async () => {
                    if (config.useTag) await dF.JournalEntryTag.get(config.contentType).init();
                    dF.Lifecycle?.init?.();
                    this.viewType = viewType;
                    this.initialized = true;
                    console.log(`'dF.${config.moduleName}' module initialized.`);
                })();

                return this.initPromise;
            },

            refresh: function(): void {
                switch (this.viewType) {
                    case "LIST":
                        dF.JournalDay.yyMnthListAjax();
                        if (config.useTag) dF.JournalEntryTag.get(config.contentType).listAjax();
                        break;
                    case "CAL":
                        if (typeof Page.refreshEventList === "function") {
                            Page.refreshEventList();
                        } else if (typeof dF.JournalDayMeta?.listAjax === "function") {
                            dF.JournalDayMeta.listAjax();
                        }
                        if (config.useTag) dF.JournalEntryTag.get(config.contentType).listAjax();
                        break;
                    case "DAILY":
                    case "WEEKLY":
                        dF.JournalDay.refresh();
                        break;
                    case "SEARCH":
                        dF.JournalEntrySearch?.get?.(config.contentType)?.search?.();
                        break;
                    default:
                        if (typeof dF.JournalDay?.refresh === "function") dF.JournalDay.refresh();
                        break;
                }
                cF.ui.unblockUI();
            },

            initForm: function(obj: Record<string, any> = {}): void {
                cF.handlebars.modal(obj, config.modalKey, ["header"]);

                config.plugin.setupFormValidation(config, module);

                cF.tinymce.init(`#${config.tinymceId}`);
                cF.tinymce.setContentWhenReady(config.tinymceId, obj.content || "");

                if (config.useTag) {
                    module.tagify = cF.tagify.initWithCtgr(config.tagInputSelector, dF.JournalEntryTag.get(config.contentType).ctgrMap);
                }
            },

            resolveChapterList: function(day: Record<string, any> = {}): Record<string, any>[] {
                const chapterList: Record<string, any>[] = Array.isArray(day?.chapterList)
                    ? day.chapterList
                    : (Array.isArray(day?.journalChapterList) ? day.journalChapterList : []);
                if (config.chapterType == null) {
                    return chapterList.filter((chapter: Record<string, any>): boolean => chapter?.chapterType !== "DREAM");
                }
                return chapterList.filter((chapter: Record<string, any>): boolean => chapter?.chapterType === config.chapterType);
            },

            resolveDreamChapterList: function(day: Record<string, any> = {}): Record<string, any>[] {
                return module.resolveChapterList(day);
            },

            createDreamChapterAndOpenModal: function(
                journalDayId: string|number,
                stdrdDt: string,
                journalDateWeekDay: string,
                onReady?: () => void
            ): void {
                if (!config.autoCreateChapterUrl) return;

                const ajaxData: FormData = new FormData();
                ajaxData.append("journalDayId", String(journalDayId));

                cF.$ajax.multipart(config.autoCreateChapterUrl, ajaxData, function(res: AjaxResponse): void {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return;
                    }

                    module.openRegModalWithDayContext(
                        journalDayId,
                        res?.rsltObj?.id,
                        stdrdDt,
                        journalDateWeekDay,
                        onReady
                    );
                }, "block");
            },

            openRegModalWithDayContext: function(
                journalDayId: string|number,
                journalChapterId: string|number|undefined,
                stdrdDt: string,
                journalDateWeekDay: string,
                onReady?: () => void,
                initialObj: Record<string, any> = {}
            ): void {
                const url: string = cF.util.bindUrl(Url.JOURNAL_DAY, { id: journalDayId });
                const ajaxData: Record<string, any> = {
                    includeDreamChapter: config.chapterType != null,
                };
                cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                    if (!res.rslt) return;

                    const chapterList: Record<string, any>[] = module.resolveChapterList(res.rsltObj);
                    if (chapterList.length === 0) {
                        if (config.autoCreateChapterUrl) {
                            module.createDreamChapterAndOpenModal(journalDayId, stdrdDt, journalDateWeekDay, onReady);
                        } else if (config.noteChapterOnlyMessageKey) {
                            Swal.fire({ text: Message.get(config.noteChapterOnlyMessageKey) });
                        }
                        return;
                    }

                    const resolvedChapterId: number = chapterList.some((chapter: Record<string, any>): boolean => {
                        return Number(chapter?.id) === Number(journalChapterId);
                    })
                        ? Number(journalChapterId)
                        : Number(chapterList[0]?.id);

                    module.initForm({
                        ...initialObj,
                        journalDayId,
                        journalChapterId: resolvedChapterId,
                        stdrdDt,
                        journalDateWeekDay,
                        chapterList,
                    });
                    onReady?.();
                });
            },

            searchPopup: function(): void {
                const baseSearchUrl: string = resolveSearchUrl(config);
                const prefix: string = config.cssPrefix;
                const keyword: string = (document.querySelector(`#${prefix}SearchKeyword`) as HTMLInputElement)?.value;
                const url: string = `${baseSearchUrl}?searchKeywords=${keyword}`;
                const options: string = "width=1960,height=1440,top=0,left=270";
                const popup: Window = cF.ui.openPopup(url, `${prefix} search`, options);
                if (popup) popup.focus();
            },

            regModal: function({
                journalDayId,
                journalChapterId,
                stdrdDt,
                journalDateWeekDay,
            }: {
                journalDayId: string|number;
                journalChapterId?: string|number;
                stdrdDt: string;
                journalDateWeekDay: string;
            }): void {
                if (isNaN(Number(journalDayId))) return;
                if (journalChapterId != null && isNaN(Number(journalChapterId))) return;

                module.openRegModalWithDayContext(journalDayId, journalChapterId, stdrdDt, journalDateWeekDay);
            },

            submit: function(): void {
                tinymce.get(config.tinymceId).save();
                module.submitMode = "submit";
                $(config.formSelector).submit();
            },

            preview: function(): void {
                tinymce.get(config.tinymceId).save();
                module.submitMode = "preview";
                $(config.formSelector).submit();
            },

            submitHandler: function(): boolean {
                if (module.submitMode === "preview") {
                    const width: number = dF.JournalEntry.resolveJournalDayPreviewWidth();
                    const height: number = Math.round(window.innerHeight * 0.9);
                    const left: number = Math.max(0, Math.round((window.screen.availWidth - width) / 2));
                    const top: number = Math.max(0, Math.round((window.screen.availHeight - height) / 2));
                    const popupNm: string = `journal_entry_preview_${String(config.entryType).toLowerCase()}`;
                    const option: string = `width=${width},height=${height},left=${left},top=${top},scrollbars=yes,resizable=yes`;
                    const popup: Window | null = cF.ui.openPopup("", popupNm, option);
                    popup?.focus();
                    const previewUrl: string = Url.JOURNAL_ENTRY_PREVIEW_POP;
                    $(config.formSelector)
                        .attr("method", "post")
                        .attr("action", previewUrl)
                        .attr("target", popupNm);
                    return true;
                }
                if (module.submitMode === "submit") {
                    $(config.formSelector)
                        .attr("method", "post")
                        .removeAttr("action")
                        .removeAttr("target");
                    module.regAjax();
                }
                return false;
            },

            regAjax: function(): void {
                const id: string = cF.util.getInputValue(`${config.formSelector} [name='id']`);
                const isMdf: boolean = cF.util.isNotEmpty(id);
                Swal.fire({
                    text: Message.get(isMdf ? "view.cnfm.mdf" : "view.cnfm.reg"),
                    showCancelButton: true,
                }).then(function(result: SwalResult): void {
                    if (!result.value) return;

                    const url: string = isMdf ? cF.util.bindUrl(config.itemUrl, { id }) : config.listUrl;
                    const ajaxData: FormData = new FormData(document.querySelector(config.formSelector) as HTMLFormElement);
                    cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                        Swal.fire({ text: res.message })
                            .then(function(): void {
                                if (!res.rslt) return;
                                module.refresh();
                            });
                    }, "block");
                });
            },

            dtlModal: function(id: string|number): void {
                if (isNaN(Number(id)) || config.detailModalKey == null) return;

                closeOpenModals();
                const self = this;
                const func: string = arguments.callee.name;
                const args: any[] = Array.from(arguments);

                const url: string = cF.util.bindUrl(config.itemUrl, { id });
                cF.ajax.get(url, null, function(res: AjaxResponse): void {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return;
                    }
                    cF.handlebars.modal(res.rsltObj, config.detailModalKey);
                    ModalHistory.push(self, func, args);
                });
            },

            mdfModal: function(id: string|number): void {
                if (isNaN(Number(id))) return;

                closeOpenModals();
                const self = this;
                const func: string = arguments.callee.name;
                const args: any[] = Array.from(arguments);

                const url: string = cF.util.bindUrl(config.itemUrl, { id });
                cF.ajax.get(url, null, function(res: AjaxResponse): void {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return;
                    }

                    const { rsltObj } = res;
                    module.openRegModalWithDayContext(
                        rsltObj.journalDayId,
                        rsltObj.journalChapterId,
                        rsltObj.stdrdDt,
                        rsltObj.journalDateWeekDay,
                        function(): void {
                            ModalHistory.push(self, func, args);
                        },
                        rsltObj
                    );
                });
            },

            delAjax: function(id: string|number): void {
                if (isNaN(Number(id))) return;

                Swal.fire({
                    text: Message.get("view.cnfm.del"),
                    showCancelButton: true,
                }).then(function(result: SwalResult): void {
                    if (!result.value) return;

                    const url: string = cF.util.bindUrl(config.itemUrl, { id });
                    cF.$ajax.delete(url, null, function(res: AjaxResponse): void {
                        Swal.fire({ text: res.message })
                            .then(function(): void {
                                if (!res.rslt) return;
                                module.refresh();
                            });
                    }, "block");
                });
            },

            toggleStateAjax: function(id: string|number, stateKey: string, { onOffFunc }: { onOffFunc: Function }): void {
                if (isNaN(Number(id))) return;

                const item = document.querySelector(`.${config.itemClass}[data-id='${id}']`) as HTMLElement;
                const cacheContext = dF.State.resolveJournalCacheContext(item);
                const payload = { id, contentType: config.contentType, stateKey, cacheContext };
                dF.State.toggleAjax(payload, function(res: AjaxResponse): void {
                    if (!item) return;
                    const lowerStateKey: string = stateKey.toLowerCase();
                    item.dataset[lowerStateKey] = res.rsltSts === "ON" ? "Y" : "N";
                    const icon: HTMLElement = item.querySelector(`.icon-${lowerStateKey}`);
                    icon?.classList.toggle("d-none", res.rsltSts !== "ON");
                    const chk: HTMLInputElement = item.querySelector(`.${config.cssPrefix}-context-${lowerStateKey}-check`);
                    if (chk) chk.checked = res.rsltSts === "ON";
                    onOffFunc(res, item);
                });
            },

            collapseAjax: function(id: string|number): void {
                if (isNaN(Number(id))) return;

                const onOffFunc: Function = function(res: AjaxResponse, item: HTMLElement): void {
                    const content: HTMLDivElement = item.querySelector(`div.${config.contentClass} .journal-content`);
                    if (!content) return console.warn("content not found.");

                    content.classList.toggle("collapsed", res.rsltSts === "ON");
                    item.classList.toggle("is-collapsed", res.rsltSts === "ON");
                };
                this.toggleStateAjax(id, "COLLAPSED", { onOffFunc });
            },

            /**
             * 컨텍스트 메뉴 스위치 값에 따라 일기 라이프사이클을 RESOLVED 또는 OPEN으로 설정한다.
             *
             * RESOLVED는 더 이상 상태 토글이 아니다. 이 스위치는 라이프사이클을 저장하고,
             * 백엔드가 영속화하는 글접기 파생 동작을 화면에도 즉시 반영한다.
             *
             * @param id journal entry ID
             * @param trigger 원하는 완료 여부를 나타내는 checkbox
             */
            resolveAjax: function(id: string|number, trigger?: HTMLInputElement): void {
                if (isNaN(Number(id))) return;

                const lifecycleKey: string = trigger?.checked ? "RESOLVED" : "OPEN";
                this.setLifecycleAjax(id, lifecycleKey);
            },

            /**
             * 일기 라이프사이클을 명시적으로 설정한다.
             *
             * @param id 저널 entry ID
             * @param lifecycleKey 설정할 라이프사이클 키
             */
            setLifecycleAjax: function(id: string|number, lifecycleKey: string): void {
                if (isNaN(Number(id))) return;

                const item = document.querySelector(`.${config.itemClass}[data-id='${id}']`) as HTMLElement;
                const cacheContext = dF.Lifecycle.resolveJournalCacheContext(item);
                const payload = { id, contentType: config.contentType, lifecycleKey, cacheContext };
                dF.Lifecycle.setAjax(payload, function(_res: AjaxResponse): void {
                    if (!item) return;

                    item.dataset.lifecycle = lifecycleKey;
                    item.dataset.resolved = lifecycleKey === "RESOLVED" ? "Y" : "N";

                    const idx: HTMLElement = item.querySelector(`.journal-${config.cssPrefix}-idx`);
                    idx?.classList.toggle("text-success", lifecycleKey === "RESOLVED");

                    const resolvedChk: HTMLInputElement = item.querySelector(`.${config.cssPrefix}-context-resolved-check`);
                    if (resolvedChk) resolvedChk.checked = lifecycleKey === "RESOLVED";

                    const lifecycleChecks: NodeListOf<HTMLInputElement> = item.querySelectorAll(`.${config.cssPrefix}-context-lifecycle-check`);
                    lifecycleChecks.forEach(function(chk: HTMLInputElement): void {
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

                    const content: HTMLDivElement = item.querySelector(`div.${config.contentClass} .journal-content`);
                    if (!content) console.warn("content not found.");
                    content?.classList.add("collapsed");
                    item.dataset.collapsed = "Y";
                    item.classList.add("is-collapsed");

                    const collapsedChk: HTMLInputElement = item.querySelector(`.${config.cssPrefix}-context-collapsed-check`);
                    if (collapsedChk) collapsedChk.checked = true;
                    const icon: HTMLElement = item.querySelector(".icon-collapsed");
                    icon?.classList.toggle("d-none", false);
                });
            },

            imprtcAjax: function(id: string|number): void {
                if (isNaN(Number(id))) return;

                const onOffFunc: Function = function(res: AjaxResponse, item: HTMLElement): void {
                    const wrapper: HTMLDivElement = item.querySelector(`div.${config.contentClass}`);
                    const content: HTMLDivElement = item.querySelector(`div.${config.contentClass} .journal-content`);
                    if (!content) return console.warn("content not found.");

                    wrapper?.classList.remove("bg-secondary");
                    content.classList.toggle("imprtc", res.rsltSts === "ON");
                };
                this.toggleStateAjax(id, "IMPRTC", { onOffFunc });
            },

            refrncAjax: function(id: string|number): void {
                if (isNaN(Number(id))) return;

                const onOffFunc: Function = function(res: AjaxResponse, item: HTMLElement): void {
                    const wrapper: HTMLDivElement = item.querySelector(`div.${config.contentClass}`);
                    const content: HTMLDivElement = item.querySelector(`div.${config.contentClass} .journal-content`);
                    if (!content) return console.warn("content not found.");

                    wrapper?.classList.remove("bg-secondary");
                    content.classList.toggle("refrnc", res.rsltSts === "ON");
                };
                this.toggleStateAjax(id, "REFRNC", { onOffFunc });
            },

            nhtmrAjax: function(id: string|number): void {
                if (isNaN(Number(id))) return;

                const onOffFunc: Function = function(res: AjaxResponse, item: HTMLElement): void {
                    item.querySelector(".dream-nhtmr-badge")?.classList.toggle("d-none", res.rsltSts !== "ON");
                };
                this.toggleStateAjax(id, "NHTMR", { onOffFunc });
            },

            hallucAjax: function(id: string|number): void {
                if (isNaN(Number(id))) return;

                const onOffFunc: Function = function(res: AjaxResponse, item: HTMLElement): void {
                    item.querySelector(".dream-halluc-badge")?.classList.toggle("d-none", res.rsltSts !== "ON");
                };
                this.toggleStateAjax(id, "HALLUC", { onOffFunc });
            },

            toggle: function(id: string|number, trigger: HTMLElement): void {
                if (isNaN(Number(id))) return;

                const item: HTMLElement = trigger.closest(`.${config.itemClass}[data-id='${id}']`);
                if (!item) return console.log("item not found.");

                const content: HTMLElement = item.querySelector(`.${config.contentClass} .journal-content`);
                if (!content) return console.log("content not found.");

                const icon: HTMLElement = config.toggleIconSelector.startsWith("#")
                    ? document.querySelector(`${config.toggleIconSelector}${id}`)
                    : item.querySelector(config.toggleIconSelector);
                if (!icon) console.log("icon not found.");

                const collapsedIds = new Set(JSON.parse(localStorage.getItem(config.storageKey) || "[]"));
                const isCollapsed: boolean = content.classList.contains("collapsed");
                if (isCollapsed) {
                    content.classList.remove("collapsed");
                    item.classList.remove("is-collapsed");
                    icon?.classList.replace("bi-arrows-expand", "bi-arrows-collapse");
                    collapsedIds.delete(String(id));
                    collapsedIds.delete(id);
                } else {
                    content.classList.add("collapsed");
                    item.classList.add("is-collapsed");
                    icon?.classList.replace("bi-arrows-collapse", "bi-arrows-expand");
                    collapsedIds.add(String(id));
                }

                if (config.plugin.persistToggleToStorage) {
                    localStorage.setItem(config.storageKey, JSON.stringify(Array.from(collapsedIds)));
                }
            },

            initCollapseState: function(): void {
                const collapsedIds = new Set(JSON.parse(localStorage.getItem(config.storageKey) || "[]"));
                document.querySelectorAll(`.${config.itemClass} .${config.contentClass}`).forEach((item: HTMLElement): void => {
                    const id: string = item.dataset.id;
                    const content: HTMLElement = item.querySelector(".journal-content");
                    const icon: HTMLElement = config.toggleIconSelector.startsWith("#")
                        ? document.querySelector(`${config.toggleIconSelector}${id}`)
                        : item.querySelector(config.toggleIconSelector);
                    if (!icon) console.log("icon not found.");
                    if (id && collapsedIds.has(id)) {
                        content?.classList.add("collapsed");
                        item.closest(`.${config.itemClass}`)?.classList.add("is-collapsed");
                        icon?.classList.replace("bi-arrows-collapse", "bi-arrows-expand");
                    }
                });
            },

            copy: function(id: string|number): void {
                if (isNaN(Number(id))) return;

                const url: string = cF.util.bindUrl(config.itemUrl, { id });
                cF.ajax.get(url, null, function(res: AjaxResponse): void {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return;
                    }
                    const rsltObj: Record<string, any> = res.rsltObj;
                    const { stdrdDt, journalDateWeekDay } = rsltObj;
                    const titleLine: string = config.plugin.extractTitleLine(rsltObj);
                    const date: string = stdrdDt + " (" + journalDateWeekDay + ")" + "\r\n";
                    const resultCn: string = rsltObj.content;
                    const replacedCn: string = resultCn
                        .replace(/<\s*hr\b[^>]*\/?>/gi, "\n---\n")
                        .replace(/<\s*br\s*\/?>/gi, "\n")
                        .replace(/<\s*\/?p[^>]*>/gi, "\n");
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
            },

            buildViewModel: function(entry: Record<string, any>, profileName: string): Record<string, any> {
                const selectedProfile: any = profile[profileName];
                if (!selectedProfile) throw new Error(`Unknown render profile: ${profileName}`);

                return {
                    ...entry,
                    view: selectedProfile,
                    contentClass: [
                        "journal-content",
                        selectedProfile.collapsed && hasState(entry, "COLLAPSED")
                            ? config.plugin.collapseClass
                            : null,
                        hasState(entry, "IMPRTC") ? "imprtc" : null,
                        hasState(entry, "REFRNC") ? "refrnc" : null,
                    ].filter(Boolean).join(" "),
                };
            },
        };

        return module;
    };

    const modules: Record<string, any> = {};
    Object.keys(configs).forEach(function(contentType: string): void {
        modules[contentType] = create(configs[contentType]);
    });

    return {
        initialized: true,
        init: function(): void {},
        get: function(contentType: string): dfModule {
            return modules[contentType];
        },
        getMeta: function(contentType: string): Record<string, any> {
            return configs[contentType];
        },
        getContentTypes: function(): string[] {
            return Object.keys(configs);
        },
        getTaggableContentTypes: function(): string[] {
            return Object.keys(configs).filter(function(contentType: string): boolean {
                return configs[contentType]?.useTag === true;
            });
        },
        getSearchPopupContentTypes: function(): string[] {
            return Object.keys(configs).filter(function(contentType: string): boolean {
                return cF.util.isNotEmpty(configs[contentType]?.searchInputSelector);
            });
        },
        initAll: function(viewType: "LIST"|"CAL"|"DAILY"|"WEEKLY"|"SEARCH"): Promise<void[]> {
            return Promise.all(
                Object.keys(configs).map(function(contentType: string): Promise<void> {
                    return modules[contentType].init(viewType);
                })
            );
        },
        bindSearchPopupEnterKeys: function(): void {
            dF.JournalEntry.getSearchPopupContentTypes().forEach(function(contentType: string): void {
                const meta: Record<string, any> = configs[contentType];
                if (cF.util.isEmpty(meta?.searchInputSelector)) return;
                cF.util.enterKey(meta.searchInputSelector, dF.JournalEntry.get(contentType).searchPopup);
            });
        },

        /**
         * 저널 일자 화면 본문 영역(월간/주간/달력/메타 래퍼) 너비에 맞춘 미리보기 팝업 폭.
         */
        resolveJournalDayPreviewWidth: function(): number {
            // 본문 줄바꿈 기준과 최대한 일치시키기 위해 페이지 래퍼보다
            // 실제 본문이 렌더링되는 카드/리스트 컨테이너 폭을 우선 사용한다.
            const contentShell: HTMLElement | null = document.querySelector(
                "#journal_day_list_div, .journal-day-monthly-page .card.post, .journal-day-weekly-page .card.post, .journal-day-calendar-page .card.post, .journal-day-meta-page .card.post"
            );
            if (contentShell) {
                const shellWidth: number = Math.round(contentShell.getBoundingClientRect().width);
                if (shellWidth > 320) return shellWidth;
            }

            const journalShell: HTMLElement | null = document.querySelector(
                ".journal-day-monthly-page, .journal-day-weekly-page, .journal-day-calendar-page, .journal-day-meta-page"
            );
            if (journalShell) {
                const shellWidth: number = Math.round(journalShell.getBoundingClientRect().width);
                // 페이지 래퍼 폭은 본문보다 넓을 수 있어 보정값을 적용한다.
                if (shellWidth > 320) return Math.max(480, shellWidth - 64);
            }

            const container: HTMLElement | null = document.querySelector("#kt_app_content_container");
            if (container) {
                const containerWidth: number = Math.round(container.getBoundingClientRect().width);
                if (containerWidth > 320) return Math.max(480, containerWidth - 64);
            }
            return Math.min(Math.max(Math.round(window.innerWidth * 0.92), 480), 1600);
        },

    };
})();
