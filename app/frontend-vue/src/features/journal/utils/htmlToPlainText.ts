/**
 * TinyMCE HTML을 클립보드용 소스 텍스트로 변환한다.
 *
 * 복사 계약(docs/migration/journal/interaction-spec.md): 복사는 저작한 소스 텍스트를
 * 그대로 담는다. TinyMCE 구조 태그(<p>·<br>·<div>·<li>·<hr>)만 개행으로 환원하고, 이름·10진수·
 * 16진수 HTML 엔티티를 화면 렌더링과 동일하게 한 번 디코딩하며, 그 외 텍스트(사용자가
 * 입력한 마크다운 마커 포함)는 그대로 둔다. 문단 구조는 보존한다(문단 사이 빈 줄 1개 유지).
 *
 * 입력 소스는 항상 저작 원문 content 여야 한다. 렌더 파생물 markdownContent 는 마커가
 * 이미 태그로 소비돼 왕복 복원이 불가하므로(손실) 이 함수의 입력으로 쓰지 않는다.
 */
export function htmlToPlainText(html: string): string {
  const sourceDocument = new DOMParser().parseFromString(html, "text/html");
  let emptyParagraphToken = "\uE000";
  while (html.includes(emptyParagraphToken)) emptyParagraphToken += "\uE001";

  /*
   * TinyMCE 의 빈 문단(<p>&nbsp;</p>·<p><br></p> 포함)은 사용자가 만든 개행이다.
   * 구조 태그 정규화 전에 토큰으로 분리해 연속 개수와 본문 앞뒤 위치를 보존한다.
   */
  sourceDocument.body.querySelectorAll("p").forEach((paragraph) => {
    const plain = (paragraph.textContent ?? "").replace(/\u00a0/g, " ").trim();
    const hasTextlessContent = paragraph.querySelector("img,table,hr,iframe,video,audio,object,embed") !== null;
    if (!plain && !hasTextlessContent) {
      paragraph.replaceWith(sourceDocument.createTextNode(emptyParagraphToken));
    }
  });

  const normalizedHtml = sourceDocument.body.innerHTML
    .replace(/<\s*hr\b[^>]*\/?>/gi, "\n------\n")
    .replace(/<\s*br\s*\/?>/gi, "\n")
    /* 레거시·서버 htmlToText 계약: div·li 시작은 한 줄 경계다. 닫는 태그까지 치환하면 중첩 구조에 빈 줄이 중복된다. */
    .replace(/<\s*(?:div|li)\b[^>]*>/gi, "\n")
    /* <p> 와 </p> 모두 줄바꿈으로 — 레거시 동일 */
    .replace(/<\s*\/?p[^>]*>/gi, "\n");
  const document = new DOMParser().parseFromString(normalizedHtml, "text/html");

  const normalizedText = (document.body.textContent ?? "")
    /* 각 줄 앞뒤 공백 제거 */
    .split("\n").map((line) => line.trim()).join("\n")
    /* 구조 태그가 중복 생성한 개행만 단일 빈 줄(\n\n)로 정규화한다. 빈 문단 토큰은 이 축약에서 보호된다. */
    .replace(/\n{2,}/g, "\n\n")
    .trim();

  return normalizedText.split(emptyParagraphToken).join("\n");
}
