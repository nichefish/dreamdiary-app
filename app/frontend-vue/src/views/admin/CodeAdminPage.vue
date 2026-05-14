<template>
  <div class="code-admin-page">
    <div class="code-admin-toolbar">
      <div>
        <h2 class="mb-1">코드 관리</h2>
        <div class="text-muted fs-7">분류 코드와 상세 코드를 관리합니다.</div>
      </div>
      <div class="code-admin-actions">
        <button type="button" class="btn btn-sm btn-light-primary" :disabled="store.loading" @click="store.fetchGroups(store.currentPage)">
          <i class="bi bi-arrow-clockwise"></i>
        </button>
        <button type="button" class="btn btn-sm btn-primary" @click="store.openGroupCreate">
          <i class="bi bi-plus-lg"></i>
          분류 코드 등록
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
              placeholder="그룹명 또는 그룹 코드 검색"
              @keyup.enter="store.fetchGroups(0)"
            />
            <button type="button" class="btn btn-sm btn-light-primary" :disabled="store.loading" @click="store.fetchGroups(0)">
              <i class="bi bi-search"></i>
            </button>
          </div>
          <select :value="store.pageSize" class="form-select form-select-solid code-admin-page-size" @change="onPageSizeChange">
            <option :value="10">10개</option>
            <option :value="25">25개</option>
            <option :value="50">50개</option>
          </select>
        </div>

        <div v-if="store.error" class="alert alert-warning py-2">{{ store.error }}</div>
        <div v-if="store.loading" class="code-admin-loading">
          <span class="spinner-border spinner-border-sm me-2"></span>
          불러오는 중
        </div>

        <div v-else class="table-responsive">
          <table class="table align-middle table-row-dashed fs-small gy-4 mb-0">
            <thead>
              <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 text-muted">
                <th class="text-center hidden-table">번호</th>
                <th>분류 코드</th>
                <th class="hidden-table">설명</th>
                <th class="text-center hidden-table">상세 코드</th>
                <th class="text-center">사용</th>
                <th class="text-center code-admin-manage-col">관리</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="!store.rows.length">
                <td colspan="6" class="text-center text-muted py-8">등록된 코드 그룹이 없습니다.</td>
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
                    {{ isUse(row.useYn) ? "사용" : "미사용" }}
                  </button>
                </td>
                <td class="text-center" @click.stop>
                  <div class="code-admin-actions justify-content-center">
                    <button type="button" class="btn btn-sm btn-icon btn-light-primary" title="수정" @click="openGroupEdit(row.id)">
                      <i class="bi bi-pencil-square"></i>
                    </button>
                    <button type="button" class="btn btn-sm btn-icon btn-light-danger" title="삭제" @click="deleteGroup(row)">
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
        <span class="text-muted fs-8">총 {{ formatNumber(store.totalElements) }}건</span>
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
              <h5 class="modal-title">분류 코드 상세</h5>
              <button type="button" class="btn-close" @click="store.closeDetail"></button>
            </div>
            <div class="modal-body">
              <div v-if="store.detailLoading" class="code-admin-loading">
                <span class="spinner-border spinner-border-sm me-2"></span>
                불러오는 중
              </div>
              <template v-else-if="store.detail">
                <div class="code-admin-detail-head">
                  <div>
                    <h3>{{ store.detail.groupName }}</h3>
                    <span>{{ store.detail.groupCode }}</span>
                  </div>
                  <button type="button" class="btn btn-sm btn-light-primary" @click="openGroupEdit(store.detail.id)">
                    <i class="bi bi-pencil-square"></i>
                    그룹 수정
                  </button>
                </div>
                <div class="code-admin-detail-description">{{ store.detail.description || "설명이 없습니다." }}</div>

                <div class="code-admin-listbar mt-6">
                  <h4 class="code-admin-section-title mb-0">상세 코드</h4>
                  <div class="code-admin-actions">
                    <button type="button" class="btn btn-sm btn-light-primary" :disabled="store.itemSortSaving || store.items.length < 2" @click="saveItemSortOrders">
                      <span v-if="store.itemSortSaving" class="spinner-border spinner-border-sm me-1"></span>
                      <i v-else class="bi bi-save"></i>
                      순서 저장
                    </button>
                    <button type="button" class="btn btn-sm btn-primary" @click="store.openItemCreate">
                      <i class="bi bi-plus-lg"></i>
                      상세 코드 등록
                    </button>
                  </div>
                </div>

                <div class="table-responsive">
                  <table class="table align-middle table-row-dashed fs-small gy-4 mb-0">
                    <thead>
                      <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 text-muted">
                        <th class="text-center code-admin-order-col">순서</th>
                        <th>코드</th>
                        <th>코드명</th>
                        <th class="hidden-table">설명</th>
                        <th class="text-center">사용</th>
                        <th class="text-center code-admin-manage-col">관리</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-if="!store.items.length">
                        <td colspan="6" class="text-center text-muted py-8">등록된 상세 코드가 없습니다.</td>
                      </tr>
                      <tr v-for="(item, index) in store.items" :key="item.id" class="cursor-pointer" @click="openItemEdit(item.id)">
                        <td class="text-center" @click.stop>
                          <div class="code-admin-order">
                            <button type="button" class="btn btn-sm btn-icon btn-light" :disabled="index === 0" title="위로" @click="store.moveItem(index, -1)">
                              <i class="bi bi-chevron-up"></i>
                            </button>
                            <button type="button" class="btn btn-sm btn-icon btn-light" :disabled="index === store.items.length - 1" title="아래로" @click="store.moveItem(index, 1)">
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
                            {{ isUse(item.useYn) ? "사용" : "미사용" }}
                          </span>
                        </td>
                        <td class="text-center" @click.stop>
                          <div class="code-admin-actions justify-content-center">
                            <button type="button" class="btn btn-sm btn-icon btn-light-primary" title="수정" @click="openItemEdit(item.id)">
                              <i class="bi bi-pencil-square"></i>
                            </button>
                            <button type="button" class="btn btn-sm btn-icon btn-light-danger" title="삭제" @click="deleteItem(item)">
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
              <button type="button" class="btn btn-sm btn-light" @click="store.closeDetail">닫기</button>
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
                <h5 class="modal-title">{{ store.isGroupEdit ? "분류 코드 수정" : "분류 코드 등록" }}</h5>
                <button type="button" class="btn-close" @click="store.closeGroupModal"></button>
              </div>
              <div class="modal-body">
                <div class="code-admin-form">
                  <div class="code-admin-form-row">
                    <label for="groupCode" class="form-label required">그룹 코드</label>
                    <div>
                      <input id="groupCode" v-model.trim="store.groupForm.groupCode" type="text" class="form-control form-control-solid" maxlength="30" :readonly="store.isGroupEdit" required />
                      <div class="text-muted fs-8 mt-1">영문, 숫자, 언더스코어만 사용할 수 있습니다.</div>
                    </div>
                  </div>
                  <div class="code-admin-form-row">
                    <label for="groupName" class="form-label required">그룹명</label>
                    <input id="groupName" v-model.trim="store.groupForm.groupName" type="text" class="form-control form-control-solid" maxlength="50" required />
                  </div>
                  <div class="code-admin-form-row">
                    <label for="groupDescription" class="form-label">설명</label>
                    <textarea id="groupDescription" v-model.trim="store.groupForm.description" class="form-control form-control-solid" rows="3" maxlength="1000"></textarea>
                  </div>
                  <div class="code-admin-form-row">
                    <label for="groupUseYn" class="form-label">사용 여부</label>
                    <div class="form-check form-switch form-check-custom form-check-solid">
                      <input id="groupUseYn" class="form-check-input cursor-pointer" type="checkbox" :checked="store.groupForm.useYn === 'Y'" @change="onGroupUseYnChange" />
                      <label class="form-check-label ms-3" for="groupUseYn">{{ store.groupForm.useYn === "Y" ? "사용" : "미사용" }}</label>
                    </div>
                  </div>
                </div>
              </div>
              <div class="modal-footer">
                <button type="button" class="btn btn-sm btn-light" @click="store.closeGroupModal">닫기</button>
                <button type="submit" class="btn btn-sm btn-primary" :disabled="store.groupSaving">
                  <span v-if="store.groupSaving" class="spinner-border spinner-border-sm me-1"></span>
                  <i v-else class="bi bi-check-lg"></i>
                  저장
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
                <h5 class="modal-title">{{ store.isItemEdit ? "상세 코드 수정" : "상세 코드 등록" }}</h5>
                <button type="button" class="btn-close" @click="store.closeItemModal"></button>
              </div>
              <div class="modal-body">
                <div class="code-admin-form">
                  <div class="code-admin-form-row">
                    <label for="itemCode" class="form-label required">코드</label>
                    <div>
                      <input id="itemCode" v-model.trim="store.itemForm.code" type="text" class="form-control form-control-solid" maxlength="30" :readonly="store.isItemEdit" required />
                      <div class="text-muted fs-8 mt-1">영문, 숫자, 언더스코어만 사용할 수 있습니다.</div>
                    </div>
                  </div>
                  <div class="code-admin-form-row">
                    <label for="itemCodeName" class="form-label required">코드명</label>
                    <input id="itemCodeName" v-model.trim="store.itemForm.codeName" type="text" class="form-control form-control-solid" maxlength="20" required />
                  </div>
                  <div class="code-admin-form-row">
                    <label for="itemDescription" class="form-label">설명</label>
                    <textarea id="itemDescription" v-model.trim="store.itemForm.description" class="form-control form-control-solid" rows="4" maxlength="1000"></textarea>
                  </div>
                  <div class="code-admin-form-row">
                    <label for="itemUseYn" class="form-label">사용 여부</label>
                    <div class="form-check form-switch form-check-custom form-check-solid">
                      <input id="itemUseYn" class="form-check-input cursor-pointer" type="checkbox" :checked="store.itemForm.useYn === 'Y'" @change="onItemUseYnChange" />
                      <label class="form-check-label ms-3" for="itemUseYn">{{ store.itemForm.useYn === "Y" ? "사용" : "미사용" }}</label>
                    </div>
                  </div>
                </div>
              </div>
              <div class="modal-footer">
                <button type="button" class="btn btn-sm btn-light" @click="store.closeItemModal">닫기</button>
                <button type="submit" class="btn btn-sm btn-primary" :disabled="store.itemSaving">
                  <span v-if="store.itemSaving" class="spinner-border spinner-border-sm me-1"></span>
                  <i v-else class="bi bi-check-lg"></i>
                  저장
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
import { computed, onMounted } from "vue";
import { useCodeAdminStore, type CodeGroupRow, type CodeItemRow } from "@/stores/codeAdmin";

const store = useCodeAdminStore();

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
    window.alert(e instanceof Error ? e.message : "코드 그룹 상세를 불러오지 못했습니다.");
  }
}

async function openGroupEdit(id: number) {
  try {
    await store.openGroupEdit(id);
  } catch (e) {
    window.alert(e instanceof Error ? e.message : "코드 그룹을 불러오지 못했습니다.");
  }
}

async function submitGroup() {
  if (!store.groupForm.groupCode.trim() || !validateCode(store.groupForm.groupCode)) {
    window.alert("그룹 코드는 영문, 숫자, 언더스코어만 사용할 수 있습니다.");
    return;
  }
  if (!store.groupForm.groupName.trim()) {
    window.alert("그룹명을 입력해주세요.");
    return;
  }
  try {
    window.alert(await store.submitGroup());
  } catch (e) {
    window.alert(e instanceof Error ? e.message : "코드 그룹을 저장하지 못했습니다.");
  }
}

async function toggleGroupUse(row: CodeGroupRow) {
  if (!window.confirm(isUse(row.useYn) ? "분류 코드를 미사용 처리할까요?" : "분류 코드를 사용 처리할까요?")) return;
  try {
    window.alert(await store.toggleGroupUse(row));
  } catch (e) {
    window.alert(e instanceof Error ? e.message : "사용 여부를 변경하지 못했습니다.");
  }
}

async function deleteGroup(row: CodeGroupRow) {
  if (!window.confirm(`${row.groupName} 분류 코드를 삭제할까요?`)) return;
  try {
    window.alert(await store.deleteGroup(row.id));
  } catch (e) {
    window.alert(e instanceof Error ? e.message : "코드 그룹을 삭제하지 못했습니다.");
  }
}

async function openItemEdit(id: number) {
  try {
    await store.openItemEdit(id);
  } catch (e) {
    window.alert(e instanceof Error ? e.message : "상세 코드를 불러오지 못했습니다.");
  }
}

async function submitItem() {
  if (!store.itemForm.code.trim() || !validateCode(store.itemForm.code)) {
    window.alert("코드는 영문, 숫자, 언더스코어만 사용할 수 있습니다.");
    return;
  }
  if (!store.itemForm.codeName.trim()) {
    window.alert("코드명을 입력해주세요.");
    return;
  }
  try {
    window.alert(await store.submitItem());
  } catch (e) {
    window.alert(e instanceof Error ? e.message : "상세 코드를 저장하지 못했습니다.");
  }
}

async function deleteItem(item: CodeItemRow) {
  if (!window.confirm(`${item.codeName} 상세 코드를 삭제할까요?`)) return;
  try {
    window.alert(await store.deleteItem(item.id));
  } catch (e) {
    window.alert(e instanceof Error ? e.message : "상세 코드를 삭제하지 못했습니다.");
  }
}

async function saveItemSortOrders() {
  try {
    const message = await store.saveItemSortOrders();
    if (message) window.alert(message);
  } catch (e) {
    window.alert(e instanceof Error ? e.message : "정렬 순서를 저장하지 못했습니다.");
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
