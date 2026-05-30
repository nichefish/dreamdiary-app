import { computed, ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";
import { swalConfirm, swalAlert } from "@/utils/swal";

export interface BoardGroupRow {
  rnum?: number;
  id: number;
  boardKey: string;
  boardName: string;
  categoryGroupCode?: string;
  description?: string;
  sortOrder?: number;
  useYn: string;
  postCount?: number;
}

export interface BoardGroupForm {
  id: number | null;
  boardKey: string;
  boardName: string;
  categoryGroupCode: string;
  description: string;
  useYn: string;
}

const EMPTY_FORM: BoardGroupForm = {
  id: null,
  boardKey: "",
  boardName: "",
  categoryGroupCode: "",
  description: "",
  useYn: "Y",
};

function toFormData(form: BoardGroupForm): FormData {
  const fd = new FormData();
  if (form.id != null) fd.append("id", String(form.id));
  fd.append("boardKey", form.boardKey.trim());
  fd.append("boardName", form.boardName.trim());
  fd.append("categoryGroupCode", form.categoryGroupCode.trim());
  fd.append("description", form.description.trim());
  fd.append("useYn", form.useYn === "Y" ? "Y" : "N");
  return fd;
}

function normalizeForm(row?: Partial<BoardGroupRow>): BoardGroupForm {
  return {
    ...EMPTY_FORM,
    id: row?.id ?? null,
    boardKey: row?.boardKey ?? "",
    boardName: row?.boardName ?? "",
    categoryGroupCode: row?.categoryGroupCode ?? "",
    description: row?.description ?? "",
    useYn: String(row?.useYn ?? "Y").toUpperCase() === "Y" ? "Y" : "N",
  };
}

export const useBoardGroupStore = defineStore("boardGroup", () => {
  const rows = ref<BoardGroupRow[]>([]);
  const totalElements = ref(0);
  const totalPages = ref(0);
  const currentPage = ref(0);
  const pageSize = ref(10);
  const loading = ref(false);
  const saving = ref(false);
  const sortSaving = ref(false);
  const error = ref("");
  const keyword = ref("");

  const modalOpen = ref(false);
  const detailLoading = ref(false);
  const form = ref<BoardGroupForm>({ ...EMPTY_FORM });

  const isEdit = computed(() => form.value.id != null);

  async function fetchList(page?: number) {
    loading.value = true;
    error.value = "";
    const targetPage = page ?? currentPage.value;
    try {
      const params: Record<string, unknown> = {
        page: targetPage,
        size: pageSize.value,
      };
      if (keyword.value.trim()) params.searchKeyword = keyword.value.trim();

      const res = await axios.get("/api/board/groups", { params });
      if (!res.data?.rslt) throw new Error(res.data?.message ?? "게시판 그룹 목록을 불러오지 못했습니다.");

      const pageResult = res.data?.rsltObj ?? {};
      rows.value = Array.isArray(pageResult.content) ? pageResult.content : [];
      totalElements.value = Number(pageResult.totalElements ?? 0);
      totalPages.value = Number(pageResult.totalPages ?? 0);
      currentPage.value = Number(pageResult.number ?? targetPage);
      pageSize.value = Number(pageResult.size ?? pageSize.value);
    } catch (e) {
      error.value = e instanceof Error ? e.message : "게시판 그룹 목록을 불러오지 못했습니다.";
      rows.value = [];
      totalElements.value = 0;
      totalPages.value = 0;
    } finally {
      loading.value = false;
    }
  }

  function openCreate() {
    form.value = { ...EMPTY_FORM };
    modalOpen.value = true;
  }

  async function openEdit(id: number) {
    modalOpen.value = true;
    detailLoading.value = true;
    form.value = { ...EMPTY_FORM, id };
    try {
      const res = await axios.get(`/api/board/groups/${id}`);
      if (!res.data?.rslt) throw new Error(res.data?.message ?? "게시판 그룹을 불러오지 못했습니다.");
      form.value = normalizeForm(res.data?.rsltObj ?? {});
    } catch (e) {
      modalOpen.value = false;
      void swalAlert(e instanceof Error ? e.message : "게시판 그룹을 불러오지 못했습니다.");
    } finally {
      detailLoading.value = false;
    }
  }

  function closeModal() {
    modalOpen.value = false;
    form.value = { ...EMPTY_FORM };
  }

  async function submitForm() {
    saving.value = true;
    try {
      const wasCreate = form.value.id == null;
      const payload = toFormData(form.value);
      const url = form.value.id != null ? `/api/board/groups/${form.value.id}` : "/api/board/groups";
      const res = await axios.post(url, payload, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      if (!res.data?.rslt) throw new Error(res.data?.message ?? "게시판 그룹을 저장하지 못했습니다.");
      closeModal();
      await fetchList(wasCreate ? 0 : currentPage.value);
      return res.data?.message ?? "저장되었습니다.";
    } finally {
      saving.value = false;
    }
  }

  async function toggleUse(row: BoardGroupRow) {
    const currentlyUse = String(row.useYn).toUpperCase() === "Y";
    const url = currentlyUse ? `/api/board/groups/${row.id}/unuse` : `/api/board/groups/${row.id}/use`;
    const res = await axios.post(url);
    if (!res.data?.rslt) throw new Error(res.data?.message ?? "사용 여부를 변경하지 못했습니다.");
    await fetchList(currentPage.value);
    return res.data?.message ?? "변경되었습니다.";
  }

  async function deleteBoard(id: number) {
    const res = await axios.delete(`/api/board/groups/${id}`);
    if (!res.data?.rslt) throw new Error(res.data?.message ?? "게시판 그룹을 삭제하지 못했습니다.");
    const nextPage = rows.value.length <= 1 && currentPage.value > 0 ? currentPage.value - 1 : currentPage.value;
    await fetchList(nextPage);
    return res.data?.message ?? "삭제되었습니다.";
  }

  function moveRow(index: number, delta: -1 | 1) {
    const targetIndex = index + delta;
    if (targetIndex < 0 || targetIndex >= rows.value.length) return;
    const nextRows = [...rows.value];
    const [row] = nextRows.splice(index, 1);
    nextRows.splice(targetIndex, 0, row);
    rows.value = nextRows;
  }

  async function saveSortOrders() {
    sortSaving.value = true;
    try {
      const pageOffset = currentPage.value * pageSize.value;
      const sortOrders = rows.value.map((row, idx) => ({
        id: row.id,
        sortOrder: pageOffset + idx,
      }));
      const res = await axios.put("/api/board/groups/sort-orders", { sortOrders });
      if (!res.data?.rslt) throw new Error(res.data?.message ?? "정렬 순서를 저장하지 못했습니다.");
      await fetchList(currentPage.value);
      return res.data?.message ?? "정렬 순서가 저장되었습니다.";
    } finally {
      sortSaving.value = false;
    }
  }

  async function changePageSize(size: number) {
    pageSize.value = size;
    await fetchList(0);
  }

  return {
    rows,
    totalElements,
    totalPages,
    currentPage,
    pageSize,
    loading,
    saving,
    sortSaving,
    error,
    keyword,
    modalOpen,
    detailLoading,
    form,
    isEdit,
    fetchList,
    openCreate,
    openEdit,
    closeModal,
    submitForm,
    toggleUse,
    deleteBoard,
    moveRow,
    saveSortOrders,
    changePageSize,
  };
});
