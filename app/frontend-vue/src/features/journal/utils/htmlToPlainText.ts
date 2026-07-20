/**
 * TinyMCE HTML을 클립보드용 평문으로 변환한다.
 *
 * 브라우저 HTML 파서를 사용해 이름·10진수·16진수 엔티티를 화면 렌더링과
 * 동일하게 한 번 디코딩하며, 기존 저널 복사 계약의 줄바꿈 정규화를 유지한다.
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
    /* 연속 빈줄 → 단일 줄바꿈 */
    .replace(/\n+/g, "\n")
    .trim();
}
