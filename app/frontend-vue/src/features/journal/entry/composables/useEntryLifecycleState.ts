/**
 * useEntryLifecycleState.ts
 * 엔트리의 라이프사이클·상태·삭제 관련 로직을 캡슐화한 composable.
 *
 * JournalEntryItem.vue 의 라이프사이클 서브메뉴(OPEN/PENDING/RESOLVED),
 * 상태 토글(IMPRTC/REFRNC/NHTMR/HALLUC/COLLAPSED), 엔트리 삭제를 담당한다.
 *
 * setLifecycle·toggleState·캐시 컨텍스트는 공유 composable(useJournalAttachableActions)에 위임하고,
 * 메뉴 옵션 정의와 deleteEntry 만 이 파일에서 관리한다.
 */
import { computed, type ComputedRef } from "vue";
import axios from "axios";
import { swalConfirm, swalRequestError, swalAjaxResult } from "@/shared/utils/swal";
import { useJournalThreadStore } from "@/features/journal/stores/journalThread";
import { useJournalAttachableActions } from "@/features/journal/shared/composables/useJournalAttachableActions";
import type { JournalEntryDto } from "@/features/journal/stores/journal";

export interface UseEntryLifecycleStateOptions {
  /** 현재 엔트리 reactive 참조 (props.entry) */
  entry: ComputedRef<JournalEntryDto>;
  /** 꿈 엔트리 여부 (RESOLVED 색상 분기용) */
  isDreamEntry: ComputedRef<boolean>;
  /** 축 쓰기 잠금 guard. false 반환 시 동작 중단. */
  guardAxisWrite: () => boolean;
  /** 액션 성공 후 화면 재조회+스크롤 트리거 */
  scrollAfterFetch: (stdrdDt?: string) => void;
  /** i18n t 함수 */
  t: (key: string) => string;
}

/**
 * 엔트리 라이프사이클 설정·상태 토글·삭제 로직.
 *
 * 반환값은 템플릿의 라이프사이클/상태 서브메뉴와 삭제 핸들러에서 직접 사용된다.
 */
export function useEntryLifecycleState(options: UseEntryLifecycleStateOptions) {
  const { entry, isDreamEntry, guardAxisWrite, scrollAfterFetch, t } = options;

  const threadStore = useJournalThreadStore();

  // ──────────────────────────────────────────────
  // 공유 composable: setLifecycle, toggleState, cacheContext
  // ──────────────────────────────────────────────

  const contentType = computed(() => entry.value.contentType ?? "");

  const {
    entryCacheYy,
    entryCacheMnth,
    setLifecycle,
    toggleState,
  } = useJournalAttachableActions({
    entry,
    contentType,
    guardWrite: guardAxisWrite,
    onSuccess: () => scrollAfterFetch(),
    t,
    skipGuardStateKeys: ["COLLAPSED"],
  });

  // ──────────────────────────────────────────────
  // menu options
  // ──────────────────────────────────────────────

  /** 라이프사이클 옵션 (OPEN/PENDING/RESOLVED) */
  const lifecycleOptions = computed(() => [
    { key: "OPEN", label: t("journal.entry.lifecycle.open"), activeClass: "text-gray-800" },
    { key: "PENDING", label: t("lifecycle.pending"), activeClass: "text-gray-600" },
    { key: "RESOLVED", label: t("status.completed"), activeClass: isDreamEntry.value ? "text-dream" : "text-success" },
  ]);

  /** 상태 옵션 (중요/참조) */
  const statusOptions = computed(() => [
    { key: "IMPRTC", label: t("state.important"), activeClass: "text-danger" },
    { key: "REFRNC", label: t("state.reference"), activeClass: "text-warning" },
  ]);

  /** 꿈 전용 상태 옵션 (악몽/환각) */
  const dreamStatusOptions = computed(() => [
    { key: "NHTMR", label: t("state.nightmare"), activeClass: "text-info" },
    { key: "HALLUC", label: t("state.hallucination"), activeClass: "text-gray-700" },
  ]);

  // ──────────────────────────────────────────────
  // delete
  // ──────────────────────────────────────────────

  /** 엔트리 삭제 (DELETE /api/journal/entry/{id}) */
  async function deleteEntry(): Promise<void> {
    if (!guardAxisWrite()) return;
    if (!entry.value.id) return;
    const stdrdDt = entry.value.stdrdDt;
    const confirmed = await swalConfirm(t("common.confirm.del"));
    if (!confirmed) return;
    try {
      const res = await axios.delete(`/api/journal/entry/${entry.value.id}`);
      if (res.data?.rslt) {
        await swalAjaxResult({
          rslt: true,
          message: res.data?.message,
          successFallback: t("common.result.deleted"),
        });
        void threadStore.refreshPeriodSummary();
        scrollAfterFetch(stdrdDt);
      } else {
        void swalAjaxResult({
          rslt: false,
          message: res.data?.message,
          failureFallback: t("journal.entry.delete.failure"),
        });
      }
    } catch (e: unknown) {
      void swalRequestError(e);
    }
  }

  return {
    // computed (캐시·data 속성 바인딩용)
    entryCacheYy,
    entryCacheMnth,
    // menu options
    lifecycleOptions,
    statusOptions,
    dreamStatusOptions,
    // actions
    setLifecycle,
    toggleState,
    deleteEntry,
  };
}
