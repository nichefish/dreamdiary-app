<template>
  <div class="menu-admin-page">
    <div class="menu-admin-toolbar">
      <div class="menu-admin-actions">
        <button type="button" class="btn btn-sm btn-light-primary" :disabled="store.loading" @click="store.fetchTree">
          <i class="bi bi-arrow-clockwise"></i>
        </button>
      </div>
    </div>

    <div class="menu-admin-board">
      <section class="menu-admin-column">
        <div class="menu-admin-column-header">
          <div>
            <div class="menu-admin-column-title">
              <i class="bi bi-people-fill"></i>
              {{ t('menu.tab.user') }}
            </div>
            <div class="text-muted fs-8">{{ t('menu.tab.user.desc') }}</div>
          </div>
          <span class="badge badge-light-primary">{{ userMenuRows.length }}</span>
        </div>

        <div v-if="store.error" class="alert alert-warning py-2">{{ store.error }}</div>
        <div v-if="store.loading" class="menu-admin-loading">
          <span class="spinner-border spinner-border-sm me-2"></span>
          {{ t('common.loading') }}
        </div>
        <ol v-else class="menu-admin-tree">
          <li v-if="!userMenuRows.length" class="menu-admin-empty">{{ t('menu.tab.user.empty') }}</li>
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
            @drag-start="(idx) => onMainDragStart('USER', idx)"
            @drop-node="(idx) => onMainDrop('USER', idx)"
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
              {{ t('menu.tab.admin') }}
            </div>
            <div class="text-muted fs-8">{{ t('menu.tab.admin.desc') }}</div>
          </div>
          <span class="badge badge-light-info">{{ adminMenuRows.length }}</span>
        </div>

        <div v-if="store.error" class="alert alert-warning py-2">{{ store.error }}</div>
        <div v-if="store.loading" class="menu-admin-loading">
          <span class="spinner-border spinner-border-sm me-2"></span>
          {{ t('common.loading') }}
        </div>
        <ol v-else class="menu-admin-tree">
          <li v-if="!adminMenuRows.length" class="menu-admin-empty">{{ t('menu.tab.admin.empty') }}</li>
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
            @drag-start="(idx) => onMainDragStart('MNGR', idx)"
            @drop-node="(idx) => onMainDrop('MNGR', idx)"
            @child-drag-start="onChildDragStart"
            @child-drop="onChildDrop"
          />
        </ol>
      </section>

      <section class="menu-admin-column menu-admin-column--system">
        <div class="menu-admin-column-header">
          <div>
            <div class="menu-admin-column-title">
              <i class="bi bi-eye-slash"></i>
              {{ t('menu.tab.hidden') }}
            </div>
            <div class="text-muted fs-8">{{ t('menu.tab.hidden.desc') }}</div>
          </div>
          <span class="badge badge-light-secondary">{{ hiddenMenuRows.length }}</span>
        </div>

        <div v-if="store.error" class="alert alert-warning py-2">{{ store.error }}</div>
        <div v-if="store.loading" class="menu-admin-loading">
          <span class="spinner-border spinner-border-sm me-2"></span>
          {{ t('common.loading') }}
        </div>
        <ol v-else class="menu-admin-tree">
          <li v-if="!hiddenMenuRows.length" class="menu-admin-empty">{{ t('menu.tab.hidden.empty') }}</li>
          <MenuAdminTreeNode
            v-for="(row, index) in hiddenMenuRows"
            :key="row.id"
            :node="row"
            :index="index"
            :sibling-count="hiddenMenuRows.length"
            :sort-saving="store.sortSaving"
            @add-child="store.openSubCreate"
            @edit="openEdit"
            @toggle-use="toggleUse"
            @delete-node="deleteMenu"
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
                <h5 class="modal-title">{{ store.isEdit ? t('menu.modal.title.edit') : t('menu.modal.title.register') }}</h5>
                <button type="button" class="btn-close" @click="store.closeModal"></button>
              </div>
              <div class="modal-body">
                <div class="menu-admin-form">
                  <div class="menu-admin-form-row">
                    <label for="useYn" class="form-label">{{ t('board.group.form.use-yn') }}</label>
                    <div class="form-check form-switch form-check-custom form-check-solid">
                      <input id="useYn" class="form-check-input cursor-pointer" type="checkbox" :checked="store.form.useYn === 'Y'" @change="onUseYnChange" />
                      <label class="form-check-label ms-3" for="useYn">{{ store.form.useYn === "Y" ? t('status.use') : t('status.unuse') }}</label>
                    </div>
                  </div>

                  <div class="menu-admin-form-row">
                    <label for="sidebarVisibleYn" class="form-label">{{ t('menu.form.sidebar-visible') }}</label>
                    <div class="form-check form-switch form-check-custom form-check-solid">
                      <input id="sidebarVisibleYn" class="form-check-input cursor-pointer" type="checkbox" :checked="store.form.sidebarVisibleYn === 'Y'" @change="onSidebarVisibleYnChange" />
                      <label class="form-check-label ms-3" for="sidebarVisibleYn">{{ store.form.sidebarVisibleYn === "Y" ? t('menu.form.sidebar-visible.show') : t('menu.form.sidebar-visible.hide') }}</label>
                    </div>
                  </div>

                  <div class="menu-admin-form-row">
                    <label class="form-label required">{{ t('menu.form.parent') }}</label>
                    <div class="form-control form-control-solid menu-admin-readonly-field">
                      {{ store.form.upperMenuNm || "-" }}
                    </div>
                  </div>

                  <div class="menu-admin-form-row">
                    <label class="form-label">{{ t('menu.form.basic-info') }}</label>
                    <div class="menu-admin-form-pair">
                      <div>
                        <label for="menuName" class="form-label required">{{ t('menu.form.name') }}</label>
                        <input id="menuName" v-model.trim="store.form.menuName" type="text" class="form-control form-control-solid" maxlength="200" required />
                      </div>
                      <div>
                        <label for="menuLabel" class="form-label required">{{ t('menu.form.label') }}</label>
                        <input id="menuLabel" v-model.trim="store.form.menuLabel" type="text" class="form-control form-control-solid" maxlength="100" required />
                      </div>
                    </div>
                  </div>

                  <div class="menu-admin-form-row">
                    <label for="menuDescription" class="form-label">{{ t('menu.form.description') }}</label>
                    <textarea
                      id="menuDescription"
                      v-model.trim="store.form.menuDescription"
                      class="form-control form-control-solid"
                      rows="3"
                      maxlength="1000"
                    ></textarea>
                  </div>

                  <!--begin::다국어 번역 (로케일 + 메뉴명 + 설명 행, + 로 추가)
                    한국어는 위 메뉴명/설명 필드가 단일 원천이라 이 목록에 포함하지 않는다.
                    선택지는 지원 로케일에서 기준 로케일(ko)을 뺀 것이며 SUPPORTED_LOCALES 가 늘면 자동 반영된다. -->
                  <div class="menu-admin-form-row">
                    <label class="form-label">{{ t('menu.form.i18n') }}</label>
                    <div>
                      <div v-for="(row, idx) in store.form.i18nRows" :key="idx" class="menu-admin-i18n-row">
                        <select v-model="row.locale" class="form-select form-select-solid menu-admin-i18n-locale">
                          <option v-for="opt in localeOptions(idx)" :key="opt" :value="opt">{{ opt }}</option>
                        </select>
                        <div class="menu-admin-i18n-fields">
                          <input
                            v-model.trim="row.menuName"
                            type="text"
                            class="form-control form-control-solid"
                            maxlength="200"
                            :placeholder="t('menu.form.i18n.name.placeholder')"
                          />
                          <input
                            v-model.trim="row.menuDescription"
                            type="text"
                            class="form-control form-control-solid"
                            maxlength="1000"
                            :placeholder="t('menu.form.i18n.description.placeholder')"
                          />
                        </div>
                        <button
                          type="button"
                          class="btn btn-sm btn-icon btn-light-danger"
                          :title="t('common.delete')"
                          @click="store.removeI18nRow(idx)"
                        >
                          <i class="bi bi-dash-lg"></i>
                        </button>
                      </div>
                      <button
                        type="button"
                        class="btn btn-sm btn-light-primary mt-1"
                        :disabled="!canAddI18nRow"
                        @click="store.addI18nRow()"
                      >
                        <i class="bi bi-plus-lg"></i>
                        {{ t('menu.form.i18n.add') }}
                      </button>
                      <div class="text-muted fs-8 mt-1">{{ t('menu.form.i18n.guide') }}</div>
                    </div>
                  </div>
                  <!--end::다국어 번역-->

                  <div class="menu-admin-form-row">
                    <label for="submenuExpandType" class="form-label required">{{ t('menu.form.submenu-expand') }}</label>
                    <select id="submenuExpandType" v-model="store.form.submenuExpandType" class="form-select form-select-solid" required>
                      <option v-for="opt in store.submenuExpandOptions" :key="opt.code" :value="opt.code">{{ opt.codeName }}</option>
                    </select>
                  </div>

                  <div v-if="store.form.submenuExpandType === 'NO_SUB'" class="menu-admin-form-row">
                    <label for="url" class="form-label required">{{ t('common.technical.url') }}</label>
                    <input id="url" v-model.trim="store.form.url" type="text" class="form-control form-control-solid" maxlength="1000" required />
                  </div>

                  <div class="menu-admin-form-row">
                    <label for="unreadCntNm" class="form-label">{{ t('menu.form.unread-count') }}</label>
                    <div class="menu-admin-unread-row">
                      <div class="form-check form-switch form-check-custom form-check-solid">
                        <input id="unreadCntEnabled" v-model="store.form.unreadCntEnabled" class="form-check-input cursor-pointer" type="checkbox" @change="onUnreadCntEnabledChange" />
                        <label class="form-check-label ms-3" for="unreadCntEnabled">{{ store.form.unreadCntEnabled ? t('status.use') : t('status.unuse') }}</label>
                      </div>
                      <input
                        v-if="store.form.unreadCntEnabled"
                        id="unreadCntNm"
                        v-model.trim="store.form.unreadCntNm"
                        type="text"
                        class="form-control form-control-solid"
                        maxlength="100"
                        required
                      />
                    </div>
                  </div>

                  <div class="menu-admin-form-row">
                    <label for="icon" class="form-label">{{ t('menu.form.icon') }}</label>
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
                  {{ t('common.save') }}
                </button>
                <button type="button" class="btn btn-sm btn-light" @click="store.closeModal">{{ t('common.close') }}</button>
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
import { computed, onMounted, ref, watch } from "vue";
import MenuAdminTreeNode from "@/features/admin/MenuAdminTreeNode.vue";
import { MENU_I18N_LOCALE_OPTIONS, useMenuAdminStore, type MenuNode, type MenuTargetMode } from "@/features/admin/stores/menuAdmin";

const store = useMenuAdminStore();

/**
 * idx 번째 행의 로케일 select 선택지.
 * 다른 행이 이미 쓰는 로케일은 제외한다 (locale 은 menu_i18n 복합 PK 라 중복 불가).
 * 자기 자신의 현재 값은 남겨야 select 가 값을 잃지 않는다.
 */
function localeOptions(idx: number): readonly string[] {
  const used = new Set(store.form.i18nRows.filter((_, i) => i !== idx).map((row) => row.locale));
  return MENU_I18N_LOCALE_OPTIONS.filter((locale) => !used.has(locale));
}

/** 아직 쓰지 않은 로케일이 남아 있을 때만 행 추가 가능 */
const canAddI18nRow = computed(() => store.form.i18nRows.length < MENU_I18N_LOCALE_OPTIONS.length);
const localeStore = useLocaleStore();
const { t } = localeStore;
const userMenuRows = computed(() => store.rows
  .filter((row) => sidebarVisible(row) && store.getMenuTargetMode(row) === "USER")
  .map((row) => withVisibleChildren(row)));
const adminMenuRows = computed(() => store.rows
  .filter((row) => sidebarVisible(row) && store.getMenuTargetMode(row) === "MNGR")
  .map((row) => withVisibleChildren(row)));
const hiddenMenuRows = computed(() => collectHiddenMenus(store.rows));
const mainDrag = ref<{ targetMode: MenuTargetMode; index: number } | null>(null);
const childDrag = ref<{ parentId: number; index: number } | null>(null);

function sidebarVisible(row: MenuNode): boolean {
  return String(row.sidebarVisibleYn ?? "Y").toUpperCase() === "Y";
}

function withVisibleChildren(row: MenuNode): MenuNode {
  return {
    ...row,
    subMenuList: (row.subMenuList ?? []).filter(sidebarVisible).map((child) => withVisibleChildren(child)),
  };
}

function collectHiddenMenus(rows: MenuNode[]): MenuNode[] {
  const hiddenRows: MenuNode[] = [];
  rows.forEach((row) => {
    if (!sidebarVisible(row)) {
      hiddenRows.push({ ...row });
      return;
    }
    hiddenRows.push(...collectHiddenMenus(row.subMenuList ?? []));
  });
  return hiddenRows;
}

async function openEdit(id: number) {
  try {
    await store.openEdit(id);
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : t("menu.detail.load.failure"));
  }
}

async function submit() {
  if (!store.form.menuName.trim()) {
    void swalAlert(t("menu.validate.name.required"));
    return;
  }
  if (!store.form.menuLabel.trim()) {
    void swalAlert(t("menu.validate.label.required"));
    return;
  }
  if (!store.form.submenuExpandType) {
    void swalAlert(t("menu.validate.submenu-expand.required"));
    return;
  }
  if (store.form.submenuExpandType === "NO_SUB" && !store.form.url.trim()) {
    void swalAlert(t("menu.validate.url.required"));
    return;
  }
  if (store.form.parentMenuId == null) {
    void swalAlert(t("menu.validate.parent.required"));
    return;
  }
  if (store.form.unreadCntEnabled && !store.form.unreadCntNm.trim()) {
    void swalAlert(t("menu.validate.unread-count.required"));
    return;
  }
  try {
    await store.submit();
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : t("menu.save.failure"));
  }
}

function onUseYnChange(event: Event) {
  store.form.useYn = (event.target as HTMLInputElement).checked ? "Y" : "N";
}

function onSidebarVisibleYnChange(event: Event) {
  store.form.sidebarVisibleYn = (event.target as HTMLInputElement).checked ? "Y" : "N";
}

function onUnreadCntEnabledChange(event: Event) {
  if (!(event.target as HTMLInputElement).checked) {
    store.form.unreadCntNm = "";
  }
}

async function toggleUse(row: MenuNode) {
  if (!await swalConfirm(t("menu.use-yn.confirm").replace("{menuName}", row.menuName ?? t("menu.default-name")))) return;
  try {
    void swalAlert(await store.toggleUse(row));
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : t("menu.use-yn.change.failure"));
  }
}

async function deleteMenu(row: MenuNode) {
  if (!await swalConfirm(t("menu.delete.confirm").replace("{menuName}", row.menuName ?? t("menu.default-name")))) return;
  try {
    await store.deleteMenu(row.id);
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : t("menu.delete.failure"));
  }
}

async function onMainDrop(targetMode: MenuTargetMode, targetIndex: number) {
  const drag = mainDrag.value;
  mainDrag.value = null;
  if (!drag || drag.targetMode !== targetMode) return;
  try {
    const message = await store.reorderMainWithinGroup(targetMode, drag.index, targetIndex);
    if (message) void swalAlert(message);
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : t("menu.order.main.failure"));
    await store.fetchTree();
  }
}

function onMainDragStart(targetMode: MenuTargetMode, index: number) {
  mainDrag.value = { targetMode, index };
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
    void swalAlert(e instanceof Error ? e.message : t("menu.order.sub.failure"));
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

/* 트리의 메뉴명·설명은 서버가 요청 locale 로 지역화해 내려주므로, 언어를 바꾸면 재조회해야
   새 언어가 트리에 반영된다. (편집 폼은 상세 조회를 원천으로 쓰며 ko 원본을 유지한다) */
watch(
  () => localeStore.locale,
  async () => {
    await store.fetchTree();
  }
);
</script>

<style scoped>
.menu-admin-page {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

/* 다국어 번역 행: [로케일 select][메뉴명+설명][삭제 버튼] */
.menu-admin-i18n-row {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.menu-admin-i18n-locale {
  width: 7rem;
  min-width: 7rem;
  flex: 0 0 auto;
}

.menu-admin-i18n-fields {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  flex: 1 1 auto;
  min-width: 0;
}

.menu-admin-toolbar,
.menu-admin-actions {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.menu-admin-toolbar {
  justify-content: flex-end;
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

.menu-admin-column--system {
  grid-column: 1 / -1;
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

.menu-admin-form-pair {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 0.75rem;
}

.menu-admin-form-pair .form-label {
  margin-bottom: 0.35rem;
  font-weight: 700;
}

.menu-admin-readonly-field {
  display: flex;
  align-items: center;
  min-height: 43px;
}

.menu-admin-unread-row {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: 0.75rem;
  align-items: center;
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
  .menu-admin-form-pair,
  .menu-admin-unread-row,
  .menu-admin-icon-editor {
    grid-template-columns: 1fr;
  }

  .menu-admin-form-row > .form-label {
    padding-top: 0;
  }
}
</style>
