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

/** 스레드 상세 아래에 계속 표시되는 저널 일자 route인지 판별한다. */
function isJournalDayRoute(routeName: unknown): boolean {
  return ["journal-weekly", "journal-monthly", "journal-daily"].includes(String(routeName));
}

/**
 * 엔트리 변경 뒤 현재 전경 모달과 배경 route가 소유한 표시 데이터를 다시 조회한다.
 * <p>
 * 스레드 상세의 카드는 원본 엔트리이므로 수정·관계·라이프사이클·상태·소속 변경을 허용한다.
 * 변경 전에는 `thread-detail` route만 상세 호스트로 보아, 주간·월간·일간 위에 직접 연 상세 모달을 놓쳤다.
 * 변경 후에는 `detailOpen`을 전경 판단 기준으로 사용하고, 열린 상세를 먼저 갱신한 뒤 배경이 저널 일자
 * route이면 배경 목록도 갱신한다. 반환값은 `thread-detail`을 유지해 배경 스크롤은 발생시키지 않는다.
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
    }
    return "thread-detail";
  }

  if (route.name === "thread-detail") {
    console.warn("[journal-entry-host] thread detail refresh skipped: detail is closed");
    return "thread-detail";
  }

  await refreshJournalDaysForRoute(journalStore, route, fallbackStdrdDt);
  return "journal-day";
}
