<template>
  <!--begin::저널 달력 페이지 (레거시 journal_day_cal.ftlh + JournalDayCalApp.ts 이식)-->
  <div class="journal-day-calendar-page">
    <JournalDayViewToolbar />

    <!--begin::카드-->
    <div class="card post" style="margin-top: 0 !important;">
      <!--begin::태그 클라우드 헤더-->
      <div v-if="store.showTagCloud" class="card-header">
        <JournalTagCloudHeader />
      </div>
      <!--end::태그 클라우드 헤더-->
      <div class="card-body position-relative">
        <!--begin::로딩 (달력 DOM 을 유지해야 하므로 오버레이 방식)-->
        <div v-if="store.loading" class="journal-cal-loading">
          <span class="spinner-border spinner-border-sm me-2"></span>
          {{ t("journal.day.calendar.loading") }}
        </div>
        <!--end::로딩-->
        <FullCalendar ref="calendarRef" :options="calendarOptions" />
      </div>
    </div>
    <!--end::카드-->
  </div>
  <!--end::저널 달력 페이지-->
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from "vue";
import { useRoute } from "vue-router";
import FullCalendar from "@fullcalendar/vue3";
import dayGridPlugin from "@fullcalendar/daygrid";
import koLocale from "@fullcalendar/core/locales/ko";
import type { CalendarOptions, EventClickArg, EventContentArg, EventMountArg } from "@fullcalendar/core";
import { Tooltip } from "bootstrap";
import { useJournalStore } from "@/features/journal/stores/journal";
import { useJournalModalStore } from "@/features/journal/stores/journalModal";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import JournalDayViewToolbar from "./components/JournalDayViewToolbar.vue";
import JournalTagCloudHeader from "./components/JournalTagCloudHeader.vue";

const store = useJournalStore();
const modalStore = useJournalModalStore();
const localeStore = useLocaleStore();
const { t } = localeStore;
const route = useRoute();

const calendarRef = ref<any>(null);

const calendarOptions = computed<CalendarOptions>(() => ({
  plugins: [dayGridPlugin],
  initialView: "dayGridMonth",
  initialDate: new Date(store.yy, store.mnth - 1, 1),
  locale: localeStore.locale === "ko" ? koLocale : "en",
  height: "auto",
  // 레거시 동일: 달력 자체 내비게이션 없음(title 만) — 월 이동은 aside 에서 수행
  headerToolbar: { left: "", center: "title", right: "" },
  eventOverlap: false,
  events: store.calEventList.map((event) => ({ ...event, id: String(event.id) })),
  eventClick: onEventClick,
  eventContent: renderEventContent,
  eventDidMount: mountEventTooltip,
  eventWillUnmount: disposeEventTooltip,
}));

/** URL query 의 월간 기간 상태를 숫자로 복원한다. (JournalDayMonthly 와 동일 규칙) */
function parsePositiveInt(value: unknown): number | null {
  const raw = Array.isArray(value) ? value[0] : value;
  if (typeof raw !== "string" || !/^\d+$/.test(raw)) return null;
  const parsed = Number(raw);
  return Number.isSafeInteger(parsed) && parsed > 0 ? parsed : null;
}

function loadCalendarView(): void {
  store.setViewType("CAL");
  const yy = parsePositiveInt(route.query.yy);
  const mnth = parsePositiveInt(route.query.mnth);
  if (yy) store.yy = yy;
  if (mnth && mnth >= 1 && mnth <= 12) store.mnth = mnth;
  void store.fetchDays({ viewType: "CAL", yy: store.yy, mnth: store.mnth });
  if (store.showTagCloud) {
    void store.fetchTagCloud();
  }
}

/**
 * 이벤트 클릭 (레거시 handleEventClick 동일).
 * JOURNAL_DAY → 일자 상세 모달, JOURNAL_DIARY/JOURNAL_DREAM → 소속 일자 상세 모달.
 * 그 외(공휴일·행사 등 일정 이벤트)는 무동작.
 */
async function onEventClick(arg: EventClickArg): Promise<void> {
  const groupId = arg.event.groupId || "";
  if (groupId === "JOURNAL_DAY") {
    await modalStore.openDayDetail(Number(arg.event.id));
    return;
  }
  if (groupId === "JOURNAL_DIARY" || groupId === "JOURNAL_DREAM") {
    const journalDayId = Number(arg.event.extendedProps.journalDayId);
    if (journalDayId) await modalStore.openDayDetail(journalDayId);
  }
}

/** 이벤트 렌더 (레거시 renderEventContent 동일 — icon HTML + title, 중요 엔트리는 magenta blink) */
function renderEventContent(arg: EventContentArg): { html: string } | string {
  const groupId = arg.event.groupId || "";
  const icon = (arg.event.extendedProps.icon as string) ?? "";
  const titleWithIcon = `${icon} ${arg.event.title}`;
  switch (groupId) {
    case "JOURNAL_DAY":
      return { html: `<div class='cursor-pointer text-truncate'>${titleWithIcon}</div>` };
    case "JOURNAL_DIARY":
    case "JOURNAL_DREAM": {
      const isImprtc = arg.event.extendedProps.imprtcYn === "Y";
      const classStr = isImprtc ? "text-magenta blink fw-bold text-truncate" : "";
      return { html: `<div class='${classStr}'>${titleWithIcon}</div>` };
    }
    default:
      return icon ? { html: `<div class='text-truncate'>${titleWithIcon}</div>` } : arg.event.title;
  }
}

/**
 * 이벤트 툴팁 마운트 (레거시 mountEventTooltip 동일 — DAY 는 제목, DIARY/DREAM 은 마크다운 본문).
 * 변경 전: jQuery tooltip + 페이지 전역 .tooltip 광폭 스타일.
 * 변경 후: bootstrap Tooltip + customClass 로 이 화면 툴팁에만 광폭 스타일 한정 (전역 오염 방지).
 */
function mountEventTooltip(arg: EventMountArg): void {
  const groupId = arg.event.groupId || "";
  let tooltipContent: string | undefined;
  switch (groupId) {
    case "JOURNAL_DAY":
      tooltipContent = arg.event.title;
      break;
    case "JOURNAL_DIARY":
      tooltipContent = `<div class="journal-diary-content">${arg.event.extendedProps.markdownContent ?? ""}</div>`;
      break;
    case "JOURNAL_DREAM":
      tooltipContent = `<div class="journal-dream-content">${arg.event.extendedProps.markdownContent ?? ""}</div>`;
      break;
    default:
      break;
  }
  if (!tooltipContent) return;
  new Tooltip(arg.el, {
    title: tooltipContent,
    html: true,
    placement: "top",
    trigger: "hover",
    customClass: "journal-cal-tooltip",
  });
}

/** 이벤트 언마운트 시 툴팁 해제 (레거시엔 없던 위생 처리 — SPA 재렌더 시 잔존 인스턴스 방지) */
function disposeEventTooltip(arg: EventMountArg): void {
  Tooltip.getInstance(arg.el)?.dispose();
}

/** 레거시 normalizeFullCalendarHarness 동일 — 음수 margin-top 으로 겹쳐 보이는 이벤트 하네스 보정 */
function normalizeFullCalendarHarness(): void {
  document.querySelectorAll<HTMLElement>(".fc-daygrid-event-harness").forEach((el) => {
    if (parseInt(el.style.marginTop, 10) < 0) el.style.marginTop = "";
  });
}

// aside 월 이동(store.yy/mnth 갱신) 시 달력 표시 월 동기화 (레거시 moveMonth 의 gotoDate 동일)
watch(
  () => [store.yy, store.mnth] as const,
  ([yy, mnth]) => {
    calendarRef.value?.getApi()?.gotoDate(new Date(yy, mnth - 1, 1));
  },
);

// 이벤트 목록 갱신 후 하네스 보정 (레거시 refreshEventList 후처리 동일)
watch(
  () => store.calEventList,
  async () => {
    await nextTick();
    normalizeFullCalendarHarness();
  },
);

onMounted(() => {
  /* 챕터 카테고리를 화면 로드 시점에 미리 캐시해 모달 오픈 시 로딩 없이 사용한다. */
  void modalStore.prefetchChapterCategories();
});

watch(
  () => route.fullPath,
  () => {
    if (route.name === "journal-calendar") {
      loadCalendarView();
    }
  },
  { immediate: true },
);
</script>

<style scoped>
.journal-cal-loading {
  position: absolute;
  top: 1rem;
  right: 1.5rem;
  z-index: 3;
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
  background: var(--bs-body-bg);
  box-shadow: 0 0.25rem 1rem rgba(15, 23, 42, 0.12);
  color: var(--bs-gray-700);
}
</style>

<style>
/* 레거시 journal_day_cal.ftlh 페이지 <style> 동일 — 툴팁 광폭 표시.
   변경 전: 페이지 전역 .tooltip 대상. 변경 후: customClass(journal-cal-tooltip)로 이 화면 툴팁에만 한정. */
.tooltip.journal-cal-tooltip {
  min-width: 900px !important;
  max-width: 1200px !important;
  width: auto !important;
}
.tooltip.journal-cal-tooltip .tooltip-inner {
  text-align: left;
  min-width: 900px !important;
  max-width: 1200px !important;
  max-height: 900px !important;
  overflow-y: auto !important;
  word-break: break-word !important;
  white-space: normal !important;
  word-wrap: break-word !important;
}
</style>
