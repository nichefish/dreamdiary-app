package io.nicheblog.dreamdiary.extension.log.sys.service;

import io.nicheblog.dreamdiary.extension.log.actvty.spec.LogSysSpec;
import io.nicheblog.dreamdiary.extension.log.sys.entity.LogSysEntity;
import io.nicheblog.dreamdiary.extension.log.sys.mapstruct.LogSysMapstruct;
import io.nicheblog.dreamdiary.extension.log.sys.model.LogSysDto;
import io.nicheblog.dreamdiary.extension.log.sys.model.LogSysParam;
import io.nicheblog.dreamdiary.extension.log.sys.repository.jpa.LogSysRepository;
import io.nicheblog.dreamdiary.global.ActiveProfile;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseReadonlyService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * LogSysService
 * <pre>
 *  시스템 로그 관리 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("logSysService")
@RequiredArgsConstructor
@Log4j2
public class LogSysService
        implements BaseReadonlyService<LogSysDto, Integer, LogSysEntity> {

    @Getter
    private final LogSysRepository repository;
    @Getter
    private final LogSysSpec spec;
    @Getter
    private final LogSysMapstruct mapstruct = LogSysMapstruct.INSTANCE;

    public LogSysMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public LogSysMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final ActiveProfile activeProfile;

    /**
     * 단일 항목 조회 (dto level)
     *
     * @param key 조회할 엔티티의 키
     * @return {@link LogSysDto} -- 조회 항목 반환
     */
    @Transactional(readOnly = true)
    public LogSysDto getDtlDto(final Integer key) throws Exception {
        final LogSysEntity retrievedEntity = this.getDtlEntity(key);

        return mapstruct.toDto(retrievedEntity);
    }

    /**
     * 시스템 로그 등록
     *
     * @param logParam 시스템 로그 파라미터
     * @return {@link Boolean} -- 로그 등록 성공 여부
     */
    public Boolean regSysActvty(final LogSysParam logParam) throws Exception {
        // 로컬 또는 개발 접속은 시스템 로그 남기지 않음
        if (!activeProfile.isProd()) return true;

        final LogSysEntity logActvty = mapstruct.paramToEntity(logParam);

        log.info("isSuccess: {}, rsltMsg: {}", logParam.getRslt(), logParam.getRsltMsg());
        final LogSysEntity rslt = repository.save(logActvty);

        return rslt.getLogSysNo() != null;
    }
}
