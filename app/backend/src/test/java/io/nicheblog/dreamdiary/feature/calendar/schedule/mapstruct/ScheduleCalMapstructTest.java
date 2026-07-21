package io.nicheblog.dreamdiary.feature.calendar.schedule.mapstruct;

import io.nicheblog.dreamdiary.feature.calendar.schedule.entity.ScheduleEntity;
import io.nicheblog.dreamdiary.feature.calendar.schedule.model.ScheduleCalDto;
import io.nicheblog.dreamdiary.feature.calendar.schedule.model.ScheduleDto;
import io.nicheblog.dreamdiary.infrastructure.code.Code;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 일정의 inclusive DB 기간과 FullCalendar exclusive 종료일 사이 매핑 계약 검증.
 */
class ScheduleCalMapstructTest {

    @Test
    void vacationTypeRoundTripsThroughWriteMapper() throws Exception {
        final ScheduleDto source = ScheduleDto.builder()
                .scheduleCd(Code.SCHEDULE_VCATN)
                .vcatnCd("ANNUAL")
                .title("휴가 일정")
                .bgnDt("2026-07-21")
                .endDt("2026-07-21")
                .build();

        final ScheduleEntity entity = ScheduleMapstruct.INSTANCE.toEntity(source);
        final ScheduleDto result = ScheduleMapstruct.INSTANCE.toDto(entity);

        assertEquals("ANNUAL", entity.getVcatnCd());
        assertEquals("ANNUAL", result.getVcatnCd());
    }

    @Test
    void multiDayScheduleUsesExclusiveEndAndAllDay() throws Exception {
        final ScheduleEntity entity = ScheduleEntity.builder()
                .id(1)
                .scheduleCd(Code.SCHEDULE_VCATN)
                .title("휴가 일정")
                .bgnDt(LocalDateTime.of(2026, 7, 21, 0, 0))
                .endDt(LocalDateTime.of(2026, 7, 23, 0, 0))
                .build();

        final ScheduleCalDto dto = ScheduleCalMapstruct.INSTANCE.toCalDto(entity);

        assertEquals("2026-07-21", dto.getStart());
        assertEquals("2026-07-24", dto.getEnd());
        assertEquals("2026-07-24", dto.getEndDt());
        assertTrue(dto.getAllDay());
    }
}
