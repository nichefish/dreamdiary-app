package io.nicheblog.dreamdiary.feature.journal.chapter.service;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.prefix.service.PrefixContentService;
import io.nicheblog.dreamdiary.feature.journal._shared.handler.JournalCacheEvictWorker;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.feature.journal.chapter.mapstruct.JournalChapterMapstruct;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.chapter.repository.jpa.JournalChapterRepository;
import io.nicheblog.dreamdiary.feature.journal.chapter.repository.mybatis.JournalChapterMapper;
import io.nicheblog.dreamdiary.feature.journal.chapter.spec.JournalChapterSpec;
import io.nicheblog.dreamdiary.feature.journal.chapter.type.ChapterType;
import io.nicheblog.dreamdiary.feature.journal.day.repository.jpa.JournalDayRepository;
import io.nicheblog.dreamdiary.feature.journal.day.service.JournalDayService;
import io.nicheblog.dreamdiary.feature.journal.day.service.helper.JournalDayResolvedGuard;
import io.nicheblog.dreamdiary.feature.journal.entry.repository.jpa.JournalEntryRepository;
import io.nicheblog.dreamdiary.feature.journal.reflection.repository.jpa.JournalReflectionRepository;
import io.nicheblog.dreamdiary.feature.journal.entry.service.JournalEntryService;
import io.nicheblog.dreamdiary.global.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * JournalChapterService 시스템 요약 역할 테스트.
 *
 * @author nichefish
 */
@ExtendWith(MockitoExtension.class)
class JournalChapterServiceTest {

    private static final Integer FIXTURE_DAY_ID = 10;

    @Mock
    private JournalChapterRepository repository;
    @Mock
    private JournalChapterSpec spec;
    @Mock
    private JournalChapterMapstruct mapstruct;
    @Mock
    private JournalChapterMapper journalChapterMapper;
    @Mock
    private JournalCacheEvictWorker journalCacheEvictWorker;
    @Mock
    private JournalDayRepository journalDayRepository;
    @Mock
    private JournalDayService journalDayService;
    @Mock
    private JournalEntryService journalEntryService;
    @Mock
    private JournalEntryRepository journalEntryRepository;
    @Mock
    private JournalReflectionRepository journalReflectionRepository;
    @Mock
    private JournalDayResolvedGuard journalDayResolvedGuard;
    @Mock
    private PrefixContentService prefixContentService;
    @Mock
    private ApplicationContext context;

    @InjectMocks
    private JournalChapterService service;

    /**
     * 첫 일반 챕터는 요청 Prefix와 무관하게 시스템 요약으로 확정한다.
     */
    @Test
    void preRegistAssignsSystemSummaryToFirstGeneralChapter() throws Exception {
        when(repository.existsByJournalDayIdAndChapterTypeNot(FIXTURE_DAY_ID, ChapterType.DREAM))
                .thenReturn(false);
        final JournalChapterDto dto = JournalChapterDto.builder()
                .journalDayId(FIXTURE_DAY_ID)
                .chapterType(ChapterType.DIARY)
                .prefixId(77)
                .build();

        service.preRegist(dto);

        assertEquals("Y", dto.getSummaryYn());
        assertNull(dto.getPrefixId());
        assertEquals(0, dto.getSortOrder());
    }

    /**
     * 기존 일반 챕터가 있으면 신규 챕터는 일반 역할을 유지한다.
     */
    @Test
    void preRegistKeepsLaterChapterNonSummary() throws Exception {
        when(repository.findLastNormalIndexByJournalDay(FIXTURE_DAY_ID, ChapterType.DREAM)).thenReturn(Optional.of(2));
        when(repository.existsByJournalDayIdAndChapterTypeNot(FIXTURE_DAY_ID, ChapterType.DREAM))
                .thenReturn(true);
        final JournalChapterDto dto = JournalChapterDto.builder()
                .journalDayId(FIXTURE_DAY_ID)
                .chapterType(ChapterType.DIARY)
                .prefixId(77)
                .build();

        service.preRegist(dto);

        assertEquals("N", dto.getSummaryYn());
        assertEquals(77, dto.getPrefixId());
        assertEquals(3, dto.getSortOrder());
    }

    /**
     * 일반 챕터가 summaryYn을 직접 요청해 시스템 역할을 위조할 수 없다.
     */
    @Test
    void preRegistRejectsManualSummarySelection() throws Exception {
        when(repository.existsByJournalDayIdAndChapterTypeNot(FIXTURE_DAY_ID, ChapterType.DREAM))
                .thenReturn(true);
        final JournalChapterDto dto = JournalChapterDto.builder()
                .journalDayId(FIXTURE_DAY_ID)
                .chapterType(ChapterType.DIARY)
                .summaryYn("Y")
                .build();

        assertThrows(BusinessException.class, () -> service.preRegist(dto));
    }

    /**
     * 챕터 말머리 목록 Scope는 챕터 유형(일기/노트)으로 분리한다.
     * DREAM·null은 사용자 말머리를 갖지 않으므로 방어적으로 예외를 던진다.
     */
    @Test
    void resolveChapterPrefixScopeContentTypeUsesChapterType() {
        assertEquals(ContentType.JOURNAL_CHAPTER_DIARY,
                JournalChapterService.resolveChapterPrefixScopeContentType(ChapterType.DIARY));
        assertEquals(ContentType.JOURNAL_CHAPTER_NOTE,
                JournalChapterService.resolveChapterPrefixScopeContentType(ChapterType.NOTE));
        assertThrows(BusinessException.class,
                () -> JournalChapterService.resolveChapterPrefixScopeContentType(ChapterType.DREAM));
        assertThrows(BusinessException.class,
                () -> JournalChapterService.resolveChapterPrefixScopeContentType(null));
    }

    /**
     * 정규화 후 시스템 요약은 맨 앞, 일반 #은 요약 다음, DREAM은 맨 뒤.
     * 일반이 기존 sortOrder=1이어도 요약을 밀어내지 않는다.
     */
    @Test
    void normalizeSortOrderPinsSummaryFirstAndDreamLast() {
        final JournalChapterEntity summary = JournalChapterEntity.builder()
                .id(1).summaryYn("Y").chapterType(ChapterType.DIARY).sortOrder(9).build();
        final JournalChapterEntity normal = JournalChapterEntity.builder()
                .id(2).summaryYn("N").chapterType(ChapterType.DIARY).sortOrder(1).build();
        final JournalChapterEntity dream = JournalChapterEntity.builder()
                .id(3).summaryYn("N").chapterType(ChapterType.DREAM).sortOrder(2).build();
        when(repository.findAllByJournalDayId(FIXTURE_DAY_ID))
                .thenReturn(new ArrayList<>(List.of(normal, dream, summary)));
        when(repository.saveAllAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.normalizeSortOrder(FIXTURE_DAY_ID);

        assertEquals(0, summary.getSortOrder());
        assertEquals(1, normal.getSortOrder());
        assertEquals(0, dream.getSortOrder());
    }

    @Test
    void chapterSortBucketRanksSummaryThenNormalThenDream() {
        assertEquals(0, JournalChapterService.chapterSortBucket(
                JournalChapterEntity.builder().summaryYn("Y").chapterType(ChapterType.DIARY).build()));
        assertEquals(1, JournalChapterService.chapterSortBucket(
                JournalChapterEntity.builder().summaryYn("N").chapterType(ChapterType.DIARY).build()));
        assertEquals(2, JournalChapterService.chapterSortBucket(
                JournalChapterEntity.builder().summaryYn("N").chapterType(ChapterType.DREAM).build()));
    }
}
