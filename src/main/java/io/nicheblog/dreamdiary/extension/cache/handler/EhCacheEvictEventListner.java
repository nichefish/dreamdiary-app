package io.nicheblog.dreamdiary.extension.cache.handler;

import io.nicheblog.dreamdiary.extension.cache.event.EhCacheEvictEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * EhCacheEvictEventListner
 * <pre>
 *  EhCache 캐시 제거 이벤트 처리 핸들러.
 *  데이터 정합성을 위해 동기 처리
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class EhCacheEvictEventListner {

    private final EhCacheEvictWorker ehCacheEvictWorker;

    /**
     * 캐시 제거 이벤트를 처리한다.
     *
     * @param event 처리할 이벤트 객체
     */
    @EventListener
    public void handleEhCacheEvictEvent(final EhCacheEvictEvent event) throws Exception {
        log.debug("EhCacheEvictEventListner.handleEhCacheEvictEvent() - event : {}", event.toString());

        ehCacheEvictWorker.handle(event);
    }
}
