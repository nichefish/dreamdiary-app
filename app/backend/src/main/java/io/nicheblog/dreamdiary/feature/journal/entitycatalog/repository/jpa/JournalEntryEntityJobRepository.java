package io.nicheblog.dreamdiary.feature.journal.entitycatalog.repository.jpa;

import io.nicheblog.dreamdiary.feature.journal.entitycatalog.entity.JournalEntryEntityJobEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * JPA repository for asynchronous entity sync queue rows.
 */
@Repository
public interface JournalEntryEntityJobRepository
        extends BaseStreamRepository<JournalEntryEntityJobEntity, Integer> {

    /**
     * Find the queue row for one journal entry.
     *
     * @param journalEntryId source journal entry ID
     * @return queue row when present
     */
    Optional<JournalEntryEntityJobEntity> findFirstByJournalEntryId(Integer journalEntryId);

    /**
     * Count queue rows by status.
     *
     * @param jobStatus queue status
     * @return row count
     */
    long countByJobStatus(String jobStatus);

    /**
     * Find all rows by status.
     *
     * @param jobStatus queue status
     * @return queue rows
     */
    List<JournalEntryEntityJobEntity> findAllByJobStatus(String jobStatus);

    /**
     * Find stale processing rows older than the given cutoff.
     *
     * @param jobStatus processing status
     * @param updatedAt cutoff time
     * @param pageable page / limit
     * @return stale processing rows
     */
    List<JournalEntryEntityJobEntity> findAllByJobStatusAndUpdatedAtBeforeOrderByUpdatedAtAscIdAsc(
            String jobStatus,
            LocalDateTime updatedAt,
            Pageable pageable
    );

    /**
     * Claim pending rows in queue order while skipping rows already locked by another worker.
     *
     * @param batchSize number of rows to claim
     * @return locked queue rows
     */
    @Query(
            value = "SELECT * FROM journal_entry_entity_job" +
                    " WHERE job_status = 'PENDING' AND deleted_at IS NULL" +
                    " ORDER BY created_at ASC, id ASC" +
                    " LIMIT :batchSize FOR UPDATE SKIP LOCKED",
            nativeQuery = true
    )
    List<JournalEntryEntityJobEntity> findAndLockPendingBatch(@Param("batchSize") int batchSize);
}
