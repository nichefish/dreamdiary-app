/**
 * journal store — fetchTagCloud 요청 경합(race) 가드 단위 테스트.
 * 섹션별 requestSeq 로 "마지막으로 시작한 요청"의 응답만 상태에 반영하고,
 * 늦게 도착한 이전 요청의 성공/실패는 모두 무시하는 계약을 고정한다.
 * axios·metronic 재초기화·locale store 는 mock 으로 대체한다 (네트워크·DOM 미사용).
 * 픽스처는 비개인 가상 태그만 사용한다 (개인 정보 금지 룰).
 */
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import axios from "axios";

vi.mock("axios", () => ({ default: { get: vi.fn() } }));
vi.mock("@/shared/utils/metronicReinit", () => ({
  reinitMetronicAfterDom: vi.fn(() => Promise.resolve()),
}));
vi.mock("@/shared/i18n/stores/locale", () => ({
  useLocaleStore: () => ({ t: (key: string) => key }),
}));

import { useJournalStore } from "./journal";

type AxiosGetResult = ReturnType<typeof axios.get>;

/** 응답 타이밍을 테스트가 직접 제어하기 위한 deferred 헬퍼 */
function deferred() {
  let resolve!: (value: { data: { rsltList: unknown[] } }) => void;
  let reject!: (reason: unknown) => void;
  const promise = new Promise<{ data: { rsltList: unknown[] } }>((res, rej) => {
    resolve = res;
    reject = rej;
  });
  return { promise: promise as unknown as AxiosGetResult, resolve, reject };
}

/** 가상 태그 픽스처 — normalizeTagCloudList 통과 후에도 동일한 형태 */
function makeTag(id: number, name: string) {
  return { id, name, ctgr: "장소", contentSize: 1, tagClass: "tag-1", textClass: "text-1" };
}

const mockedGet = vi.mocked(axios.get);

/** node 환경에는 localStorage 가 없다 — store 초기화(journal_day_sort 복원)용 in-memory stub */
function makeLocalStorageStub() {
  const bag = new Map<string, string>();
  return {
    getItem: (key: string) => bag.get(key) ?? null,
    setItem: (key: string, value: string) => void bag.set(key, value),
    removeItem: (key: string) => void bag.delete(key),
    clear: () => bag.clear(),
  };
}

describe("fetchTagCloud 요청 경합 가드", () => {
  beforeEach(() => {
    vi.stubGlobal("localStorage", makeLocalStorageStub());
    setActivePinia(createPinia());
    mockedGet.mockReset();
    vi.spyOn(console, "error").mockImplementation(() => undefined);
  });
  afterEach(() => {
    vi.restoreAllMocks();
    vi.unstubAllGlobals();
  });

  it("늦게 도착한 이전 요청 응답은 무시하고 최신 요청 결과를 유지한다", async () => {
    const store = useJournalStore();
    const first = deferred();
    const second = deferred();
    mockedGet.mockReturnValueOnce(first.promise).mockReturnValueOnce(second.promise);

    const p1 = store.fetchTagCloud({ sections: ["day"] });
    const p2 = store.fetchTagCloud({ sections: ["day"] });

    // 최신(두 번째) 요청이 먼저 도착
    second.resolve({ data: { rsltList: [makeTag(2, "바다")] } });
    await p2;
    expect(store.tagCloud.dayTagList).toEqual([makeTag(2, "바다")]);

    // 이전(첫 번째) 요청이 뒤늦게 도착 — seq 불일치로 무시되어야 한다
    first.resolve({ data: { rsltList: [makeTag(1, "숲")] } });
    await p1;
    expect(store.tagCloud.dayTagList).toEqual([makeTag(2, "바다")]);
  });

  it("늦게 도착한 이전 요청의 실패도 최신 결과를 지우지 않는다", async () => {
    const store = useJournalStore();
    const first = deferred();
    const second = deferred();
    mockedGet.mockReturnValueOnce(first.promise).mockReturnValueOnce(second.promise);

    const p1 = store.fetchTagCloud({ sections: ["day"] });
    const p2 = store.fetchTagCloud({ sections: ["day"] });

    second.resolve({ data: { rsltList: [makeTag(2, "바다")] } });
    await p2;

    first.reject(new Error("stale request failed"));
    await p1;
    expect(store.tagCloud.dayTagList).toEqual([makeTag(2, "바다")]);
  });

  it("최신 요청이 실패하면 해당 섹션만 빈 배열로 초기화한다", async () => {
    const store = useJournalStore();
    const dayReq = deferred();
    const diaryReq = deferred();
    mockedGet.mockReturnValueOnce(dayReq.promise).mockReturnValueOnce(diaryReq.promise);

    const p = store.fetchTagCloud({ sections: ["day", "diary"] });
    dayReq.reject(new Error("day section failed"));
    diaryReq.resolve({ data: { rsltList: [makeTag(3, "여행")] } });
    await p;

    expect(store.tagCloud.dayTagList).toEqual([]);
    expect(store.tagCloud.diaryTagList).toEqual([makeTag(3, "여행")]);
  });

  it("겹친 요청이 모두 끝나야 tagCloudLoading 이 해제된다", async () => {
    const store = useJournalStore();
    const first = deferred();
    const second = deferred();
    mockedGet.mockReturnValueOnce(first.promise).mockReturnValueOnce(second.promise);

    const p1 = store.fetchTagCloud({ sections: ["day"] });
    const p2 = store.fetchTagCloud({ sections: ["day"] });
    expect(store.tagCloudLoading).toBe(true);

    second.resolve({ data: { rsltList: [] } });
    await p2;
    expect(store.tagCloudLoading).toBe(true); // 첫 요청이 아직 미완료

    first.resolve({ data: { rsltList: [] } });
    await p1;
    expect(store.tagCloudLoading).toBe(false);
  });

  it("sections 미지정 시 day/diary/dream 3개 섹션을 모두 조회한다", async () => {
    const store = useJournalStore();
    mockedGet.mockImplementation(((url: string, config?: { params?: Record<string, unknown> }) => {
      if (url === "/api/journal/day/tags") {
        return Promise.resolve({ data: { rsltList: [makeTag(1, "숲")] } });
      }
      const list = config?.params?.type === "DIARY" ? [makeTag(2, "바다")] : [makeTag(3, "여행")];
      return Promise.resolve({ data: { rsltList: list } });
    }) as unknown as typeof axios.get);

    await store.fetchTagCloud();

    expect(mockedGet).toHaveBeenCalledTimes(3);
    expect(store.tagCloud.dayTagList).toEqual([makeTag(1, "숲")]);
    expect(store.tagCloud.diaryTagList).toEqual([makeTag(2, "바다")]);
    expect(store.tagCloud.dreamTagList).toEqual([makeTag(3, "여행")]);
  });

  it("응답 항목 중 id/name 이 빈 항목은 걸러진다 (normalizeTagCloudList 규칙)", async () => {
    const store = useJournalStore();
    mockedGet.mockResolvedValueOnce({
      data: { rsltList: [makeTag(1, "숲"), { id: "", name: "무명" }, { id: 9 }] },
    });

    await store.fetchTagCloud({ sections: ["day"] });

    expect(store.tagCloud.dayTagList).toEqual([makeTag(1, "숲")]);
  });
});
