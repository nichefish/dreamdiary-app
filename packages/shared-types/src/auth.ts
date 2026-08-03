export type AuthRole = {
  roleKey: string;
  roleName?: string;
};

export type AuthUser = {
  username: string;
  nickname: string;
  email: string;
  profileImageUrl?: string;
  roles: AuthRole[];
  isMngr: boolean;
  isDev: boolean;
  permissions?: string[];
};

export type LoginCredentials = {
  username: string;
  password: string;
};

