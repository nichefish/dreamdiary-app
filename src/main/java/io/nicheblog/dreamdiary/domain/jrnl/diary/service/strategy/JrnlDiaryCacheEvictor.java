package io.nicheblog.dreamdiary.domain.jrnl.diary.service.strategy;

import io.nicheblog.dreamdiary.domain.jrnl.day.entity.JrnlDayEntity;
import io.nicheblog.dreamdiary.domain.jrnl.day.service.JrnlDayService;
import io.nicheblog.dreamdiary.domain.jrnl.diary.entity.JrnlDiaryContentTagEntity;
import io.nicheblog.dreamdiary.domain.jrnl.diary.entity.JrnlDiaryEntity;
import io.nicheblog.dreamdiary.domain.jrnl.diary.entity.JrnlDiaryTagEntity;
import io.nicheblog.dreamdiary.domain.jrnl.entry.entity.JrnlEntryEntity;
import io.nicheblog.dreamdiary.domain.jrnl.entry.service.JrnlEntryService;
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
 * JrnlDiaryCacheEvictor
 * <p>
 *  저널 일기 관련 캐시 evictor.
 * </p>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class JrnlDiaryCacheEvictor
        implements CacheEvictor<JrnlCacheEvictEvent> {

    private final JrnlDayService jrnlDayService;
    private final JrnlEntryService jrnlEntryService;

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
            final Integer jrnlEntryNo = param.getJrnlEntryNo();
            final JrnlEntryEntity jrnlEntry = jrnlEntryService.getDtlEntity(jrnlEntryNo);
            final Integer jrnlDayNo = jrnlEntry.getJrnlDayNo();
            final JrnlDayEntity jrnlDay = jrnlDayService.getDtlEntity(jrnlDayNo);
            final Integer yy = jrnlDay.getYy();
            final Integer mnth = jrnlDay.getMnth();
            // jrnl_diary
            EhCacheUtils.evictMyCacheAll("myJrnlDiaryList");
            EhCacheUtils.evictMyCache("myJrnlDiaryDtlDto", postNo);
            // jrnl_day
            EhCacheUtils.evictMyCache("myJrnlDayDtlDto", jrnlDayNo);
            this.evictMyCacheForPeriod("myJrnlDayList", yy, mnth);
            this.evictMyCacheForPeriod("myJrnlDayCalList", yy, mnth);
            // jrnl_diary_tag
            EhCacheUtils.evictMyCacheAll("myJrnlDiaryTagCtgrMap");
            EhCacheUtils.evictMyCacheAll("myJrnlDiaryTagDtl");
            // 태그 처리
            EhCacheUtils.evictCache("contentTagEntityListByRef", postNo + "_JRNL_DIARY");
            // L2캐시 처리
            EhCacheUtils.clearL2Cache(JrnlDiaryEntity.class);
            EhCacheUtils.clearL2Cache(JrnlDiaryTagEntity.class);
            EhCacheUtils.clearL2Cache(JrnlDiaryContentTagEntity.class);
        } catch (final Exception e) {
            log.error("CacheEvictor error [{}]: {}", refContentType, e.getMessage(), e);
            throw e;
        }
    }
}
