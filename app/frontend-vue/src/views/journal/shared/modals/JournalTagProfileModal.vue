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
          <h5 class="modal-title">태그 프로필</h5>
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
                    카테고리 태그 색상
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
                    이 태그는 카테고리가 없어 설정할 수 없습니다.
                  </div>
                </div>
                <!--end::카테고리 태그 색상-->

                <!--begin::개별 태그 색상-->
                <div class="col-6 mb-4">
                  <label for="tagTextClassCd" class="form-label fw-bold">
                    <span :class="tagTextClass"><span>#</span>{{ model.name }}</span>
                    개별 태그 색상
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
              <label for="tagProfileCn" class="form-label fw-bold">프로필</label>
              <textarea
                id="tagProfileCn"
                v-model="model.content"
                name="content"
                class="form-control"
                rows="10"
                placeholder="이 태그가 각 콘텐츠 타입에서 어떤 의미인지 메모합니다."
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
              title="항목을 삭제합니다."
              :disabled="submitting"
              @click="onDelete"
            >
              <i class="bi bi-trash"></i>삭제
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
                <i class="bi bi-check2"></i>저장
              </button>
              <button type="button" class="btn btn-sm btn-light ms-2" @click="close">
                닫기
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
import { swalConfirm, swalAlert } from "@/utils/swal";
import { isAuthExpiredError } from "@/utils/authError";
import { ref, computed, watch, onMounted } from "vue";
import { Modal } from "bootstrap";
import { useAttachableModalStore } from "@/stores/attachableModal";
import { useJournalStore } from "@/stores/journal";

const attachableStore = useAttachableModalStore();
const journalStore = useJournalStore();

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
  hasTagCategory.value ? "카테고리와 동일 (상속)" : "기본 (카테고리 없음)"
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
  const confirmed = await swalConfirm("저장하시겠습니까?");
  if (!confirmed) return;
  submitting.value = true;
  try {
    const result = await attachableStore.saveTagProfile();
    if (result.rslt) {
      close();
      void journalStore.fetchDays();
    } else {
      void swalAlert(result.message ?? "처리에 실패했습니다.");
    }
  } catch (e: unknown) {
    if (isAuthExpiredError(e)) return;
    void swalAlert("요청 처리 중 오류가 발생했습니다.");
  } finally {
    submitting.value = false;
  }
}

/** 태그 프로필 삭제 */
async function onDelete() {
  const confirmed = await swalConfirm("삭제하시겠습니까?");
  if (!confirmed) return;
  submitting.value = true;
  try {
    const result = await attachableStore.deleteTagProfile();
    if (result.rslt) {
      close();
      void journalStore.fetchDays();
    } else {
      void swalAlert(result.message ?? "처리에 실패했습니다.");
    }
  } catch (e: unknown) {
    if (isAuthExpiredError(e)) return;
    void swalAlert("요청 처리 중 오류가 발생했습니다.");
  } finally {
    submitting.value = false;
  }
}
</script>