/**
 * JournalDayList.ts
 * 저널 일자 Vue 목록 셸.
 */

import JournalDayCard from "./JournalDayCard.js";
// 변경(D): 글로벌 `Message` 결의를 `resolveMessage` 헬퍼로 위임 — typeof guard 분기 통일(미정의 시 "" 폴백 유지를 위해 fallback="" 명시).
import { resolveMessage } from "../../../../common/messageHelper.js";

const JournalDayList = {
    name: "JournalDayList",
    components: {
        JournalDayCard,
    },
    props: {
        model: { type: Object, required: true },
    },
    computed: {
        rows(): Record<string, any>[] {
            return Array.isArray(this.model?.list) ? this.model.list : [];
        },
        showDiaries(): boolean {
            return this.model?.showDiaries !== false;
        },
        showDreams(): boolean {
            return this.model?.showDreams !== false;
        },
        emptyText(): string {
            // 변경 전 폴백("")을 보존하기 위해 fallback="" 명시.
            return resolveMessage("msg.rslt.empty", "");
        },
    },
    template: `
    <div class="journal-day-vue-render-root">
        <template v-if="rows.length > 0">
            <JournalDayCard
                v-for="day in rows"
                :key="day.id || day.stdrdDt"
                :day="day"
                :show-diaries="showDiaries"
                :show-dreams="showDreams"
            />
        </template>
        <template v-else>
            <div class="journal-day d-flex-center">{{ emptyText }}</div>
        </template>
    </div>
    `,
};

export default JournalDayList;
