# journal_todo 존재론 (아이디어)

> 상태: **아이디어 (계약 외부)** — journal_todo 의 관리 단위·완료 표현·이력을 어디에 두는지에 대한 존재론 확정본이다. 아직 구현·spec 변경을 수반하지 않는다. 현재 동작 계약은 코드(`feature/journal/todo/**`, `feature/attachable/lifecycle/**`)를 따른다.

## 배경 — 현재 구현에서 이미 성립한 것

`journal_todo` 는 이미 존재한다.

- 백엔드: `feature/journal/todo/**` 에 풀 CRUD (`JournalTodoEntity` = `journal_todo` 테이블, `JournalTodoRestController`, service/my-service(캐시), mapstruct, spec). `JournalTodoEntity` 는 `BaseAttachableEntity` 상속 + `TagEmbedModule`.
- 프론트: `JournalAsideTodoCard.vue` 가 저널 day 뷰 aside 에 마운트되어 현재 년/월(`store.yy`, `store.mnth`) 기준으로 조회·표시하고, 등록/수정은 팝업 `JournalTodoRegistModal.vue`, 삭제는 카드 인라인 버튼으로 처리한다.
- 완료 표현: 현재는 **완료 = 삭제**(trash 버튼).

즉 "dreamdiary 는 조인해서 보여주기만 하고 관리는 팝업으로"라는 패턴은 이미 서 있다. 이 노트는 그 위에서 **관리 단위를 시간(월)에서 lifecycle 로 옮기는 존재론 재정의**를 다룬다.

## 핵심 질문과 답

> 9월 28일에 만든 OPEN todo 가 10월 1일이 되었을 때 무엇이 되어야 하는가?

- 그대로 사라진다 → `yy/mnth` 가 강한 관리 단위(소유).
- 10월에도 자연스럽게 보인다 → lifecycle 이 관리 단위.
- 10월에 보이되 "9월 발생" 흔적을 가진다 → 시간은 관리 단위가 아니라 **발생 맥락/기원 축**.

**확정: 세 번째.** 안 끝난 todo 는 다음 달에도 계속 보인다(지속·누적을 원한다). 따라서 todo 는 "특정 월에 속하는 기록"이 아니라 **시간축 위에서 발생하고 lifecycle 을 따라 흐르는 객체**다. 이 방향은 lifecycle 을 개별 도메인이 아닌 attachable 공통축으로 분리한 기존 설계 철학과 같은 방향이다.

## 네 개의 축 분리

todo 를 하나의 덩어리로 보지 않고 축을 분리한다. `yy/mnth` 가 현재 구현에 있다는 이유만으로 관리 단위로 받아들이지 않는다(lifecycle 공통화 때와 같은 규율).

| 축 | 확정 계약 |
|---|---|
| **존재 단위** | `journal_todo` 하나. 월에 종속되지 않고 독립적으로 존재하며 지속·누적된다. |
| **상태 축** | `lifecycle` (`OPEN` / `PENDING` / `RESOLVED`). todo 자체 컬럼이 아니라 attachable 공통 관심사. 현재값 하나만 저장(`OPEN` = row 부재). **완료 = `RESOLVED` 전이**이며, delete-as-done 은 폐기한다. |
| **이력 축** | `history` (lifecycle 전이 로그). attachable 공통, append-only, 키는 lifecycle 과 동일하게 `ref_content_type + ref_id`. `lifecycle : history = state : event` 쌍. 회고 충실성(as-of 재구성) 전용. |
| **시간 축** | `yy/mnth` = **불변 발생지(provenance)**. "이 todo 가 속한 달"(소유)도 "배치 위치"도 아니라 "이 todo 가 발생한 저널 맥락"이다. 제거하지 않는다 — 저널 타임라인 노출과 "이 무렵 무엇을 하려 했나" 질의의 앵커 값이 있다. 이동 가능한 버킷이 아니라 불변 origin 마커로 계약을 못박는다. |

`OPEN = row 부재`의 의미는 lifecycle 참여 여부까지 row 존재성으로 표현한다는 뜻이 아니며, 참여 여부는 `AttachableContentLifecyclePolicy`가 결정한다. 참여 대상으로 확정된 content에 한해서 row 부재를 OPEN으로 해석한다.

## 투영(projection) 술어

월 화면은 todo 를 소유하지 않는다. **현재 활성 집합을 투영해서 비출 뿐**이다. 데이터를 실제로 다음 달로 옮기지 않으므로 별도 carry-over 기능이 필요 없다 — OPEN/PENDING todo 는 그냥 아직 존재하는 것이고, 월이 바뀌면 화면이 그것을 계속 비춘다.

- **활성 집합 = `OPEN` + `PENDING`.** 둘 다 미해결이므로 활성에 포함한다. `RESOLVED` 만 활성 투영에서 빠진다.
- **`PENDING`(보류)** 은 아직 해결하지 않았지만 현재 적극적으로 수행하지 않는 상태다. 활성 집합에는 포함하되, 기본 활성 목록에서 접힘/하단으로 두는 것은 투영 정책이다.
- **기본 투영 = 현재값 전방 투영.** 과거 상태를 보지 않는다.
- **회고 투영(as-of)** = history 로 재구성. "작년 9월 시점에 열려 있던 todo 를 그때 상태로" 보는 것은 이 축에서만 가능하다.

## 정렬

`sortOrder` 는 월의 속성일 수 없다(월이 todo 를 소유하지 않으므로). 현재 별도 grouping/collection 존재론이 없으므로 `sortOrder`는 활성 todo 집합 전역의 우선순위로 정의한다(origin 을 가로지르는 cross-month 순서).

## lifecycle ↔ history 정합 불변식

페어를 두면 "현재값"의 진리 원천이 둘이 되므로 수렴 원칙상 불변식을 못박는다:

> 모든 lifecycle 전이는 정확히 history 이벤트 1건을 방출하고, `lifecycle` 현재값 == history 마지막 이벤트의 `to_state` 이다.

`OPEN = row 부재`이므로 `RESOLVED` → `OPEN` 전이 후 lifecycle 현재 테이블은 row 가 없고, history 마지막 이벤트의 `to_state`는 OPEN이다. 최초 생성된 OPEN과 RESOLVED였다가 다시 OPEN으로 돌아온 상태는 현재값 테이블만 보면 동일하다. 현재 상태 투영은 둘을 구분하지 않으며, history가 붙는 순간에만 경로가 갈린다. 현재 존재 상태를 위해 과거를 저장하지 않으며, 과거를 묻기 시작할 때 history가 그 차이를 보존한다.

구현 선택(아래 둘 중 하나)은 일관성 보증이 갈리는 지점이라 존재론 확정본에서는 불변식만 고정하고 선택은 구현 시점으로 미룬다.

- (a) history 를 SSOT 로 두고 lifecycle 을 head 캐시로 파생.
- (b) lifecycle 을 현재값 authoritative 로 두고 같은 트랜잭션에서 history 를 append.

## 구현 순서(스코프 규율)

> **진행 상태(2026-09-08):** core 구현 완료 — 정책 참여·enrich(SP-T1, `690c600c8`), cross-month 활성 집합 수렴·리스트 캐시(`journalTodoListByUser`) 제거·전역 sortOrder(SP-T2a, `f5b5224aa`), 완료=`RESOLVED` 전이 카드 UI·보류/삭제(SP-T2b, `09d7a0159`). **history 페어는 의식적으로 보류** — 아키텍처적으로 옳은 쌍이나 as-of 재구성을 쓸 구체 화면 요구가 아직 없어, 사용 압력이 생길 때 별도 설계 라운드로 착수한다(아래 미결 a/b·history 스코프는 그때 확정). 빌드 검증은 사용자 머신 대기(gradle loopback 차단).

확정된 요구 — "안 끝난 건 다음 달에도 계속 보인다" — 는 현재 OPEN/PENDING 집합의 전방 투영이며 과거 상태를 보지 않는다. 따라서:

- **core**: `AttachableContentLifecyclePolicy` 에 `JOURNAL_TODO` 참여 추가, `JournalTodoDto` 가 `LifecycleCmpstnModule` 구현, 투영 = 현재 활성 집합. **history 불필요.**
- **history 페어**: 회고 충실성을 여는 다음 레이어. 아키텍처적으로 옳은 쌍이지만 core 를 여기에 묶으면 SAVEPOINT 가 불필요하게 커지므로 순서상 뒤에 둔다.

## 미결(구현 진입 전 결정 대상)

- 투영·삭제 정책: `RESOLVED` todo 의 완전 삭제를 허용할지, 아니면 lifecycle 로만 관리하고 물리 삭제는 예외로 둘지.
- history 정합 불변식의 구현 선택 (a)/(b).
- history 축의 스코프: lifecycle 전이 전용으로 좁힐지, 일반 audit 으로 넓힐지(넓히면 계약 외부 과설계 위험).

## 관련

- 상위 존재론: [일정·TODO 존재론](schedule-todo-ontology.md) — 완료 축(이 문서)과 시간 축(일정)을 facet 으로 묶는 수렴 방향. 이 문서는 그 완료 축의 SSOT다.
- 동형 문제: [스레드 상위 조직화](thread-organization.md) — 관리 단위(Arc/Collection/Tag) 문제와 같은 계열이다. "불변 발생지 + lifecycle 흐름 + 시점 투영" 템플릿이 확정되면 thread 에도 재사용 가능하다.
- 현재 lifecycle 계약: `feature/attachable/lifecycle/**` (`LifecycleKey`, `LifecycleEntity`, `AttachableContentLifecyclePolicy`).
