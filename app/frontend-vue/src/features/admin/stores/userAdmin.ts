import { computed, ref } from "vue";
import { defineStore } from "pinia";
import { apiGet, apiPost, apiDelete, assertOk } from "@/shared/api/client";
import type { RoleRow } from "@/features/admin/stores/adminPage";
import { assertAuthenticatedBeforeModal } from "@/shared/auth/sessionPing";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { swalAlert, swalFire } from "@/shared/utils/swal";

export interface UserRoleRow {
  roleKey: string;
  roleName: string;
}

export interface UserAllowedIpRow {
  id?: number;
  allowedIp: string;
}

export interface CodeOption {
  code: string;
  codeName: string;
}

export interface UserProfile {
  userProfileId?: number | null;
  brthdy?: string;
  lunarYn?: string;
  proflCn?: string;
}

export interface UserEmplym {
  id?: number | null;
  userNm?: string;
  cmpyCd?: string;
  cmpyNm?: string;
  teamCd?: string;
  teamNm?: string;
  emplymCd?: string;
  emplymNm?: string;
  rankCd?: string;
  rankNm?: string;
  emplymEmail?: string;
  emplymEmailId?: string;
  emplymEmailDomain?: string;
  emplymPhoneNumber?: string;
  apntcYn?: string;
  ecnyDt?: string;
  retireYn?: string;
  retireDt?: string;
  acntBank?: string;
  acntNo?: string;
  emplymCn?: string;
}

export interface UserRow {
  id: number;
  rnum?: number;
  isMe?: boolean;
  username: string;
  nickname?: string;
  userNm?: string;
  email?: string;
  phoneNumber?: string;
  profileImageUrl?: string;
  userRoles?: UserRoleRow[];
  userProflYn?: string;
  retireYn?: string;
  cmpyNm?: string;
  teamNm?: string;
  rankNm?: string;
  apntcYn?: string;
  lockedYn?: string;
  isLocked?: boolean;
  createdBy?: string;
  createdAt?: string;
  useAllowedIp?: boolean;
  allowedIpList?: UserAllowedIpRow[];
  content?: string;
  profile?: UserProfile | null;
  emplym?: UserEmplym | null;
}

export interface UserForm {
  id: number | null;
  username: string;
  password: string;
  nickname: string;
  emailId: string;
  emailDomain: string;
  phoneNumber: string;
  roleKeyList: string[];
  useAllowedIp: boolean;
  allowedIpListStr: string;
  content: string;
  hasProfile: boolean;
  profile: {
    brthdy: string;
    lunarYn: boolean;
    proflCn: string;
  };
  hasEmplym: boolean;
  emplym: {
    userNm: string;
    cmpyCd: string;
    teamCd: string;
    emplymCd: string;
    rankCd: string;
    emplymEmailId: string;
    emplymEmailDomain: string;
    emplymPhoneNumber: string;
    apntcYn: boolean;
    ecnyDt: string;
    retireYn: boolean;
    retireDt: string;
    acntBank: string;
    acntNo: string;
    emplymCn: string;
  };
}

const EMPTY_FORM: UserForm = {
  id: null,
  username: "",
  password: "",
  nickname: "",
  emailId: "",
  emailDomain: "gmail.com",
  phoneNumber: "",
  roleKeyList: [],
  useAllowedIp: false,
  allowedIpListStr: "",
  content: "",
  hasProfile: false,
  profile: {
    brthdy: "",
    lunarYn: false,
    proflCn: "",
  },
  hasEmplym: false,
  emplym: {
    userNm: "",
    cmpyCd: "",
    teamCd: "",
    emplymCd: "",
    rankCd: "",
    emplymEmailId: "",
    emplymEmailDomain: "",
    emplymPhoneNumber: "",
    apntcYn: false,
    ecnyDt: "",
    retireYn: false,
    retireDt: "",
    acntBank: "",
    acntNo: "",
    emplymCn: "",
  },
};

function emptyForm(): UserForm {
  return {
    ...EMPTY_FORM,
    roleKeyList: [],
    profile: { ...EMPTY_FORM.profile },
    emplym: { ...EMPTY_FORM.emplym },
  };
}

function splitEmail(email?: string): { emailId: string; emailDomain: string } {
  const [emailId = "", emailDomain = ""] = String(email ?? "").split("@");
  return { emailId, emailDomain };
}

function roleKeys(row?: Partial<UserRow>): string[] {
  return (row?.userRoles ?? []).map((role) => role.roleKey).filter(Boolean);
}

function allowedIpListStr(row?: Partial<UserRow>): string {
  return (row?.allowedIpList ?? []).map((item) => item.allowedIp).filter(Boolean).join(",");
}

function normalizeForm(row?: Partial<UserRow>): UserForm {
  const email = splitEmail(row?.email);
  const emplymEmail = splitEmail(row?.emplym?.emplymEmail);
  return {
    id: row?.id ?? null,
    username: row?.username ?? "",
    password: "",
    nickname: row?.nickname ?? row?.userNm ?? "",
    emailId: email.emailId,
    emailDomain: email.emailDomain || "gmail.com",
    phoneNumber: row?.phoneNumber ?? "",
    roleKeyList: roleKeys(row),
    useAllowedIp: Boolean(row?.useAllowedIp),
    allowedIpListStr: allowedIpListStr(row),
    content: row?.content ?? "",
    hasProfile: row?.profile != null,
    profile: {
      brthdy: row?.profile?.brthdy ?? "",
      lunarYn: String(row?.profile?.lunarYn ?? "N").toUpperCase() === "Y",
      proflCn: row?.profile?.proflCn ?? "",
    },
    hasEmplym: row?.emplym != null,
    emplym: {
      userNm: row?.emplym?.userNm ?? row?.userNm ?? "",
      cmpyCd: row?.emplym?.cmpyCd ?? "",
      teamCd: row?.emplym?.teamCd ?? "",
      emplymCd: row?.emplym?.emplymCd ?? "",
      rankCd: row?.emplym?.rankCd ?? "",
      emplymEmailId: row?.emplym?.emplymEmailId ?? emplymEmail.emailId,
      emplymEmailDomain: row?.emplym?.emplymEmailDomain ?? emplymEmail.emailDomain,
      emplymPhoneNumber: row?.emplym?.emplymPhoneNumber ?? "",
      apntcYn: String(row?.emplym?.apntcYn ?? "N").toUpperCase() === "Y",
      ecnyDt: row?.emplym?.ecnyDt ?? "",
      retireYn: String(row?.emplym?.retireYn ?? "N").toUpperCase() === "Y",
      retireDt: row?.emplym?.retireDt ?? "",
      acntBank: row?.emplym?.acntBank ?? "",
      acntNo: row?.emplym?.acntNo ?? "",
      emplymCn: row?.emplym?.emplymCn ?? "",
    },
  };
}

function toFormData(form: UserForm): FormData {
  const fd = new FormData();
  if (form.id != null) fd.append("id", String(form.id));
  fd.append("username", form.username.trim());
  if (form.password.trim()) fd.append("password", form.password);
  fd.append("nickname", form.nickname.trim());
  fd.append("emailId", form.emailId.trim());
  fd.append("emailDomain", form.emailDomain.trim());
  fd.append("phoneNumber", form.phoneNumber.trim());
  fd.append("roleKeysStr", form.roleKeyList.join(","));
  fd.append("useAllowedIp", String(form.useAllowedIp));
  fd.append("useAllowedIpYn", form.useAllowedIp ? "Y" : "N");
  fd.append("allowedIpListStr", form.useAllowedIp ? form.allowedIpListStr.trim() : "");
  fd.append("content", form.content.trim());
  if (form.hasProfile) {
    fd.append("profile.brthdy", form.profile.brthdy);
    fd.append("profile.lunarYn", form.profile.lunarYn ? "Y" : "N");
    fd.append("profile.proflCn", form.profile.proflCn.trim());
  }
  if (form.hasEmplym) {
    fd.append("emplym.userNm", form.emplym.userNm.trim());
    fd.append("emplym.cmpyCd", form.emplym.cmpyCd);
    fd.append("emplym.teamCd", form.emplym.teamCd);
    fd.append("emplym.emplymCd", form.emplym.emplymCd);
    fd.append("emplym.rankCd", form.emplym.rankCd);
    fd.append("emplym.emplymEmailId", form.emplym.emplymEmailId.trim());
    fd.append("emplym.emplymEmailDomain", form.emplym.emplymEmailDomain.trim());
    fd.append("emplym.emplymPhoneNumber", form.emplym.emplymPhoneNumber.trim());
    fd.append("emplym.apntcYn", form.emplym.apntcYn ? "Y" : "N");
    fd.append("emplym.ecnyDt", form.emplym.ecnyDt);
    fd.append("emplym.retireYn", form.emplym.retireYn ? "Y" : "N");
    fd.append("emplym.retireDt", form.emplym.retireDt);
    fd.append("emplym.acntBank", form.emplym.acntBank.trim());
    fd.append("emplym.acntNo", form.emplym.acntNo.trim());
    fd.append("emplym.emplymCn", form.emplym.emplymCn.trim());
  }
  return fd;
}

export const useUserAdminStore = defineStore("userAdmin", () => {
  const { t } = useLocaleStore();
  const rows = ref<UserRow[]>([]);
  const totalElements = ref(0);
  const totalPages = ref(0);
  const currentPage = ref(0);
  const pageSize = ref(10);
  const keyword = ref("");
  const roleKey = ref("");
  const loading = ref(false);
  const error = ref("");

  const roles = ref<RoleRow[]>([]);
  const cmpyOptions = ref<CodeOption[]>([]);
  const teamOptions = ref<CodeOption[]>([]);
  const emplymOptions = ref<CodeOption[]>([]);
  const rankOptions = ref<CodeOption[]>([]);
  const bootstrapLoading = ref(false);

  const detailOpen = ref(false);
  const detailLoading = ref(false);
  const detail = ref<UserRow | null>(null);

  const formOpen = ref(false);
  const saving = ref(false);
  const form = ref<UserForm>(emptyForm());

  const isEdit = computed(() => form.value.id != null);
  const activeRoles = computed(() => roles.value.filter((role) => String(role.useYn ?? "Y").toUpperCase() === "Y"));

  async function fetchBootstrap() {
    bootstrapLoading.value = true;
    try {
      const [bootstrapRes, cmpyRes, teamRes, emplymRes, rankRes] = await Promise.all([
        apiGet<{ roleList?: RoleRow[] }>("/api/admin/page/bootstrap"),
        apiGet<CodeOption>("/api/code/items", { params: { groupCode: "CMPY_CD" } }),
        apiGet<CodeOption>("/api/code/items", { params: { groupCode: "TEAM_CD" } }),
        apiGet<CodeOption>("/api/code/items", { params: { groupCode: "EMPLYM_CD" } }),
        apiGet<CodeOption>("/api/code/items", { params: { groupCode: "JOB_TITLE_CD" } }),
      ]);
      const payload = bootstrapRes.rsltObj ?? {};
      roles.value = Array.isArray(payload.roleList) ? payload.roleList : [];
      cmpyOptions.value = Array.isArray(cmpyRes.rsltList) ? cmpyRes.rsltList : [];
      teamOptions.value = Array.isArray(teamRes.rsltList) ? teamRes.rsltList : [];
      emplymOptions.value = Array.isArray(emplymRes.rsltList) ? emplymRes.rsltList : [];
      rankOptions.value = Array.isArray(rankRes.rsltList) ? rankRes.rsltList : [];
    } finally {
      bootstrapLoading.value = false;
    }
  }

  async function fetchUsers(page?: number) {
    loading.value = true;
    error.value = "";
    const targetPage = page ?? currentPage.value;
    try {
      const params: Record<string, unknown> = {
        page: targetPage,
        size: pageSize.value,
      };
      if (keyword.value.trim()) {
        params.searchType = "username";
        params.searchKeyword = keyword.value.trim();
      }
      if (roleKey.value) params.roleKey = roleKey.value;

      const res = await apiGet<{
        content?: UserRow[];
        totalElements?: number;
        totalPages?: number;
        number?: number;
        size?: number;
      }>("/api/users", { params });
      assertOk(res, t("user.admin.list.load.failure"));
      const pageResult = res.rsltObj ?? {};
      rows.value = Array.isArray(pageResult.content) ? pageResult.content : [];
      totalElements.value = Number(pageResult.totalElements ?? 0);
      totalPages.value = Number(pageResult.totalPages ?? 0);
      currentPage.value = Number(pageResult.number ?? targetPage);
      pageSize.value = Number(pageResult.size ?? pageSize.value);
    } catch (e) {
      error.value = e instanceof Error ? e.message : t("user.admin.list.load.failure");
    } finally {
      loading.value = false;
    }
  }

  async function changePageSize(size: number) {
    pageSize.value = size;
    await fetchUsers(0);
  }

  async function openDetail(id: number) {
    if (!await assertAuthenticatedBeforeModal()) return;
    detailOpen.value = true;
    detailLoading.value = true;
    try {
      const res = await apiGet<UserRow>(`/api/users/${id}`);
      assertOk(res, t("user.admin.detail.load.failure"));
      detail.value = res.rsltObj ?? null;
    } finally {
      detailLoading.value = false;
    }
  }

  function closeDetail() {
    detailOpen.value = false;
    detail.value = null;
  }

  async function openCreate() {
    if (!await assertAuthenticatedBeforeModal()) return;
    form.value = emptyForm();
    formOpen.value = true;
  }

  async function openEdit(id: number) {
    if (!await assertAuthenticatedBeforeModal()) return;
    saving.value = false;
    const res = await apiGet<UserRow>(`/api/users/${id}`);
    assertOk(res, t("user.admin.detail.load.failure"));
    form.value = normalizeForm(res.rsltObj ?? {});
    formOpen.value = true;
  }

  function closeForm() {
    formOpen.value = false;
    form.value = emptyForm();
  }

  /**
   * 계정 등록/수정 처리.
   * 변경 전에는 성공 직후 목록·상세를 갱신하고 호출부가 알림을 띄웠다.
   * 변경 후에는 성공 알림 OK 이후 목록·상세를 갱신한다.
   */
  async function submit() {
    saving.value = true;
    try {
      const id = form.value.id;
      const url = id != null ? `/api/users/${id}` : "/api/users";
      const res = await apiPost(url, toFormData(form.value), {
        headers: { "Content-Type": "multipart/form-data" },
      });
      assertOk(res, t("user.admin.save.failure"));
      closeForm();
      const message = res.message ?? t("common.result.saved");
      await swalFire({ icon: "success", text: message });
      await fetchUsers(id == null ? 0 : currentPage.value);
      if (detail.value?.id === id) await openDetail(id);
      return message;
    } finally {
      saving.value = false;
    }
  }

  async function passwordReset(id: number) {
    const res = await apiPost(`/api/users/${id}/password-reset`);
    assertOk(res, t("user.admin.reset-password.failure"));
    return res.message ?? t("user.admin.reset-password.success");
  }

  /**
   * 계정 삭제 처리.
   * 변경 전에는 성공 직후 목록을 갱신하고 호출부가 알림을 띄웠다.
   * 변경 후에는 성공 알림 OK 이후 목록을 갱신한다.
   */
  async function deleteUser(id: number) {
    const res = await apiDelete(`/api/users/${id}`);
    assertOk(res, t("user.admin.delete.failure"));
    if (detail.value?.id === id) closeDetail();
    const nextPage = rows.value.length <= 1 && currentPage.value > 0 ? currentPage.value - 1 : currentPage.value;
    const message = res.message ?? t("common.result.deleted");
    await swalFire({ icon: "success", text: message });
    await fetchUsers(nextPage);
    return message;
  }

  async function usernameDuplicateCheck(username: string) {
    const res = await apiGet("/api/users/duplicate-check/username", { params: { username } });
    return { ok: !!res.rslt, message: res.message ?? "" };
  }

  async function emailDuplicateCheck(email: string) {
    const res = await apiGet("/api/users/duplicate-check/email", { params: { email } });
    return { ok: !!res.rslt, message: res.message ?? "" };
  }

  return {
    rows,
    totalElements,
    totalPages,
    currentPage,
    pageSize,
    keyword,
    roleKey,
    loading,
    error,
    roles,
    cmpyOptions,
    teamOptions,
    emplymOptions,
    rankOptions,
    bootstrapLoading,
    detailOpen,
    detailLoading,
    detail,
    formOpen,
    saving,
    form,
    isEdit,
    activeRoles,
    fetchBootstrap,
    fetchUsers,
    changePageSize,
    openDetail,
    closeDetail,
    openCreate,
    openEdit,
    closeForm,
    submit,
    passwordReset,
    deleteUser,
    usernameDuplicateCheck,
    emailDuplicateCheck,
  };
});
