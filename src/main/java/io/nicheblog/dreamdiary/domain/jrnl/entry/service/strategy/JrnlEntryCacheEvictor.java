package io.nicheblog.dreamdiary.domain.jrnl.entry.service.strategy;

import io.nicheblog.dreamdiary.domain.jrnl.day.entity.JrnlDayEntity;
import io.nicheblog.dreamdiary.domain.jrnl.day.service.JrnlDayService;
import io.nicheblog.dreamdiary.domain.jrnl.entry.entity.JrnlEntryContentTagEntity;
import io.nicheblog.dreamdiary.domain.jrnl.entry.entity.JrnlEntryEntity;
import io.nicheblog.dreamdiary.domain.jrnl.entry.entity.JrnlEntryTagEntity;
import io.nicheblog.dreamdiary.domain.jrnl.entry.service.JrnlEntryService;
import io.nicheblog.dreamdiary.extension.cache.event.JrnlCacheEvictEvent;
import io.nicheblog.dreamdiary.extension.cache.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.extension.cache.service.CacheEvictor;
import io.nicheblog.dreamdiary.extension.cache.util.EhCacheUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * JrnlEntryCacheEvictor
 * <p>
 *  저널 항목 관련 캐시 evictor.
 * </p>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
public class JrnlEntryCacheEvictor
        implements CacheEvictor<JrnlCacheEvictEvent> {

    private final JrnlDayService jrnlDayService;
    private final JrnlEntryService jrnlEntryService;

    /**
     * 해당 컨텐츠 타입 관련 캐시를 제거한다.
     *
     * @param event 캐시 삭제 이벤트 객체
     * @throws Exception 캐시 삭제 과정에서 발생할 수 있는 예외
     */
    @Override
    public void evict(final JrnlCacheEvictEvent event) throws Exception {
        final JrnlCacheEvictParam param = event.getCacheEvictParam();
        final Integer postNo = param.getPostNo();
        final Integer jrnlDayNo = param.getJrnlDayNo();
        final JrnlDayEntity jrnlDay = jrnlDayService.getDtlEntity(jrnlDayNo);
        final Integer yy = jrnlDay.getYy();
        final Integer mnth = jrnlDay.getMnth();
        // jrnl_day
        EhCacheUtils.evictMyCache("myJrnlDayDtlDto", jrnlDayNo);
        this.evictMyCacheForPeriod("myJrnlDayList", yy, mnth);
        this.evictMyCacheForPeriod("myJrnlDayCalList", yy, mnth);
        // jrnl_entry_tag
        EhCacheUtils.evictMyCacheAll("myJrnlEntryTagCtgrMap");
        EhCacheUtils.evictMyCacheAll("myJrnlEntryTagDtl");
        // 태그 처리
        EhCacheUtils.evictCache("contentTagEntityListByRef", postNo + "_JRNL_ENTRY");
        // L2캐시 처리
        EhCacheUtils.clearL2Cache(JrnlEntryEntity.class);
        EhCacheUtils.clearL2Cache(JrnlEntryTagEntity.class);
        EhCacheUtils.clearL2Cache(JrnlEntryContentTagEntity.class);
    }
}
