package io.nicheblog.dreamdiary.infrastructure.log.service;

import io.nicheblog.dreamdiary.global.ActiveProfile;
import io.nicheblog.dreamdiary.infrastructure.log.entity.LogEntity;
import io.nicheblog.dreamdiary.infrastructure.log.entity.LogRepository;
import io.nicheblog.dreamdiary.infrastructure.log.entity.LogWriteMapstruct;
import io.nicheblog.dreamdiary.infrastructure.log.model.LogParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * 통합 {@code log} 테이블 적재.
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class LogWriteService {

    private final LogRepository repository;
    private final LogWriteMapstruct mapstruct;
    private final ActiveProfile activeProfile;

    public void regLog(final LogParam logParam) throws Exception {
        final LogEntity row = mapstruct.toEntity(logParam);
        repository.save(row);
        log.info(
                "ACTIVITY_LOG_SAVED user={} category={} type={} method={} uri={} status={} result={} message={}",
                logParam.getUsername(),
                logParam.getActvtyCtgr(),
                logParam.getLogType(),
                logParam.getHttpMethod(),
                logParam.getRequestUri(),
                logParam.getHttpStatus(),
                logParam.getRslt(),
                logParam.getRsltMsg()
        );
    }

    public void regAnonymousLog(final LogParam logParam) throws Exception {
        final LogEntity row = mapstruct.toEntity(logParam);
        row.setUsername(logParam.getUsername());
        row.setResult(logParam.getRslt());
        repository.save(row);

        log.info(
                "ACTIVITY_LOG_SAVED user={} category={} type={} method={} uri={} status={} result={} message={}",
                logParam.getUsername(),
                logParam.getActvtyCtgr(),
                logParam.getLogType(),
                logParam.getHttpMethod(),
                logParam.getRequestUri(),
                logParam.getHttpStatus(),
                logParam.getRslt(),
                logParam.getRsltMsg()
        );
    }

    /**
     * 시스템/배치 로그. 운영 프로파일에서만 DB 적재 (기존 SystemLogWriteService 동작 유지).
     */
    public void regSystemLog(final LogParam logParam) throws Exception {
        if (!activeProfile.isProd()) {
            return;
        }

        final LogEntity row = mapstruct.sysToEntity(logParam);
        repository.save(row);
        log.info(
                "SYSTEM_LOG_SAVED category={} result={} message={}",
                logParam.getActvtyCtgr(),
                logParam.getRslt(),
                logParam.getRsltMsg()
        );
    }
}
