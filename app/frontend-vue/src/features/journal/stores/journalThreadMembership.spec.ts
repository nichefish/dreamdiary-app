import { beforeEach, describe, expect, it, vi } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import axios from "axios";
import { useJournalThreadMembershipStore } from "./journalThreadMembership";

vi.mock("axios", () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    delete: vi.fn(),
  },
}));

vi.mock("@/shared/i18n/stores/locale", () => ({
  useLocaleStore: () => ({
    locale: "ko",
    t: (key: string) => key,
  }),
}));

vi.mock("@/shared/utils/swal", () => ({
  swalAjaxResult: vi.fn(),
  swalRequestError: vi.fn(),
}));

const FIXTURE_ENTRY_ID = 101;
const FIXTURE_OTHER_ENTRY_ID = 202;
const FIXTURE_THREAD_ID = 301;
const FIXTURE_OTHER_THREAD_ID = 302;

describe("journalThreadMembership store", () => {
  const mockedGet = vi.mocked(axios.get);

  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  it("스레드용 말머리 선택지를 JOURNAL_THREAD 콘텐츠 타입으로 조회한다", async () => {
    mockedGet
      .mockResolvedValueOnce({ data: { rsltObj: { content: [] } } })
      .mockResolvedValueOnce({
        data: {
          rsltList: [{ id: 11, name: "이슈", activeYn: "Y" }],
        },
      });
    const store = useJournalThreadMembershipStore();

    await store.openThreadOptions(FIXTURE_ENTRY_ID);

    expect(mockedGet).toHaveBeenCalledWith("/api/my/prefixes/options", {
      params: { contentType: "JOURNAL_THREAD" },
    });
    expect(store.prefixOptions).toEqual([
      { id: 11, name: "이슈", activeYn: "Y" },
    ]);
    expect(store.prefixError).toBe("");
  });

  it("현재 엔트리와 검색·말머리 조건으로 전용 후보 API를 조회한다", async () => {
    mockedGet.mockResolvedValueOnce({
      data: {
        rsltList: [{
          id: FIXTURE_THREAD_ID,
          title: "프로젝트 회고",
          prefix: { id: 11, name: "이슈", activeYn: "Y" },
          membershipCount: 3,
          member: true,
        }],
      },
    });
    const store = useJournalThreadMembershipStore();
    store.optionKeyword = "회고";
    store.optionPrefix = "11";

    const result = await store.fetchThreadOptions(FIXTURE_ENTRY_ID);

    expect(result).toBe(true);
    expect(mockedGet).toHaveBeenCalledWith("/api/journal/threads/candidates", {
      params: {
        entryId: FIXTURE_ENTRY_ID,
        limit: 7,
        keyword: "회고",
        prefixId: "11",
      },
    });
    expect(store.threadOptions).toEqual([
      expect.objectContaining({
        id: FIXTURE_THREAD_ID,
        member: true,
      }),
    ]);
    expect(store.optionsError).toBe("");
  });

  it("완료 포함이 켜지면 includeResolved=true 로 후보 API를 조회한다", async () => {
    mockedGet.mockResolvedValueOnce({ data: { rsltList: [] } });
    const store = useJournalThreadMembershipStore();
    store.optionIncludeResolved = true;

    await store.fetchThreadOptions(FIXTURE_ENTRY_ID);

    expect(mockedGet).toHaveBeenCalledWith("/api/journal/threads/candidates", {
      params: {
        entryId: FIXTURE_ENTRY_ID,
        limit: 7,
        includeResolved: true,
      },
    });
  });

  it("늦게 끝난 이전 엔트리 응답이 현재 후보를 덮어쓰지 못한다", async () => {
    let resolveFirst!: (value: unknown) => void;
    let resolveSecond!: (value: unknown) => void;
    const firstResponse = new Promise((resolve) => {
      resolveFirst = resolve;
    });
    const secondResponse = new Promise((resolve) => {
      resolveSecond = resolve;
    });
    mockedGet
      .mockImplementationOnce(() => firstResponse as ReturnType<typeof axios.get>)
      .mockImplementationOnce(() => secondResponse as ReturnType<typeof axios.get>);
    const store = useJournalThreadMembershipStore();

    const firstRequest = store.fetchThreadOptions(FIXTURE_ENTRY_ID);
    const secondRequest = store.fetchThreadOptions(FIXTURE_OTHER_ENTRY_ID);
    resolveSecond({
      data: {
        rsltList: [{
          id: FIXTURE_OTHER_THREAD_ID,
          title: "두 번째 스레드",
          membershipCount: 1,
          member: false,
        }],
      },
    });
    await secondRequest;
    resolveFirst({
      data: {
        rsltList: [{
          id: FIXTURE_THREAD_ID,
          title: "첫 번째 스레드",
          membershipCount: 2,
          member: true,
        }],
      },
    });
    await firstRequest;

    expect(store.candidateEntryId).toBe(FIXTURE_OTHER_ENTRY_ID);
    expect(store.threadOptions.map((option) => option.id)).toEqual([FIXTURE_OTHER_THREAD_ID]);
    expect(store.optionsLoading).toBe(false);
  });

  it("같은 엔트리의 재조회 실패는 직전 성공 후보와 오류 상태를 함께 보존한다", async () => {
    mockedGet
      .mockResolvedValueOnce({
        data: {
          rsltList: [{
            id: FIXTURE_THREAD_ID,
            title: "보존할 스레드",
            membershipCount: 1,
            member: false,
          }],
        },
      })
      .mockRejectedValueOnce(new Error("network failure"));
    const store = useJournalThreadMembershipStore();
    await store.fetchThreadOptions(FIXTURE_ENTRY_ID);

    const result = await store.fetchThreadOptions(FIXTURE_ENTRY_ID);

    expect(result).toBe(false);
    expect(store.threadOptions.map((option) => option.id)).toEqual([FIXTURE_THREAD_ID]);
    expect(store.optionsError).toBe("journal.entry.thread.candidates.load.failure");
  });
});
