import codeAdminUiService from "../services/codeAdminUiService.js";

export default {
    name: "CodeItemRegistForm",
    props: {
        form: { type: Object, required: true },
    },
    methods: {
        isEdit(): boolean {
            return cF.util.isNotEmpty(this.form.code);
        },
        isUse(): boolean {
            return codeAdminUiService.isYn(this.form.useYn);
        },
        onUse(e: Event): void {
            this.form.useYn = (e.target as HTMLInputElement).checked ? "Y" : "N";
        },
    },
    template: `
    <input type="hidden" name="id" :value="form.id || ''">
    <input type="hidden" name="groupCode" :value="form.groupCode">
    <input type="hidden" name="regYn" :value="form.registYn">
    <div class="row mb-4">
        <div class="col-3 col-form-label text-center"><span class="cursor-help fw-bold required">{{ $t('txt.admin.code.item.form.code') }} <i class="bi bi-question-circle"></i></span></div>
        <div class="col-4">
            <template v-if="isEdit()">
                <div class="col-form-label"><span class="text-noti fw-bold ps-1">{{ form.code }}</span><input type="hidden" name="code" :value="form.code"></div>
            </template>
            <template v-else>
                <input type="text" name="code" id="code" class="form-control form-control-solid required" :value="form.code" maxlength="30">
                <div class="text-noti">{{ $t('txt.admin.code.item.form.code-guide') }}</div>
                <div id="code_validate_span"></div>
            </template>
        </div>
    </div>
    <div class="row mb-4">
        <div class="col-3 col-form-label text-center"><label class="cursor-help fw-bold required">{{ $t('txt.admin.code.item.form.code-name') }} <i class="bi bi-question-circle"></i></label></div>
        <div class="col-9"><input type="text" name="codeName" id="codeName" class="form-control form-control-solid required" :value="form.codeName" maxlength="20"><div id="codeName_validate_span"></div></div>
    </div>
    <div class="row mb-4">
        <div class="col-3 col-form-label text-center"><label class="fw-bold">{{ $t('txt.admin.code.item.form.code-description') }}</label></div>
        <div class="col-9"><textarea name="description" id="description" class="form-control form-control-solid h-100px" wrap="hard" maxlength="1000" :placeholder="$t('txt.admin.code.item.form.code-description-placeholder')">{{ form.description }}</textarea></div>
    </div>
    <div class="row mb-4">
        <div class="col-3 col-form-label text-center"><label class="cursor-help fw-bold">{{ $t('txt.admin.code.group.detail.use-yn') }} <i class="bi bi-question-circle"></i></label></div>
        <div class="col-9 text-center d-flex align-items-center">
            <div class="form-check form-switch form-check-custom form-check-solid">
                <input type="checkbox" name="useYn" id="useYn" class="form-check-input cursor-pointer" value="Y" :checked="isUse()" @change="onUse">
                <label class="form-check-label ms-3" for="useYn" id="useYnLabel">{{ isUse() ? $t('txt.comm.use') : $t('txt.status.unuse') }}</label>
            </div>
        </div>
    </div>
    `,
};

