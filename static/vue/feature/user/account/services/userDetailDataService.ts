import { UserDetail, UserLabels } from "../types.js";

const emptyDetail: UserDetail = {
    id: 0,
    username: "",
    profileImageUrl: "",
    createdBy: "",
    createdAt: "",
    userRoles: [],
    nickname: "",
    email: "",
    phoneNumber: "",
    isLocked: false,
    useAllowedIp: false,
    allowedIpList: [],
    content: "",
    userProfileId: null,
    profile: null,
    emplym: null,
};

export default {
    parseDetailFromPageData(): UserDetail {
        const dataEl: HTMLElement | null = document.getElementById("user_detail_data");
        if (!dataEl) return { ...emptyDetail };
        try {
            return { ...emptyDetail, ...JSON.parse(dataEl.textContent || "{}") };
        } catch (e) {
            console.error("[UserDetailApp] user_detail_data parse failed", e);
            return { ...emptyDetail };
        }
    },
    parseLabels(): Partial<UserLabels> {
        const dataEl: HTMLElement | null = document.getElementById("user_detail_label_data");
        if (!dataEl) return {};
        try {
            return JSON.parse(dataEl.textContent || "{}");
        } catch (e) {
            console.error("[UserDetailApp] user_detail_label_data parse failed", e);
            return {};
        }
    },
};
