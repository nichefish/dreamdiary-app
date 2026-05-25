import { ActivityIndicator, Text, View } from "react-native";
import { createBottomTabNavigator } from "@react-navigation/bottom-tabs";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import { useAuth } from "../context/AuthContext";
import { AddEntryScreen } from "../screens/AddEntryScreen";
import { CalendarScreen } from "../screens/CalendarScreen";
import { EntryDetailScreen } from "../screens/EntryDetailScreen";
import { EntryEditScreen } from "../screens/EntryEditScreen";
import { AIChatScreen } from "../screens/AIChatScreen";
import { InterpretationScreen } from "../screens/InterpretationScreen";
import { LoginScreen } from "../screens/LoginScreen";
import { ProfileScreen } from "../screens/ProfileScreen";
import { SearchScreen } from "../screens/SearchScreen";
import { TagExploreScreen } from "../screens/TagExploreScreen";
import { TodayScreen } from "../screens/TodayScreen";
import { colors } from "../theme/colors";
import type { JournalEntry } from "../types/journalDay";

// ─── 타입 ───────────────────────────────────────────────────

export type RootStackParamList = {
  Login: undefined;
  Main: undefined;
  /** 엔트리 상세 보기 — 탭 어디서든 push 가능 */
  EntryDetail: { entry: JournalEntry; isDream: boolean };
  /** 엔트리 수정 — EntryDetail 에서 push */
  EntryEdit: { entry: JournalEntry; isDream: boolean };
  /** 꿈 해석 화면 — EntryDetail(꿈) 에서 push */
  InterpretationDetail: { entry: JournalEntry };
  /** TodayScreen FAB 등 — 특정 날짜 기록 추가 */
  AddEntry: { date: string };
  /**
   * AI 채팅 화면 — TodayScreen QuickCapturePanel·Profile, STOMP WebSocket (`/chat`).
   */
  AiChat: undefined;
};

export type MainTabParamList = {
  /** Daily 허브 — 앱 기본 탭 (빠른 기록 포함). date: 탐색 탭에서 전달 */
  Today: { date?: string } | undefined;
  Calendar: undefined;
  /** 탐색: 태그 클라우드(월간) + 태그별 엔트리 목록 */
  Tag: undefined;
  Search: undefined;
  Profile: undefined;
};

// ─── 탭 아이콘 (이모지 기반) ────────────────────────────────

function tabIcon(focused: boolean, emoji: string) {
  return (
    <Text style={{ fontSize: 20, opacity: focused ? 1 : 0.45 }}>{emoji}</Text>
  );
}

// ─── 하단 탭 (로그인 후) ──────────────────────────────────

const Tab = createBottomTabNavigator<MainTabParamList>();

function MainTabs() {
  return (
    <Tab.Navigator
      initialRouteName="Today"
      screenOptions={{
        headerShown: false,
        tabBarActiveTintColor: colors.accent,
        tabBarInactiveTintColor: colors.muted,
        tabBarStyle: {
          backgroundColor: colors.background,
          borderTopColor: colors.border
        },
        tabBarLabelStyle: { fontSize: 11, fontWeight: "700" }
      }}
    >
      <Tab.Screen
        name="Today"
        component={TodayScreen}
        options={{ tabBarLabel: "오늘", tabBarIcon: ({ focused }) => tabIcon(focused, "📖") }}
      />
      <Tab.Screen
        name="Calendar"
        component={CalendarScreen}
        options={{ tabBarLabel: "달력", tabBarIcon: ({ focused }) => tabIcon(focused, "📅") }}
      />
      <Tab.Screen
        name="Tag"
        component={TagExploreScreen}
        options={{ tabBarLabel: "태그", tabBarIcon: ({ focused }) => tabIcon(focused, "🏷️") }}
      />
      <Tab.Screen
        name="Search"
        component={SearchScreen}
        options={{ tabBarLabel: "검색", tabBarIcon: ({ focused }) => tabIcon(focused, "🔍") }}
      />
      <Tab.Screen
        name="Profile"
        component={ProfileScreen}
        options={{ tabBarLabel: "나", tabBarIcon: ({ focused }) => tabIcon(focused, "👤") }}
      />
    </Tab.Navigator>
  );
}

// ─── 루트 스택 (인증 분기 + 공유 상세 화면) ──────────────────

const Stack = createNativeStackNavigator<RootStackParamList>();

export function AppNavigator() {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <View style={{ flex: 1, justifyContent: "center", alignItems: "center", backgroundColor: colors.background }}>
        <ActivityIndicator size="large" color={colors.accent} />
      </View>
    );
  }

  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      {isAuthenticated ? (
        <>
          <Stack.Screen name="Main" component={MainTabs} />
          <Stack.Screen
            name="EntryDetail"
            component={EntryDetailScreen}
            options={{ animation: "slide_from_right" }}
          />
          <Stack.Screen
            name="EntryEdit"
            component={EntryEditScreen}
            options={{ animation: "slide_from_right" }}
          />
          <Stack.Screen
            name="InterpretationDetail"
            component={InterpretationScreen}
            options={{ animation: "slide_from_right" }}
          />
          <Stack.Screen
            name="AddEntry"
            component={AddEntryScreen}
            options={{ animation: "slide_from_right" }}
          />
          <Stack.Screen
            name="AiChat"
            component={AIChatScreen}
            options={{ animation: "slide_from_right" }}
          />
        </>
      ) : (
        <Stack.Screen name="Login" component={LoginScreen} />
      )}
    </Stack.Navigator>
  );
}