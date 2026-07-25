<template>
  <!--begin::저널 스레드 상세 모달-->
  <div
    ref="modalEl"
    class="modal fade"
    id="journal_thread_detail_modal"
    tabindex="-1"
    aria-hidden="true"
    data-bs-backdrop="static"
    data-bs-keyboard="false"
  >
    <div class="modal-dialog modal-xxl">
      <div class="modal-content">

        <!--begin::Modal Header-->
        <div class="modal-header">
          <h5 class="modal-title">{{ t("journal.thread.detail.modal.title") }}</h5>
          <div class="d-flex align-items-center gap-2">
            <button
              v-if="store.detailModel?.id"
              type="button"
              class="btn btn-sm btn-light-primary"
              :title="t('common.open-in-new-window')"
              @click="openModifyPopup"
            >
              <i class="bi bi-pencil-square me-1"></i>
              {{ t("common.mdf") }}
            </button>
            <button type="button" class="btn-close" @click="close"></button>
          </div>
        </div>
        <!--end::Modal Header-->

        <!--begin::Modal Body-->
        <div class="modal-body modal-mbl-body my-5">
          <!--begin::로딩-->
          <div v-if="store.detailLoading" class="d-flex justify-content-center py-10">
            <span class="spinner-border text-primary" role="status"></span>
          </div>
          <!--end::로딩-->

          <JournalThreadDetailContent v-else-if="store.detailModel" />
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
import { ref, watch, onBeforeUnmount, onMounted } from "vue";
import { useRoute } from "vue-router";
import { Modal } from "bootstrap";
import { useJournalThreadStore } from "@/features/journal/stores/journalThread";
import { useJournalStore } from "@/features/journal/stores/journal";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { swalAlert } from "@/shared/utils/swal";
import { joinAppBasePath } from "@/shared/utils/appPath";
import JournalThreadDetailContent from "@/features/journal/thread/components/JournalThreadDetailContent.vue";
import { refreshJournalEntryHostForRoute } from "@/features/journal/utils/journalEntryHostRefresh";
import { parseJournalThreadUpdatedMessage } from "@/features/journal/thread/utils/journalThreadPopupMessage";

const store = useJournalThreadStore();
const journalStore = useJournalStore();
const route = useRoute();
const { t } = useLocaleStore();

const modalEl = ref<HTMLElement | null>(null);
let bsModal: InstanceType<typeof Modal> | null = null;

onMounted(() => {
  window.addEventListener("message", onThreadPopupMessage);
  if (modalEl.value) {
    bsModal = new Modal(modalEl.value, { backdrop: "static", keyboard: false });
    modalEl.value.addEventListener("hidden.bs.modal", () => {
      /*
       * 독립 상세 페이지로 표면이 전환되며 모달이 숨은 경우에는 페이지 데이터를 닫지 않는다.
       * 사용자가 모달 자체를 숨긴 경우에만 상세 SSOT를 정리한다.
       */
      if (store.detailSurface === "modal") store.closeDetail();
    });
    /*
     * 저널 문맥형 호출이 전역 모달 마운트보다 먼저 modal 상세 상태를 켠 경우에도
     * 초기 상태를 놓치지 않고 같은 모달 인스턴스를 표시한다. 독립 상세 route는 page 표면이라 제외한다.
     */
    if (store.detailOpen && store.detailSurface === "modal") bsModal.show();
  }
});

onBeforeUnmount(() => {
  window.removeEventListener("message", onThreadPopupMessage);
});

watch(
  () => store.detailOpen && store.detailSurface === "modal",
  (isOpen) => {
    if (isOpen) bsModal?.show();
    else bsModal?.hide();
  }
);

function close() {
  store.closeDetail();
}

/** 현재 상세를 유지하고 스레드 자체 수정 route를 이름 있는 새 창으로 연다. */
function openModifyPopup(): void {
  const id = Number(store.detailModel?.id);
  if (!Number.isInteger(id) || id <= 0) {
    console.warn("[journal-thread] modify popup skipped: detail id is missing");
    return;
  }
  const width = Math.min(1600, window.screen.availWidth);
  const height = Math.min(1080, window.screen.availHeight);
  const url = joinAppBasePath(`/thread/${id}/edit?popup=Y`);
  const popup = window.open(
    url,
    `journal-thread-edit-${id}`,
    `width=${width},height=${height},top=0,left=270`,
  );
  if (!popup) {
    console.warn("[journal-thread] modify popup blocked", { id, url });
    void swalAlert(t("common.error.popup"));
    return;
  }
  popup.focus();
}

/**
 * 수정 팝업의 동일 출처 완료 메시지를 받아 현재 열린 스레드만 갱신한다.
 * 주간·월간·일간 배경은 기존 호스트 갱신 계약으로 함께 갱신하되 스크롤하지 않는다.
 */
function onThreadPopupMessage(event: MessageEvent): void {
  const threadId = parseJournalThreadUpdatedMessage(event.data);
  if (!threadId) return;
  if (event.origin !== window.location.origin) {
    console.warn("[journal-thread] ignored cross-origin modify message", {
      origin: event.origin,
      expectedOrigin: window.location.origin,
    });
    return;
  }
  if (!store.detailOpen || Number(store.detailModel?.id) !== threadId) {
    console.info("[journal-thread] ignored stale modify message", {
      threadId,
      detailOpen: store.detailOpen,
      detailId: store.detailModel?.id,
    });
    return;
  }
  void refreshJournalEntryHostForRoute(journalStore, store, route);
}

</script>
