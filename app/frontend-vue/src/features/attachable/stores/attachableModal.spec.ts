/**
 * attachableModal store — 관련 글/FLOW 연결 대상 검색 계약 단위 테스트.
 * 통합 엔트리 API와 실패/빈 결과 분리 상태를 가상 데이터로 검증한다.
 */
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import axios from "axios";
import { assertAuthenticatedBeforeModal } from "@/shared/auth/sessionPing";

vi.mock("axios", () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));
vi.mock("@/shared/auth/sessionPing", () => ({
  assertAuthenticatedBeforeModal: vi.fn(() => Promise.resolve(true)),
}));
vi.mock("@/shared/i18n/stores/locale", () => ({
  useLocaleStore: () => ({ t: (key: string) => key }),
}));
vi.mock("@/shared/utils/authError", () => ({
  isAuthExpiredError: vi.fn(() => false),
}));

import { useAttachableModalStore } from "./attachableModal";

const mockedGet = vi.mocked(axios.get);
const mockedAssertAuthenticated = vi.mocked(assertAuthenticatedBeforeModal);

describe("관련 글 연결 대상 검색", () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockedGet.mockReset();
    mockedAssertAuthenticated.mockReset();
    mockedAssertAuthenticated.mockResolvedValue(true);
    vi.spyOn(console, "warn").mockImplementation(() => undefined);
    vi.spyOn(console, "error").mockImplementation(() => undefined);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("선택한 대상 유형과 키워드로 통합 엔트리 API를 호출한다", async () => {
    mockedGet.mockResolvedValueOnce({
      data: {
        rslt: true,
        rsltList: [
          {
            id: 20,
            contentType: "JOURNAL_DREAM",
            title: "검색 대상 기록",
            stdrdDt: "2026-01-02",
            content: "가상 본문",
          },
        ],
      },
    });
    const store = useAttachableModalStore();
    await store.openRelated("JOURNAL_DIARY", 10);
    store.relatedKeyword = "  검색어  ";

    await store.searchRelatedTargets();

    /* 대상 유형 기본값은 출발 엔트리와 같은 유형이다 (변경 전: 반대 유형이라 DREAM 을 기대했다) */
    expect(mockedGet).toHaveBeenCalledWith("/api/journal/entries", {
      params: {
        type: "DIARY",
        searchKeywords: "검색어",
        pageSize: 8,
        sort: "DESC",
      },
    });
    expect(store.relatedSearchResults.map((item) => item.id)).toEqual([20]);
    expect(store.relatedSearchErrorMsg).toBe("");
  });

  it("대상 유형 기본값은 출발 엔트리와 같은 유형이다", async () => {
    const store = useAttachableModalStore();

    await store.openRelated("JOURNAL_DIARY", 10);
    expect(store.relatedTargetContentType).toBe("JOURNAL_DIARY");

    await store.openRelatedFlow("JOURNAL_DREAM", 20);
    expect(store.relatedTargetContentType).toBe("JOURNAL_DREAM");
  });

  it("대상 유형 select 에 없는 출발 유형(노트)은 일기로 떨어뜨린다", async () => {
    const store = useAttachableModalStore();

    await store.openRelatedFlow("JOURNAL_NOTE", 30);

    expect(store.relatedTargetContentType).toBe("JOURNAL_DIARY");
  });

  it("검색 요청 실패는 정상 0건과 구분하는 오류 상태를 남긴다", async () => {
    mockedGet.mockRejectedValueOnce(new Error("search request failed"));
    const store = useAttachableModalStore();
    await store.openRelatedFlow("JOURNAL_DREAM", 20);
    store.relatedKeyword = "검색어";

    await store.searchRelatedTargets();

    expect(store.relatedSearchAttempted).toBe(true);
    expect(store.relatedSearchResults).toEqual([]);
    expect(store.relatedSearchErrorMsg).toBe("related-content.search.failure");
  });
});
