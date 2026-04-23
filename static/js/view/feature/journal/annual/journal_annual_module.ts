/**
 * journal_annual_module.ts
 * 저널 결산 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.JournalAnnual = (function(): dfModule {
    const createEntryListConfig = function(contentType: string, targetId: string, overrides: Record<string, any> = {}): Record<string, any> {
        const meta: Record<string, any> = dF.JournalEntry.getMeta(contentType);
        return {
            targetId,
            contentType,
            module: meta.moduleExpr,
            tagModule: meta.tagModuleExpr,
            contentLabel: meta.contentLabel,
            emptyLabel: meta.emptyLabel,
            cssPrefix: meta.cssPrefix,
            iconIdPrefix: meta.iconIdPrefix,
            highlightImportant: meta.highlightImportant,
            showDreamStates: meta.hasDreamStates,
            ...overrides,
        };
    };

    const entryListConfigs: Record<string, Record<string, any>> = {
        DIARY: createEntryListConfig("JOURNAL_DIARY", "journal_annual_diary_list_div", {
            contentPaddingClass: "p-2",
            collapse: "collapse-4",
        }),
        DREAM: createEntryListConfig("JOURNAL_DREAM", "journal_annual_imprtc_dream_list_div", {
            contentPaddingClass: "p-3",
            contextFirst: true,
        }),
    };

    const tagListConfigs: Record<string, Record<string, any>> = {
        DAY: {
            targetId: "journal_annual_day_tag_list_div",
            label: Message.get("txt.day.tag"),
            module: "dF.JournalDayTag",
            tagListDivId: "journal_day_tag_list_div",
        },
        DIARY: {
            targetId: "journal_annual_diary_tag_list_div",
            label: Message.get("txt.diary.tag"),
            module: dF.JournalEntry.getMeta("JOURNAL_DIARY").tagModuleExpr,
            tagListDivId: dF.JournalEntry.getMeta("JOURNAL_DIARY").tagListTargetId,
        },
        DREAM: {
            targetId: "journal_annual_dream_tag_list_div",
            label: Message.get("txt.dream.tag"),
            module: dF.JournalEntry.getMeta("JOURNAL_DREAM").tagModuleExpr,
            tagListDivId: dF.JournalEntry.getMeta("JOURNAL_DREAM").tagListTargetId,
        },
    };

    const renderToTarget = function(data: Record<string, any>, templateId: string, targetId: string): void {
        const actual: string = cF.handlebars.compile(data, templateId);
        if (actual == null) return console.error(`template compile error: ${templateId}`);

        const targetElement: HTMLElement|null = document.getElementById(targetId);
        if (!targetElement) return console.error(`target element not found: ${targetId}`);

        targetElement.innerHTML = "";
        targetElement.insertAdjacentHTML("beforeend", actual);
        targetElement.querySelectorAll("[data-bs-toggle='tooltip']").forEach((tooltipEl: HTMLElement): void => {
            new bootstrap.Tooltip(tooltipEl);
        });
    };

    return {
        initialized: false,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.JournalAnnual.initialized) return;

            dF.JournalAnnual.initialized = true;
            console.log("'dF.JournalAnnual' module initialized.");
        },

        /**
         * form init
         * @param {Record<string, any>} obj - 폼에 바인딩할 데이터
         */
        initForm: function(obj: Record<string, any> = {}): void {
            /* show modal */
            cF.handlebars.modal(obj, "journal_annual_reg", ["header"]);

            /* jquery validation */
            cF.validate.validateForm("#journalAnnualRegForm", dF.JournalAnnual.regAjax);
            /* tagify */
            cF.tagify.initWithCtgr("#journalAnnualRegForm #tagListStr", undefined);
            // tinymce editor reset
            cF.tinymce.init('#tinymce_journalAnnualCn');
            cF.tinymce.setContentWhenReady("tinymce_journalAnnualCn", obj.content || "");
        },

        /**
         * 상세 화면으로 이동 (key로 조회)
         */
        listAjax: function(): void {
            const url: string = Url.JOURNAL_ANNUALS;
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltList } = res;
                cF.ui.closeModal();
                cF.handlebars.template(rsltList, "journal_annual_list");
                KTMenu.createInstances();
            }, "block");
        },

        /**
         * 상세 화면으로 이동 (년도로 조회)
         * @param {string|number} yy - 조회할 년도.
         */
        dtlView: function(yy: string|number): void {
            if (isNaN(Number(yy))) return;

            location.href = cF.util.bindUrl(Url.JOURNAL_ANNUAL_VIEW, {yy}) + "?section=DIARY";
        },

        /**
         * 섹션 전환 이동 (년도로 조회)
         * @param {"DIARY"|"DREAM"} section - 조회 섹션
         */
        dtlViewWithSection: function(section: "DIARY"|"DREAM"): void {
            const yy: string = cF.util.getPathVariableFromUrl(/\/annual\/(\d{4})(?:\.do)?$/);
            if (!yy) return console.warn("invalid yy.");

            location.href = cF.util.bindUrl(Url.JOURNAL_ANNUAL_VIEW, {yy}) + `?section=${section}`;
        },

        /**
         * 상세 조회 (Ajax) (년도로 조회)
         * @param {string|number} yy - 조회할 년도.
         */
        dtlAjax: function(yy: string|number): void {
            const url: string = cF.util.bindUrl(Url.JOURNAL_ANNUAL, { yy });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const rsltObj: Record<string, any> = res.rsltObj;
                /* show modal */
                cF.handlebars.template(rsltObj, "journal_annual_dtl");
            });
        },

        /**
         * URL 파라미터로부터 파라미터 객체 초기화
         */
        toggleParam: function(): void {
            const showImprtc: boolean = $("#toggleImprtc").is(":checked");
            const showRefrnc: boolean = $("#toggleRefrnc").is(":checked");

            // URL 동기화
            const url = new URL(window.location.href);
            url.searchParams.set("showImprtc", String(showImprtc));
            url.searchParams.set("showRefrnc", String(showRefrnc));
            window.history.replaceState(null, "", url.toString());

            // 재조회
            const yy: string = cF.util.getPathVariableFromUrl(/\/annual\/(\d{4})(?:\.do)?$/);
            const section: string = cF.util.getUrlParam("section");
            switch (section) {
                case "DIARY":
                    dF.JournalAnnual.getAnnualDiaryListAjax(yy);
                    break;
                case "DREAM":
                    dF.JournalAnnual.getAnnualDreamListAjax(yy);
                    break;
            }
        },

        /**
         * 중요 일기 목록 조회 (Ajax) (년도로 조회)
         * @param {string|number} yy - 조회할 년도.
         */
        getAnnualDiaryListAjax: function(yy: string|number): void {
            const showImprtc: boolean = $("#toggleImprtc").is(":checked");
            const showRefrnc: boolean = $("#toggleRefrnc").is(":checked");

            // 둘 다 false → 조회 의미 없음
            if (!showImprtc && !showRefrnc) {
                dF.JournalAnnual.renderEntryList([], "DIARY");
                return;
            }

            const url: string = cF.util.bindUrl(Url.JOURNAL_ANNUAL_DIARIES, { yy });
            const ajaxData: Record<string, any> = {
                showImprtc,
                showRefrnc
            };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }

                const viewModels: any[] = res.rsltList.map((diary: any): void =>
                    dF.JournalEntry.get("JOURNAL_DIARY").buildViewModel(diary, 'ANNUAL')
                );
                dF.JournalAnnual.renderEntryList(viewModels, "DIARY");
                document.querySelectorAll(".journal-content.collapsed").forEach(el => el.classList.remove("collapsed"));
                KTMenu.createInstances();
            });
        },

        /**
         * 중요 꿈 목록 조회 (Ajax) (년도로 조회)
         * @param {string|number} yy - 조회할 년도.
         */
        renderEntryList: function(list: Record<string, any>[] = [], type: "DIARY"|"DREAM"): void {
            const config: Record<string, any> = entryListConfigs[type];
            renderToTarget({
                list,
                ...config,
            }, "journal_annual_entry_list", config.targetId);
        },

        getAnnualDreamListAjax: function(yy: string|number): void {
            const url: string = cF.util.bindUrl(Url.JOURNAL_ANNUAL_DREAMS, { yy });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const viewModels: any[] = res.rsltList.map((dream: any): void =>
                    dF.JournalEntry.get("JOURNAL_DREAM").buildViewModel(dream, 'ANNUAL')
                );
                dF.JournalAnnual.renderEntryList(viewModels, "DREAM");
                document.querySelectorAll(".journal-content.collapsed").forEach(el => el.classList.remove("collapsed"));
                KTMenu.createInstances();
            });
        },

        /**
         * 중요 일기 목록 조회 (Ajax) (년도로 조회)
         * @param {string|number} yy - 조회할 년도.
         * @param {"DAY"|"DIARY"|"DREAM"} type - 조회 타입
         */
        getTagListAjax: function(yy: string|number, type: "DAY"|"DIARY"|"DREAM"): void {
            const url: string = cF.util.bindUrl(Url.JOURNAL_ANNUAL_TAGS, { yy }) + `?type=${type}`;
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltList = [] } = res;
                dF.JournalAnnual.renderTagList(rsltList, type);
            });
        },

        /**
         * 목록 화면으로 이동
         */
        renderTagList: function(list: Record<string, any>[] = [], type: "DAY"|"DIARY"|"DREAM"): void {
            const config: Record<string, any> = tagListConfigs[type];
            renderToTarget({
                list,
                ...config,
            }, "journal_annual_entry_tag_list", config.targetId);
        },

        list: function(): void {
            cF.ui.blockUIReplace(Url.JOURNAL_ANNUAL_LIST);
        },

        /**
         * 특정 년도 결산 생성 (Ajax)
         * @param {string|number} yy - 결산을 생성할 년도.
         */
        makeYyAnnualAjax: function(yy: string|number): void {
            const yYElmt: HTMLSelectElement = document.querySelector("#listForm #yy");
            if (yy == null) yy = yYElmt.value;
            if (cF.util.isEmpty(yy)) {
                cF.ui.swalOrAlert("yy는 필수 항목입니다.");
                return;
            }
            const url: string = Url.JOURNAL_ANNUAL_MAKE_AJAX;
            const ajaxData: Record<string, any> = { "yy": yy };
            cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                Swal.fire({ text: res.message })
                    .then(function(): void {
                        if (res.rslt) cF.ui.blockUIReload();
                    });
            }, "block");
        },

        /**
         * 전체 년도 결산 갱신 (Ajax)
         */
        makeTotalAnnualAjax: function(): void {
            const url: string = Url.JOURNAL_ANNUAL_MAKE_TOTAL_AJAX;
            cF.$ajax.post(url, null, function(res: AjaxResponse): void {
                Swal.fire({ text: res.message })
                    .then(function(): void {
                        if (res.rslt) cF.ui.blockUIReload();
                    });
            }, "block");
        },

        /**
         * 꿈 기록 완료 처리 (Ajax)
         * @param {string|number} id - 글 번호.
         */
        comptAjax: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            const url: string = Url.JOURNAL_ANNUAL_DREAM_COMPT_AJAX;
            const ajaxData: Record<string, any> = { id };
            cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                Swal.fire({ text: res.message })
                    .then(function(): void {
                        if (res.rslt) cF.ui.blockUIReload();
                    });
            }, "block");
        },

        /**
         * form submit
         */
        submit: function(): void {
            tinymce.get("tinymce_journalAnnualCn").save();
            $("#journalAnnualRegForm").submit();
        },

        /**
         * 등록(수정) 모달 호출
         * @param {string|number} yy - 년도.
         */
        mdfModal: function(yy: string|number): void {
            if (isNaN(Number(yy))) return;

            const url: string = cF.util.bindUrl(Url.JOURNAL_ANNUAL, { yy });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltObj } = res;
                /* initialize form. */
                dF.JournalAnnual.initForm(rsltObj);
            });
        },

        /**
         * 등록 (Ajax)
         */
        regAjax: function(): void {
            Swal.fire({
                text: Message.get("view.cnfm.save"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = Url.JOURNAL_ANNUAL_REG_AJAX;
                const ajaxData: FormData = new FormData(document.getElementById("journalAnnualRegForm") as HTMLFormElement);
                cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            cF.ui.blockUIReload();
                        });
                }, "block");
            });
        }
    }
})();
