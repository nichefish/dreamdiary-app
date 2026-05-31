<template>
  <!--begin::저널 엔트리(일기/꿈/노트) 등록/수정 모달-->
  <div ref="modalEl" class="modal fade" id="journal_entry_reg_modal" tabindex="-1" aria-hidden="true" data-bs-backdrop="static" data-bs-keyboard="false">
    <div class="modal-dialog modal-xl">
      <div class="modal-content">

        <!--begin::Modal Header-->
        <div class="modal-header">
          <h5 class="modal-title">{{ modalTitle }}</h5>
          <button
            type="button"
            class="btn-close"
            :title="closeArmed ? '한 번 더 클릭하면 닫힙니다' : '닫기'"
            @click="requestSafeClose"
          ></button>
        </div>
        <!--end::Modal Header-->

        <!--begin::Modal Body-->
        <div class="modal-body modal-mbl-body my-5 journal-entry-reg-modal__body">
          <!--begin::로딩-->
          <div v-if="modalStore.entryRegLoading" class="d-flex justify-content-center py-10">
            <span class="spinner-border text-primary" role="status"></span>
          </div>
          <!--end::로딩-->

          <form v-else-if="model" id="journalEntryRegForm" class="form journal-entry-reg-form" @submit.prevent>
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
                <TagifyEditor v-model="tagListStrWithCtgr" :ctgr-map="modalStore.entryCtgrMap" />
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
  <!--end::저널 엔트리(일기/꿈/노트) 등록/수정 모달-->
</template>

<script setup lang="ts">
import { swalConfirm, swalAlert } from "@/utils/swal";
import { isAuthExpiredError } from "@/utils/authError";
import { useSafeModalClose } from "@/utils/safeModalClose";
import { ref, computed, watch, onMounted, nextTick } from "vue";
import RichEditor from "@/views/common/editor/RichEditor.vue";
import TagifyEditor from "@/views/common/tag/TagifyEditor.vue";
import { Modal } from "bootstrap";
import axios from "axios";
import { useRoute } from "vue-router";
import { useJournalModalStore } from "@/stores/journalModal";
import { useJournalStore } from "@/stores/journal";
import type { JournalChapterOption } from "@/stores/journalModal";

const modalStore = useJournalModalStore();
const journalStore = useJournalStore();
const route = useRoute();
const emit = defineEmits<{
  (e: "success", payload: { entryId?: number | string; stdrdDt?: string; isModify?: boolean }): void;
}>();

const modalEl = ref<HTMLElement | null>(null);
const submitting = ref(false);
let bsModal: InstanceType<typeof Modal> | null = null;
/** 모달 닫기 애니메이션(~300ms) 동안 body.overflow:hidden 으로 scrollIntoView 가 무시되므로
 *  hidden.bs.modal 이후 실행할 스크롤 대상을 임시 보관한다. */
let pendingScrollTarget: { entryId?: number | string; stdrdDt?: string } | null = null;
const { closeArmed, requestSafeClose, resetSafeClose } = useSafeModalClose(() => {
  modalStore.closeEntryReg();
});

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
    bsModal = new Modal(modalEl.value, { backdrop: "static", keyboard: false });
    /* bootstrap 이벤트로 store 와 상태를 동기화한다 */
    modalEl.value.addEventListener("hidden.bs.modal", () => {
      resetSafeClose();
      modalStore.closeEntryReg();
      if (pendingScrollTarget) {
        const target = pendingScrollTarget;
        pendingScrollTarget = null;
        scrollToSavedPositionWhenReady(target.entryId, target.stdrdDt);
      }
    });
  }
});

watch(
  () => modalStore.entryRegOpen,
  (isOpen) => {
    if (isOpen) {
      resetSafeClose();
      bsModal?.show();
    } else bsModal?.hide();
  }
);

function close() {
  resetSafeClose();
  modalStore.closeEntryReg();
}

/** 등록/수정 후 저장한 엔트리 위치로 돌아간다. 새 글 id를 모르면 해당 일자 카드로 복귀한다. */
function scrollToSavedPosition(entryId?: number | string, stdrdDt?: string): void {
  if (modalEl.value?.classList.contains("show")) {
    /* 모달 닫기 애니메이션(Bootstrap ~300ms) 중에는 body.overflow:hidden 이므로
     * scrollIntoView 가 무시된다. hidden.bs.modal 이후 실행되도록 미룬다. */
    pendingScrollTarget = { entryId, stdrdDt };
    return;
  }
  scrollToSavedPositionWhenReady(entryId, stdrdDt);
}

function findVisibleEntryElement(entryId: number | string): HTMLElement | null {
  const targetId = `journal-entry-${entryId}`;
  const candidates = Array.from(document.querySelectorAll<HTMLElement>(`[id="${targetId}"]`));
  return candidates.find((el) => !el.closest(".modal")) ?? candidates[0] ?? null;
}

function scrollToSavedPositionWhenReady(entryId?: number | string, stdrdDt?: string, attempt = 0): void {
  void nextTick(() => {
    const entryEl = entryId ? findVisibleEntryElement(entryId) : null;
    if (entryEl) {
      entryEl.scrollIntoView({ behavior: "smooth", block: "center" });
      return;
    }
    const dayEl = stdrdDt ? document.getElementById(`journal-day-${stdrdDt}`) : null;
    if (dayEl) {
      dayEl.scrollIntoView({ behavior: "smooth", block: "start" });
      return;
    }
    if (attempt < 8) {
      window.setTimeout(() => scrollToSavedPositionWhenReady(entryId, stdrdDt, attempt + 1), 80);
    }
  });
}

function scrollToDayDetailPosition(entryId?: number | string): void {
  void nextTick(() => {
    const modal = document.getElementById("journal_day_detail_modal");
    if (!modal) return;
    const entryEl = entryId
      ? Array.from(modal.querySelectorAll<HTMLElement>("[data-id]"))
          .find((el) => el.dataset.id === String(entryId))
      : null;
    if (entryEl) {
      entryEl.scrollIntoView({ behavior: "smooth", block: "center" });
      return;
    }
    modal.querySelector<HTMLElement>(".journal-day-header")?.scrollIntoView({ behavior: "smooth", block: "start" });
  });
}

function refreshOpenDayDetail(entryId?: number | string): boolean {
  const dayId = modalStore.dayDtlData?.id;
  if (!modalStore.dayDtlOpen || !dayId) return false;
  void modalStore.openDayDtl(dayId).then(() => scrollToDayDetailPosition(entryId));
  return true;
}

function refreshCurrentDayView(entryId?: number | string, stdrdDt?: string): void {
  if (route.name === "journal-entry-search") {
    return;
  }

  void journalStore.fetchTagCloud();
  const detailRefreshed = refreshOpenDayDetail(entryId);
  const afterFetch = () => {
    if (!detailRefreshed) scrollToSavedPosition(entryId, stdrdDt);
  };

  if (route.name === "journal-weekly") {
    journalStore.setViewType("WEEKLY");
    void journalStore.fetchDays({ viewType: "WEEKLY" }).then(afterFetch);
    return;
  }

  if (route.name === "journal-monthly") {
    journalStore.setViewType("LIST");
    void journalStore.fetchDays({ viewType: "LIST" }).then(afterFetch);
    return;
  }

  void journalStore.fetchDays().then(afterFetch);
}

function resolveSavedEntryId(responseData: Record<string, unknown>, fallbackId?: number): number | string | undefined {
  const rsltObj = responseData.rsltObj as Record<string, unknown> | undefined;
  const candidates = [
    fallbackId,
    responseData.id,
    responseData.rsltId,
    rsltObj?.id,
  ];
  return candidates.find((value) => value !== undefined && value !== null && String(value) !== "") as number | string | undefined;
}

function resolveSavedDate(responseData: Record<string, unknown>, fallbackDate?: string): string | undefined {
  const rsltObj = responseData.rsltObj as Record<string, unknown> | undefined;
  const candidates = [
    rsltObj?.stdrdDt,
    rsltObj?.journalDate,
    responseData.stdrdDt,
    responseData.journalDate,
    fallbackDate,
  ];
  const savedDate = candidates.find((value) => value !== undefined && value !== null && String(value).trim() !== "");
  return savedDate == null ? undefined : String(savedDate).slice(0, 10);
}

/** 등록/수정 처리. API 는 등록/수정 모두 POST. */
async function submit() {
  if (submitting.value) return;
  if (!model.value) return;
  const wasModify = isModify.value;
  submitting.value = true;
  try {
    const confirmed = await swalConfirm(wasModify ? "수정하시겠습니까?" : "등록하시겠습니까?");
    if (!confirmed) return;

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
      const savedEntryId = resolveSavedEntryId(res.data ?? {}, model.value.id);
      const savedDate = resolveSavedDate(res.data ?? {}, model.value.stdrdDt);
      close();
      refreshCurrentDayView(savedEntryId, savedDate);
      emit("success", { entryId: savedEntryId, stdrdDt: savedDate, isModify: wasModify });
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
