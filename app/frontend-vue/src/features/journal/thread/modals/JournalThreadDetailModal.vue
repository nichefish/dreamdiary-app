<template>
  <!--begin::저널 스레드 상세 모달-->
  <div ref="modalEl" class="modal fade" id="journal_thread_detail_modal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-xxl">
      <div class="modal-content">

        <!--begin::Modal Header-->
        <div class="modal-header">
          <h5 class="modal-title">{{ t("journal.thread.detail.modal.title") }}</h5>
          <button type="button" class="btn-close" @click="close"></button>
        </div>
        <!--end::Modal Header-->

        <!--begin::Modal Body-->
        <div class="modal-body modal-mbl-body my-5">
          <!--begin::로딩-->
          <div v-if="store.detailLoading" class="d-flex justify-content-center py-10">
            <span class="spinner-border text-primary" role="status"></span>
          </div>
          <!--end::로딩-->

          <div v-else-if="store.detailModel" class="journal-thread-dtl-vue-root">
            <!--begin::헤더 (제목 + 작성자/일시)-->
            <div class="mb-0">
              <div class="d-flex align-items-center flex-wrap gap-2 mb-2">
                <span v-if="store.detailModel.categoryName" class="ctgr-span ctgr-gray">{{ store.detailModel.categoryName }}</span>
                <span class="fs-3 fw-bolder text-gray-900">{{ store.detailModel.title }}</span>
              </div>
              <div class="d-flex align-items-center flex-wrap gap-3 text-muted fs-7">
                <span v-if="store.detailModel.createdByNm"><i class="bi bi-person pe-1"></i>{{ store.detailModel.createdByNm }}</span>
                <span v-if="store.detailModel.createdDt"><i class="bi bi-clock pe-1"></i>{{ store.detailModel.createdDt }}</span>
              </div>
            </div>
            <!--end::헤더-->

            <div class="separator separator-dashed border-gray-300 my-8"></div>

            <!--begin::본문-->
            <div
              class="fs-4 fw-normal text-gray-800 px-5 py-1 pb-6 min-h-150px"
              v-html="store.detailModel.markdownContent || store.detailModel.content || ''"
            ></div>
            <!--end::본문-->

            <!--begin::태그-->
            <div v-if="hasDetailTags" class="mt-4">
              <i class="bi bi-tag me-1"></i>
              <span
                v-for="tag in store.detailModel.tag?.list"
                :key="'thread-dtl-tag-' + String(tag.tagId)"
                class="text-muted pe-1"
              >
                <span v-if="tag.ctgr" class="fs-7 text-noti">[{{ tag.ctgr }}]</span>
                #<span class="border-bottom text-primary fw-lighter opacity-hover">{{ tag.name }}</span>
              </span>
            </div>
            <!--end::태그-->

            <!--begin::댓글 영역 (TODO: 댓글 연동 예정)-->
            <div class="separator separator-dashed border-gray-200 my-6"></div>
            <div class="text-muted fs-7 text-center py-3">{{ t("journal.thread.comment.pending") }}</div>
            <!--end::댓글 영역-->
          </div>
        </div>
        <!--end::Modal Body-->

        <!--begin::Modal Footer-->
        <div class="modal-footer">
          <div class="d-flex justify-content-end">
            <button type="button" class="btn btn-sm btn-light" @click="close">{{ t("common.close") }}</button>
          </div>
        </div>
        <!--end::Modal Footer-->

      </div>
    </div>
  </div>
  <!--end::저널 스레드 상세 모달-->
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from "vue";
import { Modal } from "bootstrap";
import { useJournalThreadStore } from "@/features/journal/stores/journalThread";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

const store = useJournalThreadStore();
const { t } = useLocaleStore();

const modalEl = ref<HTMLElement | null>(null);
let bsModal: InstanceType<typeof Modal> | null = null;

const hasDetailTags = computed(() =>
  Array.isArray(store.detailModel?.tag?.list) && store.detailModel!.tag!.list!.length > 0
);

onMounted(() => {
  if (modalEl.value) {
    bsModal = new Modal(modalEl.value);
    modalEl.value.addEventListener("hidden.bs.modal", () => {
      store.closeDetail();
    });
  }
});

watch(
  () => store.detailOpen,
  (isOpen) => {
    if (isOpen) bsModal?.show();
    else bsModal?.hide();
  }
);

function close() {
  store.closeDetail();
}
</script>
