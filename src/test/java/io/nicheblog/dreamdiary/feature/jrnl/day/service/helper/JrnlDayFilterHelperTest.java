package io.nicheblog.dreamdiary.feature.jrnl.day.service.helper;

import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDayDto;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDaySearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.feature.jrnl.entry.model.JrnlEntryDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JrnlDayFilterHelperTest {

    @Test
    void filterInMemory_doesNotReduceMonthListToAnchorDayWhenEntryCategoryFilterIsUsed() {
        final JrnlDayDto firstDay = JrnlDayDto.builder()
                .jrnlDt("2026-04-01")
                .jrnlEntryList(List.of(createEntry("SUMMARY", "first summary")))
                .build();
        final JrnlDayDto secondDay = JrnlDayDto.builder()
                .jrnlDt("2026-04-02")
                .jrnlEntryList(List.of(createEntry("SUMMARY", "second summary")))
                .build();

        final JrnlDaySearchParam searchParam = JrnlDaySearchParam.builder()
                .showDiaries(true)
                .showDreams(true)
                .stdrdDt("2026-04-01")
                .entryCtgrCds(List.of("SUMMARY"))
                .build();

        final List<JrnlDayDto> filtered = JrnlDayFilterHelper.filterInMemory(List.of(firstDay, secondDay), searchParam);

        assertEquals(2, filtered.size());
        assertEquals(List.of("2026-04-01", "2026-04-02"), filtered.stream().map(JrnlDayDto::getStdrdDt).toList());
    }

    private JrnlEntryDto createEntry(final String ctgrCd, final String diaryContent) {
        return JrnlEntryDto.builder()
                .ctgrCd(ctgrCd)
                .jrnlDiaryList(List.of(JrnlDiaryDto.builder().cn(diaryContent).build()))
                .build();
    }
}
