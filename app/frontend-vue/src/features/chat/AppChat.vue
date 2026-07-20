<template>
  <template v-if="authStore.isAuthenticated">
    <div class="app-chat-engage">
      <button
        class="app-chat-engage-btn"
        type="button"
        :title="t('chat.engage.tooltip')"
        @click="chat.toggle()"
      >
        <i class="ki-duotone ki-messages fs-1 pt-1 mb-2">
          <span class="path1"></span>
          <span class="path2"></span>
          <span class="path3"></span>
          <span class="path4"></span>
          <span class="path5"></span>
        </i>
        <span
          :class="[
            'bullet bullet-dot h-6px w-6px position-absolute translate-middle',
            chat.isConnected ? 'bg-success animation-blink' : 'bg-secondary',
          ]"
        ></span>
        {{ t("chat.engage.label") }}
      </button>
    </div>

    <div
      v-if="chat.isOpen"
      class="app-chat-drawer bg-body drawer drawer-end drawer-on"
    >
      <div class="chat-shell" id="chat_messenger">
        <div class="chat-shell__header">
          <div class="chat-shell__identity">
            <div class="chat-ai-avatar">
              <i class="ki-duotone ki-abstract-26 fs-2">
                <span class="path1"></span>
                <span class="path2"></span>
              </i>
            </div>
            <div class="chat-shell__title-wrap">
              <div class="chat-shell__title">{{ t("chat.assistant.name") }}</div>
              <div class="chat-shell__status">
                <span class="chat-shell__status-dot"></span>
                <span>{{ chat.isConnected ? t("chat.status.online") : t("chat.status.connecting") }}</span>
              </div>
            </div>
          </div>
          <button
            class="chat-icon-btn"
            id="kt_drawer_chat_close"
            type="button"
            :title="t('chat.action.close')"
            @click="chat.close()"
          >
            <i class="ki-duotone ki-cross fs-2">
              <span class="path1"></span>
              <span class="path2"></span>
            </i>
          </button>
        </div>

        <div class="chat-session-bar">
          <button
            class="chat-session-add-btn"
            type="button"
            :title="t('chat.action.new-chat')"
            :disabled="chat.isSessionLoading"
            @click="chat.createSession()"
          >
            <i class="ki-duotone ki-plus fs-2"></i>
          </button>
          <div class="chat-session-list">
            <button
              v-for="session in chat.sessions"
              :key="session.id"
              :class="[
                'chat-session-chip',
                {
                  'chat-session-chip--active':
                    session.id === chat.activeSessionId,
                  'chat-session-chip--renaming':
                    renamingSessionId === session.id,
                },
              ]"
              type="button"
              @click="onSessionChipClick(session)"
            >
              <span class="chat-session-chip__body">
                <input
                  v-if="renamingSessionId === session.id"
                  :ref="(el) => setRenameInputRef(session.id, el)"
                  v-model="renameDraft"
                  class="chat-session-chip__title-input"
                  type="text"
                  maxlength="200"
                  :placeholder="t('chat.session.rename.placeholder')"
                  :disabled="isRenamingSaving"
                  @click.stop
                  @dblclick.stop
                  @keydown.enter.prevent="commitRename(session)"
                  @keydown.esc.prevent="cancelRename"
                  @blur="commitRename(session)"
                />
                <span
                  v-else
                  class="chat-session-chip__title"
                  :title="t('chat.session.rename.hint')"
                  @dblclick.stop="startRename(session)"
                >
                  {{ sessionTitle(session) }}
                </span>
                <span
                  v-if="sessionTime(session) && renamingSessionId !== session.id"
                  class="chat-session-chip__time"
                >
                  {{ sessionTime(session) }}
                </span>
              </span>
              <span
                class="chat-session-chip__delete"
                :title="t('chat.action.delete')"
                @click.stop="chat.deleteSession(session.id)"
              >
                <i class="ki-duotone ki-trash fs-5">
                  <span class="path1"></span>
                  <span class="path2"></span>
                  <span class="path3"></span>
                  <span class="path4"></span>
                  <span class="path5"></span>
                </i>
              </span>
            </button>
            <div v-if="chat.sessions.length === 0" class="chat-session-empty">
              {{ chat.isSessionLoading ? t("chat.session.preparing") : t("chat.session.empty") }}
            </div>
          </div>
        </div>

        <div ref="messageList" class="chat-message-list">
          <div
            v-if="!chat.messages || chat.messages.length === 0"
            class="chat-empty-state"
          >
            <div class="chat-empty-state__mark">
              <i class="ki-duotone ki-message-text-2 fs-1">
                <span class="path1"></span>
                <span class="path2"></span>
                <span class="path3"></span>
              </i>
            </div>
            <div class="chat-empty-state__title">
              {{ t("chat.empty.prompt") }}
            </div>
            <div class="chat-empty-state__seeds" role="list">
              <button
                v-for="seed in emptySeedPrompts"
                :key="seed.key"
                type="button"
                class="chat-empty-seed"
                role="listitem"
                :disabled="chat.isWaitingResponse"
                @click="sendSeedPrompt(seed.text)"
              >
                {{ seed.text }}
              </button>
            </div>
          </div>

          <div
            v-for="(message, index) in chat.messages"
            :key="message.id || index"
            :class="['chat-message-row', messageRowClass(message)]"
          >
            <div v-if="!isOwnMessage(message)" class="chat-message-avatar">
              <template v-if="isAssistantMessage(message)">
                <span>{{ messageInitial(message) }}</span>
              </template>
              <template v-else-if="authStore.user?.profileImageUrl">
                <img
                  :src="authStore.user.profileImageUrl"
                  alt=""
                  @error="handleProfileImageError"
                />
              </template>
              <template v-else>
                <span>{{ messageInitial(message) }}</span>
              </template>
            </div>

            <div class="chat-message-stack">
              <div class="chat-message-meta">
                <span class="chat-message-name">{{ messageName(message) }}</span>
                <span v-if="messageTime(message)" class="chat-message-time">
                  {{ messageTime(message) }}
                </span>
              </div>
              <div
                v-if="isAssistantMessage(message)"
                :class="['chat-message-bubble', messageBubbleClass(message), 'chat-message-bubble--md']"
                v-html="assistantHtml(message)"
              ></div>
              <div
                v-else
                :class="['chat-message-bubble', messageBubbleClass(message)]"
              >
                {{ messageText(message) }}
              </div>
              <div
                v-if="isAssistantMessage(message) && messageRagMetadata(message)"
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
            </div>

            <div
              v-if="isOwnMessage(message)"
              class="chat-message-avatar chat-message-avatar--user"
            >
              <template v-if="authStore.user?.profileImageUrl">
                <img
                  :src="authStore.user.profileImageUrl"
                  alt=""
                  @error="handleProfileImageError"
                />
              </template>
              <template v-else>
                <span>{{ messageInitial(message) }}</span>
              </template>
            </div>
          </div>

          <div
            v-if="chat.isWaitingResponse"
            class="chat-message-row chat-message-row--assistant chat-message-row--pending"
          >
            <div class="chat-message-avatar">
              <span>{{ t("chat.assistant.initials") }}</span>
            </div>
            <div class="chat-message-stack">
              <div class="chat-message-meta">
                <span class="chat-message-name">{{ t("chat.assistant.name") }}</span>
              </div>
              <div
                v-if="chat.streamingContent"
                class="chat-message-bubble chat-message-bubble--assistant chat-message-bubble--streaming"
              >
                {{ chat.streamingContent }}
              </div>
              <div
                v-else
                class="chat-message-bubble chat-message-bubble--assistant chat-message-bubble--typing"
              >
                <span class="chat-typing-dot"></span>
                <span class="chat-typing-dot"></span>
                <span class="chat-typing-dot"></span>
                <span v-if="waitingPhaseLabel()" class="chat-typing-phase">
                  {{ waitingPhaseLabel() }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <div class="chat-composer">
          <div class="chat-composer__main">
            <textarea
              v-model="message"
              class="chat-composer__input"
              rows="2"
              :placeholder="t('chat.composer.placeholder')"
              @keydown.enter.exact.prevent="sendMessage"
            ></textarea>
            <div class="chat-composer__settings">
              <label class="chat-memory-select">
                <span>{{ t("chat.memory.label") }}</span>
                <select
                  v-model.number="chat.setting.recentMessageLimit"
                  :disabled="chat.isSettingSaving"
                  @change="updateMemoryLimit"
                >
                  <option
                    v-for="option in MEMORY_LIMIT_OPTIONS"
                    :key="option"
                    :value="option"
                  >
                    {{ tf("chat.memory.recent-count", option) }}
                  </option>
                </select>
              </label>
              <span v-if="chat.lastError" class="chat-error">
                {{ chat.lastError }}
              </span>
            </div>
          </div>
          <button
            :class="[
              'chat-send-btn',
              { 'chat-send-btn--waiting': chat.isWaitingResponse },
            ]"
            type="button"
            :disabled="!chat.isWaitingResponse && !message.trim()"
            @click="chat.isWaitingResponse ? chat.cancelMessage() : sendMessage()"
          >
            <i class="ki-duotone ki-send fs-2">
              <span class="path1"></span>
              <span class="path2"></span>
            </i>
            <span>{{ chat.isWaitingResponse ? t("chat.action.stop") : t("chat.action.send") }}</span>
          </button>
        </div>
      </div>
    </div>
  </template>
</template>

<script lang="ts" setup>
import { nextTick, onBeforeUnmount, reactive, ref, watch, computed } from "vue";
import { useAuthStore } from "@/shared/auth/stores/auth";
import {
  type ChatMessage,
  type ChatSession,
  MEMORY_LIMIT_OPTIONS,
  useChatStore,
} from "@/features/chat/stores/chat";
import { useJournalModalStore } from "@/features/journal/stores/journalModal";
import { handleProfileImageError } from "@/shared/utils/profileImage";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

const authStore = useAuthStore();
const { t } = useLocaleStore();

/** catalog 메시지의 {0}.. placeholder 를 순서대로 치환한다. */
function tf(key: string, ...args: (string | number)[]): string {
  let message = t(key);
  args.forEach((value, index) => {
    message = message.replace(`{${index}}`, String(value));
  });
  return message;
}
const chat = useChatStore();
const journalModalStore = useJournalModalStore();
const message = ref("");
const messageList = ref<HTMLElement | null>(null);
/** message key -> expanded source list (default shows RAG_SOURCE_PREVIEW_LIMIT) */
const expandedRagSourceKeys = reactive<Record<string, boolean>>({});
const RAG_SOURCE_PREVIEW_LIMIT = 5;
/** 세션 칩 제목 인라인 편집 중인 세션 ID */
const renamingSessionId = ref<number | null>(null);
const renameDraft = ref("");
const isRenamingSaving = ref(false);
const renameInputEls = new Map<number, HTMLInputElement>();
/** blur와 Esc 취소가 경합하지 않도록 커밋 중인지 표시 */
let renameCommitLocked = false;

interface RagCountItem {
  name?: string;
  count?: number;
}

interface RagTimelineSummary {
  sourceCount?: number;
  firstDate?: string;
  lastDate?: string;
  contentKinds?: RagCountItem[];
  months?: RagCountItem[];
}

interface RagTagSummary {
  totalTags?: RagCountItem[];
  dreamTags?: RagCountItem[];
  diaryTags?: RagCountItem[];
  noteTags?: RagCountItem[];
  tagPairs?: RagCountItem[];
}

interface RagSource {
  rank?: number;
  journalEntryId?: number;
  journalDate?: string;
  contentKind?: string;
  matchType?: string;
  score?: number;
  matchedTokens?: string[];
  tags?: string[];
  snippet?: string;
}

interface PersonFocusMetadata {
  target?: string;
  tokens?: string[];
  matchedSourceCount?: number;
  journalEntityId?: number;
  canonicalLabel?: string;
  mentionCount?: number;
  journalEntryCount?: number;
  firstDate?: string;
  lastDate?: string;
  contentKinds?: RagCountItem[];
  topRoles?: string[];
  roleAxesKo?: string[];
  surfaceForms?: string[];
  journalEntryIds?: number[];
}

interface RagMetadata {
  responseMode?: string;
  guardDetail?: string;
  retryGuardDetail?: string;
  ragIntent?: string;
  ragSourceCount?: number;
  personFocus?: PersonFocusMetadata;
  ragTagSummary?: RagTagSummary;
  ragTimelineSummary?: RagTimelineSummary;
  ragSources?: RagSource[];
}

const RESPONSE_MODE_KEYS: Record<string, string> = {
  PERSON_MEANING_FALLBACK: "chat.response-mode.person-meaning-fallback",
  PERSON_STANCE_FALLBACK: "chat.response-mode.person-stance-fallback",
  PERSON_APPEARANCE_FALLBACK: "chat.response-mode.person-appearance-fallback",
  PERSON_SYNTHESIS_HYBRID: "chat.response-mode.person-synthesis-hybrid",
  RULE_PRIMARY: "chat.response-mode.rule-primary",
  LANGUAGE_FALLBACK: "chat.response-mode.language-fallback",
  LLM: "chat.response-mode.llm",
};

function personRoleLabel(roleCode: string): string {
  const key = `chat.person-role.${roleCode}`;
  const label = t(key);
  return label === key ? roleCode : label;
}

watch(
  () => authStore.isAuthenticated,
  async (isAuthenticated) => {
    if (isAuthenticated) {
      try {
        await chat.initialize();
      } catch (error) {
        chat.lastError =
          error instanceof Error ? error.message : t("chat.error.init-failure");
      }
      return;
    }

    chat.reset();
  },
  { immediate: true }
);

watch(
  () => [chat.messages.length, chat.isWaitingResponse, chat.isOpen, chat.streamingContent],
  () => {
    scrollToBottom();
  }
);

onBeforeUnmount(() => {
  chat.reset();
});

function scrollToBottom(): void {
  nextTick(() => {
    if (!messageList.value) return;
    messageList.value.scrollTop = messageList.value.scrollHeight;
  });
}

function waitingPhaseLabel(): string {
  const phase = chat.responsePhase;
  if (phase === "SEARCHING") return t("chat.waiting.searching");
  if (phase === "GENERATING") return t("chat.waiting.generating");
  return "";
}

const EMPTY_SEED_KEYS = [
  "chat.empty.seed.1",
  "chat.empty.seed.2",
  "chat.empty.seed.3",
  "chat.empty.seed.4",
] as const;

const emptySeedPrompts = computed(() =>
  EMPTY_SEED_KEYS.map((key) => ({
    key,
    text: t(key),
  })).filter((seed) => !!seed.text && seed.text !== seed.key)
);

async function sendMessage(): Promise<void> {
  const nextMessage = message.value.trim();
  if (!nextMessage) return;

  message.value = "";
  await chat.sendMessage(nextMessage);
  scrollToBottom();
}

/**
 * 빈 세션 시드 질문을 즉시 전송한다. composer 입력값은 비운다.
 */
async function sendSeedPrompt(seedText: string): Promise<void> {
  const nextMessage = seedText.trim();
  if (!nextMessage || chat.isWaitingResponse) return;
  message.value = "";
  await chat.sendMessage(nextMessage);
  scrollToBottom();
}

function updateMemoryLimit(event: Event): void {
  const target = event.target as HTMLSelectElement;
  chat.updateSetting({
    ...chat.setting,
    recentMessageLimit: Number(target.value || 50),
  });
}

function sessionTitle(session: ChatSession): string {
  return session.title || t("chat.session.default-title");
}

function setRenameInputRef(sessionId: number, el: unknown): void {
  if (el instanceof HTMLInputElement) {
    renameInputEls.set(sessionId, el);
  } else {
    renameInputEls.delete(sessionId);
  }
}

function onSessionChipClick(session: ChatSession): void {
  if (renamingSessionId.value === session.id) return;
  chat.selectSession(session.id);
}

/**
 * 세션 칩 제목 인라인 편집을 시작한다.
 */
function startRename(session: ChatSession): void {
  if (!session.id || isRenamingSaving.value) return;
  renamingSessionId.value = session.id;
  renameDraft.value = session.title || "";
  renameCommitLocked = false;
  nextTick(() => {
    const input = renameInputEls.get(session.id);
    if (!input) return;
    input.focus();
    input.select();
  });
}

function cancelRename(): void {
  renameCommitLocked = true;
  renamingSessionId.value = null;
  renameDraft.value = "";
  isRenamingSaving.value = false;
}

/**
 * 인라인 제목 편집을 저장한다. 비어 있으면 취소한다.
 */
async function commitRename(session: ChatSession): Promise<void> {
  if (renameCommitLocked) return;
  if (renamingSessionId.value !== session.id) return;

  const nextTitle = renameDraft.value.trim();
  const currentTitle = (session.title || "").trim();
  if (!nextTitle) {
    chat.lastError = t("chat.session.rename.empty");
    cancelRename();
    return;
  }
  if (nextTitle === currentTitle) {
    cancelRename();
    return;
  }

  renameCommitLocked = true;
  isRenamingSaving.value = true;
  try {
    const updated = await chat.renameSession(session.id, nextTitle);
    if (!updated) {
      chat.lastError = t("chat.session.rename.failure");
    }
  } catch (e) {
    console.error("[AppChat] renameSession failed", { sessionId: session.id }, e);
    chat.lastError = t("chat.session.rename.failure");
  } finally {
    isRenamingSaving.value = false;
    renamingSessionId.value = null;
    renameDraft.value = "";
  }
}

function sessionTime(session: ChatSession): string {
  return session.lastMessageAt || session.createdAt || "";
}

function messageRole(chatMessage: ChatMessage): string {
  return (chatMessage.role || "").toUpperCase();
}

function isAssistantMessage(chatMessage: ChatMessage): boolean {
  const role = messageRole(chatMessage);
  return role === "ASSISTANT" || role === "AI" || role === "SYSTEM";
}

function isOwnMessage(chatMessage: ChatMessage): boolean {
  return !isAssistantMessage(chatMessage) && chatMessage.isCreatedBy === true;
}

function messageName(chatMessage: ChatMessage): string {
  if (isAssistantMessage(chatMessage)) return t("chat.assistant.name");
  return chatMessage.createdByNm || authStore.user?.nickname || t("chat.user.self");
}

function messageInitial(chatMessage: ChatMessage): string {
  if (isAssistantMessage(chatMessage)) return t("chat.assistant.initials");
  return messageName(chatMessage).slice(0, 1) || t("chat.user.initials");
}

function messageTime(chatMessage: ChatMessage): string {
  return chatMessage.createdAt || "";
}

function messageText(chatMessage: ChatMessage): string {
  return chatMessage.content || "";
}

/**
 * assistant 버블용 HTML. 서버 `markdownContent`(renderChatMarkdown)를 우선하고,
 * 구 메시지·폴백은 content 평문을 escape해 문단으로 감싼다.
 */
function assistantHtml(chatMessage: ChatMessage): string {
  const html = (chatMessage.markdownContent || "").trim();
  if (html && html !== "-") return html;
  const plain = messageText(chatMessage);
  if (!plain) return "";
  return `<p>${escapeHtml(plain).replace(/\n/g, "<br>")}</p>`;
}

function escapeHtml(value: string): string {
  return value
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

function messageRagMetadata(chatMessage: ChatMessage): RagMetadata | null {
  if (!chatMessage.metadataJson) return null;

  try {
    const metadata = JSON.parse(chatMessage.metadataJson) as RagMetadata;
    if (!metadata) return null;
    if (
      metadata.responseMode ||
      metadata.guardDetail ||
      metadata.retryGuardDetail ||
      metadata.personFocus ||
      (typeof metadata.ragSourceCount === "number" && metadata.ragSourceCount > 0)
    ) {
      return metadata;
    }
    return null;
  } catch {
    return null;
  }
}

function ragMessageKey(chatMessage: ChatMessage): string {
  return String(chatMessage.id ?? `${chatMessage.role}-${chatMessage.createdAt}-${chatMessage.content}`);
}

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

function personFocusText(metadata: RagMetadata | null | undefined): string {
  const personFocus = metadata?.personFocus;
  if (!personFocus) return "";

  const parts: string[] = [];
  const target = personFocus.target || personFocus.canonicalLabel;
  if (target) parts.push(tf("chat.rag.person.target", target));

  if (
    typeof personFocus.mentionCount === "number" ||
    typeof personFocus.journalEntryCount === "number"
  ) {
    parts.push(
      tf(
        "chat.rag.person.mentions",
        personFocus.mentionCount || 0,
        personFocus.journalEntryCount || 0
      )
    );
  }

  if (personFocus.firstDate || personFocus.lastDate) {
    parts.push(
      tf(
        "chat.rag.person.period",
        personFocus.firstDate || "?",
        personFocus.lastDate || "?"
      )
    );
  }

  const kinds = formatCountItems(personFocus.contentKinds, 4);
  if (kinds) parts.push(tf("chat.rag.person.content-kinds", kinds));

  const topRoles = formatRoleList(personFocus.topRoles, 4);
  if (topRoles) parts.push(tf("chat.rag.person.roles", topRoles));

  const roleAxesKo = formatTextList(personFocus.roleAxesKo, 4);
  if (roleAxesKo) parts.push(tf("chat.rag.person.role-axes", roleAxesKo));

  const surfaceForms = formatTextList(personFocus.surfaceForms, 4);
  if (surfaceForms) parts.push(tf("chat.rag.person.surface-forms", surfaceForms));

  return parts.join(" / ");
}

function responseModeText(metadata: RagMetadata | null | undefined): string {
  const mode = metadata?.responseMode;
  if (!mode) return "";
  const key = RESPONSE_MODE_KEYS[mode];
  return key ? t(key) : mode;
}

function guardDetailText(metadata: RagMetadata | null | undefined): string {
  const parts: string[] = [];
  const detail = metadata?.guardDetail;
  const retryDetail = metadata?.retryGuardDetail;
  if (detail) {
    parts.push(tf("chat.guard.prefix", detail));
  }
  if (retryDetail && retryDetail !== detail) {
    parts.push(tf("chat.guard.retry-prefix", retryDetail));
  }
  return parts.join(" · ");
}

function topTagText(metadata: RagMetadata | null | undefined): string {
  return formatCountItems(metadata?.ragTagSummary?.totalTags, 8);
}

function tagPairText(metadata: RagMetadata | null | undefined): string {
  return formatCountItems(metadata?.ragTagSummary?.tagPairs, 5);
}

function timelineText(metadata: RagMetadata | null | undefined): string {
  const timeline = metadata?.ragTimelineSummary;
  if (!timeline) return "";

  const parts: string[] = [];
  if (timeline.firstDate || timeline.lastDate) {
    parts.push(`${timeline.firstDate || "?"} ~ ${timeline.lastDate || "?"}`);
  }

  const kinds = formatCountItems(timeline.contentKinds, 4);
  if (kinds) parts.push(kinds);

  const months = formatCountItems(timeline.months, 4);
  if (months) parts.push(months);

  return parts.join(" · ");
}

function formatCountItems(items: RagCountItem[] | undefined, limit: number): string {
  if (!items || items.length === 0) return "";
  return items
    .slice(0, limit)
    .map((item) => `${item.name || "-"}(${item.count || 0})`)
    .join(", ");
}

function formatTextList(items: string[] | undefined, limit: number): string {
  if (!items || items.length === 0) return "";
  return items.slice(0, limit).join(", ");
}

function formatRoleList(items: string[] | undefined, limit: number): string {
  if (!items || items.length === 0) return "";

  return items
    .slice(0, limit)
    .map((item) => {
      const match = item.match(/^([A-Z_]+)\((\d+)\)$/);
      if (!match) return item;

      const [, roleCode, count] = match;
      const label = personRoleLabel(roleCode);
      return `${label}(${count})`;
    })
    .join(", ");
}

function formatScore(score: number): string {
  return Number.isFinite(score) ? score.toFixed(3) : "";
}

function messageRowClass(chatMessage: ChatMessage): string {
  if (isAssistantMessage(chatMessage)) return "chat-message-row--assistant";
  return isOwnMessage(chatMessage)
    ? "chat-message-row--own"
    : "chat-message-row--other";
}

function messageBubbleClass(chatMessage: ChatMessage): string {
  if (isAssistantMessage(chatMessage)) return "chat-message-bubble--assistant";
  return isOwnMessage(chatMessage)
    ? "chat-message-bubble--own"
    : "chat-message-bubble--other";
}
</script>

<style lang="scss" scoped>
.app-chat-engage {
  position: fixed;
  right: 18px;
  bottom: 28px;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  z-index: 601;
}

.app-chat-engage-btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-width: 112px;
  height: 48px;
  padding: 0 16px;
  border: 1px solid rgba(30, 41, 59, 0.1);
  border-radius: 999px;
  background: #ffffff;
  color: #1f2a44;
  box-shadow: 0 14px 34px rgba(31, 42, 68, 0.22);
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0;
  transition: transform 0.16s ease, box-shadow 0.16s ease,
    border-color 0.16s ease;

  &:hover {
    color: #0b5c6b;
    border-color: rgba(21, 150, 166, 0.35);
    box-shadow: 0 16px 42px rgba(31, 42, 68, 0.28);
    transform: translateY(-2px);
  }

  i {
    color: #1596a6;
    font-size: 22px;
  }

  .bullet {
    top: 11px !important;
    right: 14px !important;
  }
}

.app-chat-drawer {
  position: fixed !important;
  top: 78px !important;
  right: 18px !important;
  bottom: 22px !important;
  width: min(440px, calc(100vw - 28px)) !important;
  height: calc(100vh - 100px) !important;
  z-index: 6002;
  overflow: hidden;
  border: 1px solid rgba(30, 41, 59, 0.08);
  border-radius: 14px;
  box-shadow: 0 24px 70px rgba(15, 23, 42, 0.32);
}

.chat-shell {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  background: #f8fafc;
}

.chat-shell__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 18px;
  border-bottom: 1px solid rgba(30, 41, 59, 0.08);
  background: #ffffff;
}

.chat-shell__identity {
  display: flex;
  align-items: center;
  min-width: 0;
  gap: 12px;
}

.chat-ai-avatar,
.chat-message-avatar {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: #e8f7f8;
  color: #0b7280;
}

.chat-ai-avatar {
  width: 42px;
  height: 42px;
  border-radius: 12px;
}

.chat-shell__title-wrap {
  min-width: 0;
}

.chat-shell__title {
  overflow: hidden;
  color: #111827;
  font-size: 16px;
  font-weight: 800;
  line-height: 1.2;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-shell__status {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
}

.chat-shell__status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #17c653;
  box-shadow: 0 0 0 4px rgba(23, 198, 83, 0.12);
}

.chat-icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: 0;
  border-radius: 10px;
  background: transparent;
  color: #64748b;
  transition: background 0.16s ease, color 0.16s ease;

  &:hover {
    background: #f1f5f9;
    color: #0f172a;
  }
}

.chat-session-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border-bottom: 1px solid rgba(30, 41, 59, 0.08);
  background: #ffffff;
}

.chat-session-add-btn {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: 0;
  border-radius: 10px;
  background: #1596a6;
  color: #ffffff;
  transition: background 0.16s ease, opacity 0.16s ease;

  &:hover:not(:disabled) {
    background: #0b7280;
  }

  &:disabled {
    opacity: 0.5;
  }
}

.chat-session-list {
  flex: 1 1 auto;
  display: flex;
  gap: 8px;
  min-width: 0;
  overflow-x: auto;
  scrollbar-width: thin;
}

.chat-session-chip {
  flex: 0 0 154px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  min-width: 0;
  padding: 8px 10px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #f8fafc;
  color: #334155;
  text-align: left;
  transition: border-color 0.16s ease, background 0.16s ease,
    color 0.16s ease;

  &:hover {
    border-color: rgba(21, 150, 166, 0.35);
    background: #f0fbfc;
  }
}

.chat-session-chip__body {
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.chat-session-chip--active {
  border-color: rgba(21, 150, 166, 0.58);
  background: #e8f7f8;
  color: #0b7280;
}

.chat-session-chip__title,
.chat-session-chip__time {
  display: block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-session-chip__title {
  font-size: 12px;
  font-weight: 800;
}

.chat-session-chip__title-input {
  width: 100%;
  min-width: 0;
  padding: 0;
  border: 0;
  border-radius: 4px;
  background: #ffffff;
  color: #0f172a;
  font-size: 12px;
  font-weight: 800;
  line-height: 1.3;
  outline: 1px solid rgba(21, 150, 166, 0.55);
}

.chat-session-chip--renaming {
  border-color: rgba(21, 150, 166, 0.58);
  background: #e8f7f8;
}

.chat-session-chip__time {
  margin-top: 3px;
  color: #94a3b8;
  font-size: 10px;
  font-weight: 600;
}

.chat-session-chip__delete {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 8px;
  color: #94a3b8;
  opacity: 0;
  transition: background 0.16s ease, color 0.16s ease, opacity 0.16s ease;
}

.chat-session-chip:hover .chat-session-chip__delete,
.chat-session-chip--active .chat-session-chip__delete {
  opacity: 1;
}

.chat-session-chip__delete:hover {
  background: rgba(239, 68, 68, 0.1);
  color: #dc2626;
}

.chat-session-empty {
  display: flex;
  align-items: center;
  height: 34px;
  color: #94a3b8;
  font-size: 12px;
  font-weight: 700;
}

.chat-message-list {
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 0;
  padding: 20px 18px;
  overflow-y: auto;
}

.chat-empty-state {
  display: flex;
  flex: 1 1 auto;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 220px;
  color: #64748b;
  text-align: center;
}

.chat-empty-state__mark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  margin-bottom: 12px;
  border-radius: 18px;
  background: #ffffff;
  color: #1596a6;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.08);
}

.chat-empty-state__title {
  color: #334155;
  font-size: 14px;
  font-weight: 700;
}

.chat-empty-state__seeds {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  max-width: 420px;
  margin-top: 16px;
}

.chat-empty-seed {
  max-width: 100%;
  padding: 8px 12px;
  border: 1px solid #dbe4f0;
  border-radius: 999px;
  background: #ffffff;
  color: #334155;
  font-size: 12px;
  font-weight: 600;
  line-height: 1.4;
  text-align: left;
  cursor: pointer;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.05);
  transition: border-color 0.15s ease, background 0.15s ease, color 0.15s ease;
}

.chat-empty-seed:hover:not(:disabled) {
  border-color: #9ad0d8;
  background: #f4fbfd;
  color: #0f766e;
}

.chat-empty-seed:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.chat-message-row {
  display: flex;
  align-items: flex-end;
  gap: 10px;
}

.chat-message-row--own {
  justify-content: flex-end;
}

.chat-message-row--assistant,
.chat-message-row--other {
  justify-content: flex-start;
}

.chat-message-stack {
  display: flex;
  flex-direction: column;
  max-width: min(330px, calc(100% - 52px));
}

.chat-message-row--own .chat-message-stack {
  align-items: flex-end;
}

.chat-message-meta {
  display: flex;
  align-items: baseline;
  gap: 8px;
  max-width: 100%;
  margin-bottom: 6px;
  color: #64748b;
  font-size: 11px;
  font-weight: 600;
}

.chat-message-name {
  overflow: hidden;
  color: #334155;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-message-time {
  flex: 0 0 auto;
  color: #94a3b8;
}

.chat-message-bubble {
  max-width: 100%;
  padding: 11px 13px;
  border-radius: 14px;
  font-size: 13px;
  font-weight: 600;
  line-height: 1.55;
  letter-spacing: 0;
  white-space: pre-wrap;
  word-break: break-word;
}

.chat-message-bubble--own {
  border-bottom-right-radius: 5px;
  background: #1f2a44;
  color: #ffffff;
  box-shadow: 0 12px 30px rgba(31, 42, 68, 0.16);
}

.chat-message-bubble--assistant {
  border-bottom-left-radius: 5px;
  background: #ffffff;
  color: #172033;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.08);
}

.chat-message-bubble--md {
  white-space: normal;
}

.chat-message-bubble--md > :first-child {
  margin-top: 0;
}

.chat-message-bubble--md > :last-child {
  margin-bottom: 0;
}

.chat-message-bubble--md p {
  margin: 0 0 0.55em;
}

.chat-message-bubble--md p:last-child {
  margin-bottom: 0;
}

.chat-message-bubble--md .chat-md-ul,
.chat-message-bubble--md .chat-md-ol {
  margin: 0.35em 0 0.55em;
  padding-left: 1.25em;
}

.chat-message-bubble--md .chat-md-h {
  margin: 0.55em 0 0.35em;
  font-size: 1em;
  font-weight: 700;
  line-height: 1.4;
}

.chat-message-bubble--md .chat-md-code {
  padding: 0.1em 0.35em;
  border-radius: 4px;
  background: #f1f5f9;
  font-size: 0.92em;
}

.chat-message-bubble--md .chat-md-pre {
  margin: 0.45em 0;
  padding: 0.65em 0.75em;
  overflow-x: auto;
  border-radius: 8px;
  background: #0f172a;
  color: #e2e8f0;
  font-size: 0.88em;
  line-height: 1.45;
}

.chat-message-bubble--md .chat-md-pre code {
  padding: 0;
  background: transparent;
  color: inherit;
}

.chat-message-bubble--other {
  border-bottom-left-radius: 5px;
  background: #eef2f7;
  color: #172033;
}

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

.chat-message-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  font-size: 11px;
  font-weight: 800;
}

.chat-message-avatar--user {
  background: #fff3d6;
  color: #8a5a00;
}

.chat-message-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.chat-message-row--pending {
  margin-top: -6px;
}

.chat-message-bubble--streaming {
  white-space: pre-wrap;
  word-break: break-word;
}

.chat-message-bubble--typing {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  min-width: 58px;
  min-height: 38px;
}

.chat-typing-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #1596a6;
  opacity: 0.35;
  animation: chatTyping 1.2s ease-in-out infinite;
}

.chat-typing-dot:nth-child(2) {
  animation-delay: 0.15s;
}

.chat-typing-dot:nth-child(3) {
  animation-delay: 0.3s;
}

.chat-composer {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  padding: 14px;
  border-top: 1px solid rgba(30, 41, 59, 0.08);
  background: #ffffff;
}

.chat-composer__main {
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  min-width: 0;
  gap: 8px;
}

.chat-composer__input {
  width: 100%;
  min-width: 0;
  max-height: 116px;
  padding: 12px 14px;
  resize: none;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  outline: none;
  color: #111827;
  background: #f8fafc;
  font-size: 13px;
  font-weight: 600;
  line-height: 1.45;
  transition: border-color 0.16s ease, background 0.16s ease,
    box-shadow 0.16s ease;

  &:focus {
    border-color: rgba(21, 150, 166, 0.55);
    background: #ffffff;
    box-shadow: 0 0 0 4px rgba(21, 150, 166, 0.1);
  }
}

.chat-composer__settings {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 28px;
}

.chat-memory-select {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  max-width: 100%;
  color: #64748b;
  font-size: 11px;
  font-weight: 800;
}

.chat-memory-select select {
  height: 28px;
  max-width: 132px;
  padding: 0 26px 0 10px;
  border: 1px solid #e2e8f0;
  border-radius: 999px;
  background: #f8fafc;
  color: #334155;
  font-size: 11px;
  font-weight: 800;
  outline: none;
}

.chat-memory-select select:focus {
  border-color: rgba(21, 150, 166, 0.55);
  box-shadow: 0 0 0 3px rgba(21, 150, 166, 0.1);
}

.chat-error {
  min-width: 0;
  overflow: hidden;
  color: #dc2626;
  font-size: 11px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-send-btn {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-width: 86px;
  height: 44px;
  padding: 0 14px;
  border: 0;
  border-radius: 12px;
  background: #1596a6;
  color: #ffffff;
  font-size: 13px;
  font-weight: 800;
  transition: background 0.16s ease, opacity 0.16s ease,
    transform 0.16s ease;

  &:hover:not(:disabled) {
    background: #0b7280;
    transform: translateY(-1px);
  }

  &:disabled {
    cursor: not-allowed;
    opacity: 0.55;
  }
}

.chat-send-btn--waiting {
  background: #64748b;
}

@keyframes chatTyping {
  0%,
  80%,
  100% {
    opacity: 0.28;
    transform: translateY(0);
  }

  40% {
    opacity: 1;
    transform: translateY(-3px);
  }
}

@media (max-width: 575.98px) {
  .app-chat-engage {
    right: 14px;
    bottom: 18px;
  }

  .app-chat-drawer {
    top: 64px !important;
    right: 10px !important;
    bottom: 10px !important;
    width: calc(100vw - 20px) !important;
    height: calc(100vh - 74px) !important;
    border-radius: 12px;
  }

  .chat-shell__header {
    padding: 14px;
  }

  .chat-message-list {
    padding: 16px 14px;
  }

  .chat-message-stack {
    max-width: calc(100% - 44px);
  }

  .chat-composer {
    padding: 12px;
  }

  .chat-memory-select span {
    display: none;
  }

  .chat-send-btn {
    min-width: 48px;
    padding: 0 12px;
  }

  .chat-send-btn span {
    display: none;
  }
}
</style>
