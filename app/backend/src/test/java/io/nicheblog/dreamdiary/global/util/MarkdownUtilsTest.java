package io.nicheblog.dreamdiary.global.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarkdownUtilsTest {

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
}
