import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import axios from "axios";
import { usePersonalPrefixOptionsStore } from "./personalPrefixOptions";

vi.mock("axios", () => ({ default: { get: vi.fn() } }));

const FIXTURE_THREAD_CONTENT_TYPE = "JOURNAL_THREAD";
const FIXTURE_NOTE_CONTENT_TYPE = "JOURNAL_NOTE";
const mockedGet = vi.mocked(axios.get);

describe("personalPrefixOptions store 콘텐츠 타입 캐시", () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockedGet.mockReset();
    vi.spyOn(console, "info").mockImplementation(() => undefined);
    vi.spyOn(console, "error").mockImplementation(() => undefined);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("정상 빈 목록과 동시 요청을 콘텐츠 타입별로 캐시한다", async () => {
    mockedGet.mockResolvedValue({ data: { rsltList: [] } });
    const store = usePersonalPrefixOptionsStore();

    await Promise.all([
      store.fetchOptions(FIXTURE_THREAD_CONTENT_TYPE),
      store.fetchOptions(FIXTURE_THREAD_CONTENT_TYPE),
    ]);
    await store.fetchOptions(FIXTURE_THREAD_CONTENT_TYPE);

    expect(mockedGet).toHaveBeenCalledTimes(1);
    expect(store.optionsFor(FIXTURE_THREAD_CONTENT_TYPE)).toEqual([]);
    expect(store.hasFailed(FIXTURE_THREAD_CONTENT_TYPE)).toBe(false);
  });

  it("관리 변경 콘텐츠 타입만 무효화하고 다음 소비 시 최신 목록을 조회한다", async () => {
    mockedGet
      .mockResolvedValueOnce({ data: { rsltList: [{ id: 11, name: "기존 말머리" }] } })
      .mockResolvedValueOnce({ data: { rsltList: [{ id: 12, name: "수정된 말머리" }] } });
    const store = usePersonalPrefixOptionsStore();

    await store.fetchOptions(FIXTURE_THREAD_CONTENT_TYPE);
    store.invalidate(FIXTURE_THREAD_CONTENT_TYPE);

    expect(store.optionsFor(FIXTURE_THREAD_CONTENT_TYPE)).toEqual([]);
    await store.fetchOptions(FIXTURE_THREAD_CONTENT_TYPE);

    expect(mockedGet).toHaveBeenCalledTimes(2);
    expect(store.optionsFor(FIXTURE_THREAD_CONTENT_TYPE)).toEqual([
      { id: 12, name: "수정된 말머리" },
    ]);
  });

  it("무효화 전에 시작한 늦은 응답이 새 콘텐츠 타입 상태를 덮어쓰지 못한다", async () => {
    let resolveOldRequest: (value: unknown) => void = () => undefined;
    mockedGet
      .mockImplementationOnce(() => new Promise((resolve) => { resolveOldRequest = resolve; }))
      .mockResolvedValueOnce({ data: { rsltList: [{ id: 22, name: "최신 말머리" }] } });
    const store = usePersonalPrefixOptionsStore();

    const oldRequest = store.fetchOptions(FIXTURE_NOTE_CONTENT_TYPE);
    store.invalidate(FIXTURE_NOTE_CONTENT_TYPE);
    await store.fetchOptions(FIXTURE_NOTE_CONTENT_TYPE);
    resolveOldRequest({ data: { rsltList: [{ id: 21, name: "이전 말머리" }] } });
    await oldRequest;

    expect(store.optionsFor(FIXTURE_NOTE_CONTENT_TYPE)).toEqual([
      { id: 22, name: "최신 말머리" },
    ]);
  });
});
