import { useCallback, useState } from "react";
import {
  ActivityIndicator,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View
} from "react-native";
import { useFocusEffect, useNavigation } from "@react-navigation/native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import { searchEntries } from "../api/dreamDiaryApi";
import { colors } from "../theme/colors";
import { stripHtml } from "../utils/text";
import type { RootStackParamList } from "../navigation/AppNavigator";
import type { JournalEntry } from "../types/journalDay";

// ─── 타입 ───────────────────────────────────────────────────

type Nav = NativeStackNavigationProp<RootStackParamList>;

/** 검색 타입 필터: 전체 / 꿈 / 일기 */
type FilterType = "ALL" | "DREAM" | "DIARY";

const FILTERS: Array<{ id: FilterType; label: string }> = [
  { id: "ALL", label: "전체" },
  { id: "DREAM", label: "꿈" },
  { id: "DIARY", label: "일기" }
];

// ─── 하위 컴포넌트 ──────────────────────────────────────────

function EntryCard({ entry }: { entry: JournalEntry }) {
  const navigation = useNavigation<Nav>();
  const isDream = entry.contentType === "JOURNAL_DREAM";
  const plain = entry.content ? stripHtml(entry.content) : "";
  const preview = plain.slice(0, 140);
  const hasMore = plain.length > 140;
  // stdrdDt가 있으면 YYYY.MM.DD 형태로 변환해 표시
  const dateLabel = entry.stdrdDt ? entry.stdrdDt.replace(/-/g, ".") : null;

  return (
    <Pressable
      accessibilityRole="button"
      onPress={() => navigation.navigate("EntryDetail", { entry, isDream })}
      style={[styles.entryCard, isDream && styles.entryCardDream]}
    >
      <View style={styles.cardMeta}>
        <View style={[styles.typeBadge, isDream && styles.typeBadgeDream]}>
          <Text style={[styles.typeBadgeText, isDream && styles.typeBadgeTextDream]}>
            {isDream ? "꿈" : "일기"}
          </Text>
        </View>
        {dateLabel ? (
          <Text style={styles.dateLabel}>{dateLabel}</Text>
        ) : null}
      </View>
      {entry.title ? (
        <Text style={[styles.entryTitle, isDream && styles.entryTitleDream]} numberOfLines={1}>
          {entry.title}
        </Text>
      ) : null}
      <Text style={[styles.entryContent, isDream && styles.entryContentDream]} numberOfLines={3}>
        {preview || "(내용 없음)"}
      </Text>
      {hasMore && (
        <Text style={[styles.entryMore, isDream && styles.entryMoreDream]}>더 보기 →</Text>
      )}
    </Pressable>
  );
}

// ─── 메인 화면 ──────────────────────────────────────────────

export function SearchScreen() {
  const [keyword, setKeyword] = useState("");
  const [filterType, setFilterType] = useState<FilterType>("ALL");
  const [results, setResults] = useState<JournalEntry[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  /** 최초 검색 실행 여부 — 빈 결과와 미검색 상태를 구별 */
  const [searched, setSearched] = useState(false);

  const doSearch = useCallback(async (kw: string, ft: FilterType) => {
    const trimmed = kw.trim();
    if (trimmed.length === 0) return;

    setLoading(true);
    setError(null);
    setSearched(true);

    try {
      const type = ft === "ALL" ? undefined : ft;
      const res = await searchEntries({ keyword: trimmed, type });
      setResults(res.rsltList ?? []);
    } catch {
      setError("검색 중 오류가 발생했습니다.");
      setResults([]);
    } finally {
      setLoading(false);
    }
  }, []);

  /** 필터 변경 시 기존 검색어가 있으면 즉시 재검색 */
  function handleFilterChange(ft: FilterType) {
    setFilterType(ft);
    if (keyword.trim().length > 0 && searched) {
      void doSearch(keyword, ft);
    }
  }

  useFocusEffect(
    useCallback(() => {
      if (searched && keyword.trim().length > 0) {
        void doSearch(keyword, filterType);
      }
    }, [doSearch, filterType, keyword, searched])
  );

  return (
    <SafeAreaView style={styles.safeArea}>
      {/* 키보드가 올라올 때 검색 입력 영역이 가려지지 않게 iOS/Android 모두 대응 */}
      <KeyboardAvoidingView
        behavior={Platform.select({ ios: "padding", android: undefined })}
        style={styles.keyboardArea}
      >
      <ScrollView
        contentContainerStyle={styles.container}
        keyboardShouldPersistTaps="handled"
      >
        {/* 헤더 */}
        <View style={styles.header}>
          <Text style={styles.kicker}>DreamDiary</Text>
          <Text style={styles.title}>검색</Text>
        </View>

        {/* 검색 입력 */}
        <View style={styles.searchRow}>
          <TextInput
            autoCapitalize="none"
            autoCorrect={false}
            onChangeText={setKeyword}
            onSubmitEditing={() => { void doSearch(keyword, filterType); }}
            placeholder="키워드를 입력하세요"
            placeholderTextColor={colors.muted}
            returnKeyType="search"
            style={styles.searchInput}
            value={keyword}
          />
          <Pressable
            accessibilityRole="button"
            disabled={keyword.trim().length === 0 || loading}
            onPress={() => { void doSearch(keyword, filterType); }}
            style={[
              styles.searchButton,
              (keyword.trim().length === 0 || loading) && styles.searchButtonDisabled
            ]}
          >
            {loading
              ? <ActivityIndicator size="small" color={colors.onAccent} />
              : <Text style={styles.searchButtonText}>검색</Text>
            }
          </Pressable>
        </View>

        {/* 타입 필터 */}
        <View style={styles.filterRow}>
          {FILTERS.map(f => {
            const active = f.id === filterType;
            return (
              <Pressable
                accessibilityRole="button"
                accessibilityState={{ selected: active }}
                key={f.id}
                onPress={() => handleFilterChange(f.id)}
                style={[styles.filterChip, active && styles.filterChipActive]}
              >
                <Text style={[styles.filterChipText, active && styles.filterChipTextActive]}>
                  {f.label}
                </Text>
              </Pressable>
            );
          })}
        </View>

        {/* 결과 영역 */}
        {!loading && error != null && (
          <View style={styles.center}>
            <Text style={styles.errorText}>{error}</Text>
          </View>
        )}

        {!loading && !error && searched && results.length === 0 && (
          <View style={styles.center}>
            <Text style={styles.emptyText}>검색 결과가 없습니다.</Text>
          </View>
        )}

        {!loading && !error && results.length > 0 && (
          <View style={styles.resultList}>
            <Text style={styles.resultCount}>{results.length}건</Text>
            {results.map(entry => (
              <EntryCard key={entry.id} entry={entry} />
            ))}
          </View>
        )}
      </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

// ─── 스타일 ─────────────────────────────────────────────────

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: colors.background },
  keyboardArea: { flex: 1 },
  container: { padding: 20, gap: 16, flexGrow: 1 },
  // 헤더
  header: { gap: 2 },
  kicker: {
    color: colors.accent,
    fontSize: 11,
    fontWeight: "700",
    letterSpacing: 1.5,
    textTransform: "uppercase"
  },
  title: { color: colors.text, fontSize: 26, fontWeight: "800" },
  // 검색 입력행
  searchRow: { flexDirection: "row", gap: 8, alignItems: "center" },
  searchInput: {
    flex: 1,
    backgroundColor: colors.input,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: colors.border,
    paddingHorizontal: 14,
    paddingVertical: 10,
    color: colors.text,
    fontSize: 15
  },
  searchButton: {
    backgroundColor: colors.accent,
    borderRadius: 8,
    paddingHorizontal: 16,
    paddingVertical: 10,
    minWidth: 60,
    alignItems: "center",
    justifyContent: "center"
  },
  searchButtonDisabled: { opacity: 0.45 },
  searchButtonText: { color: colors.onAccent, fontSize: 15, fontWeight: "700" },
  // 타입 필터
  filterRow: { flexDirection: "row", gap: 8 },
  filterChip: {
    paddingHorizontal: 14,
    paddingVertical: 6,
    borderRadius: 20,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.surface
  },
  filterChipActive: { backgroundColor: colors.accent, borderColor: colors.accent },
  filterChipText: { color: colors.secondaryText, fontSize: 13, fontWeight: "600" },
  filterChipTextActive: { color: colors.onAccent },
  // 결과 목록
  resultList: { gap: 10 },
  resultCount: {
    color: colors.muted,
    fontSize: 12,
    fontWeight: "700",
    textTransform: "uppercase",
    letterSpacing: 0.5
  },
  // 상태
  center: { paddingVertical: 40, alignItems: "center" },
  errorText: { color: "#C0392B", fontSize: 14 },
  emptyText: { color: colors.secondaryText, fontSize: 15 },
  // 엔트리 카드
  entryCard: {
    backgroundColor: colors.surface,
    borderRadius: 10,
    padding: 14,
    gap: 6,
    borderLeftWidth: 3,
    borderLeftColor: colors.border
  },
  entryCardDream: { backgroundColor: "#F5EEF8", borderLeftColor: "#8E44AD" },
  cardMeta: { flexDirection: "row", alignItems: "center", gap: 8 },
  typeBadge: {
    paddingHorizontal: 8,
    paddingVertical: 2,
    borderRadius: 10,
    backgroundColor: colors.border
  },
  typeBadgeDream: { backgroundColor: "#E8DAEF" },
  typeBadgeText: { color: colors.secondaryText, fontSize: 11, fontWeight: "700" },
  typeBadgeTextDream: { color: "#6C3483" },
  dateLabel: { color: colors.muted, fontSize: 11 },
  entryTitle: { color: colors.text, fontSize: 14, fontWeight: "700" },
  entryTitleDream: { color: "#5B2C6F" },
  entryContent: { color: colors.secondaryText, fontSize: 14, lineHeight: 20 },
  entryContentDream: { color: "#6C3483" },
  entryMore: { color: colors.accent, fontSize: 12, fontWeight: "600" },
  entryMoreDream: { color: "#8E44AD" }
});