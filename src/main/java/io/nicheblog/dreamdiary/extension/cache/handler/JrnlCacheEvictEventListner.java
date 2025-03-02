package io.nicheblog.dreamdiary.extension.cache.handler;

import io.nicheblog.dreamdiary.domain.jrnl.day.service.strategy.JrnlDayCacheEvictor;
import io.nicheblog.dreamdiary.domain.jrnl.diary.service.strategy.JrnlDiaryCacheEvictor;
import io.nicheblog.dreamdiary.domain.jrnl.dream.service.strategy.JrnlDreamCacheEvictor;
import io.nicheblog.dreamdiary.domain.jrnl.sumry.service.strategy.JrnlSumryCacheEvictor;
import io.nicheblog.dreamdiary.extension.cache.event.JrnlCacheEvictEvent;
import io.nicheblog.dreamdiary.extension.cache.service.CacheEvictor;
import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * JrnlCacheEvictEventListner
 * <pre>
 *  저널 캐시 제거 이벤트 처리 핸들러.
 *  데이터 정합성을 위해 동기 처리
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class JrnlCacheEvictEventListner {

    private final JrnlDayCacheEvictor jrnlDayCacheEvictor;
    private final JrnlDiaryCacheEvictor jrnlDiaryCacheEvictor;
    private final JrnlDreamCacheEvictor jrnlDreamCacheEvictor;
    private final JrnlSumryCacheEvictor jrnlSumryCacheEvictor;

    // CacheEvictor를 매핑하는 Map
    private final Map<String, CacheEvictor<JrnlCacheEvictEvent>> evictorMap = new HashMap<>();

    @PostConstruct
    private void initEvictorMap() {
        evictorMap.put(ContentType.JRNL_DAY.key, jrnlDayCacheEvictor);
        evictorMap.put(ContentType.JRNL_DIARY.key, jrnlDiaryCacheEvictor);
        evictorMap.put(ContentType.JRNL_DREAM.key, jrnlDreamCacheEvictor);
        evictorMap.put(ContentType.JRNL_SUMRY.key, jrnlSumryCacheEvictor);
    }

    /**
     * 저널 캐시 제거 이벤트를 처리한다.
     *
     * @param event 처리할 이벤트 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @EventListener
    public void handleEhCacheEvictvent(final JrnlCacheEvictEvent event) throws Exception {
        final String refContentType = event.getContentType().key;
        final CacheEvictor<JrnlCacheEvictEvent> evictor = evictorMap.get(refContentType);
        if (evictor == null) {
            log.warn("No CacheEvictor found for ContentType: {}", refContentType);
            return;
        }
        evictor.evict(event);
    }
}
