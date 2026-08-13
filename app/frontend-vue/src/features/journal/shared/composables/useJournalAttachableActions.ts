/**
 * useJournalAttachableActions.ts
 * Entry·Reflection 공통 — 라이프사이클 설정(PUT /api/lifecycles)과 상태 토글(POST /api/states)을 공유한다.
 *
 * 두 컴포넌트(JournalEntryItem, JournalReflectionItem)가 동일한 API 호출 패턴을 쓰되
 * guard 방식과 성공 후 refresh 경로만 다르므로, 그 차이를 콜백으로 받는다.
 *
 * 캐시 컨텍스트(yy/mnth/weekStartDt) 해석도 동일 패턴이라 함께 제공한다.
 */
import { computed, type ComputedRef } from "vue";
import axios from "axios";
import { swalRequestError, swalFire } from "@/shared/utils/swal";
import { useJournalStore } from "@/features/journal/stores/journal";
import { getWeekStartDateStr } from "@/features/journal/utils/journalDate";
import type { JournalEntryDto } from "@/features/journal/stores/journal";

export interface UseJournalAttachableActionsOptions {
  /** 대상 엔트리/리플렉션 reactive 참조 */
  entry: ComputedRef<JournalEntryDto>;
  /** contentType 을 외부에서 확정. Entry 는 entry.contentType, Reflection 은 고정 "JOURNAL_REFLECTION". */
  contentType: ComputedRef<string>;
  /** 쓰기 guard. false 반환 시 동작 중단. */
  guardWrite: () => boolean;
  /** 액션 성공 후 화면 재조회 트리거 */
  onSuccess: () => void;
  /** i18n t 함수 */
  t: (key: string) => string;
  /**
   * guard 를 스킵할 stateKey 목록.
   * Entry 의 COLLAPSED 는 축 잠금과 무관하므로 guard 를 안 탄다.
   * Reflection 은 빈 배열(모두 guard).
   */
  skipGuardStateKeys?: string[];
}

export interface JournalCacheContext {
  yy?: number;
  mnth?: number;
  weekStartDt?: string;
}

/**
 * 라이프사이클 설정·상태 토글·캐시 컨텍스트 해석 공유 로직.
 */
export function useJournalAttachableActions(options: UseJournalAttachableActionsOptions) {
  const { entry, contentType, guardWrite, onSuccess, t, skipGuardStateKeys = [] } = options;

  const journalStore = useJournalStore();

  // ──────────────────────────────────────────────
  // cache context
  // ──────────────────────────────────────────────

  function parseCacheNumber(value: unknown): number | undefined {
    const parsed = typeof value === "number" ? value : Number(value);
    return Number.isFinite(parsed) && parsed > 0 ? parsed : undefined;
  }

  function resolveEntryYy(): number | undefined {
    const stdrdDt = entry.value.stdrdDt?.trim();
    if (stdrdDt && stdrdDt.length >= 4) return parseCacheNumber(stdrdDt.slice(0, 4));
    return parseCacheNumber(journalStore.yy);
  }

  function resolveEntryMnth(): number | undefined {
    const stdrdDt = entry.value.stdrdDt?.trim();
    if (stdrdDt && stdrdDt.length >= 7) return parseCacheNumber(stdrdDt.slice(5, 7));
    return parseCacheNumber(journalStore.mnth);
  }

  function resolveEntryWeekStartDt(): string | undefined {
    const weekStartDt = journalStore.weekStartDt?.trim();
    if (weekStartDt) return weekStartDt;
    const stdrdDt = entry.value.stdrdDt?.trim();
    return stdrdDt ? getWeekStartDateStr(stdrdDt) : undefined;
  }

  /** 템플릿 data-yy 바인딩용 */
  const entryCacheYy = computed(() => resolveEntryYy());
  /** 템플릿 data-mnth 바인딩용 */
  const entryCacheMnth = computed(() => resolveEntryMnth());

  function resolveJournalCacheContext(): JournalCacheContext {
    const cacheContext: JournalCacheContext = {};
    const yy = entryCacheYy.value;
    const mnth = entryCacheMnth.value;
    const weekStartDt = resolveEntryWeekStartDt();
    if (yy != null) cacheContext.yy = yy;
    if (mnth != null) cacheContext.mnth = mnth;
    if (weekStartDt) cacheContext.weekStartDt = weekStartDt;
    if (Object.keys(cacheContext).length === 0) {
      console.warn("[journal] missing cache context for state update", {
        id: entry.value.id,
        contentType: contentType.value,
        stdrdDt: entry.value.stdrdDt,
      });
    }
    return cacheContext;
  }

  // ──────────────────────────────────────────────
  // actions
  // ──────────────────────────────────────────────

  /** 라이프사이클 설정 (PUT /api/lifecycles) */
  async function setLifecycle(lifecycleKey: string): Promise<void> {
    if (!guardWrite()) return;
    if (!entry.value.id) return;
    try {
      const res = await axios.put("/api/lifecycles", {
        id: entry.value.id,
        contentType: contentType.value,
        lifecycleKey,
        cacheContext: resolveJournalCacheContext(),
      });
      if (res.data?.rslt) {
        onSuccess();
      } else {
        void swalFire({ icon: "error", text: res.data?.message ?? t("common.result.failure") });
      }
    } catch (e: unknown) {
      void swalRequestError(e);
    }
  }

  /** 상태 토글 (POST /api/states) */
  async function toggleState(stateKey: string): Promise<void> {
    if (!skipGuardStateKeys.includes(stateKey) && !guardWrite()) return;
    if (!entry.value.id) return;
    try {
      const res = await axios.post("/api/states", {
        id: entry.value.id,
        contentType: contentType.value,
        stateKey,
        cacheContext: resolveJournalCacheContext(),
      });
      if (res.data?.rslt) {
        onSuccess();
      } else {
        void swalFire({ icon: "error", text: res.data?.message ?? t("common.result.failure") });
      }
    } catch (e: unknown) {
      void swalRequestError(e);
    }
  }

  return {
    entryCacheYy,
    entryCacheMnth,
    resolveJournalCacheContext,
    setLifecycle,
    toggleState,
  };
}
