<template>
  <!--begin::저널 엔트리 읽기 전용 모달 (채팅 RAG 원문 등)-->
  <div
    ref="modalEl"
    class="modal fade"
    id="journal_entry_view_modal"
    tabindex="-1"
    aria-hidden="true"
    data-bs-backdrop="static"
    data-bs-keyboard="false"
  >
    <div class="modal-dialog modal-xl modal-dialog-scrollable">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">{{ modalTitle }}</h5>
          <button
            type="button"
            class="btn-close"
            :title="t('common.close')"
            @click="close"
          ></button>
        </div>

        <div class="modal-body modal-mbl-body my-5 journal-entry-view-modal__body">
          <div v-if="modalStore.entryViewLoading" class="d-flex justify-content-center py-10">
            <span class="spinner-border text-primary" role="status"></span>
          </div>

          <template v-else-if="entry">
            <div class="row d-flex mb-5">
              <div class="col-2">
                <span class="text-gray-700 fs-6 fw-bolder">{{ t('journal.day.field.date') }}</span>
              </div>
              <div class="col-10 fs-6">
                <i class="bi bi-calendar3"></i>
                {{ entry.stdrdDt ?? '' }}
                <span v-if="entry.stdrdDt" class="fs-8 text-gray-600">
                  ({{ getWeekDayStr(entry.stdrdDt, t) }})
                </span>
                <span v-if="contentKindLabel" class="badge badge-light-primary ms-2">
                  {{ contentKindLabel }}
                </span>
              </div>
            </div>

            <div v-if="entry.prefix || entry.title" class="mb-4">
              <div class="text-gray-700 fs-6 fw-bolder mb-2">{{ t('common.title') }}</div>
              <div class="d-flex align-items-center flex-wrap fs-5 fw-bold">
                <span
                  v-if="entry.prefix"
                  class="badge me-2 fs-8"
                  :style="{ borderColor: entry.prefix.color || '', color: entry.prefix.color || '' }"
                >{{ entry.prefix.name }}</span>
                <span v-if="entry.title">{{ entry.title }}</span>
              </div>
            </div>

            <div v-if="isDream && entry.elseDreamerNm" class="mb-4">
              <div class="text-gray-700 fs-6 fw-bolder mb-2">{{ t('journal.entry.dreamer.label') }}</div>
              <div class="fs-6">{{ entry.elseDreamerNm }}</div>
            </div>

            <div class="mb-4">
              <div
                v-if="entry.markdownContent"
                class="journal-content p-2"
                :class="contentClass"
                v-html="entry.markdownContent"
              ></div>
              <div v-else class="text-muted fs-7 fst-italic">
                {{ t('journal.entry.view.empty') }}
              </div>
            </div>

            <div v-if="tagList.length > 0" class="d-flex flex-wrap gap-1 mt-1">
              <span
                v-for="tag in tagList"
                :key="tag.tagId"
                class="text-muted pe-1"
              >
                #<span class="border-bottom text-primary fw-lighter">
                  <span v-if="tag.ctgr" class="fs-7 text-noti">[{{ tag.ctgr }}]</span>{{ tag.name }}
                </span>
              </span>
            </div>
          </template>
        </div>

        <div class="modal-footer">
          <button type="button" class="btn btn-light" @click="close">
            {{ t('common.close') }}
          </button>
                    <button
            v-if="canEdit"
            type="button"
            class="btn btn-primary"
            :disabled="!entry?.id || modalStore.entryViewLoading"
            @click="openModify"
          >
            {{ t('common.edit') }}
          </button>
        </div>
      </div>
    </div>
  </div>
  <!--end::저널 엔트리 읽기 전용 모달-->
</template>

<script setup lang="ts">
/**
 * JournalEntryViewModal.vue
 * 저널 엔트리 읽기 전용 모달. 채팅 RAG 출처 딥링크 등에서 수정 폼 없이 원문을 보여 준다.
 * 본문은 목록과 동일하게 markdownContent HTML(`journal-content`)을 렌더한다.
 */
import { computed, onMounted, ref, watch } from "vue";
import { Modal } from "bootstrap";
import { useJournalModalStore } from "@/features/journal/stores/journalModal";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { getWeekDayStr } from "@/features/journal/utils/journalDate";
import { isResolvedYn } from "@/features/journal/utils/journalDayResolved";
import { swalAlert } from "@/shared/utils/swal";

const modalStore = useJournalModalStore();
const { t } = useLocaleStore();
const modalEl = ref<HTMLElement | null>(null);
let bsModal: InstanceType<typeof Modal> | null = null;

const entry = computed(() => modalStore.entryViewModel);

const isDream = computed(() => {
  const ct = entry.value?.contentType;
  return ct === "JOURNAL_DREAM" || ct === "DREAM";
});

const isNote = computed(() => {
  const ct = entry.value?.contentType;
  return ct === "JOURNAL_NOTE" || ct === "NOTE";
});

const isDiary = computed(() => {
  const ct = entry.value?.contentType;
  return ct === "JOURNAL_DIARY" || ct === "DIARY";
});

/** 모달 제목 — 유형별 읽기 전용 카탈로그 */
const modalTitle = computed(() => {
  if (isDiary.value) return t("journal.entry.view.modal.diary.title");
  if (isDream.value) return t("journal.entry.view.modal.dream.title");
  if (isNote.value) return t("journal.entry.view.modal.note.title");
  return t("journal.entry.view.modal.default.title");
});

const contentKindLabel = computed(() => {
  if (isDiary.value) return t("common.diary");
  if (isDream.value) return t("common.dream");
  if (isNote.value) return t("journal.chapter.type.note");
  return "";
});

const contentClass = computed(() => {
  if (isDream.value) return "journal-dream-content";
  if (isNote.value) return "journal-note-content";
  return "journal-diary-content";
});

const tagList = computed(() => entry.value?.tag?.list ?? []);

/** NOTE 포함 비꿈 유형은 일기 축 완결 플래그로 수정 가능 여부를 판단한다. */
const canEdit = computed(() => {
  const e = entry.value;
  if (!e) return false;
  if (isDream.value) return !isResolvedYn(e.dreamResolvedYn);
  return !isResolvedYn(e.diaryResolvedYn);
});

onMounted(() => {
  if (modalEl.value) {
    bsModal = new Modal(modalEl.value, { backdrop: "static", keyboard: false });
    modalEl.value.addEventListener("hidden.bs.modal", () => {
      modalStore.closeEntryView();
    });
  }
});

watch(
  () => modalStore.entryViewOpen,
  (isOpen) => {
    if (isOpen) bsModal?.show();
    else bsModal?.hide();
  }
);

function close() {
  modalStore.closeEntryView();
}

function openModify() {
  if (!canEdit.value) {
    void swalAlert(
      t(isDream.value ? "journal.day.dream-resolved-locked" : "journal.day.diary-resolved-locked"),
    );
    return;
  }
  void modalStore.openEntryModifyFromView();
}
</script>

<style lang="scss" scoped>
.journal-entry-view-modal__body {
  min-height: 160px;
}
</style>
