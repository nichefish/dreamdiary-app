import { computed, ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";
import { assertAuthenticatedBeforeModal } from "@/shared/auth/sessionPing";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { swalAlert } from "@/shared/utils/swal";

export interface BoardGroupRow {
  rnum?: number;
  id: number;
  boardKey: string;
  boardName: string;
  description?: string;
  sortOrder?: number;
  useYn: string;
  postCount?: number;
}

export interface BoardGroupForm {
  id: number | null;
  boardKey: string;
  boardName: string;
  description: string;
  useYn: string;
}

const EMPTY_FORM: BoardGroupForm = {
  id: null,
  boardKey: "",
  boardName: "",
  description: "",
  useYn: "Y",
};

function toFormData(form: BoardGroupForm): FormData {
  const fd = new FormData();
  if (form.id != null) fd.append("id", String(form.id));
  fd.append("boardKey", form.boardKey.trim());
  fd.append("boardName", form.boardName.trim());
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
    description: row?.description ?? "",
    useYn: String(row?.useYn ?? "Y").toUpperCase() === "Y" ? "Y" : "N",
  };
}

export const useBoardGroupStore = defineStore("boardGroup", () => {
  const { t } = useLocaleStore();
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
      if (!res.data?.rslt) throw new Error(res.data?.message ?? t("board.group.list.load.failure"));

      const pageResult = res.data?.rsltObj ?? {};
      rows.value = Array.isArray(pageResult.content) ? pageResult.content : [];
      totalElements.value = Number(pageResult.totalElements ?? 0);
      totalPages.value = Number(pageResult.totalPages ?? 0);
      currentPage.value = Number(pageResult.number ?? targetPage);
      pageSize.value = Number(pageResult.size ?? pageSize.value);
    } catch (e) {
      error.value = e instanceof Error ? e.message : t("board.group.list.load.failure");
    } finally {
      loading.value = false;
    }
  }

  async function openCreate() {
    if (!await assertAuthenticatedBeforeModal()) return;
    form.value = { ...EMPTY_FORM };
    modalOpen.value = true;
  }

  async function openEdit(id: number) {
    if (!await assertAuthenticatedBeforeModal()) return;
    detailLoading.value = true;
    form.value = { ...EMPTY_FORM, id };
    try {
      modalOpen.value = true;
      const res = await axios.get(`/api/board/groups/${id}`);
      if (!res.data?.rslt) throw new Error(res.data?.message ?? t("board.group.detail.load.failure"));
      form.value = normalizeForm(res.data?.rsltObj ?? {});
    } catch (e) {
      modalOpen.value = false;
      void swalAlert(e instanceof Error ? e.message : t("board.group.detail.load.failure"));
    } finally {
      detailLoading.value = false;
    }
  }

  function closeModal() {
    modalOpen.value = false;
    form.value = { ...EMPTY_FORM };
  }

  /**
   * 게시판 그룹 등록/수정 처리.
   * 변경 전에는 성공 직후 목록을 갱신하고 호출부가 알림을 띄웠다.
   * 변경 후에는 성공 알림 OK 이후 목록을 갱신한다.
   */
  async function submitForm() {
    saving.value = true;
    try {
      const wasCreate = form.value.id == null;
      const payload = toFormData(form.value);
      const url = form.value.id != null ? `/api/board/groups/${form.value.id}` : "/api/board/groups";
      const res = await axios.post(url, payload, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      if (!res.data?.rslt) throw new Error(res.data?.message ?? t("board.group.save.failure"));
      closeModal();
      const message = res.data?.message ?? t("common.result.saved");
      await swalAlert(message);
      await fetchList(wasCreate ? 0 : currentPage.value);
      return message;
    } finally {
      saving.value = false;
    }
  }

  async function toggleUse(row: BoardGroupRow) {
    const currentlyUse = String(row.useYn).toUpperCase() === "Y";
    const url = currentlyUse ? `/api/board/groups/${row.id}/unuse` : `/api/board/groups/${row.id}/use`;
    const res = await axios.post(url);
    if (!res.data?.rslt) throw new Error(res.data?.message ?? t("board.group.use-yn.change.failure"));
    await fetchList(currentPage.value);
    return res.data?.message ?? t("common.result.changed");
  }

  /**
   * 게시판 그룹 삭제 처리.
   * 변경 전에는 성공 직후 목록을 갱신하고 호출부가 알림을 띄웠다.
   * 변경 후에는 성공 알림 OK 이후 목록을 갱신한다.
   */
  async function deleteBoard(id: number) {
    const res = await axios.delete(`/api/board/groups/${id}`);
    if (!res.data?.rslt) throw new Error(res.data?.message ?? t("board.group.delete.failure"));
    const nextPage = rows.value.length <= 1 && currentPage.value > 0 ? currentPage.value - 1 : currentPage.value;
    const message = res.data?.message ?? t("common.result.deleted");
    await swalAlert(message);
    await fetchList(nextPage);
    return message;
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
      if (!res.data?.rslt) throw new Error(res.data?.message ?? t("board.group.order.failure"));
      await fetchList(currentPage.value);
      return res.data?.message ?? t("common.result.sort-order-saved");
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
