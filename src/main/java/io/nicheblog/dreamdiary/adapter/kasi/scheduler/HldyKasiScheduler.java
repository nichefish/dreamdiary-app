package io.nicheblog.dreamdiary.adapter.kasi.scheduler;

import io.nicheblog.dreamdiary.adapter.kasi.service.HldyKasiApiService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global._common.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.global._common.log.sys.event.LogSysEvent;
import io.nicheblog.dreamdiary.global._common.log.sys.handler.LogSysEventListener;
import io.nicheblog.dreamdiary.global._common.log.sys.model.LogSysParam;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * HldyKasiScheduler
 * <pre>
 *  API:: 한국천문연구원(KASI):: 특일 정보 등록 Scheduler
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class HldyKasiScheduler {

    private final HldyKasiApiService hldyKasiApiService;
    private final ApplicationEventPublisher publisher;

    /**
     * 1년에 한 번씩 휴일/특일 정보 API 조회하여 등록
     * 매년 1월 1일 00시 30분 실행
     *
     * @see LogSysEventListener
     */
    @Scheduled(cron = "0 30 0 1 1 *", zone = Constant.LOC_SEOUL)         // second min hour day month weekday
    @Transactional
    public void hldyKasiScheduler() {

        log.info("hldyKasiSchedule...");

        LogSysParam logParam = new LogSysParam();
        boolean isSuccess = false;
        String rsltMsg = "";
        try {
            // 기존 정보 (API로 받아온 휴일) 삭제 후 재등록
            isSuccess = hldyKasiApiService.procHldyList(DateUtils.getCurrYyStr());
            rsltMsg = MessageUtils.getMessage(isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE);
        } catch (final Exception e) {
            isSuccess = false;
            rsltMsg = MessageUtils.getExceptionMsg(e);
            logParam.setExceptionInfo(e);
        } finally {
            logParam.setResult(isSuccess, rsltMsg, ActvtyCtgr.API_KASI);
            publisher.publishEvent(new LogSysEvent(this, logParam));
        }
    }
}
