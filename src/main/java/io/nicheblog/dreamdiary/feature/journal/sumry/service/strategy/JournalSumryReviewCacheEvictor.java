package io.nicheblog.dreamdiary.feature.journal.sumry.service.strategy;

import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal._shared.handler.JournalCacheEvictor;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalCacheEvictParam;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * JournalSumryReviewCacheEvictor
 * <p>
 *  저널 결산 리뷰 관련 캐시 evictor
 * </p>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class JournalSumryReviewCacheEvictor
        implements JournalCacheEvictor {

    /**
     * 해당 컨텐츠 타입 관련 캐시를 제거한다.
     *
     * @param param 캐시 삭제 파라미터 객체
     */
    @Override
    @Transactional
    public void evict(final JournalCacheEvictParam param) throws Exception {
        final ContentType refContentType = ContentType.JOURNAL_SUMRY_REVIEW;
        try {
            final String username = param.getCreatedBy();
            final Integer reviewId = param.getId();
            final Integer journalSumryId = param.getJournalSumryId();
            final Integer yy = param.getYy();

            // Summary detail caches affected by review add/update/delete.
            if (journalSumryId != null) {
                EhCacheUtils.evictUserCacheByKey("journalSumryDtlDtoByUser", username, journalSumryId);
            }
            if (yy != null) {
                EhCacheUtils.evictUserCacheByKey("journalSumryYyDtlDtoByUser", username, yy);
            }

            // Review tag cache.
            if (reviewId != null) {
                EhCacheUtils.evictCacheByKey("tagContentEntityListByRef", reviewId + "_JOURNAL_SUMRY_REVIEW");
            }
        } catch (final Exception e) {
            log.error("CacheEvictor error [{}]: {}", refContentType, e.getMessage(), e);
            throw e;
        }
    }
}

