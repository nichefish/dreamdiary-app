import { computed, ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";

export interface MenuNode {
  id: number;
  parentMenuId?: number | null;
  menuType: "MAIN" | "SUB" | string;
  parentMenuType?: string;
  sortOrder?: number;
  useYn?: string;
  adminYn?: string;
  menuName?: string;
  menuLabel?: string;
  icon?: string;
  unreadCntNm?: string;
  url?: string;
  protectedYn?: string;
  requiredYn?: string;
  submenuExpandType?: string;
  submenuExpandTypeName?: string;
  upperMenuNm?: string;
  subMenuList?: MenuNode[];
}

export interface MenuForm {
  id: number | null;
  menuType: "MAIN" | "SUB";
  parentMenuId: number | null;
  upperMenuNm: string;
  menuName: string;
  menuLabel: string;
  icon: string;
  unreadCntNm: string;
  submenuExpandType: string;
  url: string;
  adminYn: string;
  useYn: string;
}

export interface SubmenuExpandOption {
  code: string;
  codeName: string;
}

const EMPTY_FORM: MenuForm = {
  id: null,
  menuType: "MAIN",
  parentMenuId: null,
  upperMenuNm: "",
  menuName: "",
  menuLabel: "",
  icon: "",
  unreadCntNm: "",
  submenuExpandType: "NO_SUB",
  url: "",
  adminYn: "N",
  useYn: "Y",
};

const SUBMENU_EXPAND_OPTIONS: SubmenuExpandOption[] = [
  { code: "EXTEND", codeName: "우측으로 확장" },
  { code: "LIST", codeName: "하단에 목록 표시" },
  { code: "NO_SUB", codeName: "하위메뉴 없음" },
  { code: "COLLAPSE", codeName: "글접기" },
  { code: "BOARD", codeName: "일반게시판" },
];

function yn(value: string | undefined): string {
  return String(value ?? "N").toUpperCase() === "Y" ? "Y" : "N";
}

function cloneForm(row?: Partial<MenuNode>): MenuForm {
  return {
    id: row?.id ?? null,
    menuType: String(row?.menuType ?? "MAIN") === "SUB" ? "SUB" : "MAIN",
    parentMenuId: row?.parentMenuId ?? null,
    upperMenuNm: row?.upperMenuNm ?? "",
    menuName: row?.menuName ?? "",
    menuLabel: row?.menuLabel ?? "",
    icon: row?.icon ?? "",
    unreadCntNm: row?.unreadCntNm ?? "",
    submenuExpandType: row?.submenuExpandType ?? "NO_SUB",
    url: row?.url ?? "",
    adminYn: yn(row?.adminYn),
    useYn: yn(row?.useYn) === "Y" ? "Y" : "N",
  };
}

function toFormData(form: MenuForm): FormData {
  const fd = new FormData();
  if (form.id != null) fd.append("id", String(form.id));
  fd.append("menuType", form.menuType);
  if (form.parentMenuId != null) fd.append("parentMenuId", String(form.parentMenuId));
  fd.append("menuName", form.menuName.trim());
  fd.append("menuLabel", form.menuLabel.trim());
  fd.append("icon", form.icon.trim());
  fd.append("unreadCntNm", form.unreadCntNm.trim());
  fd.append("submenuExpandType", form.submenuExpandType);
  fd.append("url", form.submenuExpandType === "NO_SUB" ? form.url.trim() : "");
  fd.append("adminYn", form.menuType === "MAIN" ? yn(form.adminYn) : "N");
  fd.append("useYn", yn(form.useYn));
  return fd;
}

function walk(nodes: MenuNode[], visit: (node: MenuNode, depth: number) => void, depth = 0) {
  nodes.forEach((node) => {
    visit(node, depth);
    if (Array.isArray(node.subMenuList)) walk(node.subMenuList, visit, depth + 1);
  });
}

function containsNode(root: MenuNode, id: number): boolean {
  if (root.id === id) return true;
  return (root.subMenuList ?? []).some((child) => containsNode(child, id));
}

export const useMenuAdminStore = defineStore("menuAdmin", () => {
  const rows = ref<MenuNode[]>([]);
  const loading = ref(false);
  const error = ref("");
  const saving = ref(false);
  const sortSaving = ref(false);
  const modalOpen = ref(false);
  const form = ref<MenuForm>({ ...EMPTY_FORM });
  const submenuExpandOptions = ref<SubmenuExpandOption[]>(SUBMENU_EXPAND_OPTIONS);

  const isEdit = computed(() => form.value.id != null);
  const isMainForm = computed(() => form.value.menuType === "MAIN");
  const parentOptions = computed(() => {
    const options: Array<{ id: number; label: string; depth: number }> = [];
    const currentId = form.value.id;
    walk(rows.value, (node, depth) => {
      if (node.menuType !== "MAIN" && node.menuType !== "SUB") return;
      if (yn(node.protectedYn) === "Y") return;
      if (node.submenuExpandType === "NO_SUB") return;
      if (currentId != null && containsNode(node, currentId)) return;
      options.push({ id: node.id, label: node.menuName ?? `#${node.id}`, depth });
    });
    return options;
  });

  async function fetchTree() {
    loading.value = true;
    error.value = "";
    try {
      const res = await axios.get("/api/menu/menu-main-list");
      if (!res.data?.rslt) throw new Error(res.data?.message ?? "메뉴 목록을 불러오지 못했습니다.");
      rows.value = Array.isArray(res.data?.rsltList) ? res.data.rsltList : [];
    } catch (e) {
      error.value = e instanceof Error ? e.message : "메뉴 목록을 불러오지 못했습니다.";
      rows.value = [];
    } finally {
      loading.value = false;
    }
  }

  function openMainCreate() {
    form.value = { ...EMPTY_FORM, menuType: "MAIN" };
    modalOpen.value = true;
  }

  function openSubCreate(parent: MenuNode) {
    form.value = {
      ...EMPTY_FORM,
      menuType: "SUB",
      parentMenuId: parent.id,
      upperMenuNm: parent.menuName ?? "",
    };
    modalOpen.value = true;
  }

  async function openEdit(id: number) {
    modalOpen.value = true;
    saving.value = false;
    const res = await axios.get(`/api/menu/${id}`);
    if (!res.data?.rslt) throw new Error(res.data?.message ?? "메뉴 상세를 불러오지 못했습니다.");
    form.value = cloneForm(res.data?.rsltObj ?? {});
  }

  function closeModal() {
    modalOpen.value = false;
    form.value = { ...EMPTY_FORM };
  }

  async function submit() {
    saving.value = true;
    try {
      const id = form.value.id;
      const url = id != null ? `/api/menu/${id}` : "/api/menus";
      const res = await axios.post(url, toFormData(form.value), {
        headers: { "Content-Type": "multipart/form-data" },
      });
      if (!res.data?.rslt) throw new Error(res.data?.message ?? "메뉴를 저장하지 못했습니다.");
      closeModal();
      await fetchTree();
      return res.data?.message ?? "저장되었습니다.";
    } finally {
      saving.value = false;
    }
  }

  async function toggleUse(row: MenuNode) {
    const nextUseYn = yn(row.useYn) === "Y" ? "N" : "Y";
    const res = await axios.patch(`/api/menu/${row.id}`, { useYn: nextUseYn });
    if (!res.data?.rslt) throw new Error(res.data?.message ?? "메뉴 상태를 변경하지 못했습니다.");
    await fetchTree();
    return res.data?.message ?? "변경되었습니다.";
  }

  async function deleteMenu(id: number) {
    const res = await axios.delete(`/api/menu/${id}`);
    if (!res.data?.rslt) throw new Error(res.data?.message ?? "메뉴를 삭제하지 못했습니다.");
    await fetchTree();
    return res.data?.message ?? "삭제되었습니다.";
  }

  async function moveMain(index: number, delta: -1 | 1) {
    const targetIndex = index + delta;
    if (targetIndex < 0 || targetIndex >= rows.value.length) return "";
    const nextRows = [...rows.value];
    const [moved] = nextRows.splice(index, 1);
    nextRows.splice(targetIndex, 0, moved);
    rows.value = nextRows;
    return saveMainSortOrders();
  }

  async function saveMainSortOrders() {
    sortSaving.value = true;
    try {
      const sortOrders = rows.value.map((row, idx) => ({
        id: row.id,
        parentMenuId: row.parentMenuId ?? null,
        sortOrder: idx,
      }));
      const res = await axios.put("/api/menus/sort-orders", { sortOrders });
      if (!res.data?.rslt) throw new Error(res.data?.message ?? "메인 메뉴 순서를 저장하지 못했습니다.");
      await fetchTree();
      return res.data?.message ?? "정렬 순서가 저장되었습니다.";
    } finally {
      sortSaving.value = false;
    }
  }

  async function moveSub(parent: MenuNode, index: number, delta: -1 | 1) {
    const children = parent.subMenuList ?? [];
    const targetIndex = index + delta;
    if (targetIndex < 0 || targetIndex >= children.length) return "";
    const nextChildren = [...children];
    const [moved] = nextChildren.splice(index, 1);
    nextChildren.splice(targetIndex, 0, moved);
    parent.subMenuList = nextChildren;
    return saveSubTreeOrder(moved.id, parent.id, nextChildren);
  }

  async function saveSubTreeOrder(movedId: number, parentMenuId: number, siblings: MenuNode[]) {
    sortSaving.value = true;
    try {
      const items = siblings.map((row, idx) => ({ id: row.id, sortOrder: idx }));
      const res = await axios.put("/api/menus/tree", {
        movedId,
        sourceParentMenuId: parentMenuId,
        targetParentMenuId: parentMenuId,
        groups: [{ parentMenuId, items }],
      });
      if (!res.data?.rslt) throw new Error(res.data?.message ?? "하위 메뉴 순서를 저장하지 못했습니다.");
      await fetchTree();
      return res.data?.message ?? "정렬 순서가 저장되었습니다.";
    } finally {
      sortSaving.value = false;
    }
  }

  return {
    rows,
    loading,
    error,
    saving,
    sortSaving,
    modalOpen,
    form,
    submenuExpandOptions,
    isEdit,
    isMainForm,
    parentOptions,
    fetchTree,
    openMainCreate,
    openSubCreate,
    openEdit,
    closeModal,
    submit,
    toggleUse,
    deleteMenu,
    moveMain,
    moveSub,
  };
});
