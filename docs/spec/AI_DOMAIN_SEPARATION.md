# AI 도메인 분리 설계

**상태:** Phase 0–6 완료 (feature/ai 능력 분리 + RAG context pipeline/text in `ai/rag` + chat ChatOrchestrator·ChatWebSocketSender)
**관련:** [CHAT_AI_PHILOSOPHY.md](CHAT_AI_PHILOSOPHY.md), [CHAT_AI_SPEC.md](CHAT_AI_SPEC.md)

## 1. 문제(분리 동기)와 현재 구조

채팅은 사용자가 보는 **채널(외연)** 이다. LLM·RAG·프롬프트·인물·가드 능력은 `feature/ai`로 분리했고, 채널 오케스트레이션은 `ChatOrchestrator` + `ChatWebSocketSender`가 담당한다.

| 현상 | 근거 |
|---|---|
| RAG 컨텍스트는 `ai/rag`가 조립 | `RagContextService` + `RagContextTextBuilder` (lookup/synthesis·tag/timeline) |
| 채널 밖 AI 확장은 `feature/ai` 소비로 가능 | journal 등 진입점은 chat을 경유하지 않고 `ai/*`를 의존 |
| 저널 벡터 데이터 SSOT는 journal | `JournalEntryEmbeddingSearchService` / `RagSearchResult` — `ai/rag`가 소비 |

철학([CHAT_AI_PHILOSOPHY.md](CHAT_AI_PHILOSOPHY.md))의 “세션 중심·연속성”은 **chat 채널** 계약이다. LLM/RAG 인프라는 그 채널에 묶이지 않아야 다른 진입점에서도 같은 능력을 쓸 수 있다.

## 2. 원칙

1. **`ai/` = 능력(capability)** — LLM 호출, 검색 정책, 프롬프트, 응답 가드, (선택) 인물 추론  
2. **`chat/` = 채널(channel)** — 세션·메시지·설정·WebSocket·오케스트레이션  
3. **저널 영속/임베딩 큐는 `journal/`에 남긴다** — `ai/rag`는 저널 검색 서비스를 **소비**하며, 임베딩 테이블·백필 워커를 흡수하지 않는다  
4. 이후 도메인 활용은 `journal/...` 또는 `feature/journal/ai/` 등이 `feature/ai/`를 의존한다 (`chat`을 경유하지 않음)  
5. 마이그레이션은 **동작·API·FE 계약 불변**으로 내부 위임부터 하고, SAVEPOINT(커밋) 단위로 자른다  

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
| Ollama HTTP·모델 설정 | `ai/client`, `ai/config` | chat에서 이관 |
| Intent·결과 merge·컨텍스트 텍스트 | `ai/rag` | `JournalEntryEmbeddingSearchService` 호출 |
| 시스템/의도 프롬프트 | `ai/prompt` | |
| 언어·할루시네이션·재시도 가드 | `ai/guard` | |
| PersonFocus / SNAPSHOT / stance | `ai/person` | 저널 entity catalog 소비 |
| 세션·메시지 CRUD | `chat/service` | 유지 |
| `processChat` 흐름 | `chat` 오케스트레이터 | 현 `ChatOrchestrator` 축소본 |
| WS PROGRESS/DELTA | `chat` (ws 유틸) | 채널 전용 |
| `chat_setting` (recentMessageLimit 등) | `chat` | 사용자 체감 채널 설정 |
| Admin RAG knobs (`rag_top_k` 등) | 1차 `chat` 유지, 이후 `ai` 설정으로 이전 검토 | 운영 UI는 admin — 소유권만 문서화 |

## 4. 목표 패키지 구조

```
feature/
  ai/
    client/          OllamaClient, *Request/*Response
    config/          OllamaProperties (+ 필요 시 AiProperties)
    rag/             RagSearchFacade(또는 Service), context text builder,
                     RagIntentClassifier (현 chat 패키지 클래스 이관·통합)
    prompt/          SystemPromptBuilder, IntentPromptResolver
    guard/           ResponseGuardService
    person/          PersonFocusResolver, Snapshot/Stance …
    model/           OllamaHealthDto 등 AI 공통 DTO
                     (RagSearchResult 는 journal.embedding.model 유지)

  chat/
    controller/      ChatController (REST + STOMP)
    service/         ChatOrchestrator (← ChatOrchestrator 축소),
                     ChatSessionService, ChatMessageService, ChatSettingService
    entity|model|…   세션·메시지·설정
    ws/              ChatWebSocketSender (broadcastProgress/Delta)
```

`RagIntent` / `RagIntentClassifier` / `RagSearchFacade`는 `feature/ai/rag`에 둔다. PersonFocus·QueryClassifier·FocusResolver·SnapshotService·Path C hybrid(`PersonSynthesisHybridService`)는 `feature/ai/person`에 둔다. 언어·person 가드 구현은 `feature/ai/guard/ResponseGuardService`에 둔다. 시스템/의도 프롬프트는 `feature/ai/prompt`(`SystemPromptBuilder`·`IntentPromptResolver`)에 둔다.

## 5. ChatOrchestrator 분해 매핑 (요약)

| 영역 | 이동 대상 |
|---|---|
| `processChat` / `cancelChat` 흐름 | `chat/.../ChatOrchestrator` |
| `broadcastProgress` / `broadcastDelta` | `chat/.../ws/ChatWebSocketSender` |
| RAG build/merge/topK/minScore | `ai/rag/*` |
| `buildSystemPromptWithRag`, intent prompt, `responseGuardPrompt` | `ai/prompt` (`SystemPromptBuilder`·`IntentPromptResolver`) |
| PersonFocus·SNAPSHOT·tag merge | `ai/person/*` (3a+3b ✓) |
| Path C hybrid·rule-primary·retry prompt | `ai/person/PersonSynthesisHybridService` (3c ✓) |
| language/person guard 구현 | `ai/guard/ResponseGuardService` (4a ✓) |
| OllamaClient·Properties·HealthDto | `ai/client|config|model` |

세부 메서드 목록은 구현 Phase 진입 시 `ChatOrchestrator` 기준으로 체크리스트화한다 (이 문서에 전수 나열하지 않음).

## 6. 의존 방향 (허용)

```
chat/ChatOrchestrator
  → ai/rag, ai/prompt, ai/guard, ai/person, ai/client
  → chat/ws, chat/ChatMessageService, chat/ChatSessionService, chat/ChatSettingService
  → (간접) journal/embedding, journal/entitycatalog

journal/ai/* (향후)
  → ai/client, ai/rag, ai/prompt
  ✗ chat 패키지 의존 금지
```

금지: `ai` → `chat` (역의존).

## 7. 마이그레이션 Phase (SAVEPOINT)

각 Phase는 **공개 API·STOMP 계약·FE 불변**, 빌드 통과, 채팅 수동 스모크 후 사용자 커밋.

| Phase | 내용 | 회귀 초점 |
|:---:|---|---|
| **0** ✓ | 본 설계 문서 확정·스펙 상호 링크 | 문서만 |
| **1** ✓ | `ai/client`·`ai/config`·Health DTO 패키지 이동, `AiChatMessage`로 chat DTO 역의존 차단, import 갱신 | Ollama health, chat 생성 |
| **2** ✓ | `ai/rag` — intent classifier 이관 + `RagSearchFacade` 검색·병합 위임 (person/context text는 chat 잔류) | LOOKUP/SUMMARY/SYNTHESIS, 근거 링크 |
| **3a+3b** ✓ | `ai/person` — PersonFocus/QueryClassifier/FocusResolver/SnapshotService (focus·tag merge·snapshot) | 인물 질문·스냅샷 |
| **3c** ✓ | Path C hybrid → `PersonSynthesisHybridService` (세션 컨텍스트는 chat, 가드는 `PersonSynthesisGuardPort`) | hybrid·rule-primary |
| **4a** ✓ | `ai/guard/ResponseGuardService` — 언어·person 가드 구현, Path C `PersonSynthesisGuardPort` | 언어 가드·person hollow/stance |
| **4b** ✓ | i/prompt — SystemPromptBuilder·IntentPromptResolver | 시스템/의도 프롬프트 |
| **5a** ✓ | chat/ws/ChatWebSocketSender — PROGRESS/DELTA/message broadcast | 스트림 DELTA/PROGRESS |
| **5b** ✓ | ChatAIService → ChatOrchestrator 이름·역할 수렴 | cancel·오케스트레이션 |
| **6** ✓ | `ai/rag` — RagContextService·RagContextTextBuilder (검색 파이프라인+context text) | LOOKUP/SUMMARY/SYNTHESIS·tag-only·metadata |

한 Phase 안에서도 파일이 크면 sub-savepoint(예: 2a intent 이동 / 2b merge 위임)를 제안한 뒤 진입한다.

## 불변 계약 (CHAT_AI_SPEC 기준)

분리 전후로 아래 동작 계약은 변경 없이 유지해야 한다. 정본: [CHAT_AI_SPEC.md](CHAT_AI_SPEC.md).

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
- `responseMode` 값(정본 CHAT_AI_SPEC): `LLM`, `PERSON_SYNTHESIS_HYBRID`, `RULE_PRIMARY`, `PERSON_MEANING_FALLBACK`, `PERSON_APPEARANCE_FALLBACK`, `LANGUAGE_FALLBACK`, 레거시 표시용 `PERSON_STANCE_FALLBACK`(신규 emit 없음)
- `guardDetail` / `retryGuardDetail` 코드 체계

### WS/REST 계약
- STOMP `/app/chat/session/{id}/send`, `/app/chat/session/{id}/cancel`
- Progress 이벤트 (`SEARCHING`, `GENERATING`), DELTA 스트리밍
- REST 세션/메시지/설정 엔드포인트 경로 및 응답 형태

## 8. 비목표 (이번 분리에서 하지 않음)

- 채팅 UX·프롬프트 문구·RAG 품질 튜닝 재설계  
- 임베딩 스키마/`journal_entry_embedding` 소유권 변경  
- 모바일 RN 채팅 앱 구조 변경 (API 불변이면 불필요)  
- `feature/journal/ai/` 신규 기능 구현 (소비 측은 후속)

## 9. 문서 역할

| 문서 | 역할 |
|---|---|
| CHAT_AI_PHILOSOPHY | 채팅 채널의 제품 철학 (세션·기억·연속성) |
| CHAT_AI_SPEC | **현재** 동작·API·파일 위치 as-built |
| **본 문서** | 능력/채널 분리 **목표 구조**와 Phase 계획 |

구현이 Phase를 통과할 때마다 CHAT_AI_SPEC의 Server 표 경로를 현재 위치에 맞게 갱신한다.
