package io.nicheblog.dreamdiary.global._common.log.actvty.event;

import io.nicheblog.dreamdiary.global._common.log.actvty.handler.LogActvtyEventListener;
import io.nicheblog.dreamdiary.global._common.log.actvty.model.LogActvtyParam;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * LogAnonActvtyEvent
 * <pre>
 *  활동 로그(비로그인) 적재 이벤트.
 * </pre>
 *
 * @author nichefish
 * @see LogActvtyEventListener
 */
@Getter
public class LogAnonActvtyEvent
        extends ApplicationEvent {

    /** 보안 컨텍스트 */
    private final SecurityContext securityContext;
    /** 활동 로그 */
    private final LogActvtyParam log;

    /* ----- */

    /**
     * 생성자.
     *
     * @param source 이벤트의 출처를 나타내는 객체
     * @param log 적제할 활동 로그 정보를 담은 객체
     */
    public LogAnonActvtyEvent(final Object source, final LogActvtyParam log) {
        super(source);
        this.securityContext = SecurityContextHolder.getContext();
        this.log = log;
    }
}
