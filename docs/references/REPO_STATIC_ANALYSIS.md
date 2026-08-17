# DreamDiary 저장소 — 정적분석 보고서

> 대상: **`main`** HEAD `feb10fd2d` (2026-08-13)
> 방법론: CODE_ARCHAEOLOGY.md §7~§14
> 범위: `main` 트리의 `app/backend` Java · `app/frontend-vue` · 평행 클라이언트. `vendor/` 제외. LOC는 `git show main:<path>` 개행 수.
> 해석의 원천: [역사서](REPO_HISTORY.md)
> 1차 재조사 2026-08-15 `[확정]`: 시대·PRESSURE 포화 확인, `attachable` 다형 축(ContentType 17종·fan-in 109·상속 22) 보강.

체크아웃 워킹트리와 숫자가 다를 수 있다. 이 표는 **`main` 블롭**만 잰다.

---

## 0. 요약 지표

| 항목 | 값 `[확정]` (`main`) |
|------|-------------|
| 전체 커밋 / 무병합 | 817 / 753 |
| `app/backend/src/main/java` 파일 | 1,006 |
| `frontend-vue` tracked | 269 |
| `frontend-react` tracked | 24 |
| `mobile-react-native` tracked | 46 |
| attachable 도메인 파일 (2위) | 193 |
| `ContentType` 다형 축 | 17종 · fan-in 109 · 상속 22엔티티 |
| 측정 최대 Java 서비스 | `ChatOrchestrator.java` 1,392 LOC |
| Entry 허브 | `JournalEntryService.java` 836 LOC |
| Vue 타입 창고 | `stores/journal.ts` 868 LOC |
| journalModal 파사드 | `stores/journalModal.ts` 152 LOC |
| revert (메시지 검색) | 0 |

**한 줄 진단**: 파일 수가 폭발한 모놀리스가 아니라, **저널 Entry 허브 + (분해 후의) 채팅 오케스트레이터 + Pinia 타입 창고**에 질량이 모인 구조. `ChatAIService` God·단일 journalModal은 `main` 트리가 아니다.

무병합 커밋 수(`main`): 2024=20(착지 노이즈) · 2025=188 · 2026=545(8월 13일 트렁크까지).

---

## 1. PRESSURE 표

| 파일 | LOC (`main`) | 응력 유형 | 위험도 | Time Horizon |
|------|-----|-----------|:------:|--------------|
| `ChatOrchestrator.java` | 1,392 | 분해 후 오케스트레이터 `[확정]` | 🟡 | 확장 시 |
| `PersonSynthesisHybridService.java` | 971 | Person 합성 `[강한추정]` | 🟡 | 확장 시 |
| `JournalEntryService.java` | 836 | STI 이후 도메인 허브 `[확정]` | 🟠 | 현재 안정 / 정책 추가 시 🔴 |
| `JournalChapterService.java` | 730 | Entry와 lockstep `[강한추정]` | 🟡 | Entry와 동상 |
| `JournalDayService.java` | 539 | rename 화석 주의 `[확정]` | 🟢 | — |
| `JournalThreadService.java` | 439 | 관계 축 `[확정]` | 🟡 | 관계 기능 추가 시 |
| `JournalReflectionService.java` | 257 | 전용 축 `[확정]` | 🟢 | 흡수 잔여 시 |
| `ResponseGuardService.java` | 393 | guard 분리 `[확정]` | 🟢 | 프롬프트 계약 변경 시 |
| `stores/journal.ts` | 868 | 타입+리스트 창고 `[강한추정]` | 🟠 | 타입 이동 시 |
| `stores/journalModal.ts` | 152 | facade `[확정]` | 🟢 | 다시 한 파일로 합칠 때 🔴 |
| `journalModalEntry.ts` | 405 | 옛 스토어 질량의 이동 `[강한추정]` | 🟡 | 등록 계약 추가 시 |
| `attachable/_shared` (BaseAttachable*/ContentType) | — | 다형 canonical 허브 `[확정]` | 🟢 | 확장 시(새 콘텐츠 타입) |

fan-in은 이름 grep 수준이며 워킹트리와 섞지 않음 — 정밀 그래프 `[미확인]`.

---

## 2. 상위 응력 — Signal → Interpretation → Counter → Horizon → Action

### 2.1 `JournalEntryService`

**Signal** `[확정]`
- `main` 836 LOC. 탄생 `42c927338`(2026-04-22).
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
- 1,392 LOC. `ChatAIService.java`는 `main`에 없음. `06ca98c26` 이후 패키지 분리.
- Person 합성 971 LOC는 Orchestrator와 **다른 파일**.

**Interpretation** `[강한추정]`: 구 God 질량이 갈라짐. 한 파일 3,505 서술은 폐기. Ⅳ의 AI 축에서 저널형 흡수·폐기를 따라옴.

**Counter**: 테스트 존재. fan-in은 채팅 입구에 한정되는 편.

**Action**: 즉시 없음. `main`에 막 들어온 접기를 저장소 조사로 다시 분해하지 말 것.

### 2.3 `stores/journal.ts` vs `journalModal*`

**Signal** `[확정]`
- `journal.ts` 868 LOC. `journalModal.ts` 152 LOC 파사드. Entry surface 405 LOC.

**Interpretation** `[강한추정]`: 응력의 이름은 “journalModal.ts”가 아니라 **등록 계약의 합집합** + 타입 창고.

**Counter**: spec lockstep은 마이그레이션 규율상 건강할 수 있음.

**Action**: 즉시 없음. 파사드를 한 파일로 되돌리지 말 것.

### 2.4 평행 클라이언트

**Signal** `[확정]`: Vue 269 / 웹 React 24 / RN 46 (`main`).

**Interpretation** `[강한추정]`: 웹 React는 Vue와 같은 제품 표면을 복제할 수 있는 자리. RN은 다른 표면.

**Action**: 제품 지위 결정 전 삭제·확장 금지.
### 2.5 `attachable` 다형 프레임워크

**Signal** `[확정]`
- 193 파일(journal 242 다음 2위 도메인). `ContentType` enum 17종.
- fan-in: `ContentType.` 참조 109 파일. `BaseAttachableEntity` 상속 22 엔티티 — board·calendar·chat·journal·admin **5+ 도메인 횡단**.

**Interpretation** `[강한추정]`
- 정책의 canonical 저장소가 **enum + `BaseAttachable*` base**다. 제어흐름에 퇴적된 게 아니라 한곳에서 읽힌다.
- 구 `clsf`(분류체계)의 후신. 2026-04 명명 혁명에서 `attachable`로 승격되며 다형 백본이 됐다.

**Counter Evidence** `[확정]`
- 값 추가(새 ContentType)가 기존 코드를 안 깨뜨린다 = 안정 축. `ContentType` churn 낮음.
- 고 fan-in은 허브 리스크지만, 축이 canonical이라 변경 FC가 예측 가능.

**Time Horizon**: 현재 ⚪ / 확장 시(새 콘텐츠 타입·정책 분기) 🟡.

**Action**: 없음. canonical 축이라 대규모 리팩터 대상 아님.

---

## 3. AUTHORSHIP / OWNERSHIP

solo. `main` 상위 응력 파일의 Git author는 `nichefish` `[확정]`. 버스팩터는 이탈이 아니라 **6개월 전 커밋·병행 SAVEPOINT·스냅샷이 트렁크를 못 따라갈 때**.

---

## 4. Dead / Zombie / TODO

| 판정 | 대상 | 등급 |
|------|------|------|
| 잠든 실험 | `app/frontend-react` 24파일 | `[확정]` 존재. 지위 `[미확인]` |
| 해소 | `ChatAIService.java` | `[확정]` `main`에 없음 |
| 해소 | 단일 `journalModal.ts` God | `[확정]` facade |
| 해소 | FreeMarker MVC 화면 | `[확정]` 2026-07-02 |
| TODO 전수 | 생략 | `[미확인]` |
| 비-트렁크 | `dev_0.28.0`의 shared/api·tmplat | `[확정]` 이 표에 넣지 않음 |

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
- 워킹트리가 `dev_0.28.0`이면 에디터 LOC와 이 표가 어긋나는 것이 정상이다.
