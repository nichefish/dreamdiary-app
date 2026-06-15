package io.nicheblog.dreamdiary.feature.journal.embedding.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.chat.client.OllamaClient;
import io.nicheblog.dreamdiary.feature.journal.embedding.entity.JournalEntryEmbeddingEntity;
import io.nicheblog.dreamdiary.feature.journal.embedding.model.RagSearchResult;
import io.nicheblog.dreamdiary.feature.journal.embedding.repository.jpa.JournalEntryEmbeddingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 저널 엔트리 임베딩 벡터를 메모리에 캐싱하고 cosine similarity로 검색하는 서비스입니다.
 *
 * <p>앱 기동 시 EMBEDDED 상태 벡터 전체를 로드하고,
 * 이후 임베딩 완료/삭제 이벤트에 따라 단건 갱신됩니다.</p>
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalEntryEmbeddingSearchService {

    private static final String STATUS_EMBEDDED = "EMBEDDED";
    private static final int SNIPPET_MAX_LENGTH = 180;
    private static final int TAG_KEYWORD_WEIGHT = 5;
    private static final int CHAPTER_KEYWORD_WEIGHT = 3;
    private static final int BODY_KEYWORD_WEIGHT = 1;
    private static final Set<String> KEYWORD_STOPWORDS = Set.of(
            "대해", "뭐", "뭘", "무엇", "말해", "말해줘", "말해줄", "알려", "알려줘", "설명",
            "누구", "누구냐", "누군지", "어떤", "관련", "맥락", "정보", "기록", "있니", "있어",
            "수", "좀", "해줘", "해줄래", "나의", "내", "에서"
    );
    private static final List<String> KEYWORD_SUFFIXES = List.of(
            "님께", "님에게", "님에", "님을", "님를", "님은", "님는", "님이", "님가", "님과", "님와", "님의",
            "에게", "께", "으로", "부터", "까지", "에서", "에는", "에게는",
            "님", "씨", "에", "을", "를", "은", "는", "이", "가", "과", "와", "도", "만", "의", "로"
    );

    private final JournalEntryEmbeddingRepository repository;
    private final OllamaClient ollamaClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** journalEntryId → 벡터(double[]) 캐시 */
    private final Map<Integer, double[]> vectorCache = new ConcurrentHashMap<>();
    /** journalEntryId → 엔티티 메타데이터 캐시 */
    private final Map<Integer, JournalEntryEmbeddingEntity> metaCache = new ConcurrentHashMap<>();

    /**
     * 앱 기동 시 EMBEDDED 상태 벡터 전체를 메모리 캐시에 로드합니다.
     */
    @PostConstruct
    public void initCache() {
        final long start = System.currentTimeMillis();
        final List<JournalEntryEmbeddingEntity> entityList =
                repository.findAllByEmbeddingStatus(STATUS_EMBEDDED);

        int loaded = 0;
        for (final JournalEntryEmbeddingEntity entity : entityList) {
            if (loadIntoCache(entity)) loaded++;
        }

        log.info("Journal entry embedding cache initialized. loaded={}, elapsed={}ms",
                loaded, System.currentTimeMillis() - start);
    }

    /**
     * 쿼리 텍스트와 의미상 가장 유사한 저널 엔트리를 반환합니다.
     *
     * <p>쿼리를 임베딩한 뒤 캐시된 벡터들과 cosine similarity를 계산하고,
     * {@code retrieval_weight}를 곱한 점수로 정렬해 상위 K개를 반환합니다.</p>
     *
     * @param queryText 검색할 텍스트
     * @param topK 반환할 최대 건수
     * @return 가중치 적용 cosine similarity 점수 내림차순 상위 K개 엔티티
     */
    public List<JournalEntryEmbeddingEntity> search(final String queryText, final int topK) {
        return search(queryText, topK, Double.NEGATIVE_INFINITY);
    }

    /**
     * 쿼리 텍스트와 의미상 가장 유사한 저널 엔트리를 최소 점수 이상으로만 반환합니다.
     *
     * @param queryText 검색할 텍스트
     * @param topK 반환할 최대 건수
     * @param minScore 반환할 최소 가중치 적용 점수
     * @return 최소 점수 이상 결과 중 상위 K개 엔티티
     */
    public List<JournalEntryEmbeddingEntity> search(final String queryText, final int topK, final double minScore) {
        return searchWithScore(queryText, topK, minScore).stream()
                .map(RagSearchResult::getEntity)
                .collect(Collectors.toList());
    }

    /**
     * 쿼리 텍스트와 의미상 가장 유사한 저널 엔트리를 검색 메타데이터와 함께 반환합니다.
     *
     * @param queryText 검색할 텍스트
     * @param topK 반환할 최대 건수
     * @param minScore 반환할 최소 가중치 적용 점수
     * @return 최소 점수 이상 결과 중 상위 K개 RAG 검색 결과
     */
    public List<RagSearchResult> searchWithScore(final String queryText, final int topK, final double minScore) {
        if (StringUtils.isBlank(queryText) || vectorCache.isEmpty()) return Collections.emptyList();

        final double[] queryVector = toDoubleArray(ollamaClient.embed(queryText));

        return vectorCache.entrySet().stream()
                .filter(entry -> metaCache.containsKey(entry.getKey()))
                .filter(entry -> AuthUtils.isCreatedBy(metaCache.get(entry.getKey()).getCreatedBy()))
                .map(entry -> {
                    final double similarity = cosineSimilarity(queryVector, entry.getValue());
                    final JournalEntryEmbeddingEntity meta = metaCache.get(entry.getKey());
                    final double weight = meta.getRetrievalWeight() != null
                            ? meta.getRetrievalWeight().doubleValue() : 1.0;
                    return Map.entry(meta, similarity * weight);
                })
                .filter(entry -> entry.getValue() >= minScore)
                .sorted(Map.Entry.<JournalEntryEmbeddingEntity, Double>comparingByValue().reversed())
                .limit(topK)
                .map(entry -> RagSearchResult.builder()
                        .entity(entry.getKey())
                        .matchType(RagSearchResult.MATCH_TYPE_VECTOR)
                        .score(entry.getValue())
                        .matchedTokens(List.of())
                        .snippet(buildSnippet(entry.getKey().getEmbeddingText(), List.of()))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 사용자 질문에서 추출한 핵심 키워드가 임베딩 텍스트/페이로드에 직접 등장하는 기록을 반환합니다.
     *
     * <p>인명, 태그, 고유명사 질문은 벡터 유사도만으로는 누락되기 쉬우므로
     * RAG 컨텍스트 구성 시 벡터 검색보다 먼저 보강 검색으로 사용합니다.</p>
     *
     * @param queryText 검색할 사용자 메시지
     * @param topK 반환할 최대 건수
     * @return 키워드 직접 매칭 점수 내림차순 상위 K개 엔티티
     */
    public List<JournalEntryEmbeddingEntity> searchByKeyword(final String queryText, final int topK) {
        return searchByKeywordWithScore(queryText, topK).stream()
                .map(RagSearchResult::getEntity)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 질문에서 추출한 핵심 키워드가 직접 등장하는 기록을 검색 메타데이터와 함께 반환합니다.
     *
     * @param queryText 검색할 사용자 메시지
     * @param topK 반환할 최대 건수
     * @return 키워드 직접 매칭 점수 내림차순 상위 K개 RAG 검색 결과
     */
    public List<RagSearchResult> searchByKeywordWithScore(final String queryText, final int topK) {
        final List<String> tokens = extractKeywordTokens(queryText);
        if (tokens.isEmpty() || metaCache.isEmpty()) return Collections.emptyList();

        return metaCache.values().stream()
                .filter(entity -> AuthUtils.isCreatedBy(entity.getCreatedBy()))
                .map(entity -> Map.entry(entity, keywordMatch(entity, tokens)))
                .filter(entry -> entry.getValue().score() > 0)
                .sorted((left, right) -> {
                    final int scoreCompare = Integer.compare(right.getValue().score(), left.getValue().score());
                    if (scoreCompare != 0) return scoreCompare;
                    return Comparator.comparing(
                                    JournalEntryEmbeddingEntity::getJournalDate,
                                    Comparator.nullsLast(Comparator.naturalOrder())
                            )
                            .reversed()
                            .compare(left.getKey(), right.getKey());
                })
                .limit(topK)
                .map(entry -> RagSearchResult.builder()
                        .entity(entry.getKey())
                        .matchType(RagSearchResult.MATCH_TYPE_KEYWORD)
                        .score((double) entry.getValue().score())
                        .matchedTokens(entry.getValue().matchedTokens())
                        .snippet(buildSnippet(entry.getKey().getEmbeddingText(), entry.getValue().matchedTokens()))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * person-meaning 질문에서 payload 태그에 인물 토큰이 직접 포함된 기록을 우선 검색합니다.
     *
     * <p>Dreamdiary 태그 계약상 인물 축 태그에는 canonical/surface 이름(예: 김원빈)이 포함되므로,
     * 본문 공출현보다 태그 매칭을 1순위 신호로 사용합니다.</p>
     *
     * @param personTokens 인물 focus 토큰 목록
     * @param topK 반환할 최대 건수
     * @return 태그 매칭 점수 내림차순 상위 결과
     */
    public List<RagSearchResult> searchByPersonTagsWithScore(final Collection<String> personTokens, final int topK) {
        if (personTokens == null || personTokens.isEmpty() || metaCache.isEmpty()) {
            return Collections.emptyList();
        }

        final List<String> normalizedTokens = personTokens.stream()
                .filter(StringUtils::isNotBlank)
                .map(this::normalizeKeywordToken)
                .filter(token -> token.length() >= 2)
                .distinct()
                .collect(Collectors.toList());
        if (normalizedTokens.isEmpty()) return Collections.emptyList();

        return metaCache.values().stream()
                .filter(entity -> AuthUtils.isCreatedBy(entity.getCreatedBy()))
                .map(entity -> {
                    final Map<String, Object> payload = readPayloadMap(entity);
                    final String tagHaystack = normalizeHaystack(payloadString(payload, "tags"));
                    if (StringUtils.isBlank(tagHaystack)) return null;

                    int score = 0;
                    final List<String> matchedTokens = new ArrayList<>();
                    for (final String token : normalizedTokens) {
                        if (!containsToken(tagHaystack, token)) continue;
                        score += TAG_KEYWORD_WEIGHT;
                        matchedTokens.add(token);
                    }
                    if (score <= 0) return null;
                    return Map.entry(entity, Map.entry(score, matchedTokens));
                })
                .filter(Objects::nonNull)
                .sorted((left, right) -> {
                    final int scoreCompare = Integer.compare(right.getValue().getKey(), left.getValue().getKey());
                    if (scoreCompare != 0) return scoreCompare;
                    return Comparator.comparing(
                                    JournalEntryEmbeddingEntity::getJournalDate,
                                    Comparator.nullsLast(Comparator.naturalOrder())
                            )
                            .reversed()
                            .compare(left.getKey(), right.getKey());
                })
                .limit(topK)
                .map(entry -> RagSearchResult.builder()
                        .entity(entry.getKey())
                        .matchType(RagSearchResult.MATCH_TYPE_TAG)
                        .score((double) entry.getValue().getKey())
                        .matchedTokens(entry.getValue().getValue())
                        .snippet(buildSnippet(entry.getKey().getEmbeddingText(), entry.getValue().getValue()))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * entity catalog에 연결된 저널 엔트리 ID 목록으로 RAG source를 직접 조회합니다.
     *
     * <p>person-meaning 질문에서 벡터 점수가 낮아 누락된 직접 연결 기록을 보강할 때 사용합니다.</p>
     *
     * @param journalEntryIds 조회할 저널 엔트리 ID 목록
     * @return 현재 사용자 소유이며 캐시에 존재하는 엔트리의 RAG 검색 결과
     */
    public List<RagSearchResult> findByJournalEntryIds(final Collection<Integer> journalEntryIds) {
        if (journalEntryIds == null || journalEntryIds.isEmpty() || metaCache.isEmpty()) {
            return Collections.emptyList();
        }

        final List<RagSearchResult> results = new ArrayList<>();
        for (final Integer journalEntryId : journalEntryIds) {
            if (journalEntryId == null) continue;

            final JournalEntryEmbeddingEntity entity = metaCache.get(journalEntryId);
            if (entity == null || !AuthUtils.isCreatedBy(entity.getCreatedBy())) continue;

            results.add(RagSearchResult.builder()
                    .entity(entity)
                    .matchType(RagSearchResult.MATCH_TYPE_ENTITY)
                    .score(1.0D)
                    .matchedTokens(List.of())
                    .snippet(buildSnippet(entity.getEmbeddingText(), List.of()))
                    .build());
        }
        return results;
    }

    /**
     * 단일 저널 엔트리의 캐시를 갱신합니다. 임베딩 완료 시 호출합니다.
     *
     * @param journalEntryId 갱신할 저널 엔트리 ID
     */
    public void refreshEntry(final Integer journalEntryId) {
        if (journalEntryId == null) return;
        repository.findFirstByJournalEntryId(journalEntryId).ifPresent(entity -> {
            if (STATUS_EMBEDDED.equals(entity.getEmbeddingStatus())) {
                loadIntoCache(entity);
            } else {
                vectorCache.remove(journalEntryId);
                metaCache.remove(journalEntryId);
            }
        });
    }

    /**
     * 단일 저널 엔트리를 캐시에서 제거합니다. 저널 엔트리 삭제 시 호출합니다.
     *
     * @param journalEntryId 제거할 저널 엔트리 ID
     */
    public void removeEntry(final Integer journalEntryId) {
        if (journalEntryId == null) return;
        vectorCache.remove(journalEntryId);
        metaCache.remove(journalEntryId);
    }

    /**
     * 품질 실측·운영 진단용: 메모리 캐시에 로드된 벡터 건수.
     */
    public int getCachedVectorCount() {
        return vectorCache.size();
    }

    /**
     * 품질 실측용: 캐시된 저널 엔트리 ID를 정렬·상한 샘플링합니다.
     */
    public List<Integer> sampleCachedJournalEntryIds(final int limit) {
        if (limit <= 0 || vectorCache.isEmpty()) return List.of();
        return vectorCache.keySet().stream()
                .sorted()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 품질 실측용: 캐시된 벡터를 조회합니다.
     */
    public Optional<double[]> getCachedVector(final Integer journalEntryId) {
        if (journalEntryId == null) return Optional.empty();
        return Optional.ofNullable(vectorCache.get(journalEntryId));
    }

    /**
     * 품질 실측용: 캐시된 임베딩 메타를 조회합니다.
     */
    public Optional<JournalEntryEmbeddingEntity> getCachedMeta(final Integer journalEntryId) {
        if (journalEntryId == null) return Optional.empty();
        return Optional.ofNullable(metaCache.get(journalEntryId));
    }

    /**
     * 엔티티를 벡터/메타 캐시에 로드합니다.
     *
     * @param entity 로드할 임베딩 엔티티
     * @return 로드 성공 여부
     */
    private boolean loadIntoCache(final JournalEntryEmbeddingEntity entity) {
        if (entity.getJournalEntryId() == null || StringUtils.isBlank(entity.getEmbeddingVectorJson())) {
            return false;
        }
        try {
            final List<Double> vectorList = objectMapper.readValue(
                    entity.getEmbeddingVectorJson(), new TypeReference<List<Double>>() {});
            vectorCache.put(entity.getJournalEntryId(), toDoubleArray(vectorList));
            metaCache.put(entity.getJournalEntryId(), entity);
            return true;
        } catch (final Exception e) {
            log.warn("Failed to load vector for journalEntryId={}", entity.getJournalEntryId(), e);
            return false;
        }
    }

    /**
     * 질문 문장에서 인명/고유명사 후보를 추출합니다.
     */
    private List<String> extractKeywordTokens(final String queryText) {
        if (StringUtils.isBlank(queryText)) return Collections.emptyList();
        return Arrays.stream(StringUtils.defaultString(queryText)
                        .replaceAll("[^\\p{L}\\p{N}\\s]", " ")
                        .split("\\s+"))
                .map(this::normalizeKeywordToken)
                .filter(token -> token.length() >= 2)
                .filter(token -> !KEYWORD_STOPWORDS.contains(token))
                .distinct()
                .limit(8)
                .collect(Collectors.toList());
    }

    /**
     * 한국어 호칭/조사를 제거해 검색 키워드로 정규화합니다.
     */
    private String normalizeKeywordToken(final String rawToken) {
        String token = StringUtils.trimToEmpty(rawToken).toLowerCase(Locale.ROOT);
        boolean changed = true;
        while (changed && token.length() > 1) {
            changed = false;
            for (final String suffix : KEYWORD_SUFFIXES) {
                if (token.length() > suffix.length() + 1 && token.endsWith(suffix)) {
                    token = token.substring(0, token.length() - suffix.length());
                    changed = true;
                    break;
                }
            }
        }
        return token;
    }

    /**
     * 추출된 키워드가 임베딩 텍스트 또는 payload에 직접 등장하는 정도를 계산합니다.
     */
    private KeywordMatch keywordMatch(final JournalEntryEmbeddingEntity entity, final List<String> tokens) {
        final Map<String, Object> payload = readPayloadMap(entity);
        final String tagHaystack = normalizeHaystack(payloadString(payload, "tags"));
        final String chapterHaystack = normalizeHaystack(String.join("\n",
                payloadString(payload, "title"),
                payloadString(payload, "journalChapterTitle"),
                payloadString(payload, "journalChapterCategoryName"),
                payloadString(payload, "journalChapterCategoryCode")
        ));
        final String bodyHaystack = normalizeHaystack(StringUtils.defaultString(entity.getEmbeddingText())
                + "\n"
                + StringUtils.defaultString(entity.getEmbeddingPayloadJson()));

        int score = 0;
        final List<String> matchedTokens = new ArrayList<>();
        for (final String token : tokens) {
            boolean matched = false;
            if (containsToken(tagHaystack, token)) {
                score += TAG_KEYWORD_WEIGHT;
                matched = true;
            }
            if (containsToken(chapterHaystack, token)) {
                score += CHAPTER_KEYWORD_WEIGHT;
                matched = true;
            }
            if (containsToken(bodyHaystack, token)) {
                score += BODY_KEYWORD_WEIGHT;
                matched = true;
            }
            if (matched) matchedTokens.add(token);
        }
        return new KeywordMatch(score, matchedTokens);
    }

    /**
     * payload JSON을 검색 가중치 계산용 Map으로 변환합니다.
     */
    private Map<String, Object> readPayloadMap(final JournalEntryEmbeddingEntity entity) {
        if (entity == null || StringUtils.isBlank(entity.getEmbeddingPayloadJson())) return Map.of();
        try {
            return objectMapper.readValue(entity.getEmbeddingPayloadJson(), new TypeReference<Map<String, Object>>() {});
        } catch (final Exception e) {
            log.debug("Failed to parse embedding payload for keyword weighting. journalEntryId={}", entity.getJournalEntryId(), e);
            return Map.of();
        }
    }

    /**
     * payload 필드를 문자열로 꺼냅니다.
     */
    private String payloadString(final Map<String, Object> payload, final String fieldName) {
        if (payload == null || !payload.containsKey(fieldName)) return "";
        final Object value = payload.get(fieldName);
        return value == null ? "" : String.valueOf(value);
    }

    /**
     * 검색 대상 문자열을 정규화합니다.
     */
    private String normalizeHaystack(final String text) {
        return StringUtils.defaultString(text).toLowerCase(Locale.ROOT);
    }

    /**
     * 정규화된 검색 대상에 토큰이 포함되어 있는지 확인합니다.
     */
    private boolean containsToken(final String haystack, final String token) {
        return StringUtils.isNotBlank(haystack) && StringUtils.isNotBlank(token) && haystack.contains(token);
    }

    /**
     * 검색 결과 확인용 스니펫을 구성합니다.
     */
    private String buildSnippet(final String text, final List<String> matchedTokens) {
        final String normalizedText = StringUtils.normalizeSpace(text);
        if (StringUtils.isBlank(normalizedText)) return "";

        for (final String token : matchedTokens) {
            final int index = StringUtils.indexOfIgnoreCase(normalizedText, token);
            if (index < 0) continue;

            final int start = Math.max(0, index - (SNIPPET_MAX_LENGTH / 3));
            final int end = Math.min(normalizedText.length(), start + SNIPPET_MAX_LENGTH);
            final String prefix = start > 0 ? "..." : "";
            final String suffix = end < normalizedText.length() ? "..." : "";
            return prefix + normalizedText.substring(start, end) + suffix;
        }

        return StringUtils.abbreviate(normalizedText, SNIPPET_MAX_LENGTH);
    }

    /**
     * 키워드 매칭 점수와 직접 매칭된 토큰 목록.
     */
    private record KeywordMatch(int score, List<String> matchedTokens) {}

    /**
     * 두 벡터의 cosine similarity를 계산합니다.
     *
     * @param a 첫 번째 벡터
     * @param b 두 번째 벡터
     * @return cosine similarity 값 (-1.0 ~ 1.0)
     */
    private double cosineSimilarity(final double[] a, final double[] b) {
        double dot = 0, normA = 0, normB = 0;
        final int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) return 0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * {@code List<Double>}을 {@code double[]}으로 변환합니다.
     *
     * @param list 변환할 Double 리스트
     * @return double 배열
     */
    private double[] toDoubleArray(final List<Double> list) {
        return list.stream().mapToDouble(Double::doubleValue).toArray();
    }
}
