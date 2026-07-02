<template>
  <div id="journal_tag_header" class="mb-6 ms-4 w-100">
    <template v-for="row in rows" :key="row.id">
      <div :id="row.id" class="row align-items-center mb-4 ms-4 min-h-42px">
        <div class="journal-tag-header__label col-auto d-none d-md-flex ms-4 me-6 text-center fs-6">
          <b>{{ row.label }} :</b>
        </div>
        <div class="col flex-grow-1">
          <span v-if="store.tagCloudLoading" class="text-muted fs-7">{{ t("common.loading") }}...</span>
          <span v-else-if="row.tags.length === 0" class="text-muted fs-7">-</span>
          <span v-else class="d-flex flex-wrap align-items-center">
            <button
              v-for="tag in row.tags"
              :key="`${row.id}-${String(tag.id)}`"
              type="button"
              class="btn btn-link py-2 me-3 px-0 cursor-pointer opacity-hover text-decoration-none d-inline-flex align-items-center"
              :title="row.tooltip"
              @click.stop="openTagContextMenu($event, tag, row.contentType)"
            >
              <span :class="[tag.tagClass, tag.textClass]" class="d-inline-flex align-items-center">
                <span v-if="tag.ctgr" class="fs-7 text-noti">[{{ tag.ctgr }}]</span>
                <span :class="`em_${tag.name}`">{{ tag.name }}</span>
              </span>
              <span class="fs-9 text-noti fw-normal tag-count">{{ tag.contentSize }}</span>
            </button>
          </span>
        </div>
      </div>
      <div v-if="row.hasSeparator" class="separator"></div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, watch } from "vue";
import { useJournalStore } from "@/features/journal/stores/journal";
import { useTagContextMenuStore } from "@/features/journal/stores/tagContextMenu";
import type { TagCloudItem } from "@/features/journal/stores/journal";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

const store = useJournalStore();
const tagContextMenuStore = useTagContextMenuStore();
const { t } = useLocaleStore();

const rows = computed(() => [
  {
    id: "journal_day_tag_header",
    label: t("journal.tag-cloud.day"),
    tags: store.tagCloud.dayTagList,
    tooltip: t("journal.tag-cloud.menu.tooltip"),
    hasSeparator: true,
    contentType: "JOURNAL_DAY",
  },
  {
    id: "journal_diary_tag_header",
    label: t("journal.tag-cloud.diary"),
    tags: store.tagCloud.diaryTagList,
    tooltip: t("journal.tag-cloud.menu.tooltip"),
    hasSeparator: true,
    contentType: "JOURNAL_DIARY",
  },
  {
    id: "journal_dream_tag_header",
    label: t("journal.tag-cloud.dream"),
    tags: store.tagCloud.dreamTagList,
    tooltip: t("journal.tag-cloud.menu.tooltip"),
    hasSeparator: false,
    contentType: "JOURNAL_DREAM",
  },
]);

watch([() => store.yy, () => store.mnth, () => store.weekStartDt, () => store.viewType], () => {
  void store.fetchTagCloud();
});

function openTagContextMenu(event: MouseEvent, tag: TagCloudItem, contentType: string) {
  tagContextMenuStore.open(event, {
    tagId: tag.id,
    name: tag.name,
    ctgr: tag.ctgr ?? "",
    contentType,
  });
}
</script>

<style scoped>
.journal-tag-header__label {
  width: 6.25rem;
  justify-content: center;
}

.tag-count {
  margin-left: 0.2em;
}
</style>
