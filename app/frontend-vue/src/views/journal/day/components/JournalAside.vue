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
            :value="store.weekStartDt || ''"
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
            <span class="fs-6 text-muted">{{ asideStore.pinnedYy != null ? String(asideStore.pinnedYy) : '----' }}</span>
            <span class="text-muted"> / </span>
            <span class="fs-6 text-muted">{{ asideStore.pinnedMnth != null ? String(asideStore.pinnedMnth) : '--' }}</span>
            <i class="bi bi-pin-map fs-7 ms-1 text-muted"></i>
          </span>
          <span class="mx-1">|</span>
          <button
            type="button"
            class="btn btn-sm btn-outline btn-light-primary px-2 pt-1"
            title="고정한 년월로 돌아가기"
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
          <span class="form-check-label text-muted fs-7">TAGCLOUD</span>
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
            <span class="form-check-label text-muted fs-7">DIARIES</span>
          </label>

          <div v-if="store.showDiaries" class="d-flex flex-column gap-2 ps-3">
            <div>
              <div class="text-muted fs-8 fw-bold mb-1">- CHAPTER CATEGORIES</div>
              <div v-if="chapterCategoryLoading" class="text-muted fs-8 px-1">Loading...</div>
              <div v-else-if="chapterCategoryOptions.length === 0" class="text-muted fs-8 px-1">카테고리를 불러오지 못했습니다.</div>
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
              <div class="text-muted fs-8 fw-bold mb-1">- DIARY LIFECYCLE</div>
              <select
                id="diaryLifecycleFilter"
                v-model="store.diaryLifecycleKey"
                class="form-select form-select-sm"
                @change="store.fetchDays()"
              >
                <option value="">전체</option>
                <option
                  v-for="option in lifecycleOptions"
                  :key="'diary-lifecycle-' + option.key"
                  :value="option.key"
                >{{ option.label }}</option>
              </select>
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
            <span class="form-check-label text-muted fs-7">DREAMS</span>
          </label>

          <div v-if="store.showDreams" class="d-flex flex-column gap-2 ps-3">
            <div>
              <div class="text-muted fs-8 fw-bold mb-1">- DREAM LIFECYCLE</div>
              <select
                id="dreamLifecycleFilter"
                v-model="store.dreamLifecycleKey"
                class="form-select form-select-sm"
                @change="store.fetchDays()"
              >
                <option value="">전체</option>
                <option
                  v-for="option in lifecycleOptions"
                  :key="'dream-lifecycle-' + option.key"
                  :value="option.key"
                >{{ option.label }}</option>
              </select>
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
        </div>

        <div class="text-gray-900 fs-6 fw-bold mt-1">ENTRY FILTER</div>
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
        필터 초기화
      </button>
      <!--end::필터 초기화 버튼-->

      <!--begin::할일 등록 버튼-->
      <button
        type="button"
        class="btn btn-sm btn-light-primary w-100"
        @click="openTodoRegist"
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

interface ChapterCategoryOption {
  code: string;
  codeName: string;
}

const chapterCategoryOptions = ref<ChapterCategoryOption[]>([]);
const chapterCategoryLoading = ref(false);
const lifecycleOptions = [
  { key: "OPEN", label: "진행 중" },
  { key: "PENDING", label: "보류" },
  { key: "RESOLVED", label: "완료" },
];

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

const hasActiveFilters = computed(() =>
  !store.showDiaries ||
  !store.showDreams ||
  !store.showTagCloud ||
  store.diaryKeyword.trim() !== "" ||
  store.dreamKeyword.trim() !== "" ||
  store.diaryLifecycleKey !== "" ||
  store.dreamLifecycleKey !== "" ||
  store.chapterCtgrCds.length > 0
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
  const d = new Date(newWeekStart + "T12:00:00");
  store.weekStartDt = newWeekStart;
  store.yy = d.getFullYear();
  store.mnth = d.getMonth() + 1;
  await store.fetchDays();
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
  store.gotoYyMnth(asideStore.pinnedYy, asideStore.pinnedMnth);
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
    // 변경 전: JOURNAL_CHAPTER_CTGR_CD(삭제된 그룹) 조회 → rsltList 빈 배열, 체크박스 미표시.
    // 변경 후: 일기·노트 코드 그룹을 modalStore 와 동일 경로로 병합한다.
    await modalStore.prefetchChapterCategories();
    const merged = new Map<string, ChapterCategoryOption>();
    for (const item of modalStore.chapterDiaryCategoryOptions) {
      if (item.code) {
        merged.set(item.code, { code: item.code, codeName: item.codeName || item.code });
      }
    }
    for (const item of modalStore.chapterNoteCategoryOptions) {
      if (item.code && !merged.has(item.code)) {
        merged.set(item.code, { code: item.code, codeName: item.codeName || item.code });
      }
    }
    chapterCategoryOptions.value = Array.from(merged.values());
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

async function resetFilters(): Promise<void> {
  const shouldRefreshTagCloud = !store.showTagCloud;
  store.showDiaries = true;
  store.showDreams = true;
  store.showTagCloud = true;
  store.diaryKeyword = "";
  store.dreamKeyword = "";
  store.diaryLifecycleKey = "";
  store.dreamLifecycleKey = "";
  store.chapterCtgrCds = [];
  if (shouldRefreshTagCloud) {
    void store.fetchTagCloud();
  }
  await store.fetchDays();
}

/** 할일 등록 모달 열기 */
function openTodoRegist() {
  modalStore.openTodoRegist({ yy: store.yy, mnth: store.mnth });
}

function toggleTagCloud() {
  store.showTagCloud = !store.showTagCloud;
  if (store.showTagCloud) {
    void store.fetchTagCloud();
  }
  void store.fetchDays();
}

</script>
