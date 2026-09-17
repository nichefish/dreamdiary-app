import { reactive } from "vue";
import type { ChatMessage } from "@/features/chat/stores/chat";
import {
  ragMessageKey,
  type RagMetadata,
  type RagSource,
} from "@/features/chat/chatMessageFormat";

/** 미리보기로 노출하는 RAG source 최대 개수 */
const RAG_SOURCE_PREVIEW_LIMIT = 5;

/**
 * useRagSources
 *
 * assistant 메시지의 RAG source 목록 펼침 상태를 관리한다.
 * 기본은 message 별로 RAG_SOURCE_PREVIEW_LIMIT 까지만 노출하고, 펼치면 전체를 노출한다.
 */
export function useRagSources() {
  /** message key -> 펼침 여부 */
  const expandedRagSourceKeys = reactive<Record<string, boolean>>({});

  function visibleRagSources(
    chatMessage: ChatMessage,
    metadata: RagMetadata | null | undefined
  ): RagSource[] {
    const sources = metadata?.ragSources || [];
    if (expandedRagSourceKeys[ragMessageKey(chatMessage)]) return sources;
    return sources.slice(0, RAG_SOURCE_PREVIEW_LIMIT);
  }

  function hiddenRagSourceCount(
    chatMessage: ChatMessage,
    metadata: RagMetadata | null | undefined
  ): number {
    const total = metadata?.ragSources?.length || 0;
    if (expandedRagSourceKeys[ragMessageKey(chatMessage)]) return 0;
    return Math.max(0, total - RAG_SOURCE_PREVIEW_LIMIT);
  }

  function expandRagSources(chatMessage: ChatMessage): void {
    expandedRagSourceKeys[ragMessageKey(chatMessage)] = true;
  }

  return {
    visibleRagSources,
    hiddenRagSourceCount,
    expandRagSources,
  };
}
