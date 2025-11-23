package io.nicheblog.dreamdiary.extension.cache.handler;

import io.nicheblog.dreamdiary.extension.cache.event.EhCacheEvictEvent;
import io.nicheblog.dreamdiary.extension.cache.service.CacheEvictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * EhCacheEvictWorker
 * <pre>
 *  EhCache 캐시 제거 Worker
 * </pre>
 *
 * @author nichefish
 * @see EhCacheEvictEventListner
 **/
@Component
@RequiredArgsConstructor
@Log4j2
public class EhCacheEvictWorker {

    private final CacheEvictService ehCacheEvictService;

    /**
     * 캐시 evict 처리
     *
     * @param event TagProcEvent
     */
    @Transactional
    public void handle(final EhCacheEvictEvent event) throws Exception {
        // 컨텐츠 타입별 캐시 evict
        ehCacheEvictService.evictClsfCache(event.getContentType(), event.getPostNo());
    }
}
