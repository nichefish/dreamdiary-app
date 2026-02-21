package io.nicheblog.dreamdiary.domain.jrnl.intrpt.repository.jpa;

import io.nicheblog.dreamdiary.domain.jrnl.diary.entity.JrnlDiaryEntity;
import io.nicheblog.dreamdiary.domain.jrnl.intrpt.entity.JrnlIntrptEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
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
     * 단건 조회 with EntityGraph
     * @param id Integer
     * @return Optional<JrnlIntrptEntity>
     */
    @EntityGraph(attributePaths = {"jrnlDream"})
    @NotNull
    Optional<JrnlIntrptEntity> findById(final @NotNull Integer id);


    /**
     * 해당 항목에서 해석 마지막 인덱스 조회
     *
     * @param jrnlDreamNo 조회할 항목 번호
     * @return {@link Optional} -- 해당 일자에서 일기의 마지막 인덱스
     */
    @Query("SELECT MAX(intrpt.idx) " +
            "FROM JrnlIntrptEntity intrpt " +
            "WHERE intrpt.jrnlDream.postNo = :jrnlDreamNo")
    Optional<Integer> findLastIndexByJrnlDay(final @Param("jrnlDreamNo") Integer jrnlDreamNo);
}