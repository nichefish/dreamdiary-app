import codeAdminUiService from "../services/codeAdminUiService.js";

export default {
    name: "CodeGroupRegistForm",
    props: {
        form: { type: Object, required: true },
    },
    methods: {
        isEdit(): boolean {
            return cF.util.isNotEmpty(this.form.id);
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
    <input type="hidden" name="regYn" :value="form.registYn">
    <div class="row mb-4">
        <div class="col-3 col-form-label text-center">
            <label class="cursor-help fw-bold required">{{ $t('txt.admin.code.group.form.group-code') }} <i class="bi bi-question-circle"></i></label>
        </div>
        <div class="col-9">
            <template v-if="isEdit()">
                <div class="col-form-label">
                    <span class="text-noti fw-bold ps-1">{{ form.groupCode }}</span>
                    <input type="hidden" name="groupCode" :value="form.groupCode">
                </div>
            </template>
            <template v-else>
                <input type="text" name="groupCode" id="groupCode" class="form-control form-control-solid required" :value="form.groupCode" maxlength="30">
                <div class="text-noti">{{ $t('txt.admin.code.group.form.group-code-guide') }}</div>
                <div id="groupCode_validate_span"></div>
            </template>
        </div>
    </div>
    <div class="row mb-3">
        <div class="col-3 col-form-label text-center">
            <label class="cursor-help fw-bold required">{{ $t('txt.admin.code.group.form.group-name') }} <i class="bi bi-question-circle"></i></label>
        </div>
        <div class="col-9">
            <input type="text" name="groupName" id="groupName" class="form-control form-control-solid required" :value="form.groupName" maxlength="50">
            <div id="groupName_validate_span"></div>
        </div>
    </div>
    <div class="row mb-4">
        <div class="col-3 col-form-label text-center"><label class="fw-bold">{{ $t('txt.admin.code.group.form.group-description') }}</label></div>
        <div class="col-9"><textarea name="description" id="description" class="form-control form-control-solid h-75px">{{ form.description }}</textarea></div>
    </div>
    <div class="row mb-4">
        <div class="col-3 col-form-label text-center"><label class="cursor-help fw-bold">{{ $t('txt.admin.code.group.detail.use-yn') }} <i class="bi bi-question-circle"></i></label></div>
        <div class="col-9">
            <div class="form-check form-switch mt-2 form-check-custom form-check-solid">
                <input type="checkbox" name="useYn" id="useYn" class="form-check-input cursor-pointer" value="Y" :checked="isUse()" @change="onUse">
                <label class="form-check-label ms-3" for="useYn" id="useYnLabel">{{ isUse() ? $t('txt.comm.use') : $t('txt.status.unuse') }}</label>
            </div>
        </div>
    </div>
    `,
};

