import { UserDetail, UserLabels } from "../types.js";
import { fallbackText } from "../../shared/profileEmplymShared.js";
import { formatEmplymAffiliation, formatEmplymJoinRetire, formatEmplymPayrollAccount, formatEmplymRank } from "../../shared/profileEmplymShared.js";
import UserProfileEmplymReadSection from "../../shared/components/UserProfileEmplymReadSection.js";

export default {
    name: "UserDetailProfileEmplymPanel",
    components: {
        UserProfileEmplymReadSection,
    },
    props: {
        detail: { type: Object, required: true },
        labels: { type: Object, required: true },
    },
    computed: {
        user(): UserDetail {
            return this.detail as UserDetail;
        },
        l(): UserLabels {
            return this.labels as UserLabels;
        },
        hasProfile(): boolean {
            return !!this.user.profile;
        },
        hasEmplym(): boolean {
            return !!this.user.emplym;
        },
        profileRows(): Array<{ label: string; value: string; lunarBadge?: boolean; lunarLabel?: string; asTextarea?: boolean }> {
            if (!this.hasProfile)
                return [];
            return [
                { label: this.l.profileAddress || "", value: `${this.fallback(this.user.profile?.addr)} / ${this.fallback(this.user.profile?.zipcode)}` },
                {
                    label: this.l.profileBirthDate || "",
                    value: this.fallback(this.user.profile?.brthdy),
                    lunarBadge: this.user.profile?.lunarYn === "Y",
                    lunarLabel: this.l.profileLunar || "",
                },
                { label: this.l.profileProfile || "", value: this.user.profile?.proflCn || "", asTextarea: true },
            ];
        },
        emplymRows(): Array<{ label: string; value: string; asTextarea?: boolean }> {
            if (!this.hasEmplym)
                return [];
            return [
                { label: this.l.emplymUserName || "", value: this.fallback(this.user.emplym?.userNm) },
                { label: this.l.emplymEmail || "", value: this.fallback(this.user.emplym?.emplymEmail) },
                { label: this.l.emplymPhoneNumber || "", value: this.fallback(this.user.emplym?.emplymPhoneNumber) },
                {
                    label: this.l.emplymAffiliation || "",
                    value: formatEmplymAffiliation(this.user.emplym || {}),
                },
                { label: this.l.emplymRank || "", value: formatEmplymRank(this.user.emplym || {}, this.fallback(this.l.probation || "")) },
                {
                    label: this.l.emplymJoinDate || "",
                    value: formatEmplymJoinRetire(this.user.emplym || {}),
                },
                {
                    label: this.l.emplymPayrollAccount || "",
                    value: formatEmplymPayrollAccount(this.user.emplym || {}),
                    asTextarea: true,
                },
            ];
        },
    },
    methods: {
        fallback(value: string | null | undefined): string {
            return fallbackText(value);
        },
    },
    template: `
    <UserProfileEmplymReadSection
        :profile-rows="profileRows"
        :emplym-rows="emplymRows"
    />
    `,
};
