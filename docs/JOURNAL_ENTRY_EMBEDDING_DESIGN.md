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
| `JOURNAL_DREAM` | `DREAM` | `1.15` | 상징/감정/해석 밀도가 높아 semantic query에서 중요 |
| `JOURNAL_NOTE` | `NOTE` | `0.90` | 외부 현실/메모 성격이 강해 기본 회상에서는 약간 낮춤 |

이 값은 embedding vector를 변형하지 않는다. 검색 결과 ranking 단계에서 score multiplier로 사용한다.

## 세그먼트 가중치

`embedding_payload_json.weights`에 다음 값을 저장한다.

| segment | weight |
| --- | ---: |
| `title` | `1.25` |
| `content` | `1.00` |
| `chapter` | `0.85` |
| `tags` | `1.35` |
| `states` | `1.20` |
| `meta` | `1.10` |
| `time` | `0.70` |

태그와 상태는 짧지만 사용자의 의미 분류가 직접 반영된 신호라 본문보다 높게 둔다.

## embedding_text

모델에 넣는 텍스트는 사람이 읽을 수 있는 labeled text로 만든다.

```text
[TYPE] DREAM

[DATE] 2025-04-10

[TITLE] ...

[CHAPTER] ...

[TAGS] 불안, 가족

[STATES] NHTMR

[META] mood=anxious

[CONTENT]
...
```

이 방식은 모델에 문맥을 주면서도, 원문을 과도하게 조작하지 않는다.

## embedding_payload_json

v1 payload 구조:

```json
{
  "schemaVersion": 1,
  "ref": {
    "type": "journal_entry",
    "id": 123,
    "contentType": "JOURNAL_DREAM",
    "kind": "DREAM",
    "typeWeight": 1.15
  },
  "time": {
    "journalDate": "2025-04-10",
    "precision": "EXACT",
    "semanticCreatedAt": "2025-04-10 00:00:00"
  },
  "text": {
    "title": "...",
    "chapterTitle": "...",
    "content": "..."
  },
  "signals": {
    "tags": ["불안", "가족"],
    "states": ["NHTMR"],
    "meta": [
      {
        "name": "mood",
        "category": "emotion",
        "value": "anxious",
        "unit": null
      }
    ]
  },
  "weights": {
    "type": 1.15,
    "title": 1.25,
    "content": 1.0,
    "chapter": 0.85,
    "tags": 1.35,
    "states": 1.2,
    "meta": 1.1,
    "time": 0.7
  },
  "embedding": {
    "status": "PENDING",
    "textHash": "...",
    "model": null
  }
}
```

## 검색 단계에서의 사용

기본 검색 점수:

```text
final_score = vector_similarity * retrieval_weight
```

추후 확장:

```text
final_score =
  vector_similarity
  * type_weight
  + tag_match_score
  + state_match_score
  + date_proximity_score
```

초기에는 타입 가중치만 사용하고, 태그/상태/날짜 가중치는 ranking 고도화 단계에서 반영한다.
