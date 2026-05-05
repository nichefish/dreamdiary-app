from __future__ import annotations

import argparse
import sys
from pathlib import Path

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8", errors="backslashreplace")


TEXT_EXTENSIONS = {
    ".css",
    ".ftlh",
    ".gradle",
    ".hbs",
    ".html",
    ".java",
    ".js",
    ".json",
    ".jsx",
    ".kt",
    ".kts",
    ".md",
    ".properties",
    ".scss",
    ".sql",
    ".ts",
    ".tsx",
    ".txt",
    ".vue",
    ".xml",
    ".yaml",
    ".yml",
}

EXCLUDED_DIRS = {
    ".git",
    ".gradle",
    ".idea",
    ".vscode",
    "build",
    "dist",
    "node_modules",
    "target",
}

EXCLUDED_PATH_PARTS = {
    "src/main/resources/static/js/vendor",
    "src/main/resources/static/lib",
    "src/main/resources/static/metronic",
    "static/js/vendor",
    "static/lib",
    "static/metronic",
}

MOJIBAKE_MARKERS = (
    "\ufffd",
    "\u00c2",
    "\u00c3",
    "\u00cc",
    "\u00cd",
    "\u00cf",
    "\u00ea",
    "\u00eb",
    "\u00ec",
    "\u00ed",
    "\u8e42\u0080",
    "\ubcc0\udcea\udcb2",
    "\udceb\udca1",
    "?\uc4d2",
    "?\ub301",
    "?\udcb4",
)


def is_excluded(path: Path) -> bool:
    parts = set(path.parts)
    if parts & EXCLUDED_DIRS:
        return True

    normalized = path.as_posix()
    return any(part in normalized for part in EXCLUDED_PATH_PARTS)


def iter_text_files(root: Path) -> list[Path]:
    return [
        path
        for path in root.rglob("*")
        if path.is_file()
        and path.suffix.lower() in TEXT_EXTENSIONS
        and not is_excluded(path)
    ]


def has_mojibake(text: str) -> tuple[int, str] | None:
    for line_no, line in enumerate(text.splitlines(), 1):
        if any(marker in line for marker in MOJIBAKE_MARKERS):
            return line_no, line.strip()
    return None


def main() -> int:
    parser = argparse.ArgumentParser(description="저장소 텍스트 파일 인코딩을 검사합니다.")
    parser.add_argument("--root", default=".", help="검사할 저장소 루트 경로")
    parser.add_argument(
        "--check-mojibake",
        action="store_true",
        help="대표적인 깨진 한글 마커도 함께 검사합니다.",
    )
    args = parser.parse_args()

    root = Path(args.root).resolve()
    failures: list[str] = []

    for path in iter_text_files(root):
        rel = path.relative_to(root).as_posix()
        data = path.read_bytes()

        if data.startswith(b"\xef\xbb\xbf"):
            failures.append(f"{rel}: UTF-8 BOM이 있습니다.")
            continue

        try:
            text = data.decode("utf-8")
        except UnicodeDecodeError as exc:
            failures.append(f"{rel}: UTF-8로 읽을 수 없습니다. {exc}")
            continue

        if args.check_mojibake:
            hit = has_mojibake(text)
            if hit is not None:
                line_no, line = hit
                failures.append(f"{rel}:{line_no}: 깨진 한글 의심 마커가 있습니다. {line[:120]}")

    if failures:
        print("인코딩 검사 실패:")
        for failure in failures:
            print(f"- {failure}")
        return 1

    print("인코딩 검사 통과")
    return 0


if __name__ == "__main__":
    sys.exit(main())
