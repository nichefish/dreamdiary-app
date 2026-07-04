<template>
  <!--begin::저널 할일 등록/수정 모달-->
  <div ref="modalEl" class="modal fade" id="journal_todo_regist_modal" tabindex="-1" aria-hidden="true" data-bs-backdrop="static" data-bs-keyboard="false">
    <div class="modal-dialog modal-xl">
      <div class="modal-content">

        <!--begin::Modal Header-->
        <div class="modal-header">
          <h5 class="modal-title">{{ isModify ? t('journal.todo.modify.modal.title') : t('journal.todo.regist.modal.title') }}</h5>
          <button
            type="button"
            class="btn-close"
            :title="closeArmed ? t('common.modal.close-armed.tooltip') : t('common.close')"
            @click="requestSafeClose"
          ></button>
        </div>
        <!--end::Modal Header-->

        <!--begin::Modal Body-->
        <div class="modal-body modal-mbl-body my-5">
          <form v-if="model" id="journalTodoRegistForm" class="form" @submit.prevent>
            <input type="hidden" name="id" :value="model.id ?? ''" />
            <input type="hidden" name="yy" :value="model.yy ?? ''" />
            <input type="hidden" name="mnth" :value="model.mnth ?? ''" />

            <!--begin::날짜-->
            <div class="row d-flex mb-8">
              <div class="col-2">
                <label class="d-flex align-items-center mb-2">
                  <span class="text-gray-700 fs-6 fw-bolder">{{ t('journal.day.field.date') }}</span>
                </label>
              </div>
              <div class="col-4 fs-6">
                <i class="bi bi-calendar3"></i>
                {{ formatTodoMonth(model.yy, model.mnth) }}
              </div>
            </div>
            <!--end::날짜-->

            <!--begin::제목 (카테고리 + 제목 + 순서)-->
            <div class="row d-flex mb-8">
              <div class="col-12">
                <label class="d-flex align-items-center mb-2">
                  <span class="text-gray-700 fs-6 fw-bolder">{{ t('common.title') }}</span>
                  <span class="text-gray-500 fs-9 ms-2">{{ t('journal.field.title-max-50') }}</span>
                </label>
              </div>
              <div class="col-lg-2">
                <!--begin::카테고리 (dead UI 보존)-->
                <select name="categoryCode" id="categoryCode" class="form-select form-select-solid" v-model="model.categoryCode">
                  <option value="">{{ t('journal.todo.category.placeholder') }}</option>
                </select>
                <!--end::카테고리-->
              </div>
              <div :class="isModify ? 'col-lg-9' : 'col-lg-10'">
                <input
                  type="text"
                  name="title"
                  id="title"
                  class="form-control form-control-solid"
                  v-model="model.title"
                  :placeholder="t('journal.todo.title.placeholder')"
                  maxlength="50"
                  required
                />
              </div>
              <div v-if="isModify" class="col-lg-1">
                <input
                  type="number"
                  name="sortOrder"
                  id="sortOrder"
                  class="form-control form-control-solid"
                  v-model.number="model.sortOrder"
                  :placeholder="t('journal.todo.sort-order.placeholder')"
                />
              </div>
            </div>
            <!--end::제목-->

            <!--begin::내용-->
            <div class="row mb-4">
              <div class="col-12">
                <label class="d-flex align-items-center mb-2" for="content">
                  <span class="text-gray-700 fs-6 fw-bolder">{{ t('common.content') }}</span>
                </label>
              </div>
              <div class="col-12">
                <RichEditor v-model="model.content" :height="350" />
              </div>
            </div>
            <!--end::내용-->

            <!--begin::태그-->
            <div class="row mb-3">
              <div>
                <label for="tagListStr" class="mb-2">
                  <span class="text-gray-700 fs-6 fw-bolder">{{ t('common.tag') }}</span>
                  <span class="text-gray-500 fs-9 mx-2">{{ t('journal.todo.tag.format-guide') }}</span>
                </label>
              </div>
              <div class="col-xl-12">
                <input
                  name="tag.tagListStr"
                  id="tagListStr"
                  class="form-control form-control-solid"
                  autocomplete="off"
                  maxlength="500"
                  v-model="tagListStrWithCtgr"
                  :placeholder="t('journal.todo.tag.placeholder')"
                />
              </div>
            </div>
            <!--end::태그-->

          </form>
        </div>
        <!--end::Modal Body-->

        <!--begin::Modal Footer-->
        <div class="modal-footer">
          <div class="d-flex justify-content-end gap-2">
            <button
              type="button"
              class="btn btn-sm btn-primary"
              :disabled="submitting"
              @click="submit"
            >
              <span v-if="submitting" class="spinner-border spinner-border-sm me-1" role="status"></span>
              {{ t('common.save') }}
            </button>
            <button
              type="button"
              class="btn btn-sm"
              :class="closeArmed ? 'btn-light-warning' : 'btn-light'"
              :title="closeArmed ? t('common.modal.close-armed.tooltip') : t('common.close')"
              @click="requestSafeClose"
            >{{ t('common.close') }}</button>
          </div>
        </div>
        <!--end::Modal Footer-->

      </div>
    </div>
  </div>
  <!--end::저널 할일 등록/수정 모달-->
</template>

<script setup lang="ts">
import { swalConfirm, swalAlert, swalRequestError } from "@/shared/utils/swal";
import { useSafeModalClose } from "@/shared/utils/safeModalClose";
import { ref, computed, watch, onMounted } from "vue";
import RichEditor from "@/shared/ui/editor/RichEditor.vue";
import { Modal } from "bootstrap";
import axios from "axios";
import { useJournalModalStore } from "@/features/journal/stores/journalModal";
import { useJournalStore } from "@/features/journal/stores/journal";
import { useRoute } from "vue-router";
import { refreshJournalDaysForRoute } from "@/features/journal/utils/journalDayRefresh";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

const modalStore = useJournalModalStore();
const journalStore = useJournalStore();
const { t } = useLocaleStore();
const route = useRoute();

const modalEl = ref<HTMLElement | null>(null);
const submitting = ref(false);
let bsModal: InstanceType<typeof Modal> | null = null;
const { closeArmed, requestSafeClose, resetSafeClose } = useSafeModalClose(() => {
  modalStore.closeTodoRegist();
});

const model = computed(() => modalStore.todoRegistModel);
const isModify = computed(() => !!model.value?.id);

/** 태그 문자열 바인딩 */
const tagListStrWithCtgr = computed({
  get: () => model.value?.tag?.tagListStrWithCtgr ?? "",
  set: (v) => { if (model.value?.tag) model.value.tag.tagListStrWithCtgr = v; },
});

onMounted(() => {
  if (modalEl.value) {
    bsModal = new Modal(modalEl.value, { backdrop: "static", keyboard: false });
    modalEl.value.addEventListener("hidden.bs.modal", () => {
      resetSafeClose();
      modalStore.closeTodoRegist();
    });
  }
});

watch(
  () => modalStore.todoRegistOpen,
  (isOpen) => {
    if (isOpen) {
      resetSafeClose();
      bsModal?.show();
    } else bsModal?.hide();
  }
);

function close() {
  resetSafeClose();
  modalStore.closeTodoRegist();
}

/** 현재 locale의 년월 표시 형식으로 할일 대상 월을 조립한다. */
function formatTodoMonth(yy?: number | string, mnth?: number | string): string {
  return t("journal.todo.date.format")
    .replace("{0}", String(yy ?? ""))
    .replace("{1}", String(mnth ?? ""));
}

/** 등록/수정 처리 */
async function submit() {
  if (!model.value) return;
  if (!model.value.title) {
    void swalAlert(t("journal.todo.title.required"));
    return;
  }

  const confirmed = await swalConfirm(isModify.value ? t("common.confirm.mdf") : t("common.confirm.reg"));
  if (!confirmed) return;

  submitting.value = true;
  try {
    const formData = new FormData();
    if (isModify.value) formData.append("id", String(model.value.id));
    formData.append("yy", String(model.value.yy ?? ""));
    formData.append("mnth", String(model.value.mnth ?? ""));
    formData.append("categoryCode", model.value.categoryCode ?? "");
    formData.append("title", model.value.title ?? "");
    if (model.value.sortOrder != null) formData.append("sortOrder", String(model.value.sortOrder));
    formData.append("content", model.value.content ?? "");
    formData.append("tag.tagListStr", tagListStrWithCtgr.value);

    const url = isModify.value
      ? `/api/journal/todo/${model.value.id}`
      : "/api/journal/todos";
    const res = await axios.post(url, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });

    if (res.data?.rslt) {
      close();
      await swalAlert(res.data?.message ?? (isModify.value ? t("common.result.modified") : t("common.result.registered")));
      void refreshJournalDaysForRoute(journalStore, route);
      void journalStore.fetchTodos();
    } else {
      void swalAlert(res.data?.message ?? t("common.result.failure"));
    }
  } catch (e: unknown) {
    void swalRequestError(e);
  } finally {
    submitting.value = false;
  }
}
</script>
