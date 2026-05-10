/**
 * 로그 관리 화면 Vue 타입
 */
export type LogListRow = {
    id: number;
    rnum: number;
    logDt: string;
    logUserNm: string;
    username: string;
    ipAddr: string;
    actionTyNm: string;
    requestUri: string;
    rsltMsg: string;
    rslt: boolean;
    isJobUser: boolean;
};

export type LogDetail = {
    title?: string;
    actvtyCtgrNm?: string;
    username?: string;
    logDt?: string;
    ipAddr?: string;
    url?: string;
    referer?: string;
    param?: string;
    content?: string;
    rslt?: string | boolean;
    rsltMsg?: string;
    exceptionNm?: string;
    exceptionMsg?: string;
};

export type LogStatsUserRow = {
    rnum: number;
    userNm: string;
    username: string;
    userProflYn: string;
    retireYn: string;
    roleKey: string;
    roleName: string;
    actvtyCnt: number;
    url: string;
    param?: string;
    content: string;
    rslt: string | boolean;
};

