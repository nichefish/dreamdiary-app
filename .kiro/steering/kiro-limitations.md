---
description: Kiro 환경 제약 사항
inclusion: auto
---

## Kiro 터미널 제약 — 금지 작업 목록

### 금지: Interactive Editor를 호출하는 Git 명령

Kiro의 `execute_pwsh`는 단일 터미널 세션을 재사용하며, interactive editor(vim/nano 등)가 뜨면 세션 전체가 블로킹된다.
복구 불가 상태에 빠지므로 아래 명령은 **절대 실행하지 않는다.**

| 금지 명령 | 이유 |
|-----------|------|
| `git rebase -i` | squash 메시지 편집 시 editor 호출 |
| `git commit` (메시지 플래그 없이) | editor 호출 |
| `git merge --edit` | editor 호출 |
| `git tag -a` (메시지 플래그 없이) | editor 호출 |

### 허용 조건

- `git commit -m "메시지"` — 에디터 안 뜸, 허용
- `$env:GIT_EDITOR = "true"` 설정 후 실행 — 허용하나 **rebase -i 재배치(reorder)는 충돌 위험으로 비권장**

### 대안

- **Interactive rebase / squash** → 사용자가 직접 수행하거나 Cursor에 위임
- **커밋 메시지 작성** → 항상 `-m` 플래그 사용
- **커밋 히스토리 분석/제안** → Kiro에서 수행 가능, 실행만 사용자에게 위임

---

## Kiro Permissions — 승인 팝업 통제

### 구조

Kiro는 Autopilot 모드와 별개로 **permissions.yaml** 규칙 체계를 가진다.
Autopilot은 "자율 실행 모드"(큰 틀), permissions.yaml은 "도구별 세부 허용/차단"(세부 통제).

- permissions.yaml에 `allow` 규칙이 없는 동작은 Autopilot이어도 **매번 승인 팝업**이 뜬다.
- 설정 파일 경로: `~/.kiro/settings/permissions.yaml`
- Kiro는 자기 설정 경로(`~/.kiro/settings/`)에 대한 쓰기를 하드 차단하므로, 이 파일은 **사용자가 직접** 생성/수정해야 한다.

### 현재 적용 규칙 요약

| capability | match 패턴 | effect |
|---|---|---|
| fs_read | (전체) | allow |
| fs_write | app/**, docs/**, scripts/**, src/**, config/**, templates/**, static/** | allow |
| shell | git *, cmd.exe *, gradlew*, python *, py *, npm *, npx * | allow |
| mcp | (전체) | allow |

### 하드코딩 불변 규칙 (override 불가)

| 경로/동작 | 효과 |
|---|---|
| `~/.kiro/settings/`, `.kiro/settings/` 쓰기 | 항상 deny |
| `.git/**` 쓰기 | 항상 ask |
| `.kiro/hooks/**`, `.kiro/agents/**` 쓰기 | 항상 ask |

---

## Pre-commit Hook과 Kiro 충돌

### 현상

`.git/hooks/pre-commit`이 존재하면 `git commit` 실행 시 hook이 트리거된다.
Kiro의 도구 승인 흐름이 hook의 자식 프로세스까지 승인 대상으로 잡으면서 순환 루프 → "Permission flow exceeded 20 approval rounds" 에러.

### 원인

현재 pre-commit hook: `python scripts/check_agent_rules_sync.py`
→ Kiro가 git commit 실행 → pre-commit이 python 프로세스 생성 → 승인 요청 → 재귀 → 20회 초과 차단

### 대응

- Kiro에서 커밋 시: `git commit --no-verify -m "메시지"` 사용
- 또는 pre-commit hook에 환경변수 분기 추가 (예: `KIRO_AGENT=1`이면 skip)
- `check_agent_rules_sync.py` 검증은 커밋 전 별도 단계로 수동 실행
