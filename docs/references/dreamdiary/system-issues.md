# 현재 시스템의 문제점 — DreamDiary (고고학 후속)

[소스코드 고고학](source-archaeology.md)이 지층·나이테를 다뤘다면, 이 문서는 그 원천이 **지금** 어떤 성격의 문제로 살아있는지만 적는다.
처방·작업 순서는 여기 두지 않는다 — 활성 마이그레이션 정본(`docs/migration/journal/*`, `DEV_NOTES`)과 SAVEPOINT가 담당한다.

기준 HEAD: 고고학 심층 시점 `d4b855a78` 문서화 기준 · 런타임 코드는 그 전후 Reflection 병행 커밋과 공존 (2026-08-03). 방법론의 세 겹(Ⅰ 동작 / Ⅱ 구조·절차 / Ⅲ 추상화)으로만 분류한다.

> **제외**: Reflection 흡수 진행 중 `DESIGN_NOTES` 요약 줄이 `reflection-absorption.md`보다 늦은 현상.
> 동일 시점 다른 에이전트가 Phase를 밀어 올리는 **병행 SAVEPOINT의 자연 지연**이며, 동기화 사고로 취급하지 않는다.
> Reflection·interpretation 공존 자체도 Phase 4 전까지 계약상 허용 상태다(정본 § Phase 계획).

---

## 진단 매트릭스

| # | 문제 | 무게중심 | 근거(고고학) | 비고 |
|---|---|:---:|---|---|
| 1 | Entry 서비스가 chapter/thread/reflection과 진화적으로 한 모듈 | **Ⅲ** | `JournalEntryService` co-change·fan-in; LOC 563→740 | 길이가 아니라 **경계**. 패키지 분할과 git이 말하는 모듈이 어긋남 |
| 2 | UI·클라이언트 평행 축의 지위 불명 (`react-init` / react-screen-overview) | **Ⅱ** | `aae6585c5` 이후 overview 문서 잔존; Vue가 주 수렴 경로 | 유령 문명인지 의도적 보류인지 git만으로 미확정 — 확인 전까지 새 작업 진입점 금지 권고 |
| 3 | 인코딩·문서 손상 재발 가능성 (절차) | **Ⅱ** | 2026-07-25 encoding 복원 커밋 연속; AGENTS UTF-8 계약 | 게이트(`check_encoding.py`)는 있으나 Windows 기본 인코딩 편집 경로가 남아 있으면 재발 조건은 구조적으로 존재 |

Ⅰ(런타임 오작동)로 확정된 신규 장애는 본 조사에서 이슈키 맵을 만들지 않았다 — 없음이 아니라 **미측정**.

---

## 겹별 읽기

### Ⅰ 동작의 문제

- 본 세션에서는 Entry hub·문서 병행 지연만 다룸. 무한루프·권한 우회 같은 표층 버그 목록은 작성하지 않음.

### Ⅱ 구조·절차의 문제

- **스키마 full**: 1.0 전 Flyway 증분은 두지 않고 full schema를 선언 SSOT로 둔다. `schema-journal-mariadb.sql`은 `journal_entry`/`journal_reflection` CREATE로 런타임 entity와 맞춘다.
- **멀티 에이전트 SAVEPOINT**: 규율(AGENTS)은 단일 진행을 전제한다. 병행 시 요약 문서 지연은 정상이지만, **정본 파일(`reflection-absorption.md`)과 as-built spec의 갱신 주체**가 흐려지면 Ⅱ로 악화된다. 지금은 정본이 앞서 있고 요약만 늦은 상태라 허용 범위.
- **react 축**: Vue 수렴 규칙과 문서가 공존하면 신규 기여자가 잘못된 진입점을 고를 수 있다.

### Ⅲ 추상화의 문제

- **Entry = 허브**: STI로 "한 테이블"을 얻었으나 서비스 경계가 chapter/thread/사유(해석) 정책까지 빨아들이면, 파일명상의 모듈 분해가 거짓이 된다. SDMS `utils`/`watcher`와 형태는 닮되, 원인 태그는 "고객분기"가 아니라 **도메인 수렴 중인 중심축**이다.
- **해석(interpretation) aggregate**: 독립 aggregate → Entry 종류(Reflection)로 재개념화하는 전환의 한가운데. Ⅲ의 작업이며 Phase 4에서야 추상화가 닫힌다.

---

## SDMS 진단과의 차이

| | SDMS | DreamDiary |
|---|---|---|
| Ⅰ 반복 버그 자리 | utils/watcher에 변형 집중 | 본 조사에서 미측정 |
| Ⅱ | 리뷰 부재·부엌싱크 커밋·gitignore 위생 | schema 이원 채널·멀티에이전트 문서 지연·평행 클라이언트 축 |
| Ⅲ | 고객분기 누적·잘못된 파일 경계 | Entry 허브 경계·interpretation→Reflection 재개념화 |
| 버스팩터 | 이탈자 83% ∩ 응력 | 이탈 0%; 자기·병행 SAVEPOINT |

통섭(고고학): *깎아 수렴하는 문명*. 이 문서의 문제는 그 문명의 **그림자** — 앞선 축을 기록·베이스라인이 따라잡지 못할 때 생긴다.

---


---

## 심층에서 올라온 진단

| # | 문제 | 무게중심 | 근거 | 비고 |
|---|---|:---:|---|---|
| 5 | `journalModal` 스토어가 등록·필터·카테고리맵·다수 모달 오케스트레이션의 싱크 | **Ⅲ** | LOC 641→1038 · fan-in 27 · prefix 커밋 +168 · chat 묶음 +62 | 길이가 아니라 **관심사 경계**. `journal.ts` 타입창고와 lockstep |
| 6 | 웹 React 축(`app/frontend-react`)이 Vue 주경로와 제품 표면을 나란히 둠 | **Ⅱ~Ⅲ** | react-init · overview Savepoint 1 · tracked 24 | overview는 분리 수렴을 쓰지만 AGENTS dual-path/수렴 규칙과 긴장. 폐기·동결·단일화 중 택일 필요 |
| 7 | follow churn 오독 위험 (`jrnl`/`clsf` rename) | **Ⅱ** | Day 74→~14, Intrpt 71→~8 (rename 보정) | 도구/조사 절차 문제. 허브 잘못 지목하면 처방 왜곡 |

### `journalModal` (Ⅲ)

정적 의존은 "모달 스토어" 하나처럼 보이지만, 내용물은 day/chapter/entry/interpretation/todo 등록 계약의 **합집합**이다.  
응력이 높은 이유는 공통유틸이 아니라 **마이그레이션 중 UI 허브로 책임이 유입**되어서다.  
스펙과 lockstep인 점은 문화적으로 건강하나, 허브가 두꺼워지는 속도를 스펙이 함께 정당화할 수 있다.

### `frontend-react` (Ⅱ~Ⅲ)

모바일(`mobile-react-native`)은 별 표면으로 읽힌다. 웹 React는 Vue와 **같은 저널 화면**을 다시 심는 축이라 성격이 다르다.  
"Savepoint 1만 있고 weekly Partial" 상태면, 지금은 기능 부채보다 **문명의 분기점**이 문제다 — 키울지 멈출지 고고학은 결정하지 않는다.

### rename 보정 (Ⅱ)

Day/Interpretation을 "최고 churn 몬스터"로 처방 우선순위에 올리면 안 된다. 치환 전 lockstep 시대의 나이테다.  
실질 주시 대상은 **STI 이후 Entry · Vue journalModal · (정책 결정 대기) React 축**.

---


*산출일: 2026-08-03 (표준+심층). 원천: [source-archaeology.md](source-archaeology.md).*
