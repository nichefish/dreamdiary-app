package io.nicheblog.dreamdiary.domain.jrnl.dream.repository.jpa;

import io.nicheblog.dreamdiary.domain.jrnl.diary.entity.JrnlDiaryEntity;
import io.nicheblog.dreamdiary.domain.jrnl.dream.entity.JrnlDreamEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

/**
 * JrnlDreamRepository
 * <pre>
 *  저널 꿈 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository("jrnlDreamRepository")
public interface JrnlDreamRepository
        extends BaseStreamRepository<JrnlDreamEntity, Integer> {

    /**
     * 태그를 포함한 목록 조회 (with EntityGraph)
     *
     * @param spec 중복 체크를 위한 날짜
     * @return {@link List} -- 태그를 포함한 목록
     */
    @EntityGraph(value = "JrnlDreamEntity.withTags", type = EntityGraph.EntityGraphType.LOAD)
    List<JrnlDreamEntity> findAll(Specification<JrnlDreamEntity> spec);

    /**
     * 해당 일자에서 꿈 마지막 인덱스 조회
     *
     * @param jrnlDayNo 조회할 일자 번호
     * @return {@link Optional} -- 해당 일자에서 꿈의 마지막 인덱스
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT MAX(dream.idx) " +
            "FROM JrnlDreamEntity dream " +
            "INNER JOIN FETCH JrnlDayEntity day ON dream.jrnlDayNo = day.postNo " +
            "WHERE dream.jrnlDayNo = :jrnlDayNo AND NOT(dream.elseDreamerNm = 'Y')")
    Optional<Integer> findLastIndexByJrnlDay(final @Param("jrnlDayNo") Integer jrnlDayNo);
}