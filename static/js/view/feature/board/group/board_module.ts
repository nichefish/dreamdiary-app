/**
 * board_module.ts
 */
if (typeof dF === "undefined") { var dF = {} as any; }
dF.Board = (function (): dfModule {
    return {
        initialized: false,

        init: function (): void {
            if (dF.Board.initialized) return;

            dF.Board.initialized = true;
            console.log("'dF.Board' module initialized.");
        },

        initForm: function (obj: Record<string, any> = {}): void {
            cF.handlebars.modal(obj, "board_reg");
            cF.validate.validateForm("#boardRegForm", dF.Board.regAjax);
            cF.ui.chckboxLabel("#boardRegForm #useYn", "\uc0ac\uc6a9//\ubbf8\uc0ac\uc6a9", "blue//gray");
            cF.validate.replaceBlankIfMatches("#boardRegForm .cddata", cF.regex.nonCd);
            cF.validate.onlyNum(".number");
        },

        initDraggable: function (): void {
            const keyExtractor: Function = (item: HTMLElement) => ({ id: Number(item.dataset.id) });
            const url: string = Url.BOARD_GROUP_SORT_ORDR_AJAX;
            dF.Board.swappable = cF.draggable.init(keyExtractor, url);
        },

        regModal: function (): void {
            dF.Board.initForm();
        },

        submit: function (): void {
            $("#boardRegForm").submit();
        },

        regAjax: function (): void {
            Swal.fire({
                text: Message.get("view.cnfm.reg"),
                showCancelButton: true,
            }).then(function (result: SwalResult): void {
                if (!result.value) return;

                const url: string = Url.BOARD_GROUP_REG_AJAX;
                const ajaxData: Record<string, any> = cF.util.getJsonFormData("#boardRegForm");
                cF.$ajax.post(url, ajaxData, function (res: AjaxResponse): void {
                    Swal.fire({ text: res.message }).then(function (): void {
                        if (res.rslt) cF.ui.blockUIReload();
                    });
                }, "block");
            });
        },

        mdfModal: function (id: number): void {
            const url: string = Url.BOARD_GROUP_DTL_AJAX;
            const ajaxData: Record<string, any> = { id: id };
            cF.ajax.get(url, ajaxData, function (res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltObj } = res;
                rsltObj.isMdf = true;
                dF.Board.initForm(rsltObj);
            });
        },

        useAjax: function (id: number): void {
            Swal.fire({
                text: Message.get("view.cnfm.use"),
                showCancelButton: true,
            }).then(function (result: SwalResult): void {
                if (!result.value) return;

                const url: string = Url.BOARD_GROUP_USE_AJAX;
                const ajaxData: Record<string, any> = { id: id };
                cF.$ajax.post(url, ajaxData, function (res: AjaxResponse): void {
                    Swal.fire({ text: res.message }).then(function (): void {
                        if (res.rslt) cF.ui.blockUIReload();
                    });
                }, "block");
            });
        },

        unuseAjax: function (id: number): void {
            Swal.fire({
                text: Message.get("view.cnfm.unuse"),
                showCancelButton: true,
            }).then(function (result: SwalResult): void {
                if (!result.value) return;

                const url: string = Url.BOARD_GROUP_UNUSE_AJAX;
                const ajaxData: Record<string, any> = { id: id };
                cF.$ajax.post(url, ajaxData, function (res: AjaxResponse): void {
                    Swal.fire({ text: res.message }).then(function (): void {
                        if (res.rslt) cF.ui.blockUIReload();
                    });
                }, "block");
            });
        },

        delAjax: function (id: number): void {
            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function (result: SwalResult): void {
                if (!result.value) return;

                const url: string = Url.BOARD_GROUP_DEL_AJAX;
                const ajaxData: Record<string, any> = { id: id };
                cF.$ajax.post(url, ajaxData, function (res: AjaxResponse): void {
                    Swal.fire({ text: res.message }).then(function (): void {
                        if (res.rslt) cF.ui.blockUIReload();
                    });
                }, "block");
            });
        },
    }
})();
