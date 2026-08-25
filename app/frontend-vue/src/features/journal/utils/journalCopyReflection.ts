/**
 * 저널 복사(엔트리·챕터·검색·스레드)가 리플렉션(해석)을 어느 범위까지 담을지 정하는 단일 계약.
 * <p>
 * 복사 split 버튼의 3단계 그라디언트를 표현한다:
 * <ul>
 *   <li>{@code full} — 주 버튼. 모든 리플렉션 포함(보류 PENDING 포함).</li>
 *   <li>{@code no-pending} — 드롭다운. 리플렉션을 포함하되 보류(PENDING)만 제외(OPEN·RESOLVED 유지).</li>
 *   <li>{@code body} — 드롭다운. 리플렉션을 전부 제외(본문만).</li>
 * </ul>
 * 리플렉션 본문 소스 선택(`content ?? markdownContent`)은 복사 내용 계약이 별도로 규정하며,
 * 이 모듈은 "어느 리플렉션을 담을지"(포함/제외)만 판정한다.
 */
export type CopyReflectionMode = "full" | "no-pending" | "body";

/**
 * 주어진 복사 모드에서 이 리플렉션을 복사에 포함할지 판정한다.
 *
 * @param mode 복사 모드
 * @param lifecycleKey 리플렉션 lifecycle 키(OPEN/PENDING/RESOLVED). 없으면 OPEN 취급.
 * @return 포함하면 true
 */
export function includeReflectionInCopy(
  mode: CopyReflectionMode,
  lifecycleKey: string | null | undefined,
): boolean {
  if (mode === "body") return false;
  if (mode === "no-pending") return lifecycleKey !== "PENDING";
  return true;
}

/**
 * 복사 성공 토스트 i18n 키를 복사 범위에 맞게 고른다.
 * <p>
 * 본문만은 항상 본문 문구, 나머지는 리플렉션이 하나도 없으면 공용 복사 문구로 수렴한다.
 *
 * @param mode 복사 모드
 * @param hasReflection 복사 대상에 리플렉션이 하나라도 있으면 true
 * @return i18n 키
 */
export function copySuccessKey(mode: CopyReflectionMode, hasReflection: boolean): string {
  if (mode === "body") return "journal.copy.body.success";
  if (!hasReflection) return "common.copy.success";
  if (mode === "no-pending") return "journal.copy.no-pending.success";
  return "journal.copy.full.success";
}
