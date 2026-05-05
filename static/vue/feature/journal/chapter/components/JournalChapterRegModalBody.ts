/**
 * JournalChapterRegModalBody.ts
 * 저널 챕터 등록/수정 모달 본문(Handlebars `journal_chapter_reg_modal_template`) Vue 이전.
 *
 * 변경(Phase B):
 *   - 기존 _journal_chapter_reg_modal_template.hbs 의 본문 마크업을 동일 DOM/클래스로 옮긴다.
 *   - JOURNAL_CHAPTER_CTGR_CD 옵션은 부모 `JournalChapterRegModalApp` 가 `model.categoryOptions` 로 주입한다.
 *   - dF.JournalChapter.initForm 에서 수행하던 `cF.handlebars.modal(obj, "journal_chapter_reg")` 호출 진입은
 *     `window.JournalChapterRegVueApp.open(model)` 단일 진입으로 수렴한다.
 *
 * @author nichefish
 */

const JournalChapterRegModalBody = {
    name: "JournalChapterRegModalBody",
    props: {
        model: { type: Object, required: true },
    },
    computed: {
        /** 수정 모드(서버에서 받은 기존 챕터) 여부. HBS `(exists id)` 와 동일. */
        isMdf(): boolean {
            return this.model?.id != null && String(this.model.id).length > 0;
        },
        /** 수정 모드의 DREAM 챕터: 타입 변경 불가(자동 라벨 안내) — HBS와 동일 분기. */
        isMdfDream(): boolean {
            return this.isMdf && this.model?.chapterType === "DREAM";
        },
        /**
         * 카테고리 옵션. JournalChapterRegModalApp 가 `__journalChapterRegBootstrap.categoryOptions`
         * 를 주입한다. 서버 모델 `JOURNAL_CHAPTER_CTGR_CD` 와 동일한 (code, codeName) 쌍.
         */
        categoryOptions(): Array<{ code: string; codeName: string }> {
            const list = this.model?.categoryOptions;
            return Array.isArray(list) ? list : [];
        },
        /** 수정 모드일 때 입력 칸 col 폭 분기 (HBS 의 `{{#if (exists id)}}7{{else}}8{{/if}}` 동일). */
        titleColClass(): string {
            return this.isMdf ? "col-lg-7" : "col-lg-8";
        },
    },
    methods: {
        t(key: string): string {
            return this.$t ? this.$t(key) : key;
        },
    },
    template: `
    <div class="journal-chapter-reg-vue-root">
        <input type="hidden" name="id" :value="model.id ?? ''">
        <input type="hidden" name="journalDayId" :value="model.journalDayId ?? ''">

        <div class="row d-flex mb-8">
            <div class="col-2">
                <label class="d-flex align-items-center mb-2" for="title">
                    <span class="text-gray-700 fs-6 fw-bolder">{{ t('txt.journal.day') }}</span>
                </label>
            </div>
            <div class="col-2 fs-6">
                <i class="bi bi-calendar3"></i>
                {{ model.stdrdDt }}
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
                <template v-if="isMdfDream">
                    <input type="hidden" name="chapterType" value="DREAM" />
                    <div class="form-control form-control-solid form-control-sm d-flex align-items-center min-h-40px">
                        <span class="fw-bolder">{{ t('txt.dream') }}</span>
                        <span class="text-muted fs-8 ms-2">({{ t('msg.journal.chapter.dream-auto-label') }})</span>
                    </div>
                </template>
                <template v-else>
                    <select name="chapterType" id="chapterType" class="form-select form-select-solid" :value="model.chapterType ?? 'DIARY'">
                        <option value="DIARY" :selected="(model.chapterType ?? 'DIARY') === 'DIARY'">{{ t('txt.diary') }}</option>
                        <option value="NOTE" :selected="model.chapterType === 'NOTE'">{{ t('txt.chapter.type.note') }}</option>
                    </select>
                </template>
            </div>
            <div class="col-lg-2">
                <select name="categoryCode" id="categoryCode" class="form-select form-select-solid" :value="model.categoryCode ?? ''">
                    <option value="" :selected="!model.categoryCode">{{ t('txt.journal.select.post-ctgr') }}</option>
                    <option
                        v-for="ctgr in categoryOptions"
                        :key="ctgr.code"
                        :value="ctgr.code"
                        :selected="model.categoryCode === ctgr.code"
                    >[{{ ctgr.codeName }}]</option>
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
                    :title="t('bs.tooltip.journal.sort-order.chapter-in-day')"
                />
            </div>
        </div>
    </div>
    `,
};

export default JournalChapterRegModalBody;
