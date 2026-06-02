<template>
  <div class="menu-admin-page">
    <div class="menu-admin-toolbar">
      <div>
        <h2 class="mb-1">메뉴 관리</h2>
        <div class="text-muted fs-7">사이드바와 관리자 메뉴 트리를 관리합니다.</div>
      </div>
      <div class="menu-admin-actions">
        <button type="button" class="btn btn-sm btn-light-primary" :disabled="store.loading" @click="store.fetchTree">
          <i class="bi bi-arrow-clockwise"></i>
        </button>
        <button type="button" class="btn btn-sm btn-primary" @click="store.openMainCreate">
          <i class="bi bi-plus-lg"></i>
          메인 메뉴 등록
        </button>
      </div>
    </div>

    <div class="menu-admin-board">
      <section class="menu-admin-column">
        <div class="menu-admin-column-header">
          <div>
            <div class="menu-admin-column-title">
              <i class="bi bi-people-fill"></i>
              사용자 화면
            </div>
            <div class="text-muted fs-8">일반 사용자에게 노출되는 메뉴</div>
          </div>
          <span class="badge badge-light-primary">{{ userMenuRows.length }}</span>
        </div>

        <div v-if="store.error" class="alert alert-warning py-2">{{ store.error }}</div>
        <div v-if="store.loading" class="menu-admin-loading">
          <span class="spinner-border spinner-border-sm me-2"></span>
          불러오는 중
        </div>
        <ol v-else class="menu-admin-tree">
          <li v-if="!userMenuRows.length" class="menu-admin-empty">등록된 사용자 화면 메뉴가 없습니다.</li>
          <MenuAdminTreeNode
            v-for="(row, index) in userMenuRows"
            :key="row.id"
            :node="row"
            :index="index"
            :sibling-count="userMenuRows.length"
            :sort-saving="store.sortSaving"
            @add-child="store.openSubCreate"
            @edit="openEdit"
            @toggle-use="toggleUse"
            @delete-node="deleteMenu"
            @drag-start="(idx) => onMainDragStart('N', idx)"
            @drop-node="(idx) => onMainDrop('N', idx)"
            @child-drag-start="onChildDragStart"
            @child-drop="onChildDrop"
          />
        </ol>
      </section>

      <section class="menu-admin-column">
        <div class="menu-admin-column-header">
          <div>
            <div class="menu-admin-column-title">
              <i class="bi bi-person-gear"></i>
              관리자 화면
            </div>
            <div class="text-muted fs-8">관리자에게 노출되는 운영 메뉴</div>
          </div>
          <span class="badge badge-light-info">{{ adminMenuRows.length }}</span>
        </div>

        <div v-if="store.error" class="alert alert-warning py-2">{{ store.error }}</div>
        <div v-if="store.loading" class="menu-admin-loading">
          <span class="spinner-border spinner-border-sm me-2"></span>
          불러오는 중
        </div>
        <ol v-else class="menu-admin-tree">
          <li v-if="!adminMenuRows.length" class="menu-admin-empty">등록된 관리자 화면 메뉴가 없습니다.</li>
          <MenuAdminTreeNode
            v-for="(row, index) in adminMenuRows"
            :key="row.id"
            :node="row"
            :index="index"
            :sibling-count="adminMenuRows.length"
            :sort-saving="store.sortSaving"
            @add-child="store.openSubCreate"
            @edit="openEdit"
            @toggle-use="toggleUse"
            @delete-node="deleteMenu"
            @drag-start="(idx) => onMainDragStart('Y', idx)"
            @drop-node="(idx) => onMainDrop('Y', idx)"
            @child-drag-start="onChildDragStart"
            @child-drop="onChildDrop"
          />
        </ol>
      </section>
    </div>

    <template v-if="store.modalOpen">
      <div class="modal fade show d-block" tabindex="-1" role="dialog" aria-modal="true">
        <div class="modal-dialog modal-xl">
          <div class="modal-content">
            <form @submit.prevent="submit">
              <div class="modal-header">
                <h5 class="modal-title">{{ store.isEdit ? "메뉴 수정" : "메뉴 등록" }}</h5>
                <button type="button" class="btn-close" @click="store.closeModal"></button>
              </div>
              <div class="modal-body">
                <div class="menu-admin-form">
                  <div class="menu-admin-form-row">
                    <label class="form-label">메뉴 유형</label>
                    <div class="menu-admin-type">
                      <span class="badge" :class="store.form.menuType === 'MAIN' ? 'badge-light-primary' : 'badge-light-info'">
                        {{ store.form.menuType === "MAIN" ? "메인 메뉴" : "하위 메뉴" }}
                      </span>
                      <span v-if="store.form.upperMenuNm" class="text-muted">{{ store.form.upperMenuNm }}</span>
                    </div>
                  </div>

                  <div v-if="store.isMainForm" class="menu-admin-form-row">
                    <label for="adminYn" class="form-label">관리자 메뉴</label>
                    <div class="form-check form-switch form-check-custom form-check-solid">
                      <input id="adminYn" class="form-check-input cursor-pointer" type="checkbox" :checked="store.form.adminYn === 'Y'" @change="onAdminYnChange" />
                      <label class="form-check-label ms-3" for="adminYn">{{ store.form.adminYn === "Y" ? "관리자" : "사용자" }}</label>
                    </div>
                  </div>

                  <div class="menu-admin-form-row">
                    <label for="useYn" class="form-label">사용 여부</label>
                    <div class="form-check form-switch form-check-custom form-check-solid">
                      <input id="useYn" class="form-check-input cursor-pointer" type="checkbox" :checked="store.form.useYn === 'Y'" @change="onUseYnChange" />
                      <label class="form-check-label ms-3" for="useYn">{{ store.form.useYn === "Y" ? "사용" : "미사용" }}</label>
                    </div>
                  </div>

                  <div v-if="!store.isMainForm" class="menu-admin-form-row">
                    <label for="parentMenuId" class="form-label required">상위 메뉴</label>
                    <select id="parentMenuId" v-model.number="store.form.parentMenuId" class="form-select form-select-solid" required>
                      <option v-for="opt in store.parentOptions" :key="opt.id" :value="opt.id">
                        {{ `${"--".repeat(opt.depth)} ${opt.label}` }}
                      </option>
                    </select>
                  </div>

                  <div class="menu-admin-form-row">
                    <label for="menuName" class="form-label required">메뉴명</label>
                    <input id="menuName" v-model.trim="store.form.menuName" type="text" class="form-control form-control-solid" maxlength="200" required />
                  </div>

                  <div class="menu-admin-form-row">
                    <label for="menuLabel" class="form-label">메뉴 라벨</label>
                    <input id="menuLabel" v-model.trim="store.form.menuLabel" type="text" class="form-control form-control-solid" maxlength="100" />
                  </div>

                  <div class="menu-admin-form-row">
                    <label for="submenuExpandType" class="form-label required">하위메뉴 표시</label>
                    <select id="submenuExpandType" v-model="store.form.submenuExpandType" class="form-select form-select-solid" required>
                      <option v-for="opt in store.submenuExpandOptions" :key="opt.code" :value="opt.code">{{ opt.codeName }}</option>
                    </select>
                  </div>

                  <div v-if="store.form.submenuExpandType === 'NO_SUB'" class="menu-admin-form-row">
                    <label for="url" class="form-label">URL</label>
                    <input id="url" v-model.trim="store.form.url" type="text" class="form-control form-control-solid" maxlength="1000" />
                  </div>

                  <div class="menu-admin-form-row">
                    <label for="unreadCntNm" class="form-label">미열람 카운트</label>
                    <input id="unreadCntNm" v-model.trim="store.form.unreadCntNm" type="text" class="form-control form-control-solid" maxlength="100" />
                  </div>

                  <div class="menu-admin-form-row">
                    <label for="icon" class="form-label">아이콘</label>
                    <div class="menu-admin-icon-editor">
                      <div class="menu-admin-icon-preview" v-html="store.form.icon || '<i class=&quot;bi bi-app&quot;></i>'"></div>
                      <textarea id="icon" v-model.trim="store.form.icon" class="form-control form-control-solid" rows="4" maxlength="1000"></textarea>
                    </div>
                  </div>
                </div>
              </div>
              <div class="modal-footer">
                <button type="submit" class="btn btn-sm btn-primary" :disabled="store.saving">
                  <span v-if="store.saving" class="spinner-border spinner-border-sm me-1"></span>
                  <i v-else class="bi bi-check-lg"></i>
                  저장
                </button>
                <button type="button" class="btn btn-sm btn-light" @click="store.closeModal">닫기</button>
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
import { computed, onMounted, ref } from "vue";
import MenuAdminTreeNode from "@/views/admin/MenuAdminTreeNode.vue";
import { useMenuAdminStore, type MenuNode } from "@/stores/menuAdmin";

const store = useMenuAdminStore();
const userMenuRows = computed(() => store.rows.filter((row) => String(row.adminYn ?? "N").toUpperCase() !== "Y"));
const adminMenuRows = computed(() => store.rows.filter((row) => String(row.adminYn ?? "N").toUpperCase() === "Y"));
const mainDrag = ref<{ adminYn: "Y" | "N"; index: number } | null>(null);
const childDrag = ref<{ parentId: number; index: number } | null>(null);

async function openEdit(id: number) {
  try {
    await store.openEdit(id);
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : "메뉴 상세를 불러오지 못했습니다.");
  }
}

async function submit() {
  if (!store.form.menuName.trim()) {
    void swalAlert("메뉴명을 입력해주세요.");
    return;
  }
  if (!store.form.submenuExpandType) {
    void swalAlert("하위메뉴 표시 방식을 선택해주세요.");
    return;
  }
  if (store.form.menuType === "SUB" && store.form.parentMenuId == null) {
    void swalAlert("상위 메뉴를 선택해주세요.");
    return;
  }
  try {
    await store.submit();
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : "메뉴를 저장하지 못했습니다.");
  }
}

function onAdminYnChange(event: Event) {
  store.form.adminYn = (event.target as HTMLInputElement).checked ? "Y" : "N";
}

function onUseYnChange(event: Event) {
  store.form.useYn = (event.target as HTMLInputElement).checked ? "Y" : "N";
}

async function toggleUse(row: MenuNode) {
  if (!await swalConfirm(`${row.menuName ?? "메뉴"} 사용 여부를 변경할까요?`)) return;
  try {
    void swalAlert(await store.toggleUse(row));
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : "메뉴 상태를 변경하지 못했습니다.");
  }
}

async function deleteMenu(row: MenuNode) {
  if (!await swalConfirm(`${row.menuName ?? "메뉴"}를 삭제할까요? 하위 메뉴도 함께 삭제됩니다.`)) return;
  try {
    await store.deleteMenu(row.id);
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : "메뉴를 삭제하지 못했습니다.");
  }
}

async function onMainDrop(adminYn: "Y" | "N", targetIndex: number) {
  const drag = mainDrag.value;
  mainDrag.value = null;
  if (!drag || drag.adminYn !== adminYn) return;
  try {
    const message = await store.reorderMainWithinGroup(adminYn, drag.index, targetIndex);
    if (message) void swalAlert(message);
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : "메인 메뉴 순서를 저장하지 못했습니다.");
    await store.fetchTree();
  }
}

function onMainDragStart(adminYn: "Y" | "N", index: number) {
  mainDrag.value = { adminYn, index };
  childDrag.value = null;
}

async function onChildDrop(parent: MenuNode, targetIndex: number) {
  const drag = childDrag.value;
  childDrag.value = null;
  if (!drag || drag.parentId !== parent.id) return;
  try {
    const message = await store.reorderSub(parent, drag.index, targetIndex);
    if (message) void swalAlert(message);
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : "하위 메뉴 순서를 저장하지 못했습니다.");
    await store.fetchTree();
  }
}

function onChildDragStart(parent: MenuNode, index: number) {
  childDrag.value = { parentId: parent.id, index };
  mainDrag.value = null;
}

onMounted(async () => {
  await store.fetchTree();
});
</script>

<style scoped>
.menu-admin-page {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.menu-admin-toolbar,
.menu-admin-actions,
.menu-admin-type {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.menu-admin-toolbar {
  justify-content: space-between;
  flex-wrap: wrap;
}

.menu-admin-actions {
  gap: 0.5rem;
  flex-wrap: wrap;
}

.menu-admin-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 160px;
  color: var(--bs-gray-600);
}

.menu-admin-board {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1rem;
  align-items: start;
}

.menu-admin-column {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  min-width: 0;
  padding: 1rem;
  border: 1px solid var(--bs-gray-200);
  border-radius: 8px;
  background: var(--bs-gray-100);
}

.menu-admin-column-header,
.menu-admin-column-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.menu-admin-column-header {
  justify-content: space-between;
  min-height: 36px;
  padding: 0 0.25rem;
}

.menu-admin-column-title {
  color: var(--bs-gray-900);
  font-size: 1rem;
  font-weight: 800;
}

.menu-admin-column-title > i {
  color: var(--bs-primary);
}

.menu-admin-empty {
  padding: 1.5rem 1rem;
  border: 1px dashed var(--bs-gray-300);
  border-radius: 8px;
  color: var(--bs-gray-600);
  text-align: center;
}

.menu-admin-tree {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 0.35rem;
}

.menu-admin-form {
  display: grid;
  gap: 1rem;
}

.menu-admin-form-row {
  display: grid;
  grid-template-columns: 150px minmax(0, 1fr);
  gap: 1rem;
  align-items: start;
}

.menu-admin-form-row > .form-label {
  padding-top: 0.75rem;
  font-weight: 700;
}

.menu-admin-icon-editor {
  display: grid;
  grid-template-columns: 64px minmax(0, 1fr);
  gap: 0.75rem;
}

.menu-admin-icon-preview {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border-radius: 8px;
  background: var(--bs-light);
  overflow: hidden;
}

@media (max-width: 768px) {
  .menu-admin-board {
    grid-template-columns: 1fr;
  }

  .menu-admin-toolbar {
    align-items: stretch;
    width: 100%;
  }

  .menu-admin-form-row,
  .menu-admin-icon-editor {
    grid-template-columns: 1fr;
  }

  .menu-admin-form-row > .form-label {
    padding-top: 0;
  }
}
</style>
