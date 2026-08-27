# Reflection RAG·엔티티 참여 정책 (아이디어)

> 상태: **아이디어 (계약 외부)** — 별도 영속 Reflection이 embedding(RAG)·엔티티 카탈로그에 참여할지는 합의되지 않았다. 현재 Reflection 도메인·영속 계약의 게이트가 아니다.

## 확인된 인프라 사실

- `journal_entry_embedding`·`journal_entry_entity_ref`·`journal_entry_entity_job`·`journal_entry_entity_role`은 `journal_entry`를 참조하는 DB FK가 없다.
- embedding sync·entity queue·entity ref sync는 `journalEntryRepository.findAll()`을 소스로 사용한다.
- 현재 인프라는 `journal_entry_id` 단일 ID 공간을 가정한다. 별도 `journal_reflection` ID 공간을 함께 소비하려면 `(source_id, content_type)` 식별이 필요하다.

| 인프라 | 현재 content type | 현재 식별자 | 참여 시 변경 후보 |
|---|---|---|---|
| `journal_entry_embedding` | NOT NULL `content_type` + `content_kind` | `journal_entry_id` + UNIQUE | 식별자를 `(source_id, content_type)`로 재해석하고 UNIQUE 변경 |
| `journal_entry_entity_ref` | 없음 | `journal_entry_id` INDEX | `source_type` 추가 |
| `journal_entry_entity_job` | 없음 | `journal_entry_id` + UNIQUE | `source_type` 추가와 UNIQUE 변경 |
| `journal_entry_entity_role` | entity ref 경유 | `journal_entry_entity_ref_id` | entity ref 변경을 따름 |
| attachable | `ref_content_type` | `ref_id + ref_content_type` | 이미 다형이므로 대상 아님 |

## 열린 제품 질문

별도 영속 Reflection이 RAG와 엔티티 추출에 참여해야 하는가?

- **참여 후보**: 두 파이프라인의 소스 반복과 식별자를 다형화하고 `journal_reflection`도 순회한다.
- **불참 후보**: 파생 인프라의 소스를 Primary 엔트리로 한정한다. 이 경우 Reflection 내용은 RAG와 인물 그래프에 포함되지 않는다.

참여로 합의하면 인프라를 `(source_id, content_type)`로 일반화한 뒤 Reflection 순회를 추가한다. entity ref·job은 source type을 저장하는 스키마 변경이 선행된다.

## 확인 경로

- 소스 반복: `JournalEntryEmbeddingQueueService#syncWithJournalEntries`, `JournalEntryEntityQueueService`, `JournalEntryEntityRefSyncService`
- 식별자·스키마: `journal_entry_embedding`, `journal_entry_entity_ref`, `journal_entry_entity_job`

## 관련 현재 계약

- [Reflection 도메인 모델](../migration/journal/reflection-domain-model.md)
- [Reflection 영속 계약](../migration/journal/reflection-persistence-contract.md)
- [Reflection 단일 타입](../spec/REFLECTION_ONE_TYPE.md)
