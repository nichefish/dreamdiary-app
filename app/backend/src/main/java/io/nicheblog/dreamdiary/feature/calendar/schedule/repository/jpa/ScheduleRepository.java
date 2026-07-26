package io.nicheblog.dreamdiary.feature.calendar.schedule.repository.jpa;

import io.nicheblog.dreamdiary.feature.calendar.schedule.entity.ScheduleEntity;
import io.nicheblog.dreamdiary.feature.calendar.schedule.model.VacationScheduleProjection;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.QueryHint;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ScheduleRepository
 * <pre>
 *  일정 상세 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface ScheduleRepository
        extends BaseStreamRepository<ScheduleEntity, Integer> {

    /**
     * 날짜(date)에 대하여 휴일 해당 여부 조회
     *
     * @param groupId - 조회할 일정 코드 또는 그룹 ID
     * @param date - 조회할 시작 날짜
     * @return 휴일에 해당하는 일정 정보가 포함된 `ScheduleEntity` 객체의 Optional
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Optional<ScheduleEntity> findByScheduleCdAndBgnDt(final String groupId, final LocalDateTime date);

    /**
     * 사용자가 참가한 일정 중 조회 범위와 겹치는 특정 일정 유형을 표시용 projection으로 조회한다.
     * 종료일 NULL인 기존 행은 시작일과 같은 단일 일정으로 취급한다.
     *
     * @param username 참가자 계정명
     * @param scheduleCd 일정 유형 코드
     * @param rangeStartInclusive 조회 시작 시각(포함)
     * @param rangeEndExclusive 조회 종료 시각(미포함)
     * @return 기간과 겹치는 일정 projection 목록
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT DISTINCT new io.nicheblog.dreamdiary.feature.calendar.schedule.model.VacationScheduleProjection(" +
            "s.id, s.title, s.vcatnCd, s.bgnDt, s.endDt) " +
            "FROM ScheduleEntity s JOIN s.prtcpntList p " +
            "WHERE p.username = :username AND s.scheduleCd = :scheduleCd " +
            "AND s.bgnDt < :rangeEndExclusive " +
            "AND COALESCE(s.endDt, s.bgnDt) >= :rangeStartInclusive " +
            "ORDER BY s.bgnDt ASC, s.id ASC")
    List<VacationScheduleProjection> findParticipantSchedulesOverlapping(
            @Param("username") final String username,
            @Param("scheduleCd") final String scheduleCd,
            @Param("rangeStartInclusive") final LocalDateTime rangeStartInclusive,
            @Param("rangeEndExclusive") final LocalDateTime rangeEndExclusive
    );
}
