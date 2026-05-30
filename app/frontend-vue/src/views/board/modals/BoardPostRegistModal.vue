<template>
  <!--begin::게시판 게시물 등록/수정 모달-->
  <div ref="modalEl" class="modal fade" id="board_post_regist_modal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-xl">
      <div class="modal-content">

        <!--begin::Modal Header-->
        <div class="modal-header">
          <h5 class="modal-title">{{ isModify ? '게시물 수정' : '게시물 등록' }}</h5>
          <button type="button" class="btn-close" @click="close"></button>
        </div>
        <!--end::Modal Header-->

        <!--begin::Modal Body-->
        <div class="modal-body modal-mbl-body my-5">
          <!--begin::로딩-->
          <div v-if="store.registLoading" class="d-flex justify-content-center py-10">
            <span class="spinner-border text-primary" role="status"></span>
          </div>
          <!--end::로딩-->

          <form v-else-if="model" id="boardPostRegistForm" class="form" @submit.prevent>
            <input type="hidden" name="id" :value="model.id ?? ''" />
            <input type="hidden" name="contentType" :value="model.contentType ?? ''" />

            <!--begin::제목-->
            <div class="row d-flex mb-8">
              <div class="col-12">
                <label class="d-flex align-items-center mb-2">
                  <span class="text-gray-700 fs-6 fw-bolder">제목</span>
                </label>
                <input
                  name="title"
                  v-model="model.title"
                  class="form-control form-control-solid"
                  placeholder="제목을 입력하세요."
                  maxlength="100"
                  autocomplete="off"
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
            <button type="button" class="btn btn-sm btn-light" @click="close">닫기</button>
          </div>
        </div>
        <!--end::Modal Footer-->

      </div>
    </div>
  </div>
  <!--end::게시판 게시물 등록/수정 모달-->
</template>

<script setup lang="ts">
import { swalConfirm, swalAlert } from "@/utils/swal";
import { ref, computed, watch, onMounted } from "vue";
import RichEditor from "@/views/common/editor/RichEditor.vue";
import TagifyEditor from "@/views/common/tag/TagifyEditor.vue";
import { Modal } from "bootstrap";
import { useBoardPostStore } from "@/stores/boardPost";

const store = useBoardPostStore();

const modalEl = ref<HTMLElement | null>(null);
let bsModal: InstanceType<typeof Modal> | null = null;

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
    bsModal = new Modal(modalEl.value);
    modalEl.value.addEventListener("hidden.bs.modal", () => {
      store.closeRegist();
    });
  }
});

watch(
  () => store.registOpen,
  (isOpen) => {
    if (isOpen) bsModal?.show();
    else bsModal?.hide();
  }
);

function close() {
  store.closeRegist();
}

async function submit() {
  const confirmed = await swalConfirm(isModify.value ? "수정하시겠습니까?" : "등록하시겠습니까?");
  if (!confirmed) return;
  await store.submitRegist();
}
</script>
