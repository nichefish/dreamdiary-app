<template>
  <div
    ref="modalEl"
    class="modal fade"
    id="meta_profile_modal"
    tabindex="-1"
    role="dialog"
    aria-hidden="true"
    data-bs-keyboard="false"
    data-bs-backdrop="static"
  >
    <div class="modal-dialog modal-dialog-centered modal-lg" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">메타 설정</h5>
          <button type="button" class="btn-close" @click="close"></button>
        </div>
        <div class="modal-body modal-mbl-body my-5">
          <div v-if="modalStore.metaProfileLoading" class="d-flex justify-content-center py-10">
            <span class="spinner-border text-primary" role="status"></span>
          </div>
          <div v-else-if="model" id="meta_profile_div">
            <div class="d-flex align-items-center gap-2 mb-5">
              <span class="badge badge-light-primary">일자 메타</span>
              <span v-if="model.ctgr" class="fs-7 text-noti">[{{ model.ctgr }}]</span>
              <span class="fs-6 fw-bold text-primary">#{{ model.name }}</span>
            </div>
            <dl class="row mb-0">
              <dt class="col-sm-3 fw-bold">기록 수</dt>
              <dd class="col-sm-9">{{ model.contentSize ?? 0 }}</dd>
              <dt v-if="model.unit" class="col-sm-3 fw-bold">단위</dt>
              <dd v-if="model.unit" class="col-sm-9">{{ model.unit }}</dd>
            </dl>
          </div>
          <div v-else class="text-muted text-center py-10">메타 정보를 불러오지 못했습니다.</div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-sm btn-light" @click="close">닫기</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from "vue";
import { Modal } from "bootstrap";
import { useJournalModalStore } from "@/stores/journalModal";

const modalStore = useJournalModalStore();
const modalEl = ref<HTMLElement | null>(null);
let bsModal: InstanceType<typeof Modal> | null = null;

const model = computed(() => modalStore.metaProfileModel);

watch(
  () => modalStore.metaProfileOpen,
  (open) => {
    if (!bsModal) return;
    if (open) bsModal.show();
    else bsModal.hide();
  }
);

function close(): void {
  modalStore.closeMetaProfile();
}

onMounted(() => {
  if (modalEl.value) {
    bsModal = new Modal(modalEl.value);
    modalEl.value.addEventListener("hidden.bs.modal", () => {
      modalStore.closeMetaProfile();
    });
  }
});
</script>
