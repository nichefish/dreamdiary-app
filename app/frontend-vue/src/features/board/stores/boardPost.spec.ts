import { beforeEach, describe, expect, it, vi } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import axios from "axios";
import { useBoardPostStore } from "./boardPost";

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
  swalConfirm: vi.fn(),
  swalAlert: vi.fn(),
  swalRequestError: vi.fn(),
  swalFire: vi.fn(),
  swalAjaxResult: vi.fn(),
}));

const FIXTURE_BOARD_KEY = "FIXTURE_BOARD";
const FIXTURE_PREFIX = {
  id: 21,
  name: "가상 말머리",
  color: "#009EF7",
  sortOrder: 0,
  activeYn: "Y",
};

describe("boardPost store 게시판 재진입 조회", () => {
  const mockedGet = vi.mocked(axios.get);

  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  it("같은 boardKey에 재진입하면 활성 Prefix 선택지를 다시 조회한다", async () => {
    let prefixRequestCount = 0;
    mockedGet.mockImplementation((url) => {
      if (url === "/api/board/posts") {
        return Promise.resolve({
          data: {
            rsltObj: { content: [], totalElements: 0, totalPages: 0, number: 0 },
          },
        });
      }
      if (url === `/api/board/${FIXTURE_BOARD_KEY}/prefixes`) {
        prefixRequestCount += 1;
        return Promise.resolve({
          data: { rsltList: prefixRequestCount === 1 ? [] : [FIXTURE_PREFIX] },
        });
      }
      if (url === "/api/tags") {
        return Promise.resolve({ data: { rsltList: [] } });
      }
      return Promise.reject(new Error(`예상하지 않은 URL: ${String(url)}`));
    });

    const store = useBoardPostStore();
    await store.setBoard(FIXTURE_BOARD_KEY);
    expect(store.prefixOptions).toEqual([]);

    await store.setBoard(FIXTURE_BOARD_KEY);

    expect(prefixRequestCount).toBe(2);
    expect(store.prefixOptions).toEqual([FIXTURE_PREFIX]);
  });
});
