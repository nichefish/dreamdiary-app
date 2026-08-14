import { computed, ref } from "vue";
import { defineStore } from "pinia";
import { apiGet, apiPost, apiPut, assertOk } from "@/shared/api/client";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

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

/** 로그인 사용자가 직접 수정할 수 있는 개인 프로필 필드. */
export interface UserMyUpdatePayload {
  nickname: string;
  phoneNumber: string | null;
  brthdy: string | null;
  lunarYn: "Y" | "N";
  proflCn: string | null;
}

const EMPTY_USER: UserMyUser = {
  username: "",
  userRoles: [],
  allowedIpList: [],
  profile: null,
  emplym: null,
};

export const useUserMyStore = defineStore("userMy", () => {
  const { t } = useLocaleStore();
  const user = ref<UserMyUser>({ ...EMPTY_USER });
  const loading = ref(false);
  const saving = ref(false);

  const hasAllowedIp = computed(() => {
    return user.value.isAllowedIpY === true || user.value.useAllowedIp === true || user.value.useAllowedIpYn === "Y";
  });

  async function fetchMyInfo() {
    loading.value = true;
    try {
      const res = await apiGet<Partial<typeof EMPTY_USER>>("/api/user/my");
      user.value = {
        ...EMPTY_USER,
        ...(res.rsltObj ?? {}),
      };
    } finally {
      loading.value = false;
    }
  }

  /**
   * 로그인 사용자의 개인 프로필 허용 필드만 저장하고 서버 확정 상태를 다시 조회한다.
   * 이메일·계정·권한·허용 IP·재직 정보는 payload 타입과 API 계약에서 제외한다.
   */
  async function updateMyInfo(payload: UserMyUpdatePayload) {
    saving.value = true;
    try {
      const res = await apiPut("/api/user/my", payload);
      assertOk(res, t("user.my.profile.update.failure"));
      await fetchMyInfo();
    } finally {
      saving.value = false;
    }
  }

  async function uploadProfileImage(file: File) {
    const fd = new FormData();
    fd.append("fileGroup0", file);
    const res = await apiPost("/api/user/my/upload-profl-img", fd, {
      headers: { "Content-Type": "multipart/form-data" },
    });
    assertOk(res, t("user.my.profile-image.change.failure"));
  }

  async function removeProfileImage() {
    const res = await apiPost("/api/user/my/remove-profl-img");
    assertOk(res, t("user.my.profile-image.delete.failure"));
  }

  async function changePassword(payload: PasswordChangePayload) {
    const fd = new FormData();
    fd.append("username", payload.username);
    fd.append("currPw", payload.currPw);
    fd.append("newPw", payload.newPw);
    const res = await apiPost("/api/user/my/pw-chg", fd);
    assertOk(res, t("user.my.pw-change.failure"));
  }

  return {
    user,
    loading,
    saving,
    hasAllowedIp,
    fetchMyInfo,
    updateMyInfo,
    uploadProfileImage,
    removeProfileImage,
    changePassword,
  };
});
