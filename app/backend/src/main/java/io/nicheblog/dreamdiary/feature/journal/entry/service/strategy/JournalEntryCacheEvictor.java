package io.nicheblog.dreamdiary.feature.journal.entry.service.strategy;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal._shared.handler.JournalCacheEvictor;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalCacheEvictParam;
import io.nicheblog.dreamdiary.feature.journal.chapter.service.JournalChapterService;
import io.nicheblog.dreamdiary.feature.journal.entry.service.JournalEntryService;
import io.nicheblog.dreamdiary.feature.journal.entry.service.policy.JournalEntryTagAxis;
import io.nicheblog.dreamdiary.feature.journal.entry.service.policy.JournalEntryTypePolicy;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 저널 엔트리 변경에 따른 화면·상세·집계 캐시 무효화 전략.
 * <p>
 * 일자·챕터·엔트리 캐시는 모든 지원 엔트리 타입에 적용하고, 태그 캐시는
 * {@link JournalEntryTagAxis#supportsTags(ContentType)}가 허용한 DIARY/DREAM 변경에만 적용한다.
 * 별도 Aggregate이며 태그를 소유하지 않는 Reflection 변경은 태그 캐시를 유지한다.
 * </p>
 */
@Component
@Log4j2
public class JournalEntryCacheEvictor
        implements JournalCacheEvictor {

    /**
     * 엔트리 변경 시 관련 캐시를 타입별 규칙에 따라 무효화한다.
     * <p>
     * 연도·일자·챕터 범위는 전달된 변경 문맥으로 좁히고, 범위를 확정할 수 없는 집계 캐시는
     * 해당 사용자 범위에서 정리한다. 태그 캐시 무효화 여부는 영속 콘텐츠 타입으로 결정한다.
     * </p>
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

            if (JournalEntryTagAxis.supportsTags(contentType)) {
                EhCacheUtils.clearUserCache("journalEntryTagCategoryMapByUser", username);
                EhCacheUtils.clearUserCache("journalEntryTagListByUser", username);
                EhCacheUtils.clearUserCache("journalEntryPeriodTagListByUser", username);
                EhCacheUtils.clearUserCache("journalEntryTagCountMapByUser", username);
            }

            if (id != null && JournalEntryTagAxis.supportsTags(contentType)) {
                EhCacheUtils.evictCacheByKey("tagContentEntityListByRef", id + "_" + contentType.key);
            }
        } catch (final Exception e) {
            log.error("CacheEvictor error [{}]: {}", contentType, e.getMessage(), e);
            throw e;
        }
    }

}
