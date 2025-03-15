package io.nicheblog.dreamdiary.extension.cache.handler;

import io.nicheblog.dreamdiary.extension.cache.event.EhCacheEvictEvent;
import io.nicheblog.dreamdiary.extension.cache.service.CacheEvictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * EhCacheEvictEventListner
 * <pre>
 *  EhCache 캐시 제거 이벤트 처리 핸들러.
 *  데이터 정합성을 위해 동기 처리
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
public class EhCacheEvictEventListner {

    private final CacheEvictService ehCacheEvictService;

    /**
     * 캐시 제거 이벤트를 처리한다.
     *
     * @param event 처리할 이벤트 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEhCacheEvictEvent(final EhCacheEvictEvent event) throws Exception {
        // 컨텐츠 타입별 캐시 evict
        ehCacheEvictService.evictClsfCache(event.getContentType(), event.getPostNo());
    }
}
