package io.nicheblog.dreamdiary.extension.cache.handler;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.extension.cache.event.EhCacheEvictEvent;
import io.nicheblog.dreamdiary.extension.cache.service.CacheWarmupService;
import io.nicheblog.dreamdiary.global.config.AsyncConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * LgnSuccessCacheWarmupEventListener
 * <pre>
 *  EhCache 캐시 제거 이벤트 처리 핸들러.
 * </pre>
 *
 * @author nichefish
 * @see AsyncConfig
 */
@Component
@RequiredArgsConstructor
public class LgnSuccessCacheWarmupEventListener {

    private final CacheWarmupService cacheWarmupService;

    /**
     * 캐시 웜업 이벤트를 처리한다.
     *
     * @param event 처리할 이벤트 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @EventListener
    @Async
    public void handleCacheWarmupEvent(final EhCacheEvictEvent event) throws Exception {
        SecurityContextHolder.setContext(event.getSecurityContext());
        final String userId = AuthUtils.getLgnUserId();

        cacheWarmupService.warmupOnLgn(userId);
    }
}
