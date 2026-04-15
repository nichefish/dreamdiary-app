package io.nicheblog.dreamdiary.feature.journal.day.repository.jpa;

import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayEntity;
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
 * JournalDayRepository
 * <pre>
 *  꿈 일자 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository("journalDayRepository")
public interface JournalDayRepository
        extends BaseStreamRepository<JournalDayEntity, Integer> {

    /**
     * 주어진 날짜에 대한 기 등록 여부를 반환합니다.
     *
     * @param journalDt 중복 체크를 위한 날짜
     * @return {@link Integer} -- 주어진 날짜에 대한 중복된 항목의 수
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT COUNT(day.journalDt) " +
            "FROM JournalDayEntity day " +
            "WHERE day.journalDt = :journalDt AND day.createdBy = :createdBy")
    Integer countByJournalDt(final @Param("journalDt") Date journalDt, final @Param("createdBy") String createdBy);

    /**
     * 주어진 날짜에 해당하는 {@link JournalDayEntity}를 반환합니다.
     *
     * @param journalDt 조회할 날짜
     * @param createdBy 등록자 ID
     * @return {@link Integer} -- 주어진 날짜에 해당하는 저널 일자 객체
     */
    @Query("SELECT day " +
            "FROM JournalDayEntity day " +
            "WHERE day.journalDt = :journalDt AND day.createdBy = :createdBy")
    @EntityGraph(value = "JournalDayEntity.withTags", type = EntityGraph.EntityGraphType.LOAD)
    JournalDayEntity findByJournalDt(final @Param("journalDt") Date journalDt, final @Param("createdBy") String createdBy);

    /**
     * 메타가 기록된 연도 목록을 최신순으로 조회합니다.
     *
     * @param metaId 메타 ID
     * @param createdBy 사용자 ID
     * @return 연도 목록
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT DISTINCT day.yy " +
            "FROM JournalDayEntity day " +
            "INNER JOIN day.meta.list metaContent " +
            "WHERE metaContent.metaId = :metaId " +
            "  AND metaContent.refContentType = 'JOURNAL_DAY' " +
            "  AND metaContent.createdBy = :createdBy " +
            "ORDER BY day.yy DESC")
    List<Integer> findDistinctYysByMetaIdAndCreatedBy(final @Param("metaId") Integer metaId, final @Param("createdBy") String createdBy);
}

