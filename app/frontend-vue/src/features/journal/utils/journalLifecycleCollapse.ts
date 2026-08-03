/** 저널 화면에서 명시적으로 표시하는 라이프사이클 키. */
export type JournalLifecycleKey = "OPEN" | "PENDING" | "RESOLVED";

/** 챕터가 하위 엔트리 전체에서 파생할 수 있는 라이프사이클 키. */
export type ChapterAggregateLifecycleKey = "PENDING" | "RESOLVED";

interface LifecycleCarrier {
  lifecycle?: {
    lifecycleKey?: string | null;
  } | null;
}

interface EntryCollapseInput {
  localOverride: boolean | null;
  forceCollapsed?: boolean | null;
  lifecycleKey?: string | null;
  disableLifecycleCollapse?: boolean;
  serverCollapsed: boolean;
}

interface ChapterCollapseInput {
  localOverride: boolean | null;
  aggregateLifecycleKey: ChapterAggregateLifecycleKey | null;
  serverCollapsed: boolean;
}

/**
 * 하위 엔트리가 하나 이상이고 모두 같은 명시적 상태일 때 챕터 집계 상태를 반환한다.
 * OPEN 또는 상태가 없는 엔트리와 PENDING/RESOLVED 혼합 목록은 집계하지 않는다.
 */
export function resolveChapterAggregateLifecycle(
  entries: readonly LifecycleCarrier[],
): ChapterAggregateLifecycleKey | null {
  if (entries.length === 0) return null;
  if (entries.every((entry) => entry.lifecycle?.lifecycleKey === "RESOLVED")) return "RESOLVED";
  if (entries.every((entry) => entry.lifecycle?.lifecycleKey === "PENDING")) return "PENDING";
  return null;
}

/**
 * 엔트리 접힘 우선순위를 적용한다.
 * 엔트리 자체 토글, 챕터 강제값, 라이프사이클 자동 접힘, 서버 COLLAPSED 순서다.
 */
export function resolveEntryCollapsed(input: EntryCollapseInput): boolean {
  if (input.localOverride !== null) return input.localOverride;
  if (input.forceCollapsed !== null && input.forceCollapsed !== undefined) return input.forceCollapsed;
  if (
    (input.lifecycleKey === "PENDING" || input.lifecycleKey === "RESOLVED")
    && !input.disableLifecycleCollapse
  ) {
    return true;
  }
  return input.serverCollapsed;
}

/** 로컬 토글, 하위 엔트리 집계, 서버 COLLAPSED 순서로 챕터 접힘을 결정한다. */
export function resolveChapterCollapsed(input: ChapterCollapseInput): boolean {
  if (input.localOverride !== null) return input.localOverride;
  if (input.aggregateLifecycleKey !== null) return true;
  return input.serverCollapsed;
}
