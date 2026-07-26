package io.nicheblog.dreamdiary.feature.journal.entry.service.helper;

import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JournalEntryHolydayHelperTest {

    @Test
    void setHolydayInfo_marksHolidayAndWeekend() throws Exception {
        final JournalEntryDto holiday = JournalEntryDto.builder().stdrdDt("2024-01-01").build();
        final JournalEntryDto weekend = JournalEntryDto.builder().stdrdDt("2024-01-06").build(); // Saturday
        final JournalEntryDto weekday = JournalEntryDto.builder().stdrdDt("2024-01-02").build(); // Tuesday
        final Map<String, List<String>> map = Map.of("2024-01-01", List.of("FIXTURE_HOLIDAY"));

        JournalEntryHolydayHelper.setHolydayInfo(List.of(holiday, weekend, weekday), map);

        assertEquals(Boolean.TRUE, holiday.getIsHolyday());
        assertEquals("FIXTURE_HOLIDAY", holiday.getHolydayNm());
        assertEquals(Boolean.TRUE, weekend.getIsHolyday());
        assertNull(weekend.getHolydayNm());
        assertEquals(Boolean.FALSE, weekday.getIsHolyday());
        assertNull(weekday.getHolydayNm());
    }

    @Test
    void setHolydayInfo_nullMapLeavesUnset() throws Exception {
        final JournalEntryDto entry = JournalEntryDto.builder().stdrdDt("2024-01-01").build();
        JournalEntryHolydayHelper.setHolydayInfo(List.of(entry), null);
        assertNull(entry.getIsHolyday());
        assertNull(entry.getHolydayNm());
    }
}
