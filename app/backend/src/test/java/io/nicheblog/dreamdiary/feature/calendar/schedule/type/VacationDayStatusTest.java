package io.nicheblog.dreamdiary.feature.calendar.schedule.type;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 휴가 세부 코드의 일자 시간 범위 판정 정책을 검증한다.
 */
class VacationDayStatusTest {

    @Test
    void knownVacationCodesMapToExplicitDayStatuses() {
        assertEquals(VacationDayStatus.AM_HALF, VacationDayStatus.fromVacationCode("AM_HALF"));
        assertEquals(VacationDayStatus.PM_HALF, VacationDayStatus.fromVacationCode("PM_HALF"));
        for (final String fullDayCode : List.of("ANNUAL", "PBLEN", "CTSNN", "MNSTR", "UNPAID")) {
            assertEquals(VacationDayStatus.FULL_DAY, VacationDayStatus.fromVacationCode(fullDayCode));
        }
    }

    @Test
    void missingOrUnregisteredCodeRemainsUnknown() {
        assertEquals(VacationDayStatus.UNKNOWN, VacationDayStatus.fromVacationCode(null));
        assertEquals(VacationDayStatus.UNKNOWN, VacationDayStatus.fromVacationCode("FUTURE_TYPE"));
        assertEquals(VacationDayStatus.UNKNOWN, VacationDayStatus.AM_HALF.merge(VacationDayStatus.UNKNOWN));
        assertEquals(VacationDayStatus.FULL_DAY, VacationDayStatus.UNKNOWN.merge(VacationDayStatus.FULL_DAY));
    }
}
