import boardAdminUiService from "../services/boardAdminUiService.js";
import { BoardRow } from "../types.js";

export default {
    name: "BoardListTable",
    props: {
        rows: { type: Array, required: true },
    },
    emits: ["modify-board", "toggle-use", "delete-board"],
    methods: {
        isUseYn(row: BoardRow): boolean {
            return boardAdminUiService.isYn(row.useYn);
        },
        formatPostCount(row: BoardRow): string {
            const n = row.postCount;
            if (n === undefined || n === null || Number.isNaN(Number(n))) {
                return "0";
            }
            return String(Number(n));
        },
    },
    updated(): void {
        this.$nextTick((): void => boardAdminUiService.syncTooltips("#board_group_list_div"));
    },
    mounted(): void {
        this.$nextTick((): void => boardAdminUiService.syncTooltips("#board_group_list_div"));
    },
    template: `
        <template v-if="!(rows && rows.length)">
            <tr><td colspan="7" class="text-center">{{ $t('view.list.empty') }}</td></tr>
        </template>
        <template v-else>
        <tr
            v-for="row in rows"
            :key="row.id"
            :id="'board-' + row.id"
            :data-id="row.id"
            class="sortable-item draggable"
        >
            <td class="text-center hidden-table">
                <div class="draggable-handle d-flex-center cursor-move pt-2"
                     data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                     :title="$t('bs.tooltip.board.group.list.drag-reorder')">
                    <i class="ki-duotone ki-abstract-14 fs-2x"><span class="path1"></span><span class="path2"></span></i>
                </div>
            </td>
            <td class="text-start ps-15 fw-bold text-gray-700">
                <span class="fw-bold">{{ row.boardName }}</span>
                <span class="ms-1">({{ row.boardKey }})</span>
                <a class="badge badge-secondary ms-2 p-2 btn-primary blank blink-slow float-end"
                   href="javascript:void(0);"
                   data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                   :title="$t('bs.tooltip.board.group.list.open-edit')"
                   @click.prevent="$emit('modify-board', row.id)">
                    <i class="bi bi-pencil-square fs-5 text-noti"></i>
                </a>
            </td>
            <td class="text-center hidden-table text-gray-700">{{ row.categoryGroupCode }}</td>
            <td class="text-center hidden-table">
                <div class="w-100 text-truncate">{{ row.description }}</div>
            </td>
            <td class="text-center hidden-table text-gray-700">{{ formatPostCount(row) }}</td>
            <td class="text-center hidden-table">
                <div v-if="isUseYn(row)" class="badge badge-success cursor-pointer"
                     data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                     :title="$t('bs.tooltip.board.group.list.toggle-use')"
                     @click="$emit('toggle-use', row.id, true)">
                    <i class="bi bi-check text-light blink-slow"></i>{{ $t('txt.status.use') }}
                </div>
                <div v-else class="badge badge-secondary cursor-pointer"
                     data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                     :title="$t('bs.tooltip.board.group.list.toggle-use')"
                     @click="$emit('toggle-use', row.id, false)">
                    <i class="bi bi-x blink-slow"></i>{{ $t('txt.status.unuse') }}
                </div>
            </td>
            <td class="text-center hidden-table">
                <div class="badge badge-danger badge-outlined cursor-pointer"
                     data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                     :title="$t('bs.tooltip.del')"
                     @click="$emit('delete-board', row.id)">
                    <i class="bi bi-trash text-danger"></i>{{ $t('txt.comm.del') }}
                </div>
            </td>
        </tr>
        </template>
    `,
};
