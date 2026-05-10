import { LogListRow } from "../types.js";

export default {
    name: "LogListTable",
    props: {
        rows: { type: Array, required: true },
    },
    emits: ["open-detail"],
    methods: {
        rowClass(row: LogListRow): string {
            return row.isJobUser ? "cursor-pointer bg-fxd-list" : "cursor-pointer";
        },
        resultLabelClass(row: LogListRow): string {
            return row.rslt ? "text-success" : "text-danger";
        },
        resultLabel(row: LogListRow): string {
            return row.rslt
                ? this.$t("txt.admin.log.list.result.success")
                : this.$t("txt.admin.log.list.result.failure");
        },
    },
    template: `
        <template v-if="!(rows && rows.length)">
            <tr><td colspan="8" class="text-center">{{ $t('view.list.empty') }}</td></tr>
        </template>
        <template v-else>
            <tr
                v-for="row in rows"
                :key="row.id"
                :class="rowClass(row)"
                @click="$emit('open-detail', row.id)"
            >
                <td class="text-center hidden-table">{{ row.rnum }}</td>
                <td class="text-center">{{ row.logDt || '-' }}</td>
                <td class="text-center hidden-table">
                    <div>{{ row.logUserNm || '-' }}</div>
                    <div>({{ row.username || '-' }})</div>
                </td>
                <td class="text-center hidden-table">{{ row.ipAddr || '-' }}</td>
                <td class="text-center hidden-table">{{ row.actionTyNm || '-' }}</td>
                <td class="text-start ps-4">{{ row.requestUri || '-' }}</td>
                <td class="text-center">{{ row.rsltMsg || '-' }}</td>
                <td class="text-center text-end hidden-table">
                    <span :class="resultLabelClass(row)">{{ resultLabel(row) }}</span>
                </td>
            </tr>
        </template>
    `,
};

