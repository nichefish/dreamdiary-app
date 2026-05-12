package io.nicheblog.dreamdiary.feature.journal.annual.service.strategy;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal._shared.handler.JournalCacheEvictor;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalCacheEvictParam;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * JournalAnnualReviewCacheEvictor
 * <p>
 *  저널 결산 리뷰 관련 캐시 evictor
 * </p>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class JournalAnnualReviewCacheEvictor
        implements JournalCacheEvictor {

    /**
     * 해당 컨텐츠 타입 관련 캐시를 제거한다.
     *
     * @param param 캐시 삭제 파라미터 객체
     */
    @Override
    @Transactional
    public void evict(final JournalCacheEvictParam param) throws Exception {
        final ContentType refContentType = ContentType.JOURNAL_ANNUAL_REVIEW;
        try {
            final String username = param.getCreatedBy();
            final Integer reviewId = param.getId();
            final Integer journalAnnualId = param.getJournalAnnualId();
            final Integer yy = param.getYy();

            // Summary detail caches affected by review add/update/delete.
            if (journalAnnualId != null) {
                EhCacheUtils.evictUserCacheByKey("journalAnnualDetailDtoByUser", username, journalAnnualId);
            }
            if (yy != null) {
                EhCacheUtils.evictUserCacheByKey("journalAnnualYyDetailDtoByUser", username, yy);
            }

            // Review tag cache.
            if (reviewId != null) {
                EhCacheUtils.evictCacheByKey("tagContentEntityListByRef", reviewId + "_JOURNAL_ANNUAL_REVIEW");
            }
        } catch (final Exception e) {
            log.error("CacheEvictor error [{}]: {}", refContentType, e.getMessage(), e);
            throw e;
        }
    }
}

