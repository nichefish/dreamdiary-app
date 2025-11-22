package io.nicheblog.dreamdiary.extension.cache.handler;

import io.nicheblog.dreamdiary.extension.cache.event.CommentCacheEvictEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * CommentCacheEvictEventListner
 * <pre>
 *  댓글 관련 캐시 제거 이벤트 처리 핸들러.
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class CommentCacheEvictEventListner {

    private final CommentCacheEvictWorker commentCacheEvictWorker;

    /**
     * 댓글 관련 캐시 제거 이벤트를 처리한다.
     *
     * @param event 처리할 이벤트 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @EventListener
    public void handleCommentCacheEvictEvent(final CommentCacheEvictEvent event) throws Exception {
        log.debug("CommentCacheEvictEventListner.handleCommentCacheEvictEvent() - event : {}", event.toString());

        commentCacheEvictWorker.handle(event);
    }
}
