<template>
  <div class="board-group-page">
    <!--begin::뷰 툴바 — 저널 스레드·게시판·코드/계정 관리 액션 행과 동일(mt-3 mb-1). ASIDE 없음. 탭용 mt-5 빈 여백은 두지 않는다.-->
    <div class="board-group-view-toolbar d-flex flex-column-fluid justify-content-end align-items-start align-items-xl-center gap-4 w-100">
      <div class="d-flex align-items-center flex-shrink-0 pe-5 mt-3 mb-1 gap-2">
        <button type="button" class="btn btn-sm btn-primary text-nowrap" @click="store.openCreate">
          <i class="bi bi-plus-lg"></i>
          {{ t('common.register') }}
        </button>
      </div>
    </div>
    <!--end::뷰 툴바-->

    <div class="card post" style="margin-top: 0 !important;">
      <div class="card-body">
        <div class="board-group-listbar">
          <div class="board-group-search">
            <input
              v-model.trim="store.keyword"
              type="search"
              class="form-control form-control-solid"
              maxlength="200"
              :placeholder="t('board.group.search.placeholder')"
              @keyup.enter="store.fetchList(0)"
            />
            <button type="button" class="btn btn-sm btn-light-primary" :disabled="store.loading" @click="store.fetchList(0)">
              <i class="bi bi-search"></i>
            </button>
          </div>
          <div class="board-group-list-actions">
            <select
              :value="store.pageSize"
              class="form-select form-select-solid"
              :aria-label="t('common.page-size.aria-label')"
              @change="onPageSizeChange"
            >
              <option :value="10">{{ t('common.page-size.10') }}</option>
              <option :value="25">{{ t('common.page-size.25') }}</option>
              <option :value="50">{{ t('common.page-size.50') }}</option>
            </select>
            <button
              type="button"
              class="btn btn-sm btn-light-primary"
              :disabled="store.sortSaving || store.loading || store.rows.length < 2"
              @click="saveSortOrders"
            >
              <span v-if="store.sortSaving" class="spinner-border spinner-border-sm me-1"></span>
              <i v-else class="bi bi-save"></i>
              {{ t('board.group.order.save') }}
            </button>
          </div>
        </div>

        <div v-if="store.error" class="alert alert-warning py-2">{{ store.error }}</div>

        <div v-if="store.loading" class="board-group-loading">
          <span class="spinner-border spinner-border-sm me-2"></span>
          {{ t('common.loading') }}
        </div>

        <div v-else class="table-responsive">
          <table class="table align-middle table-row-dashed fs-small gy-4 mb-0">
            <thead>
              <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 text-muted">
                <th class="text-center board-group-order-col">{{ t('board.group.list.order') }}</th>
                <th>{{ t('board.group.list.board-name-short') }}</th>
                <th class="text-center hidden-table">{{ t('board.group.list.category-code') }}</th>
                <th class="hidden-table">{{ t('board.group.list.col.description') }}</th>
                <th class="text-center hidden-table">{{ t('board.group.list.posts') }}</th>
                <th class="text-center">{{ t('common.use') }}</th>
                <th class="text-center board-group-manage-col">{{ t('board.group.list.manage') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="!store.rows.length">
                <td colspan="7" class="text-center text-muted py-8">{{ t('board.group.empty') }}</td>
              </tr>
              <tr v-for="(row, index) in store.rows" :key="row.id">
                <td class="text-center">
                  <div class="board-group-order">
                    <button
                      type="button"
                      class="btn btn-sm btn-icon btn-light"
                      :disabled="index === 0"
                      :title="t('common.move-up')"
                      @click="store.moveRow(index, -1)"
                    >
                      <i class="bi bi-chevron-up"></i>
                    </button>
                    <button
                      type="button"
                      class="btn btn-sm btn-icon btn-light"
                      :disabled="index === store.rows.length - 1"
                      :title="t('common.move-down')"
                      @click="store.moveRow(index, 1)"
                    >
                      <i class="bi bi-chevron-down"></i>
                    </button>
                  </div>
                </td>
                <td>
                  <div class="board-group-name">
                    <strong>{{ row.boardName }}</strong>
                    <span>{{ row.boardKey }}</span>
                  </div>
                </td>
                <td class="text-center hidden-table text-gray-700">{{ row.categoryGroupCode || "-" }}</td>
                <td class="hidden-table">
                  <div class="board-group-description">{{ row.description || "-" }}</div>
                </td>
                <td class="text-center hidden-table text-gray-700">{{ formatNumber(row.postCount) }}</td>
                <td class="text-center">
                  <button
                    type="button"
                    class="badge board-group-status"
                    :class="isUse(row) ? 'badge-light-success' : 'badge-light'"
                    @click="toggleUse(row)"
                  >
                    <i :class="isUse(row) ? 'bi bi-check2' : 'bi bi-x-lg'"></i>
                    {{ isUse(row) ? t('status.use') : t('status.unuse') }}
                  </button>
                </td>
                <td class="text-center" @click.stop>
                  <!--begin::컨텍스트 메뉴
                    변경 전: Metronic data-kt-menu 를 썼으나 .table-responsive(overflow) 안에서
                    드롭다운이 잘려 보이지 않았다. 메뉴 관리와 동일하게 Bootstrap dropdown + strategy:fixed.
                  -->
                  <div class="dropdown d-inline-flex justify-content-center">
                    <button
                      type="button"
                      class="btn btn-sm btn-icon btn-bg-light btn-active-color-primary"
                      data-bs-toggle="dropdown"
                      data-bs-auto-close="true"
                      data-bs-popper-config='{"strategy":"fixed"}'
                      aria-expanded="false"
                      :title="t('common.menu')"
                    >
                      <i class="ki-solid ki-dots-horizontal fs-2x"></i>
                    </button>
                    <div class="dropdown-menu dropdown-menu-end">
                      <button type="button" class="dropdown-item d-flex flex-stack" @click="store.openEdit(row.id)">
                        <span>{{ t('common.edit') }}</span>
                        <i class="bi bi-pencil-square fs-8"></i>
                      </button>
                      <div class="dropdown-divider"></div>
                      <button type="button" class="dropdown-item d-flex flex-stack text-danger" @click="deleteBoard(row)">
                        <span>{{ t('common.delete') }}</span>
                        <i class="bi bi-trash text-danger p-0 fs-8"></i>
                      </button>
                    </div>
                  </div>
                  <!--end::컨텍스트 메뉴-->
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="card-footer board-group-footer">
        <span class="text-muted fs-8">{{ t('board.group.pagination.total-format').replace('{0}', formatNumber(store.totalElements)) }}</span>
        <div v-if="pageNumbers.length" class="pagination mb-0">
          <button
            type="button"
            class="page-link"
            :disabled="store.currentPage <= 0"
            @click="store.fetchList(0)"
          >
            <i class="previous"></i>
          </button>
          <button
            v-for="page in pageNumbers"
            :key="page"
            type="button"
            class="page-link"
            :class="{ active: page === store.currentPage }"
            @click="store.fetchList(page)"
          >
            {{ page + 1 }}
          </button>
          <button
            type="button"
            class="page-link"
            :disabled="store.currentPage >= store.totalPages - 1"
            @click="store.fetchList(store.totalPages - 1)"
          >
            <i class="next"></i>
          </button>
        </div>
      </div>
    </div>

    <template v-if="store.modalOpen">
      <div class="modal fade show d-block" tabindex="-1" role="dialog" aria-modal="true">
        <div class="modal-dialog modal-lg">
          <div class="modal-content">
            <form @submit.prevent="submitForm">
              <div class="modal-header">
                <h5 class="modal-title">{{ store.isEdit ? t('board.group.modal.title.edit') : t('board.group.modal.title.register') }}</h5>
                <button type="button" class="btn-close" @click="store.closeModal"></button>
              </div>
              <div class="modal-body">
                <div v-if="store.detailLoading" class="board-group-loading">
                  <span class="spinner-border spinner-border-sm me-2"></span>
                  {{ t('common.loading') }}
                </div>
                <div v-else class="board-group-form">
                  <div class="board-group-form-row">
                    <label for="boardKey" class="form-label required">{{ t('board.group.form.board-code') }}</label>
                    <div>
                      <input
                        id="boardKey"
                        v-model.trim="store.form.boardKey"
                        type="text"
                        class="form-control form-control-solid"
                        maxlength="30"
                        :readonly="store.isEdit"
                        required
                      />
                      <div class="text-muted fs-8 mt-1">{{ t('board.group.form.board-code-guide-full') }}</div>
                    </div>
                  </div>
                  <div class="board-group-form-row">
                    <label for="boardName" class="form-label required">{{ t('board.group.form.board-name') }}</label>
                    <input
                      id="boardName"
                      v-model.trim="store.form.boardName"
                      type="text"
                      class="form-control form-control-solid"
                      maxlength="120"
                      required
                    />
                  </div>
                  <div class="board-group-form-row">
                    <label for="categoryGroupCode" class="form-label">{{ t('board.group.form.category-code') }}</label>
                    <input
                      id="categoryGroupCode"
                      v-model.trim="store.form.categoryGroupCode"
                      type="text"
                      class="form-control form-control-solid"
                      maxlength="30"
                    />
                  </div>
                  <div class="board-group-form-row">
                    <label for="description" class="form-label">{{ t('board.group.list.col.description') }}</label>
                    <textarea
                      id="description"
                      v-model.trim="store.form.description"
                      class="form-control form-control-solid"
                      rows="4"
                      maxlength="2000"
                    ></textarea>
                  </div>
                  <div class="board-group-form-row">
                    <label for="useYn" class="form-label">{{ t('board.group.form.use-yn') }}</label>
                    <div class="form-check form-switch form-check-custom form-check-solid">
                      <input
                        id="useYn"
                        class="form-check-input cursor-pointer"
                        type="checkbox"
                        :checked="store.form.useYn === 'Y'"
                        @change="onUseYnChange"
                      />
                      <label class="form-check-label ms-3" for="useYn">{{ store.form.useYn === "Y" ? t('status.use') : t('status.unuse') }}</label>
                    </div>
                  </div>
                </div>
              </div>
              <div class="modal-footer">
                <button type="button" class="btn btn-sm btn-light" @click="store.closeModal">{{ t('common.close') }}</button>
                <button type="submit" class="btn btn-sm btn-primary" :disabled="store.saving || store.detailLoading">
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
import { computed, onMounted } from "vue";
import { useBoardGroupStore, type BoardGroupRow } from "@/features/admin/stores/boardGroup";

const store = useBoardGroupStore();
const { t } = useLocaleStore();

const pageNumbers = computed(() => {
  if (store.totalPages <= 1) return [];
  const start = Math.max(0, store.currentPage - 2);
  const end = Math.min(store.totalPages - 1, store.currentPage + 2);
  const pages: number[] = [];
  for (let page = start; page <= end; page += 1) pages.push(page);
  return pages;
});

function formatNumber(value: number | undefined): string {
  return new Intl.NumberFormat().format(Number(value) || 0);
}

function isUse(row: BoardGroupRow): boolean {
  return String(row.useYn).toUpperCase() === "Y";
}

function validateForm(): boolean {
  if (!store.form.boardKey.trim()) {
    void swalAlert(t("board.group.validate.board-key.required"));
    return false;
  }
  if (!/^[A-Za-z0-9_-]+$/.test(store.form.boardKey.trim())) {
    void swalAlert(t("board.group.validate.board-key.format"));
    return false;
  }
  if (!store.form.boardName.trim()) {
    void swalAlert(t("board.group.validate.board-name.required"));
    return false;
  }
  return true;
}

async function onPageSizeChange(event: Event) {
  const target = event.target as HTMLSelectElement;
  await store.changePageSize(Number(target.value));
}

function onUseYnChange(event: Event) {
  const target = event.target as HTMLInputElement;
  store.form.useYn = target.checked ? "Y" : "N";
}

async function submitForm() {
  if (!validateForm()) return;
  try {
    await store.submitForm();
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : t("board.group.save.failure"));
  }
}

async function toggleUse(row: BoardGroupRow) {
  const message = isUse(row) ? t("board.group.use-yn.disable.confirm") : t("board.group.use-yn.enable.confirm");
  if (!await swalConfirm(message)) return;
  try {
    void swalAlert(await store.toggleUse(row));
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : t("board.group.use-yn.change.failure"));
  }
}

async function deleteBoard(row: BoardGroupRow) {
  if (!await swalConfirm(t("board.group.delete.confirm").replace("{boardName}", row.boardName))) return;
  try {
    await store.deleteBoard(row.id);
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : t("board.group.delete.failure"));
  }
}

async function saveSortOrders() {
  try {
    void swalAlert(await store.saveSortOrders());
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : t("board.group.order.failure"));
  }
}

onMounted(async () => {
  await store.fetchList(0);
});
</script>

<style scoped>
.board-group-page {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.board-group-listbar,
.board-group-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
}

.board-group-search,
.board-group-list-actions,
.board-group-order {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.board-group-search {
  min-width: min(420px, 100%);
}

.board-group-search .form-control {
  min-width: 260px;
}

.board-group-list-actions .form-select {
  width: 110px;
}

.board-group-order-col {
  width: 96px;
}

.board-group-manage-col {
  width: 104px;
}

.board-group-name {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.board-group-name strong,
.board-group-description {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.board-group-name span {
  color: var(--bs-gray-600);
  font-size: 0.8rem;
}

.board-group-status {
  border: 0;
}

.board-group-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 120px;
  color: var(--bs-gray-600);
}

.board-group-form {
  display: grid;
  gap: 1rem;
}

.board-group-form-row {
  display: grid;
  grid-template-columns: 140px minmax(0, 1fr);
  gap: 1rem;
  align-items: start;
}

.board-group-form-row > .form-label {
  padding-top: 0.75rem;
  font-weight: 700;
}

.page-link.active {
  background: var(--bs-primary);
  border-color: var(--bs-primary);
  color: #fff;
}

@media (max-width: 768px) {
  .board-group-listbar,
  .board-group-footer,
  .board-group-search,
  .board-group-list-actions {
    align-items: stretch;
    width: 100%;
  }

  .board-group-search .form-control,
  .board-group-list-actions .form-select {
    width: 100%;
    min-width: 0;
  }

  .board-group-form-row {
    grid-template-columns: 1fr;
  }

  .board-group-form-row > .form-label {
    padding-top: 0;
  }
}
</style>
