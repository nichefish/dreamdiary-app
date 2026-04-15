package io.nicheblog.dreamdiary.feature.flsys.repository.jpa;

import io.nicheblog.dreamdiary.feature.flsys.entity.FlsysMetaEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

/**
 * FlsysMetaRepository
 * <pre>
 *  파일시스템 메타정보 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface FlsysMetaRepository
        extends BaseStreamRepository<FlsysMetaEntity, Integer> {
    //
}

