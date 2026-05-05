/**
 * UserDetailApp.ts
 * 사용자 상세 화면 Vue 앱
 *
 * @author nichefish
 */
import UserDetailPanel from "./components/UserDetailPanel.js";
import UserDetailFooter from "./components/UserDetailFooter.js";
import userDetailDataService from "./services/userDetailDataService.js";
import createUserDetailActions from "./services/userDetailActionService.js";
import bindUserDetailEventBridge from "./services/userDetailEventBridgeService.js";
import { UserDetailState } from "./types.js";

const state = Vue.reactive({
    detail: userDetailDataService.parseDetailFromPageData(),
    labels: {
        noProfile: "",
        retired: "",
        activeEmployee: "",
        probation: "",
        locked: "잠김",
        use: "사용",
        emptyList: "",
        totalPrefix: "Total",
        unuse: "미사용",
        modifyTooltip: "",
        deleteTooltip: "",
        listTooltip: "",
        passwordResetTooltip: "",
        ...userDetailDataService.parseLabels(),
    },
}) as UserDetailState;

const actions = createUserDetailActions();

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

const UserDetailRootApp = {
    name: "UserDetailRootApp",
    components: {
        UserDetailPanel,
        UserDetailFooter,
    },
    data(): { state: UserDetailState } {
        return { state };
    },
    methods: {
        onPasswordReset(): void { actions.pwResetAjax(); },
        onModify(): void { actions.mdfForm(); },
        onDelete(): void { actions.delAjax(); },
        onList(): void { actions.list(); },
    },
    template: `
    <teleport to="#user_detail_div">
        <UserDetailPanel
            :detail="state.detail"
            :labels="state.labels"
            @password-reset="onPasswordReset"
        />
    </teleport>
    <teleport to="#user_detail_footer_div">
        <UserDetailFooter
            :labels="state.labels"
            @modify="onModify"
            @delete="onDelete"
            @list="onList"
        />
    </teleport>
    `,
};

runWhenDomReady(function(): void {
    if (!document.getElementById("user_detail_app")
        || !document.getElementById("user_detail_div")
        || !document.getElementById("user_detail_footer_div")) {
        console.error("[UserDetailApp] Vue mount root not found.");
        return;
    }

    Vue.createApp(UserDetailRootApp).mount("#user_detail_app");
    bindUserDetailEventBridge(actions);
});
