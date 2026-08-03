/**
 * Reflection 스레드 소속 자격.
 * 일기·꿈·노트를 target으로 둔 Reflection은 스레드에 추가하지 않는다.
 * 정본: docs/spec/REFLECTION_ONE_TYPE.md §4
 */

const PRIMARY_TARGET_TYPES = new Set([
  "JOURNAL_DIARY",
  "DIARY",
  "JOURNAL_DREAM",
  "DREAM",
  "JOURNAL_NOTE",
  "NOTE",
]);

/**
 * 일기·꿈·노트를 target으로 둔 Reflection이면 true (스레드 추가 UI/API 비대상).
 */
export function isPrimaryContentTargetedReflection(entry: {
  contentType?: string | null;
  refId?: number | null;
  refContentType?: string | null;
} | null | undefined): boolean {
  if (!entry) return false;
  const ct = entry.contentType ?? "";
  if (ct !== "JOURNAL_REFLECTION" && ct !== "REFLECTION") return false;
  if (entry.refId == null) return false;
  return PRIMARY_TARGET_TYPES.has(String(entry.refContentType ?? ""));
}
