# Reflection 분리 — RAG·엔티티 인프라 후속 (Deferred)

**상태: 보류(Deferred).** Reflection 을 `journal_reflection` 으로 분리할 때 downstream 소비자인 embedding(RAG)·엔티티 카탈로그 인프라가 어떻게 따라오는지 정리한다. **도메인/영속 결정의 게이트가 아니다** — 도메인 정합성이 우선이고, 이 인프라는 소스에서 재생성되는 파생물이라 모델을 뒤따른다.

> 상위: 도메인 [reflection-domain-model.md](reflection-domain-model.md), 영속 [reflection-persistence-proposal.md](reflection-persistence-proposal.md). 이 문서는 분리 확정 이후의 인프라 rework 를 다룬다.

---

## 1. 실측 결과 (2026-08-05, 읽기 전용 조사)

- **하드 FK 없음.** `journal_entry_embedding`·`journal_entry_entity_ref`·`journal_entry_entity_job`·`journal_entry_entity_role` 어디에도 `REFERENCES journal_entry` 제약이 없다. `journal_entry_id` 는 INDEX/UNIQUE 만 붙은 INT — **앱 레벨 soft 참조**다. 분리에 DDL FK 수술 불필요.
- **소스-of-truth = `journalEntryRepository.findAll()`.** embedding sync·entity queue·entity ref sync 가 모두 전체 entry 를 돌아 **현재 Reflection 행도 파이프라인에 포함**된다.
- **식별자 = `journal_entry_id`**(단일 id 공간 가정). 분리 시 `journal_reflection` 이 자체 id 공간을 가지므로 이 가정이 깨진다 → `(source_id, content_type)` 다형화 필요.

| 인프라 | content_type | 식별자 | 일반화 난이도 |
|---|---|---|---|
| `journal_entry_embedding` | **있음**(NOT NULL) + content_kind | `journal_entry_id` + UNIQUE | **中** — content_type 이미 보유. 식별자만 `(source_id, content_type)` 재해석 + UNIQUE 변경 |
| `journal_entry_entity_ref` | 없음 | `journal_entry_id` (INDEX) | **中~大** — source_type 컬럼 신설 |
| `journal_entry_entity_job` | 없음 | `journal_entry_id` + UNIQUE | **中~大** — 동일 + UNIQUE 재작업 |
| `journal_entry_entity_role` | (entity_ref 경유) | `journal_entry_entity_ref_id` | 자동 — entity_ref 만 고치면 따라옴 |
| attachable(tag/file/state) | `ref_content_type` 다형 | `ref_id`+`ref_content_type` | 무관 — 이미 다형 |

## 2. 보류된 제품 질문

**분리 후 Reflection 이 RAG(embedding)·엔티티 추출(인물 멘션)에 참여해야 하는가?**

- **참여** → 두 파이프라인의 소스 반복·식별자를 `(source_id, content_type)` 로 일반화하고 `journal_reflection` 도 순회한다.
- **불참** → `journal_reflection` 을 소스에서 제외한다(싸지만 사유가 RAG·인물 그래프에서 빠지는 기능 회귀).

이 질문은 도메인/영속 분리를 **막지 않는다.** 분리는 도메인 근거로 진행하고, 이 답은 인프라 rework 시점에 정한다.

## 3. 실행 순서 (분리 확정 후)

참여로 결정되면: 인프라를 `(source_id, content_type)` 로 **먼저 일반화**한 뒤(또는 un-merge 와 함께) `journal_reflection` 순회를 추가한다. embedding 은 content_type 을 이미 보유해 가볍고, entity_ref/job 은 source_type 신설이 선행한다.

## 관련 코드 (조사 시점 2026-08-05)

- 소스 반복: `JournalEntryEmbeddingQueueService#syncWithJournalEntries`, `JournalEntryEntityQueueService`(sync), `JournalEntryEntityRefSyncService`.
- 식별자·스키마: `journal_entry_embedding`/`entity_ref`/`entity_job` (`schema-journal-mariadb.sql`) — FK 없음, `journal_entry_id` 키.