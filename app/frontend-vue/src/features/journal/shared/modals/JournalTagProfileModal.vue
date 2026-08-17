<template>
  <!--begin::태그 프로필 모달-->
  <div
    ref="modalEl"
    class="modal fade"
    id="tag_profile_modal"
    tabindex="-1"
    role="dialog"
    aria-hidden="true"
    data-bs-keyboard="false"
    data-bs-backdrop="static"
  >
    <div class="modal-dialog modal-dialog-centered modal-lg" role="document">
      <div class="modal-content">

        <!--begin::Modal Header-->
        <div class="modal-header">
          <h5 class="modal-title">{{ t("attachable.tag.profile.modal.title") }}</h5>
          <button type="button" class="btn-close" @click="close"></button>
        </div>
        <!--end::Modal Header-->

        <!--begin::Modal Body-->
        <div class="modal-body modal-mbl-body my-5">
          <!--begin::로딩-->
          <div v-if="attachableStore.tagProfileLoading" class="d-flex justify-content-center py-10">
            <span class="spinner-border text-primary" role="status"></span>
          </div>
          <!--end::로딩-->

          <div v-else class="row" id="tag_profile_div">
            <form id="tagProfileForm" method="post">
              <input type="hidden" name="id" :value="model.id">
              <input type="hidden" name="categoryProfileId" :value="model.categoryProfileId">
              <input type="hidden" name="tagId" :value="model.tagId">
              <input type="hidden" name="tagCategoryId" :value="model.tagCategoryId">
              <input type="hidden" name="contentType" :value="model.contentType">

              <!--begin::태그 정보 헤더-->
              <div class="d-flex align-items-center gap-2 mb-5">
                <span class="badge badge-light-primary">{{ model.contentTypeLabel }}</span>
                <span v-if="model.ctgr" class="fs-7 text-noti">[{{ model.ctgr }}]</span>
                <span class="fs-6 fw-bold text-primary"><span>#</span><span>{{ model.name }}</span></span>
              </div>
              <!--end::태그 정보 헤더-->

              <div class="row">
                <!--begin::카테고리 태그 색상-->
                <div
                  class="col-6 mb-3"
                  :class="{ 'opacity-50': !hasTagCategory }"
                  :style="!hasTagCategory ? { filter: 'grayscale(1)', pointerEvents: 'none' } : {}"
                  :aria-disabled="!hasTagCategory ? 'true' : undefined"
                >
                  <label for="tagCategoryTextClassCd" class="form-label fw-bold">
                    <span :class="categoryTextClass">{{ hasTagCategory ? '[' + model.ctgr + ']' : '' }}</span>
                    {{ t("attachable.tag.profile.category-tag-color") }}
                  </label>
                  <select
                    id="tagCategoryTextClassCd"
                    v-model="model.categoryTextClassCd"
                    name="categoryTextClassCd"
                    :class="categorySelectClass"
                    :disabled="!hasTagCategory"
                  >
                    <option
                      v-for="option in attachableStore.tagProfileTextClassOptions"
                      :key="option.code"
                      :value="option.code"
                      :class="option.description"
                    >
                      {{ option.codeName }}
                    </option>
                  </select>
                  <div v-if="!hasTagCategory" class="fs-8 text-muted mt-1 fw-bold">
                    {{ t("attachable.tag.profile.no-category-guide") }}
                  </div>
                </div>
                <!--end::카테고리 태그 색상-->

                <!--begin::개별 태그 색상-->
                <div class="col-6 mb-4">
                  <label for="tagTextClassCd" class="form-label fw-bold">
                    <span :class="tagTextClass"><span>#</span>{{ model.name }}</span>
                    {{ t("attachable.tag.profile.individual-tag-color") }}
                  </label>
                  <select
                    id="tagTextClassCd"
                    v-model="model.textClassCd"
                    name="textClassCd"
                    :class="tagSelectClass"
                  >
                    <option value="">{{ tagDefaultLabel }}</option>
                    <option
                      v-for="option in attachableStore.tagProfileTextClassOptions"
                      :key="option.code"
                      :value="option.code"
                      :class="option.description"
                    >
                      {{ option.codeName }}
                    </option>
                  </select>
                </div>
                <!--end::개별 태그 색상-->
              </div>

              <!--begin::크기 고정 (태그클라우드 전용)-->
              <div class="mb-4">
                <label class="form-label fw-bold d-block">
                  {{ t("attachable.tag.profile.cloud-size") }}
                </label>
                <div class="btn-group" role="group" aria-label="cloud size lock">
                  <input
                    id="cloudSizeAuto"
                    v-model="model.cloudSizeLock"
                    type="radio"
                    name="cloudSizeLock"
                    class="btn-check"
                    value="AUTO"
                    autocomplete="off"
                  >
                  <label class="btn btn-sm btn-light btn-active-primary" for="cloudSizeAuto">
                    {{ t("attachable.tag.profile.cloud-size.auto") }}
                  </label>

                  <input
                    id="cloudSizeMin"
                    v-model="model.cloudSizeLock"
                    type="radio"
                    name="cloudSizeLock"
                    class="btn-check"
                    value="MIN"
                    autocomplete="off"
                  >
                  <label class="btn btn-sm btn-light btn-active-primary" for="cloudSizeMin">
                    {{ t("attachable.tag.profile.cloud-size.min") }}
                  </label>

                  <input
                    id="cloudSizeMax"
                    v-model="model.cloudSizeLock"
                    type="radio"
                    name="cloudSizeLock"
                    class="btn-check"
                    value="MAX"
                    autocomplete="off"
                  >
                  <label class="btn btn-sm btn-light btn-active-primary" for="cloudSizeMax">
                    {{ t("attachable.tag.profile.cloud-size.max") }}
                  </label>
                </div>
                <div class="fs-8 text-muted mt-1">
                  {{ t(cloudSizeGuideKey) }}
                </div>
              </div>
              <!--end::크기 고정-->


              <!--begin::프로필-->
              <label for="tagProfileCn" class="form-label fw-bold">{{ t("attachable.tag.profile.profile") }}</label>
              <textarea
                id="tagProfileCn"
                v-model="model.content"
                name="content"
                class="form-control"
                rows="10"
                :placeholder="t('attachable.tag.profile.profile-placeholder')"
              ></textarea>
              <!--end::프로필-->
            </form>
          </div>
        </div>
        <!--end::Modal Body-->

        <!--begin::Modal Footer-->
        <div class="modal-footer">
          <div class="d-flex justify-content-between w-100">
            <button
              v-if="model.id"
              type="button"
              class="btn btn-sm btn-light-danger"
              data-bs-toggle="tooltip"
              data-bs-placement="top"
              :title="t('common.delete.tooltip')"
              :disabled="submitting"
              @click="onDelete"
            >
              <i class="bi bi-trash"></i>{{ t("common.delete") }}
            </button>
            <span v-else></span>
            <div class="d-flex justify-content-end ms-auto">
              <button
                type="button"
                class="btn btn-sm btn-primary"
                :disabled="submitting"
                @click="onSave"
              >
                <span v-if="submitting" class="spinner-border spinner-border-sm me-1" role="status"></span>
                <i class="bi bi-check2"></i>{{ t("common.save") }}
              </button>
              <button type="button" class="btn btn-sm btn-light ms-2" @click="close">
                {{ t("common.close") }}
              </button>
            </div>
          </div>
        </div>
        <!--end::Modal Footer-->

      </div>
    </div>
  </div>
  <!--end::태그 프로필 모달-->
</template>

<script setup lang="ts">
import { swalConfirm, swalRequestError, swalAjaxResult } from "@/shared/utils/swal";
import { ref, computed, watch, onMounted } from "vue";
import { Modal } from "bootstrap";
import { useAttachableModalStore } from "@/features/attachable/stores/attachableModal";
import { useJournalStore, type TagCloudSection } from "@/features/journal/stores/journal";
import { useJournalAnnualStore } from "@/features/journal/stores/journalAnnual";
import { useJournalThreadStore } from "@/features/journal/stores/journalThread";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { useRoute } from "vue-router";
import { refreshJournalDaysForRoute } from "@/features/journal/utils/journalDayRefresh";

const emit = defineEmits<{ success: [] }>();

const attachableStore = useAttachableModalStore();
const journalStore = useJournalStore();
const threadStore = useJournalThreadStore();
const { t } = useLocaleStore();
const route = useRoute();

const modalEl = ref<HTMLElement | null>(null);
const submitting = ref(false);
let bsModal: InstanceType<typeof Modal> | null = null;

const model = computed(() => attachableStore.tagProfileModel);


const hasTagCategory = computed(() => {
  const id = String(model.value.tagCategoryId ?? "");
  return id.length > 0 && id !== "0";
});

function findTextClass(code: string): string {
  return attachableStore.tagProfileTextClassOptions.find((o) => o.code === code)?.description ?? "";
}

const categoryTextClass = computed(() => findTextClass(model.value.categoryTextClassCd));
const tagTextClass = computed(() =>
  model.value.textClassCd ? findTextClass(model.value.textClassCd) : categoryTextClass.value
);
const categorySelectClass = computed(() =>
  `form-select form-select-solid ${categoryTextClass.value}`.trim()
);
const tagSelectClass = computed(() =>
  `form-select form-select-solid ${tagTextClass.value}`.trim()
);
const tagDefaultLabel = computed(() =>
  hasTagCategory.value
    ? t("attachable.tag.profile.same-as-category")
    : t("attachable.tag.profile.default-no-category")
);

/**
 * 선택된 크기 고정 상태(AUTO/MIN/MAX)에 대응하는 안내 문구 키.
 * cloud-size.guide.{auto|min|max} 중 하나로 매핑한다.
 */
const cloudSizeGuideKey = computed(
  () => `attachable.tag.profile.cloud-size.guide.${model.value.cloudSizeLock.toLowerCase()}`
);

onMounted(() => {
  if (modalEl.value) {
    bsModal = new Modal(modalEl.value);
    modalEl.value.addEventListener("hidden.bs.modal", () => {
      attachableStore.closeTagProfile();
    });
  }

  /* TEXT_CLASS_CD 코드 목록 초기 로드 */
  void attachableStore.loadTagProfileTextClassOptions();

  /* window.JournalDayTagProfileVueApp 브리지 등록 */
  const queued = (window as any).JournalDayTagProfileVueApp as { pendingPayload?: unknown } | undefined;
  (window as any).JournalDayTagProfileVueApp = {
    mounted: true,
    open: (payload: Record<string, unknown>) => attachableStore.openTagProfile(payload),
  };
  if (queued?.pendingPayload) {
    attachableStore.openTagProfile(queued.pendingPayload as Record<string, unknown>);
  }
});

watch(
  () => attachableStore.tagProfileOpen,
  (isOpen) => {
    if (isOpen) bsModal?.show();
    else bsModal?.hide();
  }
);

function close() {
  attachableStore.closeTagProfile();
}

/**
 * contentType 에 대응하는 태그 클라우드 섹션.
 * 색(textClass)은 클라우드 API 응답에만 실리므로 저장/삭제 후 해당 섹션을 재조회한다.
 */
function tagCloudSectionsFor(contentType: string): TagCloudSection[] | undefined {
  if (contentType === "JOURNAL_DAY") return ["day"];
  if (contentType === "JOURNAL_DIARY") return ["diary"];
  if (contentType === "JOURNAL_DREAM") return ["dream"];
  return undefined;
}

/**
 * 태그 프로필 저장·삭제 후 현재 화면을 갱신한다.
 * 변경 전: refreshJournalDaysForRoute 만 호출해 태그 클라우드 색·검색 결과 프로필 본문이 남을 수 있었다.
 * 변경 후:
 * - 검색 팝업: success → loadEntries()
 * - 결산 상세: annualStore.fetchTagRows(yy, activeSection) — 결산 태그클라우드는 일자 store.fetchTagCloud 가 아니라
 *   /api/journal/annual/{yy}/tags 행이 SSOT 이므로 이 경로로 재조회한다.
 * - 스레드 상세: 소속 엔트리와 엔트리 태그 집계가 SSOT인 열린 스레드 상세를 다시 조회하고,
 *   검색·결산·일자 배경도 각 화면의 기존 갱신 경로로 이어서 갱신한다.
 * - 그 외(월간/주간/일간): 일자 목록 + contentType 대응 fetchTagCloud
 */
async function refreshAfterTagProfileChange(): Promise<void> {
  if (threadStore.detailOpen || route.name === "thread-detail") {
    await threadStore.refreshOpenDetail();
    if (route.name === "thread-detail") {
      emit("success");
      return;
    }
  }

  /* 검색 팝업은 일자/클라우드 스토어가 아니라 로컬 entries 가 SSOT — success 리스너(loadEntries)만 사용 */
  if (route.name === "journal-entry-search") {
    emit("success");
    return;
  }

  /* 결산 상세 태그클라우드는 journalAnnual.tagRows (fetchTagRows) 가 SSOT */
  if (route.name === "annual-detail") {
    const annualStore = useJournalAnnualStore();
    const yy = Number(route.params.yy) || Number(annualStore.filterYy) || 0;
    if (Number.isFinite(yy) && yy > 0) {
      await annualStore.fetchTagRows(yy, annualStore.activeSection);
    }
    emit("success");
    return;
  }

  const contentType = model.value.contentType;
  const sections = tagCloudSectionsFor(contentType);
  const tasks: Promise<unknown>[] = [refreshJournalDaysForRoute(journalStore, route)];
  if (sections) {
    tasks.push(journalStore.fetchTagCloud({ sections }));
  }
  await Promise.all(tasks);
  emit("success");
}

/** 태그 프로필 저장 */
async function onSave() {
  const confirmed = await swalConfirm(t("common.confirm.save"));
  if (!confirmed) return;
  submitting.value = true;
  try {
    const result = await attachableStore.saveTagProfile();
    if (result.rslt) {
      close();
      await swalAjaxResult({
        rslt: true,
        message: result.message,
        successFallback: t("common.result.saved"),
      });
      await refreshAfterTagProfileChange();
    } else {
      void swalAjaxResult({
        rslt: false,
        message: result.message,
        failureFallback: t("common.result.failure"),
      });
    }
  } catch (e: unknown) {
    void swalRequestError(e);
  } finally {
    submitting.value = false;
  }
}

/** 태그 프로필 삭제 */
async function onDelete() {
  const confirmed = await swalConfirm(t("common.confirm.del"));
  if (!confirmed) return;
  submitting.value = true;
  try {
    const result = await attachableStore.deleteTagProfile();
    if (result.rslt) {
      close();
      await swalAjaxResult({
        rslt: true,
        message: result.message,
        successFallback: t("common.result.deleted"),
      });
      await refreshAfterTagProfileChange();
    } else {
      void swalAjaxResult({
        rslt: false,
        message: result.message,
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
