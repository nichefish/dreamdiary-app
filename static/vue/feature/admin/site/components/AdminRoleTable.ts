import { AdminPageMeta, RoleRow } from "../types.js";

export default {
    name: "AdminRoleTable",
    props: {
        rows: { type: Array, required: true },
        meta: { type: Object, required: true },
    },
    computed: {
        m(): AdminPageMeta {
            return this.meta as AdminPageMeta;
        },
    },
    methods: {
        isMngr(row: RoleRow): boolean {
            return row.roleKey === this.m.authMngrKey;
        },
        isUser(row: RoleRow): boolean {
            return row.roleKey === this.m.authUserKey;
        },
        isDev(row: RoleRow): boolean {
            return row.roleKey === this.m.authDevKey;
        },
    },
    template: `
    <div id="admin_role_table_wrap" class="card post">
        <table class="table align-middle table-row-dashed fs-small gy-5 table-fixed mb-3">
            <thead>
                <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 text-muted">
                    <th class="col-2 text-center wb-keepall">{{ $t('txt.admin.site.role.col.role-key') }}</th>
                    <th class="col-2 text-center wb-keepall">{{ $t('txt.admin.site.role.col.role-name') }}</th>
                    <th class="col-2 text-center wb-keepall">{{ $t('txt.admin.site.role.col.sort-order') }}</th>
                    <th class="col-2 text-center wb-keepall">{{ $t('txt.admin.site.role.col.use-yn') }}</th>
                </tr>
            </thead>
            <tbody>
                <template v-if="!(rows && rows.length)">
                    <tr>
                        <td colspan="4" class="text-center">{{ $t('view.list.empty') }}</td>
                    </tr>
                </template>
                <template v-else>
                    <tr v-for="row in rows" :key="row.id">
                        <td class="text-center hidden-table">
                            <div class="text-muted fw-bold">{{ row.roleKey }}</div>
                        </td>
                        <td class="text-center">
                            <div class="d-flex-center">
                                <template v-if="isMngr(row)">
                                    <i class="bi bi-person-lines-fill text-info fs-2 me-2 opacity-75"
                                       data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                                       :title="row.roleName || '-'"></i>
                                    <div class="text-info fw-bold">{{ row.roleName }}</div>
                                    <div class="badge badge-info ms-3">{{ row.authLevel }}</div>
                                </template>
                                <template v-else-if="isUser(row)">
                                    <i class="bi bi-people-fill me-2 fs-2"
                                       data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                                       :title="row.roleName || '-'"></i>
                                    <div class="text-muted fw-bold">{{ row.roleName }}</div>
                                    <div class="badge badge-dark ms-3 opacity-50">{{ row.authLevel }}</div>
                                </template>
                                <template v-else-if="isDev(row)">
                                    <i class="bi bi-person-fill-gear text-info fs-2 me-2 opacity-75"
                                       data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                                       :title="row.roleName || '-'"></i>
                                    <div class="text-info fw-bold">
                                        <template v-if="row.parentRoleId != null">({{ row.parentRoleId }}) </template>{{ row.roleName }}
                                    </div>
                                    <div class="badge badge-info ms-3">{{ row.authLevel }}</div>
                                </template>
                            </div>
                        </td>
                        <td class="text-center hidden-table">{{ row.sortOrder }}</td>
                        <td class="text-center hidden-table">{{ row.useYn }}</td>
                    </tr>
                </template>
            </tbody>
        </table>
    </div>
    `,
};
