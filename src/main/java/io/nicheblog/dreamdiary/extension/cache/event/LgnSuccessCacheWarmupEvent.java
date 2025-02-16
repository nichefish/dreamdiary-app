package io.nicheblog.dreamdiary.extension.cache.event;

import io.nicheblog.dreamdiary.extension.cache.handler.LgnSuccessCacheWarmupEventListener;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * LgnSuccessCacheWarmupEvent
 * <pre>
 *  EhCache 캐시 evict 이벤트 :: 메인 로직과 분리
 * </pre>
 *
 * @author nichefish
 * @see LgnSuccessCacheWarmupEventListener
 */
@Getter
public class LgnSuccessCacheWarmupEvent
        extends ApplicationEvent {

    /** 보안 컨텍스트 */
    private final SecurityContext securityContext;
    /** 컨텐츠 타입 */
    private final String userId;

    /* ----- */

    /**
     * 생성자.
     *
     * @param source 이벤트의 출처를 나타내는 객체
     */
    public LgnSuccessCacheWarmupEvent(final Object source, final String userId) {
        super(source);
        this.securityContext = SecurityContextHolder.getContext();
        this.userId = userId;
    }
}
