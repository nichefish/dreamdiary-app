import { OAuth2Actions } from "../types.js";

function openOAuthPopup(url: string): void {
    const popup = cF.ui.openPopup(url, "Authorization", "width=540,height=720,top=0,left=270");
    if (popup) popup.focus();
}

export default function createOAuth2Actions(): OAuth2Actions {
    return {
        init(): void {
            console.log("'dF.oauth2' module initialized.");
        },

        popupGoogle(): void {
            openOAuthPopup(Url.OAUTH2_GOOGLE);
        },

        popupNaver(): void {
            openOAuthPopup(Url.OAUTH2_NAVER);
        },

        getHashParam(): Record<string, string> {
            const hash: string = window.location.hash.substring(1);
            const params: URLSearchParams = new URLSearchParams(hash);
            const paramsObj: Record<string, string> = {};
            params.forEach((value: string, key: string): void => {
                paramsObj[key] = value;
            });
            return paramsObj;
        },

        handleOAuth2Redirect(): void {
            const hashParam: Record<string, string> = this.getHashParam();
            if (Object.keys(hashParam).length > 0) {
                cF.ajax.post("/login/oauth2/code/naver", { ...hashParam }, function(res: AjaxResponse): void {
                    if (res.rslt) console.log("Token successfully sent to server.");
                });
                return;
            }
            console.error("No access_token found in hash.");
        },

        main(): void {
            const mainUrl = (Url as { MAIN?: string }).MAIN || "/";
            if (window.opener && !window.opener.closed) {
                window.opener.location.replace(mainUrl);
                window.close();
                return;
            }
            if (typeof cF?.ui?.blockUIReplace === "function") {
                cF.ui.blockUIReplace(mainUrl);
                return;
            }
            window.location.href = mainUrl;
        },
    };
}
