package io.nicheblog.dreamdiary.feature.calendar.schedule.service;

import io.nicheblog.dreamdiary.feature.calendar.schedule.model.VacationDayInfo;
import io.nicheblog.dreamdiary.feature.calendar.schedule.model.VacationScheduleProjection;
import io.nicheblog.dreamdiary.feature.calendar.schedule.repository.jpa.ScheduleRepository;
import io.nicheblog.dreamdiary.feature.calendar.schedule.type.VacationDayStatus;
import io.nicheblog.dreamdiary.infrastructure.code.Code;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 사용자 참가 휴가를 일자별 상태로 펼치는 조회 계약을 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class ScheduleVacationQueryServiceTest {

    private static final String FIXTURE_USERNAME = "fixture_user";

    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private ScheduleVacationQueryService scheduleVacationQueryService;

    @Test
    void multiDayVacationExpandsOnlyRequestedDatesWithOneRangeQuery() {
        final VacationScheduleProjection annual = vacation(
                1,
                "연차 휴무",
                "ANNUAL",
                LocalDateTime.of(2026, 7, 21, 0, 0),
                LocalDateTime.of(2026, 7, 23, 0, 0)
        );
        when(scheduleRepository.findParticipantSchedulesOverlapping(
                FIXTURE_USERNAME,
                Code.SCHEDULE_VCATN,
                LocalDateTime.of(2026, 7, 21, 0, 0),
                LocalDateTime.of(2026, 7, 24, 0, 0)
        )).thenReturn(List.of(annual));

        final Map<String, VacationDayInfo> result = scheduleVacationQueryService.getVacationDayMapByUser(
                FIXTURE_USERNAME,
                List.of("2026-07-21", "2026-07-23")
        );

        assertEquals(VacationDayStatus.FULL_DAY, result.get("2026-07-21").getStatus());
        assertEquals(VacationDayStatus.FULL_DAY, result.get("2026-07-23").getStatus());
        assertFalse(result.containsKey("2026-07-22"));
        verify(scheduleRepository).findParticipantSchedulesOverlapping(
                FIXTURE_USERNAME,
                Code.SCHEDULE_VCATN,
                LocalDateTime.of(2026, 7, 21, 0, 0),
                LocalDateTime.of(2026, 7, 24, 0, 0)
        );
    }

    @Test
    void oppositeHalfDaysBecomeFullDayAndUnknownCodeStaysUnknown() {
        when(scheduleRepository.findParticipantSchedulesOverlapping(
                FIXTURE_USERNAME,
                Code.SCHEDULE_VCATN,
                LocalDateTime.of(2026, 7, 21, 0, 0),
                LocalDateTime.of(2026, 7, 23, 0, 0)
        )).thenReturn(List.of(
                vacation(1, "오전 휴가", "AM_HALF", date(21), date(21)),
                vacation(2, "오후 휴가", "PM_HALF", date(21), date(21)),
                vacation(3, "기타 휴가", "FUTURE_TYPE", date(22), date(22))
        ));

        final Map<String, VacationDayInfo> result = scheduleVacationQueryService.getVacationDayMapByUser(
                FIXTURE_USERNAME,
                List.of("2026-07-21", "2026-07-22")
        );

        assertEquals(VacationDayStatus.FULL_DAY, result.get("2026-07-21").getStatus());
        assertEquals(List.of("오전 휴가", "오후 휴가"), result.get("2026-07-21").getReasonList());
        assertEquals(VacationDayStatus.UNKNOWN, result.get("2026-07-22").getStatus());
        assertEquals(List.of("기타 휴가"), result.get("2026-07-22").getReasonList());
    }

    private VacationScheduleProjection vacation(
            final Integer id,
            final String title,
            final String vcatnCd,
            final LocalDateTime bgnDt,
            final LocalDateTime endDt
    ) {
        return new VacationScheduleProjection(id, title, vcatnCd, bgnDt, endDt);
    }

    private LocalDateTime date(final int day) {
        return LocalDateTime.of(2026, 7, day, 0, 0);
    }
}
