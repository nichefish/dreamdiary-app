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

    @Async
    public void processPendingBatchAsync() {
        processPendingBatchAsync(DEFAULT_BATCH_SIZE);
    }

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
