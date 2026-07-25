<template>
  <div v-if="store.detailModel" class="journal-thread-dtl-vue-root">
    <!--begin::헤더 (제목 + 작성자/일시)-->
    <div class="mb-0">
      <div class="d-flex align-items-center flex-wrap gap-2 mb-2">
        <span v-if="store.detailModel.categoryName" class="ctgr-span ctgr-gray">{{ store.detailModel.categoryName }}</span>
        <span class="fs-3 fw-bolder text-gray-900">{{ store.detailModel.title }}</span>
      </div>
      <div class="d-flex align-items-center flex-wrap gap-3 text-muted fs-7">
        <span v-if="store.detailModel.createdByNm"><i class="bi bi-person pe-1"></i>{{ store.detailModel.createdByNm }}</span>
        <span v-if="store.detailModel.createdDt"><i class="bi bi-clock pe-1"></i>{{ store.detailModel.createdDt }}</span>
      </div>
    </div>
    <!--end::헤더-->

    <div class="separator separator-dashed border-gray-300 my-8"></div>

    <!--begin::본문-->
    <div
      class="fs-4 fw-normal text-gray-800 px-5 py-1 pb-6 min-h-150px"
      v-html="store.detailModel.markdownContent || store.detailModel.content || ''"
    ></div>
    <!--end::본문-->

    <!--begin::소속 엔트리 태그 (백엔드 집계)-->
    <div v-if="hasDetailTags" class="mt-4">
      <i class="bi bi-tags me-1 text-gray-700"></i>
      <span class="fs-8 fw-semibold text-gray-600 me-2">{{ t("journal.thread.entry-tags.title") }}</span>
      <span
        v-for="tag in store.detailModel.tag?.list"
        :key="'thread-dtl-tag-' + String(tag.tagId)"
        class="text-muted pe-1"
      >
        <span v-if="tag.ctgr" class="fs-7 text-noti">[{{ tag.ctgr }}]</span>
        #<span class="border-bottom text-primary fw-lighter opacity-hover">{{ tag.name }}</span>
      </span>
    </div>
    <!--end::소속 엔트리 태그-->

    <!--begin::소속 엔트리 목록-->
    <div class="separator separator-dashed border-gray-200 my-6"></div>
    <div class="d-flex align-items-center gap-2 mb-3 px-5">
      <i class="bi bi-diagram-3 text-gray-700"></i>
      <span class="fs-6 fw-bold text-gray-800">{{ t("journal.thread.entries.title") }}</span>
      <span v-if="store.detailEntries.length > 0" class="badge badge-light-primary">{{ store.detailEntries.length }}</span>
    </div>
    <div v-if="store.detailEntriesLoading" class="text-muted fs-7 px-5 py-2">{{ t("common.loading") }}</div>
    <div v-else-if="store.detailEntries.length === 0" class="text-muted fs-7 px-5 py-2">{{ t("journal.thread.entries.empty") }}</div>
    <div v-else class="px-3">
      <template v-for="group in entryGroups" :key="'thread-entry-group-' + (group.stdrdDt || 'nodate')">
        <!--begin::일자 헤더 — 저널 일자/연간 상세와 동일한 journal-day-header (날짜 + 요일)-->
        <div v-if="group.stdrdDt" class="journal-day-header mt-4 mb-1" :data-date="group.stdrdDt">
          <div class="d-flex flex-wrap align-items-center fs-5 fw-bold">
            <i class="bi bi-calendar3 fs-6 me-1"></i>
            <span>{{ group.stdrdDt }}</span>
            <span class="fs-8 text-gray-600">({{ getWeekDayStr(group.stdrdDt, t) }})</span>
          </div>
        </div>
        <!--end::일자 헤더-->
        <JournalEntryItem
          v-for="entry in group.entries"
          :key="'thread-entry-' + entry.id"
          :entry="entry"
          :is-dream="entry.contentType === 'JOURNAL_DREAM'"
          :disable-resolved-collapse="true"
        />
      </template>
    </div>
    <!--end::소속 엔트리 목록-->

    <!--begin::댓글 영역-->
    <div class="separator separator-dashed border-gray-200 my-6"></div>
    <div class="d-flex align-items-center justify-content-between mb-3">
      <span class="fs-6 fw-bold text-gray-800">{{ t("comment.modal.title") }}</span>
      <div v-if="store.detailModel.id" class="d-flex gap-1">
        <button
          type="button"
          class="btn btn-xs btn-icon btn-bg-light btn-active-color-primary"
          :title="t('comment.register')"
          @click="openCommentRegist"
        >
          <i class="bi bi-chat-dots fs-8"></i>
        </button>
        <button
          v-if="commentCount > 0"
          type="button"
          class="btn btn-xs btn-light-primary"
          :title="t('journal.thread.comments.tooltip')"
          @click="openCommentList"
        >
          <i class="bi bi-chat-left-text fs-8 me-1"></i>
          {{ commentCount }}
        </button>
      </div>
    </div>
    <div v-if="commentList.length === 0" class="text-muted fs-7 py-2">{{ t("comment.modal.empty") }}</div>
    <div v-else class="d-flex flex-column gap-2">
      <div
        v-for="cmt in commentList"
        :key="cmt.id"
        class="fs-8 text-muted ps-2 border-start border-2 border-gray-300"
        v-html="cmt.markdownContent || cmt.content || ''"
      ></div>
    </div>
    <!--end::댓글 영역-->
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useAttachableModalStore } from "@/features/attachable/stores/attachableModal";
import JournalEntryItem from "@/features/journal/entry/components/JournalEntryItem.vue";
import type { JournalEntryDto } from "@/features/journal/stores/journal";
import { useJournalThreadStore } from "@/features/journal/stores/journalThread";
import { getWeekDayStr } from "@/features/journal/utils/journalDate";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

const store = useJournalThreadStore();
const attachableStore = useAttachableModalStore();
const { t } = useLocaleStore();

/**
 * 일자 그룹: 소속 엔트리를 stdrdDt 별로 묶은 렌더 단위.
 * stdrdDt 가 빈 문자열이면 일자 미상 그룹(헤더 없이 카드만 렌더)이다.
 */
interface ThreadEntryGroup {
  stdrdDt: string;
  entries: JournalEntryDto[];
}

/**
 * 소속 엔트리를 stdrdDt 별로 그룹핑한다.
 * <p>
 * 백엔드가 일자 → 원본 엔트리 sortOrder → ID 오름차순으로 내려주므로 first-seen 순서가 곧 표시 순서다.
 * 저널 일자·연간 상세와 동일하게 그룹마다 journal-day-header 를 얹어 시각을 통일한다.
 *
 * @param entries 백엔드가 정렬해 내려준 소속 엔트리 목록
 * @return 일자별 그룹 목록 (입력 순서 보존)
 */
function groupEntriesByDate(entries: JournalEntryDto[]): ThreadEntryGroup[] {
  const groups: ThreadEntryGroup[] = [];
  const groupMap = new Map<string, ThreadEntryGroup>();
  entries.forEach((entry) => {
    const stdrdDt = entry.stdrdDt ?? "";
    let group = groupMap.get(stdrdDt);
    if (!group) {
      group = { stdrdDt, entries: [] };
      groupMap.set(stdrdDt, group);
      groups.push(group);
    }
    group.entries.push(entry);
  });
  return groups;
}

const entryGroups = computed(() => groupEntriesByDate(store.detailEntries));

const hasDetailTags = computed(() =>
  Array.isArray(store.detailModel?.tag?.list) && store.detailModel!.tag!.list!.length > 0
);

const commentList = computed(() => store.detailModel?.comment?.list ?? []);

const commentCount = computed(() => {
  const cnt = store.detailModel?.comment?.cnt;
  if (typeof cnt === "number") return cnt;
  return commentList.value.length;
});

const threadContentType = computed(
  () => store.detailModel?.contentType ?? "JOURNAL_THREAD"
);

function openCommentRegist(): void {
  const id = store.detailModel?.id;
  if (!id) return;
  void attachableStore.openCommentRegist(id, threadContentType.value);
}

function openCommentList(): void {
  const id = store.detailModel?.id;
  if (!id) return;
  void attachableStore.openCommentList(id, threadContentType.value);
}
</script>
