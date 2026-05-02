/**
 * journal_entry_search_module.ts
 * Common search module for journal diary/dream entries.
 */
if (typeof dF === "undefined") { var dF = {} as any; }

dF.JournalEntrySearch = (function(): dfModule {
    const createConfig = function(contentType: string): Record<string, any> {
        const meta: Record<string, any> = dF.JournalEntry.getMeta(contentType);
        return {
            moduleName: `JournalEntrySearch[${contentType}]`,
            listUrl: meta.listUrl,
            exportUrl: meta.exportUrl,
            entryType: meta.entryType,
            templateId: "journal_entry_search",
            contentType,
            module: meta.moduleExpr,
            tagModule: meta.tagModuleExpr,
            contentLabel: meta.contentLabel,
            emptyLabel: meta.emptyLabel,
            cssPrefix: meta.cssPrefix,
            iconIdPrefix: meta.iconIdPrefix,
            showDreamStates: meta.hasDreamStates,
            highlightImportant: meta.highlightImportant,
            rightBorderClass: meta.rightBorderClass,
        };
    };

    const modules: Record<string, any> = {};

    const hasSearchCondition = function(ajaxData: Record<string, any>): boolean {
        const hasKeyword: boolean = Array.isArray(ajaxData["searchKeywords"])
            ? ajaxData["searchKeywords"]?.some((k: string): boolean => cF.util.isNotEmpty(k?.trim()))
            : cF.util.isNotEmpty(ajaxData["searchKeywords"]);
        const hasTag: boolean = Array.isArray(ajaxData["tagIds"]) ? ajaxData["tagIds"].length > 0 : !!ajaxData["tagIds"];
        return hasKeyword || hasTag;
    };

    const create = function(contentType: string, config: Record<string, any>): dfModule {
        const module: dfModule = {
            initialized: false,
            currentResults: [] as any[],
            currentSearchParams: {} as Record<string, any>,

            init: function(): void {
                if (module.initialized) return;
                module.initialized = true;
                console.log(`'dF.${config.moduleName}' module initialized.`);
            },

            initSearch: function(): void {
                module.initKeyword();
                module.initTag();
                module.initSort();
                module.search();
            },

            initKeyword: function(): void {
                module.clearKeywordFields();

                const params: URLSearchParams = new URLSearchParams(window.location.search);
                const keywords: string[] = params.getAll("searchKeywords");
                if (keywords.length > 0) {
                    keywords.forEach((k: string): void => module.addKeyword(k));
                } else {
                    module.addKeyword();
                }
                $("#keywordDisplay div").removeClass("text-muted").addClass("text-primary");
            },

            initTag: function(): void {
                const params: URLSearchParams = new URLSearchParams(window.location.search);
                const tagIds: string[] = params.getAll("tagIds");
                if (tagIds.length > 0) {
                    tagIds.forEach((tagId: string): void => {
                        const tag = dF.JournalEntryTag.get(contentType).list.find(
                            (t: any): boolean => t.id === Number(tagId)
                        );
                        if (!tag) return;
                        module.select(tagId, tag.tagNm);
                    });
                }
            },

            /**
             * 팝업 재진입/새로고침 시 URL query 의 정렬 방향을 검색 폼과 아이콘에 복원한다.
             */
            initSort: function(): void {
                const params: URLSearchParams = new URLSearchParams(window.location.search);
                const sort: string = params.get("sort") === "asc" ? "asc" : "desc";
                $("#sortInput").val(sort);

                const icon = $(".bi-sort-down-alt, .bi-sort-up");
                icon.toggleClass("bi-sort-down-alt", sort === "asc");
                icon.toggleClass("bi-sort-up", sort !== "asc");
            },

            addKeyword: function(value?: string): void {
                value = value ?? cF.util.getInputValue("#keywordInput");
                value = value?.trim();
                if (!value) return;

                const normalized: string = value.toLowerCase();
                const exists: boolean = $("#journalKeywordHiddenContainer input[name='searchKeywords']")
                    .filter(function(): boolean {
                        const v = $(this).val() as string | undefined;
                        return v?.toLowerCase() === normalized;
                    }).length > 0;
                if (exists) return;

                const inputContainer: HTMLElement = document.getElementById("journalKeywordHiddenContainer");
                const input: HTMLInputElement = document.createElement("input");
                input.type = "hidden";
                input.name = "searchKeywords";
                input.value = value;
                inputContainer.appendChild(input);

                const statusContainer: HTMLElement = document.getElementById("keywordDisplay");
                const statusBadge: HTMLDivElement = document.createElement("div");
                statusBadge.className = "badge badge-light-secondary keyword-wrapper fw-lighter d-flex align-items-center gap-2 px-3 py-2 text-muted";
                statusBadge.dataset.value = value;
                statusBadge.innerHTML = `
                    ${value}
                    <i class="bi bi-x cursor-pointer" onclick="dF.JournalEntrySearch.get('${contentType}').removeKeyword('${value}')"></i>
                `;
                statusContainer.appendChild(statusBadge);
                $("#msgDisplay").empty();
            },

            removeKeyword: function(value: string): void {
                $("#journalKeywordHiddenContainer input[name='searchKeywords']")
                    .filter(function(): boolean {
                        return $(this).val() === value;
                    })
                    .remove();
                $("#keywordDisplay div.keyword-wrapper")
                    .filter(function(): boolean {
                        return $(this).attr("data-value") === value;
                    })
                    .remove();
                module.search();
            },

            clearKeywordFields: function(): void {
                $("#journalKeywordContainer").empty();
                $("#journalTagNoContainer").empty();
            },

            toggleSort: function(): void {
                const $sortInput = $("#sortInput");
                const current = $sortInput.val();
                const next: "desc"|"asc" = current === "desc" ? "asc" : "desc";
                $sortInput.val(next);

                const icon = $(".bi-sort-down-alt, .bi-sort-up");
                icon.toggleClass("bi-sort-down-alt bi-sort-up");

                module.search();
            },

            resetSearch: function(): void {
                window.location.href = window.location.pathname;
            },

            select: function(tagId: string|number, tagNm: string): void {
                const inputContainer: HTMLElement = document.getElementById("journalTagNoHiddenContainer");
                const input: HTMLInputElement = document.createElement("input");
                input.type = "hidden";
                input.name = "tagIds";
                input.value = tagId as string;
                inputContainer.appendChild(input);

                const tagContainer: HTMLElement = document.getElementById("tagDisplay");
                const tagBadge: HTMLDivElement = document.createElement("div");
                tagBadge.className = "badge badge-light-primary tag-wrapper fw-lighter d-flex align-items-center gap-2 px-3 py-2 text-primary";
                tagBadge.dataset.value = tagId as string;
                tagBadge.innerHTML = `
                    #${tagNm}
                    <i class="bi bi-x cursor-pointer" onclick="dF.JournalEntrySearch.get('${contentType}').removeTag('${tagId}')"></i>
                `;
                tagContainer.appendChild(tagBadge);
                $("#msgDisplay").empty();

                module.search();
            },

            removeTag: function(value: string): void {
                $("#journalTagNoHiddenContainer input[name='tagIds']")
                    .filter(function(): boolean {
                        return $(this).val() === value;
                    })
                    .remove();
                $("#tagDisplay div.tag-wrapper")
                    .filter(function(): boolean {
                        return $(this).attr("data-value") === value;
                    })
                    .remove();
                module.search();
            },

            search: function(): void {
                const formArray: Record<string, any> = $("#listForm").serializeArray();
                const ajaxData: Record<string, any> = {};
                formArray.forEach((item: any): void => {
                    if (ajaxData[item.name]) {
                        if (!Array.isArray(ajaxData[item.name])) ajaxData[item.name] = [ajaxData[item.name]];
                        ajaxData[item.name].push(item.value);
                    } else {
                        ajaxData[item.name] = item.value;
                    }
                });
                ajaxData.type = config.entryType;
                $("#msgDisplay").empty();
                if (!hasSearchCondition(ajaxData)) {
                    $("#msgDisplay").text("검색 조건을 하나 이상 입력하세요.");
                    cF.handlebars.template({
                        list: [],
                        emptyLabel: config.emptyLabel,
                    }, config.templateId);
                    return;
                }

                cF.ajax.get(config.listUrl, ajaxData, function(res: AjaxResponse): void {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return;
                    }
                    const viewModels: any[] = res.rsltList.map((entry: any): any =>
                        dF.JournalEntry.get(contentType).buildViewModel(entry, "SEARCH")
                    );
                    cF.handlebars.template({
                        list: viewModels,
                        contentType: config.contentType,
                        module: config.module,
                        tagModule: config.tagModule,
                        contentLabel: config.contentLabel,
                        emptyLabel: config.emptyLabel,
                        cssPrefix: config.cssPrefix,
                        iconIdPrefix: config.iconIdPrefix,
                        showDreamStates: config.showDreamStates,
                        highlightImportant: config.highlightImportant,
                        rightBorderClass: config.rightBorderClass,
                    }, config.templateId);
                    KTMenu.createInstances();

                    module.currentResults = viewModels;
                    module.currentSearchParams = ajaxData;

                    const params: URLSearchParams = cF.util.buildUrlParams(ajaxData);
                    history.replaceState(null, "", window.location.pathname + "?" + params.toString());
                    $("#keywordDisplay div.keyword-wrapper").removeClass("text-muted").addClass("text-primary");
                    $("#keywordDisplay div.keyword-wrapper").removeClass("badge-light-secondary").addClass("badge-light-primary");
                });
            },

            copy: function(): void {
                const results: [] = module.currentResults;

                if (!results || results.length === 0) {
                    Swal.fire({ text: "복사할 검색 결과가 없습니다." });
                    return;
                }

                let prevDate: string|null = null;
                const textToCopy: string = results.map((item: any): string => {
                    const date: string = `${item.stdrdDt} (${item.journalDateWeekDay})`;
                    const content: string = cF.util.htmlToText(item.markdownContent ?? "");

                    let block: string = "";
                    if (date !== prevDate) {
                        block += `\r\n${date}\r\n`;
                        prevDate = date;
                    }

                    block += [
                        `#${item.sortOrder}`,
                        content
                    ].join("\r\n");

                    return block;
                }).join("\r\n\r\n");

                navigator.clipboard.writeText(textToCopy)
                    .then((): void => {
                        Swal.fire({
                            text: `현재 페이지 ${results.length}건이 복사되었습니다.`,
                            timer: 1500,
                            showConfirmButton: false
                        });
                    })
                    .catch((): void => {
                        cF.util.legacyCopy(textToCopy);
                    });
            },

            exportTxt: function(): void {
                const ajaxData: Record<string, any> = module.currentSearchParams;
                $("#msgDisplay").empty();
                if (!hasSearchCondition(ajaxData)) {
                    $("#msgDisplay").text("검색 조건을 하나 이상 입력하세요.");
                    return;
                }

                const params: URLSearchParams = cF.util.buildUrlParams(ajaxData);
                window.location.href = config.exportUrl + "?" + params.toString();
            },
        };

        return module;
    };

    [
        "JOURNAL_DIARY",
        "JOURNAL_DREAM"
    ].forEach(function(contentType: string): void {
        modules[contentType] = create(contentType, createConfig(contentType));
    });

    return {
        initialized: true,
        init: function(): void {},
        get: function(contentType: string): dfModule {
            return modules[contentType];
        },
    };
})();
