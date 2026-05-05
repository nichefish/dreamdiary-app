/**
 * UserFormApp.ts
 * 사용자 등록/수정 화면 Vue 앱
 *
 * @author nichefish
 */
import UserFormPanel from "./components/UserFormPanel.js";
import userFormDataService from "./services/userFormDataService.js";
import createUserFormActions from "./services/userFormActionService.js";
import bindUserFormEventBridge from "./services/userFormEventBridgeService.js";
import { UserFormState } from "./types.js";
import { initEmplymFormPlugins, initProfileBirthDatepicker } from "../shared/profileEmplymShared.js";
import { createScopedI18n } from "../../../global/services/scopedI18nService.js";

const state = Vue.reactive({
    form: userFormDataService.parseForm(),
    roles: userFormDataService.parseRoles(),
    cmpyOptions: userFormDataService.parseCodeOptions("user_form_cmpy_data"),
    teamOptions: userFormDataService.parseCodeOptions("user_form_team_data"),
    emplymOptions: userFormDataService.parseCodeOptions("user_form_emplym_data"),
    rankOptions: userFormDataService.parseCodeOptions("user_form_rank_data"),
}) as UserFormState;
const i18n = createScopedI18n();

const actions = createUserFormActions();

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

function resolvePageLocale(): string {
    const w = typeof window !== "undefined" ? (window as Window & { Model?: { locale?: string } }) : undefined;
    const loc = w?.Model?.locale;
    if (loc) return loc;
    return (document.documentElement.lang || "ko").replace(/_/g, "-");
}

const UserFormRootApp = {
    name: "UserFormRootApp",
    components: {
        UserFormPanel,
    },
    data(): { state: UserFormState } {
        return { state };
    },
    methods: {
        t(key: string): string { return i18n.t(key); },
        onUsernameDupCheck(): void { actions.idDupChckAjax(); },
        onEmailDupCheck(): void { actions.emailDupChckAjax(); },
        onSubmit(): void { cF.form.submit("#userRegForm"); },
        onList(): void { actions.list(); },
        initProfilePlugins(): void {
            requestAnimationFrame(function(): void {
                cF.ui.chckboxLabel("#lunarYn", "음력//양력", "blue//gray");
                initProfileBirthDatepicker("#brthdy");
            });
        },
        initEmplymPlugins(): void {
            requestAnimationFrame(function(): void {
                initEmplymFormPlugins({
                    phoneSelector: "#emplymPhoneNumber",
                    emailDomainSelectSelector: "#emplymEmailDomainSelect",
                    emailDomainInputSelector: "#emplymEmailDomain",
                    joinDateSelector: "#ecnyDt",
                    retireDateSelector: "#retireDt",
                    bindNamespace: "userFormVue",
                });
                $("#rankCd").off("change.userFormVue").on("change.userFormVue", function(): void {
                    if ($("#rankCd").val() === "STAFF") {
                        $("#apntcYnDiv").show();
                    } else {
                        $("#apntcYnDiv").hide();
                    }
                }).trigger("change");
                cF.ui.chckboxLabel("#apntcYn", "수습//해당없음", "blue//gray");
                cF.ui.chckboxLabel("#retireYn", "퇴사//해당없음", "red//gray", function(): void {$(".retireDtDiv").show();}, function(): void {$(".retireDtDiv").hide();});
            });
        },
        onToggleProfile(): void {
            state.form.hasProfile = !state.form.hasProfile;
            if (state.form.hasProfile) this.initProfilePlugins();
        },
        onToggleEmplym(): void {
            state.form.hasEmplym = !state.form.hasEmplym;
            if (state.form.hasEmplym) this.initEmplymPlugins();
        },
    },
    template: `
    <UserFormPanel
        :form="state.form"
        :t="t"
        :roles="state.roles"
        :cmpy-options="state.cmpyOptions"
        :team-options="state.teamOptions"
        :emplym-options="state.emplymOptions"
        :rank-options="state.rankOptions"
        @username-dup-check="onUsernameDupCheck"
        @email-dup-check="onEmailDupCheck"
        @toggle-profile="onToggleProfile"
        @toggle-emplym="onToggleEmplym"
        @submit="onSubmit"
        @list="onList"
    />
    `,
};

runWhenDomReady(async function(): Promise<void> {
    await i18n.load(resolvePageLocale());
    if (!document.getElementById("user_form_app")) {
        console.error("[UserFormApp] Vue mount root not found.");
        return;
    }

    bindUserFormEventBridge(actions);
    const vm = Vue.createApp(UserFormRootApp).mount("#user_form_app") as any;

    requestAnimationFrame(function(): void {
        dF.User.initForm();
        // 프로필 정보 / 인사정보 창 활성화
        if (state.form.hasProfile) vm.initProfilePlugins();
        if (state.form.hasEmplym) vm.initEmplymPlugins();
    });
});
