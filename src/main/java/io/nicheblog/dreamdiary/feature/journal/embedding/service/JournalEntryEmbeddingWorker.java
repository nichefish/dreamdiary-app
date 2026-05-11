package io.nicheblog.dreamdiary.feature.journal.embedding.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nicheblog.dreamdiary.feature.chat.client.OllamaClient;
import io.nicheblog.dreamdiary.feature.journal.embedding.entity.JournalEntryEmbeddingEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
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
    private static final Duration STALE_PROCESSING_AGE = Duration.ofMinutes(30);

    private final JournalEntryEmbeddingQueueService queueService;
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
        queueService.requeueStaleProcessing(Date.from(Instant.now().minus(STALE_PROCESSING_AGE)), batchSize);

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

            final List<Double> vector = ollamaClient.embed(entity.getEmbeddingText());
            final String vectorJson = objectMapper.writeValueAsString(vector);
            queueService.markEmbedded(entity.getId(), ollamaClient.getEmbeddingModel(), vectorJson);
            return true;
        } catch (final RestClientException e) {
            throw e;
        } catch (final Exception e) {
            log.warn("Failed to embed journal_entry_embedding id={}", entity.getId(), e);
            queueService.markFailed(entity.getId(), e);
            return false;
        }
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
