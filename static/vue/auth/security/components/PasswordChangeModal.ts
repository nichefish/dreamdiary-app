/**
 * PasswordChangeModal.ts
 * 로그인 비밀번호 변경 모달 컴포넌트
 *
 * @author nichefish
 */
export default {
    name: "PasswordChangeModal",
    props: {
        username: { type: String, default: "" },
        currPw: { type: String, default: "" },
        newPw: { type: String, default: "" },
        newPwCf: { type: String, default: "" },
        errors: { type: Object, default: () => ({}) },
        errorMsgLines: { type: Array, default: () => [] },
    },
    emits: [
        "submit-password-change",
        "close-password-change",
        "update:currPw",
        "update:newPw",
        "update:newPwCf",
    ],
    computed: {
        currPwModel: {
            get(): string {
                return this.currPw || "";
            },
            set(value: string): void {
                this.$emit("update:currPw", value);
            },
        },
        newPwModel: {
            get(): string {
                return this.newPw || "";
            },
            set(value: string): void {
                this.$emit("update:newPw", value);
            },
        },
        newPwCfModel: {
            get(): string {
                return this.newPwCf || "";
            },
            set(value: string): void {
                this.$emit("update:newPwCf", value);
            },
        },
    },
    methods: {
        getModal(): any {
            const modalEl = this.$refs.modalEl as HTMLElement;
            if (!modalEl) return null;

            return (window as any).bootstrap.Modal.getOrCreateInstance(modalEl, {
                keyboard: false,
                backdrop: "static",
            });
        },
        show(): void {
            const modal = this.getModal();
            if (modal) modal.show();
        },
        hide(): void {
            const modal = this.getModal();
            if (modal) modal.hide();
        },
    },
    unmounted(): void {
        const modalEl = this.$refs.modalEl as HTMLElement;
        if (!modalEl) return;

        const modal = (window as any).bootstrap.Modal.getInstance(modalEl);
        if (modal) modal.dispose();
    },
    template: `
        <div ref="modalEl" class="modal fade" id="login_pw_chg_modal" tabindex="-1" role="dialog" aria-hidden="true"
             data-bs-keyboard="false" data-bs-backdrop="static">
            <div class="modal-dialog modal-dialog-centered modal-md" role="document">
                <div class="modal-content">
                    <div class="modal-header bg-dark">
                        <h5 class="modal-title text-white">{{ $t("txt.auth.login.password-change") }}</h5>
                        <button type="button" class="btn-close btn-close-white" @click="$emit('close-password-change')" :aria-label="$t('txt.auth.login.close')"></button>
                    </div>
                    <div class="modal-body">
                        <form name="loginPwChgForm" id="loginPwChgForm" class="form" @submit.prevent="$emit('submit-password-change')">
                            <input type="hidden" name="username" id="loginUsername" :value="username">
                            <div class="row">
                                <div class="col-xl-12 text-danger">
                                    <template v-for="(line, index) in errorMsgLines" :key="index">
                                        {{ line }}<br v-if="index < errorMsgLines.length - 1">
                                    </template>
                                </div>
                            </div>
                            <div class="row mb-5">
                                <div class="col-xl-3">
                                    <div class="col-form-label text-center fs-6 fw-bold">
                                        <label for="currPw">{{ $t("txt.auth.login.current-password") }}</label>
                                    </div>
                                </div>
                                <div class="col-xl-9 text-start">
                                    <input type="password" name="currPw" id="currPw" class="form-control required" maxlength="20" v-model="currPwModel">
                                    <div id="currPw_validate_span" class="text-danger">{{ errors.currPw }}</div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-xl-3">
                                    <div class="col-form-label text-center fs-6 fw-bold">
                                        <label for="newPw">{{ $t("txt.auth.login.new-password") }}</label>
                                    </div>
                                </div>
                                <div class="col-xl-9 text-start">
                                    <input type="password" name="newPw" id="newPw" class="form-control required" maxlength="20" v-model="newPwModel">
                                    <div class="fs-8 form-text text-noti">
                                        {{ $t("txt.req.password") }}
                                    </div>
                                    <div id="newPw_validate_span" class="text-danger">{{ errors.newPw }}</div>
                                </div>
                            </div>
                            <div class="row mb-5">
                                <div class="col-xl-3">
                                    <div class="col-form-label text-center fs-6 fw-bold">
                                        <label for="newPwCf">{{ $t("txt.auth.login.new-password-confirm") }}</label>
                                    </div>
                                </div>
                                <div class="col-xl-9 text-start">
                                    <input type="password" name="newPwCf" id="newPwCf" class="form-control required" maxlength="20" v-model="newPwCfModel">
                                    <div id="newPwCf_validate_span" class="text-danger">{{ errors.newPwCf }}</div>
                                </div>
                            </div>
                        </form>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-primary" @click="$emit('submit-password-change')">{{ $t("txt.auth.login.save") }}</button>
                        <button type="button" class="btn btn-light" @click="$emit('close-password-change')">{{ $t("txt.auth.login.close") }}</button>
                    </div>
                </div>
            </div>
        </div>
    `,
};
