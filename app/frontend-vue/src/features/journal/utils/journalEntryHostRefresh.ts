import type { RouteLocationNormalizedLoaded } from "vue-router";
import { refreshJournalDaysForRoute } from "@/features/journal/utils/journalDayRefresh";

type JournalDayRefreshTarget = Parameters<typeof refreshJournalDaysForRoute>[0];
type JournalRefreshRoute = Pick<RouteLocationNormalizedLoaded, "name" | "query">;

/** 열린 스레드 상세를 다시 조회할 수 있는 최소 store 계약. */
export interface JournalThreadDetailRefreshTarget {
  detailOpen: boolean;
  refreshOpenDetail: () => Promise<boolean>;
}

/** 검색 팝업이 등록하는 로컬 결과 재조회 콜백. */
type JournalEntrySearchHostRefresh = () => Promise<void>;

/** 엔트리 변경 뒤 실제로 갱신한 화면 축. */
export type JournalEntryHostRefreshScope = "thread-detail" | "journal-day" | "journal-entry-search";

/**
 * 검색 팝업(`journal-entry-search`)이 마운트될 때 로컬 `loadEntries`를 등록한다.
 * 변경 전에는 `journalStore.loading` watch로 fetchDays 부수효과에만 의존해
 * 스레드 소속 토글·상세 열림 상태에서 검색 칩이 갱신되지 않았다.
 * 변경 후에는 호스트 갱신이 등록된 콜백을 직접 호출한다.
 */
let searchHostRefresh: JournalEntrySearchHostRefresh | null = null;

/**
 * 검색 결과 호스트를 등록하고, 해제 함수를 반환한다.
 *
 * @param refresh 검색 결과 재조회 (`loadEntries`)
 * @return 등록 해제 함수
 */
export function registerJournalEntrySearchHost(refresh: JournalEntrySearchHostRefresh): () => void {
  searchHostRefresh = refresh;
  return () => {
    if (searchHostRefresh === refresh) {
      searchHostRefresh = null;
    }
  };
}

/** 스레드 상세 아래에 계속 표시되는 저널 일자 route인지 판별한다. */
function isJournalDayRoute(routeName: unknown): boolean {
  return ["journal-weekly", "journal-monthly", "journal-daily", "journal-daily-tab"].includes(String(routeName));
}

/** 엔트리 검색 팝업 route인지 판별한다. */
function isJournalEntrySearchRoute(routeName: unknown): boolean {
  return String(routeName) === "journal-entry-search";
}

/** 등록된 검색 호스트가 있으면 로컬 결과를 재조회한다. */
async function refreshRegisteredSearchHost(): Promise<void> {
  if (!searchHostRefresh) {
    console.warn("[journal-entry-host] search host refresh skipped: no registered host");
    return;
  }
  await searchHostRefresh();
}

/**
 * 엔트리 변경 뒤 현재 전경 모달과 배경 route가 소유한 표시 데이터를 다시 조회한다.
 * <p>
 * 스레드 상세의 카드는 원본 엔트리이므로 수정·관계·라이프사이클·상태·소속 변경을 허용한다.
 * 변경 전에는 `thread-detail` route만 상세 호스트로 보아, 주간·월간·일간 위에 직접 연 상세 모달을 놓쳤다.
 * 변경 후에는 `detailOpen`을 전경 판단 기준으로 사용하고, 열린 상세를 먼저 갱신한 뒤 배경이 저널 일자
 * route이면 배경 목록도 갱신한다. 검색 팝업 배경이면 등록된 `loadEntries`로 로컬 결과(스레드 칩 포함)를
 * 함께 갱신한다. 반환값이 `thread-detail`이면 배경 스크롤은 발생시키지 않는다.
 *
 * @return 갱신 대상으로 선택한 화면 축
 */
export async function refreshJournalEntryHostForRoute(
  journalStore: JournalDayRefreshTarget,
  threadStore: JournalThreadDetailRefreshTarget,
  route: JournalRefreshRoute,
  fallbackStdrdDt?: string,
): Promise<JournalEntryHostRefreshScope> {
  if (threadStore.detailOpen) {
    const refreshed = await threadStore.refreshOpenDetail();
    if (!refreshed) {
      console.warn("[journal-entry-host] thread detail refresh did not replace the open detail");
    }
    if (isJournalDayRoute(route.name)) {
      await refreshJournalDaysForRoute(journalStore, route, fallbackStdrdDt);
    } else if (isJournalEntrySearchRoute(route.name)) {
      await refreshRegisteredSearchHost();
    }
    return "thread-detail";
  }

  if (route.name === "thread-detail") {
    console.warn("[journal-entry-host] thread detail refresh skipped: detail is closed");
    return "thread-detail";
  }

  if (isJournalEntrySearchRoute(route.name)) {
    await refreshRegisteredSearchHost();
    return "journal-entry-search";
  }

  await refreshJournalDaysForRoute(journalStore, route, fallbackStdrdDt);
  return "journal-day";
}
