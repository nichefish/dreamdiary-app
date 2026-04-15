/**
 * code_group_module.ts
 * 분류 코드 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === "undefined") { var dF = {} as any; }
dF.CodeGroup = (function(): dfModule {
    return {
        initialized: false,
        swappable: null,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.CodeGroup.initialized) return;

            dF.CodeGroup.initialized = true;
            console.log("'dF.CodeGroup' module initialized.");
        },

        /**
         * form init
         * @param {Record<string, any>} obj - 폼에 바인딩할 데이터
         */
        initForm: function(obj: Record<string, any> = {}): void {
            /* show modal */
            cF.handlebars.modal(obj, "code_group_reg");

            /* jquery validation */
            cF.validate.validateForm("#clCdRegForm", dF.CodeGroup.regAjax);
            // checkbox init
            cF.ui.chckboxLabel("#clCdRegForm #useYn", "사용//미사용", "blue//gray");
            cF.validate.replaceBlankIfMatches("#clCdRegForm #clCd", cF.regex.nonCd);
            cF.validate.toUpperCase("#clCdRegForm #clCd");
        },

        /**
         * 목록 검색
         */
        search: function(): void {
            event.stopPropagation();

            const pageNoElmt: HTMLInputElement = document.querySelector("#listForm #pageNo");
            if (pageNoElmt) pageNoElmt.value = "1";
            cF.form.blockUISubmit("#listForm", `${Url.CODE_GROUP_LIST}?actionTyCd=SEARCH`);
        },

        /**
         * list load by ajax
         */
        listAjax: function(): void {
            const url: string = Url.CODE_GROUPS;
            const ajaxData: Record<string, any> = cF.util.getJsonFormData("#listForm");
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                dF.CodeGroup.renderList(res.rsltList || []);
            });
        },

        renderList: function(list: Record<string, any>[] = []): void {
            cF.handlebars.template(list, "code_group_list");
        },

        renderListFromPageData: function(): void {
            const dataEl: HTMLElement | null = document.getElementById("code_group_list_data");
            if (!dataEl) {
                dF.CodeGroup.renderList([]);
                return;
            }

            try {
                const raw: string = dataEl.textContent || "[]";
                const parsed: unknown = JSON.parse(raw);
                dF.CodeGroup.renderList(Array.isArray(parsed) ? parsed : []);
            } catch (e) {
                console.error("failed to parse code_group_list_data", e);
                dF.CodeGroup.renderList([]);
            }
        },

        regModal: function(): void {
            event.stopPropagation();
            dF.CodeGroup.initForm({});
        },

        submit: function(): void {
            event.stopPropagation();
            $("#clCdRegForm").submit();
        },

        regAjax: function(): void {
            event.stopPropagation();

            Swal.fire({
                text: Message.get("view.cnfm.reg"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                $("#clCdRegForm #regYn").val("Y");
                const url: string = Url.CODE_GROUPS;
                const ajaxData: Record<string, any> = cF.util.getJsonFormData("#clCdRegForm");
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.ui.blockUIReload();
                        });
                }, "block");
            });
        },

        dtlModal: function(clCd: string): void {
            event.stopPropagation();
            $("#clCd").val(clCd);

            const url: string = cF.util.bindUrl(Url.CODE_GROUP, { clCd });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                cF.handlebars.modal(res.rsltObj, "code_group_dtl");
                dF.CodeGroup.key = clCd;
                dF.CodeItem.initDraggable();
            });
        },

        mdfModal: function(clCd: string): void {
            event.stopPropagation();

            const url: string = cF.util.bindUrl(Url.CODE_GROUP, { clCd });
            const ajaxData: Record<string, any> = { clCd };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltObj } = res;
                rsltObj.isMdf = true;
                dF.CodeGroup.initForm(rsltObj);
                $("#code_group_dtl_modal").modal("hide");
            });
        },

        toggleUseAjax: function(clCd: string): void {
            const item: HTMLElement | null = document.querySelector(`.cl-cd-item[data-cl-cd='${clCd}']`);
            const currentUseYn: string = (item?.dataset?.useYn || "N").toUpperCase();
            const nextUseYn: string = currentUseYn === "Y" ? "N" : "Y";

            const url: string = cF.util.bindUrl(Url.CODE_GROUP, { clCd });
            const ajaxData: Record<string, any> = { useYn: nextUseYn };
            cF.$ajax.patch(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) {
                        Swal.fire({ text: res.message });
                    }
                    return;
                }
                cF.ui.blockUIReload();
            });
        },

        delAjax: function(clCd: string): void {
            event.stopPropagation();

            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = cF.util.bindUrl(Url.CODE_GROUP, { clCd });
                cF.$ajax.delete(url, null, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.ui.blockUIReload();
                        });
                }, "block");
            });
        },

        list: function(): void {
            const listUrl: string = `${Url.CODE_GROUP_LIST}?isBackToList=Y`;
            cF.ui.blockUIReplace(listUrl);
        },
    }
})();
