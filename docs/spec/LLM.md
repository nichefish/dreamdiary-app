## 로컬 LLM 구축 및 활용.

AI 채팅의 동작 기준과 현재 구현 상태는 [AI Chat Spec](CHAT_AI_SPEC.md)을 함께 갱신한다.

### 1. Ollama 다운로드 및 설치.
- Ollama란?
  - 로컬 LLM 실행 런타임 + 모델 관리 도구.
  - Ollama를 설치하면 모델 다운로드/버전 관리/교체, GPU 연결, 실행, API 노출 등을 간단한 명령어로 처리할 수 있다.
- 공식 홈페이지에서 다운로드 및 설치.
    
### 2. AI 모델 설치: qwen2.5:7b 
- Qwen이란?
  - Alibaba Cloud에서 개발한 오픈소스 계열 LLM 시리즈.
  - 한국어 성능이 비교적 우수함. 영어/중국어 포함 다국어 지원. 로컬 실행 난이도가 비교적 낮음. Ollama와 궁합이 좋음.
  - 현재 소규모 개인 PC 환경에서는 7B 모델이 가장 범용적으로 많이 사용된다.
- cmd에서 위치 상관없이 "ollama run qwen2.5:7b" 입력하여 설치.

### 3. 애플리케이션 설정 (`application.yml`)

모델명은 코드 상수가 아니라 Spring 설정으로 관리합니다 (`OllamaProperties`, prefix `app.ollama`).

| 키 | 기본값 | 용도 |
| --- | --- | --- |
| `app.ollama.base-url` | `http://localhost:11434` | Ollama API |
| `app.ollama.chat-model` | `qwen2.5:7b` | 채팅·person synthesis hybrid |
| `app.ollama.embedding-model` | `nomic-embed-text` | RAG 쿼리 임베딩·백필 worker |
| `app.ollama.chat-temperature` | `0.35` | chat temperature |
| `app.ollama.num-predict` | `768` | chat max tokens |

로컬에서 14B 시험 시 `application-local.yml`에 `app.ollama.chat-model: qwen2.5:14b`와 `app.ollama.chat-temperature: 0.28`(기본 0.35보다 낮게, 형식·근거 이탈 완화)을 두고 `ollama pull qwen2.5:14b` 후 백엔드를 재기동합니다.

---

## RAG (Retrieval-Augmented Generation) 구현

### 설계 결정

**벡터 저장 방식: MariaDB LONGTEXT + Java 레이어 cosine similarity**
- 벡터를 `embedding_vector_json LONGTEXT`에 JSON 배열로 저장, 검색 시 Java에서 계산.
- pgvector(PostgreSQL), MariaDB 11.7+ VECTOR 타입 등 네이티브 방식을 검토했으나 채택 안 함.
- 근거: 현재 ~8,400건, 최대 ~17,000건 예상. 이 규모에서는 Java 계산 + 인메모리 캐시로 충분.
- 10만 건 초과 시 MariaDB 11.7+ VECTOR 타입(VEC_DISTANCE 함수) 마이그레이션 고려.

**프레임워크 미채택 (Spring AI, LangChain4j)**
- Spring AI는 Spring Boot 3.x 필요 → 현재 2.7.18과 호환 안 됨.
- LangChain4j는 붙일 수 있으나 기존 OllamaClient/임베딩 파이프라인과 충돌, 도입 비용 > 이득.
- RAG 핵심(프롬프트 엔지니어링)은 프레임워크가 해결 못 하므로 커스텀 구현이 더 단순.

**다중 인스턴스 캐시**
- 각 인스턴스가 독립적인 인메모리 벡터 캐시를 보유. 문제 없음.
- 각자 기동 시 DB에서 전체 로드, 이후 자기가 처리한 임베딩 완료 시 갱신.

### 구현 구조

```
사용자 메시지
  → JournalEntryEmbeddingSearchService.search(message, topK=5)
      → OllamaClient.embed(message)          // 쿼리 벡터 (app.ollama.embedding-model)
      → ConcurrentHashMap 캐시에서 cosine similarity 계산
      → retrieval_weight(DREAM 1.3, DIARY 1.0, NOTE 0.85) 적용
      → 상위 5개 반환
  → 검색 결과를 systemPrompt 뒤에 주입
  → OllamaClient.chat(systemPrompt, contextMessages)  // app.ollama.chat-model
```

**캐시 갱신 시점**
- 앱 기동: `@PostConstruct`에서 EMBEDDED 상태 전체 로드.
- 임베딩 완료: `JournalEntryEmbeddingWorker.processOne()` 성공 후 단건 갱신.
- 저널 삭제: `JournalEntryEmbeddingQueueService.removeByJournalEntryId()` 호출 시 제거.

**응답 처리**
- AI 응답의 마크다운 굵은글씨(`**text**`, `__text__`) 자동 제거.
- 응답 중단: WebSocket `/chat/session/{id}/cancel` 엔드포인트 → `AtomicBoolean` 플래그로 broadcast 차단.
