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
            };
        } catch (e) {
            console.error("[UserMyPageApp] user_my_page_data parse failed", e);
            return { ...emptyPageData };
        }
    },
};
