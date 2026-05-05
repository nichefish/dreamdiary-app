/**
 * JournalEntryRegModalHeader.ts
 * 저널 entry 등록/수정 모달 헤더(Handlebars `journal_*_reg_modal_header`) Vue 이전.
 *
 * 변경(E-2):
 *   - _journal_entry_reg_modal_header_template.hbs 의 헤더 마크업을 동일 DOM/클래스로 옮긴다.
 *   - DIARY / DREAM / NOTE 분기는 기존 FreeMarker 분기(entryRegType) 와 동일한 레이아웃 규칙을 따른다.
 *
 * @author nichefish
 */

function coerceChapterList(model: Record<string, any>): Record<string, any>[] {
    const raw = model.chapterList;
    return Array.isArray(raw) ? raw : [];
}

const JournalEntryRegModalHeader = {
    name: "JournalEntryRegModalHeader",
    props: {
        contentType: { type: String, required: true },
        model: { type: Object, required: true },
    },
    computed: {
        isDiary(): boolean {
            return this.contentType === "JOURNAL_DIARY";
        },
        isDream(): boolean {
            return this.contentType === "JOURNAL_DREAM";
        },
        isNote(): boolean {
            return this.contentType === "JOURNAL_NOTE";
        },
        isEdit(): boolean {
            const id = (this.model as Record<string, any>)?.id;
            return id != null && String(id).length > 0;
        },
        chapterList(): Record<string, any>[] {
            return coerceChapterList(this.model as Record<string, any>);
        },
        /** Dream: HBS {{^ifYn elseDreamYn}} 로 정렬 행 숨김 — 체크 시(대체 꿈) 숨김. */
        showDreamSortRow(): boolean {
            if (!this.isDream || !this.isEdit) return false;
            const yn = (this.model as Record<string, any>)?.elseDreamYn;
            const checked = yn === true || yn === "Y" || yn === "y" || yn === 1;
            return !checked;
        },
        sortTooltipKey(): string {
            return this.isDream
                ? "bs.tooltip.journal.sort-order.in-dream-chapter"
                : "bs.tooltip.journal.sort-order.in-day";
        },
        sortMaxLength(): string {
            return this.isDream ? "2" : "3";
        },
        showSortCol(): boolean {
            if (!this.isEdit) return false;
            if (this.isDream) return this.showDreamSortRow;
            return true;
        },
    },
    methods: {
        t(key: string): string {
            return this.$t ? this.$t(key) : key;
        },
        chapterOptionLabel(chapter: Record<string, any>): string {
            let prefix = "";
            if (chapter.categoryName) prefix = `[${chapter.categoryName}] `;
            else if (chapter.categoryCode) prefix = `[${chapter.categoryCode}] `;
            return `${prefix}${chapter.sortOrder ?? ""} ${chapter.title ?? ""}`.trim();
        },
    },
    template: `
    <div class="journal-entry-reg-header-vue-root">
        <input type="hidden" name="id" :value="(model.id != null ? model.id : '')">
        <template v-if="isDiary">
            <input type="hidden" name="collapsedYn" :value="model.collapsedYn ?? ''">
            <input type="hidden" name="imprtcYn" :value="model.imprtcYn ?? ''">
        </template>

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

            <!-- DIARY: chapter col-lg-2 + category col-lg-2 + title col-lg-7/8 -->
            <template v-if="isDiary">
                <div class="col-lg-2">
                    <select name="journalChapterId" id="journalChapterId" class="form-select form-select-solid"
                            :value="model.journalChapterId ?? ''">
                        <option
                            v-for="ch in chapterList"
                            :key="'ch-' + ch.id"
                            :value="ch.id"
                        >{{ chapterOptionLabel(ch) }}</option>
                    </select>
                </div>
                <div class="col-lg-2">
                    <select name="ctgrCd" id="ctgrCd" class="form-select form-select-solid" :value="model.ctgrCd ?? ''">
                        <option value="">{{ t('txt.journal.select.post-ctgr') }}</option>
                    </select>
                </div>
                <div :class="isEdit ? 'col-lg-7' : 'col-lg-8'">
                    <input type="text" name="title" id="title" class="form-control"
                           :value="model.title ?? ''" :placeholder="t('txt.title')" maxlength="100" />
                    <div id="title_validate_span"></div>
                </div>
            </template>

            <!-- DREAM: hidden chapter + title col-lg-11/12 -->
            <template v-else-if="isDream">
                <input type="hidden" name="journalChapterId" id="journalChapterId" :value="model.journalChapterId ?? ''" />
                <div :class="isEdit ? 'col-lg-11' : 'col-lg-12'">
                    <input type="text" name="title" id="title" class="form-control"
                           :value="model.title ?? ''" :placeholder="t('txt.title')" maxlength="100" />
                    <div id="title_validate_span"></div>
                </div>
            </template>

            <!-- NOTE: chapter col-lg-1 + title col-lg-10/11 (FTL 기본값) -->
            <template v-else-if="isNote">
                <div class="col-lg-1">
                    <select name="journalChapterId" id="journalChapterId" class="form-select form-select-solid"
                            :value="model.journalChapterId ?? ''">
                        <option
                            v-for="ch in chapterList"
                            :key="'ch-' + ch.id"
                            :value="ch.id"
                        >{{ chapterOptionLabel(ch) }}</option>
                    </select>
                </div>
                <div :class="isEdit ? 'col-lg-10' : 'col-lg-11'">
                    <input type="text" name="title" id="title" class="form-control"
                           :value="model.title ?? ''" :placeholder="t('txt.title')" maxlength="100" />
                    <div id="title_validate_span"></div>
                </div>
            </template>

            <div v-if="showSortCol" class="col-1 d-flex ps-0">
                <div class="d-flex-center p-2 fw-bold fs-5 text-gray-600">#</div>
                <input type="number" class="form-control form-control-sm" name="sortOrder" id="sortOrder"
                       min="1" max="99" :value="model.sortOrder ?? ''"
                       :placeholder="t('txt.journal.field.sort-order.placeholder')"
                       :maxlength="sortMaxLength"
                       data-bs-toggle="tooltip" data-bs-placement="top"
                       :title="t(sortTooltipKey)" />
            </div>
        </div>
    </div>
    `,
};

export default JournalEntryRegModalHeader;
