package io.nicheblog.dreamdiary.feature.ai.rag;

import java.util.Map;

/**
 * RAG source들의 시간·유형 흐름 요약.
 *
 * @param sourceCount 소스 건수
 * @param firstDate 최초 저널일 (yyyy-MM-dd)
 * @param lastDate 최종 저널일 (yyyy-MM-dd)
 * @param contentKindCountMap contentKind 빈도
 * @param monthCountMap 월(yyyy-MM) 밀도
 */
public record RagTimelineSummary(
        int sourceCount,
        String firstDate,
        String lastDate,
        Map<String, Integer> contentKindCountMap,
        Map<String, Integer> monthCountMap
) {}
