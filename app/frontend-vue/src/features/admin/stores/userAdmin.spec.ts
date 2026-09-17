import { beforeEach, describe, expect, it, vi } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import axios from "axios";
import { useUserAdminStore } from "./userAdmin";

vi.mock("axios", () => ({
  default: { get: vi.fn(), post: vi.fn(), delete: vi.fn() },
}));

vi.mock("@/shared/i18n/stores/locale", () => ({
  useLocaleStore: () => ({ t: (key: string) => key }),
}));

vi.mock("@/shared/utils/swal", () => ({
  swalFire: vi.fn().mockResolvedValue(undefined),
  swalAlert: vi.fn().mockResolvedValue(undefined),
}));

vi.mock("@/shared/auth/sessionPing", () => ({
  assertAuthenticatedBeforeModal: vi.fn().mockResolvedValue(true),
}));

const mockedGet = vi.mocked(axios.get);
const mockedPost = vi.mocked(axios.post);
const mockedDelete = vi.mocked(axios.delete);

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

/** content 목록을 담은 페이지 결과 응답. */
function page(content: unknown[], extra: Record<string, unknown> = {}) {
  return ok({ rsltObj: { content, totalElements: content.length, totalPages: 1, number: 0, size: 10, ...extra } });
}

beforeEach(() => {
  setActivePinia(createPinia());
  vi.clearAllMocks();
  // 기본: 목록 조회는 빈 페이지 성공
  mockedGet.mockResolvedValue(page([]));
});

describe("fetchUsers", () => {
  it("성공 시 페이지 결과를 상태로 파싱한다", async () => {
    const store = useUserAdminStore();
    mockedGet.mockResolvedValueOnce(
      page([{ id: 1, username: "alice" }], { totalElements: 3, totalPages: 2, number: 1, size: 10 }),
    );

    await store.fetchUsers(1);

    expect(mockedGet).toHaveBeenCalledWith("/api/users", { params: { page: 1, size: 10 } });
    expect(store.rows.map((r) => r.id)).toEqual([1]);
    expect(store.totalElements).toBe(3);
    expect(store.totalPages).toBe(2);
    expect(store.currentPage).toBe(1);
    expect(store.error).toBe("");
    expect(store.loading).toBe(false);
  });

  it("키워드·역할 필터를 파라미터에 싣는다", async () => {
    const store = useUserAdminStore();
    store.keyword = "  alice  ";
    store.roleKey = "ADMIN";

    await store.fetchUsers(0);

    expect(mockedGet).toHaveBeenCalledWith("/api/users", {
      params: { page: 0, size: 10, searchType: "username", searchKeyword: "alice", roleKey: "ADMIN" },
    });
  });

  it("실패 시 error 를 남기고 loading 을 되돌린다", async () => {
    const store = useUserAdminStore();
    mockedGet.mockResolvedValueOnce(fail("목록 실패"));

    await store.fetchUsers(0);

    expect(store.error).toBe("목록 실패");
    expect(store.loading).toBe(false);
  });
});

describe("openDetail / openEdit", () => {
  it("openDetail 은 상세를 조회해 열고 detail 을 채운다", async () => {
    const store = useUserAdminStore();
    mockedGet.mockResolvedValueOnce(ok({ rsltObj: { id: 7, username: "bob" } }));

    await store.openDetail(7);

    expect(mockedGet).toHaveBeenCalledWith("/api/users/7");
    expect(store.detailOpen).toBe(true);
    expect(store.detail?.id).toBe(7);
    expect(store.detailLoading).toBe(false);
  });

  it("openEdit 는 조회 결과를 폼으로 정규화한다(이메일 분리·역할)", async () => {
    const store = useUserAdminStore();
    mockedGet.mockResolvedValueOnce(
      ok({
        rsltObj: {
          id: 5,
          username: "carol",
          email: "carol@example.com",
          userRoles: [{ roleKey: "USER", roleName: "사용자" }],
        },
      }),
    );

    await store.openEdit(5);

    expect(store.formOpen).toBe(true);
    expect(store.form.id).toBe(5);
    expect(store.form.emailId).toBe("carol");
    expect(store.form.emailDomain).toBe("example.com");
    expect(store.form.roleKeyList).toEqual(["USER"]);
    expect(store.isEdit).toBe(true);
  });
});

describe("submit", () => {
  it("신규는 /api/users 로 POST 하고 성공 후 첫 페이지를 재조회한다", async () => {
    const store = useUserAdminStore();
    store.form.username = "dave";
    mockedPost.mockResolvedValueOnce(ok({ message: "저장됨" }));

    const message = await store.submit();

    expect(message).toBe("저장됨");
    const [url, body, config] = mockedPost.mock.calls[0];
    expect(url).toBe("/api/users");
    expect(body).toBeInstanceOf(FormData);
    expect(config?.headers?.["Content-Type"]).toBe("multipart/form-data");
    expect(store.formOpen).toBe(false);
    expect(store.saving).toBe(false);
    // 성공 후 목록 재조회(page 0)
    expect(mockedGet).toHaveBeenCalledWith("/api/users", { params: { page: 0, size: 10 } });
  });

  it("수정은 /api/users/{id} 로 POST 한다", async () => {
    const store = useUserAdminStore();
    store.form.id = 9;
    store.form.username = "erin";
    mockedPost.mockResolvedValueOnce(ok({ message: "수정됨" }));

    await store.submit();

    expect(mockedPost.mock.calls[0][0]).toBe("/api/users/9");
  });

  it("실패면 throw 하고 saving 을 되돌린다", async () => {
    const store = useUserAdminStore();
    store.form.username = "frank";
    mockedPost.mockResolvedValueOnce(fail("저장 실패"));

    await expect(store.submit()).rejects.toThrow("저장 실패");
    expect(store.saving).toBe(false);
  });
});

describe("passwordReset / deleteUser", () => {
  it("passwordReset 은 성공 메시지를 반환한다", async () => {
    const store = useUserAdminStore();
    mockedPost.mockResolvedValueOnce(ok({ message: "초기화됨" }));

    const message = await store.passwordReset(3);

    // body 없는 POST → apiPost(url) 은 axios.post(url, undefined) 로 호출되므로 URL 인자만 확인한다.
    expect(mockedPost.mock.calls[0][0]).toBe("/api/users/3/password-reset");
    expect(message).toBe("초기화됨");
  });

  it("deleteUser 는 삭제 후 목록을 재조회한다", async () => {
    const store = useUserAdminStore();
    store.rows = [{ id: 1, username: "a" }, { id: 2, username: "b" }];
    store.currentPage = 0;
    mockedDelete.mockResolvedValueOnce(ok({ message: "삭제됨" }));

    const message = await store.deleteUser(1);

    expect(mockedDelete).toHaveBeenCalledWith("/api/users/1");
    expect(message).toBe("삭제됨");
    expect(mockedGet).toHaveBeenCalledWith("/api/users", { params: { page: 0, size: 10 } });
  });

  it("deleteUser 는 마지막 항목 삭제 시 이전 페이지로 이동한다", async () => {
    const store = useUserAdminStore();
    store.rows = [{ id: 5, username: "solo" }];
    store.currentPage = 2;
    mockedDelete.mockResolvedValueOnce(ok({ message: "삭제됨" }));

    await store.deleteUser(5);

    expect(mockedGet).toHaveBeenCalledWith("/api/users", { params: { page: 1, size: 10 } });
  });
});

describe("중복 확인 (soft)", () => {
  it("usernameDuplicateCheck 는 rslt·message 를 그대로 반환한다", async () => {
    const store = useUserAdminStore();
    mockedGet.mockResolvedValueOnce(ok({ message: "사용 가능" }));

    const result = await store.usernameDuplicateCheck("newname");

    expect(mockedGet).toHaveBeenCalledWith("/api/users/duplicate-check/username", {
      params: { username: "newname" },
    });
    expect(result).toEqual({ ok: true, message: "사용 가능" });
  });

  it("emailDuplicateCheck 는 실패 응답도 throw 없이 반환한다", async () => {
    const store = useUserAdminStore();
    mockedGet.mockResolvedValueOnce(fail("중복"));

    const result = await store.emailDuplicateCheck("dup@example.com");

    expect(result).toEqual({ ok: false, message: "중복" });
  });
});

describe("fetchBootstrap", () => {
  it("부트스트랩·코드 목록을 병렬 조회해 상태를 채운다", async () => {
    const store = useUserAdminStore();
    mockedGet
      .mockResolvedValueOnce(ok({ rsltObj: { roleList: [{ roleKey: "USER", roleName: "사용자" }] } }))
      .mockResolvedValueOnce(ok({ rsltList: [{ code: "C1", codeName: "회사1" }] }))
      .mockResolvedValueOnce(ok({ rsltList: [{ code: "T1", codeName: "팀1" }] }))
      .mockResolvedValueOnce(ok({ rsltList: [{ code: "E1", codeName: "고용1" }] }))
      .mockResolvedValueOnce(ok({ rsltList: [{ code: "R1", codeName: "직급1" }] }));

    await store.fetchBootstrap();

    expect(store.roles.map((r) => r.roleKey)).toEqual(["USER"]);
    expect(store.cmpyOptions).toHaveLength(1);
    expect(store.teamOptions).toHaveLength(1);
    expect(store.emplymOptions).toHaveLength(1);
    expect(store.rankOptions).toHaveLength(1);
    expect(store.bootstrapLoading).toBe(false);
  });
});
