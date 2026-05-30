package io.nicheblog.dreamdiary.infrastructure.cache.service;

/**
 * CacheEvictor
 * <pre>
 *   cacheEvictor 공통 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface CacheEvictor<T> {

    /**
     * 캐시 삭제
     *
     * @param object 캐시에서 삭제할 정보를 담은 파라미터
     */
    void evict(T object) throws Exception;
}
