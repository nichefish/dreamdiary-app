import { useCallback, useEffect, useState } from "react";
import { useFocusEffect } from "@react-navigation/native";
import { getWeeklyJournalDays } from "../api/dreamDiaryApi";
import type { JournalDay } from "../types/journalDay";

export type UseJournalWeekResult = {
  /** 주 시작일이 속한 주의 저널 일자 목록 (날짜 오름차순) */
  days: JournalDay[];
  loading: boolean;
  refreshing: boolean;
  error: string | null;
  hasAny: boolean;
  load: (isRefresh?: boolean) => Promise<void>;
  onRefresh: () => void;
};

/**
 * 주간(WEEKLY) 저널 일자 목록 로드·refresh 훅.
 * 서버가 반환한 일자를 날짜 오름차순으로 정렬해 그대로 노출한다(클라이언트 보정 없음).
 * @param weekStartDt 주 시작일(월요일) YYYY-MM-DD
 */
export function useJournalWeek(weekStartDt: string): UseJournalWeekResult {
  const [days, setDays] = useState<JournalDay[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async (isRefresh = false) => {
    if (!isRefresh) setLoading(true);
    setError(null);
    try {
      const res = await getWeeklyJournalDays(weekStartDt);
      const list = (res.rsltList ?? []).slice().sort((a, b) => a.stdrdDt.localeCompare(b.stdrdDt));
      setDays(list);
    } catch (e) {
      console.error("[useJournalWeek] weekly journal load failed", { weekStartDt }, e);
      setError(e instanceof Error ? e.message : "불러오는 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [weekStartDt]);

  useEffect(() => {
    void load();
  }, [load]);

  useFocusEffect(
    useCallback(() => {
      void load(true);
    }, [load])
  );

  const onRefresh = useCallback(() => {
    setRefreshing(true);
    void load(true);
  }, [load]);

  const hasAny = days.length > 0;

  return { days, loading, refreshing, error, hasAny, load, onRefresh };
}
