package io.nicheblog.dreamdiary.feature.ai.person;

import io.nicheblog.dreamdiary.feature.journal.embedding.model.RagSearchResult;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.service.JournalEntityFocusService;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.type.JournalEntityRoleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 인물 의미/태도 SNAPSHOT 집계.
 *
 * <p>{@link PersonFocusResolver}의 태그·focus 헬퍼를 재사용한다. Path C hybrid 오케스트레이션은 PersonSynthesisHybridService가 담당하며, 언어·person 가드는 {@code feature.ai.guard.ResponseGuardService}가 구현한다.</p>
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class PersonSnapshotService {

/** person-meaning/hybrid SNAPSHOT에 실을 근거 장면 최대 건수 */
private static final int PERSON_MEANING_SNAPSHOT_EVIDENCE_LIMIT = 3;
/** person-stance(태도) 질문 SNAPSHOT 근거 장면 최대 건수 */
private static final int PERSON_STANCE_SNAPSHOT_EVIDENCE_LIMIT = 20;
/** person-stance SNAPSHOT 근거 장면 1건 최대 길이 */
private static final int PERSON_STANCE_SNAPSHOT_SNIPPET_MAX_LENGTH = 400;
/** person-stance SNAPSHOT 근거 장면 총 글자 상한 */
private static final int PERSON_STANCE_SNAPSHOT_EVIDENCE_CHAR_BUDGET = 8000;
/** entity catalog 역할 축을 한국어 해석 라벨로 변환 */
private static final Map<JournalEntityRoleType, String> PERSON_ROLE_AXIS_LABELS = Map.ofEntries(
        Map.entry(JournalEntityRoleType.COLLABORATION, "협업·동행 축"),
        Map.entry(JournalEntityRoleType.TENSION, "긴장·경계 축"),
        Map.entry(JournalEntityRoleType.EVALUATION, "평가·인정 축"),
        Map.entry(JournalEntityRoleType.CARE, "위로·보호 축"),
        Map.entry(JournalEntityRoleType.CONFLICT, "갈등·대립 축"),
        Map.entry(JournalEntityRoleType.DESIRE, "원함·끌임 축"),
        Map.entry(JournalEntityRoleType.SYMBOLIC_FIGURE, "상징·대상 축"),
        Map.entry(JournalEntityRoleType.UNKNOWN, "미분류 축")
);

    private final PersonFocusResolver personFocusResolver;

/**
 * entity catalog 역할 축을 한국어 해석 라벨 목록으로 변환합니다.
 */
public List<String> formatPersonRoleAxes(final Map<JournalEntityRoleType, Integer> roleCountMap) {
    if (roleCountMap == null || roleCountMap.isEmpty()) return List.of();

    return roleCountMap.entrySet().stream()
            .sorted(Map.Entry.<JournalEntityRoleType, Integer>comparingByValue().reversed())
            .limit(6)
            .map(entry -> formatPersonRoleAxis(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
}

/**
 * 단일 역할 축을 빈도와 함께 한국어 라벨로 포맷합니다.
 */
public String formatPersonRoleAxis(final JournalEntityRoleType roleType, final int count) {
    final String label = PERSON_ROLE_AXIS_LABELS.getOrDefault(roleType, roleType.name());
    return label + "(" + count + ")";
}
/**
 * 태그 카운트를 누적합니다.
 */
private void incrementTagCounts(final Map<String, Integer> tagCountMap, final List<String> tags) {
    for (final String tag : tags) {
        if (StringUtils.isBlank(tag)) continue;
        tagCountMap.merge(tag, 1, Integer::sum);
    }
}

/**
 * Aggregates linked context tags and chapter Prefix values for person-meaning fallback/scaffold.
 */
private PersonMeaningContextAggregates buildPersonMeaningContextAggregates(
        final List<RagSearchResult> focusedResults,
        final PersonFocus personFocus
) {
    final Map<String, Integer> linkedContextTagCountMap = new LinkedHashMap<>();
    final Map<String, Integer> chapterPrefixCountMap = new LinkedHashMap<>();
    if (focusedResults == null || focusedResults.isEmpty()) {
        return new PersonMeaningContextAggregates(linkedContextTagCountMap, chapterPrefixCountMap);
    }

    for (final RagSearchResult result : focusedResults) {
        if (result == null) continue;
        incrementTagCounts(
                linkedContextTagCountMap,
                personFocusResolver.filterPersonMeaningLinkedContextTags(personFocusResolver.extractSourceTags(result), personFocus)
        );
        final String chapterPrefix = extractSourceChapterPrefix(result);
        if (StringUtils.isNotBlank(chapterPrefix)) {
            chapterPrefixCountMap.merge(chapterPrefix, 1, Integer::sum);
        }
    }
    return new PersonMeaningContextAggregates(linkedContextTagCountMap, chapterPrefixCountMap);
}

/**
 * Reads the selected chapter Prefix name from a RAG source embedding payload.
 */
private String extractSourceChapterPrefix(final RagSearchResult result) {
    if (result == null || result.getEntity() == null) return "";
    final Map<String, Object> payload = personFocusResolver.readEmbeddingPayload(result);
    return StringUtils.trimToEmpty(String.valueOf(payload.getOrDefault("journalChapterPrefixName", "")));
}
/**
 * Holds linked context tag and chapter Prefix counts for person-meaning aggregation.
 */
private record PersonMeaningContextAggregates(
        Map<String, Integer> linkedContextTagCountMap,
        Map<String, Integer> chapterPrefixCountMap
) {}
/**
 * Returns sources that carry person tags for person-meaning aggregation.
 */
public List<RagSearchResult> resolvePersonFocusedResults(
        final List<RagSearchResult> results,
        final PersonFocus personFocus
) {
    return resolvePersonFocusedResults(results, personFocus, null);
}

/**
 * Returns sources that carry person tags for person-meaning aggregation.
 */
public List<RagSearchResult> resolvePersonFocusedResults(
        final List<RagSearchResult> results,
        final PersonFocus personFocus,
        final String dominantTagStem
) {
    if (personFocus == null || results == null || results.isEmpty()) return List.of();

    final List<RagSearchResult> tagFocusedResults = results.stream()
            .filter(result -> !personFocusResolver.filterPersonMeaningTags(
                    personFocusResolver.extractSourceTags(result),
                    personFocus,
                    dominantTagStem
            ).isEmpty())
            .collect(Collectors.toList());
    if (!tagFocusedResults.isEmpty()) return tagFocusedResults;

    return List.of();
}

/**
 * person-meaning tag-only RAG 결과만으로 스냅샷을 집계하는지 확인합니다.
 *
 * <p>이 경우 entity catalog의 전역 role/kind/date 집계는 쓰지 않고 태그 매칭 source만 사용합니다.</p>
 */
public boolean isTagOnlyPersonMeaningResults(final List<RagSearchResult> results) {
    if (results == null || results.isEmpty()) return false;
    for (final RagSearchResult result : results) {
        if (result == null) continue;
        if (!RagSearchResult.MATCH_TYPE_TAG.equals(result.getMatchType())) {
            return false;
        }
    }
    return true;
}
/**
 * 질문 유형별 SNAPSHOT 예산으로 PERSON_MEANING_SCAFFOLD 재료를 집계합니다.
 *
 * <p>태도 질문은 근거 장면 건수·길이·총량을 키워 기록 본문을 더 많이 LLM에 전달합니다.</p>
 */
public PersonMeaningSnapshot build(
        final List<RagSearchResult> resultsIn,
        final PersonFocus personFocus,
        final String queryText
) {
    final PersonMeaningSnapshotOptions options = resolvePersonMeaningSnapshotOptions(queryText);
    final List<RagSearchResult> results = resultsIn == null ? List.of() : resultsIn;
    final String dominantTagStem = personFocusResolver.resolveDominantPersonTagStem(results, personFocus);
    final List<RagSearchResult> focusedResults = resolvePersonFocusedResults(results, personFocus, dominantTagStem);

    final Map<String, Integer> focusedTagCountMap = new LinkedHashMap<>();
    for (final RagSearchResult result : focusedResults) {
        incrementTagCounts(
                focusedTagCountMap,
                personFocusResolver.filterPersonMeaningTags(personFocusResolver.extractSourceTags(result), personFocus, dominantTagStem).stream()
                        .distinct()
                        .collect(Collectors.toList())
        );
    }

    final JournalEntityFocusService.PersonEntityFocusSummary entitySummary =
            personFocus == null ? null : personFocus.entitySummary();
    final TimelineSummary timelineSummary = buildTimelineSummary(focusedResults);
    final boolean tagOnlyAggregation = isTagOnlyPersonMeaningResults(focusedResults);

    final Map<String, Integer> contentKindCountMap = new LinkedHashMap<>();
    if (!tagOnlyAggregation
            && entitySummary != null
            && entitySummary.contentKindCountMap() != null) {
        contentKindCountMap.putAll(entitySummary.contentKindCountMap());
    } else if (!timelineSummary.contentKindCountMap().isEmpty()) {
        contentKindCountMap.putAll(timelineSummary.contentKindCountMap());
    }

    final List<String> roleAxesKo = !tagOnlyAggregation && entitySummary != null
            ? formatPersonRoleAxes(entitySummary.roleCountMap())
            : List.of();

    final PersonMeaningContextAggregates contextAggregates =
            buildPersonMeaningContextAggregates(focusedResults, personFocus);

    final List<String> evidenceSnippets = new ArrayList<>();
    int evidenceChars = 0;
    final List<Integer> evidenceIndices = resolveEvidenceSourceIndices(
            focusedResults.size(),
            options.evidenceLimit(),
            options.spreadEvidenceAcrossSources()
    );
    for (final int evidenceIndex : evidenceIndices) {
        if (evidenceSnippets.size() >= options.evidenceLimit()) {
            break;
        }
        final String evidenceSnippet = sanitizePersonMeaningSnippet(
                focusedResults.get(evidenceIndex),
                personFocus,
                options.snippetMaxLength()
        );
        if (StringUtils.isBlank(evidenceSnippet) || evidenceSnippets.contains(evidenceSnippet)) {
            continue;
        }
        if (evidenceChars + evidenceSnippet.length() > options.evidenceCharBudget()) {
            final int remaining = options.evidenceCharBudget() - evidenceChars;
            if (remaining < 40) {
                continue;
            }
            final String trimmed = StringUtils.abbreviate(evidenceSnippet, remaining);
            if (StringUtils.isBlank(trimmed)) {
                continue;
            }
            evidenceSnippets.add(trimmed);
            break;
        }
        evidenceSnippets.add(evidenceSnippet);
        evidenceChars += evidenceSnippet.length();
    }

    final String firstDate = !tagOnlyAggregation
            && entitySummary != null
            && StringUtils.isNotBlank(entitySummary.firstDate())
            ? entitySummary.firstDate()
            : timelineSummary.firstDate();
    final String lastDate = !tagOnlyAggregation
            && entitySummary != null
            && StringUtils.isNotBlank(entitySummary.lastDate())
            ? entitySummary.lastDate()
            : timelineSummary.lastDate();

    return new PersonMeaningSnapshot(
            focusedTagCountMap,
            roleAxesKo,
            contentKindCountMap,
            contextAggregates.linkedContextTagCountMap(),
            contextAggregates.chapterPrefixCountMap(),
            evidenceSnippets,
            firstDate,
            lastDate
    );
}
public static final int RULE_PRIMARY_EVIDENCE_MAX_LENGTH = 100;

/**
 * person-meaning fallback용 근거 장면 텍스트를 정리합니다.
 *
 * <p>embedding_text 메타라인/HTML을 제거하고 본문 중심으로 축약합니다.</p>
 */
public String sanitizePersonMeaningSnippet(final RagSearchResult result, final PersonFocus personFocus) {
    return sanitizePersonMeaningSnippet(result, personFocus, RULE_PRIMARY_EVIDENCE_MAX_LENGTH);
}

/**
 * SNAPSHOT 근거 장면 텍스트를 정리합니다.
 *
 * <p>태도 질문은 {@code maxLength}를 키워 인물 주변 본문을 더 넓게 잘라냅니다.</p>
 */
public String sanitizePersonMeaningSnippet(
        final RagSearchResult result,
        final PersonFocus personFocus,
        final int maxLength
) {
    if (result == null || result.getEntity() == null) return "";

    String body = extractEmbeddingBodyText(result.getEntity().getEmbeddingText());
    if (StringUtils.isBlank(body)) {
        body = stripHtmlForPersonMeaning(StringUtils.defaultIfBlank(result.getSnippet(), ""));
    } else {
        body = stripHtmlForPersonMeaning(body);
    }
    body = StringUtils.normalizeSpace(body);
    if (StringUtils.isBlank(body)) return "";

    if (personFocus != null) {
        final List<String> tokenOrder = new ArrayList<>();
        if (StringUtils.isNotBlank(personFocus.primaryToken())) {
            tokenOrder.add(personFocus.primaryToken());
        }
        if (personFocus.tokens() != null) {
            for (final String token : personFocus.tokens()) {
                if (StringUtils.isBlank(token) || tokenOrder.contains(token)) continue;
                tokenOrder.add(token);
            }
        }
        for (final String token : tokenOrder) {
            if (StringUtils.isBlank(token)) continue;
            final int index = StringUtils.indexOfIgnoreCase(body, token);
            if (index < 0) continue;

            final int contextBefore = Math.max(40, maxLength / 3);
            final int contextAfter = Math.max(80, maxLength - contextBefore - 12);
            final int start = Math.max(0, index - contextBefore);
            final int end = Math.min(body.length(), index + contextAfter);
            final String prefix = start > 0 ? "..." : "";
            final String suffix = end < body.length() ? "..." : "";
            return StringUtils.abbreviate(
                    prefix + body.substring(start, end) + suffix,
                    maxLength
            );
        }
    }

    return StringUtils.abbreviate(body, maxLength);
}
/**
 * embedding_text에서 본문 라인만 추출합니다.
 */
private String extractEmbeddingBodyText(final String embeddingText) {
    if (StringUtils.isBlank(embeddingText)) return "";

    final int markerIndex = embeddingText.indexOf("본문:");
    if (markerIndex < 0) return "";

    final String body = embeddingText.substring(markerIndex + "본문:".length()).trim();
    final int nextLabelIndex = body.indexOf("\n유형:");
    if (nextLabelIndex > 0) return body.substring(0, nextLabelIndex).trim();
    return body;
}

/**
 * person-meaning 스니펫에서 HTML/엔티티를 제거합니다.
 */
private String stripHtmlForPersonMeaning(final String text) {
    if (StringUtils.isBlank(text)) return "";
    return StringUtils.normalizeSpace(
            StringUtils.defaultString(text)
                    .replace("&nbsp;", " ")
                    .replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replaceAll("<[^>]+>", " ")
    );
}
/**
 * person-meaning SNAPSHOT 집계·근거 예산.
 *
 * <p>태도 질문은 기록 본문을 더 많이 실어 사용자 정서 비춤을 돕습니다.</p>
 */
public record PersonMeaningSnapshotOptions(
        int evidenceLimit,
        int snippetMaxLength,
        int evidenceCharBudget,
        boolean spreadEvidenceAcrossSources
) {
    public static PersonMeaningSnapshotOptions defaults() {
        return new PersonMeaningSnapshotOptions(
                PERSON_MEANING_SNAPSHOT_EVIDENCE_LIMIT,
                RULE_PRIMARY_EVIDENCE_MAX_LENGTH,
                Integer.MAX_VALUE,
                false
        );
    }

    public static PersonMeaningSnapshotOptions personStanceRich() {
        return new PersonMeaningSnapshotOptions(
                PERSON_STANCE_SNAPSHOT_EVIDENCE_LIMIT,
                PERSON_STANCE_SNAPSHOT_SNIPPET_MAX_LENGTH,
                PERSON_STANCE_SNAPSHOT_EVIDENCE_CHAR_BUDGET,
                true
        );
    }
}

/**
 * 질문 유형별 SNAPSHOT 근거 예산을 선택합니다.
 */
public PersonMeaningSnapshotOptions resolvePersonMeaningSnapshotOptions(final String queryText) {
    if (PersonQueryClassifier.isPersonAttitudeQuery(queryText)) {
        return PersonMeaningSnapshotOptions.personStanceRich();
    }
    return PersonMeaningSnapshotOptions.defaults();
}

/**
 * SNAPSHOT 근거 장면을 여러 source에 고르게 샘플링할 인덱스를 만듭니다.
 */
private List<Integer> resolveEvidenceSourceIndices(
        final int sourceCount,
        final int evidenceLimit,
        final boolean spreadAcrossSources
) {
    if (sourceCount <= 0 || evidenceLimit <= 0) {
        return List.of();
    }
    final int pickCount = Math.min(sourceCount, evidenceLimit);
    if (!spreadAcrossSources || sourceCount <= pickCount) {
        return java.util.stream.IntStream.range(0, pickCount).boxed().collect(Collectors.toList());
    }

    final List<Integer> indices = new ArrayList<>(pickCount);
    for (int picked = 0; picked < pickCount; picked++) {
        final int index = (int) Math.round((double) picked * (sourceCount - 1) / Math.max(1, pickCount - 1));
        if (indices.isEmpty() || index > indices.get(indices.size() - 1)) {
            indices.add(index);
        }
    }
    for (int fallback = 0; indices.size() < pickCount && fallback < sourceCount; fallback++) {
        if (!indices.contains(fallback)) {
            indices.add(fallback);
        }
    }
    indices.sort(Integer::compareTo);
    return indices;
}
/**
 * RAG source들의 시간/유형 흐름을 집계합니다.
 */
private TimelineSummary buildTimelineSummary(final List<RagSearchResult> results) {
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
            monthCountMap.merge(journalDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")), 1, Integer::sum);
        }
    }

    dateList.sort(LocalDate::compareTo);
    final String firstDate = dateList.isEmpty() ? null : dateList.get(0).toString();
    final String lastDate = dateList.isEmpty() ? null : dateList.get(dateList.size() - 1).toString();
    return new TimelineSummary(results == null ? 0 : results.size(), firstDate, lastDate, contentKindCountMap, monthCountMap);
}

    /**
     * SNAPSHOT용 시간/유형 흐름 요약(채팅 TimelineSummary와 동일 집계, person 패키지 전용).
     */
    private record TimelineSummary(
            int sourceCount,
            String firstDate,
            String lastDate,
            Map<String, Integer> contentKindCountMap,
            Map<String, Integer> monthCountMap
    ) {}
}
