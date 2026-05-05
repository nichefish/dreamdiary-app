import { UserMyPageData } from "../types.js";

const emptyPageData: UserMyPageData = {
    errorMsg: "",
    user: {
        id: null,
        username: "",
        nickname: "",
        email: "",
        phoneNumber: "",
        profileImageUrl: "",
        userRoles: [],
        isAllowedIpY: false,
        allowedIpList: [],
        userInfo: null,
    },
    vacation: {
        visible: false,
        statsYy: "",
        bgnDt: "",
        endDt: "",
        total: "0",
        used: "0",
        remains: "0",
        tooltip: "",
    },
    labels: {
        username: "아이디",
        password: "비밀번호",
        passwordChange: "비밀번호 변경",
        role: "권한",
        nickname: "표시이름",
        allowedIp: "접속IP",
        use: "사용",
        unuse: "미사용",
        department: "부서",
        rank: "직급",
        probation: "수습",
        joinDate: "입사일",
        retireDate: "퇴사일",
        phoneNumber: "전화번호",
        email: "E-mail",
        birthDate: "생년월일",
        accountNumber: "계좌번호",
        additionalInfo: "추가정보",
        vacationTitleSuffix: "년 연차 사용 현황",
        totalVacation: "총연차",
        usedVacation: "소진연차",
        remainsVacation: "잔여연차",
        currentPassword: "현재 비밀번호",
        newPassword: "새 비밀번호",
        newPasswordConfirm: "새 비밀번호 확인",
        passwordReq: "",
        save: "저장",
        close: "닫기",
        uploadProfileImageTooltip: "프로필 이미지를 변경합니다.",
        removeProfileImageTooltip: "프로필 이미지를 삭제합니다.",
        passwordChangeTooltip: "비밀번호 변경 팝업을\n호출합니다.",
        tooltipSave: "저장합니다.",
        tooltipClose: "닫습니다.",
        changedProfileNotice: "변경된 프로필은 재접속 이후에 적용됩니다.",
    },
};

export default {
    parsePageData(): UserMyPageData {
        const dataEl: HTMLElement | null = document.getElementById("user_my_page_data");
        if (!dataEl) return { ...emptyPageData };
        try {
            const parsed = JSON.parse(dataEl.textContent || "{}");
            return {
                ...emptyPageData,
                ...parsed,
                user: { ...emptyPageData.user, ...(parsed.user || {}) },
                vacation: { ...emptyPageData.vacation, ...(parsed.vacation || {}) },
                labels: { ...emptyPageData.labels, ...(parsed.labels || {}) },
            };
        } catch (e) {
            console.error("[UserMyPageApp] user_my_page_data parse failed", e);
            return { ...emptyPageData };
        }
    },
};
