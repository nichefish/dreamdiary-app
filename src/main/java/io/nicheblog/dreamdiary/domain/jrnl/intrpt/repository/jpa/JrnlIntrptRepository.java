package io.nicheblog.dreamdiary.domain.jrnl.intrpt.repository.jpa;

import io.nicheblog.dreamdiary.domain.jrnl.intrpt.entity.JrnlIntrptEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JrnlIntrptRepository
 * <pre>
 *  저널 해석 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository("jrnlIntrptRepository")
public interface JrnlIntrptRepository
        extends BaseStreamRepository<JrnlIntrptEntity, Integer> {

    /**
     * 해당 항목에서 해석 마지막 인덱스 조회
     *
     * @param jrnlDreamNo 조회할 항목 번호
     * @return {@link Optional} -- 해당 일자에서 일기의 마지막 인덱스
     */
    @Query("SELECT MAX(intrpt.idx) " +
            "FROM JrnlIntrptEntity intrpt " +
            "INNER JOIN FETCH JrnlDreamEntity dream ON intrpt.jrnlDreamNo = dream.postNo " +
            "WHERE intrpt.jrnlDreamNo = :jrnlDreamNo")
    Optional<Integer> findLastIndexByJrnlDay(final @Param("jrnlDreamNo") Integer jrnlDreamNo);
}