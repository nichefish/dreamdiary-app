/**
 * journal_diary_search_module.ts
 * 저널 일기 검색 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.JournalDiarySearch = (function(): dfModule {
    return {
        initialized: false,
        currentResults: [] as any[],
        currentSearchParams: {} as Record<string, any>,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.JournalDiarySearch.initialized) return;

            dF.JournalDiarySearch.initialized = true;
            console.log("'dF.JournalDiarySearch' module initialized.");
        },

        initSearch: function(): void {
            dF.JournalDiarySearch.initKeyword();
            dF.JournalDiarySearch.initTag();
            dF.JournalDiarySearch.search();
        },

        /**
         * keyword init
         */
        initKeyword: function(): void {
            dF.JournalDiarySearch.clearKeywordFields();

            const params: URLSearchParams = new URLSearchParams(window.location.search);
            const keywords: string[] = params.getAll("searchKeywords");
            if (keywords.length > 0) {
                keywords.forEach((k: string) => dF.JournalDiarySearch.addKeyword(k));
            } else {
                dF.JournalDiarySearch.addKeyword();
            }
            $("#keywordDisplay div").removeClass("text-muted").addClass("text-primary");
        },

        /**
         * tag init
         */
        initTag: function(): void {
            const params: URLSearchParams = new URLSearchParams(window.location.search);
            const tagIds: string[] = params.getAll("tagIds");
            if (tagIds.length > 0) {
                tagIds.forEach((tagId: string): void => {
                    const tag = dF.JournalDiaryTag.list.find(
                        (t: any): boolean => t.id === Number(tagId)
                    );
                    if (!tag) return;
                    dF.JournalDiarySearch.select(tagId, tag.tagNm);
                });
            }
        },

        /**
         * 키워드 추가
         * @param {string} [value]
         */
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
            input.name = "searchKeywords"
            input.value = value;
            inputContainer.appendChild(input);

            const statusContainer: HTMLElement = document.getElementById("keywordDisplay");
            const statusBadge: HTMLDivElement = document.createElement("div");
            statusBadge.className = "badge badge-light-secondary keyword-wrapper fw-lighter d-flex align-items-center gap-2 px-3 py-2 text-muted";
            statusBadge.dataset.value = value;
            statusBadge.innerHTML = `
                ${value}
                <i class="bi bi-x cursor-pointer" onclick="dF.JournalDiarySearch.removeKeyword('${value}')"></i>
            `;
            statusContainer.appendChild(statusBadge);
            $("#msgDisplay").empty();
        },

        /**
         * 키워드 삭제
         */
        removeKeyword: function(value: string): void {
            // hidden input 제거
            $("#journalKeywordHiddenContainer input[name='searchKeywords']")
                .filter(function (): boolean {
                    return $(this).val() === value;
                })
                .remove();
            // 칩 제거
            $("#keywordDisplay div.keyword-wrapper")
                .filter(function (): boolean {
                    return $(this).attr("data-value") === value;
                })
                .remove();
            // 재검색
            dF.JournalDiarySearch.search();
        },

        /**
         * 키워드 검색 종료
         */
        clearKeywordFields: function(): void {
            $("#journalKeywordContainer").empty();
            $("#journalTagNoContainer").empty();
        },

        /**
         * 정렬
         */
        toggleSort: function(): void {
            const $sortInput = $("#sortInput");
            const current = $sortInput.val();
            const next: "desc"|"asc" = current === "desc" ? "asc" : "desc";
            $sortInput.val(next);

            // 아이콘 변경
            const icon = $(".bi-sort-down-alt, .bi-sort-up");
            icon.toggleClass("bi-sort-down-alt bi-sort-up");

            dF.JournalDiarySearch.search();
        },

        /**
         * 키워드 검색 종료
         */
        resetSearch: function(): void {
            window.location.href = window.location.pathname;
        },

        /**
         * 태그 선택
         * @param {string|number} tagId - 조회할 태그 ID.
         * @param tagNm 태그 이름
         */
        select: function(tagId: string|number, tagNm: string): void {
            const inputContainer: HTMLElement = document.getElementById("journalTagNoHiddenContainer");
            const input: HTMLInputElement = document.createElement("input");
            input.type = "hidden";
            input.name = "tagIds"
            input.value = tagId as string;
            inputContainer.appendChild(input);

            const tagContainer: HTMLElement = document.getElementById("tagDisplay");
            const tagBadge: HTMLDivElement = document.createElement("div");
            tagBadge.className = "badge badge-light-primary tag-wrapper fw-lighter d-flex align-items-center gap-2 px-3 py-2 text-primary";
            tagBadge.dataset.value = tagId as string;
            tagBadge.innerHTML = `
                #${tagNm}
                <i class="bi bi-x cursor-pointer" onclick="dF.JournalDiarySearch.removeTag('${tagId}')"></i>
            `;
            tagContainer.appendChild(tagBadge);
            $("#msgDisplay").empty();

            dF.JournalDiarySearch.search();
        },

        /**
         * 키워드 삭제
         */
        removeTag: function(value: string): void {
            // hidden input 제거
            $("#journalTagNoHiddenContainer input[name='tagIds']")
                .filter(function (): boolean {
                    return $(this).val() === value;
                })
                .remove();
            // 칩 제거
            $("#tagDisplay div.tag-wrapper")
                .filter(function (): boolean {
                    return $(this).attr("data-value") === value;
                })
                .remove();
            // 재검색
            dF.JournalDiarySearch.search();
        },

        /**
         * 검색
         */
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
            $("#msgDisplay").empty();
            const hasKeyword: boolean = Array.isArray(ajaxData["searchKeywords"]) ? ajaxData["searchKeywords"]?.some(k => cF.util.isNotEmpty(k?.trim())) : cF.util.isNotEmpty(ajaxData["searchKeywords"]);
            const hasTag: boolean = Array.isArray(ajaxData["tagIds"]) ? ajaxData["tagIds"].length > 0 : !!ajaxData["tagIds"];
            if (!hasKeyword && !hasTag) {
                $("#msgDisplay").text("검색 조건을 하나 이상 입력하세요.");
                cF.handlebars.template([], "journal_diary_search");
                return;
            }
            const url: string = Url.JOURNAL_DIARIES;
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const viewModels: any[] = res.rsltList.map((diary: any): void =>
                    dF.JournalDiary.buildViewModel(diary, 'SEARCH')
                );
                cF.handlebars.template(viewModels, "journal_diary_search");
                KTMenu.createInstances();

                // 상태 저장
                dF.JournalDiarySearch.currentResults = viewModels;
                dF.JournalDiarySearch.currentSearchParams = ajaxData;

                const params: URLSearchParams = cF.util.buildUrlParams(ajaxData);
                history.replaceState(null, "", window.location.pathname + "?" + params.toString());
                $("#keywordDisplay div.keyword-wrapper").removeClass("text-muted").addClass("text-primary");
                $("#keywordDisplay div.keyword-wrapper").removeClass("badge-light-secondary").addClass("badge-light-primary");
            });
        },

        /**
         * 클립보드에 검색 내용 복사
         */
        copy: function(): void {
            const results: [] = dF.JournalDiarySearch.currentResults;

            if (!results || results.length === 0) {
                Swal.fire({ text: "복사할 검색 결과가 없습니다." });
                return;
            }

            let prevDate: string|null = null;
            const textToCopy: string = results.map((item: any): string => {
                const date: string = `${item.stdrdDt} (${item.journalDtWeekDay})`;
                const content: string = cF.util.htmlToText(item.markdownContent ?? "");

                let block: string = "";
                // 날짜가 바뀌었을 때만 날짜 출력
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
                .then(() => {
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

        /**
         * 검색 결과 txt 다운로드
         */
        exportTxt: function(): void {
            const ajaxData: Record<string, any> = dF.JournalDiarySearch.currentSearchParams;
            $("#msgDisplay").empty();
            const hasKeyword: boolean = Array.isArray(ajaxData["searchKeywords"]) ? ajaxData["searchKeywords"]?.some(k => cF.util.isNotEmpty(k?.trim())) : cF.util.isNotEmpty(ajaxData["searchKeywords"]);
            const hasTag: boolean = Array.isArray(ajaxData["tagIds"]) ? ajaxData["tagIds"].length > 0 : !!ajaxData["tagIds"];
            if (!hasKeyword && !hasTag) {
                $("#msgDisplay").text("검색 조건을 하나 이상 입력하세요.");
                return;
            }

            const params: URLSearchParams = cF.util.buildUrlParams(ajaxData);
            window.location.href = Url.JOURNAL_DIARIES_EXPORT + "?" + params.toString();
        }
    }
})();
