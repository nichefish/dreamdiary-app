import { ref, computed } from "vue";
import { defineStore } from "pinia";
import ApiService from "@metronic/core/services/ApiService";
import axios, { type AxiosError } from "axios";
import { AuthVerificationError, isAuthVerificationError } from "@/shared/utils/authError";
import { resolveProfileImageUrl } from "@/shared/utils/profileImage";
import { preloadCategoryMaps, useJournalModalStore } from "@/features/journal/stores/journalModal";
import { useJournalStore } from "@/features/journal/stores/journal";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { useMenuStore } from "@/shared/menu/stores/menu";

/** 라우트 이동이 서버 인증 확인 결과를 재사용하는 메모리 신선도 구간. */
export const AUTH_VERIFICATION_FRESHNESS_MS = 15_000;

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
  permissions?: string[];
  /** 활성 Spring 프로필. local 에서만 개발용 UI(예: id 툴팁)를 노출한다. */
  activeProfile?: string;
}

/** Vue SPA 로그인 실패 후 추가 조치가 필요한 상태. */
export interface LoginActionState {
  username: string;
  isCredentialExpired?: boolean;
  isDupIdLogin?: boolean;
  needsPasswordReset?: boolean;
  passwordToken?: string;
}

function resolveServerMessage(error: AxiosError<{ message?: string }>): string | undefined {
  const serverMsg = error.response?.data?.message;
  return typeof serverMsg === "string" && serverMsg.length > 0 ? serverMsg : undefined;
}

/**
 * useAuthStore
 * Spring Boot JWT 쿠키 기반 인증 상태를 관리한다.
 * - 로그인: POST /api/auth/login (JSON) → JWT HttpOnly 쿠키 발급
 * - 인증 확인: GET /api/auth/get-auth-account → 현재 쿠키로 사용자 정보 조회
 * - 로그아웃: POST /api/auth/logout-json → 쿠키 삭제
 */
export const useAuthStore = defineStore("auth", () => {
  const { t } = useLocaleStore();
  const user = ref<AuthUser | null>(null);
  const isAuthenticated = ref(false);
  const errors = ref<string[]>([]);
  const loginAction = ref<LoginActionState | null>(null);
  /** 로그아웃·세션 만료 전 인증 확인 응답을 폐기하기 위한 세대. */
  let authVerificationGeneration = 0;
  /** 마지막 정상 인증 확인 시각. 새로고침 시 초기화되는 메모리 상태다. */
  let lastAuthVerifiedAt = 0;
  /** 동시 인증 확인을 하나의 서버 요청으로 합친다. */
  let authVerificationRequest: Promise<void> | null = null;
  /** local 프로필에서만 true. 개발용 UI 게이팅에 쓴다. */
  const isLocalProfile = computed(() => user.value?.activeProfile === "local");

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
    authVerificationGeneration += 1;
    lastAuthVerifiedAt = 0;
    authVerificationRequest = null;
    useJournalModalStore().resetCategoryMaps();
    useJournalStore().resetTagCloudState();
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
   * 성공 후 강제 verifyAuth()로 사용자 정보 로드.
   */
  async function login(credentials: { username: string; password: string }) {
    errors.value = [];
    loginAction.value = null;
    try {
      const { data } = await ApiService.post("/api/auth/login", credentials);
      if (data.rslt) {
        await verifyAuth({ force: true });
      } else {
        errors.value = [data.message ?? t("auth.login.failure")];
        setLoginAction(data.rsltMap);
        throw new Error(data.message);
      }
    } catch (e) {
      if (isAuthVerificationError(e)) {
        errors.value = [e.message];
        throw e;
      }
      const axiosErr = e as AxiosError<{ message?: string; rsltMap?: unknown }>;
      const serverData = axiosErr.response?.data;
      if (!serverData) throw e;
      const serverMsg = serverData?.message;
      errors.value = [serverMsg ?? t("auth.login.failure")];
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
   * 라우터 beforeEach는 정상 결과를 짧게 재사용하고, force 호출은 신선도와 무관하게 서버를 확인한다.
   * 서버 응답: AjaxResponse.withObj() → JSON 필드명 rsltObj (ServiceResponse 기준)
   *
   * @param options force=true이면 메모리 신선도 결과를 사용하지 않고 서버를 확인한다.
   */
  async function verifyAuth(options: { force?: boolean } = {}) {
    const ageMs = Date.now() - lastAuthVerifiedAt;
    const isFresh = isAuthenticated.value
      && ageMs >= 0
      && ageMs < AUTH_VERIFICATION_FRESHNESS_MS;
    if (!options.force && isFresh) {
      console.info("[auth] 최근 인증 확인 결과 재사용", {
        ageMs,
        freshnessMs: AUTH_VERIFICATION_FRESHNESS_MS,
      });
      return;
    }

    if (authVerificationRequest) {
      console.info("[auth] 진행 중 인증 확인 요청 공유");
      return authVerificationRequest;
    }

    const requestGeneration = authVerificationGeneration;
    let request!: Promise<void>;
    request = verifyAuthFromServer(requestGeneration)
      .finally(() => {
        if (authVerificationRequest === request) authVerificationRequest = null;
      });
    authVerificationRequest = request;
    return request;
  }

  /** 서버 인증 정보를 조회하고 현재 세대의 응답만 store에 반영한다. */
  async function verifyAuthFromServer(requestGeneration: number): Promise<void> {
    try {
      const { data } = await ApiService.get("/api/auth/get-auth-account");
      if (requestGeneration !== authVerificationGeneration) {
        console.info("[auth] 무효화된 인증 확인 응답 폐기", {
          requestGeneration,
          activeGeneration: authVerificationGeneration,
        });
        return;
      }
      if (data.rslt && data.rsltObj) {
        setAuth(data.rsltObj as AuthUser);
        lastAuthVerifiedAt = Date.now();
      } else {
        purgeAuth();
      }
    } catch (e) {
      if (requestGeneration !== authVerificationGeneration) {
        console.info("[auth] 무효화된 인증 확인 실패 응답 폐기", {
          requestGeneration,
          activeGeneration: authVerificationGeneration,
        });
        return;
      }
      if (axios.isAxiosError<{ message?: string }>(e)) {
        const status = e.response?.status;
        if (status === 401) {
          purgeAuth();
          return;
        }
        throw new AuthVerificationError(
          resolveServerMessage(e) ?? t("auth.verification.failure"),
          status
        );
      }
      throw new AuthVerificationError(t("auth.verification.failure"));
    }
  }


  /** permission key 보유 여부 (서버 AuthInfo.permissions 합집합). */
  function hasPermission(permKey: string): boolean {
    const perms = user.value?.permissions;
    return Array.isArray(perms) && perms.includes(permKey);
  }

  /**
   * 관리자 사이드바 모드 사용 가능 여부.
   * SSOT는 menu.view.admin permission. isMngr 는 permissions 미포함 세션 fallback.
   */
  function canUseMngrMenuMode(): boolean {
    if (hasPermission("menu.view.admin")) return true;
    return !!user.value?.isMngr;
  }
  return {
    user,
    isLocalProfile,
    isAuthenticated,
    errors,
    loginAction,
    login,
    logout,
    verifyAuth,
    purgeAuth,
    hasPermission,
    canUseMngrMenuMode,
  };
});
