/**
 * JournalDayList.ts
 * 저널 일자 Vue 목록 셸.
 */

import JournalDayCard from "./JournalDayCard.js";

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
            return typeof Message !== "undefined" && typeof Message.get === "function"
                ? Message.get("msg.rslt.empty")
                : "";
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
