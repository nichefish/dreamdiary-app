/**
 * JournalTodoRegModalBody.ts
 * 저널 할일 등록/수정 모달 본문(Handlebars `journal_todo_reg_modal_template`) Vue 이전.
 *
 * 변경(T-2-α):
 *   - _journal_todo_reg_modal_template.hbs 본문은 단순 `_tag_tagify_partial.hbs` 인용이었다.
 *     해당 partial 의 마크업을 동일 DOM/클래스로 그대로 옮긴다.
 *   - 카테고리 매핑(dF.JournalTodoTag.ctgrMap) 은 기존부터 dead 상태였으므로 동작 변경 없이 보존한다.
 *
 * @author nichefish
 */

const JournalTodoRegModalBody = {
    name: "JournalTodoRegModalBody",
    props: {
        model: { type: Object, required: true },
    },
    computed: {
        /** 기존 HBS `{{tag.tagListStrWithCtgr}}` 와 동일한 초기 입력 값. */
        tagListStrWithCtgr(): string {
            const tag = this.model?.tag;
            if (tag && typeof tag === "object" && typeof tag.tagListStrWithCtgr === "string") {
                return tag.tagListStrWithCtgr;
            }
            return "";
        },
    },
    methods: {
        t(key: string): string {
            return this.$t ? this.$t(key) : key;
        },
    },
    template: `
    <div class="journal-todo-reg-body-vue-root">
        <div class="row">
            <div>
                <label for="tagListStr" class="mb-2">
                    <span class="text-gray-700 fs-6 fw-bolder">{{ t('txt.attachable.tag.tagify.tag') }}</span>
                    <span class="text-gray-500 fs-9 mx-2">{{ t('txt.attachable.tag.tagify.tag-guide') }}</span>
                </label>
            </div>
            <div class="col-xl-12 text-sm-start" id="tag_div">
                <input
                    name="tag.tagListStr"
                    id="tagListStr"
                    class="form-control form-control-solid no-space"
                    autocomplete="off"
                    :value="tagListStrWithCtgr"
                />
                <div class="d-flex pt-2 gap-2">
                    <div id="tag_ctgr_select_div" style="display: none; position: relative;">
                        <select id="tag_ctgr_select" class="form-select orm-select-solid py-2">
                            <option value="custom">{{ t('txt.user.form.custom-input') }}</option>
                        </select>
                    </div>
                    <div id="tag_ctgr_div" style="display:none;">
                        <input
                            type="text"
                            id="tag_ctgr"
                            class="form-control form-control-sm form-control-solid text-noti w-100px"
                            :placeholder="t('txt.attachable.tag.tagify.category-placeholder')"
                            maxlength="500"
                        >
                    </div>
                    <div id="tag_display_div" style="display:none;">
                        <input
                            type="text"
                            id="tag_display"
                            class="form-control form-control-sm form-control-solid text-dialog fw-bold fs-7 w-100px"
                            disabled
                        >
                    </div>
                </div>
            </div>
        </div>
    </div>
    `,
};

export default JournalTodoRegModalBody;