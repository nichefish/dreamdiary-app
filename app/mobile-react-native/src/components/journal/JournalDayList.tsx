import {
  ActivityIndicator,
  Platform,
  Pressable,
  StyleSheet,
  Text,
  View
} from "react-native";
import { useNavigation } from "@react-navigation/native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import { colors } from "../../theme/colors";
import { stripHtml } from "../../utils/text";
import type { RootStackParamList } from "../../navigation/AppNavigator";
import type { JournalChapter, JournalEntry } from "../../types/journalDay";

type Nav = NativeStackNavigationProp<RootStackParamList>;

function EntryCard({ entry, isDream }: { entry: JournalEntry; isDream: boolean }) {
  const navigation = useNavigation<Nav>();
  const preview = entry.content ? stripHtml(entry.content).slice(0, 160) : "";
  const hasMore = entry.content ? stripHtml(entry.content).length > 160 : false;

  return (
    <Pressable
      accessibilityRole="button"
      onPress={() => navigation.navigate("EntryDetail", { entry, isDream })}
      style={[styles.entryCard, isDream && styles.entryCardDream]}
    >
      {entry.title ? (
        <Text style={[styles.entryTitle, isDream && styles.entryTitleDream]} numberOfLines={1}>
          {entry.title}
        </Text>
      ) : null}
      <Text style={[styles.entryContent, isDream && styles.entryContentDream]} numberOfLines={4}>
        {preview || "(내용 없음)"}
      </Text>
      {hasMore && (
        <Text style={[styles.entryMore, isDream && styles.entryMoreDream]}>더 보기 →</Text>
      )}
    </Pressable>
  );
}

function ChapterSection({ chapter }: { chapter: JournalChapter }) {
  const diaries = chapter.journalDiaryList ?? [];
  const dreams = chapter.journalDreamList ?? [];
  const notes = chapter.journalNoteList ?? [];
  const allEntries = [
    ...diaries.map((e) => ({ entry: e, isDream: false })),
    ...dreams.map((e) => ({ entry: e, isDream: true })),
    ...notes.map((e) => ({ entry: e, isDream: false }))
  ];
  if (allEntries.length === 0) return null;

  const label = chapter.categoryName
    ? `[${chapter.categoryName}] ${chapter.title ?? ""}`.trim()
    : chapter.title ?? `챕터 ${chapter.sortOrder ?? ""}`.trim();

  return (
    <View style={styles.chapterSection}>
      <Text style={styles.chapterLabel}>{label}</Text>
      {allEntries.map(({ entry, isDream }) => (
        <EntryCard key={entry.id} entry={entry} isDream={isDream} />
      ))}
    </View>
  );
}

export type JournalDayListProps = {
  loading: boolean;
  error: string | null;
  hasAny: boolean;
  chapters: JournalChapter[];
  topDreams: JournalEntry[];
  /** 빈 목록 문구 */
  emptyText: string;
  /** 빈 목록 보조 힌트 (선택) */
  emptyHint?: string;
  /** center 영역 paddingVertical — 탭/스택 레이아웃 차이 보정 */
  centerPaddingVertical?: number;
};

/**
 * DAILY API 결과의 로딩·오류·빈 상태·챕터/엔트리 목록.
 * TodayScreen 공통 렌더 축.
 */
export function JournalDayList({
  loading,
  error,
  hasAny,
  chapters,
  topDreams,
  emptyText,
  emptyHint,
  centerPaddingVertical = 48
}: JournalDayListProps) {
  if (loading) {
    return (
      <View style={[styles.center, { paddingVertical: centerPaddingVertical }]}>
        <ActivityIndicator size="large" color={colors.accent} />
      </View>
    );
  }

  if (error != null) {
    return (
      <View style={[styles.center, { paddingVertical: centerPaddingVertical }]}>
        <Text style={styles.errorText}>{error}</Text>
      </View>
    );
  }

  if (!hasAny) {
    return (
      <View style={[styles.center, { paddingVertical: centerPaddingVertical }]}>
        <Text style={styles.emptyText}>{emptyText}</Text>
        {emptyHint != null && <Text style={styles.emptyHint}>{emptyHint}</Text>}
      </View>
    );
  }

  return (
    <View style={styles.content}>
      {chapters.map((ch) => (
        <ChapterSection key={ch.id} chapter={ch} />
      ))}
      {topDreams.map((e) => (
        <EntryCard key={e.id} entry={e} isDream />
      ))}
    </View>
  );
}

export type AddEntryFabProps = {
  date: string;
};

/** 선택 일자 기준 AddEntry push FAB */
export function AddEntryFab({ date }: AddEntryFabProps) {
  const navigation = useNavigation<Nav>();

  return (
    <Pressable
      accessibilityRole="button"
      accessibilityLabel="기록 추가"
      onPress={() => navigation.navigate("AddEntry", { date })}
      style={styles.fab}
    >
      <Text style={styles.fabText}>+</Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  center: { flex: 1, alignItems: "center", justifyContent: "center", gap: 8 },
  errorText: { color: "#C0392B", fontSize: 14 },
  emptyText: { color: colors.secondaryText, fontSize: 16, fontWeight: "600" },
  emptyHint: { color: colors.muted, fontSize: 13 },
  content: { gap: 16 },
  chapterSection: { gap: 8 },
  chapterLabel: {
    color: colors.secondaryText,
    fontSize: 12,
    fontWeight: "700",
    textTransform: "uppercase",
    letterSpacing: 0.3
  },
  entryCard: {
    backgroundColor: colors.surface,
    borderRadius: 10,
    padding: 14,
    gap: 4,
    borderLeftWidth: 3,
    borderLeftColor: colors.border
  },
  entryCardDream: {
    backgroundColor: "#F5EEF8",
    borderLeftColor: "#8E44AD"
  },
  entryTitle: {
    color: colors.text,
    fontSize: 14,
    fontWeight: "700"
  },
  entryTitleDream: { color: "#5B2C6F" },
  entryContent: {
    color: colors.secondaryText,
    fontSize: 14,
    lineHeight: 20
  },
  entryContentDream: { color: "#6C3483" },
  entryMore: {
    color: colors.accent,
    fontSize: 12,
    fontWeight: "600",
    marginTop: 2
  },
  entryMoreDream: { color: "#8E44AD" },
  fab: {
    position: "absolute",
    bottom: Platform.select({ ios: 32, android: 24 }),
    right: 24,
    width: 52,
    height: 52,
    borderRadius: 26,
    backgroundColor: colors.accent,
    alignItems: "center",
    justifyContent: "center",
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 3 },
    shadowOpacity: 0.25,
    shadowRadius: 6,
    elevation: 6
  },
  fabText: { color: colors.onAccent, fontSize: 28, lineHeight: 34, fontWeight: "300" }
});
