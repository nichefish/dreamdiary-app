<template>
  <div class="board-group-page">
    <div class="board-group-toolbar">
      <div>
        <h2 class="mb-1">게시판 그룹 관리</h2>
        <div class="text-muted fs-7">게시판 그룹과 카테고리 코드, 사용 여부, 노출 순서를 관리합니다.</div>
      </div>
      <div class="board-group-actions">
        <button type="button" class="btn btn-sm btn-light-primary" :disabled="store.loading" @click="reload">
          <i class="bi bi-arrow-clockwise"></i>
        </button>
        <button type="button" class="btn btn-sm btn-primary" @click="store.openCreate">
          <i class="bi bi-plus-lg"></i>
          등록
        </button>
      </div>
    </div>

    <div class="card post">
      <div class="card-body">
        <div class="board-group-listbar">
          <div class="board-group-search">
            <input
              v-model.trim="store.keyword"
              type="search"
              class="form-control form-control-solid"
              maxlength="200"
              placeholder="게시판명 또는 코드 검색"
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
              aria-label="페이지 크기"
              @change="onPageSizeChange"
            >
              <option :value="10">10개</option>
              <option :value="25">25개</option>
              <option :value="50">50개</option>
            </select>
            <button
              type="button"
              class="btn btn-sm btn-light-primary"
              :disabled="store.sortSaving || store.loading || store.rows.length < 2"
              @click="saveSortOrders"
            >
              <span v-if="store.sortSaving" class="spinner-border spinner-border-sm me-1"></span>
              <i v-else class="bi bi-save"></i>
              순서 저장
            </button>
          </div>
        </div>

        <div v-if="store.error" class="alert alert-warning py-2">{{ store.error }}</div>

        <div v-if="store.loading" class="board-group-loading">
          <span class="spinner-border spinner-border-sm me-2"></span>
          불러오는 중
        </div>

        <div v-else class="table-responsive">
          <table class="table align-middle table-row-dashed fs-small gy-4 mb-0">
            <thead>
              <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 text-muted">
                <th class="text-center board-group-order-col">순서</th>
                <th>게시판</th>
                <th class="text-center hidden-table">카테고리 코드</th>
                <th class="hidden-table">설명</th>
                <th class="text-center hidden-table">게시글</th>
                <th class="text-center">사용</th>
                <th class="text-center board-group-manage-col">관리</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="!store.rows.length">
                <td colspan="7" class="text-center text-muted py-8">등록된 게시판 그룹이 없습니다.</td>
              </tr>
              <tr v-for="(row, index) in store.rows" :key="row.id">
                <td class="text-center">
                  <div class="board-group-order">
                    <button
                      type="button"
                      class="btn btn-sm btn-icon btn-light"
                      :disabled="index === 0"
                      title="위로"
                      @click="store.moveRow(index, -1)"
                    >
                      <i class="bi bi-chevron-up"></i>
                    </button>
                    <button
                      type="button"
                      class="btn btn-sm btn-icon btn-light"
                      :disabled="index === store.rows.length - 1"
                      title="아래로"
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
                    {{ isUse(row) ? "사용" : "미사용" }}
                  </button>
                </td>
                <td class="text-center">
                  <div class="board-group-row-actions">
                    <button type="button" class="btn btn-sm btn-icon btn-light-primary" title="수정" @click="store.openEdit(row.id)">
                      <i class="bi bi-pencil-square"></i>
                    </button>
                    <button type="button" class="btn btn-sm btn-icon btn-light-danger" title="삭제" @click="deleteBoard(row)">
                      <i class="bi bi-trash"></i>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="card-footer board-group-footer">
        <span class="text-muted fs-8">총 {{ formatNumber(store.totalElements) }}건</span>
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
                <h5 class="modal-title">{{ store.isEdit ? "게시판 그룹 수정" : "게시판 그룹 등록" }}</h5>
                <button type="button" class="btn-close" @click="store.closeModal"></button>
              </div>
              <div class="modal-body">
                <div v-if="store.detailLoading" class="board-group-loading">
                  <span class="spinner-border spinner-border-sm me-2"></span>
                  불러오는 중
                </div>
                <div v-else class="board-group-form">
                  <div class="board-group-form-row">
                    <label for="boardKey" class="form-label required">게시판 코드</label>
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
                      <div class="text-muted fs-8 mt-1">영문, 숫자, 하이픈, 언더스코어만 사용할 수 있습니다.</div>
                    </div>
                  </div>
                  <div class="board-group-form-row">
                    <label for="boardName" class="form-label required">게시판명</label>
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
                    <label for="categoryGroupCode" class="form-label">카테고리 코드</label>
                    <input
                      id="categoryGroupCode"
                      v-model.trim="store.form.categoryGroupCode"
                      type="text"
                      class="form-control form-control-solid"
                      maxlength="30"
                    />
                  </div>
                  <div class="board-group-form-row">
                    <label for="description" class="form-label">설명</label>
                    <textarea
                      id="description"
                      v-model.trim="store.form.description"
                      class="form-control form-control-solid"
                      rows="4"
                      maxlength="2000"
                    ></textarea>
                  </div>
                  <div class="board-group-form-row">
                    <label for="useYn" class="form-label">사용 여부</label>
                    <div class="form-check form-switch form-check-custom form-check-solid">
                      <input
                        id="useYn"
                        class="form-check-input cursor-pointer"
                        type="checkbox"
                        :checked="store.form.useYn === 'Y'"
                        @change="onUseYnChange"
                      />
                      <label class="form-check-label ms-3" for="useYn">{{ store.form.useYn === "Y" ? "사용" : "미사용" }}</label>
                    </div>
                  </div>
                </div>
              </div>
              <div class="modal-footer">
                <button type="button" class="btn btn-sm btn-light" @click="store.closeModal">닫기</button>
                <button type="submit" class="btn btn-sm btn-primary" :disabled="store.saving || store.detailLoading">
                  <span v-if="store.saving" class="spinner-border spinner-border-sm me-1"></span>
                  <i v-else class="bi bi-check-lg"></i>
                  저장
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
import { swalConfirm, swalAlert } from "@/utils/swal";
import { computed, onMounted } from "vue";
import { useBoardGroupStore, type BoardGroupRow } from "@/stores/boardGroup";

const store = useBoardGroupStore();

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
    void swalAlert("게시판 코드를 입력해주세요.");
    return false;
  }
  if (!/^[A-Za-z0-9_-]+$/.test(store.form.boardKey.trim())) {
    void swalAlert("게시판 코드는 영문, 숫자, 하이픈, 언더스코어만 사용할 수 있습니다.");
    return false;
  }
  if (!store.form.boardName.trim()) {
    void swalAlert("게시판명을 입력해주세요.");
    return false;
  }
  return true;
}

async function reload() {
  await store.fetchList(store.currentPage);
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
    void swalAlert(e instanceof Error ? e.message : "게시판 그룹을 저장하지 못했습니다.");
  }
}

async function toggleUse(row: BoardGroupRow) {
  const message = isUse(row) ? "게시판 그룹을 미사용 처리할까요?" : "게시판 그룹을 사용 처리할까요?";
  if (!await swalConfirm(message)) return;
  try {
    void swalAlert(await store.toggleUse(row));
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : "사용 여부를 변경하지 못했습니다.");
  }
}

async function deleteBoard(row: BoardGroupRow) {
  if (!await swalConfirm(`${row.boardName} 게시판 그룹을 삭제할까요?`)) return;
  try {
    await store.deleteBoard(row.id);
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : "게시판 그룹을 삭제하지 못했습니다.");
  }
}

async function saveSortOrders() {
  try {
    void swalAlert(await store.saveSortOrders());
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : "정렬 순서를 저장하지 못했습니다.");
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

.board-group-toolbar,
.board-group-listbar,
.board-group-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
}

.board-group-actions,
.board-group-search,
.board-group-list-actions,
.board-group-row-actions,
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
  .board-group-toolbar,
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
