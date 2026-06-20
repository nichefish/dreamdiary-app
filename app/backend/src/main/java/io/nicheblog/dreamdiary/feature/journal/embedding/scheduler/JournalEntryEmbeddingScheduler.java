package io.nicheblog.dreamdiary.feature.journal.embedding.scheduler;

import io.nicheblog.dreamdiary.feature.journal.embedding.service.JournalEntryEmbeddingWorker;
import io.nicheblog.dreamdiary.feature.journal.embedding.service.JournalEntryEmbeddingQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 주기적으로 저널 엔트리 임베딩 대기열을 확인해 비동기 워커를 실행하는 스케줄러입니다.
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class JournalEntryEmbeddingScheduler {

    private final JournalEntryEmbeddingWorker journalEntryEmbeddingWorker;
    private final JournalEntryEmbeddingQueueService journalEntryEmbeddingQueueService;

    @Value("${app.journal.embedding.worker.batch-size:20}")
    private Integer batchSize;

    /**
     * 대기 중인 임베딩 작업이 있으면 설정된 배치 크기로 비동기 워커를 실행한다.
     */
    @Scheduled(
            fixedDelayString = "${app.journal.embedding.worker.fixed-delay-ms:60000}",
            initialDelayString = "${app.journal.embedding.worker.initial-delay-ms:15000}"
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
