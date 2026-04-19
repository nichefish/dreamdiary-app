package io.nicheblog.dreamdiary.feature.attachable.managt.repository.jpa;

import io.nicheblog.dreamdiary.feature.attachable.managt.entity.ManagtrEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

/**
 * ManagtrRepository
 * <pre>
 *  작업자 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface ManagtrRepository
        extends BaseStreamRepository<ManagtrEntity, Integer> {
    //
}

