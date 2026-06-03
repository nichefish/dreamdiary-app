# Markdown Content Normalization

## 배경

저널/게시글/댓글/연간 회고 등 리치 텍스트 본문은 TinyMCE HTML을 저장하고, 조회 시 `MarkdownUtils.markdown()`을 거쳐 커스텀 인라인 표현을 HTML로 감싼 뒤 `v-html`로 렌더링한다.

이 경로에서는 두 종류의 `<...>`를 반드시 구분해야 한다.

- 에디터가 만든 실제 HTML 구조: `<p>`, `<table>`, `<span>`, `<img>` 등
- 사용자가 본문 텍스트로 쓴 리터럴 HTML: `&lt;table&gt;`, `&lt;table&lt;` 등

두 번째 값은 화면에서도 텍스트여야 한다. 저장 또는 조회 중 실제 태그로 복원되면 브라우저/Jsoup 파서가 DOM 구조로 해석해서 본문 이후 HTML이 무너질 수 있다.

## 저장 시 계약

`MarkdownUtils.normalize()`는 저장 전 정규화 함수다.

해야 하는 일:

- TinyMCE가 만든 정상 HTML 구조는 보존한다.
- 스마트 따옴표, 말줄임표, 텍스트 노드 내부 화살표 같은 텍스트 치환만 수행한다.
- `&lt;table&gt;`처럼 사용자가 텍스트로 입력한 escaped HTML은 escaped 상태로 보존한다.

하지 말아야 하는 일:

- 전체 본문 문자열에 `StringEscapeUtils.unescapeHtml4()`를 적용하지 않는다.

전체 unescape는 정상 HTML 복구처럼 보일 수 있지만, 실제로는 아래 두 값을 구분하지 못한다.

```html
<!-- 실제 에디터 HTML이어야 하는 경우 -->
&lt;table&gt;...&lt;/table&gt;

<!-- 사용자가 텍스트로 쓴 경우 -->
&lt;table&gt;
```

따라서 전체 unescape는 사용자가 쓴 텍스트를 실제 태그로 바꾸는 부작용이 있다.

## 조회 시 계약

`MarkdownUtils.markdown()`은 조회 렌더링 전 변환 함수다.

해야 하는 일:

- 저장된 TinyMCE HTML 구조는 보존한다.
- ((...)), !!...!!, __...__, ||...||, <@>... 같은 커스텀 인라인 표현은 정해진 wrapper 태그로 변환한다.
- `(잘 기억이 안 난다.)` — 꿈 본문 기억 불확실 표시용 **고정 문구**만 `md-text-muted`로 감싼다. 일반 `(…)` 패턴이 아니다.
- wrapper 내부의 사용자 본문 조각은 반드시 HTML escape 후 삽입한다.
- 변환 후 남아 있는 unsafe `<`는 텍스트로 escape 한다.

하지 말아야 하는 일:

- 정규식 매치 그룹을 raw HTML 문자열로 다시 삽입하지 않는다.
- `v-html`이 받을 문자열에 사용자 본문 조각을 unescaped 상태로 섞지 않는다.

## 대표 회귀 케이스

아래 입력은 저장 후에도, 조회 후에도 `<table>` 태그가 아니라 텍스트로 남아야 한다.

```html
<p>&lt;table&gt;</p>
<p>plain &lt;table&lt; text ((marked &lt;table&lt; text))</p>
```

반대로 TinyMCE가 만든 정상 테이블은 구조로 보존되어야 한다.

```html
<table><tbody><tr><td>value</td></tr></tbody></table>
```

관련 테스트:

- `MarkdownUtilsTest.normalizePreservesEscapedLiteralHtmlText`
- `MarkdownUtilsTest.normalizePreservesValidEditorHtml`
- `MarkdownUtilsTest.markdownEscapesLiteralAngleBracketsInCustomInlineMarkup`
- `MarkdownUtilsTest.markdownPreservesValidTableHtml`

