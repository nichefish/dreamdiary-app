package io.nicheblog.dreamdiary.domain.jrnl.sumry.scheduler;

import io.nicheblog.dreamdiary.domain.jrnl.sumry.service.JrnlSumryService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global._common._clsf.tag.handler.TagEventListener;
import io.nicheblog.dreamdiary.global._common.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.global._common.log.sys.event.LogSysEvent;
import io.nicheblog.dreamdiary.global._common.log.sys.handler.LogSysEventListener;
import io.nicheblog.dreamdiary.global._common.log.sys.model.LogSysParam;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * JrnlSumryScheduler
 * <pre>
 *  저널 집계 Scheduler
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class JrnlSumryScheduler {

    private final JrnlSumryService jrnlSumryService;
    private final ApplicationEventPublisher publisher;

    /**
     * 하루에 한 번 전체 집계 갱신
     * 매일 00시 15분 실행
     *
     * @see LogSysEventListener
     */
    @Scheduled(cron = "0 15 0 * * *", zone = Constant.LOC_SEOUL)         // second min hour day month weekday
    public void jrnlSumrySchedule() {

        log.info("jrnlSumrySchedule...");

        LogSysParam logParam = new LogSysParam();
        String rsltMsg = "";
        try {
            // 결산 생성
            jrnlSumryService.makeTotalYySumry();
            // 캐시 재생성 위해 조회
            jrnlSumryService.getTotalSumry();
        } catch (final Exception e) {
            rsltMsg = MessageUtils.getExceptionMsg(e);
            logParam.setExceptionInfo(e);
        } finally {
            // 로그 관련 처리
            logParam.setResult(false, rsltMsg, ActvtyCtgr.JRNL);
            publisher.publishEvent(new LogSysEvent(this, logParam));
        }
    }

}
