<template>
  <div
    ref="modalEl"
    class="modal fade"
    id="journal_tag_list_modal"
    tabindex="-1"
    aria-hidden="true"
    data-bs-keyboard="false"
    data-bs-backdrop="static"
  >
    <div class="modal-dialog modal-xl modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header">
          <h5 id="modal_title" class="modal-title">[저널 태그 전체 목록]</h5>
          <div class="d-flex gap-4">
            <button
              type="button"
              class="btn btn-sm btn-icon btn-active-light-primary ms-2"
              data-bs-toggle="tooltip"
              data-bs-placement="top"
              data-bs-dismiss="modal"
              title="닫기"
              @click="close"
            >
              <i class="fas fa-times"></i>
            </button>
          </div>
        </div>

        <div class="modal-body modal-mbl-body my-5">
          <div v-if="attachableStore.tagListLoading" class="d-flex justify-content-center py-10">
            <span class="spinner-border text-primary" role="status"></span>
          </div>
          <div v-else-if="visibleTagGroups.length === 0" class="d-flex-center min-h-150px text-muted">
            표시할 태그가 없습니다.
          </div>
          <div v-else class="d-flex flex-column gap-6">
            <section
              v-for="group in visibleTagGroups"
              :key="group.category"
              class="border-bottom border-gray-200 pb-4"
            >
              <div class="d-flex align-items-center justify-content-between mb-3">
                <h6 class="fw-bold text-gray-800 mb-0">
                  <span v-if="group.category" class="text-noti">[{{ group.category }}]</span>
                  <span v-else>미분류</span>
                </h6>
                <span class="badge badge-light-secondary">{{ group.tags.length }}</span>
              </div>
              <div class="d-flex flex-wrap gap-2">
                <button
                  v-for="tag in group.tags"
                  :key="String(tag.id)"
                  type="button"
                  :class="['btn btn-sm btn-light-primary text-start', tag.textClass]"
                  :title="'태그 일자 목록 보기: ' + tag.name"
                  @click="openTagDetail(tag)"
                >
                  #{{ tag.name }}
                  <span class="fs-9 text-noti ms-1">[{{ tag.contentSize }}]</span>
                </button>
              </div>
            </section>
          </div>
        </div>

        <div class="modal-footer">
          <div class="d-flex justify-content-end">
            <button type="button" class="btn btn-sm btn-light" @click="close">닫기</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted } from "vue";
import { Modal } from "bootstrap";
import { useAttachableModalStore, type TagListItem } from "@/stores/attachableModal";
import { useJournalModalStore } from "@/stores/journalModal";

const attachableStore = useAttachableModalStore();
const journalModalStore = useJournalModalStore();

const modalEl = ref<HTMLElement | null>(null);
let bsModal: InstanceType<typeof Modal> | null = null;

const visibleTagGroups = computed(() =>
  Object.entries(attachableStore.tagGroupMap)
    .map(([category, tags]) => ({ category, tags }))
    .filter((group) => group.tags.length > 0)
);

onMounted(() => {
  if (modalEl.value) {
    bsModal = new Modal(modalEl.value);
    modalEl.value.addEventListener("hidden.bs.modal", () => {
      attachableStore.closeTagList();
    });
  }
});

watch(
  () => attachableStore.tagListOpen,
  (isOpen) => {
    if (isOpen) bsModal?.show();
    else bsModal?.hide();
  }
);

function close() {
  attachableStore.closeTagList();
}

function openTagDetail(tag: TagListItem) {
  void journalModalStore.openDayFilterModal({ type: "tag", id: tag.id, name: tag.name });
}
</script>
