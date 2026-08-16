# Reflection 단일 타입 (필수 target)

> 상태: 현재 계약  
> 관련 정본: `docs/migration/journal/reflection-domain-model.md`
> DESIGN_NOTES: journal-reflection 절

## 1. 도메인과 영속

Reflection은 `JOURNAL_REFLECTION` 단일 도메인 타입이며 `journal_reflection` 별도 Aggregate에 영속한다.
대상(`refId`/`refContentType`)은 필수이고 일기·꿈 또는 다른 Reflection을 가리킨다. Reflection은 챕터,
정렬 순서, 태그를 소유하지 않는다.

- 쓰기 DTO: `JournalReflectionPostDto`
- 엔티티: `JournalReflectionEntity`
- 서비스: `JournalReflectionService`
- 등록: `POST /api/journal/reflections`
- 수정: `POST /api/journal/reflection/{id}`
- 삭제: `DELETE /api/journal/reflection/{id}`
- 상세: `GET /api/journal/reflection/{id}`

Reflection 등록은 대상 엔트리의 「해석 등록」 액션에서 시작하며 서버가 대상 필수 조건을 검증한다.
대상 또는 상위 Reflection 삭제는 연결된 Reflection이 존재하면 차단한다.

## 2. 표시와 수정

Reflection은 대상 엔트리의 `reflectionList`에 포함되어 `JournalReflectionItem`으로 임베드 표시한다.
본문·제목 수정은 `JournalReflectionRegistModal`과 전용 API를 사용한다. 저장 응답의
`targetReflectionList`와 `targetLifecycleKey`로 대상 엔트리 상태를 부분 교체한다.

대상 엔트리가 접히면 Reflection도 함께 숨겨진다. 펼침 상태에서는 Reflection 자체 lifecycle과
일자 aside 기본 접힘 모드를 적용한다.

## 3. 라이프사이클과 상태

Reflection은 `OPEN`/`PENDING`/`RESOLVED` lifecycle과 `COLLAPSED`/`IMPRTC`/`REFRNC` 상태를 지원한다.
신규 Reflection은 `PENDING`으로 시작한다.

| 규칙 | 동작 |
|------|------|
| primary → `RESOLVED` | 최초 전환과 동일값 재요청 모두 해당 primary를 대상으로 둔 미완료 Reflection만 `RESOLVED`로 수렴. 이미 `RESOLVED`인 Reflection의 저장·파생 상태·캐시 후처리는 유지 |
| Reflection → `RESOLVED` | 대상 primary lifecycle 유지 |
| `RESOLVED` primary에 Reflection 등록 | primary를 `OPEN`으로 재개하고 파생 `COLLAPSED` 해제 |
| 일자 축 잠금 | Reflection 등록 허용, primary 재개는 잠금 상태 유지 |

## 4. 검색과 태그

`type=DIARY` 검색 결과 행은 `JOURNAL_DIARY` 엔트리다. 대상 일기를 가리키는 Reflection 본문에
키워드가 있으면 `JournalEntrySpec#targetReflectionKeywordSubquery`의 `EXISTS` 조건으로 대상 일기가
검색된다.

태그클라우드·결산·챕터 접힘 요약·검색 태그 조건은 요청한 DIARY 또는 DREAM 단일 축을 사용한다.
Reflection은 태그를 소유하지 않으며 Reflection 저장·수정·삭제는 엔트리 태그 캐시를 무효화하지 않는다.

## 5. 스레드 소속

Reflection은 스레드 소속 엔트리가 아니다. 스레드는 `journal_entry`의 일기·꿈·노트 엔트리를 소속시키며,
각 엔트리를 조회할 때 대상 Reflection 목록을 함께 enrich한다.

## 6. 관련글

Reflection은 두 독립 콘텐츠 사이의 대칭 관계인 `related_content`에 참여하지 않는다. Reflection에서 발견한
기록 간 관계는 Reflection의 대상 원본 엔트리와 상대 엔트리 사이에 연결한다.

## 7. 제약

- 대상 없는 Reflection 등록을 허용하지 않는다.
- Reflection에 태그·챕터·정렬 순서를 추가하지 않는다.
- Reflection을 일반 엔트리 저장 API로 보내지 않는다.
- Reflection을 스레드 소속 엔트리로 추가하지 않는다.
- Reflection을 관련글의 출발점이나 대상으로 추가하지 않는다.
