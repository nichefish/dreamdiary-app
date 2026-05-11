package io.nicheblog.dreamdiary.feature.journal.embedding.service;

import io.nicheblog.dreamdiary.feature.journal.embedding.entity.JournalEntryEmbeddingEntity;
import io.nicheblog.dreamdiary.feature.journal.embedding.model.JournalEntryEmbeddingStatsDto;
import io.nicheblog.dreamdiary.feature.journal.embedding.repository.jpa.JournalEntryEmbeddingRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class JournalEntryEmbeddingQueueService {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_EMBEDDED = "EMBEDDED";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_SKIPPED = "SKIPPED";

    private static final int MIN_BATCH_SIZE = 1;
    private static final int MAX_BATCH_SIZE = 100;
    private static final int ERROR_MESSAGE_LIMIT = 4000;

    @Getter
    private final JournalEntryEmbeddingRepository repository;

    @Transactional
    public List<JournalEntryEmbeddingEntity> claimPendingBatch(final Integer batchSize) {
        final int normalizedBatchSize = normalizeBatchSize(batchSize);
        final List<JournalEntryEmbeddingEntity> entityList = repository.findAllByEmbeddingStatusOrderByCreatedAtAscIdAsc(
                STATUS_PENDING,
                PageRequest.of(0, normalizedBatchSize)
        );

        entityList.forEach(entity -> {
            entity.setEmbeddingStatus(STATUS_PROCESSING);
            entity.setErrorMessage(null);
        });
        repository.saveAll(entityList);
        repository.flush();

        return entityList;
    }

    @Transactional
    public int requeueStaleProcessing(final Date staleBefore, final Integer batchSize) {
        final int normalizedBatchSize = normalizeBatchSize(batchSize);
        final List<JournalEntryEmbeddingEntity> entityList =
                repository.findAllByEmbeddingStatusAndUpdatedAtBeforeOrderByUpdatedAtAscIdAsc(
                        STATUS_PROCESSING,
                        staleBefore,
                        PageRequest.of(0, normalizedBatchSize)
                );

        entityList.forEach(entity -> {
            entity.setEmbeddingStatus(STATUS_PENDING);
            entity.setErrorMessage("Requeued stale PROCESSING row.");
        });
        repository.saveAll(entityList);

        if (!entityList.isEmpty()) {
            log.info("Requeued {} stale journal entry embedding rows.", entityList.size());
        }

        return entityList.size();
    }

    @Transactional
    public void markEmbedded(final Integer id, final String embeddingModel, final String embeddingVectorJson) {
        repository.findById(id).ifPresent(entity -> {
            entity.setEmbeddingStatus(STATUS_EMBEDDED);
            entity.setEmbeddingModel(embeddingModel);
            entity.setEmbeddingVectorJson(embeddingVectorJson);
            entity.setEmbeddedAt(new Date());
            entity.setErrorMessage(null);
        });
    }

    @Transactional
    public void markFailed(final Integer id, final Exception exception) {
        repository.findById(id).ifPresent(entity -> {
            entity.setEmbeddingStatus(STATUS_FAILED);
            entity.setErrorMessage(toErrorMessage(exception));
        });
    }

    @Transactional
    public void requeueByIds(final List<Integer> idList, final String reason) {
        if (idList == null || idList.isEmpty()) return;

        final List<JournalEntryEmbeddingEntity> entityList = repository.findAllById(idList);
        entityList.forEach(entity -> {
            entity.setEmbeddingStatus(STATUS_PENDING);
            entity.setErrorMessage(StringUtils.abbreviate(reason, ERROR_MESSAGE_LIMIT));
        });
    }

    @Transactional
    public void markSkipped(final Integer id, final String reason) {
        repository.findById(id).ifPresent(entity -> {
            entity.setEmbeddingStatus(STATUS_SKIPPED);
            entity.setErrorMessage(StringUtils.abbreviate(reason, ERROR_MESSAGE_LIMIT));
        });
    }

    @Transactional(readOnly = true)
    public long countPending() {
        return repository.countByEmbeddingStatus(STATUS_PENDING);
    }

    @Transactional(readOnly = true)
    public JournalEntryEmbeddingStatsDto getStats() {
        final long total = repository.count();
        final long pending = repository.countByEmbeddingStatus(STATUS_PENDING);
        final long processing = repository.countByEmbeddingStatus(STATUS_PROCESSING);
        final long embedded = repository.countByEmbeddingStatus(STATUS_EMBEDDED);
        final long failed = repository.countByEmbeddingStatus(STATUS_FAILED);
        final long skipped = repository.countByEmbeddingStatus(STATUS_SKIPPED);
        final long remaining = pending + processing;
        final long completed = embedded + failed + skipped;

        return JournalEntryEmbeddingStatsDto.builder()
                .total(total)
                .pending(pending)
                .processing(processing)
                .embedded(embedded)
                .failed(failed)
                .skipped(skipped)
                .remaining(remaining)
                .completed(completed)
                .completionRate(toPercent(completed, total))
                .vectorizedRate(toPercent(embedded, total))
                .build();
    }

    private int normalizeBatchSize(final Integer batchSize) {
        if (batchSize == null) return 20;
        return Math.max(MIN_BATCH_SIZE, Math.min(MAX_BATCH_SIZE, batchSize));
    }

    private String toErrorMessage(final Exception exception) {
        if (exception == null) return null;
        final String message = exception.getMessage() == null ? exception.toString() : exception.getMessage();
        return StringUtils.abbreviate(message, ERROR_MESSAGE_LIMIT);
    }

    private double toPercent(final long count, final long total) {
        if (total <= 0) return 0.0D;
        return Math.round(((double) count * 10000.0D) / (double) total) / 100.0D;
    }
}
