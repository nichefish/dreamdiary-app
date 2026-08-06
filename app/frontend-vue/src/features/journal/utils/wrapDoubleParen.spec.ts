import { describe, expect, it } from "vitest";
import {
  isFullyDoubleParenWrapped,
  wrapHtmlWithDoubleParen,
} from "./wrapDoubleParen";

describe("isFullyDoubleParenWrapped", () => {
  it("단순 ((...)) 는 true", () => {
    expect(isFullyDoubleParenWrapped("((본문))")).toBe(true);
  });

  it("안쪽 단일 괄호를 허용한다", () => {
    expect(isFullyDoubleParenWrapped("((a (b) c))")).toBe(true);
    expect(isFullyDoubleParenWrapped("(((중첩)))")).toBe(true);
  });

  it("부분·이중 감쌈은 false", () => {
    expect(isFullyDoubleParenWrapped("앞 ((본문))")).toBe(false);
    expect(isFullyDoubleParenWrapped("((a))((b))")).toBe(false);
    expect(isFullyDoubleParenWrapped("본문")).toBe(false);
  });
});

describe("wrapHtmlWithDoubleParen", () => {
  it("단일 문단을 감싼다", () => {
    expect(wrapHtmlWithDoubleParen("<p>본문</p>")).toEqual({
      html: "<p>((본문))</p>",
      changed: true,
    });
  });

  it("이미 감싼 문단은 멱등이다", () => {
    const html = "<p>((본문))</p>";
    expect(wrapHtmlWithDoubleParen(html)).toEqual({ html, changed: false });
    expect(wrapHtmlWithDoubleParen(wrapHtmlWithDoubleParen(html).html).changed).toBe(false);
  });

  it("여러 문단을 각각 감싼다", () => {
    expect(wrapHtmlWithDoubleParen("<p>A</p><p>B</p>")).toEqual({
      html: "<p>((A))</p><p>((B))</p>",
      changed: true,
    });
  });

  it("일부만 감싸진 경우 나머지만 감싼다", () => {
    expect(wrapHtmlWithDoubleParen("<p>((A))</p><p>B</p>")).toEqual({
      html: "<p>((A))</p><p>((B))</p>",
      changed: true,
    });
  });

  it("빈 본문은 변경 없다", () => {
    expect(wrapHtmlWithDoubleParen("")).toEqual({ html: "", changed: false });
    expect(wrapHtmlWithDoubleParen("<p></p><p><br></p>")).toEqual({
      html: "<p></p><p><br></p>",
      changed: false,
    });
  });

  it("인라인 HTML 은 평문으로 평탄화해 감싼다", () => {
    expect(wrapHtmlWithDoubleParen("<p>hello <strong>x</strong></p>")).toEqual({
      html: "<p>((hello x))</p>",
      changed: true,
    });
  });
});
