<template>
  <!--begin::저널 해석 등록/수정 모달-->
  <div ref="modalEl" class="modal fade" id="journal_interpretation_regist_modal" tabindex="-1" aria-hidden="true" data-bs-backdrop="static" data-bs-keyboard="false">
    <div class="modal-dialog modal-xl">
      <div class="modal-content">

        <!--begin::Modal Header-->
        <div class="modal-header">
          <h5 class="modal-title">저널 해석 등록/수정</h5>
          <button
            type="button"
            class="btn-close"
            :title="closeArmed ? '한 번 더 클릭하면 닫힙니다' : '닫기'"
            @click="requestSafeClose"
          ></button>
        </div>
        <!--end::Modal Header-->

        <!--begin::Modal Body-->
        <div class="modal-body modal-mbl-body my-5">
          <form v-if="model" id="journalInterpretationRegistForm" class="form" @submit.prevent>
            <input type="hidden" name="id" :value="model.id ?? ''" />
            <input type="hidden" name="refId" :value="model.refId ?? ''" />
            <input type="hidden" name="refContentType" :value="model.refContentType ?? ''" />

            <!--begin::날짜-->
            <div class="row d-flex mb-8">
              <div class="col-2">
                <label class="d-flex align-items-center mb-2">
                  <span class="text-gray-700 fs-6 fw-bolder">날짜</span>
                </label>
              </div>
              <div class="col-4 fs-6">
                <i class="bi bi-calendar3"></i>
                {{ model.stdrdDt ?? '' }}
                <span v-if="model.journalDateWeekDay" class="fs-8 text-gray-600">({{ model.journalDateWeekDay }})</span>
              </div>
            </div>
            <!--end::날짜-->

            <!--begin::카테고리 + 제목-->
            <div class="row d-flex mb-8">
              <div class="col-12">
                <label class="d-flex align-items-center mb-2">
                  <span class="text-gray-700 fs-6 fw-bolder">제목</span>
                  <span class="text-gray-500 fs-9 ms-2">(최대 100자)</span>
                </label>
              </div>
              <div class="col-lg-2">
                <select name="ctgrCd" id="ctgrCd" class="form-select form-select-solid" v-model="model.ctgrCd">
                  <option value="">-- 카테고리 선택 --</option>
                  <!--TODO: 해석 카테고리 옵션 서버 조회 미구현-->
                </select>
              </div>
              <div :class="isModify ? 'col-lg-9' : 'col-lg-10'">
                <input
                  type="text"
                  name="title"
                  id="title"
                  class="form-control"
                  v-model="model.title"
                  placeholder="제목"
                  maxlength="100"
                />
              </div>
              <div v-if="isModify" class="col-1 d-flex ps-0">
                <div class="d-flex-center p-2 fw-bold fs-5 text-gray-600">#</div>
                <input
                  type="number"
                  class="form-control form-control-sm"
                  name="sortOrder"
                  id="sortOrder"
                  min="1"
                  max="99"
                  v-model="model.sortOrder"
                  placeholder="순서"
                  maxlength="3"
                />
              </div>
            </div>
            <!--end::카테고리 + 제목-->

            <!--begin::본문-->
            <div class="row d-flex mb-8">
              <div class="col-12">
                <label class="d-flex align-items-center mb-2">
                  <span class="text-gray-700 fs-6 fw-bolder">본문</span>
                </label>
                <RichEditor v-model="model.content" />
              </div>
            </div>
            <!--end::본문-->

          </form>
        </div>
        <!--end::Modal Body-->

        <!--begin::Modal Footer-->
        <div class="modal-footer">
          <div class="d-flex justify-content-end gap-2">
            <button
              type="button"
              class="btn btn-sm btn-primary"
              :disabled="submitting"
              @click="submit"
            >
              <span v-if="submitting" class="spinner-border spinner-border-sm me-1" role="status"></span>
              저장
            </button>
            <button
              type="button"
              class="btn btn-sm"
              :class="closeArmed ? 'btn-light-warning' : 'btn-light'"
              :title="closeArmed ? '한 번 더 클릭하면 닫힙니다' : '닫기'"
              @click="requestSafeClose"
            >닫기</button>
          </div>
        </div>
        <!--end::Modal Footer-->

      </div>
    </div>
  </div>
  <!--end::저널 해석 등록/수정 모달-->
</template>

<script setup lang="ts">
import { swalConfirm, swalAlert } from "@/utils/swal";
import { isAuthExpiredError } from "@/utils/authError";
import { useSafeModalClose } from "@/utils/safeModalClose";
import { ref, computed, watch, onMounted } from "vue";
import RichEditor from "@/views/common/editor/RichEditor.vue";
import { Modal } from "bootstrap";
import axios from "axios";
import { useJournalModalStore } from "@/stores/journalModal";
import { useJournalStore } from "@/stores/journal";

const modalStore = useJournalModalStore();
const journalStore = useJournalStore();

const modalEl = ref<HTMLElement | null>(null);
const submitting = ref(false);
let bsModal: InstanceType<typeof Modal> | null = null;
const { closeArmed, requestSafeClose, resetSafeClose } = useSafeModalClose(() => {
  modalStore.closeInterpretationReg();
});

const model = computed(() => modalStore.interpretationRegModel);

/** 수정 모드 여부 */
const isModify = computed(() => !!model.value?.id);

onMounted(() => {
  if (modalEl.value) {
    bsModal = new Modal(modalEl.value, { backdrop: "static", keyboard: false });
    /* bootstrap 이벤트로 store와 상태를 동기화한다 */
    modalEl.value.addEventListener("hidden.bs.modal", () => {
      resetSafeClose();
      modalStore.closeInterpretationReg();
    });
  }
});

watch(
  () => modalStore.interpretationRegOpen,
  (isOpen) => {
    if (isOpen) {
      resetSafeClose();
      bsModal?.show();
    } else bsModal?.hide();
  }
);

function close() {
  resetSafeClose();
  modalStore.closeInterpretationReg();
}

/** 등록/수정 처리 (axios multipart). 해석 API는 등록/수정 모두 POST. */
async function submit() {
  if (!model.value) return;
  if (!model.value.title) {
    void swalAlert("제목을 입력해 주세요.");
    return;
  }

  const confirmed = await swalConfirm(isModify.value ? "수정하시겠습니까?" : "등록하시겠습니까?");
  if (!confirmed) return;

  submitting.value = true;
  try {
    const formData = new FormData();
    if (model.value.id) formData.append("id", String(model.value.id));
    formData.append("refId", String(model.value.refId ?? ""));
    formData.append("refContentType", model.value.refContentType ?? "");
    formData.append("ctgrCd", model.value.ctgrCd ?? "");
    formData.append("title", model.value.title ?? "");
    formData.append("content", model.value.content ?? "");
    if (model.value.sortOrder != null) formData.append("sortOrder", String(model.value.sortOrder));

    /* 해석 등록/수정 API는 모두 POST (backend @PostMapping) */
    const url = isModify.value
      ? `/api/journal/interpretation/${model.value.id}`
      : "/api/journal/interpretations";
    const res = await axios.post(url, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });

    if (res.data?.rslt) {
      close();
      void journalStore.fetchDays();
    } else {
      void swalAlert(res.data?.message ?? "처리에 실패했습니다.");
    }
  } catch (e: unknown) {
    if (isAuthExpiredError(e)) return;
    void swalAlert("요청 처리 중 오류가 발생했습니다.");
  } finally {
    submitting.value = false;
  }
}
</script>
