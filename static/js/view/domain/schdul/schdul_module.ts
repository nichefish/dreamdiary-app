/**
 * schdul_module.ts
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.Schdul = (function(): dfModule {
    return {
        initialized: false,
        prtcpntCnt: 0,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.Schdul.initialized) return;

            dF.Schdul.initialized = true;
            console.log("'dF.Schdul' module initialized.");
        },

        /**
         * form init
         */
        initForm: function(): void {
            /* jquery validation */
            cF.validate.validateForm("#schdulRegForm", dF.Schdul.submitHandler);
            // 엔터키 처리
            cF.util.enterKey("#searchKeyword", Page.search);
        },

        /**
         * Custom SubmitHandler
         */
        submitHandler: function(): void {
            const isReg: boolean = ($("#schdulRegForm #postNo").val() === "");
            Swal.fire({
                text: isReg ? Message.get("view.cnfm.reg") : Message.get("view.cnfm.mdf"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;
                dF.Schdul.regAjax();
            });
        },

        /**
         * 등록 모달 호출
         */
        regModal: function(): void {
            cF.handlebars.template({}, "schdul_reg", "show");
            cF.util.chckboxLabel("jandiYn", "발송//미발송", "blue//gray");
            cF.datepicker.singleDatePicker("#bgnDt", "yyyy-MM-DD");
            cF.datepicker.singleDatePicker("#endDt", "yyyy-MM-DD");

            // 잔디발송여부 클릭시 글씨 변경
            cF.util.chckboxLabel("jandiYn", "발송//미발송", "blue//gray", function(): void {
                $("#trgetTopicSpan").show();
            }, function(): void {
                $("#trgetTopicSpan").hide();
            });
            $("#jandiYn").click();
            dF.Schdul.addPrtcpnt();
        },

        /**
         * 일정 등록 모달 호출 (개인일정)
         */
        prvtRegModal: function(): void {
            cF.handlebars.template({ "isPrvt": true }, "schdul_reg", "show");
            cF.datepicker.singleDatePicker("#bgnDt", "yyyy-MM-DD");
            cF.datepicker.singleDatePicker("#endDt", "yyyy-MM-DD");
        },

        /**
         * 참여자 추가
         */
        addPrtcpnt: function(): void {
            cF.handlebars.append({ "idx": dF.Schdul.prtcpntCnt++ }, "schdul_reg_prtcpnt");
        },

        /**
         * 참여자 삭제
         */
        removePrtcpnt: function(idx): void {
            $("#schdul_reg_prtcpnt_div #prtcpntRow"+idx).remove();
        },

        /**
         * 종료일자 토글 처리
         */
        schdulCd: function(obj): void {
            if ($(obj).val() === "${Constant.SCHDUL_HLDY!}") {
                $("#endDt").val($("#startDt").val());
                $("#endDtDiv").hide();
                $("#endDtSpan").hide();
            } else {
                $("#endDtDiv").show();
                $("#endDtSpan").show();
            }
        },

        /**
         * form submit
         */
        submit: function(): void {
            $("#schdulRegForm").submit();
        },

        /**
         * 등록/수정 처리(Ajax)
         */
        regAjax: function(): void {
            const url: string = Url.SCHDUL_REG_AJAX;
            const ajaxData: Record<string, any> = cF.util.getJsonFormData("#schdulRegForm");
            cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                Swal.fire({ text: res.message })
                    .then(function(): void {
                        if (res.rslt) cF.util.blockUIReload();
                    });
            }, "block");
        },

        /**
         * 상세 모달 호출
         * @param {string|number} postNo - 조회할 글 번호.
         */
        dtlModal: function(postNo: string|number): void {
            event.stopPropagation();
            if (isNaN(Number(postNo))) return;

            const url: string = Url.SCHDUL_DTL_AJAX;
            const ajaxData: Record<string, any> = { "postNo": postNo };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const obj: Record<string, any> = res.rsltObj;
                cF.handlebars.template(obj, "schdul_dtl", "show");
                dF.Schdul.key = obj.postNo;
            });
        },

        /**
         * 수정 모달 호출
         * @param {string|number} key - 글 번호.
         */
        mdfModal: function(key: string|number): void {
            if (isNaN(Number(key))) return;

            const url: string = Url.SCHDUL_DTL_AJAX;
            const ajaxData: Record<string, any> = { "postNo": key };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }

                $("#schdul_dtl_modal").modal("hide");
                cF.handlebars.template(res.rsltObj, "schdul_reg", "show");
                const rsltObj: Record<string, any> = res.rsltObj;
                const { prtcpntList: prtcpnt } = rsltObj;
                dF.Schdul.prtcpntCnt = prtcpnt !== undefined ? prtcpnt.length : 0;

                cF.datepicker.singleDatePicker("#bgnDt", "yyyy-MM-DD", rsltObj.bgnDt);
                cF.datepicker.singleDatePicker("#endDt", "yyyy-MM-DD", rsltObj.endDt);
                // 잔디발송여부 클릭시 글씨 변경
                cF.util.chckboxLabel("jandiYn", "발송//미발송", "blue//gray", function(): void {
                    $("#trgetTopicSpan").show();
                }, function(): void {
                    $("#trgetTopicSpan").hide();
                });
            });
        },

        /**
         * 삭제 (Ajax)
         * @param {string|number} key - 참여자 번호.
         */
        delAjax: function(key: string|number): void {
            if (isNaN(Number(key))) return;

            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = Url.SCHDUL_DEL_AJAX;
                const ajaxData: Record<string, any> = { "postNo" : key };
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.util.blockUIReload();
                        });
                }, "block");
            });
        }
    }
})();