/**
 * JournalAnnualReviewRegModalHeader.ts
 * 저널 결산 리뷰 등록/수정 모달 헤더(Handlebars `journal_annual_review_reg_modal_header_template`) Vue 이전.
 *
 * 변경(A-3):
 *   - `_journal_annual_review_reg_modal_header_template.hbs` 의 헤더 마크업을 동일 DOM/속성으로 옮긴다.
 *   - 헤더는 hidden input 세 개(id + journalAnnualId + categoryCode) 만 있어 t() / i18n 도입 없이 단순 컴포넌트로 구현.
 *
 * @author nichefish
 */

const JournalAnnualReviewRegModalHeader = {
    name: "JournalAnnualReviewRegModalHeader",
    props: {
        model: { type: Object, required: true },
    },
    template: `
    <div class="journal-annual-review-reg-header-vue-root">
        <input type="hidden" name="id" :value="(model.id != null ? model.id : '')">
        <input type="hidden" name="journalAnnualId" :value="(model.journalAnnualId != null ? model.journalAnnualId : '')">
        <input type="hidden" name="categoryCode" :value="model.categoryCode ?? ''">
    </div>
    `,
};

export default JournalAnnualReviewRegModalHeader;
