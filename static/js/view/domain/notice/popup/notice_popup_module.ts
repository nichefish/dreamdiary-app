/**
 * notice_popup_module.ts
 * 공지사항 팝업 관련
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.NoticePopup = (function(): Module {
    return {
        /**
         * NoticePopup 객체 초기화
         */
        init: function(): void {
            /** 로그인 공지 목록 조회 */
            const isAuthenticated = cF.util.isNotEmpty(AuthInfo.userId)
            if (isAuthenticated) dF.NoticePopup.showNoticePopupAjax();

            console.log("'NoticePopup' module initialized.");
        },

        /**
         * 팝업 목록 조회 및 처리
         */
        showNoticePopupAjax(): void {
            // const url: string = Url.NOTICE_POPUP_LIST_AJAX;
            // cF.ajax.get(url, null, function(res) {
            //     if (cF.util.isNotEmpty(res.rsltList)) {
            //         let rsltList = res.rsltList;
            //         dF.NoticePopup.popupList = rsltList;
            //         dF.NoticePopup.popupSize = rsltList.length;
            //         dF.NoticePopup.popupMaxIdx = rsltList.length - 1;
            //         dF.NoticePopup.popupIdx = 0;
            //         dF.NoticePopup.showNoticePopup();
            //     }
            // });
        },

        /**
         * 팝업 열기 (쿠키 있으면 열지 않음, 순회하면서 체크)
         */
        showNoticePopup: function(): void {
            if (cF.util.isEmpty(dF.NoticePopup.popupList)) return;
            if (dF.NoticePopup.popupIdx > dF.NoticePopup.popupMaxIdx) return;

            const hasNoticeCookie = dF.NoticePopup.hasNoticeCookie();
            if (hasNoticeCookie) {
                dF.NoticePopup.popupIdx++;
                return dF.NoticePopup.showNoticePopup();
            }
            const url: string = Url.NOTICE_DTL_AJAX;
            const obj: Record<string, any> = dF.NoticePopup.popupList[dF.NoticePopup.popupIdx];
            const ajaxData: Record<string, any> = { "postNo": obj.postNo };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                cF.handlebars.template(res.rsltObj, "notice_popup", "show");
                dF.NoticePopup.popupIdx++;
            });
        },

        /**
         * 팝업 쿠키 존재여부 체크
         */
        hasNoticeCookie: function(): boolean {
            const obj: Record<string, any> = dF.NoticePopup.popupList[dF.NoticePopup.popupIdx];
            if (obj === undefined) return false;

            const popupCookie: string = $.cookie("notice_popup__postno_" + obj.postNo);
            return (popupCookie !== undefined);
        },

        /**
         * 팝업 닫기 버튼
         */
        closeBtnFunc: function(): void {
            dF.NoticePopup.setNoticePopupCookie();          // 쿠키 설정
            if (dF.NoticePopup.popupIdx < (dF.NoticePopup.popupSize)) {
                return dF.NoticePopup.showNoticePopup();
            } else {
                dF.NoticePopup.closeNoticePopup();
            }
        },

        /**
         * 공지사항 팝업 쿠키 세팅
         */
        setNoticePopupCookie: function(): void {
            // 팝업안보기 옵션 선택 상태일 경우 브라우저 쿠키 추가
            const isChecked: boolean = $("#noticePopupCookieChk").prop("checked");
            if (!isChecked) return;

            const postNo = $("#noticePopupPostNo").val();
            const cookieNm: string = "notice_popup__postno_" + postNo;
            // 선택지에 따라 시간 별도 세팅
            const selectedTime = $("#noticePopupCookieTime").val();
            const cookieOptions = {} as any;
            if (selectedTime === "midnight") cookieOptions.expires = cF.date.getEndOfDay(new Date());
            else if (selectedTime === "forever") cookieOptions.expires = cF.date.getCurrDateAddDay(36135);
            else cookieOptions.maxAge = selectedTime;       // Do note that maxAge is in milliseconds!!! (어처구니...)
            cookieOptions.path = "/";
            $.cookie(cookieNm, true, cookieOptions);
        },

        /**
         * 팝업 닫기
         */
        closeNoticePopup: function(): void {
            $("#notice_popup_modal").modal("hide");
        },
    }
})();