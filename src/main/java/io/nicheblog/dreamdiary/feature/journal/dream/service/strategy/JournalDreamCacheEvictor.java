package io.nicheblog.dreamdiary.feature.journal.dream.service.strategy;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal._shared.handler.JournalCacheEvictor;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalCacheEvictParam;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * JournalDreamCacheEvictor
 * <p>
 *  저널 꿈 관련 캐시 evictor.
 * </p>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class JournalDreamCacheEvictor
        implements JournalCacheEvictor {

    /**
     * 해당 컨텐츠 타입 관련 캐시를 제거한다.
     *
     * @param param 캐시 삭제 파라미터 객체
     */
    @Override
    @Transactional
    public void evict(final JournalCacheEvictParam param) throws Exception {
        final ContentType refContentType = ContentType.JOURNAL_DREAM;
        try {
            final String username = param.getCreatedBy();
            final Integer id = param.getId();
            final Integer journalDayId = param.getJournalDayId();
            final Integer yy = param.getYy();
            final Integer mnth = param.getMnth();
            final String weekStartDt = param.getWeekStartDt();
            // journal_dream
            this.evictMyYyCacheByYyPrefix(username, "journalDreamYyAnnualStatedListByUser", yy);
            EhCacheUtils.evictUserCacheByKey("journalDreamDtlDtoByUser", username, id);
            // journal_day
            if (journalDayId != null) {
                EhCacheUtils.evictUserCacheByKey("journalDayDtlDtoByUser", username, journalDayId);
            }
            this.evictMyJournalDayYyMnthCaches(username, yy, mnth);
            this.evictMyJournalDayWeeklyCaches(username, weekStartDt);
            // journal_dream_tag
            EhCacheUtils.clearUserCache("journalDreamTagCtgrMapByUser", username);
            EhCacheUtils.clearUserCache("journalDreamTagListByUser", username);
            EhCacheUtils.clearUserCache("journalDreamYyMnthTagListByUser", username);
            EhCacheUtils.clearUserCache("journalDreamWeeklyTagListByUser", username);
            EhCacheUtils.clearUserCache("journalDreamYyMnthSizedTagListByUser", username);
            EhCacheUtils.clearUserCache("journalDreamWeeklySizedTagListByUser", username);
            EhCacheUtils.clearUserCache("journalDreamCountMapByUser", username);
            // 태그 캐시 처리
            EhCacheUtils.evictCacheByKey("tagContentEntityListByRef", id + "_JOURNAL_DREAM");
        } catch (final Exception e) {
            log.error("CacheEvictor error [{}]: {}", refContentType, e.getMessage(), e);
            throw e;
        }
    }
}

