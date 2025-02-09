package io.nicheblog.dreamdiary.global._common.log.sys.handler;

import io.nicheblog.dreamdiary.global._common.log.sys.event.LogSysEvent;
import io.nicheblog.dreamdiary.global._common.log.sys.service.LogSysService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * LogSysWorker
 * <pre>
 *  시스템 로그 처리 Worker :: Runnable 구현 (Queue 처리)
 *  Queue에서 LogSysEvent를 가져와 시스템 로그를 등록합니다.
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class LogSysWorker
        implements Runnable {

    private final LogSysService logSysService;

    /** 로그 queue */
    private static final BlockingQueue<LogSysEvent> logSysQueue = new LinkedBlockingQueue<>();

    @PostConstruct
    public void init() {
        final Thread workerThread = new Thread(this);
        workerThread.start();
    }

    /**
     * 시스템 로그 Queue에서 LogSysEvent를 가져와 로그를 등록합니다.
     */
    @Override
    public void run() {
        try {
            while (true) {
                // Blocks until an element is available
                final LogSysEvent logEvent = logSysQueue.take();
                // 이벤트로부터 securityContext를 가져온다.
                SecurityContextHolder.setContext(logEvent.getSecurityContext());

                // 시스템 로그 로깅 처리
                logSysService.regSysActvty(logEvent.getLog());
            }
        } catch (final InterruptedException e) {
            log.warn("log regist failed", e);
            Thread.currentThread().interrupt();
        } catch (final Exception e) {
            log.warn("log regist failed", e);
        }
    }

    /**
     * 시스템 로그 이벤트를 큐에 추가합니다.
     *
     * @param event 큐에 추가할 LogSysEvent 객체
     */
    public void offer(final LogSysEvent event) {
        boolean isOffered = logSysQueue.offer(event);
        if (!isOffered) log.warn("queue offer failed... {}", event.toString());
    }
}
