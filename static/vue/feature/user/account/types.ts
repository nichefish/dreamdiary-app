/**
 * types.ts
 * 사용자 계정 관리 Vue 화면 공통 타입
 */

export type UserRoleRow = {
    roleKey: string;
    roleName: string;
};

export type UserRow = {
    rnum: number;
    id: number;
    isMe: boolean;
    userRoles: UserRoleRow[];
    profileImageUrl: string;
    userNm: string;
    username: string;
    userProflYn: string;
    retireYn: string;
    cmpyNm: string;
    teamNm: string;
    rankNm: string;
    apntcYn: string;
    email: string;
    isLocked: boolean;
    divide: boolean;
};

export type UserAllowedIpRow = {
    id: number;
    userId: number;
    allowedIp: string;
};

export type UserDetailProfile = {
    userProfileId: number | null;
    addr: string;
    zipcode: string;
    brthdy: string;
    lunarYn: string;
    proflCn: string;
};

export type UserDetailEmplym = {
    userNm: string;
    emplymEmail: string;
    emplymPhoneNumber: string;
    cmpyNm: string;
    teamNm: string;
    emplymNm: string;
    rankNm: string;
    ecnyDt: string;
    retireYn: string;
    retireDt: string;
    acntBank: string;
    acntNo: string;
};

export type UserDetail = {
    id: number;
    username: string;
    profileImageUrl: string;
    createdBy: string;
    createdAt: string;
    userRoles: UserRoleRow[];
    nickname: string;
    email: string;
    phoneNumber: string;
    isLocked: boolean;
    useAllowedIp: boolean;
    allowedIpList: UserAllowedIpRow[];
    content: string;
    userProfileId: number | null;
    profile: UserDetailProfile | null;
    emplym: UserDetailEmplym | null;
};

export type PaginationState = {
    currPageNo: number;
    lastPageNo: number;
    totalCnt: number;
    pageSize: number;
    isFirstPage: boolean;
    isLastPage: boolean;
    prevPageNo: number;
    nextPageNo: number;
};

export type UserLabels = {
    noProfile: string;
    retired: string;
    activeEmployee: string;
    probation: string;
    locked: string;
    use: string;
    emptyList: string;
    totalPrefix: string;
    unuse: string;
    modifyTooltip: string;
    deleteTooltip: string;
    listTooltip: string;
    passwordResetTooltip: string;
    profileAddress?: string;
    profileBirthDate?: string;
    profileLunar?: string;
    profileProfile?: string;
    emplymUserName?: string;
    emplymEmail?: string;
    emplymPhoneNumber?: string;
    emplymAffiliation?: string;
    emplymRank?: string;
    emplymJoinDate?: string;
    emplymPayrollAccount?: string;
};

export type UserListState = {
    rows: UserRow[];
    pagination: PaginationState;
    labels: UserLabels;
};

export type UserListActions = {
    page: (pageNo: number, pageSize?: number) => void;
    search: () => void;
    xlsxDownload: () => void;
    regForm: () => void;
    dtl: (id: string | number) => void;
};

export type UserDetailState = {
    detail: UserDetail;
    labels: UserLabels;
};

export type UserDetailActions = {
    pwResetAjax: () => void;
    mdfForm: () => void;
    delAjax: () => void;
    list: () => void;
};

export type UserRoleOption = {
    roleKey: string;
    roleName: string;
};

export type CodeOption = {
    code: string;
    codeName: string;
};

export type UserProfileForm = {
    brthdy: string;
    lunarYn: string;
    proflCn: string;
};

export type UserEmplymForm = {
    userNm: string;
    emplymEmailId: string;
    emplymEmailDomain: string;
    emplymPhoneNumber: string;
    cmpyCd: string;
    teamCd: string;
    emplymCd: string;
    rankCd: string;
    apntcYn: string;
    ecnyDt: string;
    retireYn: string;
    retireDt: string;
    acntBank: string;
    acntNo: string;
};

export type UserForm = {
    id: number | null;
    fileGroupId: number | null;
    mode: string;
    username: string;
    nickname: string;
    emailId: string;
    emailDomain: string;
    phoneNumber: string;
    roleKeyList: string[];
    useAllowedIp: boolean;
    allowedIpListStr: string;
    content: string;
    hasProfile: boolean;
    hasEmplym: boolean;
    profile: UserProfileForm;
    emplym: UserEmplymForm;
};

export type UserFormLabels = {
    username: string;
    usernameReq: string;
    password: string;
    passwordReq: string;
    passwordConfirm: string;
    role: string;
    nickname: string;
    nicknameReq: string;
    emailIdPlaceholder: string;
    customInput: string;
    dupCheck: string;
    phoneNumber: string;
    phoneReq: string;
    allowedIp: string;
    allowedIpReq: string;
    accountDescription: string;
    accountDescriptionPlaceholder: string;
    use: string;
    unuse: string;
    addProfile: string;
    addEmployment: string;
    userEmplymReq: string;
    save: string;
    list: string;
    tooltipUsernameDupCheck: string;
    tooltipRole: string;
    tooltipNickname: string;
    tooltipEmail: string;
    tooltipEmailDupCheck: string;
    tooltipAllowedIp: string;
    tooltipAccountDescription: string;
    tooltipAddProfile: string;
    tooltipAddEmployment: string;
    tooltipSave: string;
    tooltipList: string;
    removeProfile: string;
    removeEmployment: string;
    profileAddress: string;
    profileBirthDate: string;
    profileLunarYn: string;
    profileLunar: string;
    profileSolar: string;
    profileProfile: string;
    emplymUserName: string;
    emplymNamePlaceholder: string;
    emplymEmail: string;
    emplymPhoneNumber: string;
    emplymAffiliation: string;
    emplymCompanyOption: string;
    emplymTeamOption: string;
    emplymEmploymentTypeOption: string;
    emplymRank: string;
    emplymSelectOption: string;
    emplymProbation: string;
    emplymProbationActive: string;
    emplymNotApplicable: string;
    emplymJoinDate: string;
    emplymJoinDateGuide: string;
    emplymRetiredYn: string;
    emplymRetired: string;
    emplymRetiredDate: string;
    emplymRetiredDateGuide: string;
    emplymPayrollAccount: string;
    emplymBank: string;
    emplymAccountNumber: string;
};

export type UserFormState = {
    form: UserForm;
    roles: UserRoleOption[];
    cmpyOptions: CodeOption[];
    teamOptions: CodeOption[];
    emplymOptions: CodeOption[];
    rankOptions: CodeOption[];
    labels: UserFormLabels;
};

export type UserFormActions = {
    isMdf: () => boolean;
    initForm: () => void;
    submitHandler: () => boolean;
    idDupChckAjax: () => boolean;
    emailDupChckAjax: () => boolean;
    regAjax: () => void;
    list: () => void;
};
