package io.nicheblog.dreamdiary.infrastructure.cache.event;

import io.nicheblog.dreamdiary.infrastructure.cache.handler.LoginSuccessCacheWarmupEventListener;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * LoginSuccessCacheWarmupEvent
 * <pre>
 *  EhCache 캐시 evict 이벤트 :: 메인 로직과 분리
 * </pre>
 *
 * @author nichefish
 * @see LoginSuccessCacheWarmupEventListener
 */
@Getter
public class LoginSuccessCacheWarmupEvent
        extends ApplicationEvent {

    /** 보안 컨텍스트 */
    private final SecurityContext securityContext;
    /** 컨텐츠 타입 */
    private final String username;

    /* ----- */

    /**
     * 생성자.
     *
     * @param source 이벤트의 출처를 나타내는 객체
     */
    public LoginSuccessCacheWarmupEvent(final Object source, final String username) {
        super(source);
        this.securityContext = SecurityContextHolder.getContext();
        this.username = username;
    }

}
