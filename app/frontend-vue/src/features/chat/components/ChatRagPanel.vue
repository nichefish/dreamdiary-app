<template>
  <div
    class="chat-rag"
  >
    <details>
      <summary>
        <span>{{ tf("chat.rag.source-count", messageRagMetadata(message)?.ragSourceCount || 0) }}</span>
        <span class="chat-rag__intent">
          {{ messageRagMetadata(message)?.ragIntent || t("chat.rag.intent.default") }}
        </span>
        <span
          v-if="responseModeText(messageRagMetadata(message))"
          class="chat-rag__mode"
        >
          {{ responseModeText(messageRagMetadata(message)) }}
        </span>
        <span
          v-if="guardDetailText(messageRagMetadata(message))"
          class="chat-rag__guard"
        >
          {{ guardDetailText(messageRagMetadata(message)) }}
        </span>
      </summary>

      <div class="chat-rag__body">
        <div
          v-if="personFocusText(messageRagMetadata(message))"
          class="chat-rag__section"
        >
          <div class="chat-rag__label">{{ t("chat.rag.label.person-focus") }}</div>
          <div class="chat-rag__text">
            {{ personFocusText(messageRagMetadata(message)) }}
          </div>
        </div>

        <div
          v-if="topTagText(messageRagMetadata(message))"
          class="chat-rag__section"
        >
          <div class="chat-rag__label">{{ t("chat.rag.label.tag-summary") }}</div>
          <div class="chat-rag__text">
            {{ topTagText(messageRagMetadata(message)) }}
          </div>
        </div>

        <div
          v-if="tagPairText(messageRagMetadata(message))"
          class="chat-rag__section"
        >
          <div class="chat-rag__label">{{ t("chat.rag.label.tag-pair") }}</div>
          <div class="chat-rag__text">
            {{ tagPairText(messageRagMetadata(message)) }}
          </div>
        </div>

        <div
          v-if="timelineText(messageRagMetadata(message))"
          class="chat-rag__section"
        >
          <div class="chat-rag__label">{{ t("chat.rag.label.timeline") }}</div>
          <div class="chat-rag__text">
            {{ timelineText(messageRagMetadata(message)) }}
          </div>
        </div>

        <div
          v-if="messageRagMetadata(message)?.ragSources?.length"
          class="chat-rag__section"
        >
          <div class="chat-rag__label">{{ t("chat.rag.label.sources") }}</div>
          <div class="chat-rag-source-list">
            <button
              v-for="source in visibleRagSources(message, messageRagMetadata(message))"
              :key="`${source.rank}-${source.journalEntryId}`"
              type="button"
              class="chat-rag-source"
              :class="{ 'chat-rag-source--linkable': !!source.journalEntryId }"
              :title="
                source.journalEntryId
                  ? t('chat.rag.source.open-entry')
                  : undefined
              "
              :disabled="!source.journalEntryId"
              @click="openRagSourceEntry(source)"
            >
              <div class="chat-rag-source__meta">
                <span>{{ source.journalDate || t("chat.rag.date.missing") }}</span>
                <span>{{ source.contentKind || "UNKNOWN" }}</span>
                <span>{{ source.matchType || "MATCH" }}</span>
                <span v-if="typeof source.score === 'number'">
                  {{ formatScore(source.score) }}
                </span>
              </div>
              <div
                v-if="source.tags?.length"
                class="chat-rag-source__tags"
              >
                {{ source.tags.slice(0, 4).join(" ") }}
              </div>
              <div class="chat-rag-source__snippet">
                {{ source.snippet }}
              </div>
            </button>
            <button
              v-if="hiddenRagSourceCount(message, messageRagMetadata(message)) > 0"
              type="button"
              class="chat-rag-source-more"
              @click="expandRagSources(message)"
            >
              {{
                tf(
                  "chat.rag.source.more",
                  hiddenRagSourceCount(message, messageRagMetadata(message))
                )
              }}
            </button>
          </div>
        </div>
      </div>
    </details>
  </div>
</template>

<script lang="ts" setup>
import {
  formatScore,
  messageRagMetadata,
  tagPairText,
  timelineText,
  topTagText,
  type RagMetadata,
  type RagSource,
  guardDetailText as guardDetailTextOf,
  personFocusText as personFocusTextOf,
  responseModeText as responseModeTextOf,
  tf as tfOf,
} from "@/features/chat/chatMessageFormat";
import type { ChatMessage } from "@/features/chat/stores/chat";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { useRagSources } from "@/features/chat/useRagSources";
import { useJournalModalStore } from "@/features/journal/stores/journalModal";

/**
 * RAG 근거 패널: assistant 메시지의 metadataJson RAG 근거를 접이식 상세로 렌더한다.
 * 출처 건수·intent·responseMode·guard, personFocus·태그/타임라인, ragSources(score/snippet)를 표시하고,
 * journalEntryId 가 있는 출처 행은 읽기 전용 원문 모달로 연다. 출처 노출/펼침은 useRagSources 가 관리한다.
 * 렌더 게이트(isAssistantMessage && messageRagMetadata)는 상위 호출부(AppChat 메시지 행)가 소유한다.
 */
defineProps<{ message: ChatMessage }>();

const { t } = useLocaleStore();
const journalModalStore = useJournalModalStore();
const { visibleRagSources, hiddenRagSourceCount, expandRagSources } = useRagSources();

/** catalog 메시지의 {0}.. placeholder 를 순서대로 치환한다. (모듈 tf 에 로케일 t 주입) */
function tf(key: string, ...args: (string | number)[]): string {
  return tfOf(t, key, ...args);
}

/**
 * Opens the referenced journal entry in read-only view from a RAG source row.
 * Uses JournalEntryViewModal (global App.vue mount on non-popup routes).
 */
function openRagSourceEntry(source: RagSource): void {
  const entryId = source.journalEntryId;
  if (entryId == null || !Number.isFinite(Number(entryId))) {
    console.warn("[AppChat] RAG source missing journalEntryId", source);
    return;
  }
  void journalModalStore.openEntryView(Number(entryId));
}

/** template 바인딩 유지용 얇은 래퍼: 로케일 t 를 캡처해 모듈 impl 에 위임 */
function personFocusText(metadata: RagMetadata | null | undefined): string {
  return personFocusTextOf(metadata, t);
}

function responseModeText(metadata: RagMetadata | null | undefined): string {
  return responseModeTextOf(metadata, t);
}

function guardDetailText(metadata: RagMetadata | null | undefined): string {
  return guardDetailTextOf(metadata, t);
}
</script>

<style lang="scss" scoped>
.chat-rag {
  max-width: 100%;
  margin-top: 8px;
  color: #64748b;
  font-size: 11px;
  font-weight: 700;
}

.chat-rag details {
  max-width: 100%;
}

.chat-rag summary {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  max-width: 100%;
  padding: 5px 9px;
  border: 1px solid #dbeafe;
  border-radius: 999px;
  background: #f8fbff;
  color: #2563eb;
  cursor: pointer;
  list-style: none;
  transition: background 0.16s ease, border-color 0.16s ease;
}

.chat-rag summary::-webkit-details-marker {
  display: none;
}

.chat-rag summary:hover {
  border-color: #bfdbfe;
  background: #eff6ff;
}

.chat-rag__intent {
  color: #0f766e;
  font-size: 10px;
}

.chat-rag__mode {
  color: #b45309;
  font-size: 10px;
}

.chat-rag__body {
  margin-top: 8px;
  padding: 10px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #ffffff;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.06);
}

.chat-rag__section + .chat-rag__section {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #f1f5f9;
}

.chat-rag__label {
  margin-bottom: 4px;
  color: #334155;
  font-size: 10px;
  font-weight: 900;
  text-transform: uppercase;
}

.chat-rag__text {
  color: #64748b;
  font-size: 11px;
  line-height: 1.45;
  word-break: break-word;
}

.chat-rag-source-list {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.chat-rag-source {
  display: block;
  width: 100%;
  padding: 8px;
  border: 1px solid transparent;
  border-radius: 8px;
  background: #f8fafc;
  text-align: left;
  cursor: default;
}

.chat-rag-source--linkable {
  cursor: pointer;
  transition: border-color 0.16s ease, background 0.16s ease;
}

.chat-rag-source--linkable:hover {
  border-color: #bfdbfe;
  background: #eff6ff;
}

.chat-rag-source:disabled {
  opacity: 1;
  cursor: default;
}

.chat-rag-source-more {
  display: inline-flex;
  align-items: center;
  align-self: flex-start;
  padding: 4px 8px;
  border: 1px dashed #cbd5e1;
  border-radius: 999px;
  background: #ffffff;
  color: #2563eb;
  font-size: 10px;
  font-weight: 800;
  cursor: pointer;
}

.chat-rag-source-more:hover {
  border-color: #93c5fd;
  background: #eff6ff;
}

.chat-rag-source__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-bottom: 5px;
}

.chat-rag-source__meta span {
  padding: 2px 6px;
  border-radius: 999px;
  background: #e8f7f8;
  color: #0b7280;
  font-size: 10px;
  font-weight: 800;
}

.chat-rag-source__tags {
  margin-bottom: 4px;
  color: #2563eb;
  font-size: 10px;
  line-height: 1.4;
  word-break: break-word;
}

.chat-rag-source__snippet {
  color: #475569;
  font-size: 11px;
  font-weight: 600;
  line-height: 1.45;
  word-break: break-word;
}
</style>
