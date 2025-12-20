package io.nicheblog.dreamdiary.domain.jrnl.intrpt.service.strategy;

import io.nicheblog.dreamdiary.domain.jrnl.day.entity.JrnlDayEntity;
import io.nicheblog.dreamdiary.domain.jrnl.day.service.JrnlDayService;
import io.nicheblog.dreamdiary.domain.jrnl.dream.entity.JrnlDreamEntity;
import io.nicheblog.dreamdiary.domain.jrnl.dream.service.JrnlDreamService;
import io.nicheblog.dreamdiary.extension.cache.event.JrnlCacheEvictEvent;
import io.nicheblog.dreamdiary.extension.cache.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.extension.cache.service.CacheEvictor;
import io.nicheblog.dreamdiary.extension.cache.util.EhCacheUtils;
import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * JrnlIntrptCacheEvictor
 * <p>
 *  저널 해석 관련 캐시 evictor.
 * </p>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class JrnlIntrptCacheEvictor
        implements CacheEvictor<JrnlCacheEvictEvent> {

    private final JrnlDayService jrnlDayService;
    private final JrnlDreamService jrnlDreamService;

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
            final Integer jrnlDreamNo = param.getJrnlDreamNo();
            final JrnlDreamEntity jrnlDream = jrnlDreamService.getDtlEntity(jrnlDreamNo);
            final Integer jrnlDayNo = jrnlDream.getJrnlDayNo();
            final JrnlDayEntity jrnlDay = jrnlDayService.getDtlEntity(jrnlDayNo);
            final Integer yy = jrnlDay.getYy();
            final Integer mnth = jrnlDay.getMnth();
            // jrnl_diary
            EhCacheUtils.evictMyCacheAll("myJrnlIntrptList");
            EhCacheUtils.evictMyCache("myJrnlIntrptDtlDto", postNo);
            // jrnl_day
            EhCacheUtils.evictMyCache("myJrnlDayDtlDto", jrnlDayNo);
            this.evictMyCacheForPeriod("myJrnlDayList", yy, mnth);
            this.evictMyCacheForPeriod("myJrnlDayCalList", yy, mnth);
            // jrnl_diary_tag
            EhCacheUtils.evictMyCacheAll("myJrnlIntrptTagCtgrMap");
            EhCacheUtils.evictMyCacheAll("myJrnlIntrptTagDtl");
            // 태그 캐시 처리
            EhCacheUtils.evictCache("tagContentEntityListByRef", postNo + "_JRNL_INTRPT");
        } catch (final Exception e) {
            log.error("CacheEvictor error [{}]: {}", refContentType, e.getMessage(), e);
            throw e;
        }
    }
}
