/**
 * dtl_cd_module.ts
 *
 * @author nichefish
 */
const DtlCd: Module = (function(): Module {
    return {
        /**
         * initializes module.
         */
        init: function(): void {
            console.log("'DtlCd' module initialized.");
        },

        /**
         * form init
         * @param {Object} obj - 폼에 바인딩할 데이터
         */
        initForm: function(obj = {}): void {
            /* show modal */
            cF.handlebars.template(obj, "dtl_cd_reg", "show");

            /* jquery validation */
            cF.validate.validateForm("#dtlCdRegForm", DtlCd.regAjax);
            // checkbox init
            cF.util.chckboxLabel("useYn", "사용//미사용", "blue//gray");
            cF.validate.replaceBlankIfMatches("#dtlCdRegForm #dtlCd", cF.regex.nonCd);
            cF.validate.toUpperCase("#dtlCdRegForm #dtlCd");
        },

        /**
         * Draggable 컴포넌트 init
         */
        initDraggable: function({ refreshFunc }: { refreshFunc?: Function } = {}): void {
            const keyExtractor: Function = (item: HTMLElement) => ({ "clCd": $("#clCd").val(), "dtlCd": $(item).attr("id") });
            const url: string = Url.DTL_CD_SORT_ORDR_AJAX;
            DtlCd.swappable = cF.draggable.init("", keyExtractor, url);
        },

        /**
         * 등록 모달 호출
         */
        regModal: function(): void {
            event.stopPropagation();

            const obj = { "clCd": $("#clCd").val() };
            /* initialize form. */
            DtlCd.initForm(obj);
        },

        /**
         * form submit
         */
        submit: function(): void {
            $("#dtlCdRegForm").submit();
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

                $("#dtlCdRegForm #regYn").val("Y");
                const url: string = Url.DTL_CD_REG_AJAX;
                const ajaxData: Record<string, any> = $("#dtlCdRegForm").serializeArray();
                cF.ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.util.blockUIReload();
                        });
                }, "block");
            });
        },

        /**
         * 수정 모달 호출
         * @param {string} dtlCd - 조회할 상세 코드.
         */
        mdfModal: function(dtlCd: string) {
            const url: string = Url.DTL_CD_DTL_AJAX;
            const ajaxData: Record<string, any> = { "clCd": $("#clCd").val(), "dtlCd": dtlCd };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse) {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return false;
                }
                const { rsltObj } = res;
                rsltObj.isMdf = true;
                /* initialize form. */
                DtlCd.initForm(rsltObj);
            });
        },

        /**
         * 사용으로 변경 (Ajax)
         * @param {string} dtlCd - 변경할 상세 코드.
         */
        useAjax: function(dtlCd: string) {
            event.stopPropagation();

            Swal.fire({
                text: Message.get("view.cnfm.use"),
                showCancelButton: true,
            }).then(function(result: SwalResult) {
                if (!result.value) return;

                const url: string = Url.DTL_CD_USE_AJAX;
                const ajaxData: Record<string, any> = { "clCd": $("#clCd").val(), "dtlCd": dtlCd };
                cF.ajax.post(url, ajaxData, function(res: AjaxResponse) {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.util.blockUIReload();
                        });
                }, "block");
            });
        },

        /**
         * 미사용으로 변경 (Ajax)
         * @param {string} dtlCd - 변경할 상세 코드.
         */
        unuseAjax: function(dtlCd: string) {
            event.stopPropagation();

            Swal.fire({
                text: Message.get("view.cnfm.unuse"),
                showCancelButton: true,
            }).then(function(result: SwalResult) {
                if (!result.value) return;

                const url: string = Url.DTL_CD_UNUSE_AJAX;
                const ajaxData: Record<string, any> = { "clCd": $("#clCd").val(), "dtlCd": dtlCd };
                cF.ajax.post(url, ajaxData, function(res: AjaxResponse) {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.util.blockUIReload();
                        });
                }, "block");
            });
        },

        /**
         * 삭제 (Ajax)
         * @param {string} dtlCd - 삭제할 상세 코드.
         */
        delAjax: function(dtlCd: string) {
            event.stopPropagation();

            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = Url.DTL_CD_DEL_AJAX;
                const ajaxData: Record<string, any> = { "clCd": $("#clCd").val(), "dtlCd": dtlCd };
                cF.ajax.post(url, ajaxData, function(res: AjaxResponse) {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.util.blockUIReload();
                        });
                }, "block");
            });
        },
    }
})();