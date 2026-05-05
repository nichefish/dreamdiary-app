import { UserDetail, UserLabels } from "../types.js";

export default {
    name: "UserDetailProfileEmplymPanel",
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
    },
    methods: {
        fallback(value: string | null | undefined): string {
            return value || "-";
        },
    },
    template: `
    <template v-if="hasProfile">
        <!--begin::Menu separator-->
        <div class="separator my-2"></div>
        <!--end::Menu separator-->

        <!--begin::Card body-->
        <div class="card-body">
            <!--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2 col-4">
                    <div class="col-form-label text-center fw-bold">{{ l.profileAddress }}</div>
                </div>
                <div class="col-xl-2 col-8 col-form-label">
                    {{ fallback(user.profile?.addr) }} / {{ fallback(user.profile?.zipcode) }}
                </div>
            </div>
            <!--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2 col-4">
                    <div class="col-form-label text-center fw-bold">{{ l.profileBirthDate }}</div>
                </div>
                <div class="col-xl-2 col-8 col-form-label">
                    {{ fallback(user.profile?.brthdy) }}
                    <span v-if="user.profile?.lunarYn === 'Y'" class="badge badge-primary ms-5">{{ l.profileLunar }}</span>
                </div>
            </div>
            <!--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2 col-4">
                    <div class="col-form-label text-center fw-bold">{{ l.profileProfile }}</div>
                </div>
                <div class="col-xl-9 col-8 col-form-label">
                    <div class="div-textarea h-auto min-h-125px">{{ user.profile?.proflCn || '' }}</div>
                </div>
            </div>
        </div>
    </template>

    <template v-if="hasEmplym">
        <!--begin::Menu separator-->
        <div class="separator my-2"></div>
        <!--end::Menu separator-->

        <!--begin::Card body-->
        <div class="card-body">
            <!--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2 col-4">
                    <div class="col-form-label text-center fw-bold">{{ l.emplymUserName }}</div>
                </div>
                <div class="col-xl-2 col-8 col-form-label">
                    {{ fallback(user.emplym?.userNm) }}
                </div>
            </div>
            <!--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2 col-4">
                    <div class="col-form-label text-center fw-bold">{{ l.emplymEmail }}</div>
                </div>
                <div class="col-xl-2 col-8 col-form-label">
                    {{ fallback(user.emplym?.emplymEmail) }}
                </div>
            </div>
            <!--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2 col-4">
                    <div class="col-form-label text-center fw-bold">{{ l.emplymPhoneNumber }}</div>
                </div>
                <div class="col-xl-2 col-8 col-form-label">
                    {{ fallback(user.emplym?.emplymPhoneNumber) }}
                </div>
            </div>
            <!--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2 col-4">
                    <div class="col-form-label text-center fw-bold">{{ l.emplymAffiliation }}</div>
                </div>
                <div class="col-xl-2 col-8 col-form-label">
                    {{ fallback(user.emplym?.cmpyNm) }} / {{ fallback(user.emplym?.teamNm) }} / {{ fallback(user.emplym?.emplymNm) }}
                </div>
            </div>
            <!--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2 col-4">
                    <div class="col-form-label text-center fw-bold">{{ l.emplymRank }}</div>
                </div>
                <div class="col-xl-2 col-8 col-form-label">
                    {{ fallback(user.emplym?.rankNm) }}
                </div>
            </div>
            <!--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2 col-4">
                    <div class="col-form-label text-center fw-bold">{{ l.emplymJoinDate }}</div>
                </div>
                <div class="col-xl-2 col-8 col-form-label">
                    {{ fallback(user.emplym?.ecnyDt) }} / {{ fallback(user.emplym?.retireYn) }} / {{ fallback(user.emplym?.retireDt) }}
                </div>
            </div>
            <!--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2 col-4">
                    <div class="col-form-label text-center fw-bold">{{ l.emplymPayrollAccount }}</div>
                </div>
                <div class="col-xl-9 col-8 col-form-label">
                    <div class="div-textarea h-auto min-h-125px">{{ fallback(user.emplym?.acntBank) }} / {{ fallback(user.emplym?.acntNo) }}</div>
                </div>
            </div>
        </div>
    </template>
    `,
};
