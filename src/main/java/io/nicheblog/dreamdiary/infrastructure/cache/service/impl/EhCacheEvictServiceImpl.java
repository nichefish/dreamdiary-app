package io.nicheblog.dreamdiary.infrastructure.cache.service.impl;

import io.nicheblog.dreamdiary.infrastructure.cache.service.CacheEvictService;
import io.nicheblog.dreamdiary.infrastructure.cache.service.CacheEvictor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * CacheEvictService
 * <pre>
 *  캐시 Evict 서비스 모듈
 *  (여기저기 반복되는 공통로직 분리 위한 클래스)
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class EhCacheEvictServiceImpl
        implements CacheEvictService {

    // private final JournalDayCacheEvictor journalDayCacheEvictor;
    // private final JournalDiaryCacheEvictor journalDiaryCacheEvictor;
    // private final JournalDreamCacheEvictor journalDreamCacheEvictor;
    // private final JournalAnnualCacheEvictor journalAnnualCacheEvictor;

    // CacheEvictor를 매핑하는 Map
    private final Map<String, CacheEvictor<Integer>> evictorMap = new HashMap<>();

    @PostConstruct
    private void initEvictorMap() {
        // evictorMap.put(ContentType.JOURNAL_DAY.key, journalDayCacheEvictor);
        // evictorMap.put(ContentType.JOURNAL_DIARY.key, journalDiaryCacheEvictor);
        // evictorMap.put(ContentType.JOURNAL_DREAM.key, journalDreamCacheEvictor);
        // evictorMap.put(ContentType.JOURNAL_ANNUAL.key, journalAnnualCacheEvictor);
    }

    /**
     * 관련 캐시 삭제
     * 
     * @param refContentType - 캐시를 삭제할 컨텐츠 타입
     * @param refId - 캐시를 삭제할 게시글 번호
     */
    @Override
    public void evictClsfCache(final String refContentType, final Integer refId) throws Exception {
        final CacheEvictor<Integer> evictor = evictorMap.get(refContentType);
        if (evictor == null) {
            log.warn("No CacheEvictor found for ContentType: {}", refContentType);
            return;
        }
        evictor.evict(refId);
    }
}

