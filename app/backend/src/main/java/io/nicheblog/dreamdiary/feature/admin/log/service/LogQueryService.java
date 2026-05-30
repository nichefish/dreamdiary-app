package io.nicheblog.dreamdiary.feature.admin.log.service;

import io.nicheblog.dreamdiary.feature.admin.log.mapstruct.LogReadMapstruct;
import io.nicheblog.dreamdiary.feature.admin.log.model.LogQueryDto;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoReadableService;
import io.nicheblog.dreamdiary.infrastructure.log.entity.LogEntity;
import io.nicheblog.dreamdiary.infrastructure.log.entity.LogRepository;
import io.nicheblog.dreamdiary.infrastructure.log.spec.LogSpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * LogQueryService
 * <pre>
 *  활동 로그 조회 전용 서비스.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class LogQueryService
        implements BaseDtoReadableService<LogQueryDto, Integer, LogEntity> {

    @Getter
    private final LogRepository repository;
    @Getter
    private final LogSpec spec;
    @Getter
    private final LogReadMapstruct mapstruct = LogReadMapstruct.INSTANCE;

    @Override
    public LogReadMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
}
