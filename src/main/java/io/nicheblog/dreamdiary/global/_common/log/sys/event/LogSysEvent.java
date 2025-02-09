package io.nicheblog.dreamdiary.global._common.log.sys.event;

import io.nicheblog.dreamdiary.global._common.log.sys.handler.LogSysEventListener;
import io.nicheblog.dreamdiary.global._common.log.sys.model.LogSysParam;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * LogSysEvent
 * <pre>
 *  시스템 로그 적재 이벤트.
 * </pre>
 *
 * @author nichefish
 * @see LogSysEventListener
 */
@Getter
public class LogSysEvent
        extends ApplicationEvent {

    /** 보안 컨텍스트 */
    private final SecurityContext securityContext;
    /** 시스템 로그 */
    private final LogSysParam log;

    /* ----- */

    /**
     * 생성자.
     * 
     * @param source 이벤트의 출처를 나타내는 객체
     * @param log 적제할 시스템 로그 정보를 담은 객체
     */
    public LogSysEvent(final Object source, final LogSysParam log) {
        super(source);
        this.securityContext = SecurityContextHolder.getContext();
        this.log = log;
    }
}