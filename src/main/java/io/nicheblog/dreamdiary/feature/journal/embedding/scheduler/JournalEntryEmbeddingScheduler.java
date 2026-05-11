package io.nicheblog.dreamdiary.feature.journal.embedding.scheduler;

import io.nicheblog.dreamdiary.feature.journal.embedding.service.JournalEntryEmbeddingWorker;
import io.nicheblog.dreamdiary.feature.journal.embedding.service.JournalEntryEmbeddingQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class JournalEntryEmbeddingScheduler {

    private final JournalEntryEmbeddingWorker journalEntryEmbeddingWorker;
    private final JournalEntryEmbeddingQueueService journalEntryEmbeddingQueueService;

    @Value("${dreamdiary.embedding.worker.batch-size:20}")
    private Integer batchSize;

    @Scheduled(
            fixedDelayString = "${dreamdiary.embedding.worker.fixed-delay-ms:60000}",
            initialDelayString = "${dreamdiary.embedding.worker.initial-delay-ms:15000}"
    )
    public void processPendingEmbeddings() {
        final long pendingCount = journalEntryEmbeddingQueueService.countPending();
        if (pendingCount <= 0) {
            log.debug("Journal entry embedding scheduler tick. pending=0");
            return;
        }

        log.info("Journal entry embedding scheduler tick. pending={}, batchSize={}", pendingCount, batchSize);
        journalEntryEmbeddingWorker.processPendingBatchAsync(batchSize);
    }
}
