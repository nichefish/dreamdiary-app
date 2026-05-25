<template>
  <!--begin::저널 사이드 패널 (년월 이동 + 필터)-->
  <div class="journal-aside card card-reset card-p-0 p-5" style="width:280px; min-width:280px; max-width:280px;">
    <div class="d-flex justify-content-end mb-2">
      <button
        type="button"
        class="btn btn-sm btn-icon btn-light"
        title="필터 패널 닫기"
        @click="asideStore.hide()"
      >
        <i class="bi bi-x-lg"></i>
      </button>
    </div>

    <!--begin::필터 카드 헤더 (레거시 journal_aside_header)-->
    <div id="journal_aside_header" class="card-header min-h-auto mb-5 px-0 border-0">
      <h3 class="card-title text-gray-900 fw-bold fs-3 mb-0">
        <i class="bi bi-filter fs-2 me-1"></i> FILTER
      </h3>
      <div class="card-toolbar">
        <button
          type="button"
          class="btn btn-sm btn-icon btn-color-gray-500 btn-light"
          title="정렬 변경"
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

      <!--begin::월 내비게이션 (MONTHLY)-->
      <template v-if="store.viewType !== 'WEEKLY'">
        <!--begin::월 이동 컨트롤-->
        <div class="d-flex align-items-center justify-content-between">
          <button type="button" class="btn btn-sm btn-icon btn-light" @click="store.navigateMonth(-1)">
            <i class="bi bi-chevron-left"></i>
          </button>
          <span class="fw-bold fs-6">{{ store.mnth }}월</span>
          <button type="button" class="btn btn-sm btn-icon btn-light" @click="store.navigateMonth(1)">
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
            @click="store.gotoYyMnth(store.yy, m)"
          >
            {{ m }}월
          </button>
        </div>
        <!--end::월 그리드-->
      </template>
      <!--end::월 내비게이션-->

      <!--begin::주 내비게이션 (WEEKLY)-->
      <template v-else>
        <!--begin::주간 범위 + 이동-->
        <div class="d-flex align-items-center justify-content-between position-relative">
          <button type="button" class="btn btn-sm btn-icon btn-light" @click="store.navigateWeek(-1)">
            <i class="bi bi-chevron-left"></i>
          </button>
          <span
            class="fw-bold fs-7 text-center text-hover-primary cursor-pointer"
            title="날짜 선택"
            @click="openWeekPicker"
          >{{ weekRangeLabel }}</span>
          <input
            ref="weekPickerRef"
            type="date"
            style="position:absolute; opacity:0; width:0; height:0; pointer-events:none;"
            tabindex="-1"
            @change="onWeekPickerChange"
          />
          <button type="button" class="btn btn-sm btn-icon btn-light" @click="store.navigateWeek(1)">
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
      <button type="button" class="btn btn-sm btn-light-primary w-100" @click="store.gotoToday()">
        TODAY
      </button>
      <!--end::TODAY 버튼-->

      <!--begin::Pinpoint (현재 년월 고정 → 되돌리기)-->
      <div>
        <div class="text-gray-900 fs-6 fw-bold d-inline-block mb-2">Pinpoint</div>
        <div class="d-flex align-items-center justify-content-between px-1 mb-2 gap-1">
          <button
            type="button"
            class="btn btn-sm btn-outline btn-light-primary px-2 pt-1"
            title="현재 년월 고정"
            @click="pinpoint"
          >
            <i class="bi bi-bookmarks pe-0"></i>
          </button>
          <span class="mx-1">|</span>
          <span class="px-1 text-center">
            <span class="fs-6 text-muted">{{ pinnedYy != null ? String(pinnedYy) : '----' }}</span>
            <span class="text-muted"> / </span>
            <span class="fs-6 text-muted">{{ pinnedMnth != null ? String(pinnedMnth) : '--' }}</span>
            <i class="bi bi-pin-map fs-7 ms-1 text-muted"></i>
          </span>
          <span class="mx-1">|</span>
          <button
            type="button"
            class="btn btn-sm btn-outline btn-light-primary px-2 pt-1"
            title="고정한 년월로 돌아가기"
            :disabled="pinnedYy == null"
            @click="turnback"
          >
            <i class="bi bi-reply-all pe-0"></i>
          </button>
        </div>
      </div>
      <!--end::Pinpoint-->

      <div class="separator"></div>

      <!--begin::보기 필터 토글-->
      <div class="d-flex flex-column gap-2">
        <label class="form-check form-switch form-check-custom form-check-solid cursor-pointer">
          <input
            class="form-check-input w-30px h-20px"
            type="checkbox"
            :checked="store.showDiaries"
            @change="toggleDiaries"
          />
          <span class="form-check-label text-muted fs-7">DIARIES</span>
        </label>
        <label class="form-check form-switch form-check-custom form-check-solid cursor-pointer">
          <input
            class="form-check-input w-30px h-20px"
            type="checkbox"
            :checked="store.showDreams"
            @change="toggleDreams"
          />
          <span class="form-check-label text-muted fs-7">DREAMS</span>
        </label>
        <label class="form-check form-switch form-check-custom form-check-solid cursor-pointer">
          <input
            class="form-check-input w-30px h-20px"
            type="checkbox"
            :checked="store.showTagCloud"
            @change="toggleTagCloud"
          />
          <span class="form-check-label text-muted fs-7">TAGCLOUD</span>
        </label>
      </div>
      <!--end::보기 필터 토글-->

      <!--begin::ENTRY 필터-->
      <div class="d-flex flex-column gap-2">
        <div class="text-gray-900 fs-6 fw-bold">ENTRY FILTER</div>

        <div>
          <div class="text-muted fs-8 fw-bold mb-1">- CHAPTER CATEGORIES</div>
          <div v-if="chapterCategoryLoading" class="text-muted fs-8 px-1">Loading...</div>
          <div v-else class="journal-aside-chapter-categories d-flex flex-column gap-1">
            <label
              v-for="ctgr in chapterCategoryOptions"
              :key="ctgr.code"
              class="form-check form-check-sm form-check-custom form-check-solid cursor-pointer"
            >
              <input
                class="form-check-input w-16px h-16px"
                type="checkbox"
                :checked="isChapterCategorySelected(ctgr.code)"
                @change="toggleChapterCategory(ctgr.code)"
              />
              <span class="form-check-label text-muted fs-8">[{{ ctgr.codeName }}]</span>
            </label>
          </div>
        </div>

        <div>
          <div class="text-muted fs-8 fw-bold mb-1">- DIARY KEYWORDS</div>
          <div class="input-group input-group-sm">
            <input
              v-model="store.diaryKeyword"
              type="text"
              class="form-control form-control-sm"
              placeholder="일기 키워드"
              maxlength="200"
              @keyup.enter="store.fetchDays()"
            />
            <button
              type="button"
              class="btn btn-sm btn-icon btn-light"
              title="일기 키워드 필터 적용"
              @click="store.fetchDays()"
            >
              <i class="bi bi-funnel fs-7"></i>
            </button>
          </div>
        </div>

        <div>
          <div class="text-muted fs-8 fw-bold mb-1">- DREAM KEYWORDS</div>
          <div class="input-group input-group-sm">
            <input
              v-model="store.dreamKeyword"
              type="text"
              class="form-control form-control-sm"
              placeholder="꿈 키워드"
              maxlength="200"
              @keyup.enter="store.fetchDays()"
            />
            <button
              type="button"
              class="btn btn-sm btn-icon btn-light"
              title="꿈 키워드 필터 적용"
              @click="store.fetchDays()"
            >
              <i class="bi bi-funnel fs-7"></i>
            </button>
          </div>
        </div>
      </div>
      <!--end::ENTRY 필터-->

      <!--begin::할일 등록 버튼-->
      <button
        type="button"
        class="btn btn-sm btn-light-primary w-100"
        @click="openTodoReg"
      >
        <i class="bi bi-check2-square me-1"></i>
        할일 등록
      </button>
      <!--end::할일 등록 버튼-->

    </div>
    <!--end::년월 네비게이션-->
  </div>
  <!--end::저널 사이드 패널-->
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted } from "vue";
import axios from "axios";
import { formatLocalDateStr, getWeekStartDateStr } from "@/utils/journalDate";
import { useJournalStore } from "@/stores/journal";
import { useJournalAsideStore } from "@/stores/journalAside";
import { useJournalModalStore } from "@/stores/journalModal";

const store = useJournalStore();
const asideStore = useJournalAsideStore();
const modalStore = useJournalModalStore();

const currentYear = new Date().getFullYear();
const yyOptions = Array.from({ length: currentYear - 2009 }, (_, i) => currentYear - i);

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

/** Pinpoint — 고정된 년/월 (null: 미고정) */
const pinnedYy = ref<number | null>(null);
const pinnedMnth = ref<number | null>(null);

interface ChapterCategoryOption {
  code: string;
  codeName: string;
}

const chapterCategoryOptions = ref<ChapterCategoryOption[]>([]);
const chapterCategoryLoading = ref(false);

/** 주간 요일 버튼 목록 (월~일) */
const weekDays = computed(() => {
  if (!store.weekStartDt) return [];
  const labels = ["월", "화", "수", "목", "금", "토", "일"];
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

/** 요일 버튼 클릭 → 해당 날짜를 선택 상태로 전환 후 해당 일자 카드로 스크롤 */
async function selectWeekDay(day: { dateStr: string; hasDay: boolean }): Promise<void> {
  if (!day.hasDay) return;
  selectedDt.value = day.dateStr;
  await nextTick();
  const el = document.getElementById(`journal-day-${day.dateStr}`);
  if (el) el.scrollIntoView({ behavior: "smooth", block: "start" });
}

/** 주간 범위 레이블 클릭 → 날짜 선택기 열기 */
async function openWeekPicker(): Promise<void> {
  await nextTick();
  (weekPickerRef.value as HTMLInputElement & { showPicker?: () => void })?.showPicker?.();
}

/** 날짜 선택 → 해당 날짜가 포함된 주로 이동 후 해당 일자 카드로 스크롤 */
async function onWeekPickerChange(e: Event): Promise<void> {
  const val = (e.target as HTMLInputElement).value;
  if (!val) return;
  const newWeekStart = getWeekStartDateStr(val);
  const d = new Date(newWeekStart + "T12:00:00");
  store.weekStartDt = newWeekStart;
  store.yy = d.getFullYear();
  store.mnth = d.getMonth() + 1;
  await store.fetchDays();
  await nextTick();
  const el = document.getElementById(`journal-day-${val}`);
  if (el) el.scrollIntoView({ behavior: "smooth", block: "start" });
}

/** 현재 년/월을 Pinpoint로 고정 */
function pinpoint(): void {
  pinnedYy.value = store.yy;
  pinnedMnth.value = store.mnth;
}

/** 고정한 년/월로 되돌리기 */
function turnback(): void {
  if (pinnedYy.value == null || pinnedMnth.value == null) return;
  store.gotoYyMnth(pinnedYy.value, pinnedMnth.value);
}

function onYyChange(e: Event) {
  const val = Number((e.target as HTMLSelectElement).value);
  store.gotoYyMnth(val, store.mnth);
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
  void fetchChapterCategories();
});

async function fetchChapterCategories(): Promise<void> {
  if (chapterCategoryOptions.value.length > 0) return;
  chapterCategoryLoading.value = true;
  try {
    const res = await axios.get("/api/code/items", { params: { groupCode: "JOURNAL_CHAPTER_CTGR_CD" } });
    chapterCategoryOptions.value = (res.data?.rsltList ?? [])
      .map((item: Record<string, string>) => ({
        code: item.code ?? "",
        codeName: item.codeName ?? item.code ?? "",
      }))
      .filter((item: ChapterCategoryOption) => item.code);
  } catch {
    chapterCategoryOptions.value = [];
  } finally {
    chapterCategoryLoading.value = false;
  }
}

function isChapterCategorySelected(code: string): boolean {
  return store.chapterCtgrCds.includes(code);
}

function toggleChapterCategory(code: string): void {
  store.chapterCtgrCds = isChapterCategorySelected(code)
    ? store.chapterCtgrCds.filter((item) => item !== code)
    : [...store.chapterCtgrCds, code];
  void store.fetchDays();
}

/** 할일 등록 모달 열기 */
function openTodoReg() {
  modalStore.openTodoReg({ yy: store.yy, mnth: store.mnth });
}

function toggleTagCloud() {
  store.showTagCloud = !store.showTagCloud;
  if (store.showTagCloud) {
    void store.fetchTagCloud();
  }
  void store.fetchDays();
}

</script>
