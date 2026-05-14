<template>
  <!--begin::챕터-->
  <div class="mb-3">
    <!--begin::챕터 헤더-->
    <div class="d-flex align-items-center gap-2 ps-1 ps-md-5 py-2">
      <i class="bi fs-5" :class="iconClass"></i>
      <span class="fw-bold text-gray-700 fs-6">{{ typeLabel }}</span>
      <template v-if="chapter.categoryCode">
        <span class="text-separator mx-1">:</span>
        <span style="color:#287D94;" class="fw-semibold">{{ chapter.categoryName }}</span>
        <span class="text-muted fs-8">{{ chapter.categoryCode }}</span>
      </template>
      <!--begin::접힘 상태 배지-->
      <span v-if="isCollapsed" class="badge badge-light-secondary ms-auto">접힘</span>
      <!--end::접힘 상태 배지-->
    </div>
    <!--end::챕터 헤더-->

    <!--begin::엔트리 목록-->
    <template v-if="!isCollapsed">
      <JournalEntryItem
        v-for="entry in entryList"
        :key="entry.id"
        :entry="entry"
        :is-dream="chapter.chapterType === 'DREAM'"
      />
      <div v-if="entryList.length === 0" class="text-muted fs-8 ps-5 py-2">등록된 항목이 없습니다.</div>
    </template>
    <template v-else>
      <!--begin::접힘 시 태그 요약-->
      <div v-if="tagList.length > 0" class="d-flex flex-wrap gap-1 ps-5 py-1">
        <span
          v-for="tag in tagList"
          :key="tag.tagId"
          class="text-muted fs-8"
        >#<span v-if="tag.ctgr" class="text-noti fs-8">[{{ tag.ctgr }}]</span>{{ tag.name }}</span>
      </div>
    </template>
    <!--end::엔트리 목록-->
  </div>
  <!--end::챕터-->
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useJournalModalStore } from "@/stores/journalModal";
import type { JournalChapterDto } from "@/stores/journal";
import JournalEntryItem from "./JournalEntryItem.vue";

const props = defineProps<{
  chapter: JournalChapterDto;
}>();

const modalStore = useJournalModalStore();

/** DREAM 챕터 여부 (항목 등록 버튼 숨김) */
const isDreamChapter = computed(() => props.chapter.chapterType === "DREAM");

const isCollapsed = computed(() =>
  (props.chapter.state?.list ?? []).some((s) => s.stateKey === "COLLAPSED")
);

const iconClass = computed(() => {
  if (props.chapter.chapterType === "DREAM") return "bi-moon-stars";
  if (props.chapter.chapterType === "NOTE") return "bi-journal-text";
  return "bi-book";
});

const typeLabel = computed(() => {
  if (props.chapter.chapterType === "DREAM") return "꿈";
  if (props.chapter.chapterType === "NOTE") return "노트";
  return "일기";
});

const entryList = computed(() => props.chapter.journalEntryList ?? []);
const tagList = computed(() => props.chapter.tag?.list ?? []);

/** 챕터 수정 모달 열기 */
function openChapterMdf() {
  modalStore.openChapterReg({
    id: props.chapter.id,
    journalDayId: props.chapter.journalDayId,
    stdrdDt: props.chapter.stdrdDt,
    chapterType: props.chapter.chapterType,
    categoryCode: props.chapter.categoryCode,
    title: props.chapter.title,
    sortOrder: props.chapter.sortOrder,
  });
}

/** 일기 엔트리 신규 등록 모달 열기 */
function openEntryNew() {
  if (!props.chapter.journalDayId) return;
  modalStore.openEntryReg({
    contentType: props.chapter.chapterType === "NOTE" ? "JOURNAL_NOTE" : "JOURNAL_DIARY",
    journalDayId: props.chapter.journalDayId,
    journalChapterId: props.chapter.id,
    stdrdDt: props.chapter.stdrdDt,
    chapterList: [{ id: props.chapter.id, title: props.chapter.title, categoryCode: props.chapter.categoryCode, categoryName: props.chapter.categoryName, sortOrder: props.chapter.sortOrder }],
  });
}
</script>
