package io.nicheblog.dreamdiary.feature.journal.day.service.strategy;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal._shared.handler.JournalCacheEvictor;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalCacheEvictParam;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * JournalDayCacheEvictor
 * <p>
 *  저널 일자 관련 캐시 evictor
 * </p>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class JournalDayCacheEvictor
        implements JournalCacheEvictor {

    /**
     * 해당 컨텐츠 타입 관련 캐시를 제거한다.
     * 
     * @param param 캐시 삭제 파라미터 객체
     */
    @Override
    @Transactional
    public void evict(final JournalCacheEvictParam param) throws Exception {
        final ContentType refContentType = ContentType.JOURNAL_DAY;
        try {
            final String username = param.getCreatedBy();
            final Integer id = param.getId();
            final Integer yy = param.getYy();
            final Integer mnth = param.getMnth();
            final String weekStartDt = param.getWeekStartDt();
            final String prevWeekStartDt = param.getPrevWeekStartDt();
            // journal_day
            EhCacheUtils.evictUserCacheByKey("journalDayDtlDtoByUser", username, id);
            this.evictMyJournalDayYyMnthCaches(username, yy, mnth);
            this.evictMyJournalDayWeeklyCaches(username, prevWeekStartDt, weekStartDt);
            // journal_day_tag
            EhCacheUtils.clearUserCache("journalDayTagCategoryMapByUser", username);
            EhCacheUtils.clearUserCache("journalDayPeriodTagListByUser", username);
            EhCacheUtils.clearUserCache("journalDayTagCountMapByUser", username);
            EhCacheUtils.evictCacheByKey("tagContentEntityListByRef", id + "_JOURNAL_DAY");
            // journal_day_meta
            EhCacheUtils.clearUserCache("journalDayMetaCategoryMapByUser", username);
            EhCacheUtils.evictCacheByKey("metaContentEntityListByRef", id + "_JOURNAL_DAY");
        } catch (final Exception e) {
            log.error("CacheEvictor error [{}]: {}", refContentType, e.getMessage(), e);
            throw e;
        }
    }
}
