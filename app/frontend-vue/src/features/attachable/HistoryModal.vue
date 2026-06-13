<template>
  <!--begin::이력 모달-->
  <div ref="modalEl" class="modal fade" id="attachable_history_modal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-xl modal-dialog-centered">
      <div class="modal-content">

        <!--begin::Modal Header-->
        <div class="modal-header">
          <h5 class="modal-title">히스토리</h5>
          <button type="button" class="btn-close" @click="close"></button>
        </div>
        <!--end::Modal Header-->

        <!--begin::Modal Body-->
        <div class="modal-body modal-mbl-body my-5">
          <!--begin::로딩-->
          <div v-if="attachableStore.historyLoading" class="d-flex justify-content-center my-5">
            <span class="spinner-border text-primary" role="status"></span>
          </div>
          <!--end::로딩-->
          <template v-else>
            <!--begin::헤더 (최종 수정일자 + 전체 삭제)-->
            <div class="d-flex justify-content-between align-items-center mb-5">
              <div class="fs-7 text-muted">
                최종 수정일자:
                <span class="text-gray-700">{{ attachableStore.historyTriggeredAt || '-' }}</span>
              </div>
              <button
                v-if="attachableStore.historyList.length > 0"
                type="button"
                class="btn btn-sm btn-light-danger"
                title="이력을 전체 삭제합니다."
                @click="onClear"
              >
                <i class="bi bi-trash3"></i>
                전체 삭제
              </button>
            </div>
            <!--end::헤더-->

            <!--begin::이력 목록-->
            <div class="d-flex flex-column gap-4">
              <template v-if="attachableStore.historyList.length > 0">
                <div
                  v-for="item in attachableStore.historyList"
                  :key="item.id"
                  class="card card-bordered shadow-sm"
                >
                  <div class="card-body py-4 px-5">
                    <div class="d-flex flex-wrap justify-content-between align-items-start gap-3 mb-3">
                      <div class="flex-grow-1">
                        <div class="d-flex flex-wrap align-items-center gap-2 mb-2">
                          <span
                            v-if="item.historyType === 'RESTORE'"
                            class="badge badge-light-warning text-warning"
                          >복구</span>
                          <span v-else class="badge badge-light-primary text-primary">변경</span>
                          <span class="badge badge-light-dark text-dark"># {{ item.id }}</span>
                          <span v-if="item.fromHistoryId" class="fs-8 text-muted">
                            복구 원본 # {{ item.fromHistoryId }}
                          </span>
                        </div>
                        <div class="d-flex flex-wrap gap-4 fs-7">
                          <div>
                            <span class="text-muted">작성일시:</span>
                            <span class="fw-semibold text-dark ms-1">{{ item.createdAt || '-' }}</span>
                          </div>
                          <div>
                            <span class="text-muted">작성자:</span>
                            <span class="fw-semibold text-dark ms-1">{{ item.createdByNm || '-' }}</span>
                          </div>
                        </div>
                      </div>
                      <div class="d-flex gap-2 align-items-center">
                        <!--begin::복사 버튼-->
                        <button
                          type="button"
                          class="btn btn-xs btn-icon btn-bg-light btn-active-color-primary"
                          title="복사"
                          @click="copyHistory(item)"
                        >
                          <i class="bi bi-copy fs-8"></i>
                        </button>
                        <!--end::복사 버튼-->
                        <button
                          type="button"
                          class="btn btn-sm btn-light-primary"
                          title="선택한 히스토리 내용으로 복구합니다."
                          @click="onRestore(item.id)"
                        >
                          <i class="bi bi-arrow-counterclockwise"></i>
                          복구
                        </button>
                        <button
                          type="button"
                          class="btn btn-sm btn-light-danger"
                          title="선택한 히스토리를 삭제합니다."
                          @click="onDelete(item.id)"
                        >
                          <i class="bi bi-trash"></i>
                          삭제
                        </button>
                      </div>
                    </div>
                    <!--begin::요약-->
                    <div class="journal-history-preview fs-7 text-gray-700">
                      <div class="fw-semibold text-muted mb-2">요약</div>
                      {{ item.previewContent || '-' }}
                    </div>
                    <!--end::요약-->
                    <!--begin::상세 (접힘/펼침)-->
                    <div class="mt-3">
                      <button
                        type="button"
                        class="btn btn-sm btn-light"
                        data-bs-toggle="collapse"
                        :data-bs-target="'#attachable_history_detail_' + item.id"
                        aria-expanded="false"
                        :aria-controls="'attachable_history_detail_' + item.id"
                      >
                        <i class="bi bi-layout-text-window"></i>
                        상세
                      </button>
                    </div>
                    <div :id="'attachable_history_detail_' + item.id" class="collapse mt-3">
                      <div class="border rounded bg-light px-4 py-4 fs-7 text-gray-800 journal-history-detail">
                        <div class="fw-semibold text-muted mb-3">상세</div>
                        <span v-if="item.markdownContent" v-html="item.markdownContent"></span>
                        <span v-else>-</span>
                      </div>
                    </div>
                    <!--end::상세-->
                  </div>
                </div>
              </template>
              <div v-else class="d-flex-center min-h-150px text-muted">
                히스토리가 없습니다.
              </div>
            </div>
            <!--end::이력 목록-->
          </template>
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
  <!--end::이력 모달-->
</template>

<script setup lang="ts">
import { swalConfirm, swalAlert } from "@/shared/utils/swal";
import { ref, watch, onMounted } from "vue";
import { Modal } from "bootstrap";
import { useAttachableModalStore } from "@/features/attachable/stores/attachableModal";

/** 성공 시 상위 컴포넌트에 알린다 (목록 갱신 등 후속 처리 위임). */
const emit = defineEmits<{ success: [] }>();

const attachableStore = useAttachableModalStore();

const modalEl = ref<HTMLElement | null>(null);
let bsModal: InstanceType<typeof Modal> | null = null;

onMounted(() => {
  if (modalEl.value) {
    bsModal = new Modal(modalEl.value);
    /* bootstrap 이벤트로 store 와 상태를 동기화한다 */
    modalEl.value.addEventListener("hidden.bs.modal", () => {
      attachableStore.closeHistory();
    });
  }
});

watch(
  () => attachableStore.historyOpen,
  (isOpen) => {
    if (isOpen) bsModal?.show();
    else bsModal?.hide();
  }
);

function close() {
  attachableStore.closeHistory();
}

/** 이력 복원 처리. 성공 시 상위 컴포넌트에 알린다. */
async function onRestore(historyId: number | string) {
  if (!await swalConfirm("선택한 히스토리 내용으로 복구하시겠습니까?")) return;
  const ok = await attachableStore.restoreHistory(historyId);
  if (ok) {
    close();
    emit("success");
  } else {
    void swalAlert("복구에 실패했습니다.");
  }
}

/** 이력 단건 삭제 처리. store 에서 목록을 직접 갱신하므로 모달을 닫지 않는다. */
async function onDelete(historyId: number | string) {
  if (!await swalConfirm("선택한 히스토리를 삭제하시겠습니까?")) return;
  const ok = await attachableStore.deleteHistory(historyId);
  if (!ok) void swalAlert("삭제에 실패했습니다.");
}

/** HTML 마크업을 제거하고 평문으로 변환한다 (복사 시 사용). */
function htmlToPlainText(html: string): string {
  return html
    .replace(/<br\s*\/?>/gi, "\n")
    .replace(/<\/p>/gi, "\n")
    .replace(/<hr\s*\/?>/gi, "\n---\n")
    .replace(/<[^>]+>/g, "")
    .replace(/&nbsp;/g, " ")
    .replace(/&amp;/g, "&")
    .replace(/&lt;/g, "<")
    .replace(/&gt;/g, ">")
    .replace(/&quot;/g, String.fromCharCode(34))
    .trim();
}

/** 히스토리 내용을 클립보드에 복사한다. */
async function copyHistory(item: { markdownContent?: string; previewContent?: string }): Promise<void> {
  const plain = htmlToPlainText(item.markdownContent ?? item.previewContent ?? "");
  try {
    await navigator.clipboard.writeText(plain);
    void swalAlert("클립보드에 복사되었습니다.");
  } catch {
    void swalAlert("복사에 실패했습니다.");
  }
}

/** 이력 전체 삭제 처리. 성공 시 상위 컴포넌트에 알린다. */
async function onClear() {
  if (!await swalConfirm("이력을 전체 삭제하시겠습니까?")) return;
  const ok = await attachableStore.clearHistory();
  if (ok) {
    close();
    emit("success");
  } else {
    void swalAlert("전체 삭제에 실패했습니다.");
  }
}
</script>