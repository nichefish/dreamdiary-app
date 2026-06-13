package io.nicheblog.dreamdiary.feature.journal.entitycatalog.repository.jpa;

import io.nicheblog.dreamdiary.feature.journal.entitycatalog.entity.JournalEntryEntityRefEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository for entity mentions extracted from journal entries.
 */
@Repository
public interface JournalEntryEntityRefRepository
        extends BaseStreamRepository<JournalEntryEntityRefEntity, Integer> {

    /**
     * Find all entity mentions for one journal entry in source order.
     *
     * @param journalEntryId journal entry ID
     * @return ordered entity mentions
     */
    List<JournalEntryEntityRefEntity> findAllByJournalEntryIdOrderBySortOrderAscIdAsc(Integer journalEntryId);

    /**
     * Find all mentions linked to one normalized journal entity.
     *
     * @param journalEntityId journal entity ID
     * @return linked entry mentions
     */
    List<JournalEntryEntityRefEntity> findAllByJournalEntityIdOrderByCreatedAtAscIdAsc(Integer journalEntityId);
}
