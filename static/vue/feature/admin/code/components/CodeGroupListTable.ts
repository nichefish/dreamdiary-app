import codeAdminUiService from "../services/codeAdminUiService.js";
import { CodeGroupRow } from "../types.js";

export default {
    name: "CodeGroupListTable",
    props: {
        rows: { type: Array, required: true },
    },
    emits: ["open-detail", "toggle-use", "delete-group"],
    methods: {
        isUseYn(row: CodeGroupRow): boolean {
            return codeAdminUiService.isYn(row.useYn);
        },
    },
    updated(): void {
        this.$nextTick((): void => codeAdminUiService.syncTooltips("#code_group_list_div"));
    },
    mounted(): void {
        this.$nextTick((): void => codeAdminUiService.syncTooltips("#code_group_list_div"));
    },
    template: `
    <template v-if="rows.length === 0">
        <tr><td colspan="6" class="text-center">{{ $t('view.list.empty') }}</td></tr>
    </template>
    <template v-else>
        <tr
            v-for="row in rows"
            :key="row.id"
            class="code-group-item bg-hover-secondary"
            :id="'code-group-' + row.id"
            :data-id="row.id"
            :data-group-code="row.groupCode"
            :data-use-yn="isUseYn(row) ? 'Y' : 'N'"
            @click="$emit('open-detail', row.id)"
        >
            <td class="text-center hidden-table">{{ row.rnum }}</td>
            <td class="text-start px-6 cursor-pointer fw-bold text-gray-700">
                <span>{{ row.groupName }}</span>
                <span class="badge bg-light-info text-info ms-1">{{ row.groupCode }}</span>
            </td>
            <td class="text-start ps-6 hidden-table">{{ row.description }}</td>
            <td class="text-center hidden-table">{{ row.codeItemCnt || 0 }}</td>
            <td class="text-center hidden-table">
                <div class="badge cursor-pointer"
                    :class="isUseYn(row) ? 'badge-success' : 'badge-secondary btn-white'"
                    @click.stop="$emit('toggle-use', row.id)"
                    data-bs-toggle="tooltip"
                    data-bs-placement="top"
                    data-bs-dismiss="click"
                    :title="$t('bs.tooltip.admin.code.group.list.toggle-use')">
                    <i class="bi bi-check text-light blink-slow"></i>
                    {{ isUseYn(row) ? $t('txt.status.use') : $t('txt.status.unuse') }}
                </div>
            </td>
            <td class="text-center hidden-table">
                <div class="badge badge-danger badge-outlined cursor-pointer"
                    @click.stop="$emit('delete-group', row.id)"
                    data-bs-toggle="tooltip"
                    data-bs-placement="top"
                    data-bs-dismiss="click"
                    :title="$t('bs.tooltip.del')">
                    <i class="bi bi-trash text-danger fs-7 blink-slow"></i>
                    {{ $t('txt.comm.del') }}
                </div>
            </td>
        </tr>
    </template>
    `,
};

