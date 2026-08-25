<template>
  <!--begin::저장된 저널 엔트리/리플렉션 읽기 전용 팝업-->
  <div class="p-5">
    <div class="card post">
      <div class="card-body">
        <div v-if="loading" class="d-flex justify-content-center py-10">
          <span class="spinner-border text-primary" role="status"></span>
        </div>
        <div v-else-if="errorText" class="text-danger py-10 text-center">
          {{ errorText }}
        </div>
        <template v-else-if="entry">
          <div class="mb-0">
            <div class="d-flex flex-wrap align-items-baseline gap-3 mb-2">
              <span class="fs-3 fw-bold text-gray-900">
                <i :class="['bi me-2 text-primary', typeIconClass]"></i>{{ typeLabel }}
              </span>
              <span v-if="entry.sortOrder != null" class="text-muted fs-6"># {{ entry.sortOrder }}</span>
              <span v-if="entry.stdrdDt" class="text-muted fs-6">
                {{ entry.stdrdDt }} ({{ getWeekDayStr(entry.stdrdDt, t) }})
              </span>
            </div>
            <div v-if="entry.prefix || entry.title" class="d-flex align-items-center flex-wrap fs-5 fw-semibold text-gray-800 mb-2">
              <span
                v-if="entry.prefix"
                class="badge me-2 fs-8"
                :style="{ borderColor: entry.prefix.color || '', color: entry.prefix.color || '' }"
              >{{ entry.prefix.name }}</span>
              <span v-if="entry.title">{{ entry.title }}</span>
            </div>
            <div v-if="isDream && entry.dreamerName" class="text-muted fs-7">
              {{ t("journal.entry.dreamer.label") }}: {{ entry.dreamerName }}
            </div>
          </div>

          <div class="separator separator-dashed border-gray-300 my-8"></div>

          <div class="px-2 py-1 pb-4 journal-entry-view-page__body">
            <div :class="itemClass">
              <div class="col-1 py-3 d-none d-md-flex border-2 border-gray-300 border-end ps-5 me-4 h-75" style="width:85px;">
                &nbsp;
              </div>
              <div class="col">
                <div :class="contentClass">
                  <div
                    v-if="entry.markdownContent"
                    class="journal-content p-2"
                    v-html="entry.markdownContent"
                  ></div>
                  <div v-else class="text-muted fs-7 fst-italic">
                    {{ t("journal.entry.view.empty") }}
                  </div>
                </div>
              </div>
              <div class="col-1 py-3 d-none d-md-flex w-50px ps-2">&nbsp;</div>
            </div>
          </div>

          <div v-if="tagList.length > 0" class="d-flex flex-wrap gap-1 mt-1 px-2">
            <span v-for="tag in tagList" :key="tag.tagId" class="text-muted pe-1">
              #<span class="border-bottom text-primary fw-lighter">
                <span v-if="tag.ctgr" class="fs-7 text-noti">[{{ tag.ctgr }}]</span>{{ tag.name }}
              </span>
            </span>
          </div>
        </template>
      </div>
      <div class="card-footer">
        <div class="d-flex justify-content-end">
          <button type="button" class="btn btn-sm btn-light" @click="closePopup">
            {{ t("common.close") }}
          </button>
        </div>
      </div>
    </div>
  </div>
  <!--end::저장된 저널 엔트리/리플렉션 읽기 전용 팝업-->
</template>

<script setup lang="ts">
/**
 * 저장된 저널 엔트리·리플렉션 한 건을 ID로 조회해 읽기 전용 새 창에 표시한다.
 * 상세 API가 실제 contentType을 판별하므로 두 유형은 같은 조회·렌더링 경로를 사용한다.
 */
import axios from "axios";
import { computed, ref, watch } from "vue";
import { useRoute } from "vue-router";
import type { JournalEntryDto } from "@/features/journal/stores/journal";
import { getWeekDayStr } from "@/features/journal/utils/journalDate";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

const route = useRoute();
const { t } = useLocaleStore();
const entry = ref<JournalEntryDto | null>(null);
const loading = ref(false);
const errorText = ref("");

const entryId = computed(() => {
  const raw = Array.isArray(route.query.entryId) ? route.query.entryId[0] : route.query.entryId;
  const parsed = Number(raw);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null;
});
const isReflection = computed(() => entry.value?.contentType === "JOURNAL_REFLECTION");
const isDream = computed(() => entry.value?.contentType === "JOURNAL_DREAM");
const isNote = computed(() => entry.value?.prefixContentType === "JOURNAL_NOTE");
const typeLabel = computed(() => {
  if (isReflection.value) return t("journal.reflection.label");
  if (isDream.value) return t("common.dream");
  if (isNote.value) return t("journal.note");
  return t("common.diary");
});
const typeIconClass = computed(() => {
  if (isReflection.value) return "bi-chat-quote";
  if (isDream.value) return "bi-moon-stars";
  if (isNote.value) return "bi-journal";
  return "bi-journal-text";
});
const itemClass = computed(() => {
  if (isReflection.value) return "journal-reflection-embed";
  if (isDream.value) return "journal-dream-item";
  if (isNote.value) return "journal-note-item";
  return "journal-diary-item";
});
const contentClass = computed(() => {
  if (isReflection.value) return "journal-reflection-content";
  if (isDream.value) return "journal-dream-content";
  if (isNote.value) return "journal-note-content";
  return "journal-diary-content";
});
const tagList = computed(() => entry.value?.tag?.list ?? []);

/** route의 엔트리 ID를 현재 사용자 소유 상세 API로 조회한다. */
async function loadEntry(): Promise<void> {
  entry.value = null;
  errorText.value = "";
  if (entryId.value == null) {
    console.warn("[JournalEntryViewPage] invalid or missing entry id", { raw: route.query.entryId });
    errorText.value = t("journal.entry.view.load.failure");
    return;
  }

  loading.value = true;
  try {
    const response = await axios.get(`/api/journal/entry/${entryId.value}`);
    const retrieved = response.data?.rsltObj as JournalEntryDto | undefined;
    if (!retrieved?.id) {
      console.warn("[JournalEntryViewPage] entry detail missing", { entryId: entryId.value });
      errorText.value = t("journal.entry.view.load.failure");
      return;
    }
    entry.value = retrieved;
    console.info("[JournalEntryViewPage] entry detail loaded", {
      entryId: retrieved.id,
      contentType: retrieved.contentType,
    });
  } catch (error: unknown) {
    console.error("[JournalEntryViewPage] entry detail load failed", { entryId: entryId.value, error });
    errorText.value = t("journal.entry.view.load.failure");
  } finally {
    loading.value = false;
  }
}

function closePopup(): void {
  window.close();
}

watch(entryId, () => {
  void loadEntry();
}, { immediate: true });
</script>

<style lang="scss" scoped>
.journal-entry-view-page__body {
  min-height: 160px;
}
</style>
