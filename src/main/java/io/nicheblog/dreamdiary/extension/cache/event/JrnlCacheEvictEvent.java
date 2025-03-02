package io.nicheblog.dreamdiary.extension.cache.event;

import io.nicheblog.dreamdiary.extension.cache.handler.JrnlCacheEvictEventListner;
import io.nicheblog.dreamdiary.extension.cache.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * JrnlCacheEvictEvent
 * <pre>
 *  EhCache 캐시 evict 이벤트 :: 메인 로직과 분리
 * </pre>
 *
 * @author nichefish
 * @see JrnlCacheEvictEventListner
 */
@Getter
public class JrnlCacheEvictEvent
        extends ApplicationEvent {

    /** 보안 컨텍스트 */
    private final SecurityContext securityContext;
    /** 저장 객체 */
    private final JrnlCacheEvictParam cacheEvictParam;
    /** 컨텐츠 타입 */
    private final ContentType contentType;

    /* ----- */

    /**
     * 생성자.
     *
     * @param source 이벤트의 출처를 나타내는 객체
     */
    public JrnlCacheEvictEvent(final Object source, final JrnlCacheEvictParam cacheEvictParam, final ContentType contentType) {
        super(source);
        this.securityContext = SecurityContextHolder.getContext();
        this.cacheEvictParam = cacheEvictParam;
        this.contentType = contentType;
    }
}
