package io.nicheblog.dreamdiary.infrastructure.log.actvty.repository.jpa;

import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.entity.LogActvtyEntity;
import org.springframework.stereotype.Repository;

/**
 * LogActvtyRepository
 * <pre>
 *  활동 로그 관리 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface LogActvtyRepository
        extends BaseStreamRepository<LogActvtyEntity, Integer> {
    //
}

