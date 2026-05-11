package io.nicheblog.dreamdiary.feature.journal.embedding.repository.jpa;

import io.nicheblog.dreamdiary.feature.journal.embedding.entity.JournalEntryEmbeddingEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface JournalEntryEmbeddingRepository
        extends BaseStreamRepository<JournalEntryEmbeddingEntity, Integer> {

    List<JournalEntryEmbeddingEntity> findAllByEmbeddingStatusOrderByCreatedAtAscIdAsc(String embeddingStatus, Pageable pageable);

    List<JournalEntryEmbeddingEntity> findAllByEmbeddingStatusAndUpdatedAtBeforeOrderByUpdatedAtAscIdAsc(
            String embeddingStatus,
            Date updatedAt,
            Pageable pageable
    );

    long countByEmbeddingStatus(String embeddingStatus);
}
