package io.nicheblog.dreamdiary.feature.jrnl.dream.service.strategy;

import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.jrnl._shared.handler.JrnlCacheEvictor;
import io.nicheblog.dreamdiary.feature.jrnl._shared.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * JrnlDreamCacheEvictor
 * <p>
 *  저널 꿈 관련 캐시 evictor.
 * </p>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class JrnlDreamCacheEvictor
        implements JrnlCacheEvictor {

    /**
     * 해당 컨텐츠 타입 관련 캐시를 제거한다.
     *
     * @param param 캐시 삭제 파라미터 객체
     */
    @Override
    @Transactional
    public void evict(final JrnlCacheEvictParam param) throws Exception {
        final ContentType refContentType = ContentType.JRNL_DREAM;
        try {
            final String username = param.getCreatedBy();
            final Integer id = param.getId();
            final Integer jrnlDayId = param.getJrnlDayId();
            final Integer yy = param.getYy();
            final Integer mnth = param.getMnth();
            final String weekStartDt = param.getWeekStartDt();
            // jrnl_dream
            this.evictMyYyCacheByYyPrefix(username, "jrnlDreamYySumryStatedListByUser", yy);
            EhCacheUtils.evictUserCacheByKey("jrnlDreamDtlDtoByUser", username, id);
            // jrnl_day
            if (jrnlDayId != null) {
                EhCacheUtils.evictUserCacheByKey("jrnlDayDtlDtoByUser", username, jrnlDayId);
            }
            this.evictMyJrnlDayYyMnthCaches(username, yy, mnth);
            this.evictMyJrnlDayWeeklyCaches(username, weekStartDt);
            // jrnl_dream_tag
            EhCacheUtils.clearUserCache("jrnlDreamTagCtgrMapByUser", username);
            EhCacheUtils.clearUserCache("jrnlDreamTagListByUser", username);
            EhCacheUtils.clearUserCache("jrnlDreamYyMnthTagListByUser", username);
            EhCacheUtils.clearUserCache("jrnlDreamWeeklyTagListByUser", username);
            EhCacheUtils.clearUserCache("jrnlDreamYyMnthSizedTagListByUser", username);
            EhCacheUtils.clearUserCache("jrnlDreamWeeklySizedTagListByUser", username);
            EhCacheUtils.clearUserCache("jrnlDreamCountMapByUser", username);
            // 태그 캐시 처리
            EhCacheUtils.evictCacheByKey("tagContentEntityListByRef", id + "_JRNL_DREAM");
        } catch (final Exception e) {
            log.error("CacheEvictor error [{}]: {}", refContentType, e.getMessage(), e);
            throw e;
        }
    }
}
