<template>
  <!--begin::꿈 가상 섹션 (Phase 1: DB 챕터 분리 없이 UI 블록만)-->
  <div class="journal-dream-virtual-section">
    <!--begin::섹션 헤더-->
    <div class="d-flex align-items-center mt-2">
      <div class="d-flex-align-center journal-dream-section-header fs-6 ps-1 ps-md-5 me-5 fw-bolder">
        <span class="me-2">{{ section.title }}</span>
        <i class="bi bi-moon-stars fs-4"></i>
      </div>
      <div v-if="showActions" class="col-3 d-none d-md-flex align-items-center gap-2">
        <button
          v-if="isOwnDreamSection"
          type="button"
          class="btn btn-sm btn-light-primary btn-outlined ps-4 pe-3 py-2 cursor-pointer"
          title="저널 꿈 등록"
          @click="$emit('open-regist', '')"
        >
          <i class="bi bi-moon-stars fs-4 pe-1"></i>
          저널 꿈 등록
        </button>
        <button
          v-if="showCopyExport"
          type="button"
          class="btn btn-sm btn-light-primary btn-outlined ms-2 px-3 cursor-pointer"
          title="복사"
          @click="$emit('copy-section', section.entries)"
        >
          <i class="bi bi-copy p-0"></i>
        </button>
        <button
          v-if="showCopyExport"
          type="button"
          class="btn btn-sm btn-outline btn-light-primary ps-3 pe-2"
          title="TXT보내기"
          @click="$emit('export-day')"
        >
          <i class="fas fa-download"></i>
        </button>
        <button
          type="button"
          class="btn btn-sm btn-secondary ms-2 px-3 toggle-chapter-btn"
          @click="$emit('toggle-collapse')"
        >
          <i class="bi pe-0" :class="collapsed ? 'bi-arrows-expand' : 'bi-arrows-collapse'"></i>
        </button>
      </div>
    </div>
    <!--end::섹션 헤더-->
    <!--begin::엔트리 목록-->
    <div class="journal-chapter-item">
      <div :class="['journal-chapter-content', { collapsed }]">
        <JournalEntryItem
          v-for="dream in section.entries"
          :key="'dream-' + section.sectionKey + '-' + dream.id"
          :entry="dream"
          :is-dream="true"
          :dom-id="entryDomId(dream.id)"
        />
      </div>
    </div>
    <!--end::엔트리 목록-->
  </div>
  <!--end::꿈 가상 섹션-->
</template>

<script setup lang="ts">
import JournalEntryItem from "../../entry/components/JournalEntryItem.vue";
import { computed } from "vue";
import type { JournalDreamSectionDto } from "@/utils/journalDream";
import type { JournalEntryDto } from "@/stores/journal";

const props = defineProps<{
  section: JournalDreamSectionDto;
  collapsed: boolean;
  /** 헤더 액션(등록·접기 등) 표시 */
  showActions?: boolean;
  /** 복사·TXT는 일자 단위이므로 첫(내 꿈) 섹션에만 */
  showCopyExport?: boolean;
  entryDomIdPrefix?: string;
}>();

/** 내 꿈 섹션만 저널 꿈 등록 버튼 표시 */
const isOwnDreamSection = computed(() => props.section.sectionKey === "own");

defineEmits<{
  (e: "open-regist", dreamerName: string): void;
  (e: "copy-section", entries: JournalEntryDto[]): void;
  (e: "export-day"): void;
  (e: "toggle-collapse"): void;
}>();

function entryDomId(id?: number | string): string | undefined {
  if (!id || !props.entryDomIdPrefix) return undefined;
  return `${props.entryDomIdPrefix}${id}`;
}
</script>
