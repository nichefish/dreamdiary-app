import { UserDetailActions } from "../types.js";

export default function bindUserDetailEventBridge(actions: UserDetailActions): void {
    if (typeof window.dF === "undefined") window.dF = {};
    window.dF.User = {
        init: function(): void {},
        pwResetAjax: actions.pwResetAjax,
        mdfForm: actions.mdfForm,
        delAjax: actions.delAjax,
        list: actions.list,
    };
}
