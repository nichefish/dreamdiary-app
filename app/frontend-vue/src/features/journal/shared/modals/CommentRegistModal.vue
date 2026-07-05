<template>
  <!--begin::댓글 등록/수정 모달-->
  <div ref="modalEl" class="modal fade" id="comment_reg_modal" tabindex="-1" aria-hidden="true" data-bs-backdrop="static" data-bs-keyboard="false">
    <div class="modal-dialog modal-xl modal-dialog-centered">
      <div class="modal-content">

        <!--begin::Modal Header-->
        <div class="modal-header">
          <h5 class="modal-title">{{ t("comment.save.modal.title") }}</h5>
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
          <div v-if="attachableStore.commentRegistLoading" class="d-flex justify-content-center py-10">
            <span class="spinner-border text-primary" role="status"></span>
          </div>
          <!--end::로딩-->
          <template v-else>
            <label class="mb-2 text-gray-700 fs-6 fw-bolder" for="comment_reg_content">{{ t("common.content") }}</label>
            <!--begin::본문-->
            <RichEditor v-model="attachableStore.commentContent" :height="300" />
            <!--end::본문-->
          </template>
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
              {{ t("common.save") }}
            </button>
            <button
              type="button"
              class="btn btn-sm"
              :class="closeArmed ? 'btn-light-warning' : 'btn-light'"
              :title="closeArmed ? t('common.modal.close-armed.tooltip') : t('common.close')"
              @click="requestSafeClose"
            >{{ t("common.close") }}</button>
          </div>
        </div>
        <!--end::Modal Footer-->

      </div>
    </div>
  </div>
  <!--end::댓글 등록/수정 모달-->
</template>

<script setup lang="ts">
import { swalConfirm, swalAlert, swalRequestError } from "@/shared/utils/swal";
import { useSafeModalClose } from "@/shared/utils/safeModalClose";
import { ref, watch, onMounted } from "vue";
import RichEditor from "@/shared/ui/editor/RichEditor.vue";
import { Modal } from "bootstrap";
import axios from "axios";
import { useAttachableModalStore } from "@/features/attachable/stores/attachableModal";
import { useJournalStore } from "@/features/journal/stores/journal";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { useRoute } from "vue-router";
import { refreshJournalDaysForRoute } from "@/features/journal/utils/journalDayRefresh";
import { useJournalThreadStore } from "@/features/journal/stores/journalThread";

const attachableStore = useAttachableModalStore();
const journalStore = useJournalStore();
const { t } = useLocaleStore();
const route = useRoute();

const modalEl = ref<HTMLElement | null>(null);
const submitting = ref(false);
let bsModal: InstanceType<typeof Modal> | null = null;
const { closeArmed, requestSafeClose, resetSafeClose } = useSafeModalClose(() => {
  attachableStore.closeCommentRegist();
});

onMounted(() => {
  if (modalEl.value) {
    bsModal = new Modal(modalEl.value, { backdrop: "static", keyboard: false });
    /* bootstrap 이벤트로 store 와 상태를 동기화한다 */
    modalEl.value.addEventListener("hidden.bs.modal", () => {
      resetSafeClose();
      attachableStore.closeCommentRegist();
    });
  }
});

watch(
  () => attachableStore.commentRegistOpen,
  (isOpen) => {
    if (isOpen) {
      resetSafeClose();
      bsModal?.show();
    } else bsModal?.hide();
  }
);

function close() {
  resetSafeClose();
  attachableStore.closeCommentRegist();
}

/** 댓글 등록/수정 처리. 신규 시 /api/comments, 수정 시 /api/comment/{id} 로 multipart POST. */
async function submit() {
  if (!attachableStore.commentContent.trim()) {
    void swalAlert(t("comment.content.required"));
    return;
  }
  const isModify = !!attachableStore.commentId;
  const confirmed = await swalConfirm(isModify ? t("common.confirm.mdf") : t("common.confirm.reg"));
  if (!confirmed) return;

  submitting.value = true;
  try {
    const fd = new FormData();
    if (attachableStore.commentId) fd.append("id", String(attachableStore.commentId));
    fd.append("refId", String(attachableStore.commentRefId));
    fd.append("refContentType", attachableStore.commentRefContentType);
    fd.append("content", attachableStore.commentContent);

    const url = isModify ? `/api/comment/${attachableStore.commentId}` : "/api/comments";
    const res = await axios.post(url, fd, {
      headers: { "Content-Type": "multipart/form-data" },
    });
    if (res.data?.rslt) {
      close();
      await swalAlert(res.data?.message ?? (isModify ? t("common.result.modified") : t("common.result.registered")));
      void refreshJournalDaysForRoute(journalStore, route);
      if (attachableStore.commentRefContentType === "JOURNAL_THREAD") {
        const threadStore = useJournalThreadStore();
        const refId = Number(attachableStore.commentRefId);
        if (threadStore.detailOpen && threadStore.detailModel?.id === refId) {
          void threadStore.openDetail(refId);
        }
        void threadStore.fetchList(threadStore.currentPage);
      }
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
