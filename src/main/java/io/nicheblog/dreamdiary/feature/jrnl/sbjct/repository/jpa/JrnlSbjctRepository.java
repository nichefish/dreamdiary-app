package io.nicheblog.dreamdiary.feature.jrnl.sbjct.repository.jpa;

import io.nicheblog.dreamdiary.feature.jrnl.sbjct.entity.JrnlSbjctEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

/**
 * JrnlSbjctRepository
 * <pre>
 *  저널 주제 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository("jrnlSbjctRepository")
public interface JrnlSbjctRepository
        extends BaseStreamRepository<JrnlSbjctEntity, Integer> {
    //
}
