import { beforeEach, describe, expect, it, vi } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import axios from "axios";
import { assertAuthenticatedBeforeModal } from "@/shared/auth/sessionPing";
import { useBoardGroupStore } from "./boardGroup";

vi.mock("axios", () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    delete: vi.fn(),
    put: vi.fn(),
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

describe("boardGroup store 독립 GLOBAL Prefix 등록", () => {
  const mockedGet = vi.mocked(axios.get);
  const mockedPost = vi.mocked(axios.post);
  const mockedAuth = vi.mocked(assertAuthenticatedBeforeModal);

  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
    mockedAuth.mockResolvedValue(true);
  });

  it("등록 모달은 Prefix Scope 공유 후보 조회 없이 즉시 열린다", async () => {
    const store = useBoardGroupStore();

    await store.openCreate();

    expect(store.modalOpen).toBe(true);
    expect(mockedGet).not.toHaveBeenCalled();
  });

  it("신규 등록 payload에는 게시판 기본 정보만 포함한다", async () => {
    mockedPost.mockResolvedValueOnce({ data: { rslt: true, message: "저장 완료" } });
    mockedGet.mockResolvedValueOnce({
      data: {
        rslt: true,
        rsltObj: { content: [], totalElements: 0, totalPages: 0, number: 0, size: 10 },
      },
    });
    const store = useBoardGroupStore();
    await store.openCreate();
    store.form.boardKey = "FIXTURE_NEW_BOARD";
    store.form.boardName = "신규 가상 게시판";

    await store.submitForm();

    const payload = mockedPost.mock.calls[0]?.[1] as FormData;
    expect(mockedPost.mock.calls[0]?.[0]).toBe("/api/board/groups");
    expect(payload.get("boardKey")).toBe("FIXTURE_NEW_BOARD");
    expect(payload.has("prefixSourceBoardId")).toBe(false);
  });
});
