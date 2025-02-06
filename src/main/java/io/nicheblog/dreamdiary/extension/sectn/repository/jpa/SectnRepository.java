package io.nicheblog.dreamdiary.extension.sectn.repository.jpa;

import io.nicheblog.dreamdiary.extension.sectn.entity.SectnEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

/**
 * SectnRepository
 * <pre>
 *  단락 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository("sectnRepository")
public interface SectnRepository
        extends BaseStreamRepository<SectnEntity, Integer> {
    //
}