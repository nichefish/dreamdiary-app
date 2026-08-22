// @vitest-environment happy-dom
import { describe, expect, it } from "vitest";
import { htmlToPlainText } from "./htmlToPlainText";

/**
 * 복사 계약(docs/migration/journal/interaction-spec.md) 고정 테스트.
 * 복사는 저작한 소스 텍스트를 그대로 담는다 — 마커 보존·문단 보존·엔티티 디코드를 검증한다.
 * DOMParser 가 필요하므로 happy-dom 환경에서 실행한다.
 */
describe("htmlToPlainText — 복사 계약", () => {
  it("사용자가 입력한 마크다운 마커를 리터럴 텍스트로 보존한다", () => {
    expect(htmlToPlainText("<p>__밑줄__</p>")).toBe("__밑줄__");
    expect(htmlToPlainText("<p>!!경고!! ((알림)) ||강조||</p>")).toBe("!!경고!! ((알림)) ||강조||");
  });

  it("렌더 파생물(markdownContent)을 입력하면 마커가 소실된다 — 복사 소스로 쓰면 안 되는 이유", () => {
    // markdown() 이 __밑줄__ 를 <u>밑줄</u> 로 렌더한 결과를 넣으면 마커가 사라진다.
    expect(htmlToPlainText("<p><u>밑줄</u></p>")).toBe("밑줄");
  });

  it("문단 구조를 보존한다 — 문단 사이 빈 줄 1개", () => {
    expect(htmlToPlainText("<p>A</p>\n<p>B</p>")).toBe("A\n\nB");
  });

  it("빈 문단의 연속 개수를 보존한다", () => {
    expect(htmlToPlainText("<p>A</p><p></p><p>&nbsp;</p><p>B</p>")).toBe("A\n\n\n\nB");
  });

  it("br 로 표현된 TinyMCE 빈 문단을 보존한다", () => {
    expect(htmlToPlainText('<p>A</p><p><br data-mce-bogus="1"></p><p>B</p>')).toBe("A\n\n\nB");
  });

  it("본문 앞뒤와 단독 빈 문단을 보존한다", () => {
    expect(htmlToPlainText("<p>&nbsp;</p><p>A</p><p>&nbsp;</p>")).toBe("\n\nA\n\n");
    expect(htmlToPlainText("<p>&nbsp;</p>")).toBe("\n");
  });

  it("HTML 엔티티를 화면과 동일하게 디코드한다", () => {
    expect(htmlToPlainText("<p>&gt; 인용 &amp; &#39;</p>")).toBe("> 인용 & '");
  });

  it("br 은 개행으로 환원한다", () => {
    expect(htmlToPlainText("<p>a<br>b</p>")).toBe("a\nb");
  });

  it("div 블록 경계를 개행으로 환원한다", () => {
    expect(htmlToPlainText("<div>A</div><div>B</div>")).toBe("A\nB");
  });

  it("목록 항목 경계를 개행으로 환원한다", () => {
    expect(htmlToPlainText("<ul><li>A</li><li>B</li></ul>")).toBe("A\nB");
  });

  it("hr 은 ------ 로 환원한다", () => {
    expect(htmlToPlainText("<p>위</p><hr><p>아래</p>")).toBe("위\n\n------\n\n아래");
  });

  it("각 줄 앞뒤 공백을 제거한다", () => {
    expect(htmlToPlainText("<p>  가운데  </p>")).toBe("가운데");
  });
});
