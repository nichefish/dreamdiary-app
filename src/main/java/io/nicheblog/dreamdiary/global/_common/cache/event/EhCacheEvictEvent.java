package io.nicheblog.dreamdiary.global._common.cache.event;

import io.nicheblog.dreamdiary.global._common.cache.handler.EhCacheEvictEventListner;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * EhCacheEvictEvent
 * <pre>
 *  EhCache 캐시 evict 이벤트 :: 메인 로직과 분리
 * </pre>
 *
 * @author nichefish
 * @see EhCacheEvictEventListner
 */
@Getter
public class EhCacheEvictEvent
        extends ApplicationEvent {

    /** 컨텐츠 타입 */
    private final Integer postNo;

    /** 컨텐츠 타입 */
    private final String contentType;

    /* ----- */

    /**
     * 생성자.
     *
     * @param source 이벤트의 출처를 나타내는 객체
     * @param postNo 캐시를 제거할 게시글 번호
     * @param contentType 캐시를 제거할 컨텐츠 타입
     */
    public EhCacheEvictEvent(final Object source, final Integer postNo, final String contentType) {
        super(source);
        this.postNo = postNo;
        this.contentType = contentType;
    }
}
