<template>
  <!--begin::저널 할일 등록/수정 모달-->
  <div ref="modalEl" class="modal fade" id="journal_todo_regist_modal" tabindex="-1" aria-hidden="true" data-bs-backdrop="static" data-bs-keyboard="false">
    <div class="modal-dialog modal-xl">
      <div class="modal-content">

        <!--begin::Modal Header-->
        <div class="modal-header">
          <h5 class="modal-title">{{ isModify ? '저널 할일 수정' : '저널 할일 등록' }}</h5>
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
          <form v-if="model" id="journalTodoRegistForm" class="form" @submit.prevent>
            <input type="hidden" name="id" :value="model.id ?? ''" />
            <input type="hidden" name="yy" :value="model.yy ?? ''" />
            <input type="hidden" name="mnth" :value="model.mnth ?? ''" />

            <!--begin::날짜-->
            <div class="row d-flex mb-8">
              <div class="col-2">
                <label class="d-flex align-items-center mb-2">
                  <span class="text-gray-700 fs-6 fw-bolder">날짜</span>
                </label>
              </div>
              <div class="col-4 fs-6">
                <i class="bi bi-calendar3"></i>
                {{ model.yy }}년 {{ model.mnth }}월
              </div>
            </div>
            <!--end::날짜-->

            <!--begin::제목 (카테고리 + 제목 + 순서)-->
            <div class="row d-flex mb-8">
              <div class="col-12">
                <label class="d-flex align-items-center mb-2">
                  <span class="text-gray-700 fs-6 fw-bolder">제목</span>
                  <span class="text-gray-500 fs-9 ms-2">(최대 50자)</span>
                </label>
              </div>
              <div class="col-lg-2">
                <!--begin::카테고리 (dead UI 보존)-->
                <select name="categoryCode" id="categoryCode" class="form-select form-select-solid" v-model="model.categoryCode">
                  <option value="">-- 카테고리 선택 --</option>
                </select>
                <!--end::카테고리-->
              </div>
              <div :class="isModify ? 'col-lg-9' : 'col-lg-10'">
                <input
                  type="text"
                  name="title"
                  id="title"
                  class="form-control form-control-solid"
                  v-model="model.title"
                  placeholder="제목을 입력하세요."
                  maxlength="50"
                  required
                />
              </div>
              <div v-if="isModify" class="col-lg-1">
                <input
                  type="number"
                  name="sortOrder"
                  id="sortOrder"
                  class="form-control form-control-solid"
                  v-model.number="model.sortOrder"
                  placeholder="순서"
                />
              </div>
            </div>
            <!--end::제목-->

            <!--begin::내용-->
            <div class="row mb-4">
              <div class="col-12">
                <label class="d-flex align-items-center mb-2" for="content">
                  <span class="text-gray-700 fs-6 fw-bolder">내용</span>
                </label>
              </div>
              <div class="col-12">
                <RichEditor v-model="model.content" :height="350" />
              </div>
            </div>
            <!--end::내용-->

            <!--begin::태그-->
            <div class="row mb-3">
              <div>
                <label for="tagListStr" class="mb-2">
                  <span class="text-gray-700 fs-6 fw-bolder">태그</span>
                  <span class="text-gray-500 fs-9 mx-2">([카테고리]태그명 형식)</span>
                </label>
              </div>
              <div class="col-xl-12">
                <input
                  name="tag.tagListStr"
                  id="tagListStr"
                  class="form-control form-control-solid"
                  autocomplete="off"
                  maxlength="500"
                  v-model="tagListStrWithCtgr"
                  placeholder="태그 입력"
                />
              </div>
            </div>
            <!--end::태그-->

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
  <!--end::저널 할일 등록/수정 모달-->
</template>

<script setup lang="ts">
import { swalConfirm, swalAlert, swalRequestError } from "@/shared/utils/swal";
import { useSafeModalClose } from "@/shared/utils/safeModalClose";
import { ref, computed, watch, onMounted } from "vue";
import RichEditor from "@/shared/ui/editor/RichEditor.vue";
import { Modal } from "bootstrap";
import axios from "axios";
import { useJournalModalStore } from "@/features/journal/stores/journalModal";
import { useJournalStore } from "@/features/journal/stores/journal";
import { useRoute } from "vue-router";
import { refreshJournalDaysForRoute } from "@/features/journal/utils/journalDayRefresh";

const modalStore = useJournalModalStore();
const journalStore = useJournalStore();
const route = useRoute();

const modalEl = ref<HTMLElement | null>(null);
const submitting = ref(false);
let bsModal: InstanceType<typeof Modal> | null = null;
const { closeArmed, requestSafeClose, resetSafeClose } = useSafeModalClose(() => {
  modalStore.closeTodoRegist();
});

const model = computed(() => modalStore.todoRegistModel);
const isModify = computed(() => !!model.value?.id);

/** 태그 문자열 바인딩 */
const tagListStrWithCtgr = computed({
  get: () => model.value?.tag?.tagListStrWithCtgr ?? "",
  set: (v) => { if (model.value?.tag) model.value.tag.tagListStrWithCtgr = v; },
});

onMounted(() => {
  if (modalEl.value) {
    bsModal = new Modal(modalEl.value, { backdrop: "static", keyboard: false });
    modalEl.value.addEventListener("hidden.bs.modal", () => {
      resetSafeClose();
      modalStore.closeTodoRegist();
    });
  }
});

watch(
  () => modalStore.todoRegistOpen,
  (isOpen) => {
    if (isOpen) {
      resetSafeClose();
      bsModal?.show();
    } else bsModal?.hide();
  }
);

function close() {
  resetSafeClose();
  modalStore.closeTodoRegist();
}

/** 등록/수정 처리 */
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
    if (isModify.value) formData.append("id", String(model.value.id));
    formData.append("yy", String(model.value.yy ?? ""));
    formData.append("mnth", String(model.value.mnth ?? ""));
    formData.append("categoryCode", model.value.categoryCode ?? "");
    formData.append("title", model.value.title ?? "");
    if (model.value.sortOrder != null) formData.append("sortOrder", String(model.value.sortOrder));
    formData.append("content", model.value.content ?? "");
    formData.append("tag.tagListStr", tagListStrWithCtgr.value);

    const url = isModify.value
      ? `/api/journal/todo/${model.value.id}`
      : "/api/journal/todos";
    const res = await axios.post(url, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });

    if (res.data?.rslt) {
      close();
      await swalAlert(res.data?.message ?? (isModify.value ? "수정되었습니다." : "등록되었습니다."));
      void refreshJournalDaysForRoute(journalStore, route);
    } else {
      void swalAlert(res.data?.message ?? "처리에 실패했습니다.");
    }
  } catch (e: unknown) {
    void swalRequestError(e);
  } finally {
    submitting.value = false;
  }
}
</script>
