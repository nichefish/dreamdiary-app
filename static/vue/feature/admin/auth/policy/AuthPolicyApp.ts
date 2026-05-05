/**
 * 인증 정책 관리 화면 Vue 앱 (싱글톤 폼)
 *
 * @author nichefish
 */
import AuthPolicyForm from "./components/AuthPolicyForm.js";
import AuthPolicyFooter from "./components/AuthPolicyFooter.js";
import authPolicyDataService from "./services/authPolicyDataService.js";
import createAuthPolicyActions from "./services/authPolicyActionService.js";
import { AuthPolicyPageState } from "./types.js";
import { createScopedI18n } from "../../../../global/services/scopedI18nService.js";

const state = Vue.reactive({
    form: {
        id: "",
        inactiveLockDays: "",
        loginAttemptLimit: "",
        loginAttemptWindowMinutes: "",
        accountLockDurationMinutes: "",
        passwordChangeCycleDays: "",
        passwordResetTokenExpiryMinutes: "",
    },
}) as AuthPolicyPageState;
const i18n = createScopedI18n();

const actions = createAuthPolicyActions({
    getForm: (): AuthPolicyPageState["form"] => state.form,
    t(key: string): string {
        return i18n.t(key);
    },
});

function t(key: string): string {
    return i18n.t(key);
}

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

function resolveAuthPolicyPageLocale(): string {
    const w = typeof window !== "undefined" ? (window as Window & { Model?: { locale?: string } }) : undefined;
    const loc = w?.Model?.locale;
    if (loc) return loc;
    return (document.documentElement.lang || "ko").replace(/_/g, "-");
}

const AuthPolicyRoot = {
    name: "AuthPolicyRoot",
    components: { AuthPolicyForm, AuthPolicyFooter },
    data(): { state: AuthPolicyPageState } {
        return { state };
    },
    methods: {
        onSave(): void {
            actions.save();
        },
    },
    template: `
    <AuthPolicyForm :form="state.form" />
    <AuthPolicyFooter @save="onSave" />
    `,
};

runWhenDomReady(async function(): Promise<void> {
    await i18n.load(resolveAuthPolicyPageLocale());
    Object.assign(state.form, authPolicyDataService.parseInitialForm());

    if (!document.getElementById("auth_policy_app")) {
        console.error("[AuthPolicyApp] mount root #auth_policy_app not found.");
        return;
    }

    const app = Vue.createApp(AuthPolicyRoot);
    app.config.globalProperties.$t = (key: string): string => t(key);
    app.mount("#auth_policy_app");
});
