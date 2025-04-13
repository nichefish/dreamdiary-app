package io.nicheblog.dreamdiary.extension.cache.event;

import io.nicheblog.dreamdiary.extension.cache.handler.CacheWarmupEventListner;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * CacheWarmupEvent
 * <pre>
 *  캐시 웜업 이벤트 :: 메인 로직과 분리
 * </pre>
 *
 * @author nichefish
 * @see CacheWarmupEventListner
 */
@Getter
public class CacheWarmupEvent
        extends ApplicationEvent {

    /* ----- */

    /**
     * 생성자.
     *
     * @param source 이벤트의 출처를 나타내는 객체
     */
    public CacheWarmupEvent(final Object source) {
        super(source);
    }
}
