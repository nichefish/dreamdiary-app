import boardAdminUiService from "../services/boardAdminUiService.js";

export default {
    name: "BoardRegistForm",
    props: {
        form: { type: Object, required: true },
    },
    methods: {
        isEdit(): boolean {
            return cF.util.isNotEmpty(this.form.id);
        },
        isUse(): boolean {
            return boardAdminUiService.isYn(this.form.useYn);
        },
        onUse(e: Event): void {
            this.form.useYn = (e.target as HTMLInputElement).checked ? "Y" : "N";
        },
    },
    template: `
    <input type="hidden" name="id" :value="form.id || ''">
    <input type="hidden" name="regYn" :value="form.regYn">
    <div class="row mb-4">
        <div class="col-3 col-form-label text-center">
            <label class="cursor-help fw-bold required">{{ $t('txt.board.group.form.board-code') }} <i class="bi bi-question-circle"></i></label>
        </div>
        <div class="col-6">
            <template v-if="isEdit()">
                <div class="col-form-label">
                    <span class="text-noti fw-bold ps-1">{{ form.boardKey }}</span>
                    <input type="hidden" name="boardKey" :value="form.boardKey">
                </div>
            </template>
            <template v-else>
                <input type="text" name="boardKey" id="boardKey" class="form-control form-control-solid required cddata"
                       v-model="form.boardKey" maxlength="30">
                <div class="text-noti">{{ $t('txt.board.group.form.board-code-guide') }}</div>
                <div id="boardKey_validate_span"></div>
            </template>
        </div>
    </div>
    <div class="row mb-4">
        <div class="col-3 col-form-label text-center">
            <label class="cursor-help fw-bold required">{{ $t('txt.board.group.form.board-name') }} <i class="bi bi-question-circle"></i></label>
        </div>
        <div class="col-9">
            <input type="text" name="boardName" id="boardName" class="form-control form-control-solid required"
                   v-model="form.boardName" maxlength="120">
        </div>
    </div>
    <div class="row mb-4">
        <div class="col-3 col-form-label text-center">
            <label class="cursor-help fw-bold required">{{ $t('txt.board.group.form.category-code') }} <i class="bi bi-question-circle"></i></label>
        </div>
        <div class="col-9">
            <input type="text" name="categoryGroupCode" id="categoryGroupCode" class="form-control form-control-solid required"
                   v-model="form.categoryGroupCode" maxlength="30">
        </div>
    </div>
    <div class="row mb-4">
        <div class="col-3 col-form-label text-center"><label class="fw-bold">{{ $t('txt.board.group.form.board-description') }}</label></div>
        <div class="col-9">
            <textarea name="description" id="description" class="form-control form-control-solid h-100px"
                      wrap="hard" maxlength="2000" v-model="form.description"
                      :placeholder="$t('txt.board.group.form.board-description-placeholder')"></textarea>
        </div>
    </div>
    <div class="row mb-4">
        <div class="col-3 col-form-label text-center">
            <label class="cursor-help fw-bold">{{ $t('txt.board.group.form.use-yn') }} <i class="bi bi-question-circle"></i></label>
        </div>
        <div class="col-9 text-center d-flex align-items-center">
            <div class="form-check form-switch form-check-custom form-check-solid">
                <input type="checkbox" name="useYn" id="useYn" class="form-check-input cursor-pointer" value="Y"
                       :checked="isUse()" @change="onUse">
                <label class="form-check-label ms-3" for="useYn" id="useYnLabel">{{ isUse() ? $t('txt.comm.use') : $t('txt.status.unuse') }}</label>
            </div>
        </div>
    </div>
    `,
};
