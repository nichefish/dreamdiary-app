<template>
  <!--begin::저널 챕터 등록/수정 모달-->
  <div ref="modalEl" class="modal fade" id="journal_chapter_regist_modal" tabindex="-1" aria-hidden="true" data-bs-backdrop="static" data-bs-keyboard="false">
    <div class="modal-dialog modal-xl">
      <div class="modal-content">

        <!--begin::Modal Header-->
        <div class="modal-header">
          <h5 class="modal-title">저널 챕터 등록/수정</h5>
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
          <form v-if="model" id="journalChapterRegistForm" class="form" @submit.prevent>
            <input type="hidden" name="id" :value="model.id ?? ''" />
            <input type="hidden" name="journalDayId" :value="model.journalDayId ?? ''" />

            <!--begin::날짜-->
            <div class="row d-flex mb-8">
              <div class="col-2">
                <label class="d-flex align-items-center mb-2">
                  <span class="text-gray-700 fs-6 fw-bolder">날짜</span>
                </label>
              </div>
              <div class="col-4 fs-6 d-flex align-items-center gap-2">
                <i class="bi bi-calendar3"></i>
                {{ model.stdrdDt }}
                <span v-if="model.journalDateWeekDay" class="fs-8 text-gray-600">({{ model.journalDateWeekDay }})</span>
              </div>
              <!--begin::챕터 일자 변경 (수정 모드, 비DREAM 한정)-->
              <div v-if="isModify && !isModifyDream" class="col-6 d-flex align-items-center gap-2">
                <input
                  type="date"
                  class="form-control form-control-sm"
                  style="max-width: 160px;"
                  v-model="moveTargetDt"
                />
                <button
                  type="button"
                  class="btn btn-sm btn-light-warning"
                  :disabled="moving || !moveTargetDt"
                  @click="moveChapter"
                >
                  <span v-if="moving" class="spinner-border spinner-border-sm me-1" role="status"></span>
                  챕터 일자 변경
                </button>
              </div>
              <!--end::챕터 일자 변경-->
            </div>
            <!--end::날짜-->

            <!--begin::챕터 유형 + 카테고리 + 제목-->
            <div class="row d-flex mb-8">
              <div class="col-12">
                <label class="d-flex align-items-center mb-2">
                  <span class="text-gray-700 fs-6 fw-bolder">제목</span>
                  <span class="text-gray-500 fs-9 ms-2">(최대 100자)</span>
                </label>
              </div>
              <div class="col-lg-2">
                <!--begin::DREAM 수정 시 고정 표시-->
                <template v-if="isModifyDream">
                  <input type="hidden" name="chapterType" value="DREAM" />
                  <div class="form-control form-control-solid form-control-sm d-flex align-items-center min-h-40px">
                    <span class="fw-bolder">꿈</span>
                    <span class="text-muted fs-8 ms-2">(자동)</span>
                  </div>
                </template>
                <!--begin::일반 챕터 유형 선택-->
                <template v-else>
                  <select name="chapterType" id="chapterType" class="form-select form-select-solid" v-model="model.chapterType">
                    <option value="DIARY">일기</option>
                    <option value="NOTE">노트</option>
                  </select>
                </template>
              </div>
              <div class="col-lg-2">
                <select name="categoryCode" id="categoryCode" class="form-select form-select-solid" v-model="model.categoryCode">
                  <option value="">-- 카테고리 선택 --</option>
                  <option
                    v-for="ctgr in currentCategoryOptions"
                    :key="ctgr.code"
                    :value="ctgr.code"
                  >[{{ ctgr.codeName }}]</option>
                </select>
              </div>
              <div :class="isModify ? 'col-lg-7' : 'col-lg-8'">
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
            <!--end::챕터 유형 + 카테고리 + 제목-->

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
  <!--end::저널 챕터 등록/수정 모달-->
</template>

<script setup lang="ts">
import { swalConfirm, swalAlert } from "@/utils/swal";
import { isAuthExpiredError } from "@/utils/authError";
import { useSafeModalClose } from "@/utils/safeModalClose";
import { ref, computed, watch, onMounted, nextTick } from "vue";
import { Modal } from "bootstrap";
import axios from "axios";
import { useJournalModalStore } from "@/stores/journalModal";
import { useJournalStore } from "@/stores/journal";

const modalStore = useJournalModalStore();
const journalStore = useJournalStore();

const modalEl = ref<HTMLElement | null>(null);
const submitting = ref(false);
/** 챕터 일자 변경 대상 일자 (yyyy-MM-dd) */
const moveTargetDt = ref<string>("");
/** 챕터 일자 변경 처리 중 여부 */
const moving = ref(false);
let bsModal: InstanceType<typeof Modal> | null = null;
/** 모달 닫기 애니메이션(~300ms) 동안 body.overflow:hidden 으로 scrollIntoView 가 무시되므로
 *  hidden.bs.modal 이후 실행할 스크롤 대상 일자를 임시 보관한다. */
let pendingScrollDt: string | null = null;
const { closeArmed, requestSafeClose, resetSafeClose } = useSafeModalClose(() => {
  modalStore.closeChapterReg();
});

const model = computed(() => modalStore.chapterRegModel);

/** 수정 모드 여부 */
const isModify = computed(() => !!model.value?.id);

/** 수정 모드의 DREAM 챕터 여부 (타입 변경 불가) */
const isModifyDream = computed(() => isModify.value && model.value?.chapterType === "DREAM");

/** chapterType에 따라 일기/노트 전용 카테고리 목록을 분기한다. */
const currentCategoryOptions = computed(() =>
  model.value?.chapterType === "NOTE"
    ? modalStore.chapterNoteCategoryOptions
    : modalStore.chapterDiaryCategoryOptions
);

/* chapterType 변경 시 카테고리 선택 초기화 */
watch(
  () => model.value?.chapterType,
  () => { if (model.value) model.value.categoryCode = ""; }
);

onMounted(() => {
  if (modalEl.value) {
    bsModal = new Modal(modalEl.value, { backdrop: "static", keyboard: false });
    /* bootstrap 이벤트로 store와 상태를 동기화한다 */
    modalEl.value.addEventListener("hidden.bs.modal", () => {
      resetSafeClose();
      modalStore.closeChapterReg();
      if (pendingScrollDt) {
        const dt = pendingScrollDt;
        pendingScrollDt = null;
        void nextTick(() => {
          const el = document.getElementById(`journal-day-${dt}`);
          if (el) el.scrollIntoView({ behavior: "smooth", block: "start" });
        });
      }
    });
  }
});

watch(
  () => modalStore.chapterRegOpen,
  (isOpen) => {
    if (isOpen) {
      resetSafeClose();
      /* 모달 열릴 때 이동 대상 일자를 현재 챕터 일자로 초기화 */
      moveTargetDt.value = model.value?.stdrdDt ?? "";
      bsModal?.show();
    } else bsModal?.hide();
  }
);

function close() {
  resetSafeClose();
  modalStore.closeChapterReg();
}

/** 등록/수정 후 해당 일자 카드(#journal-day-{stdrdDt})로 스크롤한다. */
function scrollToDay(stdrdDt: string): void {
  if (modalEl.value?.classList.contains("show")) {
    /* 모달 닫기 애니메이션(Bootstrap ~300ms) 중에는 body.overflow:hidden 이므로
     * scrollIntoView 가 무시된다. hidden.bs.modal 이후 실행되도록 미룬다. */
    pendingScrollDt = stdrdDt;
    return;
  }
  void nextTick(() => {
    const el = document.getElementById(`journal-day-${stdrdDt}`);
    if (el) el.scrollIntoView({ behavior: "smooth", block: "start" });
  });
}

/** 챕터를 선택한 일자로 이동한다. */
async function moveChapter(): Promise<void> {
  if (!model.value?.id || !moveTargetDt.value) return;
  if (moveTargetDt.value === model.value.stdrdDt) {
    void swalAlert("이미 해당 일자에 속해 있습니다.");
    return;
  }
  const confirmed = await swalConfirm(`챕터를 ${moveTargetDt.value} 일자로 이동하시겠습니까?`);
  if (!confirmed) return;

  moving.value = true;
  try {
    const res = await axios.post(
      `/api/journal/chapter/${model.value.id}/move`,
      null,
      { params: { targetStdrdDt: moveTargetDt.value } }
    );
    if (res.data?.rslt) {
      const targetDt = moveTargetDt.value;
      close();
      void journalStore.fetchDays().then(() => {
        scrollToDay(targetDt);
      });
    } else {
      void swalAlert(res.data?.message ?? "일자 이동에 실패했습니다.");
    }
  } catch (e: unknown) {
    if (isAuthExpiredError(e)) return;
    void swalAlert("요청 처리 중 오류가 발생했습니다.");
  } finally {
    moving.value = false;
  }
}

/** 등록/수정 처리 (axios multipart). 챕터 API는 등록/수정 모두 POST. */
async function submit() {
  if (!model.value) return;
  const confirmed = await swalConfirm(isModify.value ? "수정하시겠습니까?" : "등록하시겠습니까?");
  if (!confirmed) return;

  submitting.value = true;
  try {
    const formData = new FormData();
    if (model.value.id) formData.append("id", String(model.value.id));
    formData.append("journalDayId", String(model.value.journalDayId ?? ""));
    formData.append("chapterType", model.value.chapterType ?? "DIARY");
    formData.append("categoryCode", model.value.categoryCode ?? "");
    formData.append("title", model.value.title ?? "");
    if (model.value.sortOrder != null) formData.append("sortOrder", String(model.value.sortOrder));

    /* 챕터 등록/수정 API는 모두 POST (backend @PostMapping) */
    const url = isModify.value
      ? `/api/journal/chapter/${model.value.id}`
      : "/api/journal/chapters";
    const res = await axios.post(url, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });

    if (res.data?.rslt) {
      const savedDate = model.value?.stdrdDt;
      close();
      void journalStore.fetchDays().then(() => {
        if (savedDate) scrollToDay(savedDate);
      });
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
