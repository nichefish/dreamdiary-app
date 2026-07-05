<template>
  <div class="code-admin-page">
    <div class="code-admin-toolbar">
      <div class="code-admin-actions">
        <button type="button" class="btn btn-sm btn-light-primary" :disabled="store.loading" @click="store.fetchGroups(store.currentPage)">
          <i class="bi bi-arrow-clockwise"></i>
        </button>
        <button type="button" class="btn btn-sm btn-primary" @click="store.openGroupCreate">
          <i class="bi bi-plus-lg"></i>
          {{ t('code.group.register') }}
        </button>
      </div>
    </div>

    <div class="card post">
      <div class="card-body">
        <div class="code-admin-listbar">
          <div class="code-admin-search">
            <input
              v-model.trim="store.keyword"
              type="search"
              class="form-control form-control-solid"
              maxlength="200"
              :placeholder="t('code.group.search.placeholder')"
              @keyup.enter="store.fetchGroups(0)"
            />
            <button type="button" class="btn btn-sm btn-light-primary" :disabled="store.loading" @click="store.fetchGroups(0)">
              <i class="bi bi-search"></i>
            </button>
          </div>
          <select :value="store.pageSize" class="form-select form-select-solid code-admin-page-size" @change="onPageSizeChange">
            <option :value="10">{{ t('common.page-size.10') }}</option>
            <option :value="25">{{ t('common.page-size.25') }}</option>
            <option :value="50">{{ t('common.page-size.50') }}</option>
          </select>
        </div>

        <div v-if="store.error" class="alert alert-warning py-2">{{ store.error }}</div>
        <div v-if="store.loading" class="code-admin-loading">
          <span class="spinner-border spinner-border-sm me-2"></span>
          {{ t('common.loading') }}
        </div>

        <div v-else class="table-responsive">
          <table class="table align-middle table-row-dashed fs-small gy-4 mb-0">
            <thead>
              <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 text-muted">
                <th class="text-center hidden-table">{{ t('code.group.list.number') }}</th>
                <th>{{ t('code.group.list.code') }}</th>
                <th class="hidden-table">{{ t('board.group.list.col.description') }}</th>
                <th class="text-center hidden-table">{{ t('code.group.list.detail-count') }}</th>
                <th class="text-center">{{ t('common.use') }}</th>
                <th class="text-center code-admin-manage-col">{{ t('board.group.list.manage') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="!store.rows.length">
                <td colspan="6" class="text-center text-muted py-8">{{ t('code.group.empty') }}</td>
              </tr>
              <tr v-for="row in store.rows" :key="row.id" class="cursor-pointer" @click="openDetail(row.id)">
                <td class="text-center hidden-table text-gray-600">{{ row.rnum }}</td>
                <td>
                  <div class="code-admin-name">
                    <strong>{{ row.groupName }}</strong>
                    <span>{{ row.groupCode }}</span>
                  </div>
                </td>
                <td class="hidden-table">
                  <div class="code-admin-description">{{ row.description || "-" }}</div>
                </td>
                <td class="text-center hidden-table">{{ formatNumber(row.codeItemCnt) }}</td>
                <td class="text-center">
                  <button
                    type="button"
                    class="badge code-admin-status"
                    :class="isUse(row.useYn) ? 'badge-light-success' : 'badge-light'"
                    @click.stop="toggleGroupUse(row)"
                  >
                    <i :class="isUse(row.useYn) ? 'bi bi-check2' : 'bi bi-x-lg'"></i>
                    {{ isUse(row.useYn) ? t('status.use') : t('status.unuse') }}
                  </button>
                </td>
                <td class="text-center" @click.stop>
                  <div class="code-admin-actions justify-content-center">
                    <button type="button" class="btn btn-sm btn-icon btn-light-primary" :title="t('common.edit')" @click="openGroupEdit(row.id)">
                      <i class="bi bi-pencil-square"></i>
                    </button>
                    <button type="button" class="btn btn-sm btn-icon btn-light-danger" :title="t('common.delete')" @click="deleteGroup(row)">
                      <i class="bi bi-trash"></i>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <div class="card-footer code-admin-footer">
        <span class="text-muted fs-8">{{ t('board.group.pagination.total-format').replace('{0}', formatNumber(store.totalElements)) }}</span>
        <div v-if="pageNumbers.length" class="pagination mb-0">
          <button type="button" class="page-link" :disabled="store.currentPage <= 0" @click="store.fetchGroups(0)">
            <i class="previous"></i>
          </button>
          <button
            v-for="page in pageNumbers"
            :key="page"
            type="button"
            class="page-link"
            :class="{ active: page === store.currentPage }"
            @click="store.fetchGroups(page)"
          >
            {{ page + 1 }}
          </button>
          <button
            type="button"
            class="page-link"
            :disabled="store.currentPage >= store.totalPages - 1"
            @click="store.fetchGroups(store.totalPages - 1)"
          >
            <i class="next"></i>
          </button>
        </div>
      </div>
    </div>

    <template v-if="store.detailOpen">
      <div class="modal fade show d-block" tabindex="-1" role="dialog" aria-modal="true">
        <div class="modal-dialog modal-xl">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">{{ t('code.group.detail.title') }}</h5>
              <button type="button" class="btn-close" @click="store.closeDetail"></button>
            </div>
            <div class="modal-body">
              <div v-if="store.detailLoading" class="code-admin-loading">
                <span class="spinner-border spinner-border-sm me-2"></span>
                {{ t('common.loading') }}
              </div>
              <template v-else-if="store.detail">
                <div class="code-admin-detail-head">
                  <div>
                    <h3>{{ store.detail.groupName }}</h3>
                    <span>{{ store.detail.groupCode }}</span>
                  </div>
                  <button type="button" class="btn btn-sm btn-light-primary" @click="openGroupEdit(store.detail.id)">
                    <i class="bi bi-pencil-square"></i>
                    {{ t('code.group.detail.edit') }}
                  </button>
                </div>
                <div class="code-admin-detail-description">{{ store.detail.description || t('code.group.detail.no-description') }}</div>

                <div class="code-admin-listbar mt-6">
                  <h4 class="code-admin-section-title mb-0">{{ t('code.group.list.detail-count') }}</h4>
                  <div class="code-admin-actions">
                    <button type="button" class="btn btn-sm btn-light-primary" :disabled="store.itemSortSaving || store.items.length < 2" @click="saveItemSortOrders">
                      <span v-if="store.itemSortSaving" class="spinner-border spinner-border-sm me-1"></span>
                      <i v-else class="bi bi-save"></i>
                      {{ t('board.group.order.save') }}
                    </button>
                    <button type="button" class="btn btn-sm btn-primary" @click="store.openItemCreate">
                      <i class="bi bi-plus-lg"></i>
                      {{ t('code.group.item.register') }}
                    </button>
                  </div>
                </div>

                <div class="table-responsive">
                  <table class="table align-middle table-row-dashed fs-small gy-4 mb-0">
                    <thead>
                      <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 text-muted">
                        <th class="text-center code-admin-order-col">{{ t('board.group.list.order') }}</th>
                        <th>{{ t('code.group.item.list.code') }}</th>
                        <th>{{ t('code.group.item.list.code-name') }}</th>
                        <th class="hidden-table">{{ t('board.group.list.col.description') }}</th>
                        <th class="text-center">{{ t('common.use') }}</th>
                        <th class="text-center code-admin-manage-col">{{ t('board.group.list.manage') }}</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-if="!store.items.length">
                        <td colspan="6" class="text-center text-muted py-8">{{ t('code.group.item.empty') }}</td>
                      </tr>
                      <tr v-for="(item, index) in store.items" :key="item.id" class="cursor-pointer" @click="openItemEdit(item.id)">
                        <td class="text-center" @click.stop>
                          <div class="code-admin-order">
                            <button type="button" class="btn btn-sm btn-icon btn-light" :disabled="index === 0" :title="t('common.move-up')" @click="store.moveItem(index, -1)">
                              <i class="bi bi-chevron-up"></i>
                            </button>
                            <button type="button" class="btn btn-sm btn-icon btn-light" :disabled="index === store.items.length - 1" :title="t('common.move-down')" @click="store.moveItem(index, 1)">
                              <i class="bi bi-chevron-down"></i>
                            </button>
                          </div>
                        </td>
                        <td><strong>{{ item.code }}</strong></td>
                        <td>{{ item.codeName }}</td>
                        <td class="hidden-table">
                          <div class="code-admin-description">{{ item.description || "-" }}</div>
                        </td>
                        <td class="text-center">
                          <span :class="isUse(item.useYn) ? 'text-success' : 'text-muted'">
                            {{ isUse(item.useYn) ? t('status.use') : t('status.unuse') }}
                          </span>
                        </td>
                        <td class="text-center" @click.stop>
                          <div class="code-admin-actions justify-content-center">
                            <button type="button" class="btn btn-sm btn-icon btn-light-primary" :title="t('common.edit')" @click="openItemEdit(item.id)">
                              <i class="bi bi-pencil-square"></i>
                            </button>
                            <button type="button" class="btn btn-sm btn-icon btn-light-danger" :title="t('common.delete')" @click="deleteItem(item)">
                              <i class="bi bi-trash"></i>
                            </button>
                          </div>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </template>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-sm btn-light" @click="store.closeDetail">{{ t('common.close') }}</button>
            </div>
          </div>
        </div>
      </div>
      <div class="modal-backdrop fade show"></div>
    </template>

    <template v-if="store.groupModalOpen">
      <div class="modal fade show d-block code-admin-modal-top" tabindex="-1" role="dialog" aria-modal="true">
        <div class="modal-dialog modal-lg">
          <div class="modal-content">
            <form @submit.prevent="submitGroup">
              <div class="modal-header">
                <h5 class="modal-title">{{ store.isGroupEdit ? t('code.group.modal.title.edit') : t('code.group.modal.title.register') }}</h5>
                <button type="button" class="btn-close" @click="store.closeGroupModal"></button>
              </div>
              <div class="modal-body">
                <div class="code-admin-form">
                  <div class="code-admin-form-row">
                    <label for="groupCode" class="form-label required">{{ t('code.group.form.group-code') }}</label>
                    <div>
                      <input id="groupCode" v-model.trim="store.groupForm.groupCode" type="text" class="form-control form-control-solid" maxlength="30" :readonly="store.isGroupEdit" required />
                      <div class="text-muted fs-8 mt-1">{{ t('code.group.form.group-code-guide') }}</div>
                    </div>
                  </div>
                  <div class="code-admin-form-row">
                    <label for="groupName" class="form-label required">{{ t('code.group.form.group-name') }}</label>
                    <input id="groupName" v-model.trim="store.groupForm.groupName" type="text" class="form-control form-control-solid" maxlength="50" required />
                  </div>
                  <div class="code-admin-form-row">
                    <label for="groupDescription" class="form-label">{{ t('board.group.list.col.description') }}</label>
                    <textarea id="groupDescription" v-model.trim="store.groupForm.description" class="form-control form-control-solid" rows="3" maxlength="1000"></textarea>
                  </div>
                  <div class="code-admin-form-row">
                    <label for="groupUseYn" class="form-label">{{ t('board.group.form.use-yn') }}</label>
                    <div class="form-check form-switch form-check-custom form-check-solid">
                      <input id="groupUseYn" class="form-check-input cursor-pointer" type="checkbox" :checked="store.groupForm.useYn === 'Y'" @change="onGroupUseYnChange" />
                      <label class="form-check-label ms-3" for="groupUseYn">{{ store.groupForm.useYn === "Y" ? t('status.use') : t('status.unuse') }}</label>
                    </div>
                  </div>
                </div>
              </div>
              <div class="modal-footer">
                <button type="button" class="btn btn-sm btn-light" @click="store.closeGroupModal">{{ t('common.close') }}</button>
                <button type="submit" class="btn btn-sm btn-primary" :disabled="store.groupSaving">
                  <span v-if="store.groupSaving" class="spinner-border spinner-border-sm me-1"></span>
                  <i v-else class="bi bi-check-lg"></i>
                  {{ t('common.save') }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
      <div class="modal-backdrop fade show code-admin-backdrop-top"></div>
    </template>

    <template v-if="store.itemModalOpen">
      <div class="modal fade show d-block code-admin-modal-top" tabindex="-1" role="dialog" aria-modal="true">
        <div class="modal-dialog modal-lg">
          <div class="modal-content">
            <form @submit.prevent="submitItem">
              <div class="modal-header">
                <h5 class="modal-title">{{ store.isItemEdit ? t('code.item.modal.title.edit') : t('code.item.modal.title.register') }}</h5>
                <button type="button" class="btn-close" @click="store.closeItemModal"></button>
              </div>
              <div class="modal-body">
                <div class="code-admin-form">
                  <div class="code-admin-form-row">
                    <label for="itemCode" class="form-label required">{{ t('code.item.form.code') }}</label>
                    <div>
                      <input id="itemCode" v-model.trim="store.itemForm.code" type="text" class="form-control form-control-solid" maxlength="30" :readonly="store.isItemEdit" required />
                      <div class="text-muted fs-8 mt-1">{{ t('code.group.form.group-code-guide') }}</div>
                    </div>
                  </div>
                  <div class="code-admin-form-row">
                    <label for="itemCodeName" class="form-label required">{{ t('code.item.form.code-name') }}</label>
                    <input id="itemCodeName" v-model.trim="store.itemForm.codeName" type="text" class="form-control form-control-solid" maxlength="50" required />
                  </div>
                  <div class="code-admin-form-row">
                    <label for="itemCodeNameEn" class="form-label">{{ t('code.item.form.code-name-en') }}</label>
                    <input id="itemCodeNameEn" v-model.trim="store.itemForm.codeNameEn" type="text" class="form-control form-control-solid" maxlength="50" :placeholder="t('code.item.form.code-name-en.placeholder')" />
                  </div>
                  <div class="code-admin-form-row">
                    <label for="itemDescription" class="form-label">{{ t('board.group.list.col.description') }}</label>
                    <textarea id="itemDescription" v-model.trim="store.itemForm.description" class="form-control form-control-solid" rows="4" maxlength="1000"></textarea>
                  </div>
                  <div class="code-admin-form-row">
                    <label for="itemUseYn" class="form-label">{{ t('board.group.form.use-yn') }}</label>
                    <div class="form-check form-switch form-check-custom form-check-solid">
                      <input id="itemUseYn" class="form-check-input cursor-pointer" type="checkbox" :checked="store.itemForm.useYn === 'Y'" @change="onItemUseYnChange" />
                      <label class="form-check-label ms-3" for="itemUseYn">{{ store.itemForm.useYn === "Y" ? t('status.use') : t('status.unuse') }}</label>
                    </div>
                  </div>
                </div>
              </div>
              <div class="modal-footer">
                <button type="button" class="btn btn-sm btn-light" @click="store.closeItemModal">{{ t('common.close') }}</button>
                <button type="submit" class="btn btn-sm btn-primary" :disabled="store.itemSaving">
                  <span v-if="store.itemSaving" class="spinner-border spinner-border-sm me-1"></span>
                  <i v-else class="bi bi-check-lg"></i>
                  {{ t('common.save') }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
      <div class="modal-backdrop fade show code-admin-backdrop-top"></div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { swalConfirm, swalAlert } from "@/shared/utils/swal";
import { computed, onMounted } from "vue";
import { useCodeAdminStore, type CodeGroupRow, type CodeItemRow } from "@/features/admin/stores/codeAdmin";

const store = useCodeAdminStore();
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

function isUse(value: string | undefined): boolean {
  return String(value ?? "N").toUpperCase() === "Y";
}

function validateCode(value: string): boolean {
  return /^[A-Za-z0-9_]+$/.test(value.trim());
}

function onPageSizeChange(event: Event) {
  void store.changePageSize(Number((event.target as HTMLSelectElement).value));
}

function onGroupUseYnChange(event: Event) {
  store.groupForm.useYn = (event.target as HTMLInputElement).checked ? "Y" : "N";
}

function onItemUseYnChange(event: Event) {
  store.itemForm.useYn = (event.target as HTMLInputElement).checked ? "Y" : "N";
}

async function openDetail(id: number) {
  try {
    await store.openDetail(id);
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : t("code.group.detail.load.failure"));
  }
}

async function openGroupEdit(id: number) {
  try {
    await store.openGroupEdit(id);
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : t("code.group.load.failure"));
  }
}

async function submitGroup() {
  if (!store.groupForm.groupCode.trim() || !validateCode(store.groupForm.groupCode)) {
    void swalAlert(t("code.group.validate.code.format"));
    return;
  }
  if (!store.groupForm.groupName.trim()) {
    void swalAlert(t("code.group.validate.name.required"));
    return;
  }
  try {
    await store.submitGroup();
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : t("code.group.save.failure"));
  }
}

async function toggleGroupUse(row: CodeGroupRow) {
  if (!await swalConfirm(isUse(row.useYn) ? t("code.group.use-yn.disable.confirm") : t("code.group.use-yn.enable.confirm"))) return;
  try {
    void swalAlert(await store.toggleGroupUse(row));
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : t("board.group.use-yn.change.failure"));
  }
}

async function deleteGroup(row: CodeGroupRow) {
  if (!await swalConfirm(t("code.group.delete.confirm").replace("{groupName}", row.groupName))) return;
  try {
    await store.deleteGroup(row.id);
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : t("code.group.delete.failure"));
  }
}

async function openItemEdit(id: number) {
  try {
    await store.openItemEdit(id);
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : t("code.item.load.failure"));
  }
}

async function submitItem() {
  if (!store.itemForm.code.trim() || !validateCode(store.itemForm.code)) {
    void swalAlert(t("code.item.validate.code.format"));
    return;
  }
  if (!store.itemForm.codeName.trim()) {
    void swalAlert(t("code.item.validate.name.required"));
    return;
  }
  try {
    await store.submitItem();
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : t("code.item.save.failure"));
  }
}

async function deleteItem(item: CodeItemRow) {
  if (!await swalConfirm(t("code.item.delete.confirm").replace("{codeName}", item.codeName))) return;
  try {
    await store.deleteItem(item.id);
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : t("code.item.delete.failure"));
  }
}

async function saveItemSortOrders() {
  try {
    const message = await store.saveItemSortOrders();
    if (message) void swalAlert(message);
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : t("board.group.order.failure"));
  }
}

onMounted(async () => {
  await store.fetchGroups(0);
});
</script>

<style scoped>
.code-admin-page {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.code-admin-toolbar,
.code-admin-listbar,
.code-admin-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
}

.code-admin-actions,
.code-admin-search,
.code-admin-order {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.code-admin-toolbar {
  justify-content: flex-end;
}

.code-admin-search {
  min-width: min(420px, 100%);
}

.code-admin-search .form-control {
  min-width: 260px;
}

.code-admin-page-size {
  width: 110px;
}

.code-admin-manage-col {
  width: 104px;
}

.code-admin-order-col {
  width: 96px;
}

.code-admin-name {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.code-admin-name strong,
.code-admin-description {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.code-admin-name span,
.code-admin-detail-head span {
  color: var(--bs-gray-600);
  font-size: 0.8rem;
}

.code-admin-status {
  border: 0;
}

.code-admin-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 120px;
  color: var(--bs-gray-600);
}

.code-admin-detail-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
}

.code-admin-detail-head h3,
.code-admin-section-title {
  font-size: 1rem;
  font-weight: 700;
}

.code-admin-detail-description {
  min-height: 72px;
  margin-top: 1rem;
  padding: 0.75rem;
  border-radius: 8px;
  background: var(--bs-light);
  color: var(--bs-gray-700);
}

.code-admin-form {
  display: grid;
  gap: 1rem;
}

.code-admin-form-row {
  display: grid;
  grid-template-columns: 140px minmax(0, 1fr);
  gap: 1rem;
  align-items: start;
}

.code-admin-form-row > .form-label {
  padding-top: 0.75rem;
  font-weight: 700;
}

.code-admin-modal-top {
  z-index: 1065;
}

.code-admin-backdrop-top {
  z-index: 1060;
}

.page-link.active {
  background: var(--bs-primary);
  border-color: var(--bs-primary);
  color: #fff;
}

@media (max-width: 768px) {
  .code-admin-toolbar,
  .code-admin-listbar,
  .code-admin-footer,
  .code-admin-search {
    align-items: stretch;
    width: 100%;
  }

  .code-admin-search .form-control,
  .code-admin-page-size {
    width: 100%;
    min-width: 0;
  }

  .code-admin-form-row {
    grid-template-columns: 1fr;
  }

  .code-admin-form-row > .form-label {
    padding-top: 0;
  }
}
</style>
