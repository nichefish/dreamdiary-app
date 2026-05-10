import { LogStatsUserRow } from "../types.js";

export default {
    name: "LogStatsUserPlaceholderTable",
    props: {
        userRows: { type: Array, required: true },
        anonymousRows: { type: Array, required: true },
    },
    computed: {
        rows(): LogStatsUserRow[] {
            return [
                ...(this.userRows as LogStatsUserRow[]),
                ...(this.anonymousRows as LogStatsUserRow[]),
            ];
        },
    },
    methods: {
        resultLabel(row: LogStatsUserRow): string {
            return row.rslt === true || row.rslt === "true" ? "성공" : "실패";
        },
        resultClass(row: LogStatsUserRow): string {
            return row.rslt === true || row.rslt === "true" ? "text-success" : "text-danger";
        },
    },
    template: `
    <template v-if="rows.length">
        <tr v-for="row in rows" :key="String(row.rnum) + ':' + (row.username || '-') + ':' + (row.url || '-')">
            <td class="text-center hidden-table">{{ row.rnum }}</td>
            <td class="text-start ps-10 hidden-table">
                <span class="fw-bold">{{ row.userNm || '-' }}</span>
                <span class="ms-1">({{ row.username || '-' }})</span>
            </td>
            <td class="text-center">{{ row.roleName || '-' }}</td>
            <td class="text-center hidden-table">총 {{ row.actvtyCnt || 0 }}건</td>
            <td class="text-start text-break">{{ row.url || '-' }}</td>
            <td class="text-start text-break">{{ row.content || row.param || '-' }}</td>
            <td class="text-center text-end hidden-table">
                <span :class="resultClass(row)">
                    {{ resultLabel(row) }}
                    <i v-if="row.rslt === true || row.rslt === 'true'" class="bi bi-check text-success fs-8"></i>
                    <i v-else class="bi bi-x text-danger fs-8"></i>
                </span>
            </td>
        </tr>
    </template>
    <tr v-else>
        <td colspan="7" class="text-center py-10">
            <div class="fw-bold mb-2">사용자별 로그 통계는 준비 중입니다.</div>
            <div class="text-muted">현재 연결된 데이터가 있으면 이 표에 그대로 표시됩니다.</div>
        </td>
    </tr>
    `,
};
