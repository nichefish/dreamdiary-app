import { UserDetail, UserLabels, UserRoleRow } from "../types.js";

export default {
    name: "UserDetailPanel",
    props: {
        detail: { type: Object, required: true },
        labels: { type: Object, required: true },
    },
    emits: ["password-reset"],
    computed: {
        user(): UserDetail {
            return this.detail as UserDetail;
        },
        l(): UserLabels {
            return this.labels as UserLabels;
        },
    },
    methods: {
        roleIconClass(role: UserRoleRow): string {
            // 관리자
            if (role.roleKey === "MNGR") return "bi bi-person-lines-fill text-info ms-1 opacity-75";
            // 사용자
            if (role.roleKey === "USER") return "bi bi-people-fill ms-1";
            // 개발자
            if (role.roleKey === "DEV") return "bi bi-person-fill-gear ms-1";
            return "bi bi-person ms-1";
        },
    },
    template: `
    <div class="card-body">
        <!--begin::Row-->
        <div class="row mb-4">
            <div class="col-xl-2 col-4 d-flex-center">
                <label class="fw-bold">아이디</label>
            </div>
            <div class="col-xl-2 col-8 col-form-label">
                <div class="btn btn-icon btn-secondary position-relative w-30px h-30px w-md-40px h-md-40px me-3">
                    <img v-if="user.profileImageUrl" :src="user.profileImageUrl" class="img-thumbnail p-0 w-100" />
                    <span v-else class="svg-icon svg-icon-1">
                        <i class="fas fa-user-circle fs-2"></i>
                    </span>
                </div>
                {{ user.username }}
            </div>
            <div class="col-xl-2 col-4">
                <div class="col-form-label text-center fw-bold">등록자</div>
            </div>
            <div class="col-xl-6 col-8 pc-d-flex col-form-label">
                <div class="col-xl-2 col-12">{{ user.createdBy }}</div>
                <div class="col-xl-6 col-12">({{ user.createdAt }})</div>
            </div>
        </div>
        <!--begin::Row-->
        <div class="row mb-4">
            <div class="col-xl-2 d-flex-center">
                <label class="fw-bold">비밀번호</label>
            </div>
            <div class="col-xl-2">
                <button
                    type="button"
                    class="btn btn-sm btn-secondary"
                    @click="$emit('password-reset')"
                    data-bs-toggle="tooltip"
                    data-bs-placement="top"
                    data-bs-dismiss="click"
                    :title="l.passwordResetTooltip"
                >
                    비밀번호 초기화
                </button>
            </div>
            <div class="col-xl-8"></div>
        </div>
        <!--begin::Row-->
        <div class="row mb-4">
            <div class="col-xl-2 col-4">
                <div class="col-form-label text-center fw-bold">
                    <label for="roleName">권한</label>
                </div>
            </div>
            <div class="col-xl-2 col-8 col-form-label">
                <template v-for="role in user.userRoles" :key="role.roleKey">
                    <i :class="roleIconClass(role)"></i>
                    {{ role.roleName }}
                </template>
            </div>
        </div>
        <!--begin::Row-->
        <div class="row mb-4">
            <div class="col-xl-2 col-4">
                <div class="col-form-label text-center fw-bold">
                    <label for="nickname">표시이름</label>
                </div>
            </div>
            <div class="col-xl-2 col-8 col-form-label">{{ user.nickname }}</div>
        </div>
        <!--begin::Row-->
        <div class="row mb-4">
            <div class="col-xl-2 col-4">
                <div class="col-form-label text-center fw-bold">
                    <label for="email">이메일</label>
                </div>
            </div>
            <div class="col-xl-2 col-8 col-form-label">{{ user.email }}</div>
        </div>
        <!--begin::Row-->
        <div class="row mb-4">
            <div class="col-xl-2 col-4">
                <div class="col-form-label text-center fw-bold">
                    <label for="phoneNumber">연락처</label>
                </div>
            </div>
            <div class="col-xl-2 col-8 col-form-label">{{ user.phoneNumber }}</div>
        </div>
        <!--begin::Row-->
        <div class="row mb-4">
            <div class="col-xl-2 col-4">
                <div class="col-form-label text-center fw-bold">잠금여부</div>
            </div>
            <div class="col-xl-9 col-8 col-form-label">
                <div v-if="user.isLocked" class="text-danger">
                    {{ l.locked }}<i class="bi bi-lock fs-9 text-danger"></i>
                </div>
                <div v-else class="text-success">
                    {{ l.use }}<i class="bi bi-check text-success"></i>
                </div>
            </div>
        </div>
        <!--begin::Row-->
        <div class="row mb-4">
            <div class="col-xl-2 col-4">
                <div class="col-form-label text-center fw-bold">
                    <label for="allowedIpListStr">접속IP</label>
                </div>
            </div>
            <div class="col-xl-2 col-4">
                <div class="col-form-label">
                    <div v-if="user.useAllowedIp" class="text-success">
                        {{ l.use }}<i class="bi bi-check text-success"></i>
                    </div>
                    <div v-else class="text-muted">
                        {{ l.unuse }}<i class="bi bi-dash fs-9 text-muted"></i>
                    </div>
                </div>
                <div class="form-check form-switch form-check-custom form-check-solid gap-4">
                    <template v-if="user.useAllowedIp">
                        <span v-for="item in user.allowedIpList" :key="item.id || item.allowedIp" class="div-textarea div-height-1 px-5">
                            {{ item.allowedIp }}
                        </span>
                    </template>
                </div>
            </div>
        </div>
        <!--begin::Row-->
        <div class="row">
            <div class="col-xl-2 col-4">
                <div class="col-form-label text-center fw-bold">계정 설명</div>
            </div>
            <div class="col-xl-9 col-8 col-form-label">
                <div class="div-textarea h-auto min-h-125px">{{ user.content }}</div>
            </div>
        </div>
    </div>
    `,
};
