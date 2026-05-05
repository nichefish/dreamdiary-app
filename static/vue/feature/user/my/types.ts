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

export type UserMyLabels = {
    username: string;
    password: string;
    passwordChange: string;
    role: string;
    nickname: string;
    allowedIp: string;
    use: string;
    unuse: string;
    department: string;
    rank: string;
    probation: string;
    joinDate: string;
    retireDate: string;
    phoneNumber: string;
    email: string;
    birthDate: string;
    accountNumber: string;
    additionalInfo: string;
    vacationTitleSuffix: string;
    totalVacation: string;
    usedVacation: string;
    remainsVacation: string;
    currentPassword: string;
    newPassword: string;
    newPasswordConfirm: string;
    passwordReq: string;
    save: string;
    close: string;
    uploadProfileImageTooltip: string;
    removeProfileImageTooltip: string;
    passwordChangeTooltip: string;
    tooltipSave: string;
    tooltipClose: string;
    changedProfileNotice: string;
};

export type UserMyPageData = {
    errorMsg: string;
    user: UserMyPage;
    vacation: UserMyVacation;
    labels: UserMyLabels;
};
