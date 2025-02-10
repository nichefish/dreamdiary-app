package io.nicheblog.dreamdiary.extension.log.stats.repository.jpa;

import io.nicheblog.dreamdiary.extension.log.stats.entity.LogStatsUserEntity;
import io.nicheblog.dreamdiary.extension.log.stats.model.LogStatsUserIntrfc;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.QueryHint;
import java.util.Date;
import java.util.List;

/**
 * LogStatsUserRepository
 * <pre>
 *  활동 로그 사용자별 통계 (JPA) Repository 인터페이스
 * </pre>
 *
 * @author nichefish
 */
@Repository("logStatsUserRepository")
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
    @Query("SELECT t.userId as userId, u.nickNm as userNm, count(t.logActvtyNo) as actvtyCnt " +
            "FROM LogActvtyEntity t " +
            "INNER JOIN FETCH UserEntity u ON t.userId = u.userId " +
            "WHERE t.logDt between :searchStartDt and :searchEndDt and u.nickNm != null " +
            "GROUP BY t.userId"
    )
    List<LogStatsUserIntrfc> getStatsUserIntrfcList(
            final @Param("searchStartDt") Date searchStartDt,
            final @Param("searchEndDt") Date searchEndDt
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
    @Query("SELECT t.userId as userId, u.nickNm as userNm, count(t.logActvtyNo) as actvtyCnt " +
            "FROM LogActvtyEntity t " +
            "LEFT JOIN FETCH UserEntity u ON t.userId = u.userId " +
            "WHERE t.logDt between :searchStartDt and :searchEndDt and u.nickNm is null " +
            "GROUP BY t.userId"
    )
    List<LogStatsUserIntrfc> getStatsNotUserIntrfcList(
            final @Param("searchStartDt") Date searchStartDt,
            final @Param("searchEndDt") Date searchEndDt
    );
}
