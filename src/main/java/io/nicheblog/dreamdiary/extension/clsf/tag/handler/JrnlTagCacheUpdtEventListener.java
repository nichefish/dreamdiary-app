package io.nicheblog.dreamdiary.extension.clsf.tag.handler;

import io.nicheblog.dreamdiary.extension.cache.handler.EhCacheEvictEventListner;
import io.nicheblog.dreamdiary.extension.clsf.tag.event.JrnlTagCacheUpdtEvent;
import io.nicheblog.dreamdiary.global.config.AsyncConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * JrnlTagCntCacheUpdtEventListener
 * <pre>
 *  태그 캐시 갱신 이벤트 처리 핸들러.
 *  태그 개수(Map) 및 태그 목록(List)을 업데이트.
 * </pre>
 *
 * @author nichefish
 * @see AsyncConfig
 * @see EhCacheEvictEventListner
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class JrnlTagCacheUpdtEventListener {

    private final JrnlTagCacheUpdtWorker jrnlTagCacheUpdtWorker;

    /**
     * 태그 캐시 갱신 이벤트를 처리한다.
     *
     * @param event 처리할 이벤트 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     * @see EhCacheEvictEventListner
     */
    @EventListener
    public void handleJrnlTagCacheUpdtEvent(final JrnlTagCacheUpdtEvent event) throws Exception {
        log.debug("JrnlTagCacheUpdtEventListener.handleJrnlTagCacheUpdtEvent() - event : {}", event.toString());

        jrnlTagCacheUpdtWorker.handle(event);
    }
}