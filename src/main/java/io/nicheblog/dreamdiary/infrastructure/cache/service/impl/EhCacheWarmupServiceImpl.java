package io.nicheblog.dreamdiary.infrastructure.cache.service.impl;

import io.nicheblog.dreamdiary.DreamdiaryInitializer;
import io.nicheblog.dreamdiary.infrastructure.cache.port.CacheWarmupTask;
import io.nicheblog.dreamdiary.infrastructure.cache.port.LoginCacheWarmupTask;
import io.nicheblog.dreamdiary.infrastructure.cache.service.CacheWarmupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * CacheWarmupService
 * <pre>
 *  캐시 웜업 서비스 모듈
 * </pre>
 *
 * @author nichefish
 * @see DreamdiaryInitializer
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class EhCacheWarmupServiceImpl
        implements CacheWarmupService {

    private final List<CacheWarmupTask> cacheWarmupTasks;
    private final List<LoginCacheWarmupTask> loginCacheWarmupTasks;

    /**
     * 캐시 웜업
     */
    @Override
    public void warmup() throws Exception {
        for (final CacheWarmupTask task : cacheWarmupTasks) {
            task.warmup();
        }
    }

    /**
     * 로그인시 캐시 웜업
     */
    @Override
    public void warmupOnLgn(final String username) throws Exception {
        for (final LoginCacheWarmupTask task : loginCacheWarmupTasks) {
            task.warmupOnLgn(username);
        }
    }
}
