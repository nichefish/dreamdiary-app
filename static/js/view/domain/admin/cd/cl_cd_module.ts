/**
 * cl_cd_module.ts
 * 분류 코드 스크립트 모듈
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
            if (pageNoElmt) pageNoElmt.value = '1';
            // submit
            cF.form.blockUISubmit("#listForm", Url.CL_CD_LIST + "?actionTyCd=SEARCH");
        },

        listAjax: function() {
            const url: string = Url.CD_CLS;
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                cF.handlebars.template(res.rsltList, "cl_cd_list");
            });
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
         * 상세 모달 호출
         * @param {string} clCd - 조회할 분류 코드.
         */
        dtlModal: function(clCd: string): void {
            event.stopPropagation();

            const url: string = cF.util.bindUrl(Url.CD_CL, { clCd });
            cF.ajax.get(url, null, function(res: AjaxResponse) {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return false;
                }
                cF.handlebars.modal(res.rsltObj, "cl_cd_dtl");
                dF.ClCd.key = clCd;
                /* init : Draggable */
                dF.DtlCd.initDraggable();
            });
        },

        /**
         * 수정 모달 호출
         * @param {string} clCd - 조회할 분류 코드.
         */
        mdfModal: function(clCd: string): void {
            event.stopPropagation();

            const url: string = cF.util.bindUrl(Url.CD_CL, { clCd });
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
         * 사용 상태 변경 (Ajax)
         */
        toggleUseAjax: function(clCd: string|number): void {
            if (isNaN(Number(clCd))) return;

            const item: HTMLElement = document.querySelector(`cl-cd-item[data-id='${clCd}']`);
            if (!item) console.warn("item does not exists.");
            const currentUseYn: string = item.dataset.useYn;
            const nextUseYn: string = currentUseYn === "Y" ? "N" : "Y";

            const url: string = cF.util.bindUrl(Url.MENU, { clCd });
            const ajaxData: Record<string, any> = { "useYn": nextUseYn };
            cF.$ajax.patch(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) return Swal.fire({ text: res.message });
                }
                cF.ui.blockUIReload();
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
                            if (res.rslt) cF.ui.blockUIReload();
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

                const url: string = cF.util.bindUrl(Url.CD_CL, { clCd });
                cF.$ajax.post(url, null, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.ui.blockUIReload();
                        });
                }, "block");
            });
        },

        /**
         * 목록 화면으로 이동
         */
        list: function(): void {
            const listUrl: string = `${Url.CL_CD_LIST}?isBackToList=Y`;
            cF.ui.blockUIReplace(listUrl);
        },
    }
})();