import { UserFormActions } from "../types.js";

export default function bindUserFormEventBridge(actions: UserFormActions): void {
    if (typeof window.dF === "undefined") window.dF = {};
    window.dF.User = {
        init: function(): void {},
        isMdf: actions.isMdf,
        initForm: actions.initForm,
        submitHandler: actions.submitHandler,
        idDupChckAjax: actions.idDupChckAjax,
        emailDupChckAjax: actions.emailDupChckAjax,
        regAjax: actions.regAjax,
        list: actions.list,
    };
}
