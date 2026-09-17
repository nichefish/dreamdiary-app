import { useCallback, useEffect, useState } from "react";
import { useFocusEffect } from "@react-navigation/native";
import { getMonthlyJournalDays } from "../api/dreamDiaryApi";
import type { JournalDay } from "../types/journalDay";

export type UseJournalMonthResult = {
  /** 기준 연·월에 속한 저널 일자 목록 (날짜 오름차순) */
  days: JournalDay[];
  loading: boolean;
  refreshing: boolean;
  error: string | null;
  load: (isRefresh?: boolean) => Promise<void>;
  onRefresh: () => void;
};

/**
 * 월간(MONTHLY) 저널 일자 목록 로드·refresh 훅.
 * 서버가 반환한 일자를 날짜 오름차순으로 정렬해 그대로 노출한다(클라이언트 보정 없음).
 * @param yearMonth 기준 연·월 "YYYY-MM"
 */
export function useJournalMonth(yearMonth: string): UseJournalMonthResult {
  const [days, setDays] = useState<JournalDay[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async (isRefresh = false) => {
    if (!isRefresh) setLoading(true);
    setError(null);
    try {
      const res = await getMonthlyJournalDays(yearMonth);
      const list = (res.rsltList ?? []).slice().sort((a, b) => a.stdrdDt.localeCompare(b.stdrdDt));
      setDays(list);
    } catch (e) {
      console.error("[useJournalMonth] monthly journal load failed", { yearMonth }, e);
      setError(e instanceof Error ? e.message : "불러오는 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [yearMonth]);

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

  return { days, loading, refreshing, error, load, onRefresh };
}
