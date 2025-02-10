package io.nicheblog.dreamdiary.extension.cache.service;

/**
 * CacheEvictService
 * <pre>
 *  캐시 Evict 서비스 모듈
 *  (여기저기 반복되는 공통로직 분리 위한 클래스)
 * </pre>
 *
 * @author nichefish
 */
public interface CacheEvictService {

    /**
     * 관련 캐시 삭제
     * 
     * @param refContentType - 캐시를 삭제할 컨텐츠 타입
     * @param refPostNo - 캐시를 삭제할 게시글 번호
     * @throws Exception 캐시 삭제 과정 중 발생할 수 있는 예외
     */
    void evictClsfCache(final String refContentType, final Integer refPostNo) throws Exception;
}