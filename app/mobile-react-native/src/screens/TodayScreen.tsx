import { useCallback, useState } from "react";
import { Pressable, SafeAreaView, StyleSheet, Text, View } from "react-native";
import { useFocusEffect, useNavigation, useRoute } from "@react-navigation/native";
import type { BottomTabNavigationProp } from "@react-navigation/bottom-tabs";
import type { RouteProp } from "@react-navigation/native";
import { JournalDailyView } from "../components/journal/JournalDailyView";
import { JournalWeeklyView } from "../components/journal/JournalWeeklyView";
import { JournalMonthlyView } from "../components/journal/JournalMonthlyView";
import { useSelectedJournalDate } from "../hooks/useSelectedJournalDate";
import type { MainTabParamList } from "../navigation/AppNavigator";
import { colors } from "../theme/colors";

type TodayNav = BottomTabNavigationProp<MainTabParamList, "Today">;
type TodayRoute = RouteProp<MainTabParamList, "Today">;

/** 날짜 조회 보기 모드 */
type ViewMode = "daily" | "weekly" | "monthly";

const VIEW_MODES: Array<{ id: ViewMode; label: string }> = [
  { id: "daily", label: "일" },
  { id: "weekly", label: "주" },
  { id: "monthly", label: "월" }
];

/**
 * Today 탭 — 날짜 조회 허브. 일/주 토글로 일별·주별 조회 뷰를 전환한다.
 * 달력·태그 탭에서 `{ date }` 로 진입하면 일별 모드로 전환해 해당 날짜를 보여준다.
 */
export function TodayScreen() {
  const navigation = useNavigation<TodayNav>();
  const route = useRoute<TodayRoute>();
  const { selectedDate, setSelectedDate, shiftDay, goToToday, atToday } = useSelectedJournalDate();
  const [viewMode, setViewMode] = useState<ViewMode>("daily");

  // 달력·태그 탭 등에서 `{ date }` 로 진입 시 일별 모드로 전환 + 선택일 동기화 (1회 소비)
  useFocusEffect(
    useCallback(() => {
      const paramDate = route.params?.date;
      if (!paramDate) return;
      setSelectedDate(paramDate);
      setViewMode("daily");
      navigation.setParams({ date: undefined });
    }, [navigation, route.params?.date, setSelectedDate])
  );

  return (
    <SafeAreaView style={styles.safeArea}>
      <View style={styles.topBar}>
        <Text style={styles.kicker}>DreamDiary</Text>
        <View style={styles.segment}>
          {VIEW_MODES.map((item) => {
            const active = item.id === viewMode;
            return (
              <Pressable
                accessibilityRole="button"
                accessibilityState={{ selected: active }}
                key={item.id}
                onPress={() => setViewMode(item.id)}
                style={[styles.segmentButton, active && styles.segmentButtonActive]}
              >
                <Text style={[styles.segmentText, active && styles.segmentTextActive]}>
                  {item.label}
                </Text>
              </Pressable>
            );
          })}
        </View>
      </View>

      {viewMode === "daily" ? (
        <JournalDailyView
          selectedDate={selectedDate}
          atToday={atToday}
          shiftDay={shiftDay}
          goToToday={goToToday}
        />
      ) : viewMode === "weekly" ? (
        <JournalWeeklyView initialDate={selectedDate} />
      ) : (
        <JournalMonthlyView initialDate={selectedDate} />
      )}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: colors.background },
  topBar: {
    paddingTop: 8,
    paddingHorizontal: 20,
    paddingBottom: 12,
    gap: 10
  },
  kicker: {
    color: colors.accent,
    fontSize: 12,
    fontWeight: "700",
    letterSpacing: 0.4,
    textTransform: "uppercase"
  },
  segment: {
    flexDirection: "row",
    gap: 6,
    backgroundColor: colors.surface,
    borderRadius: 8,
    padding: 4,
    alignSelf: "flex-start"
  },
  segmentButton: {
    minWidth: 56,
    alignItems: "center",
    borderRadius: 6,
    paddingVertical: 8,
    paddingHorizontal: 14
  },
  segmentButtonActive: { backgroundColor: colors.text },
  segmentText: { color: colors.secondaryText, fontSize: 14, fontWeight: "700" },
  segmentTextActive: { color: colors.onDark }
});
