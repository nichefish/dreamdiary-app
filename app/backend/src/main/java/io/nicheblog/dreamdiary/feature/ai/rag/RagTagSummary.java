package io.nicheblog.dreamdiary.feature.ai.rag;

import java.util.Map;

/**
 * RAG source들의 태그 빈도·공동출현 요약.
 *
 * @param totalTagCountMap 전체 태그 빈도
 * @param dreamTagCountMap DREAM 태그 빈도
 * @param diaryTagCountMap DIARY 태그 빈도
 * @param noteTagCountMap NOTE 태그 빈도
 * @param tagPairCountMap 태그 공동출현 쌍 빈도
 */
public record RagTagSummary(
        Map<String, Integer> totalTagCountMap,
        Map<String, Integer> dreamTagCountMap,
        Map<String, Integer> diaryTagCountMap,
        Map<String, Integer> noteTagCountMap,
        Map<String, Integer> tagPairCountMap
) {}
