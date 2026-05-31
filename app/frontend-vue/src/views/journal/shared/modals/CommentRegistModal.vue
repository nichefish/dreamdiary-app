<template>
  <!--begin::댓글 등록/수정 모달-->
  <div ref="modalEl" class="modal fade" id="comment_reg_modal" tabindex="-1" aria-hidden="true" data-bs-backdrop="static" data-bs-keyboard="false">
    <div class="modal-dialog modal-xl modal-dialog-centered">
      <div class="modal-content">

        <!--begin::Modal Header-->
        <div class="modal-header">
          <h5 class="modal-title">댓글 저장</h5>
          <button
            type="button"
            class="btn-close"
            :title="closeArmed ? '한 번 더 클릭하면 닫힙니다' : '닫기'"
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
            <label class="mb-2 text-gray-700 fs-6 fw-bolder" for="comment_reg_content">내용</label>
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
              저장
            </button>
            <button
              type="button"
              class="btn btn-sm"
              :class="closeArmed ? 'btn-light-warning' : 'btn-light'"
              :title="closeArmed ? '한 번 더 클릭하면 닫힙니다' : '닫기'"
              @click="requestSafeClose"
            >닫기</button>
          </div>
        </div>
        <!--end::Modal Footer-->

      </div>
    </div>
  </div>
  <!--end::댓글 등록/수정 모달-->
</template>

<script setup lang="ts">
import { swalConfirm, swalAlert } from "@/utils/swal";
import { isAuthExpiredError } from "@/utils/authError";
import { useSafeModalClose } from "@/utils/safeModalClose";
import { ref, watch, onMounted } from "vue";
import RichEditor from "@/views/common/editor/RichEditor.vue";
import { Modal } from "bootstrap";
import axios from "axios";
import { useAttachableModalStore } from "@/stores/attachableModal";
import { useJournalStore } from "@/stores/journal";

const attachableStore = useAttachableModalStore();
const journalStore = useJournalStore();

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
    void swalAlert("내용을 입력해 주세요.");
    return;
  }
  const isModify = !!attachableStore.commentId;
  const confirmed = await swalConfirm(isModify ? "수정하시겠습니까?" : "등록하시겠습니까?");
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
      void journalStore.fetchDays();
    } else {
      void swalAlert(res.data?.message ?? "처리에 실패했습니다.");
    }
  } catch (e: unknown) {
    if (isAuthExpiredError(e)) return;
    void swalAlert("요청 처리 중 오류가 발생했습니다.");
  } finally {
    submitting.value = false;
  }
}
</script>
