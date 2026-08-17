<template>
  <div class="tmplat-admin-page">
    <!--begin::뷰 툴바 — code 관리와 동일 골격(mt-3 mb-1). ASIDE 없음.-->
    <div class="tmplat-admin-view-toolbar d-flex flex-column-fluid justify-content-end align-items-start align-items-xl-center gap-4 w-100">
      <div class="d-flex align-items-center flex-shrink-0 pe-5 mt-3 mb-1 gap-2">
        <button type="button" class="btn btn-sm btn-primary text-nowrap" @click="openCreate">
          <i class="bi bi-plus-lg"></i>
          {{ t('tmplat.register') }}
        </button>
      </div>
    </div>
    <!--end::뷰 툴바-->

    <div class="card post" style="margin-top: 0 !important;">
      <div class="card-body">
        <div v-if="store.error" class="alert alert-warning py-2">{{ store.error }}</div>
        <div v-if="store.loading" class="tmplat-admin-loading">
          <span class="spinner-border spinner-border-sm me-2"></span>
          {{ t('common.loading') }}
        </div>

        <div v-else class="table-responsive">
          <table class="table align-middle table-row-dashed fs-small gy-4 mb-0">
            <thead>
              <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 text-muted">
                <th class="text-center hidden-table">{{ t('tmplat.list.number') }}</th>
                <th>{{ t('tmplat.list.title') }}</th>
                <th class="text-center tmplat-admin-order-col hidden-table">{{ t('tmplat.list.sort-order') }}</th>
                <th class="text-center">{{ t('common.use') }}</th>
                <th class="text-center tmplat-admin-manage-col">{{ t('tmplat.list.manage') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="!store.error && !store.rows.length">
                <td colspan="5" class="text-center text-muted py-8">{{ t('tmplat.empty') }}</td>
              </tr>
              <tr v-for="row in store.rows" :key="row.id" class="cursor-pointer" @click="onRowClick($event, row.id)">
                <td class="text-center hidden-table text-gray-600">{{ row.rnum }}</td>
                <td><strong>{{ row.title }}</strong></td>
                <td class="text-center hidden-table text-gray-600">{{ row.sortOrder ?? 0 }}</td>
                <td class="text-center">
                  <span :class="isUse(row.useYn) ? 'text-success' : 'text-muted'">
                    {{ isUse(row.useYn) ? t('status.use') : t('status.unuse') }}
                  </span>
                </td>
                <td class="text-center">
                  <!--begin::컨텍스트 메뉴 — code 관리와 동일(KTMenu + overflow portal).-->
                  <div class="d-flex justify-content-center">
                    <button
                      type="button"
                      class="btn btn-sm btn-icon btn-bg-light btn-active-color-primary"
                      data-kt-menu-trigger="click"
                      data-kt-menu-placement="bottom-end"
                      data-kt-menu-overflow="true"
                      :title="t('common.menu')"
                    >
                      <i class="ki-solid ki-dots-horizontal fs-2x"></i>
                    </button>
                    <div
                      class="menu menu-sub menu-sub-dropdown menu-column menu-rounded menu-gray-800 menu-state-bg-light-primary fw-semibold w-200px py-3"
                      data-kt-menu="true"
                      @click.stop
                    >
                      <div class="menu-item px-3 my-1">
                        <div class="menu-link flex-stack px-3" @click="openEdit(row.id)">
                          {{ t('common.edit') }}
                          <i class="bi bi-pencil-square fs-8"></i>
                        </div>
                      </div>
                      <div class="separator my-2"></div>
                      <div class="menu-item px-3 my-1">
                        <div class="menu-link flex-stack px-3 text-danger" @click="remove(row)">
                          {{ t('common.delete') }}
                          <i class="bi bi-trash text-danger p-0 fs-8"></i>
                        </div>
                      </div>
                    </div>
                  </div>
                  <!--end::컨텍스트 메뉴-->
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <template v-if="store.modalOpen">
      <div class="modal fade show d-block" tabindex="-1" role="dialog" aria-modal="true">
        <div class="modal-dialog modal-lg">
          <div class="modal-content">
            <form @submit.prevent="submit">
              <div class="modal-header">
                <h5 class="modal-title">{{ store.isEdit ? t('tmplat.modal.title.edit') : t('tmplat.modal.title.register') }}</h5>
                <button type="button" class="btn-close" @click="store.closeModal"></button>
              </div>
              <div class="modal-body">
                <div class="tmplat-admin-form">
                  <div class="tmplat-admin-form-row">
                    <label for="tmplatTitle" class="form-label required">{{ t('tmplat.form.title') }}</label>
                    <input id="tmplatTitle" v-model.trim="store.form.title" type="text" class="form-control form-control-solid" maxlength="200" required />
                  </div>
                  <div class="tmplat-admin-form-row">
                    <label class="form-label required">{{ t('tmplat.form.content') }}</label>
                    <RichEditor v-model="store.form.content" :height="320" />
                  </div>
                  <div class="tmplat-admin-form-row">
                    <label for="tmplatSortOrder" class="form-label">{{ t('tmplat.form.sort-order') }}</label>
                    <input id="tmplatSortOrder" v-model.number="store.form.sortOrder" type="number" min="0" max="9999" class="form-control form-control-solid tmplat-admin-sort-input" />
                  </div>
                  <div class="tmplat-admin-form-row">
                    <label for="tmplatUseYn" class="form-label">{{ t('tmplat.form.use-yn') }}</label>
                    <div class="form-check form-switch form-check-custom form-check-solid">
                      <input id="tmplatUseYn" class="form-check-input cursor-pointer" type="checkbox" :checked="store.form.useYn === 'Y'" @change="onUseYnChange" />
                      <label class="form-check-label ms-3" for="tmplatUseYn">{{ store.form.useYn === "Y" ? t('status.use') : t('status.unuse') }}</label>
                    </div>
                  </div>
                </div>
              </div>
              <div class="modal-footer">
                <button type="button" class="btn btn-sm btn-light" @click="store.closeModal">{{ t('common.close') }}</button>
                <button type="submit" class="btn btn-sm btn-primary" :disabled="store.saving">
                  <span v-if="store.saving" class="spinner-border spinner-border-sm me-1"></span>
                  <i v-else class="bi bi-check-lg"></i>
                  {{ t('common.save') }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
      <div class="modal-backdrop fade show"></div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { swalConfirm, swalAlert } from "@/shared/utils/swal";
import { onMounted, watch } from "vue";
import { isMetronicMenuEventTarget, reinitMetronicAfterDom } from "@/shared/utils/metronicReinit";
import { useTmplatAdminStore, type TmplatRow } from "@/features/admin/stores/tmplat";
import RichEditor from "@/shared/ui/editor/RichEditor.vue";

const store = useTmplatAdminStore();
const { t } = useLocaleStore();

function isUse(value: string | undefined): boolean {
  return String(value ?? "N").toUpperCase() === "Y";
}

function onUseYnChange(event: Event) {
  store.form.useYn = (event.target as HTMLInputElement).checked ? "Y" : "N";
}

async function openCreate() {
  try {
    await store.openCreate();
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : t("tmplat.load.failure"));
  }
}

async function openEdit(id: number) {
  try {
    await store.openEdit(id);
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : t("tmplat.load.failure"));
  }
}

async function submit() {
  if (!store.form.title.trim()) {
    void swalAlert(t("tmplat.validate.title.required"));
    return;
  }
  try {
    await store.submit();
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : t("tmplat.save.failure"));
  }
}

async function remove(row: TmplatRow) {
  if (!await swalConfirm(t("tmplat.delete.confirm").replace("{title}", row.title))) return;
  try {
    await store.remove(row.id);
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : t("tmplat.delete.failure"));
  }
}

/** 목록 렌더가 끝나면 Metronic 컨텍스트 메뉴를 재바인딩한다. */
watch(
  () => store.loading,
  (loading, wasLoading) => {
    if (wasLoading && !loading) void reinitMetronicAfterDom();
  }
);

function onRowClick(event: MouseEvent, id: number): void {
  if (isMetronicMenuEventTarget(event.target)) return;
  openEdit(id);
}

onMounted(async () => {
  await store.fetchList();
});
</script>

<style scoped>
.tmplat-admin-page {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.tmplat-admin-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 120px;
  color: var(--bs-gray-600);
}

.tmplat-admin-manage-col {
  width: 104px;
}

.tmplat-admin-order-col {
  width: 96px;
}

.tmplat-admin-form {
  display: grid;
  gap: 1rem;
}

.tmplat-admin-form-row {
  display: grid;
  grid-template-columns: 140px minmax(0, 1fr);
  gap: 1rem;
  align-items: start;
}

.tmplat-admin-form-row > .form-label {
  padding-top: 0.75rem;
  font-weight: 700;
}

.tmplat-admin-sort-input {
  width: 120px;
}

@media (max-width: 768px) {
  .tmplat-admin-form-row {
    grid-template-columns: 1fr;
  }

  .tmplat-admin-form-row > .form-label {
    padding-top: 0;
  }
}
</style>