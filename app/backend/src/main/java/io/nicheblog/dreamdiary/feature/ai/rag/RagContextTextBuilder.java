package io.nicheblog.dreamdiary.feature.ai.rag;

import io.nicheblog.dreamdiary.feature.ai.person.PersonFocus;
import io.nicheblog.dreamdiary.feature.ai.person.PersonFocusResolver;
import io.nicheblog.dreamdiary.feature.ai.person.PersonQueryClassifier;
import io.nicheblog.dreamdiary.feature.ai.person.PersonSnapshotService;
import io.nicheblog.dreamdiary.feature.journal.embedding.model.RagSearchResult;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RAG 검색 결과를 LLM 프롬프트용 컨텍스트 텍스트·태그/타임라인 요약으로 조립한다.
 *
 * <p>locale 키 {@code chat.ai.prompt.rag.*}를 사용한다. chat 패키지에 의존하지 않는다.</p>
 */
@Component
@RequiredArgsConstructor
public class RagContextTextBuilder {

    /** LOOKUP 컨텍스트에 포함할 엔트리당 최대 텍스트 길이 */
    private static final int RAG_TEXT_MAX_LENGTH = 300;
    /** 통섭형 RAG 컨텍스트에 포함할 엔트리당 최대 스니펫 길이 */
    private static final int RAG_SYNTHESIS_TEXT_MAX_LENGTH = 220;
    /** person-meaning 질문에서 직접 언급 source에 허용할 스니펫 길이 */
    private static final int RAG_PERSON_FOCUS_SNIPPET_MAX_LENGTH = 400;

    private final PersonFocusResolver personFocusResolver;
    private final PersonSnapshotService personSnapshotService;

    private String msg(final String key, final Object... args) {
        return MessageUtils.getMessage(key, args);
    }

    /**
     * 의도에 맞는 RAG 컨텍스트 텍스트를 구성한다.
     *
     * @param intent RAG 의도
     * @param results 검색 결과
     * @param personFocus 인물 focus (없으면 null)
     * @param queryText 사용자 질문
     * @return 프롬프트용 컨텍스트 텍스트
     */
    public String buildContextText(
            final RagIntent intent,
            final List<RagSearchResult> results,
            final PersonFocus personFocus,
            final String queryText
    ) {
        if (intent == RagIntent.SYNTHESIS || intent == RagIntent.SUMMARY) {
            return buildSynthesisContextText(intent, results, personFocus, queryText);
        }
        return buildLookupContextText(results);
    }

    /**
     * person-meaning tag-only 검색이 0건일 때 쓰는 안내 컨텍스트.
     *
     * @param personFocus 인물 focus
     * @param queryText 사용자 질문 (계약 호환용; 본문 미사용)
     * @return 빈 tag-only 안내 텍스트
     */
    public String buildPersonMeaningTagOnlyEmptyContextText(final PersonFocus personFocus, final String queryText) {
        final StringBuilder sb = new StringBuilder();
        sb.append("RAG_INTENT: SYNTHESIS\n");
        sb.append("PERSON_MEANING_TAG_ONLY: true\n");
        sb.append(msg("chat.ai.prompt.rag.tag-only-empty")).append("\n\n");
        return sb.toString().trim();
    }

    /**
     * 단순 조회형 RAG 컨텍스트를 구성한다.
     */
    public String buildLookupContextText(final List<RagSearchResult> results) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < results.size(); i++) {
            sb.append("[").append(i + 1).append("]\n");
            sb.append(StringUtils.abbreviate(
                    StringUtils.defaultString(results.get(i).getEntity().getEmbeddingText()),
                    RAG_TEXT_MAX_LENGTH
            ));
            sb.append("\n\n");
        }
        return sb.toString().trim();
    }

    /**
     * 요약/통섭형 RAG 컨텍스트를 구성한다.
     */
    public String buildSynthesisContextText(
            final RagIntent intent,
            final List<RagSearchResult> results,
            final PersonFocus personFocus,
            final String queryText
    ) {
        final StringBuilder sb = new StringBuilder();
        sb.append("RAG_INTENT: ").append(intent.name()).append('\n');
        if (PersonQueryClassifier.isPersonAttitudeQuery(queryText)) {
            sb.append(msg("chat.ai.prompt.rag.synthesis.stance-intro")).append("\n\n");
        } else {
            sb.append(msg("chat.ai.prompt.rag.synthesis.general-intro")).append("\n\n");
        }
        // PERSON_FOCUS·PERSON_MEANING_SCAFFOLD 블록은 제거됨(convergence): personFocus가 해결된
        // 질문은 항상 Path C(SNAPSHOT 프롬프트)로 가서 이 contextText를 소비하지 않고, 이 텍스트를
        // 소비하는 레거시 경로는 personFocus가 항상 null이라 블록이 렌더링될 수 없었다.
        appendTagSummaryBlock(sb, results, personFocus);
        appendTimelineSummaryBlock(sb, results);
        for (int i = 0; i < results.size(); i++) {
            final RagSearchResult result = results.get(i);
            if (result == null || result.getEntity() == null) continue;

            sb.append("- [").append(i + 1).append("] ");
            sb.append("date=").append(result.getEntity().getJournalDate()).append("; ");
            sb.append("kind=").append(result.getEntity().getContentKind()).append("; ");
            sb.append("match=").append(result.getMatchType()).append("; ");
            sb.append("score=").append(formatScore(result.getScore())).append("; ");
            if (result.getMatchedTokens() != null && !result.getMatchedTokens().isEmpty()) {
                sb.append("tokens=").append(result.getMatchedTokens()).append("; ");
            }
            sb.append("snippet=");
            sb.append(StringUtils.abbreviate(
                    StringUtils.defaultIfBlank(result.getSnippet(), result.getEntity().getEmbeddingText()),
                    resolveSynthesisSnippetMaxLength(result, personFocus)
            ));
            sb.append('\n');
        }
        return sb.toString().trim();
    }

    /**
     * RAG source들의 태그 빈도와 공동출현 쌍을 집계한다.
     */
    public RagTagSummary buildTagSummary(final List<RagSearchResult> results) {
        return buildTagSummary(results, null);
    }

    /**
     * person-meaning 질문에서는 focused source와 person 태그만 집계한다.
     */
    public RagTagSummary buildTagSummary(final List<RagSearchResult> results, final PersonFocus personFocus) {
        final Map<String, Integer> totalTagCountMap = new LinkedHashMap<>();
        final Map<String, Integer> dreamTagCountMap = new LinkedHashMap<>();
        final Map<String, Integer> diaryTagCountMap = new LinkedHashMap<>();
        final Map<String, Integer> noteTagCountMap = new LinkedHashMap<>();
        final Map<String, Integer> tagPairCountMap = new LinkedHashMap<>();

        if (results == null) {
            return new RagTagSummary(totalTagCountMap, dreamTagCountMap, diaryTagCountMap, noteTagCountMap, tagPairCountMap);
        }

        final List<RagSearchResult> scopedResults = personFocus == null
                ? results
                : personSnapshotService.resolvePersonFocusedResults(results, personFocus);
        if (scopedResults.isEmpty()) {
            return new RagTagSummary(totalTagCountMap, dreamTagCountMap, diaryTagCountMap, noteTagCountMap, tagPairCountMap);
        }

        for (final RagSearchResult result : scopedResults) {
            if (result == null || result.getEntity() == null) continue;

            final List<String> tags = personFocus == null
                    ? personFocusResolver.extractSourceTags(result).stream().distinct().collect(Collectors.toList())
                    : personFocusResolver.filterPersonMeaningTags(
                            personFocusResolver.extractSourceTags(result), personFocus
                    ).stream().distinct().collect(Collectors.toList());
            if (tags.isEmpty()) continue;

            incrementTagCounts(totalTagCountMap, tags);
            incrementTagPairCounts(tagPairCountMap, tags);

            final String contentKind = StringUtils.defaultString(result.getEntity().getContentKind());
            if ("DREAM".equalsIgnoreCase(contentKind)) {
                incrementTagCounts(dreamTagCountMap, tags);
            } else if ("DIARY".equalsIgnoreCase(contentKind)) {
                incrementTagCounts(diaryTagCountMap, tags);
            } else if ("NOTE".equalsIgnoreCase(contentKind)) {
                incrementTagCounts(noteTagCountMap, tags);
            }
        }

        return new RagTagSummary(totalTagCountMap, dreamTagCountMap, diaryTagCountMap, noteTagCountMap, tagPairCountMap);
    }

    /**
     * RAG source들의 시간/유형 흐름을 집계한다.
     */
    public RagTimelineSummary buildTimelineSummary(final List<RagSearchResult> results) {
        final Map<String, Integer> contentKindCountMap = new LinkedHashMap<>();
        final Map<String, Integer> monthCountMap = new LinkedHashMap<>();
        final List<LocalDate> dateList = new ArrayList<>();

        if (results != null) {
            for (final RagSearchResult result : results) {
                if (result == null || result.getEntity() == null) continue;

                final String contentKind = StringUtils.defaultIfBlank(result.getEntity().getContentKind(), "UNKNOWN");
                contentKindCountMap.merge(contentKind, 1, Integer::sum);

                final LocalDate journalDate = result.getEntity().getJournalDate();
                if (journalDate == null) continue;

                dateList.add(journalDate);
                monthCountMap.merge(formatDate(journalDate, "yyyy-MM"), 1, Integer::sum);
            }
        }

        dateList.sort(LocalDate::compareTo);
        final String firstDate = dateList.isEmpty() ? null : formatDate(dateList.get(0), "yyyy-MM-dd");
        final String lastDate = dateList.isEmpty() ? null : formatDate(dateList.get(dateList.size() - 1), "yyyy-MM-dd");
        return new RagTimelineSummary(
                results == null ? 0 : results.size(),
                firstDate,
                lastDate,
                contentKindCountMap,
                monthCountMap
        );
    }

    private void appendTagSummaryBlock(
            final StringBuilder sb,
            final List<RagSearchResult> results,
            final PersonFocus personFocus
    ) {
        final RagTagSummary tagSummary = buildTagSummary(results, personFocus);

        if (tagSummary.totalTagCountMap().isEmpty()) return;

        sb.append(msg("chat.ai.prompt.rag.tag-summary.header"));
        if (personFocus != null) {
            sb.append(msg(
                    "chat.ai.prompt.rag.tag-summary.person-focus-intro",
                    personFocusResolver.resolvePersonFocusTarget(personFocus)
            ));
        }
        sb.append(msg("chat.ai.prompt.rag.tag-summary.guide"));
        appendTagCountLine(sb, msg("chat.ai.prompt.rag.tag-summary.label.total"), tagSummary.totalTagCountMap());
        appendTagCountLine(sb, msg("chat.ai.prompt.rag.tag-summary.label.dream"), tagSummary.dreamTagCountMap());
        appendTagCountLine(sb, msg("chat.ai.prompt.rag.tag-summary.label.diary"), tagSummary.diaryTagCountMap());
        appendTagCountLine(sb, msg("chat.ai.prompt.rag.tag-summary.label.note"), tagSummary.noteTagCountMap());
        appendTagPairLine(sb, msg("chat.ai.prompt.rag.tag-summary.label.pair"), tagSummary.tagPairCountMap());
        sb.append('\n');
    }

    private void appendTimelineSummaryBlock(final StringBuilder sb, final List<RagSearchResult> results) {
        final RagTimelineSummary timelineSummary = buildTimelineSummary(results);
        if (timelineSummary.sourceCount() <= 0) return;

        sb.append(msg("chat.ai.prompt.rag.timeline.header"));
        if (StringUtils.isNotBlank(timelineSummary.firstDate()) || StringUtils.isNotBlank(timelineSummary.lastDate())) {
            sb.append(msg(
                    "chat.ai.prompt.rag.timeline.period",
                    StringUtils.defaultIfBlank(timelineSummary.firstDate(), "?"),
                    StringUtils.defaultIfBlank(timelineSummary.lastDate(), "?")
            ));
        }
        appendTagCountLine(sb, msg("chat.ai.prompt.rag.timeline.label.content-kind"), timelineSummary.contentKindCountMap());
        appendTagCountLine(sb, msg("chat.ai.prompt.rag.timeline.label.month-density"), timelineSummary.monthCountMap());
        sb.append(msg("chat.ai.prompt.rag.timeline.guide"));
    }

    private int resolveSynthesisSnippetMaxLength(final RagSearchResult result, final PersonFocus personFocus) {
        if (personFocus != null && personFocusResolver.mentionsPersonFocus(result, personFocus)) {
            return RAG_PERSON_FOCUS_SNIPPET_MAX_LENGTH;
        }
        return RAG_SYNTHESIS_TEXT_MAX_LENGTH;
    }

    private void incrementTagCounts(final Map<String, Integer> tagCountMap, final List<String> tags) {
        for (final String tag : tags) {
            if (StringUtils.isBlank(tag)) continue;
            tagCountMap.merge(tag, 1, Integer::sum);
        }
    }

    private void incrementTagPairCounts(final Map<String, Integer> tagPairCountMap, final List<String> tags) {
        if (tags.size() < 2) return;
        final List<String> sortedTags = tags.stream()
                .filter(StringUtils::isNotBlank)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        for (int i = 0; i < sortedTags.size(); i++) {
            for (int j = i + 1; j < sortedTags.size(); j++) {
                tagPairCountMap.merge(sortedTags.get(i) + " ↔ " + sortedTags.get(j), 1, Integer::sum);
            }
        }
    }

    private void appendTagCountLine(final StringBuilder sb, final String label, final Map<String, Integer> tagCountMap) {
        if (tagCountMap.isEmpty()) return;
        sb.append(label).append(": ").append(formatTopTags(tagCountMap, 14)).append('\n');
    }

    private void appendTagPairLine(final StringBuilder sb, final String label, final Map<String, Integer> tagPairCountMap) {
        if (tagPairCountMap.isEmpty()) return;
        sb.append(label).append(": ").append(formatTopTags(tagPairCountMap, 10)).append('\n');
    }

    private String formatTopTags(final Map<String, Integer> tagCountMap, final int limit) {
        return tagCountMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(limit)
                .map(entry -> entry.getKey() + "(" + entry.getValue() + ")")
                .collect(Collectors.joining(", "));
    }

    private String formatScore(final Double score) {
        if (score == null) return "null";
        return String.format("%.4f", score);
    }

    private String formatDate(final Object date, final String pattern) {
        if (date == null) return null;
        try {
            return new SimpleDateFormat(pattern).format(DateUtils.asDate(date));
        } catch (final Exception e) {
            return null;
        }
    }
}
