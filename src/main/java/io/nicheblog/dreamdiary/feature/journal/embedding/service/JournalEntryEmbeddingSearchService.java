package io.nicheblog.dreamdiary.feature.journal.embedding.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nicheblog.dreamdiary.feature.chat.client.OllamaClient;
import io.nicheblog.dreamdiary.feature.journal.embedding.entity.JournalEntryEmbeddingEntity;
import io.nicheblog.dreamdiary.feature.journal.embedding.repository.jpa.JournalEntryEmbeddingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
        if (StringUtils.isBlank(queryText) || vectorCache.isEmpty()) return Collections.emptyList();

        final double[] queryVector = toDoubleArray(ollamaClient.embed(queryText));

        return vectorCache.entrySet().stream()
                .filter(entry -> metaCache.containsKey(entry.getKey()))
                .map(entry -> {
                    final double similarity = cosineSimilarity(queryVector, entry.getValue());
                    final JournalEntryEmbeddingEntity meta = metaCache.get(entry.getKey());
                    final double weight = meta.getRetrievalWeight() != null
                            ? meta.getRetrievalWeight().doubleValue() : 1.0;
                    return Map.entry(meta, similarity * weight);
                })
                .sorted(Map.Entry.<JournalEntryEmbeddingEntity, Double>comparingByValue().reversed())
                .limit(topK)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
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