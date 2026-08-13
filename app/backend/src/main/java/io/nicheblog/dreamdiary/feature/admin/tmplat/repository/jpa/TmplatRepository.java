package io.nicheblog.dreamdiary.feature.admin.tmplat.repository.jpa;

import io.nicheblog.dreamdiary.feature.admin.tmplat.entity.TmplatEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

/**
 * TmplatRepository
 * <pre>
 *  템플릿 관리 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface TmplatRepository
        extends BaseStreamRepository<TmplatEntity, Integer> {
    //
}