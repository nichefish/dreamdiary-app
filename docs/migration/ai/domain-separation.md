# AI 능력·채널 분리 실행 기록

> 상태: 완료된 migration 실행 기록. 현재 구조 계약은 [`AI_DOMAIN_SEPARATION.md`](../../spec/AI_DOMAIN_SEPARATION.md), 현재 채팅 동작은 [`CHAT_AI_SPEC.md`](../../spec/CHAT_AI_SPEC.md)를 따른다.

## 목적

채팅 패키지에 함께 있던 LLM·RAG·프롬프트·인물·가드 능력을 `feature/ai`로 분리하고, 세션·메시지·WebSocket 오케스트레이션을 `feature/chat` 채널 경계에 남긴다. 공개 API·STOMP·프론트엔드 동작은 유지한다.

## 실행 결과

| Phase | 실행 내용 | 회귀 확인 축 |
|:---:|---|---|
| 0 | 설계 문서와 관련 spec 연결 | 문서 |
| 1 | `ai/client`·`ai/config`·Health DTO 이동, `AiChatMessage`로 역의존 차단 | Ollama health, chat 생성 |
| 2 | intent classifier와 `RagSearchFacade`를 `ai/rag`로 이동 | LOOKUP/SUMMARY/SYNTHESIS, 근거 링크 |
| 3a+3b | `PersonFocus`·`PersonQueryClassifier`·`PersonFocusResolver`·`PersonSnapshotService`를 `ai/person`으로 이동 | 인물 질문·스냅샷 |
| 3c | Path C hybrid를 `PersonSynthesisHybridService`로 이동 | hybrid·rule-primary |
| 4a | 언어·person 가드를 `ResponseGuardService`로 이동 | 언어 가드·person hollow/stance |
| 4b | `SystemPromptBuilder`·`IntentPromptResolver`를 `ai/prompt`로 이동 | 시스템·의도 프롬프트 |
| 5a | PROGRESS·DELTA·message broadcast를 `ChatWebSocketSender`로 분리 | 스트림 DELTA/PROGRESS |
| 5b | 채널 오케스트레이터를 `ChatOrchestrator`로 수렴 | cancel·오케스트레이션 |
| 6 | `RagContextService`·`RagContextTextBuilder`를 `ai/rag`로 이동 | LOOKUP/SUMMARY/SYNTHESIS·tag-only·metadata |

## 최종 경계

- `feature/ai`는 LLM·RAG·프롬프트·인물·응답 가드 능력을 소유한다.
- `feature/chat`은 세션·메시지·설정·WebSocket·채널 오케스트레이션을 소유한다.
- 저널 임베딩과 인물 카탈로그 데이터는 `feature/journal`이 소유하고 `feature/ai`가 소비한다.
