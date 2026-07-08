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
import { swalConfirm, swalAlert, swalRequestError, swalAjaxResult } from "@/shared/utils/swal";
import { ref, computed, watch, onMounted } from "vue";
import { Modal } from "bootstrap";
import { useAttachableModalStore } from "@/features/attachable/stores/attachableModal";
import { useJournalStore } from "@/features/journal/stores/journal";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { useRoute } from "vue-router";
import { refreshJournalDaysForRoute } from "@/features/journal/utils/journalDayRefresh";

const attachableStore = useAttachableModalStore();
const journalStore = useJournalStore();
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
      void refreshJournalDaysForRoute(journalStore, route);
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
      void refreshJournalDaysForRoute(journalStore, route);
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
