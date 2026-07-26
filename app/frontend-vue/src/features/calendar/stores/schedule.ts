import { computed, ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

export interface ScheduleCodeOption {
  code: string;
  codeName: string;
}

export interface ScheduleUserOption {
  username: string;
  userNm: string;
}

export interface ScheduleBootstrap {
  vcatnCd?: string;
  brthdyCd?: string;
  holyDayCode?: string;
  codeOptions?: ScheduleCodeOption[];
  vcatnCodeOptions?: ScheduleCodeOption[];
  jandiTopicOptions?: ScheduleCodeOption[];
  userOptions?: ScheduleUserOption[];
}

export interface ScheduleFilter {
  myPaprChk: boolean;
  vcatnChk: boolean;
  indtChk: boolean;
  outdtChk: boolean;
  tlcmmtChk: boolean;
  prvtChk: boolean;
}

export interface ScheduleCalendarEvent {
  id: string | number;
  title: string;
  start: string;
  end?: string;
  groupId?: string;
  color?: string;
  display?: string;
  [key: string]: unknown;
}

export interface ScheduleParticipant {
  username: string;
  userNm?: string;
}

export interface ScheduleForm {
  id?: string | number;
  scheduleCd: string;
  vcatnCd?: string;
  title: string;
  content?: string;
  bgnDt: string;
  endDt?: string;
  privateYn: "Y" | "N";
  prtcpntList: ScheduleParticipant[];
}

export interface ScheduleDetail extends ScheduleForm {
  scheduleNm?: string;
  prtcpnt?: string;
  isCreatedBy?: boolean;
}

/** 달력·목록 API 공통 조회 구간 (bgnDt/endDt → 서버 searchStartDt/searchEndDt) */
export interface ScheduleQueryRange {
  bgnDt: string;
  endDt: string;
  yy: number;
}

/** 목록 VIEW 행 (GET /api/schedule/list) */
export interface ScheduleListRow {
  id: number;
  scheduleCd: string;
  scheduleNm?: string;
  title: string;
  bgnDt: string;
  endDt?: string;
  privateYn: "Y" | "N";
  prtcpntListStr?: string;
}

const DEFAULT_FILTER: ScheduleFilter = {
  myPaprChk: false,
  vcatnChk: true,
  indtChk: true,
  outdtChk: true,
  tlcmmtChk: true,
  prvtChk: true,
};

const FILTER_STORAGE_KEY = "schedule_calendar_filter";

function toDateString(date: Date): string {
  const yyyy = date.getFullYear();
  const mm = String(date.getMonth() + 1).padStart(2, "0");
  const dd = String(date.getDate()).padStart(2, "0");
  return `${yyyy}-${mm}-${dd}`;
}

function addDays(date: Date, days: number): Date {
  const next = new Date(date);
  next.setDate(next.getDate() + days);
  return next;
}

/**
 * FullCalendar datesSet 구간 → API 조회 범위.
 * end는 FC 규약상 exclusive 이므로 하루 빼서 inclusive endDt 로 맞춘다.
 */
export function queryRangeFromVisible(start: Date, endExclusive: Date): ScheduleQueryRange {
  const endInclusive = addDays(endExclusive, -1);
  return {
    yy: start.getFullYear(),
    bgnDt: toDateString(start),
    endDt: toDateString(endInclusive),
  };
}

/** 목록 VIEW: 이동일 기준 해당 연도 전체 */
export function queryRangeForYear(anchor: Date): ScheduleQueryRange {
  const yyyy = anchor.getFullYear();
  return { yy: yyyy, bgnDt: `${yyyy}-01-01`, endDt: `${yyyy}-12-31` };
}

/** 초기 로드·달력 미마운트 시 당월 1일~말일 */
export function queryRangeForMonth(anchor: Date): ScheduleQueryRange {
  const start = new Date(anchor.getFullYear(), anchor.getMonth(), 1);
  const endExclusive = new Date(anchor.getFullYear(), anchor.getMonth() + 1, 1);
  return queryRangeFromVisible(start, endExclusive);
}

function readFilter(): ScheduleFilter {
  const raw = window.localStorage.getItem(FILTER_STORAGE_KEY);
  if (!raw) return { ...DEFAULT_FILTER };
  try {
    return { ...DEFAULT_FILTER, ...JSON.parse(raw) };
  } catch {
    return { ...DEFAULT_FILTER };
  }
}

function writeFilter(filter: ScheduleFilter) {
  window.localStorage.setItem(FILTER_STORAGE_KEY, JSON.stringify(filter));
}

export const useScheduleStore = defineStore("schedule", () => {
  const { t } = useLocaleStore();
  const bootstrap = ref<ScheduleBootstrap>({});
  const events = ref<ScheduleCalendarEvent[]>([]);
  const listRows = ref<ScheduleListRow[]>([]);
  const listCurrentPage = ref(0);
  const listPageSize = ref(25);
  const listTotalElements = ref(0);
  const listTotalPages = ref(0);
  const filter = ref<ScheduleFilter>(readFilter());
  const loading = ref(false);
  const listLoading = ref(false);

  const codeOptions = computed(() => bootstrap.value.codeOptions ?? []);
  const vcatnCodeOptions = computed(() => bootstrap.value.vcatnCodeOptions ?? []);
  const userOptions = computed(() => bootstrap.value.userOptions ?? []);
  const holyDayCode = computed(() => bootstrap.value.holyDayCode ?? "HOLYDAY");

  async function fetchBootstrap() {
    const res = await axios.get("/api/schedule/bootstrap");
    bootstrap.value = res.data?.rsltObj ?? {};
  }

  function buildQueryParams(range: ScheduleQueryRange, searchKeyword = "") {
    return {
      yy: range.yy,
      bgnDt: range.bgnDt,
      endDt: range.endDt,
      myPaprChked: filter.value.myPaprChk ? "Y" : "N",
      vcatnChked: filter.value.vcatnChk ? "Y" : "N",
      indtChked: filter.value.indtChk ? "Y" : "N",
      outdtChked: filter.value.outdtChk ? "Y" : "N",
      tlcmmtChked: filter.value.tlcmmtChk ? "Y" : "N",
      prvtChked: filter.value.prvtChk ? "Y" : "N",
      searchKeyword,
    };
  }

  async function fetchEvents(range: ScheduleQueryRange, searchKeyword = "") {
    loading.value = true;
    try {
      const res = await axios.get("/api/schedule/cal-list", {
        params: buildQueryParams(range, searchKeyword),
      });
      events.value = res.data?.rslt ? res.data?.rsltList ?? [] : [];
    } finally {
      loading.value = false;
    }
  }

  /**
   * 목록 VIEW 조회. 달력과 동일한 기간·필터를 사용한다.
   */
  async function fetchList(range: ScheduleQueryRange, searchKeyword = "", page?: number) {
    listLoading.value = true;
    const targetPage = page ?? listCurrentPage.value;
    try {
      const res = await axios.get("/api/schedule/list", {
        params: {
          ...buildQueryParams(range, searchKeyword),
          page: targetPage,
          size: listPageSize.value,
        },
      });
      if (!res.data?.rslt) throw new Error(res.data?.message ?? t("schedule.list.load.failure"));
      const pageResult = res.data?.rsltObj ?? {};
      listRows.value = Array.isArray(pageResult.content) ? pageResult.content : [];
      listTotalElements.value = Number(pageResult.totalElements ?? 0);
      listTotalPages.value = Number(pageResult.totalPages ?? 0);
      listCurrentPage.value = Number(pageResult.number ?? targetPage);
      listPageSize.value = Number(pageResult.size ?? listPageSize.value);
    } catch (error) {
      listRows.value = [];
      listTotalElements.value = 0;
      listTotalPages.value = 0;
      throw error;
    } finally {
      listLoading.value = false;
    }
  }

  async function changeListPageSize(size: number) {
    listPageSize.value = size;
    listCurrentPage.value = 0;
  }

  function setFilter(next: Partial<ScheduleFilter>) {
    filter.value = { ...filter.value, ...next };
    writeFilter(filter.value);
  }

  async function fetchDetail(id: string | number): Promise<ScheduleDetail> {
    const res = await axios.get("/api/schedule/cal-dtl", { params: { id } });
    if (!res.data?.rslt) throw new Error(res.data?.message ?? t("schedule.detail.load.failure"));
    return res.data.rsltObj;
  }

  /**
   * 일정 등록/수정 처리.
   * 변경 전에는 화면에서 성공 직후 달력을 갱신했다.
   * 변경 후에는 성공 메시지를 반환하고 화면에서 OK 이후 달력을 갱신한다.
   */
  async function saveSchedule(form: ScheduleForm) {
    const fd = new FormData();
    if (form.id) fd.append("id", String(form.id));
    fd.append("scheduleCd", form.scheduleCd);
    if (form.vcatnCd) fd.append("vcatnCd", form.vcatnCd);
    fd.append("title", form.title);
    fd.append("content", form.content ?? "");
    fd.append("bgnDt", form.bgnDt);
    fd.append("endDt", form.endDt || form.bgnDt);
    fd.append("privateYn", form.privateYn);
    form.prtcpntList.forEach((participant, index) => {
      fd.append(`prtcpntList[${index}].username`, participant.username);
    });

    const res = await axios.post("/api/schedule/cal-reg", fd, {
      headers: { "Content-Type": "multipart/form-data" },
    });
    if (!res.data?.rslt) throw new Error(res.data?.message ?? t("schedule.save.failure"));
    return res.data?.message ?? t("schedule.save.success");
  }

  /**
   * 일정 삭제 처리.
   * 변경 전에는 화면에서 성공 직후 달력을 갱신했다.
   * 변경 후에는 성공 메시지를 반환하고 화면에서 OK 이후 달력을 갱신한다.
   */
  async function deleteSchedule(id: string | number) {
    const fd = new FormData();
    fd.append("id", String(id));
    const res = await axios.post("/api/schedule/cal-del", fd);
    if (!res.data?.rslt) throw new Error(res.data?.message ?? t("schedule.delete.failure"));
    return res.data?.message ?? t("schedule.delete.success");
  }

  return {
    bootstrap,
    events,
    listRows,
    listCurrentPage,
    listPageSize,
    listTotalElements,
    listTotalPages,
    filter,
    loading,
    listLoading,
    codeOptions,
    vcatnCodeOptions,
    userOptions,
    holyDayCode,
    fetchBootstrap,
    fetchEvents,
    fetchList,
    changeListPageSize,
    setFilter,
    fetchDetail,
    saveSchedule,
    deleteSchedule,
  };
});
