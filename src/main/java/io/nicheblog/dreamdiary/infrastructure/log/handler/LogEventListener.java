package io.nicheblog.dreamdiary.infrastructure.log.handler;

import io.nicheblog.dreamdiary.global.config.AsyncConfig;
import io.nicheblog.dreamdiary.infrastructure.log.event.LogAnonymousEvent;
import io.nicheblog.dreamdiary.infrastructure.log.event.LogEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 통합 로그 이벤트 → 워커 큐.
 *
 * @see AsyncConfig
 */
@Component
@RequiredArgsConstructor
public class LogEventListener {

    private final LogWorker logWorker;

    @EventListener
    @Async
    public void handleLogEvent(final LogEvent event) {
        logWorker.offer(event);
    }

    @EventListener
    @Async
    public void handleLogAnonymousEvent(final LogAnonymousEvent event) {
        logWorker.offer(event);
    }
}
