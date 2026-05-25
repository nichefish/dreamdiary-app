import { useCallback } from "react";
import {
  Pressable,
  RefreshControl,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  View
} from "react-native";
import { useFocusEffect, useNavigation, useRoute } from "@react-navigation/native";
import type { BottomTabNavigationProp } from "@react-navigation/bottom-tabs";
import type { RouteProp } from "@react-navigation/native";
import { AddEntryFab, JournalDayList } from "../components/journal/JournalDayList";
import { QuickCapturePanel } from "../components/QuickCapturePanel";
import { useJournalDay } from "../hooks/useJournalDay";
import { useSelectedJournalDate } from "../hooks/useSelectedJournalDate";
import type { MainTabParamList } from "../navigation/AppNavigator";
import { colors } from "../theme/colors";
import { formatDateDots } from "../utils/date";

type TodayNav = BottomTabNavigationProp<MainTabParamList, "Today">;
type TodayRoute = RouteProp<MainTabParamList, "Today">;

export function TodayScreen() {
  const navigation = useNavigation<TodayNav>();
  const route = useRoute<TodayRoute>();
  const { selectedDate, setSelectedDate, shiftDay, goToToday, atToday } = useSelectedJournalDate();
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

  // 달력·태그 탭 등에서 `{ date }` 로 진입 시 선택일 동기화 (1회 소비)
  useFocusEffect(
    useCallback(() => {
      const paramDate = route.params?.date;
      if (!paramDate) return;
      setSelectedDate(paramDate);
      navigation.setParams({ date: undefined });
    }, [navigation, route.params?.date, setSelectedDate])
  );

  function handleGoToToday() {
    if (goToToday()) {
      void load(true);
    }
  }

  return (
    <SafeAreaView style={styles.safeArea}>
      <ScrollView
        contentContainerStyle={styles.container}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={colors.accent} />
        }
      >
        <View style={styles.header}>
          <Text style={styles.kicker}>DreamDiary</Text>
          <Text style={styles.title}>오늘</Text>

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
        </View>

        {atToday && (
          <QuickCapturePanel
            dateStr={selectedDate}
            onSaved={() => { void load(true); }}
          />
        )}

        <JournalDayList
          loading={loading}
          error={error}
          hasAny={hasAny}
          chapters={chapters}
          topDreams={topDreams}
          emptyText={atToday ? "오늘은 아직 기록이 없습니다." : "이 날의 기록이 없습니다."}
          emptyHint={atToday ? "「빠른 기록」 또는 + 버튼으로 추가하세요." : undefined}
        />
      </ScrollView>

      <AddEntryFab date={selectedDate} />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: colors.background },
  container: { flexGrow: 1, padding: 20, gap: 16, paddingBottom: 88 },
  header: { paddingTop: 8, gap: 8 },
  kicker: {
    color: colors.accent,
    fontSize: 12,
    fontWeight: "700",
    letterSpacing: 0.4,
    textTransform: "uppercase"
  },
  title: { color: colors.text, fontSize: 26, fontWeight: "800" },
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
