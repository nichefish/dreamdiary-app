package io.nicheblog.dreamdiary.feature.journal.day.service.helper;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDaySearchParam;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.service.helper.JournalEntryViewProjectionHelper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JournalDayFilterHelperTest {

    @Test
    void filterInMemory_doesNotReduceMonthListToAnchorDayWhenChapterCategoryFilterIsUsed() {
        final JournalDayDto firstDay = JournalDayDto.builder()
                .journalDate("2026-04-01")
                .journalChapterList(List.of(createChapter("SUMMARY", "first summary")))
                .build();
        final JournalDayDto secondDay = JournalDayDto.builder()
                .journalDate("2026-04-02")
                .journalChapterList(List.of(createChapter("SUMMARY", "second summary")))
                .build();

        final JournalDaySearchParam searchParam = JournalDaySearchParam.builder()
                .showDiaries(true)
                .showDreams(true)
                .stdrdDt("2026-04-01")
                .chapterCtgrCds(List.of("SUMMARY"))
                .build();

        final List<JournalDayDto> filtered = JournalDayFilterHelper.filterInMemory(List.of(firstDay, secondDay), searchParam);

        assertEquals(2, filtered.size());
        assertEquals(List.of("2026-04-01", "2026-04-02"), filtered.stream().map(JournalDayDto::getStdrdDt).toList());
    }

    private JournalChapterDto createChapter(final String categoryCode, final String diaryContent) {
        final JournalChapterDto chapter = JournalChapterDto.builder()
                .categoryCode(categoryCode)
                .build();
        JournalEntryViewProjectionHelper.applyChapterEntries(chapter, List.of(
                JournalEntryDto.builder()
                        .contentType(ContentType.JOURNAL_DIARY.key)
                        .content(diaryContent)
                        .build()
        ));
        return chapter;
    }

    @Test
    void filterInMemory_keepsCanonicalChapterEntriesInSyncWhenDiaryKeywordMatches() {
        final JournalChapterDto chapter = JournalChapterDto.builder()
                .categoryCode("SUMMARY")
                .build();
        JournalEntryViewProjectionHelper.applyChapterEntries(chapter, List.of(
                JournalEntryDto.builder().contentType(ContentType.JOURNAL_DIARY.key).content("matched diary").build(),
                JournalEntryDto.builder().contentType(ContentType.JOURNAL_DIARY.key).content("other diary").build(),
                JournalEntryDto.builder().contentType(ContentType.JOURNAL_DIARY.key).content("kept note").build()
        ));

        final JournalDayDto day = JournalDayDto.builder()
                .journalDate("2026-04-01")
                .journalChapterList(List.of(chapter))
                .build();

        final JournalDaySearchParam searchParam = JournalDaySearchParam.builder()
                .showDiaries(true)
                .showDreams(true)
                .diaryKeyword("matched")
                .build();

        final List<JournalDayDto> filtered = JournalDayFilterHelper.filterInMemory(List.of(day), searchParam);

        assertEquals(1, filtered.size());
        assertEquals(1, filtered.get(0).getJournalChapterList().get(0).getJournalEntryList().size());
        assertEquals("matched diary", filtered.get(0).getJournalChapterList().get(0).getJournalEntryList().get(0).getContent());
    }
}
