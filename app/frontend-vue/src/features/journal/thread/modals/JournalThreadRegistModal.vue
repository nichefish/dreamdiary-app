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
          <JournalThreadEditorForm />
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
import { swalConfirm } from "@/shared/utils/swal";
import { useSafeModalClose } from "@/shared/utils/safeModalClose";
import { ref, computed, watch, onMounted } from "vue";
import { useRoute } from "vue-router";
import { Modal } from "bootstrap";
import { useJournalThreadStore } from "@/features/journal/stores/journalThread";
import { useJournalStore } from "@/features/journal/stores/journal";
import { refreshJournalEntryHostForRoute } from "@/features/journal/utils/journalEntryHostRefresh";
import JournalThreadEditorForm from "@/features/journal/thread/components/JournalThreadEditorForm.vue";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

const store = useJournalThreadStore();
const journalStore = useJournalStore();
const route = useRoute();
const { t } = useLocaleStore();

const modalEl = ref<HTMLElement | null>(null);
let bsModal: InstanceType<typeof Modal> | null = null;
const { closeArmed, requestSafeClose, resetSafeClose } = useSafeModalClose(() => {
  store.closeRegist();
});

const model = computed(() => store.registModel);
const isModify = computed(() => !!model.value?.id);


onMounted(() => {
  if (modalEl.value) {
    bsModal = new Modal(modalEl.value, { backdrop: "static", keyboard: false });
    modalEl.value.addEventListener("hidden.bs.modal", () => {
      resetSafeClose();
      /*
       * 상세 → 수정 전환으로 모달 표면만 숨은 경우에는 수정 모델을 닫지 않는다.
       * 실제 등록/수정 모달이 숨은 경우에만 편집 상태를 정리하고 보류한 상세를 복원한다.
       */
      if (store.registSurface === "modal") store.closeRegist();
    });
  }
});

watch(
  () => store.registOpen && store.registSurface === "modal",
  (isOpen) => {
    if (isOpen) {
      resetSafeClose();
      bsModal?.show();
    } else bsModal?.hide();
  }
);

async function submit() {
  const confirmed = await swalConfirm(isModify.value ? t("common.confirm.mdf") : t("common.confirm.reg"));
  if (!confirmed) return;
  const shouldRefreshDetailHost = store.hasSuspendedDetailEdit;
  const succeeded = await store.submitRegist();
  if (!succeeded || !shouldRefreshDetailHost) return;
  await refreshJournalEntryHostForRoute(journalStore, store, route);
}
</script>
