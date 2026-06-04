import { computed, ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";
import { swalAlert } from "@/utils/swal";

export interface MenuNode {
  id: number;
  parentMenuId?: number | null;
  menuType: "MAIN" | "SUB" | string;
  managementType?: "MENU" | "BOARD" | string;
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
  submenuExpandType?: string;
  submenuExpandTypeName?: string;
  upperMenuNm?: string;
  subMenuList?: MenuNode[];
}

export interface MenuForm {
  id: number | null;
  parentMenuId: number | null;
  upperMenuNm: string;
  menuName: string;
  menuLabel: string;
  icon: string;
  unreadCntEnabled: boolean;
  unreadCntNm: string;
  submenuExpandType: string;
  url: string;
  useYn: string;
}

export interface SubmenuExpandOption {
  code: string;
  codeName: string;
}

export type MenuTargetMode = "USER" | "MNGR";

const EMPTY_FORM: MenuForm = {
  id: null,
  parentMenuId: null,
  upperMenuNm: "",
  menuName: "",
  menuLabel: "",
  icon: "",
  unreadCntEnabled: false,
  unreadCntNm: "",
  submenuExpandType: "NO_SUB",
  url: "",
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
    parentMenuId: row?.parentMenuId ?? null,
    upperMenuNm: row?.upperMenuNm ?? "",
    menuName: row?.menuName ?? "",
    menuLabel: row?.menuLabel ?? "",
    icon: row?.icon ?? "",
    unreadCntEnabled: Boolean(row?.unreadCntNm),
    unreadCntNm: row?.unreadCntNm ?? "",
    submenuExpandType: row?.submenuExpandType ?? "NO_SUB",
    url: row?.url ?? "",
    useYn: yn(row?.useYn) === "Y" ? "Y" : "N",
  };
}

function toFormData(form: MenuForm): FormData {
  const fd = new FormData();
  if (form.id != null) fd.append("id", String(form.id));
  if (form.parentMenuId != null) fd.append("parentMenuId", String(form.parentMenuId));
  fd.append("menuName", form.menuName.trim());
  fd.append("menuLabel", form.menuLabel.trim());
  fd.append("icon", form.icon.trim());
  fd.append("unreadCntNm", form.unreadCntEnabled ? form.unreadCntNm.trim() : "");
  fd.append("submenuExpandType", form.submenuExpandType);
  fd.append("url", form.submenuExpandType === "NO_SUB" ? form.url.trim() : "");
  fd.append("useYn", yn(form.useYn));
  return fd;
}

function getMenuTargetMode(row: MenuNode): MenuTargetMode {
  return yn(row.adminYn) === "Y" ? "MNGR" : "USER";
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

  function openSubCreate(parent: MenuNode) {
    form.value = {
      ...EMPTY_FORM,
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

  /**
   * 메뉴 등록/수정 처리.
   * 변경 전에는 성공 직후 트리를 갱신하고 호출부가 알림을 띄웠다.
   * 변경 후에는 성공 알림 OK 이후 트리를 갱신한다.
   */
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
      const message = res.data?.message ?? "저장되었습니다.";
      await swalAlert(message);
      await fetchTree();
      return message;
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

  /**
   * 메뉴 삭제 처리.
   * 변경 전에는 성공 직후 트리를 갱신하고 호출부가 알림을 띄웠다.
   * 변경 후에는 성공 알림 OK 이후 트리를 갱신한다.
   */
  async function deleteMenu(id: number) {
    const res = await axios.delete(`/api/menu/${id}`);
    if (!res.data?.rslt) throw new Error(res.data?.message ?? "메뉴를 삭제하지 못했습니다.");
    const message = res.data?.message ?? "삭제되었습니다.";
    await swalAlert(message);
    await fetchTree();
    return message;
  }

  async function reorderMainWithinGroup(targetMode: MenuTargetMode, sourceIndex: number, targetIndex: number) {
    const groupRows = rows.value.filter((row) => getMenuTargetMode(row) === targetMode);
    if (sourceIndex === targetIndex) return "";
    if (sourceIndex < 0 || sourceIndex >= groupRows.length) return "";
    if (targetIndex < 0 || targetIndex >= groupRows.length) return "";

    const nextGroupRows = [...groupRows];
    const [moved] = nextGroupRows.splice(sourceIndex, 1);
    nextGroupRows.splice(targetIndex, 0, moved);

    const groupQueue = [...nextGroupRows];
    rows.value = rows.value.map((row) => (getMenuTargetMode(row) === targetMode ? groupQueue.shift() ?? row : row));
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

  async function reorderSub(parent: MenuNode, sourceIndex: number, targetIndex: number) {
    const children = parent.subMenuList ?? [];
    if (sourceIndex === targetIndex) return "";
    if (sourceIndex < 0 || sourceIndex >= children.length) return "";
    if (targetIndex < 0 || targetIndex >= children.length) return "";
    const nextChildren = [...children];
    const [moved] = nextChildren.splice(sourceIndex, 1);
    nextChildren.splice(targetIndex, 0, moved);
    parent.subMenuList = nextChildren;
    return saveSubSortOrders(nextChildren);
  }

  async function saveSubSortOrders(siblings: MenuNode[]) {
    sortSaving.value = true;
    try {
      const sortOrders = siblings.map((row, idx) => ({
        id: row.id,
        parentMenuId: row.parentMenuId ?? null,
        sortOrder: idx,
      }));
      const res = await axios.put("/api/menus/sort-orders", { sortOrders });
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
    getMenuTargetMode,
    fetchTree,
    openSubCreate,
    openEdit,
    closeModal,
    submit,
    toggleUse,
    deleteMenu,
    reorderMainWithinGroup,
    reorderSub,
  };
});
