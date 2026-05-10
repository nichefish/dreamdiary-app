/**
 * JournalChapterItem.ts
 * Vue가 소유하는 저널 챕터 셸과 엔트리/액션 컨트롤.
 */

import JournalEntryItem from "../../entry/components/JournalEntryItem.js";
import journalChapterCrudService from "../services/journalChapterCrudService.js";

const JournalChapterItem = {
    name: "JournalChapterItem",
    components: {
        JournalEntryItem,
    },
    props: {
        chapter: { type: Object, required: true },
    },
    methods: {
        t(key: string): string {
            return this.$t ? this.$t(key) : key;
        },
        hasState(stateKey: string): boolean {
            const states = this.chapter?.state?.list;
            if (!Array.isArray(states)) return false;
            return states.some((state: Record<string, any>): boolean => state?.stateKey === stateKey);
        },
        chapterTypeLabel(): string {
            if (this.chapter?.chapterType === "DREAM") return this.t("txt.dream");
            if (this.chapter?.chapterType === "NOTE") return this.t("txt.journal.note");
            return this.t("txt.diary");
        },
        chapterIconClass(): string {
            if (this.chapter?.chapterType === "DREAM") return "bi-moon-stars";
            if (this.chapter?.chapterType === "NOTE") return "bi-journal-text";
            return "bi-book";
        },
        entryContentType(): "JOURNAL_DIARY" | "JOURNAL_DREAM" {
            return this.chapter?.chapterType === "DREAM" ? "JOURNAL_DREAM" : "JOURNAL_DIARY";
        },
        openEntryRegistModal(): void {
            dF.JournalEntry.get(this.entryContentType()).regModal({
                journalDayId: this.chapter.journalDayId,
                journalChapterId: this.chapter.id,
                stdrdDt: this.chapter.stdrdDt,
                journalDateWeekDay: this.chapter.journalDateWeekDay,
            });
        },
        toggleChapter(): void { journalChapterCrudService.toggleChapter(this.chapter.id); },
        copyChapter(): void { journalChapterCrudService.copyChapter(this.chapter.id); },
        exportChapter(): void { journalChapterCrudService.exportTxt(this.chapter.id); },
        /**
         * 수정 모달 진입.
         * 변경 전: dF.JournalChapter.modifyModal(id, stdrdDt) — 내부에서 ajax get + cF.handlebars.modal 진입.
         * 변경 후(Phase B): ajax get 후 window.JournalChapterRegistVueApp.open(rsltObj) 단일 큐 진입.
         */
        editChapter(): void {
            if (isNaN(Number(this.chapter.id))) return;
            document.querySelectorAll(".modal.show").forEach((modal: Element): void => {
                ($ as any)(modal).modal("hide");
            });
            const url: string = cF.util.bindUrl(Url.JOURNAL_CHAPTER, { id: this.chapter.id });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const bridge = window.JournalChapterRegistVueApp;
                if (!bridge) {
                    console.error("[JournalChapterItem] window.JournalChapterRegistVueApp not available.");
                    return;
                }
                if (bridge.mounted === true && typeof bridge.open === "function") {
                    bridge.open(res.rsltObj as Record<string, any>);
                } else {
                    bridge.pendingPayload = res.rsltObj as Record<string, any>;
                }
            });
        },
        toggleCollapsedState(): void { journalChapterCrudService.collapseAjax(this.chapter.id); },
        deleteChapter(): void { journalChapterCrudService.deleteAjax(this.chapter.id); },
        entryList(): Record<string, any>[] { return Array.isArray(this.chapter?.journalEntryList) ? this.chapter.journalEntryList : []; },
        tagList(): Record<string, any>[] { return Array.isArray(this.chapter?.tag?.list) ? this.chapter.tag.list : []; },
        selectTag(tag: Record<string, any>): void { dF.JournalEntryTag.get("JOURNAL_DIARY").select(tag.tagId, tag.name); },
        tooltip(labelKey: string, actionKey: string): string { return [this.t(labelKey), this.t(actionKey)].join(" "); },
    },
    template: `
    <div>
        <div class="d-flex align-items-center mt-2">
            <div class="d-flex-align-center text-gray-700 fs-6 ps-1 ps-md-5 me-5 fw-bolder">
                <span class="me-2">
                    {{ chapterTypeLabel() }}
                    <template v-if="chapter.categoryCode">:</template>
                    <template v-if="chapter.categoryCode">
                        <span style="color:#287D94;">{{ chapter.categoryName }}</span>
                        <span class="text-muted fs-8 me-1">{{ chapter.categoryCode }}</span>
                    </template>
                </span>
                <i class="bi fs-4" :class="chapterIconClass()"></i>
            </div>
            <div class="col-3 d-none d-md-flex align-items-center gap-2">
                <button type="button" class="btn btn-sm btn-light-primary btn-outlined ps-4 pe-3 py-2 cursor-pointer" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="tooltip(entryContentType() === 'JOURNAL_DREAM' ? 'txt.journal.dream' : 'txt.journal.diary', 'bs.tooltip.modal.reg')" @click="openEntryRegistModal">
                    <i class="bi fs-4 pe-1" :class="entryContentType() === 'JOURNAL_DREAM' ? 'bi-moon-stars' : 'bi-book'"></i>
                    {{ entryContentType() === 'JOURNAL_DREAM' ? t('txt.journal.dream.reg') : t('txt.journal.diary.reg') }}
                </button>
                <button type="button" class="btn btn-sm btn-light-primary btn-outlined ms-2 px-3 cursor-pointer" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="t('bs.tooltip.copy')" @click="copyChapter"><i class="bi bi-copy p-0"></i></button>
                <button type="button" class="btn btn-sm btn-outline btn-light-primary ps-3 pe-2" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="t('bs.tooltip.export-txt')" @click="exportChapter"><i class="fas fa-download"></i></button>
                <div class="me-0 d-flex align-items-center">
                    <button class="btn btn-sm btn-icon btn-bg-light btn-active-color-primary" data-kt-menu-trigger="click" data-kt-menu-placement="bottom-end" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="t('bs.tooltip.context.menu.show')">
                        <i class="ki-solid ki-dots-horizontal fs-2x"></i>
                    </button>
                    <div class="menu menu-sub menu-sub-dropdown menu-column menu-rounded menu-gray-800 menu-state-bg-light-primary fw-semibold w-200px py-3" data-kt-menu="true">
                        <div class="menu-item px-3"><div class="menu-content text-muted pb-2 px-3 fs-7 text-uppercase">{{ t("txt.journal.chapter") }}</div></div>
                        <div class="menu-item px-3 my-1 cursor-pointer" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="tooltip('txt.journal.chapter', 'bs.tooltip.modal.mdf')"><div class="menu-link flex-stack px-3" @click="editChapter">{{ t("txt.comm.edit") }}<i class="bi bi-pencil-square fs-8"></i></div></div>
                        <div class="menu-item px-3" data-kt-menu-trigger="hover" data-kt-menu-placement="right-end">
                            <a href="#" class="menu-link px-3"><span class="menu-title">{{ t("txt.comm.status") }}</span><span class="menu-arrow"></span></a>
                            <div class="menu-sub menu-sub-dropdown w-175px py-4">
                                <div class="menu-item px-3"><div class="menu-content px-3"><label class="form-check form-switch form-check-custom form-check-solid cursor-pointer" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="t('txt.status.collapsed')"><input class="form-check-input w-30px h-20px cursor-pointer chapter-context-collapsed-check" type="checkbox" value="1" :checked="hasState('COLLAPSED')" @click="toggleCollapsedState"><span class="form-check-label text-muted fs-7">{{ t("txt.status.collapsed") }}</span></label></div></div>
                            </div>
                        </div>
                        <div class="separator my-2"></div>
                        <div class="menu-item px-3 my-1 cursor-pointer" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="tooltip('txt.journal.chapter', 'bs.tooltip.del')"><div class="menu-link flex-stack px-3 text-danger" @click="deleteChapter">{{ t("txt.comm.del") }}<i class="bi bi-trash text-danger p-0 fs-8"></i></div></div>
                    </div>
                </div>
                <button type="button" class="btn btn-sm btn-secondary ms-2 px-3 toggle-chapter-btn" @click="toggleChapter"><i class="bi pe-0" :class="hasState('COLLAPSED') ? 'bi-arrows-expand' : 'bi-arrows-collapse'" :id="'chapter-toggle-icon-' + chapter.id"></i></button>
            </div>
        </div>
        <div class="journal-chapter-item" :data-id="chapter.id" :data-stdrd-dt="chapter.stdrdDt" :data-yy="chapter.yy" :data-mnth="chapter.mnth" :data-collapsed="hasState('COLLAPSED')">
            <div class="journal-chapter-content" :class="{ collapsed: hasState('COLLAPSED') }">
                <JournalEntryItem v-for="entry in entryList()" :key="'chapter-entry-' + entry.id" :entry="entry" :content-type="entryContentType()" />
            </div>
            <div class="tags journal-chapter-tags ms-2 mt-3" :class="{ 'd-none': !hasState('COLLAPSED') }">
                <i class="bi bi-tag"></i>
                <span v-for="tag in tagList()" :key="'chapter-tag-' + tag.tagId + ':' + tag.name" class="text-muted cursor-pointer pe-1" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="t('view.tag.content-list')" @click="selectTag(tag)">
                    # <span class="border-bottom text-primary fw-lighter opacity-hover"><span v-if="tag.ctgr" class="fs-7 text-noti">[{{ tag.ctgr }}]</span>{{ tag.name }}</span>
                </span>
            </div>
        </div>
    </div>
    `,
};

export default JournalChapterItem;
