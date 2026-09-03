# DIG-002 — attachable canonical 축에 우회 경로가 있는가

> 저장소: DreamDiary `main` (조사 tip `7d5553cb40`, 2026-08-27).
> 방법론: [CODE_MICRO_ARCHAEOLOGY.md](../../CODE_MICRO_ARCHAEOLOGY.md). 거시 지도: [REPO_STATIC_ANALYSIS.md](../REPO_STATIC_ANALYSIS.md) §2.5 / [REPO_HISTORY.md](../REPO_HISTORY.md) 통섭 10.

**태그**: `[DEFECT]` `[CORRECTION]` `[MECHANISM]` `[METHOD]`

---

## 발단 (SEED)

거시 명제(정적분석 §2.5 / 역사서 통섭 10): **attachable은 반복 재전개가 아니라 canonical 축으로 수렴한 성공 사례다. 정책이 제어흐름이 아니라 `ContentType` enum + `BaseAttachable*` base에 canonical하게 있다.**

이 부재-주장("우회 경로가 별로 없다 / 정책이 수렴했다")을 공격한다.

**성격 (선정독립성)**: H1(build-through 생성규칙)이 지목한 **H-directed adversarial forensic**. `Q3`(H 없어도 같은 규칙으로 선택?)=No → **독립 검증력 없음**. H1 전역 posterior를 움직이지 않는다. 발견·사례 등급 갱신에만 쓴다(계약 §4, DIG-001 admissibility 처리 준용).

---

## Comparison Eligibility / Jurisdiction Gate

경로 부재를 우회로, 구조 차이를 불일치로, 반복 표현을 중복으로 판정하기 전에 비교 대상이 **동일한 관할·역할·semantic proposition**에 속하는지 먼저 입증한다.

- `BaseAttachableService`의 관할은 attachable 데이터축 자체의 CRUD가 아니라 `BaseAttachableEntity` 기반 콘텐츠의 공통 CRUD 계약이다.
- tag/meta 후처리와 lifecycle/state/prefix/related의 독립 명령은 서로 다른 사건이다. 후자가 base CRUD를 경유하지 않는다는 사실만으로 같은 경로의 우회가 되지 않는다.
- 기능별 `ContentType` 집합은 같은 타입을 일부 공유해도 서로 다른 capability 정책일 수 있다. 같은 proposition임이 확인된 집합만 중복으로 비교한다.

---

## 가설 분해

### H2-A — Content CRUD orchestration

논리적 attachable 콘텐츠의 `regist/modify/delete`와 공통 tag/meta/managt/history 처리는 `BaseAttachableService` 계약으로 수렴한다.

**반증조건**: 동일한 콘텐츠 CRUD 관할의 override/reimplementation이 canonical hook을 생략해 계약된 의미를 잃는다.

### H2-B — Capability declaration / processing

`*CmpstnModule` 선언은 canonical processor 선택에 실제 사용되며 caller가 같은 dispatch를 반복하지 않는다.

**반증조건**: capability interface가 dispatch에 쓰이지 않거나, 동일 tag/meta 처리의 type check·processor 선택이 여러 caller에 재전개된다.

### H2-C — Package dependency architecture

attachable과 journal 사이에는 독립적으로 확인 가능한 의존 방향 계약이 있고 구현이 그 경계를 지킨다.

**반증조건**: 먼저 금지 방향을 명시한 계약을 확보한 뒤, 그 방향의 concrete dependency를 찾는다. 금지 계약이 없으면 의존 토폴로지만 관측하고 위반 판정은 보류한다.

### H2-D — Capability policy ownership

동일한 capability 지원 정책은 한 canonical 소유자에게 있으며 lockstep 변경이 필요한 재선언이 없다.

**반증조건**: 같은 semantic proposition을 나타내는 지원 집합이 둘 이상의 위치에 독립 선언된다.

---

## 고증 (1차 사료)

### E1 — 관할 모집단과 측정 정의 [확정]

`main` 블롭에 대해 class declaration과 generic implementation을 직접 대조했다.

| 측정 | 값 | 정의 |
|---|---:|---|
| 구 측정 | 22 파일 | `BaseAttachableEntity` 문자열이 등장한 Java 파일 수. base/spec/helper/mapstruct까지 포함하므로 **상속 엔티티 수로 사용할 수 없다**. |
| 직접 매핑 상속 엔티티 | 16 | `*Entity.java`의 class declaration이 실제로 `extends BaseAttachableEntity`인 `@Entity` 수. |
| 논리적 CRUD 대상 | 14 | 16개에서 같은 테이블의 목록·참조용 간소화 매핑 `BoardPostSmpEntity`, `JournalThreadSmpEntity`를 제외한 콘텐츠 aggregate 수. |
| 대응 canonical service | 14/14 | 각 논리적 CRUD 대상 entity를 generic `Entity`로 갖는 `BaseAttachableService` 구현체 수. |

14개 구현체는 `PopupService`, `CommentService`, `BoardPostService`, `ScheduleService`, `ChatMessageService`, `JournalAnnualService`, `JournalAnnualReviewService`, `JournalChapterService`, `JournalDayService`, `JournalEntryService`, `JournalReflectionService`, `JournalThreadService`, `JournalTodoService`, `UserService`다.

→ attachable 데이터축 서비스의 base 형태를 세는 방식은 관할이 달라 H2-A를 판정할 수 없다. 올바른 모집단에서는 **14/14 coverage**로 구조적 수렴이 강하게 확인된다.

### E2 — canonical modify 재구현 전수 대조 [확정]

`BaseAttachableService` 구현체의 `regist/modify/delete` override와 `BaseAttachableService.super.*` 호출, 공통 helper 호출을 대조했다.

| 경로 | canonical CRUD 재구현 | 공통 의미 | 판정 |
|---|---|---|---|
| `BoardPostService`, `JournalChapterService`, `JournalThreadService`의 override와 `JournalEntryService.regist` | 확장 | `BaseAttachableService.super.*` 위임 | canonical 경로 보존 |
| `JournalEntryService.modify/delete` | 직접 재구현 | tag/meta·managt·history helper를 직접 재호출 | 오케스트레이션 복제 `[MECHANISM]`; behavioral omission 아님 |
| `ScheduleService.modify` | 직접 재구현 | `BaseAttachableProcPostProcessor.afterWrite()` 누락 | canonical contract의 semantic hook omission `[DEFECT][확정]` |
| `UserService.modify` | 직접 재구현 | 서비스 자신이 정의한 `preModify(UserDto)` 누락 | canonical contract의 semantic hook omission `[DEFECT][확정]` |

**Schedule 증거 사슬** `[확정]`:

1. `ScheduleDto`는 `TagCmpstnModule`을 구현한다.
2. canonical `BaseAttachableService.modify()`는 저장 뒤 `afterWrite(postDto, updatedDto)`를 호출하고, 이 후처리가 `TagCmpstnModule`을 감지해 `TagProcService`를 실행한다.
3. `ScheduleService.modify()` override는 같은 수정 API의 entity 조회→매핑→저장을 직접 수행하지만 `afterWrite()`를 호출하지 않는다.
4. `ScheduleRestController`는 ID가 있는 일정 저장 요청을 이 override로 보낸다.

따라서 **이 경로로 태그 변경이 입력될 경우 canonical tag 후처리가 수행되지 않는다** `[확정]`. 현재 Vue 일정 폼은 태그 필드를 노출·전송하지 않는다 `[확정]`. 다른 API caller가 태그 입력을 실제 보내는지와 현행 사용자 영향은 `[미확인]`이다.

**User 증거 사슬** `[확정]`:

1. `UserService.preModify(UserDto)`는 `allowedIpListStr`이 비었을 때 `useAllowedIpYn="N"`과 빈 목록으로 정규화한다.
2. canonical `BaseAttachableService.modify()`는 매핑 전에 `preModify(postDto)`를 호출한다.
3. `UserService.modify()` override는 자체 `preModify()`를 호출하지 않고 바로 DTO를 entity에 매핑한다.
4. `UserRestController` 수정 endpoint는 이 override를 직접 호출한다.
5. 현재 Vue 관리자 화면은 `useAllowedIpYn`과 `allowedIpListStr`을 전송하고, IP 제한을 켠 채 빈 목록을 저장하지 못하게 하는 검증은 확인되지 않는다.

따라서 **정규화 hook은 해당 수정 경로에서 실행되지 않는다** `[확정]`. 특정 입력이 잘못된 DB 상태로 저장되는지는 실행 검증 전이므로 `[강한추정]`, 실제 사용자 장애 발생 여부는 `[미확인]`이다.

→ 공통 경로 이탈 자체는 결함이 아니다. **공통 경로 재구현 + canonical contract의 의미 누락**이 두 경로를 결함으로 만든다. `JournalEntryService`는 같은 재구현의 의미 보존 대조군이다.

### E3 — declarative capability와 canonical dispatch [확정]

`TagCmpstnModule`과 `MetaCmpstnModule`은 getter/setter/default 전달 메서드를 제공하는 declarative capability interface다. 실제 processor 선택은 `BaseAttachableProcPostProcessor`가 `instanceof TagCmpstnModule` / `MetaCmpstnModule`로 한 번 수행한다. `main`에서 `TagProcService.process`와 `MetaProcService.process`의 CRUD 후처리 dispatch는 이 helper에 모인다.

→ marker가 직접 처리 책임을 갖지 않는 것은 실패가 아니다. **capability 선언 → canonical processor 선택**을 연결하므로 H2-B를 지지한다. Schedule 결함은 이 dispatch가 잘못된 것이 아니라, 동일 CRUD 관할의 override가 dispatch hook 자체를 생략한 것이다.

### E4 — attachable ↔ journal concrete dependency [확정] / 경계 위반 [미확인]

`main`에서 attachable→journal import는 공통 후처리와 여러 서비스에 존재한다.

- `BaseAttachableProcPostProcessor` → `JournalPeriodModule`
- `CommentService` → `JournalDayResolvedGuard`
- `StateService` → `JournalContentOwnershipGuard`, `JournalDayResolvedGuard`
- `LifecycleService` → journal lifecycle cascade·ownership/resolved guard
- `RelatedContentService`/`RelatedContentQueryService` → journal entry/thread/day 구성요소
- tag/lifecycle cache adapter 구현 → journal cache/query 구성요소

journal도 attachable의 entity/service/type/capability를 폭넓게 import하므로 **양방향 concrete dependency가 존재한다** `[확정]`. 다만 attachable→journal을 금지한다고 명시한 package rule·ArchUnit test·spec은 확인되지 않았다. 따라서 이를 "역의존 위반"이나 "수렴 실패"로 판정할 근거는 `[미확인]`이다. `StateCacheUpdater`는 cache callback 하나를 inversion하지만 전체 package boundary의 증거는 아니다.

### E5 — 동일 policy 재선언의 좁은 반례 [확정]

기존에 함께 묶었던 아래 집합은 서로 다른 semantic proposition이다.

- state key 허용 정책
- state cache 대상
- related 지원 대상
- reflection lifecycle cascade 대상
- history strategy 대상

따라서 여러 `EnumSet<ContentType>`이 존재한다는 사실만으로 하나의 분산 정책이라 할 수 없다. 다만 다음 한 쌍은 같은 **cache-backed state 타입** proposition을 독립 선언한다.

- `StateService.validateStateCacheUpdaters()`의 `requiredTypes`
- `JournalStateCacheRegistry.STATE_CONTENT_TYPES`

두 집합은 `JOURNAL_CHAPTER`, `JOURNAL_DIARY`, `JOURNAL_DREAM`, `JOURNAL_REFLECTION` 4종으로 같고, 전자는 updater coverage를 검증하며 journal updater의 `supports()`는 후자를 사용한다. cache-backed 타입을 바꾸면 두 선언이 lockstep으로 맞아야 한다.

→ H2-D의 광범위한 "타입별 지원 정책 분산"은 기각한다. **state cache-backed 타입 4종의 좁은 policy duplication**만 확인된다 `[확정]`.

---

## 평결 (VERDICT)

| 가설 | 평결 | 근거 |
|---|---|---|
| H2-A — Content CRUD orchestration | **강한 수렴 + 경로 완전성 결함 2건** `[확정]` | 논리 CRUD 대상 14/14가 대응 `BaseAttachableService` 구현체를 가짐. canonical `modify` 재구현에서 `ScheduleService.afterWrite`, `UserService.preModify` semantic hook omission 확인. |
| H2-B — Capability declaration / processing | **지지** `[확정]` | `*CmpstnModule`이 declarative capability로 canonical tag/meta processor 선택에 실제 사용됨. |
| H2-C — Package dependency architecture | **구조 관측 / 위반 미결** | 양방향 concrete dependency `[확정]`; 금지 방향 계약 `[미확인]`. |
| H2-D — Capability policy ownership | **광범위 반증 기각 / 좁은 반례 확인** | 서로 다른 capability 집합은 비교 부적격. state cache-backed 4종만 동일 proposition 중복 `[확정]`. |

최종적으로 거시 명제의 **"attachable은 canonical 축으로 수렴했다"는 핵심은 공격을 견뎠다.** 구조적 coverage는 예상보다 강하다. 다만 다음 두 명제는 분리해야 한다.

> **Canonical coverage ≠ canonical path completeness.**

논리 CRUD 대상 전부에 canonical service가 존재해도, 동일 관할을 국소 재구현한 경로가 canonical contract를 완전하게 보존하는지는 별도 검증 대상이다. DIG-002가 확인한 failure mode는 "canonical structure의 부재"가 아니라 **canonical modify 주변 local reimplementation의 semantic hook omission**이다.

말할 수 있는 것:

- attachable 콘텐츠 CRUD는 14/14 service coverage로 구조적으로 강하게 수렴한다 `[확정]`.
- canonical modify contract의 semantic hook omission 2건이 존재한다 `[확정]`.
- JournalEntry의 재구현은 공통 의미를 보존하므로 override/reimplementation 자체는 결함이 아니다 `[확정]`.
- capability marker는 canonical dispatch를 지지한다 `[확정]`.
- 양방향 concrete dependency와 좁은 state cache policy duplication이 존재한다 `[확정]`.

말할 수 없는 것:

- Schedule omission이 현행 사용자 장애를 일으킨다: `[미확인]`.
- User omission으로 특정 잘못된 DB 상태가 실제 저장된다: 실행 검증 전 `[강한추정]`; 실제 장애 발생은 `[미확인]`.
- attachable→journal 의존이 금지된 아키텍처 위반이다: 금지 계약 `[미확인]`.
- 기능별 모든 `ContentType` 집합이 하나의 분산 정책이다: semantic equivalence가 없어 기각.

---

## 은행화 / 닫음

- `[CORRECTION]`: [REPO_STATIC_ANALYSIS.md](../REPO_STATIC_ANALYSIS.md) §2.5와 [REPO_HISTORY.md](../REPO_HISTORY.md) 통섭 10의 "부분 수렴" 정정을 철회하고, **14/14 구조적 수렴 + canonical path completeness 결함 2건**으로 갱신한다.
- `[DEFECT]`: `ScheduleService.modify()`의 `afterWrite()` 누락과 `UserService.modify()`의 `preModify()` 누락을 [REFACTOR_BACKLOG.md](../../REFACTOR_BACKLOG.md)에 등록한다. 실행 수정은 이 DIG 범위 밖이다.
- `[MECHANISM]`: local CRUD reimplementation은 (A) helper 의미 보존형(`JournalEntry`)과 (B) semantic hook omission형(`Schedule`, `User`)으로 갈린다.
- `[METHOD]`: Comparison Eligibility / Jurisdiction Gate를 방법론 invariant로 승격하고, 이 조사에서 난 false positive를 [REPO_HISTORY.md](../REPO_HISTORY.md) False Positive Log에 남긴다.
- H-directed(inadmissible)이므로 H1 전역 posterior는 움직이지 않는다. 사례 내부의 attachable 수렴 등급과 결함 판정만 갱신한다.

**닫음 게이트**: `main`의 직접 상속 선언, 14개 service generic, CRUD override/super/helper 호출, controller 도달 경로, 현재 Vue payload, package import, 동일 policy 집합을 전수 대조했다. H2-A~D를 뒤집을 미확인 질문은 위 영향도·금지 계약·실행 결과이며, 현재 구조 판정을 바꾸지 않으므로 DIG-002를 닫는다.

## 파생 가지 (프론티어로)

- **blind sweep 후보**: "canonical structure 주변 local reimplementation이 semantic omission의 주된 seam인가?" DIG-001·002가 후보를 만들었지만 두 사례뿐이며 DIG-002는 H-directed다. H와 무관한 기계표본/장애 유래 표본으로 검증하기 전에는 생성규칙으로 승격하지 않는다.
- DIG-003 후보는 기존 프론티어에 그대로 두며 자동 진행하지 않는다.

## 미확인 (남긴 [미확인])

- Schedule 수정 API에 태그 변경을 보내는 Vue 외 caller와 현행 사용자 영향.
- User 허용 IP 정규화 누락의 실제 DB 저장 결과와 사용자 장애 발생 여부.
- attachable→journal 방향을 금지하는 독립 package architecture 계약의 존재.
