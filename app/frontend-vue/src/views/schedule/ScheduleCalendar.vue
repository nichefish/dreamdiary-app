<template>
  <div class="schedule-calendar-page">
    <div class="schedule-toolbar">
      <div class="schedule-toolbar__date">
        <label for="schedule_anchor_date" class="form-label mb-0 fw-bold">조회일</label>
        <input
          id="schedule_anchor_date"
          v-model="anchorDateText"
          type="date"
          class="form-control form-control-solid schedule-toolbar__date-input"
          @change="goToAnchorDate"
        />
      </div>

      <div class="schedule-toolbar__search">
        <input
          v-model="searchKeyword"
          type="search"
          class="form-control form-control-solid"
          placeholder="검색어"
          maxlength="200"
          @keyup.enter="reload"
        />
        <button type="button" class="btn btn-sm btn-light-primary" @click="reload">
          <i class="bi bi-search"></i>
        </button>
      </div>

      <button
        type="button"
        class="btn btn-sm btn-light-primary"
        data-bs-toggle="collapse"
        data-bs-target="#schedule_filter_panel"
      >
        <i class="bi bi-funnel"></i>
      </button>

      <div class="schedule-toolbar__actions">
        <button type="button" class="btn btn-sm btn-primary" @click="openRegist(false)">
          <i class="bi bi-plus-lg"></i>
          일정 등록
        </button>
        <button type="button" class="btn btn-sm btn-light-primary" @click="openRegist(true)">
          <i class="bi bi-lock"></i>
          개인 일정
        </button>
      </div>
    </div>

    <div id="schedule_filter_panel" class="collapse schedule-filter">
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

    <div class="card post schedule-calendar-card">
      <div class="card-header min-h-auto">
        <ul class="nav nav-tabs nav-tabs-line ps-2 mt-5">
          <li class="nav-item">
            <span class="nav-link px-6 active">
              <i class="bi bi-calendar3 me-2"></i>
              달력 VIEW
            </span>
          </li>
          <li class="nav-item">
            <button type="button" class="nav-link px-6 border-0 bg-transparent text-muted" @click="showListNotice">
              <i class="bi bi-people me-2"></i>
              목록 VIEW
            </button>
          </li>
        </ul>
      </div>
      <div class="card-body position-relative">
        <div v-if="scheduleStore.loading" class="schedule-loading">
          <span class="spinner-border spinner-border-sm me-2"></span>
          불러오는 중
        </div>
        <FullCalendar ref="calendarRef" :options="calendarOptions" />
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
import { swalConfirm, swalAlert } from "@/utils/swal";
import { computed, onMounted, reactive, ref } from "vue";
import FullCalendar from "@fullcalendar/vue3";
import dayGridPlugin from "@fullcalendar/daygrid";
import timeGridPlugin from "@fullcalendar/timegrid";
import listPlugin from "@fullcalendar/list";
import interactionPlugin from "@fullcalendar/interaction";
import type { CalendarOptions, DatesSetArg, EventClickArg } from "@fullcalendar/core";
import { Modal } from "bootstrap";
import { useScheduleStore, type ScheduleDetail, type ScheduleFilter, type ScheduleForm } from "@/stores/schedule";

const scheduleStore = useScheduleStore();

const calendarRef = ref<any>(null);
const registModalEl = ref<HTMLElement | null>(null);
const detailModalEl = ref<HTMLElement | null>(null);
let registModal: Modal | null = null;
let detailModal: Modal | null = null;

const today = new Date();
const currentAnchor = ref(new Date(today.getFullYear(), today.getMonth(), 1));
const anchorDateText = ref(formatDate(currentAnchor.value));
const searchKeyword = ref("");
const submitting = ref(false);
const isPrivateRegist = ref(false);
const showEndDate = ref(true);
const detail = ref<ScheduleDetail | null>(null);

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

async function reload() {
  await scheduleStore.fetchEvents(currentAnchor.value, searchKeyword.value);
}

async function onDatesSet(arg: DatesSetArg) {
  currentAnchor.value = new Date(arg.view.currentStart);
  anchorDateText.value = formatDate(currentAnchor.value);
  await reload();
}

function goToAnchorDate() {
  const next = parseDate(anchorDateText.value);
  calendarRef.value?.getApi().gotoDate(next);
  currentAnchor.value = next;
  void reload();
}

function toggleFilter(key: keyof ScheduleFilter, event: Event) {
  scheduleStore.setFilter({ [key]: (event.target as HTMLInputElement).checked });
  void reload();
}

async function onEventClick(arg: EventClickArg) {
  const scheduleCd = arg.event.groupId || "";
  if (scheduleCd === scheduleStore.bootstrap.vcatnCd || scheduleCd === scheduleStore.bootstrap.brthdyCd) return;
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

function openRegist(isPrivate: boolean) {
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
    await reload();
  } catch (error) {
    void swalAlert(error instanceof Error ? error.message : "일정을 저장하지 못했습니다.");
  } finally {
    submitting.value = false;
  }
}

function modifyDetail() {
  if (!detail.value) return;
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
    await reload();
  } catch (error) {
    void swalAlert(error instanceof Error ? error.message : "일정을 삭제하지 못했습니다.");
  }
}

function showListNotice() {
  void swalAlert("목록 VIEW는 아직 준비 중입니다.");
}

onMounted(async () => {
  if (registModalEl.value) registModal = new Modal(registModalEl.value);
  if (detailModalEl.value) detailModal = new Modal(detailModalEl.value);
  await scheduleStore.fetchBootstrap();
  await reload();
});
</script>

<style scoped>
.schedule-calendar-page {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.schedule-toolbar {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.schedule-toolbar__date,
.schedule-toolbar__search,
.schedule-toolbar__actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.schedule-toolbar__date-input {
  width: 160px;
}

.schedule-toolbar__search {
  min-width: min(100%, 320px);
}

.schedule-filter {
  padding: 1rem 1.25rem;
  background: var(--bs-light);
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

@media (max-width: 768px) {
  .schedule-toolbar {
    align-items: stretch;
  }

  .schedule-toolbar__date,
  .schedule-toolbar__search,
  .schedule-toolbar__actions {
    width: 100%;
  }

  .schedule-toolbar__search input,
  .schedule-toolbar__actions .btn {
    flex: 1;
  }

  .schedule-detail-row {
    grid-template-columns: 1fr;
    gap: 0.25rem;
  }
}
</style>
