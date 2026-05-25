import { useMemo, useState } from "react";
import {
  ActivityIndicator,
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  View
} from "react-native";
import { useNavigation } from "@react-navigation/native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import { captureEntry, captureEntryForDate } from "../api/dreamDiaryApi";
import { colors } from "../theme/colors";
import { toDateStr } from "../utils/date";
import type { RootStackParamList } from "../navigation/AppNavigator";
import type { CaptureMode } from "../types/journal";

const MODES: Array<{ id: CaptureMode; label: string; placeholder: string }> = [
  { id: "dream", label: "꿈", placeholder: "깨자마자 남은 장면, 인물, 감각을 그대로 적기" },
  { id: "emotion", label: "감정", placeholder: "지금 감정, 몸의 느낌, 이유 없는 기분을 짧게 기록" },
  { id: "chat", label: "AI", placeholder: "묻고 싶은 상징, 오늘의 흐름, 정리하고 싶은 생각" }
];

export type QuickCapturePanelProps = {
  /** 저장 대상 일자 (YYYY-MM-DD) */
  dateStr: string;
  /** 저장 성공 후 목록 갱신 등 */
  onSaved?: () => void;
  /** compact: 오늘 허브 내장 / full: 전체 화면용(레거시 입력 탭 대체) */
  variant?: "compact" | "full";
};

export function QuickCapturePanel({
  dateStr,
  onSaved,
  variant = "compact"
}: QuickCapturePanelProps) {
  const rootNav = useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const [mode, setMode] = useState<CaptureMode>("dream");
  const [text, setText] = useState("");
  const [saving, setSaving] = useState(false);
  const [saveResult, setSaveResult] = useState<{ ok: boolean; msg: string } | null>(null);
  const [expanded, setExpanded] = useState(variant === "full");

  const selectedMode = useMemo(() => MODES.find((m) => m.id === mode) ?? MODES[0], [mode]);
  const canSubmit = (mode === "chat" || text.trim().length > 0) && !saving;
  const isToday = dateStr === toDateStr(new Date());
  const inputMinHeight = variant === "full" ? 220 : 120;

  async function handleSave() {
    if (mode === "chat") {
      rootNav.navigate("AiChat");
      return;
    }
    if (!canSubmit) return;
    setSaving(true);
    setSaveResult(null);
    try {
      if (isToday) {
        await captureEntry(mode, text.trim());
      } else {
        await captureEntryForDate(mode, text.trim(), dateStr);
      }
      setText("");
      setSaveResult({ ok: true, msg: "저장했습니다." });
      onSaved?.();
      if (variant === "compact") {
        setExpanded(false);
      }
    } catch (e) {
      setSaveResult({
        ok: false,
        msg: e instanceof Error ? e.message : "저장에 실패했습니다."
      });
    } finally {
      setSaving(false);
    }
  }

  if (variant === "compact" && !expanded) {
    return (
      <Pressable
        accessibilityRole="button"
        onPress={() => setExpanded(true)}
        style={styles.collapsedBar}
      >
        <Text style={styles.collapsedLabel}>빠른 기록</Text>
        <Text style={styles.collapsedHint}>꿈 · 감정 · AI</Text>
      </Pressable>
    );
  }

  return (
    <View style={styles.panel}>
      {variant === "compact" && (
        <View style={styles.panelHeader}>
          <Text style={styles.panelTitle}>빠른 기록</Text>
          <Pressable accessibilityRole="button" onPress={() => setExpanded(false)}>
            <Text style={styles.collapseBtn}>접기</Text>
          </Pressable>
        </View>
      )}

      <View style={styles.segmentedControl}>
        {MODES.map((item) => {
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

      {mode === "chat" ? (
        <View style={[styles.chatHint, { minHeight: inputMinHeight }]}>
          <Text style={styles.chatHintText}>
            AI와 꿈·감정·생각을 대화로 기록합니다.
          </Text>
        </View>
      ) : (
        <TextInput
          multiline
          onChangeText={setText}
          placeholder={selectedMode.placeholder}
          placeholderTextColor={colors.muted}
          style={[styles.input, { minHeight: inputMinHeight }]}
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
        disabled={!canSubmit}
        onPress={() => { void handleSave(); }}
        style={[styles.primaryButton, !canSubmit && styles.primaryButtonDisabled]}
      >
        {saving ? (
          <ActivityIndicator color={colors.onAccent} />
        ) : (
          <Text style={styles.primaryButtonText}>
            {mode === "chat" ? "AI 대화 시작" : "저장"}
          </Text>
        )}
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  collapsedBar: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    backgroundColor: colors.surface,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: colors.border,
    paddingHorizontal: 16,
    paddingVertical: 14
  },
  collapsedLabel: { color: colors.text, fontSize: 15, fontWeight: "700" },
  collapsedHint: { color: colors.muted, fontSize: 13, fontWeight: "600" },
  panel: { gap: 12 },
  panelHeader: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between"
  },
  panelTitle: { color: colors.text, fontSize: 15, fontWeight: "800" },
  collapseBtn: { color: colors.accent, fontSize: 13, fontWeight: "700" },
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
  input: {
    borderColor: colors.border,
    borderRadius: 8,
    borderWidth: 1,
    color: colors.text,
    fontSize: 16,
    lineHeight: 24,
    padding: 16,
    backgroundColor: colors.input
  },
  chatHint: {
    borderColor: colors.border,
    borderRadius: 8,
    borderWidth: 1,
    backgroundColor: colors.input,
    alignItems: "center",
    justifyContent: "center",
    padding: 20
  },
  chatHintText: {
    color: colors.muted,
    fontSize: 14,
    lineHeight: 22,
    textAlign: "center"
  },
  resultBox: { borderRadius: 6, paddingHorizontal: 12, paddingVertical: 8 },
  resultBoxOk: { backgroundColor: "#D5F5E3" },
  resultBoxErr: { backgroundColor: "#FADBD8" },
  resultText: { fontSize: 14, fontWeight: "600" },
  resultTextOk: { color: "#1E8449" },
  resultTextErr: { color: "#C0392B" },
  primaryButton: {
    alignItems: "center",
    backgroundColor: colors.accent,
    borderRadius: 8,
    paddingVertical: 14
  },
  primaryButtonDisabled: { opacity: 0.45 },
  primaryButtonText: { color: colors.onAccent, fontSize: 16, fontWeight: "800" }
});
