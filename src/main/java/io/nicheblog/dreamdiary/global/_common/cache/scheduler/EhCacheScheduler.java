package io.nicheblog.dreamdiary.global._common.cache.scheduler;

import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global._common.cache.util.EhCacheUtils;
import io.nicheblog.dreamdiary.global._common.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.global._common.log.actvty.handler.LogActvtyEventListener;
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
 * EhCacheClearScheduler
 * <pre>
 *  캐시 클리어 Scheduler
 * </pre>
 *
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class EhCacheScheduler {

    private final ApplicationEventPublisher publisher;

    /**
     * 1시간에 한 번씩 전체 캐시 클리어
     * 매시간 00분 실행
     *
     * @see LogSysEventListener
     */
    @Scheduled(cron = "0 0 * * * *", zone = Constant.LOC_SEOUL)         // second min hour day month weekday
    public void cacheAllClearSchedule() {

        log.info("cacheAllClearSchedule...");

        LogSysParam logParam = new LogSysParam();
        try {
            EhCacheUtils.clearAllCaches();
        } catch (final Exception e) {
            final String rsltMsg = MessageUtils.getExceptionMsg(e);
            // 수시로 이루어지므로 실패시에만 로깅한다.
            logParam.setExceptionInfo(e);
            logParam.setResult(false, rsltMsg, ActvtyCtgr.CACHE);
            publisher.publishEvent(new LogSysEvent(this, logParam));
        }
    }
}
