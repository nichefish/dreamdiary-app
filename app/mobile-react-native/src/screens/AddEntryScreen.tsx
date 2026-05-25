import { useMemo, useState } from "react";
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
import type { NativeStackScreenProps } from "@react-navigation/native-stack";
import { captureEntryForDate } from "../api/dreamDiaryApi";
import type { RootStackParamList } from "../navigation/AppNavigator";
import { colors } from "../theme/colors";
import { formatDateDots, normalizeDateStr, toDateStr } from "../utils/date";
import type { CaptureMode } from "../types/journal";

type Props = NativeStackScreenProps<RootStackParamList, "AddEntry">;

const MODES: Array<{ id: CaptureMode; label: string; placeholder: string }> = [
  { id: "dream", label: "꿈", placeholder: "깨자마자 남은 장면, 인물, 감각을 그대로 적기" },
  { id: "emotion", label: "감정", placeholder: "지금 감정, 몸의 느낌, 이유 없는 기분을 짧게 기록" },
  { id: "chat", label: "AI 대화", placeholder: "묻고 싶은 상징, 오늘의 흐름, 정리하고 싶은 생각" }
];

export function AddEntryScreen({ route, navigation }: Props) {
  const routeDate = route.params.date;
  const safeDate = normalizeDateStr(routeDate) ?? toDateStr(new Date());
  const displayDate = formatDateDots(safeDate);

  const [mode, setMode] = useState<CaptureMode>("dream");
  const [text, setText] = useState("");
  const [saving, setSaving] = useState(false);
  const [saveResult, setSaveResult] = useState<{ ok: boolean; msg: string } | null>(null);

  const selectedMode = useMemo(() => MODES.find(m => m.id === mode) ?? MODES[0], [mode]);
  // chat 모드는 AiChat 화면으로 이동하므로 text 입력 여부와 무관하게 버튼 활성화
  const canSave = (mode === "chat" || text.trim().length > 0) && !saving;

  async function handleSave() {
    // chat 모드: captureEntryForDate 대신 AiChat 화면으로 이동
    if (mode === "chat") {
      navigation.navigate("AiChat");
      return;
    }
    if (!canSave) return;
    setSaving(true);
    setSaveResult(null);
    try {
      await captureEntryForDate(mode, text.trim(), safeDate);
      navigation.goBack();
    } catch (e) {
      setSaveResult({ ok: false, msg: e instanceof Error ? e.message : "저장에 실패했습니다." });
    } finally {
      setSaving(false);
    }
  }

  return (
    <SafeAreaView style={styles.safeArea}>
      {/* 헤더 */}
      <View style={styles.header}>
        <Pressable
          accessibilityRole="button"
          onPress={() => navigation.goBack()}
          style={styles.backButton}
        >
          <Text style={styles.backArrow}>‹</Text>
          <Text style={styles.backLabel}>뒤로</Text>
        </Pressable>
        <View style={styles.headerCenter}>
          <Text style={styles.headerTitle}>기록 추가</Text>
          <Text style={styles.headerDate}>{displayDate}</Text>
        </View>
        {/* 우측 공간 확보 (backButton 너비 보정) */}
        <View style={styles.headerRight} />
      </View>

      <KeyboardAvoidingView
        behavior={Platform.select({ ios: "padding", android: undefined })}
        style={styles.keyboardArea}
      >
        <ScrollView contentContainerStyle={styles.container} keyboardShouldPersistTaps="handled">

          {/* 모드 선택 */}
          <View style={styles.segmentedControl}>
            {MODES.map(item => {
              const active = item.id === mode;
              return (
                <Pressable
                  accessibilityRole="button"
                  accessibilityState={{ selected: active }}
                  key={item.id}
                  onPress={() => setMode(item.id)}
                  style={[styles.segmentButton, active && styles.segmentButtonActive]}
                >
                  <Text style={[styles.segmentText, active && styles.segmentTextActive]}>
                    {item.label}
                  </Text>
                </Pressable>
              );
            })}
          </View>

          {/* 입력 패널 */}
          <View style={styles.capturePanel}>
            <Text style={styles.panelLabel}>{selectedMode.label} 입력</Text>
            {/* chat 모드: 텍스트 입력 대신 안내 패널 표시 (입력 내용이 저장되지 않으므로) */}
            {mode === "chat" ? (
              <View style={styles.chatHint}>
                <Text style={styles.chatHintText}>
                  AI와 꿈·감정·생각을 대화로 기록합니다.{"\n"}버튼을 눌러 대화를 시작하세요.
                </Text>
              </View>
            ) : (
              <TextInput
                multiline
                onChangeText={setText}
                placeholder={selectedMode.placeholder}
                placeholderTextColor={colors.muted}
                style={styles.input}
                textAlignVertical="top"
                value={text}
              />
            )}

            {saveResult != null && (
              <View style={[styles.resultBox, saveResult.ok ? styles.resultBoxOk : styles.resultBoxErr]}>
                <Text style={[styles.resultText, saveResult.ok ? styles.resultTextOk : styles.resultTextErr]}>
                  {saveResult.msg}
                </Text>
              </View>
            )}

            <Pressable
              accessibilityRole="button"
              disabled={!canSave}
              onPress={() => { void handleSave(); }}
              style={[styles.primaryButton, !canSave && styles.primaryButtonDisabled]}
            >
              {saving
                ? <ActivityIndicator color={colors.onAccent} />
                : <Text style={styles.primaryButtonText}>{mode === "chat" ? "AI 대화 시작" : "저장"}</Text>
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
  header: {
    flexDirection: "row",
    alignItems: "center",
    paddingHorizontal: 8,
    paddingVertical: 10,
    borderBottomWidth: 1,
    borderBottomColor: colors.border
  },
  backButton: { flexDirection: "row", alignItems: "center", gap: 2, paddingHorizontal: 8, minWidth: 64 },
  backArrow: { fontSize: 28, color: colors.accent, lineHeight: 32, fontWeight: "300" },
  backLabel: { color: colors.accent, fontSize: 16, fontWeight: "600" },
  headerCenter: { flex: 1, alignItems: "center", gap: 2 },
  headerTitle: { color: colors.text, fontSize: 16, fontWeight: "800" },
  headerDate: { color: colors.muted, fontSize: 12 },
  headerRight: { minWidth: 64 },
  container: { flexGrow: 1, padding: 20, gap: 16 },
  // 세그먼트
  segmentedControl: {
    flexDirection: "row",
    gap: 8,
    backgroundColor: colors.surface,
    borderRadius: 8,
    padding: 6
  },
  segmentButton: { flex: 1, alignItems: "center", borderRadius: 6, paddingVertical: 10 },
  segmentButtonActive: { backgroundColor: colors.text },
  segmentText: { color: colors.secondaryText, fontSize: 14, fontWeight: "700" },
  segmentTextActive: { color: colors.onDark },
  // 입력 패널
  capturePanel: { gap: 12 },
  panelLabel: { color: colors.text, fontSize: 16, fontWeight: "800" },
  input: {
    minHeight: 200,
    borderColor: colors.border,
    borderRadius: 8,
    borderWidth: 1,
    color: colors.text,
    fontSize: 16,
    lineHeight: 24,
    padding: 16,
    backgroundColor: colors.input
  },
  // 결과 박스
  resultBox: { borderRadius: 6, paddingHorizontal: 12, paddingVertical: 8 },
  resultBoxOk: { backgroundColor: "#D5F5E3" },
  resultBoxErr: { backgroundColor: "#FADBD8" },
  resultText: { fontSize: 14, fontWeight: "600" },
  resultTextOk: { color: "#1E8449" },
  resultTextErr: { color: "#C0392B" },
  // 저장 버튼
  primaryButton: {
    alignItems: "center",
    backgroundColor: colors.accent,
    borderRadius: 8,
    paddingVertical: 14
  },
  primaryButtonDisabled: { opacity: 0.45 },
  primaryButtonText: { color: colors.onAccent, fontSize: 16, fontWeight: "800" },
  // chat 모드 안내 패널 (TextInput 대신 표시)
  chatHint: {
    minHeight: 200,
    borderColor: colors.border,
    borderRadius: 8,
    borderWidth: 1,
    backgroundColor: colors.input,
    alignItems: "center",
    justifyContent: "center",
    padding: 24
  },
  chatHintText: {
    color: colors.muted,
    fontSize: 15,
    lineHeight: 24,
    textAlign: "center"
  }
});
