# AI 도메인 소유 경계

**상태:** 현재 계약 — `feature/ai` 능력과 `feature/chat` 채널의 소유 경계
**관련:** [CHAT_AI_PHILOSOPHY.md](CHAT_AI_PHILOSOPHY.md), [CHAT_AI_SPEC.md](CHAT_AI_SPEC.md)

## 1. 현재 구조

채팅은 사용자가 보는 **채널(외연)** 이다. `feature/ai`가 LLM·RAG·프롬프트·인물·가드 능력을 소유하고, `feature/chat`의 `ChatOrchestrator`와 `ChatWebSocketSender`가 채널 오케스트레이션을 담당한다.

| 현상 | 근거 |
|---|---|
| RAG 컨텍스트는 `ai/rag`가 조립 | `RagContextService` + `RagContextTextBuilder` (lookup/synthesis·tag/timeline) |
| 채널 밖 AI 확장은 `feature/ai` 소비로 가능 | journal 등 진입점은 chat을 경유하지 않고 `ai/*`를 의존 |
| 저널 벡터 데이터 SSOT는 journal | `JournalEntryEmbeddingSearchService` / `RagSearchResult` — `ai/rag`가 소비 |

철학([CHAT_AI_PHILOSOPHY.md](CHAT_AI_PHILOSOPHY.md))의 “세션 중심·연속성”은 **chat 채널** 계약이다. LLM/RAG 인프라는 그 채널에 묶이지 않아야 다른 진입점에서도 같은 능력을 쓸 수 있다.

## 2. 원칙

1. **`ai/` = 능력(capability)** — LLM 호출, 검색 정책, 프롬프트, 응답 가드, 인물 추론
2. **`chat/` = 채널(channel)** — 세션·메시지·설정·WebSocket·오케스트레이션  
3. **저널 영속/임베딩 큐는 `journal/`에 남긴다** — `ai/rag`는 저널 검색 서비스를 **소비**하며, 임베딩 테이블·백필 워커를 흡수하지 않는다  
4. 저널 등 채널 외 도메인 소비자는 `feature/ai/`를 직접 의존하며 `chat`을 경유하지 않는다.

## 3. 경계 (소유권)

```
journal/embedding/**     저널 벡터 저장·백필·인메모리 검색 (데이터 SSOT)
journal/entitycatalog/** 인물·역할 카탈로그 (데이터 SSOT)
        ▲ 소비
feature/ai/**            LLM·검색 정책·프롬프트·가드 (능력)
        ▲ 소비
feature/chat/**          세션/메시지/WS/오케스트레이터 (채널)
        ▲ 소비
frontend chat UI         AppChat / STOMP
```

| 관심사 | 패키지 | 비고 |
|---|---|---|
| Ollama HTTP·모델 설정 | `ai/client`, `ai/config` | 모델 호출과 런타임 설정 |
| Intent·결과 merge·컨텍스트 텍스트 | `ai/rag` | `JournalEntryEmbeddingSearchService` 호출 |
| 시스템/의도 프롬프트 | `ai/prompt` | |
| 언어·할루시네이션·재시도 가드 | `ai/guard` | |
| PersonFocus / SNAPSHOT / stance | `ai/person` | 저널 entity catalog 소비 |
| 세션·메시지 CRUD | `chat/service` | 채널 영속과 조회 |
| `processChat` 흐름 | `chat` 오케스트레이터 | `ChatOrchestrator` |
| WS PROGRESS/DELTA | `chat` (ws 유틸) | 채널 전용 |
| `chat_setting` (recentMessageLimit 등) | `chat` | 사용자 체감 채널 설정 |
| Admin RAG knobs (`rag_top_k` 등) | `chat` | 운영 UI는 admin이며 설정 영속·조회는 chat이 소유 |

## 4. 현재 패키지 구조

```
feature/
  ai/
    client/          OllamaClient, *Request/*Response
    config/          OllamaProperties
    rag/             RagSearchFacade, RagContextService, RagContextTextBuilder,
                     RagIntentClassifier
    prompt/          SystemPromptBuilder, IntentPromptResolver
    guard/           ResponseGuardService
    person/          PersonFocusResolver, Snapshot/Stance …
    model/           OllamaHealthDto 등 AI 공통 DTO
                     (RagSearchResult 는 journal.embedding.model 유지)

  chat/
    controller/      ChatController (REST + STOMP)
    service/         ChatOrchestrator,
                     ChatSessionService, ChatMessageService, ChatSettingService
    entity|model|…   세션·메시지·설정
    ws/              ChatWebSocketSender (broadcastProgress/Delta)
```

`RagIntent` / `RagIntentClassifier` / `RagSearchFacade`는 `feature/ai/rag`에 둔다. `PersonFocus`·`PersonQueryClassifier`·`PersonFocusResolver`·`PersonSnapshotService`·`PersonSynthesisHybridService`는 `feature/ai/person`에 둔다. 언어·person 가드 구현은 `feature/ai/guard/ResponseGuardService`에 둔다. 시스템/의도 프롬프트는 `feature/ai/prompt`(`SystemPromptBuilder`·`IntentPromptResolver`)에 둔다.

## 5. ChatOrchestrator 협력 경계

| 영역 | 소유 위치 |
|---|---|
| `processChat` / `cancelChat` 흐름 | `chat/.../ChatOrchestrator` |
| `broadcastProgress` / `broadcastDelta` | `chat/.../ws/ChatWebSocketSender` |
| RAG build/merge/topK/minScore | `ai/rag/*` |
| `buildSystemPromptWithRag`, intent prompt, `responseGuardPrompt` | `ai/prompt` (`SystemPromptBuilder`·`IntentPromptResolver`) |
| PersonFocus·SNAPSHOT·tag merge | `ai/person/*` |
| Path C hybrid·rule-primary·retry prompt | `ai/person/PersonSynthesisHybridService` |
| language/person guard 구현 | `ai/guard/ResponseGuardService` |
| OllamaClient·Properties·HealthDto | `ai/client|config|model` |

## 6. 의존 방향 (허용)

```
chat/ChatOrchestrator
  → ai/rag, ai/prompt, ai/guard, ai/person, ai/client
  → chat/ws, chat/ChatMessageService, chat/ChatSessionService, chat/ChatSettingService
  → (간접) journal/embedding, journal/entitycatalog

journal AI consumer
  → ai/client, ai/rag, ai/prompt
  ✗ chat 패키지 의존 금지
```

금지: `ai` → `chat` (역의존).

## 7. 동작 계약 (CHAT_AI_SPEC 기준)

아래 동작의 상세 정본은 [CHAT_AI_SPEC.md](CHAT_AI_SPEC.md)다.

### RAG 검색 계약
- Intent 1차 규칙 우선순위 (CHAT_AI_SPEC RAG Intent와 동일):
  1. person-about → `SYNTHESIS`
  2. 검색 단서 → `LOOKUP`
  3. synthesis 단서 → `SYNTHESIS`
  4. summary 동사 → `SUMMARY`
  5. else → `LOOKUP`
- SUMMARY∩SYNTHESIS 모호 시에만 ambiguity-gated LLM 2차 분류 (person-about·LOOKUP-force는 2차 호출 없음)
- 키워드 검색 필드 가중치 (tag:5, title/chapter:3, body:1)
- 벡터 유사도 최소 점수 (`ragMinScore`, `ragSynthesisMinScore`)
- 키워드 결과 우선 → 벡터 결과 후순위 병합 순서
- 사용자 소유 레코드만 검색

### Person 추론 계약
- PersonFocus 해석: 인물 토큰 → 태그 매칭 → 엔티티 카탈로그 연동
- Person-Meaning Path C 흐름: 태그 전용 검색 → Snapshot 빌드 → Hybrid LLM → 언어 가드 → person degradation gate → (실패 시) 1회 retry → Rule-Primary fallback
- Person-Stance (태도) rich-trust: 최소 gate(빈/짧음/scaffold-leak/generic-bucket)만 적용
- Person-Appearance: 등장 감지 → Path C 4-section prompt (stance 아님)

### Guard 체계 계약
- 언어 가드: 한국어 시 Han script 감지 → 재시도 1회 → locale 카탈로그 fallback
- 인물 의미 가드: hollow/quote-parade/scaffold-leak/3rd-person-profile 감지
- 태도 가드: `isDegradedPersonStanceRichResponse` 최소 gate만
- Guard 실패 시 정확히 1회 재시도 후 Rule-Primary fallback (Path C); 언어 가드는 person gate보다 먼저 적용

### 메타데이터 계약
- `chat_message.metadata_json` 구조: `ragIntent`, `ragSourceCount`, `personFocus`, `ragTagSummary`, `ragTimelineSummary`, `ragSources`
- 생성되는 `responseMode` 값(정본 CHAT_AI_SPEC): `LLM`, `PERSON_SYNTHESIS_HYBRID`, `RULE_PRIMARY`, `PERSON_MEANING_FALLBACK`, `PERSON_APPEARANCE_FALLBACK`, `LANGUAGE_FALLBACK`
- 저장된 메시지 조회·표시는 호환 값 `PERSON_STANCE_FALLBACK`도 해석한다.
- `guardDetail` / `retryGuardDetail` 코드 체계

### WS/REST 계약
- STOMP `/app/chat/session/{id}/send`, `/app/chat/session/{id}/cancel`
- Progress 이벤트 (`SEARCHING`, `GENERATING`), DELTA 스트리밍
- REST 세션/메시지/설정 엔드포인트 경로 및 응답 형태

## 8. 문서 역할

| 문서 | 역할 |
|---|---|
| CHAT_AI_PHILOSOPHY | 채팅 채널의 제품 철학 (세션·기억·연속성) |
| CHAT_AI_SPEC | **현재** 동작·API·파일 위치 as-built |
| **본 문서** | 능력/채널 소유 경계와 현재 의존 구조 |
