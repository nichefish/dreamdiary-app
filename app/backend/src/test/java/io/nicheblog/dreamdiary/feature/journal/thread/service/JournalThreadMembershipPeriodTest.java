package io.nicheblog.dreamdiary.feature.journal.thread.service;

import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 스레드 목록 소속 기간 enrich 단위 테스트.
 * 가상 픽스처만 사용한다.
 */
class JournalThreadMembershipPeriodTest {

    private static final Integer FIXTURE_THREAD_ID = 501;
    private static final String FIXTURE_DATE_EARLY = "2026-01-05";
    private static final String FIXTURE_DATE_MID = "2026-03-10";
    private static final String FIXTURE_DATE_LATE = "2026-07-20";

    @Test
    void applyMembershipPeriod_setsMinMaxFromStdrdDt() {
        final JournalThreadDto thread = JournalThreadDto.builder().id(FIXTURE_THREAD_ID).build();
        JournalThreadService.applyMembershipPeriod(thread, List.of(
                JournalEntryDto.builder().id(1).stdrdDt(FIXTURE_DATE_MID).build(),
                JournalEntryDto.builder().id(2).stdrdDt(FIXTURE_DATE_LATE + "T00:00:00").build(),
                JournalEntryDto.builder().id(3).stdrdDt(FIXTURE_DATE_EARLY).build(),
                JournalEntryDto.builder().id(4).stdrdDt(" ").build()
        ));

        assertEquals(FIXTURE_DATE_EARLY, thread.getFirstEntryDate());
        assertEquals(FIXTURE_DATE_LATE, thread.getLastEntryDate());
    }

    @Test
    void applyMembershipPeriod_sameDayKeepsEqualBounds() {
        final JournalThreadDto thread = JournalThreadDto.builder().id(FIXTURE_THREAD_ID).build();
        JournalThreadService.applyMembershipPeriod(thread, List.of(
                JournalEntryDto.builder().id(1).stdrdDt(FIXTURE_DATE_MID).build()
        ));

        assertEquals(FIXTURE_DATE_MID, thread.getFirstEntryDate());
        assertEquals(FIXTURE_DATE_MID, thread.getLastEntryDate());
    }

    @Test
    void applyMembershipPeriod_emptyLeavesNull() {
        final JournalThreadDto thread = JournalThreadDto.builder().id(FIXTURE_THREAD_ID).build();
        JournalThreadService.applyMembershipPeriod(thread, List.of());

        assertNull(thread.getFirstEntryDate());
        assertNull(thread.getLastEntryDate());
    }
}
