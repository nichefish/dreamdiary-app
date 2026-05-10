/**
 * LoginPanel.ts
 * 로그인 폼 컴포넌트
 *
 * @author nichefish
 */
export default {
    name: "LoginPanel",
    props: {
        domain: { type: String, default: "" },
        errors: { type: Object, default: () => ({}) },
        errorMsgLines: { type: Array, default: () => [] },
        rememberMeParam: { type: String, required: true },
        passwordDisabled: { type: Boolean, default: false },
        username: { type: String, default: "" },
        password: { type: String, default: "" },
        rememberMe: { type: Boolean, default: false },
    },
    emits: [
        "submit-login",
        "popup-google",
        "popup-naver",
        "open-user-signup",
        "update:username",
        "update:password",
        "update:rememberMe",
    ],
    computed: {
        usernameModel: {
            get(): string {
                return this.username || "";
            },
            set(value: string): void {
                this.$emit("update:username", value);
            },
        },
        passwordModel: {
            get(): string {
                return this.password || "";
            },
            set(value: string): void {
                this.$emit("update:password", value);
            },
        },
        rememberMeModel: {
            get(): boolean {
                return !!this.rememberMe;
            },
            set(value: boolean): void {
                this.$emit("update:rememberMe", value);
            },
        },
    },
    methods: {
        submit(actionUrl: string): void {
            const form = this.$refs.loginForm as HTMLFormElement;
            if (!form) return;

            cF.ui.blockUIRequest();
            cF.ui.closeModal();
            if (actionUrl) form.action = actionUrl;
            form.submit();
        },
    },
    template: `
        <div class="w-lg-500px rounded mt-20 p-10 p-lg-15 mx-auto my-auto">
            <div class="aside-logo flex-column-auto pb-5 mb-20">
                <!-- -->
            </div>
            <div class="d-flex justify-content-center fs-4 mb-15 text-secondary fw-bold ls-1">
                <i class="bi bi-cloud-moon fs-2"></i>
                <span class="px-2">{{ domain }}</span>
                <i class="bi bi-stars fs-2"></i>
            </div>
            <form ref="loginForm" name="loginForm" id="loginForm" class="w-100" method="post" @submit.prevent="$emit('submit-login')">
                <div class="opacity-75">
                    <label class="form-label fs-6 text-secondary fw-bolder" for="username">{{ $t("txt.auth.login.username") }}</label>
                    <input type="text" name="username" id="username" class="form-control form-control-lg enter required"
                           data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="$t('bs.tooltip.auth.login.username')"
                           v-model.trim="usernameModel" :placeholder="$t('txt.auth.login.username-placeholder')" autocomplete="off" maxlength="20" />
                </div>
                <div class="h-15px mt-1 mb-5">
                    <span id="username_validate_span" class="text-danger">{{ errors.username }}</span>
                </div>
                <div class="opacity-75">
                    <label class="form-label text-secondary fw-bolder fs-6 mb-1" for="password">{{ $t("txt.auth.login.password") }}</label>
                    <input type="password" name="password" id="password" class="form-control form-control-lg enter required"
                           data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="$t('bs.tooltip.auth.login.password')"
                           v-model="passwordModel" :disabled="passwordDisabled" :placeholder="$t('txt.auth.login.password-placeholder')" maxlength="20" autocomplete="off" />
                </div>
                <div class="h-15px mt-1 mb-3">
                    <span id="password_validate_span" class="text-danger">{{ errors.password }}</span>
                </div>
                <div class="text-left">
                    <span id="errorMsgSpan" class="text-danger">
                        <template v-for="(line, index) in errorMsgLines" :key="index">
                            {{ line }}<br v-if="index < errorMsgLines.length - 1">
                        </template>
                    </span>
                </div>
                <div class="me-2 float-end">
                    <label class="form-check form-check-custom form-check-solid" :for="rememberMeParam"
                           data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="$t('bs.tooltip.auth.login.remember-me')">
                        <span class="form-check-label text-secondary me-2">
                            {{ $t("txt.auth.login.remember-me") }}
                        </span>
                        <input type="checkbox" class="form-check-input cursor-pointer" :id="rememberMeParam" :name="rememberMeParam" v-model="rememberMeModel">
                    </label>
                </div>
                <div class="d-flex flex-column text-center mb-4 mt-12 gap-2">
                    <button type="submit" class="btn btn-lg btn-light-primary opacity-75 w-100"
                            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="$t('bs.tooltip.auth.login.sign-in')" >
                        <span class="indicator-label">{{ $t("txt.auth.login.sign-in") }}</span>
                        <span class="indicator-progress">{{ $t("txt.auth.login.please-wait") }}
                            <span class="spinner-border spinner-border-sm align-middle ms-2"></span>
                        </span>
                    </button>
                    <div class="d-flex gap-2">
                        <button type="button" class="btn btn-lg btn-light-danger opacity-75 w-100"
                                @click="$emit('popup-google')"
                                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="$t('bs.tooltip.auth.login.google')" >
                            <span class="d-flex-center indicator-label gap-2">
                                <span><i class="bi bi-google blink"></i></span>
                                <span>{{ $t("txt.auth.login.with-google") }}</span>
                            </span>
                            <span class="indicator-progress">{{ $t("txt.auth.login.please-wait") }}
                                <span class="spinner-border spinner-border-sm align-middle ms-2"></span>
                            </span>
                        </button>
                        <button type="button" class="btn btn-lg btn-light-success opacity-75 w-100"
                                @click="$emit('popup-naver')"
                                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="$t('bs.tooltip.auth.login.naver')" >
                            <span class="d-flex-center indicator-label gap-2">
                                <span><i class="bi bi-naver blink"></i></span>
                                <span>{{ $t("txt.auth.login.with-naver") }}</span>
                            </span>
                            <span class="indicator-progress">{{ $t("txt.auth.login.please-wait") }}
                                <span class="spinner-border spinner-border-sm align-middle ms-2"></span>
                            </span>
                        </button>
                    </div>
                </div>
                <div class="d-flex justify-content-end mb-5">
                    <button type="button" class="badge btn btn-sm btn-light-primary badge-outlined btn-outlined fw-light opacity-75 blink"
                            @click="$emit('open-user-signup')"
                            data-bs-toggle="tooltip" data-bs-placement="bottom" :title="$t('bs.tooltip.auth.login.request-user')">
                        <i class="bi bi-person-plus-fill blink"></i>{{ $t("txt.user.signup") }}
                    </button>
                </div>
                <div class="text-end mb-5">
                    {{ $t("txt.auth.login.copyright") }}
                </div>
            </form>
        </div>
    `,
};
