/**
 * useEntryRelatedContent.ts
 * 엔트리의 관련글(Related Content) 표시·해제·라벨 로직을 캡슐화한 composable.
 *
 * JournalEntryItem.vue 의 관련글 행 렌더, 연결 해제(DELETE /api/related/{id}),
 * 대상 원문 열기, 관계 유형·콘텐츠 유형 라벨 변환을 담당한다.
 *
 * 컴포넌트가 제공하는 guardAxisWrite · scrollAfterFetch 콜백에 의존해
 * 축 잠금 판정과 화면 갱신 트리거를 위임한다.
 */
import { ref, computed, type ComputedRef } from "vue";
import axios from "axios";
import { swalConfirm, swalRequestError, swalAjaxResult } from "@/shared/utils/swal";
import { useJournalModalStore } from "@/features/journal/stores/journalModal";
import type { JournalEntryDto, RelatedContentItem } from "@/features/journal/stores/journal";

export interface UseEntryRelatedContentOptions {
  /** 현재 엔트리 reactive 참조 (props.entry) */
  entry: ComputedRef<JournalEntryDto>;
  /** 축 쓰기 잠금 guard. false 반환 시 동작 중단. */
  guardAxisWrite: () => boolean;
  /** 액션 성공 후 화면 재조회+스크롤 트리거 */
  scrollAfterFetch: () => void;
  /** i18n t 함수 */
  t: (key: string) => string;
}

/**
 * 엔트리 관련글 표시·해제·라벨 로직.
 *
 * 반환값은 템플릿의 관련글 행과 해제 버튼에서 직접 사용된다.
 */
export function useEntryRelatedContent(options: UseEntryRelatedContentOptions) {
  const { entry, guardAxisWrite, scrollAfterFetch, t } = options;

  const modalStore = useJournalModalStore();

  // ──────────────────────────────────────────────
  // state
  // ──────────────────────────────────────────────

  /** 연결 해제 성공 직후 재조회 전에도 현재 행에서 제거된 관계를 숨긴다. */
  const unlinkedRelatedIds = ref<Set<number>>(new Set());

  // ──────────────────────────────────────────────
  // computed
  // ──────────────────────────────────────────────

  const relatedList = computed(() => (entry.value.relatedContentList ?? []).filter(
    (related) => related.id == null || !unlinkedRelatedIds.value.has(related.id),
  ));

  // ──────────────────────────────────────────────
  // labels
  // ──────────────────────────────────────────────

  /** 관련 콘텐츠 유형을 현재 locale 레이블로 변환한다. */
  function relatedContentTypeLabel(contentType: string): string {
    if (contentType === "JOURNAL_DIARY") return t("related-content.content-type.diary");
    if (contentType === "JOURNAL_DREAM") return t("related-content.content-type.dream");
    return contentType;
  }

  /** 관계 유형을 현재 locale 레이블로 변환한다. */
  function relationTypeLabel(relationType: string): string {
    const normalized = relationType.toLowerCase();
    if (["reference", "extension", "parallel", "cause"].includes(normalized)) {
      return t(`enum.relation-type.${normalized}`);
    }
    return relationType;
  }

  // ──────────────────────────────────────────────
  // actions
  // ──────────────────────────────────────────────

  /** 관련 엔트리 원문 열기 */
  function openRelatedTarget(targetId: number): void {
    void modalStore.openEntryView(targetId);
  }

  /** 관련 글 연결 해제. FLOW 축은 스레드 소속으로 수렴·제거됐으므로 목록에는 일반 관련글만 남는다. */
  async function unlinkRelated(related: RelatedContentItem): Promise<void> {
    if (!guardAxisWrite()) return;
    if (!related.id) {
      console.warn("[journal-entry] related content id missing for unlink", {
        entryId: entry.value.id,
        relationType: related.relationType,
        targetId: related.targetId,
      });
      return;
    }
    if (!await swalConfirm(t("related-content.unlink.confirm"))) return;
    try {
      const res = await axios.delete(`/api/related/${related.id}`);
      if (res.data?.rslt) {
        unlinkedRelatedIds.value = new Set([...unlinkedRelatedIds.value, related.id]);
        await swalAjaxResult({
          rslt: true,
          message: res.data?.message,
          successFallback: t("related-content.unlink.success"),
        });
        scrollAfterFetch();
      } else {
        console.warn("[journal-entry] related content unlink rejected", {
          entryId: entry.value.id,
          relatedContentId: related.id,
          relationType: related.relationType,
          message: res.data?.message,
        });
        void swalAjaxResult({
          rslt: false,
          message: res.data?.message,
          failureFallback: t("related-content.unlink.failure"),
        });
      }
    } catch (e: unknown) {
      console.error("[journal-entry] related content unlink failed", {
        entryId: entry.value.id,
        relatedContentId: related.id,
        relationType: related.relationType,
      }, e);
      void swalRequestError(e, t("related-content.unlink.failure"));
    }
  }

  return {
    relatedList,
    relatedContentTypeLabel,
    relationTypeLabel,
    openRelatedTarget,
    unlinkRelated,
  };
}
