package io.nicheblog.dreamdiary.feature.journal.day.service.helper;

import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDaySearchParam;
import io.nicheblog.dreamdiary.feature.journal.diary.model.JournalDiaryDto;
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
        return JournalChapterDto.builder()
                .categoryCode(categoryCode)
                .journalDiaryList(List.of(JournalDiaryDto.builder().content(diaryContent).build()))
                .build();
    }
}

