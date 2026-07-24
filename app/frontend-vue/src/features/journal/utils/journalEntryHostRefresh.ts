import type { RouteLocationNormalizedLoaded } from "vue-router";
import { refreshJournalDaysForRoute } from "@/features/journal/utils/journalDayRefresh";

type JournalDayRefreshTarget = Parameters<typeof refreshJournalDaysForRoute>[0];
type JournalRefreshRoute = Pick<RouteLocationNormalizedLoaded, "name" | "query">;

/** 열린 스레드 상세를 다시 조회할 수 있는 최소 store 계약. */
export interface JournalThreadDetailRefreshTarget {
  detailOpen: boolean;
  refreshOpenDetail: () => Promise<boolean>;
}

/** 엔트리 변경 뒤 실제로 갱신한 화면 축. */
export type JournalEntryHostRefreshScope = "thread-detail" | "journal-day";

/**
 * 엔트리 변경 뒤 현재 라우트가 소유한 표시 데이터를 다시 조회한다.
 * <p>
 * 스레드 상세의 카드는 원본 엔트리이므로 수정·관계·라이프사이클·상태·소속 변경을 허용한다.
 * 이때 일자 store를 재조회해도 스레드 상세의 카드·집계 태그는 바뀌지 않으므로,
 * `thread-detail`에서는 열린 스레드 상세를 SSOT로 갱신하고 그 밖의 화면만 기존 일자 갱신을 사용한다.
 *
 * @return 갱신 대상으로 선택한 화면 축
 */
export async function refreshJournalEntryHostForRoute(
  journalStore: JournalDayRefreshTarget,
  threadStore: JournalThreadDetailRefreshTarget,
  route: JournalRefreshRoute,
  fallbackStdrdDt?: string,
): Promise<JournalEntryHostRefreshScope> {
  if (route.name === "thread-detail") {
    if (!threadStore.detailOpen) {
      console.warn("[journal-entry-host] thread detail refresh skipped: detail is closed");
      return "thread-detail";
    }
    const refreshed = await threadStore.refreshOpenDetail();
    if (!refreshed) {
      console.warn("[journal-entry-host] thread detail refresh did not replace the open detail");
    }
    return "thread-detail";
  }

  await refreshJournalDaysForRoute(journalStore, route, fallbackStdrdDt);
  return "journal-day";
}
