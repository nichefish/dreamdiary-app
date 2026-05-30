/**
 * authError.ts
 * 전역 Axios 인터셉터에서 401 응답 시 throw 하는 인증 만료 에러 sentinel.
 * catch 블록에서 isAuthExpiredError() 로 판별해 일반 오류 alert 를 억제한다.
 */

/** 세션 만료 또는 비로그인 상태에서 API 호출 시 발생하는 인증 에러 sentinel. */
export class AuthExpiredError extends Error {
  constructor() {
    super("AUTH_EXPIRED");
    this.name = "AuthExpiredError";
  }
}

/** e 가 AuthExpiredError 인지 판별한다. */
export function isAuthExpiredError(e: unknown): e is AuthExpiredError {
  return e instanceof AuthExpiredError;
}
