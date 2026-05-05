/**
 * JournalDayMetaModalStdrdDt.ts
 * Handlebars `journal_day_stdrd_dt_partial`과 동일한 메타 모달 일자 마크업.
 */

const JournalDayMetaModalStdrdDt = {
    name: "JournalDayMetaModalStdrdDt",
    props: {
        day: { type: Object, required: true },
    },
    template: `
    <div :class="{ 'text-danger': day.isHolyday }" style="column-gap: .25rem">
        <i class="bi bi-calendar3 fs-6 me-1" :class="{ 'text-danger': day.isHolyday }"></i>
        {{ day.stdrdDt }}
        <span class="fs-8" :class="day.isHolyday ? 'text-danger' : 'text-gray-600'">({{ day.journalDateWeekDay }})</span>
        <span v-if="day.journalDatePrecision === 'APPROXIMATE'" class="badge badge-light-primary ms-2">{{ day.journalDatePrecision }}</span>
        <span v-if="day.journalDatePrecision === 'UNKNOWN'" class="badge badge-light-primary ms-2">{{ day.journalDatePrecision }}</span>
        <span class="fs-7 ms-4 text-muted" v-html="day.weather"></span>
        <div v-if="day.holydayNm" class="w-100 ps-5 fs-6 fw-normal text-truncate">{{ day.holydayNm }}</div>
    </div>
    `,
};

export default JournalDayMetaModalStdrdDt;
