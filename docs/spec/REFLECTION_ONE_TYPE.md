# Reflection 단일 타입 (optional target)

> 상태: 현재 계약  
> 관련 정본: `docs/migration/journal/reflection-absorption.md` §1  
> DESIGN_NOTES: journal-reflection 절

## 1. 질문

독립 Reflection(챕터 직속, `refId` 없음)과 다른 일기·꿈·노트를 target으로 둔 Reflection을 **한 도메인 타입**으로 두는 것이 맞는가.  
예외(완결 축 밖, 임베드+1급, 태그 일기 축 합산, 스레드 소속 제약 등)가 늘수록 **원래 다른 두 모델**을 묶은 것은 아닌가.

## 2. 결론

**한 도메인 모델이 맞다.** `JOURNAL_REFLECTION` 하나이며, target(`refId`/`refContentType`)는 **0..1 optional association**이다.  
「물고 있다 / 없다」는 타입 분기가 아니라 **관계 상태**다.

absorption 정본과 같다.

- Reflection은 본질로 타입이 정해지는 Entry다(chapter가 타입을 지시하지 않음).
- target은 표시·교차뷰 위치만 정하며 belongsTo(소속)가 아니다.
- target 삭제 시 Reflection은 nullify로 **독립화**된다(cascade 삭제 없음) → 물린 상태와 독립 상태는 같은 존재의 두 모습이다.

## 3. 예외를 어떻게 읽는가

예외는 Reflection을 둘로 쪼개야 한다는 신호가 아니라, **인접 aggregate·축에 대한 정책**이다.

| 관찰 | 축 / 정책 |
|------|-----------|
| 일자 완결 가드 밖 | day resolved 축 |
| 1급 행 + target 밑 슬림 임베드 | 표시 표면(작성 지층 vs 대상 지층) |
| 태그를 일기 클라우드·결산 DIARY에 합산·일기 검색 축 | 집계/검색 축 (`JournalEntryTagAxis`, §5.4) |
| 일기·꿈·노트를 문 Reflection은 스레드 추가 불가 | 스레드 소속 정책(아래 §4) |

타입을 Attached/Independent로 나누면 등록 API·캐시·검색 facet·임베드 enrich·target nullify·챕터 배치가 이중화되고, 흡수한 `journal_interpretation` 분리가 다시 열린다.


### 3.1 Reflection↔Reflection 위계 (현재 계약)

**개념**: 연속된 Reflection은 챕터 아래 **같은 레벨**로 둔다 — 독립 Reflection을 연달아 두거나, 같은 primary(일기·꿈·노트)에 **형제**로 첨부한다. Reflection 사이의 부모–자식 위계·종속은 제품 기본 경로가 아니다. 시간에 따른 같은 층위의 사유는 `sortOrder`·작성 순으로 읽는다.

**UI**: Reflection→Reflection 중첩 등록 메뉴를 숨긴다(`JournalReflectionItem`, 1급 `JournalEntryItem`의 Reflection 행). 권장 등록은 챕터 「리플렉션 등록」(독립)과 primary 엔트리 ⋯「리플렉션 등록」(target=일기/꿈/노트)뿐이다.

**서버·스키마**: `refId`/`refContentType`에 Reflection을 두는 것은 구조적으로 가능하고 기존 행은 유지한다. 신규 워크플로로 권장하지 않으며, API를 막아 두지 않는다(레거시·수동 호출 여지).
## 4. 스레드 소속 정책 (현재 계약)

스레드는 여러 **1급 순간/사유**를 관통하는 상위 서사다.

| Reflection 상태 | 스레드 소속 추가 |
|-----------------|------------------|
| 독립 (`refId` null) | 허용 |
| target = 일기 / 꿈 / 노트 (`JOURNAL_DIARY` · `JOURNAL_DREAM` · `JOURNAL_NOTE`) | **금지** |
| target = Reflection (`JOURNAL_REFLECTION`) | 허용(레거시). 신규 UI 없음(§3.1) |

이유: 원문(일기·꿈·노트)의 교차뷰 해석을 스레드에 또 올리면 원문과 해석이 이중 소속되기 쉽다. 독립 사유는 그 자체로 1급 순간에 가깝다.

구현:

- UI: `JournalEntryItem` / `JournalReflectionItem`에서 해당 조건이면 「스레드에 추가」 숨김
- API: `JournalThreadEntryService.regist`에서 동일 조건이면 거절
- 기존에 잘못 소속된 행의 일괄 정리는 이 계약의 범위 밖이다(신규 추가·복원만 차단)

## 5. 라이프사이클·상태 (현재 계약)

`JOURNAL_REFLECTION` 도 일기와 같이 `OPEN`/`PENDING`/`RESOLVED` 라이프사이클과 `COLLAPSED`/`IMPRTC`/`REFRNC` 상태를 허용한다.

- 서버: `AttachableContentLifecyclePolicy` · `AttachableContentStatePolicy` · `JournalContentOwnershipGuard`(영속 `contentType`으로 소유권 확정)
- 1급 행(`JournalEntryItem`): 완료 시 자동 접힘 + ⋯ 메뉴 수동 접기·중요·참조
- 임베드(`JournalReflectionItem`): lifecycle·중요·참조는 저장 가능. 본문은 항상 표시하며 접기 메뉴는 두지 않는다

### 5.1 primary(일기·꿈·노트) ↔ 딸린 Reflection 연쇄

저장은 독립이다. 완료·재개만 부모 주도로 맞춘다. 구현: `JournalReflectionLifecycleCascade`.

| 규칙 | 동작 |
|------|------|
| primary → `RESOLVED` | 그 엔트리를 target으로 둔 Reflection도 `RESOLVED` |
| Reflection → `RESOLVED` | primary는 바꾸지 않음 |
| primary는 `OPEN`·Reflection만 `RESOLVED`/`PENDING` | 허용 |
| `RESOLVED` primary에 Reflection **신규** 묶기 | primary → `OPEN`, 파생 `COLLAPSED`가 있으면 해제 |
| 독립 Reflection · (레거시) Reflection→Reflection | 위 연쇄 대상 아님 |
| 일자 축 잠금(`diaryResolvedYn`/`dreamResolvedYn`) | Reflection 등록은 허용. primary OPEN 재개는 잠금이면 생략 |

일자 완결 플래그와 엔트리 lifecycle 연쇄는 별축이다.

### 5.2 태그 (독립만)

| Reflection | 태그 |
|------------|------|
| 독립 (`refId` null) | 허용 — 등록 모달 입력, 일기 축(`JournalEntryTagAxis`) 집계 |
| 딸린 (target 있음: 일기·꿈·노트·Reflection→Reflection) | 없음 — 모달 입력란 숨김, 서버가 태그 payload 를 비움 |

타입을 둘로 나누지 않고 **target 유무(관계 상태)** 로 태그 UI·영속만 가른다. target이 Reflection인 레거시 행도 딸린 쪽으로 태그를 두지 않는다(§3.1). 읽기 DTO(`JournalEntryDto`)는 `refId`/`refContentType`을 실어 수정 모달이 상세 조회로도 딸린/독립을 판정하고, 임베드·1급 행 수정 오픈은 목록의 target을 payload에 함께 넘긴다.
### 5.3 독립 Reflection 챕터 소속·이동

| | 계약 |
|---|---|
| 소속 가능 챕터 | 같은 일자의 **DIARY·NOTE** |
| DREAM | 독립 Reflection 소속·이동·챕터 헤더 등록 대상 아님 |
| 딸린 Reflection | 챕터 이동 없음 (target 교차뷰; 소속 챕터는 등록 시 target 쪽 기본값) |
| UI | 독립 등록/수정 모달에 DIARY·NOTE 챕터 select |

독립 Reflection은 일기·노트와 **같은 레벨의 1급 행**이므로 장(챕터)만 다시 붙일 수 있다. `contentType`은 계속 `JOURNAL_REFLECTION`이다.

### 5.4 일기 검색·태그 팝업 (`type=DIARY`)

| | 계약 |
|---|---|
| 결과 행 | `JOURNAL_DIARY` **또는** 독립 Reflection(`JOURNAL_REFLECTION` · `refId` null) |
| 딸린 Reflection | 결과 행 아님. 키워드가 딸린 본문에 있으면 **타깃 원문**이 매칭(`EXISTS`) |
| 태그/`states` 스코프 | `JournalEntryTagAxis` — `JOURNAL_DIARY` ∪ `JOURNAL_REFLECTION` |
| UI | 검색 팝업·태그 클릭은 계속 `type=DIARY` (프론트 축 확장 없음) |

## 6. 하지 말 것

- `JOURNAL_REFLECTION`을 Attached/Independent contentType으로 분할
- 딸린 Reflection 에 태그 입력·영속을 다시 열기
- 독립 Reflection을 DREAM 챕터에 두거나 DREAM으로 이동시키기
- Reflection→Reflection 중첩 등록 UI를 제품 기본 경로로 복구(§3.1)
- Reflection 전부에 대한 스레드 소속 전면 금지(독립까지 막지 않음)
