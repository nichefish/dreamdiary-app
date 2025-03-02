package io.nicheblog.dreamdiary.domain.jrnl.diary.service.strategy;

import io.nicheblog.dreamdiary.domain.jrnl.diary.entity.JrnlDiaryContentTagEntity;
import io.nicheblog.dreamdiary.domain.jrnl.diary.entity.JrnlDiaryEntity;
import io.nicheblog.dreamdiary.domain.jrnl.diary.entity.JrnlDiaryTagEntity;
import io.nicheblog.dreamdiary.extension.cache.event.JrnlCacheEvictEvent;
import io.nicheblog.dreamdiary.extension.cache.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.extension.cache.service.CacheEvictor;
import io.nicheblog.dreamdiary.extension.cache.util.EhCacheUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
public class JrnlDiaryCacheEvictor
        implements CacheEvictor<JrnlCacheEvictEvent> {

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
        final Integer yy = param.getYy();
        final Integer mnth = param.getMnth();
        // jrnl_dream
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
    }
}
