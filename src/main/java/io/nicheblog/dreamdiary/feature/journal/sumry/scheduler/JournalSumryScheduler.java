package io.nicheblog.dreamdiary.feature.journal.sumry.scheduler;

import io.nicheblog.dreamdiary.feature.journal.sumry.service.JournalSumryService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.log.sys.event.LogSysEvent;
import io.nicheblog.dreamdiary.infrastructure.log.sys.handler.LogSysEventListener;
import io.nicheblog.dreamdiary.infrastructure.log.sys.model.LogSysParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * JournalSumryScheduler
 * <pre>
 *  저널 집계 Scheduler
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class JournalSumryScheduler {

    private final JournalSumryService journalSumryService;
    private final ApplicationEventPublisherWrapper publisher;

    /**
     * 하루에 한 번 전체 집계 갱신
     * 매일 00시 15분 실행
     *
     * @see LogSysEventListener
     */
    @Scheduled(cron = "0 15 0 * * *", zone = Constant.LOC_SEOUL)         // second min hour day month weekday
    public void journalSumrySchedule() {

        log.info("journalSumrySchedule...");

        final LogSysParam logParam = new LogSysParam();

        String rsltMsg = "";
        try {
            // TODO: 사용자 전체 결산 생성
            // 결산 생성
            journalSumryService.makeTotalYySumryByUser(Constant.SYSTEM_ACNT);
            // 캐시 재생성 위해 조회
            journalSumryService.getTotalSumryByUser(Constant.SYSTEM_ACNT);
        } catch (final Exception e) {
            rsltMsg = MessageUtils.getExceptionMsg(e);
            logParam.setExceptionInfo(e);
        } finally {
            // 로그 관련 처리
            logParam.setResult(false, rsltMsg, ActvtyCtgr.JOURNAL);
            publisher.publishAsyncEvent(new LogSysEvent(this, logParam));
        }
    }

}

