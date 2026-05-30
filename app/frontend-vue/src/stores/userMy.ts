import { computed, ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";

export interface UserMyRole {
  roleKey: string;
  roleName?: string;
}

export interface UserMyAllowedIp {
  allowedIp: string;
}

export interface UserMyProfile {
  userProfileId?: number | null;
  brthdy?: string | null;
  lunarYn?: string | null;
  proflCn?: string | null;
}

export interface UserMyEmplym {
  userNm?: string | null;
  emplymEmail?: string | null;
  emplymPhoneNumber?: string | null;
  cmpyNm?: string | null;
  teamNm?: string | null;
  emplymNm?: string | null;
  rankNm?: string | null;
  rankCd?: string | null;
  apntcYn?: string | null;
  ecnyDt?: string | null;
  retireYn?: string | null;
  retireDt?: string | null;
  acntBank?: string | null;
  acntNo?: string | null;
}

export interface UserMyUser {
  id?: number | null;
  username: string;
  nickname?: string | null;
  email?: string | null;
  phoneNumber?: string | null;
  profileImageUrl?: string | null;
  userRoles?: UserMyRole[];
  useAllowedIp?: boolean;
  useAllowedIpYn?: string | null;
  isAllowedIpY?: boolean;
  allowedIpList?: UserMyAllowedIp[];
  profile?: UserMyProfile | null;
  emplym?: UserMyEmplym | null;
}

export interface PasswordChangePayload {
  username: string;
  currPw: string;
  newPw: string;
}

const EMPTY_USER: UserMyUser = {
  username: "",
  userRoles: [],
  allowedIpList: [],
  profile: null,
  emplym: null,
};

export const useUserMyStore = defineStore("userMy", () => {
  const user = ref<UserMyUser>({ ...EMPTY_USER });
  const loading = ref(false);

  const hasAllowedIp = computed(() => {
    return user.value.isAllowedIpY === true || user.value.useAllowedIp === true || user.value.useAllowedIpYn === "Y";
  });

  async function fetchMyInfo() {
    loading.value = true;
    try {
      const res = await axios.get("/api/user/my");
      user.value = {
        ...EMPTY_USER,
        ...(res.data?.rsltObj ?? {}),
      };
    } finally {
      loading.value = false;
    }
  }

  async function uploadProfileImage(file: File) {
    const fd = new FormData();
    fd.append("fileGroup0", file);
    const res = await axios.post("/api/user/my/upload-profl-img", fd, {
      headers: { "Content-Type": "multipart/form-data" },
    });
    if (!res.data?.rslt) throw new Error(res.data?.message ?? "프로필 이미지를 변경하지 못했습니다.");
  }

  async function removeProfileImage() {
    const res = await axios.post("/api/user/my/remove-profl-img");
    if (!res.data?.rslt) throw new Error(res.data?.message ?? "프로필 이미지를 삭제하지 못했습니다.");
  }

  async function changePassword(payload: PasswordChangePayload) {
    const fd = new FormData();
    fd.append("username", payload.username);
    fd.append("currPw", payload.currPw);
    fd.append("newPw", payload.newPw);
    const res = await axios.post("/api/user/my/pw-chg", fd);
    if (!res.data?.rslt) throw new Error(res.data?.message ?? "비밀번호를 변경하지 못했습니다.");
  }

  return {
    user,
    loading,
    hasAllowedIp,
    fetchMyInfo,
    uploadProfileImage,
    removeProfileImage,
    changePassword,
  };
});
