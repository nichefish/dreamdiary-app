package io.nicheblog.dreamdiary.extension.cache.handler;

import io.nicheblog.dreamdiary.domain.jrnl.day.service.strategy.JrnlDayCacheEvictor;
import io.nicheblog.dreamdiary.domain.jrnl.diary.service.strategy.JrnlDiaryCacheEvictor;
import io.nicheblog.dreamdiary.domain.jrnl.dream.service.strategy.JrnlDreamCacheEvictor;
import io.nicheblog.dreamdiary.domain.jrnl.entry.service.strategy.JrnlEntryCacheEvictor;
import io.nicheblog.dreamdiary.domain.jrnl.sumry.service.strategy.JrnlSumryCacheEvictor;
import io.nicheblog.dreamdiary.domain.jrnl.todo.service.strategy.JrnlTodoCacheEvictor;
import io.nicheblog.dreamdiary.extension.cache.event.JrnlCacheEvictEvent;
import io.nicheblog.dreamdiary.extension.cache.service.CacheEvictor;
import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * JrnlCacheEvictWorker
 * <pre>
 *  저널 캐시 제거 처리 Worker
 * </pre>
 *
 * @author nichefish
 * @see JrnlCacheEvictEventListner
 **/
@Component
@RequiredArgsConstructor
@Log4j2
public class JrnlCacheEvictWorker {

    private final JrnlDayCacheEvictor jrnlDayCacheEvictor;
    private final JrnlEntryCacheEvictor jrnlEntryCacheEvictor;
    private final JrnlDiaryCacheEvictor jrnlDiaryCacheEvictor;
    private final JrnlDreamCacheEvictor jrnlDreamCacheEvictor;
    private final JrnlTodoCacheEvictor jrnlTodoCacheEvictor;
    private final JrnlSumryCacheEvictor jrnlSumryCacheEvictor;

    // CacheEvictor를 매핑하는 Map
    private final Map<String, CacheEvictor<JrnlCacheEvictEvent>> evictorMap = new HashMap<>();

    @PostConstruct
    private void initEvictorMap() {
        evictorMap.put(ContentType.JRNL_DAY.key, jrnlDayCacheEvictor);
        evictorMap.put(ContentType.JRNL_ENTRY.key, jrnlEntryCacheEvictor);
        evictorMap.put(ContentType.JRNL_DIARY.key, jrnlDiaryCacheEvictor);
        evictorMap.put(ContentType.JRNL_DREAM.key, jrnlDreamCacheEvictor);
        evictorMap.put(ContentType.JRNL_TODO.key, jrnlTodoCacheEvictor);
        evictorMap.put(ContentType.JRNL_SUMRY.key, jrnlSumryCacheEvictor);
    }

    /**
     * 태그 처리
     *
     * @param event TagProcEvent
     */
    @Transactional
    public void handle(final JrnlCacheEvictEvent event) throws Exception {
        final String refContentType = event.getContentType().key;
        final CacheEvictor<JrnlCacheEvictEvent> evictor = evictorMap.get(refContentType);
        if (evictor == null) {
            log.warn("No CacheEvictor found for ContentType: {}", refContentType);
            return;
        }
        evictor.evict(event);
    }
}
