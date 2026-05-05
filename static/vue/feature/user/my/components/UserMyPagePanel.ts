import { UserMyPage, UserMyRole, UserMyVacation } from "../types.js";
import { fallbackText } from "../../shared/profileEmplymShared.js";
import { formatEmplymAffiliation, formatEmplymJoinRetire, formatEmplymPayrollAccount, formatEmplymRank } from "../../shared/profileEmplymShared.js";
import { resolveUserRoleIconClass } from "../../shared/profileEmplymShared.js";
import UserProfileEmplymReadSection from "../../shared/components/UserProfileEmplymReadSection.js";

export default {
    name: "UserMyPagePanel",
    components: {
        UserProfileEmplymReadSection,
    },
    props: {
        user: { type: Object, required: true },
        vacation: { type: Object, required: true },
        t: { type: Function, required: true },
    },
    emits: ["upload-profile-image", "remove-profile-image", "open-password-change"],
    computed: {
        u(): UserMyPage {
            return this.user as UserMyPage;
        },
        v(): UserMyVacation {
            return this.vacation as UserMyVacation;
        },
        hasUserInfo(): boolean {
            return !!this.u.userInfo;
        },
        hasRetired(): boolean {
            return this.u.userInfo?.retireYn === "Y";
        },
        hasInfoItems(): boolean {
            return !!this.u.userInfo?.itemList?.length;
        },
        profileRows(): Array<{ label: string; value: string }> {
            if (!this.hasUserInfo)
                return [];
            return [
                { label: this.t("txt.user.my.phone-number"), value: this.u.phoneNumber || "" },
                { label: this.t("txt.user.my.email"), value: this.u.email || "" },
                { label: this.t("txt.user.my.birth-date"), value: this.u.userInfo?.brthdy || "" },
            ];
        },
        emplymRows(): Array<{ label: string; value: string }> {
            if (!this.hasUserInfo)
                return [];
            return [
                {
                    label: this.t("txt.user.my.department"),
                    value: formatEmplymAffiliation(this.u.userInfo || {}),
                },
                {
                    label: this.t("txt.user.my.rank"),
                    value: formatEmplymRank(this.u.userInfo || {}, this.t("txt.user.my.probation")),
                },
                {
                    label: this.t("txt.user.my.join-date"),
                    value: formatEmplymJoinRetire(this.u.userInfo || {}, { retirePrefix: this.t("txt.user.my.retire-date") }),
                },
                {
                    label: this.t("txt.user.my.account-number"),
                    value: formatEmplymPayrollAccount(this.u.userInfo || {}, " | "),
                },
            ];
        },
    },
    methods: {
        roleIconClass(role: UserMyRole): string {
            return resolveUserRoleIconClass(role.roleKey);
        },
        fallback(value: string | null | undefined): string {
            return fallbackText(value);
        },
    },
    template: `
    <div class="card post">
        <!--begin::Card body-->
        <div class="card-body">
            <!--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2 col-4 d-flex-center">
                    <label class="fs-6 fw-bold">{{ t('txt.user.my.username') }}</label>
                </div>
                <div class="col-xl-2 col-8 col-form-label">
                    <div class="btn btn-icon btn-light-primary position-relative w-30px h-30px w-md-40px h-md-40px me-3 cursor-default">
                        <!-- TODO: 원본 프로필 이미지 조회 -->
                        <label class="position-absolute top-0 start-100 translate-middle badge badge-sm badge-circle badge-light-primary opacity-hover cursor-pointer"
                               @click="$emit('upload-profile-image')"
                               data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="t('bs.tooltip.user.my.upload-profile-image')">
                            <i class="bi bi-pen icon-xs text-primary"></i>
                        </label>
                        <span class="position-absolute top-100 start-100 translate-middle badge badge-sm badge-circle badge-light-danger opacity-hover cursor-pointer"
                              @click="$emit('remove-profile-image')"
                              data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="t('bs.tooltip.user.my.remove-profile-image')">
                            <i class="bi bi-x icon-xs text-danger"></i>
                        </span>
                        <img v-if="u.profileImageUrl" :src="u.profileImageUrl" class="img-thumbnail p-0 w-100" />
                        <span v-else class="svg-icon svg-icon-1">
                            <i class="fas fa-user-circle fs-2 blink"></i>
                        </span>
                        <!--end::Svg Icon-->
                    </div>
                    {{ u.username }}
                </div>
            </div>
            <!--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2 col-4">
                    <div class="col-form-label text-center fs-6 fw-bold">{{ t('txt.user.my.password') }}</div>
                </div>
                <div class="col-xl-2 col-5">
                    <button type="button" class="btn btn-sm btn-secondary"
                            @click="$emit('open-password-change')"
                            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="t('bs.tooltip.user.my.password-change')">
                        {{ t('txt.user.my.password-change') }}
                    </button>
                </div>
            </div>
            <!--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2 col-4">
                    <div class="col-form-label text-center fs-6 fw-bold"><label for="roleName">{{ t('txt.user.my.role') }}</label></div>
                </div>
                <div class="col-xl-2 col-8 col-form-label">
                    <template v-for="role in u.userRoles" :key="role.roleKey">
                        <i :class="roleIconClass(role)"></i>
                        {{ role.roleName }}
                    </template>
                </div>
            </div>
            <!--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2 col-4">
                    <div class="col-form-label text-center fs-6 fw-bold"><label for="nickname">{{ t('txt.user.my.nickname') }}</label></div>
                </div>
                <div class="col-xl-2 col-8 col-form-label">{{ u.nickname }}</div>
            </div>
            <!--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2 col-4">
                    <div class="col-form-label text-center fs-6 fw-bold">
                        <label for="allowedIpListStr">{{ t('txt.user.my.allowed-ip') }}</label>
                    </div>
                </div>
                <div class="col-xl-9 col-8 col-form-label">
                    <div class="form-check form-switch form-check-custom form-check-solid">
                        <span class="me-8">{{ u.isAllowedIpY ? t('txt.status.use') : t('txt.status.unuse') }}</span>
                        <template v-if="u.isAllowedIpY">
                            <span v-for="item in u.allowedIpList" :key="item.allowedIp" class="div-textarea div-height-1 me-4">
                                {{ item.allowedIp }}
                            </span>
                        </template>
                    </div>
                </div>
            </div>
        </div>

        <template v-if="hasUserInfo">
            <UserProfileEmplymReadSection
                :profile-rows="profileRows"
                :emplym-rows="emplymRows"
            />
            <div class="card-body">
                <div v-if="hasInfoItems" class="row mb-4">
                    <div class="col-xl-2 col-4">
                        <div class="col-form-label text-center fs-6 fw-bold">{{ t('txt.user.my.additional-info') }}</div>
                    </div>
                    <div class="col-xl-9 col-8 col-form-label" id="userItemListDiv">
                        <div v-for="item in u.userInfo?.itemList" :key="item.itemNm + item.itemCn" class="row mb-2 ps-2 border-bottom border-1 text-muted">
                            <div class="col-xl-2 col-3 text-start">{{ item.itemNm }}</div>
                            <div class="col-xl-4 col-4 text-start">{{ item.itemCn }}</div>
                            <div class="col-xl-4 col-4 text-start">{{ item.itemDc }}</div>
                        </div>
                    </div>
                </div>
            </div>
        </template>

        <div class="separator my-2"></div>

        <!-- 내 휴가 정보 -->
        <div v-if="v.visible" class="card-body">
            <!--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-3 col-12">
                    <div class="fs-6 fw-bold text-center">
                        {{ v.statsYy }}{{ t('txt.user.my.vacation-title-suffix') }}<br />
                        ({{ v.bgnDt }} ~ {{ v.endDt }})
                    </div>
                </div>
                <div class="col-xl-1 col-sm-4 col-form-label"
                     data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="v.tooltip">
                    <span class="cursor-help">{{ t('txt.user.my.total-vacation') }} : {{ v.total }}</span>
                </div>
                <div class="col-xl-1 col-sm-4 col-form-label">
                    {{ t('txt.user.my.used-vacation') }} : {{ v.used }}
                </div>
                <div class="col-xl-1 col-sm-4 col-form-label">
                    {{ t('txt.user.my.remains-vacation') }} : {{ v.remains }}
                </div>
            </div>
        </div>
    </div>
    `,
};
