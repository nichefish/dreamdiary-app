import { UserDetailActions } from "../types.js";

export default function bindUserDetailEventBridge(actions: UserDetailActions): void {
    const win = window as Window & { dF?: Record<string, any> };
    if (typeof win.dF === "undefined") win.dF = {};
    win.dF.User = {
        init: function(): void {},
        pwResetAjax: actions.pwResetAjax,
        modifyForm: actions.modifyForm,
        deleteAjax: actions.deleteAjax,
        list: actions.list,
    };
}
