import { UserListActions } from "../types.js";

export default function bindUserListEventBridge(actions: UserListActions): void {
    const win = window as Window & { dF?: Record<string, any> };
    if (typeof win.dF === "undefined") win.dF = {};
    win.dF.User = {
        init: function(): void {},
        search: actions.search,
        xlsxDownload: actions.xlsxDownload,
        registForm: actions.registForm,
        detail: actions.detail,
    };
}
