/**
 * useEntryCollapse.ts
 * 엔트리의 접힘/펼침 상태 해석과 Reflection 전파 신호를 캡슐화한 composable.
 *
 * JournalEntryItem.vue 의 접힘 우선순위 체인(로컬 토글 > 챕터 강제 > lifecycle 자동 > 서버 COLLAPSED),
 * hasState 헬퍼, 리플렉션 forceCollapsedSignal 파생을 담당한다.
 *
 * toggleEntry 액션도 포함해 컴포넌트에서 접힘 관련 코드를 완전히 위임한다.
 */
import { ref, computed, type ComputedRef } from "vue";
import { resolveEntryCollapsed } from "@/features/journal/utils/journalLifecycleCollapse";
import type { JournalEntryDto } from "@/features/journal/stores/journal";

export interface UseEntryCollapseOptions {
  /** 현재 엔트리 reactive 참조 */
  entry: ComputedRef<JournalEntryDto>;
  /** 챕터 토글이 전파하는 강제 접힘 여부. null=챕터 미개입 */
  forceCollapsed: ComputedRef<boolean | null | undefined>;
  /** lifecycle 자동 접힘을 억제할지 여부 */
  disableLifecycleCollapse: ComputedRef<boolean | undefined>;
  /** lifecycle key computed (lcKey) — 외부에서 이미 파생하므로 재사용 */
  lcKey: ComputedRef<string>;
}

/**
 * 엔트리 접힘 상태 해석·토글·Reflection 신호 전파.
 *
 * 반환값은 템플릿의 isCollapsed 판정, 접힘 토글 버튼, Reflection embed prop 에 사용된다.
 */
export function useEntryCollapse(options: UseEntryCollapseOptions) {
  const { entry, forceCollapsed, disableLifecycleCollapse, lcKey } = options;

  // ──────────────────────────────────────────────
  // state
  // ──────────────────────────────────────────────

  /** 클라이언트 임시 접힘 오버라이드. null=서버 상태 따름, true=강제 접힘, false=강제 펼침 */
  const localCollapsedOverride = ref<boolean | null>(null);

  // ──────────────────────────────────────────────
  // helpers
  // ──────────────────────────────────────────────

  /** 엔트리의 state 목록에서 특정 stateKey 존재 여부를 반환한다. */
  function hasState(key: string): boolean {
    return (entry.value.state?.list ?? []).some((s) => s.stateKey === key);
  }

  // ──────────────────────────────────────────────
  // computed
  // ──────────────────────────────────────────────

  /** 서버 상태와 클라이언트 임시 오버라이드를 합산한 최종 접힘 여부.
   * 우선순위: 엔트리 자체 토글 > 챕터 강제 > lifecycle 자동 접힘 > 서버 COLLAPSED */
  const isCollapsed = computed(() => {
    return resolveEntryCollapsed({
      localOverride: localCollapsedOverride.value,
      forceCollapsed: forceCollapsed.value,
      lifecycleKey: lcKey.value,
      disableLifecycleCollapse: disableLifecycleCollapse.value,
      serverCollapsed: hasState("COLLAPSED"),
    });
  });

  /**
   * 리플렉션에 전달할 forceCollapsedSignal.
   * 엔트리가 현재 펼쳐져 있고, 자연 상태(lifecycle·서버)에서는 접혔을 것인데 누군가(자체 토글 또는 상위)가
   * 펼친 경우 "expand"를 전달한다. 자연 상태에서도 펼쳐져 있는 엔트리(OPEN + 서버 미접힘)면 null.
   */
  const reflectionForceSignal = computed<"expand" | "collapse" | null>(() => {
    if (localCollapsedOverride.value === false) return "expand";
    if (localCollapsedOverride.value === true) return "collapse";
    // 자연 상태에서 접혔을 엔트리가 상위 force로 펼쳐진 경우
    const wouldNaturallyCollapse = (lcKey.value === "PENDING" || lcKey.value === "RESOLVED")
      || hasState("COLLAPSED");
    if (wouldNaturallyCollapse && !isCollapsed.value) return "expand";
    return null;
  });

  // ──────────────────────────────────────────────
  // actions
  // ──────────────────────────────────────────────

  /** 클라이언트 전용 임시 접힘/펼침 토글 (서버 상태 무변경) */
  function toggleEntry(): void {
    localCollapsedOverride.value = !isCollapsed.value;
  }

  return {
    localCollapsedOverride,
    isCollapsed,
    reflectionForceSignal,
    hasState,
    toggleEntry,
  };
}
