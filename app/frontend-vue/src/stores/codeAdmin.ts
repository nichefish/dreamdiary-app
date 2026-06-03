import { computed, ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";
import { swalAlert } from "@/utils/swal";

export interface CodeGroupRow {
  rnum?: number;
  id: number;
  groupCode: string;
  groupName: string;
  description?: string;
  protectedYn?: string;
  useYn: string;
  codeItemCnt?: number;
  codeItems?: CodeItemRow[];
}

export interface CodeItemRow {
  rnum?: number;
  id: number;
  groupCode: string;
  code: string;
  codeName: string;
  description?: string;
  protectedYn?: string;
  useYn: string;
  sortOrder?: number;
}

export interface CodeGroupForm {
  id: number | null;
  groupCode: string;
  groupName: string;
  description: string;
  useYn: string;
}

export interface CodeItemForm {
  id: number | null;
  groupCode: string;
  code: string;
  codeName: string;
  description: string;
  useYn: string;
}

const EMPTY_GROUP_FORM: CodeGroupForm = {
  id: null,
  groupCode: "",
  groupName: "",
  description: "",
  useYn: "Y",
};

const EMPTY_ITEM_FORM: CodeItemForm = {
  id: null,
  groupCode: "",
  code: "",
  codeName: "",
  description: "",
  useYn: "Y",
};

function yn(value: string | undefined): string {
  return String(value ?? "Y").toUpperCase() === "Y" ? "Y" : "N";
}

function normalizeGroupForm(row?: Partial<CodeGroupRow>): CodeGroupForm {
  return {
    id: row?.id ?? null,
    groupCode: row?.groupCode ?? "",
    groupName: row?.groupName ?? "",
    description: row?.description ?? "",
    useYn: yn(row?.useYn),
  };
}

function normalizeItemForm(row?: Partial<CodeItemRow>, groupCode = ""): CodeItemForm {
  return {
    id: row?.id ?? null,
    groupCode: row?.groupCode ?? groupCode,
    code: row?.code ?? "",
    codeName: row?.codeName ?? "",
    description: row?.description ?? "",
    useYn: yn(row?.useYn),
  };
}

function toGroupFormData(form: CodeGroupForm): FormData {
  const fd = new FormData();
  if (form.id != null) fd.append("id", String(form.id));
  fd.append("groupCode", form.groupCode.trim().toUpperCase());
  fd.append("groupName", form.groupName.trim());
  fd.append("description", form.description.trim());
  fd.append("useYn", yn(form.useYn));
  return fd;
}

function toItemFormData(form: CodeItemForm): FormData {
  const fd = new FormData();
  if (form.id != null) fd.append("id", String(form.id));
  fd.append("groupCode", form.groupCode.trim().toUpperCase());
  fd.append("code", form.code.trim().toUpperCase());
  fd.append("codeName", form.codeName.trim());
  fd.append("description", form.description.trim());
  fd.append("useYn", yn(form.useYn));
  return fd;
}

export const useCodeAdminStore = defineStore("codeAdmin", () => {
  const rows = ref<CodeGroupRow[]>([]);
  const totalElements = ref(0);
  const totalPages = ref(0);
  const currentPage = ref(0);
  const pageSize = ref(10);
  const keyword = ref("");
  const loading = ref(false);
  const error = ref("");

  const groupModalOpen = ref(false);
  const groupSaving = ref(false);
  const groupForm = ref<CodeGroupForm>({ ...EMPTY_GROUP_FORM });

  const detailOpen = ref(false);
  const detailLoading = ref(false);
  const detail = ref<CodeGroupRow | null>(null);
  const items = ref<CodeItemRow[]>([]);
  const itemSortSaving = ref(false);

  const itemModalOpen = ref(false);
  const itemSaving = ref(false);
  const itemForm = ref<CodeItemForm>({ ...EMPTY_ITEM_FORM });

  const isGroupEdit = computed(() => groupForm.value.id != null);
  const isItemEdit = computed(() => itemForm.value.id != null);

  async function fetchGroups(page?: number) {
    loading.value = true;
    error.value = "";
    const targetPage = page ?? currentPage.value;
    try {
      const params: Record<string, unknown> = {
        page: targetPage,
        size: pageSize.value,
      };
      if (keyword.value.trim()) params.searchKeyword = keyword.value.trim();

      const res = await axios.get("/api/code/groups", { params });
      if (!res.data?.rslt) throw new Error(res.data?.message ?? "코드 그룹 목록을 불러오지 못했습니다.");
      const pageResult = res.data?.rsltObj ?? {};
      rows.value = Array.isArray(pageResult.content) ? pageResult.content : [];
      totalElements.value = Number(pageResult.totalElements ?? 0);
      totalPages.value = Number(pageResult.totalPages ?? 0);
      currentPage.value = Number(pageResult.number ?? targetPage);
      pageSize.value = Number(pageResult.size ?? pageSize.value);
    } catch (e) {
      error.value = e instanceof Error ? e.message : "코드 그룹 목록을 불러오지 못했습니다.";
      rows.value = [];
      totalElements.value = 0;
      totalPages.value = 0;
    } finally {
      loading.value = false;
    }
  }

  async function changePageSize(size: number) {
    pageSize.value = size;
    await fetchGroups(0);
  }

  function openGroupCreate() {
    groupForm.value = { ...EMPTY_GROUP_FORM };
    groupModalOpen.value = true;
  }

  async function openGroupEdit(id: number) {
    groupModalOpen.value = true;
    groupSaving.value = false;
    const res = await axios.get(`/api/code/group/${id}`);
    if (!res.data?.rslt) throw new Error(res.data?.message ?? "코드 그룹을 불러오지 못했습니다.");
    groupForm.value = normalizeGroupForm(res.data?.rsltObj ?? {});
  }

  function closeGroupModal() {
    groupModalOpen.value = false;
    groupForm.value = { ...EMPTY_GROUP_FORM };
  }

  /**
   * 코드 그룹 등록/수정 처리.
   * 변경 전에는 성공 직후 목록·상세를 갱신하고 호출부가 알림을 띄웠다.
   * 변경 후에는 성공 알림 OK 이후 목록·상세를 갱신한다.
   */
  async function submitGroup() {
    groupSaving.value = true;
    try {
      const wasCreate = groupForm.value.id == null;
      const id = groupForm.value.id;
      const url = id != null ? `/api/code/group/${id}` : "/api/code/groups";
      const res = await axios.post(url, toGroupFormData(groupForm.value), {
        headers: { "Content-Type": "multipart/form-data" },
      });
      if (!res.data?.rslt) throw new Error(res.data?.message ?? "코드 그룹을 저장하지 못했습니다.");
      closeGroupModal();
      const message = res.data?.message ?? "저장되었습니다.";
      await swalAlert(message);
      await fetchGroups(wasCreate ? 0 : currentPage.value);
      if (detail.value?.id === id) await openDetail(id);
      return message;
    } finally {
      groupSaving.value = false;
    }
  }

  async function toggleGroupUse(row: CodeGroupRow) {
    const nextUseYn = yn(row.useYn) === "Y" ? "N" : "Y";
    const res = await axios.patch(`/api/code/group/${row.id}`, { useYn: nextUseYn });
    if (!res.data?.rslt) throw new Error(res.data?.message ?? "사용 여부를 변경하지 못했습니다.");
    await fetchGroups(currentPage.value);
    if (detail.value?.id === row.id) await openDetail(row.id);
    return res.data?.message ?? "변경되었습니다.";
  }

  /**
   * 코드 그룹 삭제 처리.
   * 변경 전에는 성공 직후 목록을 갱신하고 호출부가 알림을 띄웠다.
   * 변경 후에는 성공 알림 OK 이후 목록을 갱신한다.
   */
  async function deleteGroup(id: number) {
    const res = await axios.delete(`/api/code/group/${id}`);
    if (!res.data?.rslt) throw new Error(res.data?.message ?? "코드 그룹을 삭제하지 못했습니다.");
    if (detail.value?.id === id) closeDetail();
    const nextPage = rows.value.length <= 1 && currentPage.value > 0 ? currentPage.value - 1 : currentPage.value;
    const message = res.data?.message ?? "삭제되었습니다.";
    await swalAlert(message);
    await fetchGroups(nextPage);
    return message;
  }

  async function openDetail(id: number) {
    detailOpen.value = true;
    detailLoading.value = true;
    try {
      const res = await axios.get(`/api/code/group/${id}`);
      if (!res.data?.rslt) throw new Error(res.data?.message ?? "코드 그룹 상세를 불러오지 못했습니다.");
      const group = (res.data?.rsltObj ?? {}) as CodeGroupRow;
      detail.value = group;
      if (Array.isArray(group.codeItems)) {
        items.value = group.codeItems;
      } else if (group.groupCode) {
        await fetchItems(group.groupCode);
      } else {
        items.value = [];
      }
    } finally {
      detailLoading.value = false;
    }
  }

  function closeDetail() {
    detailOpen.value = false;
    detail.value = null;
    items.value = [];
  }

  async function fetchItems(groupCode?: string) {
    const targetGroupCode = groupCode ?? detail.value?.groupCode ?? "";
    if (!targetGroupCode) {
      items.value = [];
      return;
    }
    const res = await axios.get("/api/code/items", { params: { groupCode: targetGroupCode } });
    if (!res.data?.rslt) throw new Error(res.data?.message ?? "상세 코드 목록을 불러오지 못했습니다.");
    items.value = Array.isArray(res.data?.rsltList) ? res.data.rsltList : [];
  }

  function openItemCreate() {
    itemForm.value = normalizeItemForm(undefined, detail.value?.groupCode ?? "");
    itemModalOpen.value = true;
  }

  async function openItemEdit(id: number) {
    itemModalOpen.value = true;
    const res = await axios.get("/api/code/item", { params: { id } });
    if (!res.data?.rslt) throw new Error(res.data?.message ?? "상세 코드를 불러오지 못했습니다.");
    itemForm.value = normalizeItemForm(res.data?.rsltObj ?? {}, detail.value?.groupCode ?? "");
  }

  function closeItemModal() {
    itemModalOpen.value = false;
    itemForm.value = { ...EMPTY_ITEM_FORM };
  }

  /**
   * 상세 코드 등록/수정 처리.
   * 변경 전에는 성공 직후 그룹 상세와 목록을 갱신하고 호출부가 알림을 띄웠다.
   * 변경 후에는 성공 알림 OK 이후 그룹 상세와 목록을 갱신한다.
   */
  async function submitItem() {
    itemSaving.value = true;
    try {
      const wasCreate = itemForm.value.id == null;
      const url = wasCreate ? "/api/code/items" : "/api/code/item";
      const res = await axios.post(url, toItemFormData(itemForm.value), {
        headers: { "Content-Type": "multipart/form-data" },
      });
      if (!res.data?.rslt) throw new Error(res.data?.message ?? "상세 코드를 저장하지 못했습니다.");
      const groupId = detail.value?.id;
      const groupCode = itemForm.value.groupCode;
      closeItemModal();
      const message = res.data?.message ?? "저장되었습니다.";
      await swalAlert(message);
      if (groupId) await openDetail(groupId);
      else await fetchItems(groupCode);
      await fetchGroups(currentPage.value);
      return message;
    } finally {
      itemSaving.value = false;
    }
  }

  /**
   * 상세 코드 삭제 처리.
   * 변경 전에는 성공 직후 그룹 상세와 목록을 갱신하고 호출부가 알림을 띄웠다.
   * 변경 후에는 성공 알림 OK 이후 그룹 상세와 목록을 갱신한다.
   */
  async function deleteItem(id: number) {
    const res = await axios.delete("/api/code/item", { params: { id } });
    if (!res.data?.rslt) throw new Error(res.data?.message ?? "상세 코드를 삭제하지 못했습니다.");
    const message = res.data?.message ?? "삭제되었습니다.";
    await swalAlert(message);
    if (detail.value?.id) await openDetail(detail.value.id);
    await fetchGroups(currentPage.value);
    return message;
  }

  function moveItem(index: number, delta: -1 | 1) {
    const targetIndex = index + delta;
    if (targetIndex < 0 || targetIndex >= items.value.length) return;
    const nextItems = [...items.value];
    const [row] = nextItems.splice(index, 1);
    nextItems.splice(targetIndex, 0, row);
    items.value = nextItems;
  }

  async function saveItemSortOrders() {
    if (!detail.value?.groupCode) return "";
    itemSortSaving.value = true;
    try {
      const sortOrders = items.value.map((item, idx) => ({
        id: item.id,
        groupCode: detail.value?.groupCode,
        sortOrder: idx + 1,
      }));
      const res = await axios.put("/api/code/items/sort-orders", { sortOrders });
      if (!res.data?.rslt) throw new Error(res.data?.message ?? "정렬 순서를 저장하지 못했습니다.");
      await fetchItems(detail.value.groupCode);
      return res.data?.message ?? "정렬 순서가 저장되었습니다.";
    } finally {
      itemSortSaving.value = false;
    }
  }

  return {
    rows,
    totalElements,
    totalPages,
    currentPage,
    pageSize,
    keyword,
    loading,
    error,
    groupModalOpen,
    groupSaving,
    groupForm,
    detailOpen,
    detailLoading,
    detail,
    items,
    itemSortSaving,
    itemModalOpen,
    itemSaving,
    itemForm,
    isGroupEdit,
    isItemEdit,
    fetchGroups,
    changePageSize,
    openGroupCreate,
    openGroupEdit,
    closeGroupModal,
    submitGroup,
    toggleGroupUse,
    deleteGroup,
    openDetail,
    closeDetail,
    fetchItems,
    openItemCreate,
    openItemEdit,
    closeItemModal,
    submitItem,
    deleteItem,
    moveItem,
    saveItemSortOrders,
  };
});
