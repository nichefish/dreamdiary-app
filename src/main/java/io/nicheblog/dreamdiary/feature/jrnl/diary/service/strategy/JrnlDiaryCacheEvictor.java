package io.nicheblog.dreamdiary.feature.jrnl.diary.service.strategy;

import io.nicheblog.dreamdiary.feature.clsf.ContentType;
import io.nicheblog.dreamdiary.feature.jrnl._shared.handler.JrnlCacheEvictor;
import io.nicheblog.dreamdiary.feature.jrnl._shared.model.JrnlCacheEvictParam;
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
        implements JrnlCacheEvictor {

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
            final String weekStartDt = param.getWeekStartDt();
            // jrnl_diary
            this.evictMyYyCacheByYyPrefix("jrnlDiaryYySumryStatedListByUser", yy);
            EhCacheUtils.evictMyCacheByKey("jrnlDiaryDtlDtoByUser", postNo);
            // jrnl_day
            if (jrnlDayNo != null) {
                EhCacheUtils.evictMyCacheByKey("jrnlDayDtlDtoByUser", jrnlDayNo);
            }
            this.evictMyJrnlDayYyMnthCaches(yy, mnth);
            this.evictMyJrnlDayWeeklyCaches(weekStartDt);
            // jrnl_diary_tag
            EhCacheUtils.clearMyCache("jrnlDiaryTagCtgrMapByUser");
            EhCacheUtils.clearMyCache("jrnlDiaryTagListByUser");
            EhCacheUtils.clearMyCache("jrnlDiaryYyMnthTagListByUser");
            EhCacheUtils.clearMyCache("jrnlDiaryWeeklyTagListByUser");
            EhCacheUtils.clearMyCache("jrnlDiaryYyMnthSizedTagListByUser");
            EhCacheUtils.clearMyCache("jrnlDiaryWeeklySizedTagListByUser");
            EhCacheUtils.clearMyCache("jrnlDiaryCountMapByUser");

            // 태그 캐시 처리
            if (postNo != null) {
                EhCacheUtils.evictCacheByKey("tagContentEntityListByRef", postNo + "_JRNL_DIARY");
            }
        } catch (final Exception e) {
            log.error("CacheEvictor error [{}]: {}", refContentType, e.getMessage(), e);
            throw e;
        }
    }
}
