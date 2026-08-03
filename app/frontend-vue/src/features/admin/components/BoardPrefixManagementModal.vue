<template>
  <template v-if="store.modalOpen">
    <div class="modal fade show d-block" tabindex="-1" role="dialog" aria-modal="true">
      <div class="modal-dialog modal-xl">
        <div class="modal-content">
          <div class="modal-header">
            <div>
              <h5 class="modal-title">{{ modalTitle }}</h5>
              <p class="text-muted fs-8 mb-0 mt-1">{{ t("board.group.prefix.description") }}</p>
            </div>
            <button type="button" class="btn-close" :disabled="store.saving" @click="closeModal"></button>
          </div>

          <div class="modal-body">
            <div v-if="store.error" class="alert alert-danger py-3">{{ store.error }}</div>

            <div class="d-flex justify-content-end mb-4">
              <button type="button" class="btn btn-sm btn-primary" :disabled="store.saving" @click="beginNewPrefix">
                <i class="bi bi-plus-lg me-1"></i>{{ t("board.group.prefix.add") }}
              </button>
            </div>

            <form v-if="editing" class="border rounded p-4 mb-5 bg-light" @submit.prevent="submitPrefix">
              <div class="row g-3 align-items-end">
                <div class="col-md-6">
                  <label class="form-label required">{{ t("common.name") }}</label>
                  <input v-model.trim="form.name" class="form-control form-control-sm" maxlength="100" />
                </div>
                <div class="col-md-2">
                  <label class="form-label">{{ t("board.group.prefix.color") }}</label>
                  <input v-model="form.color" type="color" class="form-control form-control-sm form-control-color w-100" />
                </div>
                <div class="col-md-2">
                  <label class="form-label">{{ t("common.sort-order") }}</label>
                  <input v-model.number="form.sortOrder" type="number" min="0" class="form-control form-control-sm" />
                </div>
                <div class="col-md-2 d-flex gap-2">
                  <button type="button" class="btn btn-sm btn-light flex-grow-1" @click="editing=false">
                    {{ t("common.cancel") }}
                  </button>
                  <button type="submit" class="btn btn-sm btn-primary flex-grow-1" :disabled="store.saving">
                    {{ t("common.save") }}
                  </button>
                </div>
              </div>
            </form>

            <div v-if="store.loading" class="board-prefix-loading">
              <span class="spinner-border spinner-border-sm me-2"></span>{{ t("common.loading") }}
            </div>
            <div v-else-if="!store.prefixes.length" class="text-muted text-center py-8">
              {{ t("board.group.prefix.empty") }}
            </div>
            <div
              v-for="prefix in store.prefixes"
              v-else
              :key="prefix.id"
              class="d-flex align-items-center border-bottom py-3"
            >
              <span class="board-prefix-color me-3" :style="{ backgroundColor: prefix.color || '#A1A5B7' }"></span>
              <div class="flex-grow-1">
                <span :class="{ 'text-muted text-decoration-line-through': prefix.activeYn === 'N' }">
                  {{ prefix.name }}
                </span>
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
                class="btn btn-sm"
                :class="prefix.activeYn === 'Y' ? 'btn-light-danger' : 'btn-light-success'"
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
</template>

<script setup lang="ts">
import { computed, reactive, ref } from "vue";
import axios from "axios";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { swalConfirm } from "@/shared/utils/swal";
import {
  useBoardPrefixesStore,
  type BoardPrefix,
} from "@/features/admin/stores/boardPrefixes";

const store = useBoardPrefixesStore();
const { t } = useLocaleStore();
const editing = ref(false);
const form = reactive<BoardPrefix>({ name: "", color: "#009EF7", sortOrder: 0 });

const modalTitle = computed(() => t("board.group.prefix.title").replace("{boardName}", store.boardName));

function report(error: unknown, fallbackKey: string) {
  if (axios.isAxiosError(error) && typeof error.response?.data?.message === "string") {
    store.error = error.response.data.message;
    return;
  }
  store.error = error instanceof Error ? error.message : t(fallbackKey);
}

function beginNewPrefix() {
  Object.assign(form, { id: undefined, name: "", color: "#009EF7", sortOrder: 0 });
  editing.value = true;
}

function beginEditPrefix(prefix: BoardPrefix) {
  Object.assign(form, { ...prefix, color: prefix.color || "#009EF7" });
  editing.value = true;
}

async function submitPrefix() {
  if (!form.name) {
    store.error = t("board.group.prefix.name.required");
    return;
  }
  try {
    await store.savePrefix({ ...form });
    editing.value = false;
    store.error = "";
  } catch (error) {
    report(error, "board.group.prefix.save.failure");
  }
}

async function togglePrefixActive(prefix: BoardPrefix) {
  if (!prefix.id || !await swalConfirm(t(
    prefix.activeYn === "Y"
      ? "board.group.prefix.disable.confirm"
      : "board.group.prefix.enable.confirm",
  ))) return;
  try {
    await store.setPrefixActive(prefix.id, prefix.activeYn !== "Y");
    store.error = "";
  } catch (error) {
    report(error, "board.group.prefix.active.failure");
  }
}

function closeModal() {
  editing.value = false;
  store.close();
}
</script>

<style scoped>
.board-prefix-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 120px;
  color: var(--bs-gray-600);
}

.board-prefix-color {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  flex: 0 0 auto;
}
</style>
