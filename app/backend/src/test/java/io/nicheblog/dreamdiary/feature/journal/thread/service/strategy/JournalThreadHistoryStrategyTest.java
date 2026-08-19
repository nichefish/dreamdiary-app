package io.nicheblog.dreamdiary.feature.journal.thread.service.strategy;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.history.HistoryType;
import io.nicheblog.dreamdiary.feature.attachable.history.model.HistoryDto;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadDto;
import io.nicheblog.dreamdiary.feature.journal.thread.service.JournalThreadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * JOURNAL_THREAD 이력 전략의 서비스 위임·DTO 결합 계약 테스트.
 */
@ExtendWith(MockitoExtension.class)
class JournalThreadHistoryStrategyTest {

    private static final String FIXTURE_USERNAME = "alice";
    private static final Integer FIXTURE_THREAD_ID = 301;
    private static final Integer FIXTURE_HISTORY_ID = 701;
    private static final String FIXTURE_RESTORED_CONTENT = "복원할 스레드 본문";

    @Mock
    private JournalThreadService journalThreadService;

    @InjectMocks
    private JournalThreadHistoryStrategy strategy;

    @Test
    void supportsJournalThreadOnly() {
        assertEquals(List.of(ContentType.JOURNAL_THREAD), List.copyOf(strategy.getContentTypes()));
    }

    @Test
    void delegatesOwnedLookupToThreadService() throws Exception {
        final JournalThreadDto expected = JournalThreadDto.builder().id(FIXTURE_THREAD_ID).build();
        when(journalThreadService.getDtlDtoByUser(FIXTURE_USERNAME, FIXTURE_THREAD_ID)).thenReturn(expected);

        final JournalThreadDto actual = strategy.getOwnedDto(FIXTURE_USERNAME, FIXTURE_THREAD_ID);

        assertSame(expected, actual);
        verify(journalThreadService).getDtlDtoByUser(FIXTURE_USERNAME, FIXTURE_THREAD_ID);
    }

    @Test
    void delegatesRestoreMetadataToThreadService() throws Exception {
        final JournalThreadDto expected = JournalThreadDto.builder().id(FIXTURE_THREAD_ID).build();
        when(journalThreadService.updtContent(
                FIXTURE_THREAD_ID,
                FIXTURE_RESTORED_CONTENT,
                HistoryType.RESTORE,
                FIXTURE_HISTORY_ID
        )).thenReturn(expected);

        final JournalThreadDto actual = strategy.updtContent(
                FIXTURE_THREAD_ID,
                FIXTURE_RESTORED_CONTENT,
                HistoryType.RESTORE,
                FIXTURE_HISTORY_ID
        );

        assertSame(expected, actual);
        verify(journalThreadService).updtContent(
                FIXTURE_THREAD_ID,
                FIXTURE_RESTORED_CONTENT,
                HistoryType.RESTORE,
                FIXTURE_HISTORY_ID
        );
    }

    @Test
    void appliesHistoryListToCurrentThread() {
        final JournalThreadDto current = JournalThreadDto.builder().id(FIXTURE_THREAD_ID).build();
        final List<HistoryDto> historyList = List.of(HistoryDto.builder().id(FIXTURE_HISTORY_ID).build());

        final JournalThreadDto actual = strategy.applyHistoryList(current, historyList);

        assertSame(current, actual);
        assertSame(historyList, actual.getHistoryList());
    }
}
