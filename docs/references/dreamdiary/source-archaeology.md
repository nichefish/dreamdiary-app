# 소스코드 고고학 — DreamDiary 저장소 지층 조사

조사 기준: `dev_0.26.0` / HEAD `d96b0dc1a` (2026-08-07) · 792커밋 · 기원 `f1759ae0c` (2024-10-12).
방법론: [CODE_ARCHAEOLOGY_CHECKLIST.md](../CODE_ARCHAEOLOGY_CHECKLIST.md). 진단·처방은 [system-issues.md](system-issues.md).
성격: **solo 저장소** — 위험축은 "이탈자"가 아니라 문서·스키마 지연 / 허브 축적 / 평행 표면 / 병행 작업(§14 solo 변이).

> 원천(과거·지층)만 담는다. 코드·활성 스펙이 이미 설명하는 현재 계약은 중복하지 않는다.
> 모든 결론에 §0 증거 등급(`[확정]`/`[강한추정]`/`[약한추정]`/`[미확인]`)을 붙인다.

---

## DreamDiary의 역사

### 제1장. 건국과 이식 (2024-10 ~ 2025-03)

2024-10-12, `f1759ae0c` "main branch history initialized." 한 줄로 저장소가 시작된다. 저자는 처음부터 한 사람 `[확정]`. 첫 커밋 시점에 이미 1,000+ 소스 파일 — **git이 시작된 것이지 코드가 시작된 것이 아니다** `[강한추정]`. 다른 곳에서 완성된 체계(Spring Boot·FreeMarker·Handlebars·jQuery)의 이식이다.

이식된 체계의 핵심 축은 **저널**: 일기·꿈·해석·할일이 각각 독립 테이블·서비스·화면을 가진 분리 체제. 화면은 FreeMarker(서버 렌더링) + Handlebars(클라이언트 파셜) 이중 구조. 사건 없는 축적기.

### 제2장. 대침묵 (2025-04 ~ 2025-07)

2025-04부터 커밋 급감(월 2건), 6·7월 **두 달 0건** `[확정, git]`. 원인은 git만으로 `[미확인]`. 멈출 수 있었다는 것 자체가 "일단 돌아가는 레거시"였다는 증거. **외부 압력(고객·일정·팀) 부재** = 개인 프로젝트의 규정적 특징 `[강한추정]` — 있었다면 두 달을 멈출 수 없다.

### 제3장. 재각성과 규율의 문서화 (2025-08 ~ 2026-03)

2025-08 소량 재개 → 12월 가속 → 2026-02~03 `DESIGN_NOTES`·`CHANGELOG` 두꺼워짐 `[확정]`. 단순 문서 추가가 아니라 **코드 작성 방식의 전환** — "왜 이렇게 하는가"를 기록하기 시작. 침묵기에 방향을 정하고 **규율을 들고 돌아왔다** `[강한추정]`.

### 제4장. 대변혁 — STI와 Vue (2026-04 ~ 05)

역사상 최고 밀도 구간. 세 사건이 프로젝트를 다른 존재로 만든다.

**첫째, 단일 테이블 상속(STI).** 2026-04-22 `42c927338` `[확정, 커밋]`. 분리 테이블(일기·꿈·해석·할일)이 하나의 `journal_entry`로 수렴한다. `JournalEntryService`가 이 순간 탄생. 단순 리팩터가 아니라 **도메인 모델의 재정의** — "일기와 꿈은 다른 것"에서 "모든 저널 기록은 하나의 엔트리"로. 이 결정의 파급력이 이후 모든 역사에 걸린다: 엔트리가 단일 허브가 되면서 이후 추가되는 모든 기능(스레드·리플렉션·임베딩·접두사)이 이 허브로 수렴한다.

**둘째, 패키지 전면 rename.** 2026-05-12 `69b74c076` `[확정]`. 행정표준용어(`jrnl`·`clsf`)가 풀네임(`journal`·`attachable`)으로 일괄 치환. 수백 파일 이동, 내용은 거의 불변 — **이름이 바뀌었다는 것은 정체성을 다시 정의했다는 것**. 부산물: `git --follow` churn이 이 시점 기준 왜곡된다. DreamDiary의 git 분석은 항상 이 rename 전후를 갈라 읽어야 한다(§심층 참고).

**셋째, Vue SPA 도입.** 2026-05-15 `eb86539fa` `[확정]`. `frontend-vue` 생성, FreeMarker/HBS 화면 경로 제거 시작. 서버 렌더링 → 클라이언트 SPA. 기술 교체이자 **개발 방식의 교체**(partial 편집 → 컴포넌트 개발).

건국이 아니라 **혁명** — 한 달 안에 도메인을 재정의하고 이름을 새로 짓고 화면을 새로 만들었다.

### 제5장. 짧은 실험 — React (2026-06-13)

`aae6585c5` "react-init" `[확정]`. Vue를 behavior reference로 두고 React를 별도 출력으로 키우려는 구상(`react-screen-overview.md`). auth 하나만 구현된 채 멈춤. Vue 축 확장이 가속되며 복제 여력이 사라짐 — **명시적 폐기 없이 잠든 실험** `[강한추정]`. 현재 제품 지위는 비즈니스 확인 사항 `[미확인]`.

### 제6장. 수렴과 청소 (2026-06 ~ 07)

- **FLOW의 생과 사** — 07-20 도입 → 07-21 스레드로 흡수·폐기 `[확정]`. 하루. 되돌린 게 아니라 **접어서 다음 구조에 합침** — 이 저장소 revert 0건의 원형.
- **FreeMarker 화면 제거** — 07-02 `c7fc9edd7` `[확정]`. 서버 렌더링 경로가 소스에서 사라짐. legacy 원본은 리포 밖 백업에만.
- **인코딩 위기** — 07-25 연속 복원 커밋 `[확정]`. Windows cp949에서 UTF-8 한글 주석 손상. **환경과 계약의 충돌** → `check_encoding.py` 게이트 탄생. 위기가 제도를 만든다 `[강한추정]`.

### 제7장. 에이전트의 등장과 규율 체계 (2026-05 ~ 08)

`AGENTS.md` 성격 작업 규칙이 명시적으로 진입 — **AI 에이전트 협업의 물증** `[강한추정]`. 이후: Conventional Commits 정착, spec을 코드 동급 자산으로(spec churn이 코드 churn 초과), SAVEPOINT 통제, 인코딩·빌드 게이트 자동화, 룰 SSOT(AGENTS.md → CLAUDE.md·cursor.mdc sync). 커밋 밀도: 2024년 34 → 2025년 221 → 2026년 대폭 증가 `[확정, git]`.

### 제8장. 체계 재설계 — 리플렉션과 스레드 (2026-08)

STI가 엔트리를 접었지만 그 위 **관계 구조**는 미정리였다. 8월에 두 축이 동시 이동.

- **리플렉션**: `interpretation`(해석)을 `reflection`으로 재정의, `journal_reflection` 전용 축으로 영속. "모든 것을 엔트리로"의 두 번째 물결 `[확정]`.
- **스레드**: 엔트리 사이 관계. FLOW가 하루 만에 실패한 자리의 대체물, thread-relation까지 확장 `[확정]`.

`JournalEntryService`가 이 흡수로 팽창한다(§구조 지형도) — 허브가 커지는 것은 수렴의 대가.

### 제9장. 안정화 — 이음새를 메우다 (2026-08-03 ~)

챕터 삭제 가드 · 리플렉션 collapse 확정(우선순위 순수 함수 + 단위 테스트로 수렴) · thread-relation 완성 · tagify maxlength · membership tag 카테고리 해소 · **커밋 메시지 한글 규약(AGENTS §10) 도입**. 성격은 하나: **기존 구조의 엣지케이스 봉합** `[확정]`. 혁명이 끝나고 제도가 안정되는 시기.

### 제10장. ChatAI — 또 하나의 문명

저널과 별개 축. `ChatAIService.java` **3,505 LOC** — 프로젝트 최대 단일 파일 `[확정]`. 성장 리듬이 저널과 다르다 `[강한추정]`:

1. 초기: 단순 LLM 호출 + WebSocket 브로드캐스트
2. RAG 도입: 저널 임베딩 검색 → 컨텍스트 빌딩 체인
3. Person Meaning: "이 사람에 대해 저널이 뭘 말하는가" 합성 메서드 다수
4. Guard 축적: 응답 품질 검증(hallucination·language·scaffold leak) 메서드 연쇄

저널이 "수렴하되 여러 서비스로 분리"인 것과 달리 ChatAI는 "하나의 서비스가 모든 것을 소유"로 성장 — **한 저장소 안 두 성장 문법**. churn 낮음 = 안정되면 잘 안 건드림. 위기가 아니라 잠재력 — 다음 기능 추가 시 재팽창할 것이다.

### 통섭 — 이 프로젝트의 역사가 말하는 것

DreamDiary는 **한 사람이 "세우고 접는" 사이클을 반복하며 수렴시킨 역사**다.

- FreeMarker를 접었다(Vue로) · 분리 테이블을 접었다(STI로) · FLOW를 접었다(Thread로) · `jrnl` 이름을 접었다(`journal`로) · interpretation을 접었다(Reflection으로) · React는 접지 않고 멈춰뒀다.

이 문명엔 **revert가 0건** `[확정]`. 되돌리지 않고 다음 구조로 접어 넣는다. 그 대가로 수렴 목적지(허브)가 계속 커지고, 접지 않은 실험(React)이 zombie로 남고, 코드가 기록보다 한 박자 앞선다. **"치우면서 달리는데, 달리는 목적지가 계속 비대해지는" 문명.**

---

## 기여자 · 버스팩터

| 작성자 | 커밋수(no-merges) | 비고 |
|---|---:|---|
| nichefish \<nichefish@gmail.com\> | 729 | 주력 `[확정]` |
| Nysnyari \<…@users.noreply.github.com\> | 1 | 동일인(이메일 로컬 `nichefish`) `[강한추정]` |

**버스팩터**: 핵심 경로 blame 사실상 100% nichefish `[확정]`. 다인·이탈 지표 해당 없음(§14 solo 변이). 위험은 "물어볼 사람 없음"이 아니라 **"6개월 전 나 / 병행 SAVEPOINT를 문서가 못 따라갈 때"**.

---

## 구조 지형도 — Entry hub (공시 단면)

대상: `app/backend/.../feature/journal/entry/service/JournalEntryService.java`.

### 성장 (LOC, 현재 경로) `[확정]`

| 시점 | 커밋 | LOC |
|---|---|---:|
| 패키지 재편 | `69b74c076` (2026-05-12) | 623 |
| FreeMarker 제거 무렵 | `c7fc9edd7` (2026-07-02) | 646 |
| HEAD | `d96b0dc1a` (2026-08-07) | **831** |

최근 646→831(+185)은 리플렉션·스레드·collapse 흡수에 딸린 팽창 — 완만 축적이 아니라 **수렴의 대가**.

### NETWORK / CHANGE

- fan-in: 참조 java **19파일** — chapter/thread/reflection/attachable/strategy·controller. fan-in ≫ fan-out 허브 `[강한추정]`(혼합 모듈로 상한).
- co-change 상위: `JournalChapterService`·`JournalThreadService`·`JournalEntryDto`·day helper·journal specs·messages·`schema-journal` `[확정]`. 정적 패키지는 나뉘나 **진화 위상은 한 덩어리** — git이 말하는 모듈 경계가 파일 경계보다 크다.

### PRESSURE

응력 高(fan-in 허브 + 지속 churn + LOC↑) × 변형: revert **0** `[확정]`, 정량 변형 `[미확인]`(이슈키 관행 없음) × 교차축: 이탈자 0 → **"응력 ∩ 진행 중 재설계"**. 원인 태그: 도메인 허브(정당) + 책임 확장(주시: delete nullify·prefix·thread 검증 유입).

---

## 심층 — 구조축 확장

### rename 보정 — follow churn의 함정 `[확정]`

`jrnl`→`journal` 치환(`69b74c076`) 이전까지 포함한 `--follow` churn은 부풀려진다.

| 파일 | follow churn | post-rename(`69b74c076..HEAD`) |
|---|---:|---:|
| `JournalDayService` | 74 | **6** |

follow 74만 보면 "몬스터 churn"이지만 rename 이후 실제는 6 — **화석이다.** DreamDiary churn은 반드시 rename 전후를 갈라 읽는다.

### 백엔드 응력표 (rename 보정) `[확정 LOC / 강한추정 fan-in]`

| 파일 | fan-in | LOC | 판정 |
|---|---:|---:|---|
| `JournalEntryService` | 19 | 831 | STI 이후 도메인 허브 — 바쁜 수렴점 |
| `JournalChapterService` | 8 | 730 | co-change로 Entry와 lockstep |
| `JournalThreadService` | 6 | 439 | 관계 축 |
| `ContentType` | **254** | ~50 | enum 허브 — fan-in 高 = 정당 타입 축 |
| `DateUtils` | **121** | ~263 | 유틸 허브 — fan-in 高 ∩ churn 低 = 괜찮은 응력 |

`ContentType`·`DateUtils`는 병목이 아니라 정당 공통 축(높은 fan-in만으로 병목 라벨 금지).

### Vue 프론트 몬스터 `[확정]`

진짜 프론트 응력은 백엔드가 아니라 저널 스토어에 걸린다.

| 파일 | fan-in(`use*`/import) | LOC | 정체 |
|---|---:|---:|---|
| `stores/journalModal.ts` | 28 | **1166** | day/chapter/entry/reflection/todo 등록 모델 + 필터 시드 + 카테고리 맵 preload/sync + 모달 오케스트레이션이 **한 스토어** — 최대 단일 파일 |
| `stores/journal.ts` | **42** | 727 | Pinia day/list 상태 + **저널 DTO 타입 창고**(Entry/Chapter/Day/Tag…) — 타입 import만으로 fan-in 최대 |

co-change `[확정]`: `journal.ts` ↔ journal specs(screen/interaction/component) 다회 = 스펙 lockstep 문화(긍정) · `journal.ts` ↔ `journalModal.ts` = 정적 import + **한 모듈로 이동**. **정적↔진화 갈림**: 스토어는 둘이지만 타입 창고 + 모달 오케스트레이션이 day 화면 전체와 lockstep → **파일 경계가 거짓**일 수 있다(원인: 마이그레이션 중 UI 허브 축적).

### 평행 클라이언트 문명 `[확정 존재 / 미확인 지위]`

| 축 | tracked 파일 | 상태 |
|---|---:|---|
| `app/frontend-vue` | 244 | 주 수렴 경로 |
| `app/frontend-react` | 24 | react-init 셸(auth만) — 잠든 실험 |
| `app/mobile-react-native` | 46 | 별도 클라이언트 |

웹 표면에 Vue 주축 + React 평행 구현 = 미병합 브랜치와 동형의 평행우주(§15). 키울지/폐기할지는 비즈니스 확인 `[미확인]`.

### 삭제 문화 `[확정]`

`app/` 아래 `--diff-filter=D` 커밋 **10건** — FreeMarker 제거·dead HBS·FLOW 수렴·마이그레이션 정리 등 **청소 의도가 분명한 사건** 포함. 수렴 철학과 일치.

---

## 지층 화석 (원천)

### UI 문명: FreeMarker/HBS → Vue

- 화면 FreeMarker MVC 제거 `c7fc9edd7`(2026-07-02). 이메일은 FreeMarker 잔존.
- legacy 원본: git 직전 트리 + 리포 밖 백업(`DEV_NOTES` 계약). UI SSOT는 legacy DOM/CSS — AGENTS "레거시 복원 모드".

### 도메인: interpretation → Reflection

- `interpretation`을 `reflection`으로 재정의, `journal_reflection` 전용 엔티티·리포지토리로 영속. 검색·상태·쓰기 축을 전용 테이블 기준으로 구성 `[확정]`.

### 스키마 베이스라인 지체 `[확정]`

- `schema/full/mariadb/schema-journal-mariadb.sql`에 분리 테이블(diary/note/dream/interpretation) CREATE는 있고 **`journal_entry` CREATE는 없음**. 런타임 STI가 베이스라인 DDL을 앞선다 = "빈 환경의 진실"이 사람 머리에 의존. 인지된 지층 어긋남으로만 기록(처방은 system-issues).

### 마이그레이션 정책 `[확정]`

- `schema/migration/` 트리 **비어있음**. **1.0 전까진 Flyway 증분 마이그레이션을 쌓지 않고 마스터 스키마만 SSOT로 삼는 정책**(Flyway `enabled: false`). 증분은 1.0부터 누적 예정.

### FLOW 폐기

- 2026-07-20 도입 → 07-21 스레드 소속으로 수렴·폐기. DESIGN_NOTES에 "폐기된 결정"으로 현재 계약과 분리.

---

## 열린 질문 (git만으로 답이 안 나오는 것)

- 2025-06~07 침묵의 원인(개인/환경).
- React 평행 축의 현재 제품 지위(폐기·보류·모바일만).
- full schema를 `journal_entry` STI로 언제 수렴할지(1.0 마스터 스키마 정합 시점).

---

## 12질문 요약 (Entry hub)

| # | 답 (등급) |
|---|---|
| WHAT | 저널 엔트리 CRUD·정책·캐시 축 중심 서비스 `[확정]` |
| WHERE | chapter/thread/reflection/attachable 등 ~19 소비자 `[강한추정]` |
| WHEN | 2026-04-22 STI 탄생 → 2026-08 reflection/thread로 831 LOC 급팽창 `[확정]` |
| WHY | STI 단일 테이블 수렴 + 후속 도메인 흡수 `[강한추정]` |
| WHO | 저널 aggregate 전체와 결합; 저자=nichefish solo `[확정]` |
| BUS_FACTOR | 이탈자 0; 병행 SAVEPOINT·자기 이력 의존 `[확정]` |
| SOURCE OF TRUTH | 런타임 entity; full schema·migration은 후행(migration은 정책상 비움) `[확정]` |
| POLICY | Reflection target nullify·day-axis 제외 등 entry 정책에 퇴적 `[확정]` |
| EXCEPTIONS | 고객사 하드코딩형 미검출(solo·비상용) `[미확인]` |
| FAILURES | revert 0; encoding 복원·collapse Boolean 캐스팅 씨름이 절차/설계 흔적 `[확정]` |
| RESPONSIBILITY | interpretation→reflection 전환 = 의도적 도메인 재정의 `[확정]` |
| DEAD/ZOMBIE | React 평행 축=지위 미확인 `[강한추정]` |
| REMOVE | Entry 단순화/분할 시 chapter·thread·reflection enrich·캐시 동시 영향 `[강한추정]` |
| NETWORK | 허브/싱크 (fan-in 19) `[강한추정]` |
| CHANGE | chapter/thread와 lockstep `[확정]` |
| PRESSURE | 응력高 × 이탈0 × 재설계중 = "바쁜 수렴점" |

---

*증거 등급: [CODE_ARCHAEOLOGY_CHECKLIST.md](../CODE_ARCHAEOLOGY_CHECKLIST.md) §0. 진단·처방: [system-issues.md](system-issues.md).*
