<template>
  <!--begin::스레드 피커 모달-->
  <div
    ref="modalEl"
    class="modal fade"
    id="journal_thread_picker_modal"
    tabindex="-1"
    aria-hidden="true"
    data-bs-backdrop="static"
    data-bs-keyboard="false"
  >
    <div class="modal-dialog modal-dialog-centered modal-lg">
      <div class="modal-content">
        <!--begin::Modal Header-->
        <div class="modal-header">
          <h5 class="modal-title fw-bold">{{ t("journal.thread.related.picker.title") }}</h5>
          <button
            type="button"
            class="btn-close"
            :title="closeArmed ? t('common.modal.close-armed.tooltip') : t('common.close')"
            @click="requestSafeClose"
          ></button>
        </div>
        <!--end::Modal Header-->

        <!--begin::Modal Body-->
        <div class="modal-body px-5 py-6">
          <!--begin::검색 필드-->
          <div class="d-flex gap-2 mb-4">
            <input
              v-model="searchKeyword"
              type="search"
              class="form-control form-control-solid"
              :placeholder="t('journal.thread.filter.keyword.placeholder')"
              @keydown.enter.prevent="onSearch"
            />
            <button type="button" class="btn btn-primary min-w-100px" @click="onSearch">
              <i class="bi bi-search me-1"></i>{{ t("common.search") }}
            </button>
          </div>
          <!--end::검색 필드-->

          <!--begin::검색 중-->
          <div v-if="loading" class="text-center py-5 text-muted">
            <span class="spinner-border spinner-border-sm me-2"></span>{{ t("common.loading") }}
          </div>
          <!--end::검색 중-->

          <!--begin::검색 실패-->
          <div v-else-if="searchError" class="text-center py-5 text-danger fs-7">
            {{ searchError }}
          </div>
          <!--end::검색 실패-->

          <!--begin::검색 결과 목록-->
          <div v-else-if="threadList.length > 0" class="d-flex flex-column gap-2 max-h-350px overflow-y-auto px-1">
            <div
              v-for="th in threadList"
              :key="'picker-thread-' + th.id"
              :class="[
                'd-flex align-items-center justify-content-between p-3 rounded border cursor-pointer transition-all',
                isSelfOrAlreadyRelated(th.id)
                  ? 'bg-light border-gray-200 opacity-60 cursor-not-allowed'
                  : selectedThreadId === th.id
                  ? 'border-primary bg-light-primary'
                  : 'border-gray-300 hover-border-primary'
              ]"
              @click="selectThread(th)"
            >
              <div class="d-flex flex-column gap-1 min-w-0 flex-grow-1 me-3">
                <div class="d-flex align-items-center gap-2">
                  <span v-if="th.prefix?.name" class="badge badge-light-info fs-8">{{ th.prefix.name }}</span>
                  <span class="fw-bold fs-6 text-gray-800 text-truncate">{{ th.title || t('journal.entry.thread.untitled') }}</span>
                  <span v-if="isSelf(th.id)" class="badge badge-light-secondary fs-9">{{ t("journal.thread.related.picker.self") }}</span>
                  <span v-else-if="isAlreadyRelated(th.id)" class="badge badge-light-warning fs-9">{{ t("journal.thread.related.picker.already-related") }}</span>
                </div>
                <div v-if="th.content" class="fs-7 text-muted text-truncate">{{ stripHtml(th.content) }}</div>
              </div>
              <div class="d-flex align-items-center gap-2">
                <i v-if="selectedThreadId === th.id" class="bi bi-check-circle-fill fs-5 text-primary"></i>
              </div>
            </div>
          </div>
          <!--end::검색 결과 목록-->

          <!--begin::결과 없음-->
          <div v-else-if="searched" class="text-center py-5 text-muted fs-7">
            {{ t("common.search.rslt.empty") }}
          </div>
          <!--end::결과 없음-->
        </div>
        <!--end::Modal Body-->

        <!--begin::Modal Footer-->
        <div class="modal-footer">
          <button
            type="button"
            class="btn btn-sm btn-primary"
            :disabled="!selectedThreadId || saving"
            @click="submit"
          >
            <span v-if="saving" class="spinner-border spinner-border-sm me-1"></span>
            {{ t("common.add") }}
          </button>
          <button
            type="button"
            class="btn btn-sm"
            :class="closeArmed ? 'btn-light-warning' : 'btn-light'"
            @click="requestSafeClose"
          >
            {{ t("common.cancel") }}
          </button>
        </div>
        <!--end::Modal Footer-->
      </div>
    </div>
  </div>
  <!--end::스레드 피커 모달-->
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from "vue";
import { Modal } from "bootstrap";
import { useJournalThreadStore } from "@/features/journal/stores/journalThread";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { useSafeModalClose } from "@/shared/utils/safeModalClose";

const threadStore = useJournalThreadStore();
const { t } = useLocaleStore();

/** HTML 태그를 제거하고 plain text만 반환한다. 피커 미리보기용. */
function stripHtml(html: string): string {
  return html.replace(/<[^>]*>/g, "").trim();
}

const modalEl = ref<HTMLElement | null>(null);
let bsModal: InstanceType<typeof Modal> | null = null;

const searchKeyword = ref("");
const selectedThreadId = ref<number | null>(null);
const saving = ref(false);

const { closeArmed, requestSafeClose, resetSafeClose } = useSafeModalClose(() => {
  threadStore.closePicker();
});

const loading = computed(() => threadStore.pickerLoading);
const searched = computed(() => threadStore.pickerSearched);
const threadList = computed(() => threadStore.pickerSearchResults);
const searchError = computed(() => threadStore.pickerSearchError);
const baseThreadId = computed(() => threadStore.detailModel?.id);

function isSelf(id?: number): boolean {
  return id != null && baseThreadId.value === id;
}

function isAlreadyRelated(id?: number): boolean {
  return id != null && threadStore.detailRelatedThreads.some((rel) => rel.targetId === id);
}

function isSelfOrAlreadyRelated(id?: number): boolean {
  return isSelf(id) || isAlreadyRelated(id);
}

function selectThread(th: { id?: number }): void {
  if (!th.id || isSelfOrAlreadyRelated(th.id)) return;
  selectedThreadId.value = th.id;
}

async function onSearch(): Promise<void> {
  selectedThreadId.value = null;
  await threadStore.searchThreadsForPicker(searchKeyword.value);
}

async function submit(): Promise<void> {
  const baseId = baseThreadId.value;
  const targetId = selectedThreadId.value;
  if (!baseId || !targetId) return;

  saving.value = true;
  try {
    const ok = await threadStore.addRelatedThread(baseId, targetId);
    if (ok) {
      resetSafeClose();
      threadStore.closePicker();
    }
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  if (modalEl.value) {
    bsModal = new Modal(modalEl.value, { backdrop: "static", keyboard: false });
    modalEl.value.addEventListener("hidden.bs.modal", () => {
      resetSafeClose();
      threadStore.closePicker();
    });
  }
});

watch(
  () => threadStore.pickerOpen,
  (isOpen) => {
    if (isOpen) {
      searchKeyword.value = "";
      selectedThreadId.value = null;
      resetSafeClose();
      bsModal?.show();
      void onSearch();
    } else {
      bsModal?.hide();
    }
  }
);
</script>

<style scoped>
.hover-border-primary:hover {
  border-color: var(--bs-primary) !important;
}
</style>
