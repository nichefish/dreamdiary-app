import { beforeEach, describe, expect, it, vi } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import axios from "axios";
import { assertAuthenticatedBeforeModal } from "@/shared/auth/sessionPing";
import { useMenuAdminStore } from "./menuAdmin";

vi.mock("axios", () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    patch: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

vi.mock("@/shared/i18n/stores/locale", () => ({
  BASE_LOCALE: "ko",
  SUPPORTED_LOCALES: ["ko", "en"],
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

const FIXTURE_MENU_ID = 42;
const FIXTURE_PARENT_ID = 1;
const OK_LIST = { data: { rslt: true, rsltList: [] } };

describe("menuAdmin 관리 스토어", () => {
  const mockedGet = vi.mocked(axios.get);
  const mockedPost = vi.mocked(axios.post);
  const mockedPatch = vi.mocked(axios.patch);
  const mockedDelete = vi.mocked(axios.delete);

  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
    vi.mocked(assertAuthenticatedBeforeModal).mockResolvedValue(true);
    mockedGet.mockResolvedValue(OK_LIST);
  });

  it("트리 조회는 GET /api/menu/menu-main-list 의 rsltList 를 rows 로 채운다", async () => {
    mockedGet.mockResolvedValueOnce({
      data: { rslt: true, rsltList: [{ id: FIXTURE_MENU_ID, menuName: "가상 메뉴", menuType: "MAIN" }] },
    });
    const store = useMenuAdminStore();

    await store.fetchTree();

    expect(mockedGet).toHaveBeenCalledWith("/api/menu/menu-main-list");
    expect(store.rows).toHaveLength(1);
    expect(store.rows[0]?.menuName).toBe("가상 메뉴");
  });

  it("트리 조회 실패 시 error 를 채운다", async () => {
    mockedGet.mockResolvedValueOnce({ data: { rslt: false, message: "로드 실패" } });
    const store = useMenuAdminStore();

    await store.fetchTree();

    expect(store.error).toBe("로드 실패");
    expect(store.rows).toEqual([]);
  });

  it("하위 등록 모달은 부모 정보를 담아 조회 없이 열린다", async () => {
    const store = useMenuAdminStore();

    await store.openSubCreate({ id: FIXTURE_PARENT_ID, menuName: "가상 상위", menuType: "MAIN" });

    expect(store.modalOpen).toBe(true);
    expect(store.isEdit).toBe(false);
    expect(store.form.parentMenuId).toBe(FIXTURE_PARENT_ID);
    expect(store.form.upperMenuNm).toBe("가상 상위");
    expect(mockedGet).not.toHaveBeenCalled();
  });

  it("수정 모달은 GET /api/menu/{id} 로 폼을 채운다", async () => {
    mockedGet.mockResolvedValueOnce({
      data: { rslt: true, rsltObj: { id: FIXTURE_MENU_ID, menuName: "가상 메뉴", menuLabel: "FIXTURE_MENU", useYn: "N" } },
    });
    const store = useMenuAdminStore();

    await store.openEdit(FIXTURE_MENU_ID);

    expect(mockedGet).toHaveBeenCalledWith(`/api/menu/${FIXTURE_MENU_ID}`);
    expect(store.isEdit).toBe(true);
    expect(store.form.menuName).toBe("가상 메뉴");
    expect(store.form.useYn).toBe("N");
  });

  it("신규 저장은 POST /api/menus 로 FormData 를 보내고, NO_SUB 는 url 을 포함한다", async () => {
    mockedPost.mockResolvedValueOnce({ data: { rslt: true, message: "저장 완료" } });
    const store = useMenuAdminStore();
    await store.openSubCreate({ id: FIXTURE_PARENT_ID, menuName: "가상 상위", menuType: "MAIN" });
    store.form.menuName = "가상 메뉴";
    store.form.menuLabel = "FIXTURE_MENU";
    store.form.url = "/app/fixture.do";

    await store.submit();

    const payload = mockedPost.mock.calls[0]?.[1] as FormData;
    expect(mockedPost.mock.calls[0]?.[0]).toBe("/api/menus");
    expect(payload.has("id")).toBe(false);
    expect(payload.get("parentMenuId")).toBe(String(FIXTURE_PARENT_ID));
    expect(payload.get("menuName")).toBe("가상 메뉴");
    expect(payload.get("menuLabel")).toBe("FIXTURE_MENU");
    expect(payload.get("url")).toBe("/app/fixture.do");
    expect(store.modalOpen).toBe(false);
  });

  it("NO_SUB 가 아니면 url 을 빈 문자열로 보낸다", async () => {
    mockedPost.mockResolvedValueOnce({ data: { rslt: true } });
    const store = useMenuAdminStore();
    await store.openSubCreate({ id: FIXTURE_PARENT_ID, menuName: "가상 상위", menuType: "MAIN" });
    store.form.menuName = "가상 메뉴";
    store.form.menuLabel = "FIXTURE_MENU";
    store.form.submenuExpandType = "LIST";
    store.form.url = "/app/fixture.do";

    await store.submit();

    const payload = mockedPost.mock.calls[0]?.[1] as FormData;
    expect(payload.get("url")).toBe("");
    expect(payload.get("submenuExpandType")).toBe("LIST");
  });

  it("수정 저장은 POST /api/menu/{id} 로 보낸다", async () => {
    mockedPost.mockResolvedValueOnce({ data: { rslt: true } });
    const store = useMenuAdminStore();
    store.form.id = FIXTURE_MENU_ID;
    store.form.menuName = "가상 메뉴";
    store.form.menuLabel = "FIXTURE_MENU";

    await store.submit();

    expect(mockedPost.mock.calls[0]?.[0]).toBe(`/api/menu/${FIXTURE_MENU_ID}`);
  });

  it("사용 여부 토글은 PATCH /api/menu/{id} 로 반전값을 보낸다", async () => {
    mockedPatch.mockResolvedValueOnce({ data: { rslt: true } });
    const store = useMenuAdminStore();

    await store.toggleUse({ id: FIXTURE_MENU_ID, menuType: "SUB", useYn: "Y" });

    expect(mockedPatch).toHaveBeenCalledWith(`/api/menu/${FIXTURE_MENU_ID}`, { useYn: "N" });
  });

  it("삭제는 DELETE /api/menu/{id} 후 트리를 재조회한다", async () => {
    mockedDelete.mockResolvedValueOnce({ data: { rslt: true } });
    const store = useMenuAdminStore();

    await store.deleteMenu(FIXTURE_MENU_ID);

    expect(mockedDelete).toHaveBeenCalledWith(`/api/menu/${FIXTURE_MENU_ID}`);
    expect(mockedGet).toHaveBeenCalledWith("/api/menu/menu-main-list");
  });

  it("다국어 행 추가는 미사용 로케일만 넣고, 남은 로케일이 없으면 더 추가하지 않는다", () => {
    const store = useMenuAdminStore();

    store.addI18nRow();
    expect(store.form.i18nRows).toEqual([{ locale: "en", menuName: "", menuDescription: "" }]);
    store.addI18nRow();
    expect(store.form.i18nRows).toHaveLength(1);
    store.removeI18nRow(0);
    expect(store.form.i18nRows).toEqual([]);
  });
});