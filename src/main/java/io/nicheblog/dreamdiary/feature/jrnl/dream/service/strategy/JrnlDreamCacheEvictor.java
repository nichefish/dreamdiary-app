package io.nicheblog.dreamdiary.feature.jrnl.dream.service.strategy;

import io.nicheblog.dreamdiary.feature.clsf.ContentType;
import io.nicheblog.dreamdiary.feature.jrnl._shared.event.JrnlCacheEvictEvent;
import io.nicheblog.dreamdiary.feature.jrnl._shared.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.infrastructure.cache.service.CacheEvictor;
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
        implements CacheEvictor<JrnlCacheEvictEvent> {

    /**
     * 해당 컨텐츠 타입 관련 캐시를 제거한다.
     *
     * @param event 캐시 삭제 이벤트 객체
     */
    @Override
    @Transactional
    public void evict(final JrnlCacheEvictEvent event) throws Exception {
        final ContentType refContentType = event.getContentType();
        try {
            final JrnlCacheEvictParam param = event.getCacheEvictParam();
            final Integer postNo = param.getPostNo();
            final Integer jrnlDayNo = param.getJrnlDayNo();
            final Integer yy = param.getYy();
            final Integer mnth = param.getMnth();
            // jrnl_dream
            EhCacheUtils.evictMyCacheAll("myJrnlDreamList");
            EhCacheUtils.evictMyCacheAll("mySumryDreamList");
            EhCacheUtils.evictMyCache("myJrnlDreamDtlDto", postNo);
            // jrnl_day
            EhCacheUtils.evictMyCache("myJrnlDayDtlDto", jrnlDayNo);
            this.evictMyCacheForPeriod("myJrnlDayList", yy, mnth);
            this.evictMyCacheForPeriod("myJrnlDayCalList", yy, mnth);
            // jrnl_dream_tag
            EhCacheUtils.evictMyCacheAll("myJrnlDreamTagCtgrMap");
            EhCacheUtils.evictMyCacheAll("myJrnlDreamTagList");
            EhCacheUtils.evictMyCacheAll("myJrnlDreamTagDtl");
            // 태그 캐시 처리
            EhCacheUtils.evictCache("tagContentEntityListByRef", postNo + "_JRNL_DREAM");
        } catch (final Exception e) {
            log.error("CacheEvictor error [{}]: {}", refContentType, e.getMessage(), e);
            throw e;
        }
    }
}
