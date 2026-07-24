import { beforeEach, describe, expect, it, vi } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import axios from "axios";
import { useJournalThreadStore } from "./journalThread";

vi.mock("axios", () => ({
  default: {
    get: vi.fn(),
  },
}));

vi.mock("@/shared/i18n/stores/locale", () => ({
  useLocaleStore: () => ({
    t: (key: string) => key,
  }),
}));

vi.mock("@/shared/auth/sessionPing", () => ({
  assertAuthenticatedBeforeModal: vi.fn().mockResolvedValue(true),
}));

vi.mock("@/shared/utils/swal", () => ({
  swalConfirm: vi.fn(),
  swalAlert: vi.fn(),
  swalRequestError: vi.fn(),
  swalAjaxResult: vi.fn(),
}));

const FIXTURE_THREAD_ID = 301;
const FIXTURE_SECOND_THREAD_ID = 302;
const FIXTURE_ENTRY_ID = 101;

describe("journalThread store 열린 상세 갱신", () => {
  const mockedGet = vi.mocked(axios.get);

  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  it("본문·집계 태그와 소속 엔트리를 같은 갱신에서 교체한다", async () => {
    const store = useJournalThreadStore();
    store.detailOpen = true;
    store.detailModel = { id: FIXTURE_THREAD_ID, title: "기존 스레드" };
    store.detailEntries = [{ id: FIXTURE_ENTRY_ID, title: "기존 엔트리" }];
    mockedGet
      .mockResolvedValueOnce({
        data: {
          rsltObj: {
            id: FIXTURE_THREAD_ID,
            title: "갱신된 스레드",
            tag: { list: [{ tagId: 1, name: "회고" }] },
          },
        },
      })
      .mockResolvedValueOnce({
        data: {
          rsltList: [{ id: FIXTURE_ENTRY_ID, title: "갱신된 엔트리" }],
        },
      });

    const refreshed = await store.refreshOpenDetail();

    expect(refreshed).toBe(true);
    expect(mockedGet).toHaveBeenNthCalledWith(1, `/api/journal/threads/${FIXTURE_THREAD_ID}`);
    expect(mockedGet).toHaveBeenNthCalledWith(2, `/api/journal/threads/${FIXTURE_THREAD_ID}/entries`);
    expect(store.detailModel?.title).toBe("갱신된 스레드");
    expect(store.detailModel?.tag?.list?.[0]?.name).toBe("회고");
    expect(store.detailEntries[0]?.title).toBe("갱신된 엔트리");
  });

  it("재조회가 실패하면 읽고 있던 상세 데이터를 비우지 않는다", async () => {
    const store = useJournalThreadStore();
    store.detailOpen = true;
    store.detailModel = { id: FIXTURE_THREAD_ID, title: "보존할 스레드" };
    store.detailEntries = [{ id: FIXTURE_ENTRY_ID, title: "보존할 엔트리" }];
    mockedGet
      .mockResolvedValueOnce({
        data: {
          rsltObj: { id: FIXTURE_THREAD_ID, title: "적용되면 안 되는 스레드" },
        },
      })
      .mockRejectedValueOnce(new Error("network failure"));

    const refreshed = await store.refreshOpenDetail();

    expect(refreshed).toBe(false);
    expect(store.detailModel?.title).toBe("보존할 스레드");
    expect(store.detailEntries[0]?.title).toBe("보존할 엔트리");
  });
});

describe("journalThread store 기간별 스레드 요약", () => {
  const mockedGet = vi.mocked(axios.get);

  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  it("주간 조회 계약과 서버 정렬 결과를 그대로 보존한다", async () => {
    mockedGet.mockResolvedValue({
      data: {
        rsltList: [
          {
            threadId: FIXTURE_THREAD_ID,
            title: "첫 번째 흐름",
            entryCount: 2,
            firstEntryDate: "2026-07-01",
          },
          {
            threadId: FIXTURE_SECOND_THREAD_ID,
            title: "두 번째 흐름",
            entryCount: 1,
            firstEntryDate: "2026-07-03",
          },
        ],
      },
    });
    const store = useJournalThreadStore();

    await store.fetchPeriodSummary({ viewType: "WEEKLY", weekStartDt: "2026-06-29" });

    expect(mockedGet).toHaveBeenCalledWith("/api/journal/threads/period-summary", {
      params: { viewType: "WEEKLY", weekStartDt: "2026-06-29" },
    });
    expect(store.periodSummary.map((item) => item.threadId)).toEqual([
      FIXTURE_THREAD_ID,
      FIXTURE_SECOND_THREAD_ID,
    ]);
    expect(store.periodSummaryError).toBe("");
  });

  it("기간 전환 중 늦게 끝난 이전 응답은 폐기한다", async () => {
    let resolveFirst!: (value: {
      data: { rsltList: Array<{ threadId: number; title: string; entryCount: number; firstEntryDate: string }> };
    }) => void;
    const firstRequest = new Promise<{
      data: { rsltList: Array<{ threadId: number; title: string; entryCount: number; firstEntryDate: string }> };
    }>((resolve) => {
      resolveFirst = resolve;
    });
    mockedGet
      .mockReturnValueOnce(firstRequest)
      .mockResolvedValueOnce({
        data: {
          rsltList: [{
            threadId: FIXTURE_SECOND_THREAD_ID,
            title: "최신 기간 흐름",
            entryCount: 3,
            firstEntryDate: "2026-08-02",
          }],
        },
      });
    const store = useJournalThreadStore();

    const first = store.fetchPeriodSummary({ viewType: "LIST", yy: 2026, mnth: 7 });
    await store.fetchPeriodSummary({ viewType: "LIST", yy: 2026, mnth: 8 });
    resolveFirst({
      data: {
        rsltList: [{
          threadId: FIXTURE_THREAD_ID,
          title: "이전 기간 흐름",
          entryCount: 1,
          firstEntryDate: "2026-07-01",
        }],
      },
    });
    await first;

    expect(store.periodSummary).toHaveLength(1);
    expect(store.periodSummary[0]?.threadId).toBe(FIXTURE_SECOND_THREAD_ID);
    expect(store.periodSummaryLoading).toBe(false);
  });

  it("조회 실패를 빈 결과와 구분해 노출한다", async () => {
    mockedGet.mockRejectedValue(new Error("network failure"));
    const store = useJournalThreadStore();

    await store.fetchPeriodSummary({ viewType: "LIST", yy: 2026, mnth: 7 });

    expect(store.periodSummary).toEqual([]);
    expect(store.periodSummaryError).toBe("journal.thread.period-summary.load.failure");
    expect(store.periodSummaryLoading).toBe(false);
  });
});
