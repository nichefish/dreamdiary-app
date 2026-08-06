import {
  computed,
  inject,
  provide,
  type ComputedRef,
  type InjectionKey,
  type MaybeRefOrGetter,
  toValue,
} from "vue";

/** 저널 일자 레이아웃이 provide 하는 리플렉션 기본 접힘 모드. 검색·스레드 등 provide 없는 화면은 false. */
export const JOURNAL_REFLECTION_DEFAULT_COLLAPSED_KEY: InjectionKey<ComputedRef<boolean>> =
  Symbol("journalReflectionDefaultCollapsed");

/** localStorage 키 — 리플렉션 기본 접힘 토글(표시 모드, 조회 필터 아님). */
export const REFLECTION_DEFAULT_COLLAPSED_STORAGE_KEY = "journal_reflection_default_collapsed";

export interface ReflectionCollapseInput {
  localOverride: boolean | null;
  forceSignal?: "expand" | "collapse" | null;
  lifecycleKey?: string | null;
  serverCollapsed: boolean;
  /** 일자 aside 토글 ON일 때 true. provide 없는 표면에서는 항상 false. */
  defaultCollapsed: boolean;
}

/**
 * 리플렉션 임베드 접힘 우선순위를 적용한다.
 * 로컬 토글 > 부모 signal > lifecycle(PENDING/RESOLVED) > 기본 접힘 모드 > 서버 COLLAPSED.
 */
export function resolveReflectionCollapsed(input: ReflectionCollapseInput): boolean {
  if (input.localOverride !== null) return input.localOverride;
  if (input.forceSignal === "expand") return false;
  if (input.forceSignal === "collapse") return true;
  if (input.lifecycleKey === "PENDING" || input.lifecycleKey === "RESOLVED") return true;
  if (input.defaultCollapsed) return true;
  return input.serverCollapsed;
}

/**
 * 저널 일자 레이아웃에서 리플렉션 기본 접힘 모드를 하위 트리에 provide 한다.
 * 토글 OFF·검색·스레드 등 provide 미설정 화면은 {@link useJournalReflectionDefaultCollapsed} 가 false 를 반환한다.
 */
export function provideJournalReflectionDefaultCollapsed(
  source: MaybeRefOrGetter<boolean>,
): ComputedRef<boolean> {
  const value = computed(() => !!toValue(source));
  provide(JOURNAL_REFLECTION_DEFAULT_COLLAPSED_KEY, value);
  return value;
}

/**
 * 일자 레이아웃 provide 값을 읽는다. provide 가 없으면 기존 계약(기본 펼침 시드)용 false.
 */
export function useJournalReflectionDefaultCollapsed(): ComputedRef<boolean> {
  return inject(
    JOURNAL_REFLECTION_DEFAULT_COLLAPSED_KEY,
    computed(() => false),
  );
}

/** localStorage 에서 토글 초기값을 읽는다. 없거나 파싱 불가면 false(기존 계약). */
export function readReflectionDefaultCollapsedFromStorage(): boolean {
  try {
    return window.localStorage.getItem(REFLECTION_DEFAULT_COLLAPSED_STORAGE_KEY) === "true";
  } catch {
    return false;
  }
}

/** 토글 값을 localStorage 에 저장한다. */
export function writeReflectionDefaultCollapsedToStorage(value: boolean): void {
  try {
    window.localStorage.setItem(REFLECTION_DEFAULT_COLLAPSED_STORAGE_KEY, String(value));
  } catch {
    // private mode 등 저장 실패는 세션 내 store 값만 유지
  }
}