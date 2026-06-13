/** Spring Boot AuthInfo 와 대응하는 React SPA 인증 사용자 정보. */
export interface AuthUser {
  username: string;
  nickname: string;
  email: string;
  profileImageUrl: string;
  roles: { roleKey: string }[];
  isMngr: boolean;
  isDev: boolean;
}

/** 로그인 실패 후 추가 조치가 필요한 상태. */
export interface LoginActionState {
  username: string;
  isCredentialExpired?: boolean;
  isDupIdLogin?: boolean;
  needsPasswordReset?: boolean;
  passwordToken?: string;
}
