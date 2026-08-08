package io.nicheblog.dreamdiary.feature.ai.rag;

import io.nicheblog.dreamdiary.feature.ai.client.OllamaClient;
import io.nicheblog.dreamdiary.feature.journal.embedding.model.RagSearchResult;
import io.nicheblog.dreamdiary.feature.journal.embedding.service.JournalEntryEmbeddingSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RAG 검색·의도 분류·결과 병합 능력.
 *
 * <p>저널 임베딩 검색 서비스를 소비하며, 채팅 채널 DTO/설정에는 의존하지 않는다.
 * 컨텍스트 조립은 {@link RagContextService}·{@link RagContextTextBuilder}가 담당한다.</p>
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class RagSearchFacade {

    private final JournalEntryEmbeddingSearchService embeddingSearchService;
    private final OllamaClient ollamaClient;

    /**
     * 질문 의도를 분류한다. 1차 휴리스틱 후, SUMMARY∩SYNTHESIS 모호 시 LLM 2차 분류를 시도한다.
     *
     * @param queryText 사용자 질문
     * @param personAboutLookup 인물 about 판정(호출자 제공)
     * @param intentClassifySystemPrompt LLM 2차용 시스템 프롬프트(채널 i18n 등에서 주입)
     * @return LOOKUP / SUMMARY / SYNTHESIS
     */
    public RagIntent detectIntent(
            final String queryText,
            final boolean personAboutLookup,
            final String intentClassifySystemPrompt
    ) {
        final RagIntent heuristic = RagIntentClassifier.classify(queryText, personAboutLookup);
        if (personAboutLookup || !RagIntentClassifier.needsLlmSecondPass(queryText)) {
            return heuristic;
        }
        final RagIntent llmIntent = classifyIntentWithLlm(queryText, intentClassifySystemPrompt);
        if (llmIntent == null) {
            log.info("AI RAG intent LLM second-pass skipped/failed, using heuristic. heuristic={}, queryLength={}",
                    heuristic, StringUtils.length(queryText));
            return heuristic;
        }
        log.info("AI RAG intent LLM second-pass. heuristic={}, llm={}, queryLength={}",
                heuristic, llmIntent, StringUtils.length(queryText));
        return llmIntent;
    }

    /**
     * 키워드 우선·벡터 후순위 병합 검색을 수행한다.
     *
     * @param queryText 검색 질의
     * @param topK 최대 건수
     * @param minScore 벡터 최소 점수
     * @return 병합된 검색 결과
     */
    public List<RagSearchResult> searchMerged(
            final String queryText,
            final int topK,
            final double minScore
    ) {
        final List<RagSearchResult> keywordResults = embeddingSearchService.searchByKeywordWithScore(queryText, topK);
        final List<RagSearchResult> vectorResults = embeddingSearchService.searchWithScore(queryText, topK, minScore);
        final List<RagSearchResult> merged = mergeKeywordThenVector(keywordResults, vectorResults, topK);
        log.info("AI RAG merged search. queryLength={}, keywordCount={}, vectorCount={}, mergedCount={}",
                StringUtils.length(queryText), keywordResults.size(), vectorResults.size(), merged.size());
        return merged;
    }

    /**
     * 의도·태도 여부에 따른 검색 top-K를 반환한다.
     *
     * @param intent RAG 의도
     * @param stanceQuery person-stance(태도) 질문 여부
     * @param limits 검색 한도
     * @return top-K
     */
    public int resolveTopK(final RagIntent intent, final boolean stanceQuery, final RagSearchLimits limits) {
        if (intent == RagIntent.SYNTHESIS && stanceQuery) {
            return limits.stanceTopK();
        }
        if (intent == RagIntent.SYNTHESIS) {
            return limits.synthesisTopK();
        }
        if (intent == RagIntent.SUMMARY) {
            return limits.summaryTopK();
        }
        return limits.topK();
    }

    /**
     * 의도별 벡터 최소 점수를 반환한다.
     *
     * @param intent RAG 의도
     * @param limits 검색 한도
     * @return 최소 점수
     */
    public double resolveMinScore(final RagIntent intent, final RagSearchLimits limits) {
        if (intent == RagIntent.SYNTHESIS) {
            return limits.synthesisMinScore();
        }
        return limits.minScore();
    }

    /**
     * 직접 키워드 검색 결과를 우선하고, 벡터 검색 결과를 뒤에 합친다.
     *
     * @param keywordResults 키워드 결과
     * @param vectorResults 벡터 결과
     * @param topK 최대 건수
     * @return 병합 결과
     */
    public List<RagSearchResult> mergeKeywordThenVector(
            final List<RagSearchResult> keywordResults,
            final List<RagSearchResult> vectorResults,
            final int topK
    ) {
        final Map<Integer, RagSearchResult> merged = new LinkedHashMap<>();
        if (keywordResults != null) {
            for (final RagSearchResult result : keywordResults) {
                if (result == null || result.getJournalEntryId() == null) {
                    continue;
                }
                merged.put(result.getJournalEntryId(), result);
                if (merged.size() >= topK) {
                    return new ArrayList<>(merged.values());
                }
            }
        }
        if (vectorResults != null) {
            for (final RagSearchResult result : vectorResults) {
                if (result == null || result.getJournalEntryId() == null) {
                    continue;
                }
                merged.putIfAbsent(result.getJournalEntryId(), result);
                if (merged.size() >= topK) {
                    break;
                }
            }
        }
        return new ArrayList<>(merged.values());
    }

    /**
     * TAG 매칭 결과를 최우선으로 두고 나머지 RAG 결과를 뒤에 병합한다.
     *
     * @param tagResults 태그 매칭 결과
     * @param results 기존 병합 결과
     * @param topK 최대 건수
     * @return 태그 우선 병합 결과
     */
    public List<RagSearchResult> mergeTagFirst(
            final List<RagSearchResult> tagResults,
            final List<RagSearchResult> results,
            final int topK
    ) {
        final Map<Integer, RagSearchResult> merged = new LinkedHashMap<>();
        if (tagResults != null) {
            for (final RagSearchResult result : tagResults) {
                if (result == null || result.getJournalEntryId() == null) {
                    continue;
                }
                merged.put(result.getJournalEntryId(), result);
            }
        }
        if (results != null) {
            for (final RagSearchResult result : results) {
                if (result == null || result.getJournalEntryId() == null) {
                    continue;
                }
                merged.putIfAbsent(result.getJournalEntryId(), result);
            }
        }
        return merged.values().stream().limit(topK).collect(Collectors.toList());
    }

    /**
     * 모호한 질문에 대해 Ollama로 LOOKUP/SUMMARY/SYNTHESIS 레이블만 받는다.
     *
     * @param queryText 사용자 질문
     * @param intentClassifySystemPrompt 시스템 프롬프트
     * @return 파싱된 의도, 실패·미지원 시 {@code null}
     */
    private RagIntent classifyIntentWithLlm(final String queryText, final String intentClassifySystemPrompt) {
        if (ollamaClient == null || StringUtils.isBlank(queryText) || StringUtils.isBlank(intentClassifySystemPrompt)) {
            return null;
        }
        try {
            final String raw = ollamaClient.chat(intentClassifySystemPrompt, queryText.trim());
            return parseIntentLabel(raw);
        } catch (final Exception e) {
            log.warn("AI RAG intent LLM second-pass error. queryLength={}, error={}",
                    StringUtils.length(queryText), e.toString());
            return null;
        }
    }

    /**
     * LLM 응답에서 LOOKUP/SUMMARY/SYNTHESIS 토큰을 추출한다. 없으면 null.
     *
     * @param raw LLM 원문
     * @return 파싱된 의도 또는 null
     */
    public RagIntent parseIntentLabel(final String raw) {
        if (StringUtils.isBlank(raw)) {
            return null;
        }
        final String upper = raw.toUpperCase(Locale.ROOT);
        int bestIdx = Integer.MAX_VALUE;
        RagIntent best = null;
        for (final RagIntent intent : RagIntent.values()) {
            final int idx = upper.indexOf(intent.name());
            if (idx >= 0 && idx < bestIdx) {
                bestIdx = idx;
                best = intent;
            }
        }
        return best;
    }
}
