#!/usr/bin/env python3
"""
AGENTS.md (SSOT) 내용을 CLAUDE.md, .cursor/rules/cursor.mdc 에 복사한다.
UTF-8 BOM 없음으로 기록하며, 한글 보존을 보장한다.
"""

import shutil
from pathlib import Path


def main() -> None:
    root = Path(__file__).resolve().parent.parent
    ssot = root / "AGENTS.md"

    targets = [
        root / "CLAUDE.md",
        root / ".cursor" / "rules" / "cursor.mdc",
    ]

    if not ssot.exists():
        print(f"[sync] SSOT not found: {ssot}")
        return

    for target in targets:
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(ssot, target)
        print(f"[sync] {ssot.name} -> {target.relative_to(root)}")

    print("[sync] Done.")


if __name__ == "__main__":
    main()
