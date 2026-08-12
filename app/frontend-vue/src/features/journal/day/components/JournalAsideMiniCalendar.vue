<template>
  <!--begin::미니 달력 (일간 view aside 전용)-->
  <div class="journal-aside-mini-calendar">
    <!--begin::요일 헤더-->
    <div class="mini-cal-header">
      <span
        v-for="(label, idx) in weekdayLabels"
        :key="label"
        :class="['mini-cal-cell mini-cal-weekday', { 'is-weekend': idx === 0 || idx === 6 }]"
      >
        {{ label }}
      </span>
    </div>
    <!--end::요일 헤더-->

    <!--begin::날짜 그리드-->
    <div class="mini-cal-grid">
      <template v-for="(cell, idx) in calendarCells" :key="idx">
        <button
          v-if="cell.day > 0"
          type="button"
          :class="['mini-cal-cell mini-cal-day', {
            'is-selected': cell.dateStr === selectedDate,
            'is-today': cell.dateStr === todayStr,
            'is-weekend': cell.isWeekend,
            'is-holiday': cell.isHoliday,
          }]"
          :title="cell.dateStr"
          @click="emit('select', cell.dateStr)"
        >
          {{ cell.day }}
        </button>
        <span v-else class="mini-cal-cell mini-cal-empty"></span>
      </template>
    </div>
    <!--end::날짜 그리드-->
  </div>
  <!--end::미니 달력-->
</template>

<script setup lang="ts">
/**
 * JournalAsideMiniCalendar — aside 사이드바에 표시하는 미니 달력.
 *
 * 해당 월의 날짜를 요일 기준 7열 그리드로 렌더링하며,
 * 날짜 클릭 시 `select` 이벤트(dateStr: 'YYYY-MM-DD')를 emit한다.
 *
 * @prop year - 표시할 연도
 * @prop month - 표시할 월 (1-based)
 * @prop selectedDate - 현재 선택된 날짜 문자열 ('YYYY-MM-DD'), 하이라이트용
 */
import { computed } from "vue";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

const props = defineProps<{
  year: number;
  month: number;
  selectedDate: string;
  /** 공휴일 날짜 문자열 배열 ('YYYY-MM-DD'). 해당 날짜를 빨간색으로 표시한다. */
  holidays?: string[];
}>();

const emit = defineEmits<{
  (e: "select", dateStr: string): void;
}>();

const { t } = useLocaleStore();

/** 오늘 날짜 문자열 (하이라이트 비교용) */
const todayStr = (() => {
  const d = new Date();
  const yy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  return `${yy}-${mm}-${dd}`;
})();

/** 요일 헤더 레이블 (일~토 순서) */
const weekdayLabels = computed(() => [
  t("common.weekday.sun"),
  t("common.weekday.mon"),
  t("common.weekday.tue"),
  t("common.weekday.wed"),
  t("common.weekday.thu"),
  t("common.weekday.fri"),
  t("common.weekday.sat"),
]);

interface CalendarCell {
  /** 날짜 (1~31). 빈 셀은 0 */
  day: number;
  /** 'YYYY-MM-DD' 문자열. 빈 셀은 빈 문자열 */
  dateStr: string;
  /** 주말 여부 (일요일 또는 토요일) */
  isWeekend: boolean;
  /** 공휴일 여부 */
  isHoliday: boolean;
}

/**
 * 해당 월의 달력 셀 배열을 생성한다.
 * 앞쪽 빈 칸(이전 월 영역) + 실제 날짜 + 뒤쪽 빈 칸으로 구성되어
 * 총 길이가 7의 배수가 된다.
 */
const calendarCells = computed<CalendarCell[]>(() => {
  const { year, month, holidays } = props;
  // 해당 월 1일의 요일 (0=일 ~ 6=토)
  const firstDayOfWeek = new Date(year, month - 1, 1).getDay();
  // 해당 월의 총 일수
  const daysInMonth = new Date(year, month, 0).getDate();
  // 공휴일 Set (O(1) 조회)
  const holidaySet = new Set(holidays ?? []);

  const cells: CalendarCell[] = [];

  // 앞쪽 빈 칸
  for (let i = 0; i < firstDayOfWeek; i++) {
    cells.push({ day: 0, dateStr: "", isWeekend: false, isHoliday: false });
  }

  // 실제 날짜
  const mm = String(month).padStart(2, "0");
  for (let d = 1; d <= daysInMonth; d++) {
    const dd = String(d).padStart(2, "0");
    const dateStr = `${year}-${mm}-${dd}`;
    const dayOfWeek = (firstDayOfWeek + d - 1) % 7; // 0=일, 6=토
    cells.push({
      day: d,
      dateStr,
      isWeekend: dayOfWeek === 0 || dayOfWeek === 6,
      isHoliday: holidaySet.has(dateStr),
    });
  }

  // 뒤쪽 빈 칸 (7의 배수 맞추기)
  const remainder = cells.length % 7;
  if (remainder > 0) {
    for (let i = 0; i < 7 - remainder; i++) {
      cells.push({ day: 0, dateStr: "", isWeekend: false, isHoliday: false });
    }
  }

  return cells;
});
</script>

<style scoped>
.journal-aside-mini-calendar {
  width: 100%;
}

.mini-cal-header,
.mini-cal-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
}

.mini-cal-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  aspect-ratio: 1;
  font-size: 0.75rem;
  border-radius: 4px;
}

.mini-cal-weekday {
  font-weight: 600;
  color: var(--bs-gray-600);
  font-size: 0.7rem;
}

.mini-cal-weekday.is-weekend {
  color: var(--bs-danger);
}

.mini-cal-day {
  border: none;
  background: transparent;
  cursor: pointer;
  color: var(--bs-gray-800);
  padding: 0;
  transition: background-color 0.15s;
}

.mini-cal-day.is-weekend {
  color: var(--bs-danger);
}

.mini-cal-day.is-holiday {
  color: var(--bs-danger);
}

.mini-cal-day:hover {
  background-color: var(--bs-gray-200);
}

.mini-cal-day.is-today {
  font-weight: 700;
  border: 1px solid var(--bs-primary);
}

.mini-cal-day.is-selected {
  background-color: var(--bs-primary);
  color: #fff;
  font-weight: 600;
}

.mini-cal-day.is-selected:hover {
  background-color: var(--bs-primary);
}

.mini-cal-empty {
  /* 빈 셀 — 공간만 차지 */
}
</style>
