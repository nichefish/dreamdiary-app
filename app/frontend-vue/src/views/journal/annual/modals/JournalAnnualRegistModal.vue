<template>
  <!--begin::저널 결산 등록/수정 모달-->
  <div ref="modalEl" class="modal fade" id="journal_annual_regist_modal" tabindex="-1" aria-hidden="true" data-bs-backdrop="static" data-bs-keyboard="false">
    <div class="modal-dialog modal-xl">
      <div class="modal-content">

        <!--begin::Modal Header-->
        <div class="modal-header">
          <h5 class="modal-title">{{ isModify ? '저널 결산 수정' : '저널 결산 등록' }}</h5>
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
          <!--begin::로딩-->
          <div v-if="store.registLoading" class="d-flex justify-content-center py-10">
            <span class="spinner-border text-primary" role="status"></span>
          </div>
          <!--end::로딩-->

          <form v-else-if="model" id="journalAnnualRegistForm" class="form" @submit.prevent>
            <input type="hidden" name="id" :value="model.id ?? ''" />
            <input type="hidden" name="yy" :value="model.yy ?? ''" />
            <input type="hidden" name="contentType" value="JOURNAL_ANNUAL" />

            <!--begin::연도-->
            <div class="row d-flex mb-8">
              <div class="col-2">
                <label class="d-flex align-items-center mb-2">
                  <span class="text-gray-700 fs-6 fw-bolder">연도</span>
                </label>
              </div>
              <div class="col-4 fs-6 fw-bold">
                <i class="bi bi-calendar3 me-1"></i>{{ model.yy }}
              </div>
            </div>
            <!--end::연도-->

            <!--begin::제목-->
            <div class="row d-flex mb-8">
              <div class="col-12">
                <label class="d-flex align-items-center mb-2">
                  <span class="text-gray-700 fs-6 fw-bolder">제목</span>
                  <span class="text-gray-500 fs-9 ms-2">(최대 100자)</span>
                </label>
              </div>
              <div class="col-12">
                <input
                  type="text"
                  name="title"
                  class="form-control"
                  v-model="model.title"
                  placeholder="제목"
                  maxlength="100"
                />
              </div>
            </div>
            <!--end::제목-->

            <!--begin::본문-->
            <div class="row d-flex mb-8">
              <div class="col-12">
                <label class="d-flex align-items-center mb-2">
                  <span class="text-gray-700 fs-6 fw-bolder">내용</span>
                </label>
                <RichEditor v-model="model.content" />
              </div>
            </div>
            <!--end::본문-->

            <!--begin::태그-->
            <div class="row d-flex mb-8">
              <div class="col-12">
                <label class="d-flex align-items-center mb-2">
                  <span class="text-gray-700 fs-6 fw-bolder">태그</span>
                </label>
                <TagifyEditor v-model="tagListStrWithCtgr" />
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
              :disabled="store.submitting"
              @click="submit"
            >
              <span v-if="store.submitting" class="spinner-border spinner-border-sm me-1" role="status"></span>
              저장
            </button>
            <button
              type="button"
              class="btn btn-sm"
              :class="closeArmed ? 'btn-warning' : 'btn-light'"
              @click="requestSafeClose"
            >{{ closeArmed ? '한 번 더 클릭해 닫기' : '닫기' }}</button>
          </div>
        </div>
        <!--end::Modal Footer-->

      </div>
    </div>
  </div>
  <!--end::저널 결산 등록/수정 모달-->
</template>

<script setup lang="ts">
import { swalConfirm, swalAlert } from "@/utils/swal";
import { useSafeModalClose } from "@/utils/safeModalClose";
import { ref, computed, watch, onMounted } from "vue";
import RichEditor from "@/views/common/editor/RichEditor.vue";
import TagifyEditor from "@/views/common/tag/TagifyEditor.vue";
import { Modal } from "bootstrap";
import { useJournalAnnualStore } from "@/stores/journalAnnual";

const store = useJournalAnnualStore();

const modalEl = ref<HTMLElement | null>(null);
let bsModal: InstanceType<typeof Modal> | null = null;
const { closeArmed, requestSafeClose, resetSafeClose } = useSafeModalClose(() => {
  store.closeRegist();
});

const model = computed(() => store.registModel);
const isModify = computed(() => !!model.value?.id);

const tagListStrWithCtgr = computed({
  get: () => model.value?.tag?.tagListStrWithCtgr ?? "",
  set: (v: string) => {
    if (!model.value) return;
    model.value.tag = model.value.tag ?? {};
    model.value.tag.tagListStrWithCtgr = v;
  },
});

onMounted(() => {
  if (modalEl.value) {
    bsModal = new Modal(modalEl.value, { backdrop: "static", keyboard: false });
    modalEl.value.addEventListener("hidden.bs.modal", () => {
      resetSafeClose();
      store.closeRegist();
    });
  }
});

watch(
  () => store.registOpen,
  (isOpen) => {
    if (isOpen) {
      resetSafeClose();
      bsModal?.show();
    } else bsModal?.hide();
  }
);

function close() {
  resetSafeClose();
  store.closeRegist();
}

async function submit() {
  if (!model.value?.yy) return;
  const confirmed = await swalConfirm(isModify.value ? "수정하시겠습니까?" : "등록하시겠습니까?");
  if (!confirmed) return;
  await store.submitRegist();
}
</script>
