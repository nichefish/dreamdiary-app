package io.nicheblog.dreamdiary.extension.cache.service;

/**
 * CacheWarmupService
 * <pre>
 *  캐시 웜업 서비스 인터페이스
 * </pre>
 *
 * @author nichefish
 */
public interface CacheWarmupService {

    /**
     * 캐시 웜업
     */
    void warmup() throws Exception;

    /**
     * 태그 카테고리 맵 팝업
     */
    void warmupTagCtgrMap() throws Exception;

    /**
     * 로그인시 웜업
     */
    void warmupOnLgn(final String userId) throws Exception;
}
