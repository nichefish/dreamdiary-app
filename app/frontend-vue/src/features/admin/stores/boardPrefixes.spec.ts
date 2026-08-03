import { beforeEach, describe, expect, it, vi } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import axios from "axios";
import { useBoardPrefixesStore } from "./boardPrefixes";

vi.mock("axios", () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    isAxiosError: vi.fn(),
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

const FIXTURE_BOARD = {
  id: 11,
  boardKey: "FIXTURE_BOARD",
  boardName: "가상 게시판",
};

const FIXTURE_MANAGEMENT = {
  boardId: FIXTURE_BOARD.id,
  boardKey: FIXTURE_BOARD.boardKey,
  boardName: FIXTURE_BOARD.boardName,
  prefixes: [
    { id: 21, name: "가상 말머리", color: "#009EF7", sortOrder: 0, activeYn: "Y" },
  ],
};

describe("boardPrefixes store 게시판별 GLOBAL Scope 관리", () => {
  const mockedGet = vi.mocked(axios.get);
  const mockedPost = vi.mocked(axios.post);
  const mockedPut = vi.mocked(axios.put);
  const mockedPatch = vi.mocked(axios.patch);

  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  it("게시판 ID로 독립 GLOBAL Prefix 관리 정보를 조회한다", async () => {
    mockedGet.mockResolvedValue({ data: { rslt: true, rsltObj: FIXTURE_MANAGEMENT } });
    const store = useBoardPrefixesStore();

    await store.open(FIXTURE_BOARD);

    expect(mockedGet).toHaveBeenCalledWith("/api/board/groups/11/prefixes");
    expect(store.modalOpen).toBe(true);
    expect(store.boardKey).toBe("FIXTURE_BOARD");
    expect(store.prefixes[0]?.id).toBe(21);
  });

  it("신규 Prefix를 게시판 관리 API로 저장한 뒤 서버 목록을 재조회한다", async () => {
    mockedPost.mockResolvedValue({ data: { rslt: true } });
    mockedGet
      .mockResolvedValueOnce({ data: { rslt: true, rsltObj: FIXTURE_MANAGEMENT } })
      .mockResolvedValueOnce({ data: { rslt: true, rsltObj: FIXTURE_MANAGEMENT } });
    const store = useBoardPrefixesStore();
    await store.open(FIXTURE_BOARD);

    const payload = { name: "신규 가상 말머리", color: "#A1B2C3", sortOrder: 1 };
    await store.savePrefix(payload);

    expect(mockedPost).toHaveBeenCalledWith("/api/board/groups/11/prefixes", payload);
    expect(mockedGet).toHaveBeenCalledTimes(2);
    expect(mockedPut).not.toHaveBeenCalled();
  });

  it("활성 상태 변경 뒤 같은 게시판 관리 정보를 재조회한다", async () => {
    mockedPatch.mockResolvedValue({ data: { rslt: true } });
    mockedGet
      .mockResolvedValueOnce({ data: { rslt: true, rsltObj: FIXTURE_MANAGEMENT } })
      .mockResolvedValueOnce({ data: { rslt: true, rsltObj: FIXTURE_MANAGEMENT } });
    const store = useBoardPrefixesStore();
    await store.open(FIXTURE_BOARD);

    await store.setPrefixActive(21, false);

    expect(mockedPatch).toHaveBeenCalledWith(
      "/api/board/groups/11/prefixes/21/active",
      null,
      { params: { active: false } },
    );
    expect(mockedGet).toHaveBeenCalledTimes(2);
  });
});
