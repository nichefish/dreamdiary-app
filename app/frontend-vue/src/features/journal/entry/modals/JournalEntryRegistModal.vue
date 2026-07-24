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
            :title="closeArmed ? t('common.modal.close-armed.tooltip') : t('common.close')"
            @click="requestSafeClose"
          ></button>
        </div>
        <!--end::Modal Header-->

        <!--begin::Modal Body-->
        <div class="modal-body modal-mbl-body my-5 journal-entry-regist-modal__body">
          <!--begin::로딩-->
          <div v-if="modalStore.entryRegistLoading" class="d-flex justify-content-center py-10">
            <span class="spinner-border text-primary" role="status"></span>
          </div>
          <!--end::로딩-->

          <form v-else-if="model" id="journalEntryRegistForm" class="form journal-entry-regist-form" @submit.prevent>
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
                  <span class="text-gray-700 fs-6 fw-bolder">{{ t('journal.day.field.date') }}</span>
                </label>
              </div>
              <div class="col-4 fs-6">
                <i class="bi bi-calendar3"></i>
                {{ model.stdrdDt ?? '' }}
                <span v-if="model.stdrdDt" class="fs-8 text-gray-600">({{ getWeekDayStr(model.stdrdDt, t) }})</span>
              </div>
            </div>
            <!--end::날짜-->

            <!--begin::제목-->
            <div class="row d-flex mb-8">
              <div class="col-12">
                <label class="d-flex align-items-center mb-2">
                  <span class="text-gray-700 fs-6 fw-bolder">{{ t('common.title') }}</span>
                  <span class="text-gray-500 fs-9 ms-2">{{ t('journal.field.title-max-100') }}</span>
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
                    <option value="">{{ t('common.category.select') }}</option>
                    <!--TODO: 일기 카테고리 옵션 서버 조회 미구현-->
                  </select>
                </div>
                <div :class="isModify ? 'col-lg-7' : 'col-lg-8'">
                  <input type="text" name="title" class="form-control" v-model="model.title"
                    :placeholder="t('common.title')" maxlength="100" />
                </div>
              </template>
              <!--end::DIARY-->

              <!--begin::DREAM: 제목만-->
              <template v-else-if="isDream">
                <div :class="isModify ? 'col-lg-11' : 'col-lg-12'">
                  <input type="text" name="title" class="form-control" v-model="model.title"
                    :placeholder="t('common.title')" maxlength="100" />
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
                    :placeholder="t('common.title')" maxlength="100" />
                </div>
              </template>
              <!--end::NOTE-->

              <!--begin::순서 (수정 모드, DREAM은 대타꿈 아닐 때)-->
              <div v-if="showSortCol" class="col-1 d-flex ps-0">
                <div class="d-flex-center p-2 fw-bold fs-5 text-gray-600">#</div>
                <input type="number" class="form-control form-control-sm" name="sortOrder"
                  min="1" max="99" v-model="model.sortOrder" :placeholder="t('journal.entry.sort-order.placeholder')"
                  :maxlength="isDream ? 2 : 3" />
              </div>
              <!--end::순서-->
            </div>
            <!--end::제목-->
            <!--begin::꿈꾼 이름 (DREAM, 비필수)-->
            <div v-if="isDream" class="row d-flex mb-8">
              <div class="col-2">
                <label class="d-flex align-items-center mb-2">
                  <span class="text-gray-700 fs-6 fw-bolder">{{ t('journal.entry.dreamer.label') }}</span>
                </label>
              </div>
              <div class="col-4">
                <input
                  type="text"
                  name="elseDreamerNm"
                  class="form-control"
                  v-model="model.elseDreamerNm"
                  :placeholder="t('journal.entry.dreamer.placeholder')"
                  maxlength="64"
                />
              </div>
            </div>
            <!--end::꿈꾼 이름-->
            <!--begin::본문-->
            <div class="row d-flex mb-8">
              <div class="col-12">
                <label class="d-flex align-items-center mb-2">
                  <span class="text-gray-700 fs-6 fw-bolder">{{ t('common.body') }}</span>
                </label>
                <RichEditor v-model="model.content" />
              </div>
            </div>
            <!--end::본문-->

            <!--begin::태그 (DIARY/DREAM 전용)-->
            <div v-if="showTag" class="row d-flex mb-8">
              <div class="col-12">
                <label class="d-flex align-items-center mb-2">
                  <span class="text-gray-700 fs-6 fw-bolder">{{ t('common.tag') }}</span>
                </label>
                <TagifyEditor v-model="tagListStrWithCtgr" :category-map="modalStore.entryCategoryMap" />
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
              {{ t('common.save') }}
            </button>
            <button
              type="button"
              class="btn btn-sm"
              :class="closeArmed ? 'btn-light-warning' : 'btn-light'"
              :title="closeArmed ? t('common.modal.close-armed.tooltip') : t('common.close')"
              @click="requestSafeClose"
            >{{ t('common.close') }}</button>
          </div>
        </div>
        <!--end::Modal Footer-->

      </div>
    </div>
  </div>
  <!--end::저널 엔트리(일기/꿈/노트) 등록/수정 모달-->
</template>

<script setup lang="ts">
import { swalConfirm, swalAlert, swalRequestError, swalAjaxResult } from "@/shared/utils/swal";
import { useSafeModalClose } from "@/shared/utils/safeModalClose";
import { ref, computed, watch, onMounted, nextTick } from "vue";
import RichEditor from "@/shared/ui/editor/RichEditor.vue";
import TagifyEditor from "@/shared/ui/tag/TagifyEditor.vue";
import { Modal } from "bootstrap";
import axios from "axios";
import { useRoute } from "vue-router";
import { useJournalModalStore } from "@/features/journal/stores/journalModal";
import { useJournalStore } from "@/features/journal/stores/journal";
import { useJournalThreadStore } from "@/features/journal/stores/journalThread";
import { refreshJournalEntryHostForRoute } from "@/features/journal/utils/journalEntryHostRefresh";
import type { JournalChapterOption } from "@/features/journal/stores/journalModal";
import { hasDreamerName } from "@/features/journal/utils/journalDream";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { getWeekDayStr } from "@/features/journal/utils/journalDate";

const modalStore = useJournalModalStore();
const journalStore = useJournalStore();
const threadStore = useJournalThreadStore();
const { t } = useLocaleStore();
const route = useRoute();
const emit = defineEmits<{
  (e: "prepare-success", payload: {
    entryId?: number | string;
    stdrdDt?: string;
    isModify?: boolean;
    waitUntil: (task: Promise<void>) => void;
  }): void;
  (e: "success", payload: { entryId?: number | string; stdrdDt?: string; isModify?: boolean }): void;
}>();

const modalEl = ref<HTMLElement | null>(null);
const submitting = ref(false);
let bsModal: InstanceType<typeof Modal> | null = null;
const { closeArmed, requestSafeClose, resetSafeClose } = useSafeModalClose(() => {
  modalStore.closeEntryRegist();
});

const model = computed(() => modalStore.entryRegistModel);

/** 수정 모드 여부 */
const isModify = computed(() => !!model.value?.id);

const isDiary = computed(() => model.value?.contentType === "JOURNAL_DIARY");
const isDream = computed(() => model.value?.contentType === "JOURNAL_DREAM");
const isNote = computed(() => model.value?.contentType === "JOURNAL_NOTE");

/** 모달 제목 */
const modalTitle = computed(() => {
  if (isDiary.value) return t("journal.entry.modal.diary.title");
  if (isDream.value) return t("journal.entry.modal.dream.title");
  if (isNote.value) return t("journal.entry.modal.note.title");
  return t("journal.entry.modal.default.title");
});

/** 태그 입력 표시 여부 (DIARY/DREAM 전용) */
const showTag = computed(() => isDiary.value || isDream.value);

/** 순서 입력 표시 여부: 수정 모드, 지정 꿈꾼(이름 있음) 꿈은 제외 */
const showSortCol = computed(() => {
  if (!isModify.value) return false;
  if (isDream.value && hasDreamerName(model.value)) return false;
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
      modalStore.closeEntryRegist();
    });
  }
});

watch(
  () => modalStore.entryRegistOpen,
  (isOpen) => {
    if (isOpen) {
      resetSafeClose();
      bsModal?.show();
    } else bsModal?.hide();
  }
);

function close() {
  resetSafeClose();
  modalStore.closeEntryRegist();
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

async function prepareOpenDayDetail(): Promise<boolean> {
  const dayId = modalStore.dayDetailData?.id;
  if (!modalStore.dayDetailOpen || !dayId) return false;
  await modalStore.openDayDetail(dayId);
  return true;
}

function scrollDayDetailIfRefreshed(entryId?: number | string, detailRefreshed = false): void {
  if (!detailRefreshed) return;
  scrollToDayDetailPosition(entryId);
}

function refreshEntryTagCloud(contentType?: string): void {
  if (contentType === "JOURNAL_DIARY") {
    void journalStore.fetchTagCloud({ sections: ["diary"] });
  } else if (contentType === "JOURNAL_DREAM") {
    void journalStore.fetchTagCloud({ sections: ["dream"] });
  }
}

async function refreshCurrentDayView(contentType?: string): Promise<boolean> {
  if (route.name === "journal-entry-search") {
    return false;
  }

  if (route.name === "thread-detail") {
    await refreshJournalEntryHostForRoute(journalStore, threadStore, route, model.value?.stdrdDt);
    return false;
  }

  refreshEntryTagCloud(contentType);
  const detailRefreshed = await prepareOpenDayDetail();
  if (detailRefreshed) return true;

  await refreshJournalEntryHostForRoute(journalStore, threadStore, route, model.value?.stdrdDt);
  return false;
}

function parseSavedEntryId(value: unknown): string | null {
  if (value === undefined || value === null) return null;
  const raw = String(value).trim();
  return /^\d+$/.test(raw) ? raw : null;
}

function resolveSavedEntryId(responseData: Record<string, unknown>, fallbackId?: number): string | undefined {
  const rsltObj = responseData.rsltObj as Record<string, unknown> | undefined;
  const candidates = [fallbackId, responseData.id, responseData.rsltId, rsltObj?.id];
  for (const value of candidates) {
    const id = parseSavedEntryId(value);
    if (id) return id;
  }
  return undefined;
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
  const fallbackEntryId = model.value.id;
  const fallbackDate = model.value.stdrdDt;
  submitting.value = true;
  try {
    const confirmed = await swalConfirm(wasModify ? t("common.confirm.mdf") : t("common.confirm.reg"));
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
    if (isDream.value) {
      formData.append("elseDreamerNm", model.value.elseDreamerNm?.trim() ?? "");
    }
    if (showTag.value) formData.append("tag.tagListStr", model.value.tag?.tagListStrWithCtgr ?? "");

    /* 등록/수정 API 는 모두 POST (backend @PostMapping) */
    const url = wasModify
      ? `/api/journal/entry/${fallbackEntryId}`
      : "/api/journal/entries";
    const res = await axios.post(url, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });

    if (res.data?.rslt) {
      if (showTag.value && model.value.contentType) {
        modalStore.applyCategoryMapsFromSaveResponse(
          res.data?.rsltMap,
          model.value.contentType,
        );
      }
      const savedEntryId = resolveSavedEntryId(res.data ?? {}, fallbackEntryId);
      const savedDate = resolveSavedDate(res.data ?? {}, fallbackDate);
      const savedContentType = model.value.contentType;
      close();
      const successPayload = { entryId: savedEntryId, stdrdDt: savedDate, isModify: wasModify };
      const prepareTasks: Promise<void>[] = [];
      if (route.name === "journal-entry-search") {
        emit("prepare-success", {
          ...successPayload,
          waitUntil: (task: Promise<void>) => {
            prepareTasks.push(task);
          },
        });
      }
      if (prepareTasks.length > 0) {
        await Promise.allSettled(prepareTasks);
      }
      await swalAjaxResult({
        rslt: true,
        message: res.data?.message,
        successFallback: wasModify ? t("common.result.modified") : t("common.result.registered"),
      });
      if (route.name === "journal-entry-search") {
        emit("success", successPayload);
      } else {
        const detailRefreshed = await refreshCurrentDayView(savedContentType);
        scrollDayDetailIfRefreshed(savedEntryId, detailRefreshed);
      }
    } else {
      void swalAjaxResult({
        rslt: false,
        message: res.data?.message,
        failureFallback: t("common.result.failure"),
      });
    }
  } catch (e: unknown) {
    void swalRequestError(e);
  } finally {
    submitting.value = false;
  }
}
</script>
