import { BoardAdminActions } from "../types.js";

type BridgeParams = {
    actions: BoardAdminActions;
};

export default function bindBoardAdminEventBridge(params: BridgeParams): void {
    const { actions } = params;
    window.addEventListener("board-admin:request-open-board-regist", (): void => {
        actions.openBoardRegist();
    });
    window.addEventListener("board-admin:request-submit-board-regist", (): void => {
        actions.submitBoardRegist();
    });
}
