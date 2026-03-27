package io.nicheblog.dreamdiary.infrastructure.cache.port;

import io.nicheblog.dreamdiary.infrastructure.cache.service.impl.EhCacheWarmupServiceImpl;

/**
 * CacheWarmupTask
 * <pre>
 *  캐시 워밍업 작업을 정의하는 인터페이스
 * </pre>
 *
 * @author nichefish
 * @see EhCacheWarmupServiceImpl
 */
public interface CacheWarmupTask {

    /**
     * 워밍업 실행
     */
    void warmup() throws Exception;
}
