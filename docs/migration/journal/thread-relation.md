# 스레드 연관(thread-relation) 설계

> **상태: 설계 확정 · 백엔드/프론트엔드 전체 구현 완료.** 이 문서가 도메인 규칙·결정 로그 정본이다.
>
> **Sub-A 완료 (백엔드 연관 CRUD):** `RelatedContentService.SUPPORTED_TYPES` JOURNAL_THREAD 추가, `resolveCreatedBy`/`resolveTitle` contentType별 디스패치 일반화, `JournalDayResolvedGuard.assertWritableForRef` JOURNAL_THREAD 잠금 없이 통과. `GET/POST/DELETE /api/related/JOURNAL_THREAD/{id}` API 동작.
>
> **Sub-B 완료 (백엔드 뷰 합성):** `JournalEntryDto.sourceThreadId` provenance 필드, `JournalThreadEntryService.getEntriesByThread(threadId, relatedThreadIds)` — 요청된 1-hop 연관 스레드만 합성·중복 제거·provenance 세팅. `GET /api/journal/threads/{id}/entries?relatedThreadIds=` 동작.
>
> **프론트엔드 완료:** `JournalThreadPickerModal.vue`(스레드 검색·선택 피커), 연관 스레드 목록·행별 합성 토글(`relatedThreadIds`), 빌려온 엔트리 출처 스레드 배지 및 멤버십 제거 게이팅(`sourceThreadId` 시 빼기 숨김).

## 1. 의도 (그림)

서로 관련된 스레드를 **연관**으로 묶고, 한 스레드 상세에서 **토글** 하나로 연관 스레드의 엔트리까지 **같은 시간축에 겹쳐** 본다.

- **행 OFF**: 그 연관 스레드는 시간축에 올리지 않는다.
- **행 ON**: 해당 연관 스레드의 엔트리를 base와 한 시간축에 날짜순으로 섞어 본다. 켜진 행이 없으면 base 멤버만 본다.

핵심: **멤버십 병합이 아니라 뷰 시점의 합집합**이다. 연관 스레드는 자기 멤버십·데이터를 그대로 두고, base 화면에서만 읽기 오버레이로 끌어온다. 토글을 끄면 즉시 원복된다.

## 2. 확정된 설계 결정

1. **연관 범위 = 1-hop 대칭.** A 기준이면 A 에 직접 연관된 것만 본다. 전이(A–B–C 에서 A 가 C 까지)는 없다.
2. **연관 유형 없음.** 단일 기본 relationType 으로 고정하고 스레드 UI 에서 유형 선택을 숨긴다.
3. **토글 상태 = 화면 임시·행 단위.** 연관 스레드마다 합성 on/off. 기본 OFF, 저장하지 않는다(뷰 옵션 성격).
4. **출처 배지.** 빌려온(연관) 엔트리에 어느 스레드에서 왔는지 배지를 단다.
5. **중복 제거.** base·연관 양쪽에 속한 엔트리는 한 번만, base 소속으로 표시한다.
6. **멤버십 제거 게이팅.** 빌려온 엔트리는 「스레드에서 빼기」를 숨긴다(안 속한 스레드에서 뺄 수 없음; 자기 것만 뺀다). 그 외 엔트리 액션(수정·복사·해석·라이프사이클)은 유지 — 막는 건 멤버십 제거 하나뿐.

## 3. 연관 모델

- thread ↔ thread, 1-hop 대칭. related_content 를 JOURNAL_THREAD 까지 일반화해 재사용한다. **스키마 변경 없음** — related_content 는 이미 (id, contentType) 제네릭 쌍(left/right)이고 JOURNAL_THREAD ContentType 도 존재한다.
- 대칭이라 정규화된 쌍(작은 키를 left)으로 1행 저장. 자기 자신 연관 금지, 동일 소유자만(기존 related-content 계약 그대로).

## 4. 뷰 합성 + provenance

- 스레드 상세 조회에 `relatedThreadIds` 옵션을 둔다. 전달된 ID(실제 1-hop 연관만)의 멤버 엔트리를 base와 합쳐(중복 제거) 기존 정렬축(일자 → 원본 엔트리 sort_order → ID)으로 내려준다.
- 응답이 각 엔트리에 **provenance(출처 스레드 / base 멤버 여부)** 를 실어, 프론트가 하나의 데이터로 둘을 처리한다:
  - 빌려온 엔트리 → **출처 스레드 배지**.
  - 빌려온 엔트리 → **「스레드에서 빼기」 숨김**.
  - base·연관 동시 소속 → dedup 후 **base 멤버로 취급**(배지 없음, 「빼기」 노출).

## 5. related_content 일반화 (구현 시 변경점)

- RelatedContentService.SUPPORTED_TYPES 에 JOURNAL_THREAD 추가.
- 소유자·제목 resolver(resolveCreatedBy/resolveTitle)를 **contentType 별 디스패치**로 일반화 — 스레드는 thread 엔티티에서 owner/title 을 해석한다(현재는 journalEntryService 위임이라 엔트리 전용).
- JournalDayResolvedGuard.assertWritableForRef 가 JOURNAL_THREAD 를 **잠금 없이 통과**한다(스레드는 일자 완결 축 밖).
- 프론트: 스레드 상세에 연관 스레드 추가·검색(스레드 피커), 연관 목록, 행별 뷰 합성 토글.

## 6. 유의점 (설계 자체 평가에서)

- **허브-스포크 ≠ 클러스터.** 1-hop 대칭이라 A–B, A–C 를 걸면 A 에서만 B·C 가 보이고 B 에서는 C 가 보이지 않는다. UI 문구·기대치를 "그 스레드 기준의 방사형"으로 못박는다. 전이적 「그룹」은 별도 확장으로 남긴다.
- **relationType 시맨틱 공백.** 재사용 대가로 스레드 맥락에서 안 쓰는 필드를 이고 간다 — UI 에서 유형 선택을 숨겨 무의미한 선택지 노출을 막는다.
- **분량·본문 희석.** 연관 스레드가 크면 합성 타임라인이 길어지고 base 서사가 묻힌다. 필요 시 빌려온 엔트리 톤다운(옅게)을 옵션으로 검토 — 현재는 배지로 충분.
- **같은 날 교차 정렬.** 서로 다른 스레드의 같은 날 엔트리 상대 순서는 다소 임의적이다(각 스레드 내부 축 기준). 수용.