# 저널 엔트리 임베딩 구조 확장 (아이디어)

> 상태: **아이디어 (계약 외부)** — 중첩 payload, 세그먼트 가중치, 복합 랭킹은 현재 저장·검색 계약이 아니다.

## 세그먼트 가중치 후보

| segment | weight |
| --- | ---: |
| `title` | `1.25` |
| `content` | `1.00` |
| `chapter` | `0.85` |
| `tags` | `1.35` |
| `states` | `1.20` |
| `meta` | `1.10` |
| `time` | `0.70` |

태그와 상태를 사용자의 의미 분류 신호로 보고 본문보다 높은 가중치를 주는 방안이다. 실제 소비식과 품질 실측 없이 값이나 필드를 계약으로 확정하지 않는다.

## 중첩 payload 후보

```json
{
  "schemaVersion": 1,
  "ref": {
    "type": "journal_entry",
    "id": 123,
    "contentType": "JOURNAL_DREAM",
    "kind": "DREAM",
    "typeWeight": 1.30
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
    "type": 1.30,
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

현재 flat payload에서 이 구조로 전환하려면 기존 행 변환, schema version 소비 규칙, 구버전 제거 시점을 함께 합의해야 한다.

## 복합 랭킹 후보

```text
final_score =
  vector_similarity
  * type_weight
  + tag_match_score
  + state_match_score
  + date_proximity_score
```

태그·상태·날짜 신호의 산식과 품질 기준은 열린 질문이다.

## 벡터 저장 엔진 전환 후보

현재 계약은 MariaDB `LONGTEXT`에 JSON 배열을 저장하고 Java 레이어에서 cosine similarity를 계산한다. 데이터 규모와 검색 부하가 커져 이 방식이 병목으로 확인되면 MariaDB 11.7+ `VECTOR` 타입과 `VEC_DISTANCE` 함수로 저장·검색 책임을 옮기는 방안을 검토한다.

전환 판단에는 단순 행 수가 아니라 실제 검색 지연, 메모리 캐시 크기, 기동 적재 시간과 운영 MariaDB 버전을 함께 사용한다. 기존 문서에서 언급한 10만 건은 관찰 시작점 후보이며 자동 전환 조건이 아니다.

## 관련 현재 계약

- [JOURNAL_ENTRY_EMBEDDING_DESIGN.md](../spec/JOURNAL_ENTRY_EMBEDDING_DESIGN.md)
