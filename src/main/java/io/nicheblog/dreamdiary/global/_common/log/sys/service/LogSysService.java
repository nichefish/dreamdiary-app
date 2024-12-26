package io.nicheblog.dreamdiary.global._common.log.sys.service;

import io.nicheblog.dreamdiary.global._common.log.actvty.spec.LogSysSpec;
import io.nicheblog.dreamdiary.global._common.log.sys.entity.LogSysEntity;
import io.nicheblog.dreamdiary.global._common.log.sys.mapstruct.LogSysMapstruct;
import io.nicheblog.dreamdiary.global._common.log.sys.model.LogSysDto;
import io.nicheblog.dreamdiary.global._common.log.sys.model.LogSysParam;
import io.nicheblog.dreamdiary.global._common.log.sys.repository.jpa.LogSysRepository;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseReadonlyService;

/**
 * LogSysService
 * <pre>
 *  시스템 로그 관리 서비스 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface LogSysService
        extends BaseReadonlyService<LogSysDto.DTL, LogSysDto.LIST, Integer, LogSysEntity, LogSysRepository, LogSysSpec, LogSysMapstruct> {

    /**
     * 시스템 로그 등록
     *
     * @param logParam 시스템 로그 파라미터
     * @return {@link Boolean} -- 로그 등록 성공 여부
     * @throws Exception 로그 등록 중 발생할 수 있는 예외
     */
    Boolean regSysActvty(final LogSysParam logParam) throws Exception;
}
