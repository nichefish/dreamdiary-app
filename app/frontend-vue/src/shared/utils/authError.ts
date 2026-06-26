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

/** 인증 확인 API가 미인증이 아닌 서버/네트워크 오류로 실패했음을 나타내는 sentinel. */
export class AuthVerificationError extends Error {
  readonly status?: number;

  constructor(message = "인증 상태를 확인하는 중 오류가 발생했습니다.", status?: number) {
    super(message);
    this.name = "AuthVerificationError";
    this.status = status;
  }
}

/** e 가 AuthVerificationError 인지 판별한다. */
export function isAuthVerificationError(e: unknown): e is AuthVerificationError {
  return e instanceof AuthVerificationError;
}
