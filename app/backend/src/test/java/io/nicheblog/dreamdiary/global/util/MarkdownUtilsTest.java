package io.nicheblog.dreamdiary.global.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarkdownUtilsTest {

    @Test
    @DisplayName("markdown wraps dream memory uncertainty fixed phrase in md-text-muted")
    void markdownWrapsDreamMemoryUncertaintyFixedPhrase() {
        final String phrase = "(잘 기억이 안 난다.)";
        final String result = MarkdownUtils.markdown("<p>앞글 " + phrase + " 뒤글</p>");

        assertTrue(result.contains("md-text-muted"));
        assertTrue(result.contains(phrase));
        assertFalse(result.contains("md-text-muted\">(다른 문구)"));
    }

    @Test
    @DisplayName("markdown does not wrap non-matching parenthetical text as memory uncertainty")
    void markdownDoesNotWrapSimilarParentheticalText() {
        final String result = MarkdownUtils.markdown("<p>(비슷하지만 다른 문구)</p>");

        assertFalse(result.contains("md-text-muted"));
    }


    @Test
    @DisplayName("((plain)) wraps content in md-text-noti")
    void markdownDoubleParenWrapsPlainContent() {
        final String result = MarkdownUtils.markdown("<p>((plain note))</p>");

        assertTrue(result.contains("md-text-noti"));
        assertTrue(result.contains("plain note"));
    }

    @Test
    @DisplayName("(((nested))) treats inner single parens as content of (( ))")
    void markdownDoubleParenKeepsBalancedInnerSingleParens() {
        final String result = MarkdownUtils.markdown("<p>(((A: 1. B: 2.)))</p>");

        assertTrue(result.contains("md-text-noti"));
        assertTrue(result.contains("(A: 1. B: 2.)"));
        assertFalse(result.contains("(A: 1. B: 2.</span>)"));
    }

    @Test
    @DisplayName("((a (b) c)) keeps mid single parens inside noti span")
    void markdownDoubleParenAllowsMidSingleParens() {
        final String result = MarkdownUtils.markdown("<p>((a (b) c))</p>");

        assertTrue(result.contains("md-text-noti"));
        assertTrue(result.contains("a (b) c"));
    }

    @Test
    @DisplayName("custom inline markdown escapes literal angle brackets")
    void markdownEscapesLiteralAngleBracketsInCustomInlineMarkup() {
        final String result = MarkdownUtils.markdown("<p>plain &lt;table&lt; text ((marked &lt;table&lt; text))</p>");

        assertTrue(result.contains("&lt;table&lt;"));
        assertFalse(result.contains("<table<"));
        assertTrue(result.contains("md-text-noti"));
    }

    @Test
    @DisplayName("markdown does not promote literal escaped span tags")
    void markdownDoesNotPromoteLiteralEscapedSpanTags() {
        final String result = MarkdownUtils.markdown("<p>&lt;span class='md-text-dialog'&gt;fake&lt;/span&gt;</p>");

        assertTrue(result.contains("&lt;span"));
        assertFalse(result.contains("<span class=\"md-text-dialog\">fake</span>"));
        assertFalse(result.contains("<span class='md-text-dialog'>fake</span>"));
    }

    @Test
    @DisplayName("markdown keeps literal html as text inside generated wrappers")
    void markdownKeepsLiteralHtmlAsTextInsideGeneratedWrappers() {
        final String result = MarkdownUtils.markdown("<p>((literal &lt;table&gt; text))</p>");

        assertTrue(result.contains("md-text-noti"));
        assertTrue(result.contains("&lt;table&gt;"));
        assertFalse(result.contains("<table>"));
    }

    @Test
    @DisplayName("valid TinyMCE table html is preserved")
    void markdownPreservesValidTableHtml() {
        final String tableHtml = "<table><tbody><tr><td>value</td></tr></tbody></table>";
        final String result = MarkdownUtils.markdown(tableHtml);

        assertTrue(result.contains("<table>"));
        assertTrue(result.contains("<td>value</td>"));
    }


    @Test
    @DisplayName("markdown nests underline inside danger regardless of pattern order")
    void markdownNestsUnderlineInsideDanger() {
        final String result = MarkdownUtils.markdown("<p>!!__밑줄__!!</p>");

        assertTrue(result.contains("md-text-danger"));
        assertTrue(result.contains("<u>밑줄</u>"));
        assertFalse(result.contains("__밑줄__"));
    }

    @Test
    @DisplayName("markdown nests danger inside underline regardless of pattern order")
    void markdownNestsDangerInsideUnderline() {
        final String result = MarkdownUtils.markdown("<p>__!!강조!!__</p>");

        assertTrue(result.contains("<u>"));
        assertTrue(result.contains("md-text-danger"));
        assertTrue(result.contains("강조"));
        assertFalse(result.contains("!!강조!!"));
    }

    @Test
    @DisplayName("markdown nests underline inside dialog quotes")
    void markdownNestsUnderlineInsideDialog() {
        final String result = MarkdownUtils.markdown("<p>\"__말__\"</p>");

        assertTrue(result.contains("md-text-dialog"));
        assertTrue(result.contains("<u>말</u>"));
        assertFalse(result.contains("__말__"));
    }

    @Test
    @DisplayName("markdown nests danger inside noti double-paren")
    void markdownNestsDangerInsideNoti() {
        final String result = MarkdownUtils.markdown("<p>((!!알림!!))</p>");

        assertTrue(result.contains("md-text-noti"));
        assertTrue(result.contains("md-text-danger"));
        assertTrue(result.contains("알림"));
        assertFalse(result.contains("!!알림!!"));
    }

    @Test
    @DisplayName("nested wrappers still escape literal html text")
    void markdownKeepsLiteralHtmlEscapedInsideNestedWrappers() {
        final String result = MarkdownUtils.markdown("<p>!!((literal &lt;table&gt; text))!!</p>");

        assertTrue(result.contains("md-text-danger"));
        assertTrue(result.contains("md-text-noti"));
        assertTrue(result.contains("&lt;table&gt;"));
        assertFalse(result.contains("<table>"));
    }

    @Test
    @DisplayName("markdown processes nested inline text nodes")
    void markdownProcessesNestedInlineTextNodes() {
        final String result = MarkdownUtils.markdown("<p><span>__밑줄__</span> 그리고 <strong>\"대화\"</strong></p>");

        assertTrue(result.contains("<span><u>밑줄</u></span>"));
        assertTrue(result.contains("<strong><span class=\"md-text-dialog\">“대화”</span></strong>"));
    }

    @Test
    @DisplayName("normalize preserves escaped literal html text")
    void normalizePreservesEscapedLiteralHtmlText() {
        final String result = MarkdownUtils.normalize("<p>&lt;table&gt;</p>");

        assertTrue(result.contains("&lt;table&gt;"));
        assertFalse(result.contains("<table>"));
    }

    @Test
    @DisplayName("normalize preserves valid editor html")
    void normalizePreservesValidEditorHtml() {
        final String result = MarkdownUtils.normalize("<p>before</p><table><tbody><tr><td>value</td></tr></tbody></table>");

        assertTrue(result.contains("<p>before</p>"));
        assertTrue(result.contains("<table>"));
        assertTrue(result.contains("<td>value</td>"));
    }

    @Test
    @DisplayName("normalize splits direct br separated editor paragraphs")
    void normalizeSplitsDirectBrSeparatedEditorParagraphs() {
        final String result = MarkdownUtils.normalize("<p>오후 대화.<br>상대: <span class=\"md-text-dialog\">\"메뉴가\"</span><br>나: 확인해볼게요</p>");

        assertTrue(result.contains("<p>오후 대화.</p>"));
        assertTrue(result.contains("<p>상대: <span class=\"md-text-dialog\">\"메뉴가\"</span></p>"));
        assertTrue(result.contains("<p>나: 확인해볼게요</p>"));
        assertFalse(result.contains("<br>"));
    }

    @Test
    @DisplayName("normalize splits pasted plain paragraphs stored as direct br in one paragraph")
    void normalizeSplitsPastedPlainParagraphsStoredAsDirectBrInOneParagraph() {
        final String result = normalizeSingleLine("<p>A<br>B<br>C</p>");

        assertEquals("<p>A</p>\\n<p>B</p>\\n<p>C</p>", result);
    }

    @Test
    @DisplayName("normalize preserves already separated paragraphs")
    void normalizePreservesAlreadySeparatedParagraphs() {
        final String result = normalizeSingleLine("<p>A</p><p>B</p>");

        assertEquals("<p>A</p>\\n<p>B</p>", result);
    }

    @Test
    @DisplayName("normalize keeps direct br inside list item")
    void normalizeKeepsDirectBrInsideListItem() {
        final String result = normalizeSingleLine("<ul><li>A<br>B</li></ul>");

        assertTrue(result.contains("<li>A<br>\\n  B</li>"));
    }

    @Test
    @DisplayName("normalize keeps nested br inside inline element")
    void normalizeKeepsNestedBrInsideInlineElement() {
        final String result = normalizeSingleLine("<p>A<span><br></span>B</p>");

        assertEquals("<p>A<span><br></span>B</p>", result);
    }

    @Test
    @Disabled("TODO: 붙여넣기 정규화 범위를 에디터 paste_postprocess로 좁힐 때 soft break 보존 계약으로 활성화")
    @DisplayName("normalize should preserve intentional soft break")
    void normalizeShouldPreserveIntentionalSoftBreak() {
        final String result = normalizeSingleLine("<p>A<br>B</p>");

        assertEquals("<p>A<br>B</p>", result);
    }

    @Test
    @Disabled("TODO: 붙여넣기 정규화 범위를 에디터 paste_postprocess로 좁힐 때 빈 줄 보존 계약으로 활성화")
    @DisplayName("normalize should preserve blank visual line")
    void normalizeShouldPreserveBlankVisualLine() {
        final String result = normalizeSingleLine("<p>A<br><br>B</p>");

        assertEquals("<p>A<br><br>B</p>", result);
    }

    @Test
    @Disabled("TODO: 붙여넣기 정규화 범위를 에디터 paste_postprocess로 좁힐 때 표 내부 문단 보존 계약으로 활성화")
    @DisplayName("normalize should preserve paragraph line breaks inside table cell")
    void normalizeShouldPreserveParagraphLineBreaksInsideTableCell() {
        final String result = normalizeSingleLine("<table><tbody><tr><td><p>A<br>B</p></td></tr></tbody></table>");

        assertTrue(result.contains("<p>A<br>B</p>"));
    }


    @Test
    @DisplayName("renderChatMarkdown escapes html then restores limited markdown")
    void renderChatMarkdownEscapesHtmlThenRestoresLimitedMarkdown() {
        final String result = MarkdownUtils.renderChatMarkdown(
                "Hello <script>x</script>\n\n**bold** and `code`\n\n- one\n- two"
        );

        assertTrue(result.contains("<strong>bold</strong>"));
        assertTrue(result.contains("<code class=\"chat-md-code\">code</code>"));
        assertTrue(result.contains("<ul class=\"chat-md-ul\">"));
        assertTrue(result.contains("&lt;script&gt;"));
        assertFalse(result.contains("<script>"));
    }

    @Test
    @DisplayName("renderChatMarkdown returns dash for empty input")
    void renderChatMarkdownReturnsDashForEmptyInput() {
        assertEquals("-", MarkdownUtils.renderChatMarkdown(""));
        assertEquals("-", MarkdownUtils.renderChatMarkdown(null));
    }

    private String normalizeSingleLine(final String html) {
        return MarkdownUtils.normalize(html).replace("\n", "\\n");
    }
}
