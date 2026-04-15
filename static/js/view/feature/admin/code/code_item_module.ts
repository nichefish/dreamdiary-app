/**
 * code_item_module.ts
 * 상세 코드 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.CodeItem = (function(): dfModule {
    return {
        initialized: false,
        swappable: null,

        init: function(): void {
            if (dF.CodeItem.initialized) return;
            dF.CodeItem.initialized = true;
            console.log("'dF.CodeItem' module initialized.");
        },

        initForm: function(obj: Record<string, any> = {}): void {
            cF.handlebars.modal(obj, "code_item_reg");
            cF.validate.validateForm("#dtlCdRegForm", dF.CodeItem.regAjax);
            cF.ui.chckboxLabel("#dtlCdRegForm #useYn", "사용//미사용", "blue//gray");
            cF.validate.replaceBlankIfMatches("#dtlCdRegForm #dtlCd", cF.regex.nonCd);
            cF.validate.toUpperCase("#dtlCdRegForm #dtlCd");
        },

        initDraggable: function(): void {
            const hasZone: boolean = !!document.querySelector(".draggable-zone-dtl-cd");
            if (!hasZone) return;
            const keyExtractor: Function = (item: HTMLElement) => ({ "clCd": $("#clCd").val(), "dtlCd": item.dataset.id || item.id });
            const url: string = Url.CODE_ITEMS_SORT_ORDERS;
            dF.CodeItem.swappable = cF.draggable.init("-dtl-cd", keyExtractor, url);
        },

        regModal: function(): void {
            event.stopPropagation();
            const obj: Record<string, any> = { "clCd": $("#clCd").val() };
            $("#code_group_dtl_modal").modal("hide");
            dF.CodeItem.initForm(obj);
        },

        submit: function(): void { $("#dtlCdRegForm").submit(); },

        regAjax: function(): void {
            Swal.fire({ text: Message.get("view.cnfm.save"), showCancelButton: true }).then(function(result: SwalResult): void {
                if (!result.value) return;
                const regYn: string = String($("#dtlCdRegForm #regYn").val() || "Y").toUpperCase();
                const url: string = regYn === "Y" ? Url.CODE_ITEMS : Url.CODE_ITEM;
                const ajaxData: Record<string, any> = cF.util.getJsonFormData("#dtlCdRegForm");
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message }).then(function(): void { if (res.rslt) cF.ui.blockUIReload(); });
                }, "block");
            });
        },

        mdfModal: function(dtlCd: string): void {
            event.stopPropagation();
            const url: string = Url.CODE_ITEM;
            const ajaxData: Record<string, any> = { "clCd": $("#clCd").val(), "dtlCd": dtlCd };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltObj } = res;
                rsltObj.isMdf = true;
                $("#code_group_dtl_modal").modal("hide");
                dF.CodeItem.initForm(rsltObj);
            });
        },

        useAjax: function(dtlCd: string): void {
            event.stopPropagation();
            Swal.fire({ text: Message.get("view.cnfm.use"), showCancelButton: true }).then(function(result: SwalResult): void {
                if (!result.value) return;
                const url: string = Url.CODE_ITEM_USE;
                const ajaxData: Record<string, any> = { "clCd": $("#clCd").val(), "dtlCd": dtlCd };
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message }).then(function(): void { if (res.rslt) cF.ui.blockUIReload(); });
                }, "block");
            });
        },

        unuseAjax: function(dtlCd: string): void {
            event.stopPropagation();
            Swal.fire({ text: Message.get("view.cnfm.unuse"), showCancelButton: true }).then(function(result: SwalResult): void {
                if (!result.value) return;
                const url: string = Url.CODE_ITEM_UNUSE;
                const ajaxData: Record<string, any> = { "clCd": $("#clCd").val(), "dtlCd": dtlCd };
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message }).then(function(): void { if (res.rslt) cF.ui.blockUIReload(); });
                }, "block");
            });
        },

        delAjax: function(dtlCd: string): void {
            event.stopPropagation();
            Swal.fire({ text: Message.get("view.cnfm.del"), showCancelButton: true }).then(function(result: SwalResult): void {
                if (!result.value) return;
                const url: string = Url.CODE_ITEM;
                const ajaxData: Record<string, any> = { "clCd": $("#clCd").val(), "dtlCd": dtlCd };
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message }).then(function(): void { if (res.rslt) cF.ui.blockUIReload(); });
                }, "block");
            });
        },
    }
})();
