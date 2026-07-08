package io.nicheblog.dreamdiary.feature.journal.entitycatalog.service;

import io.nicheblog.dreamdiary.feature.journal.entitycatalog.entity.JournalEntryEntityJobEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Background worker that consumes pending journal entity sync jobs.
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalEntryEntityWorker {

    private static final int DEFAULT_BATCH_SIZE = 20;
    private static final Duration STALE_PROCESSING_AGE = Duration.ofMinutes(10);

    private final JournalEntryEntityQueueService queueService;
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * Process pending queue rows asynchronously with the default batch size.
     */
    @Async
    public void processPendingBatchAsync() {
        processPendingBatchAsync(DEFAULT_BATCH_SIZE);
    }

    /**
     * Process pending queue rows asynchronously.
     *
     * @param batchSize requested batch size
     */
    @Async
    public void processPendingBatchAsync(final Integer batchSize) {
        if (!running.compareAndSet(false, true)) {
            log.debug("Journal entry entity worker is already running.");
            return;
        }

        try {
            processPendingBatch(batchSize);
        } finally {
            running.set(false);
        }
    }

    /**
     * Claim and process one queue batch synchronously.
     *
     * @param batchSize requested batch size
     * @return successfully processed row count
     */
    public int processPendingBatch(final Integer batchSize) {
        queueService.requeueStaleProcessing(LocalDateTime.now().minus(STALE_PROCESSING_AGE), batchSize);

        final List<JournalEntryEntityJobEntity> jobList = queueService.claimPendingBatch(batchSize);
        if (jobList.isEmpty()) return 0;

        int successCount = 0;
        for (final JournalEntryEntityJobEntity job : jobList) {
            try {
                queueService.processJob(job);
                successCount++;
            } catch (final Exception e) {
                queueService.markFailed(job.getId(), e);
                log.warn("Failed to process journal entity sync job. jobId={}, entryId={}",
                        job.getId(), job.getJournalEntryId(), e);
            }
        }

        log.info("Journal entry entity worker processed {}/{} jobs. pendingAfter={}",
                successCount, jobList.size(), queueService.countPending());
        return successCount;
    }
}
