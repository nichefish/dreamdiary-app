package io.nicheblog.dreamdiary.feature.calendar.schedule.type;

import lombok.AllArgsConstructor;

/**
 * ScheduleType
 *
 * @author nichefish
 */
@AllArgsConstructor
public enum ScheduleType {
    HOLYDAY("공휴일"),
    CEREMONY("행사"),
    TLCMMT("재택근무"),
    OUTDT("외근"),
    INDT("내부일정"),
    VCATN("휴가"),
    BRTHDY("생일"),
    ETC("기타");

    public final String desc;
}
