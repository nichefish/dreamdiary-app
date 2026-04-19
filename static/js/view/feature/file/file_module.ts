/**
 * file_module.ts
 * 첨부파일 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.FileGroup = (function(): dfModule {
    return {
        initialized: false,

        /**
         * FileGroup 객체 초기화
         */
        init: function(): void {
            if (dF.FileGroup.initialized) return;

            dF.FileGroup.fileRecordListToggle();     // 첨부파일 영역 0개인지 체크

            dF.FileGroup.initialized = true;
            console.log("'dF.FileGroup' module initialized.");
        },

        /**
         * 첨부파일 추가추가
         * (reqstItemIdx는 어차피 한 페이지 내에서 고유하므로 따로 처리해줄 필요 없다.)
         */
        addFileItem: function(): void {
            const reqstItemIdx: number = cF.util.getReqstItemIdx("input", "id^=fileGroup", "fileGroup");		// elmt, selector, elmtId
            // 감췄다 숨겼다 할것이므로 style로 설정한다.
            const tableTmpl: string = "<div class='row' id='itemContainer"+reqstItemIdx+"' style='display:none;'>" + $("#fileGroupTemplate").html() + "</div>";
            const str: string = tableTmpl.replace(/__INDEX__/g, String(reqstItemIdx));

            // 공격 탐지 내역 목록 관련 처리 로직
            const fileGroupSpan: HTMLElement = document.getElementById("fileGroupSpan");
            if (fileGroupSpan) fileGroupSpan.insertAdjacentHTML("beforeend", str);

            const fileGroupDiv: HTMLInputElement = document.getElementById("fileGroup" + reqstItemIdx) as HTMLInputElement;
            if (fileGroupDiv) {
                fileGroupDiv.click();

                fileGroupDiv.addEventListener("change", function(): void {
                    const newFileSpan: HTMLElement = document.getElementById("itemContainer" + reqstItemIdx) as HTMLElement;
                    if (this.value !== "") {
                        if (!cF.validate.fileSizeChck(this) || !cF.validate.fileExtnChck(this)) {
                            newFileSpan.remove();
                        }

                        const filename: string = (document.getElementById("fileGroup" + reqstItemIdx) as HTMLInputElement).value.split('/').pop()?.split('\\').pop();
                        const fileNmSpan: HTMLElement = document.getElementById("fileNm" + reqstItemIdx);
                        if (fileNmSpan) fileNmSpan.textContent = filename || "";

                        newFileSpan.style.display = "block";
                    } else {
                        newFileSpan.remove();
                    }
                    dF.FileGroup.fileRecordListToggle();
                });
            }
        },

        /**
         * 추가추가 개수 0개인지 체크
         */
        fileRecordListToggle: function(): void {
            if ($("#fileGroupSpan div[id^=itemContainer]").length === 0) {
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
         * @param {string|number} fileRecordId - 첨부파일 상세 번호.
         */
        delExistingFile: function(fileRecordId: string|number): void {
            if (isNaN(Number(fileRecordId))) return;

            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                $("#atchCtrl" + fileRecordId).val("D");
                $("div#itemContainer"+fileRecordId).hide();
            });
        }
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    dF.FileGroup.init();
});
