package io.nicheblog.dreamdiary.extension.log.actvty.service.impl;

import io.nicheblog.dreamdiary.extension.log.actvty.entity.LogActvtyEntity;
import io.nicheblog.dreamdiary.extension.log.actvty.mapstruct.LogActvtyMapstruct;
import io.nicheblog.dreamdiary.extension.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.extension.log.actvty.repository.jpa.LogActvtyRepository;
import io.nicheblog.dreamdiary.extension.log.actvty.service.LogActvtyService;
import io.nicheblog.dreamdiary.extension.log.actvty.spec.LogActvtySpec;
import io.nicheblog.dreamdiary.global.ActiveProfile;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * LogActvtyService
 * <pre>
 *  활동 로그 관리 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("logActvtyService")
@RequiredArgsConstructor
@Log4j2
public class LogActvtyServiceImpl
        implements LogActvtyService {

    @Getter
    private final LogActvtyRepository repository;
    @Getter
    private final LogActvtySpec spec;
    @Getter
    private final LogActvtyMapstruct mapstruct = LogActvtyMapstruct.INSTANCE;

    private final ActiveProfile activeProfile;

    /**
     * 로그인 상태에서 활동 로그 등록
     *
     * @param logParam 활동 로그 파라미터
     * @return {@link Boolean} -- 로그 등록 성공 여부
     * @throws Exception 로그 등록 중 발생할 수 있는 예외
     */
    @Override
    public Boolean regLogActvty(final LogActvtyParam logParam) throws Exception {
        // 로컬 또는 개발 접속은 활동 로그 남기지 않음
        if (!activeProfile.isProd()) return true;

        final LogActvtyEntity logActvty = mapstruct.paramToEntity(logParam);
        log.info("isSuccess: {}, rsltMsg: {}", logParam.getRslt(), logParam.getRsltMsg());
        final LogActvtyEntity rslt = repository.save(logActvty);

        return rslt.getLogActvtyNo() != null;
    }

    /**
     * 비로그인 상태에서 활동 로그 등록
     *
     * @param logParam 활동 로그 파라미터
     * @return {@link Boolean} -- 로그 등록 성공 여부
     * @throws Exception 로그 등록 중 발생할 수 있는 예외
     */
    @Override
    public Boolean regLogAnonActvty(final LogActvtyParam logParam) throws Exception {
        // 로컬 또는 개발 접속은 활동 로그 남기지 않음
        if (!activeProfile.isProd()) return true;

        final LogActvtyEntity logActvty = mapstruct.paramToEntity(logParam);
        logActvty.setUserId(logParam.getUserId());
        logActvty.setRslt(logParam.getRslt());               // 작업 결과
        logActvty.setRsltMsg(logParam.getRsltMsg());            // 작업 결과 메세지
        final LogActvtyEntity rslt = repository.save(logActvty);

        log.info("isSuccess: {}, rsltMsg: {}", logParam.getRslt(), logParam.getRsltMsg());
        return rslt.getLogActvtyNo() != null;
    }
}
