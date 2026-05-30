import { useCallback, useEffect, useMemo, useState } from "react";
import {
  ActivityIndicator,
  Pressable,
  RefreshControl,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  View
} from "react-native";
import { useFocusEffect, useNavigation } from "@react-navigation/native";
import type { BottomTabNavigationProp } from "@react-navigation/bottom-tabs";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import {
  getJournalDayTags,
  getJournalDayTagYears,
  getJournalEntryTags,
  searchEntries,
  searchJournalDaysByTag,
  type TagCloudItem
} from "../api/dreamDiaryApi";
import { colors } from "../theme/colors";
import { formatDateDots, normalizeDateStr } from "../utils/date";
import { stripHtml } from "../utils/text";
import type { MainTabParamList, RootStackParamList } from "../navigation/AppNavigator";
import { navigateToDailyHub } from "../navigation/dailyHub";
import type { JournalDay, JournalEntry } from "../types/journalDay";

type TabNav = BottomTabNavigationProp<MainTabParamList, "Tag">;
type StackNav = NativeStackNavigationProp<RootStackParamList>;

const MONTH_NAMES = ["1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월"];

function currentYearMonth(): { yy: number; mnth: number } {
  const now = new Date();
  return { yy: now.getFullYear(), mnth: now.getMonth() + 1 };
}

function shiftMonth(yy: number, mnth: number, delta: number): { yy: number; mnth: number } {
  const d = new Date(yy, mnth - 1 + delta, 1);
  return { yy: d.getFullYear(), mnth: d.getMonth() + 1 };
}

function isFutureMonth(yy: number, mnth: number): boolean {
  const now = currentYearMonth();
  const key = `${yy}-${String(mnth).padStart(2, "0")}`;
  const nowKey = `${now.yy}-${String(now.mnth).padStart(2, "0")}`;
  return key > nowKey;
}

type TagSection = {
  id: string;
  label: string;
  tags: TagCloudItem[];
  entryType?: "DIARY" | "DREAM";
};

function TagChip({
  tag,
  selected,
  onPress
}: {
  tag: TagCloudItem;
  selected: boolean;
  onPress: () => void;
}) {
  const label = tag.ctgr ? `[${tag.ctgr}] ${tag.name}` : tag.name;
  return (
    <Pressable
      accessibilityRole="button"
      onPress={onPress}
      style={[styles.chip, selected && styles.chipSelected]}
    >
      <Text style={[styles.chipText, selected && styles.chipTextSelected]} numberOfLines={1}>
        {label}
      </Text>
      <Text style={[styles.chipCount, selected && styles.chipTextSelected]}>{tag.contentSize}</Text>
    </Pressable>
  );
}

function ResultCard({ entry }: { entry: JournalEntry }) {
  const navigation = useNavigation<StackNav>();
  const isDream = entry.contentType === "JOURNAL_DREAM";
  const preview = entry.content ? stripHtml(entry.content).slice(0, 120) : "";

  return (
    <Pressable
      accessibilityRole="button"
      onPress={() => navigation.navigate("EntryDetail", { entry, isDream })}
      style={[styles.resultCard, isDream && styles.resultCardDream]}
    >
      {entry.stdrdDt ? (
        <Text style={styles.resultDate}>{entry.stdrdDt.replace(/-/g, ".")}</Text>
      ) : null}
      <Text style={[styles.resultBody, isDream && styles.resultBodyDream]} numberOfLines={3}>
        {preview || "(내용 없음)"}
      </Text>
    </Pressable>
  );
}

function DayResultRow({ day }: { day: JournalDay }) {
  const navigation = useNavigation<TabNav>();
  const date = normalizeDateStr(day.stdrdDt) ?? day.stdrdDt;

  return (
    <Pressable
      accessibilityRole="button"
      onPress={() => navigateToDailyHub(navigation, date)}
      style={styles.dayResultRow}
    >
      <Text style={styles.dayResultDate}>{formatDateDots(date)}</Text>
      <Text style={styles.dayResultMore}>오늘 탭에서 보기 →</Text>
    </Pressable>
  );
}

export function TagExploreScreen() {
  const [{ yy, mnth }, setYearMonth] = useState(currentYearMonth);
  const [sections, setSections] = useState<TagSection[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [selectedTag, setSelectedTag] = useState<TagCloudItem | null>(null);
  const [resultMode, setResultMode] = useState<"day" | "entry" | null>(null);
  const [selectedEntryType, setSelectedEntryType] = useState<"DIARY" | "DREAM" | undefined>();
  const [results, setResults] = useState<JournalEntry[]>([]);
  const [dayResults, setDayResults] = useState<JournalDay[]>([]);
  const [dayTagYears, setDayTagYears] = useState<number[]>([]);
  const [dayTagYear, setDayTagYear] = useState<number>(() => new Date().getFullYear());
  const [resultsLoading, setResultsLoading] = useState(false);
  const [resultError, setResultError] = useState<string | null>(null);

  const futureMonth = isFutureMonth(yy, mnth);
  const dayTagYearMin = dayTagYears.length > 0 ? Math.min(...dayTagYears) : null;
  const dayTagYearMax = dayTagYears.length > 0 ? Math.max(...dayTagYears) : null;

  const loadTags = useCallback(async (isRefresh = false) => {
    if (!isRefresh) setLoading(true);
    setError(null);
    try {
      const [dayTags, diaryTags, dreamTags] = await Promise.all([
        getJournalDayTags(yy, mnth),
        getJournalEntryTags(yy, mnth, "DIARY"),
        getJournalEntryTags(yy, mnth, "DREAM")
      ]);
      setSections([
        { id: "day", label: "일자 태그", tags: dayTags },
        { id: "diary", label: "일기 태그", tags: diaryTags, entryType: "DIARY" },
        { id: "dream", label: "꿈 태그", tags: dreamTags, entryType: "DREAM" }
      ]);
    } catch {
      setError("태그 목록을 불러오지 못했습니다.");
      setSections([]);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [yy, mnth]);

  useEffect(() => {
    setSelectedTag(null);
    setResultMode(null);
    setResults([]);
    setDayResults([]);
    setDayTagYears([]);
    void loadTags();
  }, [loadTags]);

  const loadDayTagResults = useCallback(async (tagId: number, year: number) => {
    setResultsLoading(true);
    setResultError(null);
    setDayResults([]);
    try {
      const res = await searchJournalDaysByTag(tagId, year);
      setDayResults(res.rsltList ?? []);
    } catch {
      setDayResults([]);
      setResultError("일자 결과를 불러오지 못했습니다.");
    } finally {
      setResultsLoading(false);
    }
  }, []);

  const loadEntryTagResults = useCallback(async (tagId: number, type: "DIARY" | "DREAM") => {
    setResultsLoading(true);
    setResultError(null);
    setResults([]);
    try {
      const res = await searchEntries({ tagIds: [tagId], type });
      setResults(res.rsltList ?? []);
    } catch {
      setResults([]);
      setResultError("엔트리 결과를 불러오지 못했습니다.");
    } finally {
      setResultsLoading(false);
    }
  }, []);

  useEffect(() => {
    if (resultMode !== "day" || selectedTag == null) return;
    const tagId = Number(selectedTag.id);
    if (!Number.isFinite(tagId)) return;
    void loadDayTagResults(tagId, dayTagYear);
  }, [dayTagYear, loadDayTagResults, resultMode, selectedTag]);

  useFocusEffect(
    useCallback(() => {
      void loadTags(true);
      if (selectedTag == null) return;
      const tagId = Number(selectedTag.id);
      if (!Number.isFinite(tagId)) return;
      if (resultMode === "day") {
        void loadDayTagResults(tagId, dayTagYear);
        return;
      }
      if (resultMode === "entry" && selectedEntryType) {
        void loadEntryTagResults(tagId, selectedEntryType);
      }
    }, [dayTagYear, loadDayTagResults, loadEntryTagResults, loadTags, resultMode, selectedEntryType, selectedTag])
  );

  const selectedLabel = useMemo(() => {
    if (!selectedTag) return null;
    return selectedTag.ctgr
      ? `[${selectedTag.ctgr}] ${selectedTag.name}`
      : selectedTag.name;
  }, [selectedTag]);

  async function onTagPress(tag: TagCloudItem, section: TagSection) {
    const tagId = Number(tag.id);
    if (!Number.isFinite(tagId)) return;

    setSelectedTag(tag);
    setResultsLoading(true);
    setResultError(null);
    setResults([]);
    setDayResults([]);

    if (section.id === "day") {
      setResultMode("day");
      setSelectedEntryType(undefined);
      setDayResults([]);
      try {
        const yearsRes = await getJournalDayTagYears(tagId);
        const years = (yearsRes.rsltList ?? [])
          .map((y) => Number(y))
          .filter((y) => Number.isFinite(y))
          .sort((a, b) => b - a);
        setDayTagYears(years);
        setDayTagYear(years.includes(yy) ? yy : (years[0] ?? yy));
      } catch {
        setDayTagYears([]);
        setDayTagYear(yy);
      }
      return;
    }

    if (!section.entryType) return;

    setResultMode("entry");
    setSelectedEntryType(section.entryType);
    await loadEntryTagResults(tagId, section.entryType);
  }

  function onRefresh() {
    setRefreshing(true);
    void loadTags(true);
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
          <Text style={styles.title}>태그</Text>
          <Text style={styles.subtitle}>이번 달 태그로 기록을 찾습니다</Text>
        </View>

        <View style={styles.monthNav}>
          <Pressable
            accessibilityRole="button"
            onPress={() => setYearMonth((p) => shiftMonth(p.yy, p.mnth, -1))}
            style={styles.monthNavBtn}
          >
            <Text style={styles.monthNavArrow}>‹</Text>
          </Pressable>
          <Text style={styles.monthLabel}>
            {yy}년 {MONTH_NAMES[mnth - 1]}
          </Text>
          <Pressable
            accessibilityRole="button"
            disabled={futureMonth}
            onPress={() => setYearMonth((p) => shiftMonth(p.yy, p.mnth, 1))}
            style={[styles.monthNavBtn, futureMonth && styles.monthNavBtnDisabled]}
          >
            <Text style={[styles.monthNavArrow, futureMonth && styles.monthNavArrowDisabled]}>›</Text>
          </Pressable>
        </View>

        {loading && (
          <View style={styles.center}>
            <ActivityIndicator size="large" color={colors.accent} />
          </View>
        )}

        {!loading && error != null && (
          <View style={styles.center}>
            <Text style={styles.errorText}>{error}</Text>
          </View>
        )}

        {!loading && !error && sections.map((section) => (
          <View key={section.id} style={styles.section}>
            <Text style={styles.sectionLabel}>{section.label}</Text>
            {section.tags.length === 0 ? (
              <Text style={styles.emptySection}>이번 달 태그가 없습니다.</Text>
            ) : (
              <View style={styles.chipRow}>
                {section.tags.map((tag) => (
                  <TagChip
                    key={`${section.id}-${tag.id}`}
                    tag={tag}
                    selected={
                      selectedTag?.id === tag.id
                      && (
                        (section.id === "day" && resultMode === "day")
                        || (section.entryType === selectedEntryType && resultMode === "entry")
                      )
                    }
                    onPress={() => { void onTagPress(tag, section); }}
                  />
                ))}
              </View>
            )}
          </View>
        ))}

        {selectedTag && resultMode === "day" && (
          <View style={styles.resultsBlock}>
            <View style={styles.dayResultsHeader}>
              <Text style={styles.resultsTitle}>
                「{selectedLabel}」 일자 목록 ({dayResults.length}건)
              </Text>
              <View style={styles.yearNav}>
                <Pressable
                  accessibilityRole="button"
                  accessibilityLabel="이전 연도"
                  disabled={dayTagYearMin == null || dayTagYear <= dayTagYearMin}
                  onPress={() => {
                    const idx = dayTagYears.indexOf(dayTagYear);
                    if (idx >= 0 && idx < dayTagYears.length - 1) {
                      setDayTagYear(dayTagYears[idx + 1]);
                    }
                  }}
                  style={[
                    styles.yearNavBtn,
                    (dayTagYearMin == null || dayTagYear <= dayTagYearMin) && styles.yearNavBtnDisabled
                  ]}
                >
                  <Text style={styles.yearNavArrow}>‹</Text>
                </Pressable>
                <Text style={styles.yearLabel}>{dayTagYear}년</Text>
                <Pressable
                  accessibilityRole="button"
                  accessibilityLabel="다음 연도"
                  disabled={dayTagYearMax == null || dayTagYear >= dayTagYearMax}
                  onPress={() => {
                    const idx = dayTagYears.indexOf(dayTagYear);
                    if (idx > 0) {
                      setDayTagYear(dayTagYears[idx - 1]);
                    }
                  }}
                  style={[
                    styles.yearNavBtn,
                    (dayTagYearMax == null || dayTagYear >= dayTagYearMax) && styles.yearNavBtnDisabled
                  ]}
                >
                  <Text style={styles.yearNavArrow}>›</Text>
                </Pressable>
              </View>
            </View>
            {resultsLoading ? (
              <ActivityIndicator color={colors.accent} style={{ marginVertical: 16 }} />
            ) : resultError ? (
              <View style={styles.resultStateBlock}>
                <Text style={styles.errorText}>{resultError}</Text>
                <Pressable
                  accessibilityRole="button"
                  onPress={() => {
                    if (!selectedTag) return;
                    const tagId = Number(selectedTag.id);
                    if (!Number.isFinite(tagId)) return;
                    void loadDayTagResults(tagId, dayTagYear);
                  }}
                  style={styles.retryBtn}
                >
                  <Text style={styles.retryBtnText}>다시 시도</Text>
                </Pressable>
              </View>
            ) : dayResults.length === 0 ? (
              <Text style={styles.emptySection}>해당 태그가 붙은 일자가 없습니다.</Text>
            ) : (
              dayResults.map((day) => (
                <DayResultRow key={`${day.id}-${day.stdrdDt}`} day={day} />
              ))
            )}
          </View>
        )}

        {selectedTag && resultMode === "entry" && selectedEntryType && (
          <View style={styles.resultsBlock}>
            <Text style={styles.resultsTitle}>
              「{selectedLabel}」 검색 결과 ({results.length}건)
            </Text>
            {resultsLoading ? (
              <ActivityIndicator color={colors.accent} style={{ marginVertical: 16 }} />
            ) : resultError ? (
              <View style={styles.resultStateBlock}>
                <Text style={styles.errorText}>{resultError}</Text>
                <Pressable
                  accessibilityRole="button"
                  onPress={() => {
                    if (!selectedTag || !selectedEntryType) return;
                    const tagId = Number(selectedTag.id);
                    if (!Number.isFinite(tagId)) return;
                    void loadEntryTagResults(tagId, selectedEntryType);
                  }}
                  style={styles.retryBtn}
                >
                  <Text style={styles.retryBtnText}>다시 시도</Text>
                </Pressable>
              </View>
            ) : results.length === 0 ? (
              <Text style={styles.emptySection}>검색 결과가 없습니다.</Text>
            ) : (
              results.map((entry) => <ResultCard key={entry.id} entry={entry} />)
            )}
          </View>
        )}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: colors.background },
  container: { flexGrow: 1, padding: 20, gap: 16, paddingBottom: 32 },
  header: { paddingTop: 8, gap: 4 },
  kicker: {
    color: colors.accent,
    fontSize: 12,
    fontWeight: "700",
    letterSpacing: 0.4,
    textTransform: "uppercase"
  },
  title: { color: colors.text, fontSize: 26, fontWeight: "800" },
  subtitle: { color: colors.muted, fontSize: 13 },
  monthNav: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    paddingVertical: 4
  },
  monthNavBtn: { paddingHorizontal: 12, paddingVertical: 6 },
  monthNavBtnDisabled: { opacity: 0.3 },
  monthNavArrow: { fontSize: 28, color: colors.accent, lineHeight: 32, fontWeight: "300" },
  monthNavArrowDisabled: { color: colors.muted },
  monthLabel: { color: colors.text, fontSize: 17, fontWeight: "700" },
  center: { alignItems: "center", paddingVertical: 32 },
  errorText: { color: "#C0392B", fontSize: 14 },
  section: { gap: 8 },
  sectionLabel: {
    color: colors.secondaryText,
    fontSize: 12,
    fontWeight: "700",
    textTransform: "uppercase",
    letterSpacing: 0.3
  },
  emptySection: { color: colors.muted, fontSize: 14 },
  chipRow: { flexDirection: "row", flexWrap: "wrap", gap: 8 },
  chip: {
    flexDirection: "row",
    alignItems: "center",
    gap: 6,
    maxWidth: "100%",
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 16,
    backgroundColor: colors.surface,
    borderWidth: 1,
    borderColor: colors.border
  },
  chipSelected: {
    backgroundColor: colors.accent,
    borderColor: colors.accent
  },
  chipText: { color: colors.text, fontSize: 13, fontWeight: "600", flexShrink: 1 },
  chipTextSelected: { color: colors.onAccent },
  chipCount: { color: colors.muted, fontSize: 11, fontWeight: "700" },
  resultsBlock: { gap: 10, marginTop: 8, paddingTop: 12, borderTopWidth: 1, borderTopColor: colors.border },
  dayResultsHeader: { gap: 8 },
  resultsTitle: { color: colors.text, fontSize: 15, fontWeight: "700" },
  yearNav: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    gap: 8
  },
  yearNavBtn: { paddingHorizontal: 10, paddingVertical: 4 },
  yearNavBtnDisabled: { opacity: 0.3 },
  yearNavArrow: { fontSize: 22, color: colors.accent, lineHeight: 26, fontWeight: "300" },
  yearLabel: { color: colors.text, fontSize: 15, fontWeight: "700", minWidth: 64, textAlign: "center" },
  resultCard: {
    backgroundColor: colors.surface,
    borderRadius: 10,
    padding: 12,
    gap: 4,
    borderLeftWidth: 3,
    borderLeftColor: colors.border
  },
  resultCardDream: { backgroundColor: "#F5EEF8", borderLeftColor: "#8E44AD" },
  resultDate: { color: colors.muted, fontSize: 12, fontWeight: "600" },
  resultBody: { color: colors.secondaryText, fontSize: 14, lineHeight: 20 },
  resultBodyDream: { color: "#6C3483" },
  dayResultRow: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    backgroundColor: colors.surface,
    borderRadius: 10,
    paddingHorizontal: 14,
    paddingVertical: 12,
    borderWidth: 1,
    borderColor: colors.border
  },
  dayResultDate: { color: colors.text, fontSize: 15, fontWeight: "700" },
  dayResultMore: { color: colors.accent, fontSize: 13, fontWeight: "600" },
  resultStateBlock: { gap: 8, paddingVertical: 6, alignItems: "flex-start" },
  retryBtn: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.surface
  },
  retryBtnText: { color: colors.accent, fontSize: 13, fontWeight: "700" }
});
