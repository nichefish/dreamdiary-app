import { describe, expect, it, vi, afterEach } from "vitest";
import {
  refreshJournalEntryHostForRoute,
  registerJournalEntrySearchHost,
} from "./journalEntryHostRefresh";

function makeJournalStore() {
  return {
    setViewType: vi.fn(),
    fetchDays: vi.fn().mockResolvedValue(undefined),
  };
}

describe("엔트리 액션 이후 현재 호스트 갱신 계약", () => {
  afterEach(() => {
    // 테스트 간 검색 호스트 등록이 남지 않도록 해제한다.
    const unregister = registerJournalEntrySearchHost(async () => undefined);
    unregister();
  });

  it("스레드 상세에서는 일자 목록 대신 열린 스레드 상세를 갱신한다", async () => {
    const journalStore = makeJournalStore();
    const threadStore = {
      detailOpen: true,
      refreshOpenDetail: vi.fn().mockResolvedValue(true),
    };

    const scope = await refreshJournalEntryHostForRoute(
      journalStore,
      threadStore,
      { name: "thread-detail", query: {} },
      "2026-07-24",
    );

    expect(scope).toBe("thread-detail");
    expect(threadStore.refreshOpenDetail).toHaveBeenCalledTimes(1);
    expect(journalStore.fetchDays).not.toHaveBeenCalled();
  });

  it("저널 일자 화면에서는 기존 라우트별 일자 갱신을 유지한다", async () => {
    const journalStore = makeJournalStore();
    const threadStore = {
      detailOpen: false,
      refreshOpenDetail: vi.fn().mockResolvedValue(false),
    };

    const scope = await refreshJournalEntryHostForRoute(
      journalStore,
      threadStore,
      { name: "journal-daily", query: { stdrdDt: "2026-07-24" } },
    );

    expect(scope).toBe("journal-day");
    expect(threadStore.refreshOpenDetail).not.toHaveBeenCalled();
    expect(journalStore.fetchDays).toHaveBeenCalledWith({
      viewType: "DAILY",
      stdrdDt: "2026-07-24",
      yy: 2026,
      mnth: 7,
    });
  });

  it("주간 화면 위에 스레드 상세가 열렸으면 전경 상세와 배경 일자 목록을 함께 갱신한다", async () => {
    const journalStore = makeJournalStore();
    const threadStore = {
      detailOpen: true,
      refreshOpenDetail: vi.fn().mockResolvedValue(true),
    };

    const scope = await refreshJournalEntryHostForRoute(
      journalStore,
      threadStore,
      { name: "journal-weekly", query: { stdrdDt: "2026-07-24" } },
      "2026-07-24",
    );

    expect(scope).toBe("thread-detail");
    expect(threadStore.refreshOpenDetail).toHaveBeenCalledTimes(1);
    expect(journalStore.fetchDays).toHaveBeenCalled();
  });

  it("검색 화면에서는 일자 fetchDays 대신 등록된 loadEntries로 로컬 결과를 갱신한다", async () => {
    const journalStore = makeJournalStore();
    const threadStore = {
      detailOpen: false,
      refreshOpenDetail: vi.fn().mockResolvedValue(false),
    };
    const loadEntries = vi.fn().mockResolvedValue(undefined);
    const unregister = registerJournalEntrySearchHost(loadEntries);

    try {
      const scope = await refreshJournalEntryHostForRoute(
        journalStore,
        threadStore,
        { name: "journal-entry-search", query: {} },
        "2026-07-24",
      );

      expect(scope).toBe("journal-entry-search");
      expect(loadEntries).toHaveBeenCalledTimes(1);
      expect(threadStore.refreshOpenDetail).not.toHaveBeenCalled();
      expect(journalStore.fetchDays).not.toHaveBeenCalled();
    } finally {
      unregister();
    }
  });

  it("검색 화면 위에 스레드 상세가 열렸으면 전경 상세와 검색 로컬 결과를 함께 갱신한다", async () => {
    const journalStore = makeJournalStore();
    const threadStore = {
      detailOpen: true,
      refreshOpenDetail: vi.fn().mockResolvedValue(true),
    };
    const loadEntries = vi.fn().mockResolvedValue(undefined);
    const unregister = registerJournalEntrySearchHost(loadEntries);

    try {
      const scope = await refreshJournalEntryHostForRoute(
        journalStore,
        threadStore,
        { name: "journal-entry-search", query: {} },
        "2026-07-24",
      );

      expect(scope).toBe("thread-detail");
      expect(threadStore.refreshOpenDetail).toHaveBeenCalledTimes(1);
      expect(loadEntries).toHaveBeenCalledTimes(1);
      expect(journalStore.fetchDays).not.toHaveBeenCalled();
    } finally {
      unregister();
    }
  });

  it("스레드 route에서 상세가 닫혔으면 무관한 일자 목록으로 우회하지 않는다", async () => {
    const journalStore = makeJournalStore();
    const threadStore = {
      detailOpen: false,
      refreshOpenDetail: vi.fn().mockResolvedValue(false),
    };

    const scope = await refreshJournalEntryHostForRoute(
      journalStore,
      threadStore,
      { name: "thread-detail", query: {} },
    );

    expect(scope).toBe("thread-detail");
    expect(threadStore.refreshOpenDetail).not.toHaveBeenCalled();
    expect(journalStore.fetchDays).not.toHaveBeenCalled();
  });
});
