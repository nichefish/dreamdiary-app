package io.nicheblog.dreamdiary.domain.jrnl.diary.repository.jpa;

import io.nicheblog.dreamdiary.domain.jrnl.diary.entity.JrnlDiaryEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.QueryHint;
import java.util.Optional;

/**
 * JrnlDiaryRepository
 * <pre>
 *  저널 일기 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository("jrnlDiaryRepository")
public interface JrnlDiaryRepository
        extends BaseStreamRepository<JrnlDiaryEntity, Integer> {

    /**
     * 단건 조회 with EntityGraph
     * @param id Integer
     * @return Optional<JrnlIntrptEntity>
     */
    @EntityGraph(attributePaths = {"jrnlEntry"})
    @NotNull
    Optional<JrnlDiaryEntity> findById(final @NotNull Integer id);

    /**
     * 해당 항목에서 일기 마지막 인덱스 조회
     *
     * @param jrnlEntryNo 조회할 항목 번호
     * @return {@link Optional} -- 해당 일자에서 일기의 마지막 인덱스
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT MAX(diary.idx) " +
            "FROM JrnlDiaryEntity diary " +
            "WHERE diary.jrnlEntry.postNo = :jrnlEntryNo")
    Optional<Integer> findLastIndexByJrnlEntry(final @Param("jrnlEntryNo") Integer jrnlEntryNo);
}