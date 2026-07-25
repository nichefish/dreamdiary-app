<template>
  <!--begin::저널 스레드 등록/수정 모달-->
  <div ref="modalEl" class="modal fade" id="journal_thread_regist_modal" tabindex="-1" aria-hidden="true" data-bs-backdrop="static" data-bs-keyboard="false">
    <div class="modal-dialog modal-xl">
      <div class="modal-content">

        <!--begin::Modal Header-->
        <div class="modal-header">
          <h5 class="modal-title">{{ isModify ? t('journal.thread.modify.modal.title') : t('journal.thread.regist.modal.title') }}</h5>
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
          <!--begin::로딩-->
          <div v-if="store.registLoading" class="d-flex justify-content-center py-10">
            <span class="spinner-border text-primary" role="status"></span>
          </div>
          <!--end::로딩-->

          <form v-else-if="model" id="journalThreadRegistForm" class="form" @submit.prevent>
            <input type="hidden" name="id" :value="model.id ?? ''" />
            <input type="hidden" name="contentType" value="JOURNAL_THREAD" />

            <!--begin::제목-->
            <div class="row d-flex mb-8">
              <div class="col-12">
                <label class="d-flex align-items-center mb-2">
                  <span class="text-gray-700 fs-6 fw-bolder">{{ t('common.title') }}</span>
                </label>
                <input
                  name="title"
                  v-model="model.title"
                  class="form-control form-control-solid"
                  :placeholder="t('journal.thread.title.placeholder')"
                  maxlength="100"
                  autocomplete="off"
                />
              </div>
            </div>
            <!--end::제목-->

            <!--begin::본문-->
            <div class="row d-flex mb-8">
              <div class="col-12">
                <label class="d-flex align-items-center mb-2">
                  <span class="text-gray-700 fs-6 fw-bolder">{{ t('common.content') }}</span>
                </label>
                <RichEditor v-model="model.content" />
              </div>
            </div>
            <!--end::본문-->

          </form>
        </div>
        <!--end::Modal Body-->

        <!--begin::Modal Footer-->
        <div class="modal-footer">
          <div class="d-flex justify-content-end gap-2">
            <button
              type="button"
              class="btn btn-sm btn-primary"
              :disabled="store.submitting"
              @click="submit"
            >
              <span v-if="store.submitting" class="spinner-border spinner-border-sm me-1" role="status"></span>
              {{ t('common.save') }}
            </button>
            <button
              type="button"
              class="btn btn-sm"
              :class="closeArmed ? 'btn-warning' : 'btn-light'"
              @click="requestSafeClose"
            >{{ closeArmed ? t('common.modal.close-armed.btn') : t('common.close') }}</button>
          </div>
        </div>
        <!--end::Modal Footer-->

      </div>
    </div>
  </div>
  <!--end::저널 스레드 등록/수정 모달-->
</template>

<script setup lang="ts">
import { swalConfirm, swalAlert } from "@/shared/utils/swal";
import { useSafeModalClose } from "@/shared/utils/safeModalClose";
import { ref, computed, watch, onMounted } from "vue";
import { useRoute } from "vue-router";
import RichEditor from "@/shared/ui/editor/RichEditor.vue";
import { Modal } from "bootstrap";
import { useJournalThreadStore } from "@/features/journal/stores/journalThread";
import { createJournalThreadUpdatedMessage } from "@/features/journal/thread/utils/journalThreadPopupMessage";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

const store = useJournalThreadStore();
const route = useRoute();
const { t } = useLocaleStore();

const modalEl = ref<HTMLElement | null>(null);
let bsModal: InstanceType<typeof Modal> | null = null;
const { closeArmed, requestSafeClose, resetSafeClose } = useSafeModalClose(() => {
  store.closeRegist();
  if (route.query.popup === "Y" && window.opener) window.close();
});

const model = computed(() => store.registModel);
const isModify = computed(() => !!model.value?.id);


onMounted(() => {
  if (modalEl.value) {
    bsModal = new Modal(modalEl.value, { backdrop: "static", keyboard: false });
    modalEl.value.addEventListener("hidden.bs.modal", () => {
      resetSafeClose();
      store.closeRegist();
    });
  }
});

watch(
  () => store.registOpen,
  (isOpen) => {
    if (isOpen) {
      resetSafeClose();
      bsModal?.show();
    } else bsModal?.hide();
  }
);

function close() {
  resetSafeClose();
  store.closeRegist();
}

async function submit() {
  const confirmed = await swalConfirm(isModify.value ? t("common.confirm.mdf") : t("common.confirm.reg"));
  if (!confirmed) return;
  const modifiedThreadId = Number(model.value?.id);
  const isModifyPopup = isModify.value && route.query.popup === "Y";
  const succeeded = await store.submitRegist();
  if (!succeeded || !isModifyPopup || !Number.isInteger(modifiedThreadId) || modifiedThreadId <= 0) return;

  if (window.opener && !window.opener.closed) {
    window.opener.postMessage(
      createJournalThreadUpdatedMessage(modifiedThreadId),
      window.location.origin,
    );
  } else {
    console.warn("[journal-thread] modify popup completed without opener", {
      threadId: modifiedThreadId,
    });
  }
  window.close();
}
</script>
