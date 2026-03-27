package io.nicheblog.dreamdiary.feature.jrnl._shared.handler;

import io.nicheblog.dreamdiary.feature.clsf.ContentType;
import io.nicheblog.dreamdiary.feature.jrnl._shared.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.feature.jrnl.day.service.strategy.JrnlDayCacheEvictor;
import io.nicheblog.dreamdiary.feature.jrnl.diary.service.strategy.JrnlDiaryCacheEvictor;
import io.nicheblog.dreamdiary.feature.jrnl.dream.service.strategy.JrnlDreamCacheEvictor;
import io.nicheblog.dreamdiary.feature.jrnl.entry.service.strategy.JrnlEntryCacheEvictor;
import io.nicheblog.dreamdiary.feature.jrnl.intrpt.service.strategy.JrnlIntrptCacheEvictor;
import io.nicheblog.dreamdiary.feature.jrnl.sumry.service.strategy.JrnlSumryCacheEvictor;
import io.nicheblog.dreamdiary.feature.jrnl.sumry.service.strategy.JrnlSumryReviewCacheEvictor;
import io.nicheblog.dreamdiary.feature.jrnl.todo.service.strategy.JrnlTodoCacheEvictor;
import io.nicheblog.dreamdiary.global.util.TransactionHookUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * JrnlCacheEvictWorker
 * <pre>
 *  저널 캐시 제거 처리 Worker
 * </pre>
 *
 * @author nichefish
 **/
@Component
@RequiredArgsConstructor
@Log4j2
public class JrnlCacheEvictWorker {

    private final JrnlDayCacheEvictor jrnlDayCacheEvictor;
    private final JrnlEntryCacheEvictor jrnlEntryCacheEvictor;
    private final JrnlDiaryCacheEvictor jrnlDiaryCacheEvictor;
    private final JrnlDreamCacheEvictor jrnlDreamCacheEvictor;
    private final JrnlIntrptCacheEvictor jrnlIntrptCacheEvictor;
    private final JrnlTodoCacheEvictor jrnlTodoCacheEvictor;
    private final JrnlSumryCacheEvictor jrnlSumryCacheEvictor;
    private final JrnlSumryReviewCacheEvictor jrnlSumryReviewCacheEvictor;

    // CacheEvictor를 매핑하는 Map
    private final Map<ContentType, JrnlCacheEvictor> evictorMap = new HashMap<>();

    @PostConstruct
    private void initEvictorMap() {
        evictorMap.put(ContentType.JRNL_DAY, jrnlDayCacheEvictor);
        evictorMap.put(ContentType.JRNL_ENTRY, jrnlEntryCacheEvictor);
        evictorMap.put(ContentType.JRNL_DIARY, jrnlDiaryCacheEvictor);
        evictorMap.put(ContentType.JRNL_DREAM, jrnlDreamCacheEvictor);
        evictorMap.put(ContentType.JRNL_INTRPT, jrnlIntrptCacheEvictor);
        evictorMap.put(ContentType.JRNL_TODO, jrnlTodoCacheEvictor);
        evictorMap.put(ContentType.JRNL_SUMRY, jrnlSumryCacheEvictor);
        evictorMap.put(ContentType.JRNL_SUMRY_REVIEW, jrnlSumryReviewCacheEvictor);
        validateEvictorMap();
    }

    /**
     * 전략 validation
     */
    private void validateEvictorMap() {
        final Set<ContentType> requiredTypes = EnumSet.of(
                ContentType.JRNL_DAY,
                ContentType.JRNL_ENTRY,
                ContentType.JRNL_DIARY,
                ContentType.JRNL_DREAM,
                ContentType.JRNL_INTRPT,
                ContentType.JRNL_TODO,
                ContentType.JRNL_SUMRY,
                ContentType.JRNL_SUMRY_REVIEW
        );

        for (final ContentType requiredType : requiredTypes) {
            if (!evictorMap.containsKey(requiredType) || evictorMap.get(requiredType) == null) {
                throw new IllegalStateException("Missing Jrnl CacheEvictor mapping for ContentType: " + requiredType);
            }
        }
    }

    /**
     * 태그 처리
     *
     * @param param 캐시 삭제 파라미터 객체
     */
    public void evictAfterCommit(final JrnlCacheEvictParam param, final ContentType contentType) throws Exception {
        if (param == null || contentType == null || ContentType.DEFAULT.equals(contentType)) return;

        final SecurityContext capturedSecurityContext = SecurityContextHolder.getContext();
        TransactionHookUtils.runAfterCommitOrNow(
                () -> this.evict(capturedSecurityContext, param, contentType),
                e -> log.error("Journal cache invalidation failed [{}:{}]: {}", contentType, param.getPostNo(), e.getMessage(), e)
        );
    }

    /**
     * evict
     * @param securityContext SecurityContext
     * @param cacheEvictParam JrnlCacheEvictParam
     * @param contentType ContentType
     */
    public void evict(
            final SecurityContext securityContext,
            final JrnlCacheEvictParam cacheEvictParam,
            final ContentType contentType
    ) throws Exception {
        final SecurityContext previousSecurityContext = SecurityContextHolder.getContext();
        try {
            SecurityContextHolder.setContext(securityContext);

            final JrnlCacheEvictor evictor = evictorMap.get(contentType);
            if (evictor == null) {
                log.warn("No CacheEvictor found for ContentType: {}", contentType);
                return;
            }
            evictor.evict(cacheEvictParam);
        } finally {
            SecurityContextHolder.setContext(previousSecurityContext);
        }
    }
}
