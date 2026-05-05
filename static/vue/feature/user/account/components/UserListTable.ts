import { UserLabels, UserRoleRow, UserRow } from "../types.js";

export default {
    name: "UserListTable",
    props: {
        rows: { type: Array, required: true },
        labels: { type: Object, required: true },
    },
    emits: ["open-detail"],
    computed: {
        items(): UserRow[] {
            return this.rows as UserRow[];
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
        userStatusIconClass(row: UserRow): string {
            if (row.userProflYn === "N") return "bi bi-person-dash vertical-middle text-muted";
            if (row.retireYn === "Y") return "bi bi-person-dash vertical-middle text-danger";
            return "bi bi-person-check vertical-middle text-success";
        },
        userStatusTooltip(row: UserRow): string {
            if (row.userProflYn === "N") return this.l.noProfile;
            if (row.retireYn === "Y") return this.l.retired;
            return this.l.activeEmployee;
        },
    },
    template: `
    <template v-if="items.length">
        <tr
            v-for="row in items"
            :key="row.id"
            class="bg-hover-secondary"
            :class="{ 'bg-light': row.isMe, 'border-top-5': row.divide }"
            @click="$emit('open-detail', row.id)"
        >
            <td class="text-center hidden-table">{{ row.rnum }}</td>
            <td class="text-center hidden-table">
                <template v-for="role in row.userRoles" :key="role.roleKey">
                    <i :class="roleIconClass(role)"></i>
                    {{ role.roleName }}
                </template>
            </td>
            <td class="text-start ps-8">
                <a href="javascript:void(0);" class="text-dark">
                    <div
                        v-if="row.profileImageUrl"
                        class="btn btn-icon btn-active-light-primary position-relative w-15px h-15px w-md-20px h-md-20px me-1"
                    >
                        <img :src="row.profileImageUrl" class="img-thumbnail p-0 w-100" />
                    </div>
                    <span class="pe-1">{{ row.userNm || "-" }} ({{ row.username || "-" }})</span>
                </a>
                <i
                    :class="userStatusIconClass(row)"
                    data-bs-toggle="tooltip"
                    data-bs-placement="top"
                    data-bs-dismiss="click"
                    :title="userStatusTooltip(row)"
                ></i>
            </td>
            <td class="text-center hidden-table">{{ row.cmpyNm || "-" }}</td>
            <td class="text-center hidden-table">{{ row.teamNm || "-" }}</td>
            <td class="text-center">
                {{ row.rankNm || "-" }}
                <span v-if="row.rankNm === '사원' && row.apntcYn === 'Y'" class="text-muted">({{ l.probation }})</span>
            </td>
            <td class="text-start ps-8 hidden-table">{{ row.email || "-" }}</td>
            <td class="text-center hidden-table">
                <span v-if="row.isLocked" class="text-danger">
                    {{ l.locked }}<i class="bi bi-lock fs-9 text-danger"></i>
                </span>
                <span v-else class="text-success">
                    {{ l.use }}<i class="bi bi-check text-success"></i>
                </span>
            </td>
        </tr>
    </template>
    <tr v-else>
        <td colspan="8" class="text-center">{{ l.emptyList }}</td>
    </tr>
    `,
};
