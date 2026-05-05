/**
 * loginAuthService.ts
 * 로그인 화면 외부 동작 모음
 *
 * @author nichefish
 */
export default {
    expireSession(callback: Function): void {
        cF.$ajax.post(Url.API_AUTH_EXPIRE_SESSION, null, callback);
    },
    /**
     * 계정 신청(가입 요청) 화면으로 이동한다.
     * 변경 전: 메서드명이 redirectUserRequest 였음(request/reqst 혼선).
     * 변경 후: USER_SIGNUP_PAGE 와 명칭을 맞춘다.
     */
    redirectUserSignup(): void {
        cF.ui.blockUIReplace(Url.USER_SIGNUP_PAGE);
    },
    openOAuthPopup(url: string): void {
        const popup = cF.ui.openPopup(url, "Authorization", "width=540,height=720,top=0,left=270");
        if (popup) popup.focus();
    },
    changePassword(data: Record<string, any>, callback: Function): void {
        cF.$ajax.post(Url.API_AUTH_LGN_PW_CHG, data, callback);
    },
};
