package io.nicheblog.dreamdiary.extension.cache.handler;

import io.nicheblog.dreamdiary.extension.cache.event.CacheWarmupEvent;
import io.nicheblog.dreamdiary.extension.cache.service.CacheWarmupService;
import io.nicheblog.dreamdiary.global.config.AsyncConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * CacheWarmupEventListner
 * <pre>
 *  캐시 웜업 이벤트 처리 핸들러.
 * </pre>
 *
 * @author nichefish
 * @see AsyncConfig
 */
@Component
@RequiredArgsConstructor
public class CacheWarmupEventListner {

    private final CacheWarmupService cacheWarmupService;

    /**
     * 캐시 웜업 이벤트를 처리한다.
     *
     * @param event 처리할 이벤트 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @EventListener
    @Async
    public void handleEhCacheEvictvent(final CacheWarmupEvent event) throws Exception {
        // 캐시 웜업 처리
        cacheWarmupService.warmup();
    }
}
