import { CodeOption, UserForm, UserFormLabels, UserRoleOption } from "../types.js";

const emptyForm: UserForm = {
    id: null,
    fileGroupId: null,
    mode: "regist",
    username: "",
    nickname: "",
    emailId: "",
    emailDomain: "gmail.com",
    phoneNumber: "",
    roleKeyList: [],
    useAllowedIp: false,
    allowedIpListStr: "",
    content: "",
    hasProfile: false,
    hasEmplym: false,
    profile: {
        brthdy: "",
        lunarYn: "N",
        proflCn: "",
    },
    emplym: {
        userNm: "",
        emplymEmailId: "",
        emplymEmailDomain: "",
        emplymPhoneNumber: "",
        cmpyCd: "",
        teamCd: "",
        emplymCd: "",
        rankCd: "",
        apntcYn: "N",
        ecnyDt: "",
        retireYn: "N",
        retireDt: "",
        acntBank: "",
        acntNo: "",
    },
};

const emptyLabels: UserFormLabels = {
    username: "아이디",
    usernameReq: "",
    password: "비밀번호",
    passwordReq: "",
    passwordConfirm: "비밀번호 확인",
    role: "권한",
    nickname: "표시이름",
    nicknameReq: "",
    emailIdPlaceholder: "",
    customInput: "직접입력",
    dupCheck: "중복확인",
    phoneNumber: "연락처",
    phoneReq: "",
    allowedIp: "접속IP",
    allowedIpReq: "",
    accountDescription: "계정 설명",
    accountDescriptionPlaceholder: "",
    use: "사용",
    unuse: "미사용",
    addProfile: "프로필 정보 추가",
    addEmployment: "직원 인사정보 추가",
    userEmplymReq: "",
    save: "저장",
    list: "목록",
    tooltipUsernameDupCheck: "",
    tooltipRole: "",
    tooltipNickname: "",
    tooltipEmail: "",
    tooltipEmailDupCheck: "",
    tooltipAllowedIp: "",
    tooltipAccountDescription: "",
    tooltipAddProfile: "",
    tooltipAddEmployment: "",
    tooltipSave: "",
    tooltipList: "",
    removeProfile: "프로필 정보 삭제-",
    removeEmployment: "직원 인사정보 삭제-",
    profileAddress: "주소",
    profileBirthDate: "생일",
    profileLunarYn: "음력여부",
    profileLunar: "음력",
    profileSolar: "양력",
    profileProfile: "프로필",
    emplymUserName: "이름",
    emplymNamePlaceholder: "",
    emplymEmail: "업무 Email",
    emplymPhoneNumber: "업무 연락처",
    emplymAffiliation: "소속",
    emplymCompanyOption: "회사 선택",
    emplymTeamOption: "팀 선택",
    emplymEmploymentTypeOption: "재직구분 선택",
    emplymRank: "직급",
    emplymSelectOption: "선택",
    emplymProbation: "수습",
    emplymProbationActive: "수습",
    emplymNotApplicable: "해당없음",
    emplymJoinDate: "입사일",
    emplymJoinDateGuide: "",
    emplymRetiredYn: "퇴사여부",
    emplymRetired: "퇴사",
    emplymRetiredDate: "퇴사일",
    emplymRetiredDateGuide: "",
    emplymPayrollAccount: "급여계좌",
    emplymBank: "은행",
    emplymAccountNumber: "계좌번호",
};

export default {
    parseForm(): UserForm {
        const dataEl: HTMLElement | null = document.getElementById("user_form_data");
        if (!dataEl) return { ...emptyForm };
        try {
            return { ...emptyForm, ...JSON.parse(dataEl.textContent || "{}") };
        } catch (e) {
            console.error("[UserFormApp] user_form_data parse failed", e);
            return { ...emptyForm };
        }
    },
    parseRoles(): UserRoleOption[] {
        const dataEl: HTMLElement | null = document.getElementById("user_form_role_data");
        if (!dataEl) return [];
        try {
            const parsed: unknown = JSON.parse(dataEl.textContent || "[]");
            return Array.isArray(parsed) ? (parsed as UserRoleOption[]) : [];
        } catch (e) {
            console.error("[UserFormApp] user_form_role_data parse failed", e);
            return [];
        }
    },
    parseCodeOptions(id: string): CodeOption[] {
        const dataEl: HTMLElement | null = document.getElementById(id);
        if (!dataEl) return [];
        try {
            const parsed: unknown = JSON.parse(dataEl.textContent || "[]");
            return Array.isArray(parsed) ? (parsed as CodeOption[]) : [];
        } catch (e) {
            console.error("[UserFormApp] " + id + " parse failed", e);
            return [];
        }
    },
    parseLabels(): UserFormLabels {
        const dataEl: HTMLElement | null = document.getElementById("user_form_label_data");
        if (!dataEl) return { ...emptyLabels };
        try {
            return { ...emptyLabels, ...JSON.parse(dataEl.textContent || "{}") };
        } catch (e) {
            console.error("[UserFormApp] user_form_label_data parse failed", e);
            return { ...emptyLabels };
        }
    },
};
