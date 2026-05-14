import { computed, ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";

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
  const bootstrap = ref<ScheduleBootstrap>({});
  const events = ref<ScheduleCalendarEvent[]>([]);
  const filter = ref<ScheduleFilter>(readFilter());
  const loading = ref(false);

  const codeOptions = computed(() => bootstrap.value.codeOptions ?? []);
  const userOptions = computed(() => bootstrap.value.userOptions ?? []);
  const holyDayCode = computed(() => bootstrap.value.holyDayCode ?? "HOLYDAY");

  async function fetchBootstrap() {
    const res = await axios.get("/api/schedule/bootstrap");
    bootstrap.value = res.data?.rsltObj ?? {};
  }

  async function fetchEvents(anchorDate: Date, searchKeyword = "") {
    loading.value = true;
    try {
      const params = {
        yy: anchorDate.getFullYear(),
        bgnDt: toDateString(addDays(anchorDate, -35)),
        endDt: toDateString(addDays(anchorDate, 45)),
        myPaprChked: filter.value.myPaprChk ? "Y" : "N",
        vcatnChked: filter.value.vcatnChk ? "Y" : "N",
        indtChked: filter.value.indtChk ? "Y" : "N",
        outdtChked: filter.value.outdtChk ? "Y" : "N",
        tlcmmtChked: filter.value.tlcmmtChk ? "Y" : "N",
        prvtChked: filter.value.prvtChk ? "Y" : "N",
        searchKeyword,
      };
      const res = await axios.get("/api/schedule/cal-list", { params });
      events.value = res.data?.rslt ? res.data?.rsltList ?? [] : [];
    } finally {
      loading.value = false;
    }
  }

  function setFilter(next: Partial<ScheduleFilter>) {
    filter.value = { ...filter.value, ...next };
    writeFilter(filter.value);
  }

  async function fetchDetail(id: string | number): Promise<ScheduleDetail> {
    const res = await axios.get("/api/schedule/cal-dtl", { params: { id } });
    if (!res.data?.rslt) throw new Error(res.data?.message ?? "일정 정보를 조회하지 못했습니다.");
    return res.data.rsltObj;
  }

  async function saveSchedule(form: ScheduleForm) {
    const fd = new FormData();
    if (form.id) fd.append("id", String(form.id));
    fd.append("scheduleCd", form.scheduleCd);
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
    if (!res.data?.rslt) throw new Error(res.data?.message ?? "일정을 저장하지 못했습니다.");
  }

  async function deleteSchedule(id: string | number) {
    const fd = new FormData();
    fd.append("id", String(id));
    const res = await axios.post("/api/schedule/cal-del", fd);
    if (!res.data?.rslt) throw new Error(res.data?.message ?? "일정을 삭제하지 못했습니다.");
  }

  return {
    bootstrap,
    events,
    filter,
    loading,
    codeOptions,
    userOptions,
    holyDayCode,
    fetchBootstrap,
    fetchEvents,
    setFilter,
    fetchDetail,
    saveSchedule,
    deleteSchedule,
  };
});
