import type { BottomTabNavigationProp } from "@react-navigation/bottom-tabs";
import { clampDateToToday, normalizeDateStr, toDateStr } from "../utils/date";
import type { MainTabParamList } from "./AppNavigator";

/**
 * 탐색(달력·태그)에서 Daily 허브(오늘 탭)로 날짜를 전달한다.
 * DayView 스택 push 대신 단일 Today 경로로 수렴.
 */
export function navigateToDailyHub(
  navigation: BottomTabNavigationProp<MainTabParamList>,
  date: string
): void {
  const normalized = normalizeDateStr(date);
  const safeDate = clampDateToToday(normalized ?? toDateStr(new Date()));
  navigation.navigate("Today", { date: safeDate });
}
