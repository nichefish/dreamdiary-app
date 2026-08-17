import { beforeEach, describe, expect, it, vi } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import axios from "axios";
import { assertAuthenticatedBeforeModal } from "@/shared/auth/sessionPing";
import { swalConfirm } from "@/shared/utils/swal";
import { useUserGroupStore } from "./userGroup";

vi.mock("axios", () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
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
  swalConfirm: vi.fn().mockResolvedValue(true),
}));

const FIXTURE_GROUP_ID = 5;

/** 페이지 응답 한 건을 만든다. */
function pageResponse(content: unknown[]) {
  return { data: { rslt: true, rsltObj: { content, totalElements: content.length, totalPages: 1, number: 0 } } };
}

describe("userGroup 관리 스토어", () => {
  const mockedGet = vi.mocked(axios.get);
  const mockedPost = vi.mocked(axios.post);
  const mockedPut = vi.mocked(axios.put);
  const mockedDelete = vi.mocked(axios.delete);
  const mockedConfirm = vi.mocked(swalConfirm);

  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
    vi.mocked(assertAuthenticatedBeforeModal).mockResolvedValue(true);
    mockedConfirm.mockResolvedValue(true);
  });

  it("목록 조회는 페이징 파라미터로 GET 하고 rsltObj.content 를 rows 로 채운다", async () => {
    mockedGet.mockResolvedValueOnce(pageResponse([{ id: FIXTURE_GROUP_ID, groupKey: "FIXTURE_GROUP", groupName: "가상 그룹", useYn: "Y" }]));
    const store = useUserGroupStore();

    await store.fetchList(0);

    expect(mockedGet).toHaveBeenCalledWith("/api/user/groups", { params: { page: 0, size: 10 } });
    expect(store.rows).toHaveLength(1);
    expect(store.rows[0]?.groupName).toBe("가상 그룹");
    expect(store.totalElements).toBe(1);
    expect(store.error).toBe("");
  });

  it("목록 조회 실패(rslt=false) 시 error 를 채운다", async () => {
    mockedGet.mockResolvedValueOnce({ data: { rslt: false, message: "권한 없음" } });
    const store = useUserGroupStore();

    await store.fetchList(0);

    expect(store.error).toBe("common.result.failure");
    expect(store.rows).toEqual([]);
  });

  it("등록 모달은 권한 목록이 없으면 GET /api/permissions 로 채운다", async () => {
    mockedGet.mockResolvedValueOnce({ data: { rslt: true, rsltList: [{ id: 1, permKey: "menu.admin.code" }] } });
    const store = useUserGroupStore();

    await store.openCreate();

    expect(store.modalOpen).toBe(true);
    expect(store.isEdit).toBe(false);
    expect(mockedGet).toHaveBeenCalledWith("/api/permissions");
    expect(store.permissions).toHaveLength(1);
  });

  it("권한 목록이 이미 있으면 등록 모달에서 다시 조회하지 않는다", async () => {
    mockedGet.mockResolvedValueOnce({ data: { rslt: true, rsltList: [{ id: 1, permKey: "menu.admin.code" }] } });
    const store = useUserGroupStore();
    await store.fetchPermissions();
    mockedGet.mockClear();

    await store.openCreate();

    expect(mockedGet).not.toHaveBeenCalled();
  });

  it("신규 저장은 POST /api/user/groups 로 JSON payload 를 보내고 목록을 재조회한다", async () => {
    mockedGet.mockResolvedValueOnce({ data: { rslt: true, rsltList: [] } }); // openCreate 권한 조회
    mockedPost.mockResolvedValueOnce({ data: { rslt: true } });
    mockedGet.mockResolvedValueOnce(pageResponse([])); // save 후 fetchList(0)
    const store = useUserGroupStore();
    await store.openCreate();
    store.form.groupKey = "FIXTURE_GROUP";
    store.form.groupName = "가상 그룹";
    store.form.permissionKeys = ["menu.admin.code"];
    store.form.memberUsernames = ["alice", "bob"];

    await store.save();

    expect(mockedPost.mock.calls[0]?.[0]).toBe("/api/user/groups");
    expect(mockedPost.mock.calls[0]?.[1]).toMatchObject({
      groupKey: "FIXTURE_GROUP",
      groupName: "가상 그룹",
      permissionKeys: ["menu.admin.code"],
      memberUsernames: ["alice", "bob"],
    });
    expect(store.modalOpen).toBe(false);
    expect(mockedGet).toHaveBeenLastCalledWith("/api/user/groups", { params: { page: 0, size: 10 } });
  });

  it("groupKey/groupName 이 비면 검증 실패로 POST 하지 않는다", async () => {
    const store = useUserGroupStore();
    store.form.groupKey = "";
    store.form.groupName = "";

    await store.save();

    expect(mockedPost).not.toHaveBeenCalled();
  });

  it("수정 저장은 PUT /api/user/groups/{id} 로 보낸다", async () => {
    mockedPut.mockResolvedValueOnce({ data: { rslt: true } });
    mockedGet.mockResolvedValueOnce(pageResponse([]));
    const store = useUserGroupStore();
    store.form.id = FIXTURE_GROUP_ID;
    store.form.groupKey = "FIXTURE_GROUP";
    store.form.groupName = "가상 그룹";

    await store.save();

    expect(mockedPut.mock.calls[0]?.[0]).toBe(`/api/user/groups/${FIXTURE_GROUP_ID}`);
    expect(mockedPost).not.toHaveBeenCalled();
  });

  it("권한 토글과 멤버 추가/제거는 폼 상태만 바꾼다", () => {
    const store = useUserGroupStore();

    store.togglePermission("menu.admin.code");
    expect(store.form.permissionKeys).toContain("menu.admin.code");
    store.togglePermission("menu.admin.code");
    expect(store.form.permissionKeys).not.toContain("menu.admin.code");

    store.form.memberInput = "alice";
    store.addMember();
    store.form.memberInput = "alice"; // 중복은 무시
    store.addMember();
    expect(store.form.memberUsernames).toEqual(["alice"]);
    store.removeMember("alice");
    expect(store.form.memberUsernames).toEqual([]);
  });

  it("삭제는 확인 후 DELETE 하고, 취소 시 DELETE 하지 않는다", async () => {
    mockedDelete.mockResolvedValueOnce({ data: { rslt: true } });
    mockedGet.mockResolvedValueOnce(pageResponse([]));
    const store = useUserGroupStore();

    await store.remove(FIXTURE_GROUP_ID);
    expect(mockedDelete).toHaveBeenCalledWith(`/api/user/groups/${FIXTURE_GROUP_ID}`);

    mockedDelete.mockClear();
    mockedConfirm.mockResolvedValueOnce(false);
    await store.remove(FIXTURE_GROUP_ID);
    expect(mockedDelete).not.toHaveBeenCalled();
  });
});