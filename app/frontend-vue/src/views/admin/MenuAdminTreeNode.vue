<template>
  <li class="menu-admin-node" :class="{ 'is-main': isMain, 'is-disabled': !useY }">
    <div class="menu-admin-node-head">
      <div class="menu-admin-node-title">
        <span class="menu-admin-node-icon" v-html="node.icon || fallbackIcon"></span>
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
        <button type="button" class="btn btn-sm btn-icon btn-light" title="위로" :disabled="index <= 0 || sortSaving" @click="$emit('move', index, -1)">
          <i class="bi bi-chevron-up"></i>
        </button>
        <button type="button" class="btn btn-sm btn-icon btn-light" title="아래로" :disabled="index >= siblingCount - 1 || sortSaving" @click="$emit('move', index, 1)">
          <i class="bi bi-chevron-down"></i>
        </button>
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
        @move="(idx, delta) => $emit('move-child', node, idx, delta)"
        @move-child="(parent, idx, delta) => $emit('move-child', parent, idx, delta)"
      />
    </ol>
  </li>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { MenuNode } from "@/stores/menuAdmin";

const props = defineProps<{
  node: MenuNode;
  index: number;
  siblingCount: number;
  sortSaving: boolean;
}>();

defineEmits<{
  (e: "add-child", node: MenuNode): void;
  (e: "edit", id: number): void;
  (e: "toggle-use", node: MenuNode): void;
  (e: "delete-node", node: MenuNode): void;
  (e: "move", index: number, delta: -1 | 1): void;
  (e: "move-child", parent: MenuNode, index: number, delta: -1 | 1): void;
}>();

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
</script>

<style scoped>
.menu-admin-node,
.menu-admin-children {
  margin: 0;
  padding: 0;
  list-style: none;
}

.menu-admin-node {
  padding: 0.75rem;
  border: 1px solid var(--bs-gray-200);
  border-radius: 8px;
  background: var(--bs-white);
}

.menu-admin-node.is-main {
  border-color: var(--bs-gray-300);
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
  align-items: flex-start;
}

.menu-admin-node-actions {
  gap: 0.5rem;
  flex-wrap: wrap;
}

.menu-admin-node-title {
  min-width: 0;
}

.menu-admin-node-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  min-width: 32px;
  height: 32px;
  border-radius: 8px;
  background: var(--bs-light);
  color: var(--bs-primary);
  overflow: hidden;
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
  gap: 0.75rem;
  margin-top: 0.75rem;
  padding-left: 1rem;
  border-left: 2px solid var(--bs-gray-200);
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
