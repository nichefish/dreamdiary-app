package io.nicheblog.dreamdiary.feature.jrnl.day.service.strategy;

import io.nicheblog.dreamdiary.feature.clsf.ContentType;
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
            final Integer postNo = param.getPostNo();
            final Integer yy = param.getYy();
            final Integer mnth = param.getMnth();
            // jrnl_day
            EhCacheUtils.evictMyCacheByKey("jrnlDayDtlDtoByUser", postNo);
            this.evictMyJrnlDayYyMnthCaches(yy, mnth);
            // jrnl_day_tag
            EhCacheUtils.clearMyCache("jrnlDayTagCtgrMapByUser");
            EhCacheUtils.clearMyCache("jrnlDayYyMnthTagListByUser");
            EhCacheUtils.clearMyCache("jrnlDayYyMnthSizedTagListByUser");
            EhCacheUtils.clearMyCache("jrnlDayCountMapByUser");
            EhCacheUtils.evictCacheByKey("tagContentEntityListByRef", postNo + "_JRNL_DAY");
            // jrnl_day_meta
            EhCacheUtils.clearMyCache("jrnlDayMetaCtgrMapByUser");
            EhCacheUtils.evictCacheByKey("metaContentEntityListByRef", postNo + "_JRNL_DAY");
        } catch (final Exception e) {
            log.error("CacheEvictor error [{}]: {}", refContentType, e.getMessage(), e);
            throw e;
        }
    }
}
