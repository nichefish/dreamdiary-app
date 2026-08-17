/**
 * TinyMCE HTML을 클립보드용 소스 텍스트로 변환한다.
 *
 * 복사 계약(docs/migration/journal/interaction-spec.md): 복사는 저작한 소스 텍스트를
 * 그대로 담는다. TinyMCE 구조 태그(<p>·<br>·<hr>)만 개행으로 환원하고, 이름·10진수·
 * 16진수 HTML 엔티티를 화면 렌더링과 동일하게 한 번 디코딩하며, 그 외 텍스트(사용자가
 * 입력한 마크다운 마커 포함)는 그대로 둔다. 문단 구조는 보존한다(문단 사이 빈 줄 1개 유지).
 *
 * 입력 소스는 항상 저작 원문 content 여야 한다. 렌더 파생물 markdownContent 는 마커가
 * 이미 태그로 소비돼 왕복 복원이 불가하므로(손실) 이 함수의 입력으로 쓰지 않는다.
 */
export function htmlToPlainText(html: string): string {
  const normalizedHtml = html
    .replace(/<\s*hr\b[^>]*\/?>/gi, "\n------\n")
    .replace(/<\s*br\s*\/?>/gi, "\n")
    /* <p> 와 </p> 모두 줄바꿈으로 — 레거시 동일 */
    .replace(/<\s*\/?p[^>]*>/gi, "\n");
  const document = new DOMParser().parseFromString(normalizedHtml, "text/html");

  return (document.body.textContent ?? "")
    /* 각 줄 앞뒤 공백 제거 */
    .split("\n").map((line) => line.trim()).join("\n")
    /* 문단 구분 보존: 연속 빈 줄은 단일 빈 줄(\n\n)로 정규화 */
    .replace(/\n{2,}/g, "\n\n")
    .trim();
}
