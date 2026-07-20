package io.nicheblog.dreamdiary.feature.journal.thread.repository.jpa;

import io.nicheblog.dreamdiary.feature.journal.thread.entity.JournalThreadEntryEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * JournalThreadEntryRepository
 * <pre>
 *  저널 스레드-엔트리 소속 (JPA) Repository 인터페이스.
 *
 *  엔티티에 {@code @Where(deleted_at IS NULL)} 이 걸려 있어 일반 조회는 살아있는 소속만 본다.
 *  소프트 삭제된 행까지 봐야 하는 곳은 {@link #findAnyByPair} 처럼 nativeQuery 로 우회한다.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface JournalThreadEntryRepository
        extends BaseStreamRepository<JournalThreadEntryEntity, Integer> {

    /**
     * 스레드의 소속 엔트리 목록.
     * sort_order 가 NULL 인 행은 뒤로 보내고, 그 안에서는 등록 순으로 정렬한다.
     *
     * @param threadId 스레드 ID
     * @param createdBy 등록자 계정명
     * @return 해당 스레드의 살아있는 소속 목록
     */
    @Query("SELECT te FROM JournalThreadEntryEntity te " +
            "WHERE te.threadId = :threadId " +
            "  AND te.createdBy = :createdBy " +
            "ORDER BY CASE WHEN te.sortOrder IS NULL THEN 1 ELSE 0 END, te.sortOrder ASC, te.createdAt ASC")
    List<JournalThreadEntryEntity> findAllByThread(
            final @Param("threadId") Integer threadId,
            final @Param("createdBy") String createdBy
    );

    /**
     * 엔트리가 속한 스레드 목록.
     * 한 엔트리가 여러 스레드에 속할 수 있으므로 List 로 돌려준다.
     *
     * @param entryId 엔트리 ID
     * @param createdBy 등록자 계정명
     * @return 해당 엔트리의 살아있는 소속 목록
     */
    List<JournalThreadEntryEntity> findAllByEntryIdAndCreatedByOrderByCreatedAtAsc(
            final Integer entryId,
            final String createdBy
    );

    /**
     * 여러 엔트리의 소속을 한 번에 조회한다. (목록 화면 N+1 방지)
     * <p>
     * 엔트리 목록을 그리는 화면은 각 엔트리가 어느 흐름에 속하는지 함께 표시한다.
     * 엔트리마다 단건 조회하면 화면 한 장에 N+1 요청이 나가므로, 일괄로 받아 메모리에서 묶는다.
     * 스레드 제목을 함께 쓰기 때문에 조인을 미리 걸어 가져온다.
     *
     * @param entryIds 대상 엔트리 ID 집합 (비어 있으면 호출하지 말 것)
     * @param createdBy 등록자 계정명
     * @return 해당 엔트리들의 살아있는 소속 목록
     */
    @Query("SELECT te FROM JournalThreadEntryEntity te " +
            "LEFT JOIN FETCH te.journalThread " +
            "WHERE te.entryId IN :entryIds " +
            "  AND te.createdBy = :createdBy " +
            "ORDER BY te.entryId ASC, te.createdAt ASC")
    List<JournalThreadEntryEntity> findAllByEntryIds(
            final @Param("entryIds") Collection<Integer> entryIds,
            final @Param("createdBy") String createdBy
    );

    /**
     * 소속 행을 소프트 삭제 여부와 무관하게 조회한다.
     * <p>
     * UNIQUE KEY {@code uk_journal_thread_entry (thread_id, entry_id, created_by)} 는 deleted_at 을
     * 포함하지 않는다. 따라서 한 번 해제(소프트 삭제)한 소속을 다시 등록하면 INSERT 가 UNIQUE 제약에
     * 걸린다. 등록 경로는 이 메서드로 기존 행을 먼저 찾아 되살리고, 없을 때만 INSERT 한다.
     *
     * @param threadId 스레드 ID
     * @param entryId 엔트리 ID
     * @param createdBy 등록자 계정명
     * @return 소프트 삭제된 행을 포함한 기존 소속 (없으면 empty)
     */
    @Query(value = "SELECT * " +
            "FROM journal_thread_entry te " +
            "WHERE te.thread_id = :threadId " +
            "  AND te.entry_id = :entryId " +
            "  AND te.created_by = :createdBy " +
            "LIMIT 1", nativeQuery = true)
    Optional<JournalThreadEntryEntity> findAnyByPair(
            final @Param("threadId") Integer threadId,
            final @Param("entryId") Integer entryId,
            final @Param("createdBy") String createdBy
    );

    /**
     * 소프트 삭제된 소속을 되살린다. (등록 경로 전용)
     *
     * @param id 소속 ID
     * @return 갱신된 행 수
     */
    @Modifying
    @Query(value = "UPDATE journal_thread_entry " +
            "SET deleted_at = NULL " +
            "WHERE id = :id", nativeQuery = true)
    int reviveById(final @Param("id") Integer id);

    /**
     * 스레드의 소속을 일괄 소프트 삭제한다. (스레드 삭제 시 정리용)
     *
     * @param threadId 스레드 ID
     * @param createdBy 등록자 계정명
     * @return 갱신된 행 수
     */
    @Modifying
    @Query("UPDATE JournalThreadEntryEntity te " +
            "SET te.deletedAt = CURRENT_TIMESTAMP " +
            "WHERE te.threadId = :threadId " +
            "  AND te.createdBy = :createdBy")
    int softDeleteAllByThread(
            final @Param("threadId") Integer threadId,
            final @Param("createdBy") String createdBy
    );
}
