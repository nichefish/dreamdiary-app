package io.nicheblog.dreamdiary.feature.journal.note.repository.jpa;

import io.nicheblog.dreamdiary.feature.journal.note.entity.JournalNoteEntity;
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
 * JournalNoteRepository
 * <pre>
 *  저널 노트 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface JournalNoteRepository
        extends BaseStreamRepository<JournalNoteEntity, Integer> {

    /**
     * 단건 조회 with EntityGraph
     * @param id Integer
     * @return 저널 노트 객체
     */
    @EntityGraph(attributePaths = {"journalChapter"})
    @NotNull
    Optional<JournalNoteEntity> findById(final @NotNull Integer id);

    /**
     * 해당 챕터에서 노트 마지막 인덱스 조회
     *
     * @param journalChapterId 조회할 항목 번호
     * @return {@link Optional} -- 해당 챕터에서 노트의 마지막 인덱스
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT MAX(note.sortOrder) " +
            "FROM JournalNoteEntity note " +
            "WHERE note.journalChapter.id = :journalChapterId")
    Optional<Integer> findLastIndexByJournalChapter(final @Param("journalChapterId") Integer journalChapterId);
}


