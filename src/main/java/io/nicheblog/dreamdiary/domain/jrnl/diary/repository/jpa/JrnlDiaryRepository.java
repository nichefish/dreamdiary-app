package io.nicheblog.dreamdiary.domain.jrnl.diary.repository.jpa;

import io.nicheblog.dreamdiary.domain.jrnl.diary.entity.JrnlDiaryEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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
     * 태그를 포함한 목록 조회 (with EntityGraph)
     *
     * @param spec 중복 체크를 위한 날짜
     * @return {@link List} -- 태그를 포함한 목록
     */
    @EntityGraph(value = "JrnlDiaryEntity.withTags", type = EntityGraph.EntityGraphType.LOAD)
    List<JrnlDiaryEntity> findAll(Specification<JrnlDiaryEntity> spec);

    /**
     * 해당 항목에서 일기 마지막 인덱스 조회
     *
     * @param jrnlEntryNo 조회할 항목 번호
     * @return {@link Optional} -- 해당 일자에서 일기의 마지막 인덱스
     */
    @Query("SELECT MAX(diary.idx) " +
            "FROM JrnlDiaryEntity diary " +
            "INNER JOIN FETCH JrnlEntryEntity entry ON diary.jrnlEntryNo = entry.postNo " +
            "WHERE diary.jrnlEntryNo = :jrnlEntryNo")
    Optional<Integer> findLastIndexByJrnlDay(final @Param("jrnlEntryNo") Integer jrnlEntryNo);
}