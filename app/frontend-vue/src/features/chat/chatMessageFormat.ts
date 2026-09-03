import type { ChatMessage } from "@/features/chat/stores/chat";

/**
 * chatMessageFormat
 *
 * AppChat 메시지·RAG 메타데이터 표시용 순수 포매팅/판정 함수 모음.
 * 로케일(t)·reactive 상태·store 에 의존하지 않으므로 단위 테스트로 계약을 고정한다.
 */

export interface RagCountItem {
  name?: string;
  count?: number;
}

export interface RagTimelineSummary {
  sourceCount?: number;
  firstDate?: string;
  lastDate?: string;
  contentKinds?: RagCountItem[];
  months?: RagCountItem[];
}

export interface RagTagSummary {
  totalTags?: RagCountItem[];
  dreamTags?: RagCountItem[];
  diaryTags?: RagCountItem[];
  noteTags?: RagCountItem[];
  tagPairs?: RagCountItem[];
}

export interface RagSource {
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

export interface PersonFocusMetadata {
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

export interface RagMetadata {
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

export function messageRole(chatMessage: ChatMessage): string {
  return (chatMessage.role || "").toUpperCase();
}

export function isAssistantMessage(chatMessage: ChatMessage): boolean {
  const role = messageRole(chatMessage);
  return role === "ASSISTANT" || role === "AI" || role === "SYSTEM";
}

export function isOwnMessage(chatMessage: ChatMessage): boolean {
  return !isAssistantMessage(chatMessage) && chatMessage.isCreatedBy === true;
}

export function messageTime(chatMessage: ChatMessage): string {
  return chatMessage.createdAt || "";
}

export function messageText(chatMessage: ChatMessage): string {
  return chatMessage.content || "";
}

export function escapeHtml(value: string): string {
  return value
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

/**
 * assistant 버블용 HTML. 서버 `markdownContent`(renderChatMarkdown)를 우선하고,
 * 구 메시지·폴백은 content 평문을 escape해 문단으로 감싼다.
 */
export function assistantHtml(chatMessage: ChatMessage): string {
  const html = (chatMessage.markdownContent || "").trim();
  if (html && html !== "-") return html;
  const plain = messageText(chatMessage);
  if (!plain) return "";
  return `<p>${escapeHtml(plain).replace(/\n/g, "<br>")}</p>`;
}

export function messageRagMetadata(chatMessage: ChatMessage): RagMetadata | null {
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

export function ragMessageKey(chatMessage: ChatMessage): string {
  return String(chatMessage.id ?? `${chatMessage.role}-${chatMessage.createdAt}-${chatMessage.content}`);
}

export function formatCountItems(items: RagCountItem[] | undefined, limit: number): string {
  if (!items || items.length === 0) return "";
  return items
    .slice(0, limit)
    .map((item) => `${item.name || "-"}(${item.count || 0})`)
    .join(", ");
}

export function formatTextList(items: string[] | undefined, limit: number): string {
  if (!items || items.length === 0) return "";
  return items.slice(0, limit).join(", ");
}

export function formatScore(score: number): string {
  return Number.isFinite(score) ? score.toFixed(3) : "";
}

export function topTagText(metadata: RagMetadata | null | undefined): string {
  return formatCountItems(metadata?.ragTagSummary?.totalTags, 8);
}

export function tagPairText(metadata: RagMetadata | null | undefined): string {
  return formatCountItems(metadata?.ragTagSummary?.tagPairs, 5);
}

export function timelineText(metadata: RagMetadata | null | undefined): string {
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

export function messageRowClass(chatMessage: ChatMessage): string {
  if (isAssistantMessage(chatMessage)) return "chat-message-row--assistant";
  return isOwnMessage(chatMessage)
    ? "chat-message-row--own"
    : "chat-message-row--other";
}

export function messageBubbleClass(chatMessage: ChatMessage): string {
  if (isAssistantMessage(chatMessage)) return "chat-message-bubble--assistant";
  return isOwnMessage(chatMessage)
    ? "chat-message-bubble--own"
    : "chat-message-bubble--other";
}

/** catalog 키를 문자열로 해석하는 번역 함수. (호출부에서 주입) */
export type TranslateFn = (key: string) => string;

/** catalog 메시지의 {0}.. placeholder 를 순서대로 치환한다. */
export function tf(t: TranslateFn, key: string, ...args: (string | number)[]): string {
  let message = t(key);
  args.forEach((value, index) => {
    message = message.replace(`{${index}}`, String(value));
  });
  return message;
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

export function personRoleLabel(roleCode: string, t: TranslateFn): string {
  const key = `chat.person-role.${roleCode}`;
  const label = t(key);
  return label === key ? roleCode : label;
}

export function formatRoleList(
  items: string[] | undefined,
  limit: number,
  t: TranslateFn
): string {
  if (!items || items.length === 0) return "";

  return items
    .slice(0, limit)
    .map((item) => {
      const match = item.match(/^([A-Z_]+)\((\d+)\)$/);
      if (!match) return item;

      const [, roleCode, count] = match;
      const label = personRoleLabel(roleCode, t);
      return `${label}(${count})`;
    })
    .join(", ");
}

export function personFocusText(
  metadata: RagMetadata | null | undefined,
  t: TranslateFn
): string {
  const personFocus = metadata?.personFocus;
  if (!personFocus) return "";

  const parts: string[] = [];
  const target = personFocus.target || personFocus.canonicalLabel;
  if (target) parts.push(tf(t, "chat.rag.person.target", target));

  if (
    typeof personFocus.mentionCount === "number" ||
    typeof personFocus.journalEntryCount === "number"
  ) {
    parts.push(
      tf(
        t,
        "chat.rag.person.mentions",
        personFocus.mentionCount || 0,
        personFocus.journalEntryCount || 0
      )
    );
  }

  if (personFocus.firstDate || personFocus.lastDate) {
    parts.push(
      tf(
        t,
        "chat.rag.person.period",
        personFocus.firstDate || "?",
        personFocus.lastDate || "?"
      )
    );
  }

  const kinds = formatCountItems(personFocus.contentKinds, 4);
  if (kinds) parts.push(tf(t, "chat.rag.person.content-kinds", kinds));

  const topRoles = formatRoleList(personFocus.topRoles, 4, t);
  if (topRoles) parts.push(tf(t, "chat.rag.person.roles", topRoles));

  const roleAxesKo = formatTextList(personFocus.roleAxesKo, 4);
  if (roleAxesKo) parts.push(tf(t, "chat.rag.person.role-axes", roleAxesKo));

  const surfaceForms = formatTextList(personFocus.surfaceForms, 4);
  if (surfaceForms) parts.push(tf(t, "chat.rag.person.surface-forms", surfaceForms));

  return parts.join(" / ");
}

export function responseModeText(
  metadata: RagMetadata | null | undefined,
  t: TranslateFn
): string {
  const mode = metadata?.responseMode;
  if (!mode) return "";
  const key = RESPONSE_MODE_KEYS[mode];
  return key ? t(key) : mode;
}

export function guardDetailText(
  metadata: RagMetadata | null | undefined,
  t: TranslateFn
): string {
  const parts: string[] = [];
  const detail = metadata?.guardDetail;
  const retryDetail = metadata?.retryGuardDetail;
  if (detail) {
    parts.push(tf(t, "chat.guard.prefix", detail));
  }
  if (retryDetail && retryDetail !== detail) {
    parts.push(tf(t, "chat.guard.retry-prefix", retryDetail));
  }
  return parts.join(" · ");
}
