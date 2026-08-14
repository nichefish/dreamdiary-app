import { beforeEach, describe, expect, it, vi } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import axios from "axios";
import { useScheduleStore } from "./schedule";

vi.mock("axios", () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

vi.mock("@/shared/i18n/stores/locale", () => ({
  useLocaleStore: () => ({
    t: (key: string) => key,
  }),
}));

/** node 환경에는 window/localStorage 가 없다. 필터 영속(readFilter/writeFilter)용 최소 스텁. */
const localStorageStub = {
  getItem: vi.fn<[string], string | null>(() => null),
  setItem: vi.fn<[string, string], void>(() => undefined),
};

const RANGE = { yy: 2026, bgnDt: "2026-08-01", endDt: "2026-08-31" };
/** 기본 필터(myPapr=N, 나머지 Y) 기준 조회 파라미터 flags */
const DEFAULT_FLAGS = {
  myPaprChked: "N",
  vcatnChked: "Y",
  indtChked: "Y",
  outdtChked: "Y",
  tlcmmtChked: "Y",
  prvtChked: "Y",
};

describe("schedule 스토어", () => {
  const mockedGet = vi.mocked(axios.get);
  const mockedPost = vi.mocked(axios.post);

  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
    localStorageStub.getItem.mockReturnValue(null);
    vi.stubGlobal("window", { localStorage: localStorageStub });
  });

  it("부트스트랩 조회는 GET /api/schedule/bootstrap 의 rsltObj 를 담고 옵션을 파생한다", async () => {
    mockedGet.mockResolvedValueOnce({
      data: { rslt: true, rsltObj: { codeOptions: [{ code: "INDT", codeName: "가상 사내" }], holyDayCode: "HOLYDAY" } },
    });
    const store = useScheduleStore();

    await store.fetchBootstrap();

    expect(mockedGet).toHaveBeenCalledWith("/api/schedule/bootstrap");
    expect(store.codeOptions).toEqual([{ code: "INDT", codeName: "가상 사내" }]);
    expect(store.holyDayCode).toBe("HOLYDAY");
  });

  it("달력 이벤트 조회는 기간·필터 파라미터로 GET 하고 rsltList 를 events 로 채운다", async () => {
    mockedGet.mockResolvedValueOnce({
      data: { rslt: true, rsltList: [{ id: 1, title: "가상 일정", start: "2026-08-14" }] },
    });
    const store = useScheduleStore();

    await store.fetchEvents(RANGE, "");

    expect(mockedGet).toHaveBeenCalledWith("/api/schedule/cal-list", {
      params: { yy: 2026, bgnDt: "2026-08-01", endDt: "2026-08-31", searchKeyword: "", ...DEFAULT_FLAGS },
    });
    expect(store.events).toHaveLength(1);
    expect(store.eventsError).toBeNull();
  });

  it("달력 이벤트 조회 soft-fail(rslt=false) 시 eventsError 를 채우고 예외를 던지지 않는다", async () => {
    mockedGet.mockResolvedValueOnce({ data: { rslt: false, message: "조회 실패" } });
    const store = useScheduleStore();

    await store.fetchEvents(RANGE, "");

    expect(store.eventsError).toBe("조회 실패");
  });

  it("목록 VIEW 조회는 page/size 를 붙여 GET 하고 rsltObj.content 를 listRows 로 채운다", async () => {
    mockedGet.mockResolvedValueOnce({
      data: { rslt: true, rsltObj: { content: [{ id: 1, scheduleCd: "INDT", title: "가상 일정", bgnDt: "2026-08-14", privateYn: "N" }], totalElements: 1, totalPages: 1, number: 0, size: 25 } },
    });
    const store = useScheduleStore();

    await store.fetchList(RANGE, "", 0);

    expect(mockedGet).toHaveBeenCalledWith("/api/schedule/list", {
      params: { yy: 2026, bgnDt: "2026-08-01", endDt: "2026-08-31", searchKeyword: "", ...DEFAULT_FLAGS, page: 0, size: 25 },
    });
    expect(store.listRows).toHaveLength(1);
    expect(store.listTotalElements).toBe(1);
  });

  it("목록 VIEW 조회 실패 시 listError 를 채우고 예외를 던진다", async () => {
    mockedGet.mockResolvedValueOnce({ data: { rslt: false, message: "목록 실패" } });
    const store = useScheduleStore();

    await expect(store.fetchList(RANGE, "", 0)).rejects.toThrow("목록 실패");
    expect(store.listError).toBe("목록 실패");
  });

  it("일정 저장은 POST /api/schedule/cal-reg 로 FormData 를 보내고 endDt 는 bgnDt 로 대체한다", async () => {
    mockedPost.mockResolvedValueOnce({ data: { rslt: true, message: "저장 완료" } });
    const store = useScheduleStore();

    const message = await store.saveSchedule({
      scheduleCd: "INDT",
      title: "가상 일정",
      bgnDt: "2026-08-14",
      privateYn: "N",
      prtcpntList: [{ username: "alice" }],
    });

    const payload = mockedPost.mock.calls[0]?.[1] as FormData;
    expect(mockedPost.mock.calls[0]?.[0]).toBe("/api/schedule/cal-reg");
    expect(payload.get("scheduleCd")).toBe("INDT");
    expect(payload.get("title")).toBe("가상 일정");
    expect(payload.get("endDt")).toBe("2026-08-14");
    expect(payload.get("prtcpntList[0].username")).toBe("alice");
    expect(message).toBe("저장 완료");
  });

  it("일정 삭제는 POST /api/schedule/cal-del 로 id FormData 를 보낸다", async () => {
    mockedPost.mockResolvedValueOnce({ data: { rslt: true, message: "삭제 완료" } });
    const store = useScheduleStore();

    await store.deleteSchedule(9);

    const payload = mockedPost.mock.calls[0]?.[1] as FormData;
    expect(mockedPost.mock.calls[0]?.[0]).toBe("/api/schedule/cal-del");
    expect(payload.get("id")).toBe("9");
  });

  it("상세 조회는 GET /api/schedule/cal-dtl 로 id 파라미터를 보낸다", async () => {
    mockedGet.mockResolvedValueOnce({ data: { rslt: true, rsltObj: { id: 9, title: "가상 일정" } } });
    const store = useScheduleStore();

    const detail = await store.fetchDetail(9);

    expect(mockedGet).toHaveBeenCalledWith("/api/schedule/cal-dtl", { params: { id: 9 } });
    expect(detail.title).toBe("가상 일정");
  });

  it("필터 변경은 상태를 병합하고 localStorage 에 영속한다", () => {
    const store = useScheduleStore();

    store.setFilter({ vcatnChk: false });

    expect(store.filter.vcatnChk).toBe(false);
    expect(store.filter.indtChk).toBe(true);
    expect(localStorageStub.setItem).toHaveBeenCalled();
  });
});