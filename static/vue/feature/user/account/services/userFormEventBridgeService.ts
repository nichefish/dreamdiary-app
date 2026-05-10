import { UserFormActions } from "../types.js";

export default function bindUserFormEventBridge(actions: UserFormActions): void {
    const win = window as Window & { dF?: Record<string, any> };
    if (typeof win.dF === "undefined") win.dF = {};
    win.dF.User = {
        init: function(): void {},
        isMdf: actions.isMdf,
        isModify: actions.isMdf,
        initForm: actions.initForm.bind(actions),
        submitHandler: actions.submitHandler.bind(actions),
        idDupChckAjax: actions.idDupChckAjax,
        emailDupChckAjax: actions.emailDupChckAjax,
        regAjax: actions.regAjax.bind(actions),
        registAjax: actions.regAjax.bind(actions),
        list: actions.list.bind(actions),
    };
}
