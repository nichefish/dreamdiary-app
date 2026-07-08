package io.nicheblog.dreamdiary.infrastructure.log.stats.repository.jpa;

import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import io.nicheblog.dreamdiary.infrastructure.log.stats.entity.LogStatsUserEntity;
import io.nicheblog.dreamdiary.infrastructure.log.stats.model.LogStatsUserIntrfc;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.QueryHint;
import java.time.LocalDateTime;
import java.util.List;

/**
 * LogStatsUserRepository
 * <pre>
 *  활동 로그 사용자별 통계 (JPA) Repository 인터페이스
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface LogStatsUserRepository
        extends BaseStreamRepository<LogStatsUserEntity, String> {

    /**
     * 로그인 유저별로 건수 조회
     *
     * @param searchStartDt 조회 시작일
     * @param searchEndDt 조회 종료일
     * @return {@link List} -- 로그인 유저별 활동 건수 통계 리스트
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT t.username as username, u.nickname as userNm, count(t.id) as actvtyCnt " +
            "FROM LogEntity t " +
            "INNER JOIN FETCH UserEntity u ON t.username = u.username " +
            "WHERE t.createdAt between :searchStartDt and :searchEndDt and u.nickname != null " +
            "AND (t.logType IS NULL OR t.logType <> 'SYSTEM') " +
            "GROUP BY t.username"
    )
    List<LogStatsUserIntrfc> getStatsUserIntrfcList(
            final @Param("searchStartDt") LocalDateTime searchStartDt,
            final @Param("searchEndDt") LocalDateTime searchEndDt
    );

    /**
     * 비로그인 유저 구분별로 건수 조회
     *
     * @param searchStartDt 조회 시작일
     * @param searchEndDt 조회 종료일
     * @return {@link List} -- 비로그인 사용자별 활동 건수 통계 리스트
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT t.username as username, u.nickname as userNm, count(t.id) as actvtyCnt " +
            "FROM LogEntity t " +
            "LEFT JOIN FETCH UserEntity u ON t.username = u.username " +
            "WHERE t.createdAt between :searchStartDt and :searchEndDt and u.nickname is null " +
            "AND (t.logType IS NULL OR t.logType <> 'SYSTEM') " +
            "GROUP BY t.username"
    )
    List<LogStatsUserIntrfc> getStatsNotUserIntrfcList(
            final @Param("searchStartDt") LocalDateTime searchStartDt,
            final @Param("searchEndDt") LocalDateTime searchEndDt
    );
}

