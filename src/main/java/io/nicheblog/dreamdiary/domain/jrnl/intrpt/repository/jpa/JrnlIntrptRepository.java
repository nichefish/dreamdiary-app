package io.nicheblog.dreamdiary.domain.jrnl.intrpt.repository.jpa;

import io.nicheblog.dreamdiary.domain.jrnl.intrpt.entity.JrnlIntrptEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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
     * 태그를 포함한 목록 조회 (with EntityGraph)
     *
     * @param spec 중복 체크를 위한 날짜
     * @return {@link List} -- 태그를 포함한 목록
     */
    @EntityGraph(value = "JrnlIntrptEntity.withTags", type = EntityGraph.EntityGraphType.LOAD)
    List<JrnlIntrptEntity> findAll(Specification<JrnlIntrptEntity> spec);

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