import React, { createContext, useCallback, useContext, useEffect, useState } from "react";
import {
  getAuthAccount,
  login as apiLogin,
  logout as apiLogout,
  refreshAccessToken
} from "../api/dreamDiaryApi";
import { setUnauthorizedHandler } from "../api/client";
import {
  clearAccessToken,
  hydrateAccessTokenFromSecureStore
} from "../auth/accessToken";
import type { AuthUser } from "../types/auth";

interface AuthContextValue {
  user: AuthUser | null;
  isAuthenticated: boolean;
  /** 앱 시작 시 기존 세션·SecureStore 토큰 확인 중 */
  isLoading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  /**
   * 기존 세션(쿠키) 유효 여부 확인 — 앱 시작 시 1회 호출.
   * SecureStore 토큰 hydrate 후 refresh 로 JWT 갱신(WebSocket·REST Authorization).
   */
  const checkAuth = useCallback(async () => {
    try {
      await hydrateAccessTokenFromSecureStore();
      const data = await getAuthAccount();
      if (data.rslt && data.rsltObj) {
        setUser(data.rsltObj);
        try {
          await refreshAccessToken();
        } catch {
          // refresh 실패 시 REST 쿠키만 유지 — AI 채팅 WS 는 토큰 없으면 연결 실패 가능
          console.warn("[Auth] refreshAccessToken failed after getAuthAccount");
        }
      } else {
        setUser(null);
        await clearAccessToken();
      }
    } catch {
      setUser(null);
      await clearAccessToken();
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    void checkAuth();
  }, [checkAuth]);

  // 401/403 응답 시 client.ts 가 이 핸들러를 호출 → 자동 로그아웃
  useEffect(() => {
    return setUnauthorizedHandler(() => {
      void clearAccessToken();
      setUser(null);
    });
  }, []);

  const login = useCallback(
    async (username: string, password: string) => {
      const data = await apiLogin(username, password);
      if (!data.rslt) throw new Error(data.message ?? "로그인에 실패했습니다.");
      // 로그인 성공 → 서버가 JWT 쿠키·Authorization 헤더 발급 → 인증 상태 갱신
      await checkAuth();
    },
    [checkAuth]
  );

  const logout = useCallback(async () => {
    try {
      await apiLogout();
    } finally {
      await clearAccessToken();
      setUser(null);
    }
  }, []);

  return (
    <AuthContext.Provider value={{ user, isAuthenticated: user !== null, isLoading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
