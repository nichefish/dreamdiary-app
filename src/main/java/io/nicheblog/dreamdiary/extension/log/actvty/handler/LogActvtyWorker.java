package io.nicheblog.dreamdiary.extension.log.actvty.handler;

import io.nicheblog.dreamdiary.extension.log.actvty.event.LogActvtyEvent;
import io.nicheblog.dreamdiary.extension.log.actvty.event.LogAnonActvtyEvent;
import io.nicheblog.dreamdiary.extension.log.actvty.service.LogActvtyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * LogActvtyWorker
 * <pre>
 *  활동 로그 처리 Worker :: Runnable 구현 (Queue 처리)
 *  Queue에서 LogActvtyEvent를 가져와 활동 로그를 등록합니다.
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
    private static final BlockingQueue<Object> logActvtyQueue = new LinkedBlockingQueue<>();

    @PostConstruct
    public void init() {
        final Thread workerThread = new Thread(this);
        workerThread.start();
    }

    /**
     * 활동 로그 Queue에서 LogActvtyEvent / LogAnonActvtyEvent를 가져와 등록합니다.
     */
    @Override
    public void run() {
        try {
            while (true) {
                // Blocks until an element is available
                final Object logEvent = logActvtyQueue.take();

                if (logEvent instanceof LogActvtyEvent event) {
                    // 이벤트로부터 securityContext를 가져온다.
                    SecurityContextHolder.setContext(event.getSecurityContext());
                    // 활동 로그 (로그인) 로깅 처리
                    logActvtyService.regLogActvty(event.getLog());
                } else if (logEvent instanceof LogAnonActvtyEvent event) {
                    // 이벤트로부터 securityContext를 가져온다.
                    SecurityContextHolder.setContext(event.getSecurityContext());
                    // 활동 로그 (비로그인) 로깅 처리
                    logActvtyService.regLogAnonActvty(event.getLog());
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
    public void offer(final Object event) {
        boolean isOffered = logActvtyQueue.offer(event);
        if (!isOffered) log.warn("queue offer failed... {}", event.toString());
    }
}
