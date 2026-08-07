#!/usr/bin/env python3
"""
pre-commit hook: AGENTS.md (SSOT) vs CLAUDE.md / .cursor/rules/cursor.mdc 동기화 검증.

AGENTS.md 내용과 CLAUDE.md, .cursor/rules/cursor.mdc 가 byte-identical 인지 확인한다.
다르면 exit 1 로 커밋을 차단하고, 어떤 파일이 다른지 안내한다.
"""

import sys
from pathlib import Path


def main() -> int:
    root = Path(__file__).resolve().parent.parent
    ssot = root / "AGENTS.md"

    targets = [
        root / "CLAUDE.md",
        root / ".cursor" / "rules" / "cursor.mdc",
    ]

    if not ssot.exists():
        print(f"[agent-rules-sync] SSOT not found: {ssot}")
        return 1

    ssot_content = ssot.read_bytes()
    failures = []

    for target in targets:
        if not target.exists():
            failures.append(f"  MISSING: {target.relative_to(root)}")
            continue
        if target.read_bytes() != ssot_content:
            failures.append(f"  DIFFERS: {target.relative_to(root)}")

    if failures:
        print("[agent-rules-sync] AGENTS.md (SSOT) 와 다음 파일이 동기화되지 않았습니다:")
        for f in failures:
            print(f)
        print()
        print("해결: AGENTS.md 를 수정한 뒤, 아래 명령으로 동기화하세요:")
        print("  python scripts/sync_agent_rules.py")
        return 1

    return 0


if __name__ == "__main__":
    sys.exit(main())
