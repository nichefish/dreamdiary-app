package io.nicheblog.dreamdiary.feature.journal.embedding.repository.jpa;

import io.nicheblog.dreamdiary.feature.journal.embedding.entity.JournalEntryEmbeddingSyncJobEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface JournalEntryEmbeddingSyncJobRepository
        extends BaseStreamRepository<JournalEntryEmbeddingSyncJobEntity, Integer> {

    Optional<JournalEntryEmbeddingSyncJobEntity> findFirstByJobKey(String jobKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT job FROM JournalEntryEmbeddingSyncJobEntity job WHERE job.jobKey = :jobKey")
    Optional<JournalEntryEmbeddingSyncJobEntity> findFirstByJobKeyForUpdate(@Param("jobKey") String jobKey);
}
