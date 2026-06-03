import { useCallback, useEffect, useMemo, useState } from "react";
import {
  ActivityIndicator,
  Pressable,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  View
} from "react-native";
import { useFocusEffect, useNavigation } from "@react-navigation/native";
import type { BottomTabNavigationProp } from "@react-navigation/bottom-tabs";
import { getMonthlyJournalDays } from "../api/dreamDiaryApi";
import { colors } from "../theme/colors";
import { normalizeDateStr, toDateStr } from "../utils/date";
import type { MainTabParamList } from "../navigation/AppNavigator";
import { navigateToDailyHub } from "../navigation/dailyHub";
import type { JournalDay } from "../types/journalDay";
import { dreamEntriesFromDay } from "../types/journalDay";

// ─── 날짜 유틸 ──────────────────────────────────────────────

const DOW_LABELS = ["일", "월", "화", "수", "목", "금", "토"];
const MONTH_NAMES = ["1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월"];

function currentYearMonth(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
}

function toDateKey(year: number, month: number, day: number): string {
  return `${year}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
}

function todayKey(): string {
  const d = new Date();
  return toDateKey(d.getFullYear(), d.getMonth() + 1, d.getDate());
}

/** YYYY-MM → { year, month } */
function parseYearMonth(ym: string): { year: number; month: number } {
  const [y, m] = ym.split("-").map(Number);
  return { year: y, month: m };
}

function shiftMonth(ym: string, delta: number): string {
  const { year, month } = parseYearMonth(ym);
  const d = new Date(year, month - 1 + delta, 1);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}`;
}

/** 주 단위 2차원 배열 반환. null = 해당 월 아님 */
function buildCalendarGrid(year: number, month: number): (number | null)[][] {
  const firstDow = new Date(year, month - 1, 1).getDay();
  const daysInMonth = new Date(year, month, 0).getDate();
  const cells: (number | null)[] = [
    ...Array<null>(firstDow).fill(null),
    ...Array.from({ length: daysInMonth }, (_, i) => i + 1)
  ];
  while (cells.length % 7 !== 0) cells.push(null);
  return Array.from({ length: cells.length / 7 }, (_, i) => cells.slice(i * 7, i * 7 + 7));
}

/** 해당 JournalDay 에 실제 엔트리가 있는지 판별 */
function hasEntries(day: JournalDay): boolean {
  if (dreamEntriesFromDay(day).length > 0) return true;
  return day.journalChapterList?.some(ch =>
    (ch.journalDiaryList?.length ?? 0) > 0 ||
    (ch.journalDreamList?.length ?? 0) > 0 ||
    (ch.journalNoteList?.length ?? 0) > 0
  ) ?? false;
}

// ─── 일 셀 컴포넌트 ──────────────────────────────────────────

interface DayCellProps {
  day: number;
  isToday: boolean;
  hasDot: boolean;
  isSunday: boolean;
  isSaturday: boolean;
  onPress: () => void;
}

function DayCell({ day, isToday, hasDot, isSunday, isSaturday, onPress }: DayCellProps) {
  return (
    <Pressable onPress={onPress} style={styles.dayCell}>
      <View style={[styles.dayCircle, isToday && styles.dayCirleToday]}>
        <Text style={[
          styles.dayText,
          isToday && styles.dayTextToday,
          isSunday && !isToday && styles.dayTextSunday,
          isSaturday && !isToday && styles.dayTextSaturday
        ]}>
          {day}
        </Text>
      </View>
      {/* 엔트리 있는 날 도트 표시 */}
      <View style={[styles.dot, hasDot ? styles.dotVisible : styles.dotHidden]} />
    </Pressable>
  );
}

// ─── 메인 화면 ──────────────────────────────────────────────

type Nav = BottomTabNavigationProp<MainTabParamList, "Calendar">;

export function CalendarScreen() {
  const navigation = useNavigation<Nav>();
  const [yearMonth, setYearMonth] = useState(currentYearMonth);
  const [monthDays, setMonthDays] = useState<JournalDay[]>([]);
  const [loading, setLoading] = useState(false);

  const fetchMonth = useCallback(async (ym: string) => {
    setLoading(true);
    try {
      const res = await getMonthlyJournalDays(ym);
      setMonthDays(res.rsltList ?? []);
    } catch {
      setMonthDays([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { void fetchMonth(yearMonth); }, [yearMonth, fetchMonth]);

  // 탭 포커스 시 현재 월 새로고침 (오늘 탭에서 기록 추가 후 복귀 시 도트 갱신)
  useFocusEffect(
    useCallback(() => { void fetchMonth(yearMonth); }, [yearMonth, fetchMonth])
  );

  const { year, month } = parseYearMonth(yearMonth);
  const today = todayKey();
  const grid = useMemo(() => buildCalendarGrid(year, month), [year, month]);

  const entryDateSet = useMemo(() => {
    const set = new Set<string>();
    for (const d of monthDays) {
      const safeDate = normalizeDateStr(d.stdrdDt);
      if (hasEntries(d) && safeDate) set.add(safeDate);
    }
    return set;
  }, [monthDays]);

  const isFutureMonth = useMemo(() => {
    const now = currentYearMonth();
    return yearMonth > now;
  }, [yearMonth]);

  return (
    <SafeAreaView style={styles.safeArea}>
      <ScrollView contentContainerStyle={styles.container}>

        {/* 헤더 */}
        <View style={styles.header}>
          <Text style={styles.kicker}>DreamDiary</Text>
          <Text style={styles.title}>달력</Text>
        </View>

        {/* 월 이동 */}
        <View style={styles.monthNav}>
          <Pressable
            accessibilityRole="button"
            accessibilityLabel="이전 달"
            onPress={() => setYearMonth(ym => shiftMonth(ym, -1))}
            style={styles.monthNavBtn}
          >
            <Text style={styles.monthNavArrow}>‹</Text>
          </Pressable>

          <Text style={styles.monthLabel}>
            {year}년 {MONTH_NAMES[month - 1]}
          </Text>

          <Pressable
            accessibilityRole="button"
            accessibilityLabel="다음 달"
            disabled={isFutureMonth}
            onPress={() => setYearMonth(ym => shiftMonth(ym, 1))}
            style={[styles.monthNavBtn, isFutureMonth && styles.monthNavBtnDisabled]}
          >
            <Text style={[styles.monthNavArrow, isFutureMonth && styles.monthNavArrowDisabled]}>›</Text>
          </Pressable>
        </View>

        {/* 이번 달 기록 일수 요약 — 로딩 완료 후 기록이 하나라도 있을 때만 표시 */}
        {!loading && entryDateSet.size > 0 && (
          <Text style={styles.monthSummary}>{entryDateSet.size}일 기록</Text>
        )}

        {/* 요일 헤더 */}
        <View style={styles.week}>
          {DOW_LABELS.map((label, i) => (
            <View key={label} style={styles.dayCell}>
              <Text style={[styles.dowLabel, i === 0 && styles.dowSunday, i === 6 && styles.dowSaturday]}>
                {label}
              </Text>
            </View>
          ))}
        </View>

        {/* 달력 그리드 */}
        {loading ? (
          <View style={styles.loadingRow}>
            <ActivityIndicator color={colors.accent} />
          </View>
        ) : (
          grid.map((week, wi) => (
            <View key={wi} style={styles.week}>
              {week.map((day, di) => {
                if (day == null) return <View key={di} style={styles.dayCell} />;
                const dateKey = toDateKey(year, month, day);
                const isToday = dateKey === today;
                const hasDot = entryDateSet.has(dateKey);
                const isFuture = dateKey > today;
                return (
                  <DayCell
                    key={di}
                    day={day}
                    isToday={isToday}
                    hasDot={hasDot}
                    isSunday={di === 0}
                    isSaturday={di === 6}
                    onPress={() => {
                      if (!isFuture) {
                        const safeDate = normalizeDateStr(dateKey) ?? toDateStr(new Date());
                        navigateToDailyHub(navigation, safeDate);
                      }
                    }}
                  />
                );
              })}
            </View>
          ))
        )}

        {/* 범례 */}
        <View style={styles.legend}>
          <View style={styles.legendItem}>
            <View style={[styles.dot, styles.dotVisible]} />
            <Text style={styles.legendText}>기록 있음</Text>
          </View>
        </View>

      </ScrollView>
    </SafeAreaView>
  );
}

// ─── 스타일 ─────────────────────────────────────────────────

const CELL_HEIGHT = 52;

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: colors.background },
  container: { flexGrow: 1, padding: 20, gap: 12 },
  header: { paddingTop: 8, gap: 4 },
  kicker: {
    color: colors.accent,
    fontSize: 12,
    fontWeight: "700",
    letterSpacing: 0.4,
    textTransform: "uppercase"
  },
  title: { color: colors.text, fontSize: 26, fontWeight: "800" },
  // 월 이동
  monthNav: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    paddingVertical: 4
  },
  monthNavBtn: { paddingHorizontal: 12, paddingVertical: 6 },
  monthNavBtnDisabled: { opacity: 0.3 },
  monthNavArrow: { fontSize: 28, color: colors.accent, lineHeight: 32, fontWeight: "300" },
  monthNavArrowDisabled: { color: colors.muted },
  monthLabel: { color: colors.text, fontSize: 18, fontWeight: "800" },
  // 이번 달 기록 일수 요약
  monthSummary: {
    color: colors.muted,
    fontSize: 12,
    textAlign: "center",
    marginTop: -8
  },
  // 달력 그리드
  week: { flexDirection: "row" },
  dayCell: {
    flex: 1,
    height: CELL_HEIGHT,
    alignItems: "center",
    justifyContent: "center",
    gap: 2
  },
  dayCircle: {
    width: 34,
    height: 34,
    borderRadius: 17,
    alignItems: "center",
    justifyContent: "center"
  },
  dayCirleToday: {
    backgroundColor: colors.accent
  },
  dayText: {
    color: colors.text,
    fontSize: 14,
    fontWeight: "500"
  },
  dayTextToday: {
    color: colors.onAccent,
    fontWeight: "800"
  },
  dayTextSunday: { color: "#C0392B" },
  dayTextSaturday: { color: "#1A5276" },
  // 엔트리 도트
  dot: {
    width: 5,
    height: 5,
    borderRadius: 3
  },
  dotVisible: { backgroundColor: colors.accent },
  dotHidden: { backgroundColor: "transparent" },
  // 요일 헤더
  dowLabel: {
    color: colors.secondaryText,
    fontSize: 12,
    fontWeight: "700"
  },
  dowSunday: { color: "#C0392B" },
  dowSaturday: { color: "#1A5276" },
  // 로딩
  loadingRow: {
    height: CELL_HEIGHT * 5,
    alignItems: "center",
    justifyContent: "center"
  },
  // 범례
  legend: {
    flexDirection: "row",
    paddingTop: 8,
    paddingHorizontal: 4,
    gap: 16
  },
  legendItem: {
    flexDirection: "row",
    alignItems: "center",
    gap: 6
  },
  legendText: {
    color: colors.muted,
    fontSize: 12
  }
});