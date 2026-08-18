package io.nicheblog.dreamdiary.feature.journal.embedding.scheduler;

import io.nicheblog.dreamdiary.feature.journal.config.JournalProperties;
import io.nicheblog.dreamdiary.feature.journal.embedding.service.JournalEntryEmbeddingQueueService;
import io.nicheblog.dreamdiary.feature.journal.embedding.service.JournalEntryEmbeddingWorker;
import io.nicheblog.dreamdiary.feature.journal.setting.service.JournalSettingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 임베딩 스케줄러가 전역 토글 OFF일 때 워커를 호출하지 않는지 검증한다.
 *
 * @author nichefish
 */
@ExtendWith(MockitoExtension.class)
class JournalEntryEmbeddingSchedulerTest {

    @Mock
    private JournalEntryEmbeddingWorker worker;

    @Mock
    private JournalEntryEmbeddingQueueService queueService;

    @Mock
    private JournalProperties journalProperties;

    @Mock
    private JournalSettingService journalSettingService;

    private JournalEntryEmbeddingScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new JournalEntryEmbeddingScheduler(
                worker,
                queueService,
                journalProperties,
                journalSettingService
        );
    }

    @Test
    void processPendingEmbeddingsSkipsWhenEmbeddingDisabled() {
        when(journalSettingService.isEmbeddingEnabled()).thenReturn(false);

        scheduler.processPendingEmbeddings();

        verifyNoInteractions(queueService);
        verifyNoInteractions(worker);
    }
}
