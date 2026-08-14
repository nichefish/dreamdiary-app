import { beforeEach, describe, expect, it, vi } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import axios from "axios";
import { assertAuthenticatedBeforeModal } from "@/shared/auth/sessionPing";
import { useTmplatAdminStore } from "./tmplat";

vi.mock("axios", () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    delete: vi.fn(),
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
  swalAlert: vi.fn().mockResolvedValue(undefined),
}));

const FIXTURE_TMPLAT_ID = 7;

describe("tmplat 관리 스토어", () => {
  const mockedGet = vi.mocked(axios.get);
  const mockedPost = vi.mocked(axios.post);
  const mockedDelete = vi.mocked(axios.delete);
  const mockedAuth = vi.mocked(assertAuthenticatedBeforeModal);

  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
    mockedAuth.mockResolvedValue(true);
  });

  it("목록 조회는 GET /api/tmplats 의 rsltList 를 rows 로 채운다", async () => {
    mockedGet.mockResolvedValueOnce({
      data: { rslt: true, rsltList: [{ id: FIXTURE_TMPLAT_ID, title: "가상 템플릿", sortOrder: 0, useYn: "Y" }] },
    });
    const store = useTmplatAdminStore();

    await store.fetchList();

    expect(mockedGet).toHaveBeenCalledWith("/api/tmplats");
    expect(store.rows).toEqual([{ id: FIXTURE_TMPLAT_ID, title: "가상 템플릿", sortOrder: 0, useYn: "Y" }]);
    expect(store.error).toBe("");
  });

  it("목록 조회 실패 시 error 를 채우고 rows 는 비운다", async () => {
    mockedGet.mockResolvedValueOnce({ data: { rslt: false, message: "실패 사유" } });
    const store = useTmplatAdminStore();

    await store.fetchList();

    expect(store.error).toBe("실패 사유");
    expect(store.rows).toEqual([]);
  });

  it("등록 모달은 조회 없이 빈 폼으로 즉시 열린다", async () => {
    const store = useTmplatAdminStore();

    await store.openCreate();

    expect(store.modalOpen).toBe(true);
    expect(store.isEdit).toBe(false);
    expect(store.form.title).toBe("");
    expect(store.form.useYn).toBe("Y");
    expect(mockedGet).not.toHaveBeenCalled();
  });

  it("신규 저장은 POST /api/tmplats 로 id 없는 FormData 를 보낸다", async () => {
    mockedPost.mockResolvedValueOnce({ data: { rslt: true, message: "저장 완료" } });
    mockedGet.mockResolvedValueOnce({ data: { rslt: true, rsltList: [] } });
    const store = useTmplatAdminStore();
    await store.openCreate();
    store.form.title = "신규 가상 템플릿";
    store.form.content = "<p>본문</p>";
    store.form.sortOrder = 3;

    await store.submit();

    const payload = mockedPost.mock.calls[0]?.[1] as FormData;
    expect(mockedPost.mock.calls[0]?.[0]).toBe("/api/tmplats");
    expect(payload.has("id")).toBe(false);
    expect(payload.get("title")).toBe("신규 가상 템플릿");
    expect(payload.get("content")).toBe("<p>본문</p>");
    expect(payload.get("sortOrder")).toBe("3");
    expect(payload.get("useYn")).toBe("Y");
    expect(store.modalOpen).toBe(false);
    expect(mockedGet).toHaveBeenCalledWith("/api/tmplats");
  });

  it("수정 모달은 GET /api/tmplat/{id} 로 폼을 채운다", async () => {
    mockedGet.mockResolvedValueOnce({
      data: { rslt: true, rsltObj: { id: FIXTURE_TMPLAT_ID, title: "기존 가상 템플릿", content: "<p>기존</p>", sortOrder: 2, useYn: "N" } },
    });
    const store = useTmplatAdminStore();

    await store.openEdit(FIXTURE_TMPLAT_ID);

    expect(mockedGet).toHaveBeenCalledWith(`/api/tmplat/${FIXTURE_TMPLAT_ID}`);
    expect(store.isEdit).toBe(true);
    expect(store.form.id).toBe(FIXTURE_TMPLAT_ID);
    expect(store.form.title).toBe("기존 가상 템플릿");
    expect(store.form.useYn).toBe("N");
    expect(store.modalOpen).toBe(true);
  });

  it("수정 저장은 POST /api/tmplat/{id} 로 id 를 포함해 보낸다", async () => {
    mockedGet.mockResolvedValueOnce({
      data: { rslt: true, rsltObj: { id: FIXTURE_TMPLAT_ID, title: "기존 가상 템플릿", content: "<p>기존</p>", sortOrder: 2, useYn: "Y" } },
    });
    mockedPost.mockResolvedValueOnce({ data: { rslt: true, message: "저장 완료" } });
    mockedGet.mockResolvedValueOnce({ data: { rslt: true, rsltList: [] } });
    const store = useTmplatAdminStore();
    await store.openEdit(FIXTURE_TMPLAT_ID);
    store.form.title = "수정된 가상 템플릿";

    await store.submit();

    const payload = mockedPost.mock.calls[0]?.[1] as FormData;
    expect(mockedPost.mock.calls[0]?.[0]).toBe(`/api/tmplat/${FIXTURE_TMPLAT_ID}`);
    expect(payload.get("id")).toBe(String(FIXTURE_TMPLAT_ID));
    expect(payload.get("title")).toBe("수정된 가상 템플릿");
  });

  it("삭제는 DELETE /api/tmplat/{id} 후 목록을 재조회한다", async () => {
    mockedDelete.mockResolvedValueOnce({ data: { rslt: true, message: "삭제 완료" } });
    mockedGet.mockResolvedValueOnce({ data: { rslt: true, rsltList: [] } });
    const store = useTmplatAdminStore();

    await store.remove(FIXTURE_TMPLAT_ID);

    expect(mockedDelete).toHaveBeenCalledWith(`/api/tmplat/${FIXTURE_TMPLAT_ID}`);
    expect(mockedGet).toHaveBeenCalledWith("/api/tmplats");
  });

  it("삭제 실패 시 예외를 던지고 목록을 재조회하지 않는다", async () => {
    mockedDelete.mockResolvedValueOnce({ data: { rslt: false, message: "삭제 실패" } });
    const store = useTmplatAdminStore();

    await expect(store.remove(FIXTURE_TMPLAT_ID)).rejects.toThrow("삭제 실패");
    expect(mockedGet).not.toHaveBeenCalled();
  });
});