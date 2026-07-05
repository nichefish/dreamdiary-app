package io.nicheblog.dreamdiary.feature.journal.embedding.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nicheblog.dreamdiary.feature.chat.client.OllamaClient;
import io.nicheblog.dreamdiary.feature.journal.embedding.entity.JournalEntryEmbeddingEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 저널 엔트리 임베딩 작업을 실제로 벡터화하는 워커 서비스입니다.
 *
 * <p>큐에서 대기 작업을 선점하고, Ollama 임베딩 API를 호출해 벡터 JSON을 저장한다.
 * Ollama 연결 실패처럼 배치 전체에 영향을 주는 오류는 남은 작업을 다시 대기 상태로 되돌린다.</p>
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalEntryEmbeddingWorker {

    private static final int DEFAULT_BATCH_SIZE = 20;
    private static final int MAX_EMBEDDING_CHUNK_CHARS = 1600;
    private static final Duration STALE_PROCESSING_AGE = Duration.ofMinutes(30);
    private static final String OLLAMA_CONTEXT_LENGTH_ERROR = "input length exceeds the context length";

    private final JournalEntryEmbeddingQueueService queueService;
    private final JournalEntryEmbeddingSearchService searchService;
    private final OllamaClient ollamaClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * 기본 배치 크기로 임베딩 작업을 비동기 처리한다.
     */
    @Async
    public void processPendingBatchAsync() {
        processPendingBatchAsync(DEFAULT_BATCH_SIZE);
    }

    /**
     * 지정한 배치 크기로 임베딩 작업을 비동기 처리한다.
     *
     * @param batchSize 한 번에 처리할 최대 작업 개수
     */
    @Async
    public void processPendingBatchAsync(final Integer batchSize) {
        if (!running.compareAndSet(false, true)) {
            log.debug("Journal entry embedding worker is already running.");
            return;
        }

        try {
            processPendingBatch(batchSize);
        } finally {
            running.set(false);
        }
    }

    /**
     * 오래된 처리 중 작업을 재대기시킨 뒤 대기 작업을 선점해 순차적으로 벡터화한다.
     *
     * @param batchSize 한 번에 처리할 최대 작업 개수
     * @return 임베딩 벡터 생성에 성공한 작업 건수
     */
    public int processPendingBatch(final Integer batchSize) {
        queueService.requeueStaleProcessing(LocalDateTime.now().minus(STALE_PROCESSING_AGE), batchSize);

        final List<JournalEntryEmbeddingEntity> entityList = queueService.claimPendingBatch(batchSize);
        if (entityList.isEmpty()) return 0;

        log.info("Journal entry embedding worker claimed {} rows.", entityList.size());

        int successCount = 0;
        for (int i = 0; i < entityList.size(); i++) {
            try {
                if (processOne(entityList.get(i))) {
                    successCount++;
                }
            } catch (final RestClientException e) {
                requeueRemaining(entityList, i, e);
                break;
            }
        }

        log.info(
                "Journal entry embedding worker processed {}/{} rows. pendingAfter={}",
                successCount,
                entityList.size(),
                queueService.countPending()
        );
        return successCount;
    }

    /**
     * 단일 임베딩 작업의 텍스트를 벡터화하고 결과 상태를 갱신한다.
     *
     * @param entity 처리할 임베딩 작업 엔티티
     * @return 벡터 생성 성공 여부
     */
    private boolean processOne(final JournalEntryEmbeddingEntity entity) {
        try {
            if (StringUtils.isBlank(entity.getEmbeddingText())) {
                queueService.markSkipped(entity.getId(), "embedding_text is blank");
                return false;
            }

            final List<Double> vector = embedText(entity.getEmbeddingText());
            final String vectorJson = objectMapper.writeValueAsString(vector);
            queueService.markEmbedded(entity.getId(), entity.getContentHash(), ollamaClient.getEmbeddingModel(), vectorJson);
            searchService.refreshEntry(entity.getJournalEntryId());
            return true;
        } catch (final RestClientException e) {
            if (isOllamaContextLengthExceeded(e)) {
                final String message = "Ollama embedding input exceeds context length. textChars="
                        + StringUtils.length(entity.getEmbeddingText());
                log.warn("{}. id={}", message, entity.getId(), e);
                queueService.markFailed(entity.getId(), new IllegalArgumentException(message, e));
                return false;
            }
            throw e;
        } catch (final Exception e) {
            log.warn("Failed to embed journal_entry_embedding id={}", entity.getId(), e);
            queueService.markFailed(entity.getId(), e);
            return false;
        }
    }

    /**
     * 긴 저널 본문은 Ollama context 한계를 넘지 않도록 청크별로 임베딩한 뒤 평균 벡터로 합친다.
     *
     * @param text 임베딩할 원문
     * @return 단일 검색 벡터
     */
    private List<Double> embedText(final String text) {
        final List<String> chunkList = splitEmbeddingText(text);
        if (chunkList.size() == 1) {
            return ollamaClient.embed(chunkList.get(0));
        }

        log.info("Embedding long journal text as {} chunks. textChars={}", chunkList.size(), text.length());

        List<Double> sumVector = null;
        int vectorCount = 0;
        for (final String chunk : chunkList) {
            final List<Double> vector = ollamaClient.embed(chunk);
            if (sumVector == null) {
                sumVector = new ArrayList<>(vector);
            } else {
                if (sumVector.size() != vector.size()) {
                    throw new IllegalStateException("Ollama embedding vector dimension changed while chunking.");
                }
                for (int i = 0; i < sumVector.size(); i++) {
                    sumVector.set(i, sumVector.get(i) + vector.get(i));
                }
            }
            vectorCount++;
        }

        if (sumVector == null || sumVector.isEmpty() || vectorCount == 0) {
            throw new IllegalStateException("Ollama embedding response is empty");
        }

        final List<Double> averageVector = new ArrayList<>(sumVector.size());
        for (final Double value : sumVector) {
            averageVector.add(value / vectorCount);
        }
        return averageVector;
    }

    /**
     * UTF-16 surrogate pair를 깨지 않는 선에서 고정 길이 청크로 분리한다.
     *
     * @param text 분리할 원문
     * @return 비어 있지 않은 청크 목록
     */
    private List<String> splitEmbeddingText(final String text) {
        final List<String> chunkList = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + MAX_EMBEDDING_CHUNK_CHARS, text.length());
            if (end > start && end < text.length() && Character.isHighSurrogate(text.charAt(end - 1))) {
                end--;
            }
            if (end <= start) {
                end = Math.min(start + MAX_EMBEDDING_CHUNK_CHARS, text.length());
            }

            final String chunk = StringUtils.trim(text.substring(start, end));
            if (StringUtils.isNotBlank(chunk)) {
                chunkList.add(chunk);
            }
            start = end;
        }
        return chunkList;
    }

    /**
     * Ollama가 영구적인 입력 길이 문제로 반환한 오류인지 판별한다.
     *
     * @param exception Ollama 호출 예외
     * @return 입력 context 초과 오류 여부
     */
    private boolean isOllamaContextLengthExceeded(final RestClientException exception) {
        if (StringUtils.containsIgnoreCase(exception.getMessage(), OLLAMA_CONTEXT_LENGTH_ERROR)) {
            return true;
        }
        if (exception instanceof HttpStatusCodeException statusCodeException) {
            return StringUtils.containsIgnoreCase(statusCodeException.getResponseBodyAsString(), OLLAMA_CONTEXT_LENGTH_ERROR);
        }
        return false;
    }

    /**
     * Ollama 호출 장애로 현재 배치의 남은 작업을 다시 대기 상태로 되돌린다.
     *
     * @param entityList 현재 워커가 선점한 작업 목록
     * @param failedIndex 장애가 발생한 작업의 목록 인덱스
     * @param exception Ollama 호출 중 발생한 예외
     */
    private void requeueRemaining(
            final List<JournalEntryEmbeddingEntity> entityList,
            final int failedIndex,
            final RestClientException exception
    ) {
        final List<Integer> remainingIdList = entityList.subList(failedIndex, entityList.size()).stream()
                .map(JournalEntryEmbeddingEntity::getId)
                .collect(Collectors.toList());
        queueService.requeueByIds(remainingIdList, "Ollama embedding call failed: " + exception.getMessage());
        log.warn("Ollama embedding call failed. Requeued {} rows.", remainingIdList.size(), exception);
    }
}
