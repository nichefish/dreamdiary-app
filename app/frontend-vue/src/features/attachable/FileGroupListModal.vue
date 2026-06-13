<template>
  <!--begin::첨부파일 목록 모달-->
  <div ref="modalEl" class="modal fade" id="file_group_list_modal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
      <div class="modal-content">

        <!--begin::Modal Header-->
        <div class="modal-header">
          <h5 class="modal-title">첨부파일</h5>
          <button type="button" class="btn-close" @click="close"></button>
        </div>
        <!--end::Modal Header-->

        <!--begin::Modal Body-->
        <div class="modal-body modal-mbl-body my-5">
          <!--begin::로딩-->
          <div v-if="attachableStore.fileListLoading" class="d-flex justify-content-center my-5">
            <span class="spinner-border text-primary" role="status"></span>
          </div>
          <!--end::로딩-->
          <!--begin::파일 목록 테이블-->
          <table v-else class="table align-middle table-row-dashed fs-small gy-3 table-fixed hoverTable mb-3">
            <tbody>
              <tr
                v-for="file in attachableStore.fileList"
                :key="file.id"
                class="bg-hover-secondary cursor-pointer"
                @click="fileDownload(file.fileGroupId, file.id)"
              >
                <td class="col-lg-7 col-8 text-start wb-keepall ps-6">
                  <a class="badge badge-secondary p-2 btn-white blank blink-slow me-2">
                    <i class="bi bi-file-earmark-arrow-down-fill text-primary blink-none"></i>
                  </a>
                  <span class="border-bottom text-primary opacity-hover">{{ file.orgnFileNm }}</span>
                </td>
                <td class="col-lg-3 text-start hidden-table fs-small">
                  ({{ numberFormat(file.fileSize) }} bytes)
                </td>
                <td class="col-lg-2 col-3 text-start">
                  <div class="badge badge-sm btn-primary badge-light-primary btn-outlined fw-normal">
                    <i class="fas fa-download fs-7 pe-1"></i>Download
                  </div>
                </td>
              </tr>
              <tr v-if="!attachableStore.fileListLoading && attachableStore.fileList.length === 0">
                <td colspan="3" class="text-center text-muted py-5">첨부파일이 없습니다.</td>
              </tr>
            </tbody>
          </table>
          <!--end::파일 목록 테이블-->
        </div>
        <!--end::Modal Body-->

        <!--begin::Modal Footer-->
        <div class="modal-footer">
          <div class="d-flex justify-content-end">
            <button type="button" class="btn btn-sm btn-light" @click="close">닫기</button>
          </div>
        </div>
        <!--end::Modal Footer-->

      </div>
    </div>
  </div>
  <!--end::첨부파일 목록 모달-->
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from "vue";
import { Modal } from "bootstrap";
import { useAttachableModalStore } from "@/features/attachable/stores/attachableModal";

const attachableStore = useAttachableModalStore();

const modalEl = ref<HTMLElement | null>(null);
let bsModal: InstanceType<typeof Modal> | null = null;

onMounted(() => {
  if (modalEl.value) {
    bsModal = new Modal(modalEl.value);
    /* bootstrap 이벤트로 store 와 상태를 동기화한다 */
    modalEl.value.addEventListener("hidden.bs.modal", () => {
      attachableStore.closeFileList();
    });
  }
});

watch(
  () => attachableStore.fileListOpen,
  (isOpen) => {
    if (isOpen) bsModal?.show();
    else bsModal?.hide();
  }
);

function close(): void {
  attachableStore.closeFileList();
}

/** 파일 다운로드 */
function fileDownload(fileGroupId: string | number, fileId: number): void {
  window.open(`/api/file/file-download.do?fileGroupId=${fileGroupId}&fileId=${fileId}`, "_blank");
}

/** 파일 크기 포맷 */
function numberFormat(value: number | string): string {
  const n = Number(value);
  return Number.isFinite(n) ? n.toLocaleString() : String(value ?? "");
}
</script>