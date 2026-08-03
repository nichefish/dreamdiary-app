/**
 * journalModal store — 사용자별 챕터(유형별)·엔트리 Prefix 선택지 캐시 계약.
 * 챕터 말머리는 일기 챕터(JOURNAL_CHAPTER_DIARY)와 노트 챕터(JOURNAL_CHAPTER_NOTE)로 분리해 캐시한다.
 * 정상 빈 목록은 완료로 캐시하고, 실패는 다음 호출에서 재시도하며, 로그아웃 초기화 뒤에는 다시 조회한다.
 */
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import axios from "axios";

vi.mock("axios", () => ({ default: { get: vi.fn() } }));
vi.mock("@/shared/auth/sessionPing", () => ({
  assertAuthenticatedBeforeModal: vi.fn().mockResolvedValue(true),
}));
vi.mock("@/shared/i18n/stores/locale", () => ({
  useLocaleStore: () => ({ t: (key: string) => key }),
}));
vi.mock("@/shared/utils/swal", () => ({
  swalRequestError: vi.fn(),
}));

import { useJournalModalStore } from "./journalModal";

const mockedGet = vi.mocked(axios.get);

describe("챕터 유형별 Prefix 선택지 캐시", () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockedGet.mockReset();
    vi.spyOn(console, "error").mockImplementation(() => undefined);
  });
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("일기 챕터 정상 빈 목록도 완료로 캐시해 중복 조회하지 않는다", async () => {
    mockedGet.mockResolvedValue({ data: { rsltList: [] } });
    const store = useJournalModalStore();

    await store.prefetchChapterPrefixes("DIARY");
    await store.prefetchChapterPrefixes("DIARY");

    expect(mockedGet).toHaveBeenCalledTimes(1);
    expect(mockedGet).toHaveBeenCalledWith("/api/my/prefixes/options", {
      params: { contentType: "JOURNAL_CHAPTER_DIARY" },
    });
    expect(store.chapterPrefixOptionsFor("DIARY")).toEqual([]);
    expect(store.chapterPrefixLoadFailedFor("DIARY")).toBe(false);
  });

  it("일기 챕터 조회 실패는 완료로 캐시하지 않아 다음 호출에서 재시도한다", async () => {
    mockedGet
      .mockRejectedValueOnce(new Error("request failed"))
      .mockResolvedValueOnce({ data: { rsltList: [{ id: 11, name: "회고", activeYn: "Y" }] } });
    const store = useJournalModalStore();

    await store.prefetchChapterPrefixes("DIARY");
    expect(store.chapterPrefixLoadFailedFor("DIARY")).toBe(true);

    await store.prefetchChapterPrefixes("DIARY");

    expect(mockedGet).toHaveBeenCalledTimes(2);
    expect(store.chapterPrefixLoadFailedFor("DIARY")).toBe(false);
    expect(store.chapterPrefixOptionsFor("DIARY")).toEqual([{ id: 11, name: "회고", activeYn: "Y" }]);
  });

  it("일기 챕터와 노트 챕터 목록을 유형별로 분리해 캐시한다", async () => {
    mockedGet
      .mockResolvedValueOnce({ data: { rsltList: [{ id: 11, name: "회고", activeYn: "Y" }] } })
      .mockResolvedValueOnce({ data: { rsltList: [{ id: 21, name: "관계", activeYn: "Y" }] } });
    const store = useJournalModalStore();

    await store.prefetchChapterPrefixes("DIARY");
    await store.prefetchChapterPrefixes("NOTE");

    expect(mockedGet).toHaveBeenCalledTimes(2);
    expect(mockedGet).toHaveBeenNthCalledWith(1, "/api/my/prefixes/options", {
      params: { contentType: "JOURNAL_CHAPTER_DIARY" },
    });
    expect(mockedGet).toHaveBeenNthCalledWith(2, "/api/my/prefixes/options", {
      params: { contentType: "JOURNAL_CHAPTER_NOTE" },
    });
    expect(store.chapterPrefixOptionsFor("DIARY")).toEqual([{ id: 11, name: "회고", activeYn: "Y" }]);
    expect(store.chapterPrefixOptionsFor("NOTE")).toEqual([{ id: 21, name: "관계", activeYn: "Y" }]);
  });

  it("인자 없는 prefetch는 일기·노트 목록을 함께 워밍한다", async () => {
    mockedGet.mockResolvedValue({ data: { rsltList: [] } });
    const store = useJournalModalStore();

    await store.prefetchChapterPrefixes();

    expect(mockedGet).toHaveBeenCalledTimes(2);
    const requested = mockedGet.mock.calls.map(
      (call) => (call[1] as { params?: { contentType?: string } } | undefined)?.params?.contentType,
    );
    expect(requested).toContain("JOURNAL_CHAPTER_DIARY");
    expect(requested).toContain("JOURNAL_CHAPTER_NOTE");
  });

  it("사용자 말머리가 없는 DREAM 챕터는 조회 없이 빈 목록을 반환한다", async () => {
    const store = useJournalModalStore();

    await store.prefetchChapterPrefixes("DREAM");

    expect(mockedGet).not.toHaveBeenCalled();
    expect(store.chapterPrefixOptionsFor("DREAM")).toEqual([]);
    expect(store.chapterPrefixLoadFailedFor("DREAM")).toBe(false);
  });

  it("세션 캐시 초기화 뒤에는 현재 사용자의 목록을 다시 조회한다", async () => {
    mockedGet
      .mockResolvedValueOnce({ data: { rsltList: [{ id: 11, name: "회고" }] } })
      .mockResolvedValueOnce({ data: { rsltList: [{ id: 21, name: "관계" }] } });
    const store = useJournalModalStore();

    await store.prefetchChapterPrefixes("DIARY");
    store.resetCategoryMaps();
    await store.prefetchChapterPrefixes("DIARY");

    expect(mockedGet).toHaveBeenCalledTimes(2);
    expect(store.chapterPrefixOptionsFor("DIARY")).toEqual([{ id: 21, name: "관계" }]);
  });
});

describe("저널 엔트리 Prefix 선택지 캐시", () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockedGet.mockReset();
    vi.spyOn(console, "error").mockImplementation(() => undefined);
  });
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("DIARY와 NOTE 목록을 contentType별로 분리해 캐시한다", async () => {
    mockedGet
      .mockResolvedValueOnce({ data: { rsltList: [{ id: 31, name: "회고", activeYn: "Y" }] } })
      .mockResolvedValueOnce({ data: { rsltList: [{ id: 41, name: "발췌", activeYn: "Y" }] } });
    const store = useJournalModalStore();

    await store.prefetchEntryPrefixes("JOURNAL_DIARY");
    await store.prefetchEntryPrefixes("JOURNAL_DIARY");
    await store.prefetchEntryPrefixes("JOURNAL_NOTE");

    expect(mockedGet).toHaveBeenCalledTimes(2);
    expect(store.entryPrefixOptionsFor("JOURNAL_DIARY")).toEqual([
      { id: 31, name: "회고", activeYn: "Y" },
    ]);
    expect(store.entryPrefixOptionsFor("JOURNAL_NOTE")).toEqual([
      { id: 41, name: "발췌", activeYn: "Y" },
    ]);
  });

  it("유형별 조회 실패는 다음 호출에서 재시도한다", async () => {
    mockedGet
      .mockRejectedValueOnce(new Error("request failed"))
      .mockResolvedValueOnce({ data: { rsltList: [{ id: 51, name: "상징", activeYn: "Y" }] } });
    const store = useJournalModalStore();

    await store.prefetchEntryPrefixes("JOURNAL_DREAM");
    expect(store.entryPrefixLoadFailedFor("JOURNAL_DREAM")).toBe(true);

    await store.prefetchEntryPrefixes("JOURNAL_DREAM");

    expect(mockedGet).toHaveBeenCalledTimes(2);
    expect(store.entryPrefixLoadFailedFor("JOURNAL_DREAM")).toBe(false);
    expect(store.entryPrefixOptionsFor("JOURNAL_DREAM")).toEqual([
      { id: 51, name: "상징", activeYn: "Y" },
    ]);
  });
});
