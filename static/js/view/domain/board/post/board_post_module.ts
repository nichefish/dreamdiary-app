/**
 * board_post_module.ts
 *
 * @author nichefish
 */
const BoardPost = (function() {
    return {
        isMdf: $("#boardPostRegForm").data("mode") === "modify",

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
            cF.util.chckboxLabel("jandiYn", "발송//미발송", "blue//gray", function() {
                $("#trgetTopicSpan").show();
            }, function() {
                $("#trgetTopicSpan").hide();
            });
        },

        /**
         * Custom SubmitHandler
         */
        submitHandler: function() {
            if (Page.submitMode === "preview") {
                const popupNm = "preview";
                const options = 'width=1280,height=1440,top=0,left=270';
                const popup = cF.util.openPopup("", popupNm, options);
                if (popup) popup.focus();
                const popupUrl = Url.BOARD_POST_REG_PREVIEW_POP;
                $("#postRegForm").attr("action", popupUrl).attr("target", popupNm);
                return true;
            } else if (Page.submitMode === "submit") {
                $("#postRegForm").removeAttr("action");
                Swal.fire({
                    text: BoardPost.isMdf ? Message.get("view.cnfm.mdf") : Message.get("view.cnfm.reg"),
                    showCancelButton: true,
                }).then(function(result: SwalResult) {
                    if (!result.value) return;

                    BoardPost.regAjax();
                });
            }
        },

        /**
         * 목록 검색
         */
        search: function() {
            $("#listForm #pageNo").val(1);
            cF.util.blockUISubmit("#listForm", Url.BOARD_POST_LIST + "?actionTyCd=SEARCH");
        },

        /**
         * 내가 작성한 글 목록 보기
         */
        myPaprList: function() {
            const boardCd = $("#boardCd").val();
            const url = Url.BOARD_POST_LIST;
            const param = `??boardCd=${boardCd!}&searchType=nickNm&searchKeyword=${AuthInfo.nickNm!}&regstrId=${AuthInfo.userId!}&pageSize=50&actionTyCd=MY_PAPR`;
            cF.util.blockUIReplace(url + param);
        },

        /**
         * 등록 화면으로 이동
         */
        regForm: function() {
            cF.util.blockUISubmit("#procForm", Url.BOARD_POST_REG_FORM);
        },

        /**
         * form submit
         */
        submit: function() {
            if (tinymce !== undefined) tinymce.activeEditor.save();
            Page.submitMode = "submit";
            $("#postRegForm").submit();
        },

        /**
         * 미리보기 팝업 호출
         */
        preview: function() {
            if (tinymce !== undefined) tinymce.activeEditor.save();
            Page.submitMode = "preview";
            $("#postRegForm").submit();
        },

        /**
         * 등록/수정 처리(Ajax)
         */
        regAjax: function() {
            const url = "<#if isMdf!false>${Url.BOARD_POST_MDF_AJAX!}<#else>${Url.BOARD_POST_REG_AJAX!}</#if>";
            const ajaxData = new FormData(document.getElementById("postRegForm"));
            cF.ajax.multipart(url, ajaxData, function(res) {
                Swal.fire({ text: res.message })
                    .then(function() {
                        if (res.rslt) BoardPost.list();
                    });
            }, "block");
        },

        /**
         * 상세 화면으로 이동
         * @param {string|number} postNo - 조회할 글 번호.
         */
        dtl: function(postNo: string|number) {
            if (isNaN(postNo)) return;

            $("#procForm #postNo").val(postNo);
            cF.util.blockUISubmit("#procForm", Url.BOARD_POST_DTL);
        },

        /**
         * 상세 모달 호출
         * @param {string|number} postNo - 조회할 글 번호.
         */
        dtlModal: function(postNo: string|number) {
            event.stopPropagation();
            if (isNaN(postNo)) return;

            const url = Url.BOARD_POST_DTL_AJAX;
            const ajaxData = { "postNo": postNo, "boardCd": $("#boardCd").val() };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse) {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return false;
                }
                cF.handlebars.template(res.rsltObj, "board_post_dtl", "show");
            });
        },

        /**
         * 수정 화면으로 이동
         */
        mdfForm: function() {
            cF.util.blockUISubmit("#procForm", Url.BOARD_POST_MDF_FORM);
        },

        /**
         * 삭제 (Ajax)
         * @param {string|number} postNo - 글 번호.
         */
        delAjax: function(postNo: string|number) {
            if (isNaN(postNo)) return;

            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult) {
                if (!result.value) return;

                const url = Url.BOARD_POST_DEL_AJAX;
                const ajaxData = $("#procForm").serializeArray();
                cF.ajax.post(url, ajaxData, function(res: AjaxResponse) {
                    Swal.fire({ text: res.message })
                        .then(function() {
                            if (res.rslt) BoardPost.list();
                        });
                }, "block");
            });
        },

        /**
         * 목록 화면으로 이동
         */
        list: function() {
            const boardCd = $("#boardCd").val();
            const listUrl = Url.BOARD_POST_LIST + "?boardCd=" + boardCd + (BoardPost.isMdf ? "&isBackToList=Y" : "");
            cF.util.blockUIReplace(listUrl);
        }
    }
})();