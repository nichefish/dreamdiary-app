import { computed, ref } from "vue";
import { defineStore } from "pinia";
import { assertAuthenticatedBeforeModal } from "@/shared/auth/sessionPing";
import { BASE_LOCALE, SUPPORTED_LOCALES, useLocaleStore } from "@/shared/i18n/stores/locale";
import { swalAlert } from "@/shared/utils/swal";
import { apiPost, apiPatch, apiPut, apiDelete, getList, getObj, getPage, unwrapOk, assertOk } from "@/shared/api/client";

/**
 * 코드 다국어 입력에서 고를 수 있는 로케일.
 * 기준 로케일(ko)의 코드명은 code_item.code_name 이 단일 원천이라 제외한다.
 * SUPPORTED_LOCALES 에 로케일을 추가하면 코드 수정 없이 여기에 자동 반영된다.
 */
export const I18N_LOCALE_OPTIONS: readonly string[] = SUPPORTED_LOCALES.filter((locale) => locale !== BASE_LOCALE);

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
  /** 다국어 번역명 (locale → 번역명). ko 는 codeName 이 기준이라 포함하지 않는다. */
  i18nNames?: Record<string, string>;
  description?: string;
  protectedYn?: string;
  useYn: string;
  sortOrder?: number;
}

/** 폼에서 편집하는 다국어 한 행. locale 은 ko 를 제외한 지원 로케일 중 하나. */
export interface CodeItemI18nRow {
  locale: string;
  codeName: string;
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
  /**
   * 다국어 번역명 행 목록.
   * 변경 전: 영문 전용 `codeNameEn` 단일 문자열이었다.
   * 변경 후: locale 무관 행 목록으로 일반화해 지원 로케일이 늘면 코드 수정 없이 등록 가능하다.
   */
  i18nRows: CodeItemI18nRow[];
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

/**
 * 빈 상세 코드 폼을 새로 만든다.
 * i18nRows 가 배열이라 상수를 얕은 복사(`{ ...EMPTY }`)하면 배열 참조가 공유되어
 * 행 추가가 원본을 오염시킨다. 그래서 상수 대신 매번 새 객체를 만드는 팩토리로 둔다.
 */
function emptyItemForm(): CodeItemForm {
  return {
    id: null,
    groupCode: "",
    code: "",
    codeName: "",
    i18nRows: [],
    description: "",
    useYn: "Y",
  };
}

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
    i18nRows: toI18nRows(row?.i18nNames),
    description: row?.description ?? "",
    useYn: yn(row?.useYn),
  };
}

/** 서버 응답의 locale → 번역명 맵을 폼 행 목록으로 변환한다. ko 는 codeName 이 기준이라 제외한다. */
function toI18nRows(i18nNames?: Record<string, string>): CodeItemI18nRow[] {
  if (!i18nNames) return [];
  return Object.entries(i18nNames)
    .filter(([locale, codeName]) => locale && locale !== BASE_LOCALE && codeName)
    .map(([locale, codeName]) => ({ locale, codeName }));
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
  /* Spring @ModelAttribute Map 바인딩 형식: i18nNames[<locale>]=<번역명> */
  for (const row of form.i18nRows) {
    const locale = row.locale.trim();
    const codeName = row.codeName.trim();
    if (!locale || locale === BASE_LOCALE || !codeName) continue;
    fd.append(`i18nNames[${locale}]`, codeName);
  }
  fd.append("description", form.description.trim());
  fd.append("useYn", yn(form.useYn));
  return fd;
}

export const useCodeAdminStore = defineStore("codeAdmin", () => {
  const { t } = useLocaleStore();
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
  const itemForm = ref<CodeItemForm>(emptyItemForm());

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

      const pageResult = await getPage<CodeGroupRow>("/api/code/groups", { config: { params }, failureMessage: t("admin.code.group.list.load.failure") });
      rows.value = pageResult.content;
      totalElements.value = pageResult.totalElements;
      totalPages.value = pageResult.totalPages;
      currentPage.value = pageResult.number;
      pageSize.value = pageResult.size;
    } catch (e) {
      error.value = e instanceof Error ? e.message : t("admin.code.group.list.load.failure");
    } finally {
      loading.value = false;
    }
  }

  async function changePageSize(size: number) {
    pageSize.value = size;
    await fetchGroups(0);
  }

  async function openGroupCreate() {
    if (!await assertAuthenticatedBeforeModal()) return;
    groupForm.value = { ...EMPTY_GROUP_FORM };
    groupModalOpen.value = true;
  }

  async function openGroupEdit(id: number) {
    if (!await assertAuthenticatedBeforeModal()) return;
    groupSaving.value = false;
    const dto = await getObj<CodeGroupRow>(`/api/code/group/${id}`, { failureMessage: t("admin.code.group.load.failure") });
    groupForm.value = normalizeGroupForm(dto ?? {});
    groupModalOpen.value = true;
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
      const res = await apiPost(url, toGroupFormData(groupForm.value), {
        headers: { "Content-Type": "multipart/form-data" },
      });
      const message = unwrapOk(res, t("admin.code.group.save.failure")) || t("common.result.saved");
      closeGroupModal();
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
    const res = await apiPatch(`/api/code/group/${row.id}`, { useYn: nextUseYn });
    const message = unwrapOk(res, t("admin.code.group.use-yn.change.failure")) || t("common.result.changed");
    await fetchGroups(currentPage.value);
    if (detail.value?.id === row.id) await openDetail(row.id);
    return message;
  }

  /**
   * 코드 그룹 삭제 처리.
   * 변경 전에는 성공 직후 목록을 갱신하고 호출부가 알림을 띄웠다.
   * 변경 후에는 성공 알림 OK 이후 목록을 갱신한다.
   */
  async function deleteGroup(id: number) {
    const res = await apiDelete(`/api/code/group/${id}`);
    assertOk(res, t("admin.code.group.delete.failure"));
    if (detail.value?.id === id) closeDetail();
    const nextPage = rows.value.length <= 1 && currentPage.value > 0 ? currentPage.value - 1 : currentPage.value;
    const message = res.message ?? t("common.result.deleted");
    await swalAlert(message);
    await fetchGroups(nextPage);
    return message;
  }

  async function openDetail(id: number) {
    if (!await assertAuthenticatedBeforeModal()) return;
    detailOpen.value = true;
    detailLoading.value = true;
    try {
      const group = (await getObj<CodeGroupRow>(`/api/code/group/${id}`, { failureMessage: t("admin.code.group.detail.load.failure") })) ?? ({} as CodeGroupRow);
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
    items.value = await getList<CodeItemRow>("/api/code/items", { config: { params: { groupCode: targetGroupCode } }, failureMessage: t("admin.code.item.list.load.failure") });
  }

  async function openItemCreate() {
    if (!await assertAuthenticatedBeforeModal()) return;
    itemForm.value = normalizeItemForm(undefined, detail.value?.groupCode ?? "");
    itemModalOpen.value = true;
  }

  async function openItemEdit(id: number) {
    if (!await assertAuthenticatedBeforeModal()) return;
    const dto = await getObj<CodeItemRow>("/api/code/item", { config: { params: { id } }, failureMessage: t("admin.code.item.detail.load.failure") });
    itemForm.value = normalizeItemForm(dto ?? {}, detail.value?.groupCode ?? "");
    itemModalOpen.value = true;
  }

  function closeItemModal() {
    itemModalOpen.value = false;
    itemForm.value = emptyItemForm();
  }

  /**
   * 다국어 행 추가. 아직 쓰지 않은 로케일을 기본 선택한다.
   * 남은 로케일이 없으면 아무 것도 하지 않는다 (locale 은 code_item_i18n 복합 PK 라 중복 불가).
   */
  function addI18nRow() {
    const used = new Set(itemForm.value.i18nRows.map((row) => row.locale));
    const next = I18N_LOCALE_OPTIONS.find((locale) => !used.has(locale));
    if (!next) return;
    itemForm.value.i18nRows.push({ locale: next, codeName: "" });
  }

  /** 다국어 행 제거. 저장 시 해당 locale 번역은 삭제된다. */
  function removeI18nRow(index: number) {
    itemForm.value.i18nRows.splice(index, 1);
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
      const res = await apiPost(url, toItemFormData(itemForm.value), {
        headers: { "Content-Type": "multipart/form-data" },
      });
      assertOk(res, t("admin.code.item.save.failure"));
      const groupId = detail.value?.id;
      const groupCode = itemForm.value.groupCode;
      closeItemModal();
      const message = res.message ?? t("common.result.saved");
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
    const res = await apiDelete("/api/code/item", { params: { id } });
    const message = unwrapOk(res, t("admin.code.item.delete.failure")) || t("common.result.deleted");
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
      const res = await apiPut("/api/code/items/sort-orders", { sortOrders });
      const message = unwrapOk(res, t("admin.code.item.order.failure")) || t("common.result.sort-order-saved");
      await fetchItems(detail.value.groupCode);
      return message;
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
    addI18nRow,
    removeI18nRow,
    openItemCreate,
    openItemEdit,
    closeItemModal,
    submitItem,
    deleteItem,
    moveItem,
    saveItemSortOrders,
  };
});
