package io.nicheblog.dreamdiary.feature.journal.sbjct.repository.jpa;

import io.nicheblog.dreamdiary.feature.journal.sbjct.entity.JournalSbjctEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

/**
 * JournalSbjctRepository
 * <pre>
 *  저널 주제 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface JournalSbjctRepository
        extends BaseStreamRepository<JournalSbjctEntity, Integer> {
    //
}


