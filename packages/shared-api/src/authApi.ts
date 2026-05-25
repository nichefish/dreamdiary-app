import type { ApiResult, AuthUser, LoginCredentials } from "@dreamdiary/shared-types";

import type { HttpClient } from "./httpClient";

export function createAuthApi(client: HttpClient) {
  return {
    login(credentials: LoginCredentials) {
      return client.post<ApiResult>("/api/auth/login", credentials);
    },

    logout() {
      return client.post<ApiResult>("/api/auth/logout-json", {});
    },

    getAuthAccount() {
      return client.get<ApiResult<AuthUser>>("/api/auth/get-auth-account");
    }
  };
}

