import { StyleSheet, Text, View } from "react-native";
import { JournalDayList } from "./JournalDayList";
import { colors } from "../../theme/colors";
import { formatDateDots, parseDateOnly } from "../../utils/date";
import type { JournalDay } from "../../types/journalDay";
import { dayHasEntries, dreamEntriesFromDay } from "../../types/journalDay";

/** 요일 표기 (일요일 시작 인덱스) */
const WEEKDAYS = ["일", "월", "화", "수", "목", "금", "토"] as const;

export type JournalDaySectionProps = {
  /** 렌더할 저널 일자 */
  day: JournalDay;
  /** 일자 안에 엔트리가 없을 때 표시 문구 */
  emptyText?: string;
};

/**
 * 목록 안 한 일자 섹션. 날짜·요일 헤더 + 해당일 저널 목록.
 * 주별·월별 조회에서 여러 일자를 날짜 순서로 나열할 때 공통으로 쓰인다.
 * 서버가 반환한 일자만 렌더하며, 일자 안에 엔트리가 없으면 `emptyText`로 구분한다.
 */
export function JournalDaySection({ day, emptyText = "기록 없음" }: JournalDaySectionProps) {
  const chapters = day.journalChapterList ?? [];
  const topDreams = dreamEntriesFromDay(day);
  const hasAny = dayHasEntries(day);
  const weekday = WEEKDAYS[parseDateOnly(day.stdrdDt).getDay()] ?? "";

  return (
    <View style={styles.daySection}>
      <View style={styles.dayHeader}>
        <Text style={styles.dayDate}>{formatDateDots(day.stdrdDt)}</Text>
        <Text style={styles.dayWeekday}>{weekday}</Text>
      </View>
      <JournalDayList
        loading={false}
        error={null}
        hasAny={hasAny}
        chapters={chapters}
        topDreams={topDreams}
        emptyText={emptyText}
        centerPaddingVertical={16}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  daySection: { gap: 8 },
  dayHeader: {
    flexDirection: "row",
    alignItems: "center",
    gap: 8,
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
    paddingBottom: 6
  },
  dayDate: {
    color: colors.text,
    fontSize: 15,
    fontWeight: "800"
  },
  dayWeekday: {
    color: colors.muted,
    fontSize: 13,
    fontWeight: "700"
  }
});
