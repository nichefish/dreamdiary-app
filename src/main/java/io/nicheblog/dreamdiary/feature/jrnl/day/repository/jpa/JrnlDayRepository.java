package io.nicheblog.dreamdiary.feature.jrnl.day.repository.jpa;

import io.nicheblog.dreamdiary.feature.jrnl.day.entity.JrnlDayEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.QueryHint;
import java.util.Date;
import java.util.List;

/**
 * JrnlDayRepository
 * <pre>
 *  꿈 일자 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository("jrnlDayRepository")
public interface JrnlDayRepository
        extends BaseStreamRepository<JrnlDayEntity, Integer> {

    /**
     * 주어진 날짜에 대한 기 등록 여부를 반환합니다.
     *
     * @param jrnlDt 중복 체크를 위한 날짜
     * @return {@link Integer} -- 주어진 날짜에 대한 중복된 항목의 수
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT COUNT(day.jrnlDt) " +
            "FROM JrnlDayEntity day " +
            "WHERE day.jrnlDt = :jrnlDt AND day.regstrId = :regstrId")
    Integer countByJrnlDt(final @Param("jrnlDt") Date jrnlDt, final @Param("regstrId") String regstrId);

    /**
     * 주어진 날짜에 해당하는 {@link JrnlDayEntity}를 반환합니다.
     *
     * @param jrnlDt 조회할 날짜
     * @param regstrId 등록자 ID
     * @return {@link Integer} -- 주어진 날짜에 해당하는 저널 일자 객체
     */
    @Query("SELECT day " +
            "FROM JrnlDayEntity day " +
            "WHERE day.jrnlDt = :jrnlDt AND day.regstrId = :regstrId")
    @EntityGraph(value = "JrnlDayEntity.withTags", type = EntityGraph.EntityGraphType.LOAD)
    JrnlDayEntity findByJrnlDt(final @Param("jrnlDt") Date jrnlDt, final @Param("regstrId") String regstrId);

    /**
     * 메타가 기록된 연도 목록을 최신순으로 조회합니다.
     *
     * @param metaId 메타 ID
     * @param regstrId 사용자 ID
     * @return 연도 목록
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT DISTINCT day.yy " +
            "FROM JrnlDayEntity day " +
            "INNER JOIN day.meta.list metaContent " +
            "WHERE metaContent.metaId = :metaId " +
            "  AND metaContent.refContentType = 'JRNL_DAY' " +
            "  AND metaContent.regstrId = :regstrId " +
            "ORDER BY day.yy DESC")
    List<Integer> findDistinctYysByMetaIdAndRegstrId(final @Param("metaId") Integer metaId, final @Param("regstrId") String regstrId);
}
