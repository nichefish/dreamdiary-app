package io.nicheblog.dreamdiary.feature.calendar.schedule.model;

import io.nicheblog.dreamdiary.feature.calendar.schedule.type.VacationDayStatus;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * 날짜 하나에 집계된 사용자 휴가 투영 정보.
 */
@Value
@Builder
public class VacationDayInfo {

    @Builder.Default
    VacationDayStatus status = VacationDayStatus.NONE;

    @Builder.Default
    List<String> reasonList = List.of();
}
