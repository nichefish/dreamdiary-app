/**
 * jrnl_day_module.ts
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.JrnlDay = (function(): Module {
    return {
        /**
         * initializes module.
         */
        init: function(): void {
            console.log("'JrnlDay' module initialized.");
        },

        /**
         * form init
         * @param {Record<string, any>} obj - 폼에 바인딩할 데이터.
         */
        initForm: function(obj: Record<string, any> = {}): void {
            /* show modal */
            cF.handlebars.template(obj, "jrnl_day_reg", "show");

            /* jquery validation */
            cF.validate.validateForm("#jrnlDayRegForm", dF.JrnlDay.regAjax, {
                rules: {
                    jrnlDt: {
                        required: function() {
                            return !$("#jrnlDayRegForm #dtUnknownYn").prop(":checked");
                        }
                    },
                    aprxmtDt: {
                        required: function() {
                            return $("#jrnlDayRegForm #dtUnknownYn").prop(":checked");
                        }
                    },
                },
                ignore: undefined
            });
            // 체크박스 상태 변경시 필드 재검증
            $("#dtUnknownYn").change(function(): void {
                $("#jrnlDt").valid();
                $("#aprxmtDt").valid();
            });
            // 일자 datepicker 날짜 검색 init : 현재 조회중인 yyyy-MM으로 처리
            cF.datepicker.singleDatePicker("#jrnlDt", "yyyy-MM-DD", obj.jrnlDt);
            // 날짜미상 datepicker 날짜 검색 init
            cF.datepicker.singleDatePicker("#aprxmtDt", "yyyy-MM-DD", obj.aprxmtDt);
            // 날짜미상 checkbox init
            cF.util.chckboxLabel("dtUnknownYn", "날짜미상//날짜미상", "blue//gray", function() {
                $("#jrnlDayRegForm #jrnlDtDiv").addClass("d-none");
                $("#jrnlDayRegForm #aprxmtDtDiv").removeClass("d-none");
                $("#jrnlDayRegForm #aprxmtDt").val($("#jrnlDayRegForm #jrnlDt").val());
            }, function() {
                $("#jrnlDayRegForm #jrnlDtDiv").removeClass("d-none");
                $("#jrnlDayRegForm #aprxmtDtDiv").addClass("d-none");
                $("#jrnlDayRegForm #jrnlDt").val($("#jrnlDayRegForm #aprxmtDt").val());
            });
            /* tagify */
            // cF.tagify.initWithCtgr("#jrnlDayRegForm #tagListStr", TagCtgrMap.jrnlDay);
        },

        /**
         * 년도-월 목록 조회 (Ajax)
         */
        yyMnthListAjax: function(): void {
            const mnth = $("#jrnl_aside #mnth").val();
            if (cF.util.isEmpty(mnth)) return;

            const url: string = Url.JRNL_DAY_LIST_AJAX;
            const ajaxData: Record<string, any> = { "yy": $("#jrnl_aside #yy").val(), "mnth": mnth };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltList } = res;
                // 정렬 처리
                const sortStr = $("#jrnl_aside #sort").val();
                if (sortStr === "ASC") {
                    $("#jrnl_aside #sortIcon").removeClass("bi-sort-numeric-up-alt").addClass("bi-sort-numeric-down");
                } else {
                    $("#jrnl_aside #sortIcon").removeClass("bi-sort-numeric-down").addClass("bi-sort-numeric-up-alt");
                    if (cF.util.isNotEmpty(rsltList)) rsltList.reverse();
                }
                $("#jrnl_dream_list_div").empty();
                cF.util.closeModal();
                cF.handlebars.template(rsltList, "jrnl_day_list");
            }, "block");
        },

        /**
         * 등록 모달 호출
         */
        regModal: function(): void {
            const obj: Record<string, any> = { "jrnlDt": dF.JrnlDay.validDt() };
            /* initialize form. */
            dF.JrnlDay.initForm(obj);
        },

        /**
         * 사이드바 기준으로 등록 모달 날짜 계산:: 메소드 분리
         */
        validDt: function(): string {
            const year: number = parseInt($("#jrnl_aside #yy").val() as string, 10);
            let month: number = parseInt($("#jrnl_aside #mnth").val() as string, 10);
            if (month === 99) month = 1;
            let day: number = parseInt(cF.date.getCurrDayStr(2), 10);

            // 만약 day가 해당 월의 마지막 날을 초과하면 마지막 날로 설정
            const lastDay: number = new Date(year, month, 0).getDate();
            if (day > lastDay) day = lastDay;

            // 결과를 yyyy-mm-dd 형식으로 반환
            const yyyyStr: string = year.toString();
            const mmStr: string = month.toString().padStart(2, '0'); // 월을 두 자리로 포맷팅
            const ddStr: string = day.toString().padStart(2, '0');   // 일을 두 자리로 포맷팅

            return yyyyStr + '-' + mmStr + '-' + ddStr;
        },

        /**
         * 아이콘 새로고침
         */
        refreshIcon: function(): void {
            const $iconClassElmt = $("#jrnlDayRegForm #weather");
            if (!$iconClassElmt.length) return;

            // val() 메서드는 string | null을 반환하므로, null 체크 필요
            const iconVal = $iconClassElmt.val() as string;
            if (iconVal !== null) {
                const weatherIconDiv = document.querySelector("#jrnlDayRegForm #weather_icon_div") as HTMLElement | null;
                if (weatherIconDiv) {
                    weatherIconDiv.innerHTML = iconVal;
                }
            }
        },

        /**
         * form submit
         */
        submit: function(): void {
            $("#jrnlDayRegForm").submit();
        },

        /**
         * 등록 (Ajax)
         */
        regAjax: function(): void {
            const postNoElmt: HTMLInputElement = document.querySelector("#jrnlDayRegForm [name='postNo']") as HTMLInputElement;
            const isReg: boolean = postNoElmt?.value === "";

            Swal.fire({
                text: Message.get(isReg ? "view.cnfm.reg" : "view.cnfm.mdf"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = isReg ? Url.JRNL_DAY_REG_AJAX : Url.JRNL_DAY_MDF_AJAX;
                const ajaxData: FormData = new FormData(document.getElementById("jrnlDayRegForm") as HTMLFormElement);
                cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            dF.JrnlDay.yyMnthListAjax();
                            // 태그 조회
                            dF.JrnlDayTag.listAjax();
                        });
                }, "block");
            });
        },

        /**
         * 등록 모달 호출
         * @param {string|number} postNo 글 번호.
         */
        mdfModal: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            const url: string = Url.JRNL_DAY_DTL_AJAX;
            const ajaxData: Record<string, any> = { "postNo" : postNo };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltObj } = res;
                /* initialize form. */
                dF.JrnlDay.initForm(rsltObj);
            });
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

                const url: string = Url.JRNL_DAY_DEL_AJAX;
                const ajaxData: Record<string, any> = { "postNo": postNo };
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            dF.JrnlDay.yyMnthListAjax();
                            dF.JrnlDayTag.listAjax();
                        });
                }, "block");
            });
        },
    }
})();