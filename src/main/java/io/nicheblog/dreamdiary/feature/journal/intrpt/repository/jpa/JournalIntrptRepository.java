package io.nicheblog.dreamdiary.feature.journal.intrpt.repository.jpa;

import io.nicheblog.dreamdiary.feature.journal.intrpt.entity.JournalIntrptEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JournalIntrptRepository
 * <pre>
 *  저널 해석 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface JournalIntrptRepository
        extends BaseStreamRepository<JournalIntrptEntity, Integer> {

    /**
     * 단건 조회 with EntityGraph
     * @param id Integer
     * @return 저널 해석 객체
     */
    @EntityGraph(attributePaths = {"journalDream"})
    @NotNull
    Optional<JournalIntrptEntity> findById(final @NotNull Integer id);

    /**
     * 해당 항목에서 해석 마지막 인덱스 조회
     *
     * @param journalDreamId 조회할 항목 번호
     * @return {@link Optional} -- 해당 일자에서 일기의 마지막 인덱스
     */
    @Query("SELECT MAX(intrpt.idx) " +
            "FROM JournalIntrptEntity intrpt " +
            "WHERE intrpt.journalDream.id = :journalDreamId")
    Optional<Integer> findLastIndexByJournalDay(final @Param("journalDreamId") Integer journalDreamId);
}


