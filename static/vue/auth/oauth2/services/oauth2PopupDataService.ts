import { OAuth2PopupData } from "../types.js";

const fallbackData: OAuth2PopupData = {
    providerKey: "",
    providerLabel: "OAuth 2.0",
    authenticatedText: "Authenticated.",
    errorMsg: "",
    returnMainLabel: "메인으로",
    returnMainTooltip: "메인으로 돌아갑니다.",
};

export default {
    parsePopupData(): OAuth2PopupData {
        const dataElement = document.getElementById("oauth2_popup_data");
        if (!dataElement) return { ...fallbackData };
        try {
            const parsed = JSON.parse(dataElement.textContent || "{}") as Partial<OAuth2PopupData>;
            return { ...fallbackData, ...parsed };
        } catch (error) {
            console.error("[OAuth2PopupApp] oauth2_popup_data parse failed", error);
            return { ...fallbackData };
        }
    },
};
