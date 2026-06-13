import { useCallback, useEffect, useState } from "react";
import {
  ActivityIndicator,
  Alert,
  Pressable,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  View
} from "react-native";
import { useFocusEffect, useNavigation } from "@react-navigation/native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import type { RootStackParamList } from "../navigation/AppNavigator";
import { useAuth } from "../context/AuthContext";
import { getMonthlyJournalDays } from "../api/dreamDiaryApi";
import { colors } from "../theme/colors";
import type { JournalDay } from "../types/journalDay";
import { dreamEntriesFromDay } from "../types/journalDay";

// ─── 통계 집계 유틸 ──────────────────────────────────────────

function currentYearMonth(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
}

interface MonthStats {
  days: number;
  dreams: number;
  diaries: number;
}

/**
 * 월별 JournalDay 목록에서 기록 일수·꿈·일기 카운트를 계산한다.
 * @param monthDays getMonthlyJournalDays 결과 배열
 */
function calcMonthStats(monthDays: JournalDay[]): MonthStats {
  let days = 0, dreams = 0, diaries = 0;
  for (const day of monthDays) {
    // 이 날짜에 엔트리가 하나라도 있으면 기록 일수 +1
    const topDreams = dreamEntriesFromDay(day);
    const chapters  = day.journalChapterList ?? [];
    const hasAny    = topDreams.length > 0 || chapters.some(ch =>
      (ch.journalDiaryList?.length ?? 0) > 0 ||
      (ch.journalDreamList?.length ?? 0) > 0 ||
      (ch.journalNoteList?.length ?? 0) > 0
    );
    if (!hasAny) continue;
    days++;
    dreams  += topDreams.length;
    for (const ch of chapters) {
      dreams  += (ch.journalDreamList?.length ?? 0);
      diaries += (ch.journalDiaryList?.length ?? 0);
    }
  }
  return { days, dreams, diaries };
}

// ─── 메인 화면 ──────────────────────────────────────────────

export function ProfileScreen() {
  const rootNav = useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const { user, logout } = useAuth();
  const [loggingOut, setLoggingOut]     = useState(false);
  const [statsLoading, setStatsLoading] = useState(true);
  const [stats, setStats]               = useState<MonthStats>({ days: 0, dreams: 0, diaries: 0 });

  /** 이번 달 통계 로드 */
  const loadStats = useCallback(async () => {
    setStatsLoading(true);
    try {
      const ym  = currentYearMonth();
      const res = await getMonthlyJournalDays(ym);
      setStats(calcMonthStats(res.rsltList ?? []));
    } catch {
      // 통계 로드 실패 시 조용히 무시 (0 유지)
    } finally {
      setStatsLoading(false);
    }
  }, []);

  // 최초 마운트 시 로드
  useEffect(() => { void loadStats(); }, [loadStats]);

  // 탭 포커스 시 재로드 (오늘 탭 저장 후 복귀 등)
  useFocusEffect(useCallback(() => { void loadStats(); }, [loadStats]));

  function handleLogout() {
    Alert.alert("로그아웃", "정말 로그아웃 하시겠습니까?", [
      { text: "취소", style: "cancel" },
      {
        text: "로그아웃",
        style: "destructive",
        onPress: async () => {
          setLoggingOut(true);
          try {
            await logout();
          } finally {
            setLoggingOut(false);
          }
        }
      }
    ]);
  }

  const ym = currentYearMonth();
  const [year, mo] = ym.split("-");
  const monthLabel = `${year}년 ${Number(mo)}월`;

  return (
    <SafeAreaView style={styles.safeArea}>
      <ScrollView contentContainerStyle={styles.container}>
        {/* 헤더 */}
        <View style={styles.header}>
          <Text style={styles.kicker}>DreamDiary</Text>
          <Text style={styles.title}>내 정보</Text>
        </View>

        {/* 사용자 정보 카드 */}
        {user && (
          <View style={styles.card}>
            <View style={styles.avatarCircle}>
              <Text style={styles.avatarText}>
                {user.nickname.charAt(0).toUpperCase()}
              </Text>
            </View>
            <Text style={styles.nickname}>{user.nickname}</Text>
            <Text style={styles.username}>@{user.username}</Text>
            {user.email ? (
              <Text style={styles.email}>{user.email}</Text>
            ) : null}
          </View>
        )}

        {/* 이번 달 통계 카드 */}
        <View style={styles.statsCard}>
          <Text style={styles.statsTitle}>{monthLabel} 기록</Text>
          {statsLoading ? (
            <ActivityIndicator color={colors.accent} style={styles.statsLoading} />
          ) : (
            <View style={styles.statsRow}>
              <View style={styles.statItem}>
                <Text style={styles.statValue}>{stats.days}</Text>
                <Text style={styles.statLabel}>기록 일수</Text>
              </View>
              <View style={styles.statDivider} />
              <View style={styles.statItem}>
                <Text style={[styles.statValue, styles.statValueDream]}>{stats.dreams}</Text>
                <Text style={styles.statLabel}>꿈 기록</Text>
              </View>
              <View style={styles.statDivider} />
              <View style={styles.statItem}>
                <Text style={styles.statValue}>{stats.diaries}</Text>
                <Text style={styles.statLabel}>일기</Text>
              </View>
            </View>
          )}
        </View>

        <Pressable
          accessibilityRole="button"
          onPress={() => rootNav.navigate("AiChat")}
          style={styles.aiChatButton}
        >
          <Text style={styles.aiChatText}>AI 대화</Text>
        </Pressable>

        {/* 로그아웃 버튼 */}
        <Pressable
          accessibilityRole="button"
          onPress={handleLogout}
          disabled={loggingOut}
          style={[styles.logoutButton, loggingOut && styles.logoutButtonDisabled]}
        >
          {loggingOut ? (
            <ActivityIndicator size="small" color="#fff" />
          ) : (
            <Text style={styles.logoutText}>로그아웃</Text>
          )}
        </Pressable>
      </ScrollView>
    </SafeAreaView>
  );
}

// ─── 스타일 ─────────────────────────────────────────────────

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: colors.background },
  container: { padding: 24, gap: 20 },
  // 헤더
  header: { gap: 2, marginBottom: 4 },
  kicker: {
    color: colors.accent,
    fontSize: 11,
    fontWeight: "700",
    letterSpacing: 1.5,
    textTransform: "uppercase"
  },
  title: { color: colors.text, fontSize: 26, fontWeight: "800" },
  // 사용자 정보 카드
  card: {
    backgroundColor: colors.surface,
    borderRadius: 14,
    padding: 24,
    alignItems: "center",
    gap: 6,
    borderWidth: 1,
    borderColor: colors.border
  },
  avatarCircle: {
    width: 68,
    height: 68,
    borderRadius: 34,
    backgroundColor: colors.accent,
    alignItems: "center",
    justifyContent: "center",
    marginBottom: 8
  },
  avatarText: { color: colors.onAccent, fontSize: 28, fontWeight: "700" },
  nickname: { color: colors.text, fontSize: 20, fontWeight: "700" },
  username: { color: colors.muted, fontSize: 14 },
  email: { color: colors.secondaryText, fontSize: 13, marginTop: 2 },
  // 이번 달 통계 카드
  statsCard: {
    backgroundColor: colors.surface,
    borderRadius: 14,
    padding: 20,
    gap: 16,
    borderWidth: 1,
    borderColor: colors.border
  },
  statsTitle: {
    color: colors.text,
    fontSize: 14,
    fontWeight: "700"
  },
  statsLoading: { paddingVertical: 12 },
  statsRow: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-around"
  },
  statItem: {
    flex: 1,
    alignItems: "center",
    gap: 4
  },
  statValue: {
    color: colors.text,
    fontSize: 28,
    fontWeight: "800"
  },
  statValueDream: { color: "#8E44AD" },
  statLabel: {
    color: colors.muted,
    fontSize: 12,
    fontWeight: "600"
  },
  statDivider: {
    width: 1,
    height: 40,
    backgroundColor: colors.border
  },
  // 로그아웃 버튼
  logoutButton: {
    backgroundColor: "#C0392B",
    borderRadius: 10,
    paddingVertical: 14,
    alignItems: "center",
    justifyContent: "center"
  },
  aiChatButton: {
    backgroundColor: colors.accent,
    borderRadius: 10,
    paddingVertical: 14,
    alignItems: "center",
    justifyContent: "center"
  },
  aiChatText: { color: colors.onAccent, fontSize: 16, fontWeight: "700" },
  logoutButtonDisabled: { opacity: 0.6 },
  logoutText: { color: "#fff", fontSize: 16, fontWeight: "700" }
});