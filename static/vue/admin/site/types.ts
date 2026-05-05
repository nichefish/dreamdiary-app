/**
 * 사이트 관리(admin_page) Vue 화면 공통 타입
 */

export type AdminPageMeta = {
    authMngrKey: string;
    authUserKey: string;
    authDevKey: string;
    /** 기준 연도(휴일 API 연도 선택 기본값) */
    currYy: number;
};

export type RoleRow = {
    id: number;
    roleKey: string;
    roleName: string;
    authLevel: number | null;
    parentRoleId: number | null;
    sortOrder: number | null;
    useYn: string;
};
