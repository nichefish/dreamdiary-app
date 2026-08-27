<template>
  <!--begin::저널 결산 리뷰 등록/수정 모달-->
  <div ref="modalEl" class="modal fade" id="journal_annual_review_regist_modal" tabindex="-1" aria-hidden="true" data-bs-backdrop="static" data-bs-keyboard="false">
    <div class="modal-dialog modal-xl">
      <div class="modal-content">

        <!--begin::Modal Header-->
        <div class="modal-header">
          <h5 class="modal-title">{{ isModify ? t('journal.annual.review.regist.modal.title.edit') : t('journal.annual.review.regist.modal.title.register') }}</h5>
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
          <div v-if="store.reviewRegistLoading" class="d-flex justify-content-center py-10">
            <span class="spinner-border text-primary" role="status"></span>
          </div>
          <!--end::로딩-->

          <form v-else-if="model" id="journalAnnualReviewRegistForm" class="form" @submit.prevent>
            <input type="hidden" name="id" :value="model.id ?? ''" />
            <input type="hidden" name="journalAnnualId" :value="model.journalAnnualId ?? ''" />
            <input type="hidden" name="contentType" value="JOURNAL_ANNUAL_REVIEW" />

            <!--begin::본문-->
            <div class="row d-flex mb-8">
              <div class="col-12">
                <label class="d-flex align-items-center mb-2">
                  <span class="text-gray-700 fs-6 fw-bolder">{{ t('common.content') }}</span>
                </label>
                <RichEditor :model-value="model.content" @update:model-value="model && (model.content = $event)" />
              </div>
            </div>
            <!--end::본문-->

            <!--begin::태그-->
            <div class="row d-flex mb-8">
              <div class="col-12">
                <label class="d-flex align-items-center mb-2">
                  <span class="text-gray-700 fs-6 fw-bolder">{{ t('common.tag') }}</span>
                </label>
                <TagifyEditor v-model="tagListStrWithCtgr" />
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
              :disabled="store.reviewSubmitting"
              @click="submit"
            >
              <span v-if="store.reviewSubmitting" class="spinner-border spinner-border-sm me-1" role="status"></span>
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
  <!--end::저널 결산 리뷰 등록/수정 모달-->
</template>

<script setup lang="ts">
import { swalConfirm, swalAlert } from "@/shared/utils/swal";
import { useSafeModalClose } from "@/shared/utils/safeModalClose";
import { ref, computed, watch, onMounted } from "vue";
import RichEditor from "@/shared/ui/editor/RichEditor.vue";
import TagifyEditor from "@/shared/ui/tag/TagifyEditor.vue";
import { Modal } from "bootstrap";
import { useJournalAnnualStore } from "@/features/journal/stores/journalAnnual";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

const store = useJournalAnnualStore();
const { t } = useLocaleStore();

const modalEl = ref<HTMLElement | null>(null);
let bsModal: InstanceType<typeof Modal> | null = null;
const { closeArmed, requestSafeClose, resetSafeClose } = useSafeModalClose(() => {
  store.closeReviewRegist();
});

const model = computed(() => store.reviewRegistModel);
const isModify = computed(() => !!model.value?.id);

const tagListStrWithCtgr = computed({
  get: () => model.value?.tag?.tagListStrWithCtgr ?? "",
  set: (v: string) => {
    if (!model.value) return;
    model.value.tag = model.value.tag ?? {};
    model.value.tag.tagListStrWithCtgr = v;
  },
});

onMounted(() => {
  if (modalEl.value) {
    bsModal = new Modal(modalEl.value, { backdrop: "static", keyboard: false });
    modalEl.value.addEventListener("hidden.bs.modal", () => {
      resetSafeClose();
      store.closeReviewRegist();
    });
  }
});

watch(
  () => store.reviewRegistOpen,
  (isOpen) => {
    if (isOpen) {
      resetSafeClose();
      bsModal?.show();
    } else bsModal?.hide();
  }
);

function close() {
  resetSafeClose();
  store.closeReviewRegist();
}

async function submit() {
  if (!model.value?.journalAnnualId) return;
  const confirmed = await swalConfirm(isModify.value ? t("journal.annual.confirm.edit") : t("journal.annual.confirm.register"));
  if (!confirmed) return;
  await store.submitReviewRegist();
}
</script>
