package io.nicheblog.dreamdiary.feature.jrnl.diary.service.strategy;

import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
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
            final String userId = param.getRegstrId();
            final Integer postNo = param.getPostNo();
            final Integer jrnlDayNo = param.getJrnlDayNo();
            final Integer yy = param.getYy();
            final Integer mnth = param.getMnth();
            final String weekStartDt = param.getWeekStartDt();
            // jrnl_diary
            this.evictMyYyCacheByYyPrefix(userId, "jrnlDiaryYySumryStatedListByUser", yy);
            EhCacheUtils.evictUserCacheByKey("jrnlDiaryDtlDtoByUser", userId, postNo);
            // jrnl_day
            if (jrnlDayNo != null) {
                EhCacheUtils.evictUserCacheByKey("jrnlDayDtlDtoByUser", userId, jrnlDayNo);
            }
            this.evictMyJrnlDayYyMnthCaches(userId, yy, mnth);
            this.evictMyJrnlDayWeeklyCaches(userId, weekStartDt);
            // jrnl_diary_tag
            EhCacheUtils.clearUserCache("jrnlDiaryTagCtgrMapByUser", userId);
            EhCacheUtils.clearUserCache("jrnlDiaryTagListByUser", userId);
            EhCacheUtils.clearUserCache("jrnlDiaryYyMnthTagListByUser", userId);
            EhCacheUtils.clearUserCache("jrnlDiaryWeeklyTagListByUser", userId);
            EhCacheUtils.clearUserCache("jrnlDiaryYyMnthSizedTagListByUser", userId);
            EhCacheUtils.clearUserCache("jrnlDiaryWeeklySizedTagListByUser", userId);
            EhCacheUtils.clearUserCache("jrnlDiaryCountMapByUser", userId);

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
