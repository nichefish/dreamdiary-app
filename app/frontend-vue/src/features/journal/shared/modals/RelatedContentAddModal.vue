<template>
  <!--begin::관련 글 추가 모달-->
  <div ref="modalEl" class="modal fade" id="related_content_add_modal" tabindex="-1" aria-hidden="true" data-bs-backdrop="static" data-bs-keyboard="false">
    <div class="modal-dialog modal-lg modal-dialog-centered">
      <div class="modal-content">

        <!--begin::Modal Header-->
        <div class="modal-header">
          <h5 class="modal-title fw-bold">{{ t("related-content.modal.title") }}</h5>
          <button
            type="button"
            class="btn-close"
            :title="closeArmed ? t('common.modal.close-armed.tooltip') : t('common.close')"
            @click="requestSafeClose"
          ></button>
        </div>
        <!--end::Modal Header-->

        <!--begin::Modal Body-->
        <div class="modal-body px-5 py-6">
          <!--begin::출처 표시-->
          <div class="rounded bg-light-primary text-primary px-4 py-3 fs-7 mb-4">
            {{ t("related-content.current-post") }}: {{ srcLabel }} #{{ attachableStore.relatedSrcId }}
          </div>
          <!--end::출처-->

          <!--begin::관련 유형 + 대상 유형 + 검색-->
          <div class="row g-3 mb-4">
            <div class="col-md-4">
              <label class="form-label fw-semibold text-gray-700">{{ t("related-content.relation-type") }}</label>
              <select v-model="attachableStore.relatedRelationType" class="form-select form-select-solid">
                <option value="REFERENCE">{{ t("enum.relation-type.reference") }}</option>
                <option value="EXTENSION">{{ t("enum.relation-type.extension") }}</option>
                <option value="PARALLEL">{{ t("enum.relation-type.parallel") }}</option>
                <option value="CAUSE">{{ t("enum.relation-type.cause") }}</option>
              </select>
            </div>
            <div class="col-md-4">
              <label class="form-label fw-semibold text-gray-700">{{ t("related-content.target-content-type") }}</label>
              <select
                v-model="attachableStore.relatedTargetContentType"
                class="form-select form-select-solid"
                @change="attachableStore.onRelatedTargetTypeChange()"
              >
                <option value="JOURNAL_DIARY">{{ t("related-content.content-type.diary") }}</option>
                <option value="JOURNAL_DREAM">{{ t("related-content.content-type.dream") }}</option>
              </select>
            </div>
            <div class="col-md-4 d-flex align-items-end">
              <button type="button" class="btn btn-light-account w-100" @click="search">{{ t("common.search") }}</button>
            </div>
          </div>
          <!--end::관련 유형 + 대상 유형 + 검색-->

          <!--begin::키워드 입력-->
          <div class="mb-4">
            <label class="form-label fw-semibold text-gray-700">{{ t("related-content.search.keyword") }}</label>
            <input
              type="text"
              v-model="attachableStore.relatedKeyword"
              class="form-control form-control-solid"
              maxlength="100"
              :placeholder="t('related-content.search.placeholder')"
              @keydown="onKeydown"
            />
            <div class="text-muted fs-8 mt-2">{{ t("related-content.search.guide") }}</div>
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
            {{ t("related-content.selected.empty") }}
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
            <span class="spinner-border spinner-border-sm me-2"></span>{{ t("related-content.search.loading") }}
          </div>
          <!--end::검색 중-->

          <!--begin::검색 결과-->
          <template v-else-if="attachableStore.relatedSearchAttempted">
            <div
              v-if="attachableStore.relatedSearchResults.length === 0"
              class="rounded border border-dashed border-gray-300 px-4 py-3 text-muted fs-7 mb-4"
            >
              {{ t("common.search.rslt.empty") }}
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
                <div class="text-muted fs-7">{{ toPreviewText(item.content) || t("related-content.preview.empty") }}</div>
              </button>
            </div>
          </template>
          <!--end::검색 결과-->

          <!--begin::메모-->
          <div>
            <label class="form-label fw-semibold text-gray-700">{{ t("related-content.memo") }}</label>
            <textarea
              v-model="attachableStore.relatedReason"
              class="form-control form-control-solid"
              rows="3"
              maxlength="255"
              :placeholder="t('related-content.memo.placeholder')"
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
              {{ t("common.save") }}
            </button>
            <button
              type="button"
              class="btn btn-sm"
              :class="closeArmed ? 'btn-light-warning' : 'btn-light'"
              :title="closeArmed ? t('common.modal.close-armed.tooltip') : t('common.cancel')"
              @click="requestSafeClose"
            >{{ t("common.cancel") }}</button>
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
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { useRoute } from "vue-router";
import { refreshJournalDaysForRoute } from "@/features/journal/utils/journalDayRefresh";

const attachableStore = useAttachableModalStore();
const journalStore = useJournalStore();
const { t } = useLocaleStore();
const route = useRoute();

const modalEl = ref<HTMLElement | null>(null);
const saving = ref(false);
let bsModal: InstanceType<typeof Modal> | null = null;
const { closeArmed, requestSafeClose, resetSafeClose } = useSafeModalClose(() => {
  attachableStore.closeRelated();
});

/** 콘텐츠 타입 → 한글 레이블 */
/** 변경: 위 기존 한글 전용 계약을 현재 locale catalog 레이블로 확장한다. */
const srcLabel = computed(() =>
  contentTypeLabel(attachableStore.relatedSrcContentType)
);

function contentTypeLabel(contentType: string): string {
  if (contentType === "JOURNAL_DIARY") return t("related-content.content-type.diary");
  if (contentType === "JOURNAL_DREAM") return t("related-content.content-type.dream");
  return contentType;
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
      await swalAlert(result.message ?? t("common.result.saved"));
      void refreshJournalDaysForRoute(journalStore, route);
    } else if (result.message) {
      void swalAlert(result.message);
    }
  } finally {
    saving.value = false;
  }
}
</script>
