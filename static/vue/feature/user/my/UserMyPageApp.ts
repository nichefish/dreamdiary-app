/**
 * UserMyPageApp.ts
 * 내 정보 화면 Vue 앱
 *
 * @author nichefish
 */
import UserMyPagePanel from "./components/UserMyPagePanel.js";
import UserMyPasswordChangeModal from "./components/UserMyPasswordChangeModal.js";
import UserMyPasswordChangeFooter from "./components/UserMyPasswordChangeFooter.js";
import userMyDataService from "./services/userMyDataService.js";
import createUserMyActions from "./services/userMyActionService.js";

const pageData = Vue.reactive(userMyDataService.parsePageData());
const actions = createUserMyActions(pageData.labels);

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

const UserMyPageRootApp = {
    name: "UserMyPageRootApp",
    components: {
        UserMyPagePanel,
        UserMyPasswordChangeModal,
        UserMyPasswordChangeFooter,
    },
    data() {
        return { pageData };
    },
    methods: {
        onUploadProfileImage(): void { actions.uploadProflImg(); },
        onRemoveProfileImage(): void { actions.removeProflImg(); },
        onOpenPasswordChange(): void { actions.openPwChangeModal(); },
        onSubmitPasswordChange(): void { actions.submitPwChange(); },
    },
    template: `
    <teleport to="#user_my_page_div">
        <UserMyPagePanel
            :user="pageData.user"
            :vacation="pageData.vacation"
            :labels="pageData.labels"
            @upload-profile-image="onUploadProfileImage"
            @remove-profile-image="onRemoveProfileImage"
            @open-password-change="onOpenPasswordChange"
        />
    </teleport>
    <teleport to="#user_my_pw_chg_div">
        <UserMyPasswordChangeModal
            :error-msg="pageData.errorMsg"
            :labels="pageData.labels"
            @submit="onSubmitPasswordChange"
        />
    </teleport>
    <teleport to="#user_my_pw_chg_footer_div">
        <UserMyPasswordChangeFooter
            :labels="pageData.labels"
            @submit="onSubmitPasswordChange"
        />
    </teleport>
    `,
};

runWhenDomReady(function(): void {
    if (!document.getElementById("user_my_app")
        || !document.getElementById("user_my_page_div")
        || !document.getElementById("user_my_pw_chg_div")
        || !document.getElementById("user_my_pw_chg_footer_div")) {
        console.error("[UserMyPageApp] Vue mount root not found.");
        return;
    }

    Vue.createApp(UserMyPageRootApp).mount("#user_my_app");
    actions.initPwChangeForm();
});
