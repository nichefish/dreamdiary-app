import { CodeOption, UserForm, UserRoleOption } from "../types.js";

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
        lunarYn: false,
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
        apntcYn: false,
        ecnyDt: "",
        retireYn: false,
        retireDt: "",
        acntBank: "",
        acntNo: "",
    },
};


export default {
    parseForm(): UserForm {
        const dataEl: HTMLElement | null = document.getElementById("user_form_data");
        if (!dataEl) return { ...emptyForm };
        try {
            const parsed = JSON.parse(dataEl.textContent || "{}") as Partial<UserForm> & {
                profile?: Partial<UserForm["profile"]> & { lunarYn?: string | boolean };
                emplym?: Partial<UserForm["emplym"]> & { apntcYn?: string | boolean; retireYn?: string | boolean };
            };
            const normalizeYn = (v: unknown): boolean => v === true || v === "Y" || v === "true";
            return {
                ...emptyForm,
                ...parsed,
                profile: {
                    ...emptyForm.profile,
                    ...(parsed.profile || {}),
                    lunarYn: normalizeYn(parsed.profile?.lunarYn),
                },
                emplym: {
                    ...emptyForm.emplym,
                    ...(parsed.emplym || {}),
                    apntcYn: normalizeYn(parsed.emplym?.apntcYn),
                    retireYn: normalizeYn(parsed.emplym?.retireYn),
                },
            };
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
};
