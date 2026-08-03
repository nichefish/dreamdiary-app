import { beforeEach, describe, expect, it, vi } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import axios from "axios";
import { useUserPrefixesStore } from "./userPrefixes";
import { usePersonalPrefixOptionsStore } from "@/features/attachable/stores/personalPrefixOptions";

vi.mock("axios", () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
  },
}));

describe("userPrefixes store 개인 목록 분리", () => {
  const mockedGet = vi.mocked(axios.get);
  const mockedPost = vi.mocked(axios.post);
  const mockedPut = vi.mocked(axios.put);
  const mockedPatch = vi.mocked(axios.patch);

  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  it("선택한 콘텐츠 타입으로 서로 다른 개인 Prefix 목록을 조회한다", async () => {
    mockedGet.mockResolvedValue({
      data: { rsltList: [{ id: 11, name: "가상 말머리", sortOrder: 0, activeYn: "Y" }] },
    });
    const store = useUserPrefixesStore();

    await store.fetchPrefixes("JOURNAL_CHAPTER_DIARY");

    expect(mockedGet).toHaveBeenCalledWith("/api/my/prefixes", {
      params: { contentType: "JOURNAL_CHAPTER_DIARY" },
    });
    expect(store.prefixes).toEqual([
      { id: 11, name: "가상 말머리", sortOrder: 0, activeYn: "Y" },
    ]);
  });

  it("늦게 끝난 이전 대상 응답이 현재 목록을 덮어쓰지 못한다", async () => {
    let resolveThread: (value: unknown) => void = () => undefined;
    let resolveDream: (value: unknown) => void = () => undefined;
    mockedGet
      .mockImplementationOnce(() => new Promise((resolve) => { resolveThread = resolve; }))
      .mockImplementationOnce(() => new Promise((resolve) => { resolveDream = resolve; }));
    const store = useUserPrefixesStore();

    const threadRequest = store.fetchPrefixes("JOURNAL_THREAD");
    const dreamRequest = store.fetchPrefixes("JOURNAL_DREAM");
    resolveDream({ data: { rsltList: [{ id: 22, name: "꿈 말머리", sortOrder: 0, activeYn: "Y" }] } });
    await dreamRequest;
    resolveThread({ data: { rsltList: [{ id: 11, name: "스레드 말머리", sortOrder: 0, activeYn: "Y" }] } });
    await threadRequest;

    expect(store.prefixes).toEqual([
      { id: 22, name: "꿈 말머리", sortOrder: 0, activeYn: "Y" },
    ]);
    expect(store.loading).toBe(false);
  });

  it("관리 모달을 닫은 뒤 도착한 응답은 폐기한다", async () => {
    let resolveRequest: (value: unknown) => void = () => undefined;
    mockedGet.mockImplementationOnce(() => new Promise((resolve) => { resolveRequest = resolve; }));
    const store = useUserPrefixesStore();

    const request = store.fetchPrefixes("JOURNAL_THREAD");
    store.clearPrefixes();
    resolveRequest({ data: { rsltList: [{ id: 11, name: "가상 말머리", sortOrder: 0, activeYn: "Y" }] } });
    await request;

    expect(store.prefixes).toEqual([]);
    expect(store.loading).toBe(false);
  });

  it("등록과 활성 상태 변경도 현재 콘텐츠 타입을 유지해 재조회한다", async () => {
    mockedPost.mockResolvedValue({ data: { rsltObj: { id: 21 } } });
    mockedPatch.mockResolvedValue({ data: { rslt: true } });
    mockedGet.mockResolvedValue({ data: { rsltList: [] } });
    const store = useUserPrefixesStore();

    await store.savePrefix("JOURNAL_NOTE", {
      name: "가상 말머리",
      color: "#009EF7",
      sortOrder: 0,
    });
    await store.setPrefixActive("JOURNAL_NOTE", 21, false);

    expect(mockedPost).toHaveBeenCalledWith("/api/my/prefixes", {
      name: "가상 말머리",
      color: "#009EF7",
      sortOrder: 0,
    }, {
      params: { contentType: "JOURNAL_NOTE" },
    });
    expect(mockedPatch).toHaveBeenCalledWith(
      "/api/my/prefixes/21/active",
      null,
      { params: { contentType: "JOURNAL_NOTE", active: false } },
    );
    expect(mockedGet).toHaveBeenNthCalledWith(1, "/api/my/prefixes", {
      params: { contentType: "JOURNAL_NOTE" },
    });
    expect(mockedGet).toHaveBeenNthCalledWith(2, "/api/my/prefixes", {
      params: { contentType: "JOURNAL_NOTE" },
    });
  });

  it("관리 수정 성공 시 해당 콘텐츠 타입의 소비자 선택지 캐시만 무효화한다", async () => {
    mockedPut.mockResolvedValue({ data: { rsltObj: { id: 31 } } });
    mockedGet
      .mockResolvedValueOnce({ data: { rsltList: [{ id: 31, name: "기존 말머리", activeYn: "Y" }] } })
      .mockResolvedValueOnce({ data: { rsltList: [{ id: 31, name: "수정된 말머리", activeYn: "Y" }] } })
      .mockResolvedValueOnce({ data: { rsltList: [{ id: 31, name: "수정된 말머리", activeYn: "Y" }] } });
    const optionStore = usePersonalPrefixOptionsStore();
    const managementStore = useUserPrefixesStore();

    await optionStore.fetchOptions("JOURNAL_THREAD");
    await managementStore.savePrefix("JOURNAL_THREAD", {
      id: 31,
      name: "수정된 말머리",
      color: "#009EF7",
      sortOrder: 0,
    });

    expect(optionStore.optionsFor("JOURNAL_THREAD")).toEqual([]);
    await optionStore.fetchOptions("JOURNAL_THREAD");

    expect(mockedGet).toHaveBeenNthCalledWith(3, "/api/my/prefixes/options", {
      params: { contentType: "JOURNAL_THREAD" },
    });
    expect(optionStore.optionsFor("JOURNAL_THREAD")).toEqual([
      { id: 31, name: "수정된 말머리", activeYn: "Y" },
    ]);
  });
});
