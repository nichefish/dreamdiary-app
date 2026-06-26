# AI Chat Spec

## Purpose

Dreamdiary AI Chat is a record-aware assistant.

It must help the user continue thinking with their own journal data, but it must not pretend to know facts that are not present in the current conversation or retrieved records.

## Current Architecture

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
| Chat orchestration | `ChatAIService` | Save user message, build context, call LLM, save/broadcast assistant message |
| Session management | `ChatSessionService` | Session ownership, default prompt, last-message timestamp/title |
| Message history | `ChatMessageService` | Recent context messages |
| LLM client | `OllamaClient` | Ollama chat and embedding API calls |
| Memory search | `JournalEntryEmbeddingSearchService` | In-memory keyword + vector search over journal embeddings |
| Entity focus summary | `JournalEntityFocusService` | Resolve entity-catalog-backed person summaries for synthesis questions |
| Entity sync queue | `JournalEntryEntityQueueService` / `JournalEntryEntityWorker` | Queue and asynchronously refresh entity refs and mention roles after journal entry writes |
| Entity queue admin API | `JournalEntryEntityAdminRestController` | Expose queue stats, full requeue sync, and failed-row requeue for operators |
| RAG search result | `RagSearchResult` | Internal retrieval DTO carrying entity, match type, score, matched tokens, and snippet |
| RAG intent | `RagIntent` | Classifies retrieval mode as `LOOKUP`, `SUMMARY`, or `SYNTHESIS` |

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
  -> ChatAIService.processChat()
  -> save USER chat_message
  -> broadcast USER message
  -> build RAG context from journal_entry_embedding
      -> classify RagIntent
      -> keyword search for names/tags/proper nouns
      -> vector search for semantic similarity
      -> return RagSearchResult metadata
      -> merge keyword results first, vector results second by journalEntryId
      -> log selected source IDs, match types, scores, tokens, and snippets
  -> build guarded system prompt
  -> OllamaClient.chat(systemPrompt, recentContextMessages)
  -> language guard validation
      -> if Chinese/Han script is detected, retry once with stricter Korean-only prompt
      -> if retry still violates guard, replace with Korean fallback response
  -> strip markdown symbols
  -> save ASSISTANT chat_message with RAG source metadataJson
  -> broadcast ASSISTANT message
```

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
  -> Ollama embedding model: nomic-embed-text
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
| `RAG_TOP_K` | `5` | Maximum records for `LOOKUP` |
| `RAG_SUMMARY_TOP_K` | `12` | Maximum records for `SUMMARY` |
| `RAG_SYNTHESIS_TOP_K` | `25` | Maximum records for `SYNTHESIS` |
| `RAG_MIN_SCORE` | `0.35` | Minimum weighted vector similarity for `LOOKUP`/`SUMMARY` |
| `RAG_SYNTHESIS_MIN_SCORE` | `0.25` | Wider vector threshold for `SYNTHESIS` |
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

Keyword search does not use the vector threshold. This is intentional: short person-name queries such as `원빈님에 대해 뭘 말해줄 수 있니` can have weak semantic similarity even when the exact name appears in records.

Keyword search is field-weighted because tags are the strongest "what this entry is about" signal in Dreamdiary.

Current keyword weights:

| Field group | Weight |
| --- | --- |
| Tags | `5` |
| Entry title, chapter title, chapter category | `3` |
| Body / full embedding text / payload fallback | `1` |

### Embedding Text

`journal_entry_embedding.embedding_text` should represent the record the way a user remembers it, not only the prose body.

Current fields included:

- content kind
- journal date
- chapter title
- chapter category
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

This keeps retrieval evidence available after search. `ChatAIService` uses it for merging, context construction, logs, and persisted message metadata.

### Message Metadata

Assistant messages persist RAG source evidence to `chat_message.metadata_json`.

Shape:

```json
{
  "ragIntent": "SYNTHESIS",
  "ragSourceCount": 25,
  "personFocus": {
    "target": "원빈",
    "tokens": ["원빈", "원빈님"],
    "matchedSourceCount": 8,
    "journalEntityId": 7,
    "canonicalLabel": "원빈",
    "mentionCount": 12,
    "journalEntryCount": 9,
    "firstDate": "2026-01-02",
    "lastDate": "2026-05-29",
    "contentKinds": [
      { "name": "DREAM", "count": 6 },
      { "name": "DIARY", "count": 3 }
    ],
    "topRoles": ["COLLABORATION(4)", "TENSION(3)"],
    "surfaceForms": ["원빈님", "원빈"],
    "journalEntryIds": [123, 124, 130]
  },
  "ragTagSummary": {
    "totalTags": [
      { "name": "[정서실험]#원빈", "count": 8 }
    ],
    "dreamTags": [],
    "diaryTags": [],
    "noteTags": [],
    "tagPairs": [
      { "name": "[관계]#공연 ↔ [정서실험]#원빈", "count": 3 }
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
      "matchedTokens": ["원빈"],
      "tags": ["[정서실험]#원빈", "[관계]#공연"],
      "snippet": "..."
    }
  ]
}
```

This metadata is available through the existing chat message DTO and WebSocket payload.

The current chat drawer UI renders `personFocus` inside the existing RAG details block as a compact human-readable summary string. It still does not expose raw `ragSources`/`personFocus` JSON as a separate structured inspector.

`personFocus` is optional. It is present only when a synthesis question is recognized as a person-meaning question and the entity catalog resolves a matching `PERSON` row or the prompt aid still has matched RAG sources to prioritize.

### RAG Intent

`ChatAIService` classifies user questions before retrieval.

Modes:

| Intent | Use case | Retrieval shape |
| --- | --- | --- |
| `LOOKUP` | Find or answer about a concrete item/person/tag | small top-K, direct context blocks |
| `SUMMARY` | Summarize a set of records | wider top-K, compact source lines |
| `SYNTHESIS` | Interpret patterns, meanings, symbols, emotional arcs, whole context | widest top-K, lower vector threshold, compact source lines |

First-pass intent detection is heuristic-based. Words such as `의미`, `통섭`, `상징`, `패턴`, `흐름`, `반복`, `변화`, `전체 맥락`, `해석`, `어떻게 생각`, `생각하고`, `어떤 감정`, `어떤 마음`, and `어떤 느낌` trigger `SYNTHESIS`.

Person-meaning hint detection (`isPersonMeaningQuery`) also treats `어떻게 생각`, `생각하고`, `어떤 감정`, `어떤 마음`, and `어떤 느낌` as person-centric synthesis signals. Example: `나는 원빈님을 어떻게 생각하고 있니?` must route to `SYNTHESIS` + person-meaning tag-only retrieval, not `LOOKUP`.

### Person-Stance Questions

When a `SYNTHESIS` question is also a **first-person attitude** question (`isPersonAttitudeQuery`), the user is asking how *they* feel or think about someone in their journal — not for a third-person personality profile or workplace analysis.

Detection requires all of:

- a extracted person focus token (for example `원빈` from `원빈님`)
- a first-person marker such as `나는`, `내가`, `나의`, or `내 `
- an attitude hint such as `어떻게 생각`, `생각하고`, `어떤 감정`, `어떤 마음`, or `어떤 느낌`

Example: `나는 원빈님을 어떻게 생각하고 있니?`

This path reuses person-meaning tag-only retrieval but changes prompt/scaffold/guard:

- RAG intro text asks for attitude **pattern synthesis** (repeat axes, linked context, record types), not episode recap or symbolic role synthesis
- `PERSON_STANCE_SCAFFOLD` replaces `PERSON_MEANING_SCAFFOLD`. Internal material includes interpretive lead, repeat axes, linked context, role axes, record types, and brief evidence scenes; the **user-facing answer** must use four sections: (1) 내 태도·정서 (2) 반복 패턴 (3) 함께 묶인 축 (4) 확정 불가
- Attitude interpretation = integrating repeat axes/context/types, **not** long event narration or ungrounded psych labels (불신·거리감·방어적 등)
- intent prompt requires 2nd-person mirroring, axis citation when scaffold material exists, and forbids HR/coaching tone, personality adjectives, collaboration advice, and episode-only answers
- hollow guard (`isHollowPersonStanceResponse`) rejects coaching/advisory tone (`고려할 수 있습니다`, `이해하기 위해서는`), record evasion/neutralization (`명시적으로 표현되지 않`, `중립적 또는 평온`), third-person trait profiles, linked-tag-only answers when person-focus tags exist, ungrounded psych labels, heavy episode narration, and other-behavior reports; strong mirror markers (`네가`, `기록을 보면`, `기록에 남긴`, `내 태도`, etc.) are required — `당신` alone is insufficient
- degraded retries use `PERSON_STANCE_RETRY` (full snapshot + 4-section shape); deterministic fallback uses `buildPersonStanceDeterministicFallback` and `responseMode=PERSON_STANCE_FALLBACK`

Symbolic meaning questions such as `원빈은 내 기록에서 어떤 의미야?` remain on the person-meaning scaffold, not person-stance.

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

When a `SYNTHESIS` question is also a person-meaning question, `ChatAIService` uses **tag-only retrieval** via `buildPersonMeaningTagOnlyRagContext(...)`. Only journal entries whose embedding payload tags contain a person focus token (substring match: `원빈` within `#김원빈`) become RAG sources.

Person-meaning retrieval tries **tag-only search first** via `searchByPersonTagsWithScore(...)`. Person tokens are normalized with the same honorific/particle stripping as keyword search (`원빈님` -> `원빈`) so tags such as `#김원빈` match.

When tag-only retrieval returns zero rows, `ChatAIService` falls back to merged synthesis retrieval (keyword + vector + person-tag merge + entity boost). It must not tell the user to attach tags when tagged/body records already exist in the merged fallback results.

When tag-only retrieval finds zero matches, the RAG context states that no tagged records exist and deterministic fallback tells the user to attach the person tag first. Body-only mentions are not used as a substitute.

The entity catalog may still be consulted **only** to expand alias tokens (`원빈` -> canonical `#김원빈`). Catalog mention counts, role axes, and linked entry IDs must not be injected into person-meaning RAG sources or `personFocus.entitySummary`.

When tagged sources exist, `ChatAIService` prepends a `PERSON_FOCUS` block before the general tag/timeline summary and appends `PERSON_MEANING_SCAFFOLD` so the model cannot reply with empty topic buckets such as "업무 협업" or "조직 관계" without citing tags or record snippets.

Prompt/scaffold behavior:

- extracts the queried person token from the user message
- runs `searchByPersonTagsWithScore(...)` as the sole retrieval path for person-meaning `SYNTHESIS` questions
- resolves the matching `journal_entity(PERSON)` row only for alias token expansion when the catalog already knows that person
- reports how many tagged sources matched
- keeps repeated-tag summary scoped to person-relevant tags whose text contains a focus token, not co-occurring scene tags from the same entry body
- sanitizes evidence snippets by stripping `embedding_text` metadata lines (`유형:`, `핵심 태그:`, `본문:` wrapper) and HTML before scaffold/fallback output
- scopes `## 태그 요약` and message `ragTagSummary` metadata to person-focused tagged sources and person-relevant tags only when `personFocus` is present
- prepends a rule-based `해석 시드:` line to `PERSON_MEANING_SCAFFOLD` and uses the same interpretive lead sentence at the top of `PERSON_MEANING_FALLBACK`
- instructs the model to answer in five sections: repeated axes, role/function, content-kind spread, evidence scenes, and unconfirmed points
- forbids generic workplace/category labels unless backed by cited tags or snippets

For `SYNTHESIS`, the context starts with a tag summary block:

```text
## 태그 요약
태그는 사용자가 의도적으로 붙인 주제 축입니다. 본문보다 강한 해석 신호로 우선 참고하세요.
전체 반복 태그: [정서실험]#원빈(8), [관계]#공연(3)
꿈 기록 태그: [상징]#원빈(4)
일기 기록 태그: [관계]#원빈(4)
연결 태그: [관계]#공연 ↔ [정서실험]#원빈(3)

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

This is intended for operational diagnosis: if a question like `원빈님에 대해...` fails, logs should show whether no source was found, a source was found but ignored by the model, or the wrong source was retrieved.

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
9. Do not expose RAG internal record indexes such as `[1]`, `[2]` in the saved assistant text. Cite by date, scene, or tag instead. The server strips leftover `[N]` / `[N] 기록` patterns after markdown removal.


### Person-Meaning Response Guard

When a `SYNTHESIS` person-meaning question resolves `personFocus`, the server validates the assistant text after the language guard:

1. A response is degraded when it leaks internal scaffold/meta field names (`role_axes_ko`, `repeated_tags`, machine-style section keys), or when it fails to cite tags/role axes from the current `PersonMeaningSnapshot`.
2. Tags that do not contain the focused person token — including co-occurring scene tags on the same entry and app/meta tags such as `#dreamdiary` — are noise for **repeated-axis** ranking only. Linked context tags (for example `[엠서클]#조직역동`) **do** count as hollow-guard evidence when cited by full tag or `#` stem.
3. When no person tag or role axis exists in the current snapshot, a response that cites sanitized evidence snippets from the snapshot still passes the guard.
4. The chat drawer RAG details block may show `personFocus.roleAxesKo` and `responseMode` (`LLM`, `PERSON_MEANING_FALLBACK`, `PERSON_STANCE_FALLBACK`, `LANGUAGE_FALLBACK`) for diagnosis.
5. Degraded responses trigger **one** `PERSON_MEANING_RETRY` Ollama call with explicit tag/context citation instructions. Only if the retry is still hollow (or language-guard invalid) does the server replace the answer with `buildPersonMeaningDeterministicFallback(...)`.
6. `chat_message.metadataJson.responseMode` records `LLM`, `PERSON_MEANING_FALLBACK`, `PERSON_STANCE_FALLBACK`, or `LANGUAGE_FALLBACK`.
7. `PERSON_MEANING_FALLBACK` must not stop at person-tag counts alone. It also aggregates **linked context tags** from the same tagged sources (for example `[엠서클]#조직역동`, `[엠서클]#김종순`) and **chapter categories** from embedding payload (`DYNAMICS`, `INTERACTION`) to explain how the person appears in the user's intentional classification axes.
8. When entity-catalog role axes are unavailable in the person-meaning path, fallback/scaffold role text must use linked context tags + chapter categories instead of the misleading `entity catalog ... not extracted` boilerplate.
9. Hollow-guard evidence accepts, in order: person tags (full tag or `#` stem), role axes, linked context tags (full tag or `#` stem), chapter category code/label, content-kind words (`꿈`/`일기`/`노트`), and sanitized snippet probes. Generic workplace buckets without these anchors remain hollow.
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

## Bad Response Guardrail

### Incident

User asked:

```text
원빈님에 대해 뭘 말해줄 수 있니
```

Bad response pattern:

```text
원빈이라는 분과 관련된您的问题似乎...
```

Problems:

- Mixed Korean and Chinese.
- Claimed the question was unrelated to previous dream journal records.
- Exposed internal retrieval judgment to the user.
- Failed to answer naturally or ask a concise clarification.

### Expected Response

If no reliable retrieved context exists:

```text
지금 기록 안에서는 원빈님에 대해 확인되는 정보가 없어요.
어떤 원빈님을 말하는 건지, 혹은 내가 봐야 할 기록 맥락이 있는지 조금만 더 알려줘.
```

If retrieved context exists:

```text
기록상으로는 원빈님이 이런 맥락에서 등장해요: ...
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
| User cancels response | Do not save or broadcast assistant response after cancellation flag is set |
| LLM response mixes Chinese/Han script | Retry once; if still invalid, save Korean fallback |

## Current Limitations

- RAG result score is carried by `RagSearchResult` and persisted to message metadata, but not yet exposed in UI.
- RAG source snippets are persisted to message metadata, but not shown to the user.
- Per-message logs include query length, merged RAG counts, source IDs, match types, scores, tokens, and compact snippets.
- Similarity thresholds and top-K values are hard-coded.
- Existing chat sessions may contain older weak `systemPrompt`; response guard rules are appended at runtime to compensate.
- Han-script detection may also catch legitimate Korean Hanja usage; this is intentional for now because the chat UX should be Korean Hangul-first.
- Keyword extraction is heuristic-based and Korean-particle aware, not a full morphological analyzer.
- `personFocus.topRoles` is currently heuristic keyword matching around direct mention context, not a full relation extractor.

## Next Candidates

1. Add source preview toggle in chat UI using `chat_message.metadataJson`.
2. Add an endpoint/link target to open a referenced journal entry from a chat source.
3. Add admin setting for `RAG_TOP_K`, `RAG_MIN_SCORE`, and RAG on/off.
4. Add a stronger classifier if heuristic `RagIntent` becomes too noisy.
5. Add a small automated test for Korean-only guard prompt construction.
