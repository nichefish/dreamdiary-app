package io.nicheblog.dreamdiary.feature.journal.entry.service.helper;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.related.service.RelatedContentQueryService;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadEntryDto;
import io.nicheblog.dreamdiary.feature.journal.thread.service.JournalThreadEntryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * 검색·목록 enrich 가 엔트리 소속 스레드를 채우는지 고정한다.
 * 픽스처는 가상 ID/제목만 사용한다.
 */
@ExtendWith(MockitoExtension.class)
class JournalEntryRelatedEnricherTest {

    private static final String FIXTURE_USERNAME = "fixture-user";
    private static final Integer FIXTURE_ENTRY_ID = 101;
    private static final Integer FIXTURE_THREAD_ID = 202;
    private static final String FIXTURE_THREAD_TITLE = "Fixture Thread Title";

    @Mock
    private RelatedContentQueryService relatedContentQueryService;

    @Mock
    private JournalThreadEntryService journalThreadEntryService;

    @InjectMocks
    private JournalEntryRelatedEnricher relatedEnricher;

    @Test
    void enrich_setsThreadListFromMembershipMap() throws Exception {
        final JournalEntryDto entry = JournalEntryDto.builder().id(FIXTURE_ENTRY_ID).build();
        when(relatedContentQueryService.getRelatedContentMapByRefs(any(), eq(FIXTURE_USERNAME)))
                .thenReturn(Map.of());
        when(journalThreadEntryService.getMapByEntryIds(eq(List.of(FIXTURE_ENTRY_ID)), eq(FIXTURE_USERNAME)))
                .thenReturn(Map.of(
                        FIXTURE_ENTRY_ID,
                        List.of(JournalThreadEntryDto.builder()
                                .id(1)
                                .threadId(FIXTURE_THREAD_ID)
                                .entryId(FIXTURE_ENTRY_ID)
                                .threadTitle(FIXTURE_THREAD_TITLE)
                                .build())
                ));

        relatedEnricher.enrich(ContentType.JOURNAL_DIARY, FIXTURE_USERNAME, List.of(entry));

        assertEquals(1, entry.getThreadList().size());
        assertEquals(FIXTURE_THREAD_ID, entry.getThreadList().get(0).getThreadId());
        assertEquals(FIXTURE_THREAD_TITLE, entry.getThreadList().get(0).getThreadTitle());
    }
}
