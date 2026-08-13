package io.nicheblog.dreamdiary.feature.ai.rag;

import io.nicheblog.dreamdiary.feature.ai.person.PersonFocus;
import io.nicheblog.dreamdiary.feature.ai.person.PersonFocusResolver;
import io.nicheblog.dreamdiary.feature.ai.person.PersonQueryClassifier;
import io.nicheblog.dreamdiary.feature.journal.embedding.model.RagSearchResult;
import io.nicheblog.dreamdiary.feature.journal.embedding.service.JournalEntryEmbeddingSearchService;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.service.JournalEntityFocusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RAG 컨텍스트 조립 능력 — 의도 분류·검색 병합·person tag-only·컨텍스트 텍스트.
 *
 * <p>채널 설정({@link RagSearchLimits})과 intent-classify 프롬프트는 호출자가 주입한다.
 * chat 패키지에 의존하지 않는다.</p>
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class RagContextService {

    /** entity catalog 연결 기록을 RAG에 강제 포함할 최대 건수 */
    private static final int RAG_ENTITY_LINK_MAX = 10;

    private final RagSearchFacade ragSearchFacade;
    private final JournalEntryEmbeddingSearchService embeddingSearchService;
    private final JournalEntityFocusService journalEntityFocusService;
    private final PersonFocusResolver personFocusResolver;
    private final RagContextTextBuilder ragContextTextBuilder;

    /**
     * 사용자 질문에 대한 RAG 컨텍스트를 조립한다.
     *
     * @param queryText 사용자 메시지
     * @param limits 검색 한도·enabled
     * @param intentClassifySystemPrompt LLM 2차 의도 분류용 시스템 프롬프트
     * @return 의도·결과·텍스트·personFocus
     */
    public RagContext build(
            final String queryText,
            final RagSearchLimits limits,
            final String intentClassifySystemPrompt
    ) {
        final RagIntent intent = detectIntent(queryText, intentClassifySystemPrompt);
        if (limits == null || !limits.enabled()) {
            log.info("AI RAG disabled by admin setting. intent={}", intent);
            return RagContext.empty(intent);
        }
        try {
            if (intent == RagIntent.SYNTHESIS && PersonQueryClassifier.isPersonMeaningQuery(queryText)) {
                return buildPersonMeaningRagContext(queryText, intent, limits);
            }
            return buildMergedRagContext(queryText, intent, limits);
        } catch (final Exception e) {
            log.warn("RAG context search failed, proceeding without context. intent={}, error={}", intent, e.getMessage());
            return RagContext.empty(intent);
        }
    }

    /**
     * 질문 문장을 보고 RAG 응답 의도를 분류한다.
     *
     * @param queryText 사용자 질문
     * @param intentClassifySystemPrompt LLM 2차용 시스템 프롬프트
     * @return LOOKUP / SUMMARY / SYNTHESIS
     */
    public RagIntent detectIntent(final String queryText, final String intentClassifySystemPrompt) {
        return ragSearchFacade.detectIntent(
                queryText,
                PersonQueryClassifier.isPersonAboutLookupQuery(queryText),
                intentClassifySystemPrompt
        );
    }

    /**
     * RAG 의도·질문 유형별 검색 폭을 반환한다.
     */
    public int resolveTopK(final RagIntent intent, final String queryText, final RagSearchLimits limits) {
        return ragSearchFacade.resolveTopK(
                intent,
                PersonQueryClassifier.isPersonAttitudeQuery(queryText),
                limits
        );
    }

    /**
     * RAG 의도별 벡터 검색 최소 점수를 반환한다.
     */
    public double resolveMinScore(final RagIntent intent, final RagSearchLimits limits) {
        return ragSearchFacade.resolveMinScore(intent, limits);
    }

    /**
     * 태그 요약을 반환한다. 메시지 metadata 조립용.
     */
    public RagTagSummary buildTagSummary(final List<RagSearchResult> results, final PersonFocus personFocus) {
        return ragContextTextBuilder.buildTagSummary(results, personFocus);
    }

    /**
     * 타임라인 요약을 반환한다. 메시지 metadata 조립용.
     */
    public RagTimelineSummary buildTimelineSummary(final List<RagSearchResult> results) {
        return ragContextTextBuilder.buildTimelineSummary(results);
    }

    private RagContext buildMergedRagContext(
            final String queryText,
            final RagIntent intent,
            final RagSearchLimits limits
    ) {
        // 태도 질문이 tag-only에서 merged 폴백으로 넘어와도 같은 확대 검색 폭(PERSON_STANCE_RAG_TOP_K)을
        // 쓰도록 queryText를 함께 넘긴다. (이전에는 intent만 넘겨 SYNTHESIS 기본 폭으로 좁아졌다.)
        final int topK = resolveTopK(intent, queryText, limits);
        final double minScore = resolveMinScore(intent, limits);
        List<RagSearchResult> mergedResults = ragSearchFacade.searchMerged(queryText, topK, minScore);
        PersonFocus personFocus = personFocusResolver.resolvePersonFocus(queryText, intent, mergedResults);
        mergedResults = personFocusResolver.mergePersonTagResults(queryText, intent, mergedResults, personFocus, topK);
        personFocus = personFocusResolver.resolvePersonFocus(queryText, intent, mergedResults);
        final List<RagSearchResult> entityBoosted = mergeEntityLinkedResults(mergedResults, personFocus, topK);
        final List<RagSearchResult> results = personFocusResolver.prioritizeResultsForPersonFocus(entityBoosted, personFocus);
        if (results.isEmpty() && personFocus == null) return RagContext.empty(intent);
        log.info("AI RAG context built. intent={}, queryLength={}, mergedCount={}",
                intent, StringUtils.length(queryText), results.size());
        if (personFocus != null) {
            log.info("AI RAG person focus applied. target={}, aliases={}, matchedSourceCount={}",
                    personFocus.primaryToken(), personFocus.tokens(), personFocus.matchedSourceCount());
        }
        logRagSources(results);

        return new RagContext(
                intent,
                results,
                ragContextTextBuilder.buildContextText(intent, results, personFocus, queryText),
                personFocus
        );
    }

    private RagContext buildPersonMeaningRagContext(
            final String queryText,
            final RagIntent intent,
            final RagSearchLimits limits
    ) {
        final RagContext tagFirstContext = buildPersonMeaningTagOnlyRagContext(queryText, intent, limits);
        if (tagFirstContext.results() != null && !tagFirstContext.results().isEmpty()) {
            return tagFirstContext;
        }

        log.info("AI RAG person-meaning tag-only empty, falling back to merged synthesis retrieval. target={}",
                tagFirstContext.personFocus() == null ? null : tagFirstContext.personFocus().primaryToken());
        return buildMergedRagContext(queryText, intent, limits);
    }

    /**
     * person-meaning SYNTHESIS questions use only journal entries whose payload tags match person focus tokens.
     *
     * <p>Entity catalog resolves alias tokens (for example {@code 민수} -> {@code 김민수}) but does not inject
     * catalog-linked entries, keyword hits, vector hits, or body-mention fallbacks.</p>
     */
    private RagContext buildPersonMeaningTagOnlyRagContext(
            final String queryText,
            final RagIntent intent,
            final RagSearchLimits limits
    ) {
        final int topK = resolveTopK(intent, queryText, limits);
        final List<String> queryTokens = PersonQueryClassifier.extractPersonFocusTokens(queryText);
        if (queryTokens.isEmpty()) {
            log.info("AI RAG person-meaning tag-only skipped. reason=noPersonToken");
            return RagContext.empty(intent);
        }

        final JournalEntityFocusService.PersonEntityFocusSummary entitySummary =
                journalEntityFocusService == null
                        ? null
                        : journalEntityFocusService.resolvePersonFocusSummary(queryTokens).orElse(null);
        final List<String> focusTokens = personFocusResolver.mergePersonFocusTokens(queryTokens, entitySummary);
        final List<RagSearchResult> tagResults = embeddingSearchService.searchByPersonTagsWithScore(focusTokens, topK);
        final PersonFocus personFocus = new PersonFocus(
                personFocusResolver.resolvePersonFocusPrimaryToken(queryText, queryTokens, entitySummary),
                focusTokens,
                tagResults.size(),
                entitySummary
        );

        if (tagResults.isEmpty()) {
            log.info("AI RAG person-meaning tag-only empty. target={}, tokens={}",
                    personFocus.primaryToken(), focusTokens);
            return new RagContext(
                    intent,
                    List.of(),
                    ragContextTextBuilder.buildPersonMeaningTagOnlyEmptyContextText(personFocus, queryText),
                    personFocus
            );
        }

        log.info("AI RAG person-meaning tag-only built. target={}, tokens={}, tagCount={}",
                personFocus.primaryToken(), focusTokens, tagResults.size());
        logRagSources(tagResults);
        return new RagContext(
                intent,
                tagResults,
                ragContextTextBuilder.buildContextText(intent, tagResults, personFocus, queryText),
                personFocus
        );
    }

    /**
     * entity catalog에 연결된 저널 엔트리를 RAG 결과 앞에 강제 병합한다.
     *
     * <p>person-meaning 질문에서 벡터 점수가 낮아 빠진 직접 연결 기록을 보강한다.</p>
     */
    private List<RagSearchResult> mergeEntityLinkedResults(
            final List<RagSearchResult> results,
            final PersonFocus personFocus,
            final int topK
    ) {
        if (personFocus == null || personFocus.entitySummary() == null || results == null) {
            return results;
        }

        final List<Integer> entryIds = personFocus.entitySummary().journalEntryIds();
        if (entryIds == null || entryIds.isEmpty()) {
            return results;
        }

        final List<Integer> limitedEntryIds = entryIds.stream()
                .limit(RAG_ENTITY_LINK_MAX)
                .collect(Collectors.toList());
        final List<RagSearchResult> entityLinked = embeddingSearchService.findByJournalEntryIds(limitedEntryIds);
        if (entityLinked.isEmpty()) {
            return results;
        }

        log.info(
                "AI RAG entity-linked boost applied. target={}, catalogEntryCount={}, injectedCount={}",
                personFocusResolver.resolvePersonFocusTarget(personFocus),
                entryIds.size(),
                entityLinked.size()
        );

        final Map<Integer, RagSearchResult> merged = new LinkedHashMap<>();
        for (final RagSearchResult result : entityLinked) {
            if (result == null || result.getJournalEntryId() == null) continue;
            merged.put(result.getJournalEntryId(), result);
        }
        for (final RagSearchResult result : results) {
            if (result == null || result.getJournalEntryId() == null) continue;
            merged.putIfAbsent(result.getJournalEntryId(), result);
        }
        return merged.values().stream().limit(topK).collect(Collectors.toList());
    }

    private void logRagSources(final List<RagSearchResult> results) {
        for (int i = 0; i < results.size(); i++) {
            final RagSearchResult result = results.get(i);
            if (result == null || result.getEntity() == null) continue;

            log.info(
                    "AI RAG source rank={} entryId={} date={} matchType={} score={} tokens={} snippet={}",
                    i + 1,
                    result.getJournalEntryId(),
                    result.getEntity().getJournalDate(),
                    result.getMatchType(),
                    formatScore(result.getScore()),
                    result.getMatchedTokens(),
                    StringUtils.abbreviate(StringUtils.defaultString(result.getSnippet()), 160)
            );
        }
    }

    private String formatScore(final Double score) {
        if (score == null) return "null";
        return String.format("%.4f", score);
    }
}
