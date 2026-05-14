<template>
  <!--begin::저널 엔트리(일기/꿈/노트) 등록/수정 모달-->
  <div ref="modalEl" class="modal fade" id="journal_entry_reg_modal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-xl">
      <div class="modal-content">

        <!--begin::Modal Header-->
        <div class="modal-header">
          <h5 class="modal-title">{{ modalTitle }}</h5>
          <button type="button" class="btn-close" @click="close"></button>
        </div>
        <!--end::Modal Header-->

        <!--begin::Modal Body-->
        <div class="modal-body modal-mbl-body my-5">
          <!--begin::로딩-->
          <div v-if="modalStore.entryRegLoading" class="d-flex justify-content-center py-10">
            <span class="spinner-border text-primary" role="status"></span>
          </div>
          <!--end::로딩-->

          <form v-else-if="model" id="journalEntryRegForm" class="form" @submit.prevent>
            <input type="hidden" name="id" :value="model.id ?? ''" />
            <input type="hidden" name="contentType" :value="model.contentType" />
            <input type="hidden" name="journalDayId" :value="model.journalDayId ?? ''" />
            <!--begin::DREAM: 챕터 ID 히든-->
            <input v-if="isDream" type="hidden" name="journalChapterId" :value="model.journalChapterId ?? ''" />
            <!--begin::DIARY: 상태 히든-->
            <template v-if="isDiary">
              <input type="hidden" name="collapsedYn" :value="model.collapsedYn ?? ''" />
              <input type="hidden" name="imprtcYn" :value="model.imprtcYn ?? ''" />
            </template>

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

            <!--begin::제목-->
            <div class="row d-flex mb-8">
              <div class="col-12">
                <label class="d-flex align-items-center mb-2">
                  <span class="text-gray-700 fs-6 fw-bolder">제목</span>
                  <span class="text-gray-500 fs-9 ms-2">(최대 100자)</span>
                </label>
              </div>

              <!--begin::DIARY: 챕터 선택 + 카테고리 + 제목-->
              <template v-if="isDiary">
                <div class="col-lg-2">
                  <select name="journalChapterId" class="form-select form-select-solid" v-model="model.journalChapterId">
                    <option v-for="ch in chapterList" :key="ch.id" :value="ch.id">
                      {{ chapterLabel(ch) }}
                    </option>
                  </select>
                </div>
                <div class="col-lg-2">
                  <select name="ctgrCd" class="form-select form-select-solid" v-model="model.ctgrCd">
                    <option value="">-- 카테고리 선택 --</option>
                    <!--TODO: 일기 카테고리 옵션 서버 조회 미구현-->
                  </select>
                </div>
                <div :class="isModify ? 'col-lg-7' : 'col-lg-8'">
                  <input type="text" name="title" class="form-control" v-model="model.title"
                    placeholder="제목" maxlength="100" />
                </div>
              </template>
              <!--end::DIARY-->

              <!--begin::DREAM: 제목만-->
              <template v-else-if="isDream">
                <div :class="isModify ? 'col-lg-11' : 'col-lg-12'">
                  <input type="text" name="title" class="form-control" v-model="model.title"
                    placeholder="제목" maxlength="100" />
                </div>
              </template>
              <!--end::DREAM-->

              <!--begin::NOTE: 챕터 선택 + 제목-->
              <template v-else-if="isNote">
                <div class="col-lg-1">
                  <select name="journalChapterId" class="form-select form-select-solid" v-model="model.journalChapterId">
                    <option v-for="ch in chapterList" :key="ch.id" :value="ch.id">
                      {{ chapterLabel(ch) }}
                    </option>
                  </select>
                </div>
                <div :class="isModify ? 'col-lg-10' : 'col-lg-11'">
                  <input type="text" name="title" class="form-control" v-model="model.title"
                    placeholder="제목" maxlength="100" />
                </div>
              </template>
              <!--end::NOTE-->

              <!--begin::순서 (수정 모드, DREAM은 대타꿈 아닐 때)-->
              <div v-if="showSortCol" class="col-1 d-flex ps-0">
                <div class="d-flex-center p-2 fw-bold fs-5 text-gray-600">#</div>
                <input type="number" class="form-control form-control-sm" name="sortOrder"
                  min="1" max="99" v-model="model.sortOrder" placeholder="순서"
                  :maxlength="isDream ? 2 : 3" />
              </div>
              <!--end::순서-->
            </div>
            <!--end::제목-->

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

            <!--begin::태그 (DIARY/DREAM 전용)-->
            <div v-if="showTag" class="row d-flex mb-8">
              <div class="col-12">
                <label class="d-flex align-items-center mb-2">
                  <span class="text-gray-700 fs-6 fw-bolder">태그</span>
                </label>
                <TagifyEditor v-model="tagListStrWithCtgr" :ctgr-map-url="ctgrMapUrl" />
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
            <button type="button" class="btn btn-sm btn-light" @click="close">닫기</button>
          </div>
        </div>
        <!--end::Modal Footer-->

      </div>
    </div>
  </div>
  <!--end::저널 엔트리(일기/꿈/노트) 등록/수정 모달-->
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from "vue";
import RichEditor from "@/views/common/editor/RichEditor.vue";
import TagifyEditor from "@/views/common/tag/TagifyEditor.vue";
import { Modal } from "bootstrap";
import axios from "axios";
import { useJournalModalStore } from "@/stores/journalModal";
import { useJournalStore } from "@/stores/journal";
import type { JournalChapterOption } from "@/stores/journalModal";

const modalStore = useJournalModalStore();
const journalStore = useJournalStore();

const modalEl = ref<HTMLElement | null>(null);
const submitting = ref(false);
let bsModal: InstanceType<typeof Modal> | null = null;

const model = computed(() => modalStore.entryRegModel);

/** 수정 모드 여부 */
const isModify = computed(() => !!model.value?.id);

const isDiary = computed(() => model.value?.contentType === "JOURNAL_DIARY");
const isDream = computed(() => model.value?.contentType === "JOURNAL_DREAM");
const isNote = computed(() => model.value?.contentType === "JOURNAL_NOTE");

/** 모달 제목 */
const modalTitle = computed(() => {
  if (isDiary.value) return "저널 일기 등록/수정";
  if (isDream.value) return "저널 꿈 등록/수정";
  if (isNote.value) return "저널 노트 등록/수정";
  return "저널 엔트리 등록/수정";
});

/** 태그 입력 표시 여부 (DIARY/DREAM 전용) */
const showTag = computed(() => isDiary.value || isDream.value);

/** 카테고리 맵 API URL (DIARY/DREAM 전용; 엔트리 타입별 분기) */
const ctgrMapUrl = computed<string>(() => {
  if (isDiary.value) return "/api/journal/entry/tag/ctgr-map?type=DIARY";
  if (isDream.value) return "/api/journal/entry/tag/ctgr-map?type=DREAM";
  return "";
});

/** 순서 입력 표시 여부: 수정 모드, DREAM 은 대타꿈(elseDreamYn='Y') 제외 */
const showSortCol = computed(() => {
  if (!isModify.value) return false;
  if (isDream.value && (model.value?.elseDreamYn === "Y" || model.value?.elseDreamYn === "y")) return false;
  return true;
});

/** 챕터 선택 목록 (DIARY: 비DREAM 챕터, NOTE: 같음) */
const chapterList = computed<JournalChapterOption[]>(() => model.value?.chapterList ?? []);

/** 챕터 선택 옵션 레이블 */
function chapterLabel(ch: JournalChapterOption): string {
  const prefix = ch.categoryName
    ? `[${ch.categoryName}] `
    : ch.categoryCode
    ? `[${ch.categoryCode}] `
    : "";
  return `${prefix}${ch.sortOrder ?? ""} ${ch.title ?? ""}`.trim();
}

/** 태그 문자열 양방향 바인딩 */
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
    /* bootstrap 이벤트로 store 와 상태를 동기화한다 */
    modalEl.value.addEventListener("hidden.bs.modal", () => {
      modalStore.closeEntryReg();
    });
  }
});

watch(
  () => modalStore.entryRegOpen,
  (isOpen) => {
    if (isOpen) bsModal?.show();
    else bsModal?.hide();
  }
);

function close() {
  modalStore.closeEntryReg();
}

/** 등록/수정 처리. API 는 등록/수정 모두 POST. */
async function submit() {
  if (!model.value) return;
  if (!model.value.title) {
    alert("제목을 입력해 주세요.");
    return;
  }

  const confirmed = window.confirm(isModify.value ? "수정하시겠습니까?" : "등록하시겠습니까?");
  if (!confirmed) return;

  submitting.value = true;
  try {
    const formData = new FormData();
    if (model.value.id) formData.append("id", String(model.value.id));
    formData.append("contentType", model.value.contentType ?? "");
    formData.append("journalDayId", String(model.value.journalDayId ?? ""));
    formData.append("journalChapterId", String(model.value.journalChapterId ?? ""));
    formData.append("title", model.value.title ?? "");
    formData.append("ctgrCd", model.value.ctgrCd ?? "");
    formData.append("content", model.value.content ?? "");
    if (model.value.sortOrder != null) formData.append("sortOrder", String(model.value.sortOrder));
    if (isDiary.value) {
      formData.append("collapsedYn", model.value.collapsedYn ?? "");
      formData.append("imprtcYn", model.value.imprtcYn ?? "");
    }
    if (showTag.value) formData.append("tag.tagListStr", model.value.tag?.tagListStrWithCtgr ?? "");

    /* 등록/수정 API 는 모두 POST (backend @PostMapping) */
    const url = isModify.value
      ? `/api/journal/entry/${model.value.id}`
      : "/api/journal/entries";
    const res = await axios.post(url, formData, {
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