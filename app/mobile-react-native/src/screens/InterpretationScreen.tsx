import { useCallback, useEffect, useRef, useState } from "react";
import {
  ActivityIndicator,
  Alert,
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
import type { NativeStackScreenProps } from "@react-navigation/native-stack";
import {
  createInterpretation,
  deleteInterpretation,
  getInterpretations
} from "../api/dreamDiaryApi";
import { colors } from "../theme/colors";
import { stripHtml } from "../utils/text";
import type { RootStackParamList } from "../navigation/AppNavigator";
import type { JournalInterpretation } from "../types/interpretation";

// ─── 타입 ───────────────────────────────────────────────────

type Props = NativeStackScreenProps<RootStackParamList, "InterpretationDetail">;

// ─── 하위 컴포넌트 ──────────────────────────────────────────

interface InterpretationCardProps {
  item: JournalInterpretation;
  onDelete: (id: number) => void;
  deleting: boolean;
}

function InterpretationCard({ item, onDelete, deleting }: InterpretationCardProps) {
  const body = item.content ? stripHtml(item.content) : "";

  function handleDelete() {
    Alert.alert("해석 삭제", "이 해석을 삭제할까요? 되돌릴 수 없습니다.", [
      { text: "취소", style: "cancel" },
      { text: "삭제", style: "destructive", onPress: () => onDelete(item.id) }
    ]);
  }

  return (
    <View style={styles.interpretCard}>
      <Text style={styles.interpretBody}>{body || "(내용 없음)"}</Text>
      <Pressable
        accessibilityRole="button"
        disabled={deleting}
        onPress={handleDelete}
        style={[styles.deleteBtn, deleting && styles.deleteBtnDisabled]}
      >
        <Text style={styles.deleteBtnText}>삭제</Text>
      </Pressable>
    </View>
  );
}

// ─── 메인 화면 ──────────────────────────────────────────────

export function InterpretationScreen({ route, navigation }: Props) {
  const { entry } = route.params;
  const dreamPreview = entry.content ? stripHtml(entry.content).slice(0, 120) : "";

  const [items, setItems] = useState<JournalInterpretation[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [newText, setNewText] = useState("");
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);

  const [deletingId, setDeletingId] = useState<number | null>(null);

  const inputRef = useRef<TextInput>(null);

  // 해석 목록 로드
  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await getInterpretations(entry.id);
      setItems(res.rsltList ?? []);
    } catch (e) {
      console.error("[InterpretationScreen] interpretation list load failed", { entryId: entry.id }, e);
      setError(e instanceof Error ? e.message : "해석 목록을 불러오지 못했습니다.");
    } finally {
      setLoading(false);
    }
  }, [entry.id]);

  useEffect(() => { void load(); }, [load]);

  // 해석 추가
  async function handleAdd() {
    const trimmed = newText.trim();
    if (trimmed.length === 0) return;

    setSaving(true);
    setSaveError(null);
    try {
      const res = await createInterpretation(entry.id, trimmed);
      if (!res.rslt) throw new Error(res.message ?? "저장에 실패했습니다.");
      setNewText("");
      inputRef.current?.blur();
      // 목록 재조회
      await load();
    } catch (e) {
      setSaveError(e instanceof Error ? e.message : "저장에 실패했습니다.");
    } finally {
      setSaving(false);
    }
  }

  // 해석 삭제
  async function handleDelete(id: number) {
    setDeletingId(id);
    try {
      const res = await deleteInterpretation(id);
      if (!res.rslt) throw new Error(res.message ?? "삭제에 실패했습니다.");
      setItems(prev => prev.filter(i => i.id !== id));
    } catch (e) {
      Alert.alert("오류", e instanceof Error ? e.message : "삭제에 실패했습니다.");
    } finally {
      setDeletingId(null);
    }
  }

  const canAdd = newText.trim().length > 0 && !saving;

  return (
    <SafeAreaView style={styles.safeArea}>
      {/* 커스텀 헤더 */}
      <View style={styles.header}>
        <Pressable
          accessibilityRole="button"
          accessibilityLabel="뒤로 가기"
          onPress={() => navigation.goBack()}
          style={styles.backButton}
        >
          <Text style={styles.backArrow}>‹</Text>
          <Text style={styles.backLabel}>뒤로</Text>
        </Pressable>
        <Text style={styles.headerTitle}>꿈 해석</Text>
        <View style={styles.headerRight} />
      </View>

      <KeyboardAvoidingView
        behavior={Platform.select({ ios: "padding", android: undefined })}
        style={styles.keyboardArea}
      >
        <ScrollView contentContainerStyle={styles.container} keyboardShouldPersistTaps="handled">

          {/* 꿈 본문 미리보기 */}
          {dreamPreview ? (
            <View style={styles.dreamPreview}>
              <Text style={styles.dreamPreviewLabel}>꿈 내용</Text>
              <Text style={styles.dreamPreviewText} numberOfLines={4}>
                {dreamPreview}
              </Text>
            </View>
          ) : null}

          {/* 해석 목록 */}
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>해석 ({items.length})</Text>

            {loading && (
              <View style={styles.center}>
                <ActivityIndicator color={colors.accent} />
              </View>
            )}

            {!loading && error != null && (
              <Text style={styles.errorText}>{error}</Text>
            )}

            {!loading && !error && items.length === 0 && (
              <Text style={styles.emptyText}>아직 해석이 없습니다.</Text>
            )}

            {!loading && !error && items.map(item => (
              <InterpretationCard
                key={item.id}
                item={item}
                onDelete={(id) => { void handleDelete(id); }}
                deleting={deletingId === item.id}
              />
            ))}
          </View>

          {/* 새 해석 추가 */}
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>새 해석 추가</Text>
            <TextInput
              ref={inputRef}
              multiline
              onChangeText={setNewText}
              placeholder="이 꿈에 대한 해석이나 느낌을 남겨보세요"
              placeholderTextColor={colors.muted}
              style={styles.addInput}
              textAlignVertical="top"
              value={newText}
            />
            {saveError != null && (
              <Text style={styles.errorText}>{saveError}</Text>
            )}
            <Pressable
              accessibilityRole="button"
              disabled={!canAdd}
              onPress={() => { void handleAdd(); }}
              style={[styles.addButton, !canAdd && styles.addButtonDisabled]}
            >
              {saving
                ? <ActivityIndicator size="small" color={colors.onAccent} />
                : <Text style={styles.addButtonText}>추가</Text>
              }
            </Pressable>
          </View>

        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

// ─── 스타일 ─────────────────────────────────────────────────

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: colors.background },
  keyboardArea: { flex: 1 },
  // 헤더
  header: {
    flexDirection: "row",
    alignItems: "center",
    paddingHorizontal: 8,
    paddingVertical: 10,
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
    backgroundColor: colors.background
  },
  backButton: { flexDirection: "row", alignItems: "center", gap: 2, paddingHorizontal: 8, minWidth: 64 },
  backArrow: { fontSize: 28, color: colors.accent, lineHeight: 32, fontWeight: "300" },
  backLabel: { color: colors.accent, fontSize: 16, fontWeight: "600" },
  headerTitle: { flex: 1, textAlign: "center", color: colors.text, fontSize: 16, fontWeight: "800" },
  headerRight: { minWidth: 64 },
  // 본문
  container: { padding: 20, gap: 20, flexGrow: 1 },
  center: { paddingVertical: 20, alignItems: "center" },
  errorText: { color: "#C0392B", fontSize: 13 },
  emptyText: { color: colors.muted, fontSize: 14 },
  // 꿈 미리보기
  dreamPreview: {
    backgroundColor: "#F5EEF8",
    borderRadius: 10,
    padding: 14,
    gap: 6,
    borderLeftWidth: 3,
    borderLeftColor: "#8E44AD"
  },
  dreamPreviewLabel: {
    color: "#8E44AD",
    fontSize: 11,
    fontWeight: "700",
    textTransform: "uppercase",
    letterSpacing: 0.5
  },
  dreamPreviewText: { color: "#6C3483", fontSize: 14, lineHeight: 22 },
  // 섹션
  section: { gap: 10 },
  sectionTitle: {
    color: colors.secondaryText,
    fontSize: 12,
    fontWeight: "700",
    textTransform: "uppercase",
    letterSpacing: 0.5
  },
  // 해석 카드
  interpretCard: {
    backgroundColor: colors.surface,
    borderRadius: 10,
    padding: 14,
    gap: 10,
    borderWidth: 1,
    borderColor: colors.border,
    flexDirection: "row",
    alignItems: "flex-start"
  },
  interpretBody: { flex: 1, color: colors.text, fontSize: 14, lineHeight: 22 },
  deleteBtn: {
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 6,
    borderWidth: 1,
    borderColor: "#E8B4B8"
  },
  deleteBtnDisabled: { opacity: 0.4 },
  deleteBtnText: { color: "#C0392B", fontSize: 12, fontWeight: "700" },
  // 추가 입력
  addInput: {
    minHeight: 120,
    backgroundColor: colors.input,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: colors.border,
    color: colors.text,
    fontSize: 15,
    lineHeight: 24,
    padding: 14
  },
  addButton: {
    backgroundColor: "#8E44AD",
    borderRadius: 8,
    paddingVertical: 12,
    alignItems: "center"
  },
  addButtonDisabled: { opacity: 0.45 },
  addButtonText: { color: "#fff", fontSize: 15, fontWeight: "700" }
});
