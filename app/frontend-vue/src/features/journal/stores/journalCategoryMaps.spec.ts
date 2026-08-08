/**
 * journalCategoryMaps — 앱 세션 categoryMap SSOT 계약.
 * preload·ensure(미적재만 HTTP)·sync·applyFromSaveResponse·reset.
 */
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import axios from "axios";

vi.mock("axios", () => ({ default: { get: vi.fn() } }));

import {
  CATEGORY_MAP_URL_DAY_TAG,
  CATEGORY_MAP_URL_ENTRY_DIARY,
  useJournalCategoryMapStore,
} from "./journalCategoryMaps";

const mockedGet = vi.mocked(axios.get);

describe("journalCategoryMaps", () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockedGet.mockReset();
    vi.spyOn(console, "error").mockImplementation(() => undefined);
  });
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("ensure 는 미적재 시에만 HTTP 하고 두 번째 호출은 캐시를 쓴다", async () => {
    mockedGet.mockResolvedValue({
      data: { rslt: true, rsltMap: { alpha: ["A"] } },
    });
    const store = useJournalCategoryMapStore();

    await store.ensure(CATEGORY_MAP_URL_DAY_TAG);
    await store.ensure(CATEGORY_MAP_URL_DAY_TAG);

    expect(mockedGet).toHaveBeenCalledTimes(1);
    expect(mockedGet).toHaveBeenCalledWith(CATEGORY_MAP_URL_DAY_TAG);
    expect(store.dayTagCategoryMap).toEqual({ alpha: ["A"] });
  });

  it("preloadAll 은 4종 URL 을 조회한다", async () => {
    mockedGet.mockResolvedValue({ data: { rslt: true, rsltMap: {} } });
    const store = useJournalCategoryMapStore();

    await store.preloadAll();

    expect(mockedGet).toHaveBeenCalledTimes(4);
  });

  it("syncAll 은 캐시를 비운 뒤 다시 적재한다", async () => {
    mockedGet
      .mockResolvedValueOnce({ data: { rslt: true, rsltMap: { old: ["X"] } } })
      .mockResolvedValue({ data: { rslt: true, rsltMap: { neu: ["Y"] } } });
    const store = useJournalCategoryMapStore();

    await store.ensure(CATEGORY_MAP_URL_DAY_TAG);
    expect(store.dayTagCategoryMap).toEqual({ old: ["X"] });

    await store.syncAll();

    expect(store.dayTagCategoryMap).toEqual({ neu: ["Y"] });
    expect(mockedGet.mock.calls.length).toBeGreaterThan(1);
  });

  it("applyFromSaveResponse 는 rsltMap 으로 세션 map 을 교체한다", () => {
    const store = useJournalCategoryMapStore();
    store.applyFromSaveResponse(
      {
        dayTagCategoryMap: { day: ["D"] },
        entryTagCategoryMap: { entry: ["E"] },
      },
      "JOURNAL_DIARY",
    );

    expect(store.dayTagCategoryMap).toEqual({ day: ["D"] });
    expect(store.entryDiaryCategoryMap).toEqual({ entry: ["E"] });
  });

  it("reset 이후 ensure 는 다시 HTTP 한다", async () => {
    mockedGet.mockResolvedValue({ data: { rslt: true, rsltMap: {} } });
    const store = useJournalCategoryMapStore();

    await store.ensure(CATEGORY_MAP_URL_ENTRY_DIARY);
    store.reset();
    await store.ensure(CATEGORY_MAP_URL_ENTRY_DIARY);

    expect(mockedGet).toHaveBeenCalledTimes(2);
  });

  it("mapForEntryContentType 은 DIARY/DREAM 만 반환하고 NOTE 는 null", async () => {
    mockedGet.mockResolvedValue({
      data: { rslt: true, rsltMap: { t: ["C"] } },
    });
    const store = useJournalCategoryMapStore();
    await store.ensure(CATEGORY_MAP_URL_ENTRY_DIARY);

    expect(store.mapForEntryContentType("JOURNAL_DIARY")).toEqual({ t: ["C"] });
    expect(store.mapForEntryContentType("JOURNAL_NOTE")).toBeNull();
    expect(store.mapForEntryContentType("JOURNAL_DREAM")).toEqual({});
  });
});