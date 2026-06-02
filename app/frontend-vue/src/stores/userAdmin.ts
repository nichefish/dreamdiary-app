import { computed, ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";
import type { RoleRow } from "@/stores/adminPage";
import { swalAlert } from "@/utils/swal";

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
        axios.get("/api/admin/page/bootstrap"),
        axios.get("/api/code/items", { params: { groupCode: "CMPY_CD" } }),
        axios.get("/api/code/items", { params: { groupCode: "TEAM_CD" } }),
        axios.get("/api/code/items", { params: { groupCode: "EMPLYM_CD" } }),
        axios.get("/api/code/items", { params: { groupCode: "JOB_TITLE_CD" } }),
      ]);
      const payload = bootstrapRes.data?.rsltObj ?? {};
      roles.value = Array.isArray(payload.roleList) ? payload.roleList : [];
      cmpyOptions.value = Array.isArray(cmpyRes.data?.rsltList) ? cmpyRes.data.rsltList : [];
      teamOptions.value = Array.isArray(teamRes.data?.rsltList) ? teamRes.data.rsltList : [];
      emplymOptions.value = Array.isArray(emplymRes.data?.rsltList) ? emplymRes.data.rsltList : [];
      rankOptions.value = Array.isArray(rankRes.data?.rsltList) ? rankRes.data.rsltList : [];
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

      const res = await axios.get("/api/users", { params });
      if (!res.data?.rslt) throw new Error(res.data?.message ?? "계정 목록을 불러오지 못했습니다.");
      const pageResult = res.data?.rsltObj ?? {};
      rows.value = Array.isArray(pageResult.content) ? pageResult.content : [];
      totalElements.value = Number(pageResult.totalElements ?? 0);
      totalPages.value = Number(pageResult.totalPages ?? 0);
      currentPage.value = Number(pageResult.number ?? targetPage);
      pageSize.value = Number(pageResult.size ?? pageSize.value);
    } catch (e) {
      error.value = e instanceof Error ? e.message : "계정 목록을 불러오지 못했습니다.";
      rows.value = [];
      totalElements.value = 0;
      totalPages.value = 0;
    } finally {
      loading.value = false;
    }
  }

  async function changePageSize(size: number) {
    pageSize.value = size;
    await fetchUsers(0);
  }

  async function openDetail(id: number) {
    detailOpen.value = true;
    detailLoading.value = true;
    try {
      const res = await axios.get(`/api/users/${id}`);
      if (!res.data?.rslt) throw new Error(res.data?.message ?? "계정 상세를 불러오지 못했습니다.");
      detail.value = res.data?.rsltObj ?? null;
    } finally {
      detailLoading.value = false;
    }
  }

  function closeDetail() {
    detailOpen.value = false;
    detail.value = null;
  }

  function openCreate() {
    form.value = emptyForm();
    formOpen.value = true;
  }

  async function openEdit(id: number) {
    formOpen.value = true;
    saving.value = false;
    const res = await axios.get(`/api/users/${id}`);
    if (!res.data?.rslt) throw new Error(res.data?.message ?? "계정 상세를 불러오지 못했습니다.");
    form.value = normalizeForm(res.data?.rsltObj ?? {});
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
      const res = await axios.post(url, toFormData(form.value), {
        headers: { "Content-Type": "multipart/form-data" },
      });
      if (!res.data?.rslt) throw new Error(res.data?.message ?? "계정을 저장하지 못했습니다.");
      closeForm();
      const message = res.data?.message ?? "저장되었습니다.";
      await swalAlert(message);
      await fetchUsers(id == null ? 0 : currentPage.value);
      if (detail.value?.id === id) await openDetail(id);
      return message;
    } finally {
      saving.value = false;
    }
  }

  async function passwordReset(id: number) {
    const res = await axios.post(`/api/users/${id}/password-reset`);
    if (!res.data?.rslt) throw new Error(res.data?.message ?? "비밀번호를 초기화하지 못했습니다.");
    return res.data?.message ?? "비밀번호가 초기화되었습니다.";
  }

  /**
   * 계정 삭제 처리.
   * 변경 전에는 성공 직후 목록을 갱신하고 호출부가 알림을 띄웠다.
   * 변경 후에는 성공 알림 OK 이후 목록을 갱신한다.
   */
  async function deleteUser(id: number) {
    const res = await axios.delete(`/api/users/${id}`);
    if (!res.data?.rslt) throw new Error(res.data?.message ?? "계정을 삭제하지 못했습니다.");
    if (detail.value?.id === id) closeDetail();
    const nextPage = rows.value.length <= 1 && currentPage.value > 0 ? currentPage.value - 1 : currentPage.value;
    const message = res.data?.message ?? "삭제되었습니다.";
    await swalAlert(message);
    await fetchUsers(nextPage);
    return message;
  }

  async function usernameDuplicateCheck(username: string) {
    const res = await axios.get("/api/users/duplicate-check/username", { params: { username } });
    return { ok: !!res.data?.rslt, message: res.data?.message ?? "" };
  }

  async function emailDuplicateCheck(email: string) {
    const res = await axios.get("/api/users/duplicate-check/email", { params: { email } });
    return { ok: !!res.data?.rslt, message: res.data?.message ?? "" };
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
