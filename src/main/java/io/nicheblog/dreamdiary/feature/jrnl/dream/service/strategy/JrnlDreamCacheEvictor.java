package io.nicheblog.dreamdiary.feature.jrnl.dream.service.strategy;

import io.nicheblog.dreamdiary.feature.clsf.ContentType;
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
            final Integer postNo = param.getPostNo();
            final Integer jrnlDayNo = param.getJrnlDayNo();
            final Integer yy = param.getYy();
            final Integer mnth = param.getMnth();
            // jrnl_dream
            this.evictMyYyCacheByYyPrefix("jrnlDreamYySumryStatedListByUser", yy);
            EhCacheUtils.evictMyCacheByKey("jrnlDreamDtlDtoByUser", postNo);
            // jrnl_day
            if (jrnlDayNo != null) {
                EhCacheUtils.evictMyCacheByKey("jrnlDayDtlDtoByUser", jrnlDayNo);
            }
            this.evictMyJrnlDayYyMnthCaches(yy, mnth);
            // jrnl_dream_tag
            EhCacheUtils.clearMyCache("jrnlDreamTagCtgrMapByUser");
            EhCacheUtils.clearMyCache("jrnlDreamTagListByUser");
            EhCacheUtils.clearMyCache("jrnlDreamYyMnthTagListByUser");
            EhCacheUtils.clearMyCache("jrnlDreamYyMnthSizedTagListByUser");
            EhCacheUtils.clearMyCache("jrnlDreamCountMapByUser");
            // 태그 캐시 처리
            EhCacheUtils.evictCacheByKey("tagContentEntityListByRef", postNo + "_JRNL_DREAM");
        } catch (final Exception e) {
            log.error("CacheEvictor error [{}]: {}", refContentType, e.getMessage(), e);
            throw e;
        }
    }
}
