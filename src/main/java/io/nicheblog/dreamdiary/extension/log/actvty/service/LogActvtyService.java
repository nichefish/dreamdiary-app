package io.nicheblog.dreamdiary.extension.log.actvty.service;

import io.nicheblog.dreamdiary.extension.log.actvty.entity.LogActvtyEntity;
import io.nicheblog.dreamdiary.extension.log.actvty.mapstruct.LogActvtyMapstruct;
import io.nicheblog.dreamdiary.extension.log.actvty.model.LogActvtyDto;
import io.nicheblog.dreamdiary.extension.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.extension.log.actvty.repository.jpa.LogActvtyRepository;
import io.nicheblog.dreamdiary.extension.log.actvty.spec.LogActvtySpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseReadonlyService;

/**
 * LogActvtyService
 * <pre>
 *  활동 로그 관리 서비스 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface LogActvtyService
        extends BaseReadonlyService<LogActvtyDto.DTL, LogActvtyDto.LIST, Integer, LogActvtyEntity, LogActvtyRepository, LogActvtySpec, LogActvtyMapstruct> {

    /**
     * 로그인 상태에서 활동 로그 등록
     *
     * @param logParam 활동 로그 파라미터
     * @return {@link Boolean} -- 로그 등록 성공 여부
     * @throws Exception 로그 등록 중 발생할 수 있는 예외
     */
    Boolean regLogActvty(final LogActvtyParam logParam) throws Exception;

    /**
     * 비로그인 상태에서 활동 로그 등록
     *
     * @param logParam 활동 로그 파라미터
     * @return {@link Boolean} -- 로그 등록 성공 여부
     * @throws Exception 로그 등록 중 발생할 수 있는 예외
     */
    Boolean regLogAnonActvty(final LogActvtyParam logParam) throws Exception;
}
