/**
 * 저널 일자 목록 갱신 — 현재 라우트·보기 타입에 맞는 fetchDays 파라미터를 구성한다.
 * journal-daily 에서 stdrdDt 없이 fetchDays() 를 호출하면 store.yy/mnth(오늘 월) 기준으로 재조회되어
 * 사용자가 보던 날짜와 어긋난다.
 */

import type { RouteLocationNormalizedLoaded } from "vue-router";
import type { JournalViewType } from "@/features/journal/stores/journal";

/** fetchDays 에 전달할 일간(DAILY) 조회 파라미터 */
export type JournalDayFetchParams = {
  viewType?: JournalViewType;
  stdrdDt?: string;
  yy?: number;
  mnth?: number;
};

/**
 * stdrdDt 에서 yy·mnth 를 추출한 DAILY 조회 파라미터.
 * JournalDayDaily.load 와 동일한 규칙.
 */
export function buildDailyFetchParams(stdrdDt: string): JournalDayFetchParams {
  const trimmed = stdrdDt.trim();
  return {
    viewType: "DAILY",
    stdrdDt: trimmed,
    yy: parseInt(trimmed.slice(0, 4), 10),
    mnth: parseInt(trimmed.slice(5, 7), 10),
  };
}

function resolveDailyStdrdDt(
  route: Pick<RouteLocationNormalizedLoaded, "query">,
  fallbackStdrdDt?: string,
): string | undefined {
  const fromQuery = route.query.stdrdDt;
  if (typeof fromQuery === "string" && fromQuery.trim()) return fromQuery.trim();
  if (fallbackStdrdDt?.trim()) return fallbackStdrdDt.trim();
  return undefined;
}

type JournalStoreRefreshTarget = {
  setViewType: (vt: JournalViewType) => void;
  fetchDays: (params?: JournalDayFetchParams) => Promise<void>;
};

/**
 * 저장·삭제·상태 변경 후 현재 화면(라우트)에 맞게 일자 목록을 재조회한다.
 *
 * @param store journal 스토어
 * @param route 현재 라우트 (name·query)
 * @param fallbackStdrdDt journal-daily 에서 query.stdrdDt 가 없을 때 사용할 기준일
 */
export async function refreshJournalDaysForRoute(
  store: JournalStoreRefreshTarget,
  route: Pick<RouteLocationNormalizedLoaded, "name" | "query">,
  fallbackStdrdDt?: string,
): Promise<void> {
  const name = route.name;

  if (name === "journal-weekly") {
    store.setViewType("WEEKLY");
    await store.fetchDays({ viewType: "WEEKLY" });
    return;
  }

  if (name === "journal-monthly") {
    store.setViewType("LIST");
    await store.fetchDays({ viewType: "LIST" });
    return;
  }

  if (name === "journal-daily") {
    const stdrdDt = resolveDailyStdrdDt(route, fallbackStdrdDt);
    store.setViewType("DAILY");
    if (stdrdDt) {
      await store.fetchDays(buildDailyFetchParams(stdrdDt));
    } else {
      await store.fetchDays({ viewType: "DAILY" });
    }
    return;
  }

  await store.fetchDays();
}