package io.nicheblog.dreamdiary.feature.attachable.related.service;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableKey;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.related.entity.RelatedContentEntity;
import io.nicheblog.dreamdiary.feature.attachable.related.mapstruct.RelatedContentMapstruct;
import io.nicheblog.dreamdiary.feature.attachable.related.model.RelatedContentDto;
import io.nicheblog.dreamdiary.feature.attachable.related.model.RelatedContentFlowDto;
import io.nicheblog.dreamdiary.feature.attachable.related.model.RelatedContentFlowSummaryDto;
import io.nicheblog.dreamdiary.feature.attachable.related.repository.jpa.RelatedContentRepository;
import io.nicheblog.dreamdiary.feature.attachable.related.type.RelationType;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterSmpEntity;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDaySmpEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.mapstruct.JournalEntryMapstruct;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.repository.jpa.JournalEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RelatedContentFlowServiceTest
 * <pre>
 *  FLOW 연결 컴포넌트 탐색·정렬 단위 테스트.
 * </pre>
 *
 * @author nichefish
 */
@ExtendWith(MockitoExtension.class)
class RelatedContentFlowServiceTest {

    private static final String FIXTURE_USERNAME = "fixture-user";
    private static final String FIXTURE_TITLE_PREFIX = "가상 기록 ";

    @Mock
    private RelatedContentRepository relatedContentRepository;
    @Mock
    private RelatedContentMapstruct relatedContentMapstruct;
    @Mock
    private RelatedContentService relatedContentService;
    @Mock
    private JournalEntryRepository journalEntryRepository;
    @Mock
    private JournalEntryMapstruct journalEntryMapstruct;

    @InjectMocks
    private RelatedContentFlowService relatedContentFlowService;

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(relatedContentService.requireOwnedContent(any(BaseAttachableKey.class)))
                .thenReturn(FIXTURE_USERNAME);
        when(journalEntryMapstruct.toDto(any(JournalEntryEntity.class))).thenAnswer(invocation -> {
            final JournalEntryEntity entity = invocation.getArgument(0);
            return JournalEntryDto.builder()
                    .id(entity.getId())
                    .contentType(entity.getContentType())
                    .title(entity.getTitle())
                    .content(entity.getContent())
                    .markdownContent(entity.getContent())
                    .stdrdDt(entity.getJournalChapter().getJournalDay().getJournalDate().toString())
                    .journalDayId(entity.getJournalChapter().getJournalDayId())
                    .journalChapterId(entity.getJournalChapterId())
                    .sortOrder(entity.getSortOrder())
                    .build();
        });
        lenient().when(relatedContentMapstruct.toDto(any(RelatedContentEntity.class))).thenAnswer(invocation -> {
            final RelatedContentEntity entity = invocation.getArgument(0);
            return RelatedContentDto.builder()
                    .id(entity.getId())
                    .leftId(entity.getLeftId())
                    .leftContentType(entity.getLeftContentType())
                    .rightId(entity.getRightId())
                    .rightContentType(entity.getRightContentType())
                    .relationType(entity.getRelationType())
                    .build();
        });
    }

    @Test
    void getFlowTraversesCycleWithoutDuplicatesAndSortsByJournalOrder() throws Exception {
        final RelatedContentEntity firstSecond = relation(11, 1, ContentType.JOURNAL_DIARY, 2, ContentType.JOURNAL_DREAM);
        final RelatedContentEntity secondThird = relation(12, 2, ContentType.JOURNAL_DREAM, 3, ContentType.JOURNAL_DIARY);
        final RelatedContentEntity thirdFirst = relation(13, 3, ContentType.JOURNAL_DIARY, 1, ContentType.JOURNAL_DIARY);
        final RelatedContentEntity unrelated = relation(14, 4, ContentType.JOURNAL_DIARY, 5, ContentType.JOURNAL_DREAM);
        when(relatedContentRepository.findAllByRelationTypeAndCreatedByOrderByCreatedAtAsc(
                RelationType.FLOW.key,
                FIXTURE_USERNAME
        )).thenReturn(List.of(firstSecond, secondThird, thirdFirst, unrelated));

        final JournalEntryEntity first = entry(1, ContentType.JOURNAL_DIARY, LocalDate.of(2026, 7, 3), 2, 1);
        final JournalEntryEntity second = entry(2, ContentType.JOURNAL_DREAM, LocalDate.of(2026, 7, 1), 1, 1);
        final JournalEntryEntity third = entry(3, ContentType.JOURNAL_DIARY, LocalDate.of(2026, 7, 3), 1, 2);
        when(journalEntryRepository.findAllByIdInAndContentTypeIn(anyCollection(), anyCollection()))
                .thenReturn(List.of(first, second, third));

        final RelatedContentFlowDto result = relatedContentFlowService.getFlow(ContentType.JOURNAL_DIARY, 1);

        assertEquals(3, result.getEntryList().size());
        assertEquals(List.of(2, 3, 1), result.getEntryList().stream().map(entry -> entry.getId()).toList());
        assertEquals(3, result.getRelationList().size());
        assertEquals(1, result.getEntryList().stream().filter(entry -> Boolean.TRUE.equals(entry.getAnchor())).count());
        assertTrue(result.getEntryList().stream().anyMatch(entry -> entry.getId() == 1 && Boolean.TRUE.equals(entry.getAnchor())));
    }

    @Test
    void getFlowReturnsAnchorWhenNoFlowRelationExists() throws Exception {
        when(relatedContentRepository.findAllByRelationTypeAndCreatedByOrderByCreatedAtAsc(
                RelationType.FLOW.key,
                FIXTURE_USERNAME
        )).thenReturn(List.of());
        final JournalEntryEntity anchor = entry(1, ContentType.JOURNAL_DIARY, LocalDate.of(2026, 7, 3), 1, 1);
        when(journalEntryRepository.findAllByIdInAndContentTypeIn(anyCollection(), anyCollection()))
                .thenReturn(List.of(anchor));

        final RelatedContentFlowDto result = relatedContentFlowService.getFlow(ContentType.JOURNAL_DIARY, 1);

        assertEquals(1, result.getEntryList().size());
        assertTrue(result.getEntryList().get(0).getAnchor());
        assertTrue(result.getRelationList().isEmpty());
        verify(relatedContentService).requireOwnedContent(new BaseAttachableKey(1, ContentType.JOURNAL_DIARY));
    }

    @Test
    void getFlowSummaryMapReturnsOneTransitiveComponentSummaryForRequestedEntries() throws Exception {
        final RelatedContentEntity firstSecond = relation(11, 1, ContentType.JOURNAL_DIARY, 2, ContentType.JOURNAL_DREAM);
        final RelatedContentEntity secondThird = relation(12, 2, ContentType.JOURNAL_DREAM, 3, ContentType.JOURNAL_DIARY);
        when(relatedContentRepository.findAllByRelationTypeAndCreatedByOrderByCreatedAtAsc(
                RelationType.FLOW.key,
                FIXTURE_USERNAME
        )).thenReturn(List.of(firstSecond, secondThird));

        final JournalEntryEntity first = entry(1, ContentType.JOURNAL_DIARY, LocalDate.of(2026, 7, 3), 2, 1);
        final JournalEntryEntity second = entry(2, ContentType.JOURNAL_DREAM, LocalDate.of(2026, 7, 1), 1, 1);
        final JournalEntryEntity third = entry(3, ContentType.JOURNAL_DIARY, LocalDate.of(2026, 7, 5), 1, 2);
        when(journalEntryRepository.findAllByIdInAndContentTypeIn(anyCollection(), anyCollection()))
                .thenReturn(List.of(first, second, third));

        final BaseAttachableKey firstKey = new BaseAttachableKey(1, ContentType.JOURNAL_DIARY);
        final BaseAttachableKey secondKey = new BaseAttachableKey(2, ContentType.JOURNAL_DREAM);
        final Map<BaseAttachableKey, RelatedContentFlowSummaryDto> result = relatedContentFlowService
                .getFlowSummaryMap(List.of(firstKey, secondKey), FIXTURE_USERNAME);

        assertEquals(2, result.size());
        assertEquals(3, result.get(firstKey).getEntryCount());
        assertEquals(2, result.get(firstKey).getRelationCount());
        assertEquals("2026-07-01", result.get(firstKey).getStartStdrdDt());
        assertEquals("2026-07-05", result.get(firstKey).getEndStdrdDt());
        assertSame(result.get(firstKey), result.get(secondKey));
        verify(relatedContentRepository, times(1))
                .findAllByRelationTypeAndCreatedByOrderByCreatedAtAsc(RelationType.FLOW.key, FIXTURE_USERNAME);
        verify(journalEntryRepository, times(1)).findAllByIdInAndContentTypeIn(anyCollection(), anyCollection());
    }

    private RelatedContentEntity relation(
            final Integer id,
            final Integer leftId,
            final ContentType leftType,
            final Integer rightId,
            final ContentType rightType
    ) {
        return RelatedContentEntity.builder()
                .id(id)
                .leftId(leftId)
                .leftContentType(leftType.key)
                .rightId(rightId)
                .rightContentType(rightType.key)
                .relationType(RelationType.FLOW.key)
                .createdBy(FIXTURE_USERNAME)
                .build();
    }

    private JournalEntryEntity entry(
            final Integer id,
            final ContentType contentType,
            final LocalDate journalDate,
            final Integer chapterSortOrder,
            final Integer entrySortOrder
    ) {
        final JournalDaySmpEntity day = JournalDaySmpEntity.builder()
                .id(id)
                .journalDate(journalDate)
                .build();
        final JournalChapterSmpEntity chapter = JournalChapterSmpEntity.builder()
                .id(id)
                .journalDayId(id)
                .journalDay(day)
                .sortOrder(chapterSortOrder)
                .build();
        return JournalEntryEntity.builder()
                .id(id)
                .contentType(contentType.key)
                .title(FIXTURE_TITLE_PREFIX + id)
                .content(FIXTURE_TITLE_PREFIX + id + " 본문")
                .journalChapterId(id)
                .journalChapter(chapter)
                .sortOrder(entrySortOrder)
                .createdBy(FIXTURE_USERNAME)
                .build();
    }
}
