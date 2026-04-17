package io.nicheblog.dreamdiary.feature.journal._shared.handler;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalCacheEvictParam;
import io.nicheblog.dreamdiary.feature.journal.annual.service.strategy.JournalAnnualCacheEvictor;
import io.nicheblog.dreamdiary.feature.journal.annual.service.strategy.JournalAnnualReviewCacheEvictor;
import io.nicheblog.dreamdiary.feature.journal.chapter.service.strategy.JournalChapterCacheEvictor;
import io.nicheblog.dreamdiary.feature.journal.day.service.strategy.JournalDayCacheEvictor;
import io.nicheblog.dreamdiary.feature.journal.diary.service.strategy.JournalDiaryCacheEvictor;
import io.nicheblog.dreamdiary.feature.journal.dream.service.strategy.JournalDreamCacheEvictor;
import io.nicheblog.dreamdiary.feature.journal.intrpt.service.strategy.JournalIntrptCacheEvictor;
import io.nicheblog.dreamdiary.feature.journal.todo.service.strategy.JournalTodoCacheEvictor;
import io.nicheblog.dreamdiary.global.util.TransactionHookUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * JournalCacheEvictWorker
 * <pre>
 *  저널 캐시 제거 처리 Worker
 * </pre>
 *
 * @author nichefish
 **/
@Component
@RequiredArgsConstructor
@Log4j2
public class JournalCacheEvictWorker {

    private final JournalDayCacheEvictor journalDayCacheEvictor;
    private final JournalChapterCacheEvictor journalChapterCacheEvictor;
    private final JournalDiaryCacheEvictor journalDiaryCacheEvictor;
    private final JournalDreamCacheEvictor journalDreamCacheEvictor;
    private final JournalIntrptCacheEvictor journalIntrptCacheEvictor;
    private final JournalTodoCacheEvictor journalTodoCacheEvictor;
    private final JournalAnnualCacheEvictor journalAnnualCacheEvictor;
    private final JournalAnnualReviewCacheEvictor journalAnnualReviewCacheEvictor;

    // CacheEvictor를 매핑하는 Map
    private final Map<ContentType, JournalCacheEvictor> evictorMap = new HashMap<>();

    @PostConstruct
    private void initEvictorMap() {
        evictorMap.put(ContentType.JOURNAL_DAY, journalDayCacheEvictor);
        evictorMap.put(ContentType.JOURNAL_CHAPTER, journalChapterCacheEvictor);
        evictorMap.put(ContentType.JOURNAL_DIARY, journalDiaryCacheEvictor);
        evictorMap.put(ContentType.JOURNAL_DREAM, journalDreamCacheEvictor);
        evictorMap.put(ContentType.JOURNAL_INTRPT, journalIntrptCacheEvictor);
        evictorMap.put(ContentType.JOURNAL_TODO, journalTodoCacheEvictor);
        evictorMap.put(ContentType.JOURNAL_ANNUAL, journalAnnualCacheEvictor);
        evictorMap.put(ContentType.JOURNAL_ANNUAL_REVIEW, journalAnnualReviewCacheEvictor);
        validateEvictorMap();
    }

    /**
     * 전략 validation
     */
    private void validateEvictorMap() {
        final Set<ContentType> requiredTypes = EnumSet.of(
                ContentType.JOURNAL_DAY,
                ContentType.JOURNAL_CHAPTER,
                ContentType.JOURNAL_DIARY,
                ContentType.JOURNAL_DREAM,
                ContentType.JOURNAL_INTRPT,
                ContentType.JOURNAL_TODO,
                ContentType.JOURNAL_ANNUAL,
                ContentType.JOURNAL_ANNUAL_REVIEW
        );

        for (final ContentType requiredType : requiredTypes) {
            if (!evictorMap.containsKey(requiredType) || evictorMap.get(requiredType) == null) {
                throw new IllegalStateException("Missing Journal CacheEvictor mapping for ContentType: " + requiredType);
            }
        }
    }

    /**
     * 태그 처리
     *
     * @param param 캐시 삭제 파라미터 객체
     */
    public void evictAfterCommit(final JournalCacheEvictParam param, final ContentType contentType) throws Exception {
        if (param == null || contentType == null || ContentType.DEFAULT.equals(contentType)) return;

        TransactionHookUtils.runAfterCommitOrNow(
                () -> this.evict(param, contentType),
                e -> log.error("Journal cache invalidation failed [{}:{}]: {}", contentType, param.getId(), e.getMessage(), e)
        );
    }

    /**
     * evict
     * @param cacheEvictParam JournalCacheEvictParam
     * @param contentType ContentType
     */
    public void evict(
            final JournalCacheEvictParam cacheEvictParam,
            final ContentType contentType
    ) throws Exception {
        final JournalCacheEvictor evictor = evictorMap.get(contentType);
        if (evictor == null) {
            log.warn("No CacheEvictor found for ContentType: {}", contentType);
            return;
        }
        evictor.evict(cacheEvictParam);
    }
}

