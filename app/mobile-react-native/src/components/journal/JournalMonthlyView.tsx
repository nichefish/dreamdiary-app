import { useMemo, useState } from "react";
import {
  ActivityIndicator,
  Pressable,
  RefreshControl,
  ScrollView,
  StyleSheet,
  Text,
  View
} from "react-native";
import { JournalDaySection } from "./JournalDaySection";
import { useJournalMonth } from "../../hooks/useJournalMonth";
import { colors } from "../../theme/colors";
import { dayHasEntries } from "../../types/journalDay";

const MONTH_NAMES = ["1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월"];

/** 오늘이 속한 연·월 "YYYY-MM" */
function currentYearMonth(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
}

/** YYYY-MM-DD 등에서 연·월 "YYYY-MM" 추출. 비정상이면 이번 달. */
function toYearMonth(dateStr: string): string {
  return /^\d{4}-\d{2}/.test(dateStr) ? dateStr.slice(0, 7) : currentYearMonth();
}

/** "YYYY-MM" → { year, month } */
function parseYearMonth(ym: string): { year: number; month: number } {
  const [y, m] = ym.split("-").map(Number);
  return { year: y, month: m };
}

/** 연·월을 delta 달만큼 이동한 "YYYY-MM" */
function shiftMonth(ym: string, delta: number): string {
  const { year, month } = parseYearMonth(ym);
  const d = new Date(year, month - 1 + delta, 1);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}`;
}

export type JournalMonthlyViewProps = {
  /** 진입 시 기준 일자 (YYYY-MM-DD). 이 날짜가 속한 달로 시작한다. */
  initialDate: string;
};

/**
 * 월별(MONTHLY) 저널 조회 뷰. Today 탭 일/주/월 토글의 '월' 모드 본문.
 * 기준 연·월을 달 단위로 이동하며, 해당 달의 저널 일자를 날짜 순서 연속 목록으로 읽는다.
 */
export function JournalMonthlyView({ initialDate }: JournalMonthlyViewProps) {
  const [yearMonth, setYearMonth] = useState<string>(() => toYearMonth(initialDate));
  const { days, loading, refreshing, error, load, onRefresh } = useJournalMonth(yearMonth);

  const atThisMonth = yearMonth >= currentYearMonth();
  const { year, month } = parseYearMonth(yearMonth);
  const monthLabel = `${year}년 ${MONTH_NAMES[month - 1]}`;

  // 엔트리가 있는 일자만 날짜순으로 (훅이 이미 오름차순 정렬)
  const entryDays = useMemo(() => days.filter(dayHasEntries), [days]);

  function shiftBy(delta: number) {
    setYearMonth((prev) => shiftMonth(prev, delta));
  }

  function goToThisMonth() {
    if (!atThisMonth) setYearMonth(currentYearMonth());
  }

  return (
    <ScrollView
      contentContainerStyle={styles.container}
      refreshControl={
        <RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={colors.accent} />
      }
    >
      <View style={styles.monthNav}>
        <Pressable
          accessibilityRole="button"
          accessibilityLabel="이전 달"
          onPress={() => shiftBy(-1)}
          style={styles.monthNavButton}
        >
          <Text style={styles.monthNavArrow}>‹</Text>
        </Pressable>

        <Pressable
          accessibilityRole="button"
          onPress={goToThisMonth}
          style={styles.monthLabelWrap}
        >
          <Text style={styles.monthLabel}>{monthLabel}</Text>
          {!atThisMonth && <Text style={styles.monthTodayHint}>이번 달로</Text>}
        </Pressable>

        <Pressable
          accessibilityRole="button"
          accessibilityLabel="다음 달"
          disabled={atThisMonth}
          onPress={() => shiftBy(1)}
          style={[styles.monthNavButton, atThisMonth && styles.monthNavButtonDisabled]}
        >
          <Text style={[styles.monthNavArrow, atThisMonth && styles.monthNavArrowDisabled]}>›</Text>
        </Pressable>
      </View>

      {loading ? (
        <View style={styles.center}>
          <ActivityIndicator size="large" color={colors.accent} />
        </View>
      ) : error != null ? (
        <View style={styles.center}>
          <Text style={styles.errorText}>{error}</Text>
          <Pressable
            accessibilityRole="button"
            onPress={() => { void load(); }}
            style={styles.retryButton}
          >
            <Text style={styles.retryButtonText}>다시 시도</Text>
          </Pressable>
        </View>
      ) : entryDays.length === 0 ? (
        <View style={styles.center}>
          <Text style={styles.emptyText}>이 달의 기록이 없습니다.</Text>
        </View>
      ) : (
        <View style={styles.monthList}>
          {entryDays.map((day) => (
            <JournalDaySection key={day.id} day={day} />
          ))}
        </View>
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flexGrow: 1, padding: 20, gap: 16, paddingBottom: 24 },
  monthNav: {
    flexDirection: "row",
    alignItems: "center",
    gap: 4
  },
  monthNavButton: {
    paddingHorizontal: 12,
    paddingVertical: 6
  },
  monthNavButtonDisabled: {
    opacity: 0.3
  },
  monthNavArrow: {
    fontSize: 28,
    color: colors.accent,
    lineHeight: 32,
    fontWeight: "300"
  },
  monthNavArrowDisabled: {
    color: colors.muted
  },
  monthLabelWrap: {
    flex: 1,
    alignItems: "center",
    gap: 2
  },
  monthLabel: {
    color: colors.text,
    fontSize: 17,
    fontWeight: "700"
  },
  monthTodayHint: {
    color: colors.accent,
    fontSize: 11,
    fontWeight: "600"
  },
  center: { alignItems: "center", justifyContent: "center", gap: 12, paddingVertical: 48 },
  errorText: { color: "#C0392B", fontSize: 14 },
  retryButton: { paddingHorizontal: 12, paddingVertical: 7 },
  retryButtonText: { color: colors.accent, fontSize: 13, fontWeight: "700" },
  emptyText: { color: colors.secondaryText, fontSize: 16, fontWeight: "600" },
  monthList: { gap: 20 }
});
