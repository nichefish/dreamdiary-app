import { API_BASE_URL } from "../config/env";
import {
  captureAccessTokenFromResponse,
  clearAccessToken,
  getAccessToken
} from "../auth/accessToken";

// string[]/number[] 를 포함하도록 QueryValue 타입 확장
type QueryValue = string | number | boolean | undefined | null | string[] | number[];

type RequestOptions = RequestInit & {
  query?: Record<string, QueryValue>;
  /** login/refresh 응답 Authorization 헤더에서 access JWT 저장 (WebSocket용) */
  captureAccessToken?: boolean;
};

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly body?: unknown
  ) {
    super(message);
    this.name = "ApiError";
  }
}

// ─── 401 전역 핸들러 ──────────────────────────────────────
// 세션 만료(401) 시 AuthContext 가 등록한 콜백을 호출해 자동 로그아웃.
// 순환 의존성 없이 연결하기 위해 모듈 레벨 콜백 패턴 사용.

let _onUnauthorized: (() => void) | null = null;

/** AuthContext 가 마운트 시 등록, 언마운트 시 해제 */
export function setUnauthorizedHandler(handler: () => void): () => void {
  _onUnauthorized = handler;
  return () => { _onUnauthorized = null; };
}

function buildUrl(path: string, query?: RequestOptions["query"]) {
  const url = new URL(path, API_BASE_URL);

  Object.entries(query ?? {}).forEach(([key, value]) => {
    if (value === undefined || value === null) return;
    if (Array.isArray(value)) {
      // Spring MVC @ModelAttribute List<String> 바인딩 — 인덱스 형태 사용
      value.forEach((v, i) => url.searchParams.set(`${key}[${i}]`, String(v)));
    } else {
      url.searchParams.set(key, String(value));
    }
  });

  return url.toString();
}

async function parseResponse(response: Response) {
  const contentType = response.headers.get("content-type") ?? "";

  if (contentType.includes("application/json")) {
    return response.json();
  }

  const text = await response.text();
  return text.length > 0 ? text : null;
}

/** 구조화된 API 오류 응답에서 사용자에게 표시할 서버 메시지만 추출한다. */
function getResponseErrorMessage(responseBody: unknown, status: number): string {
  if (responseBody && typeof responseBody === "object" && !Array.isArray(responseBody)) {
    const message = (responseBody as { message?: unknown }).message;
    if (typeof message === "string" && message.trim().length > 0) return message;
  }
  return `요청 처리 중 오류가 발생했습니다. (${status})`;
}

export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { query, headers, body, captureAccessToken, ...init } = options;
  // FormData 전송 시 Content-Type 을 직접 지정하지 않는다.
  // fetch 가 multipart/form-data + boundary 를 자동으로 설정한다.
  const isFormData = typeof FormData !== "undefined" && body instanceof FormData;
  const token = getAccessToken();

  const response = await fetch(buildUrl(path, query), {
    credentials: "include",
    ...init,
    body,
    headers: {
      Accept: "application/json",
      ...(!isFormData && { "Content-Type": "application/json" }),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...headers
    }
  });

  if (captureAccessToken && response.ok) {
    void captureAccessTokenFromResponse(response);
  }

  const responseBody = await parseResponse(response);

  if (!response.ok) {
    if (response.status === 401) {
      void clearAccessToken();
      _onUnauthorized?.();
    }
    console.error("[API] Request failed", { path, status: response.status, responseBody });
    throw new ApiError(getResponseErrorMessage(responseBody, response.status), response.status, responseBody);
  }

  return responseBody as T;
}
