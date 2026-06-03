import { useCallback, useEffect, useState } from "react";
import { useFocusEffect } from "@react-navigation/native";
import { getDailyJournalDay } from "../api/dreamDiaryApi";
import type { JournalDay, JournalChapter, JournalEntry } from "../types/journalDay";
import { dreamEntriesFromDay } from "../types/journalDay";

export type UseJournalDayOptions = {
  /** 포커스 시 조용한 refresh (스피너 없음). 기본 true */
  refreshOnFocus?: boolean;
};

export type UseJournalDayResult = {
  day: JournalDay | null;
  loading: boolean;
  refreshing: boolean;
  error: string | null;
  chapters: JournalChapter[];
  topDreams: JournalEntry[];
  hasAny: boolean;
  load: (isRefresh?: boolean) => Promise<void>;
  onRefresh: () => void;
};

/**
 * 단일 일자(DAILY) 저널 데이터 로드·refresh 훅.
 * TodayScreen 공통 축.
 */
export function useJournalDay(
  dateStr: string,
  options: UseJournalDayOptions = {}
): UseJournalDayResult {
  const { refreshOnFocus = true } = options;
  const [day, setDay] = useState<JournalDay | null>(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async (isRefresh = false) => {
    if (!isRefresh) setLoading(true);
    setError(null);
    try {
      const res = await getDailyJournalDay(dateStr);
      setDay(res.rsltList?.[0] ?? null);
    } catch {
      setError("불러오는 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [dateStr]);

  useEffect(() => {
    void load();
  }, [load]);

  useFocusEffect(
    useCallback(() => {
      if (refreshOnFocus) {
        void load(true);
      }
    }, [load, refreshOnFocus])
  );

  const onRefresh = useCallback(() => {
    setRefreshing(true);
    void load(true);
  }, [load]);

  const chapters = day?.journalChapterList ?? [];
  const topDreams = dreamEntriesFromDay(day);
  const hasAny = chapters.length > 0 || topDreams.length > 0;

  return {
    day,
    loading,
    refreshing,
    error,
    chapters,
    topDreams,
    hasAny,
    load,
    onRefresh
  };
}
