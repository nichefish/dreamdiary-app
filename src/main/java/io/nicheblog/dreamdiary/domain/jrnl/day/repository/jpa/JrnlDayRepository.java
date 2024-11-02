package io.nicheblog.dreamdiary.domain.jrnl.day.repository.jpa;

import io.nicheblog.dreamdiary.domain.jrnl.day.entity.JrnlDayEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.QueryHint;
import java.util.Date;

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
            "WHERE day.jrnlDt = :jrnlDt " +
            " AND day.regstrId = :regstrId")
    Integer countByJrnlDt(final @Param("jrnlDt") Date jrnlDt, final @Param("regstrId") String regstrId);

    /**
     * 주어진 날짜에 해당하는 {@link JrnlDayEntity}를 반환합니다.
     *
     * @param jrnlDt   조회할 날짜
     * @param regstrId
     * @return {@link Integer} -- 주어진 날짜에 해당하는 저널 일자 객체
     */
    @Query("SELECT day " +
            "FROM JrnlDayEntity day " +
            "WHERE day.jrnlDt = :jrnlDt " +
            " AND day.regstrId = :regstrId")
    JrnlDayEntity findByJrnlDt(final @Param("jrnlDt") Date jrnlDt, final @Param("regstrId") String regstrId);
}