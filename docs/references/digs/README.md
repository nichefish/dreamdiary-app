# DreamDiary 감식 발굴 프론티어 (digs/)

> 감식(미시고고학) 산출물 저장소. 방법론: [CODE_MICRO_ARCHAEOLOGY.md](../../CODE_MICRO_ARCHAEOLOGY.md).
> 목적: 이미 판 곳 재방문 방지 + 다음 표적 보존. "매번 새것 강제"가 아니다. 종료조건 = 기대수익 < 고증 비용.
> `digs/`는 실물 저장소이므로 실제 SHA·파일명·수치를 담는다(방법론 본문의 스택 비종속 규칙은 방법론 문서에만 적용).

## 봉인된 거시 명제 (피고인)

- **H1**: DreamDiary의 장기 변화 문법은 build-around보다 **build-through**이며, 기존 책임을 흡수·폐기하면서 canonical 구조로 수렴해 왔다.
  - 경쟁가설 H2: 시대적 지배 생성규칙은 없고 사례별 국소 대응의 집합이다.
  - 사전확률 봉인: H1 핵심 생존 확률 ≈ **80%** (미시감식 전). admissible 표본으로만 갱신.

## 닫힌 발굴

| DIG | 제목 | 태그 | 평결 | H1 처리 |
|-----|------|------|------|---------------|
| [DIG-001](DIG-001-service-impl-collapse.md) | service.impl 이중구조 폐기는 진짜 수렴인가 (`e5b2f81ec`) | `[CORROBORATED]` `[MECHANISM]` | H 지지 [확정]: 39개 impl 전면 삭제 + 39/39 대응 Service class화(전수) + 이후 생성규칙 변경 | H1 지목(Q3=No) → **독립 검증력 없음**. 단 사례 내부 등급은 상향(시대 Ⅱ 하위사건 [확정]). 전역 posterior 미변경 |
| [DIG-002](DIG-002-attachable-convergence-bypass.md) | attachable canonical 축에 우회 경로가 있는가 (`7d5553cb40`) | `[DEFECT]` `[CORRECTION]` `[MECHANISM]` `[METHOD]` | H2-A~D 분해: 논리 CRUD 14/14 canonical service coverage `[확정]`; canonical `modify` 재구현의 semantic hook omission 2건(Schedule `afterWrite`, User `preModify`) `[확정]`; JournalEntry 재구현은 의미 보존; 양방향 concrete dependency는 구조 관측; 동일 policy 중복은 state cache-backed 4종 한 쌍 | H1 지목(Q3=No) → **독립 검증력 없음**. 사례 내부 수렴 등급·결함 판정과 정적분석 §2.5·통섭 10을 갱신. H1 전역 posterior 미변경 |

## 열린 가설 (기대수익 순)

1. **DIG-003 후보 — ChatAIService → feature/ai/* + ChatOrchestrator 수렴의 질** (H-directed adversarial).
   - kill 질문: monolith→decomposition인가, monolith→smaller modules surrounding another god object인가. ChatOrchestrator(1,252 LOC)가 feature/ai 정책을 조정만 하는가 소유까지 하는가.
   - 가치: 방금 False Positive(ChatOrchestrator MISSING 오독)가 난 자리. "수렴의 질을 과대평가했나"를 직접 심문하되 DIG-002 결론을 이식하지 않는다.

2. **신규 가지 — canonical structure 주변 local reimplementation이 semantic omission의 주된 seam인가.**
   - DIG-002의 Schedule/User omission과 JournalEntry 의미 보존을 대조군으로 삼되, 두 결함만으로 생성규칙을 선언하지 않는다.
   - H1 관점 후보일 뿐이며 **blind validation sweep에서 H와 무관한 표본으로 검증해야 admissible**하다.

3. **(설계 필요) blind validation sweep — H1 전역 posterior를 실제로 움직이는 유일한 경로.**
   - H와 무관한 선정 규칙(무작위/기계표본/장애 유래)으로 사건을 뽑아 build-through vs build-around를 판정. admissible(Q3=Yes).
   - DIG-003(H-directed) *이후* 별도 설계. adversarial DIG와 섞지 않는다.
   - DIG-001(병렬 구조 제거)과 DIG-002(canonical coverage 주변 재구현 seam)가 후보를 만들었을 뿐 — sweep이 H1을 올릴지 내릴지 예단 금지.

## 각 발굴의 [미확인]

- DIG-001: `e5b2f81ec` 제거 동기 [미확인](코드로 불가). 39개 중 AuthService 1개만 A/B 직독(나머지는 패턴+잔존부재로 추론).
- DIG-002: Schedule 수정 API에 태그를 보내는 Vue 외 caller와 사용자 영향 [미확인]. User 허용 IP 정규화 누락의 실제 DB 저장 결과·사용자 장애 [미확인]. attachable→journal 금지 방향 계약 [미확인].

## 방법론 메모

- **선정독립성이 핵심 제약**: H1이 지목해 파는 것은 정당한 *탐색*이지만, 그 결과를 H1의 독립 *검증* 증거로 되먹이면 안 된다(계약 §4). admissible 표본(Q3=Yes: 장애·무작위·기계표본에서 뜬 것)만 거시 전역 posterior를 움직인다.
- **Comparison Eligibility / Jurisdiction Gate**: 경로·구조·표현을 비교하기 전에 동일 관할·역할·semantic proposition인지 입증한다. DIG-002 false positive를 `REPO_HISTORY.md` 로그에 은행화하고 방법론 invariant로 승격했다.
- 셋을 평균 내지 않는다. 한 표본을 독립 proposition으로 닫고, 필요 시 다음 피고인을 세운다.

### 방법론 재검토 후보 (CODE_MICRO_ARCHAEOLOGY.md 계약 §4) — 당장 문서 수정 보류

DIG-001에서 드러난 두 번째 방법론 약점(첫 번째는 measurement invariance):

> **`inadmissible`을 "거시 posterior를 0만큼 움직인다"로 쓰는 것이 너무 강할 수 있다.** selection bias 방지(독립 검증력 = 0)와 selected case 내부 evidence 무효화(조건부 정보량 = 0)는 **다른 명제**다. 후자까지 0으로 처리하면 selection bias를 막으려다 얻은 증거를 전부 폐기하는 반대쪽 과잉이 된다.

- 잠정 처리(DIG-001 채택): inadmissible 표본은 **H 전역 posterior는 안 움직이되, H가 의존하던 그 사례의 증거·해석 등급은 갱신**한다. 전역 확률을 `80→80`으로 못 박지 말고 "독립 validation update 없음 / 사례 내부 등급 상향"으로 기록.
- 계약 §4 문구("inadmissible 타일은 거시 posterior를 움직이지 못한다")를 "전역 posterior vs 사례 등급"으로 분리할지 재검토 필요. **당장 정본은 안 고친다** — 사례 1건으로 방법론을 뜯지 않고, DIG-002/003에서 같은 패턴이 반복되는지 본 뒤 승격 판단(measurement invariance 승격 때와 같은 절차).
