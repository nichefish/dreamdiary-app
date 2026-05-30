package io.nicheblog.dreamdiary.infrastructure.log.handler;

import io.nicheblog.dreamdiary.infrastructure.log.event.LogAnonymousEvent;
import io.nicheblog.dreamdiary.infrastructure.log.event.LogEvent;
import io.nicheblog.dreamdiary.infrastructure.log.model.LogParam;
import io.nicheblog.dreamdiary.infrastructure.log.service.LogWriteService;
import io.nicheblog.dreamdiary.infrastructure.log.type.LogType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 통합 로그 처리 워커 (단일 큐).
 *
 * @see LogEventListener
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class LogWorker
        implements Runnable {

    private final LogWriteService logWriteService;

    private static final BlockingQueue<Object> queue = new LinkedBlockingQueue<>();

    @PostConstruct
    public void init() {
        final Thread workerThread = new Thread(this);
        workerThread.start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                final Object logEvent = queue.take();

                if (logEvent instanceof LogEvent event) {
                    SecurityContextHolder.setContext(event.getSecurityContext());
                    final LogParam p = event.getLog();
                    if (p.getLogType() == LogType.SYSTEM) {
                        logWriteService.regSystemLog(p);
                    } else {
                        logWriteService.regLog(p);
                    }
                } else if (logEvent instanceof LogAnonymousEvent event) {
                    SecurityContextHolder.setContext(event.getSecurityContext());
                    logWriteService.regAnonymousLog(event.getLog());
                }
            } catch (final InterruptedException e) {
                log.warn("log regist failed", e);
                Thread.currentThread().interrupt();
            } catch (final Exception e) {
                log.warn("log regist failed", e);
            }
        }
    }

    public void offer(final Object event) {
        boolean isOffered = queue.offer(event);
        if (!isOffered) log.warn("queue offer failed... {}", event.toString());
    }
}
