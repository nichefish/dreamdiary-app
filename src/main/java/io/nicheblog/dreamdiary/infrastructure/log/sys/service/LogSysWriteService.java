package io.nicheblog.dreamdiary.infrastructure.log.sys.service;

import io.nicheblog.dreamdiary.global.ActiveProfile;
import io.nicheblog.dreamdiary.infrastructure.log.sys.entity.LogSysEntity;
import io.nicheblog.dreamdiary.infrastructure.log.sys.mapstruct.LogSysWriteMapstruct;
import io.nicheblog.dreamdiary.infrastructure.log.sys.model.LogSysParam;
import io.nicheblog.dreamdiary.infrastructure.log.sys.repository.jpa.LogSysRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * LogSysWriteService
 * <pre>
 *  시스템 로그 적재 전용 서비스.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class LogSysWriteService {

    private final LogSysRepository repository;
    private final LogSysWriteMapstruct mapstruct = LogSysWriteMapstruct.INSTANCE;
    private final ActiveProfile activeProfile;

    /**
     * 시스템 로그 등록
     *
     * @param logParam 시스템 로그 파라미터
     * @return {@link Boolean} -- 로그 등록 성공 여부
     */
    public Boolean regSysActvty(final LogSysParam logParam) throws Exception {
        if (!activeProfile.isProd()) return true;

        final LogSysEntity logActvty = mapstruct.toEntity(logParam);

        log.info("isSuccess: {}, rsltMsg: {}", logParam.getRslt(), logParam.getRsltMsg());
        final LogSysEntity rslt = repository.save(logActvty);

        return rslt.getLogSysNo() != null;
    }
}
