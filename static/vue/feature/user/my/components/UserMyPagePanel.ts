import { UserMyLabels, UserMyPage, UserMyRole, UserMyVacation } from "../types.js";

export default {
    name: "UserMyPagePanel",
    props: {
        user: { type: Object, required: true },
        vacation: { type: Object, required: true },
        labels: { type: Object, required: true },
    },
    emits: ["upload-profile-image", "remove-profile-image", "open-password-change"],
    computed: {
        u(): UserMyPage {
            return this.user as UserMyPage;
        },
        v(): UserMyVacation {
            return this.vacation as UserMyVacation;
        },
        l(): UserMyLabels {
            return this.labels as UserMyLabels;
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
    },
    methods: {
        roleIconClass(role: UserMyRole): string {
            // 관리자
            if (role.roleKey === "MNGR") return "bi bi-person-lines-fill text-info ms-1 opacity-75";
            // 사용자
            if (role.roleKey === "USER") return "bi bi-people-fill ms-1";
            // 개발자
            if (role.roleKey === "DEV") return "bi bi-person-fill-gear ms-1";
            return "bi bi-person ms-1";
        },
        fallback(value: string | null | undefined): string {
            return value || "-";
        },
    },
    template: `
    <div class="card post">
        <!--begin::Card body-->
        <div class="card-body">
            <!--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2 col-4 d-flex-center">
                    <label class="fs-6 fw-bold">{{ l.username }}</label>
                </div>
                <div class="col-xl-2 col-8 col-form-label">
                    <div class="btn btn-icon btn-light-primary position-relative w-30px h-30px w-md-40px h-md-40px me-3 cursor-default">
                        <!-- TODO: 원본 프로필 이미지 조회 -->
                        <label class="position-absolute top-0 start-100 translate-middle badge badge-sm badge-circle badge-light-primary opacity-hover cursor-pointer"
                               @click="$emit('upload-profile-image')"
                               data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="l.uploadProfileImageTooltip">
                            <i class="bi bi-pen icon-xs text-primary"></i>
                        </label>
                        <span class="position-absolute top-100 start-100 translate-middle badge badge-sm badge-circle badge-light-danger opacity-hover cursor-pointer"
                              @click="$emit('remove-profile-image')"
                              data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="l.removeProfileImageTooltip">
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
                    <div class="col-form-label text-center fs-6 fw-bold">{{ l.password }}</div>
                </div>
                <div class="col-xl-2 col-5">
                    <button type="button" class="btn btn-sm btn-secondary"
                            @click="$emit('open-password-change')"
                            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="l.passwordChangeTooltip">
                        {{ l.passwordChange }}
                    </button>
                </div>
            </div>
            <!--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2 col-4">
                    <div class="col-form-label text-center fs-6 fw-bold"><label for="roleName">{{ l.role }}</label></div>
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
                    <div class="col-form-label text-center fs-6 fw-bold"><label for="nickname">{{ l.nickname }}</label></div>
                </div>
                <div class="col-xl-2 col-8 col-form-label">{{ u.nickname }}</div>
            </div>
            <!--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2 col-4">
                    <div class="col-form-label text-center fs-6 fw-bold">
                        <label for="allowedIpListStr">{{ l.allowedIp }}</label>
                    </div>
                </div>
                <div class="col-xl-9 col-8 col-form-label">
                    <div class="form-check form-switch form-check-custom form-check-solid">
                        <span class="me-8">{{ u.isAllowedIpY ? l.use : l.unuse }}</span>
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
            <div class="separator my-2"></div>

            <!--begin::Card body-->
            <div class="card-body">
                <!--begin::Row-->
                <div class="row mb-4">
                    <div class="col-xl-2 col-4">
                        <div class="col-form-label text-center fs-6 fw-bold"><label for="phoneNumber">{{ l.department }}</label></div>
                    </div>
                    <div class="col-xl-2 col-8 col-form-label">
                        {{ fallback(u.userInfo?.cmpyNm) }} / {{ fallback(u.userInfo?.teamNm) }} / {{ fallback(u.userInfo?.emplymNm) }}
                    </div>
                </div>
                <!--begin::Row-->
                <div class="row mb-4">
                    <div class="col-xl-2 col-4">
                        <div class="col-form-label text-center fs-6 fw-bold">{{ l.rank }}</div>
                    </div>
                    <div class="col-xl-2 col-8 col-form-label">
                        {{ fallback(u.userInfo?.rankNm) }}
                        <span v-if="u.userInfo?.rankCd === 'STAFF' && u.userInfo?.apntcYn === 'Y'" class="text-muted">({{ l.probation }})</span>
                    </div>
                </div>
                <!--begin::Row-->
                <div class="row mb-4">
                    <div class="col-xl-2 col-4">
                        <div class="col-form-label text-center fs-6 fw-bold">{{ l.joinDate }}</div>
                    </div>
                    <div class="col-xl-2 col-8 col-form-label">{{ fallback(u.userInfo?.ecnyDt) }}</div>
                    <template v-if="hasRetired">
                        <div class="col-xl-2 col-4">
                            <div class="col-form-label text-center fs-6 fw-bold">{{ l.retireDate }}</div>
                        </div>
                        <div class="col-xl-2 col-4 col-form-label">{{ fallback(u.userInfo?.retireDt) }}</div>
                    </template>
                </div>
                <!--begin::Row-->
                <div class="row mb-4">
                    <div class="col-xl-2 col-4">
                        <div class="col-form-label text-center fs-6 fw-bold">{{ l.phoneNumber }}</div>
                    </div>
                    <div class="col-xl-2 col-8 col-form-label">{{ fallback(u.phoneNumber) }}</div>
                </div>
                <!--begin::Row-->
                <div class="row mb-4">
                    <div class="col-xl-2 col-4">
                        <div class="col-form-label text-center fs-6 fw-bold">{{ l.email }}</div>
                    </div>
                    <div class="col-xl-2 col-8 col-form-label">{{ fallback(u.email) }}</div>
                </div>
                <!--begin::Row-->
                <div class="row mb-4">
                    <div class="col-xl-2 col-4">
                        <div class="col-form-label text-center fs-6 fw-bold">{{ l.birthDate }}</div>
                    </div>
                    <div class="col-xl-2 col-8 col-form-label">{{ fallback(u.userInfo?.brthdy) }}</div>
                </div>
                <!--begin::Row-->
                <div class="row mb-4">
                    <div class="col-xl-2 col-4">
                        <div class="col-form-label text-center fs-6 fw-bold">{{ l.accountNumber }}</div>
                    </div>
                    <div class="col-xl-8 col-8 col-form-label">
                        {{ fallback(u.userInfo?.acntBank) }} <span class="px-3">|</span> {{ fallback(u.userInfo?.acntNo) }}
                    </div>
                </div>
                <div v-if="hasInfoItems" class="row mb-4">
                    <div class="col-xl-2 col-4">
                        <div class="col-form-label text-center fs-6 fw-bold">{{ l.additionalInfo }}</div>
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
                        {{ v.statsYy }}{{ l.vacationTitleSuffix }}<br />
                        ({{ v.bgnDt }} ~ {{ v.endDt }})
                    </div>
                </div>
                <div class="col-xl-1 col-sm-4 col-form-label"
                     data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="v.tooltip">
                    <span class="cursor-help">{{ l.totalVacation }} : {{ v.total }}</span>
                </div>
                <div class="col-xl-1 col-sm-4 col-form-label">
                    {{ l.usedVacation }} : {{ v.used }}
                </div>
                <div class="col-xl-1 col-sm-4 col-form-label">
                    {{ l.remainsVacation }} : {{ v.remains }}
                </div>
            </div>
        </div>
    </div>
    `,
};
