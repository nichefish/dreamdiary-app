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

    /**
     * 공통 > 마크다운 처리
     *
     * @param htmlContent Elements
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
    public static String procText(final String text) {
        final int MAX_GROUP_LENGTH = 3000;

        // 텍스트를 <pre> 태그 기준으로 분할, <pre> </pre> 사이는 처리하지 않음
        final String[] parts = text.split("(?i)(</?pre>)");
        final StringBuilder result = new StringBuilder();
        boolean insidePreTag = false;
        for (String part : parts) {
            if (part.equalsIgnoreCase("<pre>") || part.equalsIgnoreCase("</pre>")) {
                result.append(part);
                insidePreTag = !insidePreTag;
            } else if (insidePreTag) {
                result.append(part);
            } else {
                // 줄 전체가 대시 6개 이상인 경우 수평선(<hr>)으로 처리
                part = part.replaceAll("(?m)^[ \\t]*-{6,}[ \\t]*$", "<hr>");

                // " " 로 묶인 부분을 대사 스타일 처리 (HTML 이스케이프 포함)
                part = replaceDialogPattern(part, Pattern.compile("\"(.*?)\""), "“", "”", MAX_GROUP_LENGTH);

                // 『 』 로 묶인 부분도 대사 스타일 처리 (HTML 이스케이프 포함)
                part = replaceDialogPattern(part, Pattern.compile("『(.*?)』"), "『", "』", MAX_GROUP_LENGTH);

                // -- -- 로 묶인 부분을 회색으로 표시하되, - - 처리
                final Pattern grayPattern = Pattern.compile("--(.*?)(--)");
                final Matcher grayMatcher = grayPattern.matcher(part);
                while (grayMatcher.find()) {
                    final String group = grayMatcher.group(1);
                    if (group == null || group.length() > MAX_GROUP_LENGTH) continue;
                    part = part.replace("--" + group + "--", "<span class='md-text-muted'>-" + group + "-</span>");
                }

                // !! !! 로 묶인 부분을 빨간색으로 표시하되, !! !! 제거
                final Pattern redPattern = Pattern.compile("!!(.*?)!!");
                final Matcher redMatcher = redPattern.matcher(part);
                while (redMatcher.find()) {
                    final String group = redMatcher.group(1);
                    if (group == null || group.length() > MAX_GROUP_LENGTH) continue;
                    part = part.replace("!!" + group + "!!", "<span class='md-text-danger'>" + group + "</span>");
                }

                // __ __ 로 묶인 부분을 밑줄 처리하되, __ __ 제거
                final Pattern underlinePattern = Pattern.compile("__(.*?)__");
                final Matcher underlineMatcher = underlinePattern.matcher(part);
                while (underlineMatcher.find()) {
                    final String group = underlineMatcher.group(1);
                    if (group == null || group.length() > MAX_GROUP_LENGTH) continue;
                    part = part.replace("__" + group + "__", "<u>" + group + "</u>");
                }

                // || || 로 묶인 부분을 강조 처리하되, || || 제거 및 우측 구분바 추가
                final Pattern pipelinePattern = Pattern.compile("\\|\\|(.*?)\\|\\|");
                final Matcher pipelineMatcher = pipelinePattern.matcher(part);
                while (pipelineMatcher.find()) {
                    String group = pipelineMatcher.group(1);
                    if (group == null || group.length() > MAX_GROUP_LENGTH) continue;
                    part = part.replace("||" + group + "||", "<span class='md-text-muted fw-bold border-end border-2 border-gray-400 pe-5 me-3'>" + group + "</span>");
                }

                // (( )) 로 묶인 부분을 밑줄 처리하되, (( )) 제거
                final Pattern parenthesisPattern = Pattern.compile("\\(\\((.*?)\\)\\)");
                final Matcher parenthesisMatcher = parenthesisPattern.matcher(part);
                while (parenthesisMatcher.find()) {
                    final String group = parenthesisMatcher.group(1);
                    if (group == null || group.length() > MAX_GROUP_LENGTH) continue;
                    part = part.replace("((" + group + "))", "<span class='md-text-noti'>" + group + "</span>");
                }

                // <@> .로 묶인 부분을 강조 처리
                final Pattern atPattern = Pattern.compile("<@>(.*?\\.)");
                final Matcher atMatcher = atPattern.matcher(part);
                final StringBuilder buffer = new StringBuilder();
                while (atMatcher.find()) {
                    final String group = atMatcher.group(1);
                    if (group == null || group.length() > MAX_GROUP_LENGTH) continue;
                    atMatcher.appendReplacement(buffer, "<span class='md-text-muted'>@" + group + "</span>");
                }
                atMatcher.appendTail(buffer); // 변환되지 않은 나머지 텍스트를 추가
                part = buffer.toString(); // 변환된 부분을 전체 텍스트에 반영

                result.append(part);
            }
        }

        return result.toString();
    }

    /**
     * 대사 패턴 치환
     *
     * @param source 원본 문자열
     * @param pattern 치환할 정규식 (group(1)에 본문)
     * @param openPrefix 출력시 앞에 붙일 문자
     * @param closeSuffix 출력시 뒤에 붙일 문자
     * @param maxGroupLength 허용 최대 길이
     * @return 치환 결과 문자열
     */
    private static String replaceDialogPattern(
            final String source,
            final Pattern pattern,
            final String openPrefix,
            final String closeSuffix,
            final int maxGroupLength
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
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * 텍스트에디터 컨텐츠 저장 전 정규화
     *
     * @param originalText String
     * @return String
     **/
    public static String normalize(final String originalText) {
        if (StringUtils.isEmpty(originalText)) return null;

        final String unescaped = StringEscapeUtils.unescapeHtml4(originalText);
        final String replacedText = unescaped
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

        return doc.body().html();
    }
}