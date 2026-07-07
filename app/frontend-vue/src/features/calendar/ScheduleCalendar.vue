<template>
  <div class="schedule-calendar-page">
    <!--begin::일정 본문 + aside 컨테이너 (저널 레이아웃과 동일 구성)-->
    <div class="d-flex align-items-start gap-6">
      <div class="flex-grow-1 min-w-0">
        <div class="d-flex flex-column-fluid justify-content-between align-items-start align-items-xl-center gap-4 w-100">
          <ul class="nav nav-tabs nav-tabs-line ps-5 mt-5 mb-0 flex-grow-1">
            <li class="nav-item">
              <button
                type="button"
                class="nav-link px-6 border-0 bg-transparent"
                :class="{ active: viewMode === 'calendar' }"
                @click="switchToCalendarView"
              >
                <span class="nav-icon"><i class="bi bi-calendar3"></i></span>
                <span class="nav-text">{{ t('schedule.view.calendar') }}</span>
              </button>
            </li>
            <li class="nav-item">
              <button
                type="button"
                class="nav-link px-6 border-0 bg-transparent"
                :class="{ active: viewMode === 'list' }"
                @click="switchToListView"
              >
                <span class="nav-icon"><i class="bi bi-list-ul"></i></span>
                <span class="nav-text">{{ t('schedule.view.list') }}</span>
              </button>
            </li>
          </ul>

          <div class="schedule-view-toolbar__tools d-none d-md-flex align-items-center flex-shrink-0 pe-5 mt-3 gap-2">
            <div class="d-flex align-items-center gap-2">
              <label for="schedule_anchor_date" class="form-label mb-0 text-nowrap fs-7 fw-bold">{{ t('schedule.anchor-date') }}</label>
              <!--저장 모달과 동일하게 readonly 텍스트 input + flatpickr — 클릭 시 달력 표시 (변경 전: 네이티브 type="date")-->
              <input
                id="schedule_anchor_date"
                ref="anchorDtInputRef"
                :value="anchorDateText"
                type="text"
                class="form-control form-control-sm form-control-solid schedule-view-toolbar__date-input"
                autocomplete="off"
                readonly
              />
            </div>
            <div class="input-group input-group-sm">
              <input
                v-model="searchKeyword"
                type="search"
                class="form-control form-control-sm form-control-solid"
                :placeholder="t('schedule.search.placeholder')"
                maxlength="200"
                style="min-width: 140px;"
                @keyup.enter="reloadActiveView"
              />
              <button type="button" class="btn btn-sm btn-icon btn-light" :title="t('common.search')" @click="reloadActiveView">
                <i class="bi bi-search fs-7"></i>
              </button>
            </div>
            <button
              type="button"
              class="btn btn-sm btn-icon btn-light"
              :title="t('schedule.filter.advanced.tooltip')"
              data-bs-toggle="collapse"
              data-bs-target="#schedule_filter_panel"
            >
              <i class="bi bi-funnel fs-7"></i>
            </button>
            <div class="vr mx-1 opacity-25"></div>
            <button type="button" class="btn btn-sm btn-light-primary btn-outlined ps-4 pe-3 py-2 text-nowrap" @click="openRegist(false)">
              <i class="bi bi-plus-lg fs-4 pe-1"></i>
              {{ t('schedule.register') }}
            </button>
            <button type="button" class="btn btn-sm btn-light-primary btn-outlined ps-4 pe-3 py-2 text-nowrap" @click="openRegist(true)">
              <i class="bi bi-lock fs-4 pe-1"></i>
              {{ t('schedule.private') }}
            </button>
            <template v-if="!asideStore.visible">
              <div class="vr mx-1 opacity-25"></div>
              <button
                type="button"
                class="btn btn-sm btn-icon btn-light"
                :title="t('schedule.aside.open')"
                @click="asideStore.show()"
              >
                <i class="bi bi-layout-sidebar-inset-reverse"></i>
              </button>
            </template>
          </div>
        </div>

        <div id="schedule_filter_panel" class="collapse schedule-filter mx-5 mb-0">
          <label v-for="item in filterItems" :key="item.key" class="form-check form-check-sm form-check-custom form-check-solid">
            <input
              class="form-check-input"
              type="checkbox"
              :checked="scheduleStore.filter[item.key]"
              @change="toggleFilter(item.key, $event)"
            />
            <span class="form-check-label">{{ item.label }}</span>
          </label>
        </div>

        <div class="card post schedule-calendar-card" style="margin-top: 0 !important;">
          <div v-show="viewMode === 'calendar'" class="card-body position-relative">
            <div v-if="scheduleStore.loading" class="schedule-loading">
              <span class="spinner-border spinner-border-sm me-2"></span>
              {{ t('common.loading') }}
            </div>
            <FullCalendar ref="calendarRef" :options="calendarOptions" />
          </div>
          <div v-show="viewMode === 'list'" class="card-body">
            <div v-if="scheduleStore.listLoading" class="schedule-list-loading">
              <span class="spinner-border spinner-border-sm me-2"></span>
              {{ t('common.loading') }}
            </div>
            <div v-else class="table-responsive">
              <table class="table table-row-bordered table-row-gray-300 align-middle gs-0 gy-3 schedule-list-table">
                <thead>
                  <tr class="fw-bold text-muted">
                    <th class="min-w-90px">{{ t('schedule.list.col.category') }}</th>
                    <th class="min-w-200px">{{ t('common.title') }}</th>
                    <th class="min-w-100px">{{ t('schedule.list.col.start-date') }}</th>
                    <th class="min-w-100px">{{ t('schedule.list.col.end-date') }}</th>
                    <th>{{ t('schedule.list.col.participants') }}</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="scheduleStore.listRows.length === 0">
                    <td colspan="5" class="text-center text-muted py-10">{{ t('schedule.list.empty') }}</td>
                  </tr>
                  <tr
                    v-for="row in scheduleStore.listRows"
                    :key="row.id"
                    class="schedule-list-table__row"
                    @click="openListRow(row)"
                  >
                    <td>{{ row.scheduleNm || row.scheduleCd }}</td>
                    <td>
                      <span v-if="row.privateYn === 'Y'" class="me-1 text-muted" :title="t('schedule.private')">
                        <i class="bi bi-lock-fill"></i>
                      </span>
                      {{ row.title }}
                    </td>
                    <td>{{ row.bgnDt }}</td>
                    <td>{{ row.endDt || row.bgnDt }}</td>
                    <td class="text-truncate" style="max-width: 240px;">{{ row.prtcpntListStr || "-" }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <div v-if="viewMode === 'list'" class="card-footer schedule-list-footer">
            <span class="text-muted fs-8">{{ t('board.group.pagination.total-format').replace('{0}', formatNumber(scheduleStore.listTotalElements)) }}</span>
            <div class="d-flex align-items-center gap-2">
              <select
                :value="scheduleStore.listPageSize"
                class="form-select form-select-solid form-select-sm schedule-list-page-size"
                @change="onListPageSizeChange"
              >
                <option :value="10">{{ t('common.page-size.10') }}</option>
                <option :value="25">{{ t('common.page-size.25') }}</option>
                <option :value="50">{{ t('common.page-size.50') }}</option>
              </select>
              <div v-if="listPageNumbers.length" class="pagination mb-0">
                <button type="button" class="page-link" :disabled="scheduleStore.listCurrentPage <= 0" @click="goListPage(0)">
                  <i class="previous"></i>
                </button>
                <button
                  v-for="page in listPageNumbers"
                  :key="page"
                  type="button"
                  class="page-link"
                  :class="{ active: page === scheduleStore.listCurrentPage }"
                  @click="goListPage(page)"
                >
                  {{ page + 1 }}
                </button>
                <button
                  type="button"
                  class="page-link"
                  :disabled="scheduleStore.listCurrentPage >= scheduleStore.listTotalPages - 1"
                  @click="goListPage(scheduleStore.listTotalPages - 1)"
                >
                  <i class="next"></i>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!--begin::일정 aside (년월 내비게이션, 폭 280px = 저널 aside 와 동일)-->
      <aside v-if="asideStore.visible" class="schedule-calendar-page__aside flex-shrink-0">
        <ScheduleAside :yy="asideYy" :mnth="asideMnth" @goto="onAsideGoto" @today="onAsideToday" />
      </aside>
      <!--end::일정 aside-->
    </div>
    <!--end::일정 본문 + aside 컨테이너-->

    <div ref="registModalEl" class="modal fade" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">{{ t('schedule.save.modal.title') }}</h5>
            <button type="button" class="btn-close" @click="closeRegist"></button>
          </div>
          <div class="modal-body">
            <form class="form" @submit.prevent="submitRegist">
              <input type="hidden" :value="registForm.id ?? ''" />

              <div class="row g-3 mb-3">
                <div class="col-md-4">
                  <label class="form-label required" for="scheduleCd">{{ t('schedule.list.col.category') }}</label>
                  <select id="scheduleCd" v-model="registForm.scheduleCd" class="form-select form-select-solid" required @change="onScheduleCodeChange">
                    <option value="">{{ t('schedule.form.select-placeholder') }}</option>
                    <option v-for="code in filteredCodeOptions" :key="code.code" :value="code.code">
                      {{ code.codeName }}
                    </option>
                  </select>
                </div>
                <div class="col-md-8">
                  <label class="form-label required" for="scheduleTitle">{{ t('common.title') }}</label>
                  <input id="scheduleTitle" v-model="registForm.title" class="form-control form-control-solid" maxlength="120" required />
                </div>
              </div>

              <div class="row g-3 mb-3">
                <div class="col-md-6">
                  <label class="form-label required" for="scheduleBgnDt">{{ t('schedule.list.col.start-date') }}</label>
                  <!--레거시 #bgnDt: readonly 텍스트 input + cF.datepicker.singleDatePicker → flatpickr 바인딩-->
                  <input
                    id="scheduleBgnDt"
                    ref="bgnDtInputRef"
                    :value="registForm.bgnDt"
                    type="text"
                    class="form-control form-control-solid"
                    :placeholder="t('schedule.list.col.start-date')"
                    autocomplete="off"
                    readonly
                    required
                  />
                </div>
                <!--레거시 #endDtDiv: display:none 토글(v-show 동등) — v-if 사용 시 flatpickr 인스턴스가 깨진다-->
                <div class="col-md-6" v-show="showEndDate">
                  <label class="form-label" for="scheduleEndDt">{{ t('schedule.list.col.end-date') }}</label>
                  <input
                    id="scheduleEndDt"
                    ref="endDtInputRef"
                    :value="registForm.endDt"
                    type="text"
                    class="form-control form-control-solid"
                    :placeholder="t('schedule.list.col.end-date')"
                    autocomplete="off"
                    readonly
                  />
                </div>
              </div>

              <div class="mb-3">
                <label class="form-label" for="scheduleContent">{{ t('common.content') }}</label>
                <textarea id="scheduleContent" v-model="registForm.content" class="form-control form-control-solid" rows="4" maxlength="500"></textarea>
              </div>

              <div class="mb-3">
                <div class="d-flex align-items-center justify-content-between mb-2">
                  <label class="form-label mb-0">{{ t('schedule.list.col.participants') }}</label>
                  <button v-if="!isPrivateRegist" type="button" class="btn btn-sm btn-icon btn-light-primary" @click="addParticipant">
                    <i class="bi bi-plus-lg"></i>
                  </button>
                </div>
                <div v-if="isPrivateRegist" class="text-muted fs-7">
                  {{ t('schedule.form.private-note') }}
                </div>
                <div v-else class="schedule-participants">
                  <div v-for="(participant, index) in registForm.prtcpntList" :key="index" class="schedule-participants__row">
                    <select v-model="participant.username" class="form-select form-select-solid">
                      <option value="">{{ t('schedule.form.select-placeholder') }}</option>
                      <option v-for="user in scheduleStore.userOptions" :key="user.username" :value="user.username">
                        {{ user.userNm || user.username }}
                      </option>
                    </select>
                    <button type="button" class="btn btn-sm btn-icon btn-light-danger" @click="removeParticipant(index)">
                      <i class="bi bi-trash"></i>
                    </button>
                  </div>
                </div>
              </div>
            </form>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-sm btn-primary" :disabled="submitting" @click="submitRegist">
              <span v-if="submitting" class="spinner-border spinner-border-sm me-1"></span>
              {{ t('common.save') }}
            </button>
            <button type="button" class="btn btn-sm btn-light" @click="closeRegist">{{ t('common.close') }}</button>
          </div>
        </div>
      </div>
    </div>

    <div ref="detailModalEl" class="modal fade" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">{{ t('schedule.detail.modal.title') }}</h5>
            <button type="button" class="btn-close" @click="closeDetail"></button>
          </div>
          <div class="modal-body">
            <template v-if="detail">
              <div class="schedule-detail-row">
                <span class="schedule-detail-row__label"><i class="bi bi-justify-left"></i> {{ t('common.title') }}</span>
                <span>{{ detailTitle }}</span>
              </div>
              <div v-if="!isDetailHolyday" class="schedule-detail-row">
                <span class="schedule-detail-row__label"><i class="bi bi-people"></i> {{ t('schedule.list.col.participants') }}</span>
                <span>{{ detail.prtcpnt || "-" }}</span>
              </div>
              <div class="schedule-detail-row">
                <span class="schedule-detail-row__label"><i class="bi bi-info-circle"></i> {{ t('schedule.detail.label.description') }}</span>
                <span class="schedule-detail-row__content">{{ detail.content || "-" }}</span>
              </div>
              <div class="schedule-detail-row">
                <span class="schedule-detail-row__label"><i class="bi bi-calendar3"></i> {{ t('schedule.detail.label.schedule') }}</span>
                <span>{{ detail.bgnDt }}<template v-if="detail.endDt"> - {{ detail.endDt }}</template></span>
              </div>
            </template>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-sm btn-light-primary" :disabled="!detail || isDetailHolyday" @click="modifyDetail">
              {{ t('common.mdf') }}
            </button>
            <button type="button" class="btn btn-sm btn-light-danger" :disabled="!detail || isDetailHolyday" @click="deleteDetail">
              {{ t('common.del') }}
            </button>
            <button type="button" class="btn btn-sm btn-light" @click="closeDetail">{{ t('common.close') }}</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { swalConfirm, swalAlert } from "@/shared/utils/swal";
import { bindSingleDatePicker, destroySingleDatePicker } from "@/shared/utils/flatpickrSingleDate";
import type { Instance as FlatpickrInstance } from "flatpickr/dist/types/instance";
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import FullCalendar from "@fullcalendar/vue3";
import dayGridPlugin from "@fullcalendar/daygrid";
import timeGridPlugin from "@fullcalendar/timegrid";
import listPlugin from "@fullcalendar/list";
import interactionPlugin from "@fullcalendar/interaction";
import type { CalendarOptions, DatesSetArg, EventClickArg } from "@fullcalendar/core";
import { Modal } from "bootstrap";
import { assertAuthenticatedBeforeModal } from "@/shared/auth/sessionPing";
import ScheduleAside from "@/features/calendar/components/ScheduleAside.vue";
import { useScheduleAsideStore } from "@/features/calendar/stores/scheduleAside";
import {
  useScheduleStore,
  queryRangeForMonth,
  queryRangeForYear,
  queryRangeFromVisible,
  type ScheduleDetail,
  type ScheduleFilter,
  type ScheduleForm,
  type ScheduleListRow,
  type ScheduleQueryRange,
} from "@/features/calendar/stores/schedule";

const scheduleStore = useScheduleStore();
const asideStore = useScheduleAsideStore();
const { t } = useLocaleStore();
const route = useRoute();
const router = useRouter();

const calendarRef = ref<any>(null);
const registModalEl = ref<HTMLElement | null>(null);
const detailModalEl = ref<HTMLElement | null>(null);
const bgnDtInputRef = ref<HTMLInputElement | null>(null);
const endDtInputRef = ref<HTMLInputElement | null>(null);
const anchorDtInputRef = ref<HTMLInputElement | null>(null);
let registModal: Modal | null = null;
let detailModal: Modal | null = null;
let bgnDtFp: FlatpickrInstance | null = null;
let endDtFp: FlatpickrInstance | null = null;
let anchorDtFp: FlatpickrInstance | null = null;

const today = new Date();
const queryRange = ref<ScheduleQueryRange>(queryRangeForMonth(today));
const anchorDateText = ref(formatDate(today));
const searchKeyword = ref("");
const submitting = ref(false);
const isPrivateRegist = ref(false);
const showEndDate = ref(true);
const detail = ref<ScheduleDetail | null>(null);
const viewMode = ref<"calendar" | "list">("calendar");

const registForm = reactive<ScheduleForm>({
  scheduleCd: "",
  title: "",
  content: "",
  bgnDt: formatDate(today),
  endDt: formatDate(today),
  privateYn: "N",
  prtcpntList: [],
});

const filterItems = computed<Array<{ key: keyof ScheduleFilter; label: string }>>(() => [
  { key: "myPaprChk", label: t("schedule.filter.my-only") },
  { key: "vcatnChk", label: t("schedule.filter.vacation") },
  { key: "indtChk", label: t("schedule.filter.join-date") },
  { key: "outdtChk", label: t("schedule.filter.retire-date") },
  { key: "tlcmmtChk", label: t("schedule.filter.remote") },
  { key: "prvtChk", label: t("schedule.private") },
]);

const filteredCodeOptions = computed(() => {
  const excluded = ["HOLYDAY", "CEREMONY", "TLCMMT"];
  if (!isPrivateRegist.value) return scheduleStore.codeOptions;
  return scheduleStore.codeOptions.filter((item) => !excluded.includes(item.code));
});

const detailTitle = computed(() => {
  if (!detail.value) return "";
  const prefix = detail.value.scheduleNm ? `[${detail.value.scheduleNm}] ` : "";
  return `${prefix}${detail.value.title ?? ""}`;
});

const isDetailHolyday = computed(() => detail.value?.scheduleCd === scheduleStore.holyDayCode);

const listPageNumbers = computed(() => {
  const total = scheduleStore.listTotalPages;
  const current = scheduleStore.listCurrentPage;
  if (total <= 0) return [];
  const windowSize = 5;
  let start = Math.max(0, current - Math.floor(windowSize / 2));
  const end = Math.min(total - 1, start + windowSize - 1);
  start = Math.max(0, end - windowSize + 1);
  const pages: number[] = [];
  for (let page = start; page <= end; page += 1) pages.push(page);
  return pages;
});

/** aside 표시용 연/월 — 이동일(anchorDateText) 기준. 달력 이동 시 datesSet 에서 동기화된다. */
const asideYy = computed(() => parseDate(anchorDateText.value).getFullYear());
const asideMnth = computed(() => parseDate(anchorDateText.value).getMonth() + 1);

const calendarOptions = computed<CalendarOptions>(() => ({
  plugins: [dayGridPlugin, timeGridPlugin, listPlugin, interactionPlugin],
  initialView: "dayGridMonth",
  headerToolbar: {
    left: "prev,next today",
    center: "title",
    right: "dayGridMonth,timeGridWeek,listMonth",
  },
  locale: "ko",
  height: "auto",
  events: scheduleStore.events.map((event) => ({ ...event, id: String(event.id) })),
  eventClick: onEventClick,
  datesSet: onDatesSet,
}));

function formatDate(date: Date): string {
  const yyyy = date.getFullYear();
  const mm = String(date.getMonth() + 1).padStart(2, "0");
  const dd = String(date.getDate()).padStart(2, "0");
  return `${yyyy}-${mm}-${dd}`;
}

function parseDate(value: string): Date {
  const [yyyy, mm, dd] = value.split("-").map((part) => Number(part));
  return new Date(yyyy, (mm || 1) - 1, dd || 1);
}

function formatNumber(value: number): string {
  return new Intl.NumberFormat("ko-KR").format(value);
}

async function reloadActiveView() {
  if (viewMode.value === "list") {
    await scheduleStore.fetchList(queryRange.value, searchKeyword.value, scheduleStore.listCurrentPage);
    return;
  }
  await scheduleStore.fetchEvents(queryRange.value, searchKeyword.value);
}

async function switchToCalendarView() {
  viewMode.value = "calendar";
  if (scheduleStore.events.length === 0) {
    await scheduleStore.fetchEvents(queryRange.value, searchKeyword.value);
  }
}

async function switchToListView() {
  viewMode.value = "list";
  scheduleStore.listCurrentPage = 0;
  try {
    await scheduleStore.fetchList(queryRange.value, searchKeyword.value, 0);
  } catch (error) {
    void swalAlert(error instanceof Error ? error.message : t("schedule.list.load.failure"));
  }
}

async function goListPage(page: number) {
  try {
    await scheduleStore.fetchList(queryRange.value, searchKeyword.value, page);
  } catch (error) {
    void swalAlert(error instanceof Error ? error.message : t("schedule.list.load.failure"));
  }
}

async function onListPageSizeChange(event: Event) {
  const size = Number((event.target as HTMLSelectElement).value);
  await scheduleStore.changeListPageSize(size);
  try {
    await scheduleStore.fetchList(queryRange.value, searchKeyword.value, 0);
  } catch (error) {
    void swalAlert(error instanceof Error ? error.message : t("schedule.list.load.failure"));
  }
}

async function onDatesSet(arg: DatesSetArg) {
  queryRange.value = queryRangeFromVisible(arg.start, arg.end);
  anchorDateText.value = formatDate(arg.view.currentStart);
  await reloadActiveView();
}

function goToAnchorDate() {
  const next = parseDate(anchorDateText.value);
  scheduleStore.listCurrentPage = 0;
  if (viewMode.value === "calendar") {
    calendarRef.value?.getApi().gotoDate(next);
    return;
  }
  queryRange.value = queryRangeForYear(next);
  void reloadActiveView();
}

/** aside 연/월 선택 → 이동일을 해당 월 1일로 갱신 후 기존 이동일 경로(goToAnchorDate)로 반영한다. */
function onAsideGoto(yy: number, mnth: number) {
  anchorDateText.value = formatDate(new Date(yy, mnth - 1, 1));
  goToAnchorDate();
}

/** aside TODAY → 이동일을 오늘로 갱신 후 이동한다. */
function onAsideToday() {
  anchorDateText.value = formatDate(new Date());
  goToAnchorDate();
}

function toggleFilter(key: keyof ScheduleFilter, event: Event) {
  scheduleStore.setFilter({ [key]: (event.target as HTMLInputElement).checked });
  scheduleStore.listCurrentPage = 0;
  void reloadActiveView();
}

async function openListRow(row: ScheduleListRow) {
  if (row.scheduleCd === scheduleStore.bootstrap.vcatnCd || row.scheduleCd === scheduleStore.bootstrap.brthdyCd) return;
  if (!await assertAuthenticatedBeforeModal()) return;
  try {
    detail.value = await scheduleStore.fetchDetail(row.id);
    detailModal?.show();
  } catch (error) {
    void swalAlert(error instanceof Error ? error.message : t("schedule.detail.load.failure"));
  }
}

async function onEventClick(arg: EventClickArg) {
  const scheduleCd = arg.event.groupId || "";
  if (scheduleCd === scheduleStore.bootstrap.vcatnCd || scheduleCd === scheduleStore.bootstrap.brthdyCd) return;
  if (!await assertAuthenticatedBeforeModal()) return;
  try {
    detail.value = await scheduleStore.fetchDetail(arg.event.id);
    detailModal?.show();
  } catch (error) {
    void swalAlert(error instanceof Error ? error.message : t("schedule.detail.load.failure"));
  }
}

function resetRegistForm(isPrivate: boolean, source?: ScheduleDetail) {
  isPrivateRegist.value = isPrivate;
  registForm.id = source?.id;
  registForm.scheduleCd = source?.scheduleCd ?? "";
  registForm.title = source?.title ?? "";
  registForm.content = source?.content ?? "";
  registForm.bgnDt = source?.bgnDt ?? formatDate(today);
  registForm.endDt = source?.endDt ?? source?.bgnDt ?? formatDate(today);
  registForm.privateYn = isPrivate ? "Y" : source?.privateYn ?? "N";
  registForm.prtcpntList = [...(source?.prtcpntList ?? [])];
  showEndDate.value = registForm.scheduleCd !== scheduleStore.holyDayCode;
}

/** 레거시 _initDatepickers(cF.datepicker.singleDatePicker #bgnDt/#endDt) 동등 — 모달 열 때마다 재초기화한다. */
function attachRegistDatePickers(): void {
  destroyRegistDatePickers();
  if (bgnDtInputRef.value) {
    bgnDtFp = bindSingleDatePicker(bgnDtInputRef.value, {
      initial: registForm.bgnDt,
      onValue: (dateStr) => { registForm.bgnDt = dateStr; },
    });
  }
  if (endDtInputRef.value) {
    endDtFp = bindSingleDatePicker(endDtInputRef.value, {
      initial: registForm.endDt,
      onValue: (dateStr) => { registForm.endDt = dateStr; },
    });
  }
}

function destroyRegistDatePickers(): void {
  destroySingleDatePicker(bgnDtFp);
  destroySingleDatePicker(endDtFp);
  bgnDtFp = null;
  endDtFp = null;
}

async function openRegist(isPrivate: boolean) {
  if (!await assertAuthenticatedBeforeModal()) return;
  resetRegistForm(isPrivate);
  registModal?.show();
  await nextTick();
  attachRegistDatePickers();
}

function closeRegist() {
  destroyRegistDatePickers();
  registModal?.hide();
}

function closeDetail() {
  detailModal?.hide();
}

function addParticipant() {
  registForm.prtcpntList.push({ username: "" });
}

function removeParticipant(index: number) {
  registForm.prtcpntList.splice(index, 1);
}

function onScheduleCodeChange() {
  showEndDate.value = registForm.scheduleCd !== scheduleStore.holyDayCode;
  if (!showEndDate.value) {
    registForm.endDt = registForm.bgnDt;
    // 종료일을 프로그램으로 덮어쓴 경우 flatpickr 표시값도 동기화한다 (재표시 시 불일치 방지).
    endDtFp?.setDate(registForm.endDt ?? "", false);
  }
}

async function submitRegist() {
  if (!registForm.scheduleCd || !registForm.title || !registForm.bgnDt) {
    void swalAlert(t("schedule.validate.required"));
    return;
  }
  if (!await swalConfirm(registForm.id ? t("schedule.confirm.edit") : t("schedule.confirm.register"))) return;

  submitting.value = true;
  try {
    const message = await scheduleStore.saveSchedule({
      ...registForm,
      endDt: showEndDate.value ? registForm.endDt : registForm.bgnDt,
      prtcpntList: registForm.prtcpntList.filter((item) => item.username),
    });
    closeRegist();
    await swalAlert(message);
    await reloadActiveView();
  } catch (error) {
    void swalAlert(error instanceof Error ? error.message : t("schedule.save.failure"));
  } finally {
    submitting.value = false;
  }
}

async function modifyDetail() {
  if (!detail.value) return;
  if (!await assertAuthenticatedBeforeModal()) return;
  closeDetail();
  resetRegistForm(detail.value.privateYn === "Y", detail.value);
  registModal?.show();
  await nextTick();
  attachRegistDatePickers();
}

async function deleteDetail() {
  if (!detail.value?.id) return;
  if (!await swalConfirm(t("schedule.delete.confirm"))) return;
  try {
    const message = await scheduleStore.deleteSchedule(detail.value.id);
    closeDetail();
    await swalAlert(message);
    await reloadActiveView();
  } catch (error) {
    void swalAlert(error instanceof Error ? error.message : t("schedule.delete.failure"));
  }
}

/** 이동일 flatpickr 바인딩 — 툴바 input 은 상시 존재하므로 mount 시 1회 attach, unmount 시 destroy. */
function attachAnchorDatePicker(): void {
  if (!anchorDtInputRef.value) return;
  anchorDtFp = bindSingleDatePicker(anchorDtInputRef.value, {
    initial: anchorDateText.value,
    onValue: (dateStr) => {
      anchorDateText.value = dateStr;
      goToAnchorDate();
    },
  });
}

// 달력 이동(datesSet) 등 외부에서 이동일이 갱신되면 flatpickr 표시값도 동기화한다.
watch(anchorDateText, (next) => {
  if (anchorDtFp && anchorDtInputRef.value?.value !== next) {
    anchorDtFp.setDate(next, false);
  }
});

onMounted(async () => {
  if (registModalEl.value) registModal = new Modal(registModalEl.value);
  if (detailModalEl.value) detailModal = new Modal(detailModalEl.value);
  attachAnchorDatePicker();
  await scheduleStore.fetchBootstrap();
  const registQuery = route.query.regist;
  if (registQuery === "1" || registQuery === "private") {
    await openRegist(registQuery === "private");
    const nextQuery = { ...route.query };
    delete nextQuery.regist;
    void router.replace({ query: nextQuery });
  }
});

onBeforeUnmount(() => {
  destroySingleDatePicker(anchorDtFp);
  anchorDtFp = null;
  destroyRegistDatePickers();
});
</script>

<style scoped>
.schedule-view-toolbar__date-input {
  width: 148px;
}

/* aside sticky — 저널 aside(.journal-layout-vue__aside)와 동일 규칙 */
.schedule-calendar-page__aside {
  position: sticky;
  top: 1rem;
  align-self: flex-start;
  max-height: calc(100vh - 2rem);
  overflow-y: auto;
  scrollbar-gutter: stable;
}

.schedule-filter {
  padding: 0.75rem 1.25rem 1rem;
  background: var(--bs-light);
  border-radius: 0.475rem;
}

.schedule-filter.show,
.schedule-filter.collapsing {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
}

.schedule-calendar-card :deep(.fc) {
  min-height: 680px;
}

.schedule-loading {
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

.schedule-participants {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.schedule-participants__row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 0.5rem;
}

.schedule-detail-row {
  display: grid;
  grid-template-columns: 140px minmax(0, 1fr);
  gap: 1rem;
  padding: 0.75rem 0;
  border-bottom: 1px dashed var(--bs-gray-300);
}

.schedule-detail-row__label {
  font-weight: 700;
  color: var(--bs-gray-700);
}

.schedule-detail-row__content {
  white-space: pre-wrap;
}

.schedule-list-loading {
  padding: 2rem;
  text-align: center;
  color: var(--bs-gray-700);
}

.schedule-list-table__row {
  cursor: pointer;
}

.schedule-list-table__row:hover {
  background: var(--bs-gray-100);
}

.schedule-list-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.schedule-list-page-size {
  width: 88px;
}

@media (max-width: 768px) {
  .schedule-detail-row {
    grid-template-columns: 1fr;
    gap: 0.25rem;
  }
}
</style>
