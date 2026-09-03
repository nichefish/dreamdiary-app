# DreamDiary 저장소 — 정적분석 보고서

> 대상: **`main`** HEAD `7d5553cb4` (2026-08-27, `Merge pull request #155 from nichefish/dev_0.29.0`)
> 방법론: CODE_ARCHAEOLOGY.md §7~§14
> 범위: `main` 트리의 `app/backend` Java · `app/frontend-vue` · 평행 클라이언트. `vendor/` 제외. LOC는 `git show main:<path>` 개행 수.
> 해석의 원천: [역사서](REPO_HISTORY.md)
> 1차 재조사 2026-08-15 `[확정]`: 시대·PRESSURE 포화 확인, `attachable` 다형 축(ContentType 17종·fan-in 110·상속 22) 보강.
> 2차 재조사 2026-08-30 `[확정]`: tip을 `feb10fd2d`(2026-08-13)→`7d5553cb4`(2026-08-27)로 전진. 시대·서사 위계 불변, 아래 지표는 longitudinal 갱신(§0.3 자산 재사용).

체크아웃 워킹트리와 숫자가 다를 수 있다. 이 표는 **`main` 블롭**만 잰다.

**측정 정의 봉인** `[확정]`: `ContentType` fan-in은 **`ContentType.` direct-reference(점 참조) 파일 수** 기준으로만 잰다(2026-08 봉인값 109→110). `ContentType` 문자열 포함(214)은 **다른 metric**이며 이 표에 섞지 않는다 — 정의를 바꾸면 가짜 시계열 변화가 생긴다.

**LOC 갱신의 성격** `[확정]`: 아래 §0·§1의 파일별 LOC는 **기존 보고서가 이미 지목한 hotspot의 longitudinal 추적 갱신**이다. 새 tip의 "현재 최대 파일 순위"를 주장하는 전수 재측정이 아니다(전수 순위 주장에는 별도 전수 측정이 필요).

---

## 0. 요약 지표

| 항목 | 값 `[확정]` (`main`) |
|------|-------------|
| 전체 커밋 / 무병합 | 888 / 822 |
| `app/backend/src/main/java` 파일 | 1,026 |
| `frontend-vue` tracked | 295 |
| `frontend-react` tracked | 24 |
| `mobile-react-native` tracked | 46 |
| attachable 도메인 파일 (2위) | 193 |
| `ContentType` 다형 축 | 17종 · fan-in 110(`ContentType.` 점참조) · 상속 22엔티티 |
| Chat 오케스트레이터 (hotspot 추적) | `feature/chat/service/ChatOrchestrator.java` 1,252 LOC |
| Entry 허브 (hotspot 추적) | `JournalEntryService.java` 778 LOC |
| Vue 타입 창고 (hotspot 추적) | `stores/journal.ts` 837 LOC |
| journalModal 파사드 (hotspot 추적) | `stores/journalModal.ts` 142 LOC |
| revert (메시지 검색) | 0 |

**한 줄 진단**: 파일 수가 폭발한 모놀리스가 아니라, **저널 Entry 허브 + (분해 후의) 채팅 오케스트레이터 + Pinia 타입 창고**에 질량이 모인 구조. `ChatAIService` God·단일 journalModal은 `main` 트리가 아니다.

무병합 커밋 수(`main`): 2024=20(착지 노이즈) · 2025=188 · 2026=614(8월 27일 트렁크까지).

---

## 1. PRESSURE 표

모든 LOC는 hotspot longitudinal 추적 갱신(`7d5553cb4`). 괄호는 직전 스냅샷(`feb10fd2d`) 대비.

| 파일 | LOC (`main`) | 응력 유형 | 위험도 | Time Horizon |
|------|-----|-----------|:------:|--------------|
| `feature/chat/service/ChatOrchestrator.java` | 1,252 (←1,392) | 분해 후 오케스트레이터 `[확정]` | 🟡 | 확장 시 |
| `PersonSynthesisHybridService.java` | 971 | Person 합성 `[강한추정]` | 🟡 | 확장 시 |
| `JournalEntryService.java` | 778 (←836) | STI 이후 도메인 허브 `[확정]` | 🟠 | 현재 안정 / 정책 추가 시 🔴 |
| `JournalChapterService.java` | 679 (←730) | Entry와 lockstep `[강한추정]` | 🟡 | Entry와 동상 |
| `JournalDayService.java` | 507 (←539) | rename 화석 주의 `[확정]` | 🟢 | — |
| `JournalThreadService.java` | 490 (←439) | 관계 축 `[확정]` | 🟡 | 관계 기능 추가 시 |
| `JournalReflectionService.java` | 248 (←257) | 전용 축 `[확정]` | 🟢 | 흡수 잔여 시 |
| `ResponseGuardService.java` | 393 | guard 분리 `[확정]` | 🟢 | 프롬프트 계약 변경 시 |
| `stores/journal.ts` | 837 (←868) | 타입+리스트 창고 `[강한추정]` | 🟠 | 타입 이동 시 |
| `stores/journalModal.ts` | 142 (←152) | facade `[확정]` | 🟢 | 다시 한 파일로 합칠 때 🔴 |
| `journalModalEntry.ts` | 378 (←405) | 옛 스토어 질량의 이동 `[강한추정]` | 🟡 | 등록 계약 추가 시 |
| `attachable/_shared` (BaseAttachable*/ContentType) | — | 다형 canonical 허브 `[확정]` | 🟢 | 확장 시(새 콘텐츠 타입) |

`ChatOrchestrator`는 재분해되지 않았다 `[확정]`: `feature/chat/service/`에 단일 오케스트레이터로 건재하며, 1,252 LOC는 직전 스냅샷 1,392에서 축소된 값이다(재분해가 아니라 소폭 감량).

fan-in은 `ContentType.` direct-reference 파일 수(봉인 정의)이며 워킹트리와 섞지 않음 — 정밀 그래프 `[미확인]`.

---

## 2. 상위 응력 — Signal → Interpretation → Counter → Horizon → Action

### 2.1 `JournalEntryService`

**Signal** `[확정]`
- `main` 778 LOC(←836, hotspot 추적). 탄생 `42c927338`(2026-04-22).
- 현재 경로 커밋 수는 rename(`69b74c076`) 이후만 의미가 있다.

**Interpretation** `[강한추정]`
- God이라기보다 **수렴점**. STI 이후 정책이 한 서비스로 들어온다.
- 패키지 경계와 git이 말하는 모듈이 어긋날 수 있음.

**Counter Evidence**
- 테스트 존재. 길이가 ChatOrchestrator·PersonSynthesis보다 작다. revert 0.

**Time Horizon**: 현재 안정 / 정책 추가 시 🔴.

**Action**
- 즉시: 없음.
- 중기: 새 정책은 Entry가 아니라 전용 축(Reflection이 그 문법)으로 둘지 먼저 결정.
- 근거: Signal 허브 + 역사서 Ⅲ STI. Counter의 상대적 크기 → *즉시 분해* 아님.

### 2.2 `ChatOrchestrator` + `feature/ai`

**Signal** `[확정]`
- `feature/chat/service/ChatOrchestrator.java` 1,252 LOC(←1,392, hotspot 추적). `06ca98c26`(2026-08-08)에서 신규 생성.
- 같은 커밋에서 `feature/chat/service/ChatAIService.java` 삭제(D) + `feature/ai/{guard,model,person,prompt,rag}` 19파일 추가(A). `ChatAIService`는 `main` 트리에 없음 `[확정]`.
- Person 합성 971 LOC는 Orchestrator와 **다른 파일**.

**Interpretation** `[강한추정]`: 모놀리식 `ChatAIService`가 삭제되며 책임이 `feature/ai/*` 축으로 분산되고 오케스트레이션이 `ChatOrchestrator`로 수렴했다(삭제 1 / 추가 다수를 `--diff-filter` 로 확정). 이는 재분해가 아니라 **분산+수렴 착지**다.

**Counter**: 테스트 존재(`ChatOrchestratorTest`, `ChatOrchestratorOwnershipTest`). fan-in은 채팅 입구에 한정되는 편.

**Action**: 즉시 없음. `06ca98c26`은 이미 트렁크에 착지한 결과이므로 저장소 조사로 다시 분해하지 말 것.

### 2.3 `stores/journal.ts` vs `journalModal*`

**Signal** `[확정]`
- `journal.ts` 837 LOC(←868). `journalModal.ts` 142 LOC(←152) 파사드. Entry surface 378 LOC(←405). 모두 hotspot 추적.
- 파사드 계열이 chapter/day/entry/reflection/todo/oneShot 스토어로 나뉘어 있음(각 37~378 LOC).

**Interpretation** `[강한추정]`: 응력의 이름은 “journalModal.ts”가 아니라 **등록 계약의 합집합** + 타입 창고.

**Counter**: spec lockstep은 마이그레이션 규율상 건강할 수 있음.

**Action**: 즉시 없음. 파사드를 한 파일로 되돌리지 말 것.

### 2.4 평행 클라이언트

**Signal** `[확정]`: Vue 295 / 웹 React 24 / RN 46 (`main`). `packages/shared-*`(types/api/domain) 존재하나 어느 앱도 import 안 함(소비자 0).

**Interpretation** `[강한추정]`: 웹 React는 Vue와 같은 제품 표면을 복제할 수 있는 자리. RN은 다른 표면.

**Action**: 제품 지위 결정 전 삭제·확장 금지.
### 2.5 `attachable` 다형 프레임워크

**Signal** `[확정]`
- 193 파일(journal 251 다음 2위 도메인). `ContentType` enum 17종(`attachable/_shared/type/ContentType.java`).
- fan-in: `ContentType.` direct-reference 110 파일(봉인 정의).
- `BaseAttachableEntity` 측정은 세 값으로 구분한다: 문자열 등장 22 파일(상속 수로 사용 불가) / class declaration 기준 직접 매핑 상속 `@Entity` 16개 / 동일 테이블용 간소화 매핑 2개를 제외한 논리 CRUD 대상 14개. board·calendar·chat·journal·admin **5+ 도메인 횡단**.
- 논리 CRUD 대상 14개 모두 대응 `BaseAttachableService` 구현체를 가진다(14/14). 기본 `regist/modify/delete`는 tag/meta/managt/history helper를 canonical hook으로 실행한다.
- canonical `modify` 재구현 전수 대조에서 semantic hook omission 2건: `ScheduleService.modify()`는 `afterWrite()`를, `UserService.modify()`는 자체 `preModify()`를 호출하지 않는다. `JournalEntryService.modify/delete()`는 공통 helper를 직접 재호출해 의미를 보존하는 대조군이다.

**Interpretation** `[강한추정]` (감식 [DIG-002](digs/DIG-002-attachable-convergence-bypass.md))
- `ContentType` enum 값과 콘텐츠 CRUD service coverage는 **강하게 수렴**한다 `[확정]`. 위험은 canonical structure의 부재보다 **동일 관할을 국소 재구현한 경로가 canonical contract를 완전히 보존하는가**에 있다.
- `*CmpstnModule`은 declarative capability interface이며 `BaseAttachableProcPostProcessor`의 canonical tag/meta processor 선택에 실제 사용된다 `[확정]`.
- attachable과 journal은 양방향 concrete dependency를 가진다 `[확정]`. attachable→journal을 금지한 독립 package 계약은 확인되지 않아 "역의존 위반" 판정은 `[미확인]`이다.
- 기능별 `ContentType` 집합은 서로 다른 proposition이다. 동일 policy 중복으로 확인된 범위는 `StateService.requiredTypes`와 `JournalStateCacheRegistry.STATE_CONTENT_TYPES`가 별도 선언하는 cache-backed state 4종이다 `[확정]`.
- 구 `clsf`(분류체계)의 후신. 2026-04 명명 혁명에서 `attachable`로 승격되며 다형 백본이 됐다.
- **예외 한 곳** `[확정]`: `processTags`/`processMetas`가 `ContentType.JOURNAL_REFLECTION`을 if 분기로 하드코딩 제외한다(Reflection은 태그·메타를 두지 않음). 이 정책만 base 헬퍼 제어흐름에 있고 순수 선언적이지 않다.

**Counter Evidence** `[확정]`
- 값 추가(새 ContentType)가 기존 코드를 안 깨뜨린다 = enum 값 축은 안정. `ContentType` churn 낮음.
- CRUD override 자체가 결함은 아니다. `JournalEntryService`는 canonical helper 의미를 보존한다.

**Time Horizon**: 현재 🟡(semantic hook omission 2건) / 확장 시(새 local CRUD reimplementation 추가 시 계약 누락 검사 필요) 🟡.

**Action**: 대규모 attachable 재설계가 아니라 [REFACTOR_BACKLOG.md](../REFACTOR_BACKLOG.md)의 두 canonical modify hook omission을 각각 검증·수정한다. 양방향 dependency와 state cache policy 중복은 관측이며 통일 처방은 별 결정이다.

### 2.6 저널 상태의 read model 조립 비용 (복원 마찰, 결함 아님)

**Signal** `[확정]`
- write-side authoritative source는 **DB `StateEntity` 행**(행 존재 = ON, `@Where deleted_at IS NULL`). lifecycle도 DB(`LifecycleEntity`, `findByRefIdAndRefContentType`).
- read-side는 **사용자별 EhCache projection**: 월별/주별 `Map<Integer, JournalState>`(`journal*StateMapByUser`, 캐시명은 `JournalStateCacheRegistry`가 canonical).
- 재생성 경로: `JournalDayService`가 DB에서 조회해 `EhCacheUtils.put(...StateMapByUser...)`로 materialize. `JournalCacheEvictor`가 evict, `JournalDayViewHelper.getStateMap(..., regenerate)`가 재생성. 즉 캐시 miss/evict 시 **DB가 재생성의 원천**.
- 목록 표시값은 `JournalEntryStateEnricher`가 정책(`JournalEntryTypePolicy.stateCacheName/lifecycleCacheName`)에 따라 상태 캐시 + lifecycle을 DTO에 병합해 조립.

**Interpretation** `[강한추정]`
- **canonical이 없는 게 아니다.** write-canonical(DB `StateEntity` + lifecycle)은 명확하고, read model이 사용자별 캐시로 분산·materialized된 **CQRS-lite** 구조다. 불일치 시 승자는 DB(evict 후 재생성).
- 따라서 "현재 상태를 복원하려면 단일 테이블 조회로 불충분하고, 복수 상태원(state 토글행 + lifecycle) + 사용자별 projection의 합성이 필요"하다. 이것은 **read model 조립·추적 비용**이지 설계 결함이 아니다.

**Counter Evidence**
- write 경로는 단일 canonical(DB)이라 진실원천 혼선 없음. `@PostConstruct validateStateCacheUpdaters()`가 타입별 updater 유일성을 부팅 시 강제.
- 캐시는 언제나 DB에서 재구성 가능(파생물).

**Time Horizon**: 현재 ⚪(정상 동작) / 상태 원천·projection이 더 늘면(예: 새 상태 축) 조립 지점 추적 비용 🟡.

**Action**: 없음. SYSTEM_ISSUES의 설계 결함으로 승격하지 않는다 — read 경로 이해 비용일 뿐 write canonical은 단일하다.

---

## 3. AUTHORSHIP / OWNERSHIP

solo. `main` 상위 응력 파일의 Git author는 `nichefish` `[확정]`. 버스팩터는 이탈이 아니라 **6개월 전 커밋·병행 SAVEPOINT·스냅샷이 트렁크를 못 따라갈 때**.

---

## 4. Dead / Zombie / TODO

| 판정 | 대상 | 등급 |
|------|------|------|
| 잠든 실험 | `app/frontend-react` 24파일 | `[확정]` 존재. 지위 `[미확인]` |
| 미소비 예비 | `packages/shared-*`(types/api/domain) | `[확정]` 어느 앱도 import 안 함. 지위 `[미확인]` |
| 해소 | `ChatAIService.java` | `[확정]` `main`에 없음 |
| 해소 | 단일 `journalModal.ts` God | `[확정]` facade |
| 해소 | FreeMarker MVC 화면 | `[확정]` 2026-07-02 |
| TODO 전수 | 생략 | `[미확인]` |
| 비-트렁크 | 체크아웃(`dev_0.30.0`) 전용 미머지 변경 | `[확정]` 이 표에 넣지 않음 |

---

## 5. 권고 근거 역참조

**권고: Entry 즉시 분해 금지, 정책은 전용 축 우선**  
근거: Signal 836 LOC. Counter 상대 크기. 역사서 Ⅳ 관계 축 (Reflection 전용 테이블).

**권고: AI/Chat 재분해 착수 금지**  
근거: Signal `06ca98c26`이 이미 `main`.

**권고: 웹 React는 결정 전 삭제 금지**  
근거: Signal 24파일. 지위 `[미확인]`.

**권고: 다음 고고학은 트렁크 SHA를 헤더에 고정**  
근거: 체크아웃 초안 FP. 부록 §14.

---

## 6. 이번 조사에서 권장하지 않는 행동

- `JournalEntryService` 즉시 분해.
- `ChatOrchestrator`를 구 `ChatAIService` 처방으로 다시 쪼개기.
- `journalModal.ts` 152줄을 “1,166 God”로 인용.
- `frontend-react` 즉시 삭제.
- `JournalDayService`를 `--follow` churn으로 몬스터 지정.
- 착지 +324,801줄을 2024년 생산성으로 읽기.
- 체크아웃 브랜치의 다음 기능을 이 PRESSURE 표에 섞기.
- `attachable` 프레임워크 대규모 리팩터 — canonical 안정 축, 확장 시에만.

---

## 7. 조사 한계

- fan-in/co-change 정밀 그래프 없음.
- hotfix/장애 전수 없음.
- 별도 CHECKLIST 본체는 이 저장소에 없음. 방법론은 `CODE_ARCHAEOLOGY.md` 정본만 따름.
- 워킹트리가 작업 브랜치(예: `dev_0.30.0`)면 에디터 LOC와 이 표가 어긋나는 것이 정상이다.
