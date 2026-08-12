<template>
  <!--begin::저널 사이드 패널 (년월 이동 + 필터)-->
  <div class="journal-aside card card-reset card-p-0 p-5" style="width:280px; min-width:280px; max-width:280px;">
    <div class="d-flex justify-content-end mb-2">
      <button
        type="button"
        class="btn btn-sm btn-icon btn-light"
        :title="t('journal.aside.close.tooltip')"
        @click="asideStore.hide()"
      >
        <i class="bi bi-x-lg"></i>
      </button>
    </div>

    <!--begin::필터 카드 헤더 (레거시 journal_aside_header)-->
    <div id="journal_aside_header" class="card-header min-h-auto mb-5 px-0 border-0">
      <h3 class="card-title text-gray-900 fw-bold fs-3 mb-0">
        <i class="bi bi-filter fs-2 me-1"></i> {{ t("journal.aside.title.filter") }}
      </h3>
      <div class="card-toolbar">
        <button
          type="button"
          class="btn btn-sm btn-icon btn-color-gray-500 btn-light"
          :title="t('journal.aside.sort.tooltip.short')"
          @click="store.toggleSort()"
        >
          <i :class="sortIconClass" id="sortIcon" class="fs-2 pe-0"></i>
        </button>
      </div>
    </div>
    <!--end::필터 카드 헤더-->

    <!--begin::내비게이션 (월간/주간 분기)-->
    <div class="card-body p-0 d-flex flex-column gap-3">
      <!--begin::연도 선택 (공통)-->
      <select
        class="form-select form-select-sm"
        :value="store.yy"
        @change="onYyChange"
      >
        <option v-for="y in yyOptions" :key="y" :value="y">{{ y }}</option>
      </select>
      <!--end::연도 선택-->

      <!--begin::일간 미니 달력 (DAILY)-->
      <template v-if="store.viewType === 'DAILY'">
        <!--begin::월 이동 컨트롤-->
        <div class="d-flex align-items-center justify-content-between">
          <button type="button" class="btn btn-sm btn-icon btn-light" @click="navigateMonth(-1)">
            <i class="bi bi-chevron-left"></i>
          </button>
          <span class="fw-bold fs-6">{{ store.mnth }}{{ t("date.suffix.after-month-number") }}</span>
          <button type="button" class="btn btn-sm btn-icon btn-light" @click="navigateMonth(1)">
            <i class="bi bi-chevron-right"></i>
          </button>
        </div>
        <!--end::월 이동 컨트롤-->

        <!--begin::미니 달력 그리드-->
        <JournalAsideMiniCalendar
          :year="store.yy"
          :month="store.mnth"
          :selected-date="dailySelectedDate"
          :holidays="miniCalHolidays"
          @select="onMiniCalendarSelect"
        />
        <!--end::미니 달력 그리드-->
      </template>
      <!--end::일간 미니 달력-->

      <!--begin::월 내비게이션 (MONTHLY/CAL/LIST)-->
      <template v-else-if="store.viewType !== 'WEEKLY'">
        <!--begin::월 이동 컨트롤-->
        <div class="d-flex align-items-center justify-content-between">
          <button type="button" class="btn btn-sm btn-icon btn-light" @click="navigateMonth(-1)">
            <i class="bi bi-chevron-left"></i>
          </button>
          <span class="fw-bold fs-6">{{ store.mnth }}{{ t("date.suffix.after-month-number") }}</span>
          <button type="button" class="btn btn-sm btn-icon btn-light" @click="navigateMonth(1)">
            <i class="bi bi-chevron-right"></i>
          </button>
        </div>
        <!--end::월 이동 컨트롤-->

        <!--begin::월 그리드-->
        <div class="d-grid gap-1" style="grid-template-columns: repeat(3, 1fr);">
          <button
            v-for="m in 12"
            :key="m"
            type="button"
            :class="['btn btn-sm', m === store.mnth ? 'btn-primary' : 'btn-light']"
            @click="gotoYyMnth(store.yy, m)"
          >
            {{ m }}{{ t("date.suffix.after-month-number") }}
          </button>
        </div>
        <!--end::월 그리드-->
      </template>
      <!--end::월 내비게이션-->

      <!--begin::주 내비게이션 (WEEKLY)-->
      <template v-else>
        <!--begin::주간 범위 + 이동-->
        <div class="d-flex align-items-center justify-content-between position-relative">
          <button type="button" class="btn btn-sm btn-icon btn-light" @click="navigateWeek(-1)">
            <i class="bi bi-chevron-left"></i>
          </button>
          <span
            class="fw-bold fs-7 text-center text-hover-primary cursor-pointer"
            :title="t('journal.aside.date-select.tooltip')"
            @click="openWeekPicker"
          >{{ weekRangeLabel }}</span>
          <input
            ref="weekPickerRef"
            type="date"
            :value="store.weekStartDt || ''"
            style="position:absolute; opacity:0; width:0; height:0; pointer-events:none;"
            tabindex="-1"
            @change="onWeekPickerChange"
          />
          <button type="button" class="btn btn-sm btn-icon btn-light" @click="navigateWeek(1)">
            <i class="bi bi-chevron-right"></i>
          </button>
        </div>
        <!--end::주간 범위-->

        <!--begin::요일 버튼 (is-active: 선택된 날짜만 파란색 / 항목 없음: 회색음영)-->
        <div class="journal-aside-week-days">
          <button
            v-for="day in weekDays"
            :key="day.dateStr"
            type="button"
            :class="['journal-aside-week-day', { 'is-active': day.isActive }]"
            :title="day.dateStr"
            :disabled="!day.hasDay"
            @click="selectWeekDay(day)"
          >
            <span class="journal-aside-week-day__label">{{ day.label }}</span>
            <span class="journal-aside-week-day__date">{{ day.dayNum }}</span>
          </button>
        </div>
        <!--end::요일 버튼-->
      </template>
      <!--end::주 내비게이션-->

      <!--begin::TODAY 버튼-->
      <button type="button" class="btn btn-sm btn-light-primary w-100" @click="gotoToday">
        {{ t("journal.aside.today") }}
      </button>
      <!--end::TODAY 버튼-->

      <!--begin::Pinpoint (현재 년월 고정 → 되돌리기)-->
      <div>
        <div class="text-gray-900 fs-6 fw-bold d-inline-block mb-2">{{ t("journal.aside.pinpoint") }}</div>
        <div class="d-flex align-items-center justify-content-between px-1 mb-2 gap-1">
          <button
            type="button"
            class="btn btn-sm btn-outline btn-light-primary px-2 pt-1"
            :title="t('journal.aside.pin.tooltip.short')"
            @click="pinpoint"
          >
            <i class="bi bi-bookmarks pe-0"></i>
          </button>
          <span class="mx-1">|</span>
          <span class="px-1 text-center">
            <span class="fs-6 text-muted">{{ asideStore.pinnedYy != null ? String(asideStore.pinnedYy) : '----' }}</span>
            <span class="text-muted"> / </span>
            <span class="fs-6 text-muted">{{ asideStore.pinnedMnth != null ? String(asideStore.pinnedMnth) : '--' }}</span>
            <i class="bi bi-pin-map fs-7 ms-1 text-muted"></i>
          </span>
          <span class="mx-1">|</span>
          <button
            type="button"
            class="btn btn-sm btn-outline btn-light-primary px-2 pt-1"
            :title="t('journal.aside.turnback.tooltip.short')"
            :disabled="asideStore.pinnedYy == null"
            @click="turnback"
          >
            <i class="bi bi-reply-all pe-0"></i>
          </button>
        </div>
      </div>
      <!--end::Pinpoint-->

      <div class="separator"></div>

      <!--begin::표시 필터 토글-->
      <div class="d-flex flex-column gap-2">
        <label class="form-check form-switch form-check-custom form-check-solid cursor-pointer">
          <input
            id="toggleTagCloud"
            class="form-check-input w-30px h-20px"
            type="checkbox"
            :checked="store.showTagCloud"
            @change="toggleTagCloud"
          />
          <span class="form-check-label text-muted fs-7">{{ t("journal.aside.tagcloud") }}</span>
        </label>
      </div>
      <!--end::표시 필터 토글-->

      <!--begin::ENTRY 필터-->
      <div class="d-flex flex-column gap-2">
        <div class="d-flex flex-column gap-2">
          <label class="form-check form-switch form-check-custom form-check-solid cursor-pointer">
            <input
              id="toggleDiaries"
              class="form-check-input w-30px h-20px"
              type="checkbox"
              :checked="store.showDiaries"
              @change="toggleDiaries"
            />
            <span class="form-check-label text-muted fs-7">{{ t("journal.aside.diaries") }}</span>
          </label>

          <div v-if="store.showDiaries" class="d-flex flex-column gap-2 ps-3">
            <div>
              <div class="text-muted fs-8 fw-bold mb-1">- {{ t("journal.aside.chapter-prefixes") }}</div>
              <div v-if="chapterPrefixLoading" class="text-muted fs-8 px-1">{{ t("journal.aside.loading") }}</div>
              <div v-else-if="modalStore.chapterPrefixLoadFailedFor('DIARY')" class="text-muted fs-8 px-1">{{ t("journal.aside.prefix-load-failure") }}</div>
              <div v-else-if="chapterPrefixOptions.length === 0" class="text-muted fs-8 px-1">{{ t("journal.aside.prefix-empty") }}</div>
              <div v-else class="journal-aside-chapter-categories d-flex flex-column gap-1">
                <label
                  v-for="prefix in chapterPrefixOptions"
                  :key="prefix.id"
                  class="form-check form-check-sm form-check-custom form-check-solid cursor-pointer"
                >
                  <input
                    class="form-check-input w-16px h-16px"
                    type="checkbox"
                    :checked="isChapterPrefixSelected(prefix.id)"
                    @change="toggleChapterPrefix(prefix.id)"
                  />
                  <span class="form-check-label fs-8" :style="{ color: prefix.color || 'var(--bs-gray-600)' }">[{{ prefix.name }}]</span>
                </label>
              </div>
            </div>

            <div>
              <div class="text-muted fs-8 fw-bold mb-1">- {{ t("journal.aside.diary-lifecycle") }}</div>
              <select
                id="diaryLifecycleFilter"
                v-model="store.diaryLifecycleKey"
                class="form-select form-select-sm"
                @change="store.fetchDays()"
              >
                <option value="">{{ t("journal.aside.filter.all") }}</option>
                <option
                  v-for="option in lifecycleOptions"
                  :key="'diary-lifecycle-' + option.key"
                  :value="option.key"
                >{{ option.label }}</option>
              </select>
            </div>

            <div>
              <div class="text-muted fs-8 fw-bold mb-1">- {{ t("journal.aside.diary-keywords") }}</div>
              <div class="input-group input-group-sm">
                <input
                  v-model="store.diaryKeyword"
                  type="text"
                  class="form-control form-control-sm"
                  :placeholder="t('journal.aside.diary-keyword.placeholder')"
                  maxlength="200"
                  @keyup.enter="store.fetchDays()"
                />
                <button
                  type="button"
                  class="btn btn-sm btn-icon btn-light"
                  :title="t('journal.aside.diary-keyword.apply.tooltip')"
                  @click="store.fetchDays()"
                >
                  <i class="bi bi-funnel fs-7"></i>
                </button>
              </div>
            </div>
          </div>
        </div>

        <div class="d-flex flex-column gap-2">
          <label class="form-check form-switch form-check-custom form-check-solid cursor-pointer">
            <input
              id="toggleDreams"
              class="form-check-input w-30px h-20px"
              type="checkbox"
              :checked="store.showDreams"
              @change="toggleDreams"
            />
            <span class="form-check-label text-muted fs-7">{{ t("journal.aside.dreams") }}</span>
          </label>

          <div v-if="store.showDreams" class="d-flex flex-column gap-2 ps-3">
            <div>
              <div class="text-muted fs-8 fw-bold mb-1">- {{ t("journal.aside.dream-lifecycle") }}</div>
              <select
                id="dreamLifecycleFilter"
                v-model="store.dreamLifecycleKey"
                class="form-select form-select-sm"
                @change="store.fetchDays()"
              >
                <option value="">{{ t("journal.aside.filter.all") }}</option>
                <option
                  v-for="option in lifecycleOptions"
                  :key="'dream-lifecycle-' + option.key"
                  :value="option.key"
                >{{ option.label }}</option>
              </select>
            </div>

            <div>
              <div class="text-muted fs-8 fw-bold mb-1">- {{ t("journal.aside.dream-keywords") }}</div>
              <div class="input-group input-group-sm">
                <input
                  v-model="store.dreamKeyword"
                  type="text"
                  class="form-control form-control-sm"
                  :placeholder="t('journal.aside.dream-keyword.placeholder')"
                  maxlength="200"
                  @keyup.enter="store.fetchDays()"
                />
                <button
                  type="button"
                  class="btn btn-sm btn-icon btn-light"
                  :title="t('journal.aside.dream-keyword.apply.tooltip')"
                  @click="store.fetchDays()"
                >
                  <i class="bi bi-funnel fs-7"></i>
                </button>
              </div>
            </div>
          </div>
        </div>

        <label class="form-check form-switch form-check-custom form-check-solid cursor-pointer mt-2"
          :title="t('journal.aside.reflection-default-collapsed.tooltip')">
          <input
            id="toggleReflectionDefaultCollapsed"
            class="form-check-input w-30px h-20px"
            type="checkbox"
            :checked="store.reflectionDefaultCollapsed"
            @change="toggleReflectionDefaultCollapsed"
          />
          <span class="form-check-label text-muted fs-7">{{ t("journal.aside.reflection-default-collapsed") }}</span>
        </label>

        <div class="text-gray-900 fs-6 fw-bold mt-1">{{ t("journal.aside.entry-filter") }}</div>
      </div>
      <!--end::ENTRY 필터-->

      <!--begin::필터 초기화 버튼-->
      <button
        type="button"
        class="btn btn-sm btn-light-secondary w-100"
        :disabled="!hasActiveFilters"
        @click="resetFilters"
      >
        <i class="bi bi-arrow-counterclockwise me-1"></i>
        {{ t("journal.aside.filter.reset") }}
      </button>
      <!--end::필터 초기화 버튼-->


    </div>
    <!--end::년월 네비게이션-->
  </div>

  <!--begin::TODO 카드 (레거시 aside 구조 동일 — 필터 카드 밖 별도 카드. 변경 전: 카드 내 '할일 등록' 단독 버튼 → 변경 후: 레거시 TODO List 카드(등록 + 목록·삭제)로 복원)-->
  <JournalAsideTodoCard />
  <!--end::TODO 카드-->
  <!--end::저널 사이드 패널-->
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import axios from "axios";
import { formatLocalDateStr, getWeekStartDateStr } from "@/features/journal/utils/journalDate";
import { useJournalStore } from "@/features/journal/stores/journal";
import type { JournalPrefixDto } from "@/features/journal/stores/journal";
import { useJournalAsideStore } from "@/features/journal/stores/journalAside";
import { useJournalModalStore } from "@/features/journal/stores/journalModal";
import JournalAsideMiniCalendar from "@/features/journal/day/components/JournalAsideMiniCalendar.vue";
import JournalAsideTodoCard from "@/features/journal/day/components/JournalAsideTodoCard.vue";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

const store = useJournalStore();
const asideStore = useJournalAsideStore();
const modalStore = useJournalModalStore();
const { t } = useLocaleStore();
const route = useRoute();
const router = useRouter();

const currentYear = new Date().getFullYear();
const yyOptions = Array.from({ length: currentYear - 2009 }, (_, i) => currentYear - i);

/** 일간(DAILY) view에서 route query.stdrdDt → 미니 달력 선택 날짜 */
const dailySelectedDate = computed(() => (route.query.stdrdDt as string) || "");

/** 미니 달력에 표시할 공휴일 날짜 목록 */
const miniCalHolidays = ref<string[]>([]);

/** 해당 년/월의 공휴일 목록을 서버에서 조회한다. */
async function fetchMiniCalHolidays(yy: number, mnth: number): Promise<void> {
  try {
    const res = await axios.get("/api/schedule/holidays", { params: { yy, mnth } });
    miniCalHolidays.value = res.data?.rsltObj ?? [];
  } catch {
    miniCalHolidays.value = [];
  }
}

// DAILY viewType일 때 store.yy/mnth 변경을 감지해 공휴일을 재조회한다.
watch(
  () => [store.yy, store.mnth, store.viewType] as const,
  ([yy, mnth, viewType]) => {
    if (viewType === "DAILY") {
      void fetchMiniCalHolidays(yy, mnth);
    }
  },
  { immediate: true },
);

/** 미니 달력 날짜 클릭 → 해당 날짜의 일간 view로 이동 */
function onMiniCalendarSelect(dateStr: string): void {
  // 선택된 날짜의 년/월이 현재 aside 년/월과 다르면 aside도 동기화
  const [yStr, mStr] = dateStr.split("-");
  const yy = Number(yStr);
  const mnth = Number(mStr);
  if (yy !== store.yy || mnth !== store.mnth) {
    store.yy = yy;
    store.mnth = mnth;
  }
  void router.replace({ query: { stdrdDt: dateStr } });
}

const sortIconClass = computed(() =>
  store.sortOrder === "DESC"
    ? "bi bi-sort-numeric-down-alt"
    : "bi bi-sort-numeric-up-alt"
);

/** 주간 범위 레이블 (예: "05-12 ~ 05-18") */
const weekRangeLabel = computed(() => {
  if (!store.weekStartDt) return "----";
  const start = store.weekStartDt.substring(5);
  const d = new Date(store.weekStartDt + "T12:00:00");
  d.setDate(d.getDate() + 6);
  const end = formatLocalDateStr(d).substring(5);
  return `${start} ~ ${end}`;
});

/** 주간 날짜 선택기 inputRef */
const weekPickerRef = ref<HTMLInputElement | null>(null);

/** 요일 버튼에서 선택된 날짜 (기본: 오늘) */
const selectedDt = ref<string>(formatLocalDateStr(new Date()));

const chapterPrefixOptions = ref<JournalPrefixDto[]>([]);
const chapterPrefixLoading = ref(false);
const lifecycleOptions = computed(() => [
  { key: "OPEN", label: t("journal.entry.lifecycle.open") },
  { key: "PENDING", label: t("lifecycle.pending") },
  { key: "RESOLVED", label: t("status.completed") },
]);

/** 주간 요일 버튼 목록 (월~일) */
const weekDays = computed(() => {
  if (!store.weekStartDt) return [];
  const labels = [
    t("common.weekday.mon"),
    t("common.weekday.tue"),
    t("common.weekday.wed"),
    t("common.weekday.thu"),
    t("common.weekday.fri"),
    t("common.weekday.sat"),
    t("common.weekday.sun"),
  ];
  return labels.map((label, i) => {
    const d = new Date(store.weekStartDt! + "T12:00:00");
    d.setDate(d.getDate() + i);
    const dateStr = formatLocalDateStr(d);
    return {
      label,
      dateStr,
      dayNum: d.getDate(),
      hasDay: store.dayList.some((day) => day.stdrdDt === dateStr),
      isActive: dateStr === selectedDt.value,
    };
  });
});

const hasActiveFilters = computed(() =>
  !store.showDiaries ||
  !store.showDreams ||
  !store.showTagCloud ||
  store.diaryKeyword.trim() !== "" ||
  store.dreamKeyword.trim() !== "" ||
  store.diaryLifecycleKey !== "" ||
  store.dreamLifecycleKey !== "" ||
  store.chapterPrefixIds.length > 0
);

/** 요일 버튼 클릭 → 해당 날짜를 선택 상태로 전환 후 해당 일자 카드로 스크롤 */
async function selectWeekDay(day: { dateStr: string; hasDay: boolean }): Promise<void> {
  if (!day.hasDay) return;
  selectedDt.value = day.dateStr;
  await nextTick();
  const el = document.getElementById(`journal-day-${day.dateStr}`);
  if (el) el.scrollIntoView({ behavior: "smooth", block: "start" });
}

/** 주간 범위 레이블 클릭 → 날짜 선택기 열기 (표시 기준일 = store.weekStartDt) */
async function openWeekPicker(): Promise<void> {
  const el = weekPickerRef.value;
  if (el && store.weekStartDt) {
    el.value = store.weekStartDt;
  }
  await nextTick();
  (el as HTMLInputElement & { showPicker?: () => void } | null)?.showPicker?.();
}

/** 날짜 선택 → 해당 날짜가 포함된 주로 이동 후 해당 일자 카드로 스크롤 */
async function onWeekPickerChange(e: Event): Promise<void> {
  const val = (e.target as HTMLInputElement).value;
  if (!val) return;
  const newWeekStart = getWeekStartDateStr(val);
  const synced = await syncWeeklyRouteOrFetch(newWeekStart);
  if (!synced) return;
  selectedDt.value = val;
  await nextTick();
  const el = document.getElementById(`journal-day-${val}`);
  if (el) el.scrollIntoView({ behavior: "smooth", block: "start" });
}

/** 현재 년/월을 Pinpoint로 고정 (localStorage `journal_day_pinpoint`) */
function pinpoint(): void {
  asideStore.setPinpoint(store.yy, store.mnth);
}

/** 고정한 년/월로 되돌리기 */
function turnback(): void {
  if (asideStore.pinnedYy == null || asideStore.pinnedMnth == null) return;
  void gotoYyMnth(asideStore.pinnedYy, asideStore.pinnedMnth);
}

function onYyChange(e: Event) {
  const val = Number((e.target as HTMLSelectElement).value);
  void gotoYyMnth(val, store.mnth);
}

/** 월간 기간 상태를 URL query 에 반영한다. */
async function syncMonthlyRouteOrFetch(yy: number, mnth: number): Promise<boolean> {
  if (route.name === "journal-monthly") {
    const failure = await router.replace({ name: "journal-monthly", query: { yy: String(yy), mnth: String(mnth) } });
    return !failure;
  }
  store.yy = yy;
  store.mnth = mnth;
  // DAILY view에서는 aside 월 이동이 미니 달력 표시 월만 변경한다.
  // 데이터 재조회는 날짜 클릭(route query.stdrdDt 변경) 시에만 발생한다.
  if (store.viewType === "DAILY") return true;
  await store.fetchDays();
  return true;
}

/** 주간 기간 상태를 URL query 에 반영한다. */
async function syncWeeklyRouteOrFetch(weekStartDt: string): Promise<boolean> {
  if (route.name === "journal-weekly") {
    const failure = await router.replace({ name: "journal-weekly", query: { weekStartDt } });
    return !failure;
  }
  const d = new Date(weekStartDt + "T12:00:00");
  store.weekStartDt = weekStartDt;
  store.yy = d.getFullYear();
  store.mnth = d.getMonth() + 1;
  await store.fetchDays();
  return true;
}

async function navigateMonth(delta: number): Promise<void> {
  let nextMnth = store.mnth + delta;
  let nextYy = store.yy;
  if (nextMnth < 1) { nextMnth = 12; nextYy -= 1; }
  if (nextMnth > 12) { nextMnth = 1; nextYy += 1; }
  await syncMonthlyRouteOrFetch(nextYy, nextMnth);
}

async function gotoYyMnth(yy: number, mnth: number): Promise<void> {
  await syncMonthlyRouteOrFetch(yy, mnth);
}

async function navigateWeek(delta: number): Promise<void> {
  const base = new Date((store.weekStartDt || formatLocalDateStr(new Date())) + "T12:00:00");
  base.setDate(base.getDate() + delta * 7);
  const weekStartDt = formatLocalDateStr(base);
  const synced = await syncWeeklyRouteOrFetch(weekStartDt);
  if (synced) selectedDt.value = weekStartDt;
}

async function gotoToday(): Promise<void> {
  const today = new Date();
  if (store.viewType === "WEEKLY") {
    const weekStartDt = getWeekStartDateStr(formatLocalDateStr(today));
    const synced = await syncWeeklyRouteOrFetch(weekStartDt);
    if (synced) selectedDt.value = formatLocalDateStr(today);
    return;
  }
  if (store.viewType === "DAILY") {
    const todayStr = formatLocalDateStr(today);
    store.yy = today.getFullYear();
    store.mnth = today.getMonth() + 1;
    void router.replace({ query: { stdrdDt: todayStr } });
    return;
  }
  await syncMonthlyRouteOrFetch(today.getFullYear(), today.getMonth() + 1);
}

function toggleReflectionDefaultCollapsed() {
  store.toggleReflectionDefaultCollapsed();
}

function toggleDiaries() {
  store.showDiaries = !store.showDiaries;
  store.fetchDays();
}

function toggleDreams() {
  store.showDreams = !store.showDreams;
  store.fetchDays();
}

onMounted(() => {
  void fetchChapterPrefixes();
});

async function fetchChapterPrefixes(): Promise<void> {
  if (chapterPrefixOptions.value.length > 0) return;
  chapterPrefixLoading.value = true;
  try {
    await modalStore.prefetchChapterPrefixes("DIARY");
    chapterPrefixOptions.value = [...modalStore.chapterPrefixOptionsFor("DIARY")];
  } catch {
    chapterPrefixOptions.value = [];
  } finally {
    chapterPrefixLoading.value = false;
  }
}

function isChapterPrefixSelected(prefixId: number): boolean {
  return store.chapterPrefixIds.includes(prefixId);
}

function toggleChapterPrefix(prefixId: number): void {
  store.chapterPrefixIds = isChapterPrefixSelected(prefixId)
    ? store.chapterPrefixIds.filter((item) => item !== prefixId)
    : [...store.chapterPrefixIds, prefixId];
  void store.fetchDays();
}

async function resetFilters(): Promise<void> {
  const shouldRefreshTagCloud = !store.showTagCloud;
  store.showDiaries = true;
  store.showDreams = true;
  store.showTagCloud = true;
  store.diaryKeyword = "";
  store.dreamKeyword = "";
  store.diaryLifecycleKey = "";
  store.dreamLifecycleKey = "";
  store.chapterPrefixIds = [];
  if (shouldRefreshTagCloud) {
    void store.fetchTagCloud();
  }
  await store.fetchDays();
}


function toggleTagCloud() {
  store.showTagCloud = !store.showTagCloud;
  if (store.showTagCloud) {
    void store.fetchTagCloud();
  }
  void store.fetchDays();
}

</script>
