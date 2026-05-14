<template>
  <div ref="modalEl" class="modal fade" id="comment_list_modal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-xl modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">댓글</h5>
          <button type="button" class="btn-close" @click="close"></button>
        </div>

        <div class="modal-body modal-mbl-body my-5">
          <div v-if="attachableStore.commentListLoading" class="d-flex justify-content-center py-10">
            <span class="spinner-border text-primary" role="status"></span>
          </div>
          <div v-else-if="attachableStore.commentList.length === 0" class="d-flex-center min-h-150px text-muted">
            댓글이 없습니다.
          </div>
          <div v-else class="table-responsive-sm">
            <table class="table align-middle table-row-dashed fs-small gy-3 table-fixed mb-3">
              <tbody>
                <tr v-for="comment in attachableStore.commentList" :key="comment.id">
                  <td class="col-lg-2 col-3 text-start wb-keepall ps-6">
                    <div class="text-gray-800 fs-6 fw-bold lh-1 mb-2">
                      <div
                        v-if="comment.createdByInfo?.profileImageUrl"
                        class="btn btn-icon btn-active-light-primary position-relative w-15px h-15px w-md-20px h-md-20px me-1"
                      >
                        <img :src="comment.createdByInfo.profileImageUrl" class="img-thumbnail p-0 w-100" alt="" />
                      </div>
                      {{ comment.createdByNm || '-' }}
                    </div>
                    <div class="text-muted fs-7 lh-1">
                      {{ comment.createdAt || '-' }}
                    </div>
                  </td>
                  <td class="col-lg-10 col-9 text-start fs-small border-bottom-0">
                    <div class="div-textarea-smp">{{ comment.content }}</div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div class="modal-footer">
          <button type="button" class="btn btn-sm btn-light" @click="close">닫기</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from "vue";
import { Modal } from "bootstrap";
import { useAttachableModalStore } from "@/stores/attachableModal";

const attachableStore = useAttachableModalStore();

const modalEl = ref<HTMLElement | null>(null);
let bsModal: InstanceType<typeof Modal> | null = null;

onMounted(() => {
  if (modalEl.value) {
    bsModal = new Modal(modalEl.value);
    modalEl.value.addEventListener("hidden.bs.modal", () => {
      attachableStore.closeCommentList();
    });
  }
});

watch(
  () => attachableStore.commentListOpen,
  (isOpen) => {
    if (isOpen) bsModal?.show();
    else bsModal?.hide();
  }
);

function close() {
  attachableStore.closeCommentList();
}
</script>
