import { ref } from "vue";
import { defineStore } from "pinia";
import ApiService from "@metronic/core/services/ApiService";
import type { AxiosError } from "axios";
import { resolveProfileImageUrl } from "@/shared/utils/profileImage";
import { preloadCategoryMaps, useJournalModalStore } from "@/features/journal/stores/journalModal";
import { useMenuStore } from "@/shared/menu/stores/menu";

/**
 * Vue SPA 인증 사용자 정보.
 * Spring Boot AuthInfo와 대응.
 */
export interface AuthUser {
  username: string;
  nickname: string;
  email: string;
  profileImageUrl: string;
  roles: { roleKey: string }[];
  isMngr: boolean;
  isDev: boolean;
}

/** Vue SPA 로그인 실패 후 추가 조치가 필요한 상태. */
export interface LoginActionState {
  username: string;
  isCredentialExpired?: boolean;
  isDupIdLogin?: boolean;
  needsPasswordReset?: boolean;
  passwordToken?: string;
}

/**
 * useAuthStore
 * Spring Boot JWT 쿠키 기반 인증 상태를 관리한다.
 * - 로그인: POST /api/auth/login (JSON) → JWT HttpOnly 쿠키 발급
 * - 인증 확인: GET /api/auth/get-auth-account → 현재 쿠키로 사용자 정보 조회
 * - 로그아웃: POST /api/auth/logout-json → 쿠키 삭제
 */
export const useAuthStore = defineStore("auth", () => {
  const user = ref<AuthUser | null>(null);
  const isAuthenticated = ref(false);
  const errors = ref<string[]>([]);
  const loginAction = ref<LoginActionState | null>(null);

  /** 인증 상태 세팅 */
  function setAuth(authUser: AuthUser) {
    isAuthenticated.value = true;
    user.value = {
      ...authUser,
      profileImageUrl: resolveProfileImageUrl(authUser.profileImageUrl),
    };
    errors.value = [];
    loginAction.value = null;
    void preloadCategoryMaps();
  }

  /** 인증 상태 초기화 */
  function purgeAuth() {
    useJournalModalStore().resetCategoryMaps();
    useMenuStore().resetMenu();
    isAuthenticated.value = false;
    user.value = null;
    errors.value = [];
  }

  /** 로그인 실패 응답에 포함된 후속 조치 상태를 추출한다. */
  function setLoginAction(rsltMap: unknown) {
    if (!rsltMap || typeof rsltMap !== "object" || Array.isArray(rsltMap)) {
      loginAction.value = null;
      return;
    }
    const map = rsltMap as Record<string, unknown>;
    const username = typeof map.username === "string" ? map.username : "";
    const isCredentialExpired = map.isCredentialExpired === true;
    const isDupIdLogin = map.isDupIdLogin === true;
    const needsPasswordReset = map.needsPasswordReset === true;
    if (!username || (!isCredentialExpired && !isDupIdLogin && !needsPasswordReset)) {
      loginAction.value = null;
      return;
    }
    loginAction.value = {
      username,
      isCredentialExpired,
      isDupIdLogin,
      needsPasswordReset,
      passwordToken: typeof map.passwordToken === "string" ? map.passwordToken : undefined,
    };
  }

  /**
   * 로그인.
   * POST /api/auth/login → DreamdiaryAuthenticationProvider가 인증 + JWT 쿠키 발급.
   * 실패 시 서버가 HTTP 401을 반환하므로 Axios AxiosError로 잡아 message 추출.
   * 성공 후 verifyAuth()로 사용자 정보 로드.
   */
  async function login(credentials: { username: string; password: string }) {
    errors.value = [];
    loginAction.value = null;
    try {
      const { data } = await ApiService.post("/api/auth/login", credentials);
      if (data.rslt) {
        await verifyAuth();
      } else {
        errors.value = [data.message ?? "로그인에 실패했습니다."];
        setLoginAction(data.rsltMap);
        throw new Error(data.message);
      }
    } catch (e) {
      const axiosErr = e as AxiosError<{ message?: string; rsltMap?: unknown }>;
      const serverData = axiosErr.response?.data;
      if (!serverData) throw e;
      const serverMsg = serverData?.message;
      errors.value = [serverMsg ?? "로그인에 실패했습니다."];
      setLoginAction(serverData?.rsltMap);
      throw e;
    }
  }

  /**
   * 로그아웃.
   * POST /api/auth/logout-json → 서버 쿠키 삭제 + 클라이언트 상태 초기화.
   */
  async function logout() {
    try {
      await ApiService.post("/api/auth/logout-json", {});
    } finally {
      purgeAuth();
    }
  }

  /**
   * 현재 JWT 쿠키로 인증 상태를 검증하고 사용자 정보를 로드한다.
   * 라우터 beforeEach에서 매 페이지 진입 전 호출.
   * 서버 응답: AjaxResponse.withObj() → JSON 필드명 rsltObj (ServiceResponse 기준)
   */
  async function verifyAuth() {
    try {
      const { data } = await ApiService.get("/api/auth/get-auth-account");
      if (data.rslt && data.rsltObj) {
        setAuth(data.rsltObj as AuthUser);
      } else {
        purgeAuth();
      }
    } catch {
      purgeAuth();
    }
  }

  return {
    user,
    isAuthenticated,
    errors,
    loginAction,
    login,
    logout,
    verifyAuth,
    purgeAuth,
  };
});
