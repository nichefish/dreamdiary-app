import { getAssetPath } from "@metronic/core/helpers/assets";

/** SPA public 경로 기준 blank 아바타 (레거시 .png 미배포 → .svg 사용) */
const BLANK_AVATAR_REL = "media/avatars/avatar_blank.svg";

/**
 * blank 아바타 URL (import.meta.env.BASE_URL 포함).
 */
export function getBlankAvatarUrl(): string {
  return getAssetPath(BLANK_AVATAR_REL);
}

/**
 * 프로필 이미지 URL을 SPA에서 로드 가능한 경로로 정규화한다.
 * - 빈 값·avatar_blank·레거시 /metronic/assets/ 경로 처리
 */
export function resolveProfileImageUrl(url?: string | null): string {
  const blank = getBlankAvatarUrl();
  const trimmed = url?.trim();
  if (!trimmed) return blank;
  if (trimmed.includes("avatar_blank")) return blank;
  if (trimmed.startsWith("/metronic/assets/")) {
    return getAssetPath(trimmed.slice("/metronic/assets/".length));
  }
  return trimmed;
}

/**
 * img @error 핸들러. fallback 재시도 1회만 허용 (무한 요청 방지).
 */
export function handleProfileImageError(event: Event): void {
  const img = event.target as HTMLImageElement;
  const blank = getBlankAvatarUrl();
  if (img.dataset.profileImgFallback === "1") {
    img.onerror = null;
    return;
  }
  if (img.src === blank || img.src.endsWith("avatar_blank.svg") || img.src.endsWith("avatar_blank.png")) {
    img.onerror = null;
    return;
  }
  img.dataset.profileImgFallback = "1";
  img.src = blank;
}
