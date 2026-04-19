package io.nicheblog.dreamdiary.infrastructure.cache.port;

import io.nicheblog.dreamdiary.infrastructure.cache.service.impl.EhCacheWarmupServiceImpl;

/**
 * LoginCacheWarmupTask
 * <pre>
 *  로그인시 캐시 워밍업 작업을 정의하는 인터페이스
 * </pre>
 *
 * @author nichefish
 * @see EhCacheWarmupServiceImpl
 */
public interface LoginCacheWarmupTask {

    /**
     * 로그인시 캐시 웜업
     */
    void warmupOnLogin(final String username) throws Exception;
}
