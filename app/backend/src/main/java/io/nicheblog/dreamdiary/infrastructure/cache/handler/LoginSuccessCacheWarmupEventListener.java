package io.nicheblog.dreamdiary.infrastructure.cache.handler;

import io.nicheblog.dreamdiary.global.config.AsyncConfig;
import io.nicheblog.dreamdiary.infrastructure.cache.event.LoginSuccessCacheWarmupEvent;
import io.nicheblog.dreamdiary.infrastructure.cache.service.CacheWarmupService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * LoginSuccessCacheWarmupEventListener
 * <pre>
 *  EhCache 캐시 제거 이벤트 처리 핸들러.
 * </pre>
 *
 * @author nichefish
 * @see AsyncConfig
 */
@Component
@RequiredArgsConstructor
public class LoginSuccessCacheWarmupEventListener {

    private final CacheWarmupService cacheWarmupService;

    /**
     * 캐시 웜업 이벤트를 처리한다.
     *
     * @param event 처리할 이벤트 객체
     */
    @EventListener
    @Async
    public void handleCacheWarmupEvent(final LoginSuccessCacheWarmupEvent event) throws Exception {
        SecurityContextHolder.setContext(event.getSecurityContext());
        final String username = event.getUsername();

        cacheWarmupService.warmupOnLogin(username);
    }
}
