/**
 * schedule_module.ts
 * 일정 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.Schedule = (function(): dfModule {
    return {
        initialized: false,
        prtcpntCnt: 0,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.Schedule.initialized) return;

            dF.Schedule.initialized = true;
            console.log("'dF.Schedule' module initialized.");
        },

        /**
         * form init
         */
        initForm: function(): void {
            /* jquery validation */
            cF.validate.validateForm("#scheduleRegForm", dF.Schedule.submitHandler);
            // 엔터키 처리
            cF.util.enterKey("#searchKeyword", Page.search);
        },

        /**
         * Custom SubmitHandler
         */
        submitHandler: function(): void {
            const isReg: boolean = ($("#scheduleRegForm #id").val() === "");
            Swal.fire({
                text: isReg ? Message.get("view.cnfm.reg") : Message.get("view.cnfm.mdf"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;
                dF.Schedule.regAjax();
            });
        },

        /**
         * 등록 모달 호출
         */
        regModal: function(): void {
            cF.handlebars.modal({}, "schedule_reg");
            cF.datepicker.singleDatePicker("#bgnDt", "yyyy-MM-DD");
            cF.datepicker.singleDatePicker("#endDt", "yyyy-MM-DD");

            // checkbox init
            cF.ui.chckboxLabel("#jandiYn", "발송//미발송", "blue//gray", function(): void {
                $("#trgetTopicSpan").show();
            }, function(): void {
                $("#trgetTopicSpan").hide();
            });
            $("#jandiYn").click();
            dF.Schedule.addPrtcpnt();
        },

        /**
         * 일정 등록 모달 호출 (개인일정)
         */
        prvtRegModal: function(): void {
            cF.handlebars.modal({ "isPrvt": true }, "schedule_reg");
            cF.datepicker.singleDatePicker("#bgnDt", "yyyy-MM-DD");
            cF.datepicker.singleDatePicker("#endDt", "yyyy-MM-DD");
        },

        /**
         * 참여자 추가
         */
        addPrtcpnt: function(): void {
            cF.handlebars.append({ "idx": dF.Schedule.prtcpntCnt++ }, "schedule_reg_prtcpnt");
        },

        /**
         * 참여자 삭제
         */
        removePrtcpnt: function(idx): void {
            $("#schedule_reg_prtcpnt_div #prtcpntRow"+idx).remove();
        },

        /**
         * 종료일자 토글 처리
         */
        scheduleCd: function(obj): void {
            if ($(obj).val() === "${Code.SCHEDULE_HOLYDAY!}") {
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
            $("#scheduleRegForm").submit();
        },

        /**
         * 등록/수정 처리(Ajax)
         */
        regAjax: function(): void {
            const url: string = Url.SCHEDULE_REG_AJAX;
            const ajaxData: Record<string, any> = cF.util.getJsonFormData("#scheduleRegForm");
            cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                Swal.fire({ text: res.message })
                    .then(function(): void {
                        if (res.rslt) cF.ui.blockUIReload();
                    });
            }, "block");
        },

        /**
         * 상세 모달 호출
         * @param {string|number} id - 조회할 글 번호.
         */
        dtlModal: function(id: string|number): void {
            event.stopPropagation();
            if (isNaN(Number(id))) return;

            const url: string = Url.SCHEDULE_DTL_AJAX;
            const ajaxData: Record<string, any> = { "id": id };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const obj: Record<string, any> = res.rsltObj;
                cF.handlebars.template(obj, "schedule_dtl", "modal");
                dF.Schedule.key = obj.id;
            });
        },

        /**
         * 수정 모달 호출
         * @param {string|number} key - 글 번호.
         */
        mdfModal: function(key: string|number): void {
            if (isNaN(Number(key))) return;

            const url: string = Url.SCHEDULE_DTL_AJAX;
            const ajaxData: Record<string, any> = { "id": key };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }

                $("#schedule_dtl_modal").modal("hide");
                cF.handlebars.modal(res.rsltObj, "schedule_reg");
                const rsltObj: Record<string, any> = res.rsltObj;
                const { prtcpntList: prtcpnt } = rsltObj;
                dF.Schedule.prtcpntCnt = prtcpnt != null ? prtcpnt.length : 0;

                cF.datepicker.singleDatePicker("#bgnDt", "yyyy-MM-DD", rsltObj.bgnDt);
                cF.datepicker.singleDatePicker("#endDt", "yyyy-MM-DD", rsltObj.endDt);
                // 잔디발송여부 클릭시 글씨 변경
                cF.ui.chckboxLabel("#jandiYn", "발송//미발송", "blue//gray", function(): void {
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

                const url: string = Url.SCHEDULE_DEL_AJAX;
                const ajaxData: Record<string, any> = { "id" : key };
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.ui.blockUIReload();
                        });
                }, "block");
            });
        }
    }
})();


