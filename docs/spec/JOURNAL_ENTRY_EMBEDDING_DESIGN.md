# Journal Entry Embedding Design

## 목적

저널 임베딩은 단순히 본문을 벡터로 바꾸는 작업이 아니다. Dreamdiary에서는 기록의 **시점**, **종류**, **태그/상태/메타 신호**, **검색 시 가중치**가 함께 보존되어야 한다.

따라서 임베딩 데이터는 두 층으로 나눈다.

- `embedding_text`: 실제 embedding model에 넣는 텍스트
- `embedding_payload_json`: 검색/스코어링/디버깅에 쓰는 구조화 메타데이터

벡터 자체는 후속 worker가 `embedding_vector_json`에 저장한다.

## 시간 기준

임베딩 row의 `created_at`은 migration 실행 시간이 아니라 기록의 의미 시점이어야 한다.

우선순위:

```text
journal_day.journal_date
-> journal_entry.created_at
-> NOW()
```

즉, 2023년의 꿈을 2026년에 임베딩하더라도 검색/회상 축에서는 2023년 기록으로 다룬다.

## 타입 가중치

초기 v1 가중치는 다음과 같다.

| content_type | kind | weight | 이유 |
| --- | --- | ---: | --- |
| `JOURNAL_DIARY` | `DIARY` | `1.00` | 기준 개인 기록 |
| `JOURNAL_DREAM` | `DREAM` | `1.30` | 상징/감정/해석 밀도가 높아 semantic query에서 중요 (`JournalEntryEmbeddingQueueService.resolveRetrievalWeight`) |
| `JOURNAL_NOTE` | `NOTE` | `0.85` | 외부 현실/메모 성격이 강해 기본 회상에서는 약간 낮춤 (`JournalEntryEmbeddingQueueService.resolveRetrievalWeight`) |

이 값은 embedding vector를 변형하지 않는다. 검색 결과 ranking 단계에서 score multiplier로 사용한다.

## embedding_text

모델에 넣는 텍스트는 사람이 읽을 수 있는 labeled text로 만든다.

```text
유형: DREAM
날짜: 2025-04-10
챕터: ...
챕터 말머리: 회고
핵심 태그: [감정]#불안 [관계]#가족
주제 태그: [감정]#불안 [관계]#가족
태그: [감정]#불안 [관계]#가족
제목: ...
본문: ...
타인의 꿈 여부: N
꿈 제공자: ...
```

`JournalEntryEmbeddingQueueService.buildEmbeddingText()`가 `라벨: 값` 한 줄 형식으로 구성한다. 빈 값 라벨은 생략한다. RAG·person-meaning 스니펫 파서(`본문:` 등)와 동일 계약이므로 테스트 fixture도 이 접두를 따라야 한다.

## embedding_payload_json

`JournalEntryEmbeddingQueueService.buildPayload()`는 현재 다음 키를 가진 flat map을 저장한다.

```text
source, journalEntryId, contentType, contentKind, retrievalWeight,
title, journalChapterId, journalChapterTitle, journalChapterType,
journalChapterSummaryYn, journalChapterPrefixId, journalChapterPrefixName,
journalDayId, journalDate, journalDatePrecision, yy, mnth,
sortOrder, dreamerName, tags
```

시스템 요약은 사용자 Prefix가 아니며 `journalChapterSummaryYn=Y`와 요약 역할명으로 구분한다. 관리자 임베딩 동기화는 `queueForEntry` 계약으로 텍스트와 payload를 다시 구성한다. 전수 sync는 챕터와 기존 임베딩 행을 배치 조회하고, 진행 하트비트는 50건마다 갱신한다.

## 검색 단계에서의 사용

기본 검색 점수:

```text
final_score = vector_similarity * retrieval_weight
```

## 품질 실측 (Quality Eval)

한국어 의미 유사도가 현재 임베딩 모델에서 RAG에 쓸 만한지 빠르게 판단하는 운영 도구입니다. DB 네이티브 VECTOR 여부와 무관하게 **저장된 벡터·쿼리 임베딩 간 코사인**만 검증합니다.

| 항목 | 값 |
| --- | --- |
| Endpoint | `GET /api/admin/journal-entry-embeddings/quality-eval` |
| 권한 | `ROLE_MNGR` |
| 전제 | Ollama 임베딩 API 가동 (`app.ollama.embedding-model`, 기본 `nomic-embed-text`). 실행 전 `GET /api/admin/ollama/health`로 선행 핑 |

### Ollama health

| 항목 | 값 |
| --- | --- |
| Endpoint | `GET /api/admin/ollama/health` |
| 권한 | `ROLE_MNGR` |
| 동작 | `GET /api/tags`로 연결·필수 모델(`app.ollama.chat-model`, `app.ollama.embedding-model`) 설치 여부만 확인. **자동 `ollama serve`는 하지 않음** |
| `status` | `UP` (연결+모델 준비), `DEGRADED` (연결됐으나 모델 누락), `DOWN` (연결 불가) |

Quality Eval 리포트에는 실측 시점 `ollamaHealth`가 포함됩니다. health가 `DOWN`/`DEGRADED`(임베딩 모델 미설치)이면 스위트는 실행하지 않고 `OLLAMA_UNAVAILABLE`과 구체적 `summary`를 반환합니다.

### 스위트

| 코드 | 내용 | 통과 기준 |
| --- | --- | --- |
| `PARAPHRASE` | 고정 한국어 paraphrase 10쌍 코사인 | 각 쌍 ≥ 0.72, 통과율 ≥ 80% |
| `RELATED_DISTINCT` | anchor / related / unrelated 10삼중 | related−unrelated ≥ 0.05, 통과율 ≥ 80% |
| `CORPUS_SELF_RANK` | 캐시된 저널 embedding_text 프로브 vs 자기·타 벡터 | self > bestOther, 통과율 ≥ 70% |

### 권고값 (`recommendation`)

| 값 | 의미 |
| --- | --- |
| `KEEP_MODEL` | 고정 시드·코퍼스 샘플 기준 유지 가능 |
| `REVIEW_MODEL` | 한국어 모델 교체(bge-m3 등) 검토 |
| `OLLAMA_UNAVAILABLE` | Ollama 미가동·임베딩 모델 누락으로 실측 불가 (`ollamaHealth`·`summary` 참고) |

리포트에는 `SKIPPED` row 사유 샘플(최대 20건)도 포함됩니다.

Admin UI **AI Embedding Backfill** 섹션의 **Refresh** 시 Ollama 상태 배너를 함께 갱신하고, **Quality Eval** 버튼으로 동일 API를 호출합니다.
