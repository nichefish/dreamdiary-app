package io.nicheblog.dreamdiary.domain.flsys.repository.jpa;

import io.nicheblog.dreamdiary.domain.flsys.entity.FlsysMetaEntity;
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
@Repository("flsysMetaRepository")
public interface FlsysMetaRepository
        extends BaseStreamRepository<FlsysMetaEntity, Integer> {
    //
}