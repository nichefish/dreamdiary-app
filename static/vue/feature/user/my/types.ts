/**
 * types.ts
 * 내 정보 Vue 화면 타입
 */

export type UserMyRole = {
    roleKey: string;
    roleName: string;
};

export type UserMyAllowedIp = {
    allowedIp: string;
};

export type UserMyInfoItem = {
    itemNm: string;
    itemCn: string;
    itemDc: string;
};

export type UserMyInfo = {
    userProfileId: number | null;
    cmpyNm: string;
    teamNm: string;
    emplymNm: string;
    rankNm: string;
    rankCd: string;
    apntcYn: string;
    ecnyDt: string;
    retireYn: string;
    retireDt: string;
    brthdy: string;
    acntBank: string;
    acntNo: string;
    itemList: UserMyInfoItem[];
};

export type UserMyPage = {
    id: number | null;
    username: string;
    nickname: string;
    email: string;
    phoneNumber: string;
    profileImageUrl: string;
    userRoles: UserMyRole[];
    isAllowedIpY: boolean;
    allowedIpList: UserMyAllowedIp[];
    userInfo: UserMyInfo | null;
};

export type UserMyVacation = {
    visible: boolean;
    statsYy: string;
    bgnDt: string;
    endDt: string;
    total: string;
    used: string;
    remains: string;
    tooltip: string;
};

export type UserMyPageData = {
    errorMsg: string;
    user: UserMyPage;
    vacation: UserMyVacation;
};
