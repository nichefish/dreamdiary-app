package io.nicheblog.dreamdiary.extension.log.sys.repository.jpa;

import io.nicheblog.dreamdiary.extension.log.sys.entity.LogSysEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

/**
 * LogSysRepository
 * <pre>
 *  시스템 로그 관리 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository("logSysRepository")
public interface LogSysRepository
        extends BaseStreamRepository<LogSysEntity, Integer> {
    //
}
