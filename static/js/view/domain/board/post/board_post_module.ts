/**
 * board_post_module.ts
 *
 * @author nichefish
 */
const BoardPost: Module = (function(): Module {
    return {
        isMdf: $("#boardPostRegForm").data("mode") === "modify",

        /**
         * initializes module.
         */
        init: function(): void {
            console.log("'BoardPost' module initialized.");
        },

        /**
         * form init
         * @param {Object} obj - 폼에 바인딩할 데이터
         */
        initForm: function(obj = {}) {
            /* jquery validation */
            cF.validate.validateForm("#postRegForm", BoardPost.submitHandler);
            /* tinymce init */
            cF.tinymce.init("#tinymce_cn");
            /* tagify */
            cF.tagify.initWithCtgr("#postRegForm #tagListStr");
            // 잔디발송여부 클릭시 글씨 변경
            cF.util.chckboxLabel("jandiYn", "발송//미발송", "blue//gray", function(): void {
                $("#trgetTopicSpan").show();
            }, function(): void {
                $("#trgetTopicSpan").hide();
            });
        },

        /**
         * Custom SubmitHandler
         */
        submitHandler: function(): boolean {
            if (Page.submitMode === "preview") {
                const popupNm: string = "preview";
                const options: string = 'width=1280,height=1440,top=0,left=270';
                const popup = cF.util.openPopup("", popupNm, options);
                if (popup) popup.focus();
                const popupUrl: string = Url.BOARD_POST_REG_PREVIEW_POP;
                $("#postRegForm").attr("action", popupUrl).attr("target", popupNm);
                return true;
            } else if (Page.submitMode === "submit") {
                $("#postRegForm").removeAttr("action");
                Swal.fire({
                    text: BoardPost.isMdf ? Message.get("view.cnfm.mdf") : Message.get("view.cnfm.reg"),
                    showCancelButton: true,
                }).then(function(result: SwalResult): void {
                    if (!result.value) return;

                    BoardPost.regAjax();
                });
            }
        },

        /**
         * 목록 검색
         */
        search: function(): void {
            $("#listForm #pageNo").val(1);
            cF.util.blockUISubmit("#listForm", Url.BOARD_POST_LIST + "?actionTyCd=SEARCH");
        },

        /**
         * 내가 작성한 글 목록 보기
         */
        myPaprList: function(): void {
            const boardCdElement: HTMLInputElement|null = document.querySelector("#boardCd");
            if (!boardCdElement) return;  // 요소가 없으면 종료

            const boardCd: string = boardCdElement.value;
            const url: string = Url.BOARD_POST_LIST;
            const param: string = `??boardCd=${boardCd!}&searchType=nickNm&searchKeyword=${AuthInfo.nickNm!}&regstrId=${AuthInfo.userId!}&pageSize=50&actionTyCd=MY_PAPR`;
            cF.util.blockUIReplace(url + param);
        },

        /**
         * 등록 화면으로 이동
         */
        regForm: function(): void {
            cF.util.blockUISubmit("#procForm", Url.BOARD_POST_REG_FORM);
        },

        /**
         * form submit
         */
        submit: function(): void {
            if (tinymce !== undefined) tinymce.activeEditor.save();
            Page.submitMode = "submit";
            $("#postRegForm").submit();
        },

        /**
         * 미리보기 팝업 호출
         */
        preview: function(): void {
            if (tinymce !== undefined) tinymce.activeEditor.save();
            Page.submitMode = "preview";
            $("#postRegForm").submit();
        },

        /**
         * 등록/수정 처리(Ajax)
         */
        regAjax: function(): void {
            const url: string = BoardPost.isMdf ? Url.BOARD_POST_MDF_AJAX : Url.BOARD_POST_REG_AJAX;
            const ajaxData: FormData = new FormData(document.getElementById("postRegForm") as HTMLFormElement);
            cF.ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                Swal.fire({ text: res.message })
                    .then(function(): void {
                        if (res.rslt) BoardPost.list();
                    });
            }, "block");
        },

        /**
         * 상세 화면으로 이동
         * @param {string|number} postNo - 조회할 글 번호.
         */
        dtl: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            $("#procForm #postNo").val(postNo);
            cF.util.blockUISubmit("#procForm", Url.BOARD_POST_DTL);
        },

        /**
         * 상세 모달 호출
         * @param {string|number} postNo - 조회할 글 번호.
         */
        dtlModal: function(postNo: string|number): void {
            event.stopPropagation();
            if (isNaN(Number(postNo))) return;

            const url: string = Url.BOARD_POST_DTL_AJAX;
            const ajaxData: Record<string, any> = { "postNo": postNo, "boardCd": $("#boardCd").val() };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                cF.handlebars.template(res.rsltObj, "board_post_dtl", "show");
            });
        },

        /**
         * 수정 화면으로 이동
         */
        mdfForm: function(): void {
            cF.util.blockUISubmit("#procForm", Url.BOARD_POST_MDF_FORM);
        },

        /**
         * 삭제 (Ajax)
         * @param {string|number} postNo - 글 번호.
         */
        delAjax: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = Url.BOARD_POST_DEL_AJAX;
                const ajaxData: Record<string, any> = $("#procForm").serializeArray();
                cF.ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) BoardPost.list();
                        });
                }, "block");
            });
        },

        /**
         * 목록 화면으로 이동
         */
        list: function(): void {
            const boardCd = $("#boardCd").val();
            const listUrl: string = `${Url.BOARD_POST_LIST}?boardCd${boardCd}` + (BoardPost.isMdf ? "&isBackToList=Y" : "");
            cF.util.blockUIReplace(listUrl);
        }
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    BoardPost.init();
});