# Reflection 영속 계약

> **상태: 부분 구현(⚠).** 애플리케이션의 별도 Aggregate 경로와 마스터 스키마는 구현되어 있다. 기존 DB를 같은 구조로 수렴시키는 Flyway SQL과 `journal_entry` 잔존 경로 정리는 완료되지 않았다.
>
> 도메인 정체성은 [reflection-domain-model.md](reflection-domain-model.md), 사용자 동작은 [REFLECTION_ONE_TYPE.md](../../spec/REFLECTION_ONE_TYPE.md)가 정본이다. 이 문서는 Repository·Mapper·Projection·Storage 경계를 정의한다.

## 1. 현재 영속 구조

Reflection은 `journal_reflection` 전용 테이블과 `JournalReflectionEntity`·`JournalReflectionRepository`·`JournalReflectionService`로 영속한다.

- `content_type`은 `JOURNAL_REFLECTION`으로 고정한다.
- `ref_id`와 `ref_content_type`은 필수 About-A 대상 키다.
- `title`은 선택값이고 `content`는 Reflection 본문이다.
- `sort_order`는 같은 대상 아래 형제 Reflection의 1부터 시작하는 연속 순번이다.
- Reflection은 자기 `journal_chapter_id`·`journal_day_id`를 갖지 않으며 대상 체인을 통해 소유 일자를 해석한다.
- audit·soft-delete와 file·comment·state·lifecycle·history attachable 축을 사용한다.
- 태그와 Prefix는 Reflection 영속 축에 포함하지 않는다.

마스터 스키마 `schema/full/mariadb/schema-journal-mariadb.sql`은 `journal_reflection` 테이블과 대상·작성자 인덱스를 정의한다.

## 2. API·조회 경계

| 동작 | 경로·소유자 |
|---|---|
| 등록 | `POST /api/journal/reflections` · `JournalReflectionService` |
| 상세 | `GET /api/journal/reflection/{id}` |
| 수정 | `POST /api/journal/reflection/{id}` |
| 삭제 | `DELETE /api/journal/reflection/{id}` |
| 대상 엔트리 합성 | `JournalEntryReflectionEnricher`가 `ref_id + ref_content_type`으로 조회해 `reflectionList`에 배치 |
| 키워드 검색 | `JournalEntrySpec`이 `journal_reflection` EXISTS 서브쿼리로 대상 Primary 엔트리를 매칭 |

Reflection은 Primary 엔트리의 플랫 검색 결과 행이 아니라 대상 엔트리 아래 embed로 표시된다. 저장된 Reflection의 상세·수정·삭제 권한은 대상 체인이 귀속되는 `journal_day.owner_id`를 기준으로 판정한다.

## 3. 삭제·대상 불변식

- 등록과 수정은 대상 키의 존재와 소유권을 검증한다.
- 대상 엔트리·대상 챕터는 자신을 참조하는 Reflection이 있으면 서비스 경계에서 삭제를 거부한다.
- 다른 Reflection의 대상인 Reflection도 하위 참조가 있으면 삭제를 거부한다.
- 삭제 후 같은 대상 아래 `sort_order`를 다시 1..N으로 정규화한다.
- DB FK 대신 서비스 경계의 재귀 Block 정책을 사용한다.

## 4. 구현 현황

| 계약 항목 | 상태 | 저장소 근거 |
|---|:---:|---|
| 전용 Entity·Repository·Service·Controller | ✓ | `feature/journal/reflection` |
| 전용 등록·상세·수정·삭제 API | ✓ | `JournalReflectionRestController` |
| 대상 엔트리 embed·검색·권한·삭제 Block | ✓ | enricher·검색 spec·ownership guard·통합 테스트 |
| Vue 등록/수정 모달·embed 액션 | ✓ | `JournalReflectionRegistModal.vue`·`JournalReflectionItem.vue` |
| 신규 설치용 마스터 스키마 | ✓ | `schema-journal-mariadb.sql` |
| 등록·수정 대상 존재·소유권 검증 | ⚠ | 등록은 대상 키의 null 여부만 검사하고, 수정은 저장된 대상의 소유권만 검사함 |
| `content_type=JOURNAL_REFLECTION` 강제 | ⚠ | 애플리케이션 기본값은 있으나 DB `NOT NULL`·`CHECK`와 요청값 강제 고정이 없음 |
| 기존 DB 생성·데이터 이동·attachable 재키잉 Flyway SQL | ❌ | `schema/migration/mariadb/`에 해당 migration 파일 없음 |
| `journal_entry` 영속 경로 수렴 | ⚠ | target 컬럼과 Reflection 전용 분기·역참조 Repository 메서드가 남아 있음 |

## 5. 미완료 수렴 경계

기존 DB 수렴은 추적 가능한 Flyway migration 파일을 필요로 한다. 이 migration은 다음을 하나의 데이터 계약으로 처리해야 한다.

1. `journal_reflection` 생성과 제약·인덱스 구성
2. `journal_entry(content_type=JOURNAL_REFLECTION)` 데이터의 대상 필수 검증과 전용 테이블 이동
3. ID가 바뀌는 행의 attachable `ref_id + ref_content_type` 재키잉
4. 대상 없는 행의 합의된 재분류 적용
5. 이동 검증 후 `journal_entry`의 Reflection 행과 전용 target 경로 정리

재키잉 검증 기준은 [attachable-rekey-methodology.md](../attachable-rekey-methodology.md)를 따른다. 저장소에는 이 migration이 없으므로 기존 운영 DB의 적용 여부와 데이터 수렴 상태를 이 문서가 보증하지 않는다.

애플리케이션에서도 `journal_entry.ref_id/ref_content_type`, `JournalEntryService`의 Reflection 챕터 검증 분기, `JournalEntryRepository.findAllByContentTypeAndRefIdIn` 등 전용 Aggregate와 겹치는 경로가 남아 있다. 현재 쓰기 API는 `JournalReflectionService` 단일 경로를 사용하지만, 영속 모델의 완전한 단일화에는 이 잔존 경로 제거가 필요하다.

## 6. 관련 현재 계약

- [Reflection 도메인 모델](reflection-domain-model.md)
- [Reflection 단일 타입](../../spec/REFLECTION_ONE_TYPE.md)
- [저널 화면 spec](screen-spec.md)
- [저널 인터랙션 spec](interaction-spec.md)
- [저널 컴포넌트 spec](component-spec.md)
