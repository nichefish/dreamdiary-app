/** 백엔드 AuthUserDto와 대응 */
export interface AuthUser {
  username: string;
  nickname: string;
  email: string;
  profileImageUrl?: string;
  roles: { roleKey: string }[];
  isMngr: boolean;
  isDev: boolean;
}

/** 백엔드 AjaxResponse 공통 래퍼 */
export interface AjaxResponse<T = unknown> {
  rslt: boolean;
  message?: string;
  rsltObj?: T;
}
