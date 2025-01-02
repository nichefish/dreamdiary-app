/**
 * vcatn_schdul_list.ts
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
            cF.datepicker.singleDatePicker("#bgnDt", "yyyy-MM-DD", null, function(): void {
                Page.noBefore("#bgnDt", "#endDt");
            });
            cF.datepicker.singleDatePicker("#endDt", "yyyy-MM-DD", null, function(): void {
                if ($("#endDt").val() !== "") Page.noBefore("#bgnDt", "#endDt");
            });

            /* 모든 table 헤더에 클릭 이벤트를 설정한다. */
            cF.util.initSortTable();

            /* jquery validation */
            $(function(): void {
                $("#vcantSchdulRegForm").validate({
                    submitHandler: function(): boolean {
                        const isReg = ($("#vcatnSchdulNo").val() === "");
                        Swal.fire({
                            text: Message.get(isReg ? "view.cnfm.reg" : "view.cnfm.mdf"),
                            showCancelButton: true,
                        }).then(function(result: SwalResult): void {
                            if (!result.value) return;

                            VcatnSchdul.regAjax();
                        });
                        return false;
                    },
                    errorPlacement: function(error: JQuery<HTMLElement>, element: JQuery<HTMLElement>): void {
                        cF.validate.errorSpan(error, element);        // 공통 함수로 분리
                    }
                });
            });
        },

        /**
         * 유효성 검사:: 시작일 이전 날짜 선택 불가하게 막기
         */
        noBefore: function(beginSelectorStr: string, endSelectorStr: string): void {
            const vcatnCdElement: HTMLSelectElement = document.getElementById("vcatnCd") as HTMLSelectElement;
            const endElmt: HTMLInputElement = document.querySelector(endSelectorStr) as HTMLInputElement;
            const vcatnCd: string = vcatnCdElement?.value ?? '';
            if (vcatnCd === "AM_HALF" || vcatnCd === "PM_HALF") {
                const beginElmt: HTMLInputElement = document.querySelector(beginSelectorStr) as HTMLInputElement;
                if (beginElmt && endElmt) endElmt.value = beginElmt.value;
            } else {
                const isBefore: boolean = cF.date.isBefore(beginSelectorStr, endSelectorStr);
                if (!isBefore && endElmt.value !== "") {
                    Swal.fire("휴가일을 다시 설정해 주세요.");
                    endElmt.value = "";
                }
            }
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});