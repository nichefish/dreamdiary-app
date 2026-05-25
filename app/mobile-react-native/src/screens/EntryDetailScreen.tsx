import { useState } from "react";
import {
  Alert,
  Pressable,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  View
} from "react-native";
import type { NativeStackScreenProps } from "@react-navigation/native-stack";
import { deleteEntry } from "../api/dreamDiaryApi";
import type { RootStackParamList } from "../navigation/AppNavigator";
import { colors } from "../theme/colors";
import { stripHtml } from "../utils/text";

type Props = NativeStackScreenProps<RootStackParamList, "EntryDetail">;

const CONTENT_TYPE_LABELS: Record<string, string> = {
  JOURNAL_DREAM: "꿈",
  JOURNAL_DIARY: "일기",
  JOURNAL_NOTE: "노트"
};

export function EntryDetailScreen({ route, navigation }: Props) {
  const { entry, isDream } = route.params;
  const body = entry.content ? stripHtml(entry.content) : "";
  const typeLabel = CONTENT_TYPE_LABELS[entry.contentType] ?? entry.contentType;
  // stdrdDt가 있으면 "YYYY.MM.DD" 형태로 변환해 날짜 표시
  const dateLabel = entry.stdrdDt ? entry.stdrdDt.replace(/-/g, ".") : null;
  const [deleting, setDeleting] = useState(false);

  function handleEdit() {
    navigation.navigate("EntryEdit", { entry, isDream });
  }

  // 꿈 해석 화면으로 이동 (꿈 엔트리에서만 호출)
  function handleInterpretation() {
    navigation.navigate("InterpretationDetail", { entry });
  }

  function handleDelete() {
    Alert.alert(
      "기록 삭제",
      "이 기록을 삭제할까요? 되돌릴 수 없습니다.",
      [
        { text: "취소", style: "cancel" },
        {
          text: "삭제",
          style: "destructive",
          onPress: async () => {
            setDeleting(true);
            try {
              const res = await deleteEntry(entry.id);
              if (!res.rslt) throw new Error(res.message ?? "삭제에 실패했습니다.");
              navigation.goBack();
            } catch (e) {
              setDeleting(false);
              Alert.alert("오류", e instanceof Error ? e.message : "삭제에 실패했습니다.");
            }
          }
        }
      ]
    );
  }

  return (
    <SafeAreaView style={[styles.safeArea, isDream && styles.safeAreaDream]}>
      {/* 커스텀 헤더 */}
      <View style={[styles.header, isDream && styles.headerDream]}>
        <Pressable
          accessibilityRole="button"
          accessibilityLabel="뒤로 가기"
          onPress={() => navigation.goBack()}
          style={styles.backButton}
        >
          <Text style={[styles.backArrow, isDream && styles.backArrowDream]}>‹</Text>
          <Text style={[styles.backLabel, isDream && styles.backLabelDream]}>뒤로</Text>
        </Pressable>

        <View style={styles.typeBadgeWrap}>
          <View style={[styles.typeBadge, isDream && styles.typeBadgeDream]}>
            <Text style={[styles.typeBadgeText, isDream && styles.typeBadgeTextDream]}>
              {typeLabel}
            </Text>
          </View>
        </View>

        {/* 수정/삭제 액션 버튼 */}
        <View style={styles.actions}>
          <Pressable
            accessibilityRole="button"
            onPress={handleEdit}
            style={styles.actionBtn}
          >
            <Text style={[styles.actionBtnText, isDream && styles.actionBtnTextDream]}>수정</Text>
          </Pressable>
          <Pressable
            accessibilityRole="button"
            disabled={deleting}
            onPress={handleDelete}
            style={[styles.actionBtn, deleting && styles.actionBtnDisabled]}
          >
            <Text style={styles.deleteBtnText}>삭제</Text>
          </Pressable>
        </View>
      </View>

      <ScrollView contentContainerStyle={styles.container}>
        {/* 기록 날짜 (stdrdDt가 있을 때만 표시) */}
        {dateLabel != null && (
          <Text style={[styles.dateLabel, isDream && styles.dateLabelDream]}>{dateLabel}</Text>
        )}

        {/* 제목 */}
        {entry.title ? (
          <Text style={[styles.title, isDream && styles.titleDream]}>{entry.title}</Text>
        ) : null}

        {/* 본문 */}
        <Text style={[styles.body, isDream && styles.bodyDream]}>
          {body || "(내용 없음)"}
        </Text>

        {/* 꿈 엔트리일 때만 해석 버튼 표시 */}
        {isDream && (
          <Pressable
            accessibilityRole="button"
            onPress={handleInterpretation}
            style={styles.interpretButton}
          >
            <Text style={styles.interpretButtonText}>🌙 꿈 해석 보기 →</Text>
          </Pressable>
        )}
      </ScrollView>
    </SafeAreaView>
  );
}

// ─── 스타일 ─────────────────────────────────────────────────

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: colors.background },
  safeAreaDream: { backgroundColor: "#F5EEF8" },
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
  backButton: {
    flexDirection: "row",
    alignItems: "center",
    gap: 2,
    paddingRight: 12
  },
  backArrow: { fontSize: 28, color: colors.accent, lineHeight: 32, fontWeight: "300" },
  backArrowDream: { color: "#8E44AD" },
  backLabel: { color: colors.accent, fontSize: 16, fontWeight: "600" },
  backLabelDream: { color: "#8E44AD" },
  typeBadgeWrap: { flex: 1 },
  typeBadge: {
    alignSelf: "flex-start",
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 12,
    backgroundColor: colors.surface,
    borderWidth: 1,
    borderColor: colors.border
  },
  typeBadgeDream: { backgroundColor: "#E8DAEF", borderColor: "#C39BD3" },
  typeBadgeText: { color: colors.secondaryText, fontSize: 12, fontWeight: "700" },
  typeBadgeTextDream: { color: "#6C3483" },
  actions: { flexDirection: "row", gap: 4 },
  actionBtn: {
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 6,
    borderWidth: 1,
    borderColor: colors.border
  },
  actionBtnDisabled: { opacity: 0.4 },
  actionBtnText: { color: colors.accent, fontSize: 13, fontWeight: "700" },
  actionBtnTextDream: { color: "#8E44AD" },
  deleteBtnText: { color: "#C0392B", fontSize: 13, fontWeight: "700" },
  // 본문
  container: { padding: 24, gap: 12, flexGrow: 1 },
  dateLabel: { color: colors.muted, fontSize: 12, fontWeight: "500" },
  dateLabelDream: { color: "#9B59B6" },
  title: { color: colors.text, fontSize: 22, fontWeight: "800", lineHeight: 30 },
  titleDream: { color: "#4A235A" },
  body: { color: colors.secondaryText, fontSize: 16, lineHeight: 26 },
  bodyDream: { color: "#6C3483" },
  // 꿈 해석 버튼
  interpretButton: {
    marginTop: 8,
    paddingVertical: 14,
    paddingHorizontal: 16,
    backgroundColor: "#EDE0F7",
    borderRadius: 10,
    alignItems: "center",
    borderWidth: 1,
    borderColor: "#C39BD3"
  },
  interpretButtonText: { color: "#6C3483", fontSize: 15, fontWeight: "700" }
});