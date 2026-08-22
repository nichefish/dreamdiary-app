/**
 * Reflection 본문에 Markdown `((...))`(md-text-noti) 마커를 멱등적으로 씌운다.
 *
 * MarkdownUtils 는 `<p>`/`<li>` 안 **단일 텍스트 노드** 단위로 `((...))` 를 처리한다.
 * 따라서 블록을 감쌀 때 인라인 HTML 은 평문으로 평탄화해 한 텍스트 노드로 만든다.
 * 이미 블록 전체가 `((...))` 이면 변경하지 않는다(멱등).
 */

export interface WrapDoubleParenResult {
  html: string;
  changed: boolean;
}

/**
 * 문자열이 통째로 하나의 `((...))` 인지 판별한다.
 * 안쪽 단일 괄호 `( )` 균형은 MarkdownUtils.replaceDoubleParenBalanced 와 동일하다.
 */
export function isFullyDoubleParenWrapped(text: string): boolean {
  const source = (text ?? "").trim();
  if (source.length < 4 || !source.startsWith("((") || !source.endsWith("))")) {
    return false;
  }

  let j = 2;
  let parenDepth = 0;
  let closeAt = -1;
  while (j < source.length) {
    const ch = source.charAt(j);
    if (parenDepth === 0 && ch === ")" && j + 1 < source.length && source.charAt(j + 1) === ")") {
      closeAt = j;
      break;
    }
    if (ch === "(") {
      parenDepth += 1;
      j += 1;
      continue;
    }
    if (ch === ")") {
      if (parenDepth > 0) parenDepth -= 1;
      j += 1;
      continue;
    }
    j += 1;
  }
  return closeAt >= 0 && closeAt + 2 === source.length;
}

function stripTags(html: string): string {
  return html
    .replace(/<br\b[^>]*>/gi, " ")
    .replace(/<[^>]+>/g, "")
    .replace(/&nbsp;/gi, " ")
    .replace(/&#160;/gi, " ")
    .replace(/&#x0*a0;/gi, " ")
    .replace(/\u00a0/g, " ");
}

/** MarkdownUtils 수평선과 같은 하이픈 3개 이상 단독 블록인지 판별한다. */
function isHorizontalRule(text: string): boolean {
  return /^-{3,}$/.test((text ?? "").trim());
}

/** 전체 감싸기가 만든 `((---))` 형태이면 원래 수평선 문자열을 반환한다. */
function unwrapWrappedHorizontalRule(text: string): string | null {
  const source = (text ?? "").trim();
  if (!source.startsWith("((") || !source.endsWith("))")) return null;
  const inner = source.slice(2, -2).trim();
  return isHorizontalRule(inner) ? inner : null;
}

/**
 * TinyMCE HTML 의 각 `<p>`/`<li>` 본문을 `((...))` 로 감싼다.
 *
 * @param html 저장용 원문 HTML(`content`). markdown 렌더 결과가 아니다.
 */
export function wrapHtmlWithDoubleParen(html: string): WrapDoubleParenResult {
  const source = html ?? "";
  if (!source.trim()) {
    return { html: source, changed: false };
  }

  const blockPattern = /<(p|li)(\s[^>]*)?>([\s\S]*?)<\/\1>/gi;
  let changed = false;
  let matched = false;

  const replaced = source.replace(blockPattern, (_match, tag: string, attrs: string | undefined, inner: string) => {
    matched = true;
    const plain = stripTags(inner).replace(/\s+/g, " ").trim();
    if (!plain) {
      return `<${tag}${attrs ?? ""}>${inner}</${tag}>`;
    }
    const restoredHorizontalRule = unwrapWrappedHorizontalRule(plain);
    if (restoredHorizontalRule != null) {
      changed = true;
      return `<${tag}${attrs ?? ""}>${restoredHorizontalRule}</${tag}>`;
    }
    if (isHorizontalRule(plain)) {
      return `<${tag}${attrs ?? ""}>${inner}</${tag}>`;
    }
    if (isFullyDoubleParenWrapped(plain)) {
      return `<${tag}${attrs ?? ""}>${inner}</${tag}>`;
    }
    changed = true;
    return `<${tag}${attrs ?? ""}>((${plain}))</${tag}>`;
  });

  if (matched) {
    return { html: replaced, changed };
  }

  const plainOnly = stripTags(source).replace(/\s+/g, " ").trim();
  if (!plainOnly) {
    return { html: source, changed: false };
  }
  const restoredHorizontalRule = unwrapWrappedHorizontalRule(plainOnly);
  if (restoredHorizontalRule != null) {
    return { html: `<p>${restoredHorizontalRule}</p>`, changed: true };
  }
  if (isHorizontalRule(plainOnly)) {
    return { html: source, changed: false };
  }
  if (isFullyDoubleParenWrapped(plainOnly)) {
    return { html: source, changed: false };
  }
  return { html: `<p>((${plainOnly}))</p>`, changed: true };
}
