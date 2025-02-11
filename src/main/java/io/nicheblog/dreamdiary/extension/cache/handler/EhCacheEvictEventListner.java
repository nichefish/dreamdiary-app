package io.nicheblog.dreamdiary.extension.cache.handler;

import io.nicheblog.dreamdiary.extension.cache.event.EhCacheEvictEvent;
import io.nicheblog.dreamdiary.extension.cache.service.CacheEvictService;
import io.nicheblog.dreamdiary.global.config.AsyncConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * EhCacheEvictEventListner
 * <pre>
 *  EhCache 캐시 제거 이벤트 처리 핸들러.
 * </pre>
 *
 * @author nichefish
 * @see AsyncConfig
 */
@Component
@RequiredArgsConstructor
public class EhCacheEvictEventListner {

    private final CacheEvictService ehCacheEvictService;

    /**
     * 캐시 제거 이벤트를 처리한다.
     * 삭제된 엔티티 재조회와 관련될 수 있으므로 별도 트랜잭션으로 처리.
     *
     * @param event 처리할 이벤트 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleEhCacheEvictvent(final EhCacheEvictEvent event) throws Exception {
        // 컨텐츠 타입별 캐시 evict
        ehCacheEvictService.evictClsfCache(event.getContentType(), event.getPostNo());
    }
}
