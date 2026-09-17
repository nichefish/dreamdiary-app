# DIG-001 — service.impl 이중구조 폐기는 진짜 수렴인가

> 저장소: DreamDiary `main` (조사 tip `7d5553cb4`, 2026-08-27). 대상 커밋 `e5b2f81ec` (2025-11-23, "- service.impl 구조 제거").
> 방법론: [CODE_MICRO_ARCHAEOLOGY.md](../../CODE_MICRO_ARCHAEOLOGY.md). 거시 지도: [REPO_HISTORY.md](../REPO_HISTORY.md) 시대 Ⅱ 하위사건 / [REPO_STATIC_ANALYSIS.md](../REPO_STATIC_ANALYSIS.md).

**태그**: `[CORROBORATED]` `[MECHANISM]`

---

## 발단 (SEED)

거시 명제 **H1**: DreamDiary의 장기 변화 문법은 build-around(새 추상층을 계속 덧댐)보다 **build-through**이며, 기존 책임을 흡수·폐기하면서 canonical 구조로 수렴해 왔다.

출처는 REPO_HISTORY 시대 Ⅱ 하위사건("2025-11 service.impl 제거")과 통섭 2("흡수·폐기 패턴"). **출처는 신뢰도를 주지 않는다** — `[확정]`으로 적혀 있어도 DIG에서 다시 피고인이다.

이 사건을 첫 표본으로 고른 이유: 반증조건이 가장 날카롭다. "interface/impl 이중구조를 정말 없앴는가, 아니면 `Impl` suffix만 지우고 간접계층은 남았는가"는 코드 A/B 비교로 이진에 가깝게 떨어진다.

**선정독립성 (admissibility, 계약 §3)**: 이 표본은 거시가설 H1이 지목했다. `Q1`(선정 전 H 존재)=Yes · `Q2`(H가 선정확률↑)=Yes · `Q3`(H 없어도 같은 규칙으로 선택?)=**No** → **inadmissible = 탐색 전용.** 이 DIG는 서사·기록에 쓰되 **거시 posterior를 직접 움직이지 못한다**(계약 §4). H1의 검증 갱신은 admissible 표본(장애·무작위·기계표본에서 뜬 것)에서만 한다.

---

## 가설

**H(DIG-001)**: `e5b2f81ec`는 자체 정의한 `XxxService(interface) + XxxServiceImpl(class)` 이중 간접계층을 **실제로 제거**하고 단일 구현 클래스로 흡수했다(build-through의 실례).

**반증조건 (무엇이 관찰되면 H가 틀리는가)**:
1. 삭제가 일부 서비스에만 적용됐다(전면 아님). → 부분 정리.
2. `XxxServiceImpl.java`만 삭제되고 `XxxService`가 여전히 `interface`로 남아 다른 곳에 구현이 있다. → suffix rename에 불과.
3. 제거 이후 신규 도메인 서비스가 다시 `interface + impl`로 생성된다. → 생성규칙 불변(일회성 청소였을 뿐).

---

## 고증 (1차 사료)

### E1 — 삭제 규모 [확정] (도구: `git show --diff-filter=D --name-only`)
`e5b2f81ec`에서 삭제된 `.java` = **39개, 전부 `/service/impl/` 경로의 `*ServiceImpl.java`.** impl 아닌 삭제 0.
도메인 횡단: auth(Auth/AuthRole/VerificationCode), admin(Menu/LgnPolicy), board(BoardDef/Notice/BoardPost), chat(ChatMsg), jrnl(Day/DayCal/DayTag/Diary/Dream/Entry/Sbjct/Sumry/Todo + Tag 변형), user(User/UserMy/UserReqst), extension/cd(ClCd/DtlCd), clsf(Comment 등), adapter(Jandi/Kasi), vcatn(VcatnPapr).
→ **반증조건 1 기각**: 부분이 아니라 전 도메인 전면.

### E2 — interface→class 전환 [확정] (도구: A/B 두 시점 `git show <sha>:<path>` 직독 + 39개 전수 기계검사)

**심층 A/B — 표본 `AuthService`** (경로: `e5b2f81ec` 시점 `src/main/java/io/nicheblog/dreamdiary/auth/security/service/`; 당시 백엔드가 리포 루트, `app/backend/` 이동 전):
- **BEFORE (`e5b2f81ec~1`)**: `public interface AuthService extends UserDetailsService` (73줄, 주석 "서비스 인터페이스"). 별도 `service/impl/AuthServiceImpl.java` (163줄, `@Service` class)가 구현.
- **AFTER (`e5b2f81ec`)**: `@Service("authService") @RequiredArgsConstructor @Log4j2 public class AuthService implements UserDetailsService` (157줄). 필드 주입(`userRepository`·`authRoleRepository`·mapstruct)이 직접 들어옴. 주석 "서비스 인터페이스" → "서비스 모듈". after(157) ≈ before impl(163) — 구현 본문이 interface 자리로 흡수됨.

**전수 기계검사 — 39개 대응 Service 선언** (도구: 삭제된 각 `service/impl/XxxServiceImpl.java` → `service/XxxService.java`의 `e5b2f81ec` 시점 선언부 grep):
- **39/39 전부 `public class`.** interface 잔존 = **0**. MISSING/매칭실패 = 0.
- 즉 AuthService의 interface→class 전환은 표본 특수사례가 아니라 **삭제된 39개 전부에 일관 적용**됐다. E1(삭제)과 E2(대응 class화)의 연결이 개별 관측으로 봉합됨(관계 명제가 아니라 전수 관측).
→ **반증조건 2 기각 [확정]**: `Impl` suffix만 지운 게 아니라, 자체 정의한 interface 추상 자체를 39개 전 서비스에서 제거하고 단일 class로 합쳤다. 잔존 `implements UserDetailsService`(AuthService)는 Spring 프레임워크 계약이지 자체 간접계층이 아니다.

### E3 — 제거 이후 생성규칙 [확정] (도구: `main` 트리 `ls-tree` + `git grep`)
조사 tip `7d5553cb4` 기준:
- `*ServiceImpl.java` 잔존 = **3개뿐**: `auth/oauth2/.../OAuth2UserServiceImpl`, `infrastructure/cache/.../EhCacheEvictServiceImpl`, `EhCacheWarmupServiceImpl`. 셋 다 프레임워크/SPI 경계(Spring `OAuth2UserService` 확장, 캐시 워밍업 SPI)이지 도메인 서비스 이중구조가 아니다.
- 자체 `Service.java` 중 `interface` 선언 = `Base*Service` 7종(`BaseAttachableService`·`BaseDto/EntityReadable/WritableService`·`BaseSortableService`·`BaseMultipartWritableService`) + `CacheEvict/WarmupService` 2종. Base 계열은 default 메서드로 CRUD를 담는 **template-method base**(이중구조 아님), 뒤 2개는 위 SPI의 인터페이스.
- `e5b2f81ec` 이후 태어난 도메인 서비스(state 2026-01, reflection 2026-08 등)에 `XxxService(interface)+XxxServiceImpl(class)` 형태가 **재출현하지 않음.**
→ **반증조건 3 기각**: 생성규칙이 실제로 바뀌었다. 이후 신규 도메인 서비스는 단일 `@Service` class로 태어난다.

### REC / WORLD 분리 (계약 §1)
- `REC[확정]`: 커밋 메시지에 "service.impl 구조 제거"라고 기록됨.
- `WORLD[확정]`: 실제로 39개 impl 삭제 + interface→class 전환이 코드에서 확인됨. REC와 WORLD가 일치(불일치 사료 없음).

---

## 평결 (VERDICT)

**H(DIG-001) 지지 [확정].** 세 반증조건이 모두 기각됐다: (1) 전 도메인 전면, (2) interface 추상 자체 제거·단일 class 흡수, (3) 이후 생성규칙 변경(재출현 없음).

**H1(거시)에 대해**: 이 사건은 build-through/흡수·폐기의 **실례로 corroborate**된다 — 노드(각 관찰) `[확정]`, H1으로의 엣지는 **구조적추론**(시간순·전면성·이후 생성규칙 변경이 한 설명으로 수렴) → 엣지 상한 `[강한추정]`.
- **선정독립성 처리 (수정)**: 이 표본은 H1이 지목했으므로 **독립 검증력(independent validation weight) = 없음**. 즉 거시가 이미 build-through 사례로 쓰던 것을 다시 그 근거로 되먹이지 않는다(순환 방지, 계약 §4).
- **그러나 "독립 검증력 없음"과 "이 표본에서 얻은 조건부 정보량 = 0"은 다른 명제다.** 조사 전 살아있던 반증 가능성(suffix만 제거 / 부분만 / 이후 재출현)이 셋 다 kill됐고, 특히 39개 전수 class화·이후 생성규칙 변경은 거시사가 *암묵적으로 전제하던* 이 사건의 성격을 강하게 검증했다. 그래서 이 DIG가 갱신하는 것은 **H1의 전역 posterior가 아니라, H1이 의존하던 이 사례의 증거·해석 등급**이다(거시 시대 Ⅱ 하위사건 항의 등급을 `[확정]`으로 올림).
- 기록 방식: H1 전역 확률을 `80→80`으로 정밀히 못 박지 않는다. **"독립 validation update 없음, 사례 내부 등급은 상향"**으로 남긴다. H1 전역 posterior를 실제로 움직이는 것은 별도 **blind validation sweep**(H와 무관한 선정 규칙)의 몫이다.

무엇까지 말할 수 있나:
- 말할 수 있다: 이 한 사건에서 자체 interface 간접계층이 제거됐고, 이후 그 형태가 재생성되지 않았다(생성규칙 변경). [확정]
- 말할 수 없다: **왜** 제거했는가(동기). 커밋 메시지는 액션만 적고 동기를 안 준다 — [미확인]. 성능·인지부하·취향 어느 것도 코드로 확정 불가([관측층위 비약 금지]: behavior→의도 전이에 사료 없음).
- 말할 수 없다: 이 사건 하나로 H1 전체(저장소의 지배적 생성규칙)를 확정하는 것. 단일 admissible-아님 표본.

---

## 은행화 / 닫음

- 거시 역링크: [REPO_HISTORY.md](../REPO_HISTORY.md) 시대 Ⅱ 하위사건 "2025-11 service.impl 제거" — 이 DIG가 "interface 추상 자체 제거 + 이후 생성규칙 변경"까지 `[확정]`으로 보강. `[CORROBORATED]`.
- `[MECHANISM]`: DreamDiary 도메인 서비스 생성규칙 = 단일 `@Service` class (interface 간접계층 없음). template-method는 `Base*Service`로 별도 수렴.
- 이 DIG는 inadmissible이므로 REPO_HISTORY/STATIC의 수치·서사를 **정정하지 않는다**(정정 대상 사실 오류 없음). corroboration 링크만 남긴다.

## 파생 가지 (프론티어로)

- **DIG-002 후보**: attachable canonical 축의 **우회 경로** 심문(H1 표본2). BaseAttachableService를 우회하는 write path / journal에 복제된 정책 / ContentType switch 재전개 / `*CmpstnModule`이 실책임 수렴인가 명목 marker인가. — admissibility 주의(H1이 지목 → inadmissible 예상).
- **DIG-003 후보**: `ChatAIService → feature/ai/* + ChatOrchestrator(1,252 LOC)` 수렴의 **질**. monolith→decomposition인가, monolith→smaller modules surrounding another god object인가. ChatOrchestrator가 feature/ai 정책을 조정만 하는가 소유까지 하는가.

## 미확인 (남긴 [미확인])

- e5b2f81ec 제거의 **동기**: [미확인]. (성능/인지부하/취향 — 코드로 불가. behavior→의도 전이에 사료 없음.)
- (봉합됨) "39개 개별 확인 안 함" seam은 E2 전수 기계검사로 닫힘 — 39/39 class, interface 잔존 0. H `[확정]` 유지 정당.
- AuthService 외 38개의 BEFORE가 각각 정확히 `interface`였는지(→class 전환의 완전한 A/B)는 삭제 패턴(E1: 대응 impl 존재)으로 강하게 추론되나 38개 BEFORE 선언을 개별 직독하지는 않음. AFTER는 39개 전수 class로 확정. [BEFORE 개별: 강한추정 / AFTER 전수: 확정]
