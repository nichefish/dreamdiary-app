package io.nicheblog.dreamdiary.global._common.cache.handler;

import io.nicheblog.dreamdiary.global._common.cache.event.EhCacheEvictEvent;
import io.nicheblog.dreamdiary.global._common.cache.service.CacheEvictService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * EhCacheEvictEventListner
 * <pre>
 *  EhCache 캐시 제거 이벤트 처리 핸들러.
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
public class EhCacheEvictEventListner {

    private final CacheEvictService ehCacheEvictService;

    /**
     * 캐시 제거 이벤트를 처리한다.
     *
     * @param event 처리할 이벤트 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @EventListener
    public void handleEhCacheEvictvent(EhCacheEvictEvent event) throws Exception {
        // 조치자 추가
        ehCacheEvictService.evictClsfCache(event.getContentType(), event.getPostNo());
    }
}
