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
    redirectUserRequest(): void {
        cF.ui.blockUIReplace(Url.USER_REQST_REG_FORM);
    },
    openOAuthPopup(url: string): void {
        const popup = cF.ui.openPopup(url, "Authorization", "width=540,height=720,top=0,left=270");
        if (popup) popup.focus();
    },
    changePassword(data: Record<string, any>, callback: Function): void {
        cF.$ajax.post(Url.API_AUTH_LGN_PW_CHG, data, callback);
    },
};
