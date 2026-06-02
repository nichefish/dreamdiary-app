package io.nicheblog.dreamdiary.global.util;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.tika.utils.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MarkdownUtils
 * <pre>
 *  마크다운 처리 유틸리티 모듈
 * </pre>
 * TODO:: 필요별로 유틸 분리하고 필요하면 새로 만들기
 *
 * @author nichefish
 */
@Component
@Log4j2
public class MarkdownUtils {

    private static String procTextWithGeneratedPlaceholders(final String text, final int maxGroupLength) {
        final List<String> generatedHtmlList = new ArrayList<>();

        String part = text;
        part = replaceGeneratedPattern(part, Pattern.compile("(?m)^[ \\t]*-{3,}[ \\t]*$"), maxGroupLength, generatedHtmlList, matcher -> "<hr>");
        part = replaceDialogPatternWithPlaceholders(part, Pattern.compile("\"(.*?)\""), "\u201c", "\u201d", maxGroupLength, generatedHtmlList);
        part = replaceDialogPatternWithPlaceholders(part, Pattern.compile("\u201c(.*?)\u201d"), "\u201c", "\u201d", maxGroupLength, generatedHtmlList);
        part = replaceDialogPatternWithPlaceholders(part, Pattern.compile("\u300e(.*?)\u300f"), "\u300e", "\u300f", maxGroupLength, generatedHtmlList);
        part = replaceGeneratedPattern(part, Pattern.compile("--(.*?)(--)"), maxGroupLength, generatedHtmlList, matcher -> {
            final String group = matcher.group(1);
            return "<span class='md-text-muted'>-" + StringEscapeUtils.escapeHtml4(group) + "-</span>";
        });
        part = replaceGeneratedPattern(part, Pattern.compile("!!(.*?)!!"), maxGroupLength, generatedHtmlList, matcher -> {
            final String group = matcher.group(1);
            return "<span class='md-text-danger'>" + StringEscapeUtils.escapeHtml4(group) + "</span>";
        });
        part = replaceGeneratedPattern(part, Pattern.compile("__(.*?)__"), maxGroupLength, generatedHtmlList, matcher -> {
            final String group = matcher.group(1);
            return "<u>" + StringEscapeUtils.escapeHtml4(group) + "</u>";
        });
        part = replaceGeneratedPattern(part, Pattern.compile("\\|\\|(.*?)\\|\\|"), maxGroupLength, generatedHtmlList, matcher -> {
            final String group = matcher.group(1);
            return "<span class='md-text-muted fw-bold border-end border-2 border-gray-400 pe-5 me-3'>" + StringEscapeUtils.escapeHtml4(group) + "</span>";
        });
        part = replaceGeneratedPattern(part, Pattern.compile("\\(\\((.*?)\\)\\)"), maxGroupLength, generatedHtmlList, matcher -> {
            final String group = matcher.group(1);
            return "<span class='md-text-noti'>" + StringEscapeUtils.escapeHtml4(group) + "</span>";
        });
        part = replaceGeneratedPattern(part, Pattern.compile("<@>(.*?\\.)"), maxGroupLength, generatedHtmlList, matcher -> {
            final String group = matcher.group(1);
            return "<span class='md-text-muted'>@" + StringEscapeUtils.escapeHtml4(group) + "</span>";
        });

        return escapeTextAndRestoreGeneratedHtml(part, generatedHtmlList);
    }

    private static String escapeTextAndRestoreGeneratedHtml(final String text, final List<String> generatedHtmlList) {
        String escaped = StringEscapeUtils.escapeHtml4(text);
        for (int i = generatedHtmlList.size() - 1; i >= 0; i--) {
            escaped = escaped.replace(getGeneratedHtmlPlaceholder(i), generatedHtmlList.get(i));
        }
        return escaped;
    }

    private static String putGeneratedHtml(final List<String> generatedHtmlList, final String html) {
        final int idx = generatedHtmlList.size();
        generatedHtmlList.add(html);
        return getGeneratedHtmlPlaceholder(idx);
    }

    private static String getGeneratedHtmlPlaceholder(final int idx) {
        return Character.toString((char) 0) + "MD_HTML_" + idx + Character.toString((char) 0);
    }

    private static String replaceGeneratedPattern(
            final String source,
            final Pattern pattern,
            final int maxGroupLength,
            final List<String> generatedHtmlList,
            final Function<Matcher, String> replacementFactory
    ) {
        final Matcher matcher = pattern.matcher(source);
        final StringBuilder buffer = new StringBuilder();
        while (matcher.find()) {
            if (matcher.groupCount() >= 1) {
                final String group = matcher.group(1);
                if (group == null || group.length() > maxGroupLength) {
                    matcher.appendReplacement(buffer, Matcher.quoteReplacement(matcher.group(0)));
                    continue;
                }
            }

            matcher.appendReplacement(buffer, Matcher.quoteReplacement(putGeneratedHtml(generatedHtmlList, replacementFactory.apply(matcher))));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String replaceDialogPatternWithPlaceholders(
            final String source,
            final Pattern pattern,
            final String openPrefix,
            final String closeSuffix,
            final int maxGroupLength,
            final List<String> generatedHtmlList
    ) {
        final Matcher matcher = pattern.matcher(source);
        final StringBuilder buffer = new StringBuilder();
        while (matcher.find()) {
            final String group = matcher.group(1);
            if (group == null || group.length() > maxGroupLength) {
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(matcher.group(0)));
                continue;
            }

            final String escaped = StringEscapeUtils.escapeHtml4(group);
            final String replacement = "<span class='md-text-dialog'>" + openPrefix + escaped + closeSuffix + "</span>";
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(putGeneratedHtml(generatedHtmlList, replacement)));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * 공통 > 마크다운 처리
     *
     * @param htmlContent Elements
     */
    /*
     * Rendering-time contract:
     * - Stored editor HTML may contain real TinyMCE tags such as p/table/span.
     * - Text written by the user as literal HTML must remain text.
     * - Custom inline markup may create wrapper tags, but the matched user text
     *   inside those wrappers must be escaped before reinsertion.
     */
    public static String markdown(final String htmlContent) {
        final Document document = Jsoup.parseBodyFragment(htmlContent);
        final Elements paragraphs = document.select("p");
        procNodes(paragraphs);
        final Elements lis = document.select("li");
        procNodes(lis);

        // 일반 큰따옴표로 묶인 부분을 하이라이트 색상으로 표시
        return document.body().html(); // 변경된 HTML 반환
    }

    /**
     * 텍스트 마크다운 처리 :: 메소드 분리
     *
     * @param elements Elements
     **/
    public static void procNodes(final Elements elements) {
        for (final Element elmt : elements) {
            // <pre> 태그는 처리하지 않음
            if (elmt.tagName().equalsIgnoreCase("pre")) continue;

            // <li>&nbsp;- 형태의 태그에 상하 간격 부여
            if (elmt.tagName().equalsIgnoreCase("li")) {
                String html = elmt.html();
                if (html.trim().startsWith("&nbsp;-")) elmt.addClass("my-2");
            }

            // 해당 요소의 모든 자식 노드를 순회
            for (final Node child : elmt.childNodes()) {
                if (child instanceof TextNode textNode) {
                    // 텍스트 노드의 경우, 마크다운 변환 로직을 적용
                    final String text = textNode.getWholeText();
                    final String processedText = procText(text); // 변환된 텍스트 처리 :: 메소드 분리
                    if (text.equals(processedText)) continue;
                    // 텍스트 노드에 HTML 코드를 직접 삽입
                    textNode.before(processedText);
                    textNode.remove();
                }
            }
        }
    }

    /**
     * 텍스트 마크다운 처리 :: 메소드 분리
     *
     * @param text String
     * @return String
     **/
    /*
     * Do not decide safety by tag name. Text from the stored content is escaped
     * as text; only HTML generated by this renderer survives via placeholders.
     */
    public static String procText(final String text) {
        if (text == null) return null;
        final int MAX_GROUP_LENGTH = 3000;
        return procTextWithGeneratedPlaceholders(text, MAX_GROUP_LENGTH);
    }

    /**
     * 텍스트에디터 컨텐츠 저장 전 정규화
     * @param originalText String
     * @return String
     **/
    /*
     * Save-time contract:
     * - Preserve valid TinyMCE HTML structures.
     * - Preserve escaped literal HTML text such as "&lt;table&gt;" as text.
     * - Never call unescapeHtml4() on the full content string. It cannot tell
     *   real editor markup from user-authored literal markup and can turn text
     *   into active/broken HTML during save.
     */
    public static String normalize(final String originalText) {
        if (StringUtils.isEmpty(originalText)) return null;

        final String replacedText = originalText
                .replace("‘", "'")
                .replace("’", "'")
                .replace("“", "\"")
                .replace("”", "\"")
                .replace("…", "...");

        final Document doc = Jsoup.parseBodyFragment(replacedText);
        for (final Element el : doc.getAllElements()) {
            if (el.tagName().equalsIgnoreCase("script")) continue;
            if (el.tagName().equalsIgnoreCase("style")) continue;

            for (final Node child : el.childNodes()) {
                if (child instanceof TextNode textNode) {
                    final String originalTextNode = textNode.getWholeText();
                    final String replacedTextNode = originalTextNode.replaceAll("(?m)^->(?=[ \\u00A0])", "→").replaceAll("(?<=[ \\u00A0])->(?=[ \\u00A0])", "→");
                    textNode.text(replacedTextNode);
                }
            }
        }

        /*
         * BEFORE: pasted or editor-generated paragraphs stored as one <p> with
         * direct <br> separators rendered without legacy paragraph spacing.
         * AFTER: direct <br> separators split that paragraph into separate <p>
         * nodes so registered editor content keeps the legacy paragraph unit.
         */
        final int splitParagraphCount = splitParagraphsByDirectLineBreak(doc);
        if (splitParagraphCount > 0) log.debug("MarkdownUtils.normalize split paragraphs by direct br. count={}", splitParagraphCount);

        return doc.body().html();
    }

    /**
     * 단일 문단 내부의 직접 자식 br 기준 문단 분리
     *
     * @param doc Document
     * @return int 분리된 문단 수
     */
    private static int splitParagraphsByDirectLineBreak(final Document doc) {
        int splitCount = 0;
        final List<Element> paragraphs = new ArrayList<>(doc.select("p"));

        for (final Element paragraph : paragraphs) {
            final List<List<Node>> segments = splitChildNodesByDirectBr(paragraph);
            if (segments.size() <= 1) continue;

            final List<List<Node>> meaningfulSegments = new ArrayList<>();
            for (final List<Node> segment : segments) {
                if (hasMeaningfulContent(segment)) meaningfulSegments.add(segment);
            }
            if (meaningfulSegments.size() <= 1) continue;

            final Element firstParagraph = createParagraphWithNodes(paragraph, meaningfulSegments.get(0));
            paragraph.replaceWith(firstParagraph);

            Element cursor = firstParagraph;
            for (int i = 1; i < meaningfulSegments.size(); i++) {
                final Element nextParagraph = createParagraphWithNodes(paragraph, meaningfulSegments.get(i));
                cursor.after(nextParagraph);
                cursor = nextParagraph;
            }
            splitCount += meaningfulSegments.size() - 1;
        }

        return splitCount;
    }

    /**
     * 직접 자식 br을 기준으로 자식 노드 목록 분할
     *
     * @param paragraph Element
     * @return List&lt;List&lt;Node&gt;&gt;
     */
    private static List<List<Node>> splitChildNodesByDirectBr(final Element paragraph) {
        final List<List<Node>> segments = new ArrayList<>();
        List<Node> currentSegment = new ArrayList<>();

        for (final Node child : paragraph.childNodes()) {
            if (child instanceof Element childElement && childElement.tagName().equalsIgnoreCase("br")) {
                segments.add(currentSegment);
                currentSegment = new ArrayList<>();
                continue;
            }
            currentSegment.add(child.clone());
        }
        segments.add(currentSegment);

        return segments;
    }

    /**
     * 원본 문단 속성을 보존한 새 문단 생성
     *
     * @param paragraph Element
     * @param nodes List&lt;Node&gt;
     * @return Element
     */
    private static Element createParagraphWithNodes(final Element paragraph, final List<Node> nodes) {
        final Element newParagraph = paragraph.clone();
        newParagraph.empty();
        for (final Node node : nodes) {
            newParagraph.appendChild(node);
        }
        return newParagraph;
    }

    /**
     * 공백만 남은 br 분리 조각 제외
     *
     * @param nodes List&lt;Node&gt;
     * @return boolean
     */
    private static boolean hasMeaningfulContent(final List<Node> nodes) {
        for (final Node node : nodes) {
            if (node instanceof TextNode textNode) {
                if (!textNode.getWholeText().replace('\u00A0', ' ').trim().isEmpty()) return true;
                continue;
            }
            if (node instanceof Element element && element.tagName().equalsIgnoreCase("br")) continue;
            return true;
        }

        return false;
    }
}
