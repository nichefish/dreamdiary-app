<template>
  <li
    class="menu-admin-node"
    :class="{ 'is-main': isMain, 'is-disabled': !useY, 'is-dragging': dragging, 'is-drag-over': dragOver }"
    @dragover.stop="handleDragOver"
    @dragenter.stop="handleDragEnter"
    @dragleave.stop="dragOver = false"
    @drop.stop="handleDrop"
  >
    <div class="menu-admin-node-head">
      <div class="menu-admin-node-title">
        <span
          class="menu-admin-node-icon"
          :class="{ 'is-drag-disabled': !canDragHandle }"
          :draggable="canDragHandle"
          :title="canDragHandle ? t('menu.tree.drag-handle') : t('menu.tree.protected')"
          @dragstart.stop="handleDragStart"
          @dragend="handleDragEnd"
          v-html="node.icon || fallbackIcon"
        ></span>
        <div>
          <div class="menu-admin-node-name">
            <span>{{ node.menuName || "-" }}</span>
            <i v-if="protectedY" v-tooltip class="bi bi-shield-lock text-warning menu-admin-status-icon" :title="t('menu.tree.protected')"></i>
            <i v-if="!sidebarVisibleY" v-tooltip class="bi bi-eye-slash text-muted menu-admin-status-icon" :title="t('menu.tree.sidebar-hidden')"></i>
          </div>
          <div class="menu-admin-node-meta">
            <span>{{ node.menuLabel || "-" }}</span>
            <span>{{ node.url || node.submenuExpandTypeName || node.submenuExpandType || "-" }}</span>
          </div>
        </div>
      </div>
      <RouterLink v-if="boardManaged" class="menu-admin-node-link" to="/admin/board-group">
        <i class="bi bi-box-arrow-up-right"></i>
        <span>{{ t('menu.tree.go-to-board') }}</span>
      </RouterLink>
      <div v-if="!boardManaged" class="menu-admin-node-actions">
        <button
          type="button"
          class="btn btn-sm btn-icon btn-light"
          data-bs-toggle="dropdown"
          data-bs-auto-close="true"
          aria-expanded="false"
          :title="t('menu.tree.actions')"
        >
          <i class="bi bi-three-dots-vertical"></i>
        </button>
        <div class="dropdown-menu menu-admin-node-menu dropdown-menu-end">
          <button v-if="canAddChild" type="button" class="dropdown-item" @click="$emit('add-child', node)">
            <i class="bi bi-plus-lg"></i>
            <span>{{ t('menu.tree.add-submenu') }}</span>
          </button>
          <button type="button" class="dropdown-item" :disabled="protectedY" @click="$emit('edit', node.id)">
            <i class="bi bi-pencil-square"></i>
            <span>{{ t('menu.tree.edit') }}</span>
          </button>
          <button type="button" class="dropdown-item" :disabled="protectedY" @click="$emit('toggle-use', node)">
            <i :class="useY ? 'bi bi-x-lg' : 'bi bi-check2'"></i>
            <span>{{ useY ? t('menu.tree.use-yn.disable') : t('menu.tree.use-yn.enable') }}</span>
          </button>
          <div class="dropdown-divider"></div>
          <button type="button" class="dropdown-item text-danger" :disabled="protectedY" @click="$emit('delete-node', node)">
            <i class="bi bi-trash"></i>
            <span>{{ t('menu.tree.delete') }}</span>
          </button>
        </div>
      </div>
    </div>

    <ol v-if="children.length" class="menu-admin-children">
      <MenuAdminTreeNode
        v-for="(child, childIndex) in children"
        :key="child.id"
        :node="child"
        :index="childIndex"
        :sibling-count="children.length"
        :sort-saving="sortSaving"
        @add-child="$emit('add-child', $event)"
        @edit="$emit('edit', $event)"
        @toggle-use="$emit('toggle-use', $event)"
        @delete-node="$emit('delete-node', $event)"
        @drag-start="(idx) => $emit('child-drag-start', node, idx)"
        @drop-node="(idx) => $emit('child-drop', node, idx)"
        @child-drag-start="(parent, idx) => $emit('child-drag-start', parent, idx)"
        @child-drop="(parent, idx) => $emit('child-drop', parent, idx)"
      />
    </ol>
  </li>
</template>

<script setup lang="ts">
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { computed, nextTick, onMounted, ref, watch } from "vue";
import { reinitializeComponents } from "@metronic/core/plugins/keenthemes";
import type { MenuNode } from "@/features/admin/stores/menuAdmin";

const { t } = useLocaleStore();

const props = defineProps<{
  node: MenuNode;
  index: number;
  siblingCount: number;
  sortSaving: boolean;
}>();

const emit = defineEmits<{
  (e: "add-child", node: MenuNode): void;
  (e: "edit", id: number): void;
  (e: "toggle-use", node: MenuNode): void;
  (e: "delete-node", node: MenuNode): void;
  (e: "drag-start", index: number): void;
  (e: "drop-node", index: number): void;
  (e: "child-drag-start", parent: MenuNode, index: number): void;
  (e: "child-drop", parent: MenuNode, index: number): void;
}>();

const dragOver = ref(false);
const dragging = ref(false);
const children = computed(() => props.node.subMenuList ?? []);
const isMain = computed(() => props.node.menuType === "MAIN");
const useY = computed(() => yn(props.node.useYn));
const protectedY = computed(() => yn(props.node.protectedYn));
const sidebarVisibleY = computed(() => yn(props.node.sidebarVisibleYn ?? "Y"));
const canDragHandle = computed(() => !props.sortSaving && !protectedY.value);
const boardManaged = computed(() => props.node.managementType === "BOARD");
const canAddChild = computed(() => !boardManaged.value && props.node.submenuExpandType !== "NO_SUB");
const fallbackIcon = computed(() => (isMain.value ? '<i class="bi bi-folder2-open"></i>' : '<i class="bi bi-dot"></i>'));

async function refreshMenuComponents(): Promise<void> {
  await nextTick();
  reinitializeComponents();
}

onMounted(() => {
  void refreshMenuComponents();
});

watch(
  () => [props.node.id, props.node.useYn, props.node.protectedYn, props.node.sidebarVisibleYn, props.node.managementType, props.node.submenuExpandType],
  () => {
    void refreshMenuComponents();
  }
);

function yn(value: string | undefined): boolean {
  return String(value ?? "N").toUpperCase() === "Y";
}

function handleDragStart(event: DragEvent): void {
  if (!canDragHandle.value) {
    event.preventDefault();
    return;
  }
  event.dataTransfer?.setData("text/plain", String(props.node.id));
  const dragImage = (event.currentTarget as HTMLElement).closest(".menu-admin-node-head") as HTMLElement | null;
  if (dragImage) event.dataTransfer?.setDragImage?.(dragImage, 24, 22);
  event.dataTransfer && (event.dataTransfer.effectAllowed = "move");
  dragging.value = true;
  emit("drag-start", props.index);
}

function handleDragOver(event: DragEvent): void {
  if (protectedY.value || boardManaged.value) return;
  event.preventDefault();
  if (event.dataTransfer) event.dataTransfer.dropEffect = "move";
}

function handleDragEnter(event: DragEvent): void {
  if (protectedY.value || boardManaged.value) return;
  event.preventDefault();
  dragOver.value = true;
}

function handleDragEnd(): void {
  dragOver.value = false;
  dragging.value = false;
}

function handleDrop(): void {
  if (protectedY.value || boardManaged.value) return;
  dragOver.value = false;
  dragging.value = false;
  if (!props.sortSaving) emit("drop-node", props.index);
}
</script>

<style scoped>
.menu-admin-node,
.menu-admin-children {
  margin: 0;
  padding: 0;
  list-style: none;
}

.menu-admin-node {
  padding: 0.25rem 0;
  border: 0;
  border-radius: 6px;
  background: var(--bs-white);
  transition: opacity 0.12s ease, transform 0.12s ease;
}

.menu-admin-node.is-main {
  background: transparent;
}

.menu-admin-node.is-dragging {
  opacity: 0.58;
  transform: scale(0.995);
}

.menu-admin-node.is-drag-over > .menu-admin-node-head {
  background: var(--bs-primary-light);
  box-shadow: inset 3px 0 0 var(--bs-primary), 0 6px 18px rgba(var(--bs-primary-rgb), 0.12);
  transform: translateX(2px);
}

.menu-admin-node.is-disabled .menu-admin-node-name {
  text-decoration: line-through;
  color: var(--bs-gray-500);
}

.menu-admin-node-head,
.menu-admin-node-title,
.menu-admin-node-actions {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.menu-admin-node-head {
  justify-content: space-between;
  align-items: center;
  min-height: 44px;
  padding: 0.35rem 0.5rem;
  border-radius: 6px;
  background: var(--bs-white);
  border: 1px solid transparent;
  transition: background-color 0.15s ease, border-color 0.15s ease, box-shadow 0.15s ease, transform 0.15s ease;
}

.menu-admin-node-head:hover {
  background: var(--bs-gray-100);
}

.menu-admin-node-head:hover .menu-admin-node-icon {
  border-color: rgba(var(--bs-primary-rgb), 0.36);
  background: rgba(var(--bs-primary-rgb), 0.08);
  box-shadow: 0 0 0 3px rgba(var(--bs-primary-rgb), 0.08);
  transform: translateY(-1px);
}

.menu-admin-node-actions {
  position: relative;
  gap: 0.25rem;
  opacity: 0.86;
  transition: opacity 0.15s ease;
}

.menu-admin-node-head:hover .menu-admin-node-actions,
.menu-admin-node.is-drag-over .menu-admin-node-actions {
  opacity: 1;
}

.menu-admin-node-menu {
  min-width: 160px;
  padding: 0.35rem;
}

.menu-admin-node-menu .dropdown-item {
  display: flex;
  align-items: center;
  gap: 0.55rem;
  min-height: 34px;
  border-radius: 6px;
  font-size: 0.9rem;
}

.menu-admin-node-menu .dropdown-item > i {
  width: 1rem;
  text-align: center;
}

.menu-admin-node-menu .dropdown-item:disabled {
  opacity: 0.45;
}

.menu-admin-node-title {
  min-width: 0;
}

.menu-admin-node-icon {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  min-width: 32px;
  height: 32px;
  border-radius: 6px;
  border: 1px solid var(--bs-gray-200);
  background: var(--bs-light);
  color: var(--bs-primary);
  cursor: grab;
  overflow: hidden;
  transition: background-color 0.15s ease, border-color 0.15s ease, box-shadow 0.15s ease, transform 0.15s ease;
}

.menu-admin-node-icon::after {
  position: absolute;
  right: 3px;
  bottom: 2px;
  color: var(--bs-gray-500);
  content: "⋮";
  font-size: 10px;
  line-height: 1;
  opacity: 0;
  transition: opacity 0.15s ease;
}

.menu-admin-node-head:hover .menu-admin-node-icon::after,
.menu-admin-node.is-dragging .menu-admin-node-icon::after,
.menu-admin-node.is-drag-over .menu-admin-node-icon::after {
  opacity: 0.9;
}

.menu-admin-node-icon:active {
  cursor: grabbing;
  transform: scale(0.96);
}

.menu-admin-node.is-dragging .menu-admin-node-icon {
  border-color: var(--bs-primary);
  background: var(--bs-primary);
  color: #fff;
  box-shadow: 0 0 0 4px rgba(var(--bs-primary-rgb), 0.14);
}

.menu-admin-node-icon.is-drag-disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.menu-admin-node-name {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  font-weight: 700;
}

.menu-admin-status-icon {
  cursor: help;
}

.menu-admin-node-meta {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
  color: var(--bs-gray-600);
  font-size: 0.8rem;
  overflow-wrap: anywhere;
}

.menu-admin-node-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  align-self: stretch;
  flex: 0 0 auto;
  min-width: 160px;
  gap: 0.35rem;
  min-height: 34px;
  margin-left: auto;
  border: 1px solid rgba(var(--bs-primary-rgb), 0.18);
  border-radius: 6px;
  background: rgba(var(--bs-primary-rgb), 0.06);
  color: var(--bs-primary);
  font-weight: 600;
}

.menu-admin-node-link:hover {
  border-color: rgba(var(--bs-primary-rgb), 0.36);
  background: rgba(var(--bs-primary-rgb), 0.1);
  color: var(--bs-primary);
}

.menu-admin-children {
  display: grid;
  gap: 0.25rem;
  margin-top: 0.25rem;
  margin-left: 1.35rem;
  padding-left: 0.75rem;
  border-left: 1px solid var(--bs-gray-300);
}

@media (max-width: 768px) {
  .menu-admin-node-head,
  .menu-admin-node-actions {
    align-items: stretch;
    width: 100%;
  }

  .menu-admin-node-head {
    flex-direction: column;
  }

  .menu-admin-node-actions {
    justify-content: flex-end;
  }

  .menu-admin-node-link {
    width: 100%;
    max-width: none;
    margin-left: 0;
  }
}
</style>
