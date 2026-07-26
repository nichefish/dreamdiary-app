package io.nicheblog.dreamdiary.feature.calendar.schedule.model;

import lombok.Value;

import java.time.LocalDateTime;

/**
 * 사용자별 휴가 일자 투영에 필요한 일정 최소 필드.
 * 엔티티의 attachable 구성과 참가자 목록을 저널 조회 경로로 전파하지 않는다.
 */
@Value
public class VacationScheduleProjection {

    Integer id;
    String title;
    String vcatnCd;
    LocalDateTime bgnDt;
    LocalDateTime endDt;
}
