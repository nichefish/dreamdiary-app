import { useState } from "react";
import {
  ActivityIndicator,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  SafeAreaView,
  StyleSheet,
  Text,
  TextInput,
  View
} from "react-native";
import { useAuth } from "../context/AuthContext";
import { getApiBaseUrlDevHint } from "../config/env";
import { colors } from "../theme/colors";

export function LoginScreen() {
  const { login } = useAuth();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const apiDevHint = getApiBaseUrlDevHint();

  const canSubmit = username.trim().length > 0 && password.length > 0 && !isSubmitting;

  async function handleLogin() {
    if (!canSubmit) return;
    setError(null);
    setIsSubmitting(true);
    try {
      await login(username.trim(), password);
      // 성공 시 AuthContext isAuthenticated 변경 → AppNavigator가 Home으로 전환
    } catch (e) {
      setError(e instanceof Error ? e.message : "로그인에 실패했습니다.");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <SafeAreaView style={styles.safeArea}>
      <KeyboardAvoidingView
        behavior={Platform.select({ ios: "padding", android: undefined })}
        style={styles.keyboardArea}
      >
        <View style={styles.container}>

          <View style={styles.header}>
            <Text style={styles.kicker}>DreamDiary</Text>
            <Text style={styles.title}>로그인</Text>
            <Text style={styles.subtitle}>계속하려면 계정에 로그인하세요.</Text>
          </View>

          <View style={styles.form}>
            <View style={styles.fieldGroup}>
              <Text style={styles.label}>아이디</Text>
              <TextInput
                autoCapitalize="none"
                autoComplete="username"
                autoCorrect={false}
                onChangeText={setUsername}
                onSubmitEditing={handleLogin}
                placeholder="아이디 입력"
                placeholderTextColor={colors.muted}
                returnKeyType="next"
                style={styles.input}
                value={username}
              />
            </View>

            <View style={styles.fieldGroup}>
              <Text style={styles.label}>비밀번호</Text>
              <TextInput
                autoComplete="password"
                onChangeText={setPassword}
                onSubmitEditing={handleLogin}
                placeholder="비밀번호 입력"
                placeholderTextColor={colors.muted}
                returnKeyType="done"
                secureTextEntry
                style={styles.input}
                value={password}
              />
            </View>


            {apiDevHint != null && (
              <View style={styles.devHintBox}>
                <Text style={styles.devHintText}>{apiDevHint}</Text>
              </View>
            )}
            {error != null && (
              <View style={styles.errorBox}>
                <Text style={styles.errorText}>{error}</Text>
              </View>
            )}

            <Pressable
              accessibilityRole="button"
              disabled={!canSubmit}
              onPress={() => { void handleLogin(); }}
              style={[styles.loginButton, !canSubmit && styles.loginButtonDisabled]}
            >
              {isSubmitting ? (
                <ActivityIndicator color={colors.onAccent} />
              ) : (
                <Text style={styles.loginButtonText}>로그인</Text>
              )}
            </Pressable>
          </View>

        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: colors.background
  },
  keyboardArea: {
    flex: 1
  },
  container: {
    flex: 1,
    justifyContent: "center",
    paddingHorizontal: 28,
    gap: 36
  },
  header: {
    gap: 6
  },
  kicker: {
    color: colors.accent,
    fontSize: 13,
    fontWeight: "700",
    letterSpacing: 0.4,
    textTransform: "uppercase"
  },
  title: {
    color: colors.text,
    fontSize: 32,
    fontWeight: "800"
  },
  subtitle: {
    color: colors.secondaryText,
    fontSize: 15,
    lineHeight: 22
  },
  form: {
    gap: 16
  },
  fieldGroup: {
    gap: 6
  },
  label: {
    color: colors.secondaryText,
    fontSize: 13,
    fontWeight: "600"
  },
  input: {
    backgroundColor: colors.input,
    borderColor: colors.border,
    borderRadius: 8,
    borderWidth: 1,
    color: colors.text,
    fontSize: 16,
    paddingHorizontal: 14,
    paddingVertical: 12
  },
  errorBox: {
    backgroundColor: "#FFF0F0",
    borderColor: "#FFBCBC",
    borderRadius: 8,
    borderWidth: 1,
    padding: 12
  },
  devHintBox: {
    backgroundColor: "#FFF8E1",
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 10,
    marginBottom: 4
  },
  devHintText: { color: "#7D6608", fontSize: 13, lineHeight: 18 },
  errorText: {
    color: "#C0392B",
    fontSize: 14
  },
  loginButton: {
    alignItems: "center",
    backgroundColor: colors.accent,
    borderRadius: 8,
    marginTop: 8,
    paddingVertical: 15
  },
  loginButtonDisabled: {
    opacity: 0.45
  },
  loginButtonText: {
    color: colors.onAccent,
    fontSize: 16,
    fontWeight: "800"
  }
});
