package io.nicheblog.dreamdiary.infrastructure.log.event;

import io.nicheblog.dreamdiary.infrastructure.log.handler.LogEventListener;
import io.nicheblog.dreamdiary.infrastructure.log.model.LogParam;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 통합 로그 적재 이벤트 (HTTP 활동·시스템 공통).
 *
 * @see LogEventListener
 */
@Getter
public class LogEvent
        extends ApplicationEvent {

    private final SecurityContext securityContext;
    private final LogParam log;

    public LogEvent(final Object source, final LogParam log) {
        super(source);
        this.securityContext = SecurityContextHolder.getContext();
        this.log = log;
    }
}
