package io.nicheblog.dreamdiary.feature.journal.embedding.service;

import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.feature.journal.chapter.repository.jpa.JournalChapterRepository;
import io.nicheblog.dreamdiary.feature.journal.embedding.entity.JournalEntryEmbeddingEntity;
import io.nicheblog.dreamdiary.feature.journal.embedding.repository.jpa.JournalEntryEmbeddingRepository;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.repository.jpa.JournalEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 임베딩 전수 sync의 배치 조회와 진행 하트비트 간격 테스트.
 *
 * @author nichefish
 */
@ExtendWith(MockitoExtension.class)
class JournalEntryEmbeddingQueueServiceSyncTest {

    private static final String FIXTURE_CONTENT_TYPE = "JOURNAL_DIARY";

    @Mock
    private JournalEntryEmbeddingRepository repository;

    @Mock
    private JournalEntryEmbeddingSearchService searchService;

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private JournalChapterRepository journalChapterRepository;

    private JournalEntryEmbeddingQueueService service;

    @BeforeEach
    void setUp() {
        service = new JournalEntryEmbeddingQueueService(
                repository,
                searchService,
                journalEntryRepository,
                journalChapterRepository
        );
    }

    /** 하트비트는 0과 마지막, 그리고 50건 간격에서만 남긴다. */
    @Test
    void shouldReportSyncProgressAtIntervalAndEnd() {
        assertThat(JournalEntryEmbeddingQueueService.shouldReportSyncProgress(0, 120)).isFalse();
        assertThat(JournalEntryEmbeddingQueueService.shouldReportSyncProgress(49, 120)).isFalse();
        assertThat(JournalEntryEmbeddingQueueService.shouldReportSyncProgress(50, 120)).isTrue();
        assertThat(JournalEntryEmbeddingQueueService.shouldReportSyncProgress(100, 120)).isTrue();
        assertThat(JournalEntryEmbeddingQueueService.shouldReportSyncProgress(120, 120)).isTrue();
    }

    /** 전수 sync는 챕터 단건 조회와 임베딩 단건 조회를 쓰지 않는다. */
    @Test
    void syncWithJournalEntriesLoadsChaptersAndEmbeddingsInBatch() throws Exception {
        final JournalChapterEntity chapter = JournalChapterEntity.builder()
                .id(10)
                .title("fixture-chapter")
                .build();
        final List<JournalEntryEntity> entryList = List.of(
                entry(101, 10),
                entry(102, 10)
        );
        when(journalEntryRepository.findAll()).thenReturn(entryList);
        when(repository.findAll()).thenReturn(List.of());
        when(repository.save(any(JournalEntryEmbeddingEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.count()).thenReturn(2L);
        when(journalChapterRepository.findAllById(any())).thenReturn(List.of(chapter));

        final List<Integer> progress = new ArrayList<>();
        service.syncWithJournalEntries(progress::add);

        assertThat(progress).containsExactly(0, 2);
        verify(journalChapterRepository).findAllById(any());
        verify(journalChapterRepository, never()).findById(anyInt());
        verify(repository, never()).findFirstByJournalEntryId(anyInt());
    }

    private JournalEntryEntity entry(final Integer id, final Integer chapterId) {
        return JournalEntryEntity.builder()
                .id(id)
                .journalChapterId(chapterId)
                .contentType(FIXTURE_CONTENT_TYPE)
                .title("fixture-entry-" + id)
                .build();
    }
}
