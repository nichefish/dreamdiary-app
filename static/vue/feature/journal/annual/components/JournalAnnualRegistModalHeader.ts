/**
 * JournalAnnualRegistModalHeader.ts
 * 저널 결산 등록/수정 모달 헤더(Handlebars `journal_annual_regist_modal_header_template`) Vue 이전.
 *
 * 변경(A-3):
 *   - `_journal_annual_regist_modal_header_template.hbs` 의 헤더 마크업을 동일 DOM/속성으로 옮긴다.
 *   - 헤더는 hidden input 두 개(id + contentType) 만 있어 t() / i18n 도입 없이 단순 컴포넌트로 구현.
 *
 * @author nichefish
 */

const JournalAnnualRegistModalHeader = {
    name: "JournalAnnualRegistModalHeader",
    props: {
        model: { type: Object, required: true },
    },
    template: `
    <div class="journal-annual-reg-header-vue-root">
        <input type="hidden" name="id" :value="(model.id != null ? model.id : '')">
        <input type="hidden" name="yy" :value="(model.yy != null ? model.yy : '')">
        <input type="hidden" name="contentType" value="JOURNAL_ANNUAL">
    </div>
    `,
};

export default JournalAnnualRegistModalHeader;
