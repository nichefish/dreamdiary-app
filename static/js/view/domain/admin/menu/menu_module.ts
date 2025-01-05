/**
 * menu_module.ts
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.Menu = (function(): Module {
    return {
        /**
         * initializes module.
         */
        init: function(): void {
            console.log("'Menu' module initialized.");
        },

        /**
         * form init
         * @param {Record<string, any> = {}} obj - 폼에 바인딩할 데이터
         */
        initForm: function(obj: Record<string, any> = {} = {}): void {
            /* show modal */
            cF.handlebars.template(obj, "menu_reg", "show");

            /* jquery validation */
            cF.validate.validateForm("#menuRegForm", dF.Menu.regAjax);
            cF.validate.toUpperCase("#menuRegForm #menuLabel");
        },

        /**
         * Draggable 컴포넌트 init
         */
        initDraggable: function(): void {
            const keyExtractor: Function = (item: HTMLElement) => ({ "menuNo": Number($(item).attr("id")), "upperMenuNo": Number($(item).data("upper-menu-no")) });
            const url: string = Url.MENU_SORT_ORDR_AJAX;
            dF.Menu.mainSwappable = cF.draggable.init("-main", keyExtractor, url);
            dF.Menu.subSwappable = cF.draggable.init("-sub", keyExtractor, url);
        },

        /**
         * 등록 모달 호출
         */
        regModal: function(menuTyCd: string, upperMenuNo: number, upperMenuNm: string): void {
            event.stopPropagation();

            const obj: Record<string, any> = { "menuTyCd": menuTyCd, "upperMenuNo": upperMenuNo, "upperMenuNm": upperMenuNm };
            /* initialize form. */
            dF.Menu.initForm(obj);
        },

        /**
         * 아이콘 새로고침
         */
        refreshIcon: function(): void {
            const iconElmt: HTMLInputElement = document.querySelector("#menuRegForm #icon") as HTMLInputElement;
            if (!iconElmt) return;

            const menuIconDiv: HTMLElement = document.querySelector("#menuRegForm #menu_icon_div");
            if (menuIconDiv) menuIconDiv.innerHTML = iconElmt.value;
        },

        /**
         * 하위메뉴 존재여부에 따라 url 영역 표시
         */
        toggleUrlSpan: function(obj: object): void {
            const menuSubExtendTyCd = $(obj).val();
            if (menuSubExtendTyCd !== "NO_SUB") {
                $("#url_div").addClass("d-none");
            } else {
                $("#url_div").removeClass("d-none");
            }
        },

        /**
         * form submit
         */
        submit: function(): void {
            $("#menuRegForm").submit();
        },

        /**
         * 메뉴 정보 등록/수정 (Ajax)
         */
        regAjax: function(): void {
            Swal.fire({
                text: Message.get("view.cnfm.save"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const isReg: boolean = $("#menuRegForm #menuNo").val() === "";
                const url: string = isReg ? Url.MENU_REG_AJAX : Url.MENU_MDF_AJAX;
                const ajaxData: Record<string, any> = cF.util.getJsonFormData("#menuRegForm");
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
         */
        mdfModal: function(menuNo: string|number): void {
            if (isNaN(Number(menuNo))) return;

            const url: string = Url.MENU_DTL_AJAX;
            const ajaxData: Record<string, any> = { "menuNo": menuNo };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltObj } = res;
                /* initialize form. */
                dF.Menu.initForm(rsltObj);
            });
        },

        /**
         * 삭제 (Ajax)
         * @param {string|number} menuNo - 메뉴 번호.
         */
        delAjax: function(menuNo: number): void {
            if (isNaN(Number(menuNo))) return;

            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = Url.MENU_DEL_AJAX;
                const ajaxData: Record<string, any> = { "menuNo": menuNo };
                cF.ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.util.blockUIReload();
                        });
                }, "block");
            });
        },

        /**
         * 목록 검색
         */
        search: function(): void {
            $("#listForm #pageNo").val(1);
            cF.util.blockUISubmit("#listForm", Url.LOG_ACTVTY_LIST + "?actionTyCd=SEARCH");
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    dF.Menu.init();
});