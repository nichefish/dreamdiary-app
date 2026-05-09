/**
 * JournalAnnualReviewRegModalBody.ts
 * 저널 결산 리뷰 등록/수정 모달 본문(Handlebars `journal_annual_review_reg_modal_template`) Vue 이전.
 *
 * 변경(A-3):
 *   - 원본 hbs body 는 `<#include "/view/feature/attachable/tag/_tag_tagify_partial.hbs">` 한 줄로 tagify partial 만 들고 있었다(annual 과 동일).
 *   - 본 컴포넌트는 annual reg body 와 동일 마크업이며, model 인자 1개를 받는다.
 *
 * 변경(D):
 *   - `Message.get(...)` 직호출을 `resolveMessage` 헬퍼로 통일 — ESM 스코프 식별자 결의 race 차단.
 *
 * @author nichefish
 */

import { resolveMessage } from "../../../../common/messageHelper.js";

const JournalAnnualReviewRegModalBody = {
    name: "JournalAnnualReviewRegModalBody",
    props: {
        model: { type: Object, required: true },
    },
    computed: {
        tagListStrWithCtgr(): string {
            const tag = (this.model as Record<string, any>)?.tag as Record<string, any> | undefined;
            return tag?.tagListStrWithCtgr ?? "";
        },
        tagLabel(): string {
            return resolveMessage("txt.attachable.tag.tagify.tag");
        },
        tagGuide(): string {
            return resolveMessage("txt.attachable.tag.tagify.tag-guide");
        },
        ctgrPlaceholder(): string {
            return resolveMessage("txt.attachable.tag.tagify.category-placeholder");
        },
        customInputLabel(): string {
            return resolveMessage("txt.user.form.custom-input");
        },
    },
    template: `
    <div class="journal-annual-review-reg-body-vue-root">
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

export default JournalAnnualReviewRegModalBody;
