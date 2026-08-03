<template>
  <div class="user-prefix-settings">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h4 class="mb-1">{{ t("user.my.prefixes.title") }}</h4>
        <p class="text-muted mb-0">{{ t("user.my.prefixes.description") }}</p>
      </div>
    </div>

    <section
      v-for="group in prefixTargetGroups"
      :key="group.labelKey"
      class="user-prefix-domain-group"
    >
      <h5 class="text-muted fs-8 fw-bold mb-2 px-1">{{ t(group.labelKey) }}</h5>
      <div
        class="list-group user-prefix-target-list"
        role="group"
        :aria-label="`${t('user.my.prefixes.target.aria-label')}: ${t(group.labelKey)}`"
      >
        <button
          v-for="target in group.targets"
          :key="target.contentType"
          type="button"
          class="list-group-item list-group-item-action d-flex align-items-center justify-content-between gap-4 py-4"
          @click="openModal(target.contentType)"
        >
          <span>
            <span class="d-block fw-bold">{{ t(target.labelKey) }}</span>
            <span class="d-block text-muted fs-8 mt-1">{{ t(target.descriptionKey) }}</span>
          </span>
          <i class="bi bi-chevron-right text-muted"></i>
        </button>
      </div>
    </section>

    <template v-if="selectedTarget">
      <div
        class="modal fade show d-block"
        tabindex="-1"
        role="dialog"
        aria-modal="true"
        aria-labelledby="user-prefix-modal-title"
        @keydown.esc="closeModal"
      >
        <div class="modal-dialog modal-xl">
          <div class="modal-content">
            <div class="modal-header">
              <div>
                <h5 id="user-prefix-modal-title" class="modal-title">{{ t(selectedTarget.labelKey) }}</h5>
                <p class="text-muted fs-8 mb-0 mt-1">{{ t(selectedTarget.descriptionKey) }}</p>
              </div>
              <button type="button" class="btn-close" :disabled="store.saving" @click="closeModal"></button>
            </div>

            <div class="modal-body">
              <div v-if="errorMessage" class="alert alert-danger py-3">{{ errorMessage }}</div>

              <div class="d-flex justify-content-end mb-4">
                <button
                  type="button"
                  class="btn btn-sm btn-primary"
                  :disabled="store.loading || store.saving"
                  @click="beginNewPrefix"
                >
                  <i class="bi bi-plus-lg me-1"></i>{{ t("user.my.prefixes.add") }}
                </button>
              </div>

              <form v-if="editing" class="border rounded p-4 mb-5 bg-light" @submit.prevent="submitPrefix">
                <div class="row g-3 align-items-end">
                  <div class="col-md-6">
                    <label class="form-label required">{{ t("common.name") }}</label>
                    <input v-model.trim="form.name" class="form-control form-control-sm" maxlength="100" />
                  </div>
                  <div class="col-md-2">
                    <label class="form-label">{{ t("user.my.prefixes.color") }}</label>
                    <input v-model="form.color" type="color" class="form-control form-control-sm form-control-color w-100" />
                  </div>
                  <div class="col-md-2">
                    <label class="form-label">{{ t("common.sort-order") }}</label>
                    <input v-model.number="form.sortOrder" type="number" min="0" class="form-control form-control-sm" />
                  </div>
                  <div class="col-md-2 d-flex gap-2">
                    <button type="button" class="btn btn-sm btn-light flex-grow-1" @click="cancelEdit">{{ t("common.cancel") }}</button>
                    <button type="submit" class="btn btn-sm btn-primary flex-grow-1" :disabled="store.saving">{{ t("common.save") }}</button>
                  </div>
                </div>
              </form>

              <div v-if="store.loading" class="user-prefix-loading">
                <span class="spinner-border spinner-border-sm me-2"></span>{{ t("common.loading") }}
              </div>
              <div v-else-if="!store.prefixes.length" class="text-muted text-center py-8">
                {{ t("user.my.prefixes.empty") }}
              </div>
              <div v-for="prefix in store.prefixes" v-else :key="prefix.id" class="d-flex align-items-center border-bottom py-3">
                <span class="prefix-color me-3" :style="{ backgroundColor: prefix.color || '#A1A5B7' }"></span>
                <div class="flex-grow-1">
                  <span :class="{ 'text-muted text-decoration-line-through': prefix.activeYn === 'N' }">{{ prefix.name }}</span>
                  <span class="text-muted fs-8 ms-2">{{ t("common.sort-order") }} {{ prefix.sortOrder }}</span>
                </div>
                <button
                  type="button"
                  class="btn btn-sm btn-light me-2"
                  :disabled="store.saving"
                  @click="beginEditPrefix(prefix)"
                >
                  {{ t("common.edit") }}
                </button>
                <button
                  type="button"
                  :class="prefix.activeYn === 'Y' ? 'btn-light-danger' : 'btn-light-success'"
                  class="btn btn-sm"
                  :disabled="store.saving"
                  @click="togglePrefixActive(prefix)"
                >
                  {{ prefix.activeYn === "Y" ? t("status.unuse") : t("status.use") }}
                </button>
              </div>
            </div>

            <div class="modal-footer">
              <button type="button" class="btn btn-sm btn-light" :disabled="store.saving" @click="closeModal">
                {{ t("common.close") }}
              </button>
            </div>
          </div>
        </div>
      </div>
      <div class="modal-backdrop fade show"></div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import axios from "axios";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { swalConfirm } from "@/shared/utils/swal";
import { useUserPrefixesStore, type UserPrefix } from "@/features/user/stores/userPrefixes";

const store = useUserPrefixesStore();
const { t } = useLocaleStore();
const errorMessage = ref("");
const editing = ref(false);
const form = reactive<UserPrefix>({ name: "", color: "#009EF7", sortOrder: 0 });

type PersonalPrefixContentType =
  | "JOURNAL_THREAD"
  | "JOURNAL_CHAPTER_DIARY"
  | "JOURNAL_CHAPTER_NOTE"
  | "JOURNAL_DIARY"
  | "JOURNAL_DREAM"
  | "JOURNAL_NOTE";

interface PersonalPrefixTarget {
  contentType: PersonalPrefixContentType;
  labelKey: string;
  descriptionKey: string;
}

interface PersonalPrefixTargetGroup {
  labelKey: string;
  targets: readonly PersonalPrefixTarget[];
}

/**
 * 개인 말머리 관리 대상 그룹.
 * 현재는 저널 도메인의 개인 말머리 content_type 여섯 종류를 정해진 순서로 노출한다.
 * 챕터 말머리는 일기 챕터(JOURNAL_CHAPTER_DIARY)와 노트 챕터(JOURNAL_CHAPTER_NOTE)로 각각 분리 관리한다.
 * 다른 도메인을 지원할 때는 영속 구조를 바꾸지 않고 이 화면 카탈로그에 그룹을 추가한다.
 * Prefix Scope가 아직 없어도 대상 행은 표시하며 첫 Prefix 등록 시 서버가 Scope를 lazy 생성한다.
 */
const prefixTargetGroups: readonly PersonalPrefixTargetGroup[] = [{
  labelKey: "user.my.prefixes.domain.journal",
  targets: [
    {
      contentType: "JOURNAL_CHAPTER_DIARY",
      labelKey: "user.my.prefixes.target.chapter-diary",
      descriptionKey: "user.my.prefixes.target.chapter-diary.description",
    },
    {
      contentType: "JOURNAL_CHAPTER_NOTE",
      labelKey: "user.my.prefixes.target.chapter-note",
      descriptionKey: "user.my.prefixes.target.chapter-note.description",
    },
    {
      contentType: "JOURNAL_DIARY",
      labelKey: "user.my.prefixes.target.diary",
      descriptionKey: "user.my.prefixes.target.diary.description",
    },
    {
      contentType: "JOURNAL_DREAM",
      labelKey: "user.my.prefixes.target.dream",
      descriptionKey: "user.my.prefixes.target.dream.description",
    },
    {
      contentType: "JOURNAL_NOTE",
      labelKey: "user.my.prefixes.target.note",
      descriptionKey: "user.my.prefixes.target.note.description",
    },
    {
      contentType: "JOURNAL_THREAD",
      labelKey: "user.my.prefixes.target.thread",
      descriptionKey: "user.my.prefixes.target.thread.description",
    },
  ],
}];
const prefixTargets = prefixTargetGroups.flatMap((group) => group.targets);
const selectedContentType = ref<PersonalPrefixContentType | null>(null);
const selectedTarget = computed<PersonalPrefixTarget | null>(() =>
  prefixTargets.find((target) => target.contentType === selectedContentType.value) ?? null,
);

function report(error: unknown) {
  console.error("[UserMyPrefixes] 말머리 관리 요청 실패", error);
  if (axios.isAxiosError(error) && typeof error.response?.data?.message === "string") {
    errorMessage.value = error.response.data.message;
    return;
  }
  errorMessage.value = error instanceof Error ? error.message : t("common.error");
}

function resetForm() {
  Object.assign(form, { id: undefined, name: "", color: "#009EF7", sortOrder: 0 });
}

function beginNewPrefix() {
  resetForm();
  editing.value = true;
}
function beginEditPrefix(prefix: UserPrefix) {
  Object.assign(form, { ...prefix, color: prefix.color || "#009EF7" });
  editing.value = true;
}

function cancelEdit() {
  editing.value = false;
  resetForm();
}

async function submitPrefix() {
  if (!form.name) {
    errorMessage.value = t("user.my.prefixes.name.required");
    return;
  }
  if (!selectedContentType.value) {
    console.error("[UserMyPrefixes] 저장 대상 contentType 누락");
    return;
  }
  try {
    await store.savePrefix(selectedContentType.value, { ...form });
    editing.value = false;
    errorMessage.value = "";
  } catch (error) {
    report(error);
  }
}
async function togglePrefixActive(prefix: UserPrefix) {
  if (!prefix.id || !await swalConfirm(t(
    prefix.activeYn === "Y"
      ? "user.my.prefixes.disable.confirm"
      : "user.my.prefixes.enable.confirm",
  ))) return;
  if (!selectedContentType.value) {
    console.error("[UserMyPrefixes] 활성 상태 변경 대상 contentType 누락", { prefixId: prefix.id });
    return;
  }
  try {
    await store.setPrefixActive(selectedContentType.value, prefix.id, prefix.activeYn !== "Y");
  } catch (error) {
    report(error);
  }
}

async function openModal(contentType: PersonalPrefixContentType) {
  editing.value = false;
  resetForm();
  errorMessage.value = "";
  store.clearPrefixes();
  selectedContentType.value = contentType;
  try {
    await store.fetchPrefixes(contentType);
  } catch (error) {
    report(error);
  }
}

async function closeModal() {
  if (store.saving) {
    console.warn("[UserMyPrefixes] 저장 중 모달 닫기 요청 무시", {
      contentType: selectedContentType.value,
    });
    return;
  }
  if (editing.value && !await swalConfirm(t("user.my.prefixes.modal.close.confirm"))) return;
  editing.value = false;
  resetForm();
  errorMessage.value = "";
  selectedContentType.value = null;
  store.clearPrefixes();
}

onMounted(() => store.clearPrefixes());
</script>

<style scoped>
.prefix-color { width:14px; height:14px; border-radius:50%; flex:0 0 auto; }
.user-prefix-domain-group:not(:last-of-type) { margin-bottom:2rem; }
.user-prefix-target-list .list-group-item { border-left:0; border-right:0; }
.user-prefix-loading {
  display:flex;
  align-items:center;
  justify-content:center;
  min-height:120px;
  color:var(--bs-gray-600);
}
</style>
