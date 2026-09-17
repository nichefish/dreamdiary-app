# 일정·TODO 존재론 (아이디어)

> 상태: **아이디어 (계약 외부)** — `schedule`(일정)과 `journal_todo`(TODO)가 각자 독립 도메인으로 존재하는 현 상태 위에서, 둘의 관계와 그 사이에 낀 "시간에 걸린 의무"(예: 반복 기념일에 해야 할 일)를 어떻게 수렴시킬지에 대한 존재론 정리다. 아직 구현·spec 변경을 수반하지 않는다. 현재 동작 계약은 코드(`feature/calendar/schedule/**`, `feature/journal/todo/**`)를 따른다.


> 갱신(2026-09-08): 본문 존재론(직교 facet 재구성)은 유지한다. 다만 이후 탐색에서 본문 일부(2×2의 `00=공집합`, 선형 lifecycle, "완료되면 다음이 뜬다")를 정밀화하는 발견이 나왔다. 문서 하단 「탐색 SAVEPOINT — 검증 상태」를 함께 볼 것 — 그 내용은 정본으로 승격하지 않은 검증 대기 상태다.

## 배경 — 현재 두 도메인은 따로 서 있다

- **일정(`schedule`)**: `ScheduleEntity`(`schedule` 테이블) = `title`/`content` + `bgnDt`/`endDt`(시작~종료 시간 span) + `scheduleCd`(분류; 휴가 `VCATN` 등) + `privateYn` + 참여자(`SchedulePrtcpntEntity`). `BaseAttachableEntity` 상속 + `TagEmbedModule`/`CommentEmbedModule`. 공휴일은 외부 소스(KASI)에서 적재한다. **완료 상태를 갖지 않고, 반복 규칙도 없다.** 시간 위에 놓이는 span 앵커가 본질이다.
- **TODO(`journal_todo`)**: `JournalTodoEntity` = `BaseAttachableEntity` + `TagEmbedModule`, `yy/mnth`(발생지), lifecycle(`OPEN`/`PENDING`/`RESOLVED`; [journal-todo-evolution](journal-todo-evolution.md) 참조). **시간 앵커(마감·발생 시각)를 갖지 않는다** — `yy/mnth`는 마감이 아니라 "이 무렵 발생했다"는 불변 origin이다.

즉 두 저장소가 이미 분리돼 있고, "엄마 생신마다 전화한다" 같은 항목은 **어느 쪽에도 온전히 담기지 않는다**. 이것이 이중관리 불안의 구체적 실체다.

## 핵심 재구성 — "두 종류"가 아니라 "두 축(facet)"

일정과 TODO를 서로 배타적인 종류로 두면 생신 전화 같은 항목은 분류가 불가능하다(둘 다이므로). 둘을 **직교하는 두 축**으로 두면 문제가 녹는다.

- **시간 축(일정성)** — 이 항목이 *언제(WHEN)* 타임라인 위를 점유하는가?
- **완료 축(할일성)** — 이 항목이 *닫혔는가(WHETHER)* 하는 완료 상태를 갖는가?

두 축은 독립적으로 있거나 없다. 하나의 항목은 두 축의 조합으로 표현된다.

| | 완료축 없음 | 완료축 있음 |
|---|---|---|
| **시간축 없음** | (공집합) | **순수 TODO** — "이력서 업데이트" |
| **시간축 있음** | **순수 일정** — 공휴일, 남이 잡은 회의, 생신 그 자체(관망) | **시간에 걸린 의무** — "생신에 전화" |

현재 `ScheduleEntity`는 좌하단(시간축만), `JournalTodoEntity`는 우상단(완료축만)에 해당한다. **우하단(시간축+완료축)이 지금 비어 있는 칸이다.**

## 왜 서로 환원되지 않는가 (비대칭)

- **TODO → 일정 환원 불가**: 완료축만 있는 항목(이력서 업데이트)은 WHEN이 없다. 억지 마감을 박으면 가짜 시간이 생기고, 그 시각이 지나면 의무가 소멸한 것처럼 오도된다.
- **일정 → TODO 환원 불가**: 시간축만 있는 항목(공휴일, 관망 대상 회의)은 WHETHER가 없다. 완료 체크박스를 붙이면 영원히 닫히지 않는 유령 할일이 쌓인다.

두 축이 진짜로 독립적이라 어느 한쪽으로 접으면 정보가 파괴된다. 따라서 어느 하나를 다른 하나로 흡수하지 않는다.

## 존재론의 바닥 — 두 축의 성질이 다르다

- **시간 축은 세계의 시간에 대한 사실**이다. 외부적이고, 내 행동과 무관하게 *지나간다(occurrence)*. 3시 회의는 내 준비 여부와 무관하게 3시에 있다.
- **완료 축은 나의 의지에 대한 사실**이다. 내부적이고, 내가 닫기 전까지 *열려 있다(resolution)*. 내가 붙잡지 않으면 존재 자체가 없다.

"생신 전화"가 처음 TODO로 느껴지는 이유가 여기 있다. 살아있는 감각의 핵심은 "잊지 말고 해야 한다"(내부 의지)인데, 그 *방아쇠*는 생신(외부 시간)이다. **외부 시간이 방아쇠를 당기는 내부 의무** — 그것이 우하단 칸의 정체다.

## 수렴의 기질 — 이미 `BaseAttachableEntity`가 공통축이다

`ScheduleEntity`와 `JournalTodoEntity`는 둘 다 `BaseAttachableEntity`를 상속하고 tag/comment 를 임베드로 붙인다. lifecycle 은 개별 도메인 컬럼이 아니라 attachable 공통 관심사(`AttachableContentLifecyclePolicy`)로 뽑히는 방향이다([journal-todo-evolution](journal-todo-evolution.md)).

이 구조가 facet 존재론의 자연스러운 착지점을 준다:

> **완료 축(lifecycle)이 attachable 공통축이듯, 시간 축(temporal anchor)도 attachable 공통축 후보다.**

이 방향에서 세 칸은 "다른 종류의 엔티티"가 아니라 **attachable content에 어떤 축이 켜졌는가**로 표현된다.

- 순수 일정 = 시간축 참여, 완료축 미참여
- 순수 TODO = 완료축 참여, 시간축 미참여
- 시간에 걸린 의무 = 시간축 + 완료축 동시 참여

이중관리를 피하는 답은 "정밀하게 나눠 두 벌 관리"가 아니라 **단일 항목에 두 축을 옵션으로 달고, 화면은 필터로 투영**하는 것이다. 캘린더 뷰 = 시간축 참여분의 투영, 할일 뷰 = 미해결 완료축의 투영. 시간에 걸린 의무는 양쪽 뷰에 동시에 뜨지만 record 는 하나 — 이중관리가 아니라 **단일 원천의 두 투영**이다.

이는 확정된 계약이 아니라 수렴 방향 가설이다. `ScheduleEntity`가 이미 span 앵커를 자체 컬럼(`bgnDt`/`endDt`)으로 갖고 있으므로, "시간 축을 공통 모듈로 추출"은 기존 스키마를 그 축으로 재배치하는 작업이 된다.

## 가장 어려운 지점 — 반복 × 완료

"생신*마다*"는 반복이고, 완료는 반복 규칙이 아니라 **각 발생(occurrence)에 붙는다**. 현재 두 엔티티 모두 반복을 모델링하지 않는다.

- **규칙(rule)**: "매년 생신에 전화" — 반복 시간 앵커 + 완료 템플릿.
- **발생(occurrence)**: "2025 생신 전화(완료)", "2026 생신 전화(대기)" — 각자 완료 상태를 따로 가진다.

무한한 발생을 미리 실체화할 수 없으므로 **현재/다음 발생만 lazy하게 실체화**하고, 완료되면 다음이 뜨는 패턴이 자연스럽다. "규칙은 하나, 완료는 발생마다"가 반복 의무의 핵심 구조다. 규칙과 발생 중 무엇을 SSOT로 두는지, 발생을 언제 실체화하는지는 구현 진입 시점의 결정 대상이다.

## 이 앱만의 축 — 미래(전망) vs 과거(회고)

이곳은 저널(회고) 앱이다. 저널은 과거를 향하고, 일정/TODO는 미래를 향한다. 그러면 생애주기가 보인다.

> 의도(open) → 발생 시점 도래 → 실행/완료 → 저널 기록(과거)

완료된 미래 의무는 과거 기록이 될 수 있다. "2025 생신 전화 완료"가 그날 저널 엔트리로 흡수되는가, 링크되는가, 무관한가 — 이는 이 앱 고유의 열린 질문이며, 잘 풀면 일정·TODO·저널이 하나의 시간축 위에서 수렴한다.

## 미결 (구현 진입 전 결정 대상)

- **시간 축의 위치**: 시간 앵커를 attachable 공통 모듈로 추출할지, 아니면 도메인별 컬럼(`schedule.bgnDt`/`endDt`)으로 유지할지. 추출 시 `journal_todo`도 시간 앵커를 옵션으로 가질 수 있게 된다.
- **`schedule`와 `journal_todo`의 통합 범위**: 두 도메인을 하나의 attachable 항목으로 수렴시킬지, 아니면 별개 도메인을 유지하되 "시간에 걸린 의무"만 한쪽에 담고 다른 뷰에 투영할지.
- **반복 발생의 SSOT·실체화 시점**: 규칙만 저장하고 발생을 계산할지, 다음 발생만 실체화할지.
- **완료된 미래 의무 ↔ 저널 엔트리** 관계: 흡수 / 링크 / 무관.
- **관망성 순수 일정(공휴일, 남의 회의)의 취급 범위**: 이 앱이 완료축 없는 시간 항목까지 일급으로 다룰지(현재 `schedule`은 이미 다룸), "내 의무가 걸린 시간"으로 스코프를 좁힐지.

## 탐색 SAVEPOINT — 검증 상태 (2026-09-08)

> 아래는 본문 존재론을 더 밀어붙인 탐색 결과다. **정본(계약 외부 아이디어)으로 승격하지 않고 검증 대기로 남긴다.** 본문 일부 서술은 아래 발견으로 정밀화 대상이다.

### 살아남은 것 (강함)

- **temporal facet × resolution facet의 직교 재구성.** schedule/TODO 이중관리 불안의 원인을 설명한다. 이 축은 유지한다.

### 현재 가장 강한 상위 가설 (미검증)

- **entity ontology와 view ontology의 분리.** domain primitive(`Schedule`, `JournalTodo`, `Thread`, `Entry`)는 각자의 identity·생성 provenance를 가진 채 heterogeneous하게 존속한다. `Calendar`/`Todo`는 primitive가 아니라 그 population 위에 걸리는 **의미론적 projection**(UI는 그걸 렌더할 뿐, UI 자체가 아니다)이다.
  - `Calendar = { x | temporal(x) }`, `Todo = { x | resolution(x) ∧ unresolved(x) }`.
  - 따라서 `Calendar = schedules`, `Todo = journal_todos`라는 현재의 암묵적 동일시가 깨진다.
  - **판별자: entity는 자기 identity + 생성 provenance로 개별화되고, view는 아무것도 소유하지 않는(질의일 뿐인) 것으로 개별화된다.** "필드 응집"보다 이 provenance 판별자가 날카롭다.
  - 확신도: primitive 존속 ~85–90%. 순수 dissolution(모든 게 facet, `ContentEntity` 만능 primitive)은 이 저장소가 `BaseAttachableEntity`(구체 서브타입) 노선으로 피해온 방향이라 낮다.

### 검증 대기 발견사항 (본문 정밀화용, 정본 아님)

1. `00 = 공집합` → "**schedule/TODO 투영 비참여**". attachable 전체에선 timeless content(메모·자료·생각·reference)가 산다.
2. temporal은 **앵커 모양(span/point)** 과 **축 결합(deadline = resolve-before-T / trigger = T-occurrence가 obligation을 낳음)** 의 두 층이다. 우하단을 `{temporal, resolution}=true`로만 쓰면 축의 *공존*은 표현하되 축 *사이의 의미*를 잃는다.
3. 반복: **발생의 존재(rule이 결정, 잠재적 무한·비실체화)** 와 **materialization(lazy 정책)** 를 분리한다. rule은 a-temporal 생성기지만 각 occurrence는 시간에 강하게 걸린다 — row가 없어도 `2037-09-28`이라는 temporal identity를 가지므로, 표현은 '무시간'이 아니라 '비실체화'다(occurrence identity ≈ rule + recurrence key/time). 2025 미완료 + 2026 도래 → 동시 OPEN 가능. 2층 SSOT — 존재 SSOT = rule, 상태 SSOT = occurrence별 resolution.
4. 시간 4종 분리: origin(provenance) / target(prospective anchor) / journal timestamp(회고 content 자체 속성) / **journalization = prospective↔retrospective content relation**. journalization은 facet 아님 — many-to-many(한 저널이 여러 obligation 회고, 한 사건을 여러 날 기록)라 `journalized=true`로 접으면 정보손실.
5. 완료축은 이미 여러 content type을 횡단한다(`thread` lifecycle ✓, `journal_todo` 예정). temporal도 `schedule`이 담는다 → projection 재구성은 코드가 이미 표류 중인 방향(§2 계약 추정 증거).
6. B(temporal의 persistence 공통화)는 확률 문제가 아니라 **판정 유예**. "어떤 domain이 temporal participation을 하는가"가 먼저 결정되고, 저장 표현은 그 뒤에 따라온다.

### 우하단 topology 판별자 (kill-test 통과, 2026-09-08)

우하단(시간+완료)은 반드시 "두 facet을 가진 한 객체"가 아니다. 두 topology가 있고, 판별자는 **temporal referent가 obligation과 독립적으로 동일성을 유지하는가**다.

- **독립 identity를 가진 temporal referent가 있으면 → relation topology(#3)**: `엄마 생일 ──triggers──> 전화 obligation`, `회의 ──(resolve-before)──> 준비 obligation`. 의무를 지워도 referent가 남는다.
- **시간이 obligation/rule의 속성으로만 개별화되면 → facet topology(#1)**: 보고서 마감(내가 정했든 상사가 정했든, 의무를 지우면 그 시각은 별개 사건으로 남지 않는다), 매주 운동(rule을 지우면 화요일 19시도 사라진다).

**provenance는 원인이 아니라 증거다.** 외부가 정한 시각이라고 독립 entity가 되지 않는다(상사가 정한 마감 = 외부 authorship, 종속 identity). provenance가 독립성의 좋은 단서인 이유는 "다른 곳에서 authored된 것은 내 의무보다 먼저 존재하는 경향" 때문이지 authorship = independence여서가 아니다. 이 heuristic은 외부 agent가 *독립 사건*이 아니라 *내 의무의 파라미터*(마감 시각)를 정할 때 깨진다 — 여기서 "외부 provenance → 독립 entity"라는 잘못된 객체 증식을 막아야 한다.

**세 regime** (여기서 강도는 논리적 필연이 아니라 *증거의 강도*다): **independence-strong**(생일·회의·결혼식 — 독립 identity의 증거가 강함) / **dependence-strong**(task-local 마감, 내 반복 주기 — 종속 identity의 증거가 강함; 다른 시스템은 `Deadline`을 일급 객체로 만들 수도 있으나 이 모델에선 별도 identity를 줄 이유가 없다) / **elective**(병원 예약, 공과금 = 청구서를 일급 객체로 볼지 — 둘 다 자연스러워 제품이 경계를 고른다). elective에서 topology는 현실에서 읽어내는 것이 아니라 **의도적·일관되게 선택**한다.

**상위 원리(통합 가설):** entity/view, entity/entity, facet/relation 세 층을 가르는 것은 전부 같은 질문 — **무엇이 독립적으로 동일성을 유지하는가**(Quine, "no entity without identity"). provenance는 그 동일성의 강한 단서다. 단 이 원리는 **진단이지 판정이 아니다** — elective 지대의 선택까지 결정해주지는 않는다.

### 아직 열린 갈림

- **view의 내용은 존재론적 필연이 아니라 제품 결정이다.** `Calendar = ∀temporal` 공식이 "공휴일·엔트리 마감·thread 기한·schedule을 한 캘린더에 다 얹는다"를 *당위*로 밀어넣지 않도록 경계한다. 분리(entity≠view)는 강하지만, 각 view가 *어떤* facet을 *어떻게* 묶는지는 더 무른 층이다.

## Prospective projection — 2차 탐색 SAVEPOINT

> 상태: **아이디어 (계약 외부) · 2차 탐색 SAVEPOINT.** "머릿속 미래 고리"를 파고든 결과다. 1차(facet·entity≠view)와 달리 **앞부분(발견)만 수렴했고, 설계 후보는 반증 압력을 받아 강등**됐다. 아래 candidate 넷은 **서로를 근거로 삼지 않는다.** 절 이름도 "저장소(store)"에서 **projection**으로 강등한다 — "store"는 그 population을 소유하는 persistence 경계가 있다는 존재론을 몰래 선취하기 때문이다.

**봉인 문장:** Prospective라는 *현상*은 발견됐다. Prospective라는 *물건(entity/store)*은 아직 발견되지 않았다.

### A. 확정적으로 살아남은 것 (발견)

- **Prospective = projection/view.** predicate `x가 현재 나의 미래지향 attention을 점유한다`로 정의되는 heterogeneous population의 투영. "한 화면에 같이 뜬다 ≠ 같은 entity"(Calendar≠Schedule과 같은 교훈).
- **뷰(강함)**: 북극성 = "머리에 이고 있는 것들을 시간순으로 일목요연하게 밖으로." 이건 놀랄 만큼 명확한 *view* 정의(존재 정의가 아님). 1차 축 = **시간**(사용자 확정: 머릿속에서 "언제"로 묶임). 본체 = 시간순 롤링 아젠다(오늘/내일/곧/언젠가; "언젠가" 버킷 필연) / 보조 = 캘린더(날짜 있는 부분집합 렌즈; 균일시간·무날짜 꼬리 없음이라 본체 불가) / 배치 = 저널 daily 동반(주의가 있는 곳; **소유 아니라 배치**, now가 회고↔전망 경첩).
- **판별 규칙 6개 (이 line의 실제 자산)**: ① 시간에 걸렸나(temporal) ② resolution을 요구하나 ③ temporal referent가 독립 identity를 갖나(relation vs facet topology) ④ 반복 occurrence가 rule과 구별되는 identity를 갖나 ⑤ journalization은 속성인가 관계인가 ⑥ "같은 화면에 보인다"와 "같은 entity다"를 구별했나.
- **`journal_todo`는 자립 primitive로 확정.** TASK-성·per-user·cross-month, SP-T1/T2a/T2b로 구현·커밋됨. 아래 흡수(D)가 유예라고 해서 journal_todo가 잠정인 것은 아니다 — 나중에 특수 케이스로 *재해석될 수* 있을 뿐이다.

### B. topology에 대한 추가 발견 (최저 고도로)

미래지향 stance처럼 보이는 것도 **항상 item 내부 enum일 필요가 없다.** 독립 referent가 있으면 relation으로 뜬다:

- 독립 referent 있음(부산 비엔날레) → `considering(나, 비엔날레)` / `committed-to(나, 비엔날레)` — referent는 내 stance와 무관하게 존속.
- 독립 referent 없음(세금 신고) → obligation 자체가 primitive.

규칙 ③(referent 독립성)의 재사용이다. **여기서 "modality(REFERENCE/POSSIBILITY/TASK)라는 상위 축이 존재한다"까지 밀어 올리지 않는다** — 그 3분법은 미검증(C). 발견은 딱 "stance가 relation으로 뜰 수 있다"까지다.

### C. 반증 압력을 받은 설계 후보 (candidate A — 보존)

662529135로 구체화한 아래 스키마는 **틀린 게 아니라, 구체화됐기에 kill-test가 가능해진** 것이다. 반증을 얻었으므로 정본이 아니라 후보로 보존한다(왜 통합 엔티티를 만들지 않았는지 역사로 남긴다).

- **candidate A — `prospective_item` 단일 엔티티**: `id, owner(created_by), created_at, title, note, anchor_start?, anchor_end?, modality(REF/POSS/TASK), lifecycle(openness), spawned_from, attached_to`. 닫힘 어휘를 modality로 게이팅(TASK=완료 / POSSIBILITY=결정 / REFERENCE=보관)하려는 시도.
- **반증 압력**:
  - **prospective_item = entity: 낮음(~30%).** 판별자상 "attention 점유"는 predicate → view일 공산. entity 지위는 증거가 생길 때까지 **박탈**(죽이는 것 아님).
  - **REFERENCE/POSSIBILITY/TASK = primitive-level enum: 미검증(35~45%).** 독립 referent 케이스에선 relation이 더 자연스러움(B).
  - **단일 openness(OPEN/PENDING/RESOLVED) = persistence primitive: 낮음(30~40%).** `fulfilled / expired / rejected / archived / dropped`가 전부 attention에서 빠진다는 이유로 RESOLVED 하나가 되면, prospective view엔 편하나 **retrospective에서 "어떻게 과거가 되었는가"가 파괴된다** — 저널 앱에선 부수 metadata가 아니라 본질(축 ⑤ journalization 재료). 서로 다른 outcome을 view에서 active/closed로 **정규화**하는 것으로 보는 편이 유력(65~75%).

### D. `journal_todo` 흡수 = 판정 유예

`journal_todo → prospective_item`은 45%냐 55%냐가 아니라 **우변(prospective_item)의 존재론이 미검증**이라 확률 부여가 무의미하다(B temporal-persistence 유예와 동형). 목적지가 존재론적으로 성립한 뒤에나 판정한다.

### E. 아직 열린 primitive 탐색

- **최종 primitive 목록 미결.** Schedule / Obligation / Event / Occurrence / Thread / Entry / 어쩌면 Reference·Possibility — 예쁘게 통일 안 된 채 남을 수 있고, 그게 실패가 아니다. 좋은 존재론은 모든 걸 하나로 만드는 게 아니라 서로 다른 걸 같다고 착각 안 하게 하는 것.
- **Prospective가 하나의 projection인가 둘인가** — 시간 걸린 롤링 아젠다 vs 무날짜 someday/reference 선반이 다른 view일 수 있다(참고는 attach + 언젠가 양쪽에 뜸).
- openness가 persistence 상태기계인가 view normalization인가.

### 탐색 규율 (이 line 너머 적용)

> **Distinguish before unify.** 공통 projection·공통 UI 동작·공통 상태 어휘는 공통 entity의 증거가 아니다. 통합 후보는 **identity-independence + information-loss kill-test**를 통과하기 전엔 persistence primitive로 승격하지 않는다.

## Prospective population의 보존·관리 floor 계약 + 투영 원칙 (규범 하위 절)

> 상태: **아이디어 (계약 외부) · 목표 계약.** 자동 재등장(ceiling) 이전에 Prospective projection을 지탱하는 보존·관리 기반이 먼저 충족해야 할 최소 신뢰 계약이다. 이 기반이 단일 저장소인지, 여러 domain primitive를 연합한 것인지는 아직 결정하지 않는다. 현재 구조는 아직 이를 충족하지 않으며(아래 「현재 구조와의 충돌」), 구현·spec 변경을 수반하지 않는다. 위 Prospective projection 가설을 제한하는 규범 절이다. 여기서 store-first는 "먼저 보존·전 범위 검토·재발견을 성립시킨다"이지 "먼저 모든 것을 담는 통합 테이블을 만든다"가 아니다.

캐시 메모리 리마인더라는 북극성에서 가장 화려한 자동 재등장(page-in/prefetch)을 걷어내도, 실제로 필요한 보존·관리의 바닥이 남는지 시험한 결과 — 남는다. 그 바닥은 "하드 삭제 제거 + 상태 하나 추가"보다 넓다: **전 범위 검토, 이질적 항목 수용, 맥락 보존**까지가 floor다.

### active population과 managed corpus

floor의 대상은 현재 attention 집합이 아니라 관리 이력을 포함하는 corpus다. 새 엔티티를 만드는 구분이 아니라 둘 다 predicate로 정의되는 집합이다.

- **active Prospective population** — 현재 나의 미래지향 attention을 점유하는 항목.
- **managed corpus** — 현재 참여 항목 + 과거에 참여했다가 완료·폐기된 이력까지 포함하는 관리 범위.

보존 계약(F1·F4·F6)은 managed corpus에 건다. 관심에서 빠져나간 뒤에도 "어떻게 빠져나갔는가"를 남기는 것이 floor의 핵심이므로, 대상을 active population으로 좁히면 폐기·완료 항목이 보존 대상에서 빠져 F4와 모순된다.

### 구조 중립적인 floor 계약 (F1~F6)

> **Floor 서술 규율** — floor는 내부 저장 구조나 책임 위치를 지정하지 않고, 사용자에게 관찰되어야 할 결과와 금지되어야 할 의미 결합을 규정한다. 여러 구현 topology가 가능한 경우 특정 구조를 요구하지 말고, 방지하려는 손실·오도·연쇄 효과를 명시한다.

floor는 persistence topology와 무관한 시스템 수준의 관찰 가능한 계약이다. 단일 저장소에서는 한 persistence 경계가 이를 부담할 수 있고, 연합 구조에서는 source primitive·참여 관계·인덱스·projection 계층이 책임을 나누어 충족할 수 있다. 각 계약의 구체적 소유자는 persistence 구조가 결정될 때 정한다.

- **F1. 보존과 소멸의 분리** — 등록된 항목은 일반적인 상태 전이만으로 소멸하지 않는다. 완료·보류·폐기는 기록으로 남으며, 영구삭제는 별도의 명시적 행위로 구분한다. 금지 대상은 하드 삭제의 존재 자체가 아니라, 평범한 lifecycle 조작이 하드 삭제로 구현되는 것이다.
- **F2. 완전한 리뷰** — 현재 Prospective 관리 대상에 참여하거나 과거에 참여했던 모든 항목과 그 관리 결과를, 범위 누락 없이 검토할 수 있는 단일 진입점이 있다. 월·출처·상태는 기본 가림막이 되지 않는다. "단일 진입점에서 접근 가능"이면 충분하며(기본 화면에 폐기·완료를 전부 펼쳐놓으라는 뜻은 아니다), 한 진입점 안에서 페이지·그룹·탭으로 나뉘는 것은 위반이 아니다.
- **F3. 재발견 가능** — 검색·필터·정렬 또는 이에 준하는 탐색 수단으로 임의의 항목을 다시 찾을 수 있다.
- **F4. 폐기와 망각의 구분** — 의도적으로 하지 않기로 한 것과 관리에서 누락된 것을 구분할 수 있으며, 폐기된 항목도 기록으로 남는다.
- **F5. 거짓 완료 금지** — 완료할 수 없는 항목을 저장하기 위해 가짜 할 일로 만들거나 완료 상태를 강요하지 않는다. 완료축 유무를 이분법으로 미리 확정하지 않는다 — 종결 어휘가 종류마다 다를 수 있다는 것까지가 floor이고, 그 어휘의 실제 분화는 발견 대상이다.
- **F6. 맥락 보존** — 항목의 발생 시점·출처·저장 이유를 다시 확인할 수 있다. 내용만 남고 발생 맥락이 유실되지 않는다. 재발견(F3)이 가능해도 "내가 이걸 왜 저장했지?"가 남으면 실질적으로 쓸 수 없기 때문이다.
- **Source 연쇄 소멸 금지** — source 엔티티의 일반적인 상태 전이·삭제·정리만으로 Prospective 관리 이력이 함께 소멸해서는 안 된다. 사용자가 관리 이력까지 포함한 영구삭제를 명시한 경우는 예외다. 문제는 소유 위치가 아니라 source row 삭제와 관리 이력 소멸이 무조건 결합돼 있는가이며, 보장 책임과 저장 방식은 persistence 구조 결정 시 유예한다.

각 계약의 성격:

| 계약 | 성격 |
|---|---|
| F1 보존과 소멸 분리 | 긍정 보장 + 결합 금지 |
| F2 완전한 리뷰 | 긍정 보장 |
| F3 재발견 | 긍정 보장 |
| F4 폐기와 망각 구분 | 의미 결합 금지 |
| F5 거짓 완료 금지 | 의미 강제 금지 |
| F6 맥락 보존 | 긍정 보장 |
| Source 연쇄 소멸 금지 | 결합 금지 |

### 투영 원칙 (P1)

F1~F6과 층이 다르다 — 같은 데이터가 사용자에게 어떤 의미로 제시되는가에 관한 계약이며, Backlog·Today·사이드바·검색 결과 등 모든 투영에 적용된다. 보존·관리 기반이 floor를 전부 지켜도 UI가 "미처리 48건"·"장기 방치"로 표시하면 이 원칙을 위반하므로 층을 분리한다.

- **P1. 미결은 그 자체로 실패가 아니다** — 항목이 오래 열려 있다는 이유만으로 연체·실패·방치로 판정하거나 문책하지 않는다. 다만 명시된 기한이나 외부 사건으로 실제 위험이 발생한 경우에는 그 사실을 숨기지 않는다. 미결 항목의 수와 나이를 죄책감을 유발하는 성과 지표로 사용하지 않는다.

구분:

- 단지 오래 열려 있음 → 실패 아님
- 명시적 마감이 지남 → 기한 초과라는 사실(숨기지 않음)
- 가능성이 오래 보존됨 → 정상
- 정보가 오래됨 → 검토 또는 낡음 후보
- 항목 수가 많음 → 곧바로 부채·생산성 실패가 아님

P1은 F5(거짓 완료 금지)와 맞물린다: F5를 약한 버전으로 두면 참조·가능성은 floor 수준의 종결 수단이 없어 오래 OPEN으로 쌓이는데, 그 무더기가 "미처리 부채"로 읽히지 않게 하는 것이 P1이다. 부채화를 막는 장치가 P1 하나만은 아니다 — 성격별 표현·그룹 방식·카운터 설계도 함께 작용하며, P1은 그 위에 얹히는 제품 전체의 명시적 불변조건이다.

### 현재 구조와의 충돌

`journal_todo`만으로 floor를 충족할 수 없다는 것은 드러났다. 어떤 새 저장 구조가 정답인지는 아직 도출되지 않았다. floor에서 최소 세 가지가 도출된다:

- **lifecycle 삭제가 하드 삭제** — `deleteTodo`(`DELETE /api/journal/todo/{id}`)가 소멸이라 F1·F4와 충돌. 소프트 폐기 또는 폐기 이력이 필요하다.
- **전 범위 리뷰 부재** — 월 스코프 투영이라 F2와 충돌. cross-month 활성집합으로 일부 완화됐으나 "전 범위를 빠짐없이 여는 단일 진입점"은 없다.
- **비TODO 항목의 정직한 수용 불가** — 완료축을 강제해 참조·가능성을 못 담는다(F5 충돌). 비TODO 항목 수용 구조가 필요하다.

### Backlog / Today 2층

보존·관리 기반 위의 두 투영으로 갈린다. 둘은 각각 별도 store가 아니라 동일한 Prospective corpus에 대한 서로 다른 projection이며, 그 corpus의 persistence가 단일 저장소인지 여러 domain의 연합인지는 유예한다.

- **Backlog** — 전 범위 관리 공간(F2의 단일 진입점). 골격은 개인용 이슈 트래커에 수렴하나, 목적은 종결·책임이 아니라 미래 가능성의 보존이며 P1이 그 성격을 가른다.
- **Today / Reminder** — corpus 중 지금 의식에 올라올 active working set. 사이드바가 여기 속한다. 회상(재등장)은 ceiling 층이라 store-first 단계에서는 미구현이고, 이 단계에서 이슈 트래커처럼 *느껴지지* 않게 막는 것은 회상이 아니라 P1이다.

### 구현 진입 시 도출되는 요구

F1 + Source 연쇄 소멸 금지에서 필연적으로 따라오는, 구현 진입 시 반드시 풀어야 하는 요구다(ceiling의 선택적 미래 능력과 구분한다).

- source 삭제와 Prospective 관리 이력 삭제의 범위가 다를 경우, 사용자가 삭제 범위를 구별할 수 있어야 한다(원본만 삭제 / 관리 이력까지 영구삭제).
- 구체적인 UX와 저장 책임은 persistence topology 결정 시 설계한다.

### 유예된 ceiling

- 자동 재등장(page-in / prefetch)
- 시간·장소·맥락 트리거
- stale / expired 의미론 — `EXPIRED`가 `DROPPED`와 구별되는 종결 상태로 승격할 자격이 있는지가 발견 kill-test.
- 통합 엔티티 여부 — TASK/REFERENCE/POSSIBILITY를 별도 엔티티로 분리할지. 표현 단계에서는 UI상 최소한의 성격 구분만 제공하되, 이를 곧바로 공통 persistence enum으로 승격하지 않는다(primitive·relation·projection metadata 중 어디서 표현할지 유예).

## 관련

- [journal-todo-evolution](journal-todo-evolution.md) — 완료 축(lifecycle)·발생지(provenance)·회고 투영(as-of)의 존재론. 이 노트의 완료 축 서술은 그 문서를 SSOT로 참조한다.
- [thread-organization](thread-organization.md) — "불변 발생지 + lifecycle 흐름 + 시점 투영" 템플릿의 동형 문제. 시간 축을 공통화하면 이 계열과 같은 축을 공유한다.
- 현재 코드 계약: `feature/calendar/schedule/**`(일정), `feature/journal/todo/**`(TODO), `feature/attachable/**`(공통축).
