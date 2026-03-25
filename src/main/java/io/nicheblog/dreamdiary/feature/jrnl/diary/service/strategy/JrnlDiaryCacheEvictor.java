package io.nicheblog.dreamdiary.feature.jrnl.diary.service.strategy;

import io.nicheblog.dreamdiary.feature.clsf.ContentType;
import io.nicheblog.dreamdiary.feature.jrnl._shared.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.infrastructure.cache.service.CacheEvictor;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
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
        implements CacheEvictor<JrnlCacheEvictParam> {

    /**
     * 해당 컨텐츠 타입 관련 캐시를 제거한다.
     *
     * @param param 캐시 삭제 파라미터 객체
     */
    @Override
    @Transactional
    public void evict(final JrnlCacheEvictParam param) throws Exception {
        final ContentType refContentType = ContentType.JRNL_DIARY;
        try {
            final Integer postNo = param.getPostNo();
            final Integer jrnlDayNo = param.getJrnlDayNo();
            final Integer yy = param.getYy();
            final Integer mnth = param.getMnth();
            // jrnl_diary
            EhCacheUtils.evictMyCacheAll("myJrnlDiaryList");
            EhCacheUtils.evictMyCacheAll("mySumryDiaryList");
            EhCacheUtils.evictMyCache("myJrnlDiaryDtlDto", postNo);
            // jrnl_day
            if (jrnlDayNo != null) {
                EhCacheUtils.evictMyCache("myJrnlDayDtlDto", jrnlDayNo);
            }
            if (yy != null && mnth != null) {
                this.evictMyCacheForPeriod("myJrnlDayList", yy, mnth);
                this.evictMyCacheForPeriod("myJrnlDayCalList", yy, mnth);
            } else {
                EhCacheUtils.evictMyCacheAll("myJrnlDayList");
                EhCacheUtils.evictMyCacheAll("myJrnlDayCalList");
            }
            // jrnl_diary_tag
            EhCacheUtils.evictMyCacheAll("myJrnlDiaryTagCtgrMap");
            EhCacheUtils.evictMyCacheAll("myJrnlDiaryTagList");

            // 태그 캐시 처리
            if (postNo != null) {
                EhCacheUtils.evictCache("tagContentEntityListByRef", postNo + "_JRNL_DIARY");
            }
        } catch (final Exception e) {
            log.error("CacheEvictor error [{}]: {}", refContentType, e.getMessage(), e);
            throw e;
        }
    }
}
