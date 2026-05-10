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
import { createScopedI18n } from "../../../global/services/scopedI18nService.js";

const pageData = Vue.reactive(userMyDataService.parsePageData());
const i18n = createScopedI18n();
const actions = createUserMyActions((key: string): string => i18n.t(key));

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
        t(key: string): string { return i18n.t(key); },
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
            :t="t"
            @upload-profile-image="onUploadProfileImage"
            @remove-profile-image="onRemoveProfileImage"
            @open-password-change="onOpenPasswordChange"
        />
    </teleport>
    <teleport to="#user_my_pw_chg_div">
        <UserMyPasswordChangeModal
            :error-msg="pageData.errorMsg"
            :t="t"
            @submit="onSubmitPasswordChange"
        />
    </teleport>
    <teleport to="#user_my_pw_chg_footer_div">
        <UserMyPasswordChangeFooter
            :t="t"
            @submit="onSubmitPasswordChange"
        />
    </teleport>
    `,
};

function resolvePageLocale(): string {
    const w = typeof window !== "undefined" ? (window as Window & { Model?: { locale?: string } }) : undefined;
    const loc = w?.Model?.locale;
    if (loc) return loc;
    return (document.documentElement.lang || "ko").replace(/_/g, "-");
}

runWhenDomReady(async function(): Promise<void> {
    await i18n.load(resolvePageLocale());
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
