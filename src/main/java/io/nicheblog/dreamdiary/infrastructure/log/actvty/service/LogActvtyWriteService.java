package io.nicheblog.dreamdiary.infrastructure.log.actvty.service;

import io.nicheblog.dreamdiary.infrastructure.log.actvty.entity.LogActvtyEntity;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.mapstruct.LogActvtyWriteMapstruct;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.repository.jpa.LogActvtyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * LogActvtyWriteService
 * <pre>
 *  활동 로그 적재 전용 서비스.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class LogActvtyWriteService {

    private final LogActvtyRepository repository;
    private final LogActvtyWriteMapstruct mapstruct = LogActvtyWriteMapstruct.INSTANCE;

    /**
     * 로그인 상태에서 활동 로그 등록
     *
     * @param logParam 활동 로그 파라미터
     */
    public void regLogActvty(final LogActvtyParam logParam) throws Exception {
        final LogActvtyEntity logActvty = mapstruct.toEntity(logParam);
        repository.save(logActvty);
        log.info(
                "ACTIVITY_LOG_SAVED user={} category={} type={} method={} uri={} status={} result={} message={}",
                logParam.getUserId(),
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
     * 비로그인 상태에서 활동 로그 등록
     *
     * @param logParam 활동 로그 파라미터
     */
    public void regLogAnonActvty(final LogActvtyParam logParam) throws Exception {
        final LogActvtyEntity logActvty = mapstruct.toEntity(logParam);
        logActvty.setUserId(logParam.getUserId());
        logActvty.setRslt(logParam.getRslt());
        logActvty.setRsltMsg(logParam.getRsltMsg());
        repository.save(logActvty);

        log.info(
                "ACTIVITY_LOG_SAVED user={} category={} type={} method={} uri={} status={} result={} message={}",
                logParam.getUserId(),
                logParam.getActvtyCtgr(),
                logParam.getLogType(),
                logParam.getHttpMethod(),
                logParam.getRequestUri(),
                logParam.getHttpStatus(),
                logParam.getRslt(),
                logParam.getRsltMsg()
        );
    }
}
