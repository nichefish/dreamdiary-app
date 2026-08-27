// @vitest-environment happy-dom
import { describe, expect, it } from "vitest";
import {
  appendReflectionsToCopyText,
  JOURNAL_COPY_BLOCK_SEPARATOR,
  JOURNAL_COPY_LINE_BREAK,
} from "./journalCopyReflection";

const FIXTURE_PRIMARY_TEXT = "기준 본문";
const FIXTURE_OPEN_REFLECTION = "첫 번째 해석";
const FIXTURE_PENDING_REFLECTION = "두 번째 해석";

/**
 * 저널 복사의 공통 리플렉션 경계 계약을 고정한다.
 * 엔트리·챕터·검색·스레드는 같은 formatter를 사용하며 각 리플렉션 사이에 빈 줄 1개를 둔다.
 */
describe("journalCopyReflection — 공통 복사 포맷", () => {
  const reflections = [
    { content: `<p>${FIXTURE_OPEN_REFLECTION}</p>`, lifecycle: { lifecycleKey: "OPEN" } },
    { content: `<p>${FIXTURE_PENDING_REFLECTION}</p>`, lifecycle: { lifecycleKey: "PENDING" } },
  ];

  it("본문과 각 리플렉션 사이에 CRLF 빈 줄 1개를 둔다", () => {
    expect(appendReflectionsToCopyText(FIXTURE_PRIMARY_TEXT, reflections, "full")).toBe(
      [FIXTURE_PRIMARY_TEXT, FIXTURE_OPEN_REFLECTION, FIXTURE_PENDING_REFLECTION]
        .join(JOURNAL_COPY_BLOCK_SEPARATOR),
    );
  });

  it("본문과 리플렉션 내부의 LF를 CRLF로 통일한다", () => {
    const result = appendReflectionsToCopyText("기준 1\n기준 2", [
      { content: "<p>해석 1<br>해석 2</p>" },
    ], "full");

    expect(result).toBe(
      `기준 1${JOURNAL_COPY_LINE_BREAK}기준 2${JOURNAL_COPY_BLOCK_SEPARATOR}`
      + `해석 1${JOURNAL_COPY_LINE_BREAK}해석 2`,
    );
    expect(result.replace(/\r\n/g, "")).not.toContain("\n");
  });

  it("보류 제외와 본문만 모드를 같은 포맷 경계에 적용한다", () => {
    expect(appendReflectionsToCopyText(FIXTURE_PRIMARY_TEXT, reflections, "no-pending")).toBe(
      `${FIXTURE_PRIMARY_TEXT}${JOURNAL_COPY_BLOCK_SEPARATOR}${FIXTURE_OPEN_REFLECTION}`,
    );
    expect(appendReflectionsToCopyText(FIXTURE_PRIMARY_TEXT, reflections, "body")).toBe(FIXTURE_PRIMARY_TEXT);
  });
});
