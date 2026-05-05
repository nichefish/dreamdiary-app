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

