package io.nicheblog.dreamdiary.feature.jrnl.day.service.strategy;

import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.jrnl._shared.handler.JrnlCacheEvictor;
import io.nicheblog.dreamdiary.feature.jrnl._shared.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * JrnlDayCacheEvictor
 * <p>
 *  저널 일자 관련 캐시 evictor
 * </p>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class JrnlDayCacheEvictor
        implements JrnlCacheEvictor {

    /**
     * 해당 컨텐츠 타입 관련 캐시를 제거한다.
     * 
     * @param param 캐시 삭제 파라미터 객체
     */
    @Override
    @Transactional
    public void evict(final JrnlCacheEvictParam param) throws Exception {
        final ContentType refContentType = ContentType.JRNL_DAY;
        try {
            final String username = param.getRegstrId();
            final Integer id = param.getId();
            final Integer yy = param.getYy();
            final Integer mnth = param.getMnth();
            final String weekStartDt = param.getWeekStartDt();
            final String prevWeekStartDt = param.getPrevWeekStartDt();
            // jrnl_day
            EhCacheUtils.evictUserCacheByKey("jrnlDayDtlDtoByUser", username, id);
            this.evictMyJrnlDayYyMnthCaches(username, yy, mnth);
            this.evictMyJrnlDayWeeklyCaches(username, prevWeekStartDt, weekStartDt);
            // jrnl_day_tag
            EhCacheUtils.clearUserCache("jrnlDayTagCtgrMapByUser", username);
            EhCacheUtils.clearUserCache("jrnlDayYyMnthTagListByUser", username);
            EhCacheUtils.clearUserCache("jrnlDayWeeklyTagListByUser", username);
            EhCacheUtils.clearUserCache("jrnlDayYyMnthSizedTagListByUser", username);
            EhCacheUtils.clearUserCache("jrnlDayWeeklySizedTagListByUser", username);
            EhCacheUtils.clearUserCache("jrnlDayCountMapByUser", username);
            EhCacheUtils.evictCacheByKey("tagContentEntityListByRef", id + "_JRNL_DAY");
            // jrnl_day_meta
            EhCacheUtils.clearUserCache("jrnlDayMetaCtgrMapByUser", username);
            EhCacheUtils.evictCacheByKey("metaContentEntityListByRef", id + "_JRNL_DAY");
        } catch (final Exception e) {
            log.error("CacheEvictor error [{}]: {}", refContentType, e.getMessage(), e);
            throw e;
        }
    }
}
