package io.nicheblog.dreamdiary.feature.journal.entry.service.strategy;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal._shared.handler.JournalCacheEvictor;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalCacheEvictParam;
import io.nicheblog.dreamdiary.feature.journal.chapter.service.JournalChapterService;
import io.nicheblog.dreamdiary.feature.journal.entry.service.JournalEntryService;
import io.nicheblog.dreamdiary.feature.journal.entry.service.policy.JournalEntryTypePolicy;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Log4j2
public class JournalEntryCacheEvictor
        implements JournalCacheEvictor {

    /**
     * 엔트리 변경 시 관련 캐시를 타입별 규칙에 따라 무효화한다.
     *
     * @param param 캐시 무효화 파라미터
     * @throws Exception 캐시 처리 중 예외
     */
    @Override
    @Transactional
    public void evict(final JournalCacheEvictParam param) throws Exception {
        final ContentType contentType = ContentType.get(param.getContentType());
        if (!JournalEntryTypePolicy.isEntryType(contentType)) {
            log.warn("Unsupported journal entry cache eviction type: {}", param.getContentType());
            return;
        }

        try {
            final String username = param.getCreatedBy();
            final Integer id = param.getId();
            final Integer journalDayId = param.getJournalDayId();
            final Integer journalChapterId = param.getJournalChapterId();
            final Integer prevJournalChapterId = param.getPrevJournalChapterId();
            final Integer yy = param.getYy();
            final Integer mnth = param.getMnth();
            final String weekStartDt = param.getWeekStartDt();

            if (yy != null) {
                EhCacheUtils.evictUserCacheByPrefix(JournalEntryService.ANNUAL_STATED_LIST_CACHE_NAME, username, contentType.key + "_" + yy);
            } else {
                EhCacheUtils.clearUserCache(JournalEntryService.ANNUAL_STATED_LIST_CACHE_NAME, username);
            }
            EhCacheUtils.evictUserCacheByKey(JournalEntryService.DTL_CACHE_NAME, username, contentType.key + "_" + id);

            if (journalDayId != null) {
                EhCacheUtils.evictUserCacheByKey("journalDayDtlDtoByUser", username, journalDayId);
            }
            if (journalChapterId != null) {
                EhCacheUtils.evictUserCacheByKey(JournalChapterService.DETAIL_CACHE_NAME, username, journalChapterId);
            }
            if (prevJournalChapterId != null && !prevJournalChapterId.equals(journalChapterId)) {
                EhCacheUtils.evictUserCacheByKey(JournalChapterService.DETAIL_CACHE_NAME, username, prevJournalChapterId);
            }
            this.evictMyJournalDayYyMnthCaches(username, yy, mnth);
            this.evictMyJournalDayWeeklyCaches(username, weekStartDt);

            EhCacheUtils.clearUserCache("journalEntryTagCategoryMapByUser", username);
            EhCacheUtils.clearUserCache("journalEntryTagListByUser", username);
            EhCacheUtils.clearUserCache("journalEntryPeriodTagListByUser", username);
            EhCacheUtils.clearUserCache("journalEntryTagCountMapByUser", username);

            if (id != null) {
                EhCacheUtils.evictCacheByKey("tagContentEntityListByRef", id + "_" + contentType.key);
            }
        } catch (final Exception e) {
            log.error("CacheEvictor error [{}]: {}", contentType, e.getMessage(), e);
            throw e;
        }
    }

}
