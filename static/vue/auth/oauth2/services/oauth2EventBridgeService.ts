import { OAuth2Actions } from "../types.js";

declare global {
    interface Window {
        dF?: Record<string, any>;
        Page?: Record<string, any>;
    }
}

export default function bindOAuth2EventBridge(actions: OAuth2Actions): void {
    const win = window as Window & { dF?: Record<string, any>; Page?: Record<string, any> };
    if (typeof win.dF === "undefined") win.dF = {};
    win.dF.oauth2 = {
        initialized: false,
        init(): void {
            if (this.initialized) return;
            this.initialized = true;
            actions.init();
        },
        popupGoogle: actions.popupGoogle,
        popupNaver: actions.popupNaver,
        getHashParam: actions.getHashParam.bind(actions),
        handleOAuth2Redirect: actions.handleOAuth2Redirect.bind(actions),
    };

    win.Page = {
        ...(win.Page || {}),
        main: actions.main,
    };
}
