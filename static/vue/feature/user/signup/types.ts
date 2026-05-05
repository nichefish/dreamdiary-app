/**
 * 계정 신청 Vue 상태 타입
 *
 * @author nichefish
 */

export type UserSignupFormState = {
    id: string;
    fileGroupId: string;
    username: string;
    password: string;
    passwordCf: string;
    nickname: string;
    emailId: string;
    emailDomain: string;
    /** 사원용 이메일 도메인 선택(요청 본문에는 emplym*. 만 포함) */
    emailDomainSelect: string;
    phoneNumber: string;
    useAllowedIpYn: boolean;
    /** 아이디 중복 확인 통과 플래그(레거시 hidden `ipDupChckPassed`) */
    usernameDupPassed: string;
    /** 이메일 중복 확인 통과 플래그 */
    emailDupPassed: string;
    content: string;
    /** 중복 확인 UI 문구(메시지 또는 서버 메시지 표시) */
    usernameMsg: string;
    emailMsg: string;
    usernameMsgIsError: boolean;
    emailMsgIsError: boolean;
    idDupBtnDisabled: boolean;
    emailDupBtnDisabled: boolean;
    /** 서버에서 받은 기본 신청 권한 키(일반 사용자 고정) */
    authUserRoleKey: string;
    staffRankCd: string;
    showProfile: boolean;
    showEmplym: boolean;
    profile: {
        proflCn: string;
        brthdy: string;
        lunarYn: boolean;
    };
    emplym: {
        userNm: string;
        emplymEmailId: string;
        emplymEmailDomain: string;
        emplymEmailDomainSelect: string;
        emplymPhoneNumber: string;
        cmpyCd: string;
        teamCd: string;
        emplymCd: string;
        rankCd: string;
        apntcYn: boolean;
        retireYn: boolean;
        ecnyDt: string;
        retireDt: string;
        acntBank: string;
        acntNo: string;
    };
};
