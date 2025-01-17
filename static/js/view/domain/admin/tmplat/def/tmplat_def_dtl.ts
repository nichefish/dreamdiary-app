/**
 * tmplat_def_dtl.ts
 * 템플릿 정의 상세 페이지 스크립트
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
            /* initialize modules. */
            dF.TmplatDef.init();
        },

        /**
         * 수정 화면으로 이동
         */
        mdfForm: function(): void {
            cF.form.blockUISubmit("#procForm", Url.TMPLAT_MDF_FORM);
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
                const ajaxData: Record<string, any> = cF.util.getJsonFormData("#procForm");
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
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
            cF.ui.blockUIReplace(listUrl);
        }
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});