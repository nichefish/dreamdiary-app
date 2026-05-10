export default {
    name: "UserMyPasswordChangeModal",
    props: {
        errorMsg: { type: String, required: false, default: "" },
        t: { type: Function, required: true },
    },
    emits: ["submit"],
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
                    <label for="currPw">{{ t('txt.user.my.current-password') }}</label>
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
                    <label for="newPw">{{ t('txt.user.my.new-password') }}</label>
                </div>
            </div>
            <div class="col-xl-9 text-start">
                <input type="password" name="newPw" id="newPw" class="form-control required" maxlength="20">
                <div class="fs-8 form-text text-noti">
                    {{ t('txt.req.password') }}
                </div>
                <div id="newPw_validate_span"></div>
            </div>
        </div>
        <!--begin::Row-->
        <div class="row mb-5">
            <div class="col-xl-3">
                <div class="col-form-label text-center fs-6 fw-bold">
                    <label for="newPwCf">{{ t('txt.user.my.new-password-confirm') }}</label>
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
