import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import axios from "axios";
import { useAdminPageStore } from "./adminPage";

vi.mock("axios", () => ({
  default: { get: vi.fn(), post: vi.fn(), put: vi.fn(), patch: vi.fn() },
}));

vi.mock("@/shared/i18n/stores/locale", () => ({
  useLocaleStore: () => ({ t: (key: string) => key }),
}));

const mockedGet = vi.mocked(axios.get);
const mockedPost = vi.mocked(axios.post);
const mockedPut = vi.mocked(axios.put);
const mockedPatch = vi.mocked(axios.patch);

/** 성공 AjaxResponse 형태의 axios 응답. 공유 클라이언트가 `.data` 를 언랩한다. */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
function ok(payload: Record<string, unknown> = {}): any {
  return { data: { rslt: true, ...payload } };
}

/** 실패 AjaxResponse. */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
function fail(message = "실패"): any {
  return { data: { rslt: false, message } };
}

beforeEach(() => {
  setActivePinia(createPinia());
  vi.clearAllMocks();
  // 기본: 모든 요청 성공(빈 페이로드)
  mockedGet.mockResolvedValue(ok());
  mockedPost.mockResolvedValue(ok());
  mockedPut.mockResolvedValue(ok());
  mockedPatch.mockResolvedValue(ok());
});

afterEach(() => {
  // 백필 폴링 타이머 누수 방지(활성 통계가 들어온 케이스 대비)
  vi.clearAllTimers();
});

describe("fetchBootstrap (lenient)", () => {
  it("meta 와 roleList 를 상태에 반영한다", async () => {
    const store = useAdminPageStore();
    mockedGet.mockResolvedValueOnce(
      ok({ rsltObj: { meta: { currYy: "2030" }, roleList: [{ roleKey: "ADMIN", roleName: "관리자" }] } }),
    );

    await store.fetchBootstrap();

    expect(mockedGet).toHaveBeenCalledWith("/api/admin/page/bootstrap");
    expect(store.meta.currYy).toBe("2030");
    expect(store.roles.map((r) => r.roleKey)).toEqual(["ADMIN"]);
    expect(store.bootstrapLoading).toBe(false);
  });
});

describe("임베딩/엔티티 통계 (lenient, error 매핑)", () => {
  it("fetchOllamaHealth 실패는 ollamaHealthError 에 담고 throw 하지 않는다", async () => {
    const store = useAdminPageStore();
    mockedGet.mockResolvedValueOnce(fail("Ollama down"));

    await store.fetchOllamaHealth();

    expect(mockedGet).toHaveBeenCalledWith("/api/admin/ollama/health");
    expect(store.ollamaHealthError).toBe("Ollama down");
  });

  it("fetchEmbeddingStats 는 통계와 헬스를 병렬 조회한다", async () => {
    const store = useAdminPageStore();
    // 두 get(stats, health) 모두 기본 ok 로 해소
    await store.fetchEmbeddingStats();

    expect(mockedGet).toHaveBeenCalledWith("/api/admin/journal-entry-embeddings/stats");
    expect(mockedGet).toHaveBeenCalledWith("/api/admin/ollama/health");
    expect(store.embeddingStatsError).toBe("");
    expect(store.embeddingStatsLoading).toBe(false);
  });

  it("fetchEmbeddingStats 실패는 embeddingStatsError 에 담는다", async () => {
    const store = useAdminPageStore();
    mockedGet
      .mockResolvedValueOnce(fail("stats 실패")) // stats
      .mockResolvedValueOnce(ok()); // health

    await store.fetchEmbeddingStats();

    expect(store.embeddingStatsError).toBe("stats 실패");
  });

  it("fetchEntityQueueStats 실패는 entityQueueError 에 담는다", async () => {
    const store = useAdminPageStore();
    mockedGet.mockResolvedValueOnce(fail("entity 실패"));

    await store.fetchEntityQueueStats();

    expect(mockedGet).toHaveBeenCalledWith("/api/admin/journal-entry-entities/stats");
    expect(store.entityQueueError).toBe("entity 실패");
  });
});

describe("임베딩/엔티티 동작 (POST)", () => {
  it("syncEmbeddingQueue 는 AI 비활성 시 요청하지 않는다", async () => {
    const store = useAdminPageStore();
    store.journalSettingAiEnabled = false;

    await store.syncEmbeddingQueue();

    expect(mockedPost).not.toHaveBeenCalled();
  });

  it("syncEmbeddingQueue 는 sync 후 통계를 재조회한다", async () => {
    const store = useAdminPageStore();
    store.journalSettingAiEnabled = true;

    await store.syncEmbeddingQueue();

    expect(mockedPost).toHaveBeenCalledWith("/api/admin/journal-entry-embeddings/sync");
    // 성공 후 fetchEmbeddingStats → stats get 발생
    expect(mockedGet).toHaveBeenCalledWith("/api/admin/journal-entry-embeddings/stats");
    expect(store.embeddingSyncRunning).toBe(false);
  });

  it("syncEntityQueue 는 sync 후 통계를 재조회한다", async () => {
    const store = useAdminPageStore();

    await store.syncEntityQueue();

    expect(mockedPost).toHaveBeenCalledWith("/api/admin/journal-entry-entities/sync");
    expect(mockedGet).toHaveBeenCalledWith("/api/admin/journal-entry-entities/stats");
    expect(store.entityQueueSyncRunning).toBe(false);
  });

  it("requeueFailedEntityQueue 실패는 entityQueueError 에 담는다", async () => {
    const store = useAdminPageStore();
    mockedPost.mockResolvedValueOnce(fail("requeue 실패"));

    await store.requeueFailedEntityQueue();

    expect(mockedPost).toHaveBeenCalledWith("/api/admin/journal-entry-entities/requeue-failed");
    expect(store.entityQueueError).toBe("requeue 실패");
  });
});

describe("공휴일/Notion (throw 전파)", () => {
  it("syncHolyday 는 FormData(yy) 로 POST 하고 메시지를 반환한다", async () => {
    const store = useAdminPageStore();
    mockedPost.mockResolvedValueOnce(ok({ message: "처리됨" }));

    const message = await store.syncHolyday("2030");

    const [url, body] = mockedPost.mock.calls[0];
    expect(url).toBe("/api/holyday/get-holyday-account.do");
    expect(body).toBeInstanceOf(FormData);
    expect((body as FormData).get("yy")).toBe("2030");
    expect(message).toBe("처리됨");
  });

  it("syncHolyday 실패는 throw 한다", async () => {
    const store = useAdminPageStore();
    mockedPost.mockResolvedValueOnce(fail("공휴일 실패"));

    await expect(store.syncHolyday("2030")).rejects.toThrow("공휴일 실패");
  });

  it("fetchNotion 은 params 로 조회하고 AjaxResponse 전체를 반환한다", async () => {
    const store = useAdminPageStore();
    mockedGet.mockResolvedValueOnce(ok({ rsltObj: { title: "n" } }));

    const res = await store.fetchNotion("page", "abc");

    expect(mockedGet).toHaveBeenCalledWith("/api/notion/notion.do", {
      params: { dataType: "page", dataId: "abc" },
    });
    expect(res.rslt).toBe(true);
    expect(res.rsltObj).toEqual({ title: "n" });
  });

  it("fetchNotion 실패는 throw 한다", async () => {
    const store = useAdminPageStore();
    mockedGet.mockResolvedValueOnce(fail("notion 실패"));

    await expect(store.fetchNotion("page", "abc")).rejects.toThrow("notion 실패");
  });
});

describe("캐시 (rsltMap·중첩 삭제·no-body)", () => {
  it("fetchCacheMap 은 rsltMap 을 cacheMap 으로 옮긴다", async () => {
    const store = useAdminPageStore();
    mockedGet.mockResolvedValueOnce({ data: { rsltMap: { cacheA: { k1: "v1" }, cacheB: {} } } });

    await store.fetchCacheMap();

    expect(mockedGet).toHaveBeenCalledWith("/api/cache/cache-active-map");
    expect(Object.keys(store.cacheMap)).toEqual(["cacheA", "cacheB"]);
    expect(store.cacheLoading).toBe(false);
  });

  it("fetchCacheDetail 은 rsltObj 를 cacheDetail 로 옮긴다", async () => {
    const store = useAdminPageStore();
    mockedGet.mockResolvedValueOnce(ok({ rsltObj: { value: "detail" } }));

    await store.fetchCacheDetail("cacheA", "k1");

    expect(mockedGet).toHaveBeenCalledWith("/api/cache/cache-active-dtl", {
      params: { cacheName: "cacheA", cacheKey: "k1" },
    });
    expect(store.cacheDetail).toEqual({ value: "detail" });
  });

  it("clearCacheByName 은 성공 시 해당 캐시 키를 제거한다", async () => {
    const store = useAdminPageStore();
    store.cacheMap = { cacheA: { k1: "v1" }, cacheB: { k2: "v2" } };
    mockedPost.mockResolvedValueOnce(ok());

    await store.clearCacheByName("cacheA");

    const [url, body] = mockedPost.mock.calls[0];
    expect(url).toBe("/api/cache/cache-clear-by-nm");
    expect((body as FormData).get("cacheName")).toBe("cacheA");
    expect(Object.keys(store.cacheMap)).toEqual(["cacheB"]);
  });

  it("evictCacheEntry 는 성공 시 중첩 키를 제거한다", async () => {
    const store = useAdminPageStore();
    store.cacheMap = { cacheA: { k1: "v1", k2: "v2" } };
    mockedPost.mockResolvedValueOnce(ok());

    await store.evictCacheEntry("cacheA", "k1");

    expect(Object.keys(store.cacheMap.cacheA)).toEqual(["k2"]);
  });

  it("clearAllCaches 는 body 없는 POST 로 전체를 비운다", async () => {
    const store = useAdminPageStore();
    store.cacheMap = { cacheA: { k1: "v1" } };
    mockedPost.mockResolvedValueOnce(ok({ message: "처리됨" }));

    const message = await store.clearAllCaches();

    // body 없는 POST → URL 인자만 확인(apiPost(url)=axios.post(url, undefined))
    expect(mockedPost.mock.calls[0][0]).toBe("/api/cache-clear");
    expect(store.cacheMap).toEqual({});
    expect(message).toBe("처리됨");
  });

  it("clearCacheByName 실패는 throw 하고 맵을 보존한다", async () => {
    const store = useAdminPageStore();
    store.cacheMap = { cacheA: { k1: "v1" } };
    mockedPost.mockResolvedValueOnce(fail("캐시 삭제 실패"));

    await expect(store.clearCacheByName("cacheA")).rejects.toThrow("캐시 삭제 실패");
    expect(Object.keys(store.cacheMap)).toEqual(["cacheA"]);
  });
});

describe("chat RAG 설정 (PATCH) / 저널 설정 (PUT)", () => {
  it("fetchChatRagSettings 는 응답 값을 설정에 반영한다", async () => {
    const store = useAdminPageStore();
    mockedGet.mockResolvedValueOnce(ok({ rsltObj: { ragEnabled: false, ragTopK: 9 } }));

    await store.fetchChatRagSettings();

    expect(mockedGet).toHaveBeenCalledWith("/admin/chat/settings");
    expect(store.chatRagSettings.ragEnabled).toBe(false);
    expect(store.chatRagSettings.ragTopK).toBe(9);
  });

  it("saveChatRagSettings 는 PATCH body 를 싣고 응답을 반영한다", async () => {
    const store = useAdminPageStore();
    store.chatRagSettings.ragTopK = 7;
    mockedPatch.mockResolvedValueOnce(ok({ rsltObj: { ragEnabled: true, ragTopK: 7 }, message: "Saved" }));

    const message = await store.saveChatRagSettings();

    const [url, body] = mockedPatch.mock.calls[0];
    expect(url).toBe("/admin/chat/settings");
    expect((body as { ragTopK: number }).ragTopK).toBe(7);
    expect(message).toBe("Saved");
  });

  it("saveChatRagSettings 실패는 error 를 담고 rethrow 한다", async () => {
    const store = useAdminPageStore();
    mockedPatch.mockResolvedValueOnce(fail("설정 저장 실패"));

    await expect(store.saveChatRagSettings()).rejects.toThrow("설정 저장 실패");
    expect(store.chatRagSettingsError).toBe("설정 저장 실패");
  });

  it("saveJournalSetting 은 PUT body 를 싣고 rethrow 한다", async () => {
    const store = useAdminPageStore();
    store.journalSettingAiEnabled = true;
    mockedPut.mockResolvedValueOnce(fail("저널 설정 실패"));

    await expect(store.saveJournalSetting()).rejects.toThrow("저널 설정 실패");

    const [url, body] = mockedPut.mock.calls[0];
    expect(url).toBe("/api/journal/settings");
    expect((body as { aiEnabled: boolean }).aiEnabled).toBe(true);
    expect(store.journalSettingError).toBe("저널 설정 실패");
  });
});
