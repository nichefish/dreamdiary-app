# 현재 시스템의 문제점 — DreamDiary

역사·수치는 [REPO_HISTORY.md](REPO_HISTORY.md) · [REPO_STATIC_ANALYSIS.md](REPO_STATIC_ANALYSIS.md)가 담당한다. 이 문서는 그 원천이 **지금(`main`)** 어떤 성격의 문제로 살아있는지만 적는다.

기준: **`main`** HEAD `7d5553cb4` (2026-08-27). 방법론의 세 겹(Ⅰ 동작 / Ⅱ 구조·절차 / Ⅲ 추상화).

체크아웃 `dev_*`의 미머지 기능은 이 표에 넣지 않는다.

---

## 진단 매트릭스

| # | 문제 | 무게중심 | 근거 (`main`) | 비고 |
|---|---|:---:|---|---|
| 1 | Entry가 정책 수렴점 | **Ⅲ** | `JournalEntryService` 778 LOC | 길이가 아니라 경계. Reflection은 전용 축 |
| 2 | `journal.ts` 타입 창고 | **Ⅲ** | 837 LOC | modal 파사드(142)는 구 God가 아님. 질량은 `journalModalEntry`(378) 등으로 이동 |
| 3 | 웹 React 축 지위 불명 | **Ⅱ** | `frontend-react` 24파일 | RN은 다른 표면. 확인 전 삭제·확장 금지 |
| 4 | 인코딩 재발 조건 | **Ⅱ** | 2026-07-25 복원 · 게이트 존재 | 편집 경로가 cp949면 재발 |
| 5 | Chat/AI 재팽창 여지 | **Ⅲ** | Orchestrator 1,252 · Person 971 | 구 ChatAIService는 `main`에서 해소. Orchestrator는 재분해 안 됨(수렴점). 직후 재분해는 중복 전선 |

Ⅰ 동작 전수는 **미측정**.

저널 상태 read model 조립 비용(복수 상태원+사용자별 projection 합성)은 **여기 넣지 않는다** `[확정]`: write canonical은 DB `StateEntity`+lifecycle로 단일하고, 캐시는 DB에서 재생성되는 파생물이다. 이는 read 경로 이해·추적 비용이지 설계 결함이 아니다(정적분석 §2.6 참조).

해소되어 이 표에 두지 않음 `[확정]`: `ChatAIService` 단일 파일 · 단일 `journalModal.ts` 1,166 LOC · FreeMarker MVC 화면 · “6·7월 커밋 0”.

---

## 겹별 읽기

### Ⅰ 동작
- 본 세션은 트렁크 구조 스냅샷만.

### Ⅱ 구조·절차
- 1.0 전 Flyway 증분 없음. full schema가 선언 SSOT.
- 고고학 정본은 `main`. 작업 브랜치를 저장소 상태로 읽으면 Ⅱ가 오염된다.
- 웹 React는 진입점 혼선 가능.

### Ⅲ 추상화
- Entry = 정당 허브이되 정책을 계속 빨아들이면 파일명상 모듈이 거짓이 된다.
- 채팅은 접힌 뒤에도 오케스트레이터가 두껍다.

---

## 하지 말 것

- Entry·Orchestrator 즉시 분해를 작업 티켓으로 승격하지 말 것.
- `source-archaeology.md`를 다시 만들지 말 것.
- 체크아웃(`dev_0.30.0` 등) 전용 미머지 변경을 이 진단에 섞지 말 것.

*산출: 2026-08-14. 2차 재조사 갱신: 2026-08-30. 원천: `main` `7d5553cb4`.*
