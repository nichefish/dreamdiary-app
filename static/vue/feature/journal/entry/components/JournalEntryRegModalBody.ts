/**
 * JournalEntryRegModalBody.ts
 * 저널 entry 등록/수정 모달 본문(Handlebars `journal_*_reg_modal` body 템플릿) Vue 이전.
 *
 * 변경(E-2):
 *   - _journal_entry_reg_modal_body_template.hbs 의 본문(tagify partial 인용)을 동일 DOM/클래스로 옮긴다.
 *   - DIARY/DREAM 만 tagify 사용(useTag). NOTE 모달은 본 영역 비움(기존 entryRegShowTagify=false).
 *
 * @author nichefish
 */

const JournalEntryRegModalBody = {
    name: "JournalEntryRegModalBody",
    props: {
        contentType: { type: String, required: true },
        model: { type: Object, required: true },
    },
    computed: {
        showTagify(): boolean {
            const ct = this.contentType as string;
            return ct === "JOURNAL_DIARY" || ct === "JOURNAL_DREAM";
        },
        tagListStrWithCtgr(): string {
            const tag = (this.model as Record<string, any>)?.tag;
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
    <div class="journal-entry-reg-body-vue-root">
        <template v-if="showTagify">
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
        </template>
    </div>
    `,
};

export default JournalEntryRegModalBody;
