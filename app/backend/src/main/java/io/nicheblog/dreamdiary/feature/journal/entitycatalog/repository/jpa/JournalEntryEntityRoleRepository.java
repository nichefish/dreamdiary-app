package io.nicheblog.dreamdiary.feature.journal.entitycatalog.repository.jpa;

import io.nicheblog.dreamdiary.feature.journal.entitycatalog.entity.JournalEntryEntityRoleEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * JPA repository for per-mention entity role evidence.
 */
@Repository
public interface JournalEntryEntityRoleRepository
        extends BaseStreamRepository<JournalEntryEntityRoleEntity, Integer> {

    /**
     * Find all role rows linked to one entity-ref row.
     *
     * @param journalEntryEntityRefId entity-ref ID
     * @return linked role rows
     */
    List<JournalEntryEntityRoleEntity> findAllByJournalEntryEntityRefIdOrderByIdAsc(Integer journalEntryEntityRefId);

    /**
     * Find all role rows linked to many entity-ref rows.
     *
     * @param journalEntryEntityRefIdList entity-ref ID list
     * @return linked role rows
     */
    List<JournalEntryEntityRoleEntity> findAllByJournalEntryEntityRefIdIn(Collection<Integer> journalEntryEntityRefIdList);
}
