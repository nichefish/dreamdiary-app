<template>
  <!--begin::저널 일자 등록/수정 모달-->
  <div ref="modalEl" class="modal fade" id="journal_day_reg_modal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-xl">
      <div class="modal-content">

        <!--begin::Modal Header-->
        <div class="modal-header">
          <h5 class="modal-title">저널 일자 등록/수정</h5>
          <button type="button" class="btn-close" @click="close"></button>
        </div>
        <!--end::Modal Header-->

        <!--begin::Modal Body-->
        <div class="modal-body modal-mbl-body my-5">
          <form v-if="model" id="journalDayRegForm" class="form" @submit.prevent>

            <input type="hidden" name="id" :value="model.id ?? ''" />

            <!--begin::날짜 + 정확도-->
            <div class="row row-cols-lg-2 mb-3">
              <div class="col-xl-2 col-2 d-flex-center">
                <label class="fs-6 fw-bold mb-2 required" for="journalDate">날짜</label>
              </div>
              <div class="col-xl-3 col-5 d-flex flex-column" id="journalDateDiv">
                <div class="d-flex align-items-center mt-1">
                  <span class="menu-icon me-md-2 vertical-middle">
                    <label for="journalDate"><i class="bi bi-calendar3 fs-2 cursor-pointer"></i></label>
                  </span>
                  <input
                    type="text"
                    name="journalDate"
                    id="journalDate"
                    class="form-control form-control-solid w-150px"
                    v-model="model.journalDate"
                    placeholder="YYYY-MM-DD"
                    maxlength="10"
                    autocomplete="off"
                    required
                  />
                </div>
                <div id="journalDate_validate_span"></div>
              </div>
              <div class="col-xl-2 col-5 d-flex flex-column align-items-start col-form-label">
                <label class="fs-6 fw-bold mb-0 required" for="journalDatePrecision">날짜 정확도</label>
              </div>
              <div class="col-xl-3 col-5 d-flex flex-column">
                <select
                  name="journalDatePrecision"
                  id="journalDatePrecision"
                  v-model="model.journalDatePrecision"
                  class="form-select form-select-solid w-150px mt-1"
                >
                  <option value="EXACT">EXACT</option>
                  <option value="APPROXIMATE">APPROXIMATE</option>
                  <option value="UNKNOWN">UNKNOWN</option>
                </select>
              </div>
            </div>
            <!--end::날짜 + 정확도-->

            <!--begin::날씨-->
            <div class="row mb-3">
              <div class="col-2 text-center">
                <label for="weather" class="cursor-pointer col-form-label fs-6 fw-bold">날씨</label>
              </div>
              <div class="col-1 d-flex-center w-5">
                <span v-if="model.weather" v-html="model.weather" class="cursor-pointer"></span>
                <span v-else>-</span>
              </div>
              <div class="col-8">
                <textarea
                  name="weather"
                  id="weather"
                  v-model="model.weather"
                  class="form-control form-control-solid d-block"
                  placeholder="날씨 (이모지 등)"
                  maxlength="200"
                  rows="1"
                ></textarea>
              </div>
            </div>
            <!--end::날씨-->

            <!--begin::일기 완료 여부-->
            <div class="row d-flex mb-8">
              <div class="col-lg-12 col-3 d-flex align-items-center">
                <label class="text-gray-700 fs-6 fw-bolder">일기 완료 여부</label>
              </div>
              <div class="col-lg-2 col-9 d-flex align-items-center">
                <div class="form-check form-switch form-check-custom form-check-solid cursor-pointer">
                  <input
                    type="checkbox"
                    name="diaryResolvedYn"
                    id="diaryResolvedYn"
                    class="form-check-input cursor-pointer"
                    value="Y"
                    :checked="diaryResolvedChecked"
                    @change="onDiaryResolvedChange"
                  />
                  <label
                    class="form-check-label fw-bold fs-6 ms-3"
                    for="diaryResolvedYn"
                    :style="{ color: diaryResolvedChecked ? 'blue' : 'gray' }"
                  >{{ diaryResolvedChecked ? '완료' : '미완료' }}</label>
                </div>
              </div>
            </div>
            <!--end::일기 완료 여부-->

            <!--begin::태그-->
            <div class="row mb-3">
              <div>
                <label for="tagListStr" class="mb-2">
                  <span class="text-gray-700 fs-6 fw-bolder">태그</span>
                  <span class="text-gray-500 fs-9 mx-2">(쉼표로 구분, [카테고리]태그명 형식)</span>
                </label>
              </div>
              <div class="col-xl-12 text-sm-start">
                <input
                  name="tag.tagListStr"
                  id="tagListStr"
                  class="form-control form-control-solid"
                  autocomplete="off"
                  v-model="tagListStr"
                  placeholder="태그 입력"
                />
              </div>
            </div>
            <!--end::태그-->

            <!--begin::메타-->
            <div class="row mb-3">
              <div>
                <label for="metaListStr" class="mb-2">
                  <span class="text-gray-700 fs-6 fw-bolder">메타</span>
                </label>
              </div>
              <div class="col-xl-12 text-sm-start">
                <input
                  name="meta.metaListStr"
                  id="metaListStr"
                  class="form-control form-control-solid"
                  autocomplete="off"
                  v-model="metaListStr"
                  placeholder="메타 입력"
                />
              </div>
            </div>
            <!--end::메타-->

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
            <button type="button" class="btn btn-sm btn-light" @click="close">닫기</button>
          </div>
        </div>
        <!--end::Modal Footer-->

      </div>
    </div>
  </div>
  <!--end::저널 일자 등록/수정 모달-->
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from "vue";
import { Modal } from "bootstrap";
import axios from "axios";
import { useJournalModalStore } from "@/stores/journalModal";
import { useJournalStore } from "@/stores/journal";

const modalStore = useJournalModalStore();
const journalStore = useJournalStore();

const modalEl = ref<HTMLElement | null>(null);
const submitting = ref(false);
let bsModal: InstanceType<typeof Modal> | null = null;

const model = computed(() => modalStore.dayRegModel);

/** 태그 문자열 — model.tag.tagListStr 을 직접 바인딩 */
const tagListStr = computed({
  get: () => model.value?.tag?.tagListStr ?? "",
  set: (v) => { if (model.value?.tag) model.value.tag.tagListStr = v; },
});

/** 메타 문자열 — model.meta.metaListStr 을 직접 바인딩 */
const metaListStr = computed({
  get: () => model.value?.meta?.metaListStr ?? "",
  set: (v) => { if (model.value?.meta) model.value.meta.metaListStr = v; },
});

const diaryResolvedChecked = computed(
  () => String(model.value?.diaryResolvedYn ?? "").toUpperCase() === "Y"
);

function onDiaryResolvedChange(event: Event) {
  if (!model.value) return;
  model.value.diaryResolvedYn = (event.target as HTMLInputElement).checked ? "Y" : "N";
}

onMounted(() => {
  if (modalEl.value) {
    bsModal = new Modal(modalEl.value);
    /* bootstrap 이벤트로 store와 상태를 동기화한다 */
    modalEl.value.addEventListener("hidden.bs.modal", () => {
      modalStore.closeDayReg();
    });
  }
});

watch(
  () => modalStore.dayRegOpen,
  (isOpen) => {
    if (isOpen) bsModal?.show();
    else bsModal?.hide();
  }
);

function close() {
  modalStore.closeDayReg();
}

/** 등록/수정 처리 (axios multipart). */
async function submit() {
  if (!model.value) return;
  if (!model.value.journalDate) {
    alert("날짜를 입력해 주세요.");
    return;
  }

  const isEdit = !!model.value.id;
  const confirmed = window.confirm(isEdit ? "수정하시겠습니까?" : "등록하시겠습니까?");
  if (!confirmed) return;

  submitting.value = true;
  try {
    const formData = new FormData();
    if (isEdit) formData.append("id", String(model.value.id));
    formData.append("journalDate", model.value.journalDate ?? "");
    formData.append("journalDatePrecision", model.value.journalDatePrecision ?? "EXACT");
    formData.append("weather", model.value.weather ?? "");
    formData.append("diaryResolvedYn", diaryResolvedChecked.value ? "Y" : "N");
    formData.append("tag.tagListStr", tagListStr.value);
    formData.append("meta.metaListStr", metaListStr.value);

    const url = isEdit ? `/api/journal/day/${model.value.id}` : "/api/journal/days";
    const res = await axios({
      method: isEdit ? "put" : "post",
      url,
      data: formData,
      headers: { "Content-Type": "multipart/form-data" },
    });

    if (res.data?.rslt) {
      close();
      void journalStore.fetchDays();
    } else {
      alert(res.data?.message ?? "처리에 실패했습니다.");
    }
  } catch {
    alert("요청 처리 중 오류가 발생했습니다.");
  } finally {
    submitting.value = false;
  }
}
</script>