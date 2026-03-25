/**
 * cl_cd_module.ts
 * 분류 코드 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === "undefined") { var dF = {} as any; }
dF.ClCd = (function(): dfModule {
    return {
        initialized: false,
        swappable: null,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.ClCd.initialized) return;

            dF.ClCd.initialized = true;
            console.log("'dF.ClCd' module initialized.");
        },

        /**
         * form init
         * @param {Record<string, any>} obj - 폼에 바인딩할 데이터
         */
        initForm: function(obj: Record<string, any> = {}): void {
            /* show modal */
            cF.handlebars.modal(obj, "cl_cd_reg");

            /* jquery validation */
            cF.validate.validateForm("#clCdRegForm", dF.ClCd.regAjax);
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

            // pageNo를 1로 설정
            const pageNoElmt: HTMLInputElement = document.querySelector("#listForm #pageNo");
            if (pageNoElmt) pageNoElmt.value = "1";
            // submit
            cF.form.blockUISubmit("#listForm", `${Url.CL_CD_LIST}?actionTyCd=SEARCH`);
        },

        /**
         * list load by ajax
         */
        listAjax: function(): void {
            const url: string = Url.CD_CLS;
            const ajaxData: Record<string, any> = cF.util.getJsonFormData("#listForm");
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                dF.ClCd.renderList(res.rsltList || []);
            });
        },

        /**
         * render list template
         */
        renderList: function(list: Record<string, any>[] = []): void {
            cF.handlebars.template(list, "cl_cd_list");
        },

        /**
         * render list from server-side page json
         */
        renderListFromPageData: function(): void {
            const dataEl: HTMLElement | null = document.getElementById("cl_cd_list_data");
            if (!dataEl) {
                dF.ClCd.renderList([]);
                return;
            }

            try {
                const raw: string = dataEl.textContent || "[]";
                const parsed: unknown = JSON.parse(raw);
                dF.ClCd.renderList(Array.isArray(parsed) ? parsed : []);
            } catch (e) {
                console.error("failed to parse cl_cd_list_data", e);
                dF.ClCd.renderList([]);
            }
        },

        /**
         * show register modal
         */
        regModal: function(): void {
            event.stopPropagation();
            dF.ClCd.initForm({});
        },

        /**
         * form submit
         */
        submit: function(): void {
            event.stopPropagation();
            $("#clCdRegForm").submit();
        },

        /**
         * register / modify ajax
         */
        regAjax: function(): void {
            event.stopPropagation();

            Swal.fire({
                text: Message.get("view.cnfm.reg"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                $("#clCdRegForm #regYn").val("Y");
                const url: string = Url.CD_CLS;
                const ajaxData: Record<string, any> = cF.util.getJsonFormData("#clCdRegForm");
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.ui.blockUIReload();
                        });
                }, "block");
            });
        },

        /**
         * show detail modal
         * @param {string} clCd
         */
        dtlModal: function(clCd: string): void {
            event.stopPropagation();
            $("#clCd").val(clCd);

            const url: string = cF.util.bindUrl(Url.CD_CL, { clCd });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                cF.handlebars.modal(res.rsltObj, "cl_cd_dtl");
                dF.ClCd.key = clCd;
                dF.DtlCd.initDraggable();
            });
        },

        /**
         * show modify modal
         * @param {string} clCd
         */
        mdfModal: function(clCd: string): void {
            event.stopPropagation();

            const url: string = cF.util.bindUrl(Url.CD_CL, { clCd });
            const ajaxData: Record<string, any> = { clCd };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltObj } = res;
                rsltObj.isMdf = true;
                dF.ClCd.initForm(rsltObj);
                $("#cl_cd_dtl_modal").modal("hide");
            });
        },

        /**
         * toggle useYn
         * @param {string} clCd
         */
        toggleUseAjax: function(clCd: string): void {
            const item: HTMLElement | null = document.querySelector(`.cl-cd-item[data-cl-cd='${clCd}']`);
            const currentUseYn: string = (item?.dataset?.useYn || "N").toUpperCase();
            const nextUseYn: string = currentUseYn === "Y" ? "N" : "Y";

            const url: string = cF.util.bindUrl(Url.CD_CL, { clCd });
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

        /**
         * unused legacy API
         * @param {string} clCd
         */
        unuseAjax: function(clCd: string): void {
            event.stopPropagation();

            Swal.fire({
                text: Message.get("view.cnfm.unuse"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = Url.CL_CD_UNUSE_AJAX;
                const ajaxData: Record<string, any> = { clCd };
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.ui.blockUIReload();
                        });
                }, "block");
            });
        },

        /**
         * delete ajax
         * @param {string} clCd
         */
        delAjax: function(clCd: string): void {
            event.stopPropagation();

            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = cF.util.bindUrl(Url.CD_CL, { clCd });
                cF.$ajax.delete(url, null, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.ui.blockUIReload();
                        });
                }, "block");
            });
        },

        /**
         * move to list
         */
        list: function(): void {
            const listUrl: string = `${Url.CL_CD_LIST}?isBackToList=Y`;
            cF.ui.blockUIReplace(listUrl);
        },
    }
})();
