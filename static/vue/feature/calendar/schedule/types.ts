/**
 * types.ts
 * calendar/schedule feature Vue 컴포넌트에서 사용하는 타입 정의
 *
 * @author nichefish
 */

/** FullCalendar 렌더링용 이벤트 객체 */
export type ScheduleCalEvent = {
    id: string | number;
    title: string;
    start: string;
    end?: string;
    groupId?: string;
    color?: string;
    [key: string]: unknown;
};

/** 참가자 한 명 */
export type SchedulePrtcpnt = {
    username: string;
    userNm?: string;
};

/** 등록/수정 폼 데이터 */
export type ScheduleForm = {
    id?: string | number;
    scheduleCd: string;
    title: string;
    bgnDt: string;
    endDt?: string;
    content?: string;
    prtcpntList?: SchedulePrtcpnt[];
    jandiYn?: string;
    privateYn?: string;
    isPrvt?: boolean;
};

/** 상세 조회 데이터 (ScheduleForm 확장) */
export type ScheduleDetail = ScheduleForm & {
    scheduleNm?: string;
    prtcpnt?: string;
};

/** 스케줄 종류 코드 옵션 */
export type ScheduleCodeOption = {
    code: string;
    codeName: string;
};

/** 참가자 선택용 사용자 옵션 */
export type ScheduleUserOption = {
    username: string;
    userNm: string;
};

/** 달력 필터 상태 (체크박스 + 쿠키 동기화) */
export type ScheduleCalFilter = {
    myPaprChk: boolean;
    vcatnChk:  boolean;
    indtChk:   boolean;
    outdtChk:  boolean;
    tlcmmtChk: boolean;
    prvtChk:   boolean;
};
