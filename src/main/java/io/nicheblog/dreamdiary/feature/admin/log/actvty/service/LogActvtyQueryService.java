package io.nicheblog.dreamdiary.feature.admin.log.actvty.service;

import io.nicheblog.dreamdiary.feature.admin.log.actvty.mapstruct.LogActvtyReadMapstruct;
import io.nicheblog.dreamdiary.feature.admin.log.actvty.model.LogActvtyQueryDto;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoReadableService;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.entity.LogActvtyEntity;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.repository.jpa.LogActvtyRepository;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.spec.LogActvtySpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * LogActvtyQueryService
 * <pre>
 *  활동 로그 조회 전용 서비스.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class LogActvtyQueryService
        implements BaseDtoReadableService<LogActvtyQueryDto, Integer, LogActvtyEntity> {

    @Getter
    private final LogActvtyRepository repository;
    @Getter
    private final LogActvtySpec spec;
    @Getter
    private final LogActvtyReadMapstruct mapstruct = LogActvtyReadMapstruct.INSTANCE;

    @Override
    public LogActvtyReadMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
}
