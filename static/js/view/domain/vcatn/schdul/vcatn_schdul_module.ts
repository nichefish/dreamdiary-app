/**
 * vcatn_schdul_module.ts
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.VcatnSchdul = (function(): Module {
    return {
        /**
         * initializes module.
         */
        init: function(): void {
            console.log("'VcatnSchdul' module initialized.");
        },

        /**
         * 조회년도 변경
         */
        chgYy: function(obj): void {
            const url: string = `${Url.VCATN_SCHDUL_LIST!}?statsYy=` + $(obj).val();
            cF.util.blockUIReplace(url);
        },

        /**
         * 등록 모달 호출
         */
        regModal: function(): void {
            cF.handlebars.template({}, "vcatn_schdul_reg", "show");
            cF.datepicker.singleDatePicker("#bgnDt", "yyyy-MM-DD", null, function(): void {
                dF.VcatnSchdul.propEndDt();
                dF.VcatnSchdul.noBefore("#bgnDt", "#endDt");
            });
            cF.datepicker.singleDatePicker("#endDt", "yyyy-MM-DD", null, function(): void {
                if ($("#endDt").val() !== "") dF.VcatnSchdul.noBefore("#bgnDt", "#endDt");
            });
        },

        /**
         * 반차 여부 체크
         * @param {number} idx 행 인덱스.
         * @return {boolean} 반차 여부.
         */
        isHalf: function(idx?: number) : boolean {
            const validIdx: string = cF.util.isNotEmpty(idx) ? String(idx) : "";

            const vcatnCdElement: HTMLSelectElement = document.querySelector("#vcatnCd" + validIdx) as HTMLSelectElement;
            const vcatnCd: string = vcatnCdElement?.value;
            return vcatnCd === "AM_HALF" || vcatnCd === "PM_HALF";
        },

        /**
         * 시작일 이전 날짜 선택 불가
         */
        noBefore: function(beginSelectorStr: string, endSelectorStr: string, idx: number): void {
            const endElmt: HTMLInputElement = document.querySelector(endSelectorStr) as HTMLInputElement;
            if (dF.VcatnSchdul.isHalf(idx)) {
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

        /**
         * form submit
         */
        submit: function(): void {
            $("#vcantSchdulRegForm").submit();
        },

        /**
         * 등록/수정 처리(Ajax)
         */
        regAjax: function(): void {
            const isReg: boolean = ($("#vcatnSchdulNo").val() === "");
            const url: string = isReg ? Url.VCATN_SCHDUL_REG_AJAX : Url.VCATN_SCHDUL_MDF_AJAX;
            const ajaxData: FormData = new FormData(document.getElementById("vcantSchdulRegForm") as HTMLFormElement);
            cF.ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                Swal.fire({ text: res.message })
                    .then(function(): void {
                        if (res.rslt) cF.util.blockUIReload();
                    });
            }, "block");
        },

        /**
         * 반차 선택 시 종료일 안보이게
         */
        propEndDt: function(idx?: number): void {
            const isHalf: boolean = dF.VcatnSchdul.isHalf(idx);
            const validIdx: string = cF.util.isNotEmpty(idx) ? String(idx) : "";

            const lineDiv: HTMLElement = document.getElementById("lineDiv" + validIdx);
            const endDtDiv: HTMLElement = document.getElementById("endDtDiv" + validIdx);

            if (isHalf) {
                lineDiv?.classList.add("d-none");
                endDtDiv?.classList.add("d-none");

                const endDt: HTMLInputElement = document.getElementById("endDt" + validIdx) as HTMLInputElement;
                const bgnDt: HTMLInputElement = document.getElementById("bgnDt" + validIdx) as HTMLInputElement;
                if (bgnDt && endDt) {
                    endDt.value = bgnDt.value;  // bgnDt 값을 endDt에 설정
                }
                const endDtValidateSpan: HTMLElement|null = document.getElementById("endDt" + validIdx     + "_validate_span") as HTMLElement;
                if (endDtValidateSpan) endDtValidateSpan.innerHTML = "";  // validate span 비우기
            } else {
                lineDiv?.classList.remove("d-none");
                endDtDiv?.classList.remove("d-none");
            }
        },

        /**
         * 수정 팝업 호출
         */
        mdfModal: function(vcatnSchdulNo: string|number): void {
            if (isNaN(Number(vcatnSchdulNo))) return;

            $("#vcatnSchdulNoProc").val(vcatnSchdulNo);
            const url: string = Url.VCATN_SCHDUL_DTL_AJAX;
            const ajaxData: Record<string, any> = $("#procForm").serializeArray();
            cF.ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) return;

                const { rsltObj } = res;
                // 수정 modal_form에 값 세팅
                $("#vcatnSchdulNo").val(vcatnSchdulNo);
                $("#userId").val(rsltObj.userId);
                $("#vcatnCd").val(rsltObj.vcatnCd);
                $("#bgnDt").val(rsltObj.bgnDt);
                $("#endDt").val(rsltObj.endDt);
                $("#resn").val(rsltObj.resn);
                $("#vcatnRm").val(rsltObj.vcatnRm);
                $("#vcatn_schdul_reg_modal").modal("show");
            });
        },

        /**
         * 엑셀 다운로드
         */
        xlsxDownload: function(): void {
            Swal.fire({
                text: Message.get("view.cnfm.download"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                cF.util.blockUIFileDownload();
                $("#procForm").attr("action", Url.VCATN_SCHDUL_XLSX_DOWNLOAD).submit();
            });
        },

        /**
         * 삭제 (Ajax)
         * @param {string|number} vcatnSchdulNo - 일정 번호.
         */
        delAjax: function(vcatnSchdulNo: string|number): void {
            if (isNaN(Number(vcatnSchdulNo))) return;

            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                $("#vcatnSchdulNoProc").val(vcatnSchdulNo);
                const url: string = Url.VCATN_SCHDUL_DEL_AJAX;
                const ajaxData: Record<string, any> = $("#procForm").serializeArray();
                cF.ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.util.blockUIReload();
                        });
                }, "block");
            });
        }
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    dF.VcatnSchdul.init();
});