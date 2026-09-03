import {
  Pressable,
  RefreshControl,
  ScrollView,
  StyleSheet,
  Text,
  View
} from "react-native";
import { JournalDayList } from "./JournalDayList";
import { useJournalDay } from "../../hooks/useJournalDay";
import { colors } from "../../theme/colors";
import { formatDateDots } from "../../utils/date";

export type JournalDailyViewProps = {
  /** 조회 기준 일자 (YYYY-MM-DD) */
  selectedDate: string;
  /** 기준 일자가 오늘인지 여부 (다음 날 이동 제한·빈 문구 분기에 사용) */
  atToday: boolean;
  /** 기준 일자를 하루 단위로 이동 */
  shiftDay: (delta: number) => void;
  /** 기준 일자를 오늘로 되돌린다. 실제로 이동했으면 true */
  goToToday: () => boolean;
};

/**
 * 일별(DAILY) 저널 조회 뷰. Today 탭 일/주 토글의 '일' 모드 본문.
 * 선택 일자를 하루 단위로 이동하며 해당일 저널을 읽는다.
 */
export function JournalDailyView({ selectedDate, atToday, shiftDay, goToToday }: JournalDailyViewProps) {
  const {
    loading,
    refreshing,
    error,
    chapters,
    topDreams,
    hasAny,
    load,
    onRefresh
  } = useJournalDay(selectedDate);

  function handleGoToToday() {
    if (goToToday()) {
      void load(true);
    }
  }

  return (
    <ScrollView
      contentContainerStyle={styles.container}
      refreshControl={
        <RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={colors.accent} />
      }
    >
      <View style={styles.dateNav}>
        <Pressable
          accessibilityRole="button"
          accessibilityLabel="이전 날"
          onPress={() => shiftDay(-1)}
          style={styles.dateNavButton}
        >
          <Text style={styles.dateNavArrow}>‹</Text>
        </Pressable>

        <Pressable
          accessibilityRole="button"
          onPress={handleGoToToday}
          style={styles.dateLabelWrap}
        >
          <Text style={styles.dateLabel}>{formatDateDots(selectedDate)}</Text>
          {!atToday && <Text style={styles.dateTodayHint}>오늘로</Text>}
        </Pressable>

        <Pressable
          accessibilityRole="button"
          accessibilityLabel="다음 날"
          disabled={atToday}
          onPress={() => shiftDay(1)}
          style={[styles.dateNavButton, atToday && styles.dateNavButtonDisabled]}
        >
          <Text style={[styles.dateNavArrow, atToday && styles.dateNavArrowDisabled]}>›</Text>
        </Pressable>
      </View>

      <JournalDayList
        loading={loading}
        error={error}
        hasAny={hasAny}
        chapters={chapters}
        topDreams={topDreams}
        emptyText={atToday ? "오늘은 아직 기록이 없습니다." : "이 날의 기록이 없습니다."}
      />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flexGrow: 1, padding: 20, gap: 16, paddingBottom: 24 },
  dateNav: {
    flexDirection: "row",
    alignItems: "center",
    gap: 4
  },
  dateNavButton: {
    paddingHorizontal: 12,
    paddingVertical: 6
  },
  dateNavButtonDisabled: {
    opacity: 0.3
  },
  dateNavArrow: {
    fontSize: 28,
    color: colors.accent,
    lineHeight: 32,
    fontWeight: "300"
  },
  dateNavArrowDisabled: {
    color: colors.muted
  },
  dateLabelWrap: {
    flex: 1,
    alignItems: "center",
    gap: 2
  },
  dateLabel: {
    color: colors.text,
    fontSize: 17,
    fontWeight: "700"
  },
  dateTodayHint: {
    color: colors.accent,
    fontSize: 11,
    fontWeight: "600"
  }
});
