/**
 * cl_cd_module.ts
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
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
            cF.util.chckboxLabel("useYn", "사용//미사용", "blue//gray");
            cF.validate.replaceBlankIfMatches("#clCdRegForm #clCd", cF.regex.nonCd);
            cF.validate.toUpperCase("#clCdRegForm #clCd");
        },

        /**
         * Draggable 컴포넌트 init
         */
        initDraggable: function(): void {
            const keyExtractor: Function = (item: HTMLElement) => ({ "clCd": item.getAttribute("id") });
            const url: string = Url.CL_CD_SORT_ORDR_AJAX;
            dF.ClCd.swappable = cF.draggable.init("", keyExtractor, url);
        },

        /**
         * 목록 검색
         */
        search: function(): void {
            event.stopPropagation();

            // pageNo를 1로 설정
            const pageNoElmt: HTMLInputElement = document.querySelector("#listForm #pageNo") as HTMLInputElement;
            if (pageNoElmt) pageNoElmt.value = '1';
            // submit
            cF.util.blockUISubmit("#listForm", Url.CL_CD_LIST + "?actionTyCd=SEARCH");
        },

        /**
         * 등록 모달 호출
         */
        regModal: function(): void {
            event.stopPropagation();

            /* initialize form. */
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
         * 등록/수정 (Ajax)
         */
        regAjax: function(): void {
            event.stopPropagation();

            Swal.fire({
                text: Message.get("view.cnfm.reg"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                $("#clCdRegForm #regYn").val("Y");
                const url: string = Url.CL_CD_REG_AJAX;
                const ajaxData: Record<string, any> = cF.util.getJsonFormData("#clCdRegForm");
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.util.blockUIReload();
                        });
                }, "block");
            });
        },

        /**
         * 상세 화면으로 이동
         * @param {string} clCd - 조회할 분류 코드.
         */
        dtl: function(clCd: string): void {
            event.stopPropagation();

            cF.util.blockUIRequest();
            $("#procForm #clCd").val(clCd);
            cF.util.blockUISubmit("#procForm", Url.CL_CD_DTL);
        },

        /**
         * 상세 모달 호출
         * @param {string} clCd - 조회할 분류 코드.
         */
        dtlModal: function(clCd: string): void {
            event.stopPropagation();

            const url: string = Url.CL_CD_DTL_AJAX;
            const ajaxData: Record<string, any> = { "clCd": clCd };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse) {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return false;
                }
                cF.handlebars.modal(res.rsltObj, "cl_cd_dtl");
                dF.ClCd.key = clCd;
            });
        },

        /**
         * 수정 모달 호출
         * @param {string} clCd - 조회할 분류 코드.
         */
        mdfModal: function(clCd: string): void {
            event.stopPropagation();

            const url: string = Url.CL_CD_DTL_AJAX;
            const ajaxData: Record<string, any> = { "clCd": clCd };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltObj } = res;
                rsltObj.isMdf = true;
                /* initialize form. */
                dF.ClCd.initForm(rsltObj);
                $('#cl_cd_dtl_modal').modal('hide');
            });
        },

        /**
         * 사용으로 변경 (Ajax)
         * @param {string} clCd - 변경할 분류 코드.
         */
        useAjax: function(clCd: string): void {
            event.stopPropagation();

            Swal.fire({
                text: Message.get("view.cnfm.use"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = Url.CL_CD_USE_AJAX;
                const ajaxData: Record<string, any> = { "clCd": clCd };
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.util.blockUIReload();
                        });
                }, "block");
            });
        },

        /**
         * 미사용으로 변경 (Ajax)
         * @param {string} clCd - 변경할 분류 코드.
         */
        unuseAjax: function(clCd: string): void {
            event.stopPropagation();

            Swal.fire({
                text: Message.get("view.cnfm.unuse"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = Url.CL_CD_UNUSE_AJAX;
                const ajaxData: Record<string, any> = { "clCd": clCd };
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.util.blockUIReload();
                        });
                }, "block");
            });
        },

        /**
         * 삭제 (Ajax)
         * @param {string} clCd - 삭제할 분류 코드.
         */
        delAjax: function(clCd: string): void {
            event.stopPropagation();

            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = Url.CL_CD_DEL_AJAX;
                const ajaxData: Record<string, any> = { "clCd": clCd };
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.util.blockUIReload();
                        });
                }, "block");
            });
        },

        /**
         * 목록 화면으로 이동
         */
        list: function(): void {
            const listUrl: string = `${Url.CL_CD_LIST}?isBackToList=Y`;
            cF.util.blockUIReplace(listUrl);
        },
    }
})();