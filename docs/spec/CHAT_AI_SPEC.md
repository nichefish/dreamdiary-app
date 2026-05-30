# AI Chat Spec

## Purpose

Dreamdiary AI Chat is a record-aware assistant.

It must help the user continue thinking with their own journal data, but it must not pretend to know facts that are not present in the current conversation or retrieved records.

## Current Architecture

### Client

| Area | File | Responsibility |
| --- | --- | --- |
| Chat drawer UI | `app/frontend-vue/src/views/chat/AppChat.vue` | Floating AI chat panel, session list, composer, message rendering |
| Chat store | `app/frontend-vue/src/stores/chat.ts` | Session/message state, REST calls, STOMP send/cancel |
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
| RAG search result | `RagSearchResult` | Internal retrieval DTO carrying entity, match type, score, matched tokens, and snippet |
| RAG intent | `RagIntent` | Classifies retrieval mode as `LOOKUP`, `SUMMARY`, or `SYNTHESIS` |

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

RAG context must be omitted when no result passes the threshold.

Keyword search does not use the vector threshold. This is intentional: short person-name queries such as `민수님에 대해 뭘 말해줄 수 있니` can have weak semantic similarity even when the exact name appears in records.

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

This metadata is not shown in the UI yet, but it is available through the existing chat message DTO and WebSocket payload.

### RAG Intent

`ChatAIService` classifies user questions before retrieval.

Modes:

| Intent | Use case | Retrieval shape |
| --- | --- | --- |
| `LOOKUP` | Find or answer about a concrete item/person/tag | small top-K, direct context blocks |
| `SUMMARY` | Summarize a set of records | wider top-K, compact source lines |
| `SYNTHESIS` | Interpret patterns, meanings, symbols, emotional arcs, whole context | widest top-K, lower vector threshold, compact source lines |

First-pass intent detection is heuristic-based. Words such as `의미`, `통섭`, `상징`, `패턴`, `흐름`, `반복`, `변화`, `전체 맥락`, and `해석` trigger `SYNTHESIS`.

For `SYNTHESIS`, the prompt asks the model to connect:

- appearance context
- repeated patterns
- changes over time
- emotional arc
- tag/symbol links

It must present the answer as an interpretation grounded in records, not as external knowledge or a list of isolated snippets.

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
AI RAG source rank={n} entryId={id} date={date} matchType={KEYWORD|VECTOR} score={score} tokens={tokens} snippet={snippet}
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
민수님에 대해 뭘 말해줄 수 있니
```

Bad response pattern:

```text
민수가라는 분과 관련된您的问题似乎...
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

## Next Candidates

1. Add source preview toggle in chat UI using `chat_message.metadataJson`.
2. Add an endpoint/link target to open a referenced journal entry from a chat source.
3. Add admin setting for `RAG_TOP_K`, `RAG_MIN_SCORE`, and RAG on/off.
4. Add a stronger classifier if heuristic `RagIntent` becomes too noisy.
5. Add a small automated test for Korean-only guard prompt construction.
