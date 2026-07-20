import { computed, ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";
import { assertAuthenticatedBeforeModal } from "@/shared/auth/sessionPing";
import { BASE_LOCALE, SUPPORTED_LOCALES, useLocaleStore } from "@/shared/i18n/stores/locale";
import { swalAlert } from "@/shared/utils/swal";

/**
 * 메뉴 다국어 입력에서 고를 수 있는 로케일.
 * 기준 로케일(ko)의 메뉴명/설명은 menu.menu_name/menu_description 이 단일 원천이라 제외한다.
 * SUPPORTED_LOCALES 에 로케일을 추가하면 코드 수정 없이 여기에 자동 반영된다.
 */
export const MENU_I18N_LOCALE_OPTIONS: readonly string[] = SUPPORTED_LOCALES.filter((locale) => locale !== BASE_LOCALE);

/** 서버 응답/폼에서 다루는 메뉴 다국어 한 행 (menu_i18n 한 행에 대응). */
export interface MenuI18nRow {
  locale: string;
  menuName: string;
  menuDescription?: string;
}

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
  menuDescription?: string;
  icon?: string;
  unreadCntNm?: string;
  url?: string;
  protectedYn?: string;
  sidebarVisibleYn?: string;
  submenuExpandType?: string;
  submenuExpandTypeName?: string;
  upperMenuNm?: string;
  subMenuList?: MenuNode[];
  /** 관리 상세 조회에만 포함되는 locale 별 번역 목록 (ko 제외). 사이드바 응답에는 없다. */
  i18nList?: MenuI18nRow[];
}

export interface MenuForm {
  id: number | null;
  parentMenuId: number | null;
  upperMenuNm: string;
  menuName: string;
  menuLabel: string;
  menuDescription: string;
  /** 다국어 번역 행 목록 (locale 별 메뉴명/설명). ko 는 위 menuName/menuDescription 이 기준이라 제외. */
  i18nRows: MenuI18nRow[];
  icon: string;
  unreadCntEnabled: boolean;
  unreadCntNm: string;
  submenuExpandType: string;
  url: string;
  useYn: string;
  sidebarVisibleYn: string;
}

export interface SubmenuExpandOption {
  code: string;
  codeName: string;
}

export type MenuTargetMode = "USER" | "MNGR";

/**
 * 빈 메뉴 폼을 새로 만든다.
 * i18nRows 가 배열이라 상수를 얕은 복사(`{ ...EMPTY }`)하면 배열 참조가 공유되어
 * 행 추가가 다른 폼 인스턴스를 오염시킨다. 그래서 상수 대신 매번 새 객체를 만드는 팩토리로 둔다.
 */
function emptyForm(): MenuForm {
  return {
    id: null,
    parentMenuId: null,
    upperMenuNm: "",
    menuName: "",
    menuLabel: "",
    menuDescription: "",
    i18nRows: [],
    icon: "",
    unreadCntEnabled: false,
    unreadCntNm: "",
    submenuExpandType: "NO_SUB",
    url: "",
    useYn: "Y",
    sidebarVisibleYn: "Y",
  };
}

function yn(value: string | undefined): string {
  return String(value ?? "N").toUpperCase() === "Y" ? "Y" : "N";
}

/** 서버 응답의 i18nList 를 폼 행 목록으로 정규화한다. ko 는 menuName 기준이라 제외, 빈 값 방어. */
function toI18nRows(i18nList?: MenuI18nRow[]): MenuI18nRow[] {
  if (!Array.isArray(i18nList)) return [];
  return i18nList
    .filter((row) => row && row.locale && row.locale !== BASE_LOCALE && row.menuName)
    .map((row) => ({ locale: row.locale, menuName: row.menuName, menuDescription: row.menuDescription ?? "" }));
}

function cloneForm(row?: Partial<MenuNode>): MenuForm {
  return {
    id: row?.id ?? null,
    parentMenuId: row?.parentMenuId ?? null,
    upperMenuNm: row?.upperMenuNm ?? "",
    menuName: row?.menuName ?? "",
    menuLabel: row?.menuLabel ?? "",
    menuDescription: row?.menuDescription ?? "",
    i18nRows: toI18nRows(row?.i18nList),
    icon: row?.icon ?? "",
    unreadCntEnabled: Boolean(row?.unreadCntNm),
    unreadCntNm: row?.unreadCntNm ?? "",
    submenuExpandType: row?.submenuExpandType ?? "NO_SUB",
    url: row?.url ?? "",
    useYn: yn(row?.useYn) === "Y" ? "Y" : "N",
    sidebarVisibleYn: yn(row?.sidebarVisibleYn ?? "Y") === "Y" ? "Y" : "N",
  };
}

function toFormData(form: MenuForm): FormData {
  const fd = new FormData();
  if (form.id != null) fd.append("id", String(form.id));
  if (form.parentMenuId != null) fd.append("parentMenuId", String(form.parentMenuId));
  fd.append("menuName", form.menuName.trim());
  fd.append("menuLabel", form.menuLabel.trim());
  fd.append("menuDescription", form.menuDescription.trim());
  /* Spring 인덱스 리스트 바인딩: i18nList[n].locale / .menuName / .menuDescription. 빈 번역명 행·ko 는 제외. */
  let i18nIdx = 0;
  for (const row of form.i18nRows) {
    const locale = row.locale.trim();
    const name = row.menuName.trim();
    if (!locale || locale === BASE_LOCALE || !name) continue;
    fd.append(`i18nList[${i18nIdx}].locale`, locale);
    fd.append(`i18nList[${i18nIdx}].menuName`, name);
    fd.append(`i18nList[${i18nIdx}].menuDescription`, (row.menuDescription ?? "").trim());
    i18nIdx++;
  }
  fd.append("icon", form.icon.trim());
  fd.append("unreadCntNm", form.unreadCntEnabled ? form.unreadCntNm.trim() : "");
  fd.append("submenuExpandType", form.submenuExpandType);
  fd.append("url", form.submenuExpandType === "NO_SUB" ? form.url.trim() : "");
  fd.append("useYn", yn(form.useYn));
  fd.append("sidebarVisibleYn", yn(form.sidebarVisibleYn ?? "Y"));
  return fd;
}

function getMenuTargetMode(row: MenuNode): MenuTargetMode {
  return yn(row.adminYn) === "Y" ? "MNGR" : "USER";
}

export const useMenuAdminStore = defineStore("menuAdmin", () => {
  const { t } = useLocaleStore();
  const rows = ref<MenuNode[]>([]);
  const loading = ref(false);
  const error = ref("");
  const saving = ref(false);
  const sortSaving = ref(false);
  const modalOpen = ref(false);
  const form = ref<MenuForm>(emptyForm());
  const submenuExpandOptions = computed<SubmenuExpandOption[]>(() => [
    { code: "EXTEND", codeName: t("enum.submenu-expand-type.extend") },
    { code: "LIST", codeName: t("enum.submenu-expand-type.list") },
    { code: "NO_SUB", codeName: t("enum.submenu-expand-type.no_sub") },
    { code: "COLLAPSE", codeName: t("enum.submenu-expand-type.collapse") },
    { code: "BOARD", codeName: t("enum.submenu-expand-type.board") },
  ]);

  const isEdit = computed(() => form.value.id != null);

  async function fetchTree() {
    loading.value = true;
    error.value = "";
    try {
      const res = await axios.get("/api/menu/menu-main-list");
      if (!res.data?.rslt) throw new Error(res.data?.message ?? t("admin.menu.list.load.failure"));
      rows.value = Array.isArray(res.data?.rsltList) ? res.data.rsltList : [];
    } catch (e) {
      error.value = e instanceof Error ? e.message : t("admin.menu.list.load.failure");
      rows.value = [];
    } finally {
      loading.value = false;
    }
  }

  async function openSubCreate(parent: MenuNode) {
    if (!await assertAuthenticatedBeforeModal()) return;
    form.value = {
      ...emptyForm(),
      parentMenuId: parent.id,
      upperMenuNm: parent.menuName ?? "",
    };
    modalOpen.value = true;
  }

  async function openEdit(id: number) {
    if (!await assertAuthenticatedBeforeModal()) return;
    saving.value = false;
    const res = await axios.get(`/api/menu/${id}`);
    if (!res.data?.rslt) throw new Error(res.data?.message ?? t("admin.menu.detail.load.failure"));
    form.value = cloneForm(res.data?.rsltObj ?? {});
    modalOpen.value = true;
  }

  function closeModal() {
    modalOpen.value = false;
    form.value = emptyForm();
  }

  /**
   * 다국어 행 추가. 아직 쓰지 않은 로케일을 기본 선택한다.
   * 남은 로케일이 없으면 아무 것도 하지 않는다 (locale 은 menu_i18n 복합 PK 라 중복 불가).
   */
  function addI18nRow() {
    const used = new Set(form.value.i18nRows.map((row) => row.locale));
    const next = MENU_I18N_LOCALE_OPTIONS.find((locale) => !used.has(locale));
    if (!next) return;
    form.value.i18nRows.push({ locale: next, menuName: "", menuDescription: "" });
  }

  /** 다국어 행 제거. 저장 시 해당 locale 번역은 삭제된다. */
  function removeI18nRow(index: number) {
    form.value.i18nRows.splice(index, 1);
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
      if (!res.data?.rslt) throw new Error(res.data?.message ?? t("admin.menu.save.failure"));
      closeModal();
      const message = res.data?.message ?? t("common.result.saved");
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
    if (!res.data?.rslt) throw new Error(res.data?.message ?? t("admin.menu.status.change.failure"));
    await fetchTree();
    return res.data?.message ?? t("common.result.changed");
  }

  /**
   * 메뉴 삭제 처리.
   * 변경 전에는 성공 직후 트리를 갱신하고 호출부가 알림을 띄웠다.
   * 변경 후에는 성공 알림 OK 이후 트리를 갱신한다.
   */
  async function deleteMenu(id: number) {
    const res = await axios.delete(`/api/menu/${id}`);
    if (!res.data?.rslt) throw new Error(res.data?.message ?? t("admin.menu.delete.failure"));
    const message = res.data?.message ?? t("common.result.deleted");
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
      if (!res.data?.rslt) throw new Error(res.data?.message ?? t("admin.menu.main-order.failure"));
      await fetchTree();
      return res.data?.message ?? t("common.result.sort-order-saved");
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
      if (!res.data?.rslt) throw new Error(res.data?.message ?? t("admin.menu.submenu-order.failure"));
      await fetchTree();
      return res.data?.message ?? t("common.result.sort-order-saved");
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
    addI18nRow,
    removeI18nRow,
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
