<template>
  <!--begin::저널 Reflection 등록/수정 모달-->
  <div ref="modalEl" class="modal fade" id="journal_reflection_regist_modal" tabindex="-1" aria-hidden="true" data-bs-backdrop="static" data-bs-keyboard="false">
    <div class="modal-dialog modal-xl">
      <div class="modal-content">

        <!--begin::Modal Header-->
        <div class="modal-header">
          <h5 class="modal-title">{{ t('journal.reflection.modal.title') }}</h5>
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
          <form v-if="model" id="journalReflectionRegistForm" class="form" @submit.prevent>

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
              <div :class="titleColClass">
                <input
                  type="text"
                  name="title"
                  class="form-control"
                  v-model="model.title"
                  :placeholder="t('common.title')"
                  maxlength="100"
                />
              </div>
              <div v-if="isModify" class="col-1 d-flex ps-0">
                <div class="d-flex-center p-2 fw-bold fs-5 text-gray-600">#</div>
                <input
                  type="number"
                  class="form-control form-control-sm"
                  name="sortOrder"
                  min="1"
                  max="99"
                  v-model="model.sortOrder"
                  maxlength="3"
                />
              </div>
            </div>
            <!--end::제목-->

            <!--begin::본문-->
            <div class="row d-flex mb-8">
              <div class="col-12">
                <label class="d-flex align-items-center mb-2">
                  <span class="text-gray-700 fs-6 fw-bolder">{{ t('common.body') }}</span>
                </label>
                <RichEditor :model-value="model.content" @update:model-value="model && (model.content = $event)" />
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
  <!--end::저널 Reflection 등록/수정 모달-->
</template>

<script setup lang="ts">
import { swalConfirm, swalRequestError, swalAjaxResult } from "@/shared/utils/swal";
import { useSafeModalClose } from "@/shared/utils/safeModalClose";
import { ref, computed, watch, onMounted } from "vue";
import { storeToRefs } from "pinia";
import RichEditor from "@/shared/ui/editor/RichEditor.vue";
import { Modal } from "bootstrap";
import axios from "axios";
import { useJournalModalStore } from "@/features/journal/stores/journalModal";
import { useJournalStore } from "@/features/journal/stores/journal";
import { useJournalThreadStore } from "@/features/journal/stores/journalThread";
import { useRoute } from "vue-router";
import { refreshJournalEntryHostForRoute } from "@/features/journal/utils/journalEntryHostRefresh";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { getWeekDayStr } from "@/features/journal/utils/journalDate";

const modalStore = useJournalModalStore();
const { reflectionRegistModel, reflectionRegistOpen } = storeToRefs(modalStore);
const journalStore = useJournalStore();
const threadStore = useJournalThreadStore();
const { t } = useLocaleStore();
const route = useRoute();

const modalEl = ref<HTMLElement | null>(null);
const submitting = ref(false);
let bsModal: InstanceType<typeof Modal> | null = null;
const { closeArmed, requestSafeClose, resetSafeClose } = useSafeModalClose(() => {
  modalStore.closeReflectionRegist();
});

const model = reflectionRegistModel;

/** 수정 모드 여부 */
const isModify = computed(() => !!model.value?.id);

/** 제목 컬럼 폭: 수정 모드는 순번(#) 칸을 확보한다. */
const titleColClass = computed(() => (isModify.value ? "col-lg-11" : "col-lg-12"));

onMounted(() => {
  if (modalEl.value) {
    bsModal = new Modal(modalEl.value, { backdrop: "static", keyboard: false });
    /* bootstrap 이벤트로 store와 상태를 동기화한다 */
    modalEl.value.addEventListener("hidden.bs.modal", () => {
      resetSafeClose();
      modalStore.closeReflectionRegist();
    });
  }
});

watch(
  reflectionRegistOpen,
  (isOpen) => {
    if (isOpen) {
      resetSafeClose();
      bsModal?.show();
    } else bsModal?.hide();
  }
);

function close() {
  resetSafeClose();
  modalStore.closeReflectionRegist();
}

function parseSavedReflectionId(value: unknown): string | null {
  if (value === undefined || value === null) return null;
  const raw = String(value).trim();
  return /^\d+$/.test(raw) ? raw : null;
}

/** 등록 응답에서 신규 Reflection ID를 해석한다. */
function resolveSavedReflectionId(responseData: Record<string, unknown>): string | undefined {
  const rsltObj = responseData.rsltObj as Record<string, unknown> | undefined;
  const list = (responseData.rsltMap as { targetReflectionList?: { id?: number }[] } | undefined)
    ?.targetReflectionList;
  const fromList = Array.isArray(list)
    ? list.map((row) => row?.id).filter((id): id is number => id != null).sort((a, b) => b - a)[0]
    : undefined;
  const candidates = [responseData.id, responseData.rsltId, rsltObj?.id, fromList];
  for (const value of candidates) {
    const id = parseSavedReflectionId(value);
    if (id) return id;
  }
  return undefined;
}

/** 등록/수정 처리. Reflection 은 Entry 이므로 entry 등록/수정 API(POST)를 쓴다. */
async function submit() {
  if (!model.value) return;

  const confirmed = await swalConfirm(isModify.value ? t("common.confirm.mdf") : t("common.confirm.reg"));
  if (!confirmed) return;

  submitting.value = true;
  try {
    const formData = new FormData();
    if (model.value.id) formData.append("id", String(model.value.id));
    formData.append("contentType", "JOURNAL_REFLECTION");
    /* Reflection 은 대상 필수(About-A) — refId/refContentType 을 반드시 싣는다. */
    if (model.value.refId != null) formData.append("refId", String(model.value.refId));
    if (model.value.refContentType) formData.append("refContentType", model.value.refContentType);
    formData.append("title", model.value.title ?? "");
    formData.append("content", model.value.content ?? "");

    /* Reflection 은 별도 Aggregate(journal_reflection) 전용 등록/수정 API(POST)를 쓴다. */
    const url = isModify.value
      ? `/api/journal/reflection/${model.value.id}`
      : "/api/journal/reflections";
    const res = await axios.post(url, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });

    if (res.data?.rslt) {
      const createdChapterId = !isModify.value ? model.value.journalChapterId : null;
      // 일회성 접힘 ID는 챕터 펼침·부분 패치(마운트)보다 먼저 심어 expand signal 경합을 막는다.
      if (!isModify.value) {
        const createdReflectionId = resolveSavedReflectionId(res.data as Record<string, unknown>);
        if (createdReflectionId) {
          modalStore.requestReflectionCreatedCollapse(createdReflectionId);
        } else {
          console.warn("[JournalReflectionRegistModal] 등록 응답에서 Reflection ID를 해석하지 못함");
        }
      }
      if (createdChapterId != null) {
        modalStore.requestEntryCreatedChapterExpand(createdChapterId);
      }
      // close() 전에 부분 갱신에 필요한 target 정보를 캡처한다.
      const refId = model.value.refId;
      const refContentType = model.value.refContentType;
      close();
      await swalAjaxResult({
        rslt: true,
        message: res.data?.message,
        successFallback: isModify.value ? t("common.result.modified") : t("common.result.registered"),
      });
      // 부분 갱신: 응답에 target reflectionList 가 있으면 dayList in-place 교체로 fetchDays() 생략
      const rsltMap = res.data?.rsltMap;
      if (rsltMap?.targetReflectionList && refId && refContentType) {
        journalStore.patchEntryReflections(
          refId,
          refContentType,
          rsltMap.targetReflectionList,
          rsltMap.targetLifecycleKey,
        );
      } else {
        // enrichment 실패 fallback: 전체 재조회
        void refreshJournalEntryHostForRoute(journalStore, threadStore, route);
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
