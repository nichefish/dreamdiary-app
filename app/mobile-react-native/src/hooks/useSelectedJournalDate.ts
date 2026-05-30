import { useCallback, useEffect, useState } from "react";
import { AppState } from "react-native";
import { addDays, clampDateToToday, isToday, toDateStr } from "../utils/date";

export type UseSelectedJournalDateResult = {
  /** 선택 일자 (YYYY-MM-DD, 오늘 이후 불가) */
  selectedDate: string;
  /** 날짜 설정 — 미래·비정상 값은 today로 수렴 */
  setSelectedDate: (next: string | ((prev: string) => string)) => void;
  /** 하루 이동 (delta: -1 이전, +1 다음) */
  shiftDay: (delta: number) => void;
  /** 오늘로 이동. 이미 오늘이면 true (호출자 refresh 트리거용) */
  goToToday: () => boolean;
  atToday: boolean;
};

/**
 * TodayScreen 날짜 선택·미래일 가드·AppState 복귀 재검증.
 * DAILY 로드는 `useJournalDay(selectedDate)`와 분리한다.
 */
export function useSelectedJournalDate(): UseSelectedJournalDateResult {
  const [selectedDate, setSelectedDateRaw] = useState(() => toDateStr(new Date()));

  const setSelectedDate = useCallback((next: string | ((prev: string) => string)) => {
    setSelectedDateRaw((prev) => {
      const resolved = typeof next === "function" ? next(prev) : next;
      return clampDateToToday(resolved);
    });
  }, []);

  useEffect(() => {
    const sub = AppState.addEventListener("change", (nextState) => {
      if (nextState !== "active") return;
      setSelectedDateRaw((prev) => clampDateToToday(prev));
    });
    return () => sub.remove();
  }, []);

  const shiftDay = useCallback((delta: number) => {
    setSelectedDate((d) => addDays(d, delta));
  }, [setSelectedDate]);

  const goToToday = useCallback(() => {
    const today = toDateStr(new Date());
    if (selectedDate !== today) {
      setSelectedDate(today);
      return false;
    }
    return true;
  }, [selectedDate, setSelectedDate]);

  return {
    selectedDate,
    setSelectedDate,
    shiftDay,
    goToToday,
    atToday: isToday(selectedDate)
  };
}
