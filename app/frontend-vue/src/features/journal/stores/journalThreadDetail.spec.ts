import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import { computed, ref } from "vue";
import axios from "axios";
import { createJournalThreadDetail, type JournalThreadDetailDeps } from "./journalThreadDetail";
import type { ThreadPrefix, JournalThreadDto } from "@/features/journal/stores/journalThread.types";

vi.mock("axios", () => ({
  default: { get: vi.fn(), post: vi.fn(), delete: vi.fn() },
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

vi.mock("@/features/attachable/stores/attachableModal", () => ({
  useAttachableModalStore: () => ({ setLifecycle: vi.fn() }),
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

const fetchList = vi.fn().mockResolvedValue(undefined);
const fetchPrefixOptions = vi.fn().mockResolvedValue(undefined);

function makeStore(overrides: Partial<JournalThreadDetailDeps> = {}) {
  const deps: JournalThreadDetailDeps = {
    t: (key: string) => key,
    fetchList,
    fetchPrefixOptions,
    prefixOptions: computed(() => [] as ThreadPrefix[]),
    threadList: ref([]),
    ...overrides,
  };
  return createJournalThreadDetail(deps);
}

beforeEach(() => {
  setActivePinia(createPinia());
  vi.clearAllMocks();
  swalConfirm.mockResolvedValue(true);
  // 기본: 상세/엔트리/연관 조회는 최소 형태로 성공
  mockedGet.mockResolvedValue(ok({ rsltObj: { id: 1 }, rsltList: [] }));
  mockedPost.mockResolvedValue(ok());
  mockedDelete.mockResolvedValue(ok());
});

afterEach(() => {
  vi.clearAllTimers();
});

describe("quickAddPrefix", () => {
  it("말머리를 등록하고 옵션을 재조회한 뒤 현재 선택에 반영한다", async () => {
    const store = makeStore();
    // 편집 모델을 열어두어 prefixId 반영 경로를 탄다
    await store.openRegist();
    const created: ThreadPrefix = { id: 42, name: "회고", color: null, sortOrder: 0 } as ThreadPrefix;
    mockedPost.mockResolvedValueOnce(ok({ rsltObj: created }));

    const result = await store.quickAddPrefix("  회고  ");

    const [url, body, config] = mockedPost.mock.calls[0];
    expect(url).toBe("/api/my/prefixes");
    expect((body as { name: string }).name).toBe("회고");
    expect(config?.params).toEqual({ contentType: "JOURNAL_THREAD" });
    expect(fetchPrefixOptions).toHaveBeenCalled();
    expect(store.registModel.value?.prefixId).toBe(42);
    expect(result.id).toBe(42);
  });

  it("빈 이름은 요청 없이 throw 한다", async () => {
    const store = makeStore();
    await expect(store.quickAddPrefix("   ")).rejects.toThrow("user.my.prefixes.name.required");
    expect(mockedPost).not.toHaveBeenCalled();
  });

  it("빈 응답이면 throw 한다", async () => {
    const store = makeStore();
    mockedPost.mockResolvedValueOnce(ok({ rsltObj: null }));
    await expect(store.quickAddPrefix("회고")).rejects.toThrow("journal.thread.prefix.quick-add.failure");
  });
});

describe("loadModify", () => {
  it("스레드를 조회해 수정 폼을 채운다", async () => {
    const store = makeStore();
    const dto: Partial<JournalThreadDto> = { id: 5, contentType: "JOURNAL_THREAD", title: "제목", content: "본문" };
    mockedGet.mockResolvedValueOnce(ok({ rsltObj: dto }));

    const applied = await store.openModifyPage(5);

    expect(mockedGet).toHaveBeenCalledWith("/api/journal/threads/5");
    expect(applied).toBe(true);
    expect(store.registModel.value?.id).toBe(5);
    expect(store.registModel.value?.title).toBe("제목");
    expect(store.registSurface.value).toBe("page");
  });

  it("응답 ID 불일치는 폼을 닫고 알림한다", async () => {
    const store = makeStore();
    mockedGet.mockResolvedValueOnce(ok({ rsltObj: { id: 999 } }));

    const applied = await store.openModifyPage(5);

    expect(applied).toBe(false);
    expect(store.registOpen.value).toBe(false);
    expect(swalAlert).toHaveBeenCalled();
  });
});

describe("submitRegist", () => {
  it("신규는 /api/journal/threads 로 multipart POST 하고 목록을 재조회한다", async () => {
    const store = makeStore();
    await store.openRegist();
    if (store.registModel.value) store.registModel.value.title = "새 스레드";
    mockedPost.mockResolvedValueOnce(ok({ message: "등록됨" }));

    const okResult = await store.submitRegist();

    const [url, body, config] = mockedPost.mock.calls[0];
    expect(url).toBe("/api/journal/threads");
    expect(body).toBeInstanceOf(FormData);
    expect(config?.headers?.["Content-Type"]).toBe("multipart/form-data");
    expect(okResult).toBe(true);
    expect(store.registOpen.value).toBe(false);
    expect(fetchList).toHaveBeenCalledWith(0);
  });

  it("수정은 /api/journal/threads/{id} 로 POST 한다", async () => {
    const store = makeStore();
    const dto: Partial<JournalThreadDto> = { id: 7, contentType: "JOURNAL_THREAD", title: "t", content: "c" };
    mockedGet.mockResolvedValueOnce(ok({ rsltObj: dto }));
    await store.openModifyPage(7);
    mockedPost.mockResolvedValueOnce(ok({ message: "수정됨" }));

    await store.submitRegist();

    expect(mockedPost.mock.calls[0][0]).toBe("/api/journal/threads/7");
  });

  it("실패 응답은 swalAjaxResult 로 알리고 false 를 반환한다", async () => {
    const store = makeStore();
    await store.openRegist();
    mockedPost.mockResolvedValueOnce(fail("등록 실패"));

    const okResult = await store.submitRegist();

    expect(okResult).toBe(false);
    expect(swalAjaxResult).toHaveBeenCalledWith(expect.objectContaining({ rslt: false }));
    expect(fetchList).not.toHaveBeenCalled();
  });
});

describe("deleteThread", () => {
  it("확인 후 삭제하고 목록을 재조회한다", async () => {
    const store = makeStore();
    mockedDelete.mockResolvedValueOnce(ok({ message: "삭제됨" }));

    await store.deleteThread(3);

    expect(mockedDelete).toHaveBeenCalledWith("/api/journal/threads/3");
    expect(fetchList).toHaveBeenCalledWith(0);
  });

  it("확인 취소 시 삭제하지 않는다", async () => {
    const store = makeStore();
    swalConfirm.mockResolvedValueOnce(false);

    await store.deleteThread(3);

    expect(mockedDelete).not.toHaveBeenCalled();
  });
});

describe("연관 스레드", () => {
  it("fetchRelatedThreads 는 rsltList 를 상태에 반영한다", async () => {
    const store = makeStore();
    mockedGet.mockResolvedValueOnce(ok({ rsltList: [{ targetId: 11 }, { targetId: 12 }] }));

    await store.fetchRelatedThreads(9);

    expect(mockedGet).toHaveBeenCalledWith("/api/related/JOURNAL_THREAD/9");
    expect(store.detailRelatedThreads.value.map((r) => r.targetId)).toEqual([11, 12]);
  });

  it("addRelatedThread 는 관계를 추가하고 true 를 반환한다", async () => {
    const store = makeStore();
    mockedPost.mockResolvedValueOnce(ok());

    const result = await store.addRelatedThread(9, 12);

    const [url, body] = mockedPost.mock.calls[0];
    expect(url).toBe("/api/related/JOURNAL_THREAD/9");
    expect((body as { targetId: number }).targetId).toBe(12);
    expect(result).toBe(true);
  });

  it("addRelatedThread 실패 응답은 swalAjaxResult 로 알리고 false 를 반환한다", async () => {
    const store = makeStore();
    mockedPost.mockResolvedValueOnce(fail("관계 실패"));

    const result = await store.addRelatedThread(9, 12);

    expect(result).toBe(false);
    expect(swalAjaxResult).toHaveBeenCalledWith(expect.objectContaining({ rslt: false }));
  });

  it("removeRelatedThread 는 related_content 행을 삭제하고 true 를 반환한다", async () => {
    const store = makeStore();
    mockedDelete.mockResolvedValueOnce(ok());

    const result = await store.removeRelatedThread(9, 101);

    expect(mockedDelete).toHaveBeenCalledWith("/api/related/101");
    expect(result).toBe(true);
  });
});

describe("loadDetail / refreshOpenDetail", () => {
  it("상세를 조회해 열고 detailModel 을 채운다", async () => {
    const store = makeStore();
    mockedGet.mockResolvedValueOnce(ok({ rsltObj: { id: 8, title: "상세" } }));

    const applied = await store.openDetail(8);

    expect(mockedGet).toHaveBeenCalledWith("/api/journal/threads/8");
    expect(applied).toBe(true);
    expect(store.detailOpen.value).toBe(true);
    expect(store.detailModel.value?.id).toBe(8);
    expect(store.detailSurface.value).toBe("modal");
  });

  it("빈 상세는 표면을 닫고 알림한다", async () => {
    const store = makeStore();
    mockedGet.mockResolvedValueOnce(ok({ rsltObj: null }));

    const applied = await store.openDetail(8);

    expect(applied).toBe(false);
    expect(store.detailOpen.value).toBe(false);
    expect(swalAlert).toHaveBeenCalled();
  });

  it("refreshOpenDetail 은 상세·엔트리를 원자적으로 재조회한다", async () => {
    const store = makeStore();
    mockedGet.mockResolvedValueOnce(ok({ rsltObj: { id: 8, title: "상세" } }));
    await store.openDetail(8);

    mockedGet
      .mockResolvedValueOnce(ok({ rsltObj: { id: 8, title: "갱신됨" } })) // detail
      .mockResolvedValueOnce(ok({ rsltList: [{ id: 100 }] })); // entries

    const refreshed = await store.refreshOpenDetail();

    expect(refreshed).toBe(true);
    expect(store.detailModel.value?.title).toBe("갱신됨");
    expect(store.detailEntries.value.map((e) => e.id)).toEqual([100]);
  });
});
