# 스레드 연관(thread-relation) 계약

> **상태: 구현 완료(✓).** 이 문서가 스레드 연관의 현재 도메인·화면 계약 정본이다.
>
> **백엔드:** `RelatedContentService.SUPPORTED_TYPES`가 `JOURNAL_THREAD`를 지원하고, `resolveCreatedBy`·`resolveTitle`은 contentType별로 소유자·제목을 해석한다. `JournalDayResolvedGuard.assertWritableForRef`는 일자 완결 축 밖인 `JOURNAL_THREAD`를 통과시킨다. `GET/POST /api/related/JOURNAL_THREAD/{id}`와 `DELETE /api/related/{relatedContentId}`가 연관 CRUD를 제공한다.
>
> **뷰 합성:** `JournalEntryDto.sourceThreadId`가 provenance를 제공한다. `JournalThreadEntryService.getEntriesByThread(threadId, relatedThreadIds)`는 요청된 1-hop 연관 스레드만 합성하고 중복 제거와 provenance 설정을 수행한다. `GET /api/journal/threads/{id}/entries?relatedThreadIds=`가 합성 결과를 제공한다.
>
> **프론트엔드:** `JournalThreadPickerModal.vue`가 스레드 검색·선택을 제공한다. 연관 스레드 목록의 행별 토글이 `relatedThreadIds`를 구성하고, 빌려온 엔트리는 출처 스레드 배지를 표시하며 `sourceThreadId`가 있으면 멤버십 제거를 숨긴다.

## 1. 사용자 동작

서로 관련된 스레드를 **연관**으로 묶고, 한 스레드 상세에서 **토글** 하나로 연관 스레드의 엔트리까지 **같은 시간축에 겹쳐** 본다.

- **행 OFF**: 그 연관 스레드는 시간축에 올리지 않는다.
- **행 ON**: 해당 연관 스레드의 엔트리를 base와 한 시간축에 날짜순으로 섞어 본다. 켜진 행이 없으면 base 멤버만 본다.

핵심: **멤버십 병합이 아니라 뷰 시점의 합집합**이다. 연관 스레드는 자기 멤버십·데이터를 그대로 두고, base 화면에서만 읽기 오버레이로 끌어온다. 토글을 끄면 즉시 원복된다.

## 2. 관계·표시 계약

1. **연관 범위 = 1-hop 대칭.** A 기준이면 A 에 직접 연관된 것만 본다. 전이(A–B–C 에서 A 가 C 까지)는 없다.
2. **연관 유형 없음.** 단일 기본 relationType 으로 고정하고 스레드 UI 에서 유형 선택을 숨긴다.
3. **토글 상태 = 화면 임시·행 단위.** 연관 스레드마다 합성 on/off. 기본 OFF, 저장하지 않는다(뷰 옵션 성격).
4. **출처 칩.** 빌려온(연관) 엔트리에 출처 스레드 제목 칩(스레드별 고정 색, 채움)을 붙여 base와 구분한다.
5. **중복 제거.** base·연관 양쪽에 속한 엔트리는 한 번만, base 소속으로 표시한다.
6. **멤버십 제거 게이팅.** 빌려온 엔트리는 「스레드에서 빼기」를 숨긴다(안 속한 스레드에서 뺄 수 없음; 자기 것만 뺀다). 그 외 엔트리 액션(수정·복사·해석·라이프사이클)은 유지 — 막는 건 멤버십 제거 하나뿐.

## 3. 연관 모델

- thread ↔ thread 연관은 `related_content`의 `JOURNAL_THREAD` 지원을 사용하며 1-hop 대칭이다. `related_content`는 `(id, contentType)` 제네릭 쌍인 left/right를 저장한다.
- 정규화된 쌍에서 작은 키를 left에 두고 1행으로 저장한다. 자기 자신과의 연관은 금지하며 동일 소유자 콘텐츠만 연결한다.

## 4. 뷰 합성 + provenance

- 스레드 상세 조회에 `relatedThreadIds` 옵션을 둔다. 전달된 ID(실제 1-hop 연관만)의 멤버 엔트리를 base와 합쳐(중복 제거) 기존 정렬축(일자 → 원본 엔트리 sort_order → ID)으로 내려준다.
- 응답이 각 엔트리에 **provenance(출처 스레드 / base 멤버 여부)** 를 실어, 프론트가 하나의 데이터로 둘을 처리한다:
  - 빌려온 엔트리 → **출처 스레드 칩**(스레드별 고정 색).
  - 빌려온 엔트리 → **「스레드에서 빼기」 숨김**.
  - base·연관 동시 소속 → dedup 후 **base 멤버로 취급**(배지 없음, 「빼기」 노출).

## 5. related_content 연결 경계

- `RelatedContentService.SUPPORTED_TYPES`는 `JOURNAL_THREAD`를 포함한다.
- `resolveCreatedBy`·`resolveTitle`은 contentType별 디스패치로 스레드 엔티티의 owner와 title을 해석한다.
- `JournalDayResolvedGuard.assertWritableForRef`는 일자 완결 축 밖인 `JOURNAL_THREAD`를 잠금 없이 통과시킨다.
- 스레드 상세는 연관 스레드 추가·검색 피커, 연관 목록, 행별 뷰 합성 토글을 제공한다.

## 6. 현재 경계

- **허브-스포크 ≠ 클러스터.** 1-hop 대칭이라 A–B, A–C 를 걸면 A 에서만 B·C 가 보이고 B 에서는 C 가 보이지 않는다. UI 문구·기대치를 "그 스레드 기준의 방사형"으로 못박는다. 전이적 「그룹」은 지원하지 않는다.
- **relationType 시맨틱 공백.** 재사용 대가로 스레드 맥락에서 안 쓰는 필드를 이고 간다 — UI 에서 유형 선택을 숨겨 무의미한 선택지 노출을 막는다.
- **분량·본문 희석.** 연관 스레드가 크면 합성 타임라인이 길어지고 base 서사가 묻힌다. 빌려온 엔트리는 출처 칩으로 base와 구분한다.
- **같은 날 교차 정렬.** 서로 다른 스레드의 같은 날 엔트리 상대 순서는 다소 임의적이다(각 스레드 내부 축 기준). 수용.
