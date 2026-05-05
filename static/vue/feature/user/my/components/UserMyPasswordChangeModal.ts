import { UserMyLabels } from "../types.js";

export default {
    name: "UserMyPasswordChangeModal",
    props: {
        errorMsg: { type: String, required: false, default: "" },
        labels: { type: Object, required: true },
    },
    emits: ["submit"],
    computed: {
        l(): UserMyLabels {
            return this.labels as UserMyLabels;
        },
    },
    template: `
    <div>
        <!--begin::Row-->
        <div class="row">
            <div class="col-xl-12 text-danger">
                {{ errorMsg }}
            </div>
        </div>
        <!--begin::Row-->
        <div class="row mb-5">
            <div class="col-xl-3">
                <div class="col-form-label text-center fs-6 fw-bold">
                    <label for="currPw">{{ l.currentPassword }}</label>
                </div>
            </div>
            <div class="col-xl-9 text-start">
                <input type="password" name="currPw" id="currPw" class="form-control required" maxlength="20">
                <div id="currPw_validate_span"></div>
            </div>
        </div>
        <!--begin::Row-->
        <div class="row">
            <div class="col-xl-3">
                <div class="col-form-label text-center fs-6 fw-bold">
                    <label for="newPw">{{ l.newPassword }}</label>
                </div>
            </div>
            <div class="col-xl-9 text-start">
                <input type="password" name="newPw" id="newPw" class="form-control required" maxlength="20">
                <div class="fs-8 form-text text-noti">
                    {{ l.passwordReq }}
                </div>
                <div id="newPw_validate_span"></div>
            </div>
        </div>
        <!--begin::Row-->
        <div class="row mb-5">
            <div class="col-xl-3">
                <div class="col-form-label text-center fs-6 fw-bold">
                    <label for="newPwCf">{{ l.newPasswordConfirm }}</label>
                </div>
            </div>
            <div class="col-xl-9 text-start">
                <input type="password" name="newPwCf" id="newPwCf" class="form-control required" maxlength="20">
                <div id="newPwCf_validate_span"></div>
            </div>
        </div>
    </div>
    `,
};
