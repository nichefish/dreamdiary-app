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

    <div class="card post">
      <div class="card-body">
        <div v-if="store.error" class="alert alert-warning py-2">{{ store.error }}</div>
        <div v-if="store.loading" class="menu-admin-loading">
          <span class="spinner-border spinner-border-sm me-2"></span>
          불러오는 중
        </div>
        <ol v-else class="menu-admin-tree">
          <li v-if="!store.rows.length" class="text-center text-muted py-8">등록된 메뉴가 없습니다.</li>
          <MenuAdminTreeNode
            v-for="(row, index) in store.rows"
            :key="row.id"
            :node="row"
            :index="index"
            :sibling-count="store.rows.length"
            :sort-saving="store.sortSaving"
            @add-child="store.openSubCreate"
            @edit="openEdit"
            @toggle-use="toggleUse"
            @delete-node="deleteMenu"
            @move="moveMain"
            @move-child="moveSub"
          />
        </ol>
      </div>
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
                <button type="button" class="btn btn-sm btn-light" @click="store.closeModal">닫기</button>
                <button type="submit" class="btn btn-sm btn-primary" :disabled="store.saving">
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
import { onMounted } from "vue";
import MenuAdminTreeNode from "@/views/admin/MenuAdminTreeNode.vue";
import { useMenuAdminStore, type MenuNode } from "@/stores/menuAdmin";

const store = useMenuAdminStore();

async function openEdit(id: number) {
  try {
    await store.openEdit(id);
  } catch (e) {
    window.alert(e instanceof Error ? e.message : "메뉴 상세를 불러오지 못했습니다.");
  }
}

async function submit() {
  if (!store.form.menuName.trim()) {
    window.alert("메뉴명을 입력해주세요.");
    return;
  }
  if (!store.form.submenuExpandType) {
    window.alert("하위메뉴 표시 방식을 선택해주세요.");
    return;
  }
  if (store.form.menuType === "SUB" && store.form.parentMenuId == null) {
    window.alert("상위 메뉴를 선택해주세요.");
    return;
  }
  try {
    window.alert(await store.submit());
  } catch (e) {
    window.alert(e instanceof Error ? e.message : "메뉴를 저장하지 못했습니다.");
  }
}

function onAdminYnChange(event: Event) {
  store.form.adminYn = (event.target as HTMLInputElement).checked ? "Y" : "N";
}

function onUseYnChange(event: Event) {
  store.form.useYn = (event.target as HTMLInputElement).checked ? "Y" : "N";
}

async function toggleUse(row: MenuNode) {
  if (!window.confirm(`${row.menuName ?? "메뉴"} 사용 여부를 변경할까요?`)) return;
  try {
    window.alert(await store.toggleUse(row));
  } catch (e) {
    window.alert(e instanceof Error ? e.message : "메뉴 상태를 변경하지 못했습니다.");
  }
}

async function deleteMenu(row: MenuNode) {
  if (!window.confirm(`${row.menuName ?? "메뉴"}를 삭제할까요? 하위 메뉴도 함께 삭제됩니다.`)) return;
  try {
    window.alert(await store.deleteMenu(row.id));
  } catch (e) {
    window.alert(e instanceof Error ? e.message : "메뉴를 삭제하지 못했습니다.");
  }
}

async function moveMain(index: number, delta: -1 | 1) {
  try {
    const message = await store.moveMain(index, delta);
    if (message) window.alert(message);
  } catch (e) {
    window.alert(e instanceof Error ? e.message : "메인 메뉴 순서를 저장하지 못했습니다.");
    await store.fetchTree();
  }
}

async function moveSub(parent: MenuNode, index: number, delta: -1 | 1) {
  try {
    const message = await store.moveSub(parent, index, delta);
    if (message) window.alert(message);
  } catch (e) {
    window.alert(e instanceof Error ? e.message : "하위 메뉴 순서를 저장하지 못했습니다.");
    await store.fetchTree();
  }
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

.menu-admin-tree {
  margin: 0;
  padding: 0;
  list-style: none;
}

.menu-admin-tree {
  display: grid;
  gap: 1rem;
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
