<template>
  <!--begin::저널 챕터 등록/수정 모달-->
  <div ref="modalEl" class="modal fade" id="journal_chapter_regist_modal" tabindex="-1" aria-hidden="true" data-bs-backdrop="static" data-bs-keyboard="false">
    <div class="modal-dialog modal-xl">
      <div class="modal-content">

        <!--begin::Modal Header-->
        <div class="modal-header">
          <h5 class="modal-title">{{ t("journal.chapter.modal.title") }}</h5>
          <button
            type="button"
            class="btn-close"
            :title="closeArmed ? t('common.modal.close-armed.tooltip') : t('common.close')"
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
                  <span class="text-gray-700 fs-6 fw-bolder">{{ t("common.date") }}</span>
                </label>
              </div>
              <div class="col-4 fs-6 d-flex align-items-center gap-2">
                <i class="bi bi-calendar3"></i>
                {{ model.stdrdDt }}
                <span v-if="model.stdrdDt" class="fs-8 text-gray-600">({{ getWeekDayStr(model.stdrdDt, t) }})</span>
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
                  {{ t("journal.chapter.move.btn") }}
                </button>
              </div>
              <!--end::챕터 일자 변경-->
            </div>
            <!--end::날짜-->

            <!--begin::챕터 유형 + 말머리 + 제목-->
            <div class="row d-flex mb-8">
              <div class="col-12">
                <label class="d-flex align-items-center mb-2">
                  <span class="text-gray-700 fs-6 fw-bolder">{{ t("common.title") }}</span>
                  <span class="text-gray-500 fs-9 ms-2">{{ t("common.max-length.format").replace("{0}", "100") }}</span>
                </label>
              </div>
              <div class="col-lg-2">
                <!--begin::DREAM 수정 시 고정 표시-->
                <template v-if="isModifyDream">
                  <input type="hidden" name="chapterType" value="DREAM" />
                  <div class="form-control form-control-solid form-control-sm d-flex align-items-center min-h-40px">
                    <span class="fw-bolder">{{ t("journal.chapter.type.dream") }}</span>
                    <span class="text-muted fs-8 ms-2">({{ t("journal.chapter.dream-auto-label") }})</span>
                  </div>
                </template>
                <!--begin::일반 챕터 유형 선택-->
                <template v-else>
                  <select name="chapterType" id="chapterType" class="form-select form-select-solid" v-model="model.chapterType" @change="onChapterTypeChanged">
                    <option value="DIARY">{{ t("journal.chapter.type.diary") }}</option>
                    <option value="NOTE">{{ t("journal.chapter.type.note") }}</option>
                  </select>
                </template>
              </div>
              <div v-if="showChapterPrefixField" class="col-lg-2">
                <!--begin::시스템 요약은 사용자 선택 말머리가 아님-->
                <div
                  v-if="isSummaryChapter"
                  class="form-control form-control-solid d-flex align-items-center"
                >[{{ t("journal.chapter.summary") }}]</div>
                <!--end::시스템 요약-->
                <select
                  v-else
                  name="prefixId"
                  id="prefixId"
                  class="form-select form-select-solid"
                  v-model="model.prefixId"
                >
                    <option :value="null">{{ t("journal.chapter.prefix.select") }}</option>
                    <option v-if="currentInactivePrefix" :value="currentInactivePrefix.id" disabled>
                      [{{ currentInactivePrefix.name }}] ({{ t("status.unuse") }})
                    </option>
                    <option
                      v-for="prefix in chapterPrefixOptions"
                      :key="prefix.id"
                      :value="prefix.id"
                    >[{{ prefix.name }}]</option>
                  </select>
              </div>
              <div :class="chapterTitleColumnClass">
                <input
                  type="text"
                  name="title"
                  id="title"
                  class="form-control"
                  v-model="model.title"
                  :placeholder="t('common.title')"
                  maxlength="100"
                />
              </div>
              <div v-if="isModify && !isSummaryChapter" class="col-1 d-flex ps-0">
                <div class="d-flex-center p-2 fw-bold fs-5 text-gray-600">#</div>
                <input
                  type="number"
                  class="form-control form-control-sm"
                  name="sortOrder"
                  id="sortOrder"
                  min="1"
                  max="99"
                  v-model="model.sortOrder"
                  :placeholder="t('common.order')"
                  maxlength="3"
                />
              </div>
            </div>
            <!--end::챕터 유형 + 말머리 + 제목-->

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
              {{ t("common.save") }}
            </button>
            <button
              type="button"
              class="btn btn-sm"
              :class="closeArmed ? 'btn-light-warning' : 'btn-light'"
              :title="closeArmed ? t('common.modal.close-armed.tooltip') : t('common.close')"
              @click="requestSafeClose"
            >{{ t("common.close") }}</button>
          </div>
        </div>
        <!--end::Modal Footer-->

      </div>
    </div>
  </div>
  <!--end::저널 챕터 등록/수정 모달-->
</template>

<script setup lang="ts">
import { swalConfirm, swalAlert, swalRequestError, swalAjaxResult } from "@/shared/utils/swal";
import { useSafeModalClose } from "@/shared/utils/safeModalClose";
import { ref, computed, watch, onMounted, nextTick } from "vue";
import { Modal } from "bootstrap";
import axios from "axios";
import { useRoute } from "vue-router";
import { useJournalModalStore } from "@/features/journal/stores/journalModal";
import { useJournalStore } from "@/features/journal/stores/journal";
import { refreshJournalDaysForRoute } from "@/features/journal/utils/journalDayRefresh";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { resolveJournalPrefixField } from "@/features/journal/utils/journalPrefixField";
import { getWeekDayStr } from "@/features/journal/utils/journalDate";

const modalStore = useJournalModalStore();
const journalStore = useJournalStore();
const { t } = useLocaleStore();
const route = useRoute();

const modalEl = ref<HTMLElement | null>(null);
const submitting = ref(false);
/** 챕터 일자 변경 대상 일자 (yyyy-MM-dd) */
const moveTargetDt = ref<string>("");
/** 챕터 일자 변경 처리 중 여부 */
const moving = ref(false);
let bsModal: InstanceType<typeof Modal> | null = null;
/** 모달 닫기 애니메이션(~300ms) 동안 body.overflow:hidden 으로 scrollIntoView 가 무시되므로
 *  hidden.bs.modal 이후 실행할 스크롤 대상을 임시 보관한다.
 *  변경 이유: 챕터 등록 직후에는 일자 카드보다 저장된 챕터 DOM 렌더 완료가 더 늦을 수 있다. */
let pendingScrollTarget: { chapterId?: number | string; stdrdDt?: string } | null = null;
const { closeArmed, requestSafeClose, resetSafeClose } = useSafeModalClose(() => {
  modalStore.closeChapterRegist();
});

const model = computed(() => modalStore.chapterRegistModel);

/** 수정 모드 여부 */
const isModify = computed(() => !!model.value?.id);

/** 수정 모드의 DREAM 챕터 여부 (타입 변경 불가) */
const isModifyDream = computed(() => isModify.value && model.value?.chapterType === "DREAM");

/** 서버가 관리하는 시스템 요약 챕터 여부 */
const isSummaryChapter = computed(() => model.value?.summaryYn === "Y");

/**
 * 현재 폼 챕터 유형(일기/노트)에 대응하는 활성 개인 말머리 선택지.
 * 유형을 바꾸면 일기 챕터·노트 챕터가 각각 자기 목록을 표시한다(DREAM 등은 빈 목록).
 */
const chapterPrefixOptions = computed(() => modalStore.chapterPrefixOptionsFor(model.value?.chapterType));

/** 활성 목록과 비활성 과거 선택을 합산한 말머리 필드 표시 상태. */
const prefixFieldPresentation = computed(() => resolveJournalPrefixField(
  chapterPrefixOptions.value,
  model.value?.prefix,
  isSummaryChapter.value,
));

/** 기존 비활성 말머리는 수정 화면에 표시하고 같은 선택 유지 저장만 허용한다. */
const currentInactivePrefix = computed(() => prefixFieldPresentation.value.inactivePrefix);

/** 시스템 요약 또는 선택 가능한 말머리가 있는 챕터에 말머리 영역을 표시한다. */
const showChapterPrefixField = computed(() => prefixFieldPresentation.value.visible);

/** 말머리 영역의 표시 여부에 맞춰 제목 입력이 남은 열을 사용한다. */
const chapterTitleColumnClass = computed(() => {
  if (showChapterPrefixField.value) return isModify.value ? "col-lg-7" : "col-lg-8";
  return isModify.value ? "col-lg-9" : "col-lg-10";
});

/**
 * 사용자가 챕터 유형을 바꾸면 이전 유형 목록의 말머리 선택을 초기화한다.
 * 챕터 말머리 목록이 유형(일기/노트)별로 분리되어, 이전 유형의 prefixId는 새 유형 목록에 없으므로
 * 그대로 두면 저장 시 서버 Scope 검증에서 거부된다. 사용자 조작(@change)에서만 초기화하고,
 * 모달을 여는 시점(수정 폼의 기존 선택)은 건드리지 않는다.
 */
function onChapterTypeChanged(): void {
  if (model.value) model.value.prefixId = null;
}

onMounted(() => {
  if (modalEl.value) {
    bsModal = new Modal(modalEl.value, { backdrop: "static", keyboard: false });
    /* bootstrap 이벤트로 store와 상태를 동기화한다 */
    modalEl.value.addEventListener("hidden.bs.modal", () => {
      resetSafeClose();
      modalStore.closeChapterRegist();
      if (pendingScrollTarget) {
        const target = pendingScrollTarget;
        pendingScrollTarget = null;
        scrollToSavedPositionWhenReady(target.chapterId, target.stdrdDt);
      }
    });
  }
});

watch(
  () => modalStore.chapterRegistOpen,
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
  modalStore.closeChapterRegist();
}

/** 등록/수정 후 저장한 챕터 위치로 돌아간다. 챕터 id를 모르면 해당 일자 카드로 복귀한다. */
function scrollToSavedPosition(chapterId?: number | string, stdrdDt?: string): void {
  if (modalEl.value?.classList.contains("show")) {
    /* 모달 닫기 애니메이션(Bootstrap ~300ms) 중에는 body.overflow:hidden 이므로
     * scrollIntoView 가 무시된다. hidden.bs.modal 이후 실행되도록 미룬다. */
    pendingScrollTarget = { chapterId, stdrdDt };
    return;
  }
  scrollToSavedPositionWhenReady(chapterId, stdrdDt);
}

function findVisibleChapterElement(chapterId: number | string): HTMLElement | null {
  const targetId = `journal-chapter-${chapterId}`;
  const candidates = Array.from(document.querySelectorAll<HTMLElement>(`[id="${targetId}"]`));
  return candidates.find((el) => !el.closest(".modal")) ?? candidates[0] ?? null;
}

function scrollToSavedPositionWhenReady(chapterId?: number | string, stdrdDt?: string, attempt = 0): void {
  void nextTick(() => {
    const chapterEl = chapterId ? findVisibleChapterElement(chapterId) : null;
    if (chapterEl) {
      chapterEl.scrollIntoView({ behavior: "smooth", block: "center" });
      return;
    }
    const dayEl = stdrdDt ? document.getElementById(`journal-day-${stdrdDt}`) : null;
    if (dayEl && !chapterId) {
      dayEl.scrollIntoView({ behavior: "smooth", block: "start" });
      return;
    }
    if (attempt < 16) {
      window.setTimeout(() => scrollToSavedPositionWhenReady(chapterId, stdrdDt, attempt + 1), 100);
      return;
    }
    if (dayEl) {
      console.warn("[JournalChapterRegistModal] saved chapter scroll target not found; fallback to day.", { chapterId, stdrdDt });
      dayEl.scrollIntoView({ behavior: "smooth", block: "start" });
    } else {
      console.warn("[JournalChapterRegistModal] saved chapter/day scroll target not found.", { chapterId, stdrdDt });
    }
  });
}

function refreshCurrentDayView(chapterId?: number | string, stdrdDt?: string): void {
  const afterFetch = () => scrollToSavedPosition(chapterId, stdrdDt);

  void refreshJournalDaysForRoute(journalStore, route, stdrdDt).then(afterFetch);
}

function parseSavedChapterId(value: unknown): string | null {
  if (value === undefined || value === null) return null;
  const raw = String(value).trim();
  return /^\d+$/.test(raw) ? raw : null;
}

function resolveSavedChapterId(responseData: Record<string, unknown>, fallbackId?: number): string | undefined {
  const rsltObj = responseData.rsltObj as Record<string, unknown> | undefined;
  const candidates = [fallbackId, responseData.id, responseData.rsltId, rsltObj?.id];
  for (const value of candidates) {
    const id = parseSavedChapterId(value);
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

/** 챕터를 선택한 일자로 이동한다. */
async function moveChapter(): Promise<void> {
  if (!model.value?.id || !moveTargetDt.value) return;
  const fallbackChapterId = model.value.id;
  if (moveTargetDt.value === model.value.stdrdDt) {
    void swalAlert(t("journal.chapter.move.same-date"));
    return;
  }
  const confirmed = await swalConfirm(t("journal.chapter.move.confirm").replace("{0}", moveTargetDt.value));
  if (!confirmed) return;

  moving.value = true;
  try {
    const res = await axios.post(
      `/api/journal/chapter/${fallbackChapterId}/move`,
      null,
      { params: { targetStdrdDt: moveTargetDt.value } }
    );
    if (res.data?.rslt) {
      const targetDt = moveTargetDt.value;
      const savedChapterId = resolveSavedChapterId(res.data ?? {}, fallbackChapterId);
      close();
      await swalAjaxResult({
        rslt: true,
        message: res.data?.message,
        successFallback: t("common.result.processed"),
      });
      refreshCurrentDayView(savedChapterId, targetDt);
    } else {
      void swalAjaxResult({
        rslt: false,
        message: res.data?.message,
        failureFallback: t("journal.chapter.move.failure"),
      });
    }
  } catch (e: unknown) {
    void swalRequestError(e, t("common.error.processing"));
  } finally {
    moving.value = false;
  }
}

/** 등록/수정 처리 (axios multipart). 챕터 API는 등록/수정 모두 POST. */
async function submit() {
  if (!model.value) return;
  const wasModify = isModify.value;
  const fallbackChapterId = model.value.id;
  const fallbackDate = model.value.stdrdDt;
  const confirmed = await swalConfirm(wasModify ? t("common.confirm.mdf") : t("common.confirm.reg"));
  if (!confirmed) return;

  submitting.value = true;
  try {
    const formData = new FormData();
    if (model.value.id) formData.append("id", String(model.value.id));
    formData.append("journalDayId", String(model.value.journalDayId ?? ""));
    formData.append("chapterType", model.value.chapterType ?? "DIARY");
    formData.append("prefixId", model.value.prefixId == null ? "" : String(model.value.prefixId));
    formData.append("title", model.value.title ?? "");
    if (model.value.sortOrder != null) formData.append("sortOrder", String(model.value.sortOrder));

    /* 챕터 등록/수정 API는 모두 POST (backend @PostMapping) */
    const url = wasModify
      ? `/api/journal/chapter/${fallbackChapterId}`
      : "/api/journal/chapters";
    const res = await axios.post(url, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });

    if (res.data?.rslt) {
      const savedChapterId = resolveSavedChapterId(res.data ?? {}, fallbackChapterId);
      const savedDate = resolveSavedDate(res.data ?? {}, fallbackDate);
      close();
      await swalAjaxResult({
        rslt: true,
        message: res.data?.message,
        successFallback: wasModify ? t("common.result.modified") : t("common.result.registered"),
      });
      refreshCurrentDayView(savedChapterId, savedDate);
    } else {
      void swalAjaxResult({
        rslt: false,
        message: res.data?.message,
        failureFallback: t("common.result.failure"),
      });
    }
  } catch (e: unknown) {
    void swalRequestError(e, t("common.error.processing"));
  } finally {
    submitting.value = false;
  }
}
</script>
