import { computed, ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";
import { assertAuthenticatedBeforeModal } from "@/shared/auth/sessionPing";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { swalConfirm, swalAlert } from "@/shared/utils/swal";

export interface UserGroupRow {
  id: number;
  groupKey: string;
  groupName: string;
  description?: string;
  sortOrder?: number;
  useYn: string;
  memberCount?: number;
}

export interface PermissionOption {
  id: number;
  permKey: string;
  permName?: string;
  description?: string;
}

export interface UserGroupForm {
  id: number | null;
  groupKey: string;
  groupName: string;
  description: string;
  sortOrder: number;
  useYn: string;
  permissionKeys: string[];
  memberUsernames: string[];
  memberInput: string;
}

const EMPTY_FORM: UserGroupForm = {
  id: null,
  groupKey: "",
  groupName: "",
  description: "",
  sortOrder: 0,
  useYn: "Y",
  permissionKeys: [],
  memberUsernames: [],
  memberInput: "",
};

export const useUserGroupStore = defineStore("userGroup", () => {
  const { t } = useLocaleStore();
  const rows = ref<UserGroupRow[]>([]);
  const totalElements = ref(0);
  const totalPages = ref(0);
  const currentPage = ref(0);
  const pageSize = ref(10);
  const loading = ref(false);
  const saving = ref(false);
  const error = ref("");
  const keyword = ref("");
  const permissions = ref<PermissionOption[]>([]);

  const modalOpen = ref(false);
  const detailLoading = ref(false);
  const form = ref<UserGroupForm>({ ...EMPTY_FORM });

  const isEdit = computed(() => form.value.id != null);

  async function fetchPermissions() {
    const { data } = await axios.get("/api/permissions");
    permissions.value = Array.isArray(data?.rsltList) ? data.rsltList : [];
  }

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
      const { data } = await axios.get("/api/user/groups", { params });
      const pageObj = data?.rsltObj;
      rows.value = Array.isArray(pageObj?.content) ? pageObj.content : [];
      totalElements.value = Number(pageObj?.totalElements ?? 0);
      totalPages.value = Number(pageObj?.totalPages ?? 0);
      currentPage.value = Number(pageObj?.number ?? targetPage);
    } catch (e: unknown) {
      error.value = t("common.result.failure");
      rows.value = [];
    } finally {
      loading.value = false;
    }
  }

  async function openCreate() {
    if (!(await assertAuthenticatedBeforeModal())) return;
    form.value = { ...EMPTY_FORM, permissionKeys: [], memberUsernames: [] };
    modalOpen.value = true;
    if (!permissions.value.length) await fetchPermissions();
  }

  async function openEdit(id: number) {
    if (!(await assertAuthenticatedBeforeModal())) return;
    detailLoading.value = true;
    modalOpen.value = true;
    try {
      if (!permissions.value.length) await fetchPermissions();
      const { data } = await axios.get(`/api/user/groups/${id}`);
      const dto = data?.rsltObj ?? {};
      form.value = {
        id: dto.id ?? id,
        groupKey: dto.groupKey ?? "",
        groupName: dto.groupName ?? "",
        description: dto.description ?? "",
        sortOrder: Number(dto.sortOrder ?? 0),
        useYn: String(dto.useYn ?? "Y").toUpperCase() === "Y" ? "Y" : "N",
        permissionKeys: Array.isArray(dto.permissionKeys) ? [...dto.permissionKeys] : [],
        memberUsernames: Array.isArray(dto.memberUsernames) ? [...dto.memberUsernames] : [],
        memberInput: "",
      };
    } catch {
      modalOpen.value = false;
      await swalAlert(t("common.result.failure"));
    } finally {
      detailLoading.value = false;
    }
  }

  function closeModal() {
    modalOpen.value = false;
  }

  function togglePermission(permKey: string) {
    const keys = form.value.permissionKeys;
    const idx = keys.indexOf(permKey);
    if (idx >= 0) keys.splice(idx, 1);
    else keys.push(permKey);
  }

  function addMember() {
    const name = form.value.memberInput.trim();
    if (!name) return;
    if (!form.value.memberUsernames.includes(name)) {
      form.value.memberUsernames.push(name);
    }
    form.value.memberInput = "";
  }

  function removeMember(username: string) {
    form.value.memberUsernames = form.value.memberUsernames.filter((u) => u !== username);
  }

  async function save() {
    if (!form.value.groupKey.trim() || !form.value.groupName.trim()) {
      await swalAlert(t("common.validation.required"));
      return;
    }
    saving.value = true;
    try {
      const payload = {
        groupKey: form.value.groupKey.trim(),
        groupName: form.value.groupName.trim(),
        description: form.value.description.trim(),
        sortOrder: form.value.sortOrder,
        useYn: form.value.useYn,
        permissionKeys: [...form.value.permissionKeys],
        memberUsernames: [...form.value.memberUsernames],
      };
      if (form.value.id == null) {
        await axios.post("/api/user/groups", payload);
      } else {
        await axios.put(`/api/user/groups/${form.value.id}`, payload);
      }
      modalOpen.value = false;
      await fetchList(form.value.id == null ? 0 : currentPage.value);
      await swalAlert(t("common.result.success"));
    } catch {
      await swalAlert(t("common.result.failure"));
    } finally {
      saving.value = false;
    }
  }

  async function remove(id: number) {
    const ok = await swalConfirm(t("common.delete.confirm"));
    if (!ok) return;
    try {
      await axios.delete(`/api/user/groups/${id}`);
      await fetchList(currentPage.value);
      await swalAlert(t("common.result.success"));
    } catch {
      await swalAlert(t("common.result.failure"));
    }
  }

  return {
    rows,
    totalElements,
    totalPages,
    currentPage,
    pageSize,
    loading,
    saving,
    error,
    keyword,
    permissions,
    modalOpen,
    detailLoading,
    form,
    isEdit,
    fetchList,
    fetchPermissions,
    openCreate,
    openEdit,
    closeModal,
    togglePermission,
    addMember,
    removeMember,
    save,
    remove,
  };
});
