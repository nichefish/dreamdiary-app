package io.nicheblog.dreamdiary.global._common.log.actvty.handler;

import io.nicheblog.dreamdiary.global._common.log.actvty.event.LogActvtyEvent;
import io.nicheblog.dreamdiary.global._common.log.actvty.event.LogAnonActvtyEvent;
import io.nicheblog.dreamdiary.global._common.log.actvty.service.LogActvtyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * LogWorker
 * <pre>
 *  활동 로그 처리 Worker :: Runnable 구현 (Queue 처리)
 *  메일 큐에서 LogActvtyEvent를 가져와 활동 로그를 등록합니다.
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class LogActvtyWorker
        implements Runnable {

    private final LogActvtyService logActvtyService;

    /** 로그 queue */
    private static final BlockingQueue<Object> logQueue = new LinkedBlockingQueue<>();

    @PostConstruct
        public void init() {
        Thread workerThread = new Thread(this);
        workerThread.start();
    }

    /**
     * 로그 큐에서 LogActvtyEvent / LogAnonActvtyEvent를 가져와 등록합니다.
     */
    @Override
    public void run() {
        try {
            while (true) {
                // Blocks until an element is available
                Object logEvent = logQueue.take();

                if (logEvent instanceof LogActvtyEvent) {
                    // 활동 로그 (로그인) 로깅 처리
                    logActvtyService.regLogActvty(((LogActvtyEvent) logEvent).getLog());
                } else if (logEvent instanceof LogAnonActvtyEvent) {
                    // 활동 로그 (비로그인) 로깅 처리
                    logActvtyService.regLogAnonActvty(((LogAnonActvtyEvent) logEvent).getLog());
                }
            }
        } catch (final InterruptedException e) {
            log.warn("log regist failed", e);
            Thread.currentThread().interrupt();
        } catch (final Exception e) {
            log.warn("log regist failed", e);
        }
    }

    /**
     * 활동 로그 이벤트를 큐에 추가합니다.
     *
     * @param event 큐에 추가할 LogActvtyEvent / LogAnonActvtyEvent 객체
     */
    public void offer(Object event) {
        logQueue.offer(event);
    }
}
