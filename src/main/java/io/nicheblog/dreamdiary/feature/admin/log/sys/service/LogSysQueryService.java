package io.nicheblog.dreamdiary.feature.admin.log.sys.service;

import io.nicheblog.dreamdiary.feature.admin.log.sys.mapstruct.LogSysReadMapstruct;
import io.nicheblog.dreamdiary.feature.admin.log.sys.model.LogSysQueryDto;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoReadableService;
import io.nicheblog.dreamdiary.infrastructure.log.sys.entity.LogSysEntity;
import io.nicheblog.dreamdiary.infrastructure.log.sys.repository.jpa.LogSysRepository;
import io.nicheblog.dreamdiary.infrastructure.log.sys.spec.LogSysSpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * LogSysQueryService
 * <pre>
 *  시스템 로그 조회 전용 서비스.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class LogSysQueryService
        implements BaseDtoReadableService<LogSysQueryDto, Integer, LogSysEntity> {

    @Getter
    private final LogSysRepository repository;
    @Getter
    private final LogSysSpec spec;
    @Getter
    private final LogSysReadMapstruct mapstruct = LogSysReadMapstruct.INSTANCE;

    @Override
    public LogSysReadMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
}
