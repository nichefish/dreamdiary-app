/**
 * atch_file_module.ts
 *
 * @author nichefish
 */
const AtchFile: Module = (function(): Module {
    return {
        /**
         * AtchFile 객체 초기화
         */
        init: function(): void {
            console.log("'AtchFile' module initialized.");

            AtchFile.atchFileListToggle();     // 첨부파일 영역 0개인지 체크
        },

        /**
         * 첨부파일 추가추가
         * (reqstItemIdx는 어차피 한 페이지 내에서 고유하므로 따로 처리해줄 필요 없다.)
         */
        addFileItem: function(): void {
            const reqstItemIdx: number = cF.util.getReqstItemIdx("input", "id^=atchFile", "atchFile");		// elmt, selector, elmtId
            // 감췄다 숨겼다 할것이므로 style로 설정한다.
            const tableTmpl: string = "<div class='row' id='itemContainer"+reqstItemIdx+"' style='display:none;'>" + $("#atchFileTemplate").html() + "</div>";
            const str: string = tableTmpl.replace(/__INDEX__/g, String(reqstItemIdx));

            // 공격 탐지 내역 목록 관련 처리 로직
            const atchFileSpan: HTMLElement = document.getElementById("atchFileSpan");
            if (atchFileSpan) atchFileSpan.insertAdjacentHTML("beforeend", str);

            const atchFileDiv = document.getElementById("atchFile" + reqstItemIdx) as HTMLInputElement;
            if (atchFileDiv) {
                atchFileDiv.click();

                atchFileDiv.addEventListener("change", function(): void {
                    const newFileSpan: HTMLElement = document.getElementById("itemContainer" + reqstItemIdx) as HTMLElement;
                    if (this.value !== "") {
                        if (!cF.validate.fileSizeChck(this) || !cF.validate.fileExtnChck(this)) {
                            newFileSpan.remove();
                        }

                        const filename: string = (document.getElementById("atchFile" + reqstItemIdx) as HTMLInputElement).value.split('/').pop()?.split('\\').pop();
                        const fileNmSpan: HTMLElement = document.getElementById("fileNm" + reqstItemIdx);
                        if (fileNmSpan) fileNmSpan.textContent = filename || "";

                        newFileSpan.style.display = "block";
                    } else {
                        newFileSpan.remove();
                    }
                    AtchFile.atchFileListToggle();
                });
            }
        },

        /**
         * 추가추가 개수 0개인지 체크
         */
        atchFileListToggle: function(): void {
            if ($("#atchFileSpan div[id^=itemContainer]").length === 0) {
                $("#emptyFileListDiv").show();
            } else {
                $("#emptyFileListDiv").hide();
            }
        },

        /**
         * 새로 추가된 첨부파일 영역(div) 삭제
         * @param {number} idx - 추가된 첨부파일 영역(div) 삭제.
         */
        delNewFileSpan: function(idx: number): void {
            if (isNaN(idx)) return;

            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                $("div#itemContainer"+idx).remove();
            });
        },

        /**
         * 기존 첨부파일 삭제 플래그 세팅
         * @param {string|number} atchFileDtlNo - 첨부파일 상세 번호.
         */
        delExistingFile: function(atchFileDtlNo: string|number): void {
            if (isNaN(Number(atchFileDtlNo))) return;

            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                $("#atchCtrl" + atchFileDtlNo).val("D");
                $("div#itemContainer"+atchFileDtlNo).hide();
            });
        }
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    AtchFile.init();
});