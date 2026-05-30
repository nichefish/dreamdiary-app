import * as SecureStore from "expo-secure-store";

const SECURE_KEY = "dreamdiary.accessToken";

/** REST·WebSocket 동기 조회용 메모리 캐시 (SecureStore 와 동기화) */
let memoryToken: string | null = null;

const listeners = new Set<() => void>();

function notifyAccessTokenListeners(): void {
  listeners.forEach((listener) => listener());
}

/** access JWT 변경 시 (저장·삭제·hydrate) — WebSocket 재연결 등 */
export function subscribeAccessToken(listener: () => void): () => void {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

export function getAccessToken(): string | null {
  return memoryToken;
}

/** 앱 시작 시 SecureStore → 메모리 (이후 refresh 로 갱신) */
export async function hydrateAccessTokenFromSecureStore(): Promise<void> {
  try {
    const stored = await SecureStore.getItemAsync(SECURE_KEY);
    if (stored && stored.length > 0) {
      memoryToken = stored;
      notifyAccessTokenListeners();
    }
  } catch (e) {
    console.warn("[accessToken] hydrateAccessTokenFromSecureStore failed", e);
  }
}

export async function setAccessToken(token: string | null): Promise<void> {
  const next = token && token.length > 0 ? token : null;
  const changed = memoryToken !== next;
  memoryToken = next;
  try {
    if (next) {
      await SecureStore.setItemAsync(SECURE_KEY, next);
    } else {
      await SecureStore.deleteItemAsync(SECURE_KEY);
    }
  } catch (e) {
    console.warn("[accessToken] SecureStore write failed", e);
  }
  if (changed) notifyAccessTokenListeners();
}

export async function clearAccessToken(): Promise<void> {
  await setAccessToken(null);
}

/** Parse `Bearer <jwt>` from response Authorization header. */
export function parseBearerAuthorizationHeader(headerValue: string | null): string | null {
  if (!headerValue) return null;
  const trimmed = headerValue.trim();
  const prefix = "Bearer ";
  if (!trimmed.startsWith(prefix)) return null;
  const token = trimmed.slice(prefix.length).trim();
  return token.length > 0 ? token : null;
}

export async function captureAccessTokenFromResponse(response: Response): Promise<void> {
  const token =
    parseBearerAuthorizationHeader(response.headers.get("Authorization")) ??
    parseBearerAuthorizationHeader(response.headers.get("authorization"));
  if (token) await setAccessToken(token);
}
