import { Platform } from "react-native";

const fallbackBaseUrl = "http://localhost:8080";

export const API_BASE_URL = process.env.EXPO_PUBLIC_API_BASE_URL ?? fallbackBaseUrl;

function isLocalhostUrl(url: string): boolean {
  try {
    const host = new URL(url).hostname;
    return host === "localhost" || host === "127.0.0.1";
  } catch {
    return false;
  }
}

/**
 * 개발 중 localhost API URL 사용 시 플랫폼별 안내 문구.
 * null 이면 추가 안내 불필요.
 */
export function getApiBaseUrlDevHint(): string | null {
  if (!isLocalhostUrl(API_BASE_URL)) return null;

  if (Platform.OS === "android") {
    return "Android 에뮬레이터: EXPO_PUBLIC_API_BASE_URL=http://10.0.2.2:8080";
  }
  if (Platform.OS === "ios") {
    return "iOS 시뮬레이터는 localhost 가 동작할 수 있습니다. 실기기는 PC IP 를 사용하세요.";
  }
  return "실기기에서는 PC 의 LAN IP 로 EXPO_PUBLIC_API_BASE_URL 을 설정하세요.";
}
