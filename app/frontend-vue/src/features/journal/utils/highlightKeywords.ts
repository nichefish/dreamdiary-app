/**
 * highlightKeywords.ts
 * HTML 문자열 내 검색 키워드를 `<mark>` 태그로 하이라이트하는 순수 유틸.
 *
 * 브라우저 DOM TreeWalker 를 사용해 텍스트 노드만 탐색하고,
 * MARK/SCRIPT/STYLE/TEXTAREA 내부는 건너뛴다.
 * SSR 환경(`typeof document === "undefined"`)에서는 원문을 그대로 반환한다.
 */

/**
 * HTML 문자열에서 keywords 에 해당하는 부분을 `<mark class="journal-entry-search-keyword-mark">` 로 감싼다.
 *
 * @param html - 원본 HTML 문자열 (v-html 바인딩용)
 * @param keywords - 하이라이트할 키워드 배열 (빈 배열이면 원문 반환)
 * @returns 하이라이트 적용된 HTML 문자열
 */
export function highlightKeywordsInHtml(html: string, keywords: string[]): string {
  const uniqueKeywords = Array.from(new Set(keywords.map((keyword) => keyword.trim()).filter(Boolean)));
  if (!html || uniqueKeywords.length === 0 || typeof document === "undefined") return html;

  const template = document.createElement("template");
  template.innerHTML = html;

  const textNodes: Text[] = [];
  const skippedTags = new Set(["MARK", "SCRIPT", "STYLE", "TEXTAREA"]);
  const lowerKeywords = uniqueKeywords.map((keyword) => keyword.toLowerCase());
  const walker = document.createTreeWalker(
    template.content,
    NodeFilter.SHOW_TEXT,
    {
      acceptNode(node) {
        const parent = node.parentElement;
        const value = node.nodeValue ?? "";
        if (!parent || skippedTags.has(parent.tagName)) return NodeFilter.FILTER_REJECT;
        return lowerKeywords.some((keyword) => value.toLowerCase().includes(keyword))
          ? NodeFilter.FILTER_ACCEPT
          : NodeFilter.FILTER_REJECT;
      },
    },
  );

  while (walker.nextNode()) {
    textNodes.push(walker.currentNode as Text);
  }

  textNodes.forEach((node) => {
    const text = node.nodeValue ?? "";
    const lowerText = text.toLowerCase();
    const fragment = document.createDocumentFragment();
    let cursor = 0;

    while (cursor < text.length) {
      const nextMatch = findNextKeywordMatch(lowerText, lowerKeywords, cursor);
      if (!nextMatch) {
        fragment.appendChild(document.createTextNode(text.slice(cursor)));
        break;
      }
      if (nextMatch.index > cursor) {
        fragment.appendChild(document.createTextNode(text.slice(cursor, nextMatch.index)));
      }

      const mark = document.createElement("mark");
      mark.className = "journal-entry-search-keyword-mark";
      mark.textContent = text.slice(nextMatch.index, nextMatch.index + nextMatch.length);
      fragment.appendChild(mark);
      cursor = nextMatch.index + nextMatch.length;
    }

    node.parentNode?.replaceChild(fragment, node);
  });

  return template.innerHTML;
}

/**
 * 텍스트에서 다음으로 나타나는 키워드 매치를 찾는다.
 * 같은 위치에 여러 키워드가 매치되면 더 긴 것을 우선한다.
 */
function findNextKeywordMatch(
  lowerText: string,
  lowerKeywords: string[],
  cursor: number,
): { index: number; length: number } | null {
  let best: { index: number; length: number } | null = null;
  lowerKeywords.forEach((keyword) => {
    const index = lowerText.indexOf(keyword, cursor);
    if (index === -1) return;
    if (!best || index < best.index || (index === best.index && keyword.length > best.length)) {
      best = { index, length: keyword.length };
    }
  });
  return best;
}
