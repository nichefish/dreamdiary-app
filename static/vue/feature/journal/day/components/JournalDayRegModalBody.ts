/**
 * JournalDayRegModalBody.ts
 * 저널 일자 등록/수정 모달 본문(Handlebars `journal_day_reg_modal_template` + tagify partials) Vue 이전.
 */

const JournalDayRegModalBody = {
    name: "JournalDayRegModalBody",
    props: {
        model: { type: Object, required: true },
    },
    computed: {
        diaryResolvedChecked(): boolean {
            return String(this.model?.diaryResolvedYn ?? "").trim().toUpperCase() === "Y";
        },
        diaryResolvedLabelText(): string {
            return this.diaryResolvedChecked ? this.t("txt.status.completed") : this.t("txt.status.incomplete");
        },
        /**
         * 레거시 `cF.ui.chckboxLabel(..., "완료//미완료", "blue//gray")` 와 동일한 라벨 색.
         */
        diaryResolvedLabelStyle(): Record<string, string> {
            return { color: this.diaryResolvedChecked ? "blue" : "gray" };
        },
        hasWeatherPreview(): boolean {
            return typeof cF !== "undefined" && cF.util.isNotEmpty(this.model?.weather);
        },
        tagListInitial(): string {
            return String(this.model?.tag?.tagListStrWithCtgr ?? "");
        },
        metaListInitial(): string {
            return String(this.model?.meta?.metaListStr ?? "");
        },
    },
    methods: {
        t(key: string): string {
            return this.$t ? this.$t(key) : key;
        },
        onDiaryResolvedChange(event: Event): void {
            const target = event.target as HTMLInputElement | null;
            if (!target) return;
            this.model.diaryResolvedYn = target.checked ? "Y" : "N";
        },
    },
    template: `
    <div class="journal-day-reg-vue-root">
        <input type="hidden" name="id" :value="model.id ?? ''" />

        <div class="row row-cols-lg-2 mb-3">
            <div class="col-xl-2 col-2 d-flex-center">
                <label class="fs-6 fw-bold mb-2 required" for="journalDate">{{ t('txt.journal.day.field.date') }}</label>
            </div>
            <div class="col-xl-3 col-5 d-flex flex-column" id="journalDateDiv">
                <div class="d-flex align-items-center mt-1">
                    <span class="menu-icon me-md-2 vertical-middle">
                        <label for="journalDate"><i class="bi bi-calendar3 fs-2 cursor-pointer"></i></label>
                    </span>
                    <input
                        type="text"
                        name="journalDate"
                        id="journalDate"
                        class="form-control form-control-solid w-150px"
                        :value="model.journalDate"
                        :placeholder="t('txt.journal.day.placeholder.journal-date')"
                        maxlength="10"
                        autocomplete="off"
                    />
                </div>
                <div id="journalDate_validate_span"></div>
            </div>
            <div class="col-xl-2 col-5 d-flex flex-column align-items-start col-form-label">
                <div class="d-flex align-items-center gap-2">
                    <label class="fs-6 fw-bold mb-0 required" for="journalDatePrecision">{{ t('txt.journal.day.field.date-precision') }}</label>
                    <i
                        class="bi bi-question-circle cursor-pointer"
                        data-bs-toggle="tooltip"
                        data-bs-placement="top"
                        data-bs-dismiss="click"
                        :title="t('bs.tooltip.journal.date-precision')"
                    ></i>
                </div>
            </div>
            <div class="col-xl-3 col-5 d-flex flex-column">
                <select name="journalDatePrecision" id="journalDatePrecision" v-model="model.journalDatePrecision" class="form-select form-select-solid w-150px mt-1">
                    <option value="EXACT">EXACT</option>
                    <option value="APPROXIMATE">APPROXIMATE</option>
                    <option value="UNKNOWN">UNKNOWN</option>
                </select>
            </div>
        </div>

        <div class="row mb-3">
            <div class="col-2 text-center">
                <label
                    for="weather"
                    class="cursor-help col-form-label fs-6 fw-bold"
                    data-bs-toggle="tooltip"
                    data-bs-placement="top"
                    data-bs-dismiss="click"
                    :title="t('bs.tooltip.journal.weather')"
                >
                    {{ t('txt.journal.day.field.weather') }}
                    <i class="bi bi-question-circle"></i>
                </label>
            </div>
            <div class="col-1 d-flex-center w-5">
                <div
                    id="weather_icon_div"
                    class="cursor-pointer"
                    data-bs-toggle="tooltip"
                    data-bs-placement="top"
                    data-bs-dismiss="click"
                    :title="t('bs.tooltip.journal.weather-preview')"
                >
                    <span v-if="hasWeatherPreview" v-html="model.weather"></span>
                    <span v-else>-</span>
                </div>
            </div>
            <div class="col-8">
                <textarea
                    name="weather"
                    id="weather"
                    v-model="model.weather"
                    class="form-control form-control-solid d-block"
                    :placeholder="t('txt.journal.day.placeholder.weather')"
                    maxlength="200"
                    rows="1"
                ></textarea>
            </div>
            <div class="col-1 d-flex align-items-center">
                <div
                    class="btn btn-sm btn-icon btn-light-primary"
                    data-journal-day-action="refresh-icon"
                    data-bs-toggle="tooltip"
                    data-bs-placement="top"
                    data-bs-dismiss="click"
                    :title="t('bs.tooltip.journal.weather-refresh-preview')"
                >
                    <i class="bi bi-arrow-repeat"></i>
                </div>
            </div>
        </div>

        <div class="row d-flex mb-8">
            <div class="col-lg-12 col-3 d-flex align-items-center">
                <label
                    class="text-gray-700 fs-6 fw-bolder cursor-help"
                    data-bs-toggle="tooltip"
                    data-bs-placement="top"
                    :title="t('bs.tooltip.journal.diary-resolved')"
                >
                    {{ t('txt.journal.day.diary-resolved-label') }}
                    <i class="bi bi-question-circle"></i>
                </label>
            </div>
            <div class="col-lg-2 col-9 d-flex align-items-center">
                <div class="form-check form-switch form-check-custom form-check-solid cursor-pointer">
                    <input
                        type="checkbox"
                        name="diaryResolvedYn"
                        id="diaryResolvedYn"
                        class="form-check-input cursor-pointer"
                        value="Y"
                        :checked="diaryResolvedChecked"
                        @change="onDiaryResolvedChange"
                    />
                    <label
                        class="form-check-label fw-bold fs-6 ms-3"
                        for="diaryResolvedYn"
                        id="diaryResolvedYnLabel"
                        :style="diaryResolvedLabelStyle"
                    >{{ diaryResolvedLabelText }}</label>
                </div>
            </div>
        </div>

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
                    :value="tagListInitial"
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
                        <input type="text" id="tag_display" class="form-control form-control-sm form-control-solid text-dialog fw-bold fs-7 w-100px" disabled>
                    </div>
                </div>
            </div>
        </div>

        <div class="row">
            <div>
                <label for="metaListStr" class="mb-2">
                    <span class="text-gray-700 fs-6 fw-bolder">{{ t('txt.attachable.meta.tagify.meta') }}</span>
                </label>
            </div>
            <div class="col-xl-12 text-sm-start" id="meta_div">
                <input
                    name="meta.metaListStr"
                    id="metaListStr"
                    class="form-control form-control-solid no-space"
                    autocomplete="off"
                    :value="metaListInitial"
                />
                <div class="d-flex pt-2 gap-2">
                    <div id="meta_ctgr_select_div" style="display: none; position: relative;">
                        <select id="meta_ctgr_select" class="form-select orm-select-solid py-2">
                            <option value="custom">{{ t('txt.user.form.custom-input') }}</option>
                        </select>
                    </div>
                    <div id="meta_ctgr_div" style="display:none;">
                        <input
                            type="text"
                            id="meta_ctgr"
                            class="form-control form-control-sm form-control-solid text-noti w-100px"
                            :placeholder="t('txt.attachable.meta.tagify.category-placeholder')"
                            maxlength="500"
                        >
                    </div>
                    <div id="meta_display_div" style="display:none;">
                        <input type="text" id="meta_display" class="form-control form-control-sm form-control-solid text-dialog fw-bold fs-7 w-100px" disabled>
                    </div>
                    <div id="meta_value_div" style="display:none;">
                        <input
                            type="text"
                            id="meta_value"
                            class="form-control form-control-sm form-control-solid w-200px"
                            :placeholder="t('txt.attachable.meta.tagify.value-placeholder')"
                            maxlength="500"
                        >
                    </div>
                </div>
            </div>
        </div>
    </div>
    `,
};

export default JournalDayRegModalBody;
