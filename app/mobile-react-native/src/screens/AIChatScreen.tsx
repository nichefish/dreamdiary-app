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
  getChatSessions,
  createChatSession,
  deleteChatSession,
  getChatMessages
} from "../api/dreamDiaryApi";
import { bumpChatSessionFromMessage, useChatStomp } from "../hooks/useChatStomp";
import { colors } from "../theme/colors";
import type { RootStackParamList } from "../navigation/AppNavigator";
import type { ChatMessage, ChatSession } from "../types/chat";

type Props = NativeStackScreenProps<RootStackParamList, "AiChat">;

function MessageBubble({ msg }: { msg: ChatMessage }) {
  const isUser = msg.role === "USER" || msg.isCreatedBy === true;
  const body = msg.content ?? "";

  return (
    <View style={[styles.bubbleRow, isUser ? styles.bubbleRowUser : styles.bubbleRowAi]}>
      {!isUser && (
        <View style={styles.aiAvatar}>
          <Text style={styles.aiAvatarText}>AI</Text>
        </View>
      )}
      <View style={[styles.bubble, isUser ? styles.bubbleUser : styles.bubbleAi]}>
        <Text style={[styles.bubbleText, isUser ? styles.bubbleTextUser : styles.bubbleTextAi]}>
          {body || "(내용 없음)"}
        </Text>
      </View>
    </View>
  );
}

export function AIChatScreen({ navigation }: Props) {
  const scrollRef = useRef<ScrollView>(null);
  const [draft, setDraft] = useState("");

  const [sessions, setSessions] = useState<ChatSession[]>([]);
  const [currentId, setCurrentId] = useState<number | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [sessionsLoading, setSessionsLoading] = useState(true);
  const [messagesLoading, setMessagesLoading] = useState(false);
  const [creating, setCreating] = useState(false);
  const [sessionsError, setSessionsError] = useState<string | null>(null);
  const [messagesError, setMessagesError] = useState<string | null>(null);

  const handleIncomingMessage = useCallback((msg: ChatMessage) => {
    setMessages((prev) => {
      if (prev.some((m) => m.id === msg.id)) return prev;
      return [...prev, msg];
    });
    setTimeout(() => scrollRef.current?.scrollToEnd({ animated: true }), 50);
  }, []);

  const handleSessionInvalid = useCallback(() => {
    Alert.alert("세션 만료", "로그인 세션이 만료되었습니다. 다시 로그인해 주세요.", [
      { text: "확인", onPress: () => navigation.goBack() }
    ]);
  }, [navigation]);

  const bumpSession = useCallback(
    (msg: ChatMessage) => {
      if (currentId == null) return;
      setSessions((prev) => bumpChatSessionFromMessage(prev, currentId, msg));
    },
    [currentId]
  );

  const {
    connected,
    waitingResponse,
    connectionError,
    sendMessage,
    cancelMessage,
    clearConnectionError
  } = useChatStomp({
    activeSessionId: currentId,
    onIncomingMessage: handleIncomingMessage,
    onSessionInvalid: handleSessionInvalid,
    bumpSession
  });

  const loadSessions = useCallback(async () => {
    setSessionsLoading(true);
    setSessionsError(null);
    try {
      const res = await getChatSessions();
      setSessions(res.rsltList ?? []);
    } catch (e) {
      console.error("[AIChatScreen] session list load failed", e);
      setSessionsError(e instanceof Error ? e.message : "세션 목록을 불러오지 못했습니다.");
    } finally {
      setSessionsLoading(false);
    }
  }, []);

  const loadMessages = useCallback(async (sid: number) => {
    setMessagesLoading(true);
    setMessagesError(null);
    try {
      const res = await getChatMessages(sid);
      setMessages(res.rsltList ?? []);
      setTimeout(() => scrollRef.current?.scrollToEnd({ animated: false }), 50);
    } catch (e) {
      console.error("[AIChatScreen] message list load failed", { sessionId: sid }, e);
      setMessagesError(e instanceof Error ? e.message : "메시지 목록을 불러오지 못했습니다.");
    } finally {
      setMessagesLoading(false);
    }
  }, []);

  useEffect(() => { void loadSessions(); }, [loadSessions]);

  useEffect(() => {
    if (sessions.length > 0 && currentId === null) {
      setCurrentId(sessions[0].id);
    }
  }, [sessions, currentId]);

  useEffect(() => {
    if (currentId != null) void loadMessages(currentId);
  }, [currentId, loadMessages]);

  async function handleCreateSession() {
    setCreating(true);
    try {
      const res = await createChatSession();
      if (!res.rslt || !res.rsltObj) throw new Error("세션 생성에 실패했습니다.");
      const newSession = res.rsltObj;
      setSessions((prev) => [newSession, ...prev]);
      setCurrentId(newSession.id);
      setMessages([]);
      setDraft("");
    } catch (e) {
      Alert.alert("오류", e instanceof Error ? e.message : "세션 생성에 실패했습니다.");
    } finally {
      setCreating(false);
    }
  }

  function handleDeleteSession(sid: number) {
    Alert.alert("세션 삭제", "이 대화 세션을 삭제할까요?", [
      { text: "취소", style: "cancel" },
      {
        text: "삭제",
        style: "destructive",
        onPress: async () => {
          try {
            await deleteChatSession(sid);
            setSessions((prev) => {
              const next = prev.filter((s) => s.id !== sid);
              if (currentId === sid) {
                setCurrentId(next.length > 0 ? next[0].id : null);
                setMessages([]);
              }
              return next;
            });
          } catch {
            Alert.alert("오류", "세션 삭제에 실패했습니다.");
          }
        }
      }
    ]);
  }

  function handleSend() {
    const trimmed = draft.trim();
    if (!trimmed) return;
    clearConnectionError();
    if (sendMessage(trimmed)) {
      setDraft("");
    }
  }

  const currentSession = sessions.find((s) => s.id === currentId);
  const canSend = connected && !waitingResponse && draft.trim().length > 0 && currentId != null;

  return (
    <SafeAreaView style={styles.safeArea}>
      <KeyboardAvoidingView
        behavior={Platform.select({ ios: "padding", android: undefined })}
        style={styles.keyboardArea}
      >
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
            <Text style={styles.headerTitle} numberOfLines={1}>
              {currentSession?.title ?? "AI 대화"}
            </Text>
            {currentId != null && !connected && (
              <Text style={styles.connectionHint}>연결 중…</Text>
            )}
          </View>

          <Pressable
            accessibilityRole="button"
            disabled={creating}
            onPress={() => { void handleCreateSession(); }}
            style={styles.newBtn}
          >
            {creating ? (
              <ActivityIndicator size="small" color={colors.accent} />
            ) : (
              <Text style={styles.newBtnText}>+ 새 대화</Text>
            )}
          </Pressable>
        </View>

        {connectionError != null && (
          <View style={styles.errorBanner}>
            <Text style={styles.errorBannerText}>{connectionError}</Text>
          </View>
        )}

        {sessions.length > 1 && (
          <ScrollView
            horizontal
            showsHorizontalScrollIndicator={false}
            contentContainerStyle={styles.sessionTabs}
          >
            {sessions.map((s) => (
              <Pressable
                key={s.id}
                onPress={() => setCurrentId(s.id)}
                onLongPress={() => handleDeleteSession(s.id)}
                style={[styles.sessionTab, s.id === currentId && styles.sessionTabActive]}
              >
                <Text
                  style={[styles.sessionTabText, s.id === currentId && styles.sessionTabTextActive]}
                  numberOfLines={1}
                >
                  {s.title ?? `세션 #${s.id}`}
                </Text>
              </Pressable>
            ))}
          </ScrollView>
        )}

        {sessionsLoading ? (
          <View style={styles.center}>
            <ActivityIndicator size="large" color={colors.accent} />
          </View>
        ) : sessionsError != null ? (
          <View style={styles.center}>
            <Text style={styles.errorText}>{sessionsError}</Text>
            <Pressable
              accessibilityRole="button"
              onPress={() => { void loadSessions(); }}
              style={styles.retryButton}
            >
              <Text style={styles.retryButtonText}>다시 시도</Text>
            </Pressable>
          </View>
        ) : currentId === null ? (
          <View style={styles.center}>
            <Text style={styles.emptyText}>아직 대화 세션이 없습니다.</Text>
            <Pressable
              accessibilityRole="button"
              onPress={() => { void handleCreateSession(); }}
              style={styles.createButton}
            >
              {creating ? (
                <ActivityIndicator color={colors.onAccent} />
              ) : (
                <Text style={styles.createButtonText}>첫 대화 시작하기</Text>
              )}
            </Pressable>
          </View>
        ) : (
          <>
            <ScrollView
              ref={scrollRef}
              contentContainerStyle={styles.messageList}
              keyboardShouldPersistTaps="handled"
              onContentSizeChange={() => scrollRef.current?.scrollToEnd({ animated: false })}
            >
              {messagesError != null && messages.length > 0 && (
                <View style={styles.messageErrorRow}>
                  <Text style={styles.errorText}>{messagesError}</Text>
                  <Pressable
                    accessibilityRole="button"
                    onPress={() => { void loadMessages(currentId); }}
                    style={styles.retryButton}
                  >
                    <Text style={styles.retryButtonText}>다시 시도</Text>
                  </Pressable>
                </View>
              )}
              {messagesLoading && messages.length === 0 ? (
                <View style={styles.center}>
                  <ActivityIndicator color={colors.accent} />
                </View>
              ) : messagesError != null && messages.length === 0 ? (
                <View style={styles.center}>
                  <Text style={styles.errorText}>{messagesError}</Text>
                  <Pressable
                    accessibilityRole="button"
                    onPress={() => { void loadMessages(currentId); }}
                    style={styles.retryButton}
                  >
                    <Text style={styles.retryButtonText}>다시 시도</Text>
                  </Pressable>
                </View>
              ) : messages.length === 0 ? (
                <View style={styles.center}>
                  <Text style={styles.emptyText}>메시지를 입력해 대화를 시작하세요.</Text>
                </View>
              ) : (
                messages.map((msg) => <MessageBubble key={msg.id} msg={msg} />)
              )}
              {waitingResponse && (
                <View style={styles.typingRow}>
                  <ActivityIndicator size="small" color="#8E44AD" />
                  <Text style={styles.typingText}>AI 응답 생성 중…</Text>
                </View>
              )}
            </ScrollView>

            <View style={styles.composer}>
              <TextInput
                multiline
                editable={connected && currentId != null}
                onChangeText={setDraft}
                placeholder={connected ? "메시지 입력" : "연결 대기 중…"}
                placeholderTextColor={colors.muted}
                style={styles.composerInput}
                textAlignVertical="top"
                value={draft}
              />
              {waitingResponse ? (
                <Pressable
                  accessibilityRole="button"
                  onPress={cancelMessage}
                  style={styles.cancelBtn}
                >
                  <Text style={styles.cancelBtnText}>중단</Text>
                </Pressable>
              ) : (
                <Pressable
                  accessibilityRole="button"
                  disabled={!canSend}
                  onPress={handleSend}
                  style={[styles.sendBtn, !canSend && styles.sendBtnDisabled]}
                >
                  <Text style={styles.sendBtnText}>전송</Text>
                </Pressable>
              )}
            </View>
          </>
        )}
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: colors.background },
  keyboardArea: { flex: 1 },
  header: {
    flexDirection: "row",
    alignItems: "center",
    paddingHorizontal: 12,
    paddingVertical: 10,
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
    backgroundColor: colors.background
  },
  backButton: {
    flexDirection: "row",
    alignItems: "center",
    gap: 2,
    paddingRight: 8,
    minWidth: 60
  },
  backArrow: { fontSize: 28, color: colors.accent, lineHeight: 32, fontWeight: "300" },
  backLabel: { color: colors.accent, fontSize: 16, fontWeight: "600" },
  headerCenter: { flex: 1, alignItems: "center", gap: 2 },
  headerTitle: {
    color: colors.text,
    fontSize: 16,
    fontWeight: "800",
    maxWidth: "100%"
  },
  connectionHint: { color: colors.muted, fontSize: 11, fontWeight: "600" },
  newBtn: { minWidth: 60, alignItems: "flex-end" },
  newBtnText: { color: colors.accent, fontSize: 13, fontWeight: "700" },
  errorBanner: {
    backgroundColor: "#FADBD8",
    paddingHorizontal: 16,
    paddingVertical: 8
  },
  errorBannerText: { color: "#C0392B", fontSize: 12, fontWeight: "600" },
  sessionTabs: {
    paddingHorizontal: 12,
    paddingVertical: 8,
    gap: 8,
    flexDirection: "row"
  },
  sessionTab: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 16,
    backgroundColor: colors.surface,
    borderWidth: 1,
    borderColor: colors.border,
    maxWidth: 160
  },
  sessionTabActive: {
    backgroundColor: colors.accent,
    borderColor: colors.accent
  },
  sessionTabText: { color: colors.secondaryText, fontSize: 12, fontWeight: "600" },
  sessionTabTextActive: { color: colors.onAccent },
  center: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    gap: 16,
    paddingVertical: 48
  },
  errorText: { color: "#C0392B", fontSize: 14 },
  messageErrorRow: { alignItems: "center", gap: 8, paddingVertical: 8 },
  retryButton: { paddingHorizontal: 12, paddingVertical: 7 },
  retryButtonText: { color: colors.accent, fontSize: 13, fontWeight: "700" },
  emptyText: { color: colors.secondaryText, fontSize: 15, fontWeight: "600" },
  createButton: {
    backgroundColor: colors.accent,
    borderRadius: 10,
    paddingVertical: 12,
    paddingHorizontal: 24
  },
  createButtonText: { color: colors.onAccent, fontSize: 15, fontWeight: "700" },
  messageList: {
    flexGrow: 1,
    padding: 16,
    gap: 12
  },
  typingRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: 8,
    alignSelf: "flex-start",
    paddingVertical: 4
  },
  typingText: { color: colors.muted, fontSize: 13 },
  bubbleRow: {
    flexDirection: "row",
    alignItems: "flex-end",
    gap: 8,
    maxWidth: "85%"
  },
  bubbleRowUser: { alignSelf: "flex-end", flexDirection: "row-reverse" },
  bubbleRowAi: { alignSelf: "flex-start" },
  aiAvatar: {
    width: 28,
    height: 28,
    borderRadius: 14,
    backgroundColor: "#8E44AD",
    alignItems: "center",
    justifyContent: "center",
    flexShrink: 0
  },
  aiAvatarText: { color: "#fff", fontSize: 9, fontWeight: "700" },
  bubble: {
    borderRadius: 16,
    paddingHorizontal: 14,
    paddingVertical: 10,
    flexShrink: 1
  },
  bubbleUser: { backgroundColor: colors.accent, borderBottomRightRadius: 4 },
  bubbleAi: { backgroundColor: "#EDE0F7", borderBottomLeftRadius: 4 },
  bubbleText: { fontSize: 15, lineHeight: 22 },
  bubbleTextUser: { color: colors.onAccent },
  bubbleTextAi: { color: "#4A235A" },
  composer: {
    flexDirection: "row",
    alignItems: "flex-end",
    gap: 8,
    paddingHorizontal: 12,
    paddingVertical: 10,
    borderTopWidth: 1,
    borderTopColor: colors.border,
    backgroundColor: colors.background
  },
  composerInput: {
    flex: 1,
    minHeight: 40,
    maxHeight: 120,
    borderColor: colors.border,
    borderRadius: 10,
    borderWidth: 1,
    color: colors.text,
    fontSize: 15,
    lineHeight: 22,
    paddingHorizontal: 12,
    paddingVertical: 8,
    backgroundColor: colors.input
  },
  sendBtn: {
    backgroundColor: colors.accent,
    borderRadius: 10,
    paddingHorizontal: 16,
    paddingVertical: 12,
    justifyContent: "center"
  },
  sendBtnDisabled: { opacity: 0.45 },
  sendBtnText: { color: colors.onAccent, fontSize: 15, fontWeight: "700" },
  cancelBtn: {
    backgroundColor: "#C0392B",
    borderRadius: 10,
    paddingHorizontal: 14,
    paddingVertical: 12,
    justifyContent: "center"
  },
  cancelBtnText: { color: "#fff", fontSize: 14, fontWeight: "700" }
});
