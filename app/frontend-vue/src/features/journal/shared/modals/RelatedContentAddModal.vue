<template>
  <!--begin::관련 글 추가 모달-->
  <div ref="modalEl" class="modal fade" id="related_content_add_modal" tabindex="-1" aria-hidden="true" data-bs-backdrop="static" data-bs-keyboard="false">
    <div class="modal-dialog modal-lg modal-dialog-centered">
      <div class="modal-content">

        <!--begin::Modal Header-->
        <div class="modal-header">
          <h5 class="modal-title fw-bold">관련 글 추가</h5>
          <button
            type="button"
            class="btn-close"
            :title="closeArmed ? '한 번 더 클릭하면 닫힙니다' : '닫기'"
            @click="requestSafeClose"
          ></button>
        </div>
        <!--end::Modal Header-->

        <!--begin::Modal Body-->
        <div class="modal-body px-5 py-6">
          <!--begin::출처 표시-->
          <div class="rounded bg-light-primary text-primary px-4 py-3 fs-7 mb-4">
            현재 글: {{ srcLabel }} #{{ attachableStore.relatedSrcId }}
          </div>
          <!--end::출처-->

          <!--begin::관련 유형 + 대상 유형 + 검색-->
          <div class="row g-3 mb-4">
            <div class="col-md-4">
              <label class="form-label fw-semibold text-gray-700">관련 유형</label>
              <select v-model="attachableStore.relatedRelationType" class="form-select form-select-solid">
                <option value="REFERENCE">참조</option>
                <option value="EXTENSION">확장</option>
                <option value="PARALLEL">병렬</option>
                <option value="CAUSE">원인</option>
              </select>
            </div>
            <div class="col-md-4">
              <label class="form-label fw-semibold text-gray-700">대상 글 유형</label>
              <select
                v-model="attachableStore.relatedTargetContentType"
                class="form-select form-select-solid"
                @change="attachableStore.onRelatedTargetTypeChange()"
              >
                <option value="JOURNAL_DIARY">일기</option>
                <option value="JOURNAL_DREAM">꿈</option>
              </select>
            </div>
            <div class="col-md-4 d-flex align-items-end">
              <button type="button" class="btn btn-light-account w-100" @click="search">검색</button>
            </div>
          </div>
          <!--end::관련 유형 + 대상 유형 + 검색-->

          <!--begin::키워드 입력-->
          <div class="mb-4">
            <label class="form-label fw-semibold text-gray-700">검색 키워드</label>
            <input
              type="text"
              v-model="attachableStore.relatedKeyword"
              class="form-control form-control-solid"
              maxlength="100"
              placeholder="제목이나 내용 키워드를 입력해 주세요."
              @keydown="onKeydown"
            />
            <div class="text-muted fs-8 mt-2">검색 결과를 클릭하면 연결 대상이 선택됩니다.</div>
          </div>
          <!--end::키워드 입력-->

          <!--begin::선택된 대상-->
          <div
            v-if="attachableStore.relatedSelectedTarget"
            class="rounded border border-primary bg-light-primary px-4 py-3 text-start mb-4"
          >
            <div class="fw-semibold text-primary mb-1">
              {{ attachableStore.relatedSelectedTarget.title || '#' + attachableStore.relatedSelectedTarget.id }}
            </div>
            <div class="text-muted fs-7">
              {{ contentTypeLabel(attachableStore.relatedSelectedTarget.contentType) }}
              #{{ attachableStore.relatedSelectedTarget.id }}
              <span v-if="attachableStore.relatedSelectedTarget.stdrdDt">
                | {{ attachableStore.relatedSelectedTarget.stdrdDt }}
              </span>
            </div>
          </div>
          <div v-else class="rounded border border-dashed border-gray-300 px-4 py-3 text-muted fs-7 mb-4">
            아직 선택한 글이 없습니다.
          </div>
          <!--end::선택된 대상-->

          <!--begin::유효성 메시지-->
          <div
            v-if="attachableStore.relatedValidationMsg"
            class="rounded border border-dashed border-warning px-4 py-3 text-warning fs-7 mb-4"
          >
            {{ attachableStore.relatedValidationMsg }}
          </div>
          <!--end::유효성 메시지-->

          <!--begin::검색 중-->
          <div
            v-if="attachableStore.relatedSearching"
            class="rounded border border-dashed border-gray-300 px-4 py-3 text-muted fs-7 mb-4"
          >
            <span class="spinner-border spinner-border-sm me-2"></span>검색 중입니다.
          </div>
          <!--end::검색 중-->

          <!--begin::검색 결과-->
          <template v-else-if="attachableStore.relatedSearchAttempted">
            <div
              v-if="attachableStore.relatedSearchResults.length === 0"
              class="rounded border border-dashed border-gray-300 px-4 py-3 text-muted fs-7 mb-4"
            >
              검색 결과가 없습니다.
            </div>
            <div v-for="item in attachableStore.relatedSearchResults" :key="item.id" class="mb-3">
              <button
                type="button"
                :class="[
                  'btn w-100 text-start rounded border px-4 py-3',
                  attachableStore.relatedSelectedTarget?.id === item.id
                    ? 'border-primary bg-light-primary'
                    : 'border-gray-300'
                ]"
                @click="attachableStore.selectRelatedTarget(item)"
              >
                <div class="d-flex align-items-center justify-content-between gap-3 flex-wrap mb-2">
                  <span class="fw-semibold text-gray-900">{{ item.title || '#' + item.id }}</span>
                  <span class="text-muted fs-8">
                    #{{ item.id }}<span v-if="item.stdrdDt"> | {{ item.stdrdDt }}</span>
                  </span>
                </div>
                <div class="text-muted fs-7">{{ toPreviewText(item.content) || '미리보기가 없습니다.' }}</div>
              </button>
            </div>
          </template>
          <!--end::검색 결과-->

          <!--begin::메모-->
          <div>
            <label class="form-label fw-semibold text-gray-700">메모</label>
            <textarea
              v-model="attachableStore.relatedReason"
              class="form-control form-control-solid"
              rows="3"
              maxlength="255"
              placeholder="왜 연결하는지 간단히 적어 둘 수 있습니다."
            ></textarea>
          </div>
          <!--end::메모-->
        </div>
        <!--end::Modal Body-->

        <!--begin::Modal Footer-->
        <div class="modal-footer">
          <div class="d-flex justify-content-end gap-2">
            <button
              type="button"
              class="btn btn-sm btn-primary"
              :disabled="saving"
              @click="save"
            >
              <span v-if="saving" class="spinner-border spinner-border-sm me-1" role="status"></span>
              저장
            </button>
            <button
              type="button"
              class="btn btn-sm"
              :class="closeArmed ? 'btn-light-warning' : 'btn-light'"
              :title="closeArmed ? '한 번 더 클릭하면 닫힙니다' : '취소'"
              @click="requestSafeClose"
            >취소</button>
          </div>
        </div>
        <!--end::Modal Footer-->

      </div>
    </div>
  </div>
  <!--end::관련 글 추가 모달-->
</template>

<script setup lang="ts">
import { swalConfirm, swalAlert } from "@/shared/utils/swal";
import { useSafeModalClose } from "@/shared/utils/safeModalClose";
import { ref, computed, watch, onMounted } from "vue";
import { Modal } from "bootstrap";
import { useAttachableModalStore } from "@/features/attachable/stores/attachableModal";
import { useJournalStore } from "@/features/journal/stores/journal";
import { useRoute } from "vue-router";
import { refreshJournalDaysForRoute } from "@/features/journal/utils/journalDayRefresh";

const attachableStore = useAttachableModalStore();
const journalStore = useJournalStore();
const route = useRoute();

const modalEl = ref<HTMLElement | null>(null);
const saving = ref(false);
let bsModal: InstanceType<typeof Modal> | null = null;
const { closeArmed, requestSafeClose, resetSafeClose } = useSafeModalClose(() => {
  attachableStore.closeRelated();
});

/** 콘텐츠 타입 → 한글 레이블 */
const CONTENT_TYPE_LABEL: Record<string, string> = {
  JOURNAL_DIARY: "일기",
  JOURNAL_DREAM: "꿈",
};

const srcLabel = computed(() =>
  CONTENT_TYPE_LABEL[attachableStore.relatedSrcContentType] ?? attachableStore.relatedSrcContentType
);

function contentTypeLabel(contentType: string): string {
  return CONTENT_TYPE_LABEL[contentType] ?? contentType;
}

/**
 * 본문 미리보기 텍스트 생성 (HTML 제거 + 120자 절단)
 * @param value - 원본 텍스트
 */
function toPreviewText(value: string): string {
  const text = String(value ?? "")
    .replace(/<[^>]+>/g, " ")
    .replace(/\s+/g, " ")
    .trim();
  return text.length <= 120 ? text : text.substring(0, 120) + "...";
}

/**
 * 키워드 입력 keydown 핸들러 — Enter 시 검색
 * @param e - 키보드 이벤트
 */
function onKeydown(e: KeyboardEvent): void {
  if (e.key === "Enter") {
    e.preventDefault();
    void search();
  }
}

async function search() {
  await attachableStore.searchRelatedTargets();
}

onMounted(() => {
  if (modalEl.value) {
    bsModal = new Modal(modalEl.value, { backdrop: "static", keyboard: false });
    /* bootstrap 이벤트로 store 와 상태를 동기화한다 */
    modalEl.value.addEventListener("hidden.bs.modal", () => {
      resetSafeClose();
      attachableStore.closeRelated();
    });
  }
});

watch(
  () => attachableStore.relatedOpen,
  (isOpen) => {
    if (isOpen) {
      resetSafeClose();
      bsModal?.show();
    } else bsModal?.hide();
  }
);

function close() {
  resetSafeClose();
  attachableStore.closeRelated();
}

/** 관련 글 연결 저장 처리. 성공 시 페이지 데이터를 갱신한다. */
async function save() {
  saving.value = true;
  try {
    const result = await attachableStore.saveRelated();
    if (result.rslt) {
      close();
      await swalAlert(result.message ?? "저장되었습니다.");
      void refreshJournalDaysForRoute(journalStore, route);
    } else if (result.message) {
      void swalAlert(result.message);
    }
  } finally {
    saving.value = false;
  }
}
</script>
