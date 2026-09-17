import { beforeEach, describe, expect, it, vi } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import axios from "axios";
import { useJournalAnnualStore } from "./journalAnnual";

vi.mock("axios", () => ({
  default: { get: vi.fn(), post: vi.fn(), delete: vi.fn() },
}));

vi.mock("@/shared/i18n/stores/locale", () => ({
  useLocaleStore: () => ({ t: (key: string) => key }),
}));

vi.mock("@/shared/auth/sessionPing", () => ({
  assertAuthenticatedBeforeModal: vi.fn().mockResolvedValue(true),
}));

const swalConfirm = vi.fn().mockResolvedValue(true);
const swalAlert = vi.fn().mockResolvedValue(undefined);
const swalRequestError = vi.fn().mockResolvedValue(undefined);
const swalAjaxResult = vi.fn().mockResolvedValue(undefined);
vi.mock("@/shared/utils/swal", () => ({
  swalConfirm: (...args: unknown[]) => swalConfirm(...args),
  swalAlert: (...args: unknown[]) => swalAlert(...args),
  swalRequestError: (...args: unknown[]) => swalRequestError(...args),
  swalAjaxResult: (...args: unknown[]) => swalAjaxResult(...args),
}));

const mockedGet = vi.mocked(axios.get);
const mockedPost = vi.mocked(axios.post);
const mockedDelete = vi.mocked(axios.delete);

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function ok(payload: Record<string, unknown> = {}): any {
  return { data: { rslt: true, ...payload } };
}
// eslint-disable-next-line @typescript-eslint/no-explicit-any
function fail(message = "실패"): any {
  return { data: { rslt: false, message } };
}

beforeEach(() => {
  setActivePinia(createPinia());
  vi.clearAllMocks();
  swalConfirm.mockResolvedValue(true);
  mockedGet.mockResolvedValue(ok({ rsltList: [], rsltObj: {} }));
  mockedPost.mockResolvedValue(ok());
  mockedDelete.mockResolvedValue(ok());
});

describe("목록/총집계 (lenient)", () => {
  it("fetchList 는 rsltList 를 원본 목록으로 반영한다", async () => {
    const store = useJournalAnnualStore();
    mockedGet.mockResolvedValueOnce(ok({ rsltList: [{ id: 1, yy: 2030 }, { id: 2, yy: 2029 }] }));

    await store.fetchList();

    expect(mockedGet).toHaveBeenCalledWith("/api/journal/annuals");
    expect(store.annualList.map((a) => a.id)).toEqual([1, 2]);
    expect(store.error).toBeNull();
    expect(store.loading).toBe(false);
  });

  it("fetchList 실패는 error 를 남기고 목록을 비운다", async () => {
    const store = useJournalAnnualStore();
    mockedGet.mockRejectedValueOnce(new Error("network"));

    await store.fetchList();

    expect(store.error).toBe("journal.annual.list.load.failure");
    expect(store.annualList).toEqual([]);
  });

  it("fetchTotal 은 rsltObj 를 총집계로 반영한다", async () => {
    const store = useJournalAnnualStore();
    mockedGet.mockResolvedValueOnce(ok({ rsltObj: { dreamDayCnt: 12, dreamCnt: 30 } }));

    await store.fetchTotal();

    expect(mockedGet).toHaveBeenCalledWith("/api/journal/annual/total");
    expect(store.totalAnnual?.dreamDayCnt).toBe(12);
  });
});

describe("makeTotalAnnual", () => {
  it("성공 시 목록·총집계를 재조회하고 true 를 반환한다", async () => {
    const store = useJournalAnnualStore();
    mockedPost.mockResolvedValueOnce(ok({ message: "처리됨" }));

    const result = await store.makeTotalAnnual();

    // body 없는 POST → apiPost(url)=axios.post(url, undefined) 이므로 URL 인자만 확인
    expect(mockedPost.mock.calls[0][0]).toBe("/api/journal/annual/make-total");
    expect(result).toBe(true);
    expect(mockedGet).toHaveBeenCalledWith("/api/journal/annuals");
    expect(mockedGet).toHaveBeenCalledWith("/api/journal/annual/total");
  });

  it("실패 응답은 swalAjaxResult 로 알리고 false 를 반환한다", async () => {
    const store = useJournalAnnualStore();
    mockedPost.mockResolvedValueOnce(fail("생성 실패"));

    const result = await store.makeTotalAnnual();

    expect(result).toBe(false);
    expect(swalAjaxResult).toHaveBeenCalledWith(expect.objectContaining({ rslt: false }));
  });
});

describe("결산 등록/수정", () => {
  it("openModify 는 결산을 조회해 폼을 채운다", async () => {
    const store = useJournalAnnualStore();
    mockedGet.mockResolvedValueOnce(
      ok({ rsltObj: { id: 9, yy: 2030, title: "제목", tag: { tagListStrWithCtgr: "#태그" } } }),
    );

    await store.openModify(2030);

    expect(mockedGet).toHaveBeenCalledWith("/api/journal/annual/2030");
    expect(store.registOpen).toBe(true);
    expect(store.registModel?.id).toBe(9);
    expect(store.registModel?.title).toBe("제목");
    expect(store.registModel?.tag?.tagListStrWithCtgr).toBe("#태그");
  });

  it("submitRegist 는 /api/journal/annual/{yy} 로 multipart POST 하고 목록을 재조회한다", async () => {
    const store = useJournalAnnualStore();
    await store.openRegist();
    if (store.registModel) {
      store.registModel.yy = 2030;
      store.registModel.title = "새 결산";
    }
    mockedPost.mockResolvedValueOnce(ok({ message: "등록됨" }));

    const result = await store.submitRegist();

    const [url, body, config] = mockedPost.mock.calls[0];
    expect(url).toBe("/api/journal/annual/2030");
    expect(body).toBeInstanceOf(FormData);
    expect(config?.headers?.["Content-Type"]).toBe("multipart/form-data");
    expect(result).toBe(true);
    expect(store.registOpen).toBe(false);
    expect(mockedGet).toHaveBeenCalledWith("/api/journal/annuals");
  });

  it("submitRegist 는 yy 가 없으면 요청하지 않는다", async () => {
    const store = useJournalAnnualStore();
    await store.openRegist();
    if (store.registModel) store.registModel.yy = undefined;

    const result = await store.submitRegist();

    expect(result).toBe(false);
    expect(mockedPost).not.toHaveBeenCalled();
  });
});

describe("상세/엔트리/태그", () => {
  it("fetchDetail 은 rsltObj 를 상세로 반영한다", async () => {
    const store = useJournalAnnualStore();
    mockedGet.mockResolvedValueOnce(ok({ rsltObj: { id: 5, yy: 2030, title: "상세" } }));

    await store.fetchDetail(2030);

    expect(mockedGet).toHaveBeenCalledWith("/api/journal/annual/2030");
    expect(store.annualDetail?.id).toBe(5);
  });

  it("fetchEntries(DIARY) 는 diaries 경로에 토글 파라미터를 싣고 diaryEntries 를 채운다", async () => {
    const store = useJournalAnnualStore();
    mockedGet.mockResolvedValueOnce(ok({ rsltList: [{ id: 11 }, { id: 12 }] }));

    await store.fetchEntries(2030, "DIARY");

    expect(mockedGet).toHaveBeenCalledWith("/api/journal/annual/2030/diaries", {
      params: { showImprtc: true, showRefrnc: false },
    });
    expect(store.diaryEntries.map((e) => e.id)).toEqual([11, 12]);
  });

  it("fetchEntries(DREAM) 는 dreams 경로를 사용한다", async () => {
    const store = useJournalAnnualStore();
    mockedGet.mockResolvedValueOnce(ok({ rsltList: [{ id: 21 }] }));

    await store.fetchEntries(2030, "DREAM");

    expect(mockedGet).toHaveBeenCalledWith("/api/journal/annual/2030/dreams", {
      params: { showImprtc: true, showRefrnc: false },
    });
    expect(store.dreamEntries.map((e) => e.id)).toEqual([21]);
  });

  it("fetchTagRows(DIARY) 는 DAY·DIARY 태그 행을 병렬 조회한다", async () => {
    const store = useJournalAnnualStore();
    mockedGet
      .mockResolvedValueOnce(ok({ rsltList: [{ tagId: 1, name: "day" }] })) // DAY
      .mockResolvedValueOnce(ok({ rsltList: [{ tagId: 2, name: "diary" }] })); // DIARY

    await store.fetchTagRows(2030, "DIARY");

    expect(mockedGet).toHaveBeenCalledWith("/api/journal/annual/2030/tags", { params: { type: "DAY" } });
    expect(mockedGet).toHaveBeenCalledWith("/api/journal/annual/2030/tags", { params: { type: "DIARY" } });
    expect(store.tagRows.DAY.map((t) => t.tagId)).toEqual([1]);
    expect(store.tagRows.DIARY.map((t) => t.tagId)).toEqual([2]);
  });

  it("fetchTagRows(DREAM) 는 DREAM 태그 행만 조회한다", async () => {
    const store = useJournalAnnualStore();
    mockedGet.mockResolvedValueOnce(ok({ rsltList: [{ tagId: 3, name: "dream" }] }));

    await store.fetchTagRows(2030, "DREAM");

    expect(mockedGet).toHaveBeenCalledWith("/api/journal/annual/2030/tags", { params: { type: "DREAM" } });
    expect(store.tagRows.DREAM.map((t) => t.tagId)).toEqual([3]);
  });
});

describe("결산 리뷰", () => {
  it("openReviewModify 는 리뷰를 조회해 폼을 채운다", async () => {
    const store = useJournalAnnualStore();
    mockedGet.mockResolvedValueOnce(
      ok({ rsltObj: { id: 7, journalAnnualId: 3, yy: 2030, categoryCode: "C1", content: "리뷰" } }),
    );

    await store.openReviewModify(7);

    expect(mockedGet).toHaveBeenCalledWith("/api/journal/annual/review/7");
    expect(store.reviewRegistModel?.id).toBe(7);
    expect(store.reviewRegistModel?.categoryCode).toBe("C1");
  });

  it("submitReviewRegist 신규는 reviews 로 POST 하고 상세를 재조회한다", async () => {
    const store = useJournalAnnualStore();
    await store.openReviewRegist(3, 2030);
    mockedPost.mockResolvedValueOnce(ok({ message: "등록됨" }));

    const result = await store.submitReviewRegist();

    expect(mockedPost.mock.calls[0][0]).toBe("/api/journal/annual/reviews");
    expect(result).toBe(true);
    // yy 가 있으면 상세 재조회
    expect(mockedGet).toHaveBeenCalledWith("/api/journal/annual/2030");
  });

  it("submitReviewRegist 수정은 review/{id} 로 POST 한다", async () => {
    const store = useJournalAnnualStore();
    mockedGet.mockResolvedValueOnce(
      ok({ rsltObj: { id: 8, journalAnnualId: 3, yy: 2030, content: "c" } }),
    );
    await store.openReviewModify(8);
    mockedPost.mockResolvedValueOnce(ok({ message: "수정됨" }));

    await store.submitReviewRegist();

    expect(mockedPost.mock.calls[0][0]).toBe("/api/journal/annual/review/8");
  });

  it("deleteReview 는 확인 후 삭제하고 상세를 재조회한다", async () => {
    const store = useJournalAnnualStore();
    mockedDelete.mockResolvedValueOnce(ok({ message: "삭제됨" }));

    await store.deleteReview(7, 2030);

    expect(mockedDelete).toHaveBeenCalledWith("/api/journal/annual/review/7");
    expect(mockedGet).toHaveBeenCalledWith("/api/journal/annual/2030");
  });

  it("deleteReview 는 확인 취소 시 삭제하지 않는다", async () => {
    const store = useJournalAnnualStore();
    swalConfirm.mockResolvedValueOnce(false);

    await store.deleteReview(7, 2030);

    expect(mockedDelete).not.toHaveBeenCalled();
  });
});
