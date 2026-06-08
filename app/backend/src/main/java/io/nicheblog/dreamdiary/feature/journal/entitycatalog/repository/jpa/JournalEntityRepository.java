package io.nicheblog.dreamdiary.feature.journal.entitycatalog.repository.jpa;

import io.nicheblog.dreamdiary.feature.journal.entitycatalog.entity.JournalEntityEntity;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.type.JournalEntityType;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA repository for the normalized journal entity catalog.
 */
@Repository
public interface JournalEntityRepository
        extends BaseStreamRepository<JournalEntityEntity, Integer> {

    /**
     * Find the active entity row by type and normalized label.
     *
     * @param entityType entity type
     * @param normalizedLabel normalized lookup label
     * @return active entity row when found
     */
    Optional<JournalEntityEntity> findFirstByEntityTypeAndNormalizedLabel(JournalEntityType entityType, String normalizedLabel);
}
