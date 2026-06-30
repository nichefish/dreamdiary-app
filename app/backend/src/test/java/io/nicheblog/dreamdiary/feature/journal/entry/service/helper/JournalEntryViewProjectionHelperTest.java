package io.nicheblog.dreamdiary.feature.journal.entry.service.helper;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.chapter.type.ChapterType;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class JournalEntryViewProjectionHelperTest {

    private JournalEntryDto diary;
    private JournalEntryDto dream;

    @BeforeEach
    void setUp() {
        final StaticMessageSource messageSource = new StaticMessageSource();
        messageSource.addMessage("common.dream", Locale.KOREAN, "꿈");
        messageSource.addMessage("journal.dream.section.named", Locale.KOREAN, "{0} 꿈");
        messageSource.addMessage("common.dream", Locale.ENGLISH, "Dream");
        messageSource.addMessage("journal.dream.section.named", Locale.ENGLISH, "{0}''s dream");
        ReflectionTestUtils.setField(MessageUtils.class, "messageSource", messageSource);
        LocaleContextHolder.setLocale(Locale.KOREAN);
        diary = JournalEntryDto.builder().id(1).contentType(ContentType.JOURNAL_DIARY.key).build();
        dream = JournalEntryDto.builder().id(2).contentType(ContentType.JOURNAL_DREAM.key).build();
    }

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
        ReflectionTestUtils.setField(MessageUtils.class, "messageSource", null);
    }

    // =========================================================
    // getDreamEntries
    // =========================================================

    @Test
    void getDreamEntries_returnsOnlyDreamFromCanonicalList() {
        final JournalChapterDto chapter = JournalChapterDto.builder().build();
        JournalEntryViewProjectionHelper.applyChapterEntries(chapter, List.of(diary, dream));

        final List<JournalEntryDto> result = JournalEntryViewProjectionHelper.getDreamEntries(chapter);

        assertEquals(1, result.size());
        assertEquals(ContentType.JOURNAL_DREAM.key, result.get(0).getContentType());
    }

    @Test
    void getDreamEntries_emptyWhenCanonicalListHasNoDream() {
        final JournalChapterDto chapter = JournalChapterDto.builder().build();
        JournalEntryViewProjectionHelper.applyChapterEntries(chapter, List.of(diary));

        final List<JournalEntryDto> result = JournalEntryViewProjectionHelper.getDreamEntries(chapter);

        assertTrue(result.isEmpty());
    }

    @Test
    void getDreamEntries_emptyForNullChapter() {
        assertTrue(JournalEntryViewProjectionHelper.getDreamEntries(null).isEmpty());
    }

    // =========================================================
    // getEntriesByType
    // =========================================================

    @Test
    void getEntriesByType_dispatchesCorrectlyForEachContentType() {
        final JournalChapterDto chapter = JournalChapterDto.builder().build();
        JournalEntryViewProjectionHelper.applyChapterEntries(chapter, List.of(diary, dream));

        final List<JournalEntryDto> diaryResult =
                JournalEntryViewProjectionHelper.getEntriesByType(chapter, ContentType.JOURNAL_DIARY);
        final List<JournalEntryDto> dreamResult =
                JournalEntryViewProjectionHelper.getEntriesByType(chapter, ContentType.JOURNAL_DREAM);

        assertEquals(1, diaryResult.size());
        assertEquals(ContentType.JOURNAL_DIARY.key, diaryResult.get(0).getContentType());

        assertEquals(1, dreamResult.size());
        assertEquals(ContentType.JOURNAL_DREAM.key, dreamResult.get(0).getContentType());
    }

    @Test
    void getEntriesByType_matchesDirectHelperMethods() {
        final JournalChapterDto chapter = JournalChapterDto.builder().build();
        JournalEntryViewProjectionHelper.applyChapterEntries(chapter, List.of(diary, dream));

        assertEquals(
                JournalEntryViewProjectionHelper.getDiaryEntries(chapter),
                JournalEntryViewProjectionHelper.getEntriesByType(chapter, ContentType.JOURNAL_DIARY)
        );
        assertEquals(
                JournalEntryViewProjectionHelper.getDreamEntries(chapter),
                JournalEntryViewProjectionHelper.getEntriesByType(chapter, ContentType.JOURNAL_DREAM)
        );
    }

    @Test
    void getEntriesByType_returnsEmptyForNullChapter() {
        assertTrue(JournalEntryViewProjectionHelper.getEntriesByType(null, ContentType.JOURNAL_DIARY).isEmpty());
    }

    @Test
    void getEntriesByType_returnsEmptyForNullContentType() {
        final JournalChapterDto chapter = JournalChapterDto.builder().build();
        JournalEntryViewProjectionHelper.applyChapterEntries(chapter, List.of(diary));

        assertTrue(JournalEntryViewProjectionHelper.getEntriesByType(chapter, null).isEmpty());
    }

    @Test
    void getEntriesByType_returnsEmptyForUnhandledContentType() {
        final JournalChapterDto chapter = JournalChapterDto.builder().build();
        JournalEntryViewProjectionHelper.applyChapterEntries(chapter, List.of(diary));

        assertTrue(JournalEntryViewProjectionHelper.getEntriesByType(chapter, ContentType.DEFAULT).isEmpty());
    }

    @Test
    void applyDayEntryProjections_buildsDreamListsAndRemovesDreamChapter() {
        final JournalChapterDto diaryChapter = JournalChapterDto.builder()
                .id(10)
                .chapterType(ChapterType.DIARY)
                .build();
        JournalEntryViewProjectionHelper.applyChapterEntries(diaryChapter, List.of(diary));

        final JournalChapterDto dreamChapter = JournalChapterDto.builder()
                .id(20)
                .chapterType(ChapterType.DREAM)
                .build();
        JournalEntryViewProjectionHelper.applyChapterEntries(dreamChapter, List.of(dream));

        final JournalDayDto day = JournalDayDto.builder()
                .journalChapterList(new java.util.ArrayList<>(List.of(diaryChapter, dreamChapter)))
                .build();

        JournalEntryViewProjectionHelper.applyDayEntryProjections(day);

        // DREAM 챕터는 journalChapterList에서 제거되어야 한다
        assertEquals(1, day.getJournalChapterList().size());
        assertEquals(ChapterType.DIARY, day.getJournalChapterList().get(0).getChapterType());
        assertEquals(1, day.getJournalDreamSectionList().size());
        assertEquals("own", day.getJournalDreamSectionList().get(0).getSectionKey());
        assertEquals("꿈", day.getJournalDreamSectionList().get(0).getTitle());
        assertEquals(dream.getId(), day.getJournalDreamSectionList().get(0).getEntries().get(0).getId());
    }

    @Test
    void applyDayEntryProjections_splitsByDreamerNameNotElseDreamYnAlone() {
        final JournalEntryDto ownDream = JournalEntryDto.builder()
                .id(2)
                .contentType(ContentType.JOURNAL_DREAM.key)
                .elseDreamYn("Y")
                .build();
        final JournalEntryDto namedDream = JournalEntryDto.builder()
                .id(3)
                .contentType(ContentType.JOURNAL_DREAM.key)
                .elseDreamYn("N")
                .elseDreamerNm("  형  ")
                .build();

        final JournalChapterDto dreamChapter = JournalChapterDto.builder()
                .id(20)
                .chapterType(ChapterType.DREAM)
                .build();
        JournalEntryViewProjectionHelper.applyChapterEntries(dreamChapter, List.of(ownDream, namedDream));

        final JournalDayDto day = JournalDayDto.builder()
                .journalChapterList(new java.util.ArrayList<>(List.of(dreamChapter)))
                .build();

        JournalEntryViewProjectionHelper.applyDayEntryProjections(day);

        assertEquals(2, day.getJournalDreamSectionList().size());
        assertEquals("꿈", day.getJournalDreamSectionList().get(0).getTitle());
        assertEquals(ownDream.getId(), day.getJournalDreamSectionList().get(0).getEntries().get(0).getId());
        assertEquals("dreamer:형", day.getJournalDreamSectionList().get(1).getSectionKey());
        assertEquals("형 꿈", day.getJournalDreamSectionList().get(1).getTitle());
        assertEquals(namedDream.getId(), day.getJournalDreamSectionList().get(1).getEntries().get(0).getId());
    }

    @Test
    void buildSections_localizesTitlesFromLocaleContext() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        final JournalEntryDto namedDream = JournalEntryDto.builder()
                .id(3)
                .contentType(ContentType.JOURNAL_DREAM.key)
                .elseDreamerNm("Alex")
                .build();

        final var sections = JournalDreamSectionHelper.buildSections(List.of(dream), List.of(namedDream));

        assertNotNull(sections);
        assertEquals("Dream", sections.get(0).getTitle());
        assertEquals("Alex's dream", sections.get(1).getTitle());
    }
}
