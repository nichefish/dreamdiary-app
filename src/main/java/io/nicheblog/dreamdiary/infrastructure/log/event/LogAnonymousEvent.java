package io.nicheblog.dreamdiary.infrastructure.log.event;

import io.nicheblog.dreamdiary.infrastructure.log.handler.LogEventListener;
import io.nicheblog.dreamdiary.infrastructure.log.model.LogParam;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 비로그인 상태 활동 로그 적재 이벤트.
 *
 * @see LogEventListener
 */
@Getter
public class LogAnonymousEvent
        extends ApplicationEvent {

    private final SecurityContext securityContext;
    private final LogParam log;

    public LogAnonymousEvent(final Object source, final LogParam log) {
        super(source);
        this.securityContext = SecurityContextHolder.getContext();
        this.log = log;
    }
}
