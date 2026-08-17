import { beforeEach, describe, expect, it, vi } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import axios from "axios";
import { assertAuthenticatedBeforeModal } from "@/shared/auth/sessionPing";
import { useCodeAdminStore } from "./codeAdmin";

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
  swalConfirm: vi.fn().mockResolvedValue(true),
}));

const FIXTURE_GROUP_ID = 3;

/** 재조회 GET 이 실패하지 않도록 page·list 양쪽을 만족하는 기본 응답. */
const DEFAULT_GET = {
  data: {
    rslt: true,
    rsltObj: { content: [], totalElements: 0, totalPages: 0, number: 0, size: 10 },
    rsltList: [],
  },
};

describe("codeAdmin 관리 스토어", () => {
  const mockedGet = vi.mocked(axios.get);
  const mockedPost = vi.mocked(axios.post);
  const mockedPatch = vi.mocked(axios.patch);
  const mockedDelete = vi.mocked(axios.delete);

  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
    vi.mocked(assertAuthenticatedBeforeModal).mockResolvedValue(true);
    mockedGet.mockResolvedValue(DEFAULT_GET);
  });

  it("분류 목록 조회는 페이징 파라미터로 GET 하고 content 를 rows 로 채운다", async () => {
    mockedGet.mockResolvedValueOnce({
      data: { rslt: true, rsltObj: { content: [{ id: FIXTURE_GROUP_ID, groupCode: "FIXTURE_GRP", groupName: "가상 분류", useYn: "Y" }], totalElements: 1, totalPages: 1, number: 0, size: 10 } },
    });
    const store = useCodeAdminStore();

    await store.fetchGroups(0);

    expect(mockedGet).toHaveBeenCalledWith("/api/code/groups", { params: { page: 0, size: 10 } });
    expect(store.rows).toHaveLength(1);
    expect(store.rows[0]?.groupName).toBe("가상 분류");
    expect(store.totalElements).toBe(1);
  });

  it("분류 등록 모달은 조회 없이 열린다", async () => {
    const store = useCodeAdminStore();

    await store.openGroupCreate();

    expect(store.groupModalOpen).toBe(true);
    expect(store.isGroupEdit).toBe(false);
    expect(mockedGet).not.toHaveBeenCalled();
  });

  it("분류 신규 저장은 POST /api/code/groups 로 groupCode 를 대문자화한 FormData 를 보낸다", async () => {
    mockedPost.mockResolvedValueOnce({ data: { rslt: true, message: "저장 완료" } });
    const store = useCodeAdminStore();
    await store.openGroupCreate();
    store.groupForm.groupCode = "fixture_grp";
    store.groupForm.groupName = "가상 분류";

    await store.submitGroup();

    const payload = mockedPost.mock.calls[0]?.[1] as FormData;
    expect(mockedPost.mock.calls[0]?.[0]).toBe("/api/code/groups");
    expect(payload.has("id")).toBe(false);
    expect(payload.get("groupCode")).toBe("FIXTURE_GRP");
    expect(payload.get("groupName")).toBe("가상 분류");
    expect(store.groupModalOpen).toBe(false);
  });

  it("분류 수정 저장은 POST /api/code/group/{id} 로 보낸다", async () => {
    mockedPost.mockResolvedValueOnce({ data: { rslt: true } });
    const store = useCodeAdminStore();
    store.groupForm.id = FIXTURE_GROUP_ID;
    store.groupForm.groupCode = "FIXTURE_GRP";
    store.groupForm.groupName = "가상 분류";

    await store.submitGroup();

    expect(mockedPost.mock.calls[0]?.[0]).toBe(`/api/code/group/${FIXTURE_GROUP_ID}`);
  });

  it("사용 여부 토글은 PATCH /api/code/group/{id} 로 반전값을 보낸다", async () => {
    mockedPatch.mockResolvedValueOnce({ data: { rslt: true } });
    const store = useCodeAdminStore();

    await store.toggleGroupUse({ id: FIXTURE_GROUP_ID, groupCode: "FIXTURE_GRP", groupName: "가상 분류", useYn: "Y" });

    expect(mockedPatch).toHaveBeenCalledWith(`/api/code/group/${FIXTURE_GROUP_ID}`, { useYn: "N" });
  });

  it("분류 삭제는 DELETE /api/code/group/{id} 후 목록을 재조회한다", async () => {
    mockedDelete.mockResolvedValueOnce({ data: { rslt: true } });
    const store = useCodeAdminStore();

    await store.deleteGroup(FIXTURE_GROUP_ID);

    expect(mockedDelete).toHaveBeenCalledWith(`/api/code/group/${FIXTURE_GROUP_ID}`);
    expect(mockedGet).toHaveBeenCalledWith("/api/code/groups", expect.objectContaining({ params: expect.any(Object) }));
  });

  it("상세 코드 목록은 groupCode 파라미터로 GET 하고 rsltList 를 items 로 채운다", async () => {
    mockedGet.mockResolvedValueOnce({
      data: { rslt: true, rsltList: [{ id: 11, groupCode: "FIXTURE_GRP", code: "FIXTURE_CD", codeName: "가상 코드", useYn: "Y" }] },
    });
    const store = useCodeAdminStore();

    await store.fetchItems("FIXTURE_GRP");

    expect(mockedGet).toHaveBeenCalledWith("/api/code/items", { params: { groupCode: "FIXTURE_GRP" } });
    expect(store.items).toHaveLength(1);
    expect(store.items[0]?.code).toBe("FIXTURE_CD");
  });

  it("상세 코드 신규 저장은 POST /api/code/items 로 code·다국어 FormData 를 보낸다", async () => {
    mockedPost.mockResolvedValueOnce({ data: { rslt: true } });
    const store = useCodeAdminStore();
    await store.openItemCreate();
    store.itemForm.groupCode = "fixture_grp";
    store.itemForm.code = "fixture_cd";
    store.itemForm.codeName = "가상 코드";
    store.itemForm.i18nRows = [{ locale: "en", codeName: "virtual code" }];

    await store.submitItem();

    const payload = mockedPost.mock.calls[0]?.[1] as FormData;
    expect(mockedPost.mock.calls[0]?.[0]).toBe("/api/code/items");
    expect(payload.get("groupCode")).toBe("FIXTURE_GRP");
    expect(payload.get("code")).toBe("FIXTURE_CD");
    expect(payload.get("i18nNames[en]")).toBe("virtual code");
  });

  it("다국어 행 추가는 미사용 로케일만 넣고, 남은 로케일이 없으면 더 추가하지 않는다", async () => {
    const store = useCodeAdminStore();
    await store.openItemCreate();

    store.addI18nRow();
    expect(store.itemForm.i18nRows).toEqual([{ locale: "en", codeName: "" }]);
    store.addI18nRow(); // ko 는 제외라 남은 로케일 없음 → no-op
    expect(store.itemForm.i18nRows).toHaveLength(1);

    store.removeI18nRow(0);
    expect(store.itemForm.i18nRows).toEqual([]);
  });
});