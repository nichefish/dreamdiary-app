/**
 * jrnl_diary_search_module.ts
 * 저널 일기 검색 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.JrnlDiarySearch = (function(): dfModule {
    return {
        initialized: false,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.JrnlDiarySearch.initialized) return;

            dF.JrnlDiarySearch.initKeyword();
            dF.JrnlDiarySearch.search();
            dF.JrnlDiarySearch.initialized = true;
            console.log("'dF.JrnlDiarySearch' module initialized.");
        },

        /**
         * form init
         */
        initKeyword: function(): void {
            dF.JrnlDiarySearch.clearKeywordFields();

            const params = new URLSearchParams(window.location.search);
            const keywords: string[] = params.getAll("searchKeywords");
            if (keywords.length > 0) {
                keywords.forEach(k => dF.JrnlDiarySearch.addKeywordField(k));
            } else {
                dF.JrnlDiarySearch.addKeywordField();
            }
        },

        /**
         * 키워드 검색 (Ajax)
         */
        addKeywordField: function(value = ''): void {
            const container: HTMLElement = document.getElementById("jrnlKeywordContainer");
            const wrapper: HTMLDivElement = document.createElement("div");
            wrapper.className = "keyword-wrapper d-flex align-items-center gap-2";
            wrapper.innerHTML = `
                <input type="text" name="searchKeywords" class="form-control form-control-sm" placeholder="검색어 입력" value="${value}"/>
                <button type="button" class="btn btn-sm btn-light-danger btn-outlined py-2 px-3 cursor-pointer"
                        onclick="dF.JrnlDiarySearch.removeKeywordField(this);"
                        data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'txt.jrnl.day'/> <@spring.message 'bs.tooltip.modal.mdf'/>">
                    <i class="bi bi-trash p-0"></i>
                </button>
            `;
            container.appendChild(wrapper);
        },

        /**
         * 키워드 검색 (Ajax)
         */
        removeKeywordField: function(btn: HTMLElement): void {
            btn.closest("div.keyword-wrapper")?.remove();
        },

        /**
         * 키워드 검색 종료
         */
        clearKeywordFields: function(): void {
            $("#jrnlKeywordContainer").empty();
            $("#jrnlTagNoContainer").empty();
        },

        /**
         * 키워드 검색 종료
         */
        resetSearch: function(): void {
            window.location.href = window.location.pathname;
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
            $("#msgDiv").empty();
            const hasKeyword: boolean = Array.isArray(ajaxData["searchKeywords"]) ? ajaxData["searchKeywords"]?.some(k => cF.util.isNotEmpty(k?.trim())) : cF.util.isNotEmpty(ajaxData["searchKeywords"]);
            const hasTag: boolean = Array.isArray(ajaxData["tagNos"]) ? ajaxData["tagNos"].length > 0 : !!ajaxData["tagNos"];
            if (!hasKeyword && !hasTag) {
                $("#msgDiv").text("검색 조건을 하나 이상 입력하세요.");
                return;
            }
            const url: string = Url.JRNL_DIARIES;
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const viewModels = res.rsltList.map((diary: any) =>
                    dF.JrnlDiary.buildViewModel(diary, 'SEARCH')
                );
                cF.handlebars.template(viewModels, "jrnl_diary_search");

                const params = new URLSearchParams();
                Object.keys(ajaxData).forEach(key => {
                    const val = ajaxData[key];
                    if (Array.isArray(val)) {
                        val.forEach(v => {
                            if (v && v.trim() !== "") params.append(key, v);
                        });
                    } else if (val && val.trim() !== "") {
                        params.append(key, val);
                    }
                });
                history.replaceState(null, "", window.location.pathname + "?" + params.toString());
            });
        },

        /**
         * View Model 구성
         * @param {Object} diary
         * @param {String} profileName
         */
        buildViewModel: function(diary, profileName) {
            const profile: any = dF.JrnlDiary.RENDER_PROFILE[profileName];

            if (!profile) throw new Error(`Unknown render profile: ${profileName}`);

            return {
                ...diary,
                view: profile,
                cnClass: [
                    'cn',
                    profile.collapsed && diary.state?.includes('COLLAPSED') ? 'collapsed' : null
                ].filter(Boolean).join(' ')
            };
        },

        select: function() {

        }
    }
})();