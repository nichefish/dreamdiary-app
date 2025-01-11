/**
 * vcatn_papr_module.ts
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.VcatnPapr = (function(): dfModule {
    return {
        initialized: false,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.VcatnPapr.initialized) return;

            dF.VcatnPapr.initialized = true;
            console.log("'dF.VcatnPapr' module initialized.");
        },

        /**
         * form init
         */
        initForm: function(): void {
            /* jquery validation */
            cF.validate.validateForm("#vcatnPaprRegForm", dF.VcatnPapr.submitHandler);
            // 잔디발송여부 클릭시 글씨 변경
            cF.util.chckboxLabel("jandiYn", "발송//미발송", "#0095E8//gray", function(): void {
                $("#trgetTopicSpan").show();
            }, function(): void {
                $("#trgetTopicSpan").hide();
            });
        },

        /**
         * Custom SubmitHandler
         */
        submitHandler: function(): void {
            const isReg: boolean = $("#noticeRegForm").data("mode") === "regist";
            Swal.fire({
                text: Message.get(isReg ? "view.cnfm.reg" : "view.cnfm.mdf"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                dF.VcatnPapr.regAjax();
            });
        },

        /**
         * 목록 검색
         */
        search: function(): void {
            $("#listForm #pageNo").val(1);
            const url: string = `${Url.VCATN_PAPR_LIST}?actionTyCd=SEARCH`;
            cF.util.blockUISubmit("#listForm", url);
        },

        /**
         * 내가 작성한 글 목록 보기
         */
        myPaprList: function(): void {
            const url: string = Url.VCATN_PAPR_LIST;
            const param: string = `?searchType=nickNm&searchKeyword=${AuthInfo.nickNm}&regstrId=${AuthInfo.userId}&pageSize=50&actionTyCd=MY_PAPR`;
            cF.util.blockUIReplace(url + param);
        },

        /**
         * 등록 화면으로 이동
         */
        regForm: function(): void {
            cF.util.blockUISubmit("#procForm", Url.VCATN_PAPR_REG_FORM);
        },

        /**
         * form submit
         */
        submit: function(): void {
            $("#vcatnPaprRegForm").submit();
        },

        /**
         * 등록/수정 처리(Ajax)
         */
        regAjax: function(): void {
            const isReg: boolean = $("#noticeRegForm").data("mode") === "regist";
            const url: string = isReg ? Url.VCATN_PAPR_REG_AJAX : Url.VCATN_PAPR_MDF_AJAX;
            const ajaxData: FormData = new FormData(document.getElementById("vcatnPaprRegForm") as HTMLFormElement);
            cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                Swal.fire({ text: res.message })
                    .then(function(): void {
                        if (res.rslt) dF.VcatnPapr.list();
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
            cF.util.blockUISubmit("#procForm", Url.VCATN_PAPR_DTL);
        },

        /**
         * 상세 모달 호출
         * @param {string|number} postNo - 조회할 글 번호.
         */
        dtlModal: function(postNo: string|number): void {
            event.stopPropagation();
            if (isNaN(Number(postNo))) return;

            const url: string = Url.VCATN_PAPR_DTL_AJAX;
            const ajaxData: Record<string, any> = { "postNo": postNo };
            cF.ajax.get(url, ajaxData, function(res): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                cF.handlebars.modal(res.rsltObj, "vcatn_papr_dtl");
            });
        },

        /**
         * 수정 화면으로 이동
         */
        mdfForm: function(): void {
            cF.util.blockUISubmit("#procForm", Url.VCATN_PAPR_MDF_FORM);
        },

        /**
         * 관리자 확인 여부 변경 (Ajax)
         */
        cfAjax: function(): void {
            const url: string = Url.VCATN_PAPR_CF_AJAX;
            const ajaxData: Record<string, any> = cF.util.getJsonFormData("#procForm");
            cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                Swal.fire({ text: res.message })
                    .then(function(): void {
                        if (res.rslt) cF.util.blockUIReload();
                    });
            }, "block");
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

                const url: string = Url.VCATN_PAPR_DEL_AJAX;
                const ajaxData: Record<string, any> = cF.util.getJsonFormData("#procForm");
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) dF.VcatnPapr.list();
                        });
                }, "block");
            });
        },

        /**
         * 목록 화면으로 이동
         */
        list: function(): void {
            const listUrl: string = `${Url.VCATN_PAPR_LIST}?isBackToList=Y`;
            cF.util.blockUIReplace(listUrl);
        }
    }
})();