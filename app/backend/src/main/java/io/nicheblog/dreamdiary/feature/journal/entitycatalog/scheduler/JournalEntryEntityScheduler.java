package io.nicheblog.dreamdiary.feature.journal.entitycatalog.scheduler;

import io.nicheblog.dreamdiary.feature.journal.entitycatalog.service.JournalEntryEntityQueueService;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.service.JournalEntryEntityWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically checks the entity sync queue and triggers asynchronous processing.
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class JournalEntryEntityScheduler {

    private final JournalEntryEntityWorker journalEntryEntityWorker;
    private final JournalEntryEntityQueueService journalEntryEntityQueueService;

    @Value("${app.journal.entity.worker.batch-size:20}")
    private Integer batchSize;

    /**
     * Trigger the worker only when pending entity-sync rows exist.
     */
    @Scheduled(
            fixedDelayString = "${app.journal.entity.worker.fixed-delay-ms:20000}",
            initialDelayString = "${app.journal.entity.worker.initial-delay-ms:10000}"
    )
    public void processPendingEntityJobs() {
        final long pendingCount = journalEntryEntityQueueService.countPending();
        if (pendingCount <= 0) {
            log.debug("Journal entry entity scheduler tick. pending=0");
            return;
        }

        log.info("Journal entry entity scheduler tick. pending={}, batchSize={}", pendingCount, batchSize);
        journalEntryEntityWorker.processPendingBatchAsync(batchSize);
    }
}
