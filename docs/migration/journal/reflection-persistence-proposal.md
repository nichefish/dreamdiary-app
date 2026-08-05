# Reflection 영속 제안 (Commentary 분리)

**상태: 설계 확정(Accepted) · 구현 미착수.** 이 문서는 [reflection-domain-model.md](reflection-domain-model.md) 가 정의한 Reflection 도메인(별도 Aggregate Root)을 **어떻게 저장·이관할지** 제안한다. 도메인 계약은 그 문서가 SSOT 이고, 이 문서는 영속(Repository·Mapper·Projection·Storage) 결정만 다룬다.

> 이 문서는 [reflection-absorption.md](reflection-absorption.md) 의 STI 결정(§1.14 단일 테이블 상속 / §5 "Entry 서브타입, 별도 aggregate 아님")을 **대체**한다. 흡수 마이그레이션(interpretation→entry)의 실행 이력은 Git 이 보존하며, 이 제안이 착지하면 reflection-absorption.md 는 폐기한다.
> **범위**: 영속 target 설계와 현재→target 델타. 인프라 실측(embedding·entity)은 **완료**(§3) — RAG/엔티티 rework 는 [reflection-rag-entity-followup.md](reflection-rag-entity-followup.md) 로 이관(도메인 뒤따름, 게이트 아님). 구현(un-merge·삭제 정책)은 후속 SAVEPOINT.

---

## 0. 현재 상태

현재 코드·DB 는 흡수 STI 다 — Reflection 은 `journal_entry`(`contentType=JOURNAL_REFLECTION`) 행이며 target(`ref_id`/`ref_content_type`)·`journal_chapter_id` 를 Entry 와 공유한다. 흡수 마이그레이션 V0.26.0~V0.26.2 가 실 DB 에 적용됐다. 이 문서는 그 STI 를 Commentary 별도 영속으로 되가르는 target 을 제안한다.

## 1. 영속 모델 (target)

### 1.1 독립 영속 모델
Reflection 은 별도 Aggregate Root 이므로 **독립 영속 모델**(Repository·Mapper·Projection·Storage)을 갖는다. 이 단계는 AR 경계에서 자연스럽게 따라온다.

### 1.2 저장소 후보 — journal_reflection 테이블
독립 영속 모델의 물리적 실현 **현재 후보 = `journal_reflection` 테이블**이다:
- 컬럼: id · 본문 · `ref_id` + `ref_content_type`(**NOT NULL**) · audit · soft-delete · attachable 키.
- **chapter·sort·day 컬럼 없음.** Primary 스트림 배치가 아니므로 자기 chapter 를 소유하지 않고, chapter/day 는 **대상 A 를 경유**해 파생한다.
- About-A 는 `ref_content_type`/`ref_id` NOT NULL 로 스키마에 고정한다.

### 1.3 정당화 원칙
별도 Aggregate Root 가 곧 별도 테이블을 **강제하지는 않는다**(Aggregate→Storage 는 필연이 아니다). 다만 별도 AR 은 독립 영속 모델을 갖는 것이 자연스럽고, 그 모델을 기존 `journal_entry` 에 STI 로 합치려면 **별도 정당화**가 필요하다. STI 의 대표 이득(다형 peer 조회)은 Primary 스트림에서 나란히 조회되는 Diary/Dream/Note 에는 적용되지만 **Reflection 에는 적용되지 않는다**(스트림 peer 아님). 그래서 STI 유지의 정당화 후보는 **공유 인프라 결합**뿐이었고, 그것은 §3 실측으로 해소됐다. 조회 패턴·shape 유사성은 테이블 근거가 아니다.

### 1.4 상태
- **저장소 후보(journal_reflection 분리): 설계 확정(Accepted).** §3 인프라 실측 결과, 분리를 뒤집을 유일 조건(embedding·entity 가 `journal_entry` 전용 **하드 FK** 로 묶여 일반화 불가)이 **불성립**이다 — 결합은 DB FK 가 아니라 앱 레벨 soft 참조이며 `(source_id, content_type)` 로 일반화 가능하다. 도메인 정합성이 우선이고 RAG/embedding·entity 는 이를 뒤따르는 downstream 소비자이므로 분리의 게이트가 아니다.
- 남은 것은 검증이 아니라 **구현**: un-merge SQL, 대상 삭제 정책(§5), RAG/엔티티 rework([reflection-rag-entity-followup.md](reflection-rag-entity-followup.md) 로 이관).

## 2. 현재(흡수 STI) → target 델타

흡수 데이터 마이그레이션(V0.26.1/V0.26.2)이 실 DB 에 적용됐으므로, 되가르기는 **실데이터 un-merge 마이그레이션**을 수반한다(릴리즈 전이라 허용, 비용은 실재).

| 현재(흡수 STI) | target(Commentary 분리) |
|---|---|
| `journal_entry`(`JOURNAL_REFLECTION`) + target 컬럼 | `journal_reflection` 별도 테이블로 분리(un-merge SQL) |
| `journal_chapter_id` NOT NULL·hard-owned | Reflection 에 chapter 없음. chapter/day 는 대상 경유 파생 |
| 대상 삭제 시 target nullify → Chapter 직속 독립화 | 독립화 폐기. 대상 삭제 정책 재정의(§5) |
| orphan-NOTE 버킷(대상 없는 Reflection) | Standalone 폐기 → Reclassification(Diary/Note). 대상 참조 NOT NULL 로 차단 |
| `ref_content_type` nullable(독립 마커 포함) | NOT NULL(About-A) |
| embedding·entity queue 가 `journal_entry_id` 로 Reflection 포함 | `(source_id, content_type)` 일반화(soft 결합 확인). rework 는 followup 문서로 이관 |
| attachable = `ref_id`+`ref_content_type` **다형** | 그대로. 테이블 분리와 무관하게 동작 |
| `JournalEntryReflectionEnricher`(target 역참조 `findAllByContentTypeAndRefIdIn`) | `journal_reflection` 조회로 전환, Reflection repository 분리 |
| 검색: `contentType` + `refContentType` facet, 플랫 검색 배제 | 그대로(배제 정책 유지) — 조회 정책은 Repository/Projection 층 |

## 3. 인프라 실측 결과 (완료 — 2026-08-05)

질문: embedding·entity(ref/job) 인프라가 `journal_entry` 에 **하드 FK 결합**인가, `(source_id, content_type)` 로 **일반화 가능**한가?

**결과: 하드 FK 아님 — 앱 레벨 soft 참조, 일반화 가능.**
- FK 제약 없음: embedding/entity_ref/entity_job 어디에도 `REFERENCES journal_entry` 없음. `journal_entry_id` 는 INDEX/UNIQUE 만.
- 두 큐 sync 가 `journalEntryRepository.findAll()` 을 소스로 돌아 현재 Reflection 도 포함. 식별자 = `journal_entry_id`.
- **embedding**: `content_type` NOT NULL 이미 보유(반쯤 일반화) → 식별자만 `(source_id, content_type)` 재해석. 中.
- **entity_ref/job**: content_type 없음 → source_type 컬럼 신설 필요. 中~大. entity_role 은 entity_ref 경유로 자동.
- **attachable**: `ref_id`+`ref_content_type` 다형이라 테이블 분리의 **게이트는 아니다**. 단 id 재발급을 수반하는 이관은 사이드테이블(`state`·`lifecycle`·`history`·`tag_content` 등)의 `ref_id` 재키잉이 **필수**다(안 하면 고아·충돌) — [attachable-rekey-methodology.md](../attachable-rekey-methodology.md) 참조.

상세·후속 작업(제품 질문: Reflection 이 RAG/엔티티에 남는가, 실행 순서)은 [reflection-rag-entity-followup.md](reflection-rag-entity-followup.md) 로 이관한다.

## 4. 결정 로그 (영속 근거)

- **영속 분리의 근거 = AR 경계**: peer 부재·shape 차이는 증상이다. "peer 아니면 STI 불가"는 법칙이 아니라 *약함*(STI 이득 부재)이다. 별도 AR 은 독립 영속 모델이 자연스럽고, STI 로 합치려면 별도 정당화가 필요하며, 그 정당화(공유 인프라)는 §3 실측으로 해소됐다.
- **영속 모델 ≠ 테이블**: AR→영속 모델은 자연스럽지만, 영속 모델→특정 테이블은 후보 선택이며 검증 대상이다. 저장 후보가 바뀌어도 도메인(별도 AR)과 영속 모델(독립)은 불변이다.
- **RAG·엔티티는 downstream, 게이트 아님**: embedding·엔티티 카탈로그는 소스에서 재생성되는 파생 소비자다. 도메인 정합성이 우선이고 인프라가 모델을 뒤따른다. 실측이 결합을 soft(앱 레벨)·일반화 가능으로 확인해 분리를 막지 않는다.
- **attachable 다형 유지**: `ref_id`+`ref_content_type` 는 테이블 분리와 독립적으로 동작하므로 co-location 근거가 아니다.
- **조회 정책 ≠ 테이블**: 플랫 검색 배제는 Repository/Projection 층 결정이며 테이블 토폴로지를 강제하지 않는다.

## 5. 대상 삭제 메커니즘 (잠정 Block · 전체 의미론 보류)

도메인 정책([reflection-domain-model.md](reflection-domain-model.md) §5)이 잠정 **Reference→Block(재귀)+명시 cascade** 로 정해졌다. 런타임(R4c/R4d): `JournalEntryService.preDelete`·`JournalReflectionService.preDelete`·`JournalChapterService.preDelete` 가 참조 Reflection 존재 시 삭제를 거부한다(DB FK 없음 → 서비스 레벨 Block). nullify→독립화 훅은 제거됐다. 명시 cascade(대상+Reflection 동시 삭제)는 후속이다. 전체 관계 생명주기 의미론은 [RELATIONSHIP_LIFECYCLE.md](../../spec/RELATIONSHIP_LIFECYCLE.md)(보류).