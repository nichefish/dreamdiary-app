package io.nicheblog.dreamdiary.feature.ai.person;

import java.util.List;
import java.util.Map;

/**
 * person-meaning fallback·hybrid SNAPSHOT 생성에 쓰는 스캐폴드 스냅샷.
 *
 * @param repeatedTagCountMap 인물 관련 반복 태그 빈도
 * @param roleAxesKo entity catalog 역할 축 한국어 라벨
 * @param contentKindCountMap 기록 유형(DREAM/DIARY/NOTE) 빈도
 * @param linkedContextTagCountMap 인물 태그 제외 공동출현 태그
 * @param chapterPrefixCountMap 챕터 Prefix 빈도
 * @param evidenceSnippets 근거 장면 스니펫
 * @param firstDate 최초 등장일
 * @param lastDate 최근 등장일
 */
public record PersonMeaningSnapshot(
        Map<String, Integer> repeatedTagCountMap,
        List<String> roleAxesKo,
        Map<String, Integer> contentKindCountMap,
        Map<String, Integer> linkedContextTagCountMap,
        Map<String, Integer> chapterPrefixCountMap,
        List<String> evidenceSnippets,
        String firstDate,
        String lastDate
) {}