import axios, { type AxiosError } from "axios";
import { create } from "zustand";

import { http } from "@/shared/api/http";
import type { AuthUser, LoginActionState } from "@/shared/auth/types";

interface AuthState {
  user: AuthUser | null;
  isAuthenticated: boolean;
  errors: string[];
  loginAction: LoginActionState | null;
  login: (credentials: { username: string; password: string }) => Promise<void>;
  verifyAuth: () => Promise<void>;
  purgeAuth: () => void;
}

function setLoginActionFromMap(
  rsltMap: unknown,
  setAction: (action: LoginActionState | null) => void,
): void {
  if (!rsltMap || typeof rsltMap !== "object" || Array.isArray(rsltMap)) {
    setAction(null);
    return;
  }
  const map = rsltMap as Record<string, unknown>;
  const username = typeof map.username === "string" ? map.username : "";
  const isCredentialExpired = map.isCredentialExpired === true;
  const isDupIdLogin = map.isDupIdLogin === true;
  const needsPasswordReset = map.needsPasswordReset === true;
  if (!username || (!isCredentialExpired && !isDupIdLogin && !needsPasswordReset)) {
    setAction(null);
    return;
  }
  setAction({
    username,
    isCredentialExpired,
    isDupIdLogin,
    needsPasswordReset,
    passwordToken: typeof map.passwordToken === "string" ? map.passwordToken : undefined,
  });
}

/**
 * JWT 쿠키 기반 인증 store.
 * Vue {@code shared/auth/stores/auth.ts} 와 동일 API 계약을 따른다.
 */
export const useAuthStore = create<AuthState>((set, get) => ({
  user: null,
  isAuthenticated: false,
  errors: [],
  loginAction: null,

  async login(credentials) {
    set({ errors: [], loginAction: null });
    try {
      const { data } = await http.post("/api/auth/login", credentials);
      if (data.rslt) {
        await get().verifyAuth();
      } else {
        const message = data.message ?? "로그인에 실패했습니다.";
        setLoginActionFromMap(data.rsltMap, (loginAction) => set({ errors: [message], loginAction }));
        throw new Error(message);
      }
    } catch (error) {
      const axiosErr = error as AxiosError<{ message?: string; rsltMap?: unknown }>;
      const serverData = axiosErr.response?.data;
      if (!serverData) throw error;
      const serverMsg = serverData.message ?? "로그인에 실패했습니다.";
      setLoginActionFromMap(serverData.rsltMap, (loginAction) => set({ errors: [serverMsg], loginAction }));
      throw error;
    }
  },

  async verifyAuth() {
    try {
      const { data } = await http.get("/api/auth/get-auth-account");
      if (data.rslt && data.rsltObj) {
        set({
          user: data.rsltObj as AuthUser,
          isAuthenticated: true,
          errors: [],
          loginAction: null,
        });
      } else {
        get().purgeAuth();
      }
    } catch {
      get().purgeAuth();
    }
  },

  purgeAuth() {
    set({
      user: null,
      isAuthenticated: false,
      errors: [],
      loginAction: null,
    });
  },
}));
