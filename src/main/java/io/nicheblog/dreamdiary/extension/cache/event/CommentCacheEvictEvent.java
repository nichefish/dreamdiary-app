package io.nicheblog.dreamdiary.extension.cache.event;

import io.nicheblog.dreamdiary.extension.cache.handler.CommentCacheEvictEventListner;
import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * CommentCacheEvictEvent
 * <pre>
 *  댓글 관련 캐시 evict 이벤트 :: 메인 로직과 분리
 * </pre>
 *
 * @author nichefish
 * @see CommentCacheEvictEventListner
 */
@Getter
public class CommentCacheEvictEvent
        extends ApplicationEvent {

    /** 보안 컨텍스트 */
    private final SecurityContext securityContext;
    /** 저장 객체 */
    private final Integer refPostNo;
    /** 컨텐츠 타입 */
    private final ContentType refContentType;

    /* ----- */

    /**
     * 생성자.
     *
     * @param source 이벤트의 출처를 나타내는 객체
     */
    public CommentCacheEvictEvent(final Object source, final Integer refPostNo, final String refContentType) {
        super(source);
        this.securityContext = SecurityContextHolder.getContext();
        this.refPostNo = refPostNo;
        this.refContentType = ContentType.valueOf(refContentType);
    }
}
