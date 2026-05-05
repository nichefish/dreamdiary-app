import { UserListActions } from "../types.js";

export default function bindUserListEventBridge(actions: UserListActions): void {
    if (typeof window.dF === "undefined") window.dF = {};
    window.dF.User = {
        init: function(): void {},
        search: actions.search,
        xlsxDownload: actions.xlsxDownload,
        registForm: actions.registForm,
        detail: actions.detail,
    };
}
