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
            <!--begin::복사 (split — 주 버튼=전체/해석 포함, ▾ 드롭다운=본문만/해석 제외)-->
            <div v-if="store.detailModel?.id" class="btn-group" role="group">
              <button
                type="button"
                class="btn btn-sm btn-icon btn-light-primary copy-split-main"
                :title="hasThreadReflections ? t('journal.copy.full.tooltip') : t('common.copy')"
                @click="onCopy(true)"
              >
                <i class="bi bi-copy"></i>
              </button>
              <button
                type="button"
                class="btn btn-sm btn-light-primary copy-split-caret"
                data-kt-menu-trigger="click"
                data-kt-menu-placement="bottom-end"
                :title="t('common.menu')"
              >
                <i class="bi bi-caret-down-fill fs-9 pe-0"></i>
              </button>
              <div
                class="menu menu-sub menu-sub-dropdown menu-column menu-rounded menu-gray-800 menu-state-bg-light-primary fw-semibold w-175px py-2"
                data-kt-menu="true"
              >
                <div class="menu-item px-3 my-1 cursor-pointer">
                  <div class="menu-link flex-stack px-3" @click="onCopy(false)">
                    {{ t('journal.copy.body.label') }}
                    <i class="bi bi-clipboard fs-8"></i>
                  </div>
                </div>
              </div>
            </div>
            <!--end::복사 (split)-->
            <!--begin::다운로드 (split — 주 버튼=전체/해석 포함, ▾ 드롭다운=본문만/해석 제외)-->
            <div v-if="store.detailModel?.id" class="btn-group" role="group">
              <button
                type="button"
                class="btn btn-sm btn-icon btn-light-primary copy-split-main"
                :title="hasThreadReflections ? t('journal.download.full.tooltip') : t('common.export-text.tooltip')"
                @click="onDownload(true)"
              >
                <i class="bi bi-download"></i>
              </button>
              <button
                type="button"
                class="btn btn-sm btn-light-primary copy-split-caret"
                data-kt-menu-trigger="click"
                data-kt-menu-placement="bottom-end"
                :title="t('common.menu')"
              >
                <i class="bi bi-caret-down-fill fs-9 pe-0"></i>
              </button>
              <div
                class="menu menu-sub menu-sub-dropdown menu-column menu-rounded menu-gray-800 menu-state-bg-light-primary fw-semibold w-175px py-2"
                data-kt-menu="true"
              >
                <div class="menu-item px-3 my-1 cursor-pointer">
                  <div class="menu-link flex-stack px-3" @click="onDownload(false)">
                    {{ t('journal.download.body.label') }}
                    <i class="bi bi-download fs-8"></i>
                  </div>
                </div>
              </div>
            </div>
            <!--end::다운로드 (split)-->
            <button
              v-if="store.detailModel?.id"
              type="button"
              class="btn btn-sm btn-light-primary"
              :disabled="!hasHistory"
              :title="t('common.history')"
              @click="hasHistory ? openHistory() : undefined"
            >
              <i class="bi bi-clock-history me-1"></i>
              {{ t("common.history") }}
            </button>
            <button
              v-if="store.detailModel?.id"
              type="button"
              class="btn btn-sm btn-light-primary"
              :title="t('common.mdf')"
              @click="openModify"
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
import { ref, watch, onMounted, computed } from "vue";
import { Modal } from "bootstrap";
import { useJournalThreadStore } from "@/features/journal/stores/journalThread";
import { useAttachableModalStore } from "@/features/attachable/stores/attachableModal";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import JournalThreadDetailContent from "@/features/journal/thread/components/JournalThreadDetailContent.vue";
import { copyThreadDetail, downloadThreadDetail } from "@/features/journal/utils/journalThreadExport";

const store = useJournalThreadStore();
const attachableStore = useAttachableModalStore();
const { t } = useLocaleStore();

/** 현재 스레드에 본문 변경 이력이 하나 이상 존재하는지 여부. */
const hasHistory = computed(() => !!store.detailModel?.history?.historyTriggeredAt);

const modalEl = ref<HTMLElement | null>(null);
let bsModal: InstanceType<typeof Modal> | null = null;

onMounted(() => {
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

/** 현재 저널 문맥을 유지한 채 상세 모달을 같은 앱의 수정 모달로 전환한다. */
function openModify(): void {
  const id = Number(store.detailModel?.id);
  if (!Number.isInteger(id) || id <= 0) {
    console.warn("[journal-thread] modify skipped: detail id is missing");
    return;
  }
  void store.openModifyFromDetail(id);
}

/** 현재 스레드의 본문 이력 모달을 연다. */
function openHistory(): void {
  const id = store.detailModel?.id;
  if (!id) return;
  void attachableStore.openHistory(store.detailModel?.contentType ?? "JOURNAL_THREAD", id);
}

/** 스레드 소속 엔트리 중 하나라도 리플렉션이 있으면 true (▾ 드롭다운 노출 조건). */
const hasThreadReflections = computed(() =>
  store.detailEntries.some((e) => (e.reflectionList?.length ?? 0) > 0),
);

/** 스레드 제목 + 소속 엔트리를 클립보드에 복사한다. (includeReflection: 해석 포함 여부) */
function onCopy(includeReflection = true): void {
  void copyThreadDetail(store.detailModel, store.detailEntries, t, includeReflection);
}

/** 스레드 소속 엔트리를 서버 텍스트 내보내기로 다운로드한다. (includeReflection: 해석 포함 여부) */
function onDownload(includeReflection = true): void {
  const id = store.detailModel?.id;
  if (!id) return;
  downloadThreadDetail(id, includeReflection);
}

</script>
