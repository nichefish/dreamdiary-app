/**
 * JournalTodoRegModalHeader.ts
 * 저널 할일 등록/수정 모달 헤더(Handlebars `journal_todo_reg_modal_header_template`) Vue 이전.
 *
 * 변경(T-2-α):
 *   - _journal_todo_reg_modal_header_template.hbs 의 헤더 마크업을 동일 DOM/클래스로 옮긴다.
 *   - categoryCode <select> 는 기존과 동일하게 빈 옵션(`txt.journal.select.post-ctgr`)만 노출한다(dead UI 보존).
 *
 * @author nichefish
 */

const JournalTodoRegModalHeader = {
    name: "JournalTodoRegModalHeader",
    props: {
        model: { type: Object, required: true },
    },
    computed: {
        /** 수정 모드(서버에서 받은 기존 todo) 여부. HBS `(exists id)` 와 동일. */
        isMdf(): boolean {
            return this.model?.id != null && String(this.model.id).length > 0;
        },
        /** 수정 모드일 때 입력 칸 col 폭 분기. HBS `{{#if (exists id)}}9{{else}}10{{/if}}` 와 동일. */
        titleColClass(): string {
            return this.isMdf ? "col-lg-9" : "col-lg-10";
        },
    },
    methods: {
        t(key: string): string {
            return this.$t ? this.$t(key) : key;
        },
    },
    template: `
    <div class="journal-todo-reg-header-vue-root">
        <input type="hidden" name="id" :value="model.id ?? ''">
        <input type="hidden" name="yy" :value="model.yy ?? ''">
        <input type="hidden" name="mnth" :value="model.mnth ?? ''">

        <div class="row d-flex mb-8">
            <div class="col-2">
                <label class="d-flex align-items-center mb-2" for="title">
                    <span class="text-gray-700 fs-6 fw-bolder">{{ t('txt.journal.day') }}</span>
                </label>
            </div>
            <div class="col-2 fs-6">
                <i class="bi bi-calendar3"></i>
                {{ model.yy ?? '' }}{{ t('txt.date.suffix.after-year-number') }}{{ model.mnth ?? '' }}{{ t('txt.date.suffix.after-month-number') }}
            </div>
        </div>

        <div class="row d-flex mb-8">
            <div class="col-12">
                <label class="d-flex align-items-center mb-2" for="title">
                    <span class="text-gray-700 fs-6 fw-bolder">{{ t('txt.title') }}</span>
                    <span class="text-gray-500 fs-9">{{ t('txt.journal.field.title-max-50') }}</span>
                </label>
            </div>
            <div class="col-lg-2">
                <select name="categoryCode" id="categoryCode" class="form-select form-select-solid" :value="model.categoryCode ?? ''">
                    <option value="" :selected="!model.categoryCode">{{ t('txt.journal.select.post-ctgr') }}</option>
                </select>
            </div>
            <div :class="['', titleColClass]">
                <input
                    type="text"
                    name="title"
                    id="title"
                    class="form-control"
                    :value="model.title ?? ''"
                    :placeholder="t('txt.title')"
                    maxlength="100"
                />
                <div id="title_validate_span"></div>
            </div>
            <div v-if="isMdf" class="col-1 d-flex ps-0">
                <div class="d-flex-center p-2 fw-bold fs-5 text-gray-600">#</div>
                <input
                    type="number"
                    class="form-control form-control-sm"
                    name="sortOrder"
                    id="sortOrder"
                    min="1"
                    max="99"
                    :value="model.sortOrder ?? ''"
                    :placeholder="t('txt.journal.field.sort-order.placeholder')"
                    maxlength="3"
                    data-bs-toggle="tooltip"
                    data-bs-placement="top"
                    :title="t('bs.tooltip.journal.sort-order.in-day')"
                />
            </div>
        </div>
    </div>
    `,
};

export default JournalTodoRegModalHeader;