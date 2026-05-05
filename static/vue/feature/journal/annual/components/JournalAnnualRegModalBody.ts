/**
 * JournalAnnualRegModalBody.ts
 * 저널 결산 등록/수정 모달 본문(Handlebars `journal_annual_reg_modal_template`) Vue 이전.
 *
 * 변경(A-3):
 *   - 원본 hbs body 는 `<#include "/view/feature/attachable/tag/_tag_tagify_partial.hbs">` 한 줄로 tagify partial 만 들고 있었다.
 *     해당 partial 의 핵심은 input value 에 `{{tag.tagListStrWithCtgr}}` 를 바인딩하는 것이며, 본 컴포넌트는 동일 DOM/속성을
 *     model 기반 v-bind 로 옮긴다.
 *   - 메시지 키(`txt.attachable.tag.tagify.tag` 등)는 spring `Message` 글로벌(런타임 메시지 캐시)을 통해 가져온다(다른 Vue 폼들과 동일 패턴).
 *
 * @author nichefish
 */

const JournalAnnualRegModalBody = {
    name: "JournalAnnualRegModalBody",
    props: {
        model: { type: Object, required: true },
    },
    computed: {
        tagListStrWithCtgr(): string {
            const tag = (this.model as Record<string, any>)?.tag as Record<string, any> | undefined;
            return tag?.tagListStrWithCtgr ?? "";
        },
        tagLabel(): string {
            return Message.get("txt.attachable.tag.tagify.tag");
        },
        tagGuide(): string {
            return Message.get("txt.attachable.tag.tagify.tag-guide");
        },
        ctgrPlaceholder(): string {
            return Message.get("txt.attachable.tag.tagify.category-placeholder");
        },
        customInputLabel(): string {
            return Message.get("txt.user.form.custom-input");
        },
    },
    template: `
    <div class="journal-annual-reg-body-vue-root">
        <div class="row">
            <div>
                <label for="tagListStr" class="mb-2">
                    <span class="text-gray-700 fs-6 fw-bolder">{{ tagLabel }}</span>
                    <span class="text-gray-500 fs-9 mx-2">{{ tagGuide }}</span>
                </label>
            </div>
            <div class="col-xl-12 text-sm-start" id="tag_div">
                <input name="tag.tagListStr" id="tagListStr" class="form-control form-control-solid no-space" autocomplete="off" :value="tagListStrWithCtgr" />
                <div class="d-flex pt-2 gap-2">
                    <div id="tag_ctgr_select_div" style="display: none; position: relative;">
                        <select id="tag_ctgr_select" class="form-select orm-select-solid py-2">
                            <option value="custom">{{ customInputLabel }}</option>
                        </select>
                    </div>
                    <div id="tag_ctgr_div" style="display:none;">
                        <input type="text" id="tag_ctgr" class="form-control form-control-sm form-control-solid text-noti w-100px"
                               :placeholder="ctgrPlaceholder" maxlength="500">
                    </div>
                    <div id="tag_display_div" style="display:none;">
                        <input type="text" id="tag_display" class="form-control form-control-sm form-control-solid text-dialog fw-bold fs-7 w-100px" disabled>
                    </div>
                </div>
            </div>
        </div>
    </div>
    `,
};

export default JournalAnnualRegModalBody;
