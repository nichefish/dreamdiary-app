/**
 * tmplat_def_dtl.ts
 *
 * @author nichefish
 */
// @ts-ignore
const Page: Page = (function(): Page {
    return {
        /**
         * Page 객체 초기화
         */
        init: function(): void {
            console.log("Page scripts initialized.");
        },

        /**
         * 수정 화면으로 이동
         */
        mdfForm: function(): void {
            cF.util.blockUISubmit("#procForm", Url.TMPLAT_MDF_FORM);
        },

        /**
         * 삭제 (Ajax)
         */
        delAjax: function(): void {
            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
        }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = Url.TMPLAT_DEL_AJAX;
                const ajaxData: Record<string, any> = $("#procForm").serializeArray();
                cF.ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) Page.list();
                        });
                });
            });
        },

        /**
         * 목록 화면으로 이동
         */
        list: function() {
            const listUrl: string = `${Url.NOTICE_LIST!}?isBackToList=Y`;
            cF.util.blockUIReplace(listUrl);
        }
    }
})();