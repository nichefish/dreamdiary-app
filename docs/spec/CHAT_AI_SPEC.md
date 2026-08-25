# AI Chat Spec

## Purpose

Dreamdiary AI Chat is a record-aware assistant.

It must help the user continue thinking with their own journal data, but it must not pretend to know facts that are not present in the current conversation or retrieved records.

## Current Architecture

LLM 클라이언트·설정·RAG intent/검색 병합·person focus/snapshot·Path C hybrid·언어/person 가드(`ResponseGuardService`)는 `feature/ai`에 두고, 세션 오케스트레이션·시스템/의도 프롬프트·세션 컨텍스트 로딩은 `feature/chat`에 둔다. 목표 분리는 [AI_DOMAIN_SEPARATION.md](AI_DOMAIN_SEPARATION.md)를 본다.

### Client

| Area | File | Responsibility |
| --- | --- | --- |
| Chat drawer UI | `app/frontend-vue/src/features/chat/AppChat.vue` | Floating AI chat panel, session list, composer, message rendering |
| Chat store | `app/frontend-vue/src/features/chat/stores/chat.ts` | Session/message state, REST calls, STOMP send/cancel |
| Admin operations UI | `app/frontend-vue/src/features/admin/AdminPage.vue` / `app/frontend-vue/src/features/admin/stores/adminPage.ts` | Show embedding backfill and entity queue stats / sync controls for operators |
| Mobile chat | `app/mobile-react-native/src/screens/AIChatScreen.tsx` | Native chat screen using the same chat API and STOMP contract |

### Server

| Area | File | Responsibility |
| --- | --- | --- |
| WebSocket endpoint | `ChatController` | STOMP send/cancel handling |
| Chat orchestration | `ChatOrchestrator` | Save user message, delegate RAG to `RagContextService`, detect Path C, load session messages, inject `ResponseGuardService`, call LLM/hybrid, save/broadcast assistant message |
| RAG context | `RagContextService` / `RagContextTextBuilder` | Intent, merged/tag-only retrieval, context text, tag/timeline summaries |
| Session management | `ChatSessionService` | Session ownership, default prompt, last-message timestamp/title, manual title rename (`PATCH /chat/sessions/{id}`) |
| Message history | `ChatMessageService` | Recent context messages |
| LLM client | `feature/ai/client/OllamaClient` | Ollama chat and embedding API calls (`AiChatMessage` role/content) |
| Ollama settings | `feature/ai/config/OllamaProperties` | `app.ollama.*` base URL and model names (`application.yml`, profile overrides) |
| LLM message (internal) | `feature/ai/model/AiChatMessage` | role/content only; chat maps from `ChatMessageDto` |
| Ollama health | `feature/ai/model/OllamaHealthDto` | Admin/eval Ollama readiness |
| System prompt | `feature/ai/prompt/SystemPromptBuilder` | Session base + response-guard + RAG journal block |
| Intent prompt | `feature/ai/prompt/IntentPromptResolver` | LOOKUP/SUMMARY/SYNTHESIS/stance/appearance intent appendices + intent-classify |
| Memory search | `JournalEntryEmbeddingSearchService` | In-memory keyword + vector search over journal embeddings |
| Entity focus summary | `JournalEntityFocusService` | Resolve entity-catalog-backed person summaries for synthesis questions |
| Entity sync queue | `JournalEntryEntityQueueService` / `JournalEntryEntityWorker` | Queue and asynchronously refresh entity refs and mention roles after journal entry writes |
| Entity queue admin API | `JournalEntryEntityAdminRestController` | Expose queue stats, full requeue sync, and failed-row requeue for operators |
| RAG search result | `RagSearchResult` | Internal retrieval DTO carrying entity, match type, score, matched tokens, and snippet |
| RAG intent | `feature/ai/rag/RagIntent` | Classifies retrieval mode as `LOOKUP`, `SUMMARY`, or `SYNTHESIS` |
| RAG intent rules | `feature/ai/rag/RagIntentClassifier` | Heuristic LOOKUP/SUMMARY/SYNTHESIS (person-about flag from chat) |
| RAG search facade | `feature/ai/rag/RagSearchFacade` | Intent detect (heuristic+LLM), keyword/vector merge, topK/minScore, tag-first merge |
| Person focus DTO | `feature/ai/person/PersonFocus` | Person-meaning focus tokens, match count, entity catalog summary |
| Person query rules | `feature/ai/person/PersonQueryClassifier` | Person-meaning/attitude/appearance/about-lookup heuristics and focus token extraction |
| Person focus resolver | `feature/ai/person/PersonFocusResolver` | Resolve focus, person-tag merge, source priority, tag filtering |
| Person snapshot | `feature/ai/person/PersonSnapshotService` | Person-meaning/stance SNAPSHOT aggregation and evidence budgets |
| Person Path C hybrid | `feature/ai/person/PersonSynthesisHybridService` | SYNTHESIS person hybrid prompt/LLM/retry/rule-primary; guards via `PersonSynthesisGuardPort` |
| Response guard | `feature/ai/guard/ResponseGuardService` | Language/person hollow·stance guards; implements `PersonSynthesisGuardPort` for Path C and chat LLM path |
| Person synthesis result | `feature/ai/person/PersonSynthesisResult` | Path C content/responseMode/guardDetail mapping target for chat |

### Ollama Configuration

Chat and embedding model names are **not** hardcoded in `OllamaClient`. They bind from Spring configuration:

| Property | Default | Used by |
| --- | --- | --- |
| `app.ollama.base-url` | `http://localhost:11434` | `OllamaClient` HTTP calls, `GET /api/admin/ollama/health` |
| `app.ollama.chat-model` | `qwen2.5:7b` | General chat, person-meaning retry, `PERSON_SYNTHESIS_HYBRID` |
| `app.ollama.embedding-model` | `nomic-embed-text` | RAG query embedding, journal backfill worker, quality eval |
| `app.ollama.chat-temperature` | `0.35` | Chat `options.temperature` (factual RAG bias) |
| `app.ollama.num-predict` | `768` | Chat `options.num_predict` max tokens |
| `app.ollama.connect-timeout-ms` | `5000` | Ollama HTTP connect timeout |
| `app.ollama.read-timeout-ms` | `300000` | Ollama HTTP read timeout (14B local generation) |
| `app.journal.embedding.cache-on-startup` | `true` | Load persisted `EMBEDDED` vectors into the in-memory RAG cache during application startup; the test profile sets it to `false` |

Local override example (`application-local.yml`):

```yaml
app:
  ollama:
    chat-model: qwen2.5:14b
```

Health check compares installed Ollama tags against the **configured** chat and embedding model names.


### Authentication Boundary

- `GET /chat` is the only public HTTP route under `/chat`; `WebSocketAuthInterceptor` validates its JWT or existing Principal during the WebSocket handshake.
- Chat settings, sessions, and message REST routes under `/chat/**` require an authenticated Spring Security context and return the common JSON 401 response when unauthenticated.
- Message history has one ownership-scoped route: `GET /chat/sessions/{sessionId}/messages`. The former general search route `GET /chat/messages` is removed because it did not establish session ownership and had no client caller.
- STOMP send and cancel handlers use the authenticated handshake session attributes.
- STOMP cancel validates ownership through `ChatSessionService` before changing the session cancellation flag.
- Session expiration notifications use the authenticated user's `/user/queue/session-invalid` destination. They must never be broadcast through a shared topic because one user's destroyed session must not invalidate another user's chat UI.

## Message Flow

```text
User message
  -> WebSocket /app/chat/session/{sessionId}/send
  -> ChatOrchestrator.processChat()
  -> save USER chat_message
  -> broadcast USER message
  -> broadcast PROGRESS phase=SEARCHING
  -> build RAG context from journal_entry_embedding
      -> classify RagIntent
      -> keyword search for names/tags/proper nouns
      -> vector search for semantic similarity
      -> return RagSearchResult metadata
      -> merge keyword results first, vector results second by journalEntryId
      -> log selected source IDs, match types, scores, tokens, and snippets
  -> broadcast PROGRESS phase=GENERATING
  -> build guarded system prompt
  -> main LLM path: OllamaClient.chatStream(...) broadcasts DELTA chunks on the same session topic
      -> hybrid / person-retry / language-guard retry still use non-streaming OllamaClient.chat(...)
  -> language guard validation (Accept-Language / LocaleContextHolder) on the completed text
      -> ko: if Chinese/Han script is detected, retry once with stricter Korean-only prompt (non-stream)
      -> en: if excessive Hangul or Han script is detected, retry once with English-only prompt (non-stream)
      -> if retry still violates guard, replace with locale catalog fallback (`chat.ai.language-fallback.*`)
  -> strip leftover internal RAG citations ([N] / [N] 기록); keep markdown symbols in stored content
  -> save ASSISTANT chat_message with RAG source metadataJson
  -> mapstruct sets markdownContent via MarkdownUtils.renderChatMarkdown(content)
  -> broadcast ASSISTANT message (client replaces the temporary streaming bubble with the final message)
```

### Progress Events

While `processChat` runs, the server may broadcast non-message progress payloads on the same session topic:

```json
{ "rslt": true, "rsltObj": { "type": "PROGRESS", "sessionId": 1, "phase": "SEARCHING" } }
```

Phases:

| Phase | When |
| --- | --- |
| `SEARCHING` | Immediately before RAG context build |
| `GENERATING` | After RAG context is ready, before LLM / hybrid response generation |

Clients must not append progress payloads to the message list. The chat drawer shows the phase next to the typing indicator (`chat.waiting.searching` / `chat.waiting.generating`) and clears it when the assistant message arrives or the user cancels.

While `GENERATING` runs on the **main LLM path**, the server may also broadcast token/chunk deltas:

```json
{ "rslt": true, "rsltObj": { "type": "DELTA", "sessionId": 1, "delta": "..." } }
```

Clients accumulate `delta` into a temporary assistant bubble (`streamingContent`) and must not push DELTA events into `messages`. When the final ASSISTANT message arrives (or the user cancels / switches session), the temporary bubble is cleared. Hybrid and retry paths do not emit DELTA.

## i18n (server chat responses)

User-visible rule-primary / guard / clarification strings in `ChatOrchestrator` use `MessageUtils` + `chat.ai.*` keys in `messages_ko.properties` / `messages_en.properties`.

| Key prefix | Purpose |
| --- | --- |
| `chat.ai.guard.*` | LLM system guard + language retry appendices (`responseGuardPrompt()`, `languageRetryPrompt()`) |
| `chat.ai.clarify.*` | Deterministic fallback when person focus / RAG context is insufficient |
| `chat.ai.language-fallback.*` | Safe assistant bubble when LLM output fails language guard |
| `chat.ai.lead.*` | Rule-primary interpretive lead sentences |
| `chat.ai.hint.content-kind.*` | Content-kind hints inside lead body assembly |
| `chat.ai.fallback.*` | Rule-primary deterministic fallback bodies (`buildPerson*DeterministicFallback`), section labels, lead-body fragments, linked-context role hints, content-kind spread labels |
| `chat.ai.lead.person-appearance` | person-appearance interpretive lead (uses `buildPersonMeaningLeadBody` + appearance framing) |
| `chat.ai.prompt.*` | LLM system/RAG/intent prompt bodies (`buildSystemPromptWithRag`, `buildIntentPrompt`, synthesis RAG intros). D.3a–c: intent, tag/timeline, hybrid/SNAPSHOT; D.3d: `appendPersonGuardRetryHint`, `buildPersonMeaningRetryPrompt`, `buildPersonStanceRetryPrompt` (`chat.ai.prompt.rag.retry.*`) |

Locale follows `AcceptHeaderLocaleResolver` (axios `Accept-Language` from Vue). Korean NLP keyword lists for intent/person detection remain Korean-only; English UI gets English guard prompts and English user-facing fallbacks.

Client chat shell labels use the separate `chat.*` namespace served via `/i18n/{locale}.json`.

## Memory Model

### Session Memory

The active chat session is the boundary of conversation memory.

The LLM receives:

```text
system prompt
+ response guard rules
+ optional RAG journal context
+ recent N messages from the same session
```

It must not mix messages from another chat session.

The chat drawer UI must keep these two limits visually distinct:

| UI label | Setting / metadata | Meaning |
| --- | --- | --- |
| `저널 검색 N건` | `ragSourceCount` | Journal entries retrieved for the current question via RAG |
| `대화 기억 최근 N개` | `recentMessageLimit` | Recent chat messages from the same session included in LLM context. Chat drawer select options: `25`, `50`, `100`, `200`. Server clamp: `2`–`200`. Default for new settings: `50`. |

They are unrelated numbers and must not be presented as if they should match.

### Journal RAG Memory

Journal records are retrieved by intent-aware hybrid search.

```text
user query
  -> classify intent: LOOKUP, SUMMARY, SYNTHESIS
  -> extract direct keyword candidates for names/tags/proper nouns
  -> weighted keyword match against tags, chapter fields, embedding_text, and payload
  -> Ollama embedding model: `app.ollama.embedding-model` (default `nomic-embed-text`)
  -> cosine similarity against cached journal_entry_embedding vectors
  -> retrieval_weight applied to vector results
  -> current user's records only
  -> min score threshold applied to vector results
  -> keyword results merged first, vector results second
  -> top K records converted to plain context text
```

Current constants:

| Constant | Value | Meaning |
| --- | --- | --- |
| `RAG_TOP_K` (admin `chat_setting.rag_top_k`) | default `5` | Maximum records for `LOOKUP` (ADMIN GLOBAL; `PATCH /admin/chat/settings`) |
| `RAG_SUMMARY_TOP_K` (admin `chat_setting.rag_summary_top_k`) | default `12` | Maximum records for `SUMMARY` |
| `RAG_SYNTHESIS_TOP_K` (admin `chat_setting.rag_synthesis_top_k`) | default `25` | Maximum records for non-stance `SYNTHESIS` |
| `PERSON_STANCE_RAG_TOP_K` (admin `chat_setting.rag_stance_top_k`) | default `50` | Maximum records for person-stance (attitude) retrieval |
| `RAG_MIN_SCORE` (admin `chat_setting.rag_min_score`) | default `0.35` | Minimum weighted vector similarity for `LOOKUP`/`SUMMARY` |
| `RAG_SYNTHESIS_MIN_SCORE` (admin `chat_setting.rag_synthesis_min_score`) | default `0.25` | Wider vector threshold for `SYNTHESIS` |
| `rag_enabled` (admin) | default `true` | When false, `buildRagContext` returns empty and chat continues without journal RAG |
| `RAG_TEXT_MAX_LENGTH` | `300` | Max characters per retrieved record |
| `RAG_SYNTHESIS_TEXT_MAX_LENGTH` | `220` | Max snippet characters per synthesis source |
| `RAG_PERSON_FOCUS_SNIPPET_MAX_LENGTH` | `400` | Max snippet characters for person-focused synthesis sources |
| `RAG_ENTITY_LINK_MAX` | `10` | Reserved constant; person-meaning retrieval no longer injects entity-catalog entries |

RAG context must be omitted when no result passes the threshold.

### Entity Sync Freshness

`personFocus` now depends on an asynchronous entity sync queue rather than an in-request write hook.

```text
journal entry save/modify/content update
  -> queue entity sync row
  -> scheduler notices pending jobs
  -> worker regenerates journal_entry_entity_ref + journal_entry_entity_role
  -> later AI chat reads refreshed personFocus/topRoles
```

This means `personFocus` and `topRoles` are eventually consistent. Immediately after a journal entry write, AI chat can briefly observe the previous entity summary until the queued worker finishes processing that entry.

Server startup enqueues the same embedding queue sync job as Admin `Sync Entries` when `app.journal.embedding.sync-on-startup=true` (default). Vector generation still runs asynchronously via the embedding worker scheduler after queue rows exist.

Admin queue visibility is available through:

- `GET /api/admin/journal-entry-embeddings/stats`
- `POST /api/admin/journal-entry-embeddings/sync`
- `POST /api/admin/journal-entry-embeddings/requeue-failed`
- `GET /api/admin/journal-entry-embeddings/quality-eval` — 한국어 임베딩 품질 실측 (Ollama 필요). 상세: `JOURNAL_ENTRY_EMBEDDING_DESIGN.md` § 품질 실측
- `GET /api/admin/ollama/health` — 로컬 Ollama 연결·필수 모델 점검 (자동 기동 없음). 상세: `JOURNAL_ENTRY_EMBEDDING_DESIGN.md` § Ollama health
- `GET /api/admin/journal-entry-entities/stats`
- `POST /api/admin/journal-entry-entities/sync`
- `POST /api/admin/journal-entry-entities/requeue-failed`

Admin stats use `total` as the active journal entry count (Entries baseline). `queueRows` and `unqueuedEntries` expose how many queue rows exist versus entries not yet queued. Progress bars use entry coverage (`embedded/total`, `synced/total`), not queue-row self-percentages.

The existing Vue admin page surfaces embedding and entity queue status with:

- Refresh
- Requeue Failed
- Sync Entries
- Entries / Embedded(or Synced) / Unqueued / Pending stat cards
- entry coverage progress plus queue completion secondary rate

The full sync endpoint requeues every current `journal_entry` into the entity queue and removes stale queue rows whose source entry disappeared.

Keyword search does not use the vector threshold. This is intentional: short person-name queries such as `민수님에 대해 뭘 말해줄 수 있니` can have weak semantic similarity even when the exact name appears in records.

Keyword search is field-weighted because tags are the strongest "what this entry is about" signal in Dreamdiary.

Current keyword weights:

| Field group | Weight |
| --- | --- |
| Tags | `5` |
| Entry title, chapter title, chapter Prefix | `3` |
| Body / full embedding text / payload fallback | `1` |

### Embedding Text

`journal_entry_embedding.embedding_text` should represent the record the way a user remembers it, not only the prose body.

Current fields included:

- content kind
- journal date
- chapter title
- chapter Prefix (`journalChapterPrefixId`, `journalChapterPrefixName`; system summary role is `journalChapterSummaryYn`)
- tags as `[category]#tag`
- repeated tag signal lines (`핵심 태그`, `주제 태그`, `태그`) so embedding search also treats tags as strong topic hints
- entry title
- entry body
- whether it is another person's dream
- dream provider name

Existing embeddings created before this schema change must be re-synced and re-embedded before the new tag/chapter fields become searchable.

### Retrieval Result DTO

RAG search returns `RagSearchResult` internally.

Fields:

- `entity`: selected `JournalEntryEmbeddingEntity`
- `matchType`: `KEYWORD` or `VECTOR`
- `score`: keyword match count or weighted vector similarity
- `matchedTokens`: direct keyword tokens that matched the record
- `tags`: tags extracted from the source payload
- `snippet`: short debug/source preview text

This keeps retrieval evidence available after search. `ChatOrchestrator` uses it for merging, context construction, logs, and persisted message metadata.

### Message Metadata

Assistant messages persist RAG source evidence to `chat_message.metadata_json`.

Shape:

```json
{
  "ragIntent": "SYNTHESIS",
  "ragSourceCount": 25,
  "personFocus": {
    "target": "민수",
    "tokens": ["민수", "민수님"],
    "matchedSourceCount": 8,
    "journalEntityId": 7,
    "canonicalLabel": "민수",
    "mentionCount": 12,
    "journalEntryCount": 9,
    "firstDate": "2026-01-02",
    "lastDate": "2026-05-29",
    "contentKinds": [
      { "name": "DREAM", "count": 6 },
      { "name": "DIARY", "count": 3 }
    ],
    "topRoles": ["COLLABORATION(4)", "TENSION(3)"],
    "surfaceForms": ["민수님", "민수"],
    "journalEntryIds": [123, 124, 130]
  },
  "ragTagSummary": {
    "totalTags": [
      { "name": "[정서실험]#민수", "count": 8 }
    ],
    "dreamTags": [],
    "diaryTags": [],
    "noteTags": [],
    "tagPairs": [
      { "name": "[관계]#공연 ↔ [정서실험]#민수", "count": 3 }
    ]
  },
  "ragTimelineSummary": {
    "sourceCount": 25,
    "firstDate": "2026-01-02",
    "lastDate": "2026-05-29",
    "contentKinds": [
      { "name": "DREAM", "count": 12 },
      { "name": "DIARY", "count": 10 }
    ],
    "months": [
      { "name": "2026-05", "count": 8 }
    ]
  },
  "ragSources": [
    {
      "rank": 1,
      "journalEntryId": 123,
      "journalDate": "2026-05-29",
      "contentKind": "DREAM",
      "matchType": "KEYWORD",
      "score": 1.0,
      "matchedTokens": ["민수"],
      "tags": ["[정서실험]#민수", "[관계]#공연"],
      "snippet": "..."
    }
  ]
}
```

This metadata is available through the existing chat message DTO and WebSocket payload.

The chat drawer (`AppChat.vue`) renders a collapsible RAG details block under each assistant message when `metadataJson` carries RAG evidence:

- summary chip: `ragSourceCount`, `ragIntent`, optional `responseMode` / guard detail
- sections: `personFocus` (compact human-readable string), tag summary, tag pairs, timeline
- `ragSources` list: date, content kind, match type, score, tags, snippet (default first 5; expandable via “+N more”)
- each source row with `journalEntryId` opens read-only `JournalEntryViewModal` via `journalModal.openEntryView(id)` (global mount on non-popup routes in `App.vue`; optional Edit switches to `JournalEntryRegistModal`)

It still does not expose raw `ragSources`/`personFocus` JSON as a separate structured inspector.

`personFocus` is optional. It is present only when a synthesis question is recognized as a person-meaning question and the entity catalog resolves a matching `PERSON` row or the prompt aid still has matched RAG sources to prioritize.

### RAG Intent

`ChatOrchestrator` classifies user questions before retrieval.

Modes:

| Intent | Use case | Retrieval shape |
| --- | --- | --- |
| `LOOKUP` | Find or answer about a concrete item/person/tag | small top-K, direct context blocks |
| `SUMMARY` | Summarize a set of records | wider top-K, compact source lines |
| `SYNTHESIS` | Interpret patterns, meanings, symbols, emotional arcs, whole context | widest top-K, lower vector threshold, compact source lines |

First-pass intent detection lives in `RagIntentClassifier` (pure rules; `ChatOrchestrator#detectRagIntent` supplies the person-about flag and delegates to `RagSearchFacade#detectIntent`). Priority order:

1. Person-about lookup (`isPersonAboutLookupQuery`) → `SYNTHESIS`
2. Explicit search cues (`찾아줘`, `검색해`, `어디에 있`, …) → `LOOKUP` (beats synthesis/summary keywords)
3. Synthesis cues (`의미`, `통섭`, `엮`, `상징`, `패턴`, `흐름`, `반복`, `변화`, `전체 맥락`, `해석`, `어떻게 생각`, `생각하고`, `어떤 감정`, `어떤 마음`, `어떤 느낌`, `어떻게 느끼`, `느끼고`, …) → `SYNTHESIS`
4. Summary verbs (`요약`, `정리`, `모아`, `묶어`, `전체적으로`, `한번에`, `돌아봐`) → `SUMMARY`
5. `최근` alone is **not** `SUMMARY`; `최근` + a summary companion (`요약`/`정리`/`흐름`/`패턴`/…) → `SUMMARY`
6. Else → `LOOKUP`

When the first-pass heuristic sees **both** SUMMARY and SYNTHESIS cues (and no LOOKUP-force cue), `RagSearchFacade#detectIntent` runs an **ambiguity-gated LLM second pass**: one non-streaming Ollama call with `chat.ai.prompt.intent-classify` that must answer `LOOKUP` / `SUMMARY` / `SYNTHESIS` only. Parse failure or Ollama error falls back to the heuristic label. Person-about and LOOKUP-force paths never call the second pass.

Person-meaning hint detection (`isPersonMeaningQuery`) also treats `어떻게 생각`, `생각하고`, `어떤 감정`, `어떤 마음`, and `어떤 느낌` as person-centric synthesis signals. Example: `나는 민수님을 어떻게 생각하고 있니?` must route to `SYNTHESIS` + person-meaning tag-only retrieval, not `LOOKUP`.

### Person-Stance Questions

When a `SYNTHESIS` question is also a **first-person attitude** question (`isPersonAttitudeQuery`), the user is asking how *they* feel or think about someone in their journal — not for a third-person personality profile or workplace analysis.

Detection requires all of:

- a extracted person focus token (for example `민수` from `민수님`)
- an explicit first-person **subject** marker: `나는`, `내가`, `나의`, `나한테`, or `나에게` — **not** locative scope alone (`내 대화`, `내 기록`)
- an attitude hint such as `어떻게 생각`, `생각하고`, `어떤 감정`, `어떤 마음`, `어떤 느낌`, `어떻게 느끼`, or `느끼고` (all now routed to `SYNTHESIS`/Path C by `detectRagIntent` + `PERSON_FOCUS_HINTS`)
- **not** a person-appearance query (`등장`, `나타나`, `보여`, or `내 대화/내 기록` scope without `나는/내가`)

Example (stance): `나는 민수님을 어떻게 생각하고 있니?`

**Not stance** — use person-meaning / appearance prompt instead: `내 대화에서 지연님은 어떤 느낌으로 등장하고 있니?`

This path reuses person-meaning tag-only retrieval but runs a **rich-trust** prompt/guard (Option A). The design goal is to read as much journal text as possible and let the LLM speak, rather than force a rigid template:

- RAG intro text asks for attitude **pattern synthesis** grounded in the retrieved records.
- The hybrid prompt for attitude questions asks for **free-form prose** (no forced numbered sections), 2nd-person framing (`네가`, `내 태도`, `내 마음`), reading many evidence scenes, continuing the user's own journal analysis notes, and writing at length. The **only** hard content rule is anti-fabrication: do not assert facts/roles/relationships absent from the records (say `기록만으로는 확실치 않다` instead).
- **Live acceptance gate** for attitude answers is `isDegradedPersonStanceRichResponse`: it rejects **only** blank, too-short (<40 chars), scaffold-field leaks, and record-ungrounded generic org buckets (`isGenericPersonBucketHallucination`). Formatting (4 sections), coaching/advisory tone, org narrative, 3rd-person subject dominance, episode narration, direct quotes, and psych labels are **no longer rejection reasons** for attitude answers. Korean-only language guard still applies upstream.
- `describePersonStanceRichGuardFailure` maps the minimal gate to codes `person_stance_too_short`, `person_stance_scaffold_leak`, `person_stance_generic_bucket`.
- The legacy stance guard detectors (`isHollowPersonStanceResponse`, `isPersonStanceCoachingTone`, `isPersonStanceMissingSectionShape`, `isPersonStanceGenericOrgNarrative`, `hasPersonStanceMirrorMarkers`, and their marker-constant arrays, etc.) have been **removed** (convergence). Only `isThirdPersonPersonalityProfile` remains — it is shared by the non-attitude person-meaning hollow guard.
- degraded retries use `PERSON_STANCE_RETRY`, itself **rich-trust**: it no longer re-imposes the 4-section / anti-tone regime — it only asks the model to cite more evidence scenes, keep 2nd-person framing, and avoid empty org generalities. Retry-hint codes are `person_stance_too_short` / `_scaffold_leak` / `_generic_bucket`. The deterministic stance fallback (`buildPersonStanceDeterministicFallback`, surfaced as `responseMode=RULE_PRIMARY` from Path C) is likewise **rich-trust prose** (no 4-section dump; see below), reached only when the minimal gate fails (blank/short/leak/generic-bucket).
- `buildPersonStanceInterpretiveLead` / `RULE_PRIMARY` stance fallback open with `네가 기록에 남긴 태도로 보면,` plus tag/context aggregation — not `기록상 {인물}은(는)` third-person subject
- The stance `RULE_PRIMARY` fallback (Path C) is now **rich-trust prose** (Option A ②-B): the `buildPersonStanceInterpretiveLead` 2nd-person lead (tag/context aggregation) + one honest caveat sentence (`상대의 성격이나 조직에서의 역할처럼 기록에 직접 안 적힌 건 단정하기 어려워`) + up to **3** evidence snippets joined inline as prose (`이런 장면들이 그렇게 느끼게 했어: …`). The old numbered `(1) 내 태도·정서` / `(2) 반복 패턴` / `(3) 함께 묶인 축` / `(4) 확정 불가` section helpers have been **removed** (convergence). Meaning/appearance rule-primary fallbacks keep their existing structured shapes.
- **Routing convergence (③):** every `isPersonAttitudeQuery` phrasing (including `어떻게 느끼`/`느끼고`) now classifies as `SYNTHESIS` + person-meaning, so attitude questions are answered **and** fall back only through Path C. The legacy `resolveLlmChatResponse` degraded path no longer has an attitude branch and no longer emits `responseMode=PERSON_STANCE_FALLBACK`; it now serves only LOOKUP person-meaning/appearance and general chat. `PERSON_STANCE_FALLBACK` survives as a **legacy** responseMode value for pre-convergence `chat_message` rows (the chat UI still maps it for historical display).
- **Unresolved-person edge (rich-trust, F1/F2):** an attitude question can bypass Path C only when `personFocus` cannot be resolved at all (no person tags, no record mentions, no entity summary). That case takes the general prompt path, where the `buildIntentPrompt` attitude branch is now **rich-trust** too: free-form 2nd-person prose, anti-fabrication only, and an instruction to say honestly when the records hold no cue about the person (plus a short clarifying question). The old strict regime (4-item scaffold skeleton, advisory/tone bans, mandatory person-tag citation) is removed. No person guard applies on this path (`responseMode=LLM`). The `PERSON_STANCE_SCAFFOLD` context block builder was removed entirely (convergence): Path C never consumed `RagContext.text`, and the legacy path can never have a resolved `personFocus`, so the block could not reach any LLM prompt. (The remaining `PERSON_FOCUS` / `PERSON_MEANING_SCAFFOLD` context blocks were removed for the same reason in F4 — see the person-meaning retrieval section.)

### Person Synthesis Hybrid (Path C)

When `shouldUseRulePrimaryPersonSynthesisResponse` is true — `SYNTHESIS` intent, resolved `personFocus`, and `isPersonMeaningQuery` — the server runs **Path C**:

1. Build `PersonMeaningSnapshot` from tag-only (or focused) RAG sources (same material as rule-primary). Default snapshot includes up to **3** evidence snippets (`PERSON_MEANING_SNAPSHOT_EVIDENCE_LIMIT`), each ~100 chars. **Person-stance (attitude) questions** use a richer budget: RAG up to **50** entries (`PERSON_STANCE_RAG_TOP_K`) — applied in **both** the tag-only retrieval and the merged fallback retrieval (`RagContextService` merged path passes `queryText` to `resolveTopK`; F3 alignment) — snapshot evidence up to **20** snippets (`PERSON_STANCE_SNAPSHOT_EVIDENCE_LIMIT`), ~400 chars each, **8000** total char budget, sampled across tagged sources (not only the first rows).
2. Call Ollama **once** with `PERSON_SYNTHESIS_HYBRID` system prompt containing only the snapshot block (no full journal dump). **Recent session messages (up to 5)** may be included as follow-up context; full history is not injected. **Attitude questions use the rich-trust prompt**: free-form prose (no forced sections), 2nd-person framing, read many evidence scenes, do not assert facts/roles absent from the records. Local `app.ollama.num-predict` may be raised (e.g. 2048) so deep answers are not truncated.
3. Apply the Korean language guard, then the person degradation gate (`isDegradedPersonResponse`). **Attitude answers use the minimal `isDegradedPersonStanceRichResponse` gate** (blank/short/scaffold-leak/generic-bucket only); meaning/appearance keep the fuller hollow guards. On failure, **one** retry with `PERSON_MEANING_RETRY` / `PERSON_STANCE_RETRY` appendix that includes the first `guardDetail` reason. Stance retries include the richer evidence list.
4. If the LLM answer passes guards → `responseMode=PERSON_SYNTHESIS_HYBRID`.
5. If guards still fail or no tagged sources exist → `buildRulePrimaryPersonSynthesisResponse` and `responseMode=RULE_PRIMARY`. `metadataJson.guardDetail` records the **first** guard failure code; when a retry still fails, `metadataJson.retryGuardDetail` records the retry failure code (chat UI shows `guard: … · retry: …`). Rule-primary fallbacks use the same snapshot budget as hybrid for the query type (stance: up to **20** snippets / 400 chars (tag-only RAG **50**); others: **3** / 100 chars). Meaning/appearance rule-primary label the evidence block `근거 장면:`; **stance rule-primary now uses inline prose snippets** (`이런 장면들이 그렇게 느끼게 했어: …`, up to 3) instead of a labeled block.

`isPersonMeaningQuery` also matches person-about questions when a person token is extracted and the query contains hints such as `에 대해`, `뭘 말해`, `알려줘`. Those questions route to `SYNTHESIS` via `detectRagIntent`.

Appearance / meaning question types use structured rule-primary fallback shapes; **attitude uses rich-trust prose** in both hybrid and fallback. In all cases the LLM is asked to write natural interpretive prose grounded in the snapshot.

User-facing tag lines in snapshot and fallbacks use `formatTopTagsForDisplay`. Short person tokens disambiguate via `resolveDominantPersonTagStem`. Tag-only paths retain `personFocus.entitySummary` for canonical display labels while aggregation uses tagged sources (`isTagOnlyPersonMeaningResults`).

The legacy full-RAG LLM path (with hollow guard, retry, and `PERSON_*_FALLBACK` modes) remains for non-person synthesis and `LOOKUP`/`SUMMARY` questions.


### Person-Appearance Questions

When `isPersonAppearanceQuery` is true (dialogue/record scope + how someone **appears**), routing stays on **person-meaning** (Path C snapshot with the appearance four-section prompt), not person-stance. Intent prompt forbids quote parades, trait conclusions (`친근하다`, `자연스럽다`), and `추론하자면` generalizations. Hollow guard also rejects `isPersonMeaningQuoteParade` and expanded `isThirdPersonPersonalityProfile` even when mirror openers are present.
Symbolic meaning questions such as `민수는 내 기록에서 어떤 의미야?` remain on the person-meaning treatment, not person-stance.

For `SYNTHESIS`, the prompt asks the model to connect:

- appearance context
- repeated patterns
- changes over time
- emotional arc
- tag/symbol links
- uncertainty boundary for inferred real-world relationship roles

It must present the answer as an interpretation grounded in records, not as external knowledge or a list of isolated snippets.

For person meaning questions, the assistant must not infer real-world roles such as manager, coworker, lover, or family member unless retrieved records directly say so. The expected response shape is:

- how the person appears in the records
- repeated relationship/theme axes
- emotional or desire function in the user's flow
- what cannot be confirmed from the retrieved records

When a `SYNTHESIS` question is also a person-meaning question, `RagContextService` uses **tag-only retrieval** (person-tag search first). Only journal entries whose embedding payload tags contain a person focus token (substring match: `민수` within `#김민수`) become RAG sources.

Person-meaning retrieval tries **tag-only search first** via `searchByPersonTagsWithScore(...)`. Person tokens are normalized with the same honorific/particle stripping as keyword search (`민수님` -> `민수`) so tags such as `#김민수` match.

When tag-only retrieval returns zero rows, `RagContextService` falls back to merged synthesis retrieval (keyword + vector + person-tag merge + entity boost). It must not tell the user to attach tags when tagged/body records already exist in the merged fallback results.

When tag-only retrieval finds zero matches, the RAG context states that no tagged records exist and deterministic fallback tells the user to attach the person tag first. Body-only mentions are not used as a substitute.

The entity catalog may still be consulted **only** to expand alias tokens (`민수` -> canonical `#김민수`) and to populate `personFocus.entitySummary` for **canonical display labels** and chat metadata (`canonicalLabel`, `surfaceForms`). Catalog-wide role axes, content-kind counts, and linked entry IDs must not drive person-meaning **aggregation** when tag-only RAG sources exist; `buildPersonMeaningSnapshot` uses tagged-source timeline/kind data in that case (`isTagOnlyPersonMeaningResults`).

The `PERSON_FOCUS` / `PERSON_MEANING_SCAFFOLD` context-text blocks have been **removed** (convergence, F4): whenever `personFocus` resolves, the question is handled by Path C, whose `SNAPSHOT` prompt block carries the same aggregation material — and Path C never consumes `RagContext.text`. The legacy prompt path (which does consume `RagContext.text`) can only run with `personFocus == null`, so the blocks could never render there. The anti-bucket rule ("업무 협업"/"조직 관계" without cited tags or snippets) is enforced by the Path C snapshot prompt + hollow guards, and by the legacy SYNTHESIS intent prompt wording.

Prompt/snapshot behavior:

- extracts the queried person token from the user message
- runs `searchByPersonTagsWithScore(...)` as the sole retrieval path for person-meaning `SYNTHESIS` questions
- resolves the matching `journal_entity(PERSON)` row only for alias token expansion when the catalog already knows that person
- reports how many tagged sources matched
- keeps repeated-tag summary scoped to person-relevant tags whose text contains a focus token, not co-occurring scene tags from the same entry body
- sanitizes evidence snippets by stripping `embedding_text` metadata lines (`유형:`, `핵심 태그:`, `본문:` wrapper) and HTML before snapshot/fallback output
- scopes `## 태그 요약` and message `ragTagSummary` metadata to person-focused tagged sources and person-relevant tags only when `personFocus` is present
- prepends a rule-based `해석 시드:` line to the Path C `SNAPSHOT` block and uses the same interpretive lead sentence at the top of `PERSON_MEANING_FALLBACK`
- instructs the model (hybrid meaning/appearance prompt and legacy SYNTHESIS intent prompt) to organize the answer around: repeated axes, role/function, content-kind spread, evidence scenes, and unconfirmed points
- forbids generic workplace/category labels unless backed by cited tags or snippets

For `SYNTHESIS`, the context starts with a tag summary block:

```text
## 태그 요약
태그는 사용자가 의도적으로 붙인 주제 축입니다. 본문보다 강한 해석 신호로 우선 참고하세요.
전체 반복 태그: [정서실험]#민수(8), [관계]#공연(3)
꿈 기록 태그: [상징]#민수(4)
일기 기록 태그: [관계]#민수(4)
연결 태그: [관계]#공연 ↔ [정서실험]#민수(3)

## 시간/유형 흐름
기간: 2026-01-02 ~ 2026-05-29
기록 유형: DREAM(12), DIARY(10), NOTE(3)
월별 밀도: 2026-05(8), 2026-04(5)
시간축은 주제가 어느 시기에 응집되거나 옮겨가는지 보는 보조 해석 축입니다.
```

This lets the model synthesize around the user's intentional classification axis before reading isolated snippets.

Tag pairs are co-occurrence signals. They should be interpreted as relationship axes between themes, not merely as independent topic counts.

Timeline summary is a temporal signal. It should be used to notice clustering, changes over time, and whether a theme appears more often in dreams, diary entries, or notes.

### RAG Source Logs

When RAG context is built, the server logs a compact source trace.

Summary log:

```text
AI RAG context built. intent={LOOKUP|SUMMARY|SYNTHESIS}, queryLength={n}, keywordCount={n}, vectorCount={n}, mergedCount={n}
```

Per-source log:

```text
AI RAG source rank={n} entryId={id} date={date} matchType={KEYWORD|VECTOR|ENTITY|TAG} score={score} tokens={tokens} snippet={snippet}
```

This is intended for operational diagnosis: if a question like `민수님에 대해...` fails, logs should show whether no source was found, a source was found but ignored by the model, or the wrong source was retrieved.

## Response Rules

The assistant response must follow these rules even if the session prompt is weak or old:

1. Respond in Korean unless the user explicitly asks for another language.
2. Do not mix Chinese, English, or Japanese into a Korean answer without user intent.
3. Use retrieved journal records only when they are clearly relevant.
4. If a person, event, or fact is not confirmed by chat context or retrieved journal records, say that it is not confirmed and ask for a short clarification.
5. Do not over-explain internal RAG decisions such as "this is unrelated to previous records."
6. Keep the answer conversational and useful, not defensive.
7. Do not assign a real-world relationship role to a person unless that role is directly present in retrieved records.
8. For person/symbol meaning questions, separate confirmed appearance context, repeated axes, emotional function, and uncertain points.
9. Do not expose RAG internal record indexes such as `[1]`, `[2]` in the saved assistant text. Cite by date, scene, or tag instead. The server strips leftover `[N]` / `[N] 기록` patterns before save; markdown symbols are kept so the chat drawer can render limited HTML via `markdownContent`.


### Person-Meaning Response Guard

When a `SYNTHESIS` person-meaning question resolves `personFocus`, the server validates the assistant text after the language guard:

1. A response is degraded when it leaks internal scaffold/meta field names (`role_axes_ko`, `repeated_tags`, machine-style section keys), or when it fails to cite tags/role axes from the current `PersonMeaningSnapshot`.
2. Tags that do not contain the focused person token — including co-occurring scene tags on the same entry and app/meta tags such as `#dreamdiary` — are noise for **repeated-axis** ranking only. Linked context tags (for example `[엠서클]#조직역동`) **do** count as hollow-guard evidence when cited by full tag or `#` stem.
3. When no person tag or role axis exists in the current snapshot, a response that cites sanitized evidence snippets from the snapshot still passes the guard.
4. The chat drawer RAG details block may show `personFocus.roleAxesKo` and `responseMode` (`LLM`, `PERSON_SYNTHESIS_HYBRID`, `RULE_PRIMARY`, `PERSON_MEANING_FALLBACK`, `PERSON_STANCE_FALLBACK` (legacy), `PERSON_APPEARANCE_FALLBACK`, `LANGUAGE_FALLBACK`) for diagnosis.
5. Degraded responses trigger **one** `PERSON_MEANING_RETRY` Ollama call with explicit tag/context citation instructions. Only if the retry is still hollow (or language-guard invalid) does the server replace the answer with `buildPersonMeaningDeterministicFallback(...)`.
6. `chat_message.metadataJson.responseMode` records `LLM`, `PERSON_SYNTHESIS_HYBRID`, `RULE_PRIMARY`, `PERSON_MEANING_FALLBACK`, `PERSON_STANCE_FALLBACK` (legacy, no longer emitted — historical rows only), `PERSON_APPEARANCE_FALLBACK`, or `LANGUAGE_FALLBACK`. Person retry fallbacks and `RULE_PRIMARY` also persist `guardDetail` / `retryGuardDetail` when guards reject the LLM output; `LANGUAGE_FALLBACK` sets `guardDetail=language_guard`.
7. `PERSON_MEANING_FALLBACK` must not stop at person-tag counts alone. It also aggregates **linked context tags** from the same tagged sources and **chapter Prefix names** from embedding payload to explain how the person appears in the user's intentional journal grouping.
8. When entity-catalog role axes are unavailable in the person-meaning path, fallback/scaffold role text must use linked context tags + chapter Prefix names instead of the misleading `entity catalog ... not extracted` boilerplate.
9. Hollow-guard evidence accepts, in order: person tags (full tag or `#` stem), role axes, linked context tags (full tag or `#` stem), chapter Prefix names, content-kind words (`꿈`/`일기`/`노트`), and sanitized snippet probes. Generic workplace buckets without these anchors remain hollow.
10. `isDegradedPersonResponse(...)` also covers `LOOKUP` person-attitude questions (`isPersonMeaningQuery` + extracted person token). Answers that cite generic workplace buckets such as `조직 내` or `업무 협업` without `#`, `기록상`, `반복`, or `태그` evidence are retried once and may fall back to `PERSON_MEANING_FALLBACK`.
11. `LOOKUP` questions with an extracted person token receive an additional intent prompt that forbids unsupported organizational role inference and internal `[N]` citations.
## Language Guard

Prompt rules alone are not considered sufficient.

The server validates generated assistant text before saving it.

Current behavior:

1. If generated text contains 2 or more Han-script characters, it is treated as a Korean-only guard violation.
2. The server retries the Ollama chat call once with an additional strict Korean-only prompt.
3. If the retried response still violates the guard, the server saves a deterministic Korean fallback instead of the model output.
4. Existing assistant messages that violate the guard are replaced in future prompt context with:

```text
[이전 AI 응답은 언어 규칙 위반으로 맥락에서 제외되었습니다.]
```

This prevents a bad stored assistant message from contaminating later responses in the same session.

Retry prompt text comes from locale catalog key `chat.ai.guard.language-retry` via `languageRetryPrompt()`. Deterministic fallbacks use `chat.ai.language-fallback.*` via `buildLanguageFallback(...)`. Regression coverage: `ChatOrchestratorTest` language-guard methods.

## Bad Response Guardrail

### Incident

User asked:

```text
민수님에 대해 뭘 말해줄 수 있니
```

Bad response pattern:

```text
민수라는 분과 관련된 질문인데 이전 기록과 무관하다고 답함
```

Problems:

- Mixed Korean and Chinese.
- Claimed the question was unrelated to previous dream journal records.
- Exposed internal retrieval judgment to the user.
- Failed to answer naturally or ask a concise clarification.

### Expected Response

If no reliable retrieved context exists:

```text
지금 기록 안에서는 민수님에 대해 확인되는 정보가 없어요.
어떤 민수님을 말하는 건지, 혹은 내가 봐야 할 기록 맥락이 있는지 조금만 더 알려줘.
```

If retrieved context exists:

```text
기록상으로는 민수님이 이런 맥락에서 등장해요: ...
다만 이건 기록에 적힌 범위 안에서만 말할 수 있어요.
```

When the retrieved context contains the asked name or keyword, the assistant must interpret it as a Dreamdiary-internal memory lookup first, not as an external celebrity/person lookup.

## Privacy Boundary

RAG retrieval must be scoped to the current logged-in user.

The vector cache may contain many users' records, but `JournalEntryEmbeddingSearchService.search(...)` must filter by ownership before scoring results are returned to chat.

## Failure Behavior

| Failure | Required Behavior |
| --- | --- |
| Ollama embedding API fails | Continue chat without RAG context |
| Vector cache empty | Continue chat without RAG context |
| No result above threshold | Continue chat without RAG context |
| Ollama chat API fails | Surface chat error to client |
| User cancels response | Set cancel flag; main-path stream reader stops and skips save/final broadcast. Non-stream hybrid/retry waits for HTTP completion then skips save |
| LLM response mixes Chinese/Han script | Retry once; if still invalid, save Korean fallback |

## Current Limitations

- Chat UI exposes RAG score/snippet/source rows from `metadataJson` (collapsible details; default preview 5 sources with expand).
- Source rows deep-link to read-only `JournalEntryViewModal` (markdownContent HTML, same as list). Edit is optional via the view modal footer.
- Assistant bubbles render limited markdown HTML from `ChatMessageDto.markdownContent` (`MarkdownUtils.renderChatMarkdown`). USER bubbles stay plain text. Stored `content` remains plain markdown (not stripped).
- Empty sessions show catalog seed prompts (`chat.empty.seed.1`..`4`) under `chat.empty.prompt`. Clicking a seed sends it immediately via `chat.sendMessage` (not personalized / not RAG-derived).
- Session chip titles can be renamed with double-click inline edit (`PATCH /chat/sessions/{id}`). Manual titles are not overwritten by first-message auto-title (`DEFAULT_TITLE` only).
- Main-path assistant generation streams via Ollama NDJSON (`chatStream`) and WS `DELTA` events; hybrid/retry/language-guard retry remain non-streaming. Final saved text still passes language/person guards before persist.
- Per-message logs include query length, merged RAG counts, source IDs, match types, scores, tokens, and compact snippets.
- LOOKUP/SUMMARY/SYNTHESIS/stance top-K, LOOKUP-SUMMARY min-score, SYNTHESIS min-score, and `rag_enabled` are admin-configurable via `chat_setting` (`GET`/`PATCH /admin/chat/settings`, Admin AI tab). Code constants remain defaults only.
- Existing chat sessions may contain older weak `systemPrompt`; response guard rules are appended at runtime to compensate.
- Han-script detection may also catch legitimate Korean Hanja usage; this is intentional for now because the chat UX should be Korean Hangul-first.
- Keyword extraction is heuristic-based and Korean-particle aware, not a full morphological analyzer.
- `personFocus.topRoles` comes from entity-catalog role rows (`journal_entry_entity_role`) produced by `JournalEntityRoleExtractor` (hardened keyword heuristics: noise cues like `조금`/`작업`/`처럼` removed; multi-hit confidence boost). Still not a full LLM relation extractor; roles refresh when the entity sync worker reprocesses entries.

## Recent Contract Additions

1. SUMMARY/SYNTHESIS/STANCE-specific top-K and synthesis min-score are admin-configurable (`rag_summary_top_k`, `rag_synthesis_top_k`, `rag_stance_top_k`, `rag_synthesis_min_score`).
2. Ambiguity-gated LLM second-pass runs when SUMMARY+SYNTHESIS cues overlap (`RagIntentClassifier#needsLlmSecondPass` + `chat.ai.prompt.intent-classify`), with heuristic fallback on failure.
3. Korean-only guard prompt construction is covered by `ChatOrchestratorTest` (`languageRetryPrompt_*`, `containsDisallowedHanScript_*`, `buildLanguageFallback_*`).
