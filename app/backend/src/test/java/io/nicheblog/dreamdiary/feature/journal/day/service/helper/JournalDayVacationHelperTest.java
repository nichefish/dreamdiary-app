package io.nicheblog.dreamdiary.feature.journal.day.service.helper;

import io.nicheblog.dreamdiary.feature.calendar.schedule.model.VacationDayInfo;
import io.nicheblog.dreamdiary.feature.calendar.schedule.type.VacationDayStatus;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 사용자 휴가 투영값을 저널 일자 DTO에 적용·초기화하는 계약을 검증한다.
 */
class JournalDayVacationHelperTest {

    @Test
    void appliesVacationInfoToMatchingJournalDay() {
        final JournalDayDto journalDay = JournalDayDto.builder()
                .journalDate("2026-07-21")
                .build();
        final VacationDayInfo vacationDayInfo = VacationDayInfo.builder()
                .status(VacationDayStatus.AM_HALF)
                .reasonList(List.of("오전 휴가"))
                .build();

        JournalDayVacationHelper.setVacationInfo(
                journalDay,
                Map.of("2026-07-21", vacationDayInfo)
        );

        assertEquals(VacationDayStatus.AM_HALF, journalDay.getVacationDayStatus());
        assertEquals(List.of("오전 휴가"), journalDay.getVacationReasonList());
    }

    @Test
    void missingProjectionClearsStaleCachedVacationInfo() {
        final JournalDayDto journalDay = JournalDayDto.builder()
                .journalDate("2026-07-21")
                .vacationDayStatus(VacationDayStatus.FULL_DAY)
                .vacationReasonList(List.of("이전 휴가"))
                .build();

        JournalDayVacationHelper.setVacationInfo(journalDay, Map.of());

        assertEquals(VacationDayStatus.NONE, journalDay.getVacationDayStatus());
        assertTrue(journalDay.getVacationReasonList().isEmpty());
    }
}
