package io.nicheblog.dreamdiary.feature.ai.person;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nicheblog.dreamdiary.feature.ai.rag.RagIntent;
import io.nicheblog.dreamdiary.feature.ai.rag.RagSearchFacade;
import io.nicheblog.dreamdiary.feature.journal.embedding.model.RagSearchResult;
import io.nicheblog.dreamdiary.feature.journal.embedding.service.JournalEntryEmbeddingSearchService;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.service.JournalEntityFocusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 인물 focus 해석·태그 병합·source 우선순위 정렬.
 *
 * <p>저널 entity catalog·임베딩 검색·{@link RagSearchFacade}를 소비한다. chat 패키지에 의존하지 않는다.</p>
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class PersonFocusResolver {

    private final JournalEntityFocusService journalEntityFocusService;
    private final JournalEntryEmbeddingSearchService embeddingSearchService;
    private final RagSearchFacade ragSearchFacade;
    private final ObjectMapper objectMapper = new ObjectMapper();

/**
 * person-meaning 질문에서는 person 태그 매칭 결과를 RAG 앞쪽에 우선 병합합니다.
 */
public List<RagSearchResult> mergePersonTagResults(
        final String queryText,
        final RagIntent intent,
        final List<RagSearchResult> results,
        final PersonFocus personFocus,
        final int topK
) {
    if (intent != RagIntent.SYNTHESIS || !PersonQueryClassifier.isPersonMeaningQuery(queryText)) {
        return results;
    }

    final List<String> tagTokens = personFocus != null && !personFocus.tokens().isEmpty()
            ? personFocus.tokens()
            : PersonQueryClassifier.extractPersonFocusTokens(queryText);
    if (tagTokens.isEmpty()) return results;

    final List<RagSearchResult> tagResults = embeddingSearchService.searchByPersonTagsWithScore(tagTokens, topK);
    if (tagResults.isEmpty()) return results;

    log.info("AI RAG person-tag search applied. tokens={}, hitCount={}", tagTokens, tagResults.size());
    return ragSearchFacade.mergeTagFirst(tagResults, results, topK);
}
/**
 * 통섭 인물 질문에서 여러 source에 반복되는 인물 축을 좁혀야 할 때 person focus를 구성합니다.
 */
public PersonFocus resolvePersonFocus(
        final String queryText,
        final RagIntent intent,
        final List<RagSearchResult> results
) {
    if (intent != RagIntent.SYNTHESIS || !PersonQueryClassifier.isPersonMeaningQuery(queryText)) return null;

    final List<String> tokens = PersonQueryClassifier.extractPersonFocusTokens(queryText);
    if (tokens.isEmpty()) return null;
    final JournalEntityFocusService.PersonEntityFocusSummary entitySummary =
            journalEntityFocusService == null ? null : journalEntityFocusService.resolvePersonFocusSummary(tokens).orElse(null);
    final List<String> focusTokens = mergePersonFocusTokens(tokens, entitySummary);

    int matchedSourceCount = 0;
    for (final RagSearchResult result : results) {
        if (mentionsPersonTokens(result, focusTokens)) {
            matchedSourceCount++;
        }
    }
    if (matchedSourceCount <= 0 && entitySummary == null) return null;

    return new PersonFocus(resolvePersonFocusPrimaryToken(queryText, tokens, entitySummary), focusTokens, matchedSourceCount, entitySummary);
}
/**
 * Merge query person tokens with canonical entity labels already normalized by the entity catalog.
 */
public List<String> mergePersonFocusTokens(
        final List<String> tokens,
        final JournalEntityFocusService.PersonEntityFocusSummary entitySummary
) {
    final Set<String> seen = new LinkedHashSet<>();
    final List<String> mergedTokens = new ArrayList<>();

    if (tokens != null) {
        for (final String token : tokens) {
            if (StringUtils.isBlank(token) || !seen.add(token)) continue;
            mergedTokens.add(token);
        }
    }

    if (entitySummary == null) return mergedTokens;

    final String canonicalLabel = StringUtils.trimToEmpty(entitySummary.canonicalLabel());
    if (StringUtils.isNotBlank(canonicalLabel) && seen.add(canonicalLabel)) {
        mergedTokens.add(canonicalLabel);
    }

    for (final String surfaceForm : entitySummary.topSurfaceForms(4)) {
        if (StringUtils.isBlank(surfaceForm) || !seen.add(surfaceForm)) continue;
        mergedTokens.add(surfaceForm);
    }

    return mergedTokens;
}

/**
 * Prefer the catalog canonical label as the primary focus target once the entity summary exists.
 */
public String resolvePersonFocusPrimaryToken(
        final String queryText,
        final List<String> tokens,
        final JournalEntityFocusService.PersonEntityFocusSummary entitySummary
) {
    if (entitySummary != null && StringUtils.isNotBlank(entitySummary.canonicalLabel())) {
        return entitySummary.canonicalLabel();
    }
    return selectPrimaryPersonTokenFromQuery(queryText, tokens);
}

/**
 * 질문 문장에서 person focus 대상 이름 토큰을 고릅니다.
 *
 * <p>{@code 지연님}처럼 호칭이 붙은 토큰을 우선하고, {@code 대화} 같은 범위어는 건너뜁니다.</p>
 */
public String selectPrimaryPersonTokenFromQuery(final String queryText, final List<String> tokens) {
    if (tokens == null || tokens.isEmpty()) return null;

    final String text = StringUtils.defaultString(queryText);
    for (final String token : tokens) {
        if (StringUtils.isBlank(token)) continue;
        if (text.contains(token + "님")) {
            return token;
        }
    }
    for (final String token : tokens) {
        if (StringUtils.isNotBlank(token)) {
            return token;
        }
    }
    return null;
}
/**
 * person focus 관련성이 높은 source를 우선 근거로 쓰도록 정렬합니다.
 */
public List<RagSearchResult> prioritizeResultsForPersonFocus(
        final List<RagSearchResult> results,
        final PersonFocus personFocus
) {
    if (personFocus == null || results == null || results.isEmpty()) return results;

    return results.stream()
            .sorted(Comparator
                    .comparing((RagSearchResult result) -> resolvePersonFocusMatchPriority(result, personFocus), Comparator.reverseOrder())
                    .thenComparing(result -> countDirectPersonTokenMatches(result, personFocus), Comparator.reverseOrder())
                    .thenComparing(result -> result.getScore() == null ? 0D : result.getScore(), Comparator.reverseOrder()))
            .collect(Collectors.toList());
}

/**
 * person-meaning RAG 정렬 우선순위: TAG > ENTITY > person 태그 > 본문 언급.
 */
public int resolvePersonFocusMatchPriority(final RagSearchResult result, final PersonFocus personFocus) {
    if (result == null || personFocus == null) return 0;
    if (RagSearchResult.MATCH_TYPE_TAG.equals(result.getMatchType())) return 40;
    if (RagSearchResult.MATCH_TYPE_ENTITY.equals(result.getMatchType())) return 30;
    if (hasPersonTagMatch(result, personFocus)) return 25;
    if (mentionsPersonFocus(result, personFocus)) return 20;
    return 0;
}

/**
 * source payload 태그에 person focus 토큰이 포함되어 있는지 확인합니다.
 */
public boolean hasPersonTagMatch(final RagSearchResult result, final PersonFocus personFocus) {
    return !filterPersonMeaningTags(extractSourceTags(result), personFocus).isEmpty();
}

/**
 * source가 person focus 토큰을 포함하는지 확인합니다.
 */
public boolean mentionsPersonFocus(final RagSearchResult result, final PersonFocus personFocus) {
    if (personFocus == null) return false;
    return mentionsPersonTokens(result, personFocus.tokens());
}

/**
 * Resolve the display target for one PERSON_FOCUS block.
 */
public String resolvePersonFocusTarget(final PersonFocus personFocus) {
    if (personFocus == null) return null;
    if (personFocus.entitySummary() != null && StringUtils.isNotBlank(personFocus.entitySummary().canonicalLabel())) {
        return personFocus.entitySummary().canonicalLabel();
    }
    return personFocus.primaryToken();
}

/**
 * source 텍스트가 person token을 포함하는지 확인합니다.
 */
public boolean mentionsPersonTokens(final RagSearchResult result, final List<String> tokens) {
    if (result == null || result.getEntity() == null || tokens == null || tokens.isEmpty()) return false;

    final String sourceText = buildPersonFocusSourceText(result);
    for (final String token : tokens) {
        if (StringUtils.isBlank(token)) continue;
        if (StringUtils.contains(sourceText, token)) {
            return true;
        }
    }
    return false;
}

/**
 * KEYWORD 매칭 source에서 person token이 직접 매칭되는 횟수를 계산합니다.
 */
public Integer countDirectPersonTokenMatches(final RagSearchResult result, final PersonFocus personFocus) {
    if (result == null || personFocus == null || result.getMatchedTokens() == null) return 0;

    int count = 0;
    for (final String matchedToken : result.getMatchedTokens()) {
        if (personFocus.tokens().contains(PersonQueryClassifier.stripTrailingJosa(StringUtils.trimToEmpty(matchedToken)))) {
            count++;
        }
    }
    return count;
}

/**
 * person focus source 매칭에 사용할 보조 텍스트를 수집합니다.
 */
public String buildPersonFocusSourceText(final RagSearchResult result) {
    if (result == null || result.getEntity() == null) return "";

    final StringBuilder sb = new StringBuilder();
    appendSourcePart(sb, result.getEntity().getEmbeddingText());
    appendSourcePart(sb, result.getSnippet());
    appendSourcePart(sb, String.join(" ", extractSourceTags(result)));

    final Map<String, Object> payload = readEmbeddingPayload(result);
    appendSourcePart(sb, payload.get("chapterTitle"));
    appendSourcePart(sb, payload.get("journalChapterPrefixName"));
    appendSourcePart(sb, payload.get("entryTitle"));
    appendSourcePart(sb, payload.get("dreamProviderName"));
    return sb.toString();
}

/**
 * source 텍스트를 person focus 검색용 문자열에 추가합니다.
 */
public void appendSourcePart(final StringBuilder sb, final Object value) {
    final String text = StringUtils.defaultString(value == null ? null : String.valueOf(value));
    if (StringUtils.isBlank(text)) return;
    if (sb.length() > 0) sb.append(' ');
    sb.append(text);
}
/**
 * 사용자 응답용 태그 표시 문자열에서 [엔서클] 등 메타 접두를 제거합니다.
 */
public String formatDisplayTag(final String rawTag) {
    if (StringUtils.isBlank(rawTag)) return "";
    String normalized = StringUtils.normalizeSpace(rawTag).replaceAll("\\[[^\\]]+\\]", "");
    final int hashIndex = normalized.indexOf('#');
    if (hashIndex >= 0) {
        normalized = normalized.substring(hashIndex);
    }
    return StringUtils.trim(normalized);
}
/**
 * RAG source payload에서 태그 목록을 추출합니다.
 */
public List<String> extractSourceTags(final RagSearchResult result) {
    if (result == null || result.getEntity() == null) return List.of();
    final Map<String, Object> payload = readEmbeddingPayload(result);
    final Object rawTags = payload.get("tags");
    if (rawTags == null) return List.of();

    final String tagText = StringUtils.normalizeSpace(String.valueOf(rawTags));
    if (StringUtils.isBlank(tagText)) return List.of();

    final List<String> tagList = new ArrayList<>();
    for (final String token : tagText.split("\\s+")) {
        if (StringUtils.isBlank(token)) continue;
        tagList.add(token);
    }
    return tagList;
}

/**
 * 임베딩 payload JSON을 Map으로 변환합니다.
 */
public Map<String, Object> readEmbeddingPayload(final RagSearchResult result) {
    if (result == null || result.getEntity() == null || StringUtils.isBlank(result.getEntity().getEmbeddingPayloadJson())) {
        return Map.of();
    }
    try {
        return objectMapper.readValue(result.getEntity().getEmbeddingPayloadJson(), new TypeReference<Map<String, Object>>() {});
    } catch (final Exception e) {
        log.debug("Failed to parse RAG source payload. journalEntryId={}", result.getJournalEntryId(), e);
        return Map.of();
    }
}
/**
 * person-meaning 해석에 쓸 인물 태그만 남깁니다.
 *
 * <p>인물 축 태그에는 canonical/surface 이름(예: 김민수)이 포함된다는 Dreamdiary 태그 계약을 따릅니다.</p>
 */
/**
 * person-meaning 해석에 쓸 인물 태그만 남깁니다.
 */
public List<String> filterPersonMeaningTags(final List<String> tags, final PersonFocus personFocus) {
    return filterPersonMeaningTags(tags, personFocus, null);
}

/**
 * person-meaning 해석에 쓸 인물 태그만 남깁니다.
 *
 * <p>{@code dominantTagStem}이 있으면 짧은 이름 토큰(예: 지연)의 오매칭(#문지연)을 제거합니다.</p>
 */
public List<String> filterPersonMeaningTags(
        final List<String> tags,
        final PersonFocus personFocus,
        final String dominantTagStem
) {
    if (tags == null || tags.isEmpty()) return List.of();
    return tags.stream()
            .filter(tag -> isPersonRelevantTag(tag, personFocus, dominantTagStem))
            .collect(Collectors.toList());
}

/**
 * 태그 문자열에 person focus 토큰이 포함되어 있는지 확인합니다.
 */
public boolean isPersonRelevantTag(final String tag, final PersonFocus personFocus) {
    return isPersonRelevantTag(tag, personFocus, null);
}

/**
 * 태그 문자열이 person focus(및 선택적 dominant stem)와 맞는지 확인합니다.
 */
public boolean isPersonRelevantTag(
        final String tag,
        final PersonFocus personFocus,
        final String dominantTagStem
) {
    if (StringUtils.isBlank(tag) || personFocus == null || personFocus.tokens() == null) return false;

    final String normalizedTag = StringUtils.lowerCase(StringUtils.deleteWhitespace(tag));
    if (StringUtils.contains(normalizedTag, "dreamdiary")) return false;

    if (StringUtils.isNotBlank(dominantTagStem)) {
        final String stem = extractPersonTagStem(tag);
        return StringUtils.isNotBlank(stem)
                && stem.equalsIgnoreCase(StringUtils.deleteWhitespace(dominantTagStem));
    }

    for (final String token : personFocus.tokens()) {
        if (StringUtils.isBlank(token)) continue;
        final String normalizedToken = StringUtils.lowerCase(StringUtils.deleteWhitespace(token));
        if (StringUtils.isNotBlank(normalizedToken) && normalizedTag.contains(normalizedToken)) {
            return true;
        }
    }
    return false;
}

/**
 * person 태그에서 # stem만 추출합니다.
 */
public String extractPersonTagStem(final String tag) {
    final String displayTag = formatDisplayTag(tag);
    if (StringUtils.isBlank(displayTag)) return "";
    return displayTag.startsWith("#") ? displayTag.substring(1) : displayTag;
}

/**
 * 짧은 person 토큰(예: 지연)에 대해 반복 빈도가 가장 높은 태그 stem을 고릅니다.
 */
public String resolveDominantPersonTagStem(
        final List<RagSearchResult> results,
        final PersonFocus personFocus
) {
    if (personFocus == null || results == null || results.isEmpty()) return null;

    final String primaryToken = StringUtils.trimToEmpty(personFocus.primaryToken());
    if (StringUtils.isBlank(primaryToken) || primaryToken.length() > 3) return null;

    final Map<String, Integer> stemCounts = new LinkedHashMap<>();
    for (final RagSearchResult result : results) {
        if (result == null) continue;
        for (final String tag : extractSourceTags(result)) {
            if (!isPersonRelevantTag(tag, personFocus, null)) continue;

            final String stem = extractPersonTagStem(tag);
            if (StringUtils.isBlank(stem)) continue;
            if (!stem.equalsIgnoreCase(primaryToken)
                    && !StringUtils.endsWithIgnoreCase(stem, primaryToken)) {
                continue;
            }
            stemCounts.merge(stem, 1, Integer::sum);
        }
    }

    return stemCounts.entrySet().stream()
            .max(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                    .thenComparing(Map.Entry::getKey))
            .map(Map.Entry::getKey)
            .orElse(null);
}

/**
 * Keeps co-occurring scene tags from person-meaning sources while excluding person-focus tags.
 */
public List<String> filterPersonMeaningLinkedContextTags(final List<String> tags, final PersonFocus personFocus) {
    if (tags == null || tags.isEmpty()) return List.of();
    return tags.stream()
            .filter(tag -> !isPersonRelevantTag(tag, personFocus))
            .collect(Collectors.toList());
}
}
