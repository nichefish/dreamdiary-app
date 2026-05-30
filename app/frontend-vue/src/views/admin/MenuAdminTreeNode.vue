<template>
  <li
    class="menu-admin-node"
    :class="{ 'is-main': isMain, 'is-disabled': !useY, 'is-dragging': dragging, 'is-drag-over': dragOver }"
    @dragover.prevent.stop="handleDragOver"
    @dragenter.prevent.stop="dragOver = true"
    @dragleave.stop="dragOver = false"
    @drop.prevent.stop="handleDrop"
  >
    <div class="menu-admin-node-head">
      <div class="menu-admin-node-title">
        <span
          class="menu-admin-node-icon"
          :class="{ 'is-drag-disabled': sortSaving }"
          draggable="true"
          title="끌어서 순서 변경"
          @dragstart.stop="handleDragStart"
          @dragend="handleDragEnd"
          v-html="node.icon || fallbackIcon"
        ></span>
        <div>
          <div class="menu-admin-node-name">
            <span>{{ node.menuName || "-" }}</span>
            <i v-if="protectedY" class="bi bi-shield-lock text-muted"></i>
            <i v-if="requiredY" class="bi bi-exclamation-diamond text-warning"></i>
          </div>
          <div class="menu-admin-node-meta">
            <span>{{ node.menuLabel || "-" }}</span>
            <span>{{ node.url || node.submenuExpandTypeName || node.submenuExpandType || "-" }}</span>
          </div>
        </div>
      </div>
      <div class="menu-admin-node-actions">
        <button v-if="canAddChild" type="button" class="btn btn-sm btn-icon btn-light-primary" title="하위 메뉴 추가" @click="$emit('add-child', node)">
          <i class="bi bi-plus-lg"></i>
        </button>
        <button type="button" class="btn btn-sm btn-icon btn-light-primary" title="수정" @click="$emit('edit', node.id)">
          <i class="bi bi-pencil-square"></i>
        </button>
        <button type="button" class="btn btn-sm btn-icon" :class="useY ? 'btn-light-success' : 'btn-light'" title="사용 여부" :disabled="requiredY" @click="$emit('toggle-use', node)">
          <i :class="useY ? 'bi bi-check2' : 'bi bi-x-lg'"></i>
        </button>
        <button type="button" class="btn btn-sm btn-icon btn-light-danger" title="삭제" :disabled="protectedY" @click="$emit('delete-node', node)">
          <i class="bi bi-trash"></i>
        </button>
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
import { computed, ref } from "vue";
import type { MenuNode } from "@/stores/menuAdmin";

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
const requiredY = computed(() => yn(props.node.requiredYn));
const protectedY = computed(() => yn(props.node.protectedYn));
const canAddChild = computed(() => !protectedY.value && props.node.submenuExpandType !== "NO_SUB");
const fallbackIcon = computed(() => (isMain.value ? '<i class="bi bi-folder2-open"></i>' : '<i class="bi bi-dot"></i>'));

function yn(value: string | undefined): boolean {
  return String(value ?? "N").toUpperCase() === "Y";
}

function handleDragStart(event: DragEvent): void {
  if (props.sortSaving) {
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
  if (event.dataTransfer) event.dataTransfer.dropEffect = "move";
}

function handleDragEnd(): void {
  dragOver.value = false;
  dragging.value = false;
}

function handleDrop(): void {
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
  gap: 0.25rem;
  flex-wrap: wrap;
  opacity: 0.72;
  transition: opacity 0.15s ease;
}

.menu-admin-node-head:hover .menu-admin-node-actions,
.menu-admin-node.is-drag-over .menu-admin-node-actions {
  opacity: 1;
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

.menu-admin-node-meta {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
  color: var(--bs-gray-600);
  font-size: 0.8rem;
  overflow-wrap: anywhere;
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
}
</style>
