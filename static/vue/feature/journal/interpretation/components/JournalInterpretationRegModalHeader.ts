/**
 * JournalInterpretationRegModalHeader.ts
 * 저널 해석 등록/수정 모달 헤더(Handlebars `journal_interpretation_reg_modal_header`) Vue 이전.
 *
 * 변경(I-2):
 *   - _journal_interpretation_reg_modal_header_template.hbs 의 헤더 마크업을 동일 DOM/클래스로 옮긴다.
 *   - 단일 contentType (JOURNAL_INTERPRETATION) — entry 와 달리 분기 없음.
 *   - 카테고리(ctgrCd) <option> 정적 자리 표시는 hbs 동등으로 보존(외부 채움 흐름 유지).
 *
 * @author nichefish
 */

const JournalInterpretationRegModalHeader = {
    name: "JournalInterpretationRegModalHeader",
    props: {
        model: { type: Object, required: true },
    },
    computed: {
        isEdit(): boolean {
            const id = (this.model as Record<string, any>)?.id;
            return id != null && String(id).length > 0;
        },
        /** hbs: col-lg-{{#if (exists id)}}9{{else}}10{{/if}}. */
        titleColClass(): string {
            return this.isEdit ? "col-lg-9" : "col-lg-10";
        },
    },
    methods: {
        t(key: string): string {
            return this.$t ? this.$t(key) : key;
        },
    },
    template: `
    <div class="journal-interpretation-reg-header-vue-root">
        <input type="hidden" name="id" :value="(model.id != null ? model.id : '')">
        <input type="hidden" name="refId" :value="(model.refId != null ? model.refId : '')">
        <input type="hidden" name="refContentType" :value="model.refContentType ?? ''">

        <div class="row d-flex mb-8">
            <div class="col-2">
                <label class="d-flex align-items-center mb-2" for="title">
                    <span class="text-gray-700 fs-6 fw-bolder">{{ t('txt.journal.day') }}</span>
                </label>
            </div>
            <div class="col-2 fs-6">
                <i class="bi bi-calendar3"></i>
                {{ model.stdrdDt ?? '' }}
                <span v-if="model.journalDateWeekDay" class="fs-8 text-gray-600">({{ model.journalDateWeekDay }})</span>
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
                <select name="ctgrCd" id="ctgrCd" class="form-select form-select-solid" :value="model.ctgrCd ?? ''">
                    <option value="">{{ t('txt.journal.select.post-ctgr') }}</option>
                </select>
            </div>
            <div :class="titleColClass">
                <input type="text" name="title" id="title" class="form-control"
                       :value="model.title ?? ''" :placeholder="t('txt.title')" maxlength="100" />
                <div id="title_validate_span"></div>
            </div>
            <div v-if="isEdit" class="col-1 d-flex ps-0">
                <div class="d-flex-center p-2 fw-bold fs-5 text-gray-600">#</div>
                <input type="number" class="form-control form-control-sm" name="sortOrder" id="sortOrder"
                       min="1" max="99" :value="model.sortOrder ?? ''"
                       :placeholder="t('txt.journal.field.sort-order.placeholder')"
                       maxlength="3"
                       data-bs-toggle="tooltip" data-bs-placement="top"
                       :title="t('bs.tooltip.journal.sort-order.in-interpretation-group')">
            </div>
        </div>
    </div>
    `,
};

export default JournalInterpretationRegModalHeader;
