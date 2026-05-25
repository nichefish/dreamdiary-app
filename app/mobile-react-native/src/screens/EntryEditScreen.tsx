import { useState } from "react";
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
import { updateEntry } from "../api/dreamDiaryApi";
import type { RootStackParamList } from "../navigation/AppNavigator";
import { exitAfterEntrySave } from "../navigation/exitAfterEntrySave";
import { colors } from "../theme/colors";
import { stripHtml } from "../utils/text";

type Props = NativeStackScreenProps<RootStackParamList, "EntryEdit">;

export function EntryEditScreen({ route, navigation }: Props) {
  const { entry, isDream } = route.params;

  // 백엔드 HTML 본문을 평문으로 변환해 편집 초기값으로 사용
  const [title, setTitle] = useState(entry.title ?? "");
  const [content, setContent] = useState(entry.content ? stripHtml(entry.content) : "");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const canSave = content.trim().length > 0 && !saving;

  async function handleSave() {
    if (!canSave) return;
    setSaving(true);
    setError(null);
    try {
      const res = await updateEntry(entry.id, {
        content: content.trim(),
        title: title.trim() || undefined
      });
      if (!res.rslt) throw new Error(res.message ?? "저장에 실패했습니다.");
      // 수정 완료 후 현재 스택 깊이에 맞춰 목록 화면으로 복귀
      exitAfterEntrySave(navigation);
    } catch (e) {
      setError(e instanceof Error ? e.message : "저장에 실패했습니다.");
      setSaving(false);
    }
  }

  return (
    <SafeAreaView style={[styles.safeArea, isDream && styles.safeAreaDream]}>
      {/* 헤더 */}
      <View style={[styles.header, isDream && styles.headerDream]}>
        <Pressable
          accessibilityRole="button"
          onPress={() => navigation.goBack()}
          style={styles.cancelBtn}
        >
          <Text style={[styles.cancelText, isDream && styles.cancelTextDream]}>취소</Text>
        </Pressable>

        <Text style={[styles.headerTitle, isDream && styles.headerTitleDream]}>기록 수정</Text>

        <Pressable
          accessibilityRole="button"
          disabled={!canSave}
          onPress={() => { void handleSave(); }}
          style={[styles.saveBtn, !canSave && styles.saveBtnDisabled]}
        >
          {saving
            ? <ActivityIndicator color={isDream ? "#8E44AD" : colors.accent} />
            : <Text style={[styles.saveText, isDream && styles.saveTextDream]}>저장</Text>
          }
        </Pressable>
      </View>

      <KeyboardAvoidingView
        behavior={Platform.select({ ios: "padding", android: undefined })}
        style={styles.keyboardArea}
      >
        <ScrollView contentContainerStyle={styles.container} keyboardShouldPersistTaps="handled">
          {/* 제목 입력 */}
          <TextInput
            onChangeText={setTitle}
            placeholder="제목 (선택)"
            placeholderTextColor={colors.muted}
            style={[styles.titleInput, isDream && styles.titleInputDream]}
            value={title}
          />

          {/* 구분선 */}
          <View style={[styles.divider, isDream && styles.dividerDream]} />

          {/* 본문 입력 */}
          <TextInput
            multiline
            onChangeText={setContent}
            placeholder="내용을 입력하세요"
            placeholderTextColor={colors.muted}
            style={[styles.bodyInput, isDream && styles.bodyInputDream]}
            textAlignVertical="top"
            value={content}
          />

          {/* 오류 */}
          {error != null && (
            <View style={styles.errorBox}>
              <Text style={styles.errorText}>{error}</Text>
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
  safeAreaDream: { backgroundColor: "#F5EEF8" },
  keyboardArea: { flex: 1 },
  // 헤더
  header: {
    flexDirection: "row",
    alignItems: "center",
    paddingHorizontal: 16,
    paddingVertical: 10,
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
    backgroundColor: colors.background
  },
  headerDream: { backgroundColor: "#F5EEF8", borderBottomColor: "#D7BDE2" },
  cancelBtn: { paddingHorizontal: 4, paddingVertical: 6 },
  cancelText: { color: colors.secondaryText, fontSize: 15, fontWeight: "600" },
  cancelTextDream: { color: "#8E44AD" },
  headerTitle: {
    flex: 1,
    textAlign: "center",
    color: colors.text,
    fontSize: 16,
    fontWeight: "800"
  },
  headerTitleDream: { color: "#4A235A" },
  saveBtn: { paddingHorizontal: 4, paddingVertical: 6 },
  saveBtnDisabled: { opacity: 0.4 },
  saveText: { color: colors.accent, fontSize: 15, fontWeight: "800" },
  saveTextDream: { color: "#8E44AD" },
  // 본문
  container: { padding: 20, gap: 0, flexGrow: 1 },
  titleInput: {
    color: colors.text,
    fontSize: 20,
    fontWeight: "700",
    paddingVertical: 12,
    paddingHorizontal: 0
  },
  titleInputDream: { color: "#4A235A" },
  divider: { height: 1, backgroundColor: colors.border, marginVertical: 4 },
  dividerDream: { backgroundColor: "#D7BDE2" },
  bodyInput: {
    color: colors.secondaryText,
    fontSize: 16,
    lineHeight: 26,
    paddingVertical: 12,
    paddingHorizontal: 0,
    minHeight: 300
  },
  bodyInputDream: { color: "#6C3483" },
  errorBox: {
    marginTop: 12,
    backgroundColor: "#FADBD8",
    borderRadius: 6,
    padding: 12
  },
  errorText: { color: "#C0392B", fontSize: 14, fontWeight: "600" }
});
