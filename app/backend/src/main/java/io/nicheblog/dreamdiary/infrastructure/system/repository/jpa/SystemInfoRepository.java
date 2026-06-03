package io.nicheblog.dreamdiary.infrastructure.system.repository.jpa;

import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import io.nicheblog.dreamdiary.infrastructure.system.entity.SystemInfoEntity;
import org.springframework.stereotype.Repository;

/**
 * SystemInfoRepository
 *
 * @author nichefish
 */
@Repository
public interface SystemInfoRepository
        extends BaseStreamRepository<SystemInfoEntity, Integer> {
    //
}
