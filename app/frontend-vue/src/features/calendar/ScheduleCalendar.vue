1<template>
  <div class="schedule-calendar-page">
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
            <span class="nav-text">달력 VIEW</span>
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
            <span class="nav-text">목록 VIEW</span>
          </button>
        </li>
      </ul>

      <div class="schedule-view-toolbar__tools d-none d-md-flex align-items-center flex-shrink-0 pe-5 mt-3 gap-2">
        <div class="d-flex align-items-center gap-2">
          <label for="schedule_anchor_date" class="form-label mb-0 text-nowrap fs-7 fw-bold">이동일</label>
          <input
            id="schedule_anchor_date"
            v-model="anchorDateText"
            type="date"
            class="form-control form-control-sm form-control-solid schedule-view-toolbar__date-input"
            @change="goToAnchorDate"
          />
        </div>
        <div class="input-group input-group-sm">
          <input
            v-model="searchKeyword"
            type="search"
            class="form-control form-control-sm form-control-solid"
            placeholder="검색어"
            maxlength="200"
            style="min-width: 140px;"
            @keyup.enter="reloadActiveView"
          />
          <button type="button" class="btn btn-sm btn-icon btn-light" title="검색" @click="reloadActiveView">
            <i class="bi bi-search fs-7"></i>
          </button>
        </div>
        <button
          type="button"
          class="btn btn-sm btn-icon btn-light"
          title="고급 필터"
          data-bs-toggle="collapse"
          data-bs-target="#schedule_filter_panel"
        >
          <i class="bi bi-funnel fs-7"></i>
        </button>
        <div class="vr mx-1 opacity-25"></div>
        <button type="button" class="btn btn-sm btn-light-primary btn-outlined ps-4 pe-3 py-2 text-nowrap" @click="openRegist(false)">
          <i class="bi bi-plus-lg fs-4 pe-1"></i>
          일정 등록
        </button>
        <button type="button" class="btn btn-sm btn-light-primary btn-outlined ps-4 pe-3 py-2 text-nowrap" @click="openRegist(true)">
          <i class="bi bi-lock fs-4 pe-1"></i>
          개인 일정
        </button>
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
          불러오는 중
        </div>
        <FullCalendar ref="calendarRef" :options="calendarOptions" />
      </div>
      <div v-show="viewMode === 'list'" class="card-body">
        <div v-if="scheduleStore.listLoading" class="schedule-list-loading">
          <span class="spinner-border spinner-border-sm me-2"></span>
          불러오는 중
        </div>
        <div v-else class="table-responsive">
          <table class="table table-row-bordered table-row-gray-300 align-middle gs-0 gy-3 schedule-list-table">
            <thead>
              <tr class="fw-bold text-muted">
                <th class="min-w-90px">구분</th>
                <th class="min-w-200px">제목</th>
                <th class="min-w-100px">시작일</th>
                <th class="min-w-100px">종료일</th>
                <th>참여자</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="scheduleStore.listRows.length === 0">
                <td colspan="5" class="text-center text-muted py-10">표시할 일정이 없습니다.</td>
              </tr>
              <tr
                v-for="row in scheduleStore.listRows"
                :key="row.id"
                class="schedule-list-table__row"
                @click="openListRow(row)"
              >
                <td>{{ row.scheduleNm || row.scheduleCd }}</td>
                <td>
                  <span v-if="row.privateYn === 'Y'" class="me-1 text-muted" title="개인 일정">
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
        <span class="text-muted fs-8">총 {{ formatNumber(scheduleStore.listTotalElements) }}건</span>
        <div class="d-flex align-items-center gap-2">
          <select
            :value="scheduleStore.listPageSize"
            class="form-select form-select-solid form-select-sm schedule-list-page-size"
            @change="onListPageSizeChange"
          >
            <option :value="10">10개</option>
            <option :value="25">25개</option>
            <option :value="50">50개</option>
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

    <div ref="registModalEl" class="modal fade" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">일정 저장</h5>
            <button type="button" class="btn-close" @click="closeRegist"></button>
          </div>
          <div class="modal-body">
            <form class="form" @submit.prevent="submitRegist">
              <input type="hidden" :value="registForm.id ?? ''" />

              <div class="row g-3 mb-3">
                <div class="col-md-4">
                  <label class="form-label required" for="scheduleCd">구분</label>
                  <select id="scheduleCd" v-model="registForm.scheduleCd" class="form-select form-select-solid" required @change="onScheduleCodeChange">
                    <option value="">선택</option>
                    <option v-for="code in filteredCodeOptions" :key="code.code" :value="code.code">
                      {{ code.codeName }}
                    </option>
                  </select>
                </div>
                <div class="col-md-8">
                  <label class="form-label required" for="scheduleTitle">제목</label>
                  <input id="scheduleTitle" v-model="registForm.title" class="form-control form-control-solid" maxlength="120" required />
                </div>
              </div>

              <div class="row g-3 mb-3">
                <div class="col-md-6">
                  <label class="form-label required" for="scheduleBgnDt">시작일</label>
                  <input id="scheduleBgnDt" v-model="registForm.bgnDt" type="date" class="form-control form-control-solid" required />
                </div>
                <div class="col-md-6" v-if="showEndDate">
                  <label class="form-label" for="scheduleEndDt">종료일</label>
                  <input id="scheduleEndDt" v-model="registForm.endDt" type="date" class="form-control form-control-solid" />
                </div>
              </div>

              <div class="mb-3">
                <label class="form-label" for="scheduleContent">내용</label>
                <textarea id="scheduleContent" v-model="registForm.content" class="form-control form-control-solid" rows="4" maxlength="500"></textarea>
              </div>

              <div class="mb-3">
                <div class="d-flex align-items-center justify-content-between mb-2">
                  <label class="form-label mb-0">참여자</label>
                  <button v-if="!isPrivateRegist" type="button" class="btn btn-sm btn-icon btn-light-primary" @click="addParticipant">
                    <i class="bi bi-plus-lg"></i>
                  </button>
                </div>
                <div v-if="isPrivateRegist" class="text-muted fs-7">
                  개인 일정은 본인과 참여자 화면에만 표시됩니다.
                </div>
                <div v-else class="schedule-participants">
                  <div v-for="(participant, index) in registForm.prtcpntList" :key="index" class="schedule-participants__row">
                    <select v-model="participant.username" class="form-select form-select-solid">
                      <option value="">선택</option>
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
              저장
            </button>
            <button type="button" class="btn btn-sm btn-light" @click="closeRegist">닫기</button>
          </div>
        </div>
      </div>
    </div>

    <div ref="detailModalEl" class="modal fade" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">일정 정보</h5>
            <button type="button" class="btn-close" @click="closeDetail"></button>
          </div>
          <div class="modal-body">
            <template v-if="detail">
              <div class="schedule-detail-row">
                <span class="schedule-detail-row__label"><i class="bi bi-justify-left"></i> 제목</span>
                <span>{{ detailTitle }}</span>
              </div>
              <div v-if="!isDetailHolyday" class="schedule-detail-row">
                <span class="schedule-detail-row__label"><i class="bi bi-people"></i> 참여자</span>
                <span>{{ detail.prtcpnt || "-" }}</span>
              </div>
              <div class="schedule-detail-row">
                <span class="schedule-detail-row__label"><i class="bi bi-info-circle"></i> 설명</span>
                <span class="schedule-detail-row__content">{{ detail.content || "-" }}</span>
              </div>
              <div class="schedule-detail-row">
                <span class="schedule-detail-row__label"><i class="bi bi-calendar3"></i> 일정</span>
                <span>{{ detail.bgnDt }}<template v-if="detail.endDt"> - {{ detail.endDt }}</template></span>
              </div>
            </template>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-sm btn-light-primary" :disabled="!detail || isDetailHolyday" @click="modifyDetail">
              수정
            </button>
            <button type="button" class="btn btn-sm btn-light-danger" :disabled="!detail || isDetailHolyday" @click="deleteDetail">
              삭제
            </button>
            <button type="button" class="btn btn-sm btn-light" @click="closeDetail">닫기</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { swalConfirm, swalAlert } from "@/shared/utils/swal";
import { computed, onMounted, reactive, ref } from "vue";
import FullCalendar from "@fullcalendar/vue3";
import dayGridPlugin from "@fullcalendar/daygrid";
import timeGridPlugin from "@fullcalendar/timegrid";
import listPlugin from "@fullcalendar/list";
import interactionPlugin from "@fullcalendar/interaction";
import type { CalendarOptions, DatesSetArg, EventClickArg } from "@fullcalendar/core";
import { Modal } from "bootstrap";
import { assertAuthenticatedBeforeModal } from "@/shared/auth/sessionPing";
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

const calendarRef = ref<any>(null);
const registModalEl = ref<HTMLElement | null>(null);
const detailModalEl = ref<HTMLElement | null>(null);
let registModal: Modal | null = null;
let detailModal: Modal | null = null;

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

const filterItems: Array<{ key: keyof ScheduleFilter; label: string }> = [
  { key: "myPaprChk", label: "내 일정만" },
  { key: "vcatnChk", label: "휴가" },
  { key: "indtChk", label: "입사" },
  { key: "outdtChk", label: "퇴사" },
  { key: "tlcmmtChk", label: "재택근무" },
  { key: "prvtChk", label: "개인 일정" },
];

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
    void swalAlert(error instanceof Error ? error.message : "일정 목록을 불러오지 못했습니다.");
  }
}

async function goListPage(page: number) {
  try {
    await scheduleStore.fetchList(queryRange.value, searchKeyword.value, page);
  } catch (error) {
    void swalAlert(error instanceof Error ? error.message : "일정 목록을 불러오지 못했습니다.");
  }
}

async function onListPageSizeChange(event: Event) {
  const size = Number((event.target as HTMLSelectElement).value);
  await scheduleStore.changeListPageSize(size);
  try {
    await scheduleStore.fetchList(queryRange.value, searchKeyword.value, 0);
  } catch (error) {
    void swalAlert(error instanceof Error ? error.message : "일정 목록을 불러오지 못했습니다.");
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
    void swalAlert(error instanceof Error ? error.message : "일정 정보를 조회하지 못했습니다.");
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
    void swalAlert(error instanceof Error ? error.message : "일정 정보를 조회하지 못했습니다.");
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

async function openRegist(isPrivate: boolean) {
  if (!await assertAuthenticatedBeforeModal()) return;
  resetRegistForm(isPrivate);
  registModal?.show();
}

function closeRegist() {
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
  if (!showEndDate.value) registForm.endDt = registForm.bgnDt;
}

async function submitRegist() {
  if (!registForm.scheduleCd || !registForm.title || !registForm.bgnDt) {
    void swalAlert("필수 값을 입력해 주세요.");
    return;
  }
  if (!await swalConfirm(registForm.id ? "일정을 수정하시겠습니까?" : "일정을 등록하시겠습니까?")) return;

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
    void swalAlert(error instanceof Error ? error.message : "일정을 저장하지 못했습니다.");
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
}

async function deleteDetail() {
  if (!detail.value?.id) return;
  if (!await swalConfirm("일정을 삭제하시겠습니까?")) return;
  try {
    const message = await scheduleStore.deleteSchedule(detail.value.id);
    closeDetail();
    await swalAlert(message);
    await reloadActiveView();
  } catch (error) {
    void swalAlert(error instanceof Error ? error.message : "일정을 삭제하지 못했습니다.");
  }
}

onMounted(async () => {
  if (registModalEl.value) registModal = new Modal(registModalEl.value);
  if (detailModalEl.value) detailModal = new Modal(detailModalEl.value);
  await scheduleStore.fetchBootstrap();
});
</script>

<style scoped>
.schedule-view-toolbar__date-input {
  width: 148px;
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
