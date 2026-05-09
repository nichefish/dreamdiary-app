/**
 * LoginFormApp.ts
 * 로그인 화면 Vue 앱
 *
 * @author nichefish
 */
import LoginPanel from "./components/LoginPanel.js";
import PasswordChangeModal from "./components/PasswordChangeModal.js";
import i18nCatalogService from "../../global/services/i18nCatalogService.js";
import loginAuthService from "./services/loginAuthService.js";
import loginValidationService, {
    LoginErrors,
    PasswordChangeErrors,
} from "./services/loginValidationService.js";
// 변경(D): `Message.get` 직호출을 `resolveMessage` 헬퍼로 위임.
import { resolveMessage } from "../../common/messageHelper.js";

type I18nState = {
    messages: Record<string, string>;
};

const i18nState: I18nState = {
    messages: {},
};

const LoginFormApp = {
    name: "LoginFormApp",
    components: {
        LoginPanel,
        PasswordChangeModal,
    },
    data() {
        return {
            login: {
                username: Model.username || "",
                password: "",
                rememberMe: false,
                passwordDisabled: false,
            },
            pwChange: {
                currPw: "",
                newPw: "",
                newPwCf: "",
            },
            isReady: false,
            isPasswordChangeOpen: false,
            errors: {
                login: {
                    username: "",
                    password: "",
                } as LoginErrors,
                pwChange: {
                    currPw: "",
                    newPw: "",
                    newPwCf: "",
                } as PasswordChangeErrors,
            },
        };
    },
    computed: {
        errorMsgLines(): string[] {
            return this.splitErrorMsg(Model.errorMsg || "");
        },
        viewText(): Record<string, string> {
            return {
                domain: Model.domain || "",
                rememberMeParam: Model.rememberMeParam || "",
            };
        },
    },
    watch: {
        isPasswordChangeOpen(isOpen: boolean): void {
            this.$nextTick((): void => {
                const modal = this.$refs.passwordChangeModal;
                if (!modal) return;
                isOpen ? modal.show() : modal.hide();
            });
        },
    },
    methods: {
        runInitialFlow(): void {
            if (Model.isDupIdLogin) this.confirmDuplicateLogin();
            if (Model.isCredentialExpired || Model.needsPasswordReset) this.openPasswordChangeModal();
        },
        splitErrorMsg(errorMsg: string): string[] {
            if (cF.util.isEmpty(errorMsg)) return [];
            return errorMsg
                .split(/(?:&lt;br\s*\/?&gt;|<br\s*\/?>)/gi)
                .filter((line: string): boolean => !cF.util.isEmpty(line));
        },
        resetLoginErrors(): void {
            this.errors.login = loginValidationService.emptyLoginErrors();
        },
        resetPasswordChangeErrors(): void {
            this.errors.pwChange = loginValidationService.emptyPasswordChangeErrors();
        },
        validateLogin(): boolean {
            this.errors.login = loginValidationService.validateLogin(this.login, this.t);
            return !loginValidationService.hasLoginErrors(this.errors.login);
        },
        validatePasswordChangeForm(): boolean {
            this.errors.pwChange = loginValidationService.validatePasswordChange(this.pwChange, cF.regex.pw, this.t);
            return !loginValidationService.hasPasswordChangeErrors(this.errors.pwChange);
        },
        submitLogin(): void {
            if (!this.validateLogin()) return;
            this.$refs.loginPanel.submit(Url.API_AUTH_LGN_PROC);
        },
        confirmDuplicateLogin(): void {
            Swal.fire({
                title: resolveMessage("view.auth.dupLogin"),
                text: resolveMessage("view.cnfm.dupLogin"),
                icon: "warning",
                showCancelButton: true,
                confirmButtonText: this.t("txt.auth.login.sign-in"),
                cancelButtonText: this.t("txt.auth.login.cancel"),
            }).then((result: SwalResult): void => {
                if (result.value) {
                    this.login.username = Model.username;
                    this.login.passwordDisabled = true;
                    this.$nextTick(this.submitLogin);
                    return;
                }

                loginAuthService.expireSession(function(): void {
                    cF.ui.blockUIReplace(Url.APP_AUTH_LGN_FORM);
                });
            });
        },
        t(key: string): string {
            return i18nCatalogService.t(i18nState.messages, key);
        },
        openUserSignup(): void {
            loginAuthService.redirectUserSignup();
        },
        popupGoogle(): void {
            loginAuthService.openOAuthPopup(Url.OAUTH2_GOOGLE);
        },
        popupNaver(): void {
            loginAuthService.openOAuthPopup(Url.OAUTH2_NAVER);
        },
        openPasswordChangeModal(): void {
            this.isPasswordChangeOpen = true;
        },
        closePasswordChangeModal(): void {
            this.isPasswordChangeOpen = false;
        },
        resetPasswordChangeForm(): void {
            this.pwChange.currPw = "";
            this.pwChange.newPw = "";
            this.pwChange.newPwCf = "";
            this.resetPasswordChangeErrors();
        },
        validatePasswordChange(): void {
            if (!this.validatePasswordChangeForm()) return;
            this.submitPasswordChange();
        },
        submitPasswordChange(): void {
            const ajaxData: Record<string, any> = {
                username: this.login.username,
                currPw: this.pwChange.currPw,
                newPw: this.pwChange.newPw,
            };

            loginAuthService.changePassword(ajaxData, (res: AjaxResponse): void => {
                Swal.fire({ text: res.message }).then((): void => {
                    this.resetPasswordChangeForm();
                    if (res.rslt) cF.ui.blockUIReplace(Url.APP_AUTH_LGN_FORM);
                });
            });
        },
    },
    async mounted(): Promise<void> {
        i18nState.messages = await i18nCatalogService.load(Model.locale);
        this.isReady = true;
        this.$nextTick((): void => this.runInitialFlow());
    },
    template: `
        <template v-if="isReady">
            <LoginPanel
                ref="loginPanel"
                v-model:username="login.username"
                v-model:password="login.password"
                v-model:rememberMe="login.rememberMe"
                :domain="viewText.domain"
                :rememberMeParam="viewText.rememberMeParam"
                :passwordDisabled="login.passwordDisabled"
                :errors="errors.login"
                :errorMsgLines="errorMsgLines"
                @submit-login="submitLogin"
                @popup-google="popupGoogle"
                @popup-naver="popupNaver"
                @open-user-signup="openUserSignup"
            />
            <PasswordChangeModal
                ref="passwordChangeModal"
                v-model:currPw="pwChange.currPw"
                v-model:newPw="pwChange.newPw"
                v-model:newPwCf="pwChange.newPwCf"
                :username="login.username"
                :errors="errors.pwChange"
                :errorMsgLines="errorMsgLines"
                @submit-password-change="validatePasswordChange"
                @close-password-change="closePasswordChangeModal"
            />
        </template>
    `,
};

document.addEventListener("DOMContentLoaded", function(): void {
    const app = Vue.createApp(LoginFormApp);
    app.config.globalProperties.$t = function(key: string): string {
        return i18nCatalogService.t(i18nState.messages, key);
    };
    app.mount("#login-vue-app");
});
